/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.model;

import android.graphics.drawable.Drawable;
import android.net.Uri;

public class AppInfoForManage {
	private String appId;
	private Drawable icon;
	private CharSequence label;
	private CharSequence version;
	private long size;
	private long cacheSize = 0;
	private CharSequence unit;
	
	private int partnerId;
	
	public int getPartnerId() {
		return partnerId;
	}
	public void setPartnerId(int partnerId) {
		this.partnerId = partnerId;
	}

	private String packageName;	
	private boolean isInstalled = false;
	
	private CharSequence description;	
	
	private boolean isSelected = false;
	
	private String packagePath;
	private int flag = 0;
	
	private boolean usePrivacy = false;
	
	private boolean useCost = false;
	private boolean useGps = false;
	private boolean useNetwork = false;
	
	public boolean isUsePrivacy() {
		return usePrivacy;
	}
	public void setUsePrivacy(boolean usePrivacy) {
		this.usePrivacy = usePrivacy;
	}
	public boolean isUseCost() {
		return useCost;
	}
	public void setUseCost(boolean useCost) {
		this.useCost = useCost;
	}
	public boolean isUseGps() {
		return useGps;
	}
	public void setUseGps(boolean useGps) {
		this.useGps = useGps;
	}
	public boolean isUseNetwork() {
		return useNetwork;
	}
	public void setUseNetwork(boolean useNetwork) {
		this.useNetwork = useNetwork;
	}
	
	public CharSequence getUnit() {
		return unit;
	}
	public void setUnit(CharSequence unit) {
		this.unit = unit;
	}
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public CharSequence getDescription() {
		return description;
	}
	public void setDescription(CharSequence description) {
		this.description = description;
	}
	
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public String getPackagePath() {
		return packagePath;
	}
	public void setPackagePath(String packagePath) {
		this.packagePath = packagePath;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public boolean isInstalled() {
		return isInstalled;
	}
	public void setInstalled(boolean isInstalled) {
		this.isInstalled = isInstalled;
	}
	
	private Uri uri;
	
	public Uri getUri() {
		return uri;
	}
	public void setUri(Uri uri) {
		this.uri = uri;
	}
	
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public CharSequence getLabel() {
		return label;
	}
	public void setLabel(CharSequence label) {
		this.label = label;
	}
	public CharSequence getVersion() {
		return version;
	}
	public void setVersion(CharSequence version) {
		this.version = version;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	
	public long getCacheSize() {
		return cacheSize;
	}
	public void setCacheSize(long cacheSize) {
		this.cacheSize = cacheSize;
	}
	@Override
	public String toString() {
		return "AppInfoForManage [icon=" + icon + ", label=" + label
				+ ", version=" + version + ", size=" + size + ", isInstalled="
				+ isInstalled + ", isSelected=" + isSelected + ", packagePath="
				+ packagePath + ", flag=" + flag + ", uri=" + uri
				+ ", packageName=" + packageName + "]";
	}
	
}
