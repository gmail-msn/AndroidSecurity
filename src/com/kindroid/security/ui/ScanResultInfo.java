/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

public class ScanResultInfo {
	private ApplicationInfo mAppInfo;
	private boolean mChecked;
	private String mVirusName;
	private PackageInfo pInfo;

	public PackageInfo getpInfo() {
		return pInfo;
	}

	public void setpInfo(PackageInfo pInfo) {
		this.pInfo = pInfo;
	}

	public ScanResultInfo(ApplicationInfo appinfo,PackageInfo pInfo, String mVirusName)
	{
		mAppInfo = appinfo;
		mChecked = false;
		this.mVirusName=mVirusName;
		this.pInfo = pInfo;
	}
	
	public ApplicationInfo getAppInfo() {
		return mAppInfo;
	}
	public boolean getChecked() {
		return mChecked;
	}
	
	public void setChecked(boolean checked) {
		mChecked = checked;
	}
	public String getVirusName() {
		return mVirusName;
	}
}
