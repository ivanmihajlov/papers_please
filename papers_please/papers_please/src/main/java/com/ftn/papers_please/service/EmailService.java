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
	
	public void assignedReviewerEmail(String reviewerEmail, String paperTitle, String scientificPaperId, String coverLetterId)
			throws MailException, InterruptedException {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(reviewerEmail);
		mail.setFrom(environment.getProperty(EMAIL_SENDER));
		mail.setSubject("Review assignment for scientific paper: " + paperTitle);
		mail.setText("You have been assigned to review \"" + 
				paperTitle + "\". You can accept or decline the review request." +
				"\n\nLinks:\n\n" + "Paper HTML: http://localhost:8088/api/scientificPapers/html/" + scientificPaperId
				+ "\nPaper PDF: http://localhost:8088/api/scientificPapers/pdf/" + scientificPaperId
				+ "\n\nCover letter HTML: http://localhost:8088/api/coverLetters/html/" + coverLetterId
				+ "\nCover letter PDF: http://localhost:8088/api/coverLetters/pdf/" + coverLetterId);
		javaMailSender.send(mail);
	}
	
	public void acceptedReviewEmail(String editorEmail, String reviewerName, String reviewerSurname, String paperTitle)
			throws MailException, InterruptedException {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(editorEmail);
		mail.setFrom(environment.getProperty(EMAIL_SENDER));
		mail.setSubject("Accepted review for paper \"" + paperTitle + "\"");
		mail.setText(reviewerName + " " + reviewerSurname + " accepted your review assignment for paper \"" + paperTitle + "\"");
		javaMailSender.send(mail);
	}
	
	public void rejectedReviewEmail(String editorEmail, String reviewerName, String reviewerSurname, String paperTitle)
			throws MailException, InterruptedException {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(editorEmail);
		mail.setFrom(environment.getProperty(EMAIL_SENDER));
		mail.setSubject("Rejected review for paper \"" + paperTitle + "\"");
		mail.setText(reviewerName + " " + reviewerSurname + " rejected your review assignment for paper \"" + paperTitle + "\"");
		javaMailSender.send(mail);
	}
	
	public void acceptedPaperEmail(String authorEmail, String paperTitle) throws MailException, InterruptedException {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(authorEmail);
		mail.setFrom(environment.getProperty(EMAIL_SENDER));
		mail.setSubject("Accepted paper submission for \"" + paperTitle + "\"");
		mail.setText("Your paper \"" + paperTitle + "\" has been accepted and is now published.\n\nThank you for using Papers, please!");
		javaMailSender.send(mail);
	}
	
	public void rejectedPaperEmail(String authorEmail, String paperTitle) throws MailException, InterruptedException {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(authorEmail);
		mail.setFrom(environment.getProperty(EMAIL_SENDER));
		mail.setSubject("Rejected paper submission for \"" + paperTitle + "\"");
		mail.setText("Sorry, your paper \"" + paperTitle + "\" has been rejected.");
		javaMailSender.send(mail);
	}
	
	public void newRevisionPaperEmail(String authorEmail, String paperTitle) throws MailException, InterruptedException {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(authorEmail);
		mail.setFrom(environment.getProperty(EMAIL_SENDER));
		mail.setSubject("Revision needed for \"" + paperTitle + "\"");
		mail.setText("Your paper \"" + paperTitle + "\" needs a revision.");
		javaMailSender.send(mail);
	}
	
}
