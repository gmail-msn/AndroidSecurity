/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.08
 * Description:
 */
package com.kindroid.security.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.kindroid.security.util.KindroidSecurityApplication;

public class AppEngActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		KindroidSecurityApplication application = (KindroidSecurityApplication) getApplication();
		boolean isTrue = application.isAppIsActive();
		if (!isTrue) {
			Bundle bundle=getIntent().getExtras();
			if(bundle!=null){
				int type=bundle.getInt("type");
				if(type==1){
					startActivity(new Intent(this, NetTrafficTabMain.class));
				}else if(type==2){
					startActivity(new Intent(this, BlockTabMain.class));
				}else{
//					startActivity(new Intent(this, DefenderTabMain.class));
				}
			}
		}
		finish();

	}
}