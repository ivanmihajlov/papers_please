package com.ftn.papers_please.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmldb.api.modules.XMLResource;

import com.ftn.papers_please.repository.PaperRepository;
import com.ftn.papers_please.util.DOMParser;
import com.ftn.papers_please.util.XSLFOTransformer;
import com.ftn.papers_please.exceptions.ProcessStatusException;
import com.ftn.papers_please.exceptions.RevisionForbiddenException;
import com.ftn.papers_please.dto.SearchData;
import com.ftn.papers_please.fuseki.FusekiReader;
import com.ftn.papers_please.fuseki.FusekiManager;
import com.ftn.papers_please.fuseki.MetadataExtractor;
import com.ftn.papers_please.exceptions.MaxChapterLevelsExceededException;
import com.ftn.papers_please.model.scientific_paper.ScientificPaper;

@Service
public class PaperService {
	
	private static final String QUERY_FILE_PATH = "src/main/resources/sparql/metadataSearch.rq";
	private static final String QUOTES_FILE_PATH = "src/main/resources/sparql/findQuotes.rq";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Value("${paper-schema-path}")
	private String spSchemaPath;
	
	@Value("${max-chapter-levels}")
	private int maxChapterLevels;
	
	@Autowired
	private PaperRepository paperRepository;

	@Autowired
	private PublishingProcessService publishingProcessService;
	
	@Autowired
	private MetadataExtractor metadataExtractor;

	@Autowired
	private FusekiManager fusekiManager;
	
	@Autowired
	private XSLFOTransformer xslfoTransformer;
	
	public XMLResource findOne(String id) throws Exception {
		return paperRepository.findOne(id);
	}

	public ScientificPaper findOneUnmarshalled(String id) {
		return paperRepository.findOneUnmarshalled(id);
	}
	
	public XMLResource findOneXml(String id) throws Exception {
		XMLResource paperXml = paperRepository.findOne(id);
		return paperXml;
	}
	
	public byte[] findOnePdf(String id) throws Exception {
		String xmlString = paperRepository.findOne(id).getContent().toString();
		String xslString = PaperRepository.PAPER_XSL_FO_PATH;
		ByteArrayOutputStream paperPdf = xslfoTransformer.generatePDF(xmlString, xslString); 
		return paperPdf.toByteArray();
    }
    
    public byte[] findOneHtml(String id) throws Exception {
    	String xmlString = paperRepository.findOne(id).getContent().toString();
    	String xslString = PaperRepository.PAPER_XSL_PATH;
    	ByteArrayOutputStream paperHtml = xslfoTransformer.generateHTML(xmlString, xslString); 
		return paperHtml.toByteArray();
	}
	
	public String getAll(String searchText, String loggedAuthor) {
		return paperRepository.getAll(searchText, loggedAuthor);
	}
	
	public String getByIds(Set<String> ids, String loggedAuthor) {
		StringBuilder sb = new StringBuilder();
		sb.append("<search>");
		ids.forEach(id -> sb.append(paperRepository.getById(id, loggedAuthor)));
		sb.append("</search>");
		return sb.toString();
	}
	
	// get paper IDs from URLs
	// e.g. https://github.com/ivanmihajlov/papers_please/papers/paper123 -> paper123
	public Set<String> getIdsFromUrls(Set<String> paperURLs) {
		Set<String> paperIds = new HashSet<>();
		for (String url : paperURLs) {
			String[] idArray = url.split("/");
			String id = idArray[idArray.length - 1];
			paperIds.add(id);
		}
		return paperIds;
	}
	
	// SPARQL query returns a set matching papers' URLs
	// XQuery queries then try to find papers with those IDs
	public String metadataSearch(SearchData searchData, String loggedAuthor) throws IOException {
		HashMap<String, String> values = new HashMap<>();
		values.put("keyword", searchData.getKeyword());
		values.put("title", searchData.getTitle());
		values.put("author", searchData.getAuthor());
		values.put("affiliation", searchData.getAffiliation());

		String acceptedFromDate = searchData.getAcceptedFromDate() == null ? ""
				: sdf.format(searchData.getAcceptedFromDate());
		String acceptedToDate = searchData.getAcceptedToDate() == null ? ""
				: sdf.format(searchData.getAcceptedToDate());
		String receivedFromDate = searchData.getReceivedFromDate() == null ? ""
				: sdf.format(searchData.getReceivedFromDate());
		String receivedToDate = searchData.getReceivedToDate() == null ? ""
				: sdf.format(searchData.getReceivedToDate());
		
		values.put("acceptedFromDate", acceptedFromDate);
		values.put("acceptedToDate", acceptedToDate);
		values.put("receivedFromDate", receivedFromDate);
		values.put("receivedToDate", receivedToDate);

		Set<String> paperURLs = FusekiReader.executeQuery(QUERY_FILE_PATH, values);
		Set<String> paperIds = getIdsFromUrls(paperURLs);
		return getByIds(paperIds, loggedAuthor);
	}
	
