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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.util.UnitsConversion;

public class SoftManageListAdapter extends BaseAdapter {
	private Activity mActivity;
	private List<AppInfoForManage> items;
	public boolean mOnlyStateChange = false;

	SoftManageListAdapter(Activity activity) {
		this.mActivity = activity;
		this.items = new ArrayList<AppInfoForManage>();
	}

	public void setSelectedAll(boolean checked) {
		for (AppInfoForManage aifm : items) {
			aifm.setSelected(checked);
		}
	}

	public void setSelectedAllForInstall(boolean checked) {
		for (AppInfoForManage aifm : items) {
			// if(!aifm.isInstalled())
			aifm.setSelected(checked);
		}

	}
	public List<AppInfoForManage> getItems(){
		return this.items;
	}
	public void setItems(List<AppInfoForManage> items){
		this.items = items;
	}

	public void sortItems(int flag, boolean desc) {
		final boolean tDesc = desc;
		switch (flag) {
		case 0:
			List<AppInfoForManage> tempList = new ArrayList<AppInfoForManage>();
			Collections.sort(items, new Comparator<AppInfoForManage>() {
				@Override
				public int compare(AppInfoForManage object1,
						AppInfoForManage object2) {
					// TODO Auto-generated method stub
					if (tDesc) {
						return object1.getSize() > object2.getSize() ? -1 : 1;
					} else {
						return object2.getSize() > object1.getSize() ? -1 : 1;
					}
				}

			});
			break;
		case 1:
			tempList = new ArrayList<AppInfoForManage>();
			Collections.sort(items, new Comparator<AppInfoForManage>() {
				@Override
				public int compare(AppInfoForManage object1,
						AppInfoForManage object2) {
					// TODO Auto-generated method stub
					Locale locale = mActivity.getResources().getConfiguration().locale;
					Collator myCollator = Collator.getInstance(locale);
					String label1 = object1.getLabel().toString().trim();

					String label2 = object2.getLabel().toString().trim();

					if (tDesc) {
						if (myCollator.compare(label2, label1) < 0) {
							return -1;
						} else if (myCollator.compare(label2, label1) > 0) {
							return 1;
						} else {
							return 0;
						}
					} else {
						if (myCollator.compare(label1, label2) < 0) {
							return -1;
						} else if (myCollator.compare(label1, label2) > 0) {
							return 1;
						} else {
							return 0;
						}
					}
				}

			});
			break;
		}
	}

	public void addItem(AppInfoForManage aifm) {
		this.items.add(aifm);
	}

	public void deleteItem(int index) {
		this.items.remove(index);
	}

	public void delteItem(AppInfoForManage aifm) {
		this.items.remove(aifm);
	}

	public void clearItems() {
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
			convertView = this.mActivity.getLayoutInflater().inflate(
				R.layout.softmanage_list_item, null);
		}
		final AppInfoForManage aifm = items.get(position);
		ImageView iconView = (ImageView) convertView
				.findViewById(R.id.softmanage_app_icon);
		iconView.setImageDrawable(aifm.getIcon());
		TextView nameView = (TextView) convertView
				.findViewById(R.id.softmanage_app_label);
		nameView.setText(aifm.getLabel());
		if (aifm.getFlag() == 0) {
			TextView versionView = (TextView) convertView
					.findViewById(R.id.softmanage_app_version);
			versionView.setVisibility(View.GONE);
		} else {
			TextView versionView = (TextView) convertView
					.findViewById(R.id.softmanage_app_version);
			if (aifm.getVersion() != null) {
				versionView.setText(aifm.getVersion());
			} else {
				versionView.setText(R.string.softmanage_version_unknown);
			}
			versionView.setVisibility(View.VISIBLE);
		}
		TextView sizeView = (TextView) convertView
				.findViewById(R.id.softmanage_app_size);
		sizeView.setText(Html.fromHtml(mActivity
				.getString(R.string.softmanage_use_memory_text)
				+ new UnitsConversion().defaultConversionForHtml(aifm.getSize())));
		CheckBox actionView = (CheckBox) convertView
				.findViewById(R.id.softmanage_action_checkbox);

		if (aifm.getFlag() != 0) {
			if (aifm.isInstalled()) {
				TextView isInstalledView = (TextView) convertView
						.findViewById(R.id.softmanage_already_installed_tip);
				isInstalledView.setVisibility(View.VISIBLE);
			}else{
				TextView isInstalledView = (TextView) convertView.findViewById(R.id.softmanage_already_installed_tip);
				isInstalledView.setVisibility(View.GONE);
			}
			actionView
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							aifm.setSelected(isChecked);
							if(mOnlyStateChange){
								return;
							}
							ApksManageListActivity amla = (ApksManageListActivity) mActivity;
							if(!isChecked){
								try{
									amla.changeAllCb(isChecked);
								}catch(Exception e){
									e.printStackTrace();
								}								
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
										amla.changeAllCb(true);
									}catch(Exception e){
										e.printStackTrace();
									}
									mOnlyStateChange = false;
								}
							}
							
						}
					});
			actionView.setChecked(aifm.isSelected());

		} else {
			if (actionView.VISIBLE != View.VISIBLE)
				actionView.setVisibility(View.VISIBLE);

			actionView
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							aifm.setSelected(isChecked);
						}
					});
			actionView.setChecked(aifm.isSelected());
			if (aifm.isUseCost()) {
				convertView.findViewById(R.id.audit_cost_icon).setVisibility(
						View.VISIBLE);
			}else{
				convertView.findViewById(R.id.audit_cost_icon).setVisibility(
						View.GONE);
			}
			if (aifm.isUseGps()) {
				convertView.findViewById(R.id.audit_gps_icon).setVisibility(
						View.VISIBLE);
			}else{
				convertView.findViewById(R.id.audit_gps_icon).setVisibility(
						View.GONE);
			}
			if (aifm.isUseNetwork()) {
				convertView.findViewById(R.id.audit_network_icon).setVisibility(
						View.VISIBLE);
			}else{
				convertView.findViewById(R.id.audit_network_icon).setVisibility(
						View.GONE);
			}
			if (aifm.isUsePrivacy()) {
				convertView.findViewById(R.id.audit_privacy_icon).setVisibility(
						View.VISIBLE);
			}else{
				convertView.findViewById(R.id.audit_privacy_icon).setVisibility(
						View.GONE);
			}
		}

		return convertView;
	}

}
