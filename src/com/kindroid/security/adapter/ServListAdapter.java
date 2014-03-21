/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.model.ServInfo;

public class ServListAdapter extends BaseAdapter {
	private Context mContext;
	private List<ServInfo> mListProc;
	
	public ServListAdapter(Context context, List<ServInfo> listProc)
	{
		mContext = context;
		mListProc = listProc;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListProc.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mListProc.get(position);
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
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
			convertView = inflater.inflate(R.layout.proc_item, null);
		}
		ImageView imageIcon = (ImageView) convertView.findViewById(R.id.appImageView);
		imageIcon.setImageDrawable(mListProc.get(position).getIcon(mContext));
		TextView appName = (TextView) convertView.findViewById(R.id.appTextView);
		TextView memoryUse = (TextView) convertView.findViewById(R.id.memoryTextView);
		appName.setText(mListProc.get(position).getLabelName());
		memoryUse.setText(mContext.getResources().getString(R.string.service_run_in).toString() + mListProc.get(position).getAppName());
		CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.procCheck);
		checkbox.setVisibility(View.GONE);
		convertView.findViewById(R.id.dataImageView).setVisibility(View.VISIBLE);		
		
		return convertView;
	}

}