	public String getQuotedBy(String paperId) throws IOException {
		HashMap<String, String> values = new HashMap<>();
		values.put("quotedPaper", "https://github.com/ivanmihajlov/papers_please/scientific_papers/" + paperId);
		Set<String> paperURLs = FusekiReader.executeQuery(QUOTES_FILE_PATH, values);
		Set<String> paperIds = getIdsFromUrls(paperURLs);
		return getByIds(paperIds, "");
	}
	
	public String save(String scientificPaperXml, String paperVersion) throws Exception {
		// validate paper XML using the paper schema
		Document document = DOMParser.buildDocument(scientificPaperXml, spSchemaPath);
		String id = paperRepository.getNextId();
		generateIds(document, id);

		// change the ID in paper XML (needed for metadata search)
		NodeList paperNodeList = document.getElementsByTagName("scientific_paper");
		Element paperElement = (Element) paperNodeList.item(0);
		paperElement.getAttributes().getNamedItem("id").setNodeValue(id);

		generateMetadataAttributes(paperElement, id);

		paperElement.setAttribute("version", paperVersion.toString());
		paperElement.setAttribute("status", "PENDING");

		String newXml = DOMParser.getStringFromDocument(document);
		paperRepository.save(newXml, id);
		
		String rdfFilePath = "src/main/resources/rdf/newMetadata.rdf";
		metadataExtractor.extractMetadata(newXml, rdfFilePath);
		fusekiManager.saveMetadata(rdfFilePath, "/scientificPapers");
		return id;
	}
	
	public void withdrawScientificPaper(String paperId, String authorUsername) throws Exception {
		String processId = publishingProcessService.findOneByPaperId(paperId);
		String status = publishingProcessService.getProcessStatus(processId);
		String authorFromProcess = publishingProcessService.getAuthorFromProcess(processId);
		
		if (!authorFromProcess.equals(authorUsername))
			throw new RevisionForbiddenException("You are not the author of this paper!");

		if (status.equalsIgnoreCase("WITHDRAWN"))
			throw new ProcessStatusException("Paper already withrawn!");

		if (status.equalsIgnoreCase("ACCEPTED") || status.equalsIgnoreCase("REJECTED") || status.equalsIgnoreCase("WITHDRAWN"))
			throw new ProcessStatusException("Withdrawing not possible!");

		publishingProcessService.updateStatus(processId, "WITHDRAWN");
		paperRepository.updateStatus(paperId, "WITHDRAWN");
	}
	
	public void generateMetadataAttributes(Element paperElement, String id) throws Exception {
		String aboutAttributeValue = "https://github.com/ivanmihajlov/papers_please/scientific_papers/" + id;
	    paperElement.setAttribute("rdfa:about", aboutAttributeValue);
		paperElement.setAttribute("rdfa:vocab", "https://github.com/ivanmihajlov/papers_please/predicate/");

		// titles
		NodeList titlesNodeList = paperElement.getElementsByTagName("title");
		for (int i = 0; i < 2; i++) { // there could only be 2 titles
			Element titleElement = (Element) titlesNodeList.item(i);
			titleElement.setAttribute("rdfa:property", "pred:titled");
			titleElement.setAttribute("rdfa:datatype", "xs:string");
		}

		// keywords
		NodeList keywordsNodeList = paperElement.getElementsByTagName("keyword");
		for (int i = 0; i < keywordsNodeList.getLength(); i++) {
			Element keywordElement = (Element) keywordsNodeList.item(i);
			keywordElement.setAttribute("rdfa:property", "pred:keyword");
			keywordElement.setAttribute("rdfa:datatype", "xs:string");
		}

		// get authors and change rdfa:href attribute value
		NodeList authorsNodeList = paperElement.getElementsByTagName("author");
		for (int i = 0; i < authorsNodeList.getLength(); i++) {
			Element authorElement = (Element) authorsNodeList.item(i);
			authorElement.setAttribute("rdfa:href", aboutAttributeValue);
			authorElement.setAttribute("rdfa:rel", "pred:isTheAuthorOf");
			authorElement.setAttribute("rdfa:rev", "pred:isWrittenBy");
			authorElement.setAttribute("rdfa:about", "author" + i);

			Element firstName = (Element) authorElement.getElementsByTagName("first_name").item(0);
			Element lastName = (Element) authorElement.getElementsByTagName("last_name").item(0);
			firstName.setAttribute("rdfa:property", "pred:firstName");
			firstName.setAttribute("rdfa:datatype", "xs:string");
			lastName.setAttribute("rdfa:property", "pred:lastName");
			lastName.setAttribute("rdfa:datatype", "xs:string");

			Element affiliationElement = (Element) authorElement.getElementsByTagName("affiliation").item(0);
			affiliationElement.setAttribute("rdfa:href", "author" + i);
			affiliationElement.setAttribute("rdfa:rel", "pred:hasAMember");
			affiliationElement.setAttribute("rdfa:rev", "pred:isAMemberOf");
			affiliationElement.setAttribute("rdfa:about", "affiliation");

			Element affiliationNameElement = (Element) affiliationElement.getElementsByTagName("name").item(0);
			affiliationNameElement.setAttribute("rdfa:property", "pred:affiliationNamed");
			affiliationNameElement.setAttribute("rdfa:datatype", "xs:string");
		}

		// received date
		Element receivedDateElement = (Element) paperElement.getElementsByTagName("received_date").item(0);
		receivedDateElement.setTextContent(sdf.format(new Date()));
		receivedDateElement.setAttribute("rdfa:property", "pred:received");
		receivedDateElement.setAttribute("rdfa:datatype", "xs:date");

		// revised date
		Element revisedDateElement = (Element) paperElement.getElementsByTagName("revised_date").item(0);
		revisedDateElement.setTextContent("");
		revisedDateElement.setAttribute("rdfa:property", "pred:revised");
		revisedDateElement.setAttribute("rdfa:datatype", "xs:date");

		// accepted date
		Element acceptedDateElement = (Element) paperElement.getElementsByTagName("accepted_date").item(0);
		acceptedDateElement.setTextContent("");
		acceptedDateElement.setAttribute("rdfa:property", "pred:accepted");
		acceptedDateElement.setAttribute("rdfa:datatype", "xs:date");
		
		// quotes
		NodeList quoteList = paperElement.getElementsByTagName("quote");
		for (int i = 0; i < quoteList.getLength(); i++) {
			Element quoteElement = (Element) quoteList.item(i);
			String quoteSourcePaperId = quoteElement.getElementsByTagName("source").item(0).getTextContent();
			findOne(quoteSourcePaperId); // check ID validity
			quoteElement.setAttribute("rdfa:href", aboutAttributeValue);
			quoteElement.setAttribute("rdfa:rel", "pred:isQuotedBy");
			quoteElement.setAttribute("rdfa:rev", "pred:quotes");
			quoteElement.setAttribute("rdfa:about", "https://github.com/ivanmihajlov/papers_please/scientific_papers/" + quoteSourcePaperId);
		}
	}
	
