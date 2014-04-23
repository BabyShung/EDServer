package com.bluecheese.server.servlets;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class ChangeSelfieServlet extends HttpServlet{

	private static final String userName = "root";
	private static final String passWord = "";
	private static final int MAX_SELFIE_SIZE = 10 * 1024 * 1024; //maximun size of selfie is 10MB
	public ChangeSelfieServlet() {
		// TODO Auto-generated constructor stub
	
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String url = null;
		Connection conn = null;
		JSONObject result = new JSONObject();
		//set the header of http response
		resp.setContentType("application/json;charset=utf-8");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter writer = resp.getWriter();
		//read the input request and transfer to a json object 
		try {
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
			String s = null;
			while((s = br.readLine()) != null) {
				sb.append(s);
			}
			JSONObject data = new JSONObject(sb.toString());
			//check validation of selfie
			String uid = data.getString("uid");
			if(uid == null || uid.equals("")) {
				result.put("status", false);
				result.put("log", "Invalid uid!");
				writer.println(result);
				writer.flush();
				return;
			}

			JSONArray jarray = data.getJSONArray("uselfie");
			byte[] selfie = new byte[jarray.length()];
			for(int i = 0; i < selfie.length; i++) {
				selfie[i] = Byte.parseByte(jarray.get(i).toString());
			}
			
//			byte[] selfie = (byte[])data.get("uselfie");
			if(data.isNull("uselfie") || selfie.length > MAX_SELFIE_SIZE) {
				result.put("status", false);
				result.put("log", "Invalid picture!");
				writer.println(result);
				writer.flush();
				return;
			} 
			//connect to database
			try {
				if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
		    		// Load the class that provides the new "jdbc:google:mysql://" prefix.
					
		    		Class.forName("com.mysql.jdbc.GoogleDriver");
		    		url = "jdbc:google:mysql://edible-bluecheese-server:bluecheese/bluecheese";
		    	} else {
		    		// Local MySQL instance to use during development.
		    		Class.forName("com.mysql.jdbc.Driver");
		    		url = "jdbc:mysql://127.0.0.1:3721/bluecheese";
		    	}
				conn = DriverManager.getConnection(url, userName, passWord);
			} catch (Exception e) {
				e.printStackTrace();
				result.put("status", false);
				result.put("log", e.toString());
				writer.println(result);
				return;
			}
			if(!checkIDExistence(conn, uid)) {
				result.put("status", false);
				result.put("log", "User ID does not exist!");
				writer.println(result);
				writer.flush();
				return;
			}
			String statement = "UPDATE User SET uselfie = ? WHERE uid = ?";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setBlob(1, new ByteArrayInputStream(selfie));
			stmt.setString(2, uid);
			int suc = stmt.executeUpdate();
			if(suc == 1) {
				result.put("status", true);
				result.put("log", "Selfie Change Success!");
				result.put("uselfie", selfie);
			} else {
				result.put("status", false);
				result.put("log", "Error Occurs!");
			}
			
			writer.println(result);
			writer.flush();
			
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean checkIDExistence(Connection conn, String uid) throws SQLException {
		
		String statement = "SELECT uid FROM User WHERE uid = ?";
		PreparedStatement stmt = conn.prepareStatement(statement);
		stmt.setString(1, uid);
		ResultSet rs = stmt.executeQuery();
		return rs.next();
	}

}
