package com.ftn.papers_please.repository;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XMLResource;

import com.ftn.papers_please.exceptions.ResourceNotFoundException;
import com.ftn.papers_please.exceptions.DatabaseException;
import com.ftn.papers_please.util.DBManager;

@Repository
public class CoverLetterRepository {

	public static final String COVER_LETTER_XSL_FO_PATH = "src/main/resources/xsl-fo/cover_letter_fo.xsl";
	public static final String COVER_LETTER_XSL_PATH = "src/main/resources/xslt/cover_letter.xsl";
	
	@Autowired
	private DBManager dbManager;
	
	@Value("${cover-letter-collection-id}")
	private String coverLetterCollectionId;
	
	@Value("${cover-letter-schema-path}")
	private String coverLetterSchemaPath;
	
	public XMLResource findOne(String id) throws Exception {
		 XMLResource result = dbManager.findOne(coverLetterCollectionId, id);
		 if (result == null)
			 throw new ResourceNotFoundException("Couldn't find the cover letter with ID " + id);
		 return result;
	}
	
	public String getNextId() {
		String id = "letter0";
		
		// generate cover letter ID
		try {
			ResourceSet rs = dbManager.executeXQuery(coverLetterCollectionId, "count(/.)", new HashMap<>(), "");
			id = "letter" + rs.getIterator().nextResource().getContent().toString();
		} catch (Exception e) {
			throw new DatabaseException("Error while saving cover letter!");
		}
		return id;
	}
	
	public String save(String coverLetterXml) throws Exception {
		String id = getNextId();
		dbManager.save(coverLetterCollectionId, getNextId(), coverLetterXml);
		return id;
	}
	
}
