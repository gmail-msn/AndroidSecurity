/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.kindroid.security.R;
import com.kindroid.security.model.VirusHistory;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class VirusHistoryListAdapter extends BaseAdapter {
	private Activity mActivity;
	private List<VirusHistory> items;
	private static boolean des;
	
	public VirusHistoryListAdapter(Activity activity){
		this(activity, true);
	}
	public VirusHistoryListAdapter(Activity activity, boolean des){
		this.mActivity = activity;
		this.items = new ArrayList<VirusHistory>();
		this.des = des;
	}
	public void delteItem(VirusHistory vh){
		this.items.remove(vh);
	}
	public void clearItems(){
		this.items.clear();
	}
	public void addItem(VirusHistory vh){
		if(!des){
			this.items.add(vh);
		}else{
			this.items.add(0, vh);
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
		VirusHistory vh = items.get(position);
		if(convertView == null){
			convertView = this.mActivity.getLayoutInflater().inflate(R.layout.virus_history_list_item, null);
		}
		TextView clean_time_tv = (TextView)convertView.findViewById(R.id.clean_time_tv);
		TextView history_virus_num = (TextView)convertView.findViewById(R.id.history_virus_num);
		TextView history_care_num = (TextView)convertView.findViewById(R.id.history_care_num);
		Date date = new Date(vh.getTime());		
		Locale locale = this.mActivity.getResources().getConfiguration().locale;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		clean_time_tv.setText(df.format(date));
		history_virus_num.setText(vh.getVirus_num() + "");
		if(vh.getVirus_num() == 0){
			history_virus_num.setTextColor(this.mActivity.getResources().getColor(R.color.light_green));
		}else{
			history_virus_num.setTextColor(Color.RED);
		}
		history_care_num.setText(vh.getCare_num() + "");
		if(position > 0){
			convertView.setBackgroundResource(R.drawable.virus_history_list_bg_on);
		}
		return convertView;
	}

}
