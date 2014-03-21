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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.ehoo.client.request.IRequest;
import com.ehoo.client.request.Request;
import com.kindroid.security.R;

public class SoftCenterTabActivity extends TabActivity implements
		OnClickListener {
	private TabHost mTabHost;
	private LinearLayout recommend_tab_linear;
	private LinearLayout category_tab_linear;
	private LinearLayout search_tab_linear;
	private TabSpec mRecommendTab;
	private TabSpec mCategoryTab;
	private TabSpec mSearchTab;	
	public static IRequest request = null;
	public static Boolean initReuqest = false; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.softcenter_tab);
		View home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent homepage = new Intent(SoftCenterTabActivity.this,
						DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
			}
		});
		mTabHost = this.getTabHost();
		recommend_tab_linear = (LinearLayout)findViewById(R.id.recommend_tab_linear);
		recommend_tab_linear.setOnClickListener(this);
		category_tab_linear = (LinearLayout)findViewById(R.id.category_tab_linear);
		category_tab_linear.setOnClickListener(this);
		search_tab_linear = (LinearLayout)findViewById(R.id.search_tab_linear);
		search_tab_linear.setOnClickListener(this);
		Intent rIntent = new Intent(this, SoftCenterRecommendActivity.class);
		Intent cIntent = new Intent(this, SoftCenterTopicsActivity.class);
		Intent sIntent = new Intent(this, SoftCenterSearchActivity.class);
		mRecommendTab = mTabHost.newTabSpec("mRecommendTab").setIndicator("mRecommendTab").setContent(rIntent);
		mCategoryTab = mTabHost.newTabSpec("mCategoryTab").setIndicator("mCategoryTab").setContent(cIntent);
		mSearchTab = mTabHost.newTabSpec("mSearchTab").setIndicator("mSearchTab").setContent(sIntent);
		mTabHost.addTab(mRecommendTab);
		mTabHost.addTab(mCategoryTab);
		mTabHost.addTab(mSearchTab);
		/*
		synchronized(SoftCenterTabActivity.initReuqest){
			if(initReuqest == false){
				request = new Request();
				request.init(true);
				initReuqest = true;
				Log.d("cache", "create and init request");
			}
		}
		*/
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		/*
		synchronized(SoftCenterTabActivity.initReuqest){
			if(initReuqest == true){
				request.close();
				request = null;
				initReuqest = false;
				Log.d("cache", "close request");
			}
		}
		*/
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.recommend_tab_linear:
			recommend_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_on);
			category_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_bg);
			search_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_bg);
			mTabHost.setCurrentTab(0);
			break;
		case R.id.category_tab_linear:
			recommend_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_bg);
			category_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_on);
			search_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_bg);
			mTabHost.setCurrentTab(1);
			break;
		case R.id.search_tab_linear:
			recommend_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_bg);
			category_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_bg);
			search_tab_linear.setBackgroundResource(R.drawable.softcenter_tab_on);
			mTabHost.setCurrentTab(2);
			break;
		}
	}


}
