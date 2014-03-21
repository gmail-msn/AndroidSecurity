/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.08
 * Description:
 */
package com.kindroid.security.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import com.kindroid.security.R;
import com.kindroid.security.util.AppItemDetail;
import com.kindroid.security.util.MemoryUtil;

public class AppTraficItemAdapter extends BaseAdapter {

	private Context mContext;

	LayoutInflater inflater;
	List<Map.Entry<String, AppItemDetail>> infoIds;
	PackageManager pm;

	public AppTraficItemAdapter(Context context,
			List<Map.Entry<String, AppItemDetail>> infoIds) {
		mContext = context;
		inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.infoIds = infoIds;
		pm = mContext.getPackageManager();
	}

	@Override
	public int getCount() {

		if (infoIds == null)
			return 0;

		return infoIds.size();
	}

	@Override
	public Object getItem(int arg0) {
		return infoIds.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = inflater.inflate(R.layout.app_traffic_item, null);
		}

		ImageView appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
		TextView app_name = (TextView) convertView.findViewById(R.id.app_name);
		TextView app_traffic_send = (TextView) convertView
				.findViewById(R.id.traffic_send_tv);
		TextView app_traffic_down = (TextView) convertView
				.findViewById(R.id.traffic_down_tv);
		TextView app_traffic_all = (TextView) convertView
				.findViewById(R.id.traffic_all_tv);

		AppItemDetail app = infoIds.get(position).getValue();

		if (app == null)
			return convertView;

		try {
			PackageInfo pi = pm.getPackageInfo(app.getPkg(),
					PackageManager.GET_SIGNATURES);
			Drawable drawable = pm.getApplicationIcon(app.getPkg());
			appIcon.setBackgroundDrawable(drawable);
			app_name.setText(pi.applicationInfo.loadLabel(pm).toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			app_name.setText(app.getPkg());
			appIcon.setBackgroundResource(R.drawable.icon_default);
			e.printStackTrace();
		}
		app_traffic_send.setText(MemoryUtil.formatMemoryBySize(1, app.getTx()));
		app_traffic_down.setText(MemoryUtil.formatMemoryBySize(1, app.getRx()));
		app_traffic_all.setText(MemoryUtil.formatMemoryBySize(1, app.getTotal()));
		return convertView;

	}
}
