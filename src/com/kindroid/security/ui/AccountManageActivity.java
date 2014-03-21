/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import com.kindroid.security.R;
import com.kindroid.security.util.Utilis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AccountManageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_manage_unlogined);
		View home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent homepage = new Intent(AccountManageActivity.this,
						DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
			}
		});
		View login_linear = findViewById(R.id.login_linear);
		View backup_linear = findViewById(R.id.backup_linear);
		View anti_theft_linear = findViewById(R.id.anti_theft_linear);
		Button register_button = (Button)findViewById(R.id.register_button);
		Button forgot_pass_button = (Button)findViewById(R.id.forgot_pass_button);
		login_linear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AccountManageActivity.this, LoginActivity.class);
				startActivityForResult(intent, 9);				
			}
		});
		backup_linear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AccountManageActivity.this, BackupRestoreActivity.class);
				startActivity(intent);
				finish();
			}
		});
		anti_theft_linear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AccountManageActivity.this, RemoteSecurityTabActivity.class);
				startActivity(intent);
				finish();
			}
		});
		register_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AccountManageActivity.this, RegisterActivity.class);
				startActivityForResult(intent, 8);				
			}
		});
		forgot_pass_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AccountManageActivity.this, ForgotPwdActivity.class);
				startActivity(intent);				
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if((requestCode == 9) && resultCode == Activity.RESULT_OK){
			Intent intent = new Intent(this, AccountManageLoginedActivity.class);
			startActivity(intent);
			finish();
		}else if((requestCode == 8) && resultCode == Activity.RESULT_OK){
			if(Utilis.hasLogined()){
				Intent intent = new Intent(this, AccountManageLoginedActivity.class);
				startActivity(intent);
				finish();
			}
		}else{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	

}
