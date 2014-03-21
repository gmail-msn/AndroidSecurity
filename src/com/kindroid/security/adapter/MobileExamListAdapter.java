/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.data.MobileExamItem;
import com.kindroid.security.model.AutoStartAppItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heli.zhao
 *
 */
public class MobileExamListAdapter extends BaseAdapter {
	private Context mContext;
	private List<MobileExamItem> mListItems;
	private LayoutInflater mLayoutFlater;
	private boolean mIsExaming = true;
	
	public MobileExamListAdapter(Context context){
		mContext = context;
		mListItems = new ArrayList<MobileExamItem>();
		mLayoutFlater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public int getItemIndex(MobileExamItem item){
		return mListItems.indexOf(item);
	}
	public boolean isIsExaming() {
		return mIsExaming;
	}
	public void setIsExaming(boolean mIsExaming) {
		this.mIsExaming = mIsExaming;
	}
	public List<MobileExamItem> getItems(){
		return mListItems;
	}
	public void clear(){
		mListItems.clear();
	}
	public void addItem(MobileExamItem item){
		mListItems.add(item);
	}
	public void delItem(MobileExamItem item){
		mListItems.remove(item);
	}
	public void delItem(int index){
		mListItems.remove(index);
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
			convertView = mLayoutFlater.inflate(R.layout.mobile_exam_list_item, null);
		}
		final MobileExamItem item = mListItems.get(position);
		TextView mItemDesp = (TextView)convertView.findViewById(R.id.mobile_exam_item_desp);
		TextView mSafeActionTv = (TextView)convertView.findViewById(R.id.safe_action_tv);
		View mDangerActionLinear = convertView.findViewById(R.id.danger_action_linear);
		TextView mDangerActioinTv = (TextView)convertView.findViewById(R.id.danger_action_tv);
		if(item.getItemStatus()> 1){
			mItemDesp.setText(item.getDangerDesp());
		}else{
			mItemDesp.setText(item.getSafeDesp());
		}
		if(mIsExaming){
			mItemDesp.setTextColor(R.color.grey);
			mSafeActionTv.setVisibility(View.GONE);
			mDangerActionLinear.setVisibility(View.GONE);
		}else{
			mItemDesp.setTextColor(R.color.black);
			if(item.getItemStatus()> 1){
				mSafeActionTv.setVisibility(View.GONE);
				mDangerActioinTv.setText(item.getDangerAction());
				mDangerActionLinear.setVisibility(View.VISIBLE);
				mDangerActionLinear.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						item.forwardOptimizeAction();
					}
				});
			}else{
				mDangerActionLinear.setVisibility(View.GONE);
				mSafeActionTv.setText(item.getSafeAction());
				mSafeActionTv.setVisibility(View.VISIBLE);
			}
		}
		return convertView;
	}

}
