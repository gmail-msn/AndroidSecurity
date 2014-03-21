/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import com.kindroid.security.R;
import com.kindroid.security.util.BackupDBHelper;

public class BacklogActivity extends ListActivity {
	private BackupLogListAdapter mListAdapter;
	private boolean mLoadingData = false;
	private TextView back_log_sum_text;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backup_log);
		View home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent homepage = new Intent(BacklogActivity.this,
						DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
			}
		});
		back_log_sum_text = (TextView)findViewById(R.id.back_log_sum_text);
		back_log_sum_text.setText(String.format(getString(R.string.back_log_sum_text), 0));
		View clean_history_action_linear = findViewById(R.id.clean_history_action_linear);
		clean_history_action_linear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//delete log
				if(mListAdapter.getCount() > 0){
					getDeletePromptDialog();
				}
			}
		});
		mListAdapter = new BackupLogListAdapter(this);
		setListAdapter(mListAdapter);
	}
	private void getDeletePromptDialog(){
		final Dialog promptDialog = new Dialog(
				this, R.style.softDialog);
		View view = LayoutInflater.from(
				this).inflate(
				R.layout.soft_uninstall_prompt_dialog, null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);
		promptText.setText(R.string.backup_delete_log_prompt_text);
		Button button_ok = (Button) promptDialog
				.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				deleteLogFromDB();				
				mListAdapter.clearItems();
				mListAdapter.notifyDataSetChanged();
				back_log_sum_text.setText(String.format(getString(R.string.back_log_sum_text), 0));
				promptDialog.dismiss();

			}
		});
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
				promptDialog.dismiss();

			}
		});
		promptDialog.show();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!mLoadingData){
			mListAdapter.clearItems();
			loadLogData();
			back_log_sum_text.setText(String.format(getString(R.string.back_log_sum_text), mListAdapter.getCount()));
			mListAdapter.notifyDataSetChanged();
		}
	}
	private void loadLogData(){
		BackupDBHelper dh = new BackupDBHelper(this, BackupDBHelper.logDBName);
		SQLiteDatabase db = dh.getReadableDatabase();
		Cursor cs = db.query(BackupDBHelper.logTableName, null, null, null, null, null, null);
		if(cs == null || cs.getCount() == 0){			
			if(cs != null){
				cs.close();
			}
    		db.close();
    		return;
    	}
		while(cs.moveToNext()){
    		Map<String, String> item = new HashMap<String, String>();
    		item.put(BackupDBHelper.COLUMN_FLAG, cs.getString(cs.getColumnIndex(BackupDBHelper.COLUMN_FLAG)));
    		item.put(BackupDBHelper.COLUMN_NTRAF, cs.getString(cs.getColumnIndex(BackupDBHelper.COLUMN_NTRAF)));
    		item.put(BackupDBHelper.COLUMN_NUM, cs.getString(cs.getColumnIndex(BackupDBHelper.COLUMN_NUM)));
    		item.put(BackupDBHelper.COLUMN_TIME, cs.getString(cs.getColumnIndex(BackupDBHelper.COLUMN_TIME)));
    		item.put(BackupDBHelper.COLUMN_TYPE, cs.getString(cs.getColumnIndex(BackupDBHelper.COLUMN_TYPE)));
    		mListAdapter.addItem(item);
    	}
    	try{
	    	cs.close();
	    	db.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		
	}

	private void deleteLogFromDB(){
		BackupDBHelper dh = new BackupDBHelper(this, BackupDBHelper.logDBName);
		SQLiteDatabase db = dh.getWritableDatabase();
    	db.delete(BackupDBHelper.logTableName, null, null);
    	db.close();
	}

}
