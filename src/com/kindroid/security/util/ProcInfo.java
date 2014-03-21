/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Debug;
import android.os.Debug.MemoryInfo;

public class ProcInfo implements Comparable<ProcInfo> {
	private String mLabelName;
	private String mPackageName;
	private Drawable mIcon;
	private int mMemory;
	private static ActivityManager mActivityManager;
	private static PackageManager mPackageManager;
	private int mPid;
	private boolean mChecked = false;
	
	public ProcInfo(String packageName)
	{
	    this.mPackageName = packageName;
	}	
	
	public void setPid(int pid)
	{
		mPid = pid;
	}
	
	public int getPid()
	{
		return mPid;
	}
	
	public String getPackageName()
	{
		return mPackageName;
	}
	
	public void setChecked(boolean checked)
	{
		mChecked = checked;
	}
	
	public boolean getChecked()
	{
		return mChecked;
	}
	
	public int compareTo(ProcInfo application)
	{
	    String labelThis = this.mLabelName;
	    String labelTarget = application.mLabelName;
	    return labelThis.compareTo(labelTarget);
	}
	
	public String getLabelName(Context context)
	{
		if (mLabelName == null) {
			if (mPackageManager == null) {
				mPackageManager = context.getPackageManager();	
			}
			
			ApplicationInfo applicationInfo;
			try {
				applicationInfo = mPackageManager.getApplicationInfo(mPackageName, PackageManager.GET_META_DATA);
				
				mLabelName = mPackageManager.getApplicationLabel(applicationInfo).toString();			
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return mLabelName;
	}

	public int getMemory(Context context)
	{
		if (mActivityManager == null) {
	        mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		}
		int[] arrayPid = new int[1];
		arrayPid[0] = mPid;
		MemoryInfo[] mis = mActivityManager.getProcessMemoryInfo(arrayPid);
		mMemory = mis[0].getTotalPss();
		if(mMemory <= 0){
			MemoryInfo mi = new MemoryInfo();
			Debug.getMemoryInfo(mi);
			mMemory = mi.getTotalPss();
		}
		return mMemory;
	}
	
	public Drawable getIcon(Context context)
	{
	    if (mIcon == null) 
	    {
		    if (mPackageManager == null) {
		        mPackageManager = context.getPackageManager();
		    }
		    
			try {
				mIcon = mPackageManager.getApplicationIcon(mPackageName);
			} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
	    }

	    return mIcon;
	}
}
