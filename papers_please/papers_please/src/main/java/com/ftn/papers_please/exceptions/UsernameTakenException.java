package com.ftn.papers_please.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UsernameTakenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UsernameTakenException() { super("Username is already taken!");}
}
