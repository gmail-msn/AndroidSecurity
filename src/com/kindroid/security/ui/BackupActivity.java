/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.util.ApkManager;
import com.kindroid.security.util.BackupDBHelper;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.Utilis;

public class BackupActivity extends Activity {
	private boolean mContactsBackup = false;
	private boolean mSmsBackup = false;
	private boolean mApplistBackup = false;
	
	private boolean mBackupContacts = false;
	private boolean mBackupSms = false;
	private boolean mBackupApplist = false;
	
	private boolean mContactsBackSucc = false;
	private boolean mSmsBackSucc = false;
	private boolean mApplistBackSucc = false;

	private static boolean isBackingContacts = false;
	private static boolean isBackingSms = false;
	private static boolean isBackingApp = false;

	private static boolean mCanCancel = true;

	public static final String CONTACTS_BACK_FILE = "contacts.bak";
	public static final String SMS_BACK_FILE = "sms.bak";
	public static final String APPLIST_BACKUP_FILE = "applist.bak";
	public static final String LAST_BACK_CONTACTS_TIME = "last_back_contacts_time";

	private int contactsExist = 0;
	private int smsExist = 0;
	private int appsExist = 0;

	private CheckBox backup_contacts_cb;
	private CheckBox backup_sms_cb;
	private CheckBox backup_applist_cb;
	private CheckBox select_al_cb;
	private View backup_action_linear;
	private View backup_cancel_linear;
	private View backup_progress_linear;
	private ProgressBar backup_progress;
	private TextView backuping_text;
	private TextView backup_progress_text;

	private BackupThread mBackupThread;
	
	private View home_page;
	
