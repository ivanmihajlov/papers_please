package com.ftn.papers_please.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmldb.api.modules.XMLResource;

import com.ftn.papers_please.repository.PaperRepository;
import com.ftn.papers_please.util.DOMParser;
import com.ftn.papers_please.exceptions.MaxChapterLevelsExceededException;
import com.ftn.papers_please.model.scientific_paper.ScientificPaper;

@Service
public class PaperService {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Value("${paper-schema-path}")
	private String spSchemaPath;
	
	@Value("${max-chapter-levels}")
	private int maxChapterLevels;
	
	@Autowired
	private PaperRepository paperRepository;
	
	public XMLResource findOne(String id) throws Exception {
		return paperRepository.findOne(id);
	}

	public ScientificPaper findOneUnmarshalled(String id) {
		return paperRepository.findOneUnmarshalled(id);
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

		// TODO set metadata attributes

		paperElement.setAttribute("version", paperVersion.toString());
		paperElement.setAttribute("status", "PENDING");

		String newXml = DOMParser.getStringFromDocument(document);
		paperRepository.save(newXml, id);
		return id;
	}
	
	public void generateMetadataAttributes(Element paperElement, String id) throws Exception {

		String aboutAttributeValue = "https://github.com/ivanmihajlov/papers_please/scientific_papers/" + id;
	    paperElement.setAttribute("rdfa:about", aboutAttributeValue);
		paperElement.setAttribute("rdfa:vocab", "https://github.com/ivanmihajlov/papers_please/predicate/");

		// titles
		NodeList titlesNodeList = paperElement.getElementsByTagName("title");
		for (int i = 0; i < titlesNodeList.getLength(); i++) {
			Element titleElement = (Element) titlesNodeList.item(i);
			titleElement.setAttribute("rdfa:property", "pred:title");
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
	
}
