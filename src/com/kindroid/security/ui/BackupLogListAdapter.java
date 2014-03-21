/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.kindroid.security.R;
import com.kindroid.security.util.BackupDBHelper;

public class BackupLogListAdapter extends BaseAdapter {
	private Activity mActivity;
	private List<Map<String, String>> items;
	private static boolean des;
	
	public BackupLogListAdapter(Activity activity){
		this(activity, true);
	}
	public BackupLogListAdapter(Activity activity, boolean des){
		this.mActivity = activity;
		this.items = new ArrayList<Map<String, String>>();
		this.des = des;
	}
	
	public void delteItem(Map<String, String> item){
		this.items.remove(item);
	}
	public void clearItems(){
		this.items.clear();
	}
	public void addItem(Map<String, String> item){
		if(!des){
			this.items.add(item);
		}else{
			this.items.add(0, item);
		}
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView == null){
			convertView = this.mActivity.getLayoutInflater().inflate(R.layout.backup_log_list_item, null);
		}
		Map<String, String> item = items.get(position);
		TextView log_time_tv = (TextView)convertView.findViewById(R.id.log_time_tv);
		TextView log_flag_text = (TextView)convertView.findViewById(R.id.log_flag_text);
		TextView log_type_text = (TextView)convertView.findViewById(R.id.log_type_text);
		TextView log_ntraf_num_text = (TextView)convertView.findViewById(R.id.log_ntraf_num_text);
		
		long time = Long.parseLong(item.get(BackupDBHelper.COLUMN_TIME));
		int flag = Integer.parseInt(item.get(BackupDBHelper.COLUMN_FLAG));
		int type = Integer.parseInt(item.get(BackupDBHelper.COLUMN_TYPE));
		int num = Integer.parseInt(item.get(BackupDBHelper.COLUMN_NUM));
		int ntraf = Integer.parseInt(item.get(BackupDBHelper.COLUMN_NTRAF));
		Date date = new Date(time);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log_time_tv.setText(df.format(date));
		if(flag == 0){			
			if(type < 4){
				log_flag_text.setText(R.string.backup_log_back_fail_text);
			}else{
				log_flag_text.setText(R.string.backup_log_restore_fail_text);
				
			}			
			switch(type){
			case 1:
				log_type_text.setText(this.mActivity.getString(R.string.backup_log_contacts_text) + ", " + this.mActivity.getString(R.string.backup_log_fail_text));
				break;
			case 2:
				log_type_text.setText(this.mActivity.getString(R.string.backup_log_sms_text) + ", " + this.mActivity.getString(R.string.backup_log_fail_text));
				break;
			case 3:
				log_type_text.setText(this.mActivity.getString(R.string.backup_software_list_text) + ", " + this.mActivity.getString(R.string.backup_log_fail_text));
				break;
			case 4:
				if(num == 0){
					log_type_text.setText(this.mActivity.getString(R.string.backup_log_contacts_text) + ", " + this.mActivity.getString(R.string.backup_log_fail_text));
				}else{
					log_type_text.setText(this.mActivity.getString(R.string.backup_log_contacts_text) + ", " + this.mActivity.getString(R.string.restore_log_cancel_prompt_text));
				}
				break;
			case 5:
				if(num == 0){
					log_type_text.setText(this.mActivity.getString(R.string.backup_log_sms_text) + ", " + this.mActivity.getString(R.string.backup_log_fail_text));
				}else{
					log_type_text.setText(this.mActivity.getString(R.string.backup_log_sms_text) + ", " + this.mActivity.getString(R.string.restore_log_cancel_prompt_text));
				}
				break;
			}
			log_flag_text.setTextColor(this.mActivity.getResources().getColor(R.color.red));
		}else{			
			if(type < 4){
				log_flag_text.setText(R.string.backup_log_back_succ_text);
			}else{
				log_flag_text.setText(R.string.backup_log_restore_succ_text);
			}
			
			if(num < 0){
				num = 0;
			}
			switch(type){
			case 1:
				log_type_text.setText(this.mActivity.getString(R.string.backup_log_contacts_text) + "(" + num + ")");
				break;
			case 2:
				log_type_text.setText(this.mActivity.getString(R.string.backup_log_sms_text) + "(" + num + ")");
				break;
			case 3:
				log_type_text.setText(this.mActivity.getString(R.string.backup_software_list_text) + "(" + num + ")");
				break;
			case 4:
				log_type_text.setText(this.mActivity.getString(R.string.backup_log_contacts_text) + "(" + num + ")");
				break;
			case 5:
				log_type_text.setText(this.mActivity.getString(R.string.backup_log_sms_text) + "(" + num + ")");
				break;
			}
			log_flag_text.setTextColor(this.mActivity.getResources().getColor(R.color.light_green));
		}
		if(ntraf >= 1024){
			log_ntraf_num_text.setText(ntraf / 1024 + "KB");
		}else{
			log_ntraf_num_text.setText(ntraf + "B");
		}
		
		
		return convertView;
	}

}
