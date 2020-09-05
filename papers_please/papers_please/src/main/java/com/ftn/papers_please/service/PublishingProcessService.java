package com.ftn.papers_please.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmldb.api.modules.XMLResource;

import com.ftn.papers_please.repository.PublishingProcessRepository;
import com.ftn.papers_please.util.DOMParser;
import com.ftn.papers_please.util.FileUtil;
import com.ftn.papers_please.util.XUpdateTemplate;
import com.ftn.papers_please.model.publishing_process.VersionReview;
import com.ftn.papers_please.model.scientific_paper.ScientificPaper;
import com.ftn.papers_please.model.user.TUser;
import com.ftn.papers_please.util.DBManager;
import com.ftn.papers_please.exceptions.CustomUnexpectedException;
import com.ftn.papers_please.model.publishing_process.PublishingProcess;
import com.ftn.papers_please.model.publishing_process.PublishingProcess.PaperVersion;
import com.ftn.papers_please.model.publishing_process.PublishingProcess.PaperVersion.VersionReviews;
import com.ftn.papers_please.repository.PaperRepository;

@Service
public class PublishingProcessService {

	@Value("${publishing-process-collection-id}")
	private String collectionId;
	
	@Value("${publishing-process-schema-path}")
	private String schemaPath;
	
	@Value("${publishing-process-template-path}")
	private String templatePath;
	
	@Autowired
	private PublishingProcessRepository publishingProcessRepository;

	@Autowired
	private PaperRepository paperRepository;
	
	@Autowired
	private DBManager dbManager;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	public List<PublishingProcess> getAll() {
		return  publishingProcessRepository.getAll();
	}
	
	public XMLResource findOne(String id) throws Exception {
		return publishingProcessRepository.findOne(id);
	}
	
	public String findOneByPaperId(String paperId) throws Exception {
		return publishingProcessRepository.findOneByPaperId(paperId);
	}

	public PublishingProcess findOneUnmarshalled(String id)  {
		return publishingProcessRepository.findOneUnmarshalled(id);
	}
	
	public String createProcess(String paperId, String authorId) throws Exception {
		String template = FileUtil.readFile(templatePath);
		Document document = DOMParser.buildDocument(template, schemaPath);
		
		// set publishing process ID
		String id = publishingProcessRepository.getNextId();
		NodeList nodeList = document.getElementsByTagName("publishing-process");
		Element paperElement = (Element) nodeList.item(0);
		paperElement.getAttributes().getNamedItem("id").setNodeValue(id);

		// set scientific paper ID
		NodeList paperIdNode = document.getElementsByTagName("scientific-paper-id");
		Element paperIdElement = (Element) paperIdNode.item(0);
		paperIdElement.setTextContent(paperId);

		// set author ID
		NodeList authorIdNode = document.getElementsByTagName("author-id");
		Element authorIdElement = (Element) authorIdNode.item(0);
		authorIdElement.setTextContent(authorId);

		// convert to string and save to the database
		String documentString = DOMParser.getStringFromDocument(document);
		publishingProcessRepository.save(documentString, id);
		return id;
	}
	
	public void addCoverLetter(String processId, String coverLetterId) throws Exception {
		String updatePath = "/publishing-process/paper-version[last()]/cover-letter-id";
		String xUpdateExpression =  String.format(XUpdateTemplate.UPDATE, updatePath, coverLetterId);
		dbManager.executeXUpdate(collectionId, xUpdateExpression, processId);
	}
	
