/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.kindroid.security.util.CMDExecute;

public class ProcessMemoryUtil {
	private static final int INDEX_FIRST = -1;
	private static final int INDEX_PID = INDEX_FIRST + 1;
	private static final int INDEX_CPU = INDEX_FIRST + 2;
	private static final int INDEX_STAT = INDEX_FIRST + 3;
	private static final int INDEX_THR = INDEX_FIRST + 4;
	private static final int INDEX_VSS = INDEX_FIRST + 5;
	private static final int INDEX_RSS = INDEX_FIRST + 6;
	private static final int INDEX_PCY = INDEX_FIRST + 7;
	private static final int INDEX_UID = INDEX_FIRST + 8;
	private static final int INDEX_NAME = INDEX_FIRST + 9;
	private static final int Length_ProcStat = 9;
	private List<String[]> PMUList = null;

	public ProcessMemoryUtil() {
		initMemoryData();
	}

	private String getProcessRunningInfo() {
		
		String result = null;
		CMDExecute cmdexe = new CMDExecute();
		try {
			String[] args = { "/system/bin/top", "-n", "1" };
			result = cmdexe.run(args, "/system/bin/");
		} catch (IOException ex) {
			
		}
		return result;
	}

	private int parseProcessRunningInfo(String infoString) {
		String tempString = "";
		boolean bIsProcInfo = false;
		String[] rows = null;
		String[] columns = null;
		rows = infoString.split("[\n]+");

		for (int i = 0; i < rows.length; i++) {
			tempString = rows[i];
			if (tempString.indexOf("PID") == -1) {
				if (bIsProcInfo == true) {
					tempString = tempString.trim();
					columns = tempString.split("[ ]+");
					if (columns.length == Length_ProcStat) {
						PMUList.add(columns);
					}
				}
			} else {
				bIsProcInfo = true;
			}
		}
		return PMUList.size();
	}

	private void initMemoryData() {
		PMUList = new ArrayList<String[]>();
		String resultString = getProcessRunningInfo();
		parseProcessRunningInfo(resultString);
	}

	public String getMemInfoByName(String procName) {
		String result = "";
		String tempString = "";
		for (Iterator<String[]> iterator = PMUList.iterator(); iterator
				.hasNext();) {
			String[] item = (String[]) iterator.next();
			tempString = item[INDEX_NAME];
			if (tempString != null && tempString.equals(procName)) {
				result = "CPU:" + item[INDEX_CPU] + "  Mem:" + item[INDEX_RSS];
				break;
			}
		}
		return result;
	}

	public String getMemInfoByPid(int pid) {
		String result = "";
		String tempPidString = "";
		int tempPid = 0;
		int count = PMUList.size();
		
		for (int i = 0; i < count; i++) {
			String[] item = PMUList.get(i);
			tempPidString = item[INDEX_PID];
			if (tempPidString == null) {
				continue;
			}
			
			tempPid = Integer.parseInt(tempPidString);
			if (tempPid == pid) {
				result = item[INDEX_RSS];
				break;
			}
		}
		return result;
	}
}