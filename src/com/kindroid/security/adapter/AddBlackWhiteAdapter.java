/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: jie.li
 * Date: 2011-07
 * Description:
 */

package com.kindroid.security.adapter;

import com.kindroid.security.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AddBlackWhiteAdapter extends BaseAdapter {
	//private Context context;
	private LayoutInflater layInflater;
	String mText[];

	public AddBlackWhiteAdapter(Activity context, String text[]) {
		//this.context = context;
		this.mText = text;

		layInflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mText == null) {
			return 0;
		} else {
			return mText.length;
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = layInflater.inflate(R.layout.add_list_item, null);
		}
		TextView mHandleTv= (TextView) convertView.findViewById(R.id.handle_tv);
		mHandleTv.setText(mText[position]);
		return convertView;
	}
}
