package com.bluecheese.server.tools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

public class BlobToByteArray {

	public BlobToByteArray() {
		// TODO Auto-generated constructor stub
	}

	public static byte[] getByteArray(Blob blob) throws SQLException, IOException {
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
