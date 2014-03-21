/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-09
 * Description:
 */
package com.kindroid.security.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.PhoneType;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author heli.zhao
 * 
 */
public class OutCallReceiver extends BroadcastReceiver {
	private boolean isNewOutCall = false;
	private static View mPhoneAddressView;
	private Context mContext;
	private static Timer timer = new Timer();
	private WindowManager wm;
	private static float sTouchStartX;
	private static float sTouchStartY;
	private static WindowManager.LayoutParams wmParams;
	private static float sLastStartX = 0;
	private static float sLastStartY = 0;;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction()
				.equals("android.intent.action.NEW_OUTGOING_CALL")) {
			try {
				String number = intent
						.getStringExtra("android.intent.extra.PHONE_NUMBER");
				if (number == null) {
					return;
				}
				if (number.startsWith("*")) {
					return;
				}
				if (number.startsWith("#")) {
					return;
				}
				displayPhoneAddress(context, number);
				isNewOutCall = true;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if (intent.getAction().equals(
				TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
			try {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				switch (tm.getCallState()) {
				case TelephonyManager.CALL_STATE_IDLE:
					if (isNewOutCall) {
						removePopView(context);
						isNewOutCall = false;
					}
					break;
				
				}

			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}

	private void removePopView(Context context) {
		if (mPhoneAddressView == null) {
			return;
		}

		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		try {
			wm.removeView(mPhoneAddressView);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	private void displayPhoneAddress(Context context, String number) {
		if (mPhoneAddressView != null) {
			removePopView(mContext);
		}
		boolean hasName = false;
		PhoneType pt = ConvertUtils.getPhonetype(number);
		String cityName = null;
		if (pt.getPhonetype() == 2) {
			String pNumber = pt.getPhoneNo();
			cityName = null;
			try {
				cityName = ConvertUtils.getCityNameByCode(context,
						pNumber.substring(0, 3));
			} catch (Exception e) {
				cityName = null;
				e.printStackTrace();
			}
			if (cityName != null) {				
				hasName = true;
			} else {
				try {
					cityName = ConvertUtils.getCityNameByCode(context,
							pNumber.substring(0, 4));
				} catch (Exception e) {
					cityName = null;
					e.printStackTrace();
				}
				if (cityName != null) {
					hasName = true;
				}
			}

		} else if (pt.getPhonetype() == 1) {
			// String regionCode = ConvertUtils.getCode(this, pt.getPhoneNo());
			cityName = null;
			try {
				// cityName = ConvertUtils.getCityNameByCode(this, regionCode);
				cityName = ConvertUtils.getPhoneCity(context, pt.getPhoneNo());
			} catch (Exception e) {
				cityName = null;
				e.printStackTrace();
			}
			if (cityName != null) {
				hasName = true;
			}
		} else if (pt.getPhonetype() == 0) {
			cityName = ConvertUtils.getAddrForPhone(pt.getPhoneNo());
			if (cityName != null) {
				hasName = true;
			}

		}
		if(!hasName || cityName == null){
			return;
		}
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPhoneAddressView = inflater.inflate(R.layout.popview, null);

		wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		wmParams = new WindowManager.LayoutParams();
		wmParams.width = wmParams.WRAP_CONTENT;
		wmParams.height = wmParams.WRAP_CONTENT;
		wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		wmParams.format = 1;
		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;

		TextView tv = (TextView) mPhoneAddressView
				.findViewById(R.id.phone_city);
		tv.setText(cityName);
		wm.addView(mPhoneAddressView, wmParams);
		timer.schedule(new TimerTaskForPopview(context), 12000);
		mPhoneAddressView.setOnTouchListener(new PopViewOnTouchListener());
	}
	private class PopViewOnTouchListener implements View.OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			float x = event.getRawX();
			
			float y = event.getRawY() - 25; 
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:				
				sTouchStartX = event.getX();
				sTouchStartY = event.getY();
				
				break;
			case MotionEvent.ACTION_MOVE:	
				updateViewPosition(x, y, wm);
				break;
			case MotionEvent.ACTION_UP:
				updateViewPosition(x, y, wm);
				sLastStartX = x - sTouchStartX;
				sLastStartY = y - sTouchStartY;
				sTouchStartX = sTouchStartY = 0;
				break;
			}

			return false;
		}
		
	}
	private static void updateViewPosition(float x, float y, WindowManager wm) {
		// 更新浮动窗口位置参数
		wmParams.x = (int) (x - sTouchStartX);
		wmParams.y = (int) (y - sTouchStartY);
		wm.updateViewLayout(mPhoneAddressView, wmParams);		
	}

	private class TimerTaskForPopview extends TimerTask {
		TimerTaskForPopview(Context context) {
			mContext = context;
		}

		public void run() {
			if (mPhoneAddressView != null) {
				mHandler.sendEmptyMessage(0);
			}
		}
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				removePopView(mContext);
				break;
			}
		}

	};

}
