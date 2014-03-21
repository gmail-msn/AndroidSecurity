package com.kindroid.security.model;

public class BlackListItem {
	private int mType;
	private int mId;
	private String mContactName;
	private String mPhoneNumber;
	private String mContentDesp;	
	private boolean mSelected;
	public int getmType() {
		return mType;
	}
	public void setType(int mType) {
		this.mType = mType;
	}
	public int getId() {
		return mId;
	}
	public void setId(int mId) {
		this.mId = mId;
	}
	public String getContactName() {
		return mContactName;
	}
	public void setContactName(String mContactName) {
		this.mContactName = mContactName;
	}
	public String getPhoneNumber() {
		return mPhoneNumber;
	}
	public void setPhoneNumber(String mPhoneNumber) {
		this.mPhoneNumber = mPhoneNumber;
	}
	public boolean isSelected() {
		return mSelected;
	}
	public void setSelected(boolean mSelected) {
		this.mSelected = mSelected;
	}
	public String getContentDesp() {
		return mContentDesp;
	}
	public void setContentDesp(String mContentDesp) {
		this.mContentDesp = mContentDesp;
	}

}
