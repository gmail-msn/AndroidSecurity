/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-09
 * Description:
 */
package com.kindroid.security.model;

/**
 * @author heli.zhao
 *
 */
public class InterceptModeItem {
	private String mModeTitle;
	private String mModeDesp;
	private boolean mIsSelected;
	
	public String getModeTitle() {
		return mModeTitle;
	}
	public void setModeTitle(String mModeTitle) {
		this.mModeTitle = mModeTitle;
	}
	public String getModeDesp() {
		return mModeDesp;
	}
	public void setModeDesp(String mModeDesp) {
		this.mModeDesp = mModeDesp;
	}
	public boolean isSelected() {
		return mIsSelected;
	}
	public void setIsSelected(boolean mIsSelected) {
		this.mIsSelected = mIsSelected;
	}
	
}
