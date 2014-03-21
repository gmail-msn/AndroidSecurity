/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;

public class OnAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean enableNetWork = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		if (enableNetWork)
			context.startService(new Intent(context, NetTrafficService.class));
	}

}
