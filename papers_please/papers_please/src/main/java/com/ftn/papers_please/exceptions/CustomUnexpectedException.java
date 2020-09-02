package com.ftn.papers_please.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomUnexpectedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CustomUnexpectedException(String message) {
        super(message);
    }
}
