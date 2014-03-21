/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.09
 * Description:
 */

package com.kindroid.security.ui;

import com.kindroid.security.R;

import android.app.TabActivity;
import android.content.Intent;

import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class InterceptHistory extends TabActivity implements
		View.OnClickListener {

	/** Called when the activity is first created. */

	private LinearLayout mSmsTabL;
	private LinearLayout mPhoneTabL;
	private TabHost mTabHost;
	
	
	private TabSpec mTab1;
	private TabSpec mTab2;
	private Intent mInterceptHistorySmsI;
	private Intent mInterceptHistoryPhoneI;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.intercept_history);
		findView();

	}

	private void findView() {
		mTabHost=getTabHost();
		mSmsTabL = (LinearLayout) findViewById(R.id.sms_tab_linear);
		mPhoneTabL = (LinearLayout) findViewById(R.id.phone_tab_linear);
		
		mInterceptHistorySmsI = new Intent(this, InterceptHistorySms.class);
		mInterceptHistoryPhoneI = new Intent(this,
				InterceptHistoryPhone.class);
	

		mTab1 = mTabHost.newTabSpec("tab1").setIndicator("tab1")
				.setContent(mInterceptHistorySmsI);
		mTab2 = mTabHost.newTabSpec("tab2").setIndicator("tab2")
				.setContent(mInterceptHistoryPhoneI);
		
		
		
		
		bindListerToView();
	}

	private void bindListerToView() {
		mTabHost.addTab(mTab1);
		mTabHost.addTab(mTab2);
		mSmsTabL.setOnClickListener(this);
		mPhoneTabL.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sms_tab_linear:
			mSmsTabL.setBackgroundResource(R.drawable.softcenter_tab_on);
			mPhoneTabL.setBackgroundResource(R.drawable.softcenter_tab_bg);
			mTabHost.setCurrentTab(0);
			break;
		case R.id.phone_tab_linear:
			mSmsTabL.setBackgroundResource(R.drawable.softcenter_tab_bg);
			mPhoneTabL.setBackgroundResource(R.drawable.softcenter_tab_on);
			mTabHost.setCurrentTab(1);
			break;
		}
	}

}