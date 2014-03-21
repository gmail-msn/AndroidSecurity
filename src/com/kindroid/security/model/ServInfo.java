/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class ServInfo {
	private String mLabelName;
	private String mPackageName;
	private Drawable mIcon;
	private boolean mChecked = false;
	private ComponentName mService;	
	private String mAppName;	
	private static PackageManager mPackageManager;
	
	public ServInfo(String packageName)
	{
	    this.mPackageName = packageName;
	}	
	
	public ComponentName getService() {
		return mService;
	}


	public void setService(ComponentName mService) {
		this.mService = mService;
	}


	public String getAppName() {
		return mAppName;
	}


	public void setAppName(String mAppName) {
		this.mAppName = mAppName;
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
	
	public int compareTo(ServInfo service)
	{
	    String labelThis = this.mLabelName;
	    String labelTarget = service.mLabelName;
	    return labelThis.compareTo(labelTarget);
	}
	public void setLabelName(String labelName){
		this.mLabelName = labelName;
	}
	public String getLabelName()
	{		
		return mLabelName;
	}

	public Drawable getIcon(Context context)
	{
	    if (mIcon == null) 
	    {
		    if (mPackageManager == null) {
		        mPackageManager = context.getPackageManager();
		    }
		    
			try {
				//mIcon = mPackageManager.getApplicationIcon(mPackageName);
				PackageInfo pInfo = mPackageManager.getPackageInfo(mPackageName, PackageManager.GET_SIGNATURES);
				mIcon = pInfo.applicationInfo.loadIcon(mPackageManager);
			} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
	    }

	    return mIcon;
	}

}
