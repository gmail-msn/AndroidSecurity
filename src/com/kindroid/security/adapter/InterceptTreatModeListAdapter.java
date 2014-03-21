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

/**
 * @author heli.zhao
 *
 */
public class InterceptTreatModeListAdapter extends BaseAdapter {
	private Context mContext;
	private String[] mTreatModeNames;
	private boolean[] mTreatModeSelected;
	
	public InterceptTreatModeListAdapter(Context context){
		mContext = context;
		mTreatModeNames = mContext.getResources().getStringArray(R.array.intercept_tream_mode);
		mTreatModeSelected = new boolean[mTreatModeNames.length];
		for(int i = 0; i < mTreatModeNames.length; i++){
			mTreatModeSelected[i] = false;
		}
	}
	public void setPosition(int position){
		for(int i = 0; i < mTreatModeSelected.length; i++){
			if(i == position){
				mTreatModeSelected[i] = true;
			}else{
				mTreatModeSelected[i] = false;
			}
		}
	}
	public int getSelected(){
		int ret = 0;
		for(int i = 0; i < mTreatModeSelected.length; i++){
			if(mTreatModeSelected[i]){
				ret = i;
				break;
			}
		}
		return ret;
	}
	public String getSelectedModeName(int index){
		return mTreatModeNames[index];
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mTreatModeNames.length;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mTreatModeNames[position];
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
			convertView = inflater.inflate(R.layout.treat_mode_list_item, null);
		}
		TextView mTitle = (TextView)convertView.findViewById(R.id.mode_title_tv);
		mTitle.setText(mTreatModeNames[position]);
		RadioButton radio_button = (RadioButton)convertView.findViewById(R.id.radio_button);
		radio_button.setChecked(mTreatModeSelected[position]);
		return convertView;
	}
	

}
