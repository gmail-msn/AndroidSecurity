/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kindroid.security.AVEngine;
import com.kindroid.security.ApkSignatureInfo;
import com.kindroid.security.R;
import com.kindroid.security.VirusInfo;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.util.SQLiteDBHelper;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.SQLiteHelper;
import com.kindroid.security.util.UtilShareprefece;
import com.kindroid.security.util.Utilis;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class VirusScanFirstActivity extends Activity {
	private List<ScanResultInfo> mListNoRisk = new ArrayList<ScanResultInfo>();
	private List<ScanResultInfo> mListWarning = new ArrayList<ScanResultInfo>();
	private List<ScanResultInfo> mListRisk = new ArrayList<ScanResultInfo>();
	private List<ScanResultInfo> mListDanger = new ArrayList<ScanResultInfo>();
	private boolean mIsCanceled = false;
	private ProgressBar mProgressBar;
	private static final int SCANNING = 1;
	private static final int FINISH_SCAN = 2;

	private static final int UNINSTALL_CODE = 3;
	private List<ScanResultCategory> mScanCategory;
	private TextView scan_progress_text;

	private static final long perminite = 60000L;
	private static final long perhour = 3600000L;
	private static final long perday = 86400000L;
	private static final long permonth = 2592000000L;
	private CheckBox select_al_cb;
	private int numNeedTreat;
	private static int scanCompletePercent = 0;
	private VirusCleanListAdapter listAdapter;
	private static int step = 0;
	private static int[] state = new int[4];
	private static int cleanSucc = 0;
	private static boolean isBackPressed = false;
	private static VirusScanFirstActivity thisInstance;
	public static boolean isScanning = false;
	private static VirusScanFirstActivity instance;
	private static boolean mCurrentDisplay = false;
	private static boolean mCloudScan = true;
	private static final String mCloudServer = "http://203.156.192.254:8080/AVEngine/jsonrpc/CloudScan";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		instance = this;
		initDatabase();
		thisInstance = this;
		loadFirstPage();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mCurrentDisplay = true;
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mCurrentDisplay = false;
	}

	public static void backToPrev() {
		thisInstance.onBackPressed();
	}

	public static int getStep() {
		return step;
	}

	public static void changeTab(final VirusScanTabActivity vsta,
			final View tabView) {
		final Dialog promptDialog = new Dialog(thisInstance, R.style.softDialog);
		View view = LayoutInflater.from(thisInstance).inflate(
				R.layout.soft_uninstall_prompt_dialog, null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);
		promptText.setText(R.string.virus_scan_cancel_prompt_text);

		View button_ok = promptDialog.findViewById(R.id.button_ok);
		View button_cancel = promptDialog.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				thisInstance.mIsCanceled = true;
				promptDialog.dismiss();
				isScanning = false;
				vsta.onClick(tabView);

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

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		isBackPressed = true;
		switch (step) {
		case 0:
			super.onBackPressed();
			break;
		case 1:
			if (scanCompletePercent < 100) {
				final Dialog promptDialog = new Dialog(
						VirusScanFirstActivity.this, R.style.softDialog);
				View view = LayoutInflater.from(VirusScanFirstActivity.this)
						.inflate(R.layout.soft_uninstall_prompt_dialog, null);
				promptDialog.setContentView(view);

				TextView promptText = (TextView) promptDialog
						.findViewById(R.id.prompt_text);
				promptText.setText(R.string.virus_scan_cancel_prompt_text);

				View button_ok = promptDialog.findViewById(R.id.button_ok);
				View button_cancel = promptDialog
						.findViewById(R.id.button_cancel);
				button_ok.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mIsCanceled = true;
						promptDialog.dismiss();
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

			break;
		case 2:
			loadFirstPage();
			break;
		case 3:
			loadThirdPage();
			break;
		}
	}

	private String formatLastScanTime(long last_scan_time) {
		String retStr = null;
		if (last_scan_time == 0) {
			retStr = "0 " + getString(R.string.virus_scan_days_ago_text);
		} else {
			long period = System.currentTimeMillis() - last_scan_time;
			if (period > permonth) {
				retStr = "<font color=\'#64BD45\'><big>" + period / permonth
						+ "</big></font> "
						+ getString(R.string.virus_scan_months_ago_text);

			} else if (period > perday) {
				retStr = "<font color=\'#64BD45\'><big>" + period / perday + "</big></font> "
						+ getString(R.string.virus_scan_days_ago_text);
			} else if (period > perhour) {
				retStr = "<font color=\'#64BD45\'><big>" + period / perhour + "</big></font> "
						+ getString(R.string.virus_scan_hours_ago_text);
			} else if (period > perminite) {
				retStr = "<font color=\'#64BD45\'><big>" + period / perminite + "</big></font> "
						+ getString(R.string.virus_scan_minites_ago_text);
			} else {
				retStr = "<font color=\'#64BD45\'><big>" + period / 1000 + "</big></font> "
						+ getString(R.string.virus_scan_seconds_ago_text);
			}
		}
		return retStr;
	}

	private void loadFirstPage() {
		step = 0;
		setContentView(R.layout.virus_scan_1);
		
//		View home_page = findViewById(R.id.home_icon);
//		home_page.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub				
//					Intent homepage = new Intent(VirusScanFirstActivity.this,
//							DefenderTabMain.class);
//					homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//					startActivity(homepage);
//					finish();				
//			}
//		});
		TextView cloud_scan_prompt_text = (TextView) findViewById(R.id.cloud_scan_prompt_text);
		cloud_scan_prompt_text.setText(Html
				.fromHtml(getString(R.string.virus_cloud_scan_prompt_text)));
		CheckBox cloud_scan_checkbox = (CheckBox) findViewById(R.id.cloud_scan_checkbox);
		cloud_scan_checkbox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						mCloudScan = isChecked;
					}

				});
		cloud_scan_checkbox.setChecked(true);
		mCloudScan = true;
		View virus_scan_1_layout = findViewById(R.id.virus_scan_1_layout);
		TextView scan_progress_text = (TextView) findViewById(R.id.scan_progress_text);
		scan_progress_text.setText(scanCompletePercent + "%");
		View virus_scan_begin_linear = findViewById(R.id.virus_scan_begin_linear);
		TextView virus_scan_last_time_date = (TextView) findViewById(R.id.virus_scan_last_time_date);
		SharedPreferences sp = KindroidSecurityApplication.sh;
		long last_scan_time = sp.getLong(UtilShareprefece.LAST_SCAN_TIME, 0L);
		virus_scan_last_time_date.setText(Html.fromHtml(formatLastScanTime(last_scan_time)));
		if (last_scan_time != 0) {
			// process history
			SQLiteDBHelper dh = new SQLiteDBHelper(this,
					SQLiteDBHelper.historyDBName);
			SQLiteDatabase db = dh.getWritableDatabase();
			Cursor cs = db.query(SQLiteDBHelper.cleanHistoryTableName, null,
					"time=?", new String[] { String.valueOf(last_scan_time) },
					null, null, null);

			View last_clean_linear = findViewById(R.id.last_clean_linear);
			if (cs.getCount() > 0) {
				last_clean_linear.setVisibility(View.VISIBLE);
				TextView last_unclean_result_sum = (TextView) findViewById(R.id.last_unclean_result_sum);
				last_unclean_result_sum.setText(String.valueOf(cs.getCount()));
				View button_for_clean_linear = findViewById(R.id.button_for_clean_linear);
				button_for_clean_linear
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								Intent intent = new Intent(
										VirusScanFirstActivity.this,
										VirusCleanActivity.class);
								startActivityForResult(intent, 99);
							}
						});
			} else {
				last_clean_linear.setVisibility(View.GONE);
			}
			try {
				cs.close();
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		TextView virus_scan_update_last_time = (TextView) findViewById(R.id.virus_scan_update_last_time);
		long last_update_time = sp.getLong(UtilShareprefece.LAST_UPDATE_TIME,
				0L);
		if (last_update_time == 0) {
			virus_scan_update_last_time
					.setText(R.string.virus_has_no_update_text);
		} else {
			Date date = new Date(last_update_time);
			Locale locale = this.getResources().getConfiguration().locale;
			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, locale);
			virus_scan_update_last_time.setText(df.format(date));
		}
		virus_scan_begin_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				isScanning = true;
				loadSecondPage();
			}
		});
	}

	public static void completeUpdate() {
		if (step == 0) {
			SharedPreferences sp = KindroidSecurityApplication.sh;
			TextView virus_scan_update_last_time = (TextView) instance
					.findViewById(R.id.virus_scan_update_last_time);
			long last_update_time = sp.getLong(
					UtilShareprefece.LAST_UPDATE_TIME, 0L);
			if (last_update_time == 0) {
				virus_scan_update_last_time
						.setText(R.string.virus_has_no_update_text);
			} else {
				Date date = new Date(last_update_time);
				Locale locale = instance.getResources().getConfiguration().locale;
				DateFormat df = DateFormat.getDateInstance(DateFormat.LONG,
						locale);
				virus_scan_update_last_time.setText(df.format(date));
			}
		}
	}

	private void initDatabase() {
		try {
			SQLiteHelper myDbHelper = new SQLiteHelper(this);

			myDbHelper.createDataBase();

			myDbHelper.openDataBase();

			myDbHelper.close();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		} catch (SQLException sqle) {
			throw sqle;
		}
	}

	public void doScan() {
		mListNoRisk.clear();
		mListWarning.clear();
		mListRisk.clear();
		mListDanger.clear();
		cleanSucc = 0;

		mIsCanceled = false;

		TextView apkView = (TextView) findViewById(R.id.apktext);
		apkView.setText("", BufferType.EDITABLE);
		apkView.setMovementMethod(ScrollingMovementMethod.getInstance());
		new ApkScanning().start();
	}

	private void loadSecondPage() {
		step = 1;
		isBackPressed = false;
		scanCompletePercent = 0;
		setContentView(R.layout.virus_scan_2);
		VirusScanTabActivity.hideHomeIcon();
		mProgressBar = (ProgressBar) findViewById(R.id.scan_progress);
		scan_progress_text = (TextView) findViewById(R.id.scan_progress_text);
		mScanCategory = new ArrayList<ScanResultCategory>();
		mScanCategory.add(new ScanResultCategory(this, 0, mListNoRisk));
		mScanCategory.add(new ScanResultCategory(this, 1, mListWarning));
		mScanCategory.add(new ScanResultCategory(this, 2, mListRisk));
		mScanCategory.add(new ScanResultCategory(this, 3, mListDanger));

		doScan();
		View virus_scan_cancel_linear = findViewById(R.id.virus_scan_cancel_linear);
		virus_scan_cancel_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				final Dialog promptDialog = new Dialog(
						VirusScanFirstActivity.this, R.style.softDialog);
				View view = LayoutInflater.from(VirusScanFirstActivity.this)
						.inflate(R.layout.soft_uninstall_prompt_dialog, null);
				promptDialog.setContentView(view);

				TextView promptText = (TextView) promptDialog
						.findViewById(R.id.prompt_text);
				promptText.setText(R.string.virus_scan_cancel_prompt_text);

				View button_ok = promptDialog.findViewById(R.id.button_ok);
				View button_cancel = promptDialog
						.findViewById(R.id.button_cancel);
				button_ok.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						promptDialog.dismiss();
						mIsCanceled = true;
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
		});

	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 0:
				scan_progress_text.setText("0%");
				break;
			case SCANNING:
				if (!mIsCanceled) {
					TextView apkView = (TextView) findViewById(R.id.apktext);
					String textOrigin = apkView.getText().toString();
					String text = (String) msg.obj + "\n" + textOrigin;
					
					apkView.setText(text);
					scan_progress_text.setText(msg.arg2 + "%");
					mProgressBar.setProgress(msg.arg2);
				}
				break;
			case FINISH_SCAN:
				loadThirdPage();

				break;
			case 3:
				Toast.makeText(VirusScanFirstActivity.this,
						R.string.cloud_scan_network_unavailable,
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	private void loadThirdPage() {
		step = 2;
		setContentView(R.layout.virus_scan_3);
		VirusScanTabActivity.showHomeIcon();
//		View home_page = findViewById(R.id.home_icon);
//		home_page.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub				
//					Intent homepage = new Intent(VirusScanFirstActivity.this,
//							DefenderTabMain.class);
//					homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//					startActivity(homepage);
//					finish();
//				
//			}
//		});
		View virus_scan_report_linear = findViewById(R.id.virus_scan_report_linear);
		TextView virus_scan_fail_prompt_text = (TextView) findViewById(R.id.virus_scan_result_text);
		TextView virus_report_text = (TextView) findViewById(R.id.virus_report_text);
		TextView virus_scan_3_progress_text = (TextView) findViewById(R.id.virus_scan_3_progress_text);
		virus_scan_3_progress_text.setText(scanCompletePercent + "%");
		ScanResultCategory c0 = mScanCategory.get(0);
		ScanResultCategory c1 = mScanCategory.get(1);
		ScanResultCategory c2 = mScanCategory.get(2);
		ScanResultCategory c3 = mScanCategory.get(3);

		TextView viruses_found_text = (TextView) findViewById(R.id.viruses_found_text);
		TextView clean_success_text = (TextView) findViewById(R.id.clean_success_text);
		TextView care_to_use_text = (TextView) findViewById(R.id.care_to_use_text);
		TextView need_to_treat_text = (TextView) findViewById(R.id.need_to_treat_text);
		if (c2.getResultList().size() == 0 && c3.getResultList().size() == 0
				&& c1.getResultList().size() == 0) {
			virus_scan_fail_prompt_text
					.setText(Html
							.fromHtml(getString(R.string.virus_scan_success_prompt_text)));
			virus_report_text.setVisibility(View.GONE);
			virus_scan_report_linear.setVisibility(View.GONE);
			need_to_treat_text.setText("0");
			care_to_use_text.setText(String.valueOf(c1.getResultList().size()));
			clean_success_text.setText(String.valueOf(cleanSucc));
			viruses_found_text.setText("0");
		} else {
			viruses_found_text.setText(String
					.valueOf(c3.getResultList().size()));
			clean_success_text.setText(String.valueOf(cleanSucc));
			care_to_use_text.setText(String.valueOf(c1.getResultList().size()));
			need_to_treat_text.setText(String.valueOf((c3.getResultList()
					.size()
					+ c2.getResultList().size()
					+ c1.getResultList().size() - cleanSucc)));
			virus_scan_fail_prompt_text.setText(Html
					.fromHtml(getString(R.string.virus_scan_fail_prompt_text)));
			
			virus_scan_report_linear.setVisibility(View.VISIBLE);
		}
		if (virus_scan_report_linear.VISIBLE == View.VISIBLE) {
			virus_scan_report_linear
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							loadFourthPage();
						}
					});
		}
		View virus_scan_again_linear = findViewById(R.id.virus_scan_again_linear);
		virus_scan_again_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				loadSecondPage();
			}
		});
	}

	private void loadFourthPage() {
		step = 3;
		setContentView(R.layout.virus_scan_4);
//		View home_page = findViewById(R.id.home_icon);
//		home_page.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//					Intent homepage = new Intent(VirusScanFirstActivity.this,
//							DefenderTabMain.class);
//					homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//					startActivity(homepage);
//					finish();				
//			}
//		});
		TextView viruses_found_text = (TextView) findViewById(R.id.viruses_found_text_1);
		TextView care_to_use_text = (TextView) findViewById(R.id.care_to_use_text_1);
		TextView need_to_treat_text = (TextView) findViewById(R.id.need_to_treat_text_1);
		TextView clean_success_text = (TextView) findViewById(R.id.clean_success_text_1);
		ScanResultCategory c0 = mScanCategory.get(0);
		ScanResultCategory c1 = mScanCategory.get(1);
		ScanResultCategory c2 = mScanCategory.get(2);
		ScanResultCategory c3 = mScanCategory.get(3);
		viruses_found_text.setText(String.valueOf(c3.getResultList().size()
				+ c2.getResultList().size()));
		clean_success_text.setText(String.valueOf(cleanSucc));
		care_to_use_text.setText(String.valueOf(c1.getResultList().size()));
		need_to_treat_text.setText(String.valueOf(c3.getResultList().size()
				+ c2.getResultList().size() + c1.getResultList().size()
				- cleanSucc));
		// need_to_treat_text.setText(numNotScanned);
		ListView virus_scan_need_treat_list = (ListView) findViewById(R.id.virus_scan_need_treat_list);
		listAdapter = new VirusCleanListAdapter(this);

		List<ScanResultInfo> resultList = c3.getResultList();
		PackageManager pm = getPackageManager();
		List<PackageInfo> packInfos = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
		for (ScanResultInfo sri : resultList) {
			PackageInfo pInfo = sri.getpInfo();
			AppInfoForManage aifm = new AppInfoForManage();
			aifm.setPackageName(pInfo.packageName);			
			aifm.setLabel(pInfo.applicationInfo.loadLabel(pm));
			aifm.setVersion(getString(R.string.virus_risk_level));
			aifm.setIcon(pInfo.applicationInfo.loadIcon(pm));
			aifm.setDescription(sri.getVirusName());
			aifm.setFlag(1);
			aifm.setInstalled(true);
			
			listAdapter.addItem(aifm);
		}
		resultList = c1.getResultList();
		for (ScanResultInfo sri : resultList) {
			PackageInfo pInfo = sri.getpInfo();
			AppInfoForManage aifm = new AppInfoForManage();
			aifm.setPackageName(pInfo.packageName);
			
			aifm.setLabel(pInfo.applicationInfo.loadLabel(pm));
			aifm.setVersion(getString(R.string.virus_warning_level));
			aifm.setIcon(pInfo.applicationInfo.loadIcon(pm));

			aifm.setDescription(sri.getVirusName());
			aifm.setFlag(1);
			aifm.setInstalled(true);
			
			listAdapter.addItem(aifm);
		}

		resultList = c2.getResultList();
		for (ScanResultInfo sri : resultList) {
			PackageInfo pInfo = sri.getpInfo();
			AppInfoForManage aifm = new AppInfoForManage();
			aifm.setPackageName(pInfo.packageName);
			
			aifm.setLabel(pInfo.applicationInfo.loadLabel(pm));
			aifm.setVersion(getString(R.string.virus_risk_level));
			aifm.setIcon(pInfo.applicationInfo.loadIcon(pm));
			aifm.setInstalled(true);
			aifm.setDescription(sri.getVirusName());
			aifm.setFlag(1);

			listAdapter.addItem(aifm);
		}

		virus_scan_need_treat_list.setAdapter(listAdapter);
		virus_scan_need_treat_list
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					public void onItemClick(AdapterView parent, View v,
							int position, long id) {
						// TODO Auto-generated method stub
						AppInfoForManage aifm = (AppInfoForManage) listAdapter
								.getItem(position);
						if (aifm.isSelected()) {
							aifm.setSelected(false);
							CheckBox softmanage_action_checkbox = (CheckBox) v
									.findViewById(R.id.softmanage_action_checkbox);
							softmanage_action_checkbox.setChecked(false);
						} else {
							aifm.setSelected(true);
							CheckBox softmanage_action_checkbox = (CheckBox) v
									.findViewById(R.id.softmanage_action_checkbox);
							softmanage_action_checkbox.setChecked(true);
						}
					}

				});
		select_al_cb = (CheckBox) findViewById(R.id.select_al_cb);
		select_al_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(!listAdapter.mOnlyStateChange){
					listAdapter.setSelectedAll(isChecked);
					listAdapter.notifyDataSetChanged();
				}

			}
		});
		View uninstall_action_linear = findViewById(R.id.uninstall_action_linear);
		uninstall_action_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int sum = 0;
				for (int i = 0; i < listAdapter.getCount(); i++) {
					AppInfoForManage aifm = (AppInfoForManage) listAdapter
							.getItem(i);
					if (aifm.isSelected())
						sum++;

				}
				if (sum > 0) {
					final Dialog promptDialog = new Dialog(
							VirusScanFirstActivity.this, R.style.softDialog);
					// promptDialog.setContentView(R.layout.soft_uninstall_prompt_dialog);
					View view = LayoutInflater
							.from(VirusScanFirstActivity.this)
							.inflate(R.layout.soft_uninstall_prompt_dialog,
									null);
					promptDialog.setContentView(view);

					TextView promptText = (TextView) promptDialog
							.findViewById(R.id.prompt_text);
					promptText.setText(R.string.softmanage_uninstall_tip_text);
					Button button_ok = (Button) promptDialog
							.findViewById(R.id.button_ok);
					Button button_cancel = (Button) promptDialog
							.findViewById(R.id.button_cancel);
					button_ok.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							promptDialog.dismiss();
							uninstallSelected();
						}
					});
					button_cancel
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									promptDialog.dismiss();
								}
							});
					promptDialog.show();
				} else {
					final Dialog promptDialog = new Dialog(
							VirusScanFirstActivity.this, R.style.softDialog);
					// promptDialog.setContentView(R.layout.soft_uninstall_prompt_dialog);
					View view = LayoutInflater
							.from(VirusScanFirstActivity.this)
							.inflate(R.layout.soft_uninstall_prompt_dialog,
									null);
					promptDialog.setContentView(view);

					TextView promptText = (TextView) promptDialog
							.findViewById(R.id.prompt_text);
					promptText.setText(R.string.softmanage_select_app);
					Button button_ok = (Button) promptDialog
							.findViewById(R.id.button_ok);
					Button button_cancel = (Button) promptDialog
							.findViewById(R.id.button_cancel);
					button_ok.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							promptDialog.dismiss();

						}
					});
					button_cancel.setVisibility(View.GONE);
					promptDialog.show();
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == UNINSTALL_CODE) {
			refreshResultInfo();
		} else if (requestCode == 99 && resultCode == Activity.RESULT_OK) {
			finish();
		}

	}

	private void refreshResultInfo() {
		PackageManager pckMan = getPackageManager();
		List<PackageInfo> packs = pckMan
				.getInstalledPackages(PackageManager.GET_SIGNATURES);

		int oldcount = 0;
		for (ScanResultCategory category : mScanCategory) {

			List<ScanResultInfo> listResult = category.getResultList();

			if (category.getRank() != 0) {
				// skip the rank 0
				oldcount += listResult.size();
			}

			List<ScanResultInfo> listResultTemp = new ArrayList<ScanResultInfo>(
					listResult);

			category.getResultList().clear();

			for (ScanResultInfo resultinfo : listResultTemp) {
				String packagename = resultinfo.getAppInfo().packageName;
				// flag for uninstalled virus package
				boolean mCleaned = true;
				for (PackageInfo packageInfo : packs) {
					if (packageInfo.versionName == null)
						continue;

					if (packageInfo.packageName
							.compareToIgnoreCase(packagename) == 0) {
						category.getResultList().add(resultinfo);
						mCleaned = false;
						break;
					}
				}
				if (mCleaned) {
					SQLiteDBHelper dh = new SQLiteDBHelper(
							VirusScanFirstActivity.this,
							SQLiteDBHelper.historyDBName);
					SQLiteDatabase db = dh.getWritableDatabase();
					db.delete(SQLiteDBHelper.cleanHistoryTableName, "pname=?",
							new String[] { packagename });
					try {
						db.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			listResultTemp.clear();
		}

		int newcount = 0;
		listAdapter.clearItems();
		for (ScanResultCategory category : mScanCategory) {
			if (category.getRank() != 0) {
				List<ScanResultInfo> listResultnew = category.getResultList();
				newcount += listResultnew.size();
				PackageManager pm = getPackageManager();
				List<PackageInfo> packInfos = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);

				for (ScanResultInfo resultinfo : listResultnew) {
					PackageInfo pInfo = resultinfo.getpInfo();
					AppInfoForManage aifm = new AppInfoForManage();
					aifm.setPackageName(pInfo.packageName);
					
					aifm.setLabel(pInfo.applicationInfo.loadLabel(pm));

					if (category.getRank() == 1) {
						aifm.setVersion(getString(R.string.virus_warning_level));
					} else if (category.getRank() == 2
							|| category.getRank() == 3) {
						aifm.setVersion(getString(R.string.virus_risk_level));
					}

					aifm.setIcon(pInfo.applicationInfo.loadIcon(pm));
					aifm.setDescription(resultinfo.getVirusName());
					aifm.setFlag(1);
					aifm.setInstalled(true);
					
					listAdapter.addItem(aifm);
				}
			}
		}

		int dealed = oldcount - newcount;
		cleanSucc += dealed;

		TextView viruses_found_text = (TextView) findViewById(R.id.viruses_found_text_1);
		TextView care_to_use_text = (TextView) findViewById(R.id.care_to_use_text_1);
		TextView need_to_treat_text = (TextView) findViewById(R.id.need_to_treat_text_1);
		TextView clean_success_text = (TextView) findViewById(R.id.clean_success_text_1);

		ScanResultCategory c1 = mScanCategory.get(1);
		ScanResultCategory c2 = mScanCategory.get(2);
		ScanResultCategory c3 = mScanCategory.get(3);

		clean_success_text.setText(String.valueOf(cleanSucc));
		need_to_treat_text.setText(String.valueOf(c3.getResultList().size()
				+ c2.getResultList().size() + c1.getResultList().size()));
		viruses_found_text.setText(String.valueOf(c3.getResultList().size()
				+ c2.getResultList().size()));
		care_to_use_text.setText(String.valueOf(c1.getResultList().size()));
		// change preference
		SharedPreferences sp = KindroidSecurityApplication.sh;
		Editor editor = sp.edit();
		editor.putInt(UtilShareprefece.LAST_VIRUS_SUM, c1.getResultList()
				.size() + c2.getResultList().size() + c3.getResultList().size());
		editor.commit();
		select_al_cb.setChecked(false);
		listAdapter.notifyDataSetChanged();
	}

	private void uninstallApp(String packageName) {
		Uri uri = Uri.fromParts("package", packageName, null);
		Intent i = new Intent(Intent.ACTION_DELETE, uri);
		startActivityForResult(i, UNINSTALL_CODE);
	}

	private void uninstallSelected() {
		List<AppInfoForManage> uninstallApps = new ArrayList<AppInfoForManage>();
		for (int i = 0; i < listAdapter.getCount(); i++) {
			AppInfoForManage aifm = (AppInfoForManage) listAdapter.getItem(i);
			if (aifm.isSelected()) {

				uninstallApps.add(aifm);

			}

		}

		for (AppInfoForManage aifm : uninstallApps) {
			uninstallApp(aifm.getPackageName());
		}
	}

	private class ApkScanning extends Thread {
		public void run() {
			isBackPressed = false;
			PackageManager pckMan = getPackageManager();
			boolean isSystemApp = false;
			List<PackageInfo> packs = pckMan
					.getInstalledPackages(PackageManager.GET_SIGNATURES);

			int count = packs.size();

			mProgressBar.setProgress(0);
			mHandler.sendEmptyMessage(0);
			isScanning = true;
//			AVEngine.avEngineInit();
			double mFloat = 1.0;
			if (mCloudScan) {
				mFloat = 0.7;
			}
			Map<ApkSignatureInfo, PackageInfo> mCloudPackagesMap = new HashMap<ApkSignatureInfo, PackageInfo>();
			for (int i = 0; i < count; i++) {
				isSystemApp = false;

				if (mIsCanceled) {
					break;
				}

				PackageInfo pi = packs.get(i);

				if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
					isSystemApp = true;
				}

				if (!isSystemApp) {
					// Compare with black list of certs
					VirusInfo info = new VirusInfo();
					ApkSignatureInfo asInfo = new ApkSignatureInfo();
					int ret = AVEngine.avEngineCheck(
							pi.applicationInfo.sourceDir, mCloudScan, info,
							asInfo);

					if (ret == 1) {
						mScanCategory
								.get(1)
								.getResultList()
								.add(new ScanResultInfo(pi.applicationInfo, pi,
										getResources().getString(
												R.string.black_certificate)));
					} else if (ret == 2) {
						mScanCategory
								.get(2)
								.getResultList()
								.add(new ScanResultInfo(pi.applicationInfo, pi,
										info.mVirusName));
					} else if (ret == 3) {
						mScanCategory
								.get(3)
								.getResultList()
								.add(new ScanResultInfo(pi.applicationInfo, pi,
										info.mVirusName));
					} else {
						mScanCategory
								.get(0)
								.getResultList()
								.add(new ScanResultInfo(pi.applicationInfo, pi,
										info.mVirusName));
						if (mCloudScan) {
							mCloudPackagesMap.put(asInfo, pi);
						}
					}
				}

				String packagename = pi.packageName;

				Message msg = new Message();
				msg.what = 1;
				msg.arg2 = Double.valueOf(((i + 1) * mFloat / count) * 100)
						.intValue();
				scanCompletePercent = msg.arg2;
				msg.obj = packagename;
				mHandler.sendMessage(msg);

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

//			AVEngine.avEngineClose();
			if (mCloudScan && !mIsCanceled
					&& Utilis.checkNetwork(VirusScanFirstActivity.this)) {
				if (mCloudPackagesMap.size() > 0) {
					final JSONObject param = new JSONObject();
					BufferedReader bis = null;
					Message msg = new Message();
					msg.what = 1;
					msg.arg2 = 70;
					scanCompletePercent = msg.arg2;
					msg.obj = getString(R.string.virus_cloud_scan_tip);
					mHandler.sendMessage(msg);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						param.put("method", "scanFiles");
						param.put("id", System.currentTimeMillis());
						param.put("jsonrpc", "2.0");
						JSONArray jsArray = new JSONArray();
						Iterator<ApkSignatureInfo> iter = mCloudPackagesMap
								.keySet().iterator();

						int t = 0;
						while (iter.hasNext()) {
							ApkSignatureInfo asInfo = iter.next();
							PackageInfo pi = mCloudPackagesMap.get(asInfo);
							JSONObject jsObject = new JSONObject();
							jsObject.put("fileFinger", asInfo.fileFinger);
							jsObject.put("featureCode", asInfo.featureCode);
							String appname = pi.applicationInfo.loadLabel(
									pckMan).toString();
							if (appname != null) {
								jsObject.put("appName", appname);
							} else {
								jsObject.put("appName", pi.packageName);
							}
							jsArray.put(jsObject);
							msg = new Message();
							msg.what = 1;
							msg.arg2 = Double
									.valueOf(
											((t++) * 1.0 / mCloudPackagesMap
													.size()) * 20).intValue() + 70;
							scanCompletePercent = msg.arg2;
							msg.obj = pi.packageName;
							mHandler.sendMessage(msg);
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						JSONArray array = new JSONArray();
						array.put(jsArray);
						param.put("params", array);

						HttpClient client = new DefaultHttpClient();
						HttpPost postRequest = new HttpPost(new URI(
								mCloudServer));
						postRequest.setHeader("Content-type",
								"application/json-rpc");

						StringEntity entity = new StringEntity(
								param.toString(), "utf-8");
						entity.setContentEncoding("utf-8");
						postRequest.setEntity(entity);

						HttpResponse response = client.execute(postRequest);
						bis = new BufferedReader(new InputStreamReader(response
								.getEntity().getContent()));
						StringBuilder sb = new StringBuilder();
						String line = bis.readLine();
						while (line != null) {
							sb.append(line);
							line = bis.readLine();
						}

						JSONObject result = new JSONObject(sb.toString());

						if (result.get("result") != null
								&& (param.getLong("id") == result.getLong("id"))) {
							JSONArray rArray = result.getJSONArray("result");
							Object[] aArray = mCloudPackagesMap.keySet()
									.toArray();
							for (int i = 0; i < rArray.length(); i++) {
								JSONObject jObject = rArray.getJSONObject(i);
								String fileFinger = jObject
										.getString("fileFinger");
								String virusName = jObject
										.getString("virusName");
								String virusDescription = jObject
										.getString("virusDescription");
								String virusBehavior = jObject
										.getString("virusBehavior");
								for (int j = 0; j < aArray.length; j++) {
									ApkSignatureInfo aInfo = (ApkSignatureInfo) aArray[j];
									if (aInfo.fileFinger.equals(fileFinger)) {
										PackageInfo pi = mCloudPackagesMap
												.get(aInfo);
										mScanCategory
												.get(3)
												.getResultList()
												.add(new ScanResultInfo(
														pi.applicationInfo, pi,
														virusName));
										break;
									}
								}
								msg = new Message();
								msg.what = 1;
								msg.arg2 = Double.valueOf(
										((i + 1) * 1.0 / rArray.length()) * 10)
										.intValue() + 90;
								scanCompletePercent = msg.arg2;
								msg.obj = virusName;
								mHandler.sendMessage(msg);
								try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			} else if (!Utilis.checkNetwork(VirusScanFirstActivity.this) && !mIsCanceled) {
//				mHandler.sendEmptyMessage(3);// cloud scan unavailable
			}
			Message msg = new Message();
			msg.what = 1;
			msg.arg2 = 100;
			msg.obj = "";
			mHandler.sendMessage(msg);
			if (!mIsCanceled) {
				scanCompletePercent = 100;
				SharedPreferences sp = KindroidSecurityApplication.sh;
				Editor editor = sp.edit();
				long time = System.currentTimeMillis();
				editor.putLong(UtilShareprefece.LAST_SCAN_TIME, time);
				editor.putInt(UtilShareprefece.LAST_VIRUS_SUM, mScanCategory
						.get(3).getResultList().size()
						+ mScanCategory.get(2).getResultList().size()
						+ mScanCategory.get(1).getResultList().size());
				editor.commit();
				// write history
				SQLiteDBHelper dh = new SQLiteDBHelper(
						VirusScanFirstActivity.this,
						SQLiteDBHelper.historyDBName);
				SQLiteDatabase db = dh.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put("time", time);
				values.put("virus_num", mScanCategory.get(3).getResultList()
						.size()
						+ mScanCategory.get(2).getResultList().size());
				values.put("care_num", mScanCategory.get(1).getResultList()
						.size());

				db.insert(SQLiteDBHelper.historyTableName, null, values);
				List<ScanResultInfo> dangerList = mScanCategory.get(3)
						.getResultList();
				PackageManager pm = getPackageManager();
				for (ScanResultInfo sri : dangerList) {
					values = new ContentValues();
					values.put("time", time);
					values.put("label", sri.getpInfo().applicationInfo
							.loadLabel(pm).toString());
					values.put("rank", 2);
					values.put("vname", sri.getVirusName());
					values.put("pname", sri.getpInfo().packageName);
					values.put("pversion", sri.getpInfo().versionCode);
					db.insert(SQLiteDBHelper.cleanHistoryTableName, null,
							values);
				}
				List<ScanResultInfo> riskList = mScanCategory.get(2)
						.getResultList();
				for (ScanResultInfo sri : riskList) {
					values = new ContentValues();
					values.put("time", time);
					values.put("label", sri.getpInfo().applicationInfo
							.loadLabel(pm).toString());
					values.put("rank", 2);
					values.put("vname", sri.getVirusName());
					values.put("pname", sri.getpInfo().packageName);
					values.put("pversion", sri.getpInfo().versionCode);
					db.insert(SQLiteDBHelper.cleanHistoryTableName, null,
							values);
				}
				List<ScanResultInfo> warnList = mScanCategory.get(1)
						.getResultList();
				for (ScanResultInfo sri : warnList) {
					values = new ContentValues();
					values.put("time", time);
					values.put("label", sri.getpInfo().applicationInfo
							.loadLabel(pm).toString());
					values.put("rank", 1);
					values.put("vname", sri.getVirusName());
					values.put("pname", sri.getpInfo().packageName);
					values.put("pversion", sri.getpInfo().versionCode);
					db.insert(SQLiteDBHelper.cleanHistoryTableName, null,
							values);
				}
				db.close();
			}
			mHandler.sendEmptyMessage(FINISH_SCAN);
			isScanning = false;

		}
	}

}
