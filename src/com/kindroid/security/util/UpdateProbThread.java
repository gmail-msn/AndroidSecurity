/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-09
 * Description:
 */
package com.kindroid.security.util;

import android.content.Context;

import com.kindroid.security.service.SmsReciver;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author zhl
 *
 */
public class UpdateProbThread extends Thread {
	private Context mContext;
	
	public UpdateProbThread(Context context){
		this.mContext = context;
	}
	
	public void run(){
		File probPath = mContext.getDir("files", Context.MODE_PRIVATE);
		if(!probPath.exists()){
			try{
				probPath.mkdirs();
			}catch(Exception e){
				e.printStackTrace();
			}
			return;
		}
		File nFile = new File(probPath, "normal.dat");
		File sFile = new File(probPath, "spam.dat");
		
		if(!nFile.exists() || !sFile.exists()){
			return;
		}
		Map<String, Integer> nMap = parseStatics(nFile);
		Map<String, Integer> sMap = parseStatics(sFile);
		
		Map<String, Double> nProbMap = parseProb(nMap);
		Map<String, Double> sProbMap = parseProb(sMap);
		
		Map<String, Double> probMap = parseProb(nProbMap, sProbMap);
		
		if(probMap.size() >= SmsReciver.spamProbProps.size()){
			SmsReciver.spamProbProps = probMap;
		}
		saveProb(probMap);
		UpdateStaticsThread.mUpdated = false;
	}
	private void saveProb(Map<String, Double> pMap){
		File probPath = mContext.getDir("files", Context.MODE_PRIVATE);
		if(!probPath.exists()){
			try{
				probPath.mkdirs();
			}catch(Exception e){
				e.printStackTrace();
			}
			return;
		}
		File pFile = new File(probPath, "prob.dat");
		BufferedWriter bw = null;
		try{
			Set<String> ks = pMap.keySet();
			Iterator<String> it = ks.iterator();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pFile), "utf-8"));
			while(it.hasNext()){
				String k = it.next();
				bw.write(k + "=" + pMap.get(k) + "\n");
			}
			bw.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(bw != null){
				try{
					bw.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	private Map<String, Double> parseProb(Map<String, Double> nMap, Map<String, Double> sMap){
		Map<String, Double> mp = new HashMap<String, Double>();
		Set<String> keys = sMap.keySet();
		Iterator<String> iter = keys.iterator();
		while(iter.hasNext()){
			String k = iter.next();
			double np = 0;
			if(nMap.containsKey(k)){
				np = nMap.get(k);
			}
			double sp = sMap.get(k);
			mp.put(k, sp/(np + sp));
		}
		keys = nMap.keySet();
		iter = keys.iterator();
		while(iter.hasNext()){
			String k = iter.next();
			if(!mp.containsKey(k)){
				mp.put(k, 0.0);
			}
		}
		return mp;
	}
	private Map<String, Double> parseProb(Map<String, Integer> map){
		Map<String, Double> mMap = new HashMap<String, Double>();
		Collection<Integer> values = map.values();
		double sum = 0;
		Iterator<Integer> iter = values.iterator();
		while(iter.hasNext()){
			sum = sum + iter.next();
		}
		Set<String> keys = map.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String key = it.next();
			mMap.put(key, map.get(key) * 1.0 / sum);
		}
		
		return mMap;
	}
	
	private Map<String, Integer> parseStatics(File file){
		Map<String, Integer> nMap = new HashMap<String, Integer>();
		BufferedReader nBr = null;
		try{
			nBr = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			String line = nBr.readLine();
			while(line != null){
				if(line.contains("=")){
					String[] strs = line.split("=");
					try{
						nMap.put(strs[0], Integer.parseInt(strs[1]));
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				line = nBr.readLine();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(nBr != null){
				try{
					nBr.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return nMap;
	}
}
