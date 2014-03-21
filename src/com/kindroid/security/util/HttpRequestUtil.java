/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpRequestUtil {

	public final static String boundary = "---------------------------7d71b526e00e4";
	public final static String prefix = "--";
	public final static String newLine = "\r\n";
	
	public final static String fileContentType = "Content-Type: application/octet-stream\r\n\r\n";
	public final static String fileContentDisposition = "Content-Disposition: form-data; name=\"%name\"; filename=\"%fileName\"\r\n";
	public final static String paramContentDisposition = "Content-Disposition: form-data; name=\"%name\"\r\n\r\n";
	
	public static Map<String, Object> getData (String urlStr, int timeout) throws IOException {
		URL url = new URL(urlStr);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);
		connection.connect();
		
		Map<String, Object> res = fetchResp(connection);
		connection.disconnect();
		
		return res;
	}
	
	public static Map<String, Object> postData (String urlStr, Map<String, String> paramMap, int timeout) throws IOException {
		if (null == urlStr || "".equals(urlStr) || null == paramMap) {
			return null;
		}
		
		String postData = null;
		for (String paramName : paramMap.keySet()) {
			String paramData = paramMap.get(paramName);
			if (null == postData) {
				postData = URLEncoder.encode(paramName, "UTF-8") + "=" + URLEncoder.encode(paramData, "UTF-8");
			} else {
				postData = "&" + URLEncoder.encode(paramName, "UTF-8") + "=" + URLEncoder.encode(paramData, "UTF-8");
			}
		}
		
		URL url = new URL(urlStr);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);
		
		if (null != postData) {
			connection.getOutputStream().write(postData.getBytes());
		}
		connection.getOutputStream().flush();
		connection.getOutputStream().close();
		
		Map<String, Object> res = fetchResp(connection);
		connection.disconnect();
		
		return res;
	}

	public static Map<String, Object> postData (String urlStr, Map<String, String> paramMap, Map<String, File> fileMap, int timeout) throws IOException {
		if (null == urlStr || "".equals(urlStr) || null == paramMap || null == fileMap) {
			return null;
		}
		
		URL url = new URL( urlStr );
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);
		
		upload(connection, paramMap, fileMap);
		Map<String, Object> res = fetchResp((HttpURLConnection) connection);
		connection.disconnect();
		
		return res;
	}
	
	public static void upload(HttpURLConnection connection, Map<String, String> paramMap, Map<String, File> fileMap) throws IOException {
		// set property
		connection.setDoOutput( true );
		//connection.setRequestProperty( "Accept", "*/*" );
		//connection.setRequestProperty( "Accept-Language", "ja" );
		//connection.setRequestProperty( "Accept-Encoding", "gzip, deflate" );
		//connection.setRequestProperty( "Connection", "Keep-Alive" );
		//connection.setRequestProperty( "Cache-Control", "no-cache" );
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		
		// write data
		DataOutputStream dos = new DataOutputStream( connection.getOutputStream() );
		for (Iterator<String> iterator = fileMap.keySet().iterator(); iterator.hasNext(); ) {
			String key = iterator.next();
			File file = fileMap.get(key);
			
			String fcd = fileContentDisposition.replaceAll("%name", key);
			fcd = fcd.replaceAll("%fileName", file.getName());
		
			dos.writeBytes(prefix + boundary + newLine);
			dos.writeBytes(fcd);
			dos.writeBytes(fileContentType);
	       
			FileInputStream fstram = new FileInputStream(file);
			byte[] buf = new byte[4000];
			int len = 0;
			while (-1 != len) {
				len = fstram.read(buf);
				if(len > 0){
					dos.write(buf, 0, len);
				}
			}
			dos.writeBytes(newLine);
			fstram.close();
		}
	     
		for (Iterator<String> iterator = paramMap.keySet().iterator(); iterator.hasNext(); ) {
			String key = iterator.next();
			String value = paramMap.get(key);
	      
			String fcd = paramContentDisposition.replaceAll("%name", key);
			dos.writeBytes(prefix + boundary + newLine);
			dos.writeBytes(fcd);
			dos.writeBytes(value);
			dos.writeBytes(newLine);
		}
		
		dos.writeBytes(prefix + boundary + prefix + newLine);
		dos.close();
	}
	
	public static Map<String, Object> fetchResp (HttpURLConnection connection) throws IOException {
		Map<String, Object> res = new HashMap<String, Object> ();
		
		byte[] content = readData(connection.getInputStream());
		int code = connection.getResponseCode();
		String contentType = connection.getHeaderField("Content-Type");
		
		connection.disconnect();
		
		res.put("Content", content);
		res.put("Status", code);
		res.put("Content-Type", contentType);
		
		return res;
	}
	
	public static byte[] readData (InputStream is) {
		byte[] data = null;
		byte[] temp = new byte[1024];
		BufferedInputStream bis = new BufferedInputStream(is);  
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
        int size = 0;
        
        try {
	        while ((size = bis.read(temp, 0, 1024)) != -1) {
	              baos.write(temp, 0, size);
	        }
	        data = baos.toByteArray();
	        bis.close();
	        is.close();
	        baos.close();
        } catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	public InputStream getInputStream (URLConnection uc) throws Exception{
	    if (uc==null) {
	      return null;
	    }
	    
	    return uc.getInputStream();
	}
	
}


