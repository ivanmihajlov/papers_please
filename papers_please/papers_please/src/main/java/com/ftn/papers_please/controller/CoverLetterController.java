package com.ftn.papers_please.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xmldb.api.modules.XMLResource;

import com.ftn.papers_please.service.CoverLetterService;
import com.ftn.papers_please.service.PublishingProcessService;

@RestController
@RequestMapping(value = "/api/coverLetters")
@CrossOrigin
public class CoverLetterController {

	@Autowired
	private CoverLetterService coverLetterService;

	@Autowired
	private PublishingProcessService publishingProcessService;

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> findOne(@PathVariable("id") String id) throws Exception {
		XMLResource resource = coverLetterService.findOne(id);
		return new ResponseEntity<>(resource.getContent().toString(), HttpStatus.OK);
	}

	@GetMapping(value = "/xml/{id}", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> findOneXml(@PathVariable("id") String id) throws Exception {
		XMLResource resource = coverLetterService.findOneXml(id);
		return new ResponseEntity<>(resource.getContent().toString(), HttpStatus.OK);
	}

	@GetMapping(value = "/html/{id}", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> findOneHtml(@PathVariable("id") String id) throws Exception {
		byte[] resource = coverLetterService.findOneHtml(id);
		return new ResponseEntity<>(new String(resource), HttpStatus.OK);
	}

	@GetMapping(value = "/pdf/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> findOnePdf(@PathVariable("id") String id) throws Exception {
		byte[] contents = coverLetterService.findOnePdf(id);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.add("Content-Disposition", "inline; filename=" + id + ".pdf");
		ResponseEntity<byte[]> response = new ResponseEntity<>(contents, headers, HttpStatus.OK);
		return response;
	}
	
	@PreAuthorize("hasRole('ROLE_AUTHOR')")
	@PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> add(@RequestParam(("processId")) String processId, @RequestBody String coverLetterXml) throws Exception {
		String coverLetterId = coverLetterService.save(coverLetterXml);
		publishingProcessService.addCoverLetter(processId, coverLetterId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
	
	@GetMapping(value = "/template", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getTemplate() throws Exception {
		return new ResponseEntity<>(coverLetterService.getTemplate(), HttpStatus.OK);
	}
	
}
