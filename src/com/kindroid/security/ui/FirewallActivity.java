package com.kindroid.security.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.security.R;
import com.kindroid.security.ui.AppItemAdapter;
import com.kindroid.security.ui.DefenderTabMain;
import com.kindroid.security.ui.DefenderTabMain.UpdatingThread;
import com.kindroid.security.util.AppItemDetail;
import com.kindroid.security.util.AppNetWorkDataBase;
import com.kindroid.security.util.Configuration;
import com.kindroid.security.util.FirewallApi;
import com.kindroid.security.util.RunCMDUtils;
import com.kindroid.security.util.Utilis;
import com.kindroid.security.util.FirewallApi.FirewallApp;
import com.kindroid.security.util.KindroidSecurityApplication;

/**
 * Main application activity. This is the screen displayed when you open the
 * application
 */
public class FirewallActivity extends Activity implements OnClickListener {
	private static final int NET_TYPE_WIFI = 0;
	private static final int NET_TYPE_3G = 1;
	// Menu options
	private static final int MENU_DISABLE = 0;
	private static final int MENU_TOGGLELOG = 1;
	private static final int MENU_APPLY = 2;
	private static final int MENU_SHOWRULES = 3;
	private static final int MENU_HELP = 4;
	private static final int MENU_SHOWLOG = 5;
	private static final int MENU_CLEARLOG = 6;
	private static final int MENU_SETPWD = 7;

	/** progress dialog instance */
	private Dialog progress = null;
	private ListView listview;
//	LinearLayout save_rule_linear;
//	LinearLayout switchText_linear;
	LinearLayout firewall_prompt_content;
	LinearLayout firewall_main_content;
	LinearLayout back_to_main_linear;

//	private TextView mSwitchFirewall;
//	private TextView mSaveRule;

//	private TextView mFirewallStatus;
	private TextView mNoRootPermission;
	private Boolean mHasRootPermission = null;

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
	private static boolean isLoadingData = false;
	private static boolean isDealingData = false;
	
	private TextView appNumTextView;
	
	private boolean menuShowed = false;
	private PopupWindow menuWindow;
	
	private FirewallApp[] apps;
	private AppItemAdapter adapter;
	private Dialog mProgressDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkPreferences();
		mHasRootPermission = null;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.app_firewall);
