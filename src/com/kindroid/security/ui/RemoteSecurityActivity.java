/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import com.kindroid.security.R;
import com.kindroid.security.adapter.RemoteSecerityAdapter;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.RemoteSecurityModel;

public class RemoteSecurityActivity extends Activity implements
		android.view.View.OnClickListener, OnItemClickListener {

	private ArrayList<RemoteSecurityModel> modelList = new ArrayList<RemoteSecurityModel>();
	private ListView listView;
	private RemoteSecerityAdapter adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.remote_security_activity);
		listView = (ListView) findViewById(R.id.listproc);
		initDate();
		adapter = new RemoteSecerityAdapter(this, modelList, 1, listView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

	}

	void initDate() {
		RemoteSecurityModel model = new RemoteSecurityModel();
		model.setIconId(R.drawable.security_fuc_icon);
		model.setAppName(getResources().getString(R.string.security_fuc));
		model.setDes("");
		modelList.add(model);

		model = new RemoteSecurityModel();
		model.setIconId(R.drawable.remote_clock_icon);
		model.setAppName(getResources().getString(R.string.remote_lock_mobile));
		model.setDes(getResources().getString(R.string.lock_lost_mobile));
		modelList.add(model);

		model = new RemoteSecurityModel();
		model.setIconId(R.drawable.remote_del_data_icon);
		model.setAppName(getResources().getString(R.string.remote_data_del));
		model.setDes(getResources().getString(R.string.mobile_data_del));
		modelList.add(model);

		model = new RemoteSecurityModel();
		model.setIconId(R.drawable.remote_gps);
		model.setAppName(getResources().getString(R.string.remote_gps_mobile));
		model.setDes(getResources().getString(R.string.get_lost_mobile_gps));
		modelList.add(model);
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		adapter = new RemoteSecerityAdapter(this, modelList, 1, listView);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (arg2 == 0) {
			boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
					Constant.SHAREDPREFERENCES_REMOTESECURITY, false);
			
			if(!isTrue){
				Intent intent=new Intent("ehoo.com.update.remote.ui");
				intent.putExtra("upid", 1);
				sendBroadcast(intent);
				
			}else{
				PopDialog(1);
			}

		} else if (arg2 == 1) {
			boolean isTrue=KindroidSecurityApplication.sh.getBoolean(Constant.SHAREDPREFERENCES_AFTERUPDATESIMTOLOCKMOBILE, false);
			PopDialog(isTrue?3:2);
		
		} else if (arg2 == 2) {
			Intent intent = new Intent(RemoteSecurityActivity.this,
					RemoteDelDataActivity.class);
			startActivity(intent);
		} else if (arg2 == 3) {
			Intent intent = new Intent(RemoteSecurityActivity.this,
					RemoteGpsActivity.class);
			startActivity(intent);
		}

	}
	
	void PopDialog(int type){
		final Dialog promptDialog = new Dialog(RemoteSecurityActivity.this,
				R.style.softDialog);
		View view = LayoutInflater.from(RemoteSecurityActivity.this).inflate(
				R.layout.soft_uninstall_prompt_dialog, null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);
		int branch=1;
		if(type==1){
			promptText.setText(R.string.close_security_and_clear_safemobile);
		}else if(type==2){
			branch=2;
			promptText.setText(R.string.enable_sim_card);
		}else if(type==3){
			branch=2;
			promptText.setText(R.string.unable_sim_card);
		}
		
		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		final int branch_copy=branch;
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				promptDialog.dismiss();
				
				
				startActivityForResult(new Intent(RemoteSecurityActivity.this,LoginActivity.class), branch_copy);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==1&&resultCode==RESULT_OK){
			
			if (data != null & data.getExtras() != null) {
				String passwd=data.getExtras().getString("passwd");
				if(passwd!=null){
					Editor editor=KindroidSecurityApplication.sh.edit();
					editor.putString(Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD, ConvertUtils.getMD5(passwd.getBytes()));
					editor.putBoolean(Constant.SHAREDPREFERENCES_REMOTESECURITY, false);
					editor.putString(Constant.SHAREDPREFERENCES_SAFEMOBILENUMBER, "");
					editor.putString(Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD, passwd);
					editor.commit();
					adapter.notifyDataSetChanged();
				}
			}
			
		}else if(requestCode==2&&resultCode==RESULT_OK){
			if (data != null & data.getExtras() != null) {
				String passwd=data.getExtras().getString("passwd");
				if(passwd!=null){
					Editor editor=KindroidSecurityApplication.sh.edit();
					boolean isTrue=KindroidSecurityApplication.sh.getBoolean(Constant.SHAREDPREFERENCES_AFTERUPDATESIMTOLOCKMOBILE, false);
					editor.putBoolean(Constant.SHAREDPREFERENCES_AFTERUPDATESIMTOLOCKMOBILE, !isTrue);
					editor.putString(Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD, passwd);
					editor.commit();
					adapter.notifyDataSetChanged();
				}
			}
		}
		
	}
	
	

}