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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.ArrayList;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;

public class VirusCleanListAdapter extends BaseAdapter {
	private Activity mActivity;
	private List<AppInfoForManage> items;
	public boolean mOnlyStateChange = false;
	
	VirusCleanListAdapter(Activity activity){
		this.mActivity = activity;
		this.items = new ArrayList<AppInfoForManage>();
	}
	public void setSelectedAll(boolean checked){
		for(AppInfoForManage aifm:items){
			if(aifm.isInstalled())
				aifm.setSelected(checked);
		}
	}
	public void setSelectedAllForUninstall(boolean checked){
		for(AppInfoForManage aifm:items){
			if(aifm.isInstalled())
				aifm.setSelected(checked);
		}		
			
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
			convertView = this.mActivity.getLayoutInflater().inflate(R.layout.softmanage_list_item, null);
		}
		final AppInfoForManage aifm = items.get(position);
		ImageView iconView = (ImageView)convertView.findViewById(R.id.softmanage_app_icon);
		if(aifm.isInstalled()){
			iconView.setImageDrawable(aifm.getIcon());
		}else{
			iconView.setImageResource(R.drawable.icon_default);
		}
		TextView nameView = (TextView)convertView.findViewById(R.id.softmanage_app_label);
		nameView.setText(aifm.getLabel());
		TextView versionView = (TextView)convertView.findViewById(R.id.softmanage_app_version);
		if(aifm.getVersion() != null){
			versionView.setText(aifm.getVersion());				
		}else{
			versionView.setText(R.string.softmanage_version_unknown);
		}
		versionView.setVisibility(View.VISIBLE);
		TextView sizeView = (TextView)convertView.findViewById(R.id.softmanage_app_size);
		sizeView.setText(aifm.getDescription());
		CheckBox actionView = (CheckBox)convertView.findViewById(R.id.softmanage_action_checkbox);
		
		if(!aifm.isInstalled()){
			actionView.setVisibility(View.GONE);
			TextView isInstalledView = (TextView)convertView.findViewById(R.id.softmanage_already_installed_tip);
			isInstalledView.setText(R.string.virus_clean_uninstalled);
			isInstalledView.setVisibility(View.VISIBLE);
		}else{
			if(actionView.getVisibility() != View.VISIBLE)
				actionView.setVisibility(View.VISIBLE);
			
			actionView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					aifm.setSelected(isChecked);
					if(!isChecked){
						mOnlyStateChange = true;
						try{
							CheckBox select_al_cb = (CheckBox)mActivity.findViewById(R.id.select_al_cb);
							select_al_cb.setChecked(isChecked);
						}catch(Exception e){
							e.printStackTrace();
						}
						mOnlyStateChange = false;
					}else{
						boolean select_al = true;
						for(AppInfoForManage aifm : items){
							if(!aifm.isSelected()){
								select_al = false;
								break;
							}
						}
						if(select_al){
							mOnlyStateChange = true;
							try{
								CheckBox select_al_cb = (CheckBox)mActivity.findViewById(R.id.select_al_cb);
								select_al_cb.setChecked(isChecked);
							}catch(Exception e){
								e.printStackTrace();
							}
							mOnlyStateChange = false;
						}
					}
					
				}
			});
			actionView.setChecked(aifm.isSelected());
		}
	
		
		return convertView;
	}

}
