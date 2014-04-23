package com.bluecheese.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bluecheese.server.tools.Card;
import com.bluecheese.server.tools.DataBaseConnection;
import com.bluecheese.server.tools.JSONDataReader;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class CreateCardServlet extends HttpServlet{

	public CreateCardServlet() {
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
		//set the header of response and get the writer
		resp.setContentType("application/json;charset=utf-8");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter writer = resp.getWriter();
		try {
			try {
				JSONObject data = JSONDataReader.readData(req);
				JSONObject result = new JSONObject();
				String uid = data.getString("uid");
				String en = data.getString("en");
				String cn = data.getString("cn");
				String memo = data.getString("memo");
				String create_location = data.getString("create_location");
				int ctype = Card.COMMON;
				int avg_score = 0;
				Date current_time = new Date();
				
				if(uid == null || en == null || cn == null) {
					result.put("status", false);
					result.put("log", "Invalid Data");
					writer.println(result);
					writer.flush();
					return;
				}
				
				//check if uid, en and cn are legal
				
				Connection conn = DataBaseConnection.createConnection();
				if(checkExistence(conn, en)) {
					result.put("status", false);
					result.put("log", "Card has already existed!");
					writer.println(result);
					writer.flush();
					return;
				}
				else {
					//insert an instance into Card table
					String statement = "INSERT INTO Card (en, cn, memo, ctype, avg_score) VALUES (?, ?, ?, ?, ?)";
					PreparedStatement stmt = conn.prepareStatement(statement);
					stmt.setString(1, en);
					stmt.setString(2, cn);
					stmt.setString(3, memo);
					stmt.setInt(4, ctype);
					stmt.setInt(5, avg_score);
					int succ = stmt.executeUpdate();
					if(succ > 0) {
						//if card insertion succeeds, update corresponding instance in Card_Creation table
						//first get cid
						int cid = getCardID(conn, en);
						if(cid == -1) {
							result.put("status", false);
							result.put("log", "Error Occurs!");
							writer.println(result);
							writer.flush();
							return;
						} else {
							//update corresponding instance in Card_Creation table
							statement = "UPDATE Card_Creation SET uid = ?, create_date = ?, create_location = ? WHERE cid = ?";
							stmt = conn.prepareStatement(statement);
							stmt.setString(1, uid);
							stmt.setDate(2, new java.sql.Date(current_time.getTime()));
							stmt.setString(3, create_location);
							stmt.setInt(4, cid);
							stmt.executeUpdate();
							
							//return the detail of the card
							statement = "SELECT * FROM Card, Card_Creation WHERE Card.cid = Card_Creation.cid and Card.cid = ?";
							stmt = conn.prepareStatement(statement);
							stmt.setInt(1, cid);
							ResultSet rs = stmt.executeQuery();
							if(rs.next()) {
								result.put("status", true);
								result.put("log", "Card Creation Success!");
								result.put("cid", rs.getInt("Card.cid"));
								result.put("en", rs.getString("en"));
								result.put("cn", rs.getString("cn"));
								result.put("memo", rs.getString("memo"));
								result.put("ctype", rs.getInt("ctype"));
								result.put("avg_score", rs.getInt("avg_score"));
								result.put("creator", rs.getString("uid"));
								result.put("creat_date", new Date(rs.getDate("create_date").getTime()).toString());
								result.put("creat_location", rs.getString("create_location"));
								writer.println(result);
								writer.flush();
							} else {
								result.put("status", false);
								result.put("log", "Error Occurs!");
								writer.println(result);
								writer.flush();
								return;
							}
							
							
						}
					} else {
						result.put("status", false);
						result.put("log", "Error Occurs!");
						writer.println(result);
						writer.flush();
						return;
					}
				}
				
				
				
				
			} catch(Exception e) {
				e.printStackTrace();
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			
		}
		
		
		
		
	}
	

	
	private boolean checkExistence(Connection conn, String en) throws SQLException {
		String statement = "SELECT uid FROM Card WHERE en = ?";
		PreparedStatement stmt = conn.prepareStatement(statement);
		stmt.setString(1, en);
		ResultSet rs = stmt.executeQuery();
		return rs.next();
	}
	
	private int getCardID(Connection conn, String en) throws SQLException {
		String statement = "SELECT cid FROM Card WHERE en = ?";
		PreparedStatement stmt = conn.prepareStatement(statement);
		stmt.setString(1, en);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			return rs.getInt("cid");
		}
		return -1;
	}
 }
