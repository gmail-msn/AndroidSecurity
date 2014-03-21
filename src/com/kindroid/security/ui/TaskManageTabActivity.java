/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;

import com.kindroid.security.R;

public class TaskManageTabActivity extends TabActivity implements
		View.OnClickListener {

	private TabHost mTabHost;
	private View proc_tab_linear;
	private View service_tab_linear;
	private boolean mForOptimize = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.proc_tab);
		mForOptimize = getIntent().getBooleanExtra(MobileExamActivity.MOBILE_EXAM_OPTIMIZE_INTENT, false);
		
		proc_tab_linear = findViewById(R.id.proc_tab_linear);
		service_tab_linear = findViewById(R.id.service_tab_linear);
		proc_tab_linear.setOnClickListener(this);
		service_tab_linear.setOnClickListener(this);
		
		findViewById(R.id.home_icon).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(!mForOptimize){
							Intent homepage = new Intent(
									TaskManageTabActivity.this,
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

		mTabHost = getTabHost();
		TabHost.TabSpec procSpec = mTabHost.newTabSpec("proc");
		procSpec.setIndicator(getString(R.string.proc_manage_proc_text));
		Intent intent = new Intent();
		if(mForOptimize){
			intent.putExtra(MobileExamActivity.MOBILE_EXAM_OPTIMIZE_INTENT, true);
		}
		intent.setClass(this, ProcessManagerActivity.class);
		procSpec.setContent(intent);
		mTabHost.addTab(procSpec);

		TabHost.TabSpec serviceSpec = mTabHost.newTabSpec("service");
		serviceSpec
				.setIndicator(getString(R.string.proc_manage_service_text));
		intent = new Intent();
		intent.setClass(this, ServiceManageActivity.class);
		serviceSpec.setContent(intent);
		mTabHost.addTab(serviceSpec);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.proc_tab_linear:
			proc_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_on);
			service_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_bg);			
			mTabHost.setCurrentTab(0);
			break;
		case R.id.service_tab_linear:
			proc_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_bg);
			service_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_on);
			mTabHost.setCurrentTab(1);
			break;		
		}
	}

}
