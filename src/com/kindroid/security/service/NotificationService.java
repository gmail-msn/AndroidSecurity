/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.kindroid.security.notification.NetTrafficNotification;

public class NotificationService extends Service {
	
	public static int showType=0;
	
	NetTrafficNotification mNetTrafficNotification;
	
    private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				mNetTrafficNotification.showNetTrafficNotification();
				break;
			case 1:
				mNetTrafficNotification.showInterceptNotification();
				break;
			default:
				break;
			}
		}
    	
    };

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (mNetTrafficNotification == null) {
			mNetTrafficNotification = new NetTrafficNotification(getApplication(),this);
		}
		
		handler.sendEmptyMessageDelayed(showType, 1);
		if(showType!=0){
			showType=0;
		}
	}
}
