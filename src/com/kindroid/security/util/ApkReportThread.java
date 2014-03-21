/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;

public class ApkReportThread extends Thread {
	private static final int RES_ID = Config.MARKET_ID;
	private Context mContext;
	
	public ApkReportThread(Context context){
		this.mContext = context;
	}
	
	public void run(){
		if(!Utilis.checkNetwork(mContext)){
			return;
		}
		BufferedReader in = null;
		TelephonyManager telephonyManager=(TelephonyManager) this.mContext.getSystemService(Context.TELEPHONY_SERVICE);
		String imei=telephonyManager.getDeviceId();
		PackageManager pm = this.mContext.getPackageManager();
		HttpClient client = new DefaultHttpClient();
		try{			
			HttpGet request = new HttpGet();
			JSONObject param = new JSONObject();
			param.put("resID", RES_ID);
			param.put("manuer", Build.MANUFACTURER);
			param.put("model", Build.MODEL);
			param.put("build", Build.DISPLAY);
			param.put("fingerprint", Build.FINGERPRINT);
			param.put("imei", imei);
			param.put("host", Build.HOST);
			param.put("device", Build.DEVICE);
			param.put("brand", Build.BRAND);			
			param.put("board", Build.BOARD);
			param.put("osversion", Build.VERSION.RELEASE);
			param.put("sdkversion", Build.VERSION.SDK_INT);
			if(Build.VERSION.SDK_INT >= 8){
				param.put("hardware", Build.HARDWARE);
			}else{
				param.put("hardware", "unknown");
			}
			
			try{
				PackageInfo packageInfo = pm.getPackageInfo("com.kindroid.security", PackageManager.GET_SIGNATURES);
				param.put("version", packageInfo.versionName);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
			if(tm.getSubscriberId() != null){
				param.put("sim", tm.getSubscriberId());
			}else{
				param.put("sim", "");
			}
			
			String enStr = new String(Base64.encodeBase64(param.toString().getBytes()));
			request.setURI(new URI(Constant.REPORT_URL + URLEncoder.encode(enStr,"utf-8")));
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = in.readLine();			
			in.close();
			
			
			JSONObject result = new JSONObject(line);
			if(result.getString("result").equals("0")){
				SharedPreferences sp = KindroidSecurityApplication.sh;
				Editor editor = sp.edit();
				long time = System.currentTimeMillis();
				editor.putLong(UtilShareprefece.LAST_REPORT_TIME, time);
				editor.commit();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(in != null){
				try{
					in.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
}
