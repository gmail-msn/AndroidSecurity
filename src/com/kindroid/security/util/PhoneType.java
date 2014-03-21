package com.kindroid.security.util;

public class PhoneType {
	
	/** 
	 *0 other
	 *1 telphone
	 *2 nativphone
	 * */
	private int phonetype;
	private String phoneNo;
	public int getPhonetype() {
		return phonetype;
	}
	public void setPhonetype(int phonetype) {
		this.phonetype = phonetype;
	}
	public String getPhoneNo() {
		return phoneNo;
	}
	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

}
