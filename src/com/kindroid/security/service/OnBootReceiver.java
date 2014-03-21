/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;

/**
 * Broadcast receiver that get launched after boot up. It delays the initial
 * callback to the service in order to speed up boot.
 */
public class OnBootReceiver extends BroadcastReceiver {

	private static final long DELAY = 2 * 60 * 1000;
	String SMS_SEND_ACTIOIN = "sms_send_myaction";

	TelephonyManager mTelephonyMgr;
	Context context;
	int sendNumber;
	
	public static final String START_TIME = "start_elapsed_time";
	
	public static final int RETRY_TIMES = 50;
	public static final int RETRY_PERIOD = 3000;
	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmManager mgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(context, OnAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

		// Delays the initial start of the service as we don't need to start
		// right after boot.
		long t = SystemClock.elapsedRealtime() + DELAY;
		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, t, pi);
		if (intent != null
				&& intent.getAction() != null
				&& intent.getAction().equals(
						"android.intent.action.BOOT_COMPLETED")) {
			SharedPreferences sp = KindroidSecurityApplication.sh;
			Editor editor = sp.edit();
			editor.putLong(START_TIME, SystemClock.uptimeMillis());
			editor.commit();
			Log.d("KindroidSecurity", "update start time");
			
			this.context = context;
			mTelephonyMgr = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			sendNumber = 0;
			new SendSmsThread().start();
		}

	}

	class SendSmsThread extends Thread {

		@Override
		public void run() {
			
			sendNumber++;
			if (sendNumber > RETRY_TIMES)
				return;
			
			boolean kindSecurityFuc = KindroidSecurityApplication.sh
					.getBoolean(Constant.SHAREDPREFERENCES_REMOTESECURITY,
							false);
			boolean isLockMobile = KindroidSecurityApplication.sh.getBoolean(
					Constant.SHAREDPREFERENCES_AFTERUPDATESIMTOLOCKMOBILE,
					false);
			if (!kindSecurityFuc)
				return;
			int SimState = mTelephonyMgr.getSimState();
			if (SimState == TelephonyManager.SIM_STATE_ABSENT) {
				if(isLockMobile){
					context.startService(new Intent(context,LocakMobileService.class));
				}
			}
			else if (SimState == TelephonyManager.SIM_STATE_READY) {

				String simSerialNumber = mTelephonyMgr.getSubscriberId();
				
				if (simSerialNumber == null)
					return;

				String oldSerialNumber = KindroidSecurityApplication.sh
						.getString(Constant.SHAREDPREFERENCES_SIMUNIQUETAG, "");
				
				if (!oldSerialNumber.equals(simSerialNumber)) {
					if(isLockMobile){
						context.startService(new Intent(context,LocakMobileService.class));
					}
					
					String safeMobileNumber = KindroidSecurityApplication.sh
							.getString(
									Constant.SHAREDPREFERENCES_SAFEMOBILENUMBER,
									"");

					if (safeMobileNumber.equals(""))
						return;

					// Send Message

					PendingIntent mPI = PendingIntent.getBroadcast(context, 0,
							new Intent(SMS_SEND_ACTIOIN), 0);
					SmsManager smsManager = SmsManager.getDefault();
					String smsMessage = KindroidSecurityApplication.sh
							.getString(
									Constant.SHAREDPREFERENCES_AFTERUPDATESIMSENDMES,
									context.getResources().getString(
											R.string.your_mobile_maybe_lost));
					String str = context.getResources().getString(
							R.string.remote_security_pop);
					smsMessage += str;
					smsManager.sendTextMessage(safeMobileNumber, null,
							smsMessage, mPI, null);

				}

			} else {
				if(sendNumber==RETRY_TIMES&&isLockMobile){
					context.startService(new Intent(context,LocakMobileService.class));
				}
				try {
					Thread.sleep(RETRY_PERIOD);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new SendSmsThread().start();
			}
		}

	}

}