	public void generateIds(Document document, String paperId) throws MaxChapterLevelsExceededException {
		// set abstract ID
		Element abstractEl = (Element) document.getElementsByTagName("abstract").item(0);
		abstractEl.setAttribute("id", paperId + "/abstract");

		// set chapter IDs
		NodeList chapters = document.getElementsByTagName("chapter");
		for (int i = 0; i < chapters.getLength(); i++) {
			Element chapter = (Element) chapters.item(i);

			// set ID for the highest level chapter (body) and its subchapters
			if (chapter.getParentNode().getNodeName().equals("body")) {
				String id = paperId + "/chapter" + i;
				chapter.setAttribute("id", id);

				// set paragraph IDs
				NodeList paragraphs = chapter.getElementsByTagName("paragraph");
				for (int j = 0; j < paragraphs.getLength(); j++) {
					Element paragraph = (Element) paragraphs.item(j);
					paragraph.setAttribute("id", id + "/paragraph" + j);
				}
				setSubchapterIds(chapter, id, 1);
			}
		}

		// set image IDs
		NodeList images = document.getElementsByTagName("image");
		for (int i = 0; i < images.getLength(); i++) {
			Element image = (Element) images.item(i);
			image.setAttribute("id", paperId + "/image" + i);
		}

		// set table IDs
		NodeList tables = document.getElementsByTagName("table");
		for (int i = 0; i < tables.getLength(); i++) {
			Element table = (Element) tables.item(i);
			table.setAttribute("id", paperId + "/table" + i);
		}
	}
	
	// set subchapter IDs recursively
	public void setSubchapterIds(Element chapter, String parentId, int levelCount) throws MaxChapterLevelsExceededException {
		if (levelCount > maxChapterLevels)
			throw new MaxChapterLevelsExceededException("Allowed chapter levels excedeed, maximum is " + maxChapterLevels);

		NodeList subchapters = chapter.getElementsByTagName("chapter");
		for (int i = 0; i < subchapters.getLength(); i++) {
			Element subchapter = (Element) subchapters.item(i);
			// set ID for this subchapter and its subchapters
			if (((Element) subchapter.getParentNode()).getAttribute("id").equals(parentId)) {
				String id = parentId + "/subchapter" + i;
				subchapter.setAttribute("id", id);

				// set paragraph IDs
				NodeList paragraphs = subchapter.getElementsByTagName("paragraph");
				for (int j = 0; j < paragraphs.getLength(); j++) {
					Element paragraph = (Element) paragraphs.item(j);
					paragraph.setAttribute("id", id + "/paragraph" + j);
				}
				setSubchapterIds(subchapter, id, levelCount + 1);
			}
		}
	}
	
	public void update(ScientificPaper scientificPaper) {
		paperRepository.update(scientificPaper);
	}
	
	public String getMetadataXml(String id) throws Exception {
		return paperRepository.getMetadataXml(id);
	}

	public String getMetadataJson(String id) throws Exception {
		return paperRepository.getMetadataJson(id);
	}
	
}
