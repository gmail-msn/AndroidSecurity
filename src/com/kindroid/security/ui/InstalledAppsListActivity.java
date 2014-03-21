/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-07
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.util.ApkManager;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.LoadingAppsThread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InstalledAppsListActivity extends ListActivity {
	private SoftManageListAdapter listAdapter;
	private LoadingAppsThread mLodingAppsThread;

	private TextView installed_sum_tv;

	private View installed_action_linear;
	private View installed_menu_linear;
	private View installed_left_menu;
	private View installed_right_menu;	
	private boolean isLoadingData = false;
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
	
	public boolean mSizeSortDesc = true;
	private boolean mShowMenu = false;
	private boolean mNameSortDesc = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.installed_list);
		View home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent homepage = new Intent(InstalledAppsListActivity.this,
						DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
			}
		});
		installed_menu_linear = findViewById(R.id.installed_menu_linear);
		installed_left_menu = findViewById(R.id.installed_left_menu);
		installed_right_menu = findViewById(R.id.installed_right_menu);
		installed_left_menu.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(listAdapter != null){
					mSizeSortDesc = !mSizeSortDesc;
					listAdapter.sortItems(0, mSizeSortDesc);					
					listAdapter.notifyDataSetChanged();
				}
				SoftManageTabActivity.showTabMenu();
				installed_menu_linear.setVisibility(View.GONE);
				mShowMenu = false;	
			}
		});
		installed_right_menu.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub					
				Intent intent = new Intent(InstalledAppsListActivity.this, AboutAuditIconActivity.class);
				startActivity(intent);
				SoftManageTabActivity.showTabMenu();
				installed_menu_linear.setVisibility(View.GONE);
				mShowMenu = false;	
			}
		});
		
		installed_action_linear = findViewById(R.id.installed_action_linear);
		installed_action_linear.setOnClickListener(new InstallActionListener());
		//load list adapter
		//loadListAdapter();
	}
	private class InstallActionListener implements View.OnClickListener{

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
						InstalledAppsListActivity.this, R.style.softDialog);
				View view = LayoutInflater.from(
						InstalledAppsListActivity.this).inflate(
						R.layout.soft_uninstall_prompt_dialog, null);
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
						InstalledAppsListActivity.this, R.style.softDialog);
				View view = LayoutInflater.from(
						InstalledAppsListActivity.this).inflate(
						R.layout.soft_uninstall_prompt_dialog, null);
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
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(resultCode != -1){
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}		
	}
	

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(!mShowMenu){
			super.onBackPressed();
		}else{
			SoftManageTabActivity.showTabMenu();
			installed_menu_linear.setVisibility(View.GONE);
			mShowMenu = false;
		}
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU:
			if(mShowMenu){
				SoftManageTabActivity.showTabMenu();
				installed_menu_linear.setVisibility(View.GONE);
				mShowMenu = false;
			}else{
				SoftManageTabActivity.hideTabMenu();
				installed_menu_linear.setVisibility(View.VISIBLE);
				mShowMenu = true;
			}
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub		
		AppInfoForManage aifm = (AppInfoForManage) listAdapter
				.getItem(position);
		
		if(Build.VERSION.SDK_INT >= 9){
			Intent intent = new Intent();
			intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			intent.setData(Uri.fromParts("package", aifm.getPackageName(), null));
			startActivityForResult(intent, position);
			return;
		}
		if(Build.VERSION.SDK_INT == 8){
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			intent.putExtra("pkg", aifm.getPackageName());
			startActivityForResult(intent, position);
			return;
		}
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
		intent.putExtra("com.android.settings.ApplicationPkgName", aifm.getPackageName());
		startActivityForResult(intent, position);
		
	}

	private void uninstallSelected() {
		for (int i = 0; i < listAdapter.getCount(); i++) {
			AppInfoForManage aifm = (AppInfoForManage) listAdapter.getItem(i);
			if (aifm.isSelected()) {
				
				Uri pUri = Uri.parse("package:" + aifm.getPackageName());
				ApkManager.uninstallPackage(this, pUri);
			}
			
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (!isLoadingData && (listAdapter == null ||listAdapter.isEmpty())) {	
			loadListAdapter();
		}else if(isLoadingData){
			getListView().setVisibility(View.GONE);
			View loading_linear = findViewById(R.id.loading_linear);
			loading_linear.setVisibility(View.VISIBLE);
		}else{
			refreshListAdapter();
		}
		
	}
	private void refreshListAdapter(){
		isLoadingData = true;
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
		
		TextView prompt_progress_text = (TextView)findViewById(R.id.prompt_progress_text);
		prompt_progress_text.setText("0%");
		prompt_progress_text.setVisibility(View.VISIBLE);		
		new RefreshListAdapterThread().start();
		new LoadingItem().start();
	}
	private class RefreshListAdapterThread extends Thread{
		public void run(){
			refreshListState();
			handler.sendEmptyMessage(6);
		}
	}
	private void refreshListState(){		
		List<PackageInfo> mList = ApkManager.getInstalledPackages(this, false);
		List<PackageInfo> mInstalledList = new ArrayList<PackageInfo>();
		List<AppInfoForManage> mDeletedList = new ArrayList<AppInfoForManage>();
		int len = listAdapter.getCount();
		for(int i = 0; i < len; i++){
			AppInfoForManage aifm = (AppInfoForManage)listAdapter.getItem(i);
			boolean installed = false;
			for(PackageInfo pi : mList){
				if(pi.packageName.equals(aifm.getPackageName())){
					installed = true;
					mInstalledList.add(pi);
				}
			}
			if(!installed){
				mDeletedList.add(aifm);
			}
			Message msg = new Message();
			msg.what = 5;
			msg.arg1 = Double.valueOf((i * 1.0)/len*80).intValue();
			handler.sendMessage(msg);
		}
		
		for(int i = 0; i < mDeletedList.size(); i++){
			AppInfoForManage aifm = mDeletedList.get(i);
			listAdapter.delteItem(aifm);
			Message msg = new Message();
			msg.what = 5;
			msg.arg1 = Double.valueOf((i * 1.0)/mDeletedList.size()*6).intValue() + 80;
			handler.sendMessage(msg);
		}
		Message msg = new Message();
		msg.what = 5;
		msg.arg1 = 86;
		handler.sendMessage(msg);
		
		for(int i = 0; i < mInstalledList.size(); i++){
			PackageInfo pi = mInstalledList.get(i);
			mList.remove(pi);
			msg = new Message();
			msg.what = 5;
			msg.arg1 = Double.valueOf((i * 1.0)/mInstalledList.size()*6).intValue() + 86;
			handler.sendMessage(msg);
		}
		msg = new Message();
		msg.what = 5;
		msg.arg1 = 92;
		handler.sendMessage(msg);
		addListAdapter(listAdapter, mList);	
		
		msg = new Message();
		msg.what = 5;
		msg.arg1 = 100;
		handler.sendMessage(msg);
	}
	
	private void addListAdapter(SoftManageListAdapter listAdapter, List<PackageInfo> mList){
		PackageManager pm = getPackageManager();
		for(int i = 0; i < mList.size(); i++){		
			PackageInfo pInfo = mList.get(i);
			
			AppInfoForManage aifm = new AppInfoForManage();					
			aifm.setPackageName(pInfo.packageName);
			aifm.setLabel(pInfo.applicationInfo.loadLabel(pm));
			CharSequence version = pInfo.versionName;
			if (version != null) {
				aifm.setVersion(pInfo.versionName.concat(getString(R.string.softmanage_version_title)));
			}
			aifm.setIcon(pInfo.applicationInfo.loadIcon(pm));
			// èŽ·å�–å®‰è£…åŒ…å¤§å°�
			long apkSize = new File(pInfo.applicationInfo.sourceDir)
					.length();
			aifm.setSize(apkSize);			
			aifm.setFlag(0);
			//audit protected
			try{
				pInfo = pm.getPackageInfo(pInfo.packageName, PackageManager.GET_PERMISSIONS);			
				String[] perms = pInfo.requestedPermissions;
				LoadingAppsThread.setPermissions(perms, aifm, getPackageManager());
			}catch(Exception e){
				e.printStackTrace();
			}
			listAdapter.addItem(aifm);	
			Message msg = new Message();
			msg.what = 5;
			msg.arg1 = Double.valueOf((i * 1.0)/mList.size()*6).intValue() + 92;
			handler.sendMessage(msg);
		}
	}

	private void loadListAdapter() {
		isLoadingData = true;
		listAdapter = new SoftManageListAdapter(this);			
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
		
		TextView prompt_progress_text = (TextView)findViewById(R.id.prompt_progress_text);
		prompt_progress_text.setText("0%");
		prompt_progress_text.setVisibility(View.VISIBLE);
		
		new LoadingItem().start();
		mLodingAppsThread = new LoadingAppsThread(this, handler, listAdapter,
				LoadingAppsThread.LOAD_INSTALLED);
		mLodingAppsThread.start();
		
	}

	private class LoadingItem extends Thread {
		public void run() {
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

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case LoadingAppsThread.LOAD_INSTALLED:				
				if(listAdapter != null && listAdapter.getCount() > 0){
					listAdapter.sortItems(1, mNameSortDesc);
				}
				setListAdapter(listAdapter);				
				installed_sum_tv = (TextView) findViewById(R.id.installed_sum_tv);
				installed_sum_tv.setText(listAdapter.getCount() + "");				
				isLoadingData = false;
				View loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				break;
			case 1:
				loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				break;
			case LoadingAppsThread.LOAD_INSTALLED_PROGRESS:
				TextView prompt_progress_text = (TextView)findViewById(R.id.prompt_progress_text);
				prompt_progress_text.setText(Double.valueOf((msg.arg1 * 1.0)/msg.arg2*100).intValue() + "%");
				break;
			case 5:
				//update refresh progress
				prompt_progress_text = (TextView)findViewById(R.id.prompt_progress_text);
				prompt_progress_text.setText(msg.arg1 + "%");
				break;
			case 6:
				//complete refresh list state
				if(listAdapter != null && listAdapter.getCount() > 0){
					listAdapter.sortItems(1, mNameSortDesc);
				}
				setListAdapter(listAdapter);
				listAdapter.notifyDataSetChanged();
				installed_sum_tv = (TextView) findViewById(R.id.installed_sum_tv);
				installed_sum_tv.setText(listAdapter.getCount() + "");
				isLoadingData = false;
				loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				break;
			}
		}
	};

}
