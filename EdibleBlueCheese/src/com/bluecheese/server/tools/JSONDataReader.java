package com.bluecheese.server.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class JSONDataReader {

	public JSONDataReader() {
		// TODO Auto-generated constructor stub
	}
	
	public static JSONObject readData(HttpServletRequest req) throws IOException, JSONException {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
		String s = null;
		while((s = br.readLine()) != null) {
			sb.append(s);
		}
		JSONObject data = new JSONObject(sb.toString());
		return data;
	}

}
