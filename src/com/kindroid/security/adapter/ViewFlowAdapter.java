/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.adapter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.kindroid.security.R;
import com.kindroid.security.ui.BackupTabActivity;
import com.kindroid.security.ui.BlockTabMain;
import com.kindroid.security.ui.CacheClearActivity;
import com.kindroid.security.ui.FirewallActivity;
import com.kindroid.security.ui.MobileExamActivity;
import com.kindroid.security.ui.NetTrafficTabMain;
import com.kindroid.security.ui.RemoteSecurityTabActivity;
import com.kindroid.security.ui.SoftManageTabActivity;
import com.kindroid.security.ui.StartManageActivity;
import com.kindroid.security.ui.TaskManageTabActivity;
import com.kindroid.security.ui.UseFucActivity;
import com.kindroid.security.ui.VirusScanTabActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heli.zhao
 *
 */
public class ViewFlowAdapter extends BaseAdapter {
	private Activity mContext;
	private LayoutInflater mLayoutInflater;
	private String text[];
	private int id[];
	private static List<GridListAdapter> mListItems;
	private static List<String[]> mTextList;
	private static List<int[]> mIntList;
	private static int mScreenHeight;
	private static float mScreenDensity = 1;
	private static int mDefaultExtraSpace = 120;
	private static int mDefaultRowHeight = 100;
	private static int mRowNum = 3;
	private static int mStatusTopHeight;
	private static int mPageNum = 1;
	
	public ViewFlowAdapter(Activity context, String text[], int id[]){
		this.mContext = context;
		this.text = text;
		this.id = id;
		mLayoutInflater = LayoutInflater.from(context);
		if(mListItems == null || mListItems.size() == 0){
			initAdapter();
		}
	}
	public void initAdapter(){
		DisplayMetrics metric = new DisplayMetrics();
		mContext.getWindowManager().getDefaultDisplay().getMetrics(metric);
		mScreenDensity = metric.density;
		mScreenHeight = metric.heightPixels;		
		View mContentView = mContext.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
		Rect rect = new Rect();
		mContentView.getWindowVisibleDisplayFrame(rect);
		int statusTop = rect.top;
		if(statusTop <= 0){
			statusTop = 25;
		}
		int mUseHeight = mScreenHeight - (int)(mScreenDensity * (mDefaultExtraSpace + statusTop));
		int mRowHeight = (int)(mDefaultRowHeight * mScreenDensity);
		mRowNum = mUseHeight / mRowHeight;
		mPageNum = id.length / (mRowNum * 3) + 1;
		mListItems = new ArrayList<GridListAdapter>();
		for(int i = 0; i < mPageNum; i++){
			int size = id.length - (3*i*mRowNum);
			if(size > 0){
				if(size >= mRowNum * 3){
					List<String> mText = new ArrayList<String>();
					List<Integer> mId = new ArrayList<Integer>();
					for(int j = 3*i*mRowNum; j < 3*i*mRowNum + mRowNum * 3; j++){
						mText.add(text[j]);
						mId.add(id[j]);
					}
					GridListAdapter mGridAdapter = new GridListAdapter(mContext, mText, mId);
					mListItems.add(mGridAdapter);
				}else{
					List<String> mText = new ArrayList<String>();
					List<Integer> mId = new ArrayList<Integer>();
					for(int j = 3*i*mRowNum; j < id.length; j++){
						mText.add(text[j]);
						mId.add(id[j]);
					}
					GridListAdapter mGridAdapter = new GridListAdapter(mContext, mText, mId);
					mListItems.add(mGridAdapter);
				}
			}
		}
		
	}
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListItems.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mListItems.get(position);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView == null){
			convertView = mLayoutInflater.inflate(R.layout.vf_list_item, null);
		}
		GridView mGridView = (GridView)convertView.findViewById(R.id.gridView);
		mGridView.setAdapter(mListItems.get(position));
		final int pageIndex = position;
		mGridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				itemClick(pageIndex * mRowNum * 3 + position);
			}
			
		});
		return convertView;
	}
	private void itemClick(int fucIndex){
		switch (fucIndex) {
		case 0:
			mContext.startActivity(new Intent(mContext,
					NetTrafficTabMain.class));
		
			break;
		case 1:
			mContext.startActivity(new Intent(mContext,
					BlockTabMain.class));
			
			
			break;
		case 2:
			mContext.startActivity(new Intent(mContext,
					VirusScanTabActivity.class));
			
			break;
		case 3:

			mContext.startActivity(new Intent(mContext,
					SoftManageTabActivity.class));

			break;
		case 4:
			mContext.startActivity(new Intent(mContext,
					TaskManageTabActivity.class));
			

			break;

		case 5:
			
			try {
				Intent i = new Intent();
				ComponentName com = new ComponentName("com.android.settings",
						"com.android.settings.fuelgauge.PowerUsageSummary");
				i.setComponent(com);
				mContext.startActivity(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			break;
		case 6:
			mContext.startActivity(new Intent(mContext,
					BackupTabActivity.class));

			break;
		case 7:
			mContext.startActivity(new Intent(mContext,
					RemoteSecurityTabActivity.class));
			break;
		case 8:
			mContext.startActivity(new Intent(mContext, CacheClearActivity.class));
			break;
		case 9:
			mContext.startActivity(new Intent(mContext, StartManageActivity.class));
			break;
		case 10:
			mContext.startActivity(new Intent(mContext, MobileExamActivity.class));
			break;
		case 11:
			mContext.startActivity(new Intent(mContext, FirewallActivity.class));
			break;
		default:
			break;
		}
	}

}
