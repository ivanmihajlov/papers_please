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

import com.ftn.papers_please.exceptions.DatabaseException;
import com.ftn.papers_please.model.user.TUser;
import com.ftn.papers_please.util.DBManager;

@Repository
public class UserRepository {
	
	@Autowired
	private DBManager dbManager;
	
	@Value("${user-collection-id}")
	private String userCollectionId;

	public TUser findByUsername(String username) {
		try {
			String xPathExpression = String.format("//user[username='%s']", username);
			ResourceSet result = dbManager.executeXPath(userCollectionId, xPathExpression);
			if (result == null)
				return null;
			
			ResourceIterator i = result.getIterator();
            Resource res = null;
            TUser user = null;
            
            while(i.hasMoreResources()) {
            	try {
                    res = i.nextResource();
                    user = unmarshallUser(res.getContent().toString());
                } finally {
                    try { 
                    	((EXistResource)res).freeResources(); 
                    } catch (XMLDBException e) {
                    	e.printStackTrace();
                    }
                }
            }
            return user;
		} catch (Exception e) {
			throw new DatabaseException("Finding user by username failed!");
		}
	}

	public TUser findById(String userId) {
		try {
			String xPathExpression = String.format("//user[@user_id='%s']", userId);
			ResourceSet result = dbManager.executeXPath(userCollectionId, xPathExpression);
			if (result == null)
				return null;

			ResourceIterator i = result.getIterator();
			Resource res = null;
			TUser user = null;

			while(i.hasMoreResources()) {
				try {
					res = i.nextResource();
					user = unmarshallUser(res.getContent().toString());
				} finally {
					try {
						((EXistResource)res).freeResources();
					} catch (XMLDBException e) {
						e.printStackTrace();
					}
				}
			}
			return user;
		} catch (Exception e) {
			throw new DatabaseException("Finding user by ID failed!");
		}
	}

	public List<TUser> findAll() {
		try {
			String xPathExpression = "/user";
			ResourceSet result = dbManager.executeXPath(userCollectionId, xPathExpression);
			if (result == null)
				return null;

			ResourceIterator i = result.getIterator();
			Resource res = null;
			List<TUser> users = new ArrayList<>();

			while(i.hasMoreResources()) {
				try {
					res = i.nextResource();
					TUser user = unmarshallUser(res.getContent().toString());
					users.add(user);
				} finally {
					try {
						((EXistResource)res).freeResources();
					} catch (XMLDBException e) {
						e.printStackTrace();
					}
				}
			}
			return users;
		} catch (Exception e) {
			throw new DatabaseException("Finding all users failed!");
		}
	}

	public void save(TUser user) {
		try {
			ResourceSet rs = dbManager.executeXQuery(userCollectionId, "count(/.)", new HashMap<>(), "");
			String id = "user" + rs.getIterator().nextResource().getContent().toString();
			
			user.setUserId(id);
			String userXML = marshallUser(user);
			dbManager.save(userCollectionId, id, userXML);
		} catch (JAXBException e) {
			throw new DatabaseException("Marshalling new user failed!");
		} catch (Exception e) {
			throw new DatabaseException("User registration failed!");
		}
	}

	public void update(TUser user) {
		try {
			String userXML = marshallUser(user);
			dbManager.save(userCollectionId, user.getUserId(),  userXML);
		} catch (JAXBException e) {
			throw new DatabaseException("Marshalling user failed!");
		} catch (Exception e) {
			throw new DatabaseException("User update failed!");
		}
	}
	
	private String marshallUser(TUser user) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(TUser.class);
	 	Marshaller marshaller = context.createMarshaller();
	 	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	 	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	 	
        marshaller.marshal(user, stream);
        return new String(stream.toByteArray());
	}	
	
	private TUser unmarshallUser(String userXML) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(TUser.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return (TUser) unmarshaller.unmarshal(new StringReader(userXML));
	}

}
