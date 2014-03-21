/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.data;

/**
 * @author heli.zhao
 *
 */
public interface MobileExamItem {
	/**
	 * 4 item status for mobile examined result
	 * 0:safe,1:danger,2:need to optimize,3:optimized
	 */
	public static final int SAFE_ITEM = 0;
	public static final int OPTIMIZED_ITEM = 1;
	public static final int UNOPTIMIZED_ITEM = 2;
	public static final int DANGER_ITEM = 3;
	
	public static final int FLAG_AUTO_START_ITEM = 0;
	public static final int FLAG_CACHE_CLEAN_ITEM = 1;
	public static final int FLAG_SECURITY_SERVICE_ITEM = 2;
	public static final int FLAG_NET_TRAFFIC_MONITOR_ITEM = 3;
	public static final int FLAG_REMOTE_SECURITY_ITEM = 4;
	public static final int FLAG_RUNNING_TASK_ITEM = 5;
	public static final int FLAG_VIRUS_SCAN_ITEM = 6;
	public static final int VIRUS_UPDATE_ITEM = 7;
	
	/**
	 * flag for this item type
	 * @return
	 */
	public int getFlag();
	/**
	 * get description for safe status
	 * @return description for safe status
	 */
	public String getSafeDesp();
	
	/**
	 * get description for danger status
	 * @return description for danger status
	 */
	public String getDangerDesp();
	
	/**
	 * get action text for optimized status
	 * @return action text for optimized status
	 */
	public String getSafeAction();
	
	/**
	 * get action text for unoptimized status
	 * @return action text for unoptimized status
	 */ 
	public String getDangerAction();
	
	public void setDangerAction(String action);
	public void setSafeAction(String action);
	
	/**
	 * get status for this examined item
	 * @return status for this examined item
	 */
	public int getItemStatus();
	
	public void setItemStatus(int status);
	
	/**
	 * callback method for optimize action
	 */
	public void optimizeAction();
	/**
	 * callback method for forward optimize operation to target activity
	 */
	public void forwardOptimizeAction();
	
	public int getIncScore();
	public int getDecScore();

}
