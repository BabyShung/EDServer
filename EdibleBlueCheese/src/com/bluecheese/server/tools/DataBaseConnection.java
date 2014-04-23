package com.bluecheese.server.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.google.appengine.api.utils.SystemProperty;

public class DataBaseConnection {

//	private static final String USER_NAME = "root";
//	private static final String PASS_WORD = "";
	
	public DataBaseConnection() {
		// TODO Auto-generated constructor stub
	}

	public static Connection createConnection() throws ClassNotFoundException, SQLException {
		String url = "";
		Connection conn = null;
		String user_name = "";
		String pass_word = "";
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
    		// Load the class that provides the new "jdbc:google:mysql://" prefix.
    		Class.forName("com.mysql.jdbc.GoogleDriver");
    		url = "jdbc:google:mysql://edible-bluecheese-server:bluecheese/bluecheese";
    		user_name = "root";
    		pass_word = "";
    	} else {
    		// Local MySQL instance to use during development.
    		Class.forName("com.mysql.jdbc.Driver");
    		url = "jdbc:mysql://127.0.0.1:3721/bluecheese";
    		user_name = "root";
    		pass_word = "edible2014";
    	}
		conn = DriverManager.getConnection(url, user_name, pass_word);
		return conn;
	}
}