//		mFirewallStatus = (TextView) findViewById(R.id.label_mode);
		FirewallApi.assertBinaries(this, false);
		findViews();
		setFirewallStatusText();
		setupMenu();
		SharedPreferences sp = KindroidSecurityApplication.sh;
		FirewallApi.sDefaultIptables = sp.getInt(FirewallApi.DEFAULT_IPTABLES_APP, 0);
	}

	public boolean isFirstInstall() {
		boolean mFirstOfReinstall = false;
		File fireWallPrefile = new File(
				"/data/data/com.kindroid.security/shared_prefs/DroidWallPrefs.xml");
		if (!fireWallPrefile.exists()) {
			mFirstOfReinstall = true;
		}
		return mFirstOfReinstall;
	}
	
	
	public boolean hasRootPermission() {

		if (mHasRootPermission != null) {
			return mHasRootPermission;
		}

		boolean rooted = true;
//		if (KindroidSecurityApplication.mFirstOfReinstall) {
		if (isFirstInstall()) {
			if (FirewallApi.purgeIptablesOnceReinstall(FirewallActivity.this, false)) {
				rooted = true;
				KindroidSecurityApplication.mFirstOfReinstall = false;
			} else {
				rooted = false;
			}
		} else {
//			if (!FirewallApi.hasRootAccess(FirewallActivity.this, false)) {
			if (!RunCMDUtils.isRooted(FirewallActivity.this)) {
				rooted = false;
			} else {
				rooted = true;
			}
		}
		mHasRootPermission = rooted;
		return mHasRootPermission;
	}

	private void findViews() {
//		mSwitchFirewall = (TextView) findViewById(R.id.switchTextView);
		firewall_main_content = (LinearLayout) findViewById(R.id.firewall_main_content);
		firewall_prompt_content = (LinearLayout) findViewById(R.id.firewall_prompt_content);
//		save_rule_linear = (LinearLayout) findViewById(R.id.save_rule_linear);
//		switchText_linear = (LinearLayout) findViewById(R.id.switchText_linear);
		back_to_main_linear = (LinearLayout) findViewById(R.id.back_to_main_linear);
//		save_rule_linear.setOnClickListener(this);
//		switchText_linear.setOnClickListener(this);
		back_to_main_linear.setOnClickListener(this);
		boolean enabled = FirewallApi.isEnabled(this);
//		if (enabled) {
//			mSwitchFirewall.setText(R.string.app_firewall_disable);
//		} else {
//			mSwitchFirewall.setText(R.string.app_firewall_enable);
//		}
//		mSaveRule = (TextView) findViewById(R.id.saveRuleTextView);
		findViewById(R.id.home_icon).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent homepage = new Intent(FirewallActivity.this,
								DefenderTabMain.class);
						homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(homepage);
						finish();
					}
				});

	}

	private void setupMenu() {
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

		View layout = inflater.inflate(R.layout.firewall_menu, null);

		menuWindow = new PopupWindow(layout, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layout.findViewById(R.id.disableWifiLayout).setOnClickListener(menuListener);
		layout.findViewById(R.id.allowWifiLayout).setOnClickListener(menuListener);
		layout.findViewById(R.id.disable3GLayout).setOnClickListener(menuListener);
		layout.findViewById(R.id.allow3GLayout).setOnClickListener(menuListener);

	}
	
	public void initLoadingDialog(boolean enable) {
		mProgressDialog = new Dialog(this, R.style.softDialog){

			@Override
			public void onBackPressed() {
			}
		};
		View view = LayoutInflater.from(this).inflate(
				R.layout.startmanage_prompt_dialog, null);
		mProgressDialog.setContentView(view);
		if (enable) {
			TextView tv = (TextView) mProgressDialog
					.findViewById(R.id.prompt_dialog_text);
			tv.setText(R.string.firewall_loading);
		}
		one = (ImageView) mProgressDialog.findViewById(R.id.pr_one);
		two = (ImageView) mProgressDialog.findViewById(R.id.pr_two);
		three = (ImageView) mProgressDialog.findViewById(R.id.pr_three);
		four = (ImageView) mProgressDialog.findViewById(R.id.pr_four);
		five = (ImageView) mProgressDialog.findViewById(R.id.pr_five);

		one_copy = (ImageView) mProgressDialog.findViewById(R.id.pr_one_copy);
		two_copy = (ImageView) mProgressDialog.findViewById(R.id.pr_two_copy);
		three_copy = (ImageView) mProgressDialog
				.findViewById(R.id.pr_three_copy);
		four_copy = (ImageView) mProgressDialog.findViewById(R.id.pr_four_copy);
		five_copy = (ImageView) mProgressDialog.findViewById(R.id.pr_five_copy);
		mProgressDialog.show();
		new LoadingThread().start();
//		initList();
	}
	private void initList(){
		if (this.listview == null) {
			this.listview = (ListView) this.findViewById(R.id.listview);
		}
		/*
		if (hasRootPermission()) {
			// Force re-loading the application list
			if (this.listview.getVisibility() != View.VISIBLE)
				this.listview.setVisibility(View.VISIBLE);
//			View layout3 = findViewById(R.id.firewall_layout3);
//			if (layout3 != null && layout3.getVisibility() != View.VISIBLE)
//				layout3.setVisibility(View.VISIBLE);
			TextView text_no_root = (TextView) findViewById(R.id.text_no_root);
			if (text_no_root != null
					&& text_no_root.getVisibility() != View.GONE)
				text_no_root.setVisibility(View.GONE);

			ImageView image_no_support = (ImageView) findViewById(R.id.img_nosupport);
			if (image_no_support != null
					&& image_no_support.getVisibility() != View.GONE)
				image_no_support.setVisibility(View.GONE);

			FirewallApi.applications = null;
			final String pwd = getSharedPreferences(FirewallApi.PREFS_NAME, 0)
					.getString(FirewallApi.PREF_PASSWORD, "");
			if (pwd.length() == 0) {
				// No password lock
				showOrLoadApplications();
			}
			appNumTextView = (TextView) findViewById(R.id.appNumTextView);
			if (null != FirewallApi.applications) {
				appNumTextView.setText(FirewallApi.applications.length + "");
			} else {
				appNumTextView.setText("0");
			}
		} else {
			if (this.listview.getVisibility() == View.VISIBLE)
				this.listview.setVisibility(View.GONE);
//			View layout3 = findViewById(R.id.firewall_layout3);
//			if (layout3 != null && layout3.getVisibility() == View.VISIBLE)
//				layout3.setVisibility(View.GONE);
			TextView text_no_root = (TextView) findViewById(R.id.text_no_root);
			if (text_no_root != null
					&& text_no_root.getVisibility() == View.GONE)
				text_no_root.setVisibility(View.VISIBLE);

			ImageView image_no_support = (ImageView) findViewById(R.id.img_nosupport);
			if (image_no_support != null
					&& image_no_support.getVisibility() == View.GONE)
				image_no_support.setVisibility(View.VISIBLE);

			View contenthead = findViewById(R.id.firewall_layout_head);

			if (contenthead != null
					&& contenthead.getVisibility() == View.VISIBLE)
				contenthead.setVisibility(View.GONE);
		}
		*/
		
		// Force re-loading the application list
		if (this.listview.getVisibility() != View.VISIBLE)
			this.listview.setVisibility(View.VISIBLE);
//		View layout3 = findViewById(R.id.firewall_layout3);
//		if (layout3 != null && layout3.getVisibility() != View.VISIBLE)
//			layout3.setVisibility(View.VISIBLE);
		TextView text_no_root = (TextView) findViewById(R.id.text_no_root);
		if (text_no_root != null
				&& text_no_root.getVisibility() != View.GONE)
			text_no_root.setVisibility(View.GONE);

		ImageView image_no_support = (ImageView) findViewById(R.id.img_nosupport);
		if (image_no_support != null
				&& image_no_support.getVisibility() != View.GONE)
			image_no_support.setVisibility(View.GONE);
		
		final String pwd = getSharedPreferences(FirewallApi.PREFS_NAME, 0)
				.getString(FirewallApi.PREF_PASSWORD, "");
		if (pwd.length() == 0) {
			// No password lock
			showOrLoadApplications();
		}
		appNumTextView = (TextView) findViewById(R.id.appNumTextView);
		if (null != FirewallApi.applications) {
			appNumTextView.setText(FirewallApi.applications.length + "");
		} else {
			appNumTextView.setText("0");
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		initList();
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		FirewallApi.applications = null;
	}

	Handler permissionHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Boolean hasPermission = (Boolean) msg.obj;
			if (!hasPermission) {
				Toast.makeText(FirewallActivity.this, getResources().getString(R.string.firewall_has_no_root), Toast.LENGTH_SHORT).show();
			}
		}
		
	};
	
	class GetPermission extends Thread {

		@Override
		public void run() {
			boolean hasPermission = hasRootPermission();
			Message msg = permissionHandler.obtainMessage();
			msg.obj = hasPermission;
			msg.what = 0;
			permissionHandler.sendMessage(msg);
		}
		
	}
	@Override
	protected void onPause() {
		super.onPause();
		this.listview.setAdapter(null);
	}

	private OnClickListener menuListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			isDealingData = true;
			initLoadingDialog(true);
			
			switch (v.getId()) {
			case R.id.disableWifiLayout:
//				changeFirewallStatus(NET_TYPE_WIFI, false);
				new DealingData(NET_TYPE_WIFI, false).start();
				break;
			case R.id.allowWifiLayout:
//				changeFirewallStatus(NET_TYPE_WIFI, true);
				new DealingData(NET_TYPE_WIFI, true).start();
				break;
			case R.id.disable3GLayout:
//				changeFirewallStatus(NET_TYPE_3G, false);
				new DealingData(NET_TYPE_3G, false).start();
				break;
			case R.id.allow3GLayout:
//				changeFirewallStatus(NET_TYPE_3G, true);
				new DealingData(NET_TYPE_3G, true).start();
				break;
			}
			menuWindow.dismiss();
			menuShowed = false;
			
		}
	};
	
	Handler dealingHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			isDealingData = false;
			Boolean[] result = (Boolean[]) msg.obj;
			if (result[0]) {
//				sortApp(adapter.getItems());
				adapter.notifyDataSetChanged();
				FirewallApi.saveRules(FirewallActivity.this, adapter);
			} else {
				changeFirewallStatus(msg.arg1, !result[1]);
				Toast.makeText(FirewallActivity.this, getResources().getString(R.string.firewall_not_surpport), Toast.LENGTH_SHORT).show();
			}
			mProgressDialog.dismiss();
		}
		
	};
	
	class DealingData extends Thread {
		int netType;
		boolean disableOrNot;
		public DealingData(int net, boolean disOrNot) {
			this.netType = net;
			this.disableOrNot = disOrNot;
		}
		@Override
		public void run() {
			changeFirewallStatus(netType, disableOrNot);
			boolean success = FirewallApi.applingIptablesRules(FirewallActivity.this, adapter, false);
			Message msg = dealingHandler.obtainMessage();
			Boolean[] blean_obj = new Boolean[] {success, disableOrNot};
			msg.obj = blean_obj;
			msg.arg1 = netType;
			msg.what = 0;
			dealingHandler.sendMessage(msg);
		}		
	}
	
	public void changeFirewallStatus(int netType, boolean disableOrNot) {
		if (netType == NET_TYPE_WIFI) {
			/*
			if (null != apps && apps.length > 0) {
				for (int i = 0; i < apps.length; i++) {
					FirewallApp app = apps[i];
					app.selected_wifi = disableOrNot;
				}
			}
			*/
			if(adapter != null && adapter.getCount() > 0){
				for(int i = 0; i < adapter.getCount(); i++){
					FirewallApp app = (FirewallApp)adapter.getItem(i);
					app.selected_wifi = disableOrNot;
				}
			}
		} 
		
		if (netType == NET_TYPE_3G) {
			/*
			if (null != apps && apps.length > 0) {
				for (int i = 0; i < apps.length; i++) {
					FirewallApp app = apps[i];
					app.selected_3g = disableOrNot;
				}
			}
			*/
			if(adapter != null && adapter.getCount() > 0){
				for(int i = 0; i < adapter.getCount(); i++){
					FirewallApp app = (FirewallApp)adapter.getItem(i);
					app.selected_3g = disableOrNot;
				}
			}
		}
	}
	
	private class LoadingThread extends Thread {
		public void run() {
			do {
				for (int j = 0; j < 5; j++) {
					if (!isDealingData) {
						break;
					}
					try {
						sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					loadingHandler.sendEmptyMessage(j);
				}
			} while (isDealingData);

		}
	}

	private Handler loadingHandler = new Handler() {

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

			}
		}
	};
	
	/**
	 * Check if the stored preferences are OK
	 */
	private void checkPreferences() {
		final SharedPreferences prefs = getSharedPreferences(
				FirewallApi.PREFS_NAME, 0);
		final Editor editor = prefs.edit();
		boolean changed = false;
		if (prefs.getString(FirewallApi.PREF_MODE, "").length() == 0) {
			editor.putString(FirewallApi.PREF_MODE, FirewallApi.MODE_WHITELIST);
			changed = true;
		}
		/* delete the old preference names */
		if (prefs.contains("AllowedUids")) {
			editor.remove("AllowedUids");
			changed = true;
		}
		if (prefs.contains("Interfaces")) {
			editor.remove("Interfaces");
			changed = true;
		}
		if (changed)
			editor.commit();
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		return super.onMenuItemSelected(featureId, item);
	}
	
	public boolean isNetfilterSupported() {
		if ((new File("/proc/config.gz")).exists() == false) {
			if ((new File("/proc/net/netfilter")).exists() == false)
				return false;
			if ((new File("/proc/net/ip_tables_targets")).exists() == false)
				return false;
		} else {
			if (!Configuration.hasKernelFeature("CONFIG_NETFILTER=")
					|| !Configuration
							.hasKernelFeature("CONFIG_IP_NF_IPTABLES=")
					|| !Configuration.hasKernelFeature("CONFIG_NF_NAT"))
				return false;
		}
		return true;
	}

	/**
	 * If the applications are cached, just show them, otherwise load and show
	 */
	private void showOrLoadApplications() {
		if (isLoadingData)
			return;
		if (FirewallApi.applications == null) {
			// The applications are not cached.. so lets display the progress
			// dialog
			final Dialog progressdlg = new Dialog(this, R.style.softDialog);
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
			progressdlg.setContentView(view);
			TextView prompt_dialog_text = (TextView) progressdlg
					.findViewById(R.id.prompt_dialog_text);
			prompt_dialog_text.setText(R.string.reading);
			TextView prompt_progress_text = (TextView) progressdlg
					.findViewById(R.id.prompt_progress_text);
			prompt_progress_text.setText("0%");
			prompt_progress_text.setVisibility(View.VISIBLE);
			isLoadingData = true;
			progressdlg.show();
			new LoadingItem().start();

			final Handler handler = new Handler() {
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case 0:
						showApplications();
						isLoadingData = false;
						if (progressdlg != null) {
							progressdlg.dismiss();
						}
						if (FirewallApi.applications != null) {
							
							appNumTextView.setText(FirewallApi.applications.length + "");
						} else {
							appNumTextView.setText("0");
						}
						break;
					case 5:
						TextView prompt_progress_text = (TextView) progressdlg
								.findViewById(R.id.prompt_progress_text);
						prompt_progress_text.setText(msg.arg1 + "%");

						break;
					}

				}
			};
			new Thread() {
				public void run() {
					FirewallApi.getApps(FirewallActivity.this, handler);
					/*
					 * if(KindroidSecurityApplication.mFirstOfReinstall){
					 * FirewallApi.purgeIptables(FirewallActivity.this, false);
					 * }
					 */
					handler.sendEmptyMessage(0);
				}
			}.start();
		} else {
			// the applications are cached, just show the list
			showApplications();
		}
	}
	
	public static void sortApp(FirewallApp[] apps) {
		Arrays.sort(apps, new Comparator<FirewallApp>() {
			@Override
			public int compare(FirewallApp o1, FirewallApp o2) {
			/*	if ((o1.selected_wifi | o1.selected_3g) == (o2.selected_wifi | o2.selected_3g)) {
					return o1.names[0].compareTo(o2.names[0]);
				}*/
				if ((o1.selected_wifi || o1.selected_3g) && !o2.selected_3g && !o2.selected_wifi) {
					return 1;
				}else if(!o1.selected_wifi && !o1.selected_3g && !o2.selected_3g && !o2.selected_wifi){
					return o1.names[0].compareTo(o2.names[0]);
				}else if(!o1.selected_wifi && !o1.selected_3g && (o2.selected_3g || o2.selected_wifi)){
					return -1;
				}else if(o1.selected_wifi && o1.selected_3g && o2.selected_3g && o2.selected_wifi){
					return o1.names[0].compareTo(o2.names[0]);
				}
				
				/*if (o1.selected_wifi || o1.selected_3g)
					return -1;*/
			
				return -1;
			}
		});
	}
	
	/**
	 * Show the list of applications
	 */
	private void showApplications() {

		apps = FirewallApi.getApps(this);
		// Sort applications - selected first, then alphabetically
		Arrays.sort(apps, new Comparator<FirewallApp>() {
			@Override
			public int compare(FirewallApp o1, FirewallApp o2) {
			/*	if ((o1.selected_wifi | o1.selected_3g) == (o2.selected_wifi | o2.selected_3g)) {
					return o1.names[0].compareTo(o2.names[0]);
				}*/
				if ((o1.selected_wifi || o1.selected_3g) && !o2.selected_3g && !o2.selected_wifi) {
					return 1;
				}
				if (o1.selected_wifi && o1.selected_3g) {
					return 1;
				}
				/*if (o1.selected_wifi || o1.selected_3g)
					return -1;*/
			
				return -1;
			}
		});
		//compute day and month data
		Cursor cursor = AppNetWorkDataBase.get(this).getAppDayAndMonthData(Calendar.getInstance());
		initNetTrafficData(cursor, apps);
		if(adapter == null){
			adapter = new AppItemAdapter(FirewallActivity.this, apps);
		}
		this.listview.setAdapter(adapter);
		if (cursor != null) {
			cursor.close();
			AppNetWorkDataBase.close();
		}
		
		if (apps != null) {
			
			appNumTextView.setText(apps.length + "");
		} else {
			appNumTextView.setText("0");
		}
	}
	
	void initNetTrafficData(Cursor c, FirewallApp[] apps) {
		PackageManager mPm = getPackageManager();
		if (c == null || c.getCount() == 0)
			return;
		while (c.moveToNext()) {
			String pkg = c.getString(c.getColumnIndex("pkg"));
			try {
				mPm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES);
			} catch (Exception e) {
				continue;
			}
			
			for (int i = 0; i < apps.length; i++) {
				FirewallApp app = apps[i];
				if (pkg.equals(app.packgeName)) {
					app.total_day = c.getLong(c.getColumnIndex("total_day"));
					app.total_month = c.getLong(c.getColumnIndex("total_month"));
				}
			}
		}
	
		if (c != null) {
			c.close();
			AppNetWorkDataBase.close();
		}
	}

	/**
	 * Enables or disables the firewall
	 */
	private void disableOrEnable() {
		final boolean enabled = !FirewallApi.isEnabled(this);
		FirewallApi.setEnabled(this, enabled);
		if (enabled) {
			FirewallApi.saveRules(FirewallActivity.this, adapter);
			applyOrSaveRules();
			setTitle(R.string.title_enabled);
		} else {
			purgeRules();
			setTitle(R.string.title_disabled);
		}
	}

	/**
	 * Apply or save iptable rules, showing a visual indication
	 */
	private void applyOrSaveRules() {
		if (isLoadingData)
			return;
		final Handler handler;
		final boolean enabled = FirewallApi.isEnabled(this);

		final Dialog progressdlg = new Dialog(this, R.style.softDialog);

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
		progressdlg.setContentView(view);
		TextView tv = (TextView) progressdlg
				.findViewById(R.id.prompt_dialog_text);

		if (enabled) {
			tv.setText(getResources().getString(R.string.applying_rule));
		} else {
			tv.setText(getResources().getString(R.string.saving_rule));
		}
		isLoadingData = true;
		progressdlg.show();
		new LoadingItem().start();

		handler = new Handler() {
			public void handleMessage(Message msg) {
				isLoadingData = false;
				if (progressdlg != null)
					progressdlg.dismiss();

				switch (msg.what) {
				case 0:
					if (enabled) {
						Toast.makeText(
								FirewallActivity.this,
								getResources().getString(
										R.string.apply_rule_success),
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(
								FirewallActivity.this,
								getResources().getString(
										R.string.save_rule_success),
								Toast.LENGTH_SHORT).show();
					}
					break;
				case 1:
					if (enabled) {
						FirewallApi.setEnabled(FirewallActivity.this, false);
						setFirewallStatusText();
						Toast.makeText(
								FirewallActivity.this,
								getResources().getString(
										R.string.apply_rule_failed),
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(
								FirewallActivity.this,
								getResources().getString(
										R.string.save_rule_failed),
								Toast.LENGTH_SHORT).show();
					}
					break;
				case 2:
					Toast.makeText(
							FirewallActivity.this,
							getResources().getString(
									R.string.save_rule_success),
							Toast.LENGTH_SHORT).show();
					break;
				}

				showOrLoadApplications();
			}
		};

		new Thread() {
			public void run() {
				if (!FirewallApi.hasRootAccess(FirewallActivity.this, false)) {
					handler.sendEmptyMessage(1);
					return;
				}

				if (enabled) {
					if (FirewallApi.applyIptablesRules(FirewallActivity.this,adapter,
							false)) {
						handler.sendEmptyMessage(0);
						return;
					} else {
						handler.sendEmptyMessage(1);
						return;
					}
				} else {
					FirewallApi.saveRules(FirewallActivity.this, adapter);
					handler.sendEmptyMessage(2);
					return;
				}
			}
		}.start();
	}

	/**
	 * Purge iptable rules, showing a visual indication
	 */
	private void purgeRules() {
		if (isLoadingData)
			return;
		final Handler handler;

		if (progress == null) {
			progress = new Dialog(this, R.style.softDialog);
		}
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
		progress.setContentView(view);
		TextView tv = (TextView) progress.findViewById(R.id.prompt_dialog_text);

		tv.setText(getResources().getString(R.string.deleting_rule));

		isLoadingData = true;
		progress.show();
		new LoadingItem().start();

		handler = new Handler() {
			public void handleMessage(Message msg) {
				isLoadingData = false;
				if (progress != null)
					progress.dismiss();
				switch (msg.what) {
				case 0:
					Toast.makeText(
							FirewallActivity.this,
							getResources().getString(
									R.string.delete_rule_success),
							Toast.LENGTH_SHORT).show();
					break;
				case 1:
					FirewallApi.setEnabled(FirewallActivity.this, true);
					setFirewallStatusText();
					Toast.makeText(
							FirewallActivity.this,
							getResources().getString(
									R.string.delete_rule_failed),
							Toast.LENGTH_SHORT).show();
					break;
				}
				showOrLoadApplications();
			}
		};
		new Thread() {
			public void run() {
				if (!FirewallApi.hasRootAccess(FirewallActivity.this, false)) {
					handler.sendEmptyMessage(1);
					return;
				}
				if (FirewallApi.purgeIptables(FirewallActivity.this, false)) {
					handler.sendEmptyMessage(0);
					return;
				} else {
					handler.sendEmptyMessage(1);
					return;
				}

			}
		}.start();

	}

	private class LoadingItem extends Thread {

		public void run() {
			mProgressHandler.sendEmptyMessage(6);
			do {
				for (int j = 0; j < 5; j++) {
					try {
						sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mProgressHandler.sendEmptyMessage(j);
				}
			} while (isLoadingData);
			mProgressHandler.sendEmptyMessage(7);
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

//			case 6:
//				View switchText_linear = findViewById(R.id.switchText_linear);
//				if (switchText_linear != null) {
//					switchText_linear.setEnabled(false);
//				}
//				if (save_rule_linear != null) {
//					save_rule_linear.setEnabled(false);
//				}
//				break;
//
//			case 7:
//				switchText_linear = findViewById(R.id.switchText_linear);
//				if (switchText_linear != null) {
//					switchText_linear.setEnabled(true);
//				}
//				if (save_rule_linear != null) {
//					save_rule_linear.setEnabled(true);
//				}
//				break;

			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		/*case R.id.switchText_linear:

			if (!isNetfilterSupported() || !hasRootPermission()) {
				firewall_main_content.setVisibility(View.GONE);
				firewall_prompt_content.setVisibility(View.VISIBLE);
				TextView prompt_text = (TextView) findViewById(R.id.prompt_text);
				if (!isNetfilterSupported()) {
					prompt_text.setText(R.string.firewall_not_support);
				} else if (!hasRootPermission()) {
					prompt_text.setText(R.string.firewall_noroot);
				}
				back_to_main_linear.setVisibility(View.VISIBLE);
				break;
			}

			switchText_linear.setBackgroundResource(R.drawable.linear_focuse);
			save_rule_linear.setBackgroundResource(R.drawable.linear_unfocuse);

			disableOrEnable();
			boolean enabled = FirewallApi.isEnabled(this);
			
			if (enabled) {
				mSwitchFirewall.setText(R.string.app_firewall_disable);
			} else {
				mSwitchFirewall.setText(R.string.app_firewall_enable);
			}
			
			setFirewallStatusText();
			break;
		case R.id.save_rule_linear:

			if (!isNetfilterSupported() || !hasRootPermission()) {
				firewall_main_content.setVisibility(View.GONE);
				firewall_prompt_content.setVisibility(View.VISIBLE);
				back_to_main_linear.setVisibility(View.VISIBLE);
				TextView prompt_text = (TextView) findViewById(R.id.prompt_text);
				if (!isNetfilterSupported()) {
					prompt_text.setText(R.string.firewall_not_support);
				} else if (!hasRootPermission()) {
					prompt_text.setText(R.string.firewall_noroot);
				}
				break;
			}

			switchText_linear.setBackgroundResource(R.drawable.linear_unfocuse);
			save_rule_linear.setBackgroundResource(R.drawable.linear_focuse);

			applyOrSaveRules();

			break;*/
		case R.id.back_to_main_linear:
			finish();
		}
	}

	public void setFirewallStatusText() {
		boolean enabled = FirewallApi.isEnabled(this);
		/*if (enabled) {
			mFirewallStatus.setText(R.string.app_firewall_status_enable);
			mSwitchFirewall.setText(R.string.app_firewall_disable);
		} else {
			mFirewallStatus.setText(R.string.app_firewall_status_diable);
			mSwitchFirewall.setText(R.string.app_firewall_enable);
		}*/

	}

}
