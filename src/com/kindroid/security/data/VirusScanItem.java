/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.data;

import android.content.Context;

import com.kindroid.security.R;
import com.kindroid.security.ui.MobileExamActivity;
import com.kindroid.security.ui.StartManageActivity;
import com.kindroid.security.ui.VirusScanTabActivity;

/**
 * @author heli.zhao
 *
 */
public class VirusScanItem implements MobileExamItem {
	private MobileExamActivity mContext;
	private int mItemStatus = MobileExamItem.SAFE_ITEM;
	private long mLastVirusScanTime;
	private String mDangerAction;
	private String mSafeAction;
	
	public VirusScanItem(MobileExamActivity context){
		this.mContext = context;
	}

	public long getLastVirusScanTime() {
		return mLastVirusScanTime;
	}


	public void setLastVirusScanTime(long mLastVirusScanTime) {
		this.mLastVirusScanTime = mLastVirusScanTime;
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getSafeDesp()
	 */
	@Override
	public String getSafeDesp() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.danger_desp_for_virus_scan);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDangerDesp()
	 */
	@Override
	public String getDangerDesp() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.danger_desp_for_virus_scan);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getSafeAction()
	 */
	@Override
	public String getSafeAction() {
		// TODO Auto-generated method stub
		if(mSafeAction == null){
			return mContext.getString(R.string.mobile_exam_action_nofound);
		}else{
			return mSafeAction;
		}
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDangerAction()
	 */
	@Override
	public String getDangerAction() {
		// TODO Auto-generated method stub
		if(mDangerAction == null){
			return mContext.getString(R.string.mobile_exam_action_clean);
		}else{
			return mDangerAction;
		}
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
		
	}


	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#forwardOptimizeAction()
	 */
	@Override
	public void forwardOptimizeAction() {
		// TODO Auto-generated method stub
		mContext.forwardTargetActivity(this, VirusScanTabActivity.class);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getFlag()
	 */
	@Override
	public int getFlag() {
		// TODO Auto-generated method stub
		return MobileExamItem.FLAG_VIRUS_SCAN_ITEM;
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#setDangerAction(java.lang.String)
	 */
	@Override
	public void setDangerAction(String action) {
		// TODO Auto-generated method stub
		mDangerAction = action;
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#setSafeAction(java.lang.String)
	 */
	@Override
	public void setSafeAction(String action) {
		// TODO Auto-generated method stub
		mSafeAction = action;
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
