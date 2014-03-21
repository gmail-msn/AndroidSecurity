/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.kindroid.security.R;

public class SoftManageTabActivity extends TabActivity implements
		View.OnClickListener {

	private int current;
	private TabHost mTabHost;
	private LinearLayout installed_linear;
	private LinearLayout apkmanage_linear;
	private LinearLayout apkmanage_left_menu;
	private LinearLayout apkmanage_right_menu;
	private LinearLayout apkmanage_menu_linear;
	private LinearLayout soft_man_tab_linear;
	private View apkmanage_left_menu_icon;
	private TextView apkmanage_left_menu_text;
	private static SoftManageTabActivity instance;
	public static boolean isShowApkmanageMenu = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.soft_man_tab);
		installed_linear = (LinearLayout) findViewById(R.id.installed_linear);
		apkmanage_linear = (LinearLayout) findViewById(R.id.apkmanage_linear);
		apkmanage_left_menu = (LinearLayout) findViewById(R.id.apkmanage_left_menu);
		apkmanage_right_menu = (LinearLayout) findViewById(R.id.apkmanage_right_menu);
		apkmanage_menu_linear = (LinearLayout) findViewById(R.id.apkmanage_menu_linear);
		soft_man_tab_linear = (LinearLayout) findViewById(R.id.soft_man_tab_linear);
		apkmanage_left_menu_text = (TextView) findViewById(R.id.apkmanage_left_menu_text);
		apkmanage_left_menu_icon = findViewById(R.id.apkmanage_left_menu_icon);
		instance = this;
		mTabHost = getTabHost();

		TabHost.TabSpec installedSpec = mTabHost.newTabSpec("installed");
		installedSpec
				.setIndicator(getString(R.string.softmanage_installed_tab));
		Intent intent = new Intent();
		intent.setClass(this, InstalledAppsListActivity.class);
		
		installedSpec.setContent(intent);
		mTabHost.addTab(installedSpec);
		TabHost.TabSpec apkManageSpec = mTabHost.newTabSpec("apkmanage");
		apkManageSpec
				.setIndicator(getString(R.string.softmanage_apkmanage_tab));
		intent = new Intent();
		intent.setClass(this, ApksManageListActivity.class);
		apkManageSpec.setContent(intent);
		mTabHost.addTab(apkManageSpec);

		current = R.id.installed_linear;
		installed_linear.setOnClickListener(this);
		apkmanage_linear.setOnClickListener(this);
		apkmanage_left_menu.setOnClickListener(this);
		apkmanage_right_menu.setOnClickListener(this);

	}

	public static void hideApkmanageMenu() {
		instance.apkmanage_menu_linear.setVisibility(View.GONE);
		instance.soft_man_tab_linear.setVisibility(View.VISIBLE);
		isShowApkmanageMenu = false;
	}

	public static void showApkmanageMenu() {
		instance.apkmanage_menu_linear.setVisibility(View.VISIBLE);
		instance.soft_man_tab_linear.setVisibility(View.GONE);
		isShowApkmanageMenu = true;
	}

	public static void hideTabMenu() {
		instance.soft_man_tab_linear.setVisibility(View.GONE);
	}

	public static void showTabMenu() {
		instance.soft_man_tab_linear.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		current = v.getId();
		switch (v.getId()) {
		case R.id.installed_linear:
			installed_linear.setBackgroundResource(R.drawable.linear_focuse);
			apkmanage_linear.setBackgroundResource(R.drawable.linear_unfocuse);
			mTabHost.setCurrentTab(0);
			break;
		case R.id.apkmanage_linear:
			installed_linear.setBackgroundResource(R.drawable.linear_unfocuse);
			apkmanage_linear.setBackgroundResource(R.drawable.linear_focuse);
			mTabHost.setCurrentTab(1);
			break;
		case R.id.apkmanage_left_menu:
			if (apkmanage_menu_linear.VISIBLE == View.VISIBLE) {
				apkmanage_menu_linear.setVisibility(View.GONE);
			}
			soft_man_tab_linear.setVisibility(View.VISIBLE);
			ApksManageListActivity.changeMenuItem();
			if (ApksManageListActivity.isInstall_Prompt) {
				apkmanage_left_menu_text
						.setText(R.string.softmanage_left_menu_text1);
				apkmanage_left_menu_icon
						.setBackgroundResource(R.drawable.icon_shanchu);
			} else {
				apkmanage_left_menu_text
						.setText(R.string.softmanage_left_menu_text);
				apkmanage_left_menu_icon
						.setBackgroundResource(R.drawable.icon_anzhuang);
			}
			apkmanage_left_menu
					.setBackgroundResource(R.drawable.menu_item_focuse);
			apkmanage_right_menu
					.setBackgroundResource(R.drawable.menu_item_unfocuse);
			break;
		case R.id.apkmanage_right_menu:
			if (apkmanage_menu_linear.VISIBLE == View.VISIBLE)
				apkmanage_menu_linear.setVisibility(View.GONE);
			soft_man_tab_linear.setVisibility(View.VISIBLE);
			ApksManageListActivity.refreshData();
			apkmanage_left_menu
					.setBackgroundResource(R.drawable.menu_item_unfocuse);
			apkmanage_right_menu
					.setBackgroundResource(R.drawable.menu_item_focuse);
			break;
		}

	}

}
