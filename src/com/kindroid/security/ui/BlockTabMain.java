/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.0907
 * Description:
 */
package com.kindroid.security.ui;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.kindroid.security.service.UpdateProbService;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.UpdateProbThread;
import com.kindroid.security.util.UpdateStaticsThread;
import com.kindroid.security.util.Utilis;

import com.kindroid.security.R;

public class BlockTabMain extends TabActivity implements View.OnClickListener {

	BroadcastReceiver mBroadcast;
	private TabHost mTabHost;
	private LinearLayout mInterceptHistoryL;
	private LinearLayout mInterceptBlackWhiteListL;
	private LinearLayout mInterceptSettingL;
	private TextView mFunctionTitleT;
	

	private TabSpec mTab1;
	private TabSpec mTab2;
	private TabSpec mTab3;
	private Intent mInterceptHistoryI;
	private Intent mInterceptBlackWhiteListI;
	private Intent mInterceptSettingI;

	private boolean mIsActive = true;
	public static BlockTabMain instance;
	
	private BroadcastReceiver mReciver;

	/** Called when the activity is first created. */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		KindroidSecurityApplication application = (KindroidSecurityApplication) getApplication();
		application.setAppIsActive(true);
		instance = this;
		
		setContentView(R.layout.block_tab_main);
		findView();
		bindListenerToView();
		addBackLister();
		IntentFilter mIt = new IntentFilter(
				Constant.BROACTUPDATEINFINISHBLOCK);

		registerReceiver(mReciver, mIt);

		

		// setupMenu();

	}
	void addBackLister(){
		View view = findViewById(R.id.home_icon);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent homepage = new Intent(BlockTabMain.this,
						DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
			}
		});
		mReciver=new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				Intent homepage = new Intent(BlockTabMain.this,
						DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
				
			}
		};
	}
	

	void findView() {
		mTabHost = this.getTabHost();
		mInterceptHistoryL = (LinearLayout) findViewById(R.id.mobile_sum_linear);
		mInterceptBlackWhiteListL = (LinearLayout) findViewById(R.id.fuc_list_linear);
		mInterceptSettingL = (LinearLayout) findViewById(R.id.soft_center_linear);
		mFunctionTitleT=(TextView) findViewById(R.id.function_title_tv);
		mInterceptHistoryI = new Intent(this, InterceptHistory.class);
		mInterceptBlackWhiteListI = new Intent(this,
				InterceptBlackWhiteList.class);
		mInterceptSettingI = new Intent(this, InterceptSetting.class);

		mTab1 = mTabHost.newTabSpec("tab1").setIndicator("tab1")
				.setContent(mInterceptHistoryI);
		mTab2 = mTabHost.newTabSpec("tab2").setIndicator("tab2")
				.setContent(mInterceptBlackWhiteListI);
		mTab3 = mTabHost.newTabSpec("tab3").setIndicator("tab3")
				.setContent(mInterceptSettingI);
	}

	void bindListenerToView() {
		mTabHost.addTab(mTab1);

		mTabHost.addTab(mTab2);
		mTabHost.addTab(mTab3);
		mInterceptHistoryL.setOnClickListener(this);
		mInterceptBlackWhiteListL.setOnClickListener(this);
		mInterceptSettingL.setOnClickListener(this);

	}
	public static void hideBottomLinear(){
		if(instance != null){
			instance.findViewById(R.id.bottom).setVisibility(View.GONE);
		}
	}
	public static void showBottomLinear(){
		if(instance != null){
			instance.findViewById(R.id.bottom).setVisibility(View.VISIBLE);
		}
	}

	class UiUpdateBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (!intent.getAction().equals("ehoo.com.update.ui"))
				return;
			int id = intent.getIntExtra("upid", -1);
			if (id < 0 || id > 1)
				return;
			getTabHost().setCurrentTab(id);
		}

	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		unregisterReceiver(mReciver);
		/*
		if(UpdateStaticsThread.mUpdated){
			UpdateProbThread upt = new UpdateProbThread(this);
			upt.start();
		}
		*/
		Intent intent = new Intent(this, UpdateProbService.class);
		startService(intent);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		/*
		 * if (v.getId() == current) return; current = v.getId();
		 */
		switch (v.getId()) {
		case R.id.mobile_sum_linear:
			mInterceptHistoryL.setBackgroundResource(R.drawable.linear_focuse);
			mInterceptBlackWhiteListL
					.setBackgroundResource(R.drawable.linear_unfocuse);
			mInterceptSettingL
					.setBackgroundResource(R.drawable.linear_unfocuse);
			mFunctionTitleT.setText(R.string.anti_spam_title);
			mTabHost.setCurrentTab(0);

			break;
		case R.id.fuc_list_linear:
			mInterceptHistoryL
					.setBackgroundResource(R.drawable.linear_unfocuse);
			mInterceptBlackWhiteListL
					.setBackgroundResource(R.drawable.linear_focuse);
			mInterceptSettingL
					.setBackgroundResource(R.drawable.linear_unfocuse);
			mFunctionTitleT.setText(R.string.anti_spam_title);
			mTabHost.setCurrentTab(1);

			break;
		case R.id.soft_center_linear:
			mInterceptHistoryL
					.setBackgroundResource(R.drawable.linear_unfocuse);
			mInterceptBlackWhiteListL
					.setBackgroundResource(R.drawable.linear_unfocuse);
			mInterceptSettingL.setBackgroundResource(R.drawable.linear_focuse);
			mFunctionTitleT.setText(R.string.anti_spam_title);
			mTabHost.setCurrentTab(2);

			break;

		}

	}

	private OnClickListener menuListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.account_man_linear:
				if (!Utilis.hasLogined()) {
					startActivity(new Intent(BlockTabMain.this,
							AccountManageActivity.class));
				} else {
					startActivityForResult(new Intent(BlockTabMain.this,
							AccountManageLoginedActivity.class), 0);
				}
				break;
			case R.id.about_us_linear:
				startActivityForResult(new Intent(BlockTabMain.this,
						AboutUsActivity.class), 1);

				break;
			case R.id.use_help_linear:
				startActivityForResult(new Intent(BlockTabMain.this,
						HelpActivity.class), 2);

				break;
			case R.id.feedback_linear:
				startActivity(new Intent(BlockTabMain.this,
						FeedbackActivity.class));
				break;
			}
		}
	};

	private boolean checkNetwork() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo != null) {
			if (networkinfo.isConnected())
				return true;
		}
		return false;
	}

	// private void setupMenu() {
	// LayoutInflater inflater = (LayoutInflater) this
	// .getSystemService(LAYOUT_INFLATER_SERVICE);
	//
	// View layout = inflater.inflate(R.layout.menu, null);
	//
	// menuWindow = new PopupWindow(layout, LayoutParams.FILL_PARENT,
	// LayoutParams.WRAP_CONTENT);
	// layout.findViewById(R.id.account_man_linear).setOnClickListener(
	// menuListener);
	// layout.findViewById(R.id.about_us_linear).setOnClickListener(
	// menuListener);
	// layout.findViewById(R.id.online_update_linear).setOnClickListener(
	// menuListener);
	// layout.findViewById(R.id.use_help_linear).setOnClickListener(
	// menuListener);
	// layout.findViewById(R.id.feedback_linear).setOnClickListener(
	// menuListener);
	//
	// }

	protected void onResume() {
		super.onResume();

		mIsActive=true;

	};

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mIsActive=false;
	}

}