/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.data;

import android.app.Activity;
import android.content.Context;

import com.kindroid.security.R;
import com.kindroid.security.model.AutoStartAppItem;
import com.kindroid.security.ui.MobileExamActivity;
import com.kindroid.security.ui.StartManageActivity;
import com.kindroid.security.ui.TaskManageTabActivity;
import com.kindroid.security.util.RunCMDUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heli.zhao
 *
 */
public class AutoStartOnBootItem implements MobileExamItem {
	private MobileExamActivity mContext;
	private int mItemStatus = MobileExamItem.SAFE_ITEM;
	private List<AutoStartAppItem> mAutoStartListItems;
	private String mDangerAction;
	private String mSafeAction;
	
	public AutoStartOnBootItem(MobileExamActivity context){
		this.mContext = context;
		this.mAutoStartListItems = new ArrayList<AutoStartAppItem>();
	}
	
	public void addOptimizeItem(AutoStartAppItem item){
		this.mAutoStartListItems.add(item);
	}
	public void delOptimizeItem(AutoStartAppItem item){
		this.mAutoStartListItems.remove(item);
	}
	public void clearOptimizeItems(){
		this.mAutoStartListItems.clear();
	}
	public AutoStartAppItem getOptimizeItem(int index){
		return mAutoStartListItems.get(index);
	}
	public int getOptimizeItemSum(){
		return mAutoStartListItems.size();
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getSafeDesp()
	 */
	@Override
	public String getSafeDesp() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.safe_desp_for_autostart_onboot);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDangerDesp()
	 */
	@Override
	public String getDangerDesp() {
		// TODO Auto-generated method stub
		return String.format(mContext.getString(R.string.danger_desp_for_autostart_onboot), mAutoStartListItems.size());
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getSafeAction()
	 */
	@Override
	public String getSafeAction() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.mobile_exam_action_complete);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDangerAction()
	 */
	@Override
	public String getDangerAction() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.mobile_exam_action_optimize);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getItemStatus()
	 */
	@Override
	public int getItemStatus() {
		// TODO Auto-generated method stub
		return mItemStatus;
	}
	public void setItemStatus(int status){
		this.mItemStatus = status;
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#optimizeAction()
	 */
	@Override
	public void optimizeAction() {
		// TODO Auto-generated method stub
		boolean hasEnabled = false;
		List<AutoStartAppItem> mList = new ArrayList<AutoStartAppItem>();
		for (int i = 0; i < mAutoStartListItems.size(); i++) {
			AutoStartAppItem item = mAutoStartListItems.get(i);
			
			hasEnabled = true;
			if (item.getComponents() != null
					&& item.getComponents().size() > 0) {
				boolean mDisabled = true;
				for (String compomentName : item.getComponents()) {
					try {
						boolean ret = RunCMDUtils.rootCommand("pm disable "
								+ item.getPackageName() + "/"
								+ compomentName);
						if (!ret) {
							mDisabled = false;
						}
					} catch (Exception e) {
						e.printStackTrace();
						mDisabled = false;
					}
				}
				if (mDisabled) {
					mList.add(item);
				}
			}
		}
		for(AutoStartAppItem item : mList){
			mAutoStartListItems.remove(item);
		}
		if(mAutoStartListItems.size() == 0){
			setItemStatus(MobileExamItem.SAFE_ITEM);
		}
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#forwardOptimizeAction()
	 */
	@Override
	public void forwardOptimizeAction() {
		// TODO Auto-generated method stub
		
		mContext.forwardTargetActivity(this, StartManageActivity.class);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getFlag()
	 */
	@Override
	public int getFlag() {
		// TODO Auto-generated method stub
		return MobileExamItem.FLAG_AUTO_START_ITEM;
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#setDangerAction(java.lang.String)
	 */
	@Override
	public void setDangerAction(String action) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#setSafeAction(java.lang.String)
	 */
	@Override
	public void setSafeAction(String action) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getIncScore()
	 */
	@Override
	public int getIncScore() {
		// TODO Auto-generated method stub
		return 5;
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDecScore()
	 */
	@Override
	public int getDecScore() {
		// TODO Auto-generated method stub
		return -5;
	}

}
