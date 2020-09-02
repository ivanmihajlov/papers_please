package com.ftn.papers_please.fuseki;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FusekiAuthenticationUtilities {

	static public class FusekiConnectionProperties {

		public String endpoint;
		public String dataset;
		public String queryEndpoint;
		public String updateEndpoint;
		public String dataEndpoint;
		
		public FusekiConnectionProperties(Properties props) {
			super();
			dataset = props.getProperty("conn.dataset").trim();
			endpoint = props.getProperty("conn.endpoint").trim();
			queryEndpoint = String.join("/", endpoint, dataset, props.getProperty("conn.query").trim());
			updateEndpoint = String.join("/", endpoint, dataset, props.getProperty("conn.update").trim());
			dataEndpoint = String.join("/", endpoint, dataset, props.getProperty("conn.data").trim());
		}
	}

	public static FusekiConnectionProperties loadProperties() throws IOException {
		String propsName = "fuseki.properties";
		InputStream propsStream = openStream(propsName);
		if (propsStream == null)
			throw new IOException("Couldn't read properties from " + propsName);

		Properties props = new Properties();
		props.load(propsStream);
		return new FusekiConnectionProperties(props);
	}

	public static InputStream openStream(String fileName) throws IOException {
		return FusekiAuthenticationUtilities.class.getClassLoader().getResourceAsStream(fileName);
	}
	
}
