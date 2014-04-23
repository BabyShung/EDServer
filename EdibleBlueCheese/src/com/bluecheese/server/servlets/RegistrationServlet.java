package com.bluecheese.server.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bluecheese.server.tools.User;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class RegistrationServlet extends HttpServlet{

	private static final String userName = "root";
	private static final String passWord = "";
//	private static final String passWord = "edible2014";

	public RegistrationServlet() {
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
		String uid = "";
		String upwd = "";
		String uname = "";
		JSONObject result = new JSONObject();
		//set the header of http response
		resp.setContentType("application/json;charset=utf-8");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter writer = resp.getWriter();
		//read the input request and transfer to a json object 
		try {
			try {
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
				String s = null;
				while((s = br.readLine()) != null) {
					sb.append(s);
				}
				JSONObject data = new JSONObject(sb.toString());
				if(!data.isNull("uid") && !data.isNull("upwd") && !data.isNull("uname")
						&& !data.getString("uid").equals("") && !data.getString("upwd").equals("") && !data.getString("uname").equals("")) {
					uid = data.getString("uid");
					upwd = data.getString("upwd");
					uname = data.getString("uname");
				}
				else {
					result.put("status", false);
					result.put("log", "Invalid user id or password!");
					writer.println(result);
					writer.flush();
					return;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result.put("status", false);
				result.put("log", e.toString());
				writer.println(result);
				return;
			}			
			
			//check if user id, password and name are legal
			
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
			
			//check if this user id has been used
			if(checkIDExistence(conn, uid) == true) {
				result.put("status", false);
				result.put("log", "This id has been used!");
			}
			else {
				Date currentDate = new Date();
				String statement = "INSERT INTO User (uid, upwd, uname, utype, ucreate_time) VALUES (?, ?, ?, ?, ?)";
				PreparedStatement stmt = conn.prepareStatement(statement);
				stmt.setString(1, uid);
				stmt.setString(2, upwd);
				stmt.setString(3, uname);
				stmt.setInt(4, User.Common);
				stmt.setDate(5, new java.sql.Date(currentDate.getTime()));
				int suc = stmt.executeUpdate();
				if(suc == 1) {
					result.put("status", true);
					result.put("log", "Registration Success!");
					result.put("uid", uid);
					result.put("uname", uname);
					result.put("utype", User.Common);
					result.put("ucreate_time", currentDate.toString());
					result.put("uselfie", JSONObject.NULL);
				} else {
					result.put("status", false);
					result.put("log", "Error Occurs!");
				}
			}
			
			writer.println(result);
			writer.flush();
			
			
		} catch (JSONException | SQLException e) {
			// TODO Auto-generated catch block
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
