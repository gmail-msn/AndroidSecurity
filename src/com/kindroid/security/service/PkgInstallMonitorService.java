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
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.kindroid.security.AVEngine;
import com.kindroid.security.ApkSignatureInfo;
import com.kindroid.security.R;
import com.kindroid.security.VirusInfo;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.ui.InstallScanCleanActivity;
import com.kindroid.security.ui.NewInstalledPkgWarn;
import com.kindroid.security.ui.VirusCleanActivity;
import com.kindroid.security.ui.VirusScanFirstActivity;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.SQLiteDBHelper;
import com.kindroid.security.util.SQLiteHelper;
import com.kindroid.security.util.UtilShareprefece;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author heli.zhao
 * 
 */
public class PkgInstallMonitorService extends Service {

	private NotificationManager mNotificationMgr;
	private int mNotifyId = 1008999;
	public static final String PARCEL_NAME_APP = "installed_app_name";
	public static final String PARCEL_NAME_VIRUS_DESP = "virus_desp";
	public static final String PARCEL_NAME_PKG = "installed_pkg_name";
	public static final String PARCEL_NAME_VIRUS_NAME = "virus_name";
	public static final String PARCEL_NAME_RISK_LEVEL = "risk_level";
	
	public static List<AppInfoForManage> mRiskInstalledPkg = new ArrayList<AppInfoForManage>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		this.mNotificationMgr = (NotificationManager) getSystemService("notification");
		initDatabase();
	}
	private void initDatabase() {
		try {
			SQLiteHelper myDbHelper = new SQLiteHelper(this);

			myDbHelper.createDataBase();

			myDbHelper.openDataBase();

			myDbHelper.close();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		} catch (SQLException sqle) {
			throw sqle;
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		if (intent != null && intent.getData() != null) {
			String pkgName = intent.getData().getEncodedSchemeSpecificPart();
			PackageManager mPackageManager = getPackageManager();
			try {
				PackageInfo mPackageInfo = mPackageManager.getPackageInfo(
						pkgName, PackageManager.GET_SIGNATURES);
				
				VirusInfo info = new VirusInfo();
				ApkSignatureInfo asInfo = new ApkSignatureInfo();
				int ret = AVEngine.avEngineCheck(
						mPackageInfo.applicationInfo.sourceDir, false, info,
						asInfo);				
				updateNotify(mPackageInfo, ret, info, pkgName);
			} catch (Exception e) {
				mNotificationMgr
						.cancel(PkgInstallMonitor.PKG_INSTALL_NOTIFY_ID);
				Log.e("KindroidSecurity", "Package cant found!");
			}
		}
	}

	private void updateNotify(PackageInfo mPackageInfo, int mRiskLevel,
			VirusInfo vInfo, String pkgName) {
		mNotificationMgr.cancel(PkgInstallMonitor.PKG_INSTALL_NOTIFY_ID);
		String appName = mPackageInfo.packageName;
		try {
			appName = mPackageInfo.applicationInfo.loadLabel(
					getPackageManager()).toString();
		} catch (Exception e) {
			if (appName == null) {
				appName = pkgName;
			}
		}
		Object[] arrayOfObject = new Object[1];
		arrayOfObject[0] = appName;
		if (mRiskLevel < 1) {
			Notification localNotification = new Notification(
					R.drawable.status_bar_icon, getString(
							R.string.install_scan_clean, arrayOfObject),
					System.currentTimeMillis());
			Intent localIntent = new Intent("android.intent.action.MAIN");
			localIntent.addCategory("android.intent.category.HOME");
			Uri localUri = Uri.parse(mNotifyId + "");
			localIntent.setData(localUri);
			PendingIntent localPendingIntent = PendingIntent.getActivity(
					this, 0, localIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			
			localNotification.setLatestEventInfo(this, getString(R.string.softmanage_prompt_dialog_title), getString(
					R.string.install_scan_clean, arrayOfObject),
					localPendingIntent);
			localNotification.flags = 16;
			this.mNotificationMgr.notify(mNotifyId + 1, localNotification);
		} else {
			for(AppInfoForManage aifm : mRiskInstalledPkg){
				if(aifm.getPackageName().trim().equals(pkgName.trim())){
					mRiskInstalledPkg.remove(aifm);
					break;
				}
			}
			String mRiskDesp = getString(
					R.string.install_scan_trojan, arrayOfObject);
			if(mRiskLevel == 1){
				mRiskDesp = getString(
						R.string.install_scan_warn, arrayOfObject);
			}
			Notification localNotification = new Notification(
					R.drawable.icon08, mRiskDesp,
					System.currentTimeMillis());
			
			Intent localIntent = new Intent(this, InstallScanCleanActivity.class);
			localIntent.putExtra(PARCEL_NAME_APP, appName);
			localIntent.setAction("android.intent.action.VIEW");
			localIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			/*
			StringBuilder sb = new StringBuilder();
			if(!TextUtils.isEmpty(vInfo.mVirusName)){
				sb.append(vInfo.mVirusName);
				if(!TextUtils.isEmpty(vInfo.mVirusDesc)){
					sb.append(',').append(vInfo.mVirusDesc);
				}
			}else if(!TextUtils.isEmpty(vInfo.mVirusDesc)){
				sb.append(vInfo.mVirusDesc);
			}
			localIntent.putExtra(PARCEL_NAME_VIRUS_DESP, sb.toString());
			localIntent.putExtra(PARCEL_NAME_PKG, pkgName);
			localIntent.putExtra(PARCEL_NAME_VIRUS_NAME, vInfo.mVirusName);
			localIntent.putExtra(PARCEL_NAME_RISK_LEVEL, mRiskLevel);
			*/
			PackageManager pm = getPackageManager();
			AppInfoForManage aifm = new AppInfoForManage();		
			aifm.setPackageName(pkgName);
			aifm.setFlag(mRiskLevel);
			if(mRiskLevel > 1){
				aifm.setVersion(getString(R.string.virus_risk_level));
				aifm.setDescription(vInfo.mVirusName);
			}else{
				aifm.setVersion(getString(R.string.virus_warning_level));
				aifm.setDescription(getString(R.string.black_certificate));
			}			
			mRiskInstalledPkg.add(aifm);
			//Intent localIntent = new Intent(this, VirusCleanActivity.class);
			PendingIntent localPendingIntent = PendingIntent.getActivity(
					this, 0, localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			if(mRiskLevel == 1){
				localNotification.setLatestEventInfo(this, getString(R.string.softmanage_prompt_dialog_title), getString(
						R.string.install_scan_warn, arrayOfObject),
						localPendingIntent);
			}else{
				localNotification.setLatestEventInfo(this, getString(R.string.softmanage_prompt_dialog_title), getString(
					R.string.install_scan_trojan, arrayOfObject),
					localPendingIntent);
			}
			localNotification.flags = 16;
			this.mNotificationMgr.notify(mNotifyId, localNotification);
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (this.mNotificationMgr == null) {
			return;
		}
		try {
			this.mNotificationMgr.cancel(PkgInstallMonitor.PKG_INSTALL_NOTIFY_ID);
			this.mNotificationMgr = null;
		} catch (Exception e) {

		}
	}

}
