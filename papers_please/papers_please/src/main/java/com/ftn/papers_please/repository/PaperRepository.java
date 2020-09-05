package com.ftn.papers_please.repository;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.exist.xmldb.EXistResource;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

import com.ftn.papers_please.util.DBManager;
import com.ftn.papers_please.util.XUpdateTemplate;
import com.ftn.papers_please.util.FileUtil;
import com.ftn.papers_please.exceptions.DatabaseException;
import com.ftn.papers_please.exceptions.ResourceNotFoundException;
import com.ftn.papers_please.fuseki.MetadataExtractor;
import com.ftn.papers_please.model.scientific_paper.ScientificPaper;

@Repository
public class PaperRepository {
	
	private static final String XQUERY_PATH = "src/main/resources/xQuery";
	public static final String PAPER_XSL_FO_PATH = "src/main/resources/xsl-fo/paper_fo.xsl";
	public static final String PAPER_XSL_PATH = "src/main/resources/xslt/paper.xsl";
	public static final String ANONYMOUS_PAPER_XSL_FO_PATH = "src/main/resources/xsl-fo/anonymous_paper_fo.xsl";
	public static final String ANONYMOUS_PAPER_XSL_PATH = "src/main/resources/xslt/anonymous_paper.xsl";

	@Autowired
	private DBManager dbManager;
	
	@Autowired
	private MetadataExtractor metadataExtractor;
	
	@Value("${paper-collection-id}")
	private String paperCollectionId;
	
	@Value("${paper-schema-path}")
	private String paperSchemaPath;
	
	public XMLResource findOne(String id) throws Exception {
		XMLResource result = dbManager.findOne(paperCollectionId, id);
		if (result == null)
			throw new ResourceNotFoundException("Couldn't find the paper with ID " + id);
		return result;
	}
	
	public ScientificPaper findOneUnmarshalled(String id) {
		try {
			String xPathExpression = String.format("/scientific_paper[@id='%s']", id);
			ResourceSet result = dbManager.executeXPath(paperCollectionId, xPathExpression);
			if (result == null)
				return null;

			ResourceIterator i = result.getIterator();
			Resource res = null;
			ScientificPaper paper = null;

			while(i.hasMoreResources()) {
				try {
					res = i.nextResource();
					paper = unmarshallPaper(res.getContent().toString());
				} finally {
					try {
						((EXistResource)res).freeResources();
					} catch (XMLDBException e) {
						e.printStackTrace();
					}
				}
			}
			return paper;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DatabaseException("Finding paper by ID failed!");
		}
	}
	
	public String getById(String id, String loggedAuthor) {
		String xQueryPath = XQUERY_PATH + "\\findById.txt";
		String result = "";

		HashMap<String, String> params = new HashMap<>();
		params.put("id", id);
		params.put("loggedAuthor", loggedAuthor);

		try {
			ResourceSet rs = dbManager.executeXQuery(paperCollectionId, "", params, xQueryPath);
			result = dbManager.resourceSetToString(rs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public String getAll(String searchText, String loggedAuthor) {
		String xQueryPath = XQUERY_PATH + "\\textSearch.txt";
		String result = "";

		HashMap<String, String> params = new HashMap<>();
		params.put("searchText", searchText);
		params.put("loggedAuthor", loggedAuthor);

		try {
			ResourceSet rs = dbManager.executeXQuery(paperCollectionId, "", params, xQueryPath);
			result = dbManager.resourceSetToString(rs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public String getNextId() {
		String id = "paper0";
		
		// generate paper ID
		try {
			ResourceSet rs = dbManager.executeXQuery(paperCollectionId, "count(/.)", new HashMap<>(), "");
			id = "paper" + rs.getIterator().nextResource().getContent().toString();
		} catch (Exception e) {
			throw new DatabaseException("Generating paper ID failed!");
		}
		return id;
	}
	
	public String getMetadataXml(String id) throws Exception {
		XMLResource paper = findOne(id);
		if (paper == null)
			throw new ResourceNotFoundException("Couldn't find paper with id: " + id);
		
		// extract the metadata to a file and read it
		String rdfFilePath = "src/main/resources/rdf/metadata.rdf";
		metadataExtractor.extractMetadata(paper.getContent().toString(), rdfFilePath);
		return FileUtil.readFile(rdfFilePath);
	}

	public String getMetadataJson(String id) throws Exception {
		String metadataXml = getMetadataXml(id);
		JSONObject xmlJSONObj = XML.toJSONObject(metadataXml);
		return xmlJSONObj.toString(4);
	}
	
	public void updateStatus(String paperId, String newStatus) throws Exception {
		String updatePath = "/scientific_paper/@status";
		String xUpdateExpression = String.format(XUpdateTemplate.UPDATE, updatePath, newStatus);
		dbManager.executeXUpdate(paperCollectionId, xUpdateExpression, paperId);
	}
	
	public void save(String scientificPaperXml, String id) throws Exception {
		dbManager.save(paperCollectionId, id, scientificPaperXml);
	}
	
	public boolean update(String scientificPaperXml, String id) throws Exception {
		dbManager.save(paperCollectionId, id, scientificPaperXml);
		return true;
	}
	
	public void update(ScientificPaper scientificPaper) {
		try {
			String spXML = marshallPaper(scientificPaper);
			dbManager.save(paperCollectionId, scientificPaper.getId(), spXML);
		} catch (JAXBException e) {
			throw new DatabaseException("Error while marshalling scientific paper!");
		} catch (Exception e) {
			throw new DatabaseException("Error while updating scientific paper!");
		}
	}
	
	private ScientificPaper unmarshallPaper(String paperXML) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(ScientificPaper.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return (ScientificPaper) unmarshaller.unmarshal(new StringReader(paperXML));
	}
	
	private String marshallPaper(ScientificPaper scientificPaper) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(ScientificPaper.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		marshaller.marshal(scientificPaper, stream);
		return new String(stream.toByteArray());
	}
	
}
