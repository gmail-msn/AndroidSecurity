/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.07
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kindroid.security.R;

public class HelpActivity extends Activity {

	TextView introduction_tv;
	TextView introduction_des_tv;
	TextView main_func_des_tv;
	LinearLayout main_linear;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.help);
		findView();
		String introduction = getResources().getString(R.string.help_introduction)
				+ "  " + ": ";
		introduction_tv.setText(introduction);
		introduction_tv.setTextSize(18);
		introduction_tv.setTextColor(Color.WHITE);

		introduction_des_tv.setText(R.string.introduct_des);
		introduction_des_tv.setTextColor(getResources().getColor(
				R.color.light_black));
		introduction_des_tv.setPadding(16, 10, 10, 8);

		main_func_des_tv.setText(R.string.main_fuc_des);
		main_func_des_tv.setTextColor(Color.WHITE);
		main_func_des_tv.setTextSize(18);
		String title[] = getResources().getStringArray(R.array.help_title);
		String content[] = getResources().getStringArray(R.array.help_content);
		if (title.length != content.length)
			return;
		for (int i = 0; i < content.length; i++) {
			addView(title[i], content[i]);
		}

	}

	void addView(String title, String content) {
		TextView title_tv = new TextView(this);
		title_tv.setText(title);
		title_tv.setTextSize(18);
		title_tv.setTextColor(getResources().getColor(R.color.green));
		title_tv.setPadding(16, 12, 16, 16);
		main_linear.addView(title_tv, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView content_tv = new TextView(this);
		content_tv.setText(content);
		content_tv.setTextColor(getResources().getColor(R.color.light_black));
		content_tv.setPadding(16, 0, 16, 16);
		main_linear.addView(content_tv, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		View view = new View(this);
		view.setBackgroundResource(R.drawable.virus_scan_line01);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT, 1);

		main_linear.addView(view, lp);

	}

	void findView() {
		introduction_tv = (TextView) findViewById(R.id.introduction_tv);
		introduction_des_tv = (TextView) findViewById(R.id.introduction_des_tv);
		main_func_des_tv = (TextView) findViewById(R.id.main_func_des_tv);
		main_linear = (LinearLayout) findViewById(R.id.main_linear);
	}

}