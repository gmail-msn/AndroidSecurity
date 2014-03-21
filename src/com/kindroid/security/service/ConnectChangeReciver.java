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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.sax.StartElementListener;

import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.PopViewAid;

public class ConnectChangeReciver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		boolean enableNetWork = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		boolean enableAppNetWork = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLEAPPTRAFFICMOITER, true);

		NetworkInfo localNetworkInfo = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();
		if (localNetworkInfo != null) {
			if (localNetworkInfo.getType() == 0 && enableNetWork
					&& enableAppNetWork) {
				if (PopViewAid.linear == null) {
					Intent intent_copy = new Intent(context,
							AppTrafficService.class);
					context.startService(intent_copy);
					KindroidSecurityApplication
							.setUpdatePolicy(KindroidSecurityApplication.SERVICE_HIGH);
					intent_copy=new Intent(context,NetTrafficService.class);
					context.startService(intent_copy);
					
				}
			} else {
				if (PopViewAid.linear != null) {
					Intent intent_copy = new Intent(context,
							AppTrafficService.class);
					context.stopService(intent_copy);
				}
			}
		} else {
			if (PopViewAid.linear != null) {
				Intent intent_copy = new Intent(context,
						AppTrafficService.class);
				context.stopService(intent_copy);
			}

		}

	}

}
