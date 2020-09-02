package com.ftn.papers_please.dto;

import java.util.Date;

public class SearchData {
	
	private String title;
	private String author;
	private String affiliation;
	private String keyword;
	private Date acceptedFromDate;
	private Date acceptedToDate;
	private Date receivedFromDate;
	private Date receivedToDate;
	
	public SearchData() {
		super();
	}

	public SearchData(String title, String author, String affiliation, String keyword, Long acceptedFromDate,
			Long acceptedToDate, Long receivedFromDate, Long receivedToDate) {
		super();
		this.title = title;
		this.author = author;
		this.affiliation = affiliation;
		this.keyword = keyword;
		this.acceptedFromDate = acceptedFromDate == null ? null : new Date(acceptedFromDate);
		this.acceptedToDate = acceptedToDate == null ? null : new Date(acceptedToDate);
		this.receivedFromDate = receivedFromDate == null ? null : new Date(receivedFromDate);
		this.receivedToDate = receivedToDate == null ? null : new Date(receivedToDate);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Date getAcceptedFromDate() {
		return acceptedFromDate;
	}

	public void setAcceptedFromDate(Date acceptedFromDate) {
		this.acceptedFromDate = acceptedFromDate;
	}

	public Date getAcceptedToDate() {
		return acceptedToDate;
	}

	public void setAcceptedToDate(Date acceptedToDate) {
		this.acceptedToDate = acceptedToDate;
	}

	public Date getReceivedFromDate() {
		return receivedFromDate;
	}

	public void setReceivedFromDate(Date receivedFromDate) {
		this.receivedFromDate = receivedFromDate;
	}

	public Date getReceivedToDate() {
		return receivedToDate;
	}

	public void setReceivedToDate(Date receivedToDate) {
		this.receivedToDate = receivedToDate;
	}
		
}