	private boolean mOnlyChangeState = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backup);
		home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setResult(Activity.RESULT_OK);
				finish();
			}
		});
		View privacy_text = findViewById(R.id.privacy_text);		
		privacy_text.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(BackupActivity.this,
						PrivacyActivity.class);
				startActivity(intent);

			}
		});
		backup_contacts_cb = (CheckBox) findViewById(R.id.backup_contacts_cb);

		backup_contacts_cb
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						mContactsBackup = isChecked;
						if(!isChecked){
							changeAllCbState(isChecked);
						}else{
							if(backup_applist_cb.isChecked() && backup_sms_cb.isChecked()){
								changeAllCbState(isChecked);
							}
						}
					}

				});
		backup_sms_cb = (CheckBox) findViewById(R.id.backup_sms_cb);

		backup_sms_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				mSmsBackup = isChecked;
				if(!isChecked){
					changeAllCbState(isChecked);
				}else{
					if(backup_applist_cb.isChecked() && backup_contacts_cb.isChecked()){
						changeAllCbState(isChecked);
					}
				}
			}

		});
		backup_applist_cb = (CheckBox) findViewById(R.id.backup_applist_cb);

		backup_applist_cb
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						mApplistBackup = isChecked;
						if(!isChecked){
							changeAllCbState(isChecked);
						}else{
							if(backup_sms_cb.isChecked() && backup_contacts_cb.isChecked()){
								changeAllCbState(isChecked);
							}
						}
					}

				});
		select_al_cb = (CheckBox) findViewById(R.id.select_al_cb);
		select_al_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(!mOnlyChangeState){
					backup_contacts_cb.setChecked(isChecked);
					backup_sms_cb.setChecked(isChecked);
					backup_applist_cb.setChecked(isChecked);
				}
			}

		});
		backup_action_linear = findViewById(R.id.backup_action_linear);
		backup_action_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mContactsBackup && !mSmsBackup && !mApplistBackup) {
					Toast.makeText(BackupActivity.this,
							R.string.backup_select_content_prompt,
							Toast.LENGTH_LONG).show();
					return;
				}
				if (mContactsBackup && (contactsExist == 0)) {
					Toast.makeText(
							BackupActivity.this,
							getString(R.string.backup_contacts_text)
									+ getString(R.string.backup_has_no_date_to_back_prompt_text),
							Toast.LENGTH_LONG).show();
					return;
				}
				if (mSmsBackup && (smsExist == 0)) {
					Toast.makeText(
							BackupActivity.this,
							getString(R.string.backup_sms_text)
									+ getString(R.string.backup_has_no_date_to_back_prompt_text),
							Toast.LENGTH_LONG).show();
					return;
				}
				if (mApplistBackup && (appsExist == 0)) {
					Toast.makeText(
							BackupActivity.this,
							getString(R.string.backup_software_list_text)
									+ getString(R.string.backup_has_no_date_to_back_prompt_text),
							Toast.LENGTH_LONG).show();
					return;
				}
				promptBackup();
			}
		});
		backup_cancel_linear = findViewById(R.id.backup_cancel_linear);
		backup_progress_linear = findViewById(R.id.backup_progress_linear);
		View backup_applist_out_linear = findViewById(R.id.backup_applist_out_linear);
		View backup_contacts_out_linear = findViewById(R.id.backup_contacts_out_linear);
		View backup_sms_out_linear = findViewById(R.id.backup_sms_out_linear);
		backup_sms_out_linear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				backup_sms_cb.setChecked(!backup_sms_cb.isChecked());
			}
		});
		backup_contacts_out_linear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				backup_contacts_cb.setChecked(!backup_contacts_cb.isChecked());
			}
		});
		backup_applist_out_linear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				backup_applist_cb.setChecked(!backup_applist_cb.isChecked());
			}
		});
		
	}
	private void changeAllCbState(boolean state){
		mOnlyChangeState = true;
		select_al_cb.setChecked(state);
		mOnlyChangeState = false;
	}
	private void promptBackup(){
		final Dialog promptDialog = new Dialog(this, R.style.Theme_CustomDialog);
		// promptDialog.setContentView(R.layout.soft_uninstall_prompt_dialog);
		View view = LayoutInflater.from(this).inflate(
				R.layout.restore_prompt_dialog, null);
		promptDialog.setContentView(view);

		// 更改提示文字
		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);

		promptText.setText(R.string.backup_start_prompt_text);

		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				promptDialog.dismiss();
			}
		});
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startBackup();
				promptDialog.dismiss();
			}
		});
		promptDialog.show();
	}
	private void startBackup() {
		mBackupContacts = mContactsBackup;
		mBackupSms = mSmsBackup;
		mBackupApplist = mApplistBackup;
		if(!Utilis.checkNetwork(this)){
			Toast.makeText(this, R.string.bakcup_remote_network_unabailable_text, Toast.LENGTH_LONG).show();
			return;
		}
		backup_action_linear.setVisibility(View.GONE);
		backup_cancel_linear.setVisibility(View.VISIBLE);
		mBackupThread = new BackupThread();
		backup_cancel_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCanCancel) {
					mBackupThread.pauseBackup();
					getCancelPromptDialog();
					mBackupThread.continueBackup();
				}
			}
		});
		backup_progress_linear.setVisibility(View.VISIBLE);
		backup_progress = (ProgressBar) findViewById(R.id.backup_progress);
		backuping_text = (TextView) findViewById(R.id.backuping_text);
		backup_progress_text = (TextView) findViewById(R.id.backup_progress_text);
		mCanCancel = true;
		home_page.setVisibility(View.GONE);
		mBackupThread.start();
	}

	private void getCancelPromptDialog() {
		final Dialog promptDialog = new Dialog(BackupActivity.this,
				R.style.softDialog);
		View view = LayoutInflater.from(BackupActivity.this).inflate(
				R.layout.soft_uninstall_prompt_dialog, null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);

		promptText.setText(R.string.cancel_backup_text);

		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				promptDialog.dismiss();
			}
		});
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBackupThread.cancelBackup();
				promptDialog.dismiss();
			}
		});
		promptDialog.show();
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// start contacts backup
			case 0:
				backuping_text.setText(getString(R.string.backuping_text)
						+ " " + getString(R.string.backup_contacts_text));
				backup_progress_text.setText("(0%)");
				backup_progress.setProgress(0);
				break;
			//update progress
			case 1:
				backup_progress.setProgress(msg.arg1);
				backup_progress_text.setText("(" + msg.arg1 + "%)");
				break;
			//contacts backup success
			case 2:	
				
				Toast.makeText(BackupActivity.this, getString(R.string.backup_contacts_text)
						+ getString(R.string.backup_completed_confirm_prompt_text), Toast.LENGTH_LONG).show();
				break;
			//contacts backup fail
			case 3:
				
				Toast.makeText(BackupActivity.this, getString(R.string.backup_contacts_text)
						+ getString(R.string.backup_error_prompt_text), Toast.LENGTH_LONG).show();
				break;
			//start sms backup
			case 4:
				backuping_text.setText(getString(R.string.backuping_text)
						+ " " + getString(R.string.backup_sms_text));
				backup_progress_text.setText("(0%)");
				backup_progress.setProgress(0);
				break;
			//update sms backup progress
			case 5:
				backup_progress.setProgress(msg.arg1);
				backup_progress_text.setText("(" + msg.arg1 + "%)");
				break;
			//backup sms success
			case 6:
				
				Toast.makeText(BackupActivity.this, getString(R.string.backup_sms_text)
						+ getString(R.string.backup_completed_confirm_prompt_text), Toast.LENGTH_LONG).show();
				break;
			//backup sms fail
			case 7:
				
				Toast.makeText(BackupActivity.this, getString(R.string.backup_sms_text)
						+ getString(R.string.backup_error_prompt_text), Toast.LENGTH_LONG).show();
				break;
			//start backup applist
			case 8:
				backuping_text.setText(getString(R.string.backuping_text)
						+ " " + getString(R.string.backup_software_list_text));
				backup_progress_text.setText("(0%)");
				backup_progress.setProgress(0);
				break;
			//update applist backup progress
			case 9:
				backup_progress.setProgress(msg.arg1);
				backup_progress_text.setText("(" + msg.arg1 + "%)");
				break;
			//backup applist success
			case 10:
				Toast.makeText(BackupActivity.this, getString(R.string.backup_software_list_text)
						+ getString(R.string.backup_completed_confirm_prompt_text), Toast.LENGTH_LONG).show();
				break;
			//backup applist fail
			case 11:
				Toast.makeText(BackupActivity.this, getString(R.string.backup_software_list_text)
						+ getString(R.string.backup_error_prompt_text), Toast.LENGTH_LONG).show();
				break;
			//backup canceled
			case 23:
				Toast.makeText(BackupActivity.this, R.string.backup_cancel_prompt_text, Toast.LENGTH_LONG).show();
				break;
				
			//complete backup, refresh ui
			case 60:
				backup_progress_linear.setVisibility(View.GONE);
				backup_cancel_linear.setVisibility(View.GONE);
				backup_action_linear.setVisibility(View.VISIBLE);
				StringBuilder sb = new StringBuilder();
				StringBuilder sb1 = new StringBuilder();
				//uncheck checked item				
				if(backup_contacts_cb.isChecked()){
					backup_contacts_cb.setChecked(false);
					if(mContactsBackSucc){
						sb.append(getString((R.string.backup_contacts_text))).append(", ");
					}else{
						sb1.append(getString((R.string.backup_contacts_text))).append(", ");
					}
				}
				if(backup_sms_cb.isChecked()){
					backup_sms_cb.setChecked(false);
					if(mSmsBackSucc){
						sb.append(getString((R.string.backup_sms_text))).append(", ");
					}else{
						sb1.append(getString((R.string.backup_sms_text))).append(", ");
					}
				}
				if(backup_applist_cb.isChecked()){
					backup_applist_cb.setChecked(false);
					if(mApplistBackSucc){
						sb.append(getString((R.string.backup_software_list_text))).append(", ");
					}else{
						sb1.append(getString((R.string.backup_software_list_text))).append(", ");
					}
				}
				if(select_al_cb.isChecked()){
					select_al_cb.setChecked(false);					
				}
				if(sb.length() > 0){
					sb.append(getString(R.string.backup_completed_confirm_prompt_text));
				}
				if(sb1.length() > 0){
					sb1.append(getString(R.string.backup_error_prompt_text));
				}
				sb.append("\n").append(sb1);
				Toast.makeText(BackupActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
				home_page.setVisibility(View.VISIBLE);
				break;
				

			}
		}
	};
	/**
	 * record backup log
	 * @param time backup time;
	 * @param flag flag of success or fail for backup
	 * @param type backup type of contacts or sms or applist
	 * @param num number of backed info
	 * @param traf net traffic for upload backup file
	 */
	private void backLog(long time, int flag, int type, int num, int traf){
		BackupDBHelper dh = new BackupDBHelper(this, BackupDBHelper.logDBName);
		SQLiteDatabase db = dh.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(BackupDBHelper.COLUMN_TIME, time);
		values.put(BackupDBHelper.COLUMN_FLAG, flag);
		values.put(BackupDBHelper.COLUMN_TYPE, type);		
		values.put(BackupDBHelper.COLUMN_NUM, num);
		values.put(BackupDBHelper.COLUMN_NTRAF, traf);		

		db.insert(BackupDBHelper.logTableName, null, values);
		db.close();
	}

	private class BackupThread extends Thread {
		private boolean mPause = false;
		private boolean mCancelBackup = false;

		BackupThread() {
			mPause = false;
			mCancelBackup = false;
		}

		void pauseBackup() {
			mPause = true;
		}

		void continueBackup() {
			mPause = false;
		}

		void cancelBackup() {
			mCancelBackup = true;
		}

		public void run() {
			if (mBackupContacts && !mCancelBackup) {
				isBackingContacts = true;
				mHandler.sendEmptyMessage(0);
				int ret = contactsBackup();
				if (ret > 0) {
					// upload backup file
					int traf = remoteContactsBackup(ret);
					if(traf > 0){
						// record success log
						long bTime = System.currentTimeMillis();
						backLog(bTime, 1, 1, ret, traf);
						SharedPreferences sp = KindroidSecurityApplication.sh;
						Editor editor = sp.edit();
						long time = System.currentTimeMillis();
						editor.putLong(LAST_BACK_CONTACTS_TIME, bTime);
						editor.commit();
						mContactsBackSucc = true;
						
					}else{
						// record fail log
						backLog(System.currentTimeMillis(), 0, 1, ret, 0);
						mContactsBackSucc = false;
						
					}
				} else {
					// record fail log
					backLog(System.currentTimeMillis(), 0, 1, ret, 0);
					mContactsBackSucc = false;
					
				}
				mCanCancel = true;
				isBackingContacts = false;
			}
			if (mBackupSms && !mCancelBackup) {
				isBackingSms = true;
				mHandler.sendEmptyMessage(4);
				int ret = smsBackup();
				if (ret > 0) {
					// upload backup file
					int traf = remoteSmsBackup(ret);
					if(traf > 0){
						// record success log
						backLog(System.currentTimeMillis(), 1, 2, ret, traf);
						mSmsBackSucc = true;
						
					}else{
						// record fail log
						backLog(System.currentTimeMillis(), 0, 2, ret, 0);
						mSmsBackSucc = false;
						
					}
					
				}else{
					// record fail log
					backLog(System.currentTimeMillis(), 0, 2, ret, 0);
					mSmsBackSucc = false;
					
				}
				mCanCancel = true;
				isBackingSms = false;
			}
			if (mBackupApplist && !mCancelBackup) {
				isBackingApp = true;
				mHandler.sendEmptyMessage(8);
				int ret = appBackup();
				if(ret > 0){
					// upload backup file
					int traf = remoteAppBackup(ret);
					if(traf > 0){
						// record success log
						backLog(System.currentTimeMillis(), 1, 3, ret, traf);
						mApplistBackSucc = true;
						
					}else{
						// record fail log
						backLog(System.currentTimeMillis(), 0, 3, ret, 0);
						mApplistBackSucc = false;
						
					}
				}else{
					// record fail log
					backLog(System.currentTimeMillis(), 0, 3, ret, 0);
					mApplistBackSucc = false;
					
				}
				mCanCancel = true;
				isBackingApp = false;
			}
			mHandler.sendEmptyMessage(60);
		}
		private int remoteAppBackup(int entriesCount){
			File backupPath = getDir("backup", Context.MODE_PRIVATE);		
			String fileName = APPLIST_BACKUP_FILE;
			File dbFile = new File(backupPath, fileName);
			if(!dbFile.exists() || mCancelBackup){
				return -2;
			}
			String mToken = Utilis.getToken();
			if(mToken == null){
				return -2;
			}
			Message msg = new Message();
			msg.what = 9;
			msg.arg1 = 72;
			mHandler.sendMessage(msg);
			if (!Utilis.checkNetwork(BackupActivity.this)) {
				return -2;
			}
			FileInputStream fis = null;
			HttpURLConnection hc = null;
			while (mPause) {

			}
			if (mCancelBackup) {
				return -2;
			}
			try{
				URL url = new URL(Constant.REMOTE_BACKUP_URL);
				hc =  (HttpURLConnection) url.openConnection();
			    String[] props = new String[] {"token", "type", "entriesCount"}; // 字段名 
			    String[] values = new String[] {mToken, "3", entriesCount+""}; // 字段值 
			    fis = new FileInputStream(dbFile);   // 文件内容 
			    String BOUNDARY = "---------------------------7d4a6d158c9"; // 分隔符 
			    StringBuffer sb = new StringBuffer(); 
			    for(int i=0; i < props.length; i++) {
				    sb = sb.append("--"); 
				    sb = sb.append(BOUNDARY); 
				    sb = sb.append("\r\n"); 
				    sb = sb.append("Content-Disposition: form-data; name=\""+ props[i] + "\"\r\n\r\n"); 
				    sb = sb.append(values[i]);
				    sb = sb.append("\r\n");
			    } 
			    String propsFile = "file";		    
			    sb = sb.append("--"); 
			    sb = sb.append(BOUNDARY); 
			    sb = sb.append("\r\n"); 
			    sb = sb.append("Content-Disposition: form-data; name=\"" + propsFile + "\"; filename=\"" + fileName + "\"\r\n"); 
			    sb = sb.append("Content-Type: application/octet-stream\r\n\r\n"); 
			    byte[] data = sb.toString().getBytes(); 
			    byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes(); 
			    
			    hc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY); 
			    hc.setDoOutput(true);
			    OutputStream output = hc.getOutputStream(); 
			    output.write(data);
			    msg = new Message();
				msg.what = 9;
				msg.arg1 = 75;
				mHandler.sendMessage(msg);
			    byte[] buffer = new byte[1024];
			    long fLen = dbFile.length();
			    int mount = 0;
			    int amountRead = fis.read(buffer);
				while (amountRead != -1) {				
					output.write(buffer, 0, amountRead);
					mount = mount + amountRead;
					msg = new Message();
					msg.what = 9;
					msg.arg1 = Double.valueOf((mount * 1.0 / fLen) * 25).intValue() + 75;
					mHandler.sendMessage(msg);
					amountRead = fis.read(buffer);
				}
				fis.close();

			    output.write(end_data); 
			    output.flush();
			    output.close();
			    BufferedReader reader = new BufferedReader(new InputStreamReader(hc.getInputStream(), "utf-8"));				
				
				reader.close();
				
				hc.disconnect();
				return mount;
			}catch(Exception e){
				e.printStackTrace();
				return -3;
			}finally{
				if(fis != null){
					try{
						fis.close();
						dbFile.delete();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				if(hc != null){
					hc.disconnect();
				}
			}
			
		}
		
		private int appBackup(){
			List<PackageInfo> installedApps = ApkManager.getInstalledPackages(BackupActivity.this, false);
			if(installedApps.size() == 0){
				return 0;
			}			
			File backupPath = getDir("backup", Context.MODE_PRIVATE);
			if (!backupPath.exists()){
				try{
					backupPath.mkdirs();
				}catch(Exception e){
					e.printStackTrace();					
					return -1;
				}
			}
			String backRootPath = backupPath.getAbsolutePath();
			String fileName = APPLIST_BACKUP_FILE;
			
			File dbFile = new File(backRootPath, fileName);
			if(dbFile.exists()){
				boolean del = dbFile.delete();
				if(!del){
					return -1;
				}
			}
			while (mPause) {

			}
			if (mCancelBackup) {
				return -2;
			}
			SQLiteDatabase localSQLiteDatabase = SQLiteDatabase.openDatabase(
					dbFile.getAbsolutePath(), null,
					SQLiteDatabase.CREATE_IF_NECESSARY);	

			String sql = "create table applist(packageName TEXT, label TEXT, version TEXT, size TEXT)";
			try{
				localSQLiteDatabase.execSQL(sql);
			}catch(Exception e){
				e.printStackTrace();
				if(localSQLiteDatabase != null){
					try{
						localSQLiteDatabase.close();
					}catch(Exception e2){
						e2.printStackTrace();
					}
				}
				return -1;
			}
			
			PackageManager pm = getPackageManager();
			int c = 0;
			while (mPause) {

			}
			if (mCancelBackup) {
				if(localSQLiteDatabase != null){
					try{
						localSQLiteDatabase.close();
					}catch(Exception e2){
						e2.printStackTrace();
					}
				}
				return -2;
			}
			for(PackageInfo pInfo : installedApps){
				if(mCancelBackup)
					break;
				ContentValues cv = new ContentValues();
				cv.put("packageName", pInfo.packageName);
				String label = pInfo.applicationInfo.loadLabel(pm).toString();
				if(label != null)
					cv.put("label", label);
				else
					cv.put("label", pInfo.packageName);
				if(pInfo.versionName != null)
					cv.put("version", pInfo.versionName);
				else
					cv.put("version", "Unkown");
				long apkSize = new File(pInfo.applicationInfo.sourceDir).length();
				cv.put("size", apkSize);

				try {
					localSQLiteDatabase.insertOrThrow("applist", null, cv);
					c++;
				} catch (Exception e) {
					e.printStackTrace();
				}				
				Message msg = new Message();
				msg.what = 9;
				msg.arg1 = Double.valueOf((c * 1.0 / installedApps.size()) * 70).intValue();
				mHandler.sendMessage(msg);
				
			}
			try{
				localSQLiteDatabase.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			while (mPause) {

			}
			if (mCancelBackup) {
				return -2;
			}
			return c;
		}
		private int remoteSmsBackup(int entriesCount){
			File backupPath = getDir("backup", Context.MODE_PRIVATE);		
			String fileName = SMS_BACK_FILE;
			File dbFile = new File(backupPath, fileName);
			if(!dbFile.exists() || mCancelBackup){
				return -2;
			}
			String mToken = Utilis.getToken();
			if(mToken == null){
				return -2;
			}
			Message msg = new Message();
			msg.what = 5;
			msg.arg1 = 72;
			mHandler.sendMessage(msg);
			if (!Utilis.checkNetwork(BackupActivity.this)) {
				return -2;
			}
			FileInputStream fis = null;
			HttpURLConnection hc = null;
			while (mPause) {

			}
			if (mCancelBackup) {
				return -2;
			}
			mCanCancel = false;
			try{
				URL url = new URL(Constant.REMOTE_BACKUP_URL);
				hc =  (HttpURLConnection) url.openConnection();
			    String[] props = new String[] {"token", "type", "entriesCount"}; 
			    String[] values = new String[] {mToken, "2", entriesCount+""}; 
			    fis = new FileInputStream(dbFile);  
			    String BOUNDARY = "---------------------------7d4a6d158c9"; 
			    StringBuffer sb = new StringBuffer(); 
			    for(int i=0; i < props.length; i++) {
				    sb = sb.append("--"); 
				    sb = sb.append(BOUNDARY); 
				    sb = sb.append("\r\n"); 
				    sb = sb.append("Content-Disposition: form-data; name=\""+ props[i] + "\"\r\n\r\n"); 
				    sb = sb.append(values[i]);
				    sb = sb.append("\r\n");
			    } 
			    String propsFile = "file";		    
			    sb = sb.append("--"); 
			    sb = sb.append(BOUNDARY); 
			    sb = sb.append("\r\n"); 
			    sb = sb.append("Content-Disposition: form-data; name=\"" + propsFile + "\"; filename=\"" + fileName + "\"\r\n"); 
			    sb = sb.append("Content-Type: application/octet-stream\r\n\r\n"); 
			    
			    byte[] data = sb.toString().getBytes(); 
			    byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes(); 
			    hc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY); 
			    hc.setDoOutput(true);
			    OutputStream output = hc.getOutputStream(); 
			    output.write(data); 
			    msg = new Message();
				msg.what = 5;
				msg.arg1 = 75;
				mHandler.sendMessage(msg);
			    byte[] buffer = new byte[1024];
			    long fLen = dbFile.length();
			    int mount = 0;
			    int amountRead = fis.read(buffer);
				while (amountRead != -1) {				
					output.write(buffer, 0, amountRead);
					mount = mount + amountRead;
					msg = new Message();
					msg.what = 5;
					msg.arg1 = Double.valueOf((mount * 1.0 / fLen) * 25).intValue() + 75;
					mHandler.sendMessage(msg);
					amountRead = fis.read(buffer);
				}
				fis.close();

			    output.write(end_data); 
			    output.flush();
			    output.close();
			    BufferedReader reader = new BufferedReader(new InputStreamReader(hc.getInputStream(), "utf-8"));
				
				reader.close();
				hc.disconnect();
				return mount;
			}catch(Exception e){
				e.printStackTrace();
				return -3;
			}finally{
				if(fis != null){
					try{
						fis.close();
						dbFile.delete();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				if(hc != null){
					hc.disconnect();
				}
			}
			
		}
		private int smsBackup(){		
			File backupPath = getDir("backup", Context.MODE_PRIVATE);
			if (!backupPath.exists()){
				try{
					backupPath.mkdirs();
				}catch(Exception e){
					e.printStackTrace();					
					return -1;
				}
			}
			
			String fileName = SMS_BACK_FILE;
			Uri localUri = Uri.parse("content://sms");
			Cursor localCursor = getContentResolver().query(localUri, null, null,
					null, null);
			if (localCursor == null || localCursor.getCount() <= 0) {
				return 0;
			}
					
			while (mPause) {

			}
			if (mCancelBackup) {
				return -2;
			}
			String backRootPath = backupPath.getAbsolutePath();
			File dbFile = new File(backRootPath, fileName);
			
			if(dbFile.exists()){
				boolean del = dbFile.delete();
				if(!del){
					return -1;
				}
			}
			SQLiteDatabase localSQLiteDatabase = SQLiteDatabase.openDatabase(
					dbFile.getAbsolutePath(), null, SQLiteDatabase.CREATE_IF_NECESSARY);
			while (mPause) {

			}
			if (mCancelBackup) {
				if(localSQLiteDatabase != null){
					try{
						localSQLiteDatabase.close();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				return -2;
			}
			String sql1 = "create table smstable (_id INTEGER primary key,thread_id INTEGER,address TEXT,person INTEGER,date INTEGER,protocol INTEGER,read INTEGER,status INTEGER,type INTEGER,reply_path_present INTEGER,subject TEXT,body TEXT,service_center TEXT,locked INTEGER,error_code INTEGER,seen INTEGER)";
			localSQLiteDatabase.execSQL(sql1);

			String sql2 = "create table threadtable (_id INTEGER primary key,date INTEGER,message_count INTEGER,recipient_ids INTEGER,snippet TEXT,snippet_cs INTEGER,read INTEGER,type INTEGER,error INTEGER,has_attachmen INTEGER)";
			localSQLiteDatabase.execSQL(sql2);
			while (mPause) {

			}
			if (mCancelBackup) {
				if(localSQLiteDatabase != null){
					try{
						localSQLiteDatabase.close();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				return -2;
			}
			int n = 0;
			int m = localCursor.getCount();
			String sql3 = "insert into smstable (_id,thread_id,address,person,date,protocol,read,status,type,reply_path_present,subject,body,service_center,locked)values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			while (localCursor.moveToNext() && !mCancelBackup) {
				Object[] arrayOfObject = new Object[14];
				arrayOfObject[0] = Long.valueOf(localCursor.getLong(localCursor
						.getColumnIndex("_id")));
				arrayOfObject[1] = Long.valueOf(localCursor.getLong(localCursor
						.getColumnIndex("thread_id")));
				arrayOfObject[2] = localCursor.getString(localCursor
						.getColumnIndex("address"));
				arrayOfObject[3] = Long.valueOf(localCursor.getLong(localCursor
						.getColumnIndex("person")));
				arrayOfObject[4] = Long.valueOf(localCursor.getLong(localCursor
						.getColumnIndex("date")));
				arrayOfObject[5] = Long.valueOf(localCursor.getLong(localCursor
						.getColumnIndex("protocol")));
				arrayOfObject[6] = Long.valueOf(localCursor.getLong(localCursor
						.getColumnIndex("read")));
				arrayOfObject[7] = Long.valueOf(localCursor.getLong(localCursor
						.getColumnIndex("status")));
				arrayOfObject[8] = Long.valueOf(localCursor.getLong(localCursor
						.getColumnIndex("type")));
				arrayOfObject[9] = Long.valueOf(localCursor.getLong(localCursor
						.getColumnIndex("reply_path_present")));
				arrayOfObject[10] = localCursor.getString(localCursor
						.getColumnIndex("subject"));
				arrayOfObject[11] = localCursor.getString(localCursor
						.getColumnIndex("body"));
				arrayOfObject[12] = localCursor.getString(localCursor
						.getColumnIndex("service_center"));
				arrayOfObject[13] = Long.valueOf(localCursor.getLong(localCursor
						.getColumnIndex("locked")));			
				try{				
					localSQLiteDatabase.execSQL(sql3, arrayOfObject);
					n++;
				}catch(Exception e){
					e.printStackTrace();
				}
				
				Message msg = new Message();
				msg.what = 5;
				msg.arg1 = Double.valueOf((n * 1.0 / m) * 70).intValue();
				mHandler.sendMessage(msg);

			}
			try {
				localSQLiteDatabase.close();
				localCursor.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			while (mPause) {

			}
			if (mCancelBackup) {
				return -2;
			}
			return n;
		}
		
		
		private int remoteContactsBackup(int entriesCount){
			File backupPath = getDir("backup", Context.MODE_PRIVATE);
			String fileName = CONTACTS_BACK_FILE;
			File dbFile = new File(backupPath, fileName);
			if(!dbFile.exists() || mCancelBackup){
				return -1;
			}
			String mToken = Utilis.getToken();
			if(mToken == null){
				return -2;
			}
			if (!Utilis.checkNetwork(BackupActivity.this)) {
				return -2;
			}
			FileInputStream fis = null;
			HttpURLConnection hc = null;
			Message msg = new Message();
			msg.what = 1;
			msg.arg1 = 72;
			mHandler.sendMessage(msg);
			while (mPause) {

			}
			if (mCancelBackup) {
				if (dbFile.exists()) {
					dbFile.delete();					
				}
				return -2;
			}
			//set mCanCancel to false to unable backup
			mCanCancel = false;
			try{
				URL url = new URL(Constant.REMOTE_BACKUP_URL);
				hc =  (HttpURLConnection) url.openConnection();
			    String[] props = new String[] {"token", "type", "entriesCount"};  
			    String[] values = new String[] {mToken, "1", entriesCount+""}; 
			    fis = new FileInputStream(dbFile);    
			    String BOUNDARY = "---------------------------7d4a6d158c9"; 
			    StringBuffer sb = new StringBuffer(); 
			    for(int i=0; i < props.length; i++) {
				    sb = sb.append("--"); 
				    sb = sb.append(BOUNDARY); 
				    sb = sb.append("\r\n"); 
				    sb = sb.append("Content-Disposition: form-data; name=\""+ props[i] + "\"\r\n\r\n"); 
				    sb = sb.append(values[i]);
				    sb = sb.append("\r\n");
			    } 
			    String propsFile = "file";		    
			    sb = sb.append("--"); 
			    sb = sb.append(BOUNDARY); 
			    sb = sb.append("\r\n"); 
			    sb = sb.append("Content-Disposition: form-data; name=\"" + propsFile + "\"; filename=\"" + fileName + "\"\r\n"); 
			    sb = sb.append("Content-Type: application/octet-stream\r\n\r\n"); 
			    
			    byte[] data = sb.toString().getBytes(); 
			    byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes(); 
			    hc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY); 
			    hc.setDoOutput(true);
			    OutputStream output = hc.getOutputStream(); 
			    output.write(data); 
			    msg = new Message();
				msg.what = 1;
				msg.arg1 = 75;
				mHandler.sendMessage(msg);
			    byte[] buffer = new byte[1024];
			    long fLen = dbFile.length();
			    int mount = 0;
			    int amountRead = fis.read(buffer);
			    
				while (amountRead != -1) {				
					output.write(buffer, 0, amountRead);
					mount = mount + amountRead;
					msg = new Message();
					msg.what = 1;
					msg.arg1 = Double.valueOf((mount * 1.0 / fLen) * 25).intValue() + 75;
					mHandler.sendMessage(msg);
					amountRead = fis.read(buffer);
				}
				fis.close();

			    output.write(end_data); 
			    output.flush();
			    output.close();
			    BufferedReader reader = new BufferedReader(new InputStreamReader(hc.getInputStream(), "utf-8"));
				
				reader.close();
				hc.disconnect();
				return mount;
			}catch(Exception e){
				e.printStackTrace();
				return -3;
			}finally{				
				if(fis != null){
					try{
						fis.close();
						dbFile.delete();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				if(hc != null){
					hc.disconnect();
				}
			}			
		}

		private int contactsBackup() {
			File backupPath = getDir("backup", Context.MODE_PRIVATE);
			if (!backupPath.exists()) {
				try {
					backupPath.mkdirs();
				} catch (Exception e) {
					e.printStackTrace();
					return -1;
				}
			}

			String backRootPath = backupPath.getAbsolutePath();

			String fileName = CONTACTS_BACK_FILE;
			Uri localUri = ContactsContract.Data.CONTENT_URI;
			Cursor localCursor = getContentResolver().query(localUri, null,
					null, null, null);
			if (localCursor == null || localCursor.getCount() == 0) {
				return 0;
			}
			while (mPause) {

			}
			if (mCancelBackup) {
				return -2;
			}
			File dbFile = new File(backRootPath, fileName);			
			if (dbFile.exists()) {
				boolean del = dbFile.delete();
				if (!del) {
					return -1;
				}
			}
			SQLiteDatabase localSQLiteDatabase = SQLiteDatabase.openDatabase(
					dbFile.getAbsolutePath(), null,
					SQLiteDatabase.CREATE_IF_NECESSARY);

			while (mPause) {

			}
			if (mCancelBackup) {
				if (dbFile.exists()) {
					dbFile.delete();					
				}
				return -2;
			}
			String[] columns = localCursor.getColumnNames();
			StringBuilder sb = new StringBuilder("create table data(");
			for (int i = 0; i < columns.length; i++) {
				if (!columns[i]
						.equals(ContactsContract.CommonDataKinds.Photo.PHOTO))
					sb.append(columns[i]).append(" TEXT");
				else
					sb.append(columns[i]).append(" BLOB");

				if (i == columns.length - 1) {
					sb.append(")");
				} else {
					sb.append(",");
				}

			}
			while (mPause) {

			}
			if (mCancelBackup) {
				if (dbFile.exists()) {
					dbFile.delete();					
				}
				return -2;
			}
			localSQLiteDatabase.execSQL(sb.toString());
			while (mPause) {

			}
			if (mCancelBackup) {
				try{
					localSQLiteDatabase.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				if (dbFile.exists()) {
					dbFile.delete();					
				}
				return -2;
			}
			int s = 0;
			int c = localCursor.getCount();
			while (localCursor.moveToNext() && !mCancelBackup) {
				ContentValues cv = new ContentValues();

				for (int i = 0; i < columns.length; i++) {

					if (!columns[i]
							.equals(ContactsContract.CommonDataKinds.Photo.PHOTO)
							&& (localCursor.getString(localCursor
									.getColumnIndex(columns[i])) != null)) {
						String rv = localCursor.getString(localCursor
								.getColumnIndex(columns[i]));
						if (rv != null && !columns[i].equals("_id"))
							cv.put(columns[i], rv);						
					} 
				}

				try {
					localSQLiteDatabase.insertOrThrow("data", null, cv);
					s++;
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				Message msg = new Message();
				msg.what = 1;
				msg.arg1 = Double.valueOf((s * 1.0 / c) * 70).intValue();
				mHandler.sendMessage(msg);
			}
			try {			
				localCursor.close();
				localCursor = localSQLiteDatabase.query(true, "data", new String[] {
						"contact_id", RawContacts.ACCOUNT_TYPE,
						RawContacts.ACCOUNT_NAME }, null, null, null, null, null, null);
				if (localCursor == null || localCursor.getCount() <= 0) {
					s = 0;
				}else{
					s = localCursor.getCount();
				}
				localCursor.close();
				localSQLiteDatabase.close();
			} catch (Exception e) {
				e.printStackTrace();
			}			
			
			while (mPause) {

			}
			if (mCancelBackup) {
				if (dbFile.exists()) {
					dbFile.delete();					
				}
				return -2;
			} 
			return s;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (isBackingContacts || isBackingSms || isBackingApp) {
			mBackupThread.pauseBackup();
			getCancelPromptDialog();
			mBackupThread.continueBackup();
		} else {
			super.onBackPressed();
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mContactsBackup = backup_contacts_cb.isChecked();
		mSmsBackup = backup_sms_cb.isChecked();
		mApplistBackup = backup_applist_cb.isChecked();
		loadData();
		TextView backup_contacts_exist_text = (TextView) findViewById(R.id.backup_contacts_exist_text);
		TextView backup_sms_exist_text = (TextView) findViewById(R.id.backup_sms_exist_text);
		TextView backup_applist_exist_text = (TextView) findViewById(R.id.backup_applist_exist_text);
		backup_contacts_exist_text.setText(String.format(
				getString(R.string.backup_contacts_exist_prompt_text),
				contactsExist));
		backup_sms_exist_text.setText(String.format(
				getString(R.string.backup_sms_exist_prompt_text), smsExist));
		backup_applist_exist_text.setText(String
				.format(getString(R.string.backup_applist_exist_prompt_text),
						appsExist));

	}

	private void loadData() {
		Uri localUri = ContactsContract.Contacts.CONTENT_URI;
		try {
			Cursor localCursor = getContentResolver().query(localUri, null,
					null, null, null);
			if (localCursor != null)
				contactsExist = localCursor.getCount();
			localCursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			localUri = Uri.parse("content://sms");
			Cursor localCursor = getContentResolver().query(localUri, null,
					null, null, null);
			if (localCursor != null)
				smsExist = localCursor.getCount();
			localCursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// query app num
		List<PackageInfo> installedList = ApkManager.getInstalledPackages(this,
				false);
		appsExist = installedList.size();
	}

}
