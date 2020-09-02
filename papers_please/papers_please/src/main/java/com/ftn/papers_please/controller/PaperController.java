package com.ftn.papers_please.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xmldb.api.modules.XMLResource;

import com.ftn.papers_please.service.PublishingProcessService;
import com.ftn.papers_please.security.TokenUtils;
import com.ftn.papers_please.service.PaperService;

@RestController
@RequestMapping(value = "/api/scientificPapers")
@CrossOrigin()
public class PaperController {

	@Autowired
	private PaperService paperService;

	@Autowired
	private PublishingProcessService publishingProcessService;
	
	@Autowired
	private TokenUtils tokenUtils;
	
	@Autowired
	private HttpServletRequest request;
	
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> findOne(@PathVariable("id") String id) throws Exception {
		XMLResource resource = paperService.findOne(id);
		return new ResponseEntity<>(resource.getContent().toString(), HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_AUTHOR')")
	@PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> addPaper(@RequestBody String scientificPaperXml) throws Exception {
		String username = tokenUtils.getUsernameFromRequest(request);
		String paperId = paperService.save(scientificPaperXml, "1"); // 1 is the paper version
		String processId = publishingProcessService.createProcess(paperId, username);
		return new ResponseEntity<>(processId, HttpStatus.CREATED);
	}
	
}
