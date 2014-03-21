/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-09
 * Description:
 */
package com.kindroid.security.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.kindroid.security.R;

/**
 * @author heli.zhao
 *
 */
public class PkgInstallMonitor extends BroadcastReceiver {
	
	public static int PKG_INSTALL_NOTIFY_ID = 1008699;
	String ACTION_UNINSTALL_SHORTCUT ="com.android.launcher.action.UNINSTALL_SHORTCUT";  

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent == null){
			return;
		}
		String mAction = intent.getAction();
	    if (TextUtils.isEmpty(mAction)){
	      return;
	    }
	    if(mAction.equals("android.intent.action.PACKAGE_REMOVED") || mAction.equals(Intent.ACTION_PACKAGE_REPLACED)){
	    	String packageName = intent.getData().getEncodedSchemeSpecificPart();
		    if(packageName.startsWith("com.kindroid.security")){
		    	delShortCut(context);
		    }
	    	return;
	    }
	    if (!mAction.equals("android.intent.action.PACKAGE_ADDED") && !mAction.equals(Intent.ACTION_PACKAGE_REPLACED)){
	        return;
	    }
	    String packageName = intent.getData().getEncodedSchemeSpecificPart();
	    if(packageName.startsWith("com.kindroid.security")){
	    	return;
	    }
	    PackageManager pm = context.getPackageManager();
	    try{
	    	if ((pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).applicationInfo.flags & 0x1) != 0){
	            return;
	    	}
	    }catch(Exception e){
	    	
	    }
	    notifyInstallation(context, packageName);
	    Intent localIntent = intent.setClass(context, PkgInstallMonitorService.class);
	    ComponentName localComponentName = context.startService(intent);
   
	}
	private void delShortCut(Context context){
		 Intent intent = new Intent(ACTION_UNINSTALL_SHORTCUT ); 
	     intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name)); 
	     ComponentName comp = new ComponentName("com.kindroid.security",".ui.DefenderTabMain");
	     intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent().setComponent(comp).setAction("android.intent.action.MAIN"));   
	     context.sendBroadcast(intent);  

	}
	
	private void notifyInstallation(Context mContext, String pkgName){
		NotificationManager localNotificationManager = (NotificationManager)mContext.getSystemService("notification");
	    Notification localNotification = new Notification(R.drawable.icon, mContext.getString(R.string.install_scanning), System.currentTimeMillis());
	    Intent localIntent1 = new Intent("android.intent.action.MAIN");
	    localIntent1.addCategory("android.intent.category.HOME");
	    Uri localUri = Uri.parse(PKG_INSTALL_NOTIFY_ID + "");
	    localIntent1.setData(localUri);
	    PendingIntent localPendingIntent = PendingIntent.getActivity(mContext, 0, localIntent1, PendingIntent.FLAG_CANCEL_CURRENT);
	    Object[] arrayOfObject = new Object[1];
	    arrayOfObject[0] = pkgName;
	    String str2 = mContext.getString(R.string.install_scan_soft, arrayOfObject);
	    String str3 = mContext.getString(R.string.install_scanning);
	    localNotification.setLatestEventInfo(mContext, str2, str3, localPendingIntent);
	    localNotification.flags = 8;
	    localNotificationManager.notify(PKG_INSTALL_NOTIFY_ID, localNotification);
	}

}
