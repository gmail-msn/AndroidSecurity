/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.model.ServInfo;

public class TaskUtil {
	private static final int MAX_SERVICES = 200;

	public static void killProcess(String packageName, Context context) {

		if (packageName.compareToIgnoreCase(context
				.getString(R.string.package_name)) == 0) {
			return;
		}

		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		if (isFroyoSDK()) {
			manager.killBackgroundProcesses(packageName);			
		} else {
			manager.restartPackage(packageName);
		}
	}

	public static boolean isFroyoSDK() {
		Integer SDK = Integer.valueOf(Integer.parseInt(Build.VERSION.SDK));
		if (SDK.intValue() >= 8) {
			return true;
		}
		return false;
	}

	public static List<ProcInfo> getRunningApp(Context context) {

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procList = activityManager
				.getRunningAppProcesses();
		List<ProcInfo> list = new ArrayList<ProcInfo>();
		for (RunningAppProcessInfo appinfo : procList) {
			try {
				ApplicationInfo applicationInfo = context.getPackageManager()
						.getApplicationInfo(appinfo.pkgList[0],
								PackageManager.GET_META_DATA);
				if (applicationInfo.packageName
						.equals(context.getPackageName()))
					continue;
				if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
						&& (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					ProcInfo application = new ProcInfo(appinfo.pkgList[0]);
					application.setPid(appinfo.pid);
					list.add(application);
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

		}
		return list;
	}

	public static List<ServInfo> getRunningServices(Context context) {
		PackageManager pm = context.getPackageManager();
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<ActivityManager.RunningServiceInfo> services = activityManager
				.getRunningServices(MAX_SERVICES);
		final int NS = services != null ? services.size() : 0;
		List<ServInfo> list = new ArrayList<ServInfo>();
		for (int i=0; i<NS; i++) {
            ActivityManager.RunningServiceInfo rsi = services.get(i);            
            // We are not interested in services that have not been started
            // and don't have a known client, because
            // there is nothing the user can do about them.
            if (!rsi.started && rsi.clientLabel == 0) {
                continue;
            }
            
            // We likewise don't care about services running in a
            // persistent process like the system or phone.
            if ((rsi.flags&ActivityManager.RunningServiceInfo.FLAG_PERSISTENT_PROCESS)
                    != 0) {
                continue;
            }
            
            try{
            	ServiceInfo si = pm.getServiceInfo(rsi.service, 0);
            	if(si.packageName.equals("com.kindroid.security")){
            		continue;
            	}
            	ServInfo servInfo = new ServInfo(si.packageName); 
            	servInfo.setLabelName(si.name.substring(si.name.lastIndexOf('.') + 1));            	
            	ApplicationInfo applicationInfo = pm.getApplicationInfo(si.packageName, PackageManager.GET_META_DATA);
            	String appName = pm.getApplicationLabel(applicationInfo).toString();
            	
            	servInfo.setAppName(appName);
            	servInfo.setService(rsi.service);
            	list.add(servInfo);
            }catch(NameNotFoundException nnfe){
            	nnfe.printStackTrace();
            }
            
		}
		
		return list;
	}
}
