package com.bluecheese.server.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Blob;
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
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class SearchUserServlet extends HttpServlet{

	private static final String userName = "root";
	private static final String passWord = "";

	public SearchUserServlet() {
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
		String request = ""; //this is what user searching for
		JSONObject result = new JSONObject();
		JSONArray userArray = new JSONArray(); 
	
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
				if(!data.isNull("request") && !data.getString("request").equals("")){
					request = data.getString("request");
				}
				else {
					result.put("status", false);
					result.put("log", "Invalid user request!");
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
				writer.flush();
				return;
			}			
			
			//check if request is legal
			
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
				writer.flush();
				return;
			}
			
			
			String statement = "SELECT uid, uname, ucreate_time, utype, uselfie FROM User WHERE uid = ? OR uname = ?";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1, request);
			stmt.setString(2, request);
			ResultSet rs = stmt.executeQuery();
			result.put("status", false);
			int num = 0;
			while(rs.next()) {
				num ++;
				result.put("status", true);
				JSONObject json = new JSONObject();
				json.put("uid", rs.getString("uid"));
				json.put("uname", rs.getString("uname"));
				json.put("ucreate_time", rs.getDate("ucreate_time").toString());
				json.put("utype", rs.getInt("utype"));
				Blob blob = rs.getBlob("uselfie");
				if(blob != null) {
					byte[] selfie = getByteArray(blob);
					json.put("uselfie", selfie);
				}
				else {
					json.put("uselfie", JSONObject.NULL);
				}
				userArray.put(json);
			}
			result.put("log", num + " Users in total!");
			result.put("results", userArray);
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
	
	private byte[] getByteArray(Blob blob) throws SQLException, IOException {
		BufferedInputStream is = new BufferedInputStream(blob.getBinaryStream());
  	  	byte[] imgData = new byte[(int) blob.length()];
  	  	int len = imgData.length;
  	  	int offset = 0;
  	  	int read = 0;
  	  	while (offset < len && (read = is.read(imgData, offset, len - offset)) >= 0) {  
  		  	offset += read;  
  	  	} 
  	  	return imgData;
	}

}
