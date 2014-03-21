/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.08
 * Description:
 */
package com.kindroid.security.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.KindroidSecurityApplication;

public class LockMobileActivity extends Activity implements
		View.OnClickListener {
	private ImageView logo;
	private EditText lock_mobile_passwd;
	private Button button_ok;
	private Button button_send;
	
	private static final int mRandomPwdLength = 6;
	public static final String SMS_SEND_PWD_ACTIOIN = "sms_send_lockpwd_to_safe_mobile_number";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lock_mobile_activity);
		logo = (ImageView) findViewById(R.id.logo);
		lock_mobile_passwd = (EditText) findViewById(R.id.lock_mobile_passwd);
		button_ok = (Button) findViewById(R.id.button_ok);
		button_ok.setOnClickListener(this);
		button_send= (Button)findViewById(R.id.send_pwd);
		button_send.setOnClickListener(this);
		

		String str = getResources().getConfiguration().locale.getLanguage();

		if ("zh".equals(str)) {
			logo.setBackgroundResource(R.drawable.logo_zh);
		} else {
			logo.setBackgroundResource(R.drawable.logo_en);
		}

	}

	public void onAttachedToWindow() {

		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);

		super.onAttachedToWindow();

	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getKeyCode()==KeyEvent.KEYCODE_HOME){			
		}else{			
		}
		
		return super.dispatchKeyEvent(event);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.button_ok:
			String str = lock_mobile_passwd.getText().toString().trim();
			String passwd = KindroidSecurityApplication.sh.getString(
					Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD, "");
			if (!ConvertUtils.getMD5(str.getBytes()).equals(passwd)) {
				Toast.makeText(
						LockMobileActivity.this,
						getResources()
								.getString(
										R.string.please_input_correct_remote_security_passwd),
						Toast.LENGTH_LONG).show();
				return;
			}
			finish();
			break;
		case R.id.send_pwd:
			SharedPreferences sh = KindroidSecurityApplication.sh;
			String safeNumber = sh.getString(Constant.SHAREDPREFERENCES_SAFEMOBILENUMBER, "");
			if(TextUtils.isEmpty(safeNumber)){
				Toast.makeText(
						LockMobileActivity.this,
						getResources()
								.getString(
										R.string.safe_mobile_number_unexist),
						Toast.LENGTH_LONG).show();
				return;
			}
			String pwd = ConvertUtils.randomString(mRandomPwdLength);
			Editor editor = sh.edit();
			editor.putString(Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD,
					ConvertUtils.getMD5(pwd.getBytes()));
			editor.commit();
			//send new pwd to safe mobile number
			PendingIntent mPI = PendingIntent.getBroadcast(this, 0,
					new Intent(SMS_SEND_PWD_ACTIOIN), 0);
			SmsManager smsManager = SmsManager.getDefault();
			
			smsManager.sendTextMessage(safeNumber, null,
					pwd, mPI, null);
			Toast.makeText(
					LockMobileActivity.this,
					getResources()
							.getString(
									R.string.sms_send_pwd_to_safe_number),
					Toast.LENGTH_LONG).show();
			break;
		}

	}

}