package com.bluecheese.server.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class UpdateProfileServlet extends HttpServlet{

	private static final String userName = "root";
	private static final String passWord = "";
	public UpdateProfileServlet() {
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
			
			String uid = data.getString("uid");
			String old_uname = data.getString("old_uname");
			String new_uname = data.getString("new_uname");
			
			//check if id, name are legal
			
			//update user profile
			if(updateProfile(conn, uid, old_uname, new_uname)) {
				result.put("status", true);
				result.put("log", "Profile Change Success!");
			}
			else {
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
	
	

	private boolean updateProfile(Connection conn, String uid, String old_uname, String new_uname) throws SQLException {
		String statement = "UPDATE User SET uname = ? WHERE uid = ? AND uname = ?";
		PreparedStatement stmt = conn.prepareStatement(statement);
		stmt.setString(1, new_uname);
		stmt.setString(2, uid);
		stmt.setString(3, old_uname);
		int suc = stmt.executeUpdate();
		if(suc == 1)
			return true;
		else
			return false;
	}
}
