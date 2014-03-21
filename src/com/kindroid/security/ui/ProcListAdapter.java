/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.util.ProcInfo;
import com.kindroid.security.util.UnitsConversion;

public class ProcListAdapter extends BaseAdapter {

	private Context mContext;
	private List<ProcInfo> mListProc;
	private LayoutInflater mInflater;

	public ProcListAdapter(Context context, List<ProcInfo> listProc) {
		mContext = context;
		mListProc = listProc;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mListProc.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mListProc.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.proc_item, null);
		}
		ImageView imageIcon = (ImageView) convertView
				.findViewById(R.id.appImageView);
		imageIcon.setImageDrawable(mListProc.get(position).getIcon(mContext));
		TextView appName = (TextView) convertView
				.findViewById(R.id.appTextView);
		TextView memoryUse = (TextView) convertView
				.findViewById(R.id.memoryTextView);
		appName.setText(mListProc.get(position).getLabelName(mContext));
		String memuse = mContext.getString(R.string.softmanage_use_memory_text)
				+ new UnitsConversion()
						.defaultConversion(Integer.valueOf(mListProc.get(
								position).getMemory(mContext)) * 1024);
		memoryUse.setText(memuse);

		final CheckBox checkbox = (CheckBox) convertView
				.findViewById(R.id.procCheck);
		checkbox.setTag(new Integer(position));

		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mListProc.get((Integer) buttonView.getTag()).setChecked(
						isChecked);
			}
		});
		checkbox.setChecked(mListProc.get(position).getChecked());
		return convertView;
	}
}
