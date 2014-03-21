/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: jie.li
 * Date: 2011-07
 * Description:
 */

package com.kindroid.security.adapter;

import com.kindroid.security.R;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GridListAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater layInflater;
	String text[];
	int id[];
	private List<String> mText;
	private List<Integer> mId;

	public GridListAdapter(Activity context, String text[], int id[]) {
		this.context = context;
		this.text = text;
		this.id = id;
		layInflater = LayoutInflater.from(context);

	}
	public GridListAdapter(Activity context, List<String> mText, List<Integer> mId) {
		this.context = context;
		this.mId = mId;
		this.mText = mText;
		layInflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		/*
		if (id == null || text == null)
			return 0;
		if (id.length != text.length)
			return 0;
		return id.length;
		*/
		if (mId == null || mText == null)
			return 0;
		if (mId.size() != mText.size())
			return 0;
		return mId.size();
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
			convertView = layInflater.inflate(R.layout.grid_item, null);
		}
		TextView tv_bg = (TextView) convertView.findViewById(R.id.tv_bg);
		TextView tv_me = (TextView) convertView.findViewById(R.id.tv_me);
//		tv_bg.setBackgroundResource(id[position]);
//		tv_me.setText(text[position]);
		tv_bg.setBackgroundResource((int)(mId.get(position)));
		tv_me.setText(mText.get(position));

		return convertView;
	}

}
