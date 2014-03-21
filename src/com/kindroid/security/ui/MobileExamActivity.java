/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.kindroid.security.R;
import com.kindroid.security.adapter.MobileExamListAdapter;
import com.kindroid.security.data.AutoStartOnBootItem;
import com.kindroid.security.data.CacheClearItem;
import com.kindroid.security.data.EnableSecurityServiceItem;
import com.kindroid.security.data.MobileExamItem;
import com.kindroid.security.data.NetTrafficMonitorItem;
import com.kindroid.security.data.RemoteSecurityItem;
import com.kindroid.security.data.RunningTaskItem;
import com.kindroid.security.data.VirusScanItem;
import com.kindroid.security.data.VirusUpdateItem;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.model.AutoStartAppItem;
import com.kindroid.security.ui.CacheClearActivity.PkgSizeObserver;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.LoadingAppsThread;
import com.kindroid.security.util.MemoryUtil;
import com.kindroid.security.util.ProcInfo;
import com.kindroid.security.util.UnitsConversion;
import com.kindroid.security.util.UtilShareprefece;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author heli.zhao
 * 
 */
public class MobileExamActivity extends Activity implements
		View.OnClickListener {
	private MobileExamListAdapter mSafeAdatper;
	private MobileExamListAdapter mDangerAdatper;
	private MobileExamListAdapter mUnoptimizedAdatper;
	private MobileExamListAdapter mOptimizedAdatper;

	private TextView mFenshuTv;
	private RatingBar mRatingBar;
	private ProgressBar mProgressBar;
	private TextView mScanTitleTv;
	private TextView mScanAppTv;

	private View mSafeItemLinear;
	private View mDangerItemLinear;
	private View mUnoptimizedItemLinear;
	private View mOptimizedItemLinear;

	private TextView mSafeItemTitleTv;
	private TextView mDangerItemTitleTv;
	private TextView mUnoptimizedItemTitleTv;
	private TextView mOptimizedItemTitleTv;

	private ListView mSafeItemListView;
	private ListView mDangerItemListView;
	private ListView mUnoptimizedItemListView;
	private ListView mOptimziedItemListView;

	private View mCancelExamButton;
	private View mOptimizeButton;
	private View mScanningLinear;
	private View mScanResultLinear;
	private TextView mScanResultTv;

	public static final int UPDATE_SCAN_TITLE = 0;
	public static final int UPDATE_SCAN_APP = 1;
	public static final int UPDATE_SAFE_ADAPTER = 2;
	public static final int UPDATE_DANGER_ADAPTER = 3;
	public static final int UPDATE_UNOPTIMIZED_ADAPTER = 4;
	public static final int UPDATE_OPTIMIZED_ADAPTER = 5;
	public static final int COMPLETE_EXAM = 6;
	public static final int UPDATE_PROGRESS = 7;
	public static final int COMPLETE_OPTIMIZE = 8;
	public static final int COMPLETE_ONE_OPTIMZE = 9;

	private boolean mCancelExam = false;
	private int mRateMarks = 100;
	private int mBaseProgress = 0;

	// private static final int TRAFFIC_MONITOR_MARK = 5;
	// private static final int REMOTE_SECURITY_MARK = 5;
	// private static final int SECURITY_SERVICE_MARK = 5;
	// private static final int VIRUS_SCAN_MARK = 5;
	// private static final int RUNNING_TASK_MARK = 5;
	// private static final int CACHE_CLEAR_MARK = 5;
	// private static final int AUTO_START_MARK = 5;
	// private static final int VIRUS_UPDATE_MARK = 5;

	public static final long MONTH_PERIOD = 2592000000L;

	private PkgSizeObserver pkgObserver;
	private int mCacheSize = 0;
	private int installedAppsCount = 0;
	private int count = 0;

	private static final String ANDROID_RESOURCES = "http://schemas.android.com/apk/res/android";

	public static final String MOBILE_EXAM_LAST_TIME = "last_time_for_mobile_exam";
	public static final String MOBILE_EXAM_LAST_SCORE = "last_score_for_mobile_exam";
	public static final String MOBILE_OPTIMIZE_LAST_TIME = "last_optimize_for_mobile_exam";
	public static final String MOBILE_OPTIMIZE_LAST_SCORE = "last_score_for_mobile_optimize";

	public static final String MOBILE_EXAM_OPTIMIZE_INTENT = "intent_for_mobile_exam_optimize";

	private float mScreenDensity = 1;
	private int mListItemHeight = 35;
	public static final int RESULT_CODE_FOR_FINISH = 1001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mobile_examine);
		// screen density
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		mScreenDensity = metric.density; // screen density（0.75 / 1.0 / 1.5）

		View home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent homepage = new Intent(MobileExamActivity.this,
						DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
			}
		});

		mSafeAdatper = new MobileExamListAdapter(this);
		mDangerAdatper = new MobileExamListAdapter(this);
		mUnoptimizedAdatper = new MobileExamListAdapter(this);
		mOptimizedAdatper = new MobileExamListAdapter(this);
		findViews();
		startExamine();
	}

	private void findViews() {
		mFenshuTv = (TextView) findViewById(R.id.fenshu_tv);
		mRatingBar = (RatingBar) findViewById(R.id.healthRatingBar);
		mProgressBar = (ProgressBar) findViewById(R.id.mobile_exam_progress);
		mScanTitleTv = (TextView) findViewById(R.id.scan_title_tv);
		mScanAppTv = (TextView) findViewById(R.id.scan_app_tv);

		mSafeItemLinear = findViewById(R.id.safe_item_linear);
		mDangerItemLinear = findViewById(R.id.danger_item_linear);
		mUnoptimizedItemLinear = findViewById(R.id.unoptimized_item_linear);
		mOptimizedItemLinear = findViewById(R.id.optimized_item_linear);

		mSafeItemTitleTv = (TextView) findViewById(R.id.safe_item_title_tv);
		mDangerItemTitleTv = (TextView) findViewById(R.id.danger_item_title_tv);
		mUnoptimizedItemTitleTv = (TextView) findViewById(R.id.unoptimized_item_title_tv);
		mOptimizedItemTitleTv = (TextView) findViewById(R.id.optimized_item_title_tv);

		mSafeItemListView = (ListView) findViewById(R.id.safe_list_view);
		mDangerItemListView = (ListView) findViewById(R.id.danger_list_view);
		mUnoptimizedItemListView = (ListView) findViewById(R.id.unoptimized_list_view);
		mOptimziedItemListView = (ListView) findViewById(R.id.optimized_list_view);

		mScanResultTv = (TextView) findViewById(R.id.scan_result_tv);
		mScanningLinear = findViewById(R.id.scanning_linear);
		mScanResultLinear = findViewById(R.id.scan_result_linear);
		mCancelExamButton = findViewById(R.id.cancel_scan_button);
		mOptimizeButton = findViewById(R.id.optimize_button);
		mCancelExamButton.setOnClickListener(this);
		mOptimizeButton.setOnClickListener(this);
	}

	private void startExamine() {
		mRateMarks = 100;
		mScanningLinear.setVisibility(View.VISIBLE);
		mScanResultLinear.setVisibility(View.GONE);
		mFenshuTv.setText(mRateMarks + "");
		mRatingBar.setRating(5);
		new MobileExamineThread().start();
		mSafeItemListView.setAdapter(mSafeAdatper);
		mDangerItemListView.setAdapter(mDangerAdatper);
		mUnoptimizedItemListView.setAdapter(mUnoptimizedAdatper);
		mOptimziedItemListView.setAdapter(mOptimizedAdatper);
	}

	public synchronized void updateExamList(MobileExamItem mItem) {
		mRateMarks = mRateMarks + mItem.getIncScore();
		mUnoptimizedAdatper.delItem(mItem);
		mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
		mSafeAdatper.addItem(mItem);
		mHandler.sendEmptyMessage(UPDATE_SAFE_ADAPTER);
		mHandler.sendEmptyMessage(COMPLETE_ONE_OPTIMZE);
	}

	public void forwardTargetActivity(MobileExamItem mItem, Class clz) {
		int index = mUnoptimizedAdatper.getItemIndex(mItem);
		if (index != -1) {
			Intent intent = new Intent(this, clz);
			intent.putExtra(MOBILE_EXAM_OPTIMIZE_INTENT, true);
			startActivityForResult(intent, index);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CODE_FOR_FINISH) {
			finish();
		}
		MobileExamItem mItem = (MobileExamItem) mUnoptimizedAdatper
				.getItem(requestCode);
		if (mItem == null) {
			return;
		}
		switch (mItem.getFlag()) {
		case MobileExamItem.FLAG_AUTO_START_ITEM:
			if (resultCode == Activity.RESULT_OK) {

			}
			AutoStartOnBootItem aItem = (AutoStartOnBootItem) mItem;
			if (aItem.getOptimizeItemSum() == 0) {
				mItem.setItemStatus(MobileExamItem.SAFE_ITEM);
				updateExamList(mItem);
			}

			break;
		case MobileExamItem.FLAG_REMOTE_SECURITY_ITEM:
			boolean kindSecurityFuc = KindroidSecurityApplication.sh
					.getBoolean(Constant.SHAREDPREFERENCES_REMOTESECURITY,
							false);
			if (kindSecurityFuc) {
				mItem.setItemStatus(MobileExamItem.SAFE_ITEM);
				updateExamList(mItem);
			}
			break;
		case MobileExamItem.FLAG_VIRUS_SCAN_ITEM:
			SharedPreferences sp = KindroidSecurityApplication.sh;
			long last_scan_time = sp.getLong(UtilShareprefece.LAST_SCAN_TIME,
					0L);
			VirusScanItem vItem = (VirusScanItem) mItem;
			if (last_scan_time > vItem.getLastVirusScanTime()) {
				vItem.setLastVirusScanTime(last_scan_time);
				mItem.setItemStatus(MobileExamItem.SAFE_ITEM);
				mItem.setSafeAction(getString(R.string.mobile_exam_action_complete));
				updateExamList(mItem);
			}
			break;
		case MobileExamItem.FLAG_RUNNING_TASK_ITEM:
			RunningTaskItem rItem = (RunningTaskItem) mItem;
			if (rItem.getProcSum() == 0) {
				rItem.setItemStatus(MobileExamItem.SAFE_ITEM);
				updateExamList(mItem);
			} else {
				int useMem = 0;
				for (ProcInfo pi : rItem.getProcInfos()) {
					useMem = useMem + pi.getMemory(this);
				}
				rItem.setUseMemory(new UnitsConversion()
						.defaultConversion(useMem * 1024));
				mUnoptimizedAdatper.notifyDataSetChanged();
			}
			break;
		case MobileExamItem.VIRUS_UPDATE_ITEM:
			sp = KindroidSecurityApplication.sh;
			long last_update_time = sp.getLong(
					UtilShareprefece.LAST_UPDATE_TIME, 0L);
			VirusUpdateItem uItem = (VirusUpdateItem) mItem;
			long period = System.currentTimeMillis() - last_update_time;
			if (period < MONTH_PERIOD) {
				mItem.setItemStatus(MobileExamItem.OPTIMIZED_ITEM);
				mItem.setSafeAction(getString(R.string.mobile_exam_action_complete));
				updateExamList(mItem);
			}
			break;
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_SCAN_TITLE:
				mScanTitleTv.setText(msg.obj.toString());
				break;
			case UPDATE_SCAN_APP:
				mScanAppTv.setText(msg.obj.toString());
				break;
			case UPDATE_SAFE_ADAPTER:
				mSafeItemTitleTv.setText(String.format(
						getString(R.string.safe_item_text),
						mSafeAdatper.getCount()));
				mFenshuTv.setText(mRateMarks + "");
				mRatingBar.setRating((float) (mRateMarks * 1.0 / 110 * 5));
				if (mSafeItemLinear.getVisibility() != View.VISIBLE) {
					mSafeItemLinear.setVisibility(View.VISIBLE);
				}
				LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT, Double
								.valueOf(
										(mSafeAdatper.getCount())
												* mScreenDensity * 30)
								.intValue());
				mSafeItemListView.setLayoutParams(lp1);

				mSafeAdatper.notifyDataSetChanged();
				break;
			case UPDATE_DANGER_ADAPTER:
				mDangerAdatper.notifyDataSetChanged();
				mDangerItemTitleTv.setText(String.format(
						getString(R.string.danger_item_text),
						mDangerAdatper.getCount()));
				mFenshuTv.setText(mRateMarks + "");
				mRatingBar.setRating((float) (mRateMarks * 1.0 / 100 * 5));
				if (mDangerItemLinear.getVisibility() != View.VISIBLE) {
					mDangerItemLinear.setVisibility(View.VISIBLE);
				}

				lp1 = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT, Double.valueOf(
								(mDangerAdatper.getCount()) * mScreenDensity
										* 38).intValue());
				mDangerItemListView.setLayoutParams(lp1);

				break;
			case UPDATE_UNOPTIMIZED_ADAPTER:
				mUnoptimizedAdatper.notifyDataSetChanged();
				mUnoptimizedItemTitleTv.setText(String.format(
						getString(R.string.unoptimized_item_text),
						mUnoptimizedAdatper.getCount()));
				mFenshuTv.setText(mRateMarks + "");
				mRatingBar.setRating((float) (mRateMarks * 1.0 / 100 * 5));
				if (mUnoptimizedItemLinear.getVisibility() != View.VISIBLE) {
					mUnoptimizedItemLinear.setVisibility(View.VISIBLE);
				}
				lp1 = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT, Double.valueOf(
								(mUnoptimizedAdatper.getCount())
										* mScreenDensity * 38).intValue());
				mUnoptimizedItemListView.setLayoutParams(lp1);
				break;
			case UPDATE_OPTIMIZED_ADAPTER:
				mOptimizedAdatper.notifyDataSetChanged();
				mOptimizedItemTitleTv.setText(String.format(
						getString(R.string.optimized_item_text),
						mOptimizedAdatper.getCount()));
				mFenshuTv.setText(mRateMarks + "");
				mRatingBar.setRating((float) (mRateMarks * 1.0 / 100 * 5));
				if (mOptimizedItemLinear.getVisibility() != View.VISIBLE) {
					mOptimizedItemLinear.setVisibility(View.VISIBLE);
				}
				lp1 = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT, Double.valueOf(
								(mOptimizedAdatper.getCount()) * mScreenDensity
										* 30).intValue());
				mOptimziedItemListView.setLayoutParams(lp1);
				break;
			case COMPLETE_EXAM:
				mBaseProgress = 0;
				mFenshuTv.setText(mRateMarks + "");
				mRatingBar.setRating((float) (mRateMarks * 1.0 / 100 * 5));
				SharedPreferences sp = KindroidSecurityApplication.sh;
				Editor editor = sp.edit();
				editor.putLong(MOBILE_EXAM_LAST_TIME,
						System.currentTimeMillis());
				editor.putInt(MOBILE_EXAM_LAST_SCORE, mRateMarks);
				editor.commit();

				mSafeAdatper.setIsExaming(false);
				mSafeAdatper.notifyDataSetChanged();
				mDangerAdatper.setIsExaming(false);
				mDangerAdatper.notifyDataSetChanged();
				mUnoptimizedAdatper.setIsExaming(false);
				mUnoptimizedAdatper.notifyDataSetChanged();
				mOptimizedAdatper.setIsExaming(false);
				mOptimizedAdatper.notifyDataSetChanged();
				mScanningLinear.setVisibility(View.GONE);
				mScanResultLinear.setVisibility(View.VISIBLE);
				if (mUnoptimizedAdatper.getCount() > 0) {
					mScanResultTv
							.setText(String
									.format(getString(R.string.mobile_exam_result_unoptimized_prompt),
											mUnoptimizedAdatper.getCount()));
				} else {
					mScanResultTv
							.setText(R.string.mobile_exam_result_safe_prompt);
				}
				if (!mCancelExam) {
					mCancelExamButton.setVisibility(View.GONE);
					mOptimizeButton.setVisibility(View.VISIBLE);
				} else {
					mCancelExamButton.setVisibility(View.GONE);
					mOptimizeButton.setVisibility(View.VISIBLE);
					mCancelExam = false;
				}

				break;
			case UPDATE_PROGRESS:
				mProgressBar.setProgress(msg.arg1);
				break;
			case COMPLETE_OPTIMIZE:
				mBaseProgress = 0;
				mFenshuTv.setText(mRateMarks + "");
				mRatingBar.setRating((float) (mRateMarks * 1.0 / 100 * 5));
				mSafeAdatper.setIsExaming(false);
				mSafeAdatper.notifyDataSetChanged();
				mDangerAdatper.setIsExaming(false);
				mDangerAdatper.notifyDataSetChanged();
				mUnoptimizedAdatper.setIsExaming(false);
				mUnoptimizedAdatper.notifyDataSetChanged();
				mOptimizedAdatper.setIsExaming(false);
				mOptimizedAdatper.notifyDataSetChanged();
				mScanningLinear.setVisibility(View.GONE);
				mScanResultLinear.setVisibility(View.VISIBLE);
				if (mUnoptimizedAdatper.getCount() > 0) {
					mScanResultTv
							.setText(String
									.format(getString(R.string.mobile_exam_result_unoptimized_prompt),
											mUnoptimizedAdatper.getCount()));
					mOptimizeButton.setClickable(true);
					Toast.makeText(MobileExamActivity.this,
							R.string.mobile_exam_optimize_uncomplete_prompt,
							Toast.LENGTH_LONG).show();
				} else {
					mScanResultTv
							.setText(R.string.mobile_exam_result_safe_prompt);
					mOptimizeButton.setClickable(false);
				}
				mCancelExamButton.setVisibility(View.GONE);
				mOptimizeButton.setVisibility(View.VISIBLE);
				sp = KindroidSecurityApplication.sh;
				editor = sp.edit();
				editor.putLong(MOBILE_OPTIMIZE_LAST_TIME,
						System.currentTimeMillis());
				editor.putInt(MOBILE_OPTIMIZE_LAST_SCORE, mRateMarks);
				editor.putInt(MOBILE_EXAM_LAST_SCORE, mRateMarks);
				editor.commit();
				break;
			case COMPLETE_ONE_OPTIMZE:
				mFenshuTv.setText(mRateMarks + "");
				mRatingBar.setRating((float) (mRateMarks * 1.0 / 100 * 5));
				mSafeAdatper.setIsExaming(false);
				mSafeAdatper.notifyDataSetChanged();
				mDangerAdatper.setIsExaming(false);
				mDangerAdatper.notifyDataSetChanged();
				mUnoptimizedAdatper.setIsExaming(false);
				mUnoptimizedAdatper.notifyDataSetChanged();
				mOptimizedAdatper.setIsExaming(false);
				mOptimizedAdatper.notifyDataSetChanged();
				if (mUnoptimizedAdatper.getCount() > 0) {
					mOptimizeButton.setClickable(true);
					mScanResultTv
							.setText(String
									.format(getString(R.string.mobile_exam_result_unoptimized_prompt),
											mUnoptimizedAdatper.getCount()));
				} else {
					mScanResultTv
							.setText(R.string.mobile_exam_result_safe_prompt);
					mOptimizeButton.setClickable(false);
				}
				sp = KindroidSecurityApplication.sh;
				editor = sp.edit();
				editor.putLong(MOBILE_OPTIMIZE_LAST_TIME,
						System.currentTimeMillis());
				editor.putInt(MOBILE_OPTIMIZE_LAST_SCORE, mRateMarks);
				editor.putInt(MOBILE_EXAM_LAST_SCORE, mRateMarks);
				editor.commit();
				break;

			}
		}
	};

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		mCancelExam = true;
	}

	private void startOptimize() {
		mScanAppTv.setText("");
		mScanTitleTv.setText(R.string.start_manage_disabling_text);
		mScanningLinear.setVisibility(View.VISIBLE);
		mProgressBar.setProgress(0);
		mScanResultLinear.setVisibility(View.GONE);
		new OptimizeThread().start();
	}

	private class OptimizeThread extends Thread {
		public void run() {
			List<MobileExamItem> mList = new ArrayList<MobileExamItem>();
			for (int i = 0; i < mUnoptimizedAdatper.getCount(); i++) {
				Message msg = new Message();
				msg.what = UPDATE_PROGRESS;
				msg.arg1 = Double.valueOf(
						(i * 1.0 / mUnoptimizedAdatper.getCount()) * 20)
						.intValue();
				mHandler.sendMessage(msg);
				MobileExamItem mItem = (MobileExamItem) mUnoptimizedAdatper
						.getItem(i);
				mList.add(mItem);
			}
			int i = 0;
			for (MobileExamItem mItem : mList) {
				i++;
				Message msg = new Message();
				msg.what = UPDATE_PROGRESS;
				msg.arg1 = Double.valueOf((i * 1.0 / mList.size()) * 80)
						.intValue() + 20;
				mHandler.sendMessage(msg);
				try {
					mItem.optimizeAction();
					if (mItem.getItemStatus() < 1) {
						mUnoptimizedAdatper.delItem(mItem);
						mSafeAdatper.addItem(mItem);
						mRateMarks = mRateMarks + 5;
						mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
						mHandler.sendEmptyMessage(UPDATE_SAFE_ADAPTER);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			mHandler.sendEmptyMessage(COMPLETE_OPTIMIZE);

		}
	}

	private class MobileExamineThread extends Thread {

		public void run() {
			try {
				sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}

			scanAutoStartOnBoot();
			if (mCancelExam) {
				mHandler.sendEmptyMessage(COMPLETE_EXAM);
				return;
			}
			
			try {
				sleep(200);
			} catch (Exception e) {
				e.printStackTrace();
			}
			scanVirusUpdate();
			if (mCancelExam) {
				mHandler.sendEmptyMessage(COMPLETE_EXAM);
				return;
			}
			try {
				sleep(200);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			scanTrafficMonitor();
			if (mCancelExam) {
				mHandler.sendEmptyMessage(COMPLETE_EXAM);
				return;
			}
			try {
				sleep(200);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			scanRemoteSecurity();
			if (mCancelExam) {
				mHandler.sendEmptyMessage(COMPLETE_EXAM);
				return;
			}
			
			try {
				sleep(200);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			scanRunningTask();
			if (mCancelExam) {
				mHandler.sendEmptyMessage(COMPLETE_EXAM);
				return;
			}
			try {
				sleep(200);
			} catch (Exception e) {
				e.printStackTrace();
			}
			scanSecurityService();
			if (mCancelExam) {
				mHandler.sendEmptyMessage(COMPLETE_EXAM);
				return;
			}
			try {
				sleep(200);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			scanCloudVirusScan();
			if (mCancelExam) {
				mHandler.sendEmptyMessage(COMPLETE_EXAM);
				return;
			}
			
			try {
				sleep(200);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			scanCache();

		}
	}

	private void scanVirusUpdate() {
		Message msg = new Message();
		msg.what = UPDATE_SCAN_APP;
		msg.obj = "";
		mHandler.sendMessage(msg);

		msg = new Message();
		msg.what = UPDATE_SCAN_TITLE;
		msg.obj = getString(R.string.mobile_exam_scan_virus_update);
		mHandler.sendMessage(msg);

		SharedPreferences sp = KindroidSecurityApplication.sh;
		long last_scan_time = sp.getLong(UtilShareprefece.LAST_UPDATE_TIME, 0L);
		VirusUpdateItem mItem = new VirusUpdateItem(this);
		mItem.setLastUpdateTime(last_scan_time);
		if (last_scan_time == 0) {
			mItem.setItemStatus(MobileExamItem.UNOPTIMIZED_ITEM);
			mUnoptimizedAdatper.addItem(mItem);
			mRateMarks = mRateMarks + mItem.getDecScore();
			mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
		} else {
			long period = System.currentTimeMillis() - last_scan_time;
			if (period >= MONTH_PERIOD) {
				mItem.setItemStatus(MobileExamItem.UNOPTIMIZED_ITEM);
				mUnoptimizedAdatper.addItem(mItem);
				mRateMarks = mRateMarks + mItem.getDecScore();
				mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
			} else {
				mItem.setItemStatus(MobileExamItem.OPTIMIZED_ITEM);
				mSafeAdatper.addItem(mItem);
				mHandler.sendEmptyMessage(UPDATE_SAFE_ADAPTER);
			}
		}
		mBaseProgress = mBaseProgress + 5;
		msg = new Message();
		msg.what = UPDATE_PROGRESS;
		msg.arg1 = mBaseProgress;
		mHandler.sendMessage(msg);
	}

	private void scanAutoStartOnBoot() {
		Message msg = new Message();
		msg.what = UPDATE_SCAN_APP;
		msg.obj = "";
		mHandler.sendMessage(msg);

		msg = new Message();
		msg.what = UPDATE_SCAN_TITLE;
		msg.obj = getString(R.string.mobile_exam_scan_autostart_onboot);
		mHandler.sendMessage(msg);

		PackageManager pm = getPackageManager();
		List<PackageInfo> packs = pm.getInstalledPackages(0);

		int num = 0;
		AutoStartOnBootItem mItem = new AutoStartOnBootItem(this);
		for (PackageInfo packageInfo : packs) {
			num++;
			if (mCancelExam) {
				break;
			}
			msg = new Message();
			msg.what = UPDATE_SCAN_APP;
			msg.obj = packageInfo.applicationInfo.loadLabel(pm);
			mHandler.sendMessage(msg);

			msg = new Message();
			msg.what = UPDATE_PROGRESS;
			msg.arg1 = mBaseProgress
					+ Double.valueOf((num * 1.0 / packs.size()) * 30)
							.intValue();
			mHandler.sendMessage(msg);

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
							String receivername = xmlParser.getAttributeValue(
									ANDROID_RESOURCES, "name");

							int outerDepthReceiver = xmlParser.getDepth();

							while ((eventType = xmlParser.next()) != XmlPullParser.END_DOCUMENT
									&& (eventType != XmlPullParser.END_TAG || xmlParser
											.getDepth() > outerDepthReceiver)) {

								if (eventType == XmlPullParser.END_TAG
										|| eventType == XmlPullParser.TEXT) {
									continue;
								}

								if (xmlParser.getName().equals("intent-filter")) {
									int outerDepthIntent = xmlParser.getDepth();

									while ((eventType = xmlParser.next()) != XmlPullParser.END_DOCUMENT
											&& (eventType != XmlPullParser.END_TAG || xmlParser
													.getDepth() > outerDepthIntent)) {

										if (eventType == XmlPullParser.END_TAG
												|| eventType == XmlPullParser.TEXT) {
											continue;
										}

										String nodeName = xmlParser.getName();
										if (nodeName.equals("action")) {
											String valueAction = xmlParser
													.getAttributeValue(
															ANDROID_RESOURCES,
															"name");

											if (valueAction
													.contains("BOOT_COMPLETED")) {
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
					boolean mEnabled = false;
					for (int i = 0; i < item.getComponents().size(); i++) {
						ComponentName componentName = new ComponentName(
								packageInfo.packageName, item.getComponents()
										.get(i));
						int enableSetting = pm
								.getComponentEnabledSetting(componentName);
						if (enableSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
							mEnabled = true;
						}
					}
					if (mEnabled) {
						mItem.setItemStatus(MobileExamItem.UNOPTIMIZED_ITEM);
						item.setPackageName(packageInfo.packageName);
						mItem.addOptimizeItem(item);
					}

				}
			} catch (IOException ioe) {

			} catch (XmlPullParserException xppe) {

			}

		}
		if (mItem.getItemStatus() > 1) {
			mUnoptimizedAdatper.addItem(mItem);
			mRateMarks = mRateMarks + mItem.getDecScore();
			mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
		} else {
			mSafeAdatper.addItem(mItem);
			mHandler.sendEmptyMessage(UPDATE_SAFE_ADAPTER);
		}
		mBaseProgress = mBaseProgress + 30;
	}

	private void scanCache() {
		Message msg = new Message();
		msg.what = UPDATE_SCAN_APP;
		msg.obj = "";
		mHandler.sendMessage(msg);

		msg = new Message();
		msg.what = UPDATE_SCAN_TITLE;
		msg.obj = getString(R.string.mobile_exam_scan_cache);
		mHandler.sendMessage(msg);
		List<PackageInfo> installedCacheAppList = getPackageManager()
				.getInstalledPackages(0);
		mCacheSize = 0;
		count = 0;
		installedAppsCount = installedCacheAppList.size();
		for (PackageInfo pInfo : installedCacheAppList) {
			getPkgInfo(pInfo.packageName);
		}
	}

	private void getPkgInfo(String pkg) {
		PackageManager pm = getPackageManager();

		try {
			if (pkgObserver == null) {
				pkgObserver = new PkgSizeObserver();
			}
			Method getPackageSizeInfo = pm.getClass().getDeclaredMethod(
					"getPackageSizeInfo", String.class,
					IPackageStatsObserver.class);
			getPackageSizeInfo.setAccessible(true);
			getPackageSizeInfo.invoke(pm, pkg, pkgObserver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class PkgSizeObserver extends IPackageStatsObserver.Stub {

		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			try {
				PackageManager pm = getPackageManager();
				PackageInfo pi = pm.getPackageInfo(pStats.packageName,
						PackageManager.GET_PERMISSIONS);

				Message msg = new Message();
				msg.what = UPDATE_SCAN_APP;
				msg.obj = pi.applicationInfo.loadLabel(pm);
				mHandler.sendMessage(msg);
				if (pStats.cacheSize > 0) {
					mCacheSize = mCacheSize + (int) pStats.cacheSize;
				}
				count++;
				msg = new Message();
				msg.what = UPDATE_PROGRESS;
				msg.arg1 = mBaseProgress
						+ Double.valueOf(
								(count * 1.0 / installedAppsCount) * 30)
								.intValue();
				mHandler.sendMessage(msg);

				if (count == installedAppsCount) {
					CacheClearItem mItem = new CacheClearItem(
							MobileExamActivity.this);
					if (mCacheSize > 0) {
						mItem.setItemStatus(MobileExamItem.UNOPTIMIZED_ITEM);
						mUnoptimizedAdatper.addItem(mItem);
						mRateMarks = mRateMarks + mItem.getDecScore();
						mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
					} else {
						mItem.setItemStatus(MobileExamItem.SAFE_ITEM);
						mSafeAdatper.addItem(mItem);
						mHandler.sendEmptyMessage(UPDATE_SAFE_ADAPTER);
					}
					mBaseProgress = mBaseProgress + 30;
					mHandler.sendEmptyMessage(COMPLETE_EXAM);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void scanRunningTask() {
		Message msg = new Message();
		msg.what = UPDATE_SCAN_APP;
		msg.obj = "";
		mHandler.sendMessage(msg);

		msg = new Message();
		msg.what = UPDATE_SCAN_TITLE;
		msg.obj = getString(R.string.mobile_exam_scan_running_task);
		mHandler.sendMessage(msg);
		MemoryInfo memoryInfo = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(memoryInfo);
		long totalMemory = MemoryUtil.getTotalMemory();
		List<RunningAppProcessInfo> procList = activityManager
				.getRunningAppProcesses();
		List<ProcInfo> list = new ArrayList<ProcInfo>();
		PackageManager pm = getPackageManager();
		RunningTaskItem mItem = new RunningTaskItem(this);
		int i = 0;
		int useMem = 0;
		
		for (RunningAppProcessInfo appinfo : procList) {
			try {
				ApplicationInfo applicationInfo = pm.getApplicationInfo(
						appinfo.pkgList[0], PackageManager.GET_META_DATA);
				CharSequence appLabel = applicationInfo.loadLabel(pm);
				if (appLabel != null) {
					msg = new Message();
					msg.what = UPDATE_SCAN_APP;
					msg.obj = appLabel;
					mHandler.sendMessage(msg);
				}
				if (applicationInfo.packageName.equals(getPackageName()))
					continue;
				if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
						&& (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					ProcInfo application = new ProcInfo(appinfo.pkgList[0]);
					application.setPid(appinfo.pid);
					mItem.addProc(application);
					useMem = useMem + application.getMemory(this);
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			i++;
			
			msg = new Message();
			msg.what = UPDATE_PROGRESS;
			msg.arg1 = mBaseProgress
					+ Double.valueOf((i * 1.0 / procList.size()) * 20)
							.intValue();
			mHandler.sendMessage(msg);
			if (mCancelExam) {
				break;
			}
		}
		
		int memUsage = Double.valueOf((useMem * 1024.0 / totalMemory) * 100)
				.intValue();

		if (mItem.getTaskSum() > 0) {
			mItem.setItemStatus(MobileExamItem.UNOPTIMIZED_ITEM);
			mItem.setUseMemory(new UnitsConversion()
					.defaultConversion(useMem * 1024));
			mRateMarks = mRateMarks + mItem.getDecScore();
			mUnoptimizedAdatper.addItem(mItem);			
			mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
		}
		mBaseProgress = mBaseProgress + 20;
	}

	private void scanCloudVirusScan() {
		Message msg = new Message();
		msg.what = UPDATE_SCAN_APP;
		msg.obj = "";
		mHandler.sendMessage(msg);

		msg = new Message();
		msg.what = UPDATE_SCAN_TITLE;
		msg.obj = getString(R.string.mobile_exam_scan_virus_scan);
		mHandler.sendMessage(msg);
		SharedPreferences sp = KindroidSecurityApplication.sh;
		long last_scan_time = sp.getLong(UtilShareprefece.LAST_SCAN_TIME, 0L);
		VirusScanItem mItem = new VirusScanItem(this);
		mItem.setLastVirusScanTime(last_scan_time);
		if (last_scan_time == 0) {
			mItem.setItemStatus(MobileExamItem.UNOPTIMIZED_ITEM);
			mUnoptimizedAdatper.addItem(mItem);
			mRateMarks = mRateMarks + mItem.getDecScore();
			mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
		} else {
			long period = System.currentTimeMillis() - last_scan_time;
			if (period >= MONTH_PERIOD) {
				mItem.setItemStatus(MobileExamItem.UNOPTIMIZED_ITEM);
				mUnoptimizedAdatper.addItem(mItem);
				mRateMarks = mRateMarks + mItem.getDecScore();
				mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
			} else {
				mItem.setItemStatus(MobileExamItem.OPTIMIZED_ITEM);
				mSafeAdatper.addItem(mItem);
				mHandler.sendEmptyMessage(UPDATE_SAFE_ADAPTER);
			}
		}
		mBaseProgress = mBaseProgress + 5;
		msg = new Message();
		msg.what = UPDATE_PROGRESS;
		msg.arg1 = mBaseProgress;
		mHandler.sendMessage(msg);
	}

	private void scanSecurityService() {
		Message msg = new Message();
		msg.what = UPDATE_SCAN_APP;
		msg.obj = "";
		mHandler.sendMessage(msg);

		msg = new Message();
		msg.what = UPDATE_SCAN_TITLE;
		msg.obj = getString(R.string.mobile_exam_scan_security_sevice);
		mHandler.sendMessage(msg);
		ComponentName componentName = new ComponentName(
				"com.kindroid.security",
				"com.kindroid.security.service.OnBootReceiver");
		PackageManager pm = getPackageManager();
		int enableSetting = pm.getComponentEnabledSetting(componentName);
		EnableSecurityServiceItem mItem = new EnableSecurityServiceItem(this);
		if (enableSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
			mItem.setItemStatus(MobileExamItem.SAFE_ITEM);
			mSafeAdatper.addItem(mItem);
			mHandler.sendEmptyMessage(UPDATE_SAFE_ADAPTER);
		} else {
			mRateMarks = mRateMarks + mItem.getDecScore();
			mItem.setItemStatus(MobileExamItem.UNOPTIMIZED_ITEM);
			mUnoptimizedAdatper.addItem(mItem);
			mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
		}
		mBaseProgress = mBaseProgress + 5;
		msg = new Message();
		msg.what = UPDATE_PROGRESS;
		msg.arg1 = mBaseProgress;
		mHandler.sendMessage(msg);
	}

	private void scanRemoteSecurity() {
		Message msg = new Message();
		msg.what = UPDATE_SCAN_APP;
		msg.obj = "";
		mHandler.sendMessage(msg);

		msg = new Message();
		msg.what = UPDATE_SCAN_TITLE;
		msg.obj = getString(R.string.mobile_exam_scan_remote_security);
		mHandler.sendMessage(msg);
		boolean kindSecurityFuc = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_REMOTESECURITY, false);
		RemoteSecurityItem mItem = new RemoteSecurityItem(this);
		if (kindSecurityFuc) {
			mItem.setItemStatus(MobileExamItem.SAFE_ITEM);
			mSafeAdatper.addItem(mItem);
			mHandler.sendEmptyMessage(UPDATE_SAFE_ADAPTER);
		} else {
			mRateMarks = mRateMarks + mItem.getDecScore();
			mItem.setItemStatus(MobileExamItem.UNOPTIMIZED_ITEM);
			mUnoptimizedAdatper.addItem(mItem);
			mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
		}
		mBaseProgress = mBaseProgress + 5;
		msg = new Message();
		msg.what = UPDATE_PROGRESS;
		msg.arg1 = mBaseProgress;
		mHandler.sendMessage(msg);
	}

	private void scanTrafficMonitor() {
		Message msg = new Message();
		msg.what = UPDATE_SCAN_APP;
		msg.obj = "";
		mHandler.sendMessage(msg);

		msg = new Message();
		msg.what = UPDATE_SCAN_TITLE;
		msg.obj = getString(R.string.mobile_exam_scan_traffic_monitor);
		mHandler.sendMessage(msg);
		boolean isEnable = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		NetTrafficMonitorItem mItem = new NetTrafficMonitorItem(this);
		if (isEnable) {
			mItem.setItemStatus(MobileExamItem.SAFE_ITEM);
			mSafeAdatper.addItem(mItem);
			mHandler.sendEmptyMessage(UPDATE_SAFE_ADAPTER);
		} else {
			mRateMarks = mRateMarks + mItem.getDecScore();
			mItem.setItemStatus(MobileExamItem.UNOPTIMIZED_ITEM);
			mUnoptimizedAdatper.addItem(mItem);
			mHandler.sendEmptyMessage(UPDATE_UNOPTIMIZED_ADAPTER);
		}
		mBaseProgress = mBaseProgress + 5;
		msg = new Message();
		msg.what = UPDATE_PROGRESS;
		msg.arg1 = mBaseProgress;
		mHandler.sendMessage(msg);
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
		case R.id.cancel_scan_button:
			mCancelExam = true;
			break;
		case R.id.optimize_button:
			startOptimize();
			break;
		}
	}
}
