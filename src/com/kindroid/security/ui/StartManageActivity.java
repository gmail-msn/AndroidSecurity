/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-09
 * Description:
 */
package com.kindroid.security.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.kindroid.security.R;
import com.kindroid.security.adapter.StartManageListAdapter;
import com.kindroid.security.data.AutoStartOnBootItem;
import com.kindroid.security.model.AutoStartAppItem;
import com.kindroid.security.service.OnBootReceiver;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.RunCMDUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author heli.zhao
 * 
 */
public class StartManageActivity extends ListActivity implements
		View.OnClickListener {
	private StartManageListAdapter mEnableAdapter;
	private View mAccelActionLinear;
	private View mSummaryLinear;
	private TextView mEnableSumText;
	private TextView mStartTimeText;
	
	private TextView mProgressText;

	private Map<String, PackageStats> pkgSizes;

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

	private static final int FINISH_LOADING = 0;
	private static final int UPDATE_ENABLE_ADAPTER = 1;
	private static final int UPDATE_DISABLE_ADAPTER = 2;
	private static final int START_MANAGE_FAIL = 3;
	private static final int UPDATE_ACCEL = 4;
	private static final int FINISH_ACCEL = 5;
	private static final int NO_ROOTED = 6;
	private static final int UPDATE_PROGRESS = 7;

	private boolean isLoadingData = false;
	private boolean isEnableTab = true;

	public static final String PROMPT_EVERY_TIME = "prompt_every_time";

	private Dialog mProgressDialog;

	private static final String BOOT_START_PERMISSION = "android.permission.RECEIVE_BOOT_COMPLETED";
	private static final String ANDROID_RESOURCES = "http://schemas.android.com/apk/res/android";

	public static AutoStartOnBootItem sOptimizeItem;
	private boolean mForOptimize = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startmanage_list);
		mForOptimize = getIntent().getBooleanExtra(MobileExamActivity.MOBILE_EXAM_OPTIMIZE_INTENT, false);
		
		View home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!mForOptimize){
					Intent homepage = new Intent(StartManageActivity.this,
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
		
		mAccelActionLinear = findViewById(R.id.accel_action_linear);
		mSummaryLinear = findViewById(R.id.summary_linear);
		mEnableSumText = (TextView) findViewById(R.id.auto_start_sum);
		mStartTimeText = (TextView) findViewById(R.id.start_time);		
		mProgressText = (TextView)findViewById(R.id.prompt_progress_text);		
		mAccelActionLinear.setOnClickListener(this);

		SharedPreferences sp = KindroidSecurityApplication.sh;
		long startElapsedTime = sp.getLong(OnBootReceiver.START_TIME, 0);

		if (startElapsedTime > 0) {
			double startTime = startElapsedTime * 1.0 / 1000;
			DecimalFormat df = new DecimalFormat("###.00");
			mStartTimeText
					.setText(Html.fromHtml(String.format(
							getString(R.string.start_time_text),
							df.format(startTime))));
		} else {
			mStartTimeText.setText(R.string.start_manage_no_start_time);
		}

		mEnableAdapter = new StartManageListAdapter(this);
		loadAdapter();
	}

	private void loadAdapter() {
		isLoadingData = true;
		mAccelActionLinear.setClickable(false);
		
		mSummaryLinear.setVisibility(View.GONE);
		getListView().setVisibility(View.GONE);
		View loading_linear = findViewById(R.id.loading_linear);
		loading_linear.setVisibility(View.VISIBLE);

		one = (ImageView) findViewById(R.id.pr_one);
		two = (ImageView) findViewById(R.id.pr_two);
		three = (ImageView) findViewById(R.id.pr_three);
		four = (ImageView) findViewById(R.id.pr_four);
		five = (ImageView) findViewById(R.id.pr_five);

		one_copy = (ImageView) findViewById(R.id.pr_one_copy);
		two_copy = (ImageView) findViewById(R.id.pr_two_copy);
		three_copy = (ImageView) findViewById(R.id.pr_three_copy);
		four_copy = (ImageView) findViewById(R.id.pr_four_copy);
		five_copy = (ImageView) findViewById(R.id.pr_five_copy);

		TextView tv = (TextView) findViewById(R.id.prompt_dialog_text);
		tv.setText(R.string.start_manage_loading_text);

		new LoadingItem().start();
		new LoadAdapterThread().start();

	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case FINISH_LOADING:
				updateSummary(0);
				mEnableAdapter.sortItems(false);
				setListAdapter(mEnableAdapter);
				mEnableAdapter.notifyDataSetChanged();
				getListView().setVisibility(View.VISIBLE);
				findViewById(R.id.loading_linear).setVisibility(View.GONE);
				mSummaryLinear.setVisibility(View.VISIBLE);
				isLoadingData = false;
				getListView().setClickable(true);
				mAccelActionLinear.setClickable(true);
				
				break;
			case UPDATE_ENABLE_ADAPTER:
				mEnableAdapter.sortItems(false);
				mEnableAdapter.notifyDataSetChanged();
				updateSummary(0);
				isLoadingData = false;
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}
				getListView().setClickable(true);
				mAccelActionLinear.setClickable(true);
				break;
			case UPDATE_DISABLE_ADAPTER:
				mEnableAdapter.sortItems(false);
				mEnableAdapter.notifyDataSetChanged();
				updateSummary(0);
				isLoadingData = false;
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}
				getListView().setClickable(true);
				mAccelActionLinear.setClickable(true);
				break;
			case START_MANAGE_FAIL:
				Toast.makeText(StartManageActivity.this, R.string.start_manage_fail_prompt,
						Toast.LENGTH_LONG).show();
				isLoadingData = false;
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}				
				getListView().setClickable(true);
				mAccelActionLinear.setClickable(true);
				break;
			case UPDATE_ACCEL:
				mEnableAdapter.sortItems(false);
				mEnableAdapter.notifyDataSetChanged();
				updateSummary(0);
				
				getListView().setClickable(true);
				mAccelActionLinear.setClickable(true);
				break;
			case FINISH_ACCEL:
				isLoadingData = false;
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}
				if(msg.arg1 == 0){
					Toast.makeText(StartManageActivity.this, R.string.start_manage_no_enabled,
							Toast.LENGTH_LONG).show();
				}
				getListView().setClickable(true);
				mAccelActionLinear.setClickable(true);
				break;
			case NO_ROOTED:
				Toast.makeText(StartManageActivity.this, R.string.start_manage_norooted_prompt, Toast.LENGTH_LONG).show();								
				isLoadingData = false;
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}
				getListView().setClickable(true);
				mAccelActionLinear.setClickable(true);
				break;
			case UPDATE_PROGRESS:
				mProgressText.setText(msg.arg1 + "%");
				break;
			
			}
		}
	};

	private void updateSummary(int type) {
		switch (type) {
		case 0:
			int mEnableSum = 0;
			for (int i = 0; i < mEnableAdapter.getCount(); i++) {
				AutoStartAppItem item = (AutoStartAppItem) mEnableAdapter.getItem(i);
				if(item.isEnable()){
					mEnableSum++;
				}
			}
			mEnableSumText.setText(Html.fromHtml(String.format(
					getString(R.string.allow_autostart_sum), mEnableSum)));
			break;
		
		}
	}

	private class LoadAdapterThread extends Thread {
		public void run() {
//			RunCMDUtils.isRooted(StartManageActivity.this);
			PackageManager pm = getPackageManager();
			List<PackageInfo> packs = pm.getInstalledPackages(0);
			pkgSizes = new HashMap<String, PackageStats>();
			int num = 0;
			for (PackageInfo packageInfo : packs) {
				if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
						|| (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					continue;
				}
				if (packageInfo.packageName.equals("com.kindroid.security")) {
					continue;
				}
				AssetManager am = null;
				XmlResourceParser xmlParser = null;
				try {
					am = createPackageContext(packageInfo.packageName, 0)
							.getAssets();
					xmlParser = am.openXmlResourceParser("AndroidManifest.xml");

				} catch (Exception e) {
					e.printStackTrace();
				}
				if (am == null || xmlParser == null) {
					continue;
				}
				try {
					int eventType = xmlParser.getEventType();
					AutoStartAppItem item = new AutoStartAppItem();
					while (eventType != XmlPullParser.END_DOCUMENT) {
						switch (eventType) {
						case XmlPullParser.START_TAG:
							if (!xmlParser.getName().matches("receiver")) {
								eventType = xmlParser.nextToken();
								continue;
							} else {
								String receivername = xmlParser
										.getAttributeValue(ANDROID_RESOURCES,
												"name");

								int outerDepthReceiver = xmlParser.getDepth();

								while ((eventType = xmlParser.next()) != XmlPullParser.END_DOCUMENT
										&& (eventType != XmlPullParser.END_TAG || xmlParser
												.getDepth() > outerDepthReceiver)) {

									if (eventType == XmlPullParser.END_TAG
											|| eventType == XmlPullParser.TEXT) {
										continue;
									}

									if (xmlParser.getName().equals(
											"intent-filter")) {
										int outerDepthIntent = xmlParser
												.getDepth();

										while ((eventType = xmlParser.next()) != XmlPullParser.END_DOCUMENT
												&& (eventType != XmlPullParser.END_TAG || xmlParser
														.getDepth() > outerDepthIntent)) {

											if (eventType == XmlPullParser.END_TAG
													|| eventType == XmlPullParser.TEXT) {
												continue;
											}

											String nodeName = xmlParser
													.getName();
											if (nodeName.equals("action")) {
												String valueAction = xmlParser
														.getAttributeValue(
																ANDROID_RESOURCES,
																"name");

												if (valueAction
														.contains("BOOT_COMPLETED")) {
													getPkgInfo(packageInfo.packageName);
													try {
														PackageInfo pi = pm
																.getPackageInfo(
																		packageInfo.packageName,
																		PackageManager.GET_RECEIVERS);
														ActivityInfo[] receiverInfos = pi.receivers;
														if (receiverInfos != null) {
															for (ActivityInfo receiverInfo : receiverInfos) {
																if (receiverInfo.name
																		.contains(receivername)) {
																	receivername = receiverInfo.name;
																	item.addComponent(receivername);
																	break;
																}
															}

														}
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
											}
										}
									}
								}
							}
							break;
						}
						eventType = xmlParser.nextToken();
					}
					if (item.getComponents() != null
							&& item.getComponents().size() > 0) {
						try {
							item.setPackageName(packageInfo.packageName);
							item.setAppIcon(packageInfo.applicationInfo
									.loadIcon(pm));
							item.setAppLabel(packageInfo.applicationInfo
									.loadLabel(pm).toString());
							item.setVersion(packageInfo.versionName
									.concat(getString(R.string.softmanage_version_title)));

							long apkSize = new File(
									packageInfo.applicationInfo.sourceDir)
									.length();
							if (pkgSizes.get(packageInfo.packageName) != null) {
								PackageStats ps = pkgSizes
										.get(packageInfo.packageName);
								item.setSize(ps.cacheSize + ps.codeSize
										+ ps.dataSize);
							} else {
								item.setSize(apkSize);
							}
							boolean mEnabled = false;
							for (int i = 0; i < item.getComponents().size(); i++) {
								ComponentName componentName = new ComponentName(
										packageInfo.packageName, item
												.getComponents().get(i));
								int enableSetting = pm
										.getComponentEnabledSetting(componentName);
								if (enableSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
									mEnabled = true;
								}
							}
							item.setEnable(mEnabled);
							mEnableAdapter.addItem(item);
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (IOException ioe) {

				} catch (XmlPullParserException xppe) {

				}
				num++;
				Message msg = new Message();
				msg.what = UPDATE_PROGRESS;
				msg.arg1 = Double.valueOf((num * 1.0) / packs.size() * 100).intValue();
				mHandler.sendMessage(msg);
			}
			/*
			ComponentName componentName = new ComponentName(
					"com.kindroid.security", "com.kindroid.security.service.OnBootReceiver");
			int enableSetting = pm.getComponentEnabledSetting(componentName);
			if(enableSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
				try {
					boolean ret = RunCMDUtils
							.rootCommand("pm enable "
									+ "com.kindroid.security/com.kindroid.security.service.OnBootReceiver");
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			*/
			mHandler.sendEmptyMessage(FINISH_LOADING);
		}
	}

	private class LoadingItem extends Thread {
		public void run() {
			do {
				for (int j = 0; j < 5; j++) {
					if (!isLoadingData) {
						break;
					}
					try {
						sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					mProgressHandler.sendEmptyMessage(j);
				}
			} while (isLoadingData);

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

			}
		}
	};

	public void getPkgInfo(String pkg) {
		PackageManager pm = getPackageManager();
		try {
			Method getPackageSizeInfo = pm.getClass().getMethod(
					"getPackageSizeInfo", String.class,
					IPackageStatsObserver.class);
			getPackageSizeInfo.invoke(pm, pkg, new PkgSizeObserver());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	class PkgSizeObserver extends IPackageStatsObserver.Stub {

		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			// TODO Auto-generated method stub
			pkgSizes.put(pStats.packageName, pStats);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {		
		case R.id.accel_action_linear:
			if (mEnableAdapter.getCount() == 0) {
				Toast.makeText(this, R.string.start_manage_no_enabled,
						Toast.LENGTH_LONG).show();
				break;
			}
			getListView().setClickable(false);
			startAccel(false);
			new AccelThread().start();

			break;
		}
	}

	private void startAccel(boolean enable) {
		isLoadingData = true;
		mAccelActionLinear.setClickable(false);
		getListView().setClickable(false);
		mProgressDialog = new Dialog(this, R.style.softDialog){

			@Override
			public void onBackPressed() {
				// TODO Auto-generated method stub
				
			}
			
		};
		View view = LayoutInflater.from(this).inflate(
				R.layout.startmanage_prompt_dialog, null);
		mProgressDialog.setContentView(view);
		if (enable) {
			TextView tv = (TextView) mProgressDialog
					.findViewById(R.id.prompt_dialog_text);
			tv.setText(R.string.start_manage_enabling_text);
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
		new LoadingItem().start();
	}

	private class AccelThread extends Thread {
		public void run() {
			if (!RunCMDUtils.isRooted(StartManageActivity.this)) {
				mHandler.sendEmptyMessage(NO_ROOTED);
				return;
			}
			List<AutoStartAppItem> mList = new ArrayList<AutoStartAppItem>(
					mEnableAdapter.getItems());
			boolean hasEnabled = false;
			if(mForOptimize && sOptimizeItem != null){
				sOptimizeItem.clearOptimizeItems();
			}
			for (int i = 0; i < mList.size(); i++) {
				AutoStartAppItem item = mList.get(i);
				if(!item.isEnable()){
					continue;
				}
				hasEnabled = true;
				if (item.getComponents() != null
						&& item.getComponents().size() > 0) {
					boolean mDisabled = true;
					for (String compomentName : item.getComponents()) {
						try {
							boolean ret = RunCMDUtils.rootCommand("pm disable "
									+ item.getPackageName() + "/"
									+ compomentName);
							if (!ret) {
								mDisabled = false;
							}
						} catch (Exception e) {
							e.printStackTrace();
							mDisabled = false;
						}
					}
					if (mDisabled) {
						item.setEnable(false);						
						mHandler.sendEmptyMessage(UPDATE_ACCEL);
						
					}else{
						if(mForOptimize && sOptimizeItem != null){
							sOptimizeItem.addOptimizeItem(item);
						}
					}
				}
			}
			Message msg = new Message();
			if(hasEnabled){
				msg.arg1 = 1;
			}else{
				msg.arg1 = 0;
			}
			msg.what = FINISH_ACCEL;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		getListView().setClickable(false);
		super.onListItemClick(l, v, position, id);
		if(isLoadingData){
			return;
		}
		final AutoStartAppItem item = (AutoStartAppItem) mEnableAdapter.getItem(position);
		if (item != null && item.getComponents() != null && item.getComponents().size() > 0) {
			if(item.isEnable()){
				startAccel(false);
				new Thread(){
					public void run(){
						if (!RunCMDUtils.isRooted(StartManageActivity.this)) {
							mHandler.sendEmptyMessage(NO_ROOTED);
							return;
						}
						boolean mDisabled = true;
						for (String compomentName : item.getComponents()) {
							try {
								boolean ret = RunCMDUtils.rootCommand("pm disable "
										+ item.getPackageName() + "/" + compomentName);
								if (!ret) {
									mDisabled = false;
								}
							} catch (Exception e) {
								e.printStackTrace();
								mDisabled = false;
							}
						}
						if (mDisabled) {
							item.setEnable(false);
							if(mForOptimize && sOptimizeItem != null){
								for(int i = 0; i < sOptimizeItem.getOptimizeItemSum(); i++){
									AutoStartAppItem mItem = sOptimizeItem.getOptimizeItem(i);
									if(item.getPackageName().equals(mItem.getPackageName())){
										sOptimizeItem.delOptimizeItem(mItem);
										break;
									}
								}
							}
							mHandler.sendEmptyMessage(UPDATE_ENABLE_ADAPTER);
						} else {
							mHandler.sendEmptyMessage(START_MANAGE_FAIL);
						}
					}
				}.start();
			}else{
				final Dialog promptDialog = new Dialog(this, R.style.softDialog);
				View view = LayoutInflater.from(this).inflate(
						R.layout.start_manage_prompt_dialog, null);
				promptDialog.setContentView(view);

				TextView promptText = (TextView) promptDialog
						.findViewById(R.id.prompt_text);
				promptText.setText(R.string.start_manage_enable_prompt);

				Button button_ok = (Button) promptDialog
						.findViewById(R.id.button_ok);

				View button_cancel = promptDialog.findViewById(R.id.button_cancel);
				button_ok.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						promptDialog.dismiss();
						getListView().setClickable(false);
						if(item.getComponents() != null && item.getComponents().size() > 0){						
							startAccel(true);
							new Thread() {
								public void run() {
									if(!RunCMDUtils.isRooted(StartManageActivity.this)){
										mHandler.sendEmptyMessage(NO_ROOTED);
										return;
									}
									boolean mEnabled = true;
									for (String compomentName : item.getComponents()) {
										try {
											boolean ret = RunCMDUtils
													.rootCommand("pm enable "
															+ item.getPackageName()
															+ "/" + compomentName);
											if (!ret) {
												mEnabled = false;
											}
										} catch (Exception e) {
											e.printStackTrace();
											mEnabled = false;
										}
									}
									if (mEnabled) {	
										item.setEnable(true);
										
										if(mForOptimize && sOptimizeItem != null){
											boolean mExist = false;
											for(int i = 0; i < sOptimizeItem.getOptimizeItemSum(); i++){
												AutoStartAppItem mItem = sOptimizeItem.getOptimizeItem(i);
												if(item.getPackageName().equals(mItem.getPackageName())){
													mExist = true;
													break;
												}
											}
											if(!mExist){
												sOptimizeItem.addOptimizeItem(item);
											}
										}
										mHandler.sendEmptyMessage(UPDATE_DISABLE_ADAPTER);
									}else {
										mHandler.sendEmptyMessage(START_MANAGE_FAIL);
									}

								}
							}.start();
						}					
					}
				});
				button_cancel.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						promptDialog.dismiss();
					}
				});
				promptDialog.show();
			}
		}
		
	}

}