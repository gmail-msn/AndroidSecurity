/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-09
 * Description:
 */
package com.kindroid.security.util;

import android.content.Context;
import android.content.Intent;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKTokenizer;

import com.kindroid.security.service.SmsReciver;
import com.kindroid.security.service.UpdateProbService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author heli.zhao
 * 
 */
public class UpdateStaticsThread extends Thread {
	public static boolean mUpdate = false;
	public static boolean mUpdated = false;
	private String mSmsString;
	private int flag;
	private Context mContext;
	private boolean updateProb = false;

	public UpdateStaticsThread(String str, int flag, Context context, boolean updateProb) {
		this.flag = flag;
		this.mSmsString = str;
		this.mContext = context;
		this.updateProb = updateProb;
	}
	
	public UpdateStaticsThread(String str, int flag, Context context) {
		this(str, flag, context,false);
	}
	

	public synchronized void run() {
		mUpdate = true;
		try {
			File probPath = mContext.getDir("files", Context.MODE_PRIVATE);
			if (!probPath.exists()) {
				probPath.mkdirs();
			}
			File probFile = new File(probPath, "normal.dat");
			if (!probFile.exists()) {
				InputStream is = null;
				BufferedReader br = null;
				BufferedWriter bw = null;
				try {
					is = mContext.getAssets().open("normal.dat");
					br = new BufferedReader(new InputStreamReader(is, "utf-8"));
					bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(probFile), "utf-8"));
					String line = br.readLine();
					while(line != null){
						bw.write(line);
						bw.write("\n");
						line = br.readLine();
					}
					br.close();
					bw.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if(br != null){
						try{
							br.close();
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					if(bw != null){
						try{
							bw.close();
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
			probFile = new File(probPath, "spam.dat");
			if (!probFile.exists()) {
				InputStream is = null;
				BufferedReader br = null;
				BufferedWriter bw = null;
				try {
					is = mContext.getAssets().open("spam.dat");
					br = new BufferedReader(new InputStreamReader(is, "utf-8"));
					bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(probFile), "utf-8"));
					String line = br.readLine();
					while(line != null){
						bw.write(line);
						bw.write("\n");
						line = br.readLine();
					}
					br.close();
					bw.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if(br != null){
						try{
							br.close();
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					if(bw != null){
						try{
							bw.close();
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
			if (flag == 0) {
				probFile = new File(probPath, "normal.dat");
			}

			Properties props = new Properties();
			BufferedReader br = null;
			try{
				br = new BufferedReader(new InputStreamReader(new FileInputStream(probFile)));
				String line = br.readLine();
				while(line != null){
					if(line.contains("=")){
						String[] tokens = line.split("=");
						try{
							props.setProperty(tokens[0], tokens[1]);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					line = br.readLine();
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(br != null){
					try{
						br.close();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}

			IKTokenizer tokenizer = new IKTokenizer(
					new StringReader(mSmsString), true);
			
			while (tokenizer.incrementToken()) {
				CharTermAttribute ta = tokenizer
						.getAttribute(CharTermAttribute.class);
				String str = ta.toString();
				
				int count = 0;
				int ind = mSmsString.indexOf(str);
				while(ind != -1){
					count++;
					ind = mSmsString.indexOf(str, ind + str.length());
				}
				
				if (props.get(str) != null) {
					try {
						props.setProperty(
								str,
								String.valueOf(Integer.parseInt((String) props
										.get(str)) + count));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					props.setProperty(str, String.valueOf(count));
				}
			}
			
			BufferedWriter bw= null;
			try{
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(probFile), "utf-8"));
				Enumeration<Object> enumer = props.keys();
				while(enumer.hasMoreElements()){
					String key = (String)enumer.nextElement();
					try{
						bw.write(key + "=" + props.getProperty(key));
						bw.write("\n");
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				bw.flush();
			}catch(Exception e){
				if(bw != null){
					try{
						bw.close();
					}catch(Exception e2){
						e.printStackTrace();
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		mUpdate = false;
		if(updateProb){
			Intent intent = new Intent(mContext, UpdateProbService.class);
			mContext.startService(intent);
			updateProb = false;
		}
		mUpdated = true;
	}
}
