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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.util.ApkManager;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.LoadingAppsThread;
import com.kindroid.security.util.UtilShareprefece;

public class ApksManageListActivity extends ListActivity {
	private SoftManageListAdapter listAdapter;
	private static List<AppInfoForManage> mApkList = new ArrayList<AppInfoForManage>();
	private LoadingAppsThread mLodingAppsThread;
	
	private TextView installed_sum_tv;

	private CheckBox select_al_cb;
	private View installed_action_linear;
	private TextView installed_text_tv;
	private ImageView installed_image_v;
	  
	final static int LEFT_MENU = Menu.FIRST;    
	final static int RIGHT_MENU = Menu.FIRST+1;  
	private static ApksManageListActivity instance;
	public static boolean isInstall_Prompt = true;
	public static boolean isLoadingData = false;
	private static boolean mCurrentDisplay = false;
	private static boolean sUpdateCache = false;
	
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
	
	private boolean mOnlyChangeState = false;
	private boolean mNameSortDesc = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.apkmanage_list);
		View home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent homepage = new Intent(ApksManageListActivity.this, DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
			}
		});
		instance = this;
		installed_text_tv = (TextView)findViewById(R.id.installed_text_tv);
		installed_image_v = (ImageView)findViewById(R.id.installed_image_v);
		select_al_cb = (CheckBox)findViewById(R.id.select_al_cb);
		select_al_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!mOnlyChangeState) {
					listAdapter.setSelectedAllForInstall(isChecked);
					listAdapter.mOnlyStateChange = true;
					listAdapter.notifyDataSetChanged();
					listAdapter.mOnlyStateChange = false;
				}else{
					//buttonView.setChecked(isChecked);
				}
			}
		});
		installed_action_linear = findViewById(R.id.installed_action_linear);
		installed_action_linear.setOnClickListener(new InstallActionListener());
		/*
		if(listAdapter == null || listAdapter.getCount() == 0){
			listAdapter = new SoftManageListAdapter(this);
			loadListAdapter();
		}else{
			listAdapter.mOnlyStateChange = true;
			setListAdapter(null);		
			listAdapter.mOnlyStateChange = false;
		}
		*/
		if(mApkList.size() == 0){
			listAdapter = new SoftManageListAdapter(this);
			loadListAdapter();
		}else{
			listAdapter = new SoftManageListAdapter(this);
			loadListAdapterFromBuffer();
		}
		isInstall_Prompt = true;
		
	}
	private class InstallActionListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int sum = 0;
			for(int i = 0; i < listAdapter.getCount(); i++){
				AppInfoForManage aifm = (AppInfoForManage)listAdapter.getItem(i);
				if(aifm.isSelected())
					sum++;				
			}
			
			if(sum > 0){
				final Dialog promptDialog = new Dialog(ApksManageListActivity.this, R.style.softDialog);
				View view = LayoutInflater.from(ApksManageListActivity.this).inflate(
						R.layout.soft_uninstall_prompt_dialog, null);
				promptDialog.setContentView(view);
				
				TextView promptText = (TextView)promptDialog.findViewById(R.id.prompt_text);
				if(isInstall_Prompt){
					promptText.setText(R.string.softmanage_install_tip_text);
				}else{
					promptText.setText(R.string.softmanage_delete_tip_text);
				}
				Button button_ok = (Button)promptDialog.findViewById(R.id.button_ok);
				Button button_cancel = (Button)promptDialog.findViewById(R.id.button_cancel);
				button_ok.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub							
						promptDialog.dismiss();
						if(isInstall_Prompt){
							installSelected();
						}else{
							deleteSelected();
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
			}else{
				final Dialog promptDialog = new Dialog(ApksManageListActivity.this, R.style.softDialog);
				View view = LayoutInflater.from(ApksManageListActivity.this).inflate(
						R.layout.soft_uninstall_prompt_dialog, null);
				promptDialog.setContentView(view);
				
				TextView promptText = (TextView)promptDialog.findViewById(R.id.prompt_text);
				promptText.setText(R.string.softmanage_apk_noselect);
				
				Button button_ok = (Button)promptDialog.findViewById(R.id.button_ok);
				Button button_cancel = (Button)promptDialog.findViewById(R.id.button_cancel);
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
	
	public void changeAllCb(boolean state){
		if(select_al_cb.isChecked() == state){
			return;
		}
		
		mOnlyChangeState = true;
		select_al_cb.setChecked(state);
		mOnlyChangeState = false;
		
	}
	
	public static void refreshData(){
		if(!isLoadingData){
			isLoadingData = true;
			instance.listAdapter.clearItems();
			instance.listAdapter.notifyDataSetChanged();			
			instance.loadListAdapter();
		}
	}
	
	public static void changeMenuItem(){
		if(isInstall_Prompt){
			instance.installed_text_tv.setText(R.string.softmanage_left_menu_text1);
			instance.installed_image_v.setImageResource(R.drawable.icon_shanchu);
			
			isInstall_Prompt = false;
		}else{
			instance.installed_text_tv.setText(R.string.softmanage_left_menu_text);
			instance.installed_image_v.setImageResource(R.drawable.icon_anzhuang);
			
			isInstall_Prompt = true;
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		mCurrentDisplay = false;		
	}
	
	private void deleteSelected(){
		List<AppInfoForManage> listApps = new ArrayList<AppInfoForManage>();
		for(int i = 0; i < listAdapter.getCount(); i++){
			AppInfoForManage aifm = (AppInfoForManage)listAdapter.getItem(i);
			if(aifm.isSelected()){
				
				listApps.add(aifm);
			}
			
		}
		for(AppInfoForManage aifm : listApps){
			File apkFile = new File(aifm.getPackagePath());
			if(apkFile.exists()){
				apkFile.delete();				
			}
			listAdapter.delteItem(aifm);
		}
		installed_sum_tv = (TextView) findViewById(R.id.installed_sum_tv);
		installed_sum_tv.setText(listAdapter.getCount() + "");
		listAdapter.mOnlyStateChange = true;
		listAdapter.notifyDataSetChanged();
		listAdapter.mOnlyStateChange = false;
	}
	
	private void installSelected(){
		for(int i = 0; i < listAdapter.getCount(); i++){
			AppInfoForManage aifm = (AppInfoForManage)listAdapter.getItem(i);
			if(aifm.isSelected()){
				ApkManager.installApk(this, Uri.fromFile(new File(aifm.getPackagePath())));				
			}			
		}		
	}
	
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		select_al_cb.setChecked(false);
		if(!isLoadingData){
			refreshListState();
		}else if(isLoadingData){
			View loading_linear = findViewById(R.id.loading_linear);
			loading_linear.setVisibility(View.VISIBLE);
			getListView().setVisibility(View.GONE);
		}
		mCurrentDisplay = true;
		
	}
	private void refreshListState(){
//		this.getListView().setClickable(false);
		for(int i = 0; i < listAdapter.getCount(); i++){
			AppInfoForManage aifm = (AppInfoForManage)listAdapter.getItem(i);
			PackageManager pm = getPackageManager();
			try {
				PackageInfo pi = pm.getPackageInfo(
						aifm.getPackageName(),
						PackageManager.GET_ACTIVITIES);
				if (pi != null)
					aifm.setInstalled(true);
			} catch (NameNotFoundException e) {
				aifm.setInstalled(false);
			}
//			aifm.setSelected(false);
		}
		listAdapter.mOnlyStateChange = true;
		setListAdapter(listAdapter);
		listAdapter.notifyDataSetChanged();
		listAdapter.mOnlyStateChange = false;
//		this.getListView().setClickable(true);
		installed_sum_tv = (TextView) findViewById(R.id.installed_sum_tv);
		installed_sum_tv.setText(listAdapter.getCount() + "");
		installed_sum_tv.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mCurrentDisplay = false;
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {		
		if(listAdapter != null && !isLoadingData){
			final Dialog promptDialog = new Dialog(ApksManageListActivity.this, R.style.softDialog);
			View view = LayoutInflater.from(ApksManageListActivity.this).inflate(
					R.layout.apk_install_delete_prompt, null);
			promptDialog.setContentView(view);
			View install_tv = promptDialog.findViewById(R.id.apk_install_tv);
			View delete_tv = promptDialog.findViewById(R.id.apk_delete_tv);
			final AppInfoForManage aifm = (AppInfoForManage)listAdapter.getItem(position);
			install_tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog.dismiss();
					ApkManager.installApk(ApksManageListActivity.this, Uri.fromFile(new File(aifm.getPackagePath())));	
				}
			});
			delete_tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					File apkFile = new File(aifm.getPackagePath());
					if(apkFile.exists()){
						apkFile.delete();
						listAdapter.delteItem(aifm);
						SharedPreferences sp = KindroidSecurityApplication.sh;
						Editor editor = sp.edit();
						editor.putInt(UtilShareprefece.LAST_APK_SUM,listAdapter.getCount());
						editor.commit();
					}
					promptDialog.dismiss();
					listAdapter.mOnlyStateChange = true;
					listAdapter.notifyDataSetChanged();
					listAdapter.mOnlyStateChange = false;
					installed_sum_tv = (TextView) findViewById(R.id.installed_sum_tv);
					installed_sum_tv.setText(listAdapter.getCount() + "");
				}
			});
			View close_v = promptDialog.findViewById(R.id.apk_dialog_close);
			close_v.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog.dismiss();
				}
			});
			promptDialog.show();
		}
		
	}
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(SoftManageTabActivity.isShowApkmanageMenu){
			SoftManageTabActivity.hideApkmanageMenu();
		}else{
			super.onBackPressed();
		}
		
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU:
			if(SoftManageTabActivity.isShowApkmanageMenu){
				SoftManageTabActivity.hideApkmanageMenu();
			}else{
				SoftManageTabActivity.showApkmanageMenu();
			}
			break;
		
		}
		return super.onKeyDown(keyCode, event);
	}
	private void loadListAdapterFromBuffer(){
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
		
		TextView tv = (TextView)findViewById(R.id.prompt_dialog_text);
		tv.setText(R.string.softmanage_scan_apk_text);		
		
		isLoadingData = true;
		new LoadingItem().start();
		for(AppInfoForManage aifm : mApkList){
			aifm.setSelected(false);
			listAdapter.addItem(aifm);
		}
		handler.sendEmptyMessage(6);
	}

	private void loadListAdapter(){
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, R.string.sdcard_noexist, Toast.LENGTH_LONG).show();
			return;
		}
		getListView().setVisibility(View.GONE);
		
		mLodingAppsThread = new LoadingAppsThread(this, handler, listAdapter, LoadingAppsThread.LOAD_APKMANAGE);
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
		
		TextView tv = (TextView)findViewById(R.id.prompt_dialog_text);
		tv.setText(R.string.softmanage_scan_apk_text);		
		
		isLoadingData = true;
		mLodingAppsThread.start();
		new LoadingItem().start();
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
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			
			switch(msg.what){
				case LoadingAppsThread.LOAD_APKMANAGE:					
					isLoadingData = false;
					installed_sum_tv = (TextView) findViewById(R.id.installed_sum_tv);
					installed_sum_tv.setText(listAdapter.getCount() + "");
					installed_sum_tv.setVisibility(View.VISIBLE);
					select_al_cb.setChecked(false);
					if(listAdapter != null && listAdapter.getCount() > 0){
						listAdapter.sortItems(1, mNameSortDesc);
					}
					listAdapter.mOnlyStateChange = true;
					setListAdapter(listAdapter);
					listAdapter.notifyDataSetChanged();
					listAdapter.mOnlyStateChange = false;
					mApkList.clear();
					for(int i = 0; i < listAdapter.getCount(); i++){
						mApkList.add((AppInfoForManage)listAdapter.getItem(i));
					}
					
					View loading_linear = findViewById(R.id.loading_linear);
					loading_linear.setVisibility(View.GONE);
					getListView().setVisibility(View.VISIBLE);
					
					break;
				case 3:
					if(msg.arg1 > 100){
						installed_sum_tv = (TextView) findViewById(R.id.installed_sum_tv);
						installed_sum_tv.setText(listAdapter.getCount() + "");
						select_al_cb.setChecked(false);
						if(listAdapter != null && listAdapter.getCount() > 0){
							listAdapter.sortItems(1, mNameSortDesc);
						}
						listAdapter.mOnlyStateChange = true;
						setListAdapter(listAdapter);
						listAdapter.mOnlyStateChange = false;
						mApkList.clear();
						for(int i = 0; i < listAdapter.getCount(); i++){
							mApkList.add((AppInfoForManage)listAdapter.getItem(i));
						}
						
					}
					break;
				case 6:
					isLoadingData = false;
					installed_sum_tv = (TextView) findViewById(R.id.installed_sum_tv);
					installed_sum_tv.setText(listAdapter.getCount() + "");
					installed_sum_tv.setVisibility(View.VISIBLE);
					select_al_cb.setChecked(false);
					if(listAdapter != null && listAdapter.getCount() > 0){
						listAdapter.sortItems(1, mNameSortDesc);
					}
					listAdapter.mOnlyStateChange = true;
					setListAdapter(listAdapter);
					listAdapter.notifyDataSetChanged();
					listAdapter.mOnlyStateChange = false;
					
					loading_linear = findViewById(R.id.loading_linear);
					loading_linear.setVisibility(View.GONE);
					getListView().setVisibility(View.VISIBLE);
					break;
			}
		}
	};
	
}
