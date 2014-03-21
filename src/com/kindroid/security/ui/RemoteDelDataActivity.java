/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.util.ConvertUtils;

public class RemoteDelDataActivity extends Activity implements
		android.view.View.OnClickListener {

	TextView function_title_tv;
	TextView command_title_tv;
	TextView command_des_tv;

	EditText lost_mobile_num_et;

	EditText kind_security_safe_num_et;

	Button button_ok;
	Button button_cancel;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.remote_deldata_activity);
		findView();
		function_title_tv.setText(getResources().getString(
				R.string.remote_data_del));
		command_title_tv.setText(getResources().getString(
				R.string.comman_del_content));
		command_des_tv.setText(getResources().getString(
				R.string.command_del_data));

	}

	void findView() {
		function_title_tv = (TextView) findViewById(R.id.function_title_tv);
		command_title_tv = (TextView) findViewById(R.id.command_title_tv);
		command_des_tv = (TextView) findViewById(R.id.command_des_tv);
		lost_mobile_num_et = (EditText) findViewById(R.id.lost_mobile_num_et);
		kind_security_safe_num_et = (EditText) findViewById(R.id.kind_security_safe_num_et);
		button_ok = (Button) findViewById(R.id.button_ok);
		button_cancel = (Button) findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(this);
		button_cancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.button_ok:
			String str = lost_mobile_num_et.getText().toString().trim();
			String str2 = kind_security_safe_num_et.getText().toString().trim();
			if (checkMessage(str, str2)) {
				String content = "#ehoo_del#" + str2;
				sendSms(str, content);
			}
			break;
		case R.id.button_cancel:
			finish();
			break;
		}

	}

	void sendSms(String mobile, String content) {

		PendingIntent mPI = PendingIntent.getBroadcast(this, 0, new Intent(
				"sms_send_del_action"), 0);
		SmsManager smsManager = SmsManager.getDefault();

		smsManager.sendTextMessage(mobile, null, content, mPI, null);
		finish();

	}

	boolean checkMessage(String str, String str2) {

		if (!ConvertUtils.isCellphone(str)) {
			Toast.makeText(
					this,
					getResources().getString(R.string.input_valide_mobile),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (str2.length() == 0) {
			Toast.makeText(this,
					getResources().getString(R.string.passwd_can_notbe_null),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

}