/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-07
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.Utilis;

public class AppBackupDetailActivity extends Activity {
	private AppListAdapter listAdapter;
	private ListView backup_applist_listview;
	private Dialog backupProgressDialog;
	private TextView prompt_dialog_text;
	private static boolean isLoadingData = false;
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
	public static String mToken = null;
	public static final String APPLIST_BACKUP_FILE = "applist.bak";
	private boolean mCancelLoading = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applist_detail);
		View home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setResult(Activity.RESULT_OK);
				finish();
			}
		});
		listAdapter = new AppListAdapter(this);
		backup_applist_listview = (ListView) findViewById(R.id.backup_applist_listview);
		backup_applist_listview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				AppInfoForManage aifm = (AppInfoForManage)listAdapter.getItem(position);
				
				Uri uri = Uri.parse("market://search?q=pname:"+aifm.getPackageName());  
				Intent it = new Intent(Intent.ACTION_VIEW);
				it.setPackage("com.android.vending");
				it.setData(uri);
				startActivitySafely(it); 

			}
			
		});
		//delete buffer data
		File backupPath = getDir("backup", Context.MODE_PRIVATE);		
		String fileName = BackupActivity.APPLIST_BACKUP_FILE;		
		File dbFile = new File(backupPath, fileName);
		if(dbFile.exists()){
			dbFile.delete();
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!isLoadingData) {
			isLoadingData = true;
			listAdapter.clearItems();			
			loadApplistData();	
			
		}		
	}

	void startActivitySafely(Intent intent) {
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.applist_app_not_found_text,Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {

		}
	}
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				
				break;
			case 1:
				if (backupProgressDialog != null) {
					backupProgressDialog.dismiss();
				}
				isLoadingData = false;
				Toast.makeText(AppBackupDetailActivity.this, R.string.backup_applist_noback, Toast.LENGTH_LONG).show();
				break;
			case 10:
				backup_applist_listview.setAdapter(listAdapter);
				//backup_applist_listview
				if (backupProgressDialog != null) {
					backupProgressDialog.dismiss();
				}
				listAdapter.notifyDataSetChanged();
				isLoadingData = false;
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
	
	private boolean downloadRemoteData(){
		if (!checkNetwork()) {
			return false;
		}
		mToken = Utilis.getToken();
		if(mToken == null){
			return false;
		}
		File backupPath = getDir("backup", Context.MODE_PRIVATE);
		if (!backupPath.exists()){
			try{
				backupPath.mkdirs();
			}catch(Exception e){
				e.printStackTrace();				
				return false;
			}
		}
		String fileName = APPLIST_BACKUP_FILE;
		File dbFile = new File(backupPath, fileName);
		if (!dbFile.exists()) {
			try{
				dbFile.createNewFile();
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		
		boolean ret = true;		
		// download remote backup file
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(Constant.REMOTE_RESTORE_URL + URLEncoder.encode(mToken) + "/3");
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			Header[] h = response.getAllHeaders();
			
			if (entity != null) {				
				inputStream = new BufferedInputStream(entity.getContent());
				outputStream = new BufferedOutputStream(new FileOutputStream(dbFile));
				long cLen = entity.getContentLength();
				if(cLen <= 0){
					throw new Exception();
				}
				byte[] buffer = new byte[512];
				int len = inputStream.read(buffer);
				int count = 0;
				while (len != -1) {	
					outputStream.write(buffer, 0, len);					
					len = inputStream.read(buffer);
				}
				outputStream.flush();
				
			} else {
				ret = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(outputStream != null){
				try{
					outputStream.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			httpclient.getConnectionManager().shutdown();
		}
		return ret;
	}
	private void loadApplistData(){
		if (backupProgressDialog == null) {
			backupProgressDialog = new Dialog(this, R.style.softDialog){

				@Override
				public void onBackPressed() {
					// TODO Auto-generated method stub
					if(isLoadingData){
						mCancelLoading = true;
						isLoadingData = false;
					}else{
						super.onBackPressed();
					}
				}
				
			};
		}
		View view = LayoutInflater.from(this).inflate(
				R.layout.softmanage_prompt_dialog, null);
		backupProgressDialog.setContentView(view);
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
		prompt_dialog_text = (TextView) backupProgressDialog
				.findViewById(R.id.prompt_dialog_text);
		prompt_dialog_text.setText(R.string.backup_applist_loaddata_progress_text);
		backupProgressDialog.show();
		new LoadingItem().start();
		mCancelLoading = false;
		new LoadAppsThread().start();
		
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
	private class LoadAppsThread extends Thread{
		
		public void run(){	
			File backupPath = getDir("backup", Context.MODE_PRIVATE);
			
			String fileName = BackupActivity.APPLIST_BACKUP_FILE;		
			File dbFile = new File(backupPath, fileName);
			if(!dbFile.exists()){
				if(downloadRemoteData()){
					
				}else{
					if(dbFile.exists()){
						dbFile.delete();
					}
					handler.sendEmptyMessage(1);
					return;
				}
			}
			if(mCancelLoading){
				return;
			}
			SQLiteDatabase localSQLiteDatabase = null;
			try{
				localSQLiteDatabase = SQLiteDatabase.openDatabase(			
					dbFile.getAbsolutePath(), null, 1);
			}catch(Exception e){
				e.printStackTrace();
				dbFile.delete();
				handler.sendEmptyMessage(1);
				return;
			}
			if (localSQLiteDatabase == null){
				handler.sendEmptyMessage(1);
				return;
			}
			Cursor localCursor = localSQLiteDatabase.query("applist", null, null,
					null, null, null, null);
			if(localCursor == null || localCursor.getCount() <= 0){
				try{
					localSQLiteDatabase.close();
					localCursor.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				handler.sendEmptyMessage(1);
				return;
			}
			while(localCursor.moveToNext() && !mCancelLoading){
				AppInfoForManage aifm = new AppInfoForManage();
				aifm.setPackageName(localCursor.getString(localCursor.getColumnIndex("packageName")));
				aifm.setLabel(localCursor.getString(localCursor.getColumnIndex("label")));
				aifm.setVersion(localCursor.getString(localCursor.getColumnIndex("version")));
				int size = Integer.parseInt(localCursor.getString(localCursor.getColumnIndex("size")));
				aifm.setSize(size);
				
				PackageManager pm = getPackageManager();
				try{
					PackageInfo pi = pm.getPackageInfo(aifm.getPackageName(), PackageManager.GET_ACTIVITIES);
					if(pi != null){
						aifm.setInstalled(true);
						aifm.setIcon(pi.applicationInfo.loadIcon(pm));
					}
				}catch(NameNotFoundException e){
					aifm.setInstalled(false);
					aifm.setIcon(null);
				}
				
				listAdapter.addItem(aifm);
			}
			try{
				localSQLiteDatabase.close();
				localCursor.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			if(mCancelLoading){
				listAdapter.clearItems();
			}
			handler.sendEmptyMessage(10);
		}
	}
	
}
