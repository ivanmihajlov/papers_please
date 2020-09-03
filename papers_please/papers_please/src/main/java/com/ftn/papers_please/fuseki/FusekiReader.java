package com.ftn.papers_please.fuseki;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.StringSubstitutor;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.RDFNode;

import com.ftn.papers_please.fuseki.FusekiAuthenticationUtilities.FusekiConnectionProperties;
import com.ftn.papers_please.util.DBManager;

public class FusekiReader {
	
	private FusekiReader() {}
	
	public static Set<String> executeQuery(String filePath, Map<String, String> params) throws IOException {
		FusekiConnectionProperties conn = FusekiAuthenticationUtilities.loadProperties();
		
		// read SPARQL query from the file
		String sparqlQueryTemplate = DBManager.readFile(filePath, StandardCharsets.UTF_8);
		String sparqlQuery = StringSubstitutor.replace(sparqlQueryTemplate, params, "{{", "}}");
		System.out.println("\nSPARQL query:\n" + sparqlQuery);
		
		// create a QueryExecution which will access the SPARQL service via HTTP
		QueryExecution query = QueryExecutionFactory.sparqlService(conn.queryEndpoint, sparqlQuery);
		ResultSet results = query.execSelect();
		Set<String> papers = new HashSet<>(); // set of paper URLs, e.g. https://github.com/ivanmihajlov/papers_please/papers/paper123
		String varName;
		RDFNode varValue;
		
		while(results.hasNext()) {
			QuerySolution querySolution = results.next();
			Iterator<String> variableBindings = querySolution.varNames();
		    while (variableBindings.hasNext()) {
		    	varName = variableBindings.next();
		    	varValue = querySolution.get(varName);
		    	if (varName.equals("paper"))
		    		papers.add(varValue.toString());
		    }
		}
		
		System.out.println("\nSPARQL query result:");
		ResultSetFormatter.outputAsXML(System.out, results);
		
		query.close();
		return papers;
	}

}
