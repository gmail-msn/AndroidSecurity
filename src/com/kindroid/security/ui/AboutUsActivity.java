/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kindroid.security.R;

public class AboutUsActivity extends Activity {

	private LinearLayout linear_bg;
	private TextView soft_vername_tv;

	int type = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about_us_activity);
		linear_bg = (LinearLayout) findViewById(R.id.linear_bg);
		soft_vername_tv = (TextView) findViewById(R.id.soft_version_name_tv);
		
		PackageManager manager = getPackageManager();
		
		try {
			PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			String str=String.format(getString(R.string.soft_version), info.versionName);
			soft_vername_tv.setText(str);
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		String str = getResources().getConfiguration().locale.getLanguage();

		if ("zh".equals(str)) {
			linear_bg.setBackgroundResource(R.drawable.about);
		} else {
			linear_bg.setBackgroundResource(R.drawable.about01);
		}

	}

}