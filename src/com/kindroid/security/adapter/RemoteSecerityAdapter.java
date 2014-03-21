/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.08
 * Description:
 */

package com.kindroid.security.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.RemoteSecurityModel;

public class RemoteSecerityAdapter extends BaseAdapter {

	private Context mContext;
	private int type;
	LayoutInflater inflater;
	List<RemoteSecurityModel> list;
	private ListView listView;

	public RemoteSecerityAdapter(Context context,
			List<RemoteSecurityModel> list, int type, ListView listView) {
		mContext = context;
		inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.type = type;
		this.list = list;
		this.listView = listView;
	}

	@Override
	public int getCount() {
		if (list == null)
			return 0;
		return list.size();

	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = inflater.inflate(R.layout.remote_security_item, null);
		}

		ImageView imageIcon = (ImageView) convertView
				.findViewById(R.id.appImageView);

		RelativeLayout.LayoutParams rl = (android.widget.RelativeLayout.LayoutParams) imageIcon
				.getLayoutParams();
		rl.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
		rl.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
		rl.addRule(RelativeLayout.CENTER_VERTICAL);
		imageIcon.setLayoutParams(rl);
		TextView appName = (TextView) convertView
				.findViewById(R.id.appTextView);

		TextView remote_security_status = (TextView) convertView
				.findViewById(R.id.remote_security_status);
		TextView des = (TextView) convertView.findViewById(R.id.memoryTextView);

		TextView safeMobileStatusTv = (TextView) convertView
				.findViewById(R.id.safe_mobile_status_tv);
		TextView safeMobileNumber = (TextView) convertView
				.findViewById(R.id.safe_mobile_number_tv);

		ImageView datImage = (ImageView) convertView
				.findViewById(R.id.dataImageView);
		TextView checkbox = (TextView) convertView.findViewById(R.id.procCheck);

		appName.setTextSize(20);
		des.setTextSize(14);
		des.setTextColor(Color.rgb(179, 179, 179));

		checkbox.setVisibility(View.INVISIBLE);
		datImage.setVisibility(View.INVISIBLE);
		safeMobileStatusTv.setVisibility(View.GONE);
		safeMobileNumber.setVisibility(View.GONE);
		if (type == 1) {
			imageIcon.setImageDrawable(mContext.getResources().getDrawable(
					list.get(position).getIconId()));
			appName.setText(list.get(position).getAppName());
			des.setText(list.get(position).getDes());
			if (position == 1) {
				checkbox.setVisibility(View.VISIBLE);
				boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
						Constant.SHAREDPREFERENCES_AFTERUPDATESIMTOLOCKMOBILE, false);
				if(isTrue){
					checkbox.setBackgroundResource(R.drawable.checkbox_true);
				}else{
					checkbox.setBackgroundResource(R.drawable.checkbox_false);
				}
				return convertView;

			}

			if (position != 0)
				return convertView;
			checkbox.setVisibility(View.VISIBLE);
			boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
					Constant.SHAREDPREFERENCES_REMOTESECURITY, false);
			des.setVisibility(View.GONE);
			if (isTrue) {
				checkbox.setBackgroundResource(R.drawable.checkbox_true);
				safeMobileStatusTv.setVisibility(View.VISIBLE);
				safeMobileNumber.setVisibility(View.VISIBLE);
				remote_security_status.setText(R.string.open);
				safeMobileStatusTv.setText(R.string.safe_mobile_has_set);
				String str = mContext.getResources().getString(
						R.string.safe_mobile_number)
						+ ":";
				String safe_number = KindroidSecurityApplication.sh.getString(
						Constant.SHAREDPREFERENCES_SAFEMOBILENUMBER, "");
				str += "<font color=#64BD45>" + safe_number + "</font>";
				safeMobileNumber.setText(Html.fromHtml(str));
			} else {
				checkbox.setBackgroundResource(R.drawable.checkbox_false);
				remote_security_status.setText(R.string.close);
				safeMobileStatusTv.setText(R.string.safe_mobile_has_noset);
				safeMobileStatusTv.setVisibility(View.VISIBLE);

			}

			return convertView;
		}
		
		return convertView;

	}
}
