package com.ftn.papers_please.fuseki;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.springframework.stereotype.Component;

import com.ftn.papers_please.fuseki.FusekiAuthenticationUtilities.FusekiConnectionProperties;

@Component
public class FusekiManager {
	
	public void saveMetadata(String rdfFilePath, String graphUri) throws IOException {
		FusekiConnectionProperties conn = FusekiAuthenticationUtilities.loadProperties();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Model model = ModelFactory.createDefaultModel();
		model.read(rdfFilePath);
		model.write(out, SparqlUtil.NTRIPLES);

		UpdateRequest request = UpdateFactory.create() ;
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, conn.updateEndpoint);
		processor.execute();

		String sparqlUpdate = SparqlUtil.insertData(conn.dataEndpoint + graphUri, new String(out.toByteArray()));

		UpdateRequest update = UpdateFactory.create(sparqlUpdate);
		processor = UpdateExecutionFactory.createRemote(update, conn.updateEndpoint);
		processor.execute();
	}
	
}
