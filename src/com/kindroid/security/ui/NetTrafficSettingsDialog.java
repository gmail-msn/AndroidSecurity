/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.security.R;
import com.kindroid.security.service.NotificationService;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.UtilShareprefece;

public class NetTrafficSettingsDialog extends Activity implements OnClickListener{
	
	public final static int UPDATE_CELLULAR_LIMIT = 10011;

	public final static String INPUT_TYPE = "input_type";
	public final static String INPUT_VALUE = "input_value";

	public final static int INPUT_MONTH_LIMIT = 3;
	public final static int INPUT_CAL_DAY = 5;
	public final static int INPUT_LEFT_LIMIT = 4;
	
	private EditText mEditText;
	
	private TextView mOk;
	private TextView mCancel;
	private TextView mTitle;
	SharedPreferences sh;
	
	int mType = -1;
	
	KindroidSecurityApplication mApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.net_traffic_settings_dialog);
		findViews();
		 mApp=(KindroidSecurityApplication)getApplication();
		mType = getIntent().getIntExtra(INPUT_TYPE, -1);
		String value=getIntent().getStringExtra(INPUT_VALUE);
		if (mType == INPUT_MONTH_LIMIT) {
			mTitle.setText(R.string.net_traffice_settings_limit_dialog_title);
		} else if (mType == INPUT_LEFT_LIMIT) {
			mTitle.setText(R.string.net_traffice_settings_limit_dialog_left);
			
			
			InputFilter[] FilterArray = new InputFilter[1];
			FilterArray[0] = new InputFilter.LengthFilter(7);
		
			mEditText.setFilters(FilterArray);
			mEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
			
			
			
			
		} else if (mType == INPUT_CAL_DAY) {
			mTitle.setText(R.string.net_traffice_settings_cal_day);
			mEditText=(EditText) findViewById(R.id.limitNum_two);
		}
		mEditText.setVisibility(View.VISIBLE);
		mEditText.setText(value);
		mEditText.setSelection(mEditText.getText().toString().trim().length());
		KindroidSecurityApplication application = (KindroidSecurityApplication) getApplication();
		sh = application.getAdapter(SharedPreferences.class);
	}
	
	private void findViews() {
		mEditText = (EditText) findViewById(R.id.limitNum);
		mTitle = (TextView) findViewById(R.id.dialog_title);
		
		mOk = (TextView) findViewById(R.id.ok);
		mOk.setClickable(true);
		mOk.setOnClickListener(this);
		mCancel = (TextView) findViewById(R.id.cancel);
		mCancel.setClickable(true);
		mCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ok) {
			if (mType == 3) {
				if (checkMessage(mEditText.getText().toString(), 3)) {
					UtilShareprefece.getShareprefece().storeMessage(sh,UtilShareprefece.LIMIT_PER_MON_INT,mEditText.getText().toString());
					mApp.startService(new Intent(NetTrafficSettingsDialog.this,NotificationService.class));
					finish();
				}
			} else if (mType == 4) {
				if (checkMessage(mEditText.getText().toString(), 4)) {
					UtilShareprefece.getShareprefece().storeMessage(sh,UtilShareprefece.LEFT_PER_MON_INT,mEditText.getText().toString());
					mApp.startService(new Intent(NetTrafficSettingsDialog.this,NotificationService.class));
					finish();
				}
			} else if (mType == 5) {
				if (checkMessage(mEditText.getText().toString(), 5)) {
					UtilShareprefece.getShareprefece().storeMessage(sh,UtilShareprefece.CAL_DATE_INT,mEditText.getText().toString());
					mApp.startService(new Intent(NetTrafficSettingsDialog.this,NotificationService.class));
				
					finish();
				}
			}
		} else if (v.getId() == R.id.cancel) {
			finish();
		}
	}
	
	boolean checkMessage(String str, int type) {
		if (str.length() == 0) {
			Toast.makeText(this,getResources().getString(R.string.message_connot_null),Toast.LENGTH_SHORT).show();
			return false;
		} 
		if(type==3){
			if(str.trim().equals("0")){
				Toast.makeText(this,getResources().getString(R.string.network_need_oval_zero),Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		if (type == 4) {
		//	int limit = Integer.parseInt(sh.getString(UtilShareprefece.LIMIT_PER_MON_INT, "30"));
			
			if(str.startsWith(".")){
				Toast.makeText(this,getResources().getString(R.string.corect_format_num),Toast.LENGTH_SHORT).show();
				return false;
			}
		
			
	//		double left = Double.parseDouble(str);
			
//			if (left > limit) {
//				Toast.makeText(this,getResources().getString(R.string.left_less_limit),Toast.LENGTH_SHORT).show();
//				return false;
//			}
			
		}
		if (type == 5) {
			int day = Integer.parseInt(str);
			if (day <= 0 || day > 31) {
				Toast.makeText(this,getResources().getString(R.string.less_zero_more_31),Toast.LENGTH_SHORT).show();
				return false;
			}
		}

		return true;

	}

	
}
