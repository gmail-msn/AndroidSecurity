/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.07
 * Description:
 */
package com.kindroid.security.ui;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import org.json.JSONObject;

import java.util.Locale;

import com.ehoo.client.request.Request;
import com.kindroid.security.R;
import com.kindroid.security.service.LocakMobileService;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.HttpRequest;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.Utilis;



public class DefenderTabMain extends TabActivity implements
		View.OnClickListener {

	public static int backTimes = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		return super.onMenuItemSelected(featureId, item);
	}

	BroadcastReceiver broadcast;
	private TabHost mTabHost;
	private LinearLayout mobile_exp_linear;
	private LinearLayout use_func_linear;
	private LinearLayout soft_center_linear;

	private TabSpec tabs1;
	private TabSpec tabs2;
	private TabSpec tabs3;
	private Intent mobileExpIntent;
	private Intent useFucIntent;
	private Intent softCenterIntent;
	private int current;

	private boolean menuShowed = false;
	private PopupWindow menuWindow;

	String downUrl;
	boolean isActive = true;
	private UpgradeDialog upgradeDialog;

	private ImageView one;
	private ImageView two;
	private ImageView three;
	private ImageView four;
	private ImageView five;

	private ImageView one_copy;
	private ImageView two_copy;
	private ImageView three_copy;
	private ImageView four_copy;
	private ImageView five_copy;

	Dialog loadingProgressDialog;

	KindroidSecurityApplication application;

	boolean isLoading = false;
	private static String releaseNotes;

	/** Called when the activity is first created. */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.defender_tab_main);

		application = (KindroidSecurityApplication) getApplication();
		application.setAppIsActive(true);
		findView();
		bindListenerToView();
		current = R.id.mobile_sum_linear;

		IntentFilter ifilter = new IntentFilter();
		broadcast = new UiUpdateBroadcast();
		ifilter.addAction("ehoo.com.update.ui");
		registerReceiver(broadcast, ifilter);
		setupMenu();
		backTimes = 0;
		upgradeDialog = new UpgradeDialog(this);
		new Thread(new InitMapRunable()).start();
		//add for softcenter request
		synchronized(SoftCenterTabActivity.initReuqest){
			if(SoftCenterTabActivity.initReuqest == false){
				SoftCenterTabActivity.request = new Request();
				SoftCenterTabActivity.request.init(true);
				SoftCenterTabActivity.initReuqest = true;				
			}
		}
		
	}

	void findView() {
		mTabHost = this.getTabHost();
		mobile_exp_linear = (LinearLayout) findViewById(R.id.mobile_sum_linear);
		use_func_linear = (LinearLayout) findViewById(R.id.fuc_list_linear);
		soft_center_linear = (LinearLayout) findViewById(R.id.soft_center_linear);
		mobileExpIntent = new Intent(this, MobileExpActivity.class);
		useFucIntent = new Intent(this, UseFucActivity.class);
		softCenterIntent = new Intent(this, SoftCenterTabActivity.class);

		tabs1 = mTabHost.newTabSpec("tab1").setIndicator("tab1")
				.setContent(mobileExpIntent);
		tabs2 = mTabHost.newTabSpec("tab2").setIndicator("tab2")
				.setContent(useFucIntent);
		tabs3 = mTabHost.newTabSpec("tab3").setIndicator("tab3")
				.setContent(softCenterIntent);
	}

	void bindListenerToView() {
		mTabHost.addTab(tabs1);

		mTabHost.addTab(tabs2);
		mTabHost.addTab(tabs3);
		mobile_exp_linear.setOnClickListener(this);
		use_func_linear.setOnClickListener(this);
		soft_center_linear.setOnClickListener(this);
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				backTimes = 0;
			}
		});
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
		// TODO Auto-generated method stub
		unregisterReceiver(broadcast);
		//close request for softcenter
		synchronized(SoftCenterTabActivity.initReuqest){
			if(SoftCenterTabActivity.initReuqest == true){
				SoftCenterTabActivity.request.close();
				//SoftCenterTabActivity.request = null;
				SoftCenterTabActivity.initReuqest = false;				
			}
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) {
		case R.id.mobile_sum_linear:
			mobile_exp_linear.setBackgroundResource(R.drawable.linear_focuse);
			use_func_linear.setBackgroundResource(R.drawable.linear_unfocuse);
			soft_center_linear
					.setBackgroundResource(R.drawable.linear_unfocuse);
			mTabHost.setCurrentTab(0);
			current = v.getId();
			break;
		case R.id.fuc_list_linear:
			mobile_exp_linear.setBackgroundResource(R.drawable.linear_unfocuse);
			use_func_linear.setBackgroundResource(R.drawable.linear_focuse);
			soft_center_linear
					.setBackgroundResource(R.drawable.linear_unfocuse);
			mTabHost.setCurrentTab(1);
			current = v.getId();
			break;
		case R.id.soft_center_linear:
			mobile_exp_linear.setBackgroundResource(R.drawable.linear_unfocuse);
			use_func_linear.setBackgroundResource(R.drawable.linear_unfocuse);
			soft_center_linear.setBackgroundResource(R.drawable.linear_focuse);
			Intent intent = new Intent(this, SoftCenterTabActivity.class);
			startActivity(intent);
			
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
					startActivity(new Intent(DefenderTabMain.this,
							AccountManageActivity.class));
				} else {
					startActivityForResult(new Intent(DefenderTabMain.this,
							AccountManageLoginedActivity.class), 0);
				}
				break;
			case R.id.about_us_linear:
				startActivityForResult(new Intent(DefenderTabMain.this,
						AboutUsActivity.class), 1);

				break;
			case R.id.use_help_linear:
				startActivityForResult(new Intent(DefenderTabMain.this,
						HelpActivity.class), 2);

				break;
			case R.id.feedback_linear:
				startActivity(new Intent(DefenderTabMain.this,
						FeedbackActivity.class));
				break;
			case R.id.online_update_linear:

				if (!checkNetwork()) {
					Toast.makeText(DefenderTabMain.this,
							R.string.bakcup_remote_network_unabailable_text,
							Toast.LENGTH_LONG).show();
					return;
				}
				new UpdatingThread().start();
				isLoading = true;
				showDialog();
				break;
			}
			menuWindow.dismiss();
			menuShowed = false;

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

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub

		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && menuShowed) {
			menuWindow.dismiss();
			menuShowed = false;
			return true;
		}
		if (event.getAction() != KeyEvent.ACTION_UP)
			return super.dispatchKeyEvent(event);

		if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			if (!menuShowed) {
				if (menuWindow == null)
					setupMenu();
				else
					menuWindow.showAtLocation(this.findViewById(R.id.bottom),
							Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
				menuShowed = true;
			} else {
				menuWindow.dismiss();
				menuShowed = false;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	private void setupMenu() {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);

		View layout = inflater.inflate(R.layout.menu, null);

		menuWindow = new PopupWindow(layout, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		layout.findViewById(R.id.account_man_linear).setOnClickListener(
				menuListener);
		layout.findViewById(R.id.about_us_linear).setOnClickListener(
				menuListener);
		layout.findViewById(R.id.online_update_linear).setOnClickListener(
				menuListener);
		layout.findViewById(R.id.use_help_linear).setOnClickListener(
				menuListener);
		layout.findViewById(R.id.feedback_linear).setOnClickListener(
				menuListener);

	}

	private void showDialog() {

		if (loadingProgressDialog == null) {
			loadingProgressDialog = new Dialog(this, R.style.softDialog) {
				public void onBackPressed() {
					isLoading = false;

				};
			};
			View view = LayoutInflater.from(this).inflate(
					R.layout.softmanage_prompt_dialog, null);
			one = (ImageView) view.findViewById(R.id.pr_one);
			two = (ImageView) view.findViewById(R.id.pr_two);
			three = (ImageView) view.findViewById(R.id.pr_three);
			four = (ImageView) view.findViewById(R.id.pr_four);
			five = (ImageView) view.findViewById(R.id.pr_five);

			one_copy = (ImageView) view.findViewById(R.id.pr_one_copy);
			two_copy = (ImageView) view.findViewById(R.id.pr_two_copy);
			three_copy = (ImageView) view.findViewById(R.id.pr_three_copy);
			four_copy = (ImageView) view.findViewById(R.id.pr_four_copy);
			five_copy = (ImageView) view.findViewById(R.id.pr_five_copy);
			loadingProgressDialog.setContentView(view);
			TextView tv = (TextView) loadingProgressDialog
					.findViewById(R.id.prompt_dialog_text);
			tv.setText(R.string.request_message_now);
		}

		loadingProgressDialog.show();

		new LoadingItem().start();
	}

	private class LoadingItem extends Thread {
		public void run() {

			while (isLoading) {
				for (int j = 0; j < 5; j++) {
					if (!isLoading)
						break;
					try {
						sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mProgressHandler.sendEmptyMessage(j);
				}
			}
			mProgressHandler.sendEmptyMessage(0);
			mProgressHandler.sendEmptyMessage(6);
		}
	}

	private Handler mProgressHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			one.setVisibility(View.VISIBLE);
			two.setVisibility(View.VISIBLE);
			three.setVisibility(View.VISIBLE);
			four.setVisibility(View.VISIBLE);
			five.setVisibility(View.VISIBLE);

			one_copy.setVisibility(View.INVISIBLE);
			two_copy.setVisibility(View.INVISIBLE);
			three_copy.setVisibility(View.INVISIBLE);
			four_copy.setVisibility(View.INVISIBLE);
			five_copy.setVisibility(View.INVISIBLE);

			switch (msg.what) {
			case 0:
				one.setVisibility(View.INVISIBLE);
				one_copy.setVisibility(View.VISIBLE);
				break;
			case 1:
				two.setVisibility(View.INVISIBLE);
				two_copy.setVisibility(View.VISIBLE);
				break;
			case 2:
				three.setVisibility(View.INVISIBLE);
				three_copy.setVisibility(View.VISIBLE);
				break;
			case 3:
				four.setVisibility(View.INVISIBLE);
				four_copy.setVisibility(View.VISIBLE);
				break;
			case 4:
				five.setVisibility(View.INVISIBLE);
				five_copy.setVisibility(View.VISIBLE);
				break;
			case 6:
				loadingProgressDialog.cancel();
				break;

			}
		}
	};

	class UpdatingThread extends Thread {
		public void run() {
			int versionCode = 0;
			PackageManager manager = getPackageManager();
			try {
				PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
				versionCode = info.versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			if (hasNewVersion(versionCode, DefenderTabMain.this)) {
				mGlobalHandle
						.sendEmptyMessage(MobileExpActivity.UPGRADE_VERSION);
			} else {
				mGlobalHandle
						.sendEmptyMessage(MobileExpActivity.UPGRADE_VERSION_NO);
			}
			isLoading = false;

		}
	}

	public boolean hasNewVersion(int oldVersionCode, Context ctx) {
		boolean b = false;
		try {
			String url = null;
			if (Locale.getDefault().getLanguage().equals("zh")) {
				url = Constant.UPGRADE_ZH_URL;
			} else {
				url = Constant.UPGRADE_EN_URL;
			}
			String str = HttpRequest.getData(url);
			JSONObject jobj = new JSONObject(str);
			if (jobj != null) {
				int result = jobj.getInt("result");
				if (result == 0) {
					downUrl = jobj.getString("upgradePath");
					int version = jobj.getInt("version");
					if (version > oldVersionCode && downUrl != null) {
						b = true;
						releaseNotes = jobj.getString("releaseNote");
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	protected Handler mGlobalHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (!isActive)
				return;

			switch (msg.what) {
			case MobileExpActivity.UPGRADE_VERSION:
				upgradeDialog.show();
				upgradeDialog.findViewById(R.id.upgrade_prompt_repeat_linear).setVisibility(View.GONE);
				upgradeDialog.findViewById(R.id.upgrade_confirm_linear).setVisibility(View.GONE);
				upgradeDialog.downUrl(downUrl);
				break;
			case MobileExpActivity.UPGRADE_VERSION_NO:
				Toast.makeText(DefenderTabMain.this,
						R.string.show_upgrade_newest, Toast.LENGTH_SHORT)
						.show();

				break;
			default:
				break;
			}
		}
	};

	protected void onResume() {
		super.onResume();
		
		isActive = true;
		
		if (current == R.id.mobile_sum_linear) {
			mobile_exp_linear
					.setBackgroundResource(R.drawable.linear_focuse);

		} else if (current == R.id.fuc_list_linear) {
			use_func_linear.setBackgroundResource(R.drawable.linear_focuse);
		}
		soft_center_linear
				.setBackgroundResource(R.drawable.linear_unfocuse);
	};

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isActive = false;
	}
	class InitMapRunable implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ConvertUtils.initMap(DefenderTabMain.this);
			
			
		}
		
	}

}