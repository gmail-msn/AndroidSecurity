/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.data;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.kindroid.security.R;
import com.kindroid.security.ui.MobileExamActivity;
import com.kindroid.security.ui.NetTrafficSettings;
import com.kindroid.security.ui.ProcessManagerActivity;
import com.kindroid.security.ui.StartManageActivity;
import com.kindroid.security.ui.TaskManageTabActivity;
import com.kindroid.security.util.ProcInfo;
import com.kindroid.security.util.TaskUtil;
import com.kindroid.security.util.UnitsConversion;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heli.zhao
 *
 */
public class RunningTaskItem implements MobileExamItem {
	private MobileExamActivity mContext;
	private int mItemStatus = MobileExamItem.SAFE_ITEM;
	private List<ProcInfo> mListProc;
	private String mUseMemory;	
	private String mReleaseMemory;
	private String mDangerAction;
	private String mSafeAction;
	private int mRunningTasks;
	private int mScore = -5;
	
	public RunningTaskItem(MobileExamActivity context){
		this.mContext = context;
		mListProc = new ArrayList<ProcInfo>();
	}
	public void addProc(ProcInfo proc){
		mListProc.add(proc);
	}
	public void delProc(ProcInfo proc){
		mListProc.remove(proc);
	}
	public void clearProc(){
		mListProc.clear();
	}
	public List<ProcInfo> getProcInfos(){
		return mListProc;
	}
	public int getProcSum(){
		return mListProc.size();
	}
	
	public String getUseMemory() {
		return mUseMemory;
	}

	public void setUseMemory(String mUseMemory) {
		this.mUseMemory = mUseMemory;
	}

	public String getReleaseMemory() {
		return mReleaseMemory;
	}

	public void setReleaseMemory(String mReleaseMemory) {
		this.mReleaseMemory = mReleaseMemory;
	}

	
	public int getTaskSum() {
		return mListProc.size();
	}


	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getSafeDesp()
	 */
	@Override
	public String getSafeDesp() {
		// TODO Auto-generated method stub
		return String.format(mContext.getString(R.string.safe_desp_for_running_task), mRunningTasks, mUseMemory);
		
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDangerDesp()
	 */
	@Override
	public String getDangerDesp() {
		// TODO Auto-generated method stub
		return String.format(mContext.getString(R.string.danger_desp_for_running_task), mListProc.size(), mUseMemory);
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
		int releaseMem = 0;
		for(ProcInfo pInfo : mListProc){
			try{
				TaskUtil.killProcess(pInfo.getPackageName(), mContext);
				releaseMem = releaseMem + pInfo.getMemory(mContext);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		setReleaseMemory(new UnitsConversion().defaultConversion(releaseMem * 1024));
		setItemStatus(MobileExamItem.SAFE_ITEM);
		mContext.sendBroadcast(new Intent(NetTrafficSettings.NET_TRAFFIC_UPDATE_SETTINGS));
	}
	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#forwardOptimizeAction()
	 */
	@Override
	public void forwardOptimizeAction() {
		// TODO Auto-generated method stub
		mRunningTasks = mListProc.size();
		ProcessManagerActivity.sOptimizeItem = this;
		mContext.forwardTargetActivity(this, TaskManageTabActivity.class);
	}
	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getFlag()
	 */
	@Override
	public int getFlag() {
		// TODO Auto-generated method stub
		return MobileExamItem.FLAG_RUNNING_TASK_ITEM;
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
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo(); 
		activityManager.getMemoryInfo(outInfo);
		if(outInfo.lowMemory){
			return 0;
		}else{
			return (0-mScore);
		}
		
	}
	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDecScore()
	 */
	@Override
	public int getDecScore() {
		// TODO Auto-generated method stub
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo(); 
		activityManager.getMemoryInfo(outInfo);
		if(outInfo.lowMemory){
			mScore = -10;
		}else{
			mScore = -5;
		}
		return mScore;
	}

}
