/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.KindroidSecurityApplication;

public class LocakMobileService extends Service {
	private static final int mRandomPwdLength = 6;
	public static final String SMS_SEND_PWD_ACTIOIN = "sms_send_lockpwd_to_safe_mobile_number";

	@Override
	public void onCreate() {
		super.onCreate();

		final WindowManager mWm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		WindowManager.LayoutParams localLayoutParams1 = new WindowManager.LayoutParams();
		localLayoutParams1.width = WindowManager.LayoutParams.FILL_PARENT;
		localLayoutParams1.height = WindowManager.LayoutParams.FILL_PARENT;
		localLayoutParams1.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| WindowManager.LayoutParams.FLAG_FULLSCREEN;
		localLayoutParams1.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		
		//add screen orientation by miao
		localLayoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		
		final View view = LayoutInflater.from(this).inflate(
				R.layout.lock_mobile_activity, null);
		
		ImageView logo = (ImageView) view.findViewById(R.id.logo);
		final EditText lock_mobile_passwd = (EditText) view
				.findViewById(R.id.lock_mobile_passwd);
		final TextView login_status_tv=(TextView) view.findViewById(R.id.login_status_tv);
		Button button_ok = (Button) view.findViewById(R.id.button_ok);
		Button button_send= (Button)view.findViewById(R.id.send_pwd);
		button_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
				if(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
					login_status_tv.setText(R.string.sim_card_abssent);
					return;
				}
				SharedPreferences sh = KindroidSecurityApplication.sh;				
				String safeNumber = sh.getString(Constant.SHAREDPREFERENCES_SAFEMOBILENUMBER, "");
				/*
				if(TextUtils.isEmpty(safeNumber)){
					login_status_tv.setText(R.string.safe_mobile_number_unexist);
					
					return;
				}
				*/
				String pwd = ConvertUtils.randomString(mRandomPwdLength);
				Editor editor = sh.edit();
				editor.putString(Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD,
						ConvertUtils.getMD5(pwd.getBytes()));
				editor.commit();
				//send new pwd to safe mobile number
				PendingIntent mPI = PendingIntent.getBroadcast(LocakMobileService.this, 0,
						new Intent(SMS_SEND_PWD_ACTIOIN), 0);
				SmsManager smsManager = SmsManager.getDefault();
				
				smsManager.sendTextMessage(safeNumber, null,
						pwd, mPI, null);
				login_status_tv.setText(R.string.sms_send_pwd_to_safe_number);
				
				
			}
			
		});
		
		

		String str = getResources().getConfiguration().locale.getLanguage();

		if ("zh".equals(str)) {
			logo.setBackgroundResource(R.drawable.logo_zh);
		} else {
			logo.setBackgroundResource(R.drawable.logo_en);
		}

		mWm.addView(view, localLayoutParams1);
		

		button_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String str = lock_mobile_passwd.getText().toString().trim();
				String passwd = KindroidSecurityApplication.sh.getString(
						Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD, "");
				if (!ConvertUtils.getMD5(str.getBytes()).equals(passwd)) {
					int id = R.string.please_input_correct_remote_security_passwd;
					login_status_tv.setText(id);
					return;
				}
				mWm.removeView(view);
				stopSelf();
			}
		});
		
	}
	

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


	@Override
	public void onStart(Intent intent, final int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
