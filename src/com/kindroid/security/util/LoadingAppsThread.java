/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.ui.SoftManageListAdapter;
import com.kindroid.security.ui.ApksManageListActivity;

public class LoadingAppsThread extends Thread {
	private int flag;
	public static final int LOAD_INSTALLED = 0;
	public static final int LOAD_RECOMMEND = 2;
	public static final int LOAD_APKMANAGE = 1;
	public static final int LOAD_INSTALLED_PROGRESS = 3;
	private static final String mBufferDBName = "SoftcenterBuffer.db";
	public static final int NETWORK_ERROR = 6;

	private Context context;
	private Handler handler;
	private SoftManageListAdapter listAdapter;

	private Map<String, PackageStats> pkgSizes;
	public static boolean mBufferExist = true;
	public static boolean mUpdateBuffer = false;
	
	private boolean mCancelLoading = false;
	
	public LoadingAppsThread(Context context, Handler handler,
			SoftManageListAdapter listAdapter, int flag) {
		this.context = context;
		this.listAdapter = listAdapter;
		this.handler = handler;
		this.flag = flag;
		mCancelLoading = false;		
	}
	/*
	public void cancelLoading(){
		
		if(flag == LOAD_APKMANAGE){
			Log.d("KindroidSecurity", "cancel load apk");
			ApkManager.cancelLoadApk();
		}
		mCancelLoading = true;
	}
	*/
	public static void setPermissions(String[] permissions, AppInfoForManage aifm, PackageManager pm){
		if(permissions == null){
			return;
		}
		for(String p : permissions){
			try{
				PermissionInfo permInfo = pm.getPermissionInfo(p, PackageManager.GET_META_DATA);
				if(permInfo.group.equals("android.permission-group.LOCATION")){
					aifm.setUseGps(true);
					continue;
				}
				if(permInfo.group.equals("android.permission-group.NETWORK")){
					aifm.setUseNetwork(true);
					continue;
				}
				if(permInfo.group.equals("android.permission-group.COST_MONEY")){
					aifm.setUseCost(true);
					continue;
				}
				if(permInfo.group.equals("android.permission-group.PERSONAL_INFO")
						|| permInfo.group.equals("android.permission-group.STORAGE")){
					aifm.setUsePrivacy(true);
					continue;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
		}
		
	}

	@Override
	public void run() {
		switch (flag) {
		case LOAD_INSTALLED:
			List<PackageInfo> installedList = ApkManager.getInstalledPackages(
					context, false);
			pkgSizes = new HashMap<String, PackageStats>();
			/*
			if(mCancelLoading){
				this.handler.sendEmptyMessage(LOAD_INSTALLED);
				break;
			}
			*/
			for (PackageInfo pInfo : installedList) {
				
				getPkgInfo(pInfo.packageName);
			}
			// int interm = 0;
			int s = 0;
			int len = installedList.size();
			for (PackageInfo pInfo : installedList) {
				/*
				if(mCancelLoading){					
					break;
				}
				*/
				try {
					PackageManager pm = this.context.getPackageManager();
					
					PackageInfo pi = pm.getPackageInfo(pInfo.packageName, PackageManager.GET_PERMISSIONS);
					AppInfoForManage aifm = new AppInfoForManage();					
					aifm.setPackageName(pi.packageName);
					
					aifm.setLabel(pi.applicationInfo.loadLabel(pm));
					CharSequence version = pi.versionName;
					if (version != null) {
						aifm.setVersion(pi.versionName.concat(this.context
								.getString(R.string.softmanage_version_title)));
					}
					aifm.setIcon(pi.applicationInfo.loadIcon(pm));
					// èŽ·å�–å®‰è£…åŒ…å¤§å°�
					long apkSize = new File(pInfo.applicationInfo.sourceDir)
							.length();

					if (pkgSizes.get(pi.packageName) != null) {
						PackageStats ps = pkgSizes.get(pi.packageName);
						aifm.setSize(ps.cacheSize + ps.codeSize
										+ ps.dataSize);
					} else {
						aifm.setSize(apkSize);
					}
					aifm.setFlag(LOAD_INSTALLED);
					//audit protected		
					String[] perms = pi.requestedPermissions;
					
					setPermissions(perms, aifm, pm);

					this.listAdapter.addItem(aifm);
					s++;
					Message msg = new Message();
					msg.what = LOAD_INSTALLED_PROGRESS;
					msg.arg1 = s;
					msg.arg2 = len;
					this.handler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			/*
			if(mCancelLoading){
				listAdapter.clearItems();
			}
			*/
			this.handler.sendEmptyMessage(LOAD_INSTALLED);
			break;
		
		case LOAD_APKMANAGE:
			List<String> apks = ApkManager.getNotinstalledPackages(
					this.context, handler);
			Iterator<String> iter = apks.iterator();
			while (iter.hasNext() && !mCancelLoading) {

				AppInfoForManage aifm = ApkManager.getAppInfoFromApk(context,
						iter.next());
				if (aifm != null) {
					aifm.setFlag(LOAD_APKMANAGE);
					this.listAdapter.addItem(aifm);

				}

			}
			if(mCancelLoading){
				this.listAdapter.clearItems();
			}else{
				SharedPreferences sp = KindroidSecurityApplication.sh;
				Editor editor = sp.edit();
				editor.putInt(UtilShareprefece.LAST_APK_SUM, listAdapter.getCount());
				editor.commit();
			}
			
			this.handler.sendEmptyMessage(LOAD_APKMANAGE);
			((ApksManageListActivity)context).isLoadingData = false;
			//ApkManager.setCancel(false);
			break;
		}
	}

	public void getPkgInfo(String pkg) {
		PackageManager pm = context.getPackageManager();
		try {
			Method getPackageSizeInfo = pm.getClass().getMethod(
					"getPackageSizeInfo", String.class,
					IPackageStatsObserver.class);
			getPackageSizeInfo.invoke(pm, pkg, new PkgSizeObserver());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	class PkgSizeObserver extends IPackageStatsObserver.Stub {

		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			// TODO Auto-generated method stub
			pkgSizes.put(pStats.packageName, pStats);
		}
	}

}
