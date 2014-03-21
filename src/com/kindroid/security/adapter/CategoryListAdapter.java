/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.model.CategoryInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heli.zhao
 *
 */
public class CategoryListAdapter extends BaseAdapter {
	private Activity mActivity;
	private List<CategoryInfo> items;
	private LayoutInflater mLayoutFlater;
	
	public CategoryListAdapter(Activity mActivity){
		this.mActivity = mActivity;
		items = new ArrayList<CategoryInfo>();
		mLayoutFlater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public void addAll(List<CategoryInfo> mList){
		this.items.addAll(mList);
	}
	public void addItem(CategoryInfo aifm){
		this.items.add(aifm);
	}
	
	public void deleteItem(int index){
		this.items.remove(index);
	}
	
	public void delteItem(CategoryInfo aifm){
		this.items.remove(aifm);
	}
	
	public void clearItems(){
		this.items.clear();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return items.get(position);
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
			convertView = mLayoutFlater.inflate(R.layout.softcenter_category_list_item, null);
		}
		CategoryInfo item = items.get(position);
		ImageView mIcon = (ImageView)convertView.findViewById(R.id.category_icon);
		if(item.getIcon() != null){
			mIcon.setImageDrawable(item.getIcon());
		}
		TextView mNameTv = (TextView)convertView.findViewById(R.id.category_name_tv);
		if(item.getName() != null){
			mNameTv.setText(item.getName());
		}
				
		return convertView;
	}

}
