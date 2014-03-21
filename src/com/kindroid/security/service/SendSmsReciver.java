/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;

public class SendSmsReciver extends BroadcastReceiver {
	int num = 0;
	Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		// TODO Auto-generated method stub
		if (intent.getAction().equals("sms_send_myaction")) {
			num++;
			new UpdateSimThread().start();

		} else if (intent.getAction().equals("sms_send_del_action")) {
			try{
				if(getResultCode()==Activity.RESULT_OK){
					Toast.makeText(context,R.string.sms_send_suc, Toast.LENGTH_SHORT).show();
				}else
					Toast.makeText(context, R.string.sms_send_fail, Toast.LENGTH_SHORT).show();
				
			}catch(Exception e){
				e.printStackTrace();
			}

		}

	}

	class UpdateSimThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {				
				int number = getResultCode();
				if (number == Activity.RESULT_OK)
					num = 0;
				else {
					if (num == 4) {
						num = 0;
						return;
					}
					Thread.sleep(10000);
					String safeMobileNumber = KindroidSecurityApplication.sh
							.getString(
									Constant.SHAREDPREFERENCES_SAFEMOBILENUMBER,
									"");
					PendingIntent mPI = PendingIntent.getBroadcast(context, 0,
							new Intent("sms_send_myaction"), 0);
					SmsManager smsManager = SmsManager.getDefault();
					String smsMessage = KindroidSecurityApplication.sh
							.getString(
									Constant.SHAREDPREFERENCES_AFTERUPDATESIMSENDMES,
									context.getResources().getString(R.string.your_mobile_maybe_lost));
					String str=context.getResources().getString(R.string.remote_security_pop);
					smsMessage+=str;
					smsManager.sendTextMessage(safeMobileNumber, null,
							smsMessage, mPI, null);
				}

			} catch (Exception e) {
				e.getStackTrace();
			}

		}
	}

}
