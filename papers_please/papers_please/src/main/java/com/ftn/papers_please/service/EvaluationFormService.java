package com.ftn.papers_please.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmldb.api.modules.XMLResource;

import com.ftn.papers_please.repository.EvaluationFormRepository;
import com.ftn.papers_please.util.XSLFOTransformer;
import com.ftn.papers_please.util.FileUtil;
import com.ftn.papers_please.util.DOMParser;
import com.ftn.papers_please.exceptions.CustomUnexpectedException;
import com.ftn.papers_please.model.evaluation_form.EvaluationForm;

@Service
public class EvaluationFormService {

	@Autowired
	private EvaluationFormRepository evaluationFormRepository;

	@Value("${evaluation-form-schema-path}")
	private String evaluationFormSchemaPath;
	
	@Value("${evaluation-form-template-path}")
	private String efTemplatePath;
	
	@Autowired
	private XSLFOTransformer xslfoTransformer;
	
	public XMLResource findOne(String id) throws Exception {
		return evaluationFormRepository.findOne(id);
	}

	public EvaluationForm findOneUnmarshalled(String reviewId) {
		return evaluationFormRepository.findOneUnmarshalled(reviewId);
	}
	
	public XMLResource findOneXml(String id) throws Exception {
		XMLResource evaluationFormXml = evaluationFormRepository.findOne(id);
		return evaluationFormXml;
	}
	
	public byte[] findOneHtml(String id) throws Exception {
    	String xmlString = evaluationFormRepository.findOne(id).getContent().toString();
    	String xslString = EvaluationFormRepository.EVALUATION_FORM_XSL_PATH;
    	ByteArrayOutputStream evaluationFormHtml = xslfoTransformer.generateHTML(xmlString, xslString); 
		return evaluationFormHtml.toByteArray();
	}
	
	public byte[] findOnePdf(String id) throws Exception {
		String xmlString = evaluationFormRepository.findOne(id).getContent().toString();
		String xslString = EvaluationFormRepository.EVALUATION_FORM_XSL_FO_PATH;
		ByteArrayOutputStream evaluationFormPdf = xslfoTransformer.generatePDF(xmlString, xslString); 
		return evaluationFormPdf.toByteArray();
    }
	
	public String save(String evaluationFormXML, String paperId, String reviewerId) {
		try {
			Document document = DOMParser.buildDocument(evaluationFormXML, evaluationFormSchemaPath);
			String evaluationFormId = evaluationFormRepository.getNextId();
			NodeList revaluationFormNodeList = document.getElementsByTagName("evaluation_form");
			Element evaluationFormElement = (Element) revaluationFormNodeList.item(0);

			evaluationFormElement.setAttribute("id", evaluationFormId);
			evaluationFormElement.setAttribute("scientific_paper_id", paperId);
			evaluationFormElement.setAttribute("reviewer_id", reviewerId);

			String xmlFile = DOMParser.getStringFromDocument(document);
			evaluationFormRepository.save(xmlFile, evaluationFormId);
			return evaluationFormId;
		} catch (Exception e) {
			throw new CustomUnexpectedException("Error while saving evaluation form!");
		}
	}
	
	public String getReviewsForMerge(String processId, String paperId) throws Exception {
    	String reviewsForMergeStr = evaluationFormRepository.getReviewsForMerge(processId, paperId);
    	Document document = DOMParser.buildDocumentWithoutSchema(reviewsForMergeStr);
    	NodeList reviews = document.getDocumentElement().getElementsByTagName("evaluation_form");
    	
		for (int i = 0; i < reviews.getLength(); i++) {
			Element review = (Element) reviews.item(i);
			NodeList reviewers = review.getElementsByTagName("reviewer");
			for (int j = 0; j < reviewers.getLength(); j++)
				review.removeChild(reviewers.item(j));
		}
    	
		reviewsForMergeStr = DOMParser.getStringFromDocument(document);
    	return reviewsForMergeStr;
    }
	
	public byte[] getMergedHtml(String processId, String paperId) throws Exception {
    	String merged = getReviewsForMerge(processId, paperId);
    	String xslPath = EvaluationFormRepository.MERGE_XSL_PATH;
    	ByteArrayOutputStream evaluationFormHtml = xslfoTransformer.generateHTML(merged, xslPath); 
		return evaluationFormHtml.toByteArray();
    }
	
	public String getTemplate() throws IOException {
		return FileUtil.readFile(efTemplatePath);
	}
	
}
