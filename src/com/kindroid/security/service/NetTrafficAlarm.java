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
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;

import com.kindroid.security.util.KindroidSecurityApplication;

/**
 * {@link NetTrafficAlarm} registers a broadcast receiver that will be triggered
 * by the alarm manager.
 */
public class NetTrafficAlarm {

	/**
	 * State of the alarm.
	 */
	enum State {
		LOW, HIGH,MID
	}

	private static final long INTER_ACTIVE = 5000;
	
	private static final long INTER_ACTIVE_MID = 60 * 1000;

	private static final long INTER_STANDBY = 60 * 60 * 1000;

	private State mCurrentState = null;

	private final KindroidSecurityApplication mApp;

	private final AlarmManager mAm;

	private final PendingIntent mAs;

	/**
	 * The constructor.
	 * 
	 * @param srv
	 *            The {@link Service}.
	 * @param cls
	 *            The {@link BroadcastReceiver} triggered by the alarm.
	 */
	public NetTrafficAlarm(Service srv, Class<? extends BroadcastReceiver> cls) {
		mAm = (AlarmManager) srv.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(srv, cls);
		mAs = PendingIntent.getBroadcast(srv, 0, i, 0);
		mApp = (KindroidSecurityApplication) srv.getApplication();
	}

	/**
	 * Returns the current state of the alarm.
	 * 
	 * @return The current {@link State}.
	 */
	public State getCurrentState() {
		return mCurrentState;
	}

	/**
	 * Registers a new alarm if needed.
	 * 
	 * @param state
	 *            The {@link State}.
	 * @return <code>true</code> if a new alarm has been registered.
	 *         <code>false</code> otherwise.
	 */
	public boolean registerAlarm(State state) {
		if (mCurrentState != state) {
			switch (state) {
			case HIGH:
				registerActiveAlarm();
				//Log.d("regis", "high");
				break;
			case LOW:
				//Log.d("regis", "low");
				registerStandbyAlarm();
				break;
			case MID:
				registerActiveMidAlarm();
			}
			mCurrentState = state;
			return true;
		}
		return false;
	}

	/**
	 * Sets the alarm active.
	 */
	private void registerActiveAlarm() {
		long t = SystemClock.elapsedRealtime() + INTER_ACTIVE;
		
		mAm.setRepeating(AlarmManager.ELAPSED_REALTIME, t, INTER_ACTIVE, mAs);
		// Logging.
		if (KindroidSecurityApplication.LOG_ENABLED) {
			Log.i(getClass().getName(), "Set alarm to active mode.");
		}
	}
	
	private void registerActiveMidAlarm() {
		long t = SystemClock.elapsedRealtime() + INTER_ACTIVE_MID;
		
		mAm.setRepeating(AlarmManager.ELAPSED_REALTIME, t, INTER_ACTIVE, mAs);
		// Logging.
		if (KindroidSecurityApplication.LOG_ENABLED) {
			Log.i(getClass().getName(), "Set alarm to active mode.");
		}
	}

	/**
	 * Sets the alarm to standby. Every hour at :59.
	 */
	private void registerStandbyAlarm() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 0);
		long t = calendar.getTimeInMillis();
		// Sets the alarm.
		mAm.setRepeating(AlarmManager.RTC_WAKEUP, t, INTER_STANDBY, mAs);
		// Logging.
		if (KindroidSecurityApplication.LOG_ENABLED) {
			Log.i(getClass().getName(), "Set alarm to standby mode.");
		}
	}

}
