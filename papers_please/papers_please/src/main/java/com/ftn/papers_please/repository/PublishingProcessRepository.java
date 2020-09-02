package com.ftn.papers_please.repository;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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
import com.ftn.papers_please.util.XUpdateTemplate;
import com.ftn.papers_please.exceptions.CustomUnexpectedException;
import com.ftn.papers_please.exceptions.DatabaseException;
import com.ftn.papers_please.exceptions.ResourceNotFoundException;
import com.ftn.papers_please.model.publishing_process.PublishingProcess;

@Repository
public class PublishingProcessRepository {

	@Autowired
	private DBManager dbManager;

	@Value("${publishing-process-collection-id}")
	private String publishingProcessCollectionId;
	
	public List<PublishingProcess> getAll() {
		try {
			String xPath = "/publishing-process";
			ResourceSet result = dbManager.executeXPath(publishingProcessCollectionId, xPath);
			if (result == null)
				return new ArrayList<PublishingProcess>();

			ResourceIterator i = result.getIterator();
			Resource res = null;
			List<PublishingProcess> publishingProcesses = new ArrayList<>();
			while (i.hasMoreResources()) {
				try {
					res = i.nextResource();
					PublishingProcess publishingProcess = unmarshallPublishingProcess(res.getContent().toString());
					publishingProcesses.add(publishingProcess);
				} finally {
					try {
						((EXistResource)res).freeResources();
					} catch (XMLDBException e) {
						e.printStackTrace();
					}
				}
			}
			return  publishingProcesses;
		} catch(Exception e) {
			throw new DatabaseException("Finding all publishing processes failed!");
		}
	}
	
	public XMLResource findOne(String id) throws Exception {
		XMLResource result = dbManager.findOne(publishingProcessCollectionId, id);
		if (result == null)
			throw new ResourceNotFoundException("Couldn't find the publishing process with id " + id);
		return result;
	}

	public PublishingProcess findOneUnmarshalled(String id) {
		try {
			String xPath = String.format("/publishing-process[@id='%s']", id);
			ResourceSet result = dbManager.executeXPath(publishingProcessCollectionId, xPath);
			if (result == null)
				return null;

			ResourceIterator i = result.getIterator();
			Resource res = null;
			while (i.hasMoreResources()) {
				try {
					res = i.nextResource();
					PublishingProcess publishingProcess = unmarshallPublishingProcess(res.getContent().toString());
					return  publishingProcess;
				} finally {
					try {
						((EXistResource)res).freeResources();
					} catch (XMLDBException e) {
						e.printStackTrace();
					}
				}
			}
			return  null;
		} catch(Exception e) {
			throw new DatabaseException("Finding paper by ID failed!");
		}
	}
	
	public String getNextId() {
		String id = "process0";
		
		// generate publishing process ID
		try {
			ResourceSet rs = dbManager.executeXQuery(publishingProcessCollectionId, "count(/.)", new HashMap<>(), "");
			id = "process" + rs.getIterator().nextResource().getContent().toString();
		} catch (Exception e) {
			throw new DatabaseException("Generating publishing process ID failed!");
		}
		return id;
	}
	
	public void updateStatus(String processId, String newStatus) throws Exception {
		String updatePath = "/publishing-process/@status";
		String xUpdateExpression = String.format(XUpdateTemplate.UPDATE, updatePath, newStatus);
		dbManager.executeXUpdate(publishingProcessCollectionId, xUpdateExpression, processId);
	}
	
	public void assignEditor(String processId, String userId) {
		try {
			XMLResource result = dbManager.findOne(publishingProcessCollectionId, processId);
			if (result == null)
				throw new ResourceNotFoundException("Couldn't fint thep ublishing process with id " + processId);

			String updatePath = "/publishing-process/editor-id";
			String xUpdateExpression = String.format(XUpdateTemplate.UPDATE, updatePath, userId);
			dbManager.executeXUpdate(publishingProcessCollectionId, xUpdateExpression, processId);
		} catch (Exception e) {
			throw new CustomUnexpectedException("Error occurred while assigning editor " + userId + " to publishing process " + processId);
		}
	}
	
	public void save(String publishingProcessXml, String id) throws Exception {
		dbManager.save(publishingProcessCollectionId, id, publishingProcessXml);
	}
	
	public void save(PublishingProcess process) {
		try {
			String processXML = marshallPublishingProcess(process);
			dbManager.save(publishingProcessCollectionId, process.getId(),  processXML);
		} catch (JAXBException e) {
			throw new DatabaseException("Error while marshalling publishing process.");
		} catch (Exception e) {
			throw new DatabaseException("Error while updating publishing process.");
		}
	}
	
	private PublishingProcess unmarshallPublishingProcess(String publishingProcessXML) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(PublishingProcess.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return (PublishingProcess) unmarshaller.unmarshal(new StringReader(publishingProcessXML));
	}

	private String marshallPublishingProcess(PublishingProcess publishingProcess) throws Exception {
		JAXBContext context = JAXBContext.newInstance(PublishingProcess.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		marshaller.marshal(publishingProcess, stream);
		return new String(stream.toByteArray());
	}
}
