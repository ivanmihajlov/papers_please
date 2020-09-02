package com.ftn.papers_please.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DomErrorHandler implements ErrorHandler{

	@Override
	public void error(SAXParseException err) throws SAXParseException {
		throw err;
	}

	@Override
	public void fatalError(SAXParseException err) throws SAXException {
		throw err;
	}
	
	@Override
    public void warning(SAXParseException err) throws SAXParseException {
    	System.out.println("[WARN] Warning at line: " + err.getLineNumber() + ", URI: " + err.getSystemId());
        System.out.println("[WARN] " + err.getMessage());
    }

}
