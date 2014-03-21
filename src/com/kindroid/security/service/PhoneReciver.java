/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.service;

import java.lang.reflect.Method;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.InterceptDataBase;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.NativeCursor;
import com.kindroid.security.util.PhoneType;
import com.kindroid.security.util.PhoneUtils;

public class PhoneReciver extends BroadcastReceiver {

	private Context context;
	private TelephonyManager tm;
	private long beginTime = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context = context;
		
		try {
			// String state =
			// intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			// 呼入的号码
			String phoneNumber = intent
					.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
			// System.out.println(state + "\t" + phoneNumber);

			tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			switch (tm.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING:// 来电响铃
				
				InterceptPhoneHandle(phoneNumber);
				// 挂断
				break;// 响铃
			case TelephonyManager.CALL_STATE_OFFHOOK: // 来电接通 去电拨出
				break;// 摘机
			case TelephonyManager.CALL_STATE_IDLE: // 来去电电话挂断
				break;// 挂机
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void InterceptPhoneHandle(String phoneNumber) {
		int night_model = ConvertUtils.betweenNightModel(context);

		if (night_model == 0) {
			int model = KindroidSecurityApplication.sh.getInt(
					Constant.SHAREDPREFERENCES_BLOCKINGRULES, 1);
			blockRulesHandle(model, phoneNumber);
		} else {
			blockRulesHandle(night_model, phoneNumber);
		}

	}

	private void InterceptHandleStyle(boolean isTrue) {
		if (!isTrue)
			return;
		int type = KindroidSecurityApplication.sh.getInt(
				Constant.INTERCEPT_TREAT_MODE, 1);
		switch (type) {
		case 2:
			try {
				
				 PhoneUtils.getITelephony(tm).silenceRinger();// 静铃
				 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				try {
					Method getITelephonyMethod = TelephonyManager.class
							.getDeclaredMethod("getITelephony", (Class[]) null);
					getITelephonyMethod.setAccessible(true);
					TelephonyManager mTelephonyManager = (TelephonyManager) context
							.getSystemService(Context.TELEPHONY_SERVICE);
					Object mITelephony = getITelephonyMethod.invoke(
							mTelephonyManager, (Object[]) null);
					// 挂断 - mITelephony.endCall();
					mITelephony.getClass()
							.getMethod("silenceRinger", new Class[] {})
							.invoke(mITelephony, new Object[] {});
					// 取消通知 - mITelephony.cancelMissedCallsNotification()
					mITelephony
							.getClass()
							.getMethod("cancelMissedCallsNotification",
									new Class[] {})
							.invoke(mITelephony, new Object[] {});
				} catch (Exception ne) {
					ne.printStackTrace();
				}
				e.printStackTrace();
			}

			break;
		default:

			try {
				PhoneUtils.getITelephony(tm).endCall();// 静铃
			} catch (Exception e) {
				// TODO Auto-generated catch block
				try {
					Method getITelephonyMethod = TelephonyManager.class
							.getDeclaredMethod("getITelephony", (Class[]) null);
					getITelephonyMethod.setAccessible(true);
					TelephonyManager mTelephonyManager = (TelephonyManager) context
							.getSystemService(Context.TELEPHONY_SERVICE);
					Object mITelephony = getITelephonyMethod.invoke(
							mTelephonyManager, (Object[]) null);
					// 挂断 - mITelephony.endCall();
					mITelephony.getClass().getMethod("endCall", new Class[] {})
							.invoke(mITelephony, new Object[] {});
					// 取消通知 - mITelephony.cancelMissedCallsNotification()
					mITelephony
							.getClass()
							.getMethod("cancelMissedCallsNotification",
									new Class[] {})
							.invoke(mITelephony, new Object[] {});
				} catch (Exception ne) {
					e.printStackTrace();
				}
				e.printStackTrace();
			}
			break;
		}

	}

	private void blockRulesHandle(int model, String address) {

		PhoneType ph = ConvertUtils.getPhonetype(address);
		if (ph.getPhoneNo() == null) {
			return;
		}
		int i = 0;
		boolean isTrue = false;
		switch (model) {
		case 1:
			NativeCursor nc = new NativeCursor();
			nc.setmRequestType(1);
			nc = ConvertUtils.isBlackOrWhiteList(context, nc, ph);
			if (nc.ismIsExists() && nc.ismRingStatus()) {
				
				nc = new NativeCursor();
				
				nc = ConvertUtils.isBlackOrWhiteList(context, nc, ph);
				if (!nc.ismIsExists()) {
					isTrue = true;
					InterceptHandleStyle(isTrue);
					InterceptDataBase.get(context).insertMmsPhone(4,
							ph.getPhoneNo(), Calendar.getInstance(), "", 0,
							"");
				}
			}
			i = 1;

			break;
		case 2:
			boolean hasContact = ConvertUtils.hasNumInContact(ph.getPhoneNo(),
					context);
			if (!hasContact) {
				NativeCursor n = new NativeCursor();
				n.setmRequestType(2);
				n = ConvertUtils.isBlackOrWhiteList(context, n, ph);
				if (!n.ismIsExists()) {
					isTrue = true;
					InterceptHandleStyle(isTrue);
					InterceptDataBase.get(context)
							.insertMmsPhone(4, ph.getPhoneNo(),
									Calendar.getInstance(), "", 0, "");
				}
			}

			i = 2;

			break;
		case 3:
			NativeCursor n = new NativeCursor();
			n.setmRequestType(2);
			n = ConvertUtils.isBlackOrWhiteList(context, n, ph);
			if (!n.ismIsExists()) {
				isTrue = true;
				InterceptHandleStyle(isTrue);
				InterceptDataBase.get(context).insertMmsPhone(4,
						ph.getPhoneNo(), Calendar.getInstance(), "", 0, "");
			}
			i = 3;
			break;
		case 4:
			i = 4;
			break;
		case 5:
			i = 5;

		case 6:
			i = 6;
			isTrue = true;
			InterceptHandleStyle(isTrue);			
			InterceptDataBase.get(context).insertMmsPhone(4, ph.getPhoneNo(),
					Calendar.getInstance(), "", 0, "");

			break;

		default:
			break;
		}		
		
		if (isTrue) {
			SmsReciver.sendBroact(context, 4);
		}		

	}

	// private boolean containKeyWork(String content) {
	// boolean mHasKeyWork=false;
	// if(content.trim().length()==0){
	// return mHasKeyWork;
	// }
	// try{
	// Cursor c = InterceptDataBase.get(context).selectKeyWordList();
	//
	// if(c!=null&&c.getCount()>0){
	// while(c.moveToNext()){
	// String keyword =
	// c.getString(c.getColumnIndex(InterceptDataBase.KEYWORDZH));
	// if(content.contains(keyword)){
	// mHasKeyWork=true;
	// break;
	// }
	//
	// }
	// }
	// if(c!=null){
	// c.close();
	// }
	//
	// }catch(Exception e){
	// e.printStackTrace();
	// }
	// return mHasKeyWork;
	// }

}
