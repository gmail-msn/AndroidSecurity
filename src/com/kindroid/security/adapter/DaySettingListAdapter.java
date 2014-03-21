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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.model.DayListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heli.zhao
 *
 */
public class DaySettingListAdapter extends BaseAdapter {
	private Context mContext;
	private List<DayListItem> mItems;
	
	public DaySettingListAdapter(Context context){
		this.mContext = context;
		mItems = new ArrayList<DayListItem>();
	}
	public void addItem(DayListItem item){
		mItems.add(item);
	}
	public void delItem(int index){
		mItems.remove(index);
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
			convertView = inflater.inflate(R.layout.day_setting_item, null);
		}
		final DayListItem item = (DayListItem)mItems.get(position);
		TextView mDayTv = (TextView)convertView.findViewById(R.id.day_name);
		mDayTv.setText(item.getName());
		final CheckBox mSelectCb = (CheckBox)convertView.findViewById(R.id.select_cb);
		mSelectCb.setChecked(item.isSelected());
		/*
		mSelectCb.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				item.setSelected(isChecked);
			}
			
		});
		
		convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mSelectCb.setChecked(!mSelectCb.isChecked());				
			}
		});
		*/
		return convertView;
	}

}
