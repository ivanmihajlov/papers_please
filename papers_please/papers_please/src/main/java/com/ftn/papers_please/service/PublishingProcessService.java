package com.ftn.papers_please.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmldb.api.modules.XMLResource;

import com.ftn.papers_please.repository.PublishingProcessRepository;
import com.ftn.papers_please.util.DOMParser;
import com.ftn.papers_please.util.FileUtil;
import com.ftn.papers_please.util.XUpdateTemplate;
import com.ftn.papers_please.util.DBManager;
import com.ftn.papers_please.exceptions.CustomUnexpectedException;
import com.ftn.papers_please.model.publishing_process.PublishingProcess;
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
