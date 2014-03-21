/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */
package com.kindroid.security.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.adapter.ServListAdapter;
import com.kindroid.security.model.ServInfo;
import com.kindroid.security.util.TaskUtil;

public class ServiceManageActivity extends Activity {

	private ServListAdapter mListProcAdapter;
	private List<ServInfo> mProcList = new ArrayList<ServInfo>();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.proc_list);
		View proc_menu_linear = findViewById(R.id.proc_menu_linear);
		proc_menu_linear.setVisibility(View.GONE);
		
		mListProcAdapter = new ServListAdapter(this, mProcList);
		ListView listproc = (ListView) findViewById(R.id.listproc);		
		listproc.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				toSettingActivity(mProcList.get(position).getPackageName());
			}

		});		
		
	}
	private void toSettingActivity(String packageName){
		if(Build.VERSION.SDK_INT >= 9){
			Intent intent = new Intent();
			intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			intent.setData(Uri.fromParts("package", packageName, null));
			startActivity(intent);
			return;
		}
		if(Build.VERSION.SDK_INT == 8){
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			intent.putExtra("pkg", packageName);
			startActivity(intent);
			return;
		}
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
		intent.putExtra("com.android.settings.ApplicationPkgName", packageName);
		startActivity(intent);	
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		refreshData();
	}
	
	private void refreshData() {
		mProcList.clear();
		mProcList.addAll(TaskUtil.getRunningServices(this));
		ListView listproc = (ListView) findViewById(R.id.listproc);		
		listproc.setAdapter(mListProcAdapter);
	}

}
