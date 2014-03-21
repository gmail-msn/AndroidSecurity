/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-09
 * Description:
 */
package com.kindroid.security.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.model.BlackListItem;
import com.kindroid.security.model.InterceptModeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heli.zhao
 *
 */
public class InterceptModeSettingAdapter extends BaseAdapter {
	private List<InterceptModeItem> mItems;
	private Context mContext;
	
	public InterceptModeSettingAdapter(Context ctx){
		mContext = ctx;
		mItems = new ArrayList<InterceptModeItem>();
	}
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mItems.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mItems.get(position);
	}
	public void addItem(InterceptModeItem item){
		mItems.add(item);
	}
	public void delItem(int position){
		mItems.remove(position);
	}
	public void clear(){
		mItems.clear();
	}
	public void setMode(int index){
		for(int i = 0; i < mItems.size(); i++){
			InterceptModeItem item = mItems.get(i);
			if(i == index){
				item.setIsSelected(true);
			}else{
				item.setIsSelected(false);
			}
		}
	}
	public void setMode(InterceptModeItem item){
		for(InterceptModeItem i : mItems){
			if(item.equals(i)){
				item.setIsSelected(true);
			}else{
				item.setIsSelected(false);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
			convertView = inflater.inflate(R.layout.intercept_mode_setting_item, null);
		}
		InterceptModeItem item = (InterceptModeItem)mItems.get(position);
		TextView mode_title_tv = (TextView)convertView.findViewById(R.id.mode_title_tv);
		TextView mode_desp_tv = (TextView)convertView.findViewById(R.id.mode_desp_tv);
		RadioButton radio_button = (RadioButton)convertView.findViewById(R.id.radio_button);
		mode_title_tv.setText(item.getModeTitle());
		mode_desp_tv.setText(item.getModeDesp());
		radio_button.setChecked(item.isSelected());
		
		return convertView;
	}

}
