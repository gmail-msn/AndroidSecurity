/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.kindroid.security.R;
import com.kindroid.security.model.Device;
import com.kindroid.security.model.Interface;
import com.kindroid.security.model.NetTrafficModel;
import com.kindroid.security.model.TrafficCounter;
import com.kindroid.security.notification.NetTrafficNotification;
import com.kindroid.security.service.NetTrafficAlarm.State;
import com.kindroid.security.ui.DefenderTabMain;
import com.kindroid.security.util.AppNetWorkDataBase;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.HandlerContainer;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.UtilShareprefece;

/**
 * {@link NetTrafficService} is the service responsible for regular update of
 * the model. We have to update it regularly because we get our data from the
 * underlying Linux system and Android does not provide a "shutdown event" at
 * the moment. So we poll regularly in order not to lose too many information.
 */
public class NetTrafficService extends WakefulService {
	private String[] WIFI_INTERFACES = { //
	"eth0", "tiwlan0", "wlan0", "athwlan0", "eth1" //
	};
	private String str = null;
	Thread updateAppThread;
	private boolean mShowNotifaciton = true;

	private final BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			sendBroadcast(new Intent(NetTrafficService.this,
					OnAlarmReceiver.class));
			registerAlarm();
		}
	};

	private final Runnable mUpdateRunnable = new Runnable() {
		public void run() {
			try {
				// Only updates if the model is loaded.
				if (mModel.isLoaded()) {
					updateInterfaceData();
				}
				int sysVersion = Integer.parseInt(VERSION.SDK);
				if (sysVersion >= 8 && updateAppThread == null) {

					int num = KindroidSecurityApplication.sh.getInt(
							Constant.SHAREDPREFERENCES_APPFILEEXIT, 1);
					if (num == 1) {
						File file = new File(Constant.APPTRAFFICDIR);
						Editor et = KindroidSecurityApplication.sh.edit();
						if (file.exists()) {
							et.putInt(Constant.SHAREDPREFERENCES_APPFILEEXIT, 2);
							updateAppThread = new UpdateAppThread();
							updateAppThread.start();

						} else {
							et.putInt(Constant.SHAREDPREFERENCES_APPFILEEXIT, 3);
						}
						et.commit();

					} else if (num == 2) {
						updateAppThread = new UpdateAppThread();
						updateAppThread.start();
					}

				}

			} finally {
				// Releases the local wake lock.
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (mShowNotifaciton) {
							Intent intent = new Intent(NetTrafficService.this,
									NotificationService.class);
							startService(intent);
						}
					}
				}, 1000);

			}
		}
	};

	private int mPollingMode = -1;

	private boolean mWifiUpdate = false;

	private KindroidSecurityApplication mApp;

	private NetTrafficModel mModel;

	private NetTrafficAlarm mAlarm;

	private WifiManager mWifiManager;

	@Override
	public void onCreate() {
		super.onCreate();
		mShowNotifaciton = true;

		mApp = (KindroidSecurityApplication) getApplication();
		mModel = mApp.getAdapter(NetTrafficModel.class);

		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		mAlarm = new NetTrafficAlarm(this, OnAlarmReceiver.class);

		IntentFilter f = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
		registerReceiver(mWifiReceiver, f);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		int p = KindroidSecurityApplication.getUpdatePolicy();

		// Tweak to check if the preference changed. TODO Remove this hack.
		SharedPreferences preferences = mApp
				.getAdapter(SharedPreferences.class);
		boolean wifiUpdate = preferences.getBoolean("wifiUpdate", false);
		if (mPollingMode != p || wifiUpdate != mWifiUpdate) {
			mPollingMode = p;
			mWifiUpdate = wifiUpdate;
			registerAlarm();
		}

		Handler handler = mApp.getAdapter(HandlerContainer.class)
				.getSlowHandler();
		handler.removeCallbacks(mUpdateRunnable);
		handler.post(mUpdateRunnable);

		if (KindroidSecurityApplication.LOG_ENABLED) {
			Log.d(getClass().getName(), "Service onStart -> " + p);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mShowNotifaciton = false;
		mApp.getAdapter(HandlerContainer.class).getSlowHandler()
				.post(mUpdateRunnable);
		unregisterReceiver(mWifiReceiver);

		NetTrafficNotification notification = new NetTrafficNotification(
				getApplication(), this);
		notification.cancelNotification();

		if (KindroidSecurityApplication.LOG_ENABLED) {
			Log.d(getClass().getName(), "Service onDestroy.");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Registers the alarm, changing the refresh rate as appropriate.
	 */
	private void registerAlarm() {
		
		if (mPollingMode == KindroidSecurityApplication.SERVICE_HIGH) {
			mAlarm.registerAlarm(State.HIGH);
		} else if (mPollingMode == KindroidSecurityApplication.SERVICE_MID) {
			mAlarm.registerAlarm(State.MID);
		} else {
			switch (mWifiManager.getWifiState()) {
			case WifiManager.WIFI_STATE_ENABLED:
				// Checks if the WiFi "tweak" if enabled.
				SharedPreferences preferences = mApp
						.getAdapter(SharedPreferences.class);
				if (preferences.getBoolean("wifiUpdate", false)) {
					mAlarm.registerAlarm(State.LOW);

				} else {
					mAlarm.registerAlarm(State.HIGH);

				}
				break;
			case WifiManager.WIFI_STATE_DISABLED:
				mAlarm.registerAlarm(State.LOW);

				break;
			}
		}
	}
	
	private void updateInterfaceData() {		
		
		Device device = Device.getDevice();
		String bluetooth = device.getBluetooth();
		String[] interfaces = device.getInterfaces();
		
		for (int i = 0; i < interfaces.length; i++) {
			String inter = interfaces[i];
			if (inter.equals(bluetooth) && !SysClassNet.isUp(inter)) {

				continue;
			}
			try {
				// Reads the values.
				long rx = 0;
				long tx = 0;

				Interface tmp = mModel.getInterface(inter);
				if (tmp.getPrettyName().equals("Wi-Fi") && str == null) {

					for (int j = 0; j < WIFI_INTERFACES.length; j++) {
						long temp_rx = SysClassNet
								.getRxBytes(WIFI_INTERFACES[j]);
						long temp_tx = SysClassNet
								.getTxBytes(WIFI_INTERFACES[j]);
						if (temp_rx != 0 || temp_tx != 0) {
							str = WIFI_INTERFACES[j];
							rx = temp_rx;
							tx = temp_tx;
						}
					}

				} else if (tmp.getPrettyName().equals("Wi-Fi") && str != null) {
					rx = SysClassNet.getRxBytes(str);
					tx = SysClassNet.getTxBytes(str);

				} else {
					rx = SysClassNet.getRxBytes(inter);
					tx = SysClassNet.getTxBytes(inter);
				}

				tmp.updateBytes(rx, tx);
				mModel.commit();

			} catch (IOException e) {
				Log.e(getClass().getName(), "I/O Error", e);
			}
		}
		checkAlert();		
	}

	private void checkAlert() {
		NotificationManager notify = mApp.getAdapter(NotificationManager.class);
		for (Interface provider : mModel.getInterfaces()) {
			if (!provider.getPrettyName().equals(
					getResources().getString(R.string.interfaceTypeCell)))
				return;

			for (TrafficCounter c : provider.getCounters()) {
				if (c.getType() != 1)
					continue;

				int id = (int) ((provider.getId() << 10) + c.getId());
				String a = KindroidSecurityApplication.sh.getString(
						UtilShareprefece.LIMIT_PER_MON_INT, "0");
				long alert = 0;
				if (a != null) {
					alert = Long.valueOf(a);
					alert *= 1024 * 1024;
				}
				if (alert > 0) {

					long[] bytes = c.getBytes();
					long total = bytes[0] + bytes[1];

					if (total > alert) {
						String inter = provider.getPrettyName();

						Notification n = new Notification();
						n.when = System.currentTimeMillis();
						n.icon = R.drawable.icon;

						n.flags |= Notification.FLAG_AUTO_CANCEL;
						n.tickerText = getResources().getString(
								R.string.notifyExceedTitle, inter);

						// The PendingIntent to launch our activity if the user
						// selects this notification
						PendingIntent contentIntent = PendingIntent
								.getActivity(this, 0, new Intent(this,
										DefenderTabMain.class), 0);

						double t = total;
						double left = (t - alert) / (1024 * 1024);
						String str = "";
						if (left == (int) left) {
							str = ((int) left) + "";
						} else {
							str = (((int) left) + 1) + "";
						}
						String s = getResources().getString(
								R.string.notifyExceed, "", str,
								KindroidSecurityApplication.BYTE_UNITS[2], "");

						n.setLatestEventInfo(this, getText(R.string.app_name),
								s, contentIntent);

						notify.notify(id, n);

					} else {
						notify.cancel(id);
					}
				} else {
					notify.cancel(id);
				}
			}
		}
	}

	class UpdateAppThread extends Thread {
		@Override
		public void run() {
			try {
				PackageManager pckMan = getPackageManager();

				List<PackageInfo> packs = pckMan
						.getInstalledPackages(PackageManager.GET_PERMISSIONS
								| PackageManager.GET_SIGNATURES);
				boolean isTrue = false;
				if (!KindroidSecurityApplication.sh
						.contains(Constant.SHAREDPREFERENCES_APPFIRST_INSTALL))
					isTrue = true;

				for (PackageInfo pInfo : packs) {
					boolean permission = hasInternetPermission(pInfo);
					if (permission) {
						long rx = TrafficStats
								.getUidRxBytes(pInfo.applicationInfo.uid);
						long tx = TrafficStats
								.getUidTxBytes(pInfo.applicationInfo.uid);

						if (rx == -1 || tx == -1)
							continue;
						AppNetWorkDataBase.get(NetTrafficService.this)
								.insertOrUpdateDetail(pInfo.packageName, rx,
										tx, Calendar.getInstance(), 0);
						if (isTrue)
							AppNetWorkDataBase.get(NetTrafficService.this)
									.insertOrUpdateDetail(pInfo.packageName,
											rx, tx, Calendar.getInstance(), 1);

					}

				}

				Editor et = KindroidSecurityApplication.sh.edit();
				et.putBoolean(Constant.SHAREDPREFERENCES_APPFIRST_INSTALL,
						false);
				et.commit();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				updateAppThread = null;
			}

		}
	}

	boolean hasInternetPermission(PackageInfo pi) {
		boolean isTrue = false;
		String permission[] = pi.requestedPermissions;
		if (permission == null) {
			return isTrue;
		}

		for (String str : permission) {
			if (str.equals("android.permission.INTERNET")) {
				isTrue = true;
				break;
			}
		}
		return isTrue;

	}

}
