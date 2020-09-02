package com.ftn.papers_please.util;

import static com.ftn.papers_please.util.XUpdateTemplate.TARGET_NAMESPACE;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.xml.transform.OutputKeys;

import org.exist.xmldb.EXistResource;
import org.springframework.stereotype.Component;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.CompiledExpression;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

import com.ftn.papers_please.util.AuthenticationUtilities.ConnectionProperties;

@Component
public class DBManager {

	private static ConnectionProperties conn;

	public void save(String collectionId, String documentId, String xml) throws Exception {

		conn = AuthenticationUtilities.loadProperties();

		// initialize database driver
		Class<?> cl = Class.forName(conn.driver);

		// encapsulation of the database driver functionality
		Database database = (Database) cl.newInstance();
		database.setProperty("create-database", "true");

		// entry point for the API which enables getting the Collection reference
		DatabaseManager.registerDatabase(database);

		// a collection of Resources stored within an XML database
		Collection col = null;
		XMLResource res = null;

		try {
			col = getOrCreateCollection(collectionId);
			res = (XMLResource) col.createResource(documentId, XMLResource.RESOURCE_TYPE);
			res.setContent(xml);
			col.storeResource(res);
		} finally {
			// cleanup
			if (res != null) {
				try {
					((EXistResource) res).freeResources();
				} catch (XMLDBException xe) {
					xe.printStackTrace();
				}
			}
			if (col != null) {
				try {
					col.close();
				} catch (XMLDBException xe) {
					xe.printStackTrace();
				}
			}
		}
	}

	public XMLResource findOne(String collectionId, String documentId) throws Exception {

		conn = AuthenticationUtilities.loadProperties();
		Class<?> cl = Class.forName(conn.driver);
		Database database = (Database) cl.newInstance();
		database.setProperty("create-database", "true");
		DatabaseManager.registerDatabase(database);

		Collection col = null;
		XMLResource res = null;

		try {
			col = DatabaseManager.getCollection(conn.uri + collectionId);
			col.setProperty(OutputKeys.INDENT, "yes");
			res = (XMLResource) col.getResource(documentId);

			if (res == null) {
				System.out.println("[WARNING] Document '" + documentId + "' not found!");
			} else {
				System.out.println("[INFO] Document as an XML resource: ");
				System.out.println(res.getContent());
			}
		} finally {
			if (res != null) {
				try {
					((EXistResource) res).freeResources();
				} catch (XMLDBException xe) {
					xe.printStackTrace();
				}
			}
			if (col != null) {
				try {
					col.close();
				} catch (XMLDBException xe) {
					xe.printStackTrace();
				}
			}
		}
		return res;
	}

	public ResourceSet executeXQuery(String collectionId, String xqueryExpression, HashMap<String, String> parameterMap,
			String xqueryFilePath) throws Exception {

		conn = AuthenticationUtilities.loadProperties();
		Class<?> cl = Class.forName(conn.driver);
		Database database = (Database) cl.newInstance();
		database.setProperty("create-database", "true");
		DatabaseManager.registerDatabase(database);

		Collection col = null;
		ResourceSet result;

		try {
			col = DatabaseManager.getCollection(conn.uri + collectionId);
			if (col == null)
				col = getOrCreateCollection(collectionId);
			
			XQueryService xqueryService = (XQueryService) col.getService("XQueryService", "1.0");
			xqueryService.setProperty("indent", "yes");
			xqueryService.setNamespace("b", TARGET_NAMESPACE);

			// read XQuery if the expression isn't provided
			if (xqueryExpression.isEmpty())
				xqueryExpression = readFile(xqueryFilePath, StandardCharsets.UTF_8);
			
			for(String param : parameterMap.keySet())
                xqueryService.declareVariable(param, parameterMap.get(param));
			
			CompiledExpression compiledXquery = xqueryService.compile(xqueryExpression);
			result = xqueryService.execute(compiledXquery);
		} finally {
			if (col != null) {
				try {
					col.close();
				} catch (XMLDBException xe) {
					xe.printStackTrace();
				}
			}
		}
		return result;
	}

	public ResourceSet executeXPath(String collectionId, String xPathExpression) throws Exception {
		
		conn = AuthenticationUtilities.loadProperties();
		Class<?> cl = Class.forName(conn.driver);
		Database database = (Database) cl.newInstance();
		database.setProperty("create-database", "true");
		DatabaseManager.registerDatabase(database);

		Collection collection = null;
		ResourceSet result;

		try {
			collection = DatabaseManager.getCollection(conn.uri + collectionId);
			if (collection == null)
				collection = getOrCreateCollection(collectionId);

			XPathQueryService xpathService = (XPathQueryService) collection.getService("XPathQueryService", "1.0");
			xpathService.setProperty("indent", "yes");
			xpathService.setNamespace("", TARGET_NAMESPACE);
			result = xpathService.query(xPathExpression);
		} finally {
			if (collection != null) {
				try {
					collection.close();
				} catch (XMLDBException xe) {
					xe.printStackTrace();
				}
			}
		}
		return result;
	}
	
	public void executeXUpdate(String collectionId, String xUpdateExpression, String documentId) throws XMLDBException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        
		conn = AuthenticationUtilities.loadProperties();
        Class<?> cl = Class.forName(conn.driver);
        Database database = (Database) cl.newInstance();
        database.setProperty("create-database", "true");
        DatabaseManager.registerDatabase(database);
        
        Collection col = null;
        
        try {
            col = DatabaseManager.getCollection(conn.uri + collectionId, conn.username, conn.password);
            col.setProperty("indent", "yes");
            
            XUpdateQueryService xupdateService = (XUpdateQueryService) col.getService("XUpdateQueryService", "1.0");
            xupdateService.setProperty("indent", "yes");
            xupdateService.updateResource(documentId, xUpdateExpression);
        } finally {
            if(col != null) {
                try { 
                	col.close();
                } catch (XMLDBException xe) {
                	xe.printStackTrace();
                }
            }
        }
	}

	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	private Collection getOrCreateCollection(String collectionUri) throws XMLDBException {
		return getOrCreateCollection(collectionUri, 0);
	}

	private Collection getOrCreateCollection(String collectionUri, int pathSegmentOffset) throws XMLDBException {

		Collection col = DatabaseManager.getCollection(conn.uri + collectionUri, conn.username, conn.password);

		// create the collection if it doesn't exist
		if (col == null) {
			if (collectionUri.startsWith("/"))
				collectionUri = collectionUri.substring(1);

			String pathSegments[] = collectionUri.split("/");
			if (pathSegments.length > 0) {
				StringBuilder path = new StringBuilder();
				for (int i = 0; i <= pathSegmentOffset; i++)
					path.append("/" + pathSegments[i]);

				Collection startCol = DatabaseManager.getCollection(conn.uri + path, conn.username, conn.password);
				
				// if child collection doesn't exist
				if (startCol == null) {
					String parentPath = path.substring(0, path.lastIndexOf("/"));
					Collection parentCol = DatabaseManager.getCollection(conn.uri + parentPath, conn.username, conn.password);
					CollectionManagementService cms = (CollectionManagementService) parentCol.getService("CollectionManagementService", "1.0");
					
					col = cms.createCollection(pathSegments[pathSegmentOffset]);
					col.close();
					parentCol.close();
				} else {
					startCol.close();
				}
			}
			return getOrCreateCollection(collectionUri, ++pathSegmentOffset);
		} else {
			return col;
		}
	}
	
}
