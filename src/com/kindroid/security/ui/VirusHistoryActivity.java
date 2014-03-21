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
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.kindroid.security.R;
import com.kindroid.security.model.VirusHistory;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.SQLiteDBHelper;
import com.kindroid.security.util.UtilShareprefece;

public class VirusHistoryActivity extends ListActivity {
	VirusHistoryListAdapter vla;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.virus_history);
//		View home_page = findViewById(R.id.home_icon);
//		home_page.setOnClickListener(new View.OnClickListener() {			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//					Intent homepage = new Intent(VirusHistoryActivity.this, DefenderTabMain.class);
//					homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//					startActivity(homepage);
//					finish();				
//			}
//		});
		LinearLayout clean_history_action_linear = (LinearLayout)findViewById(R.id.clean_history_action_linear);
		clean_history_action_linear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(vla.getCount() <= 0)
					return;
				final Dialog promptDialog1 = new Dialog(VirusHistoryActivity.this, R.style.softDialog);
				View view = LayoutInflater.from(VirusHistoryActivity.this).inflate(
						R.layout.soft_uninstall_prompt_dialog, null);
				promptDialog1.setContentView(view);

				TextView promptText = (TextView) promptDialog1
						.findViewById(R.id.prompt_text);
				promptText.setText(R.string.clean_history_confirm);
				View button_ok = promptDialog1.findViewById(R.id.button_ok);
				View button_cancel = promptDialog1.findViewById(R.id.button_cancel);
				button_ok.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						promptDialog1.dismiss();
						deleteHistory();
						vla.clearItems();
						vla.notifyDataSetChanged();
					}
				});
				button_cancel.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						promptDialog1.dismiss();
					}
				});
				promptDialog1.show();
			}
		});
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		if(position == 0){
			Intent intent = new Intent(this, VirusCleanActivity.class);
			startActivityForResult(intent, 99);
		}
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 99
				&& resultCode == Activity.RESULT_OK) {
			finish();
		}
	}
	private void deleteHistory(){
		SQLiteDBHelper dh = new SQLiteDBHelper(this, SQLiteDBHelper.historyDBName);
    	SQLiteDatabase db = dh.getReadableDatabase();
    	db.delete(SQLiteDBHelper.historyTableName, null, null);
    	db.delete(SQLiteDBHelper.cleanHistoryTableName, null, null);
    	db.close();
    	//write delete time
    	SharedPreferences sp = KindroidSecurityApplication.sh;
		Editor editor = sp.edit();
		long time = System.currentTimeMillis();
		editor.putLong(UtilShareprefece.LAST_DELETE_TIME, time);
		//clean last scan time
		editor.putLong(UtilShareprefece.LAST_SCAN_TIME, 0);
		editor.putInt(UtilShareprefece.LAST_VIRUS_SUM, 0);
		editor.commit();
		TextView history_clean_time = (TextView)findViewById(R.id.history_clean_time);
		Date date = new Date(time);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		history_clean_time.setText(df.format(date));
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		TextView history_clean_time = (TextView)findViewById(R.id.history_clean_time);
		SharedPreferences sp = KindroidSecurityApplication.sh;
		long last_delete_time = sp.getLong(UtilShareprefece.LAST_DELETE_TIME, 0L);
		if(last_delete_time == 0){
			history_clean_time.setText(R.string.clean_no_history);
		}else{
			Date date = new Date(last_delete_time);
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			history_clean_time.setText(df.format(date));
		}
		if(vla != null)
			vla.clearItems();
		loadHistoryData();
	}

	private void loadHistoryData(){
		SQLiteDBHelper dh = new SQLiteDBHelper(this, SQLiteDBHelper.historyDBName);
    	SQLiteDatabase db = dh.getReadableDatabase();
    	Cursor cs = db.query(SQLiteDBHelper.historyTableName, null, null, null, null, null, null);
    	
    	if(cs == null){
    		db.close();
    		return;
    	}
    	vla = new VirusHistoryListAdapter(this);
    	
    	while(cs.moveToNext()){
    		VirusHistory vh = new VirusHistory();
    		vh.setTime(cs.getLong(cs.getColumnIndex("time")));
    		vh.setVirus_num(cs.getInt(cs.getColumnIndex("virus_num")));
    		vh.setCare_num(cs.getInt(cs.getColumnIndex("care_num")));
    		vla.addItem(vh);
    	}
    	try{
	    	cs.close();
	    	db.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	this.setListAdapter(vla);
	}

}
