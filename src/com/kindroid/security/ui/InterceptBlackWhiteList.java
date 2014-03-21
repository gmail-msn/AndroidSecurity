/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.09
 * Description:
 */
package com.kindroid.security.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.kindroid.security.R;

public class InterceptBlackWhiteList extends TabActivity implements
		View.OnClickListener {

	private LinearLayout mBlackListTabL;
	private LinearLayout mWhiteListTabL;
	private TabHost mTabHost;

	private TabSpec mTab1;
	private TabSpec mTab2;
	private Intent mBlackListI;
	private Intent mWhiteListI;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.intercept_black_whitelist);
		findView();

	}

	private void findView() {
		mTabHost = getTabHost();
		mBlackListTabL = (LinearLayout) findViewById(R.id.sms_tab_linear);
		mWhiteListTabL = (LinearLayout) findViewById(R.id.phone_tab_linear);

		mBlackListI = new Intent(this, InterceptBlackList.class);
		mWhiteListI = new Intent(this, InterceptWhiteList.class);

		mTab1 = mTabHost.newTabSpec("tab1").setIndicator("tab1")
				.setContent(mBlackListI);
		mTab2 = mTabHost.newTabSpec("tab2").setIndicator("tab2")
				.setContent(mWhiteListI);

		bindListerToView();
	}

	private void bindListerToView() {
		mTabHost.addTab(mTab1);
		mTabHost.addTab(mTab2);
		mBlackListTabL.setOnClickListener(this);
		mWhiteListTabL.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sms_tab_linear:
			mBlackListTabL.setBackgroundResource(R.drawable.softcenter_tab_on);
			mWhiteListTabL.setBackgroundResource(R.drawable.softcenter_tab_bg);
			mTabHost.setCurrentTab(0);
			break;
		case R.id.phone_tab_linear:
			mBlackListTabL.setBackgroundResource(R.drawable.softcenter_tab_bg);
			mWhiteListTabL.setBackgroundResource(R.drawable.softcenter_tab_on);
			mTabHost.setCurrentTab(1);
			break;
		}
	}

}