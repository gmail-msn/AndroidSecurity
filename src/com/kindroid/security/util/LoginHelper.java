/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.kindroid.security.util.AccountProtoc.Account;
import com.kindroid.security.util.AccountProtoc.AccountRequest;
import com.kindroid.security.util.AccountProtoc.AccountResponse;
import com.kindroid.security.util.CommonProtoc.RequestType;
import com.kindroid.security.util.ResponseProtoc.ResponseContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public class LoginHelper {

	public static int validateUser(String userName, String pwd) {
		int ret = 0;

		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(Constant.LOGIN_URL + userName + "/"
					+ pwd);
			BufferedReader in = null;
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			StringBuilder sb = new StringBuilder();
			String line = in.readLine();
			while (line != null) {
				sb.append(line);
				line = in.readLine();
			}
			JSONObject result = new JSONObject(sb.toString());
			ret = result.getInt("result");
			if (ret == 0) {
				String s = (String) result.get("token");
				if (s == null)
					return -1;
				Editor editor = KindroidSecurityApplication.sh.edit();
				editor.putString(Constant.SHAREDPREFERENCES_TOKEN, s);
				editor.putString(Constant.SHAREDPREFERENCES_USERNAME, userName);
				editor.commit();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ret = -1;
		}

		return ret;
	}

	public static int forgotPwd(String userName, String email) {
		int ret = 0;

		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(Constant.FORGOTPWD_URL + userName
					+ "/" + email);
			BufferedReader in = null;
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			StringBuilder sb = new StringBuilder();
			String line = in.readLine();
			while (line != null) {
				sb.append(line);
				line = in.readLine();
			}

			JSONObject result = new JSONObject(sb.toString());
			ret = result.getInt("result");

		} catch (Exception e) {
			e.printStackTrace();
			ret = -1;
		}

		return ret;
	}

	public static int registerAccount(Context ctx, String username,
			String email, String password, String phoneNum) {
		try {
			AccountRequest.Builder aRequestBuilder = AccountRequest
					.newBuilder();
			aRequestBuilder.setRequestType(RequestType.REGISTER);
			Account.Builder aCcountBuilder = Account.newBuilder();
			aCcountBuilder.setName(username);
			aCcountBuilder.setPassword(password);
			aCcountBuilder.setEmail(email);
			if (!TextUtils.isEmpty(phoneNum)) {
				aCcountBuilder.setPhone(phoneNum);
			}
			aRequestBuilder.setAccount(aCcountBuilder);
			RequestProtoc.Request.Builder request = RequestProtoc.Request
					.newBuilder();
			request.setAccountRequest(aRequestBuilder);
			Base64Handler base64 = new Base64Handler();
			HttpClient client = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost();
			int ret = -1;
			URI url = new URI(Constant.REGISTER_URL);
			httpPost.setURI(url);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("request", base64
					.encode(request.build().toByteArray())));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));			
			HttpResponse response = client.execute(httpPost);
			ResponseProtoc.Response resp = ResponseProtoc.Response
					.parseFrom(Base64.decodeBase64(ConvertUtils
							.InputStreamToByte(response.getEntity()
									.getContent())));
			ResponseContext rc = resp.getContext();
			ret = rc.getResult();

			if (ret == 0) {
				AccountResponse ar = resp.getAccountResponse();
				String s = ar.getToken();
				if (s == null)
					return -1;
				KindroidSecurityApplication.sh.edit()
						.putString(Constant.SHAREDPREFERENCES_TOKEN, s)
						.commit();
				KindroidSecurityApplication.sh
						.edit()
						.putString(Constant.SHAREDPREFERENCES_USERNAME,
								username).commit();
			}else{
				ret = rc.getErrNO();
			}
			return ret;
		} catch (IOException ex) {

			ex.printStackTrace();
		} catch (Exception ex) {

			ex.printStackTrace();
		}
		return -1;
	}

	public static String InputStreamToString(InputStream is) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String str = br.readLine();
		while (str != null) {
			sb.append(str);
			str = br.readLine();
		}
		is.close();

		return sb.toString();

	}

	public static InputStream postData2(String urlStr, String request)
			throws ParserConfigurationException, IOException,
			ConnectTimeoutException, UnknownHostException {
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setDoOutput(true);
		connection.setDoInput(true);
		// connection.setUseCaches(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Accept-Charset", "utf-8");
		connection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		connection.setConnectTimeout(6000);
		connection.setReadTimeout(5000);

		// String requestContent = "request=" + request;
		connection.getOutputStream().write(request.getBytes());
		connection.getOutputStream().flush();
		connection.getOutputStream().close();

		return connection.getInputStream();
	}

}
