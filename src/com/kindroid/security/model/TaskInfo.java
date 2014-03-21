/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.model;

import java.io.Serializable;

public class TaskInfo implements Serializable {
	private static final long serialVersionUID = 9008854636823759769L; 
	private String urlStr;
	private String savePath;
	private String fileName;
	private int threadNum;
	private int percent;
	private long fileSize;
	private int state;

	public TaskInfo(){
		this.state = 1;
		this.fileSize = 0;
		this.percent = 0;
		
	}
	public TaskInfo(String urlStr, String savePath, String fileName, int threadNum,
			int percent, long fileSize, int state) {
		this.urlStr = urlStr;
		this.savePath = savePath;
		this.fileName = fileName;
		this.threadNum = threadNum;
		this.percent = percent;
		this.fileSize = fileSize;
		this.state = state;
	}
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public String getUrlStr() {
		return urlStr;
	}
	public void setUrlStr(String urlStr) {
		this.urlStr = urlStr;
	}
	public String getSavePath() {
		return savePath;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getThreadNum() {
		return threadNum;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	public int getPercent() {
		return percent;
	}
	public void setPercent(int percent) {
		this.percent = percent;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	@Override
	public String toString() {
		return "TaskInfo [urlStr=" + urlStr + ", savePath=" + savePath
				+ ", fileName=" + fileName + ", threadNum=" + threadNum
				+ ", percent=" + percent + "]";
	}
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		if(this.savePath != null & this.fileName != null)
			return (this.savePath + this.fileName).hashCode();
		else
			return super.hashCode();
	}
	
	
	
}
