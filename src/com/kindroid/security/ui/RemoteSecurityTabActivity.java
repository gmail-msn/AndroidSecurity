/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;

public class RemoteSecurityTabActivity extends TabActivity implements
		View.OnClickListener {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		return super.onMenuItemSelected(featureId, item);
	}

	private TabHost mTabHost;
	private LinearLayout remote_security_linear;
	private LinearLayout detail_setting_linear;

	private TabSpec tabs1;
	private TabSpec tabs2;

	private Intent remoteSecurityIntent;
	private Intent detailSettingIntent;
	TextView function_title_tv;
	TextView des_title_tv;

	BroadcastReceiver broadcast;

	private int current;
	private boolean mForOptimize = false;

	/** Called when the activity is first created. */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.remote_security_tab_activity);

		findView();
		bindListenerToView();
		mForOptimize = getIntent().getBooleanExtra(MobileExamActivity.MOBILE_EXAM_OPTIMIZE_INTENT, false);
		View view = findViewById(R.id.home_icon);		
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!mForOptimize){
					Intent homepage = new Intent(RemoteSecurityTabActivity.this,
							DefenderTabMain.class);
					homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(homepage);
					finish();
				}else{
					setResult(MobileExamActivity.RESULT_CODE_FOR_FINISH);
					finish();
				}
			}
		});

		current = R.id.remote_security_linear;
		broadcast = new UiUpdateBroadcast();
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction("ehoo.com.update.remote.ui");
		registerReceiver(broadcast, ifilter);
		if (!KindroidSecurityApplication.sh
				.contains(Constant.SHAREDPREFERENCES_FIRSTINSTALLREMOTESECURITY))
			detail_setting_linear.performClick();

	}

	void findView() {
		function_title_tv = (TextView) findViewById(R.id.function_title_tv);
		des_title_tv = (TextView) findViewById(R.id.des_title_tv);
		mTabHost = this.getTabHost();
		remote_security_linear = (LinearLayout) findViewById(R.id.remote_security_linear);
		detail_setting_linear = (LinearLayout) findViewById(R.id.detail_setting_linear);

		remoteSecurityIntent = new Intent(this, RemoteSecurityActivity.class);
		detailSettingIntent = new Intent(this,
				RemoteSecuritySetupActivity.class);

		tabs1 = mTabHost.newTabSpec("tab1").setIndicator("tab1")
				.setContent(remoteSecurityIntent);
		tabs2 = mTabHost.newTabSpec("tab2").setIndicator("tab2")
				.setContent(detailSettingIntent);

	}

	class UiUpdateBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (!intent.getAction().equals("ehoo.com.update.remote.ui"))
				return;
			int id = intent.getIntExtra("upid", -1);

			if (id == -1) {
				boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
						Constant.SHAREDPREFERENCES_REMOTESECURITY, false);
				if (isTrue)
					des_title_tv.setText(getResources().getString(
							R.string.remote_security_open));
				else
					des_title_tv.setText(getResources().getString(
							R.string.remote_security_close));
			} else {
				if (id == 0)
					remote_security_linear.performClick();
				else if (id == 1)
					detail_setting_linear.performClick();

			}

		}

	}

	void bindListenerToView() {
		mTabHost.addTab(tabs1);

		mTabHost.addTab(tabs2);

		remote_security_linear.setOnClickListener(this);
		detail_setting_linear.setOnClickListener(this);

	}
	

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_REMOTESECURITY, false);
		if (isTrue)
			des_title_tv.setText(getResources().getString(
					R.string.remote_security_open));
		else
			des_title_tv.setText(getResources().getString(
					R.string.remote_security_close));
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(broadcast);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (v.getId() == current)
			return;
		current = v.getId();
		switch (v.getId()) {
		case R.id.remote_security_linear:
			remote_security_linear
					.setBackgroundResource(R.drawable.linear_focuse);
			detail_setting_linear
					.setBackgroundResource(R.drawable.linear_unfocuse);
			mTabHost.setCurrentTab(0);
		
			break;
		case R.id.detail_setting_linear:
			remote_security_linear
					.setBackgroundResource(R.drawable.linear_unfocuse);
			detail_setting_linear
					.setBackgroundResource(R.drawable.linear_focuse);
			mTabHost.setCurrentTab(1);
			
			break;

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 1) {
			if (resultCode == RESULT_OK && data != null
					&& data.getBooleanExtra("logined", false)) {

			} else
				finish();
		}

		super.onActivityResult(requestCode, resultCode, data);

	}
}