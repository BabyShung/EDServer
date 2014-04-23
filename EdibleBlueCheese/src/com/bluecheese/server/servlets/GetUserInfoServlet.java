package com.bluecheese.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bluecheese.server.tools.BlobToByteArray;
import com.bluecheese.server.tools.DataBaseConnection;
import com.bluecheese.server.tools.JSONDataReader;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class GetUserInfoServlet extends HttpServlet{

	public GetUserInfoServlet() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter writer = resp.getWriter();
		String uid = req.getParameter("uid");
		JSONObject result = new JSONObject();
		Connection conn = null;
		try {
			try {
				if(uid != null && !uid.equals("")) {
					//check if the uid is legal
					
					//connect to database and get the info of the specific user
					conn = DataBaseConnection.createConnection();
					String statement = "SELECT uid, uname, ucreate_time, utype, uselfie FROM User WHERE uid = ?";
					PreparedStatement stmt = conn.prepareStatement(statement);
					stmt.setString(1, uid);
					ResultSet rs = stmt.executeQuery();
					result.put("status", false);
					JSONArray userArray = new JSONArray();
					while(rs.next()) {
						result.put("status", true);
						JSONObject json = new JSONObject();
						json.put("uid", rs.getString("uid"));
						json.put("uname", rs.getString("uname"));
						json.put("ucreate_time", rs.getDate("ucreate_time").toString());
						json.put("utype", rs.getInt("utype"));
						Blob blob = rs.getBlob("uselfie");
						if(blob != null) {
							byte[] selfie = BlobToByteArray.getByteArray(blob);
							json.put("uselfie", selfie);
						}
						else {
							json.put("uselfie", JSONObject.NULL);
						}
						userArray.put(json);
					}
					result.put("log", rs.getRow() + " Users in total are found!");
					result.put("results", userArray);
					writer.println(result);
					writer.flush();
				}
				else {
					result.put("status", false);
					result.put("log", "Invalid User ID!");
					writer.println(result);
					writer.flush();
					return;
				}
			} catch(Exception e) {
				e.printStackTrace();
				result.put("status", false);
				result.put("log", e.toString());
				writer.println(result);
				writer.flush();
				return;
			}
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

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter writer = resp.getWriter();
		JSONObject result = new JSONObject();
		Connection conn = null;
		try {
			try {
				String uid = JSONDataReader.readData(req).getString("uid");
				if(uid != null && !uid.equals("")) {
					//check if the uid is legal
					
					//connect to database and get the info of the specific user
					conn = DataBaseConnection.createConnection();
					String statement = "SELECT uid, uname, ucreate_time, utype, uselfie FROM User WHERE uid = ?";
					PreparedStatement stmt = conn.prepareStatement(statement);
					stmt.setString(1, uid);
					ResultSet rs = stmt.executeQuery();
					result.put("status", false);
					JSONArray userArray = new JSONArray();
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
							byte[] selfie = BlobToByteArray.getByteArray(blob);
							json.put("uselfie", selfie);
						}
						else {
							json.put("uselfie", JSONObject.NULL);
						}
						userArray.put(json);
					}
					result.put("log", num + " Users in total are found!");
					result.put("results", userArray);
					writer.println(result);
					writer.flush();
				}
				else {
					result.put("status", false);
					result.put("log", "Invalid User ID!");
					writer.println(result);
					writer.flush();
					return;
				}
			} catch(Exception e) {
				e.printStackTrace();
				result.put("status", false);
				result.put("log", e.toString());
				writer.println(result);
				writer.flush();
				return;
			}
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
	
	

}
