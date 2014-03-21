/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MemoryUtil {

	public static long getTotalMemory() {
		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);

			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
			localBufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return initial_memory;
	}

	public static String getPercent(long current, long total) {
		NumberFormat format = NumberFormat.getPercentInstance();
		double result = (double) current / total;
		return format.format(result);
	}

	public static String formatFileSize(int type, long length) {

		String result = "";
		if (type != 1) {
			DecimalFormat df = new DecimalFormat("###.");
			df.setMinimumFractionDigits(1);
			result = df.format((double) length / 1048576);
		} else {
			DecimalFormat df = new DecimalFormat("###");
			df.setMinimumFractionDigits(1);
			result = df.format((double) length / 1048576);
		}

		return result;
	}

	public static String formatMemorySize(int type, long length) {

		DecimalFormat df = new DecimalFormat("###.");
		String result = "";

		if (type != 1) {
			df.setMinimumFractionDigits(1);
			result = df.format((double) length / 1048576);

		} else {
			df.setMinimumFractionDigits(2);
			result = df.format((double) length / 1048576);
		}

		return result;
	}

	public static String formatMemoryBySize(int type, long length) {
		String result="";
		if(type==1){
			DecimalFormat df = new DecimalFormat("###.");
			df.setMinimumFractionDigits(2);
			if(length>=1073741824){
				result = df.format((double) length / 1073741824);
				result+="G";
			}else if(length>=1048576){
				result = df.format((double) length / 1048576);
				result+="M";
			}else if(length>=1024){
				result = df.format((double) length / 1024);
				result+="K";
			}else{
				result+=length+"B";
			}

		}
		return result;


	}

}
