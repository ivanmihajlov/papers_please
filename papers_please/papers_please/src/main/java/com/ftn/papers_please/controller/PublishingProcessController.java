package com.ftn.papers_please.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.papers_please.service.CustomUserDetailsService;
import com.ftn.papers_please.service.PublishingProcessService;
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
	
}
