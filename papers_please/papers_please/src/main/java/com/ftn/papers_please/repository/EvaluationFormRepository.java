package com.ftn.papers_please.repository;

import java.io.StringReader;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.exist.xmldb.EXistResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

import com.ftn.papers_please.util.DBManager;
import com.ftn.papers_please.exceptions.CustomUnexpectedException;
import com.ftn.papers_please.exceptions.DatabaseException;
import com.ftn.papers_please.model.evaluation_form.EvaluationForm;
import com.ftn.papers_please.exceptions.ResourceNotFoundException;

@Repository
public class EvaluationFormRepository {
	
	public static final String EVALUATION_FORM_XSL_FO_PATH = "src/main/resources/xsl-fo/evaluation_form_fo.xsl";
	public static final String EVALUATION_FORM_XSL_PATH = "src/main/resources/xslt/evaluation_form.xsl";
	public static final String MERGE_XSL_PATH = "src/main/resources/xslt/mergeReviews.xsl";

	@Autowired
	private DBManager dbManager;
	
	@Value("${evaluation-form-collection-id}")
	private String evaluationFormCollectionId;
	
	public XMLResource findOne(String id) throws Exception {
		 XMLResource result = dbManager.findOne(evaluationFormCollectionId, id);
		 if (result == null)
			 throw new ResourceNotFoundException("Couldn't find evaluation form with ID " + id);
		 return result;
	}
	
	public EvaluationForm findOneUnmarshalled(String reviewId) {
		try {
			String xPath = String.format("/evaluation_form[@id='%s']", reviewId);
			ResourceSet result = dbManager.executeXPath(evaluationFormCollectionId, xPath);
			if (result == null)
				return null;
			
			ResourceIterator i = result.getIterator();
			Resource res = null;

			while (i.hasMoreResources()) {
				try {
					res = i.nextResource();
					EvaluationForm ef = unmarshallEvaluationForm(res.getContent().toString());
					return ef;
				} finally {
					try {
						((EXistResource)res).freeResources();
					} catch (XMLDBException e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		} catch (Exception e) {
			throw new DatabaseException("Exception while getting publishing processes." + reviewId);
		}
	}
	
	public String getReviewsForMerge(String processId, String paperId) throws Exception {
		String xQueryPath = "./src/main/resources/xQuery/getReviewsForMerge.txt";
		HashMap<String, String> params = new HashMap<>();
		params.put("id", processId);
		ResourceSet result = dbManager.executeXQuery(evaluationFormCollectionId, "", params, xQueryPath);
		if(result.getSize() == 0)
			throw new ResourceNotFoundException("Reviews not found");
		
		String resStr = "";
		ResourceIterator i = result.getIterator();
		Resource res = null;

		while (i.hasMoreResources()) {
			try {
				res = i.nextResource();
				resStr += res.getContent().toString();
			} finally {
				try {
					((EXistResource)res).freeResources();
				} catch (XMLDBException e) {
					e.printStackTrace();
				}
			}
		}
		return resStr;
	}
	
	public void save(String evaluationFormXML, String evaluationFormId) {
		try {
			dbManager.save(evaluationFormCollectionId, evaluationFormId, evaluationFormXML);
		} catch (Exception e) {
			throw new CustomUnexpectedException("Error while saving evaluation form!");
		}
	}
	
	public String getNextId() {
		String id = "evaluationForm0";
		
		// generate evaluation form ID
		try {
			ResourceSet rs = dbManager.executeXQuery(evaluationFormCollectionId, "count(/.)", new HashMap<>(), "");
			id = "evaluationForm" + rs.getIterator().nextResource().getContent().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}
	
	private EvaluationForm unmarshallEvaluationForm(String efXML) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(EvaluationForm.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return (EvaluationForm) unmarshaller.unmarshal(new StringReader(efXML));
	}
	
}
