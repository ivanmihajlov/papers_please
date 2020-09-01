package com.ftn.papers_please.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AuthenticationUtilities {
	
	private static String connectionUri = "xmldb:exist://%1$s:%2$s/exist/xmlrpc";

	static public class ConnectionProperties {

		public String host;
		public int port;
		public String username;
		public String password;
		public String driver;
		public String uri;

		public ConnectionProperties(Properties props) {
			super();
			username = props.getProperty("conn.username").trim();
			password = props.getProperty("conn.password").trim();
			host = props.getProperty("conn.host").trim();
			port = Integer.parseInt(props.getProperty("conn.port"));
			uri = String.format(connectionUri, host, port);
			driver = props.getProperty("conn.driver").trim();
		}
	}

	public static ConnectionProperties loadProperties() throws IOException {
		String filename = "exist.properties";
		InputStream stream = openStream(filename);
		if (stream == null)
			throw new IOException("Failed to read properties file " + filename);

		Properties props = new Properties();
		props.load(stream);

		return new ConnectionProperties(props);
	}

	public static InputStream openStream(String fileName) throws IOException {
		return AuthenticationUtilities.class.getClassLoader().getResourceAsStream(fileName);
	}
	
}
