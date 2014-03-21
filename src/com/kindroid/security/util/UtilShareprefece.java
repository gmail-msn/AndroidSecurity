/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UtilShareprefece {
	private static UtilShareprefece sharePrefence;
	public static final String LIMIT_PER_MON_INT="limit_per_mon_int";
	public static final String LEFT_PER_MON_INT="left_per_mon_int";
	public static final String CAL_DATE_INT="cal_date_int";
	public static final String LAST_SCAN_TIME = "last_scan_time";
	public static final String LAST_UPDATE_TIME = "last_update_time";
	public static final String LAST_VIRUS_SUM = "last_virus_sum";
	public static final String LAST_APK_SUM = "last_apk_sum";
	public static final String LAST_DELETE_TIME = "last_delete_time";
	public static final String LAST_REPORT_TIME = "last_report_time";
	public static final String SHOW_UPGRADE_PROMPT = "show_upgrade_prompt";
	public static final String LAST_UPGRADE_VERSION = "last_upgrade_version";
	
	public static synchronized UtilShareprefece getShareprefece(){
		if(sharePrefence==null)
			sharePrefence=new UtilShareprefece();
		return sharePrefence;
	}
	public void storeMessage(SharedPreferences sh,String name ,String value){
		if(sh==null)
			return;
		Editor editor=sh.edit();
		editor.putString(name, value);
		editor.commit();
	}

	

}
