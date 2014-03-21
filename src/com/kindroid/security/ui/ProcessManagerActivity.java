/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import com.kindroid.security.R;
import com.kindroid.security.data.AutoStartOnBootItem;
import com.kindroid.security.data.RunningTaskItem;
import com.kindroid.security.util.ProcInfo;
import com.kindroid.security.util.TaskUtil;

public class ProcessManagerActivity extends Activity implements
		android.view.View.OnClickListener {

	private final int FILLDATATOVIEW = 2;
	private final int CLOSEPROCE = 3;

	private ProcListAdapter mListProcAdapter;
	private ArrayList<ProcInfo> mProcList = new ArrayList<ProcInfo>();
//	private ListView mListView;

	private LinearLayout closeAllProcLinear;
	private LinearLayout closeSelProcLinear;	
	
	private Thread onekeyThread;
	private ActivityManager activityManager;
	
	public static RunningTaskItem sOptimizeItem;
	private boolean mForOptimize = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.proc_list);
		mForOptimize = getIntent().getBooleanExtra(MobileExamActivity.MOBILE_EXAM_OPTIMIZE_INTENT, false);
		
		activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//		mListView = (ListView) findViewById(R.id.listproc);
		
		closeAllProcLinear = (LinearLayout) findViewById(R.id.close_al_process_linear);
		closeSelProcLinear = (LinearLayout) findViewById(R.id.close_select_process_linear);
		
		closeAllProcLinear.setOnClickListener(this);
		closeSelProcLinear.setOnClickListener(this);
		
		refreshData();
		
		mListProcAdapter = new ProcListAdapter(this, mProcList);
		ListView listproc = (ListView) findViewById(R.id.listproc);
		listproc.setAdapter(mListProcAdapter);
		listproc.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				CheckBox procCheck = (CheckBox) view
						.findViewById(R.id.procCheck);
				boolean b = !procCheck.isChecked();
				procCheck.setChecked(b);
				ProcInfo pi = mProcList.get(position);
				pi.setChecked(b);
			}

		});
		
//		mListView.setAdapter(mListProcAdapter);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.close_al_process_linear) {
			if(mProcList.size() > 0){
				closeDialog(2);
			}

		} else if (v.getId() == R.id.close_select_process_linear) {
			boolean notShowDialog = false;

			for (ProcInfo procinfo : mProcList) {
				if (procinfo.getChecked()) {
					notShowDialog = true;
					break;
				}
			}
			if (!notShowDialog) {
				
				showDialog();
				
				return;
			}
			closeDialog(1);
		
		} 
		sendBroadcast(new Intent(NetTrafficSettings.NET_TRAFFIC_UPDATE_SETTINGS));
	}

	void clickUpdateData() {
		mProcList.clear();
		mProcList.addAll(TaskUtil.getRunningApp(this));
		if(mForOptimize && sOptimizeItem != null){
			sOptimizeItem.clearProc();
			for (ProcInfo procinfo : mProcList) {
				sOptimizeItem.addProc(procinfo);
			}
			
		}
		mListProcAdapter.notifyDataSetChanged();		
	}
	
	
	
	void closeDialog(final int type){
		final Dialog promptDialog = new Dialog(ProcessManagerActivity.this,
				R.style.softDialog);
		View view = LayoutInflater.from(ProcessManagerActivity.this).inflate(
				R.layout.soft_uninstall_prompt_dialog, null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);
		
		
		promptText.setText(type==1?R.string.sure_close_selected:R.string.sure_close_all);
		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(type==1){
					for (ProcInfo procinfo : mProcList) {
						if (procinfo.getChecked()) {
							TaskUtil.killProcess(procinfo.getPackageName(), ProcessManagerActivity.this);
						}
					}					
					clickUpdateData();
					
				}else{
					for (ProcInfo procinfo : mProcList) {
						TaskUtil.killProcess(procinfo.getPackageName(), ProcessManagerActivity.this);
					}
					clickUpdateData();
				}
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
	
	

	void showDialog() {
		
		final Dialog promptDialog = new Dialog(ProcessManagerActivity.this,
				R.style.softDialog);
		View view = LayoutInflater.from(ProcessManagerActivity.this).inflate(
				R.layout.soft_uninstall_prompt_dialog, null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);
		promptText.setText(R.string.proce_select_app);
		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
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
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub		
		super.onPause();
		
	}

	class ThreadUpdate extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub			
			mProcList.clear();
			mProcList.addAll(TaskUtil
					.getRunningApp(ProcessManagerActivity.this));
			Message mes_fill = Message.obtain();
			mes_fill.what = FILLDATATOVIEW;
			
			handler.sendMessage(mes_fill);

			handler.sendEmptyMessage(CLOSEPROCE);

		}

	}

	private Handler handler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			
			case FILLDATATOVIEW:
				
				mListProcAdapter.notifyDataSetChanged();
				break;
			case CLOSEPROCE:
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Thread.sleep(1000);
							handler.sendEmptyMessage(5);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}).start();
				break;
			
			case 5:				
				onekeyThread = null;

			}

		};
	};

	private void refreshData() {
		mProcList.clear();
		mProcList.addAll(TaskUtil.getRunningApp(this));
	}

}