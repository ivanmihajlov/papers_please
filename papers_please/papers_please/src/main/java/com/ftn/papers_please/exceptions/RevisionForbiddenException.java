package com.ftn.papers_please.exceptions;

public class RevisionForbiddenException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public RevisionForbiddenException(String message){
		super(message);
	}

}
