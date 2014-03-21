/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import javax.xml.parsers.ParserConfigurationException;

import com.kindroid.security.data.SystemData;
import com.kindroid.security.util.Appstore.GetImageRequest;
import com.kindroid.security.util.Appstore.Request;
import com.kindroid.security.util.Appstore.GetImageRequest.AppImageUsage;

public class HttpRequest {
	
	public final static String GUEST_TOKEN = "MHx8MHx8MHx8MjAxMS0wNS0yMyAyMDowNDowMXx8TXNnZ0FZVC9NUG9jVW1jUUZQbFlBOE1zWUFxNm40RTFkVTMvWGhXdGxrSmdpWmpQT3FpVjVPTWN3TzFUSU9oNWJiakYreERmSU4xcQ0KYXpUZlM3bEpqQVhERkdyZTB4QU9acTBONTVGUjZtc1VBOXRXVWhKVXhOMVh2dk9wM1hRMTltWkZpOE9BNFluZkhFL0N0UUFrU3BWbw0KSHB3aWZTQzhsTVZscXNqUGQrST0=";

	public static String getData(String urlStr) throws IOException{
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		connection.setRequestProperty("Cache-Control","no-cache");
		connection.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
		String result = "";
		String lines;
		while ((lines = reader.readLine()) != null){
			result += lines;
		}
		reader.close();
		connection.disconnect();
		return result;
	}
	public static InputStream postData(String urlStr,Request.Builder request) throws ParserConfigurationException, IOException,ConnectTimeoutException,UnknownHostException{
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		setHeader(connection);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(10000);
		connection.getOutputStream().write("version=1".getBytes());
		String token = TextUtils.isEmpty(SystemData.getToken())?SystemData.getGuestToken():SystemData.getToken(); 
		connection.getOutputStream().write(("&token="+token).getBytes());
		connection.getOutputStream().write("&".getBytes());
		
		String reqBase = new String(Base64.encodeBase64(request.build().toByteArray(),true)).toString();
		String requestContent="request="+URLEncoder.encode(reqBase);
		
		connection.getOutputStream().write(requestContent.getBytes());
		
		connection.getOutputStream().flush();
		connection.getOutputStream().close();
		return connection.getInputStream();
	}
	
	public static InputStream request(String appId,int which) throws ParserConfigurationException, IOException,ConnectTimeoutException,UnknownHostException{
		Request.Builder requestBuild=Request.newBuilder();
		GetImageRequest.Builder imageRequest=GetImageRequest.newBuilder();
		imageRequest.setAppId(appId);
		imageRequest.setImageId(String.valueOf(which));
		imageRequest.setImageUsage(AppImageUsage.SCREENSHOT);
		requestBuild.setImageRequest(imageRequest.build());
		
		URL url = new URL(Constant.APP_IMAGE_URL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		HttpRequest.setHeader(connection);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(10000);
		connection.getOutputStream().write("version=1".getBytes());
		connection.getOutputStream().write(("&token="+GUEST_TOKEN).getBytes());
		connection.getOutputStream().write("&".getBytes());
		
		String reqBase = new String(Base64.encodeBase64(requestBuild.build().toByteArray(),true)).toString();
		String requestContent="request="+reqBase;
		
		connection.getOutputStream().write(requestContent.getBytes());
		connection.getOutputStream().flush();
		connection.getOutputStream().close();
		return connection.getInputStream();
	}
	
	public static void setHeader(URLConnection con) {
		con.setRequestProperty("User-Agent","Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.3) Gecko/2008092510 Ubuntu/8.04 (hardy) Firefox/3.0.3");
		con.setRequestProperty("Accept-Language", "en-us,en;q=0.7,zh-cn;q=0.3");
		con.setRequestProperty("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		con.setRequestProperty("Keep-Alive", "300");
		con.setRequestProperty("Connection", "keep-alive");
		con.setRequestProperty("If-Modified-Since","Fri, 02 Jan 2009 17:00:05 GMT");
		con.setRequestProperty("If-None-Match", "\"1261d8-4290-df64d224\"");
		con.setRequestProperty("Cache-Control", "max-age=0");
		con.setRequestProperty("Referer","");
		con.setRequestProperty("Host","www.ehoo.com");
	}
}
