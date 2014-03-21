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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.service.PkgInstallMonitorService;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.SQLiteDBHelper;
import com.kindroid.security.util.UtilShareprefece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author heli.zhao
 *
 */
public class InstallScanCleanActivity extends Activity {
	private int cleanSucc = 0;
	private VirusCleanListAdapter listAdapter;
	private static final int UNINSTALL_CODE = 1;
	private static List<Map<String, String>> virusList;
	private static List<Map<String, String>> careList;

	private TextView viruses_found_text;
	private TextView care_to_use_text;
	private TextView need_to_treat_text;
	private TextView clean_success_text;
	private CheckBox select_al_cb;
	private Dialog loadingProgressDialog;
	private int virus_last_found;
	private int care_last_found;
	
	private String mInstalledPkgName;
	private String mVirusName;
	private String mVirusDesp;
	private int mRiskLevel;
	private ListView mVirusList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.virus_scan_4);
		mInstalledPkgName = getIntent().getStringExtra(PkgInstallMonitorService.PARCEL_NAME_PKG);
		mVirusName = getIntent().getStringExtra(PkgInstallMonitorService.PARCEL_NAME_VIRUS_NAME);
		mVirusDesp = getIntent().getStringExtra(PkgInstallMonitorService.PARCEL_NAME_VIRUS_DESP);
		mRiskLevel = getIntent().getIntExtra(PkgInstallMonitorService.PARCEL_NAME_RISK_LEVEL, 0);
		
		View home_page = findViewById(R.id.home_icon);

		home_page.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
				//finish();
			}
		});
		viruses_found_text = (TextView) findViewById(R.id.viruses_found_text_1);
		care_to_use_text = (TextView) findViewById(R.id.care_to_use_text_1);
		need_to_treat_text = (TextView) findViewById(R.id.need_to_treat_text_1);
		clean_success_text = (TextView) findViewById(R.id.clean_success_text_1);

		this.mVirusList = (ListView) findViewById(R.id.virus_scan_need_treat_list);
		
		mVirusList
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
				if (!listAdapter.mOnlyStateChange) {
					listAdapter.setSelectedAllForUninstall(isChecked);
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
							InstallScanCleanActivity.this, R.style.softDialog);
					View view = LayoutInflater.from(InstallScanCleanActivity.this)
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
							InstallScanCleanActivity.this, R.style.softDialog);
					View view = LayoutInflater.from(InstallScanCleanActivity.this)
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
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		listAdapter = new VirusCleanListAdapter(this);
		
		PackageInfo pInfo = null;
		PackageManager pm = getPackageManager();
		int careToUse = 0;
		int riskToUse = 0;
		int needTreat = PkgInstallMonitorService.mRiskInstalledPkg.size();
		int cleaned = 0;
		List<AppInfoForManage> cleanedPkg = new ArrayList<AppInfoForManage>();
		for(AppInfoForManage aifm : PkgInstallMonitorService.mRiskInstalledPkg){
			if(aifm.getFlag() > 1){
				riskToUse++;
			}else{
				careToUse++;
			}
			try{
				pInfo = getPackageManager().getPackageInfo(aifm.getPackageName(), PackageManager.GET_SIGNATURES);
				aifm.setIcon(pInfo.applicationInfo.loadIcon(pm));		
				aifm.setInstalled(true);
				aifm.setLabel(pInfo.applicationInfo.loadLabel(pm));
				listAdapter.addItem(aifm);
			}catch(Exception e){
				pInfo = null;
				cleanedPkg.add(aifm);
				cleaned++;
				needTreat--;
			}			
		}
		for(AppInfoForManage aifm : cleanedPkg){
			PkgInstallMonitorService.mRiskInstalledPkg.remove(aifm);
		}
		viruses_found_text.setText(riskToUse + "");
		care_to_use_text.setText(careToUse + "");
		need_to_treat_text.setText(needTreat + "");
		clean_success_text.setText(cleaned + "");
		/*
		if(pInfo == null){
			if(mRiskLevel > 1){
				viruses_found_text.setText("1");
				care_to_use_text.setText("0");
			}else{
				viruses_found_text.setText("0");
				care_to_use_text.setText("1");
			}
			need_to_treat_text.setText("0");
			clean_success_text.setText("1");
			select_al_cb.setChecked(false);
			this.mVirusList.setAdapter(null);
			return;
		}
		if(mRiskLevel > 1){
			viruses_found_text.setText("1");
			care_to_use_text.setText("0");
		}else{
			viruses_found_text.setText("0");
			care_to_use_text.setText("1");
		}
		need_to_treat_text.setText("1");
		clean_success_text.setText("0");
		
		AppInfoForManage aifm = new AppInfoForManage();		
		aifm.setPackageName(mInstalledPkgName);
		aifm.setLabel(pInfo.applicationInfo.loadLabel(pm));
		if(mRiskLevel > 1){
			aifm.setVersion(getString(R.string.virus_risk_level));
			aifm.setDescription(mVirusName);
		}else{
			aifm.setVersion(getString(R.string.virus_warning_level));
			aifm.setDescription(getString(R.string.black_certificate));
		}
		aifm.setIcon(pInfo.applicationInfo.loadIcon(pm));		
		aifm.setInstalled(true);
		listAdapter.addItem(aifm);
		*/
		select_al_cb.setChecked(false);
		this.mVirusList.setAdapter(listAdapter);
		
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

	private void uninstallApp(String packageName) {
		Uri uri = Uri.fromParts("package", packageName, null);
		Intent i = new Intent(Intent.ACTION_DELETE, uri);
		startActivityForResult(i, UNINSTALL_CODE);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(this.listAdapter.getCount() == 0){
			quitCurrentActivity();
			return;
		}
		final Dialog promptDialog = new Dialog(
				this, R.style.softDialog);
		View view = LayoutInflater.from(this)
				.inflate(R.layout.soft_uninstall_prompt_dialog, null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);
		promptText.setText(R.string.cancel_installed_scan_clean);

		View button_ok = promptDialog.findViewById(R.id.button_ok);
		View button_cancel = promptDialog
				.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
				promptDialog.dismiss();	
				quitCurrentActivity();
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
	private void quitCurrentActivity(){
		KindroidSecurityApplication application = (KindroidSecurityApplication) getApplication();
		boolean isTrue = application.isAppIsActive();
		if(isTrue){
			finish();
		}else{
			super.onBackPressed();
			Intent intent = new Intent(this, DefenderTabMain.class);
			startActivity(intent);
			finish();
		}
		PkgInstallMonitorService.mRiskInstalledPkg.clear();
	}
	
/*
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == UNINSTALL_CODE) {
			// refreshResultInfo();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void refreshResultInfo() {
		LoadingDataThread ldt = new LoadingDataThread();
		listAdapter.clearItems();
		showProgressDialog();
		ldt.start();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (loadingProgressDialog != null) {
			loadingProgressDialog.dismiss();
		}
	}

	private void showProgressDialog() {
		if (loadingProgressDialog == null) {
			loadingProgressDialog = new Dialog(this, R.style.softDialog);
		}
		View view = LayoutInflater.from(this).inflate(
				R.layout.softmanage_prompt_dialog, null);
		loadingProgressDialog.setContentView(view);
		TextView tv = (TextView) loadingProgressDialog
				.findViewById(R.id.prompt_dialog_text);
		tv.setText(R.string.loading_virus_history_prompt_text);
		loadingProgressDialog.show();

	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				viruses_found_text.setText(String.valueOf(virus_last_found));
				clean_success_text.setText(String.valueOf(virus_last_found
						+ care_last_found - virusList.size() - careList.size()
						+ cleanSucc));
				care_to_use_text.setText(String.valueOf(care_last_found));
				need_to_treat_text.setText(String.valueOf(virusList.size()
						+ careList.size() - cleanSucc));
				SharedPreferences sp = KindroidSecurityApplication.sh;
				Editor editor = sp.edit();
				editor.putInt(UtilShareprefece.LAST_VIRUS_SUM, virusList.size()
						+ careList.size() - cleanSucc);
				editor.commit();
				select_al_cb.setChecked(false);
				listAdapter.notifyDataSetChanged();
				if (loadingProgressDialog != null) {
					loadingProgressDialog.dismiss();
				}
				break;

			}
		}
	};

	private class LoadingDataThread extends Thread {
		public void run() {
			virusList = new ArrayList<Map<String, String>>();
			careList = new ArrayList<Map<String, String>>();
			cleanSucc = 0;
			SharedPreferences sp = KindroidSecurityApplication.sh;
			long last_scan_time = sp.getLong(UtilShareprefece.LAST_SCAN_TIME,
					0L);
			if (last_scan_time != 0) {
				// process history
				SQLiteDBHelper dh = new SQLiteDBHelper(InstallScanCleanActivity.this,
						SQLiteDBHelper.historyDBName);
				SQLiteDatabase db = dh.getReadableDatabase();
				Cursor cs = db.query(SQLiteDBHelper.cleanHistoryTableName,
						null, "time=? and rank=?",
						new String[] { String.valueOf(last_scan_time), "2" },
						null, null, null);
				while (cs.moveToNext()) {
					Map<String, String> vInfo = new HashMap<String, String>();
					vInfo.put("vname", cs.getString(cs.getColumnIndex("vname")));
					vInfo.put("pname", cs.getString(cs.getColumnIndex("pname")));
					vInfo.put("pversion",
							cs.getString(cs.getColumnIndex("pversion")));
					vInfo.put("rank", cs.getString(cs.getColumnIndex("rank")));
					vInfo.put("time", cs.getString(cs.getColumnIndex("time")));
					vInfo.put("label", cs.getString(cs.getColumnIndex("label")));
					virusList.add(vInfo);
				}

				cs.close();
				cs = db.query(SQLiteDBHelper.cleanHistoryTableName, null,
						"time=? and rank=?",
						new String[] { String.valueOf(last_scan_time), "1" },
						null, null, null);
				while (cs.moveToNext()) {
					Map<String, String> vInfo = new HashMap<String, String>();
					vInfo.put("vname", cs.getString(cs.getColumnIndex("vname")));
					vInfo.put("pname", cs.getString(cs.getColumnIndex("pname")));
					vInfo.put("pversion",
							cs.getString(cs.getColumnIndex("pversion")));
					vInfo.put("rank", cs.getString(cs.getColumnIndex("rank")));
					vInfo.put("time", cs.getString(cs.getColumnIndex("time")));
					vInfo.put("label", cs.getString(cs.getColumnIndex("label")));
					careList.add(vInfo);
				}
				cs.close();
				// last history data
				cs = db.query(SQLiteDBHelper.historyTableName, null, "time=?",
						new String[] { String.valueOf(last_scan_time) }, null,
						null, null);

				if (cs != null && cs.moveToNext()) {
					virus_last_found = cs
							.getInt(cs.getColumnIndex("virus_num"));
					care_last_found = cs.getInt(cs.getColumnIndex("care_num"));
					cs.close();
				} else {
					virus_last_found = virusList.size();
					care_last_found = careList.size();
				}
				db.close();

			}
			PackageManager pm = getPackageManager();
			List<PackageInfo> packs = pm
					.getInstalledPackages(PackageManager.GET_SIGNATURES);
			int vNum = 0;
			for (Map<String, String> vInfo : virusList) {
				AppInfoForManage aifm = new AppInfoForManage();
				String packageName = vInfo.get("pname");
				String pversion = vInfo.get("pversion");
				aifm.setPackageName(packageName);
				boolean mCleaned = true;
				for (PackageInfo packageInfo : packs) {
					if (packageInfo.versionName == null)
						continue;
					if ((packageInfo.packageName
							.compareToIgnoreCase(packageName) == 0)
							&& (packageInfo.versionCode == Integer
									.parseInt(pversion))) {
						aifm.setLabel(packageInfo.applicationInfo.loadLabel(pm));
						aifm.setVersion(getString(R.string.virus_risk_level));
						aifm.setIcon(packageInfo.applicationInfo.loadIcon(pm));
						aifm.setDescription(vInfo.get("vname"));
						aifm.setInstalled(true);
						mCleaned = false;
						vNum++;
						break;
					}
				}
				if (mCleaned) {
					aifm.setLabel(vInfo.get("label"));
					aifm.setVersion(getString(R.string.virus_risk_level));
					aifm.setIcon(null);
					aifm.setDescription(vInfo.get("vname"));
					aifm.setInstalled(false);
					cleanSucc++;
					SQLiteDBHelper dh = new SQLiteDBHelper(
							InstallScanCleanActivity.this,
							SQLiteDBHelper.historyDBName);
					SQLiteDatabase db = dh.getWritableDatabase();
					db.delete(
							SQLiteDBHelper.cleanHistoryTableName,
							"pname=? and pversion=?",
							new String[] { vInfo.get("pname"),
									vInfo.get("pversion") });
					db.close();
				}
				aifm.setSelected(false);
				if (aifm.isInstalled()) {
					listAdapter.addItem(aifm);
				}
			}
			int cNum = 0;
			for (Map<String, String> vInfo : careList) {
				AppInfoForManage aifm = new AppInfoForManage();
				String packageName = vInfo.get("pname");
				String pversion = vInfo.get("pversion");
				aifm.setPackageName(packageName);
				boolean mCleaned = true;
				for (PackageInfo packageInfo : packs) {
					if (packageInfo.versionName == null)
						continue;
					if ((packageInfo.packageName
							.compareToIgnoreCase(packageName) == 0)
							&& (packageInfo.versionCode == Integer
									.parseInt(pversion))) {
						aifm.setLabel(packageInfo.applicationInfo.loadLabel(pm));
						aifm.setVersion(getString(R.string.virus_warning_level));
						aifm.setIcon(packageInfo.applicationInfo.loadIcon(pm));
						aifm.setDescription(getString(R.string.black_certificate));
						aifm.setInstalled(true);
						mCleaned = false;
						cNum++;
						break;
					}
				}
				if (mCleaned) {
					aifm.setLabel(vInfo.get("label"));
					aifm.setVersion(getString(R.string.virus_warning_level));
					aifm.setIcon(null);
					aifm.setDescription(getString(R.string.black_certificate));
					aifm.setInstalled(false);
					cleanSucc++;
					SQLiteDBHelper dh = new SQLiteDBHelper(
							InstallScanCleanActivity.this,
							SQLiteDBHelper.historyDBName);
					SQLiteDatabase db = dh.getWritableDatabase();
					db.delete(
							SQLiteDBHelper.cleanHistoryTableName,
							"pname=? and pversion=?",
							new String[] { vInfo.get("pname"),
									vInfo.get("pversion") });

					db.close();
				}
				aifm.setSelected(false);
				if (aifm.isInstalled()) {
					listAdapter.addItem(aifm);
				}

			}

			// change preference
			Editor editor = sp.edit();
			editor.putInt(UtilShareprefece.LAST_VIRUS_SUM,
					listAdapter.getCount() - cleanSucc);
			editor.commit();

			handler.sendEmptyMessage(0);
		}
	}
	*/
}
