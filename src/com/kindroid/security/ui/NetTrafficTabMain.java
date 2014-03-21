/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import java.io.File;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;

/**
 * @author jie.li
 * 
 */

public class NetTrafficTabMain extends TabActivity implements OnClickListener {

	private LinearLayout netTraffic;
	private LinearLayout netTraffic_app;
	private LinearLayout netTraffic_settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.net_traffic_tab_main);
		KindroidSecurityApplication application = (KindroidSecurityApplication) getApplication();
		application.setAppIsActive(true);
		findViews();
		addTab();
		int sysVersion = Integer.parseInt(VERSION.SDK);
		if (sysVersion < 8) {
			netTraffic_app.setVisibility(View.GONE);
		} else {
			int num = KindroidSecurityApplication.sh.getInt(
					Constant.SHAREDPREFERENCES_APPFILEEXIT, 1);
			if (num == 1) {
				File file = new File(Constant.APPTRAFFICDIR);
				Editor et = KindroidSecurityApplication.sh.edit();
				if (file.exists()) {
					et.putInt(Constant.SHAREDPREFERENCES_APPFILEEXIT, 2);
				} else {
					et.putInt(Constant.SHAREDPREFERENCES_APPFILEEXIT, 3);
					netTraffic_app.setVisibility(View.GONE);
				}
				et.commit();
			} else if (num == 3) {
				netTraffic_app.setVisibility(View.GONE);
			}
		}
	}

	private void findViews() {
		netTraffic = (LinearLayout) findViewById(R.id.net_traffic);

		netTraffic.setOnClickListener(this);
		netTraffic_settings = (LinearLayout) findViewById(R.id.net_traffic_settings);
		netTraffic_settings.setOnClickListener(this);
		netTraffic_app = (LinearLayout) findViewById(R.id.net_traffic_app);

		netTraffic_app.setOnClickListener(this);
		View view = findViewById(R.id.home_icon);

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent homepage = new Intent(NetTrafficTabMain.this,
						DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
			}
		});
	}

	private void addTab() {
		TabHost host = getTabHost();
		Intent summary = new Intent(this, NetTrafficSummary.class);
		Intent settings = new Intent(this, NetTrafficSettings.class);
		Intent nettraffic_app = new Intent(this, NetTrafficAppTabMain.class);
		TabSpec summaryTab = host.newTabSpec("tab1").setIndicator("tab1")
				.setContent(summary);
		TabSpec appTab = host.newTabSpec("tab2").setIndicator("tab2")
				.setContent(nettraffic_app);
		TabSpec settingsTab = host.newTabSpec("tab3").setIndicator("tab3")
				.setContent(settings);
		host.addTab(summaryTab);
		host.addTab(appTab);
		host.addTab(settingsTab);
		getTabHost().setCurrentTab(0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.net_traffic:
			netTraffic.setBackgroundResource(R.drawable.linear_focuse);
			netTraffic_settings
					.setBackgroundResource(R.drawable.linear_unfocuse);
			netTraffic_app.setBackgroundResource(R.drawable.linear_unfocuse);
			getTabHost().setCurrentTab(0);
			break;
		case R.id.net_traffic_app:
			netTraffic.setBackgroundResource(R.drawable.linear_unfocuse);
			netTraffic_settings
					.setBackgroundResource(R.drawable.linear_unfocuse);
			netTraffic_app.setBackgroundResource(R.drawable.linear_focuse);
			getTabHost().setCurrentTab(1);
			break;

		case R.id.net_traffic_settings:
			netTraffic.setBackgroundResource(R.drawable.linear_unfocuse);
			netTraffic_settings.setBackgroundResource(R.drawable.linear_focuse);
			netTraffic_app.setBackgroundResource(R.drawable.linear_unfocuse);
			getTabHost().setCurrentTab(2);
			break;
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}

}
