package com.ftn.papers_please.controller;

import java.io.IOException;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xmldb.api.modules.XMLResource;

import com.ftn.papers_please.service.PublishingProcessService;
import com.ftn.papers_please.dto.SearchData;
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
	
	@GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> findAll(@RequestParam(defaultValue = "") String searchText,
			@RequestParam(defaultValue = "") String title, @RequestParam(defaultValue = "") String author,
			@RequestParam(defaultValue = "") String affiliation, @RequestParam(defaultValue = "") String keyword,
			@RequestParam(defaultValue = "") String loggedAuthor,
			@RequestParam(required = false) Long acceptedFromDate, @RequestParam(required = false) Long acceptedToDate,
			@RequestParam(required = false) Long receivedFromDate, @RequestParam(required = false) Long receivedToDate
	) throws IOException {
		if (!loggedAuthor.equals(""))
			loggedAuthor = tokenUtils.getUsernameFromRequest(request);

		System.out.println("Title: " + title + " Author: " + author + " Affiliation: " + affiliation + " Keyword: "
				+ keyword + "Accepted From Date: " + acceptedFromDate + "Accepted To Date: " + acceptedToDate
				+ "Recieved from: " + receivedFromDate + "Recieved to: " + receivedToDate + " Logged author: "
				+ loggedAuthor);
		
		String retVal = "";
		if (title.equals("") && author.equals("") && affiliation.equals("") && keyword.equals("")
				&& acceptedFromDate == null && acceptedToDate == null && receivedFromDate == null && receivedToDate == null)
			retVal = paperService.getAll(searchText, loggedAuthor);
		else {
			SearchData searchData = new SearchData(title, author, affiliation, keyword,
					acceptedFromDate, acceptedToDate, receivedFromDate, receivedToDate);
			retVal = paperService.metadataSearch(searchData, loggedAuthor);
		}
		return new ResponseEntity<>(retVal, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_AUTHOR')")
	@PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> addPaper(@RequestBody String scientificPaperXml) throws Exception {
		String username = tokenUtils.getUsernameFromRequest(request);
		String paperId = paperService.save(scientificPaperXml, "1"); // 1 is the paper version
		String processId = publishingProcessService.createProcess(paperId, username);
		return new ResponseEntity<>(processId, HttpStatus.CREATED);
	}
	
	@GetMapping(value = "/metadata/xml/{id}", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getMetadataXml(@PathVariable("id") String id) throws Exception {
		String metadata = paperService.getMetadataXml(id);
		return new ResponseEntity<>(metadata, HttpStatus.OK);
	}

	@GetMapping(value = "/metadata/json/{id}", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getMetadataJson(@PathVariable("id") String id) throws Exception {
		String metadata = paperService.getMetadataJson(id);
		return new ResponseEntity<>(metadata, HttpStatus.OK);
	}
	
}