	public void assignReviewer(PublishingProcess process, TUser user) {
		try {
			PaperVersion latestVersion = process.getPaperVersion().get(process.getLatestVersion().intValue()-1);
			VersionReview versionReview = new VersionReview();
			versionReview.setReviewerId(user.getUserId());
			versionReview.setReviewId(null);
			versionReview.setStatus("PENDING");

			if (latestVersion.getVersionReviews() == null) {
				VersionReviews reviews = new VersionReviews();
				reviews.getVersionReview().add(versionReview);
				latestVersion.setVersionReviews(reviews);
			} else
				latestVersion.getVersionReviews().getVersionReview().add(versionReview);

			if (checkIfEnoughReviewers(latestVersion.getVersionReviews()))
				process.setStatus("WAITING_FOR_REVIEWERS_ACCEPTANCE");

			publishingProcessRepository.update(process);
			
			try {
				String reviewerEmail = user.getEmail();
				String scientificPaperId = latestVersion.getScientificPaperId();
				String coverLetterId = latestVersion.getCoverLetterId();
				ScientificPaper scientificPaper = paperRepository.findOneUnmarshalled(scientificPaperId);
				String scientificPaperName = scientificPaper.getHead().getTitle().get(0).getValue();
				emailService.assignedReviewerEmail(reviewerEmail, scientificPaperName, scientificPaperId, coverLetterId);
			} catch (MailException | InterruptedException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			throw new CustomUnexpectedException("Error while assigning a reviewer!");
		}
    }
	
	public void acceptOrRejectReview(PublishingProcess process, String userId, boolean accepted) {
		try {
			PaperVersion latestVersion = process.getPaperVersion().get(process.getLatestVersion().intValue()-1);
			List<VersionReview> versionReviews = latestVersion.getVersionReviews().getVersionReview();

			for (VersionReview review : versionReviews) {
				if (review.getReviewerId().equals(userId) && review.getStatus().equals("PENDING")) {
					if (accepted)
						review.setStatus("ACCEPTED");
					else
						review.setStatus("REJECTED");
				}
			}

			if (accepted) {
				if (checkIfAllAccepted(latestVersion.getVersionReviews())) {
					process.setStatus("REVIEWS_ACCEPTED");
					String paperId = process.getPaperVersion().get(process.getLatestVersion().intValue()-1).getScientificPaperId();
					paperRepository.getById(paperId, "REVIEWING");
				}
			} else
				process.setStatus("NEW_REVIEWER_NEEDED");

			publishingProcessRepository.update(process);
			
			try {
				TUser reviewer = customUserDetailsService.findById(userId);
				String reviewerName = reviewer.getName();
				String reviewerSurname = reviewer.getSurname();
				String paperId = latestVersion.getScientificPaperId();
				ScientificPaper paper = paperRepository.findOneUnmarshalled(paperId);
				String paperTitle = paper.getHead().getTitle().get(0).getValue();
				String editorId = process.getEditorId();
				TUser editor = customUserDetailsService.findById(editorId);
				String editorEmail = editor.getEmail();
				if (accepted)
					emailService.acceptedReviewEmail(editorEmail, reviewerName, reviewerSurname, paperTitle);
				else
					emailService.rejectedReviewEmail(editorEmail, reviewerName, reviewerSurname, paperTitle);
			} catch (MailException | InterruptedException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			throw new CustomUnexpectedException("Unexpected error while accepting/rejecting the review");
		}
	}
	
	public void submitReview(PublishingProcess process, String userId, String reviewId) {
		try {
			PaperVersion latestVersion = process.getPaperVersion().get(process.getLatestVersion().intValue()-1);
			List<VersionReview> versionReviews = latestVersion.getVersionReviews().getVersionReview();

			for (VersionReview review : versionReviews) {
				if (review.getReviewerId().equals(userId) && review.getStatus().equals("ACCEPTED")) {
					review.setStatus("FINISHED");
					review.setReviewId(reviewId);
				}
			}

			if (checkIfAllReviewsFinished(latestVersion.getVersionReviews()))
				process.setStatus("REVIEWS_DONE");

			publishingProcessRepository.update(process);
		} catch (Exception e) {
			throw new CustomUnexpectedException("Error while submitting the review!");
		}
	}
	
	public void updateStatus(String processId, String status) {
		try {
			PublishingProcess publishingProcess = publishingProcessRepository.findOneUnmarshalled(processId);
			String paperId = publishingProcess.getPaperVersion().get(publishingProcess.getLatestVersion().intValue()-1).getScientificPaperId();
			
			paperRepository.updateStatus(paperId, status);
			publishingProcessRepository.updateStatus(processId, status);
		} catch (Exception e) {
			throw new CustomUnexpectedException("Error while updating publishing process status!");
		}
    }
	
	public void addNewPaperVersion(String processId, String paperVersionId) throws Exception{
		String updatePath = "/publishing-process/paper-version[last()]";
		String insertString = " <paper-version>\r\n" + 
				           "        <scientific-paper-id>" + paperVersionId +"</scientific-paper-id>\r\n" + 
				           "        <cover-letter-id></cover-letter-id>\r\n" + 
				           "    </paper-version>";
		
		String xUpdateExpression =  String.format(XUpdateTemplate.INSERT_AFTER, updatePath, insertString);
		dbManager.executeXUpdate(collectionId, xUpdateExpression, processId);
	}
	
	public List<PublishingProcess> getReviewsByStatus(String userId, String status) {
		List<PublishingProcess> allProcesses = publishingProcessRepository.getAll();
		List<PublishingProcess> result = new ArrayList<>();

		for (PublishingProcess process : allProcesses) {
			PaperVersion latestVersion = process.getPaperVersion().get(process.getLatestVersion().intValue()-1);
			VersionReviews reviews = latestVersion.getVersionReviews();
			if (reviews == null)
				continue;
			
			for (VersionReview review : reviews.getVersionReview())
				if (review.getReviewerId().equals(userId) && review.getStatus().equals(status))
					result.add(process);
		}	
		return result;
	}
	
	private boolean checkIfAllAccepted(VersionReviews versionReviews) {
		int accepted = 0;
		for (VersionReview review: versionReviews.getVersionReview())
			if (review.getStatus().equals("ACCEPTED"))
				accepted++;
		return accepted == 2;
	}
	
	private boolean checkIfEnoughReviewers(VersionReviews versionReviews) {
		int reviewers = 0;
		for (VersionReview review : versionReviews.getVersionReview())
			if (review.getStatus().equals("PENDING") || review.getStatus().equals("ACCEPTED") || review.getStatus().equals("FINISHED"))
				reviewers++;
		return reviewers == 2;
	}
	
	private boolean checkIfAllReviewsFinished(VersionReviews versionReviews) {
		int reviews = 0;
		for (VersionReview review : versionReviews.getVersionReview())
			if (review.getStatus().equals("FINISHED"))
				reviews++;
		return reviews >= 2;
	}
	
	public String getProcessStatus(String processId) throws Exception {
		return publishingProcessRepository.getProcessStatus(processId);
	}
	
	public String getAuthorFromProcess(String processId) throws Exception {
		return publishingProcessRepository.getAuthorFromProcess(processId);
	}
	
	public String getCoverLetterByPaperId(String paperId) throws Exception {
		return publishingProcessRepository.getCoverLetterByPaperId(paperId);
	}
	
	public void assignEditor(String processId, String userId) throws Exception {
		publishingProcessRepository.assignEditor(processId, userId);
	}
	
	public void update(PublishingProcess process) {
		publishingProcessRepository.save(process);
	}
	
}
