/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;

import com.kindroid.security.R;
import com.kindroid.security.service.NetTrafficService;
import com.kindroid.security.ui.MobileExamActivity;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;

/**
 * @author heli.zhao
 *
 */
public class NetTrafficMonitorItem implements MobileExamItem {
	private MobileExamActivity mContext;
	private int mItemStatus = MobileExamItem.SAFE_ITEM;
	private String mDangerAction;
	private String mSafeAction;
	
	public NetTrafficMonitorItem(MobileExamActivity context){
		this.mContext = context;
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getSafeDesp()
	 */
	@Override
	public String getSafeDesp() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.safe_desp_for_nettraffic_monitor);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDangerDesp()
	 */
	@Override
	public String getDangerDesp() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.danger_desp_for_nettraffic_monitor);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getSafeAction()
	 */
	@Override
	public String getSafeAction() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.mobile_exam_action_enabled);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDangerAction()
	 */
	@Override
	public String getDangerAction() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.mobile_exam_action_enable);
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
	 * this method used to be callback for optimizing to enable net traffic monitor
	 */
	@Override
	public void optimizeAction() {
		// TODO Auto-generated method stub
		boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		Editor et = KindroidSecurityApplication.sh.edit();
		if(!isTrue){
			try{
				Intent intent = new Intent();
				intent.setClass(mContext, NetTrafficService.class);
				KindroidSecurityApplication.setUpdatePolicy(KindroidSecurityApplication.SERVICE_MID);
				mContext.startService(intent);
				et.putBoolean(Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER,
						true);
				et.commit();
				setItemStatus(MobileExamItem.SAFE_ITEM);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#forwardOptimizeAction()
	 */
	@Override
	public void forwardOptimizeAction() {
		// TODO Auto-generated method stub
		optimizeAction();
		mContext.updateExamList(this);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getFlag()
	 */
	@Override
	public int getFlag() {
		// TODO Auto-generated method stub
		return MobileExamItem.FLAG_NET_TRAFFIC_MONITOR_ITEM;
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
