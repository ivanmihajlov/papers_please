package com.ftn.papers_please.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	private Environment environment;
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	private static final String EMAIL_SENDER = "spring.mail.username";

	public void registrationEmail(String recipientEmail) throws MailException, InterruptedException {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(recipientEmail);
		mail.setFrom(environment.getProperty(EMAIL_SENDER));
		mail.setSubject("Welcome to Papers, please!");
		mail.setText("Your \"Papers, please!\" registration was successful. To start using the website go to http://localhost:4200/login");
		javaMailSender.send(mail);
	}
}
