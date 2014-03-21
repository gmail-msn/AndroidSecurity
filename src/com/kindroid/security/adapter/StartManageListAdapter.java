/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.model.AutoStartAppItem;
import com.kindroid.security.util.UnitsConversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author heli.zhao
 *
 */
public class StartManageListAdapter extends BaseAdapter {
	private Context mContext;
	private List<AutoStartAppItem> mListItems;
	private LayoutInflater mLayoutFlater;
	
	public StartManageListAdapter(Context context){
		mContext = context;
		mListItems = new ArrayList<AutoStartAppItem>();
		mLayoutFlater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public List<AutoStartAppItem> getItems(){
		return mListItems;
	}
	public void clear(){
		mListItems.clear();
	}
	public void addItem(AutoStartAppItem item){
		mListItems.add(item);
	}
	public void delItem(AutoStartAppItem item){
		mListItems.remove(item);
	}
	public void delItem(int index){
		mListItems.remove(index);
	}
	public void sortItems(final boolean sortBy){
		Collections.sort(mListItems, new Comparator<AutoStartAppItem>() {
			@Override
			public int compare(AutoStartAppItem object1,
					AutoStartAppItem object2) {
				// TODO Auto-generated method stub
				if(sortBy){
					if(object1.isEnable() && object2.isEnable()){
						if(object1.getPackageName().compareTo(object2.getPackageName()) > 0){
							return 1;
						}else{
							return -1;
						}
					}else if(object1.isEnable()){
						return 1;
					}else if(object2.isEnable()){
						return -1;
					}else{
						if(object1.getPackageName().compareTo(object2.getPackageName()) > 0){
							return 1;
						}else{
							return -1;
						}
					}
				}else{
					if(!object1.isEnable() && !object2.isEnable()){
						if(object1.getPackageName().compareTo(object2.getPackageName()) > 0){
							return 1;
						}else{
							return -1;
						}
					}else if(!object1.isEnable()){
						return 1;
					}else if(!object2.isEnable()){
						return -1;
					}else{
						if(object1.getPackageName().compareTo(object2.getPackageName()) > 0){
							return 1;
						}else{
							return -1;
						}
					}
				}
			}

		});
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListItems.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mListItems.get(position);
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
			convertView = mLayoutFlater.inflate(R.layout.startmanage_list_item, null);
		}
		AutoStartAppItem item = mListItems.get(position);
		ImageView mIconView = (ImageView)convertView.findViewById(R.id.app_icon);
		if(item.getAppIcon() != null){
			mIconView.setImageDrawable(item.getAppIcon());
		}
		TextView mAppLabel = (TextView)convertView.findViewById(R.id.app_label);
		mAppLabel.setText(item.getAppLabel());
		
		TextView mAppVersion = (TextView)convertView.findViewById(R.id.app_version);
		mAppVersion.setText(item.getVersion());
		
		TextView mAppSize = (TextView)convertView.findViewById(R.id.app_size);
		mAppSize.setText(Html.fromHtml(new UnitsConversion().defaultConversionForHtml(item.getSize())));
		
		ImageView mEnableIcon = (ImageView)convertView.findViewById(R.id.enable_icon);
		ImageView mDisableIcon = (ImageView)convertView.findViewById(R.id.disable_icon);
		if(item.isEnable()){
			mDisableIcon.setVisibility(View.GONE);
			mEnableIcon.setVisibility(View.VISIBLE);
		}else{
			mDisableIcon.setVisibility(View.VISIBLE);
			mEnableIcon.setVisibility(View.GONE);
		}
		
		return convertView;
	}

}