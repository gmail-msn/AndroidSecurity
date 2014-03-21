/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

public class HistoryNativeCursor {
	/**
	 * requestType is 3 or 4，sms or phone
	 * */
	private int mRequestType;
	private int mId;
	private String mAddress;
	private String mBody;
	private String mDate;
	private int mRead;
	private String mRemark;
	
	private boolean isSelect;
	
	
	public boolean isSelect() {
		return isSelect;
	}
	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}
	public int getmRequestType() {
		return mRequestType;
	}
	public void setmRequestType(int mRequestType) {
		this.mRequestType = mRequestType;
	}
	public int getmId() {
		return mId;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}
	public String getmAddress() {
		return mAddress;
	}
	public void setmAddress(String mAddress) {
		this.mAddress = mAddress;
	}
	public String getmBody() {
		return mBody;
	}
	public void setmBody(String mBody) {
		this.mBody = mBody;
	}
	public String getmDate() {
		return mDate;
	}
	public void setmDate(String mDate) {
		this.mDate = mDate;
	}
	public int getmRead() {
		return mRead;
	}
	public void setmRead(int mRead) {
		this.mRead = mRead;
	}
	public String getmRemark() {
		return mRemark;
	}
	public void setmRemark(String mRemark) {
		this.mRemark = mRemark;
	}
	

}
