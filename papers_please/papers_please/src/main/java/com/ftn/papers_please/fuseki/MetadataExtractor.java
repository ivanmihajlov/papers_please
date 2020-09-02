package com.ftn.papers_please.fuseki;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;

@SuppressWarnings("restriction")
@Component
public class MetadataExtractor {
	
	private static final String XSLT_FILE = "src/main/resources/xsl/grddl.xsl";
	private TransformerFactory transformerFactory;
	
	public MetadataExtractor() throws SAXException, IOException {
		transformerFactory = new TransformerFactoryImpl();
	}

	/**
	 * Generates RDF/XML based on RDFa metadata from an XML containing 
	 * input stream by applying GRDDL XSL transformation.
	 *  
	 * @param in XML containing input stream
	 * @param out RDF/XML output stream
	 */
	public void extractMetadata(String xml, String outFilePath) throws FileNotFoundException, TransformerException {
		OutputStream out = new FileOutputStream(outFilePath);
		StreamSource transformSource = new StreamSource(new File(XSLT_FILE));
		Transformer grddlTransformer = transformerFactory.newTransformer(transformSource);
		
		// set the indentation properties
		grddlTransformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
		grddlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		// transform the subject
		StreamSource source = new StreamSource(new StringReader(xml));
		StreamResult result = new StreamResult(out);
		grddlTransformer.transform(source, result);
	}

}
