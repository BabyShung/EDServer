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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class ChangePwdServlet extends HttpServlet{

	private static final String userName = "root";
	private static final String passWord = "";

	public ChangePwdServlet() {
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
		String old_upwd = "";// this is the old password
		String new_upwd = "";
		String new_upwd_retype = "";
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
				if(!data.isNull("uid") && !data.isNull("old_upwd") && !data.isNull("new_upwd") && !data.isNull("new_upwd_retype")
						&& !data.getString("uid").equals("") && !data.getString("old_upwd").equals("") && !data.getString("new_upwd").equals("")
						&& !data.getString("new_upwd_retype").equals("") ) {
					uid = data.getString("uid");
					old_upwd = data.getString("old_upwd");
					new_upwd = data.getString("new_upwd");
					new_upwd_retype = data.getString("new_upwd_retype");
				}
				else {
					result.put("status", false);
					result.put("log", "Invalid user ID or password!");
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
			
			//check if user id, password are legal
			
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
			
			
			
			//check old password match database
			if(checkOldPwd(conn, uid, old_upwd)) {
				//check if the two new passwords match
				if(new_upwd.equals(new_upwd_retype)){
					//update pwd
					String statement = "UPDATE User SET upwd = ? WHERE uid = ?";
					PreparedStatement stmt = conn.prepareStatement(statement);
					stmt.setString(1, new_upwd);
					stmt.setString(2, uid);
					if(stmt.executeUpdate() > 0) {
						result.put("status", true);
						result.put("log", "Password Update Success!");
					} else {
						result.put("status", false);
						result.put("log", "Password Update Failed!");
					}
				} else{
					result.put("status", false);
					result.put("log", "Two Times Typing of New Password are not matching!");
				}			
			} else {
				// fail
				result.put("status", false);
				result.put("log", "Invalid User Password!");
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
	

	public boolean checkOldPwd(Connection conn,String uid, String old_upwd) throws SQLException {
		
		String statement = "SELECT upwd FROM User WHERE uid = ?";
		PreparedStatement stmt = conn.prepareStatement(statement);
		stmt.setString(1, uid);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			if(rs.getString("upwd").equals(old_upwd)) {
				return true;
			}
			else 
				return false;
		}
		return false;
	}
	

}
