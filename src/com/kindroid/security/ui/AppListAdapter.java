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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.util.UnitsConversion;

public class AppListAdapter extends BaseAdapter {

	private Activity mActivity;
	private List<AppInfoForManage> items;
	
	AppListAdapter(Activity activity){
		this.mActivity = activity;
		this.items = new ArrayList<AppInfoForManage>();
	}
	public void addItem(AppInfoForManage aifm){
		this.items.add(aifm);
	}
	
	public void deleteItem(int index){
		this.items.remove(index);
	}
	
	public void delteItem(AppInfoForManage aifm){
		this.items.remove(aifm);
	}
	public void clearItems(){
		this.items.clear();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.items.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return this.items.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View retView = this.mActivity.getLayoutInflater().inflate(R.layout.app_list_item, null);
		AppInfoForManage aifm = items.get(position);
		if(aifm.getIcon() != null){
			ImageView iconView = (ImageView)retView.findViewById(R.id.applist_app_icon);
			iconView.setImageDrawable(aifm.getIcon());
		}
		TextView applist_app_label = (TextView)retView.findViewById(R.id.applist_app_label);
		applist_app_label.setText(aifm.getLabel());
		
		TextView applist_app_version = (TextView)retView.findViewById(R.id.applist_app_version);
		if(aifm.getVersion().equals("Unkown")){
			applist_app_version.setText(R.string.softmanage_version_unknown);
		}else{
			applist_app_version.setText(aifm.getVersion() + mActivity.getString(R.string.softmanage_version_title));
		}
		TextView applist_app_size = (TextView)retView.findViewById(R.id.applist_app_size);
		applist_app_size.setText(new UnitsConversion().defaultConversion(aifm.getSize()));
		
		if(aifm.isInstalled()){
			View applist_right_icon = retView.findViewById(R.id.applist_right_icon);
			applist_right_icon.setVisibility(View.GONE);
			View applist_already_installed_tip = retView.findViewById(R.id.applist_already_installed_tip);
			applist_already_installed_tip.setVisibility(View.VISIBLE);
		}
		
		return retView;
	}

}
