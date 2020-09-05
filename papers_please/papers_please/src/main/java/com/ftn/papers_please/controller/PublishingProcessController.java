package com.ftn.papers_please.controller;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.papers_please.service.CustomUserDetailsService;
import com.ftn.papers_please.service.PublishingProcessService;
import com.ftn.papers_please.dto.PublishingProcessDTO;
import com.ftn.papers_please.model.publishing_process.PublishingProcess;
import com.ftn.papers_please.model.publishing_process.PublishingProcess.PaperVersion;
import com.ftn.papers_please.model.scientific_paper.ScientificPaper;
import com.ftn.papers_please.model.user.TUser;
import com.ftn.papers_please.mapper.PublishingProcessMapper;
import com.ftn.papers_please.security.TokenUtils;
import com.ftn.papers_please.service.EmailService;
import com.ftn.papers_please.service.PaperService;

@RestController
@RequestMapping(value = "/api/publishingProcess")
@CrossOrigin()
public class PublishingProcessController {

	@Autowired
	private PublishingProcessService publishingProcessService;

	@Autowired
	private PaperService paperService;

	@Autowired
	private CustomUserDetailsService userService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private PublishingProcessMapper mapper;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private TokenUtils tokenUtils;
	
	@GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> findOne(@RequestParam(("paperId")) String paperId) throws Exception {
		String resource = publishingProcessService.findOneByPaperId(paperId);
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}
	
	@PutMapping(value="/assignEditor/{processId}")
	@PreAuthorize("hasRole('ROLE_EDITOR')")
	public ResponseEntity<PublishingProcessDTO> assignPaperToEditor(@PathVariable("processId") String processId) {
		try {
			String username = tokenUtils.getUsernameFromRequest(request);
			TUser user = userService.findByUsername(username);
			publishingProcessService.assignEditor(processId, user.getUserId());
			PublishingProcess process = publishingProcessService.findOneUnmarshalled(processId);
			ScientificPaper scientificPaper = paperService.findOneUnmarshalled(process.getPaperVersion().get(process.getLatestVersion().intValue()-1).getScientificPaperId());
			PublishingProcessDTO publishingProcessDTO = mapper.toDTO(scientificPaper, process,process.getLatestVersion().intValue()-1);
			return new ResponseEntity<>(publishingProcessDTO, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping(value="/ongoing")
	@PreAuthorize("hasRole('ROLE_EDITOR')")
	public ResponseEntity<List<PublishingProcessDTO>> getPublicationsInOngoingProcess() {
		List<PublishingProcessDTO> result = new ArrayList<>();
		List<PublishingProcess> processes = publishingProcessService.getAll();
		
		for (PublishingProcess process : processes) {
			if (!isInOngoingProcess(process))
				continue;
			try {
                PaperVersion paperVersion = process.getPaperVersion().get(process.getLatestVersion().intValue()-1);
                if (paperVersion == null)
                    continue;
				ScientificPaper scientificPaper = paperService.findOneUnmarshalled(paperVersion.getScientificPaperId());
				PublishingProcessDTO publishingProcessDTO = mapper.toDTO(scientificPaper, process, process.getLatestVersion().intValue()-1);
				result.add(publishingProcessDTO);
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PutMapping(value="/status/{processId},{status}")
	@PreAuthorize("hasRole('ROLE_EDITOR')")
	public ResponseEntity<PublishingProcessDTO> updatePaperStatus(@PathVariable("processId") String processId, @PathVariable("status") String status) {
		try {
			if (!status.equalsIgnoreCase("ACCEPTED") && !status.equalsIgnoreCase("REJECTED") && !status.equalsIgnoreCase("NEW_REVISION"))
				return new ResponseEntity("Invalid paper status", HttpStatus.BAD_REQUEST);
			
            PublishingProcess process = publishingProcessService.findOneUnmarshalled(processId);
            if (!process.getStatus().equals("REVIEWS_DONE"))
				return new ResponseEntity("Paper reviews not done yet", HttpStatus.BAD_REQUEST);
			int paperVersion = process.getLatestVersion().intValue()-1;
			ScientificPaper scientificPaper = paperService.findOneUnmarshalled(process.getPaperVersion().get(paperVersion).getScientificPaperId());

        	process.setStatus(status);
        	if (status.equals("NEW_REVISION")) {
        	    BigInteger version = BigInteger.valueOf(process.getLatestVersion().intValue() + 1);
        	    process.setLatestVersion(version);
        	    scientificPaper.setVersion(version);
        	    scientificPaper.setStatus("REVISION");
        	    
        	    PaperVersion pv = new PaperVersion();
        	    pv.setScientificPaperId(scientificPaper.getId());
        	    pv.setCoverLetterId(process.getPaperVersion().get(paperVersion).getCoverLetterId());
        	    process.getPaperVersion().add(pv);
            } else if (status.equals("ACCEPTED")) {
        		scientificPaper.setAcceptedDate(new Date());
        		scientificPaper.setStatus(status);
			} else if (status.equals("REJECTED"))
        		scientificPaper.setStatus(status);

        	publishingProcessService.update(process);
        	paperService.update(scientificPaper);
			PublishingProcessDTO publishingProcessDTO = mapper.toDTO(scientificPaper, process, paperVersion);
			
			String authorId = process.getAuthorId();
			TUser author = userService.findByUsername(authorId);
			String authorEmail = author.getEmail();
			String paperTitle = scientificPaper.getHead().getTitle().get(0).getValue();

			switch (status) {
				case "ACCEPTED":
					try {
						emailService.acceptedPaperEmail(authorEmail, paperTitle);
					} catch (MailException | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case "REJECTED":
					try {
						emailService.rejectedPaperEmail(authorEmail, paperTitle);
					} catch (MailException | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case "NEW_REVISION":
					try {
						emailService.newRevisionPaperEmail(authorEmail, paperTitle);
					} catch (MailException | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				default:
				  return new ResponseEntity("Invalid paper status", HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>(publishingProcessDTO, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	private boolean isInOngoingProcess(PublishingProcess process) {
		String status = process.getStatus();
		if (status.equals("ACCEPTED") || status.equals("REJECTED") || status.equals("WITHDRAWN"))
			return false;
		return true;
	}
	
}
