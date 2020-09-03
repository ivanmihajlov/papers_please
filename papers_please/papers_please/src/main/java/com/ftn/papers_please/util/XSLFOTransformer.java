package com.ftn.papers_please.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import net.sf.saxon.TransformerFactoryImpl;

@Component
public class XSLFOTransformer {
	private FopFactory fopFactory;
	private TransformerFactory transformerFactory;
	public static final String FOP_XCONF = "src/main/resources/fop/fop.xconf";
	
	public XSLFOTransformer() throws SAXException, IOException {
		fopFactory = FopFactory.newInstance(new File(FOP_XCONF));
		transformerFactory = new TransformerFactoryImpl();
	}
	
	public ByteArrayOutputStream generatePDF(String xmlString, String xslString) throws Exception {
		File xslFile = new File(xslString);
		StreamSource transformSource = new StreamSource(xslFile);
		Transformer xslFoTransformer = transformerFactory.newTransformer(transformSource);
		StreamSource source = new StreamSource(new StringReader(xmlString));
		FOUserAgent userAgent = fopFactory.newFOUserAgent();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, outStream);
		Result res = new SAXResult(fop.getDefaultHandler());
		xslFoTransformer.transform(source, res);
		return outStream;
	}

	public ByteArrayOutputStream generateHTML( String xmlString,String xslString)throws Exception {
		File xslFile = new File(xslString);
		StreamSource transformSource = new StreamSource(xslFile);
		StreamSource source = new StreamSource(new StringReader(xmlString));
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(outStream);
		
		Transformer transformer = transformerFactory.newTransformer(transformSource);
		transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xhtml");
		
		transformer.transform(source, result);
		return outStream;
	}
	
}
