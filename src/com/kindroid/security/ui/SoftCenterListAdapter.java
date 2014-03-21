/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.util.UnitsConversion;

public class SoftCenterListAdapter extends BaseAdapter {
	private Activity mActivity;
	private List<AppInfoForManage> items;
	
	SoftCenterListAdapter(Activity activity){
		this.mActivity = activity;
		this.items = new ArrayList<AppInfoForManage>();
	}
	public void sortItems(Comparator<AppInfoForManage> comparator){
		Collections.sort(items, comparator);
	}
	public void addAll(List<AppInfoForManage> mList){
		this.items.addAll(mList);
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
		if(convertView == null){
			convertView  = this.mActivity.getLayoutInflater().inflate(R.layout.softcenter_recommend_list_item, null);
		}
		final AppInfoForManage aifm = items.get(position);
		ImageView iconView = (ImageView)convertView.findViewById(R.id.softcenter_app_icon);
		if(aifm.getIcon() != null){
			iconView.setImageDrawable(aifm.getIcon());
		}else{
			iconView.setImageDrawable(mActivity.getPackageManager().getDefaultActivityIcon());
		}
		TextView nameView = (TextView)convertView.findViewById(R.id.softcenter_app_label);
		nameView.setText(aifm.getLabel());
		TextView versionView = (TextView)convertView.findViewById(R.id.softcenter_app_version);
		if(aifm.getVersion() != null){
			versionView.setText(aifm.getVersion());				
		}else{
			versionView.setText(R.string.softmanage_version_unknown);
		}
		TextView sizeView = (TextView)convertView.findViewById(R.id.softcenter_app_size);
		sizeView.setText(Html.fromHtml(new UnitsConversion().defaultConversionForHtml(aifm.getSize())));
		TextView isInstalledView = (TextView)convertView.findViewById(R.id.softcenter_already_installed_text);
		if(aifm.isInstalled()){			
			isInstalledView.setVisibility(View.VISIBLE);
		}else{
			isInstalledView.setVisibility(View.GONE);
		}
		//set price
		TextView priceText = (TextView)convertView.findViewById(R.id.price_text);
		priceText.setText(aifm.getUnit());
		
		return convertView;
	}

}
