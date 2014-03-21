/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.model;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author heli.zhao
 *
 */
public class AutoStartAppItem{
	private String mPackageName;
	private String mAppLabel;
	private String mVersion;
	private long mSize;
	private boolean mEnable;
	private Drawable mAppIcon;
	private List<String> mComponents;
	
	public long getSize() {
		return mSize;
	}
	public void setSize(long mSize) {
		this.mSize = mSize;
	}
	
	
	public Drawable getAppIcon() {
		return mAppIcon;
	}
	public void setAppIcon(Drawable mAppIcon) {
		this.mAppIcon = mAppIcon;
	}
	
	
	public String getPackageName() {
		return mPackageName;
	}
	public void setPackageName(String mPackageName) {
		this.mPackageName = mPackageName;
	}
	public String getAppLabel() {
		return mAppLabel;
	}
	public void setAppLabel(String mAppLabel) {
		this.mAppLabel = mAppLabel;
	}
	public String getVersion() {
		return mVersion;
	}
	public void setVersion(String mVersion) {
		this.mVersion = mVersion;
	}
	public boolean isEnable() {
		return mEnable;
	}
	public void setEnable(boolean mEnable) {
		this.mEnable = mEnable;
	}
	public void addComponent(String componentName){
		if(mComponents == null){
			mComponents = new ArrayList<String>();
		}
		mComponents.add(componentName);
	}
	public List<String> getComponents(){
		return mComponents;
	}	

}
