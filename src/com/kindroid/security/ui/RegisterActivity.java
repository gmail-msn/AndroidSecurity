/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kindroid.security.R;
import com.kindroid.security.util.LoginHelper;
import com.kindroid.security.util.Utilis;

public class RegisterActivity extends Activity implements View.OnClickListener {
	EditText et_username;
	EditText et_passwd;
	EditText et_sure_password;
	EditText et_email;
	EditText phoneNum_input;
	TextView forgotpsd_tv;
	TextView register_tv;
	Button login_bt;
	Button cancel_bt;
	
	

	private ImageView one;
	private ImageView two;
	private ImageView three;
	private ImageView four;
	private ImageView five;

	private ImageView one_copy;
	private ImageView two_copy;
	private ImageView three_copy;
	private ImageView four_copy;
	private ImageView five_copy;

	Dialog loadingProgressDialog;
	boolean isLoading=false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register);
		findView();
		login_bt.setOnClickListener(this);
		cancel_bt.setOnClickListener(this);
		AccountManager accountManager = AccountManager.get(this);
		Account[] accounts = accountManager.getAccountsByType("com.google");
		if(accounts!=null&&accounts.length>0){
			et_email.setText(accounts[0].name);
		}
		
	}

	void findView() {
		et_username = (EditText) findViewById(R.id.user_name_input);
		et_passwd = (EditText) findViewById(R.id.password_input);
		et_email=(EditText) findViewById(R.id.email_input);
		login_bt = (Button) findViewById(R.id.button_ok);
		cancel_bt=(Button) findViewById(R.id.button_cancel);
		et_sure_password=(EditText) findViewById(R.id.sure_password_input);
		phoneNum_input = (EditText) findViewById(R.id.phoneNum_input);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_cancel:
			finish();
			break;
		case R.id.button_ok:
			if (!checkNetwork()) {
				Toast.makeText(RegisterActivity.this,
						R.string.bakcup_remote_network_unabailable_text,
						Toast.LENGTH_LONG).show();
				return;
			}			
			String uname = et_username.getText().toString().trim();
			String pwd = et_passwd.getText().toString().trim();
			String email=et_email.getText().toString().trim();			
			String sure_pwd=et_sure_password.getText().toString().trim();	
			String phone_num = phoneNum_input.getText().toString().trim();
			 if (uname.length() < 3) {
				 Toast.makeText(RegisterActivity.this,
							R.string.username_can_not_less_3,
							Toast.LENGTH_LONG).show();
					return;
				 
				} else if (pwd.length() < 4) {
					
					Toast.makeText(RegisterActivity.this,
							 R.string.password_notlessthan4,
								Toast.LENGTH_LONG).show();
					return;
				}
				else if(!sure_pwd.equals(pwd)){
					Toast.makeText(
							this,
							getResources().getString(
									R.string.passwd_surepasswd_same),
							Toast.LENGTH_SHORT).show();
					return;
				}
			 
			 else if (!checkEmail(email)) {
					 Toast.makeText(RegisterActivity.this,
							 R.string.email_invalid,
								Toast.LENGTH_LONG).show();
					return;
				}
			 else if(!Utilis.checkPhoneNum(phone_num) && !TextUtils.isEmpty(phone_num)){
				 Toast.makeText(RegisterActivity.this,
						 R.string.account_register_phone_error,
							Toast.LENGTH_LONG).show();
				return;
			 }
			isLoading=true;
			showDialog();
			new RequestThread(uname, pwd,email, phone_num).start();
			
		}
	}
	
	private boolean checkNetwork() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo != null) {			
			if (networkinfo.isConnected())
				return true;
		}
		return false;
	}

	private void showDialog() {
		
		if(loadingProgressDialog==null){
			loadingProgressDialog = new Dialog(this, R.style.softDialog){
				public void onBackPressed() {
					isLoading=false;
					
				};
			};
			View view = LayoutInflater.from(this).inflate(
					R.layout.softmanage_prompt_dialog, null);
			one = (ImageView) view.findViewById(R.id.pr_one);
			two = (ImageView) view.findViewById(R.id.pr_two);
			three = (ImageView) view.findViewById(R.id.pr_three);
			four = (ImageView) view.findViewById(R.id.pr_four);
			five = (ImageView) view.findViewById(R.id.pr_five);

			one_copy = (ImageView) view.findViewById(R.id.pr_one_copy);
			two_copy = (ImageView) view.findViewById(R.id.pr_two_copy);
			three_copy = (ImageView) view.findViewById(R.id.pr_three_copy);
			four_copy = (ImageView) view.findViewById(R.id.pr_four_copy);
			five_copy = (ImageView) view.findViewById(R.id.pr_five_copy);
			loadingProgressDialog.setContentView(view);
			TextView tv = (TextView) loadingProgressDialog
					.findViewById(R.id.prompt_dialog_text);
			tv.setText(R.string.request_message_now);
		}

	
		loadingProgressDialog.show();

		new LoadingItem().start();
	}
	
	
	private Handler handler=new Handler(){
		public void dispatchMessage(Message msg) {
			boolean loginResult = true;
			
			switch (msg.what) {
			
			case -2:
				loadingProgressDialog.cancel();
				return;
			
			case -1:
				Toast.makeText(RegisterActivity.this,
						R.string.network_tip, Toast.LENGTH_LONG)
						.show();
				break;
			case 0:
				loadingProgressDialog.cancel();
				Toast.makeText(RegisterActivity.this,
						R.string.register_suc, Toast.LENGTH_LONG)
						.show();
				Intent intent=new Intent();
				intent.putExtra("passwd", et_passwd.getText().toString().trim());
				setResult(RESULT_OK,intent);
				finish();
				break;
			case 101:
				Toast.makeText(RegisterActivity.this,
						R.string.username_invalid, Toast.LENGTH_LONG)
						.show();
				break;
			case 102:
				Toast.makeText(RegisterActivity.this,
						R.string.password_invalid, Toast.LENGTH_LONG)
						.show();
				break;
			case 103:
				Toast.makeText(RegisterActivity.this,
						R.string.account_register_error_no103, Toast.LENGTH_LONG)
						.show();
				break;
			case 111:
				Toast.makeText(RegisterActivity.this,
						R.string.username_al_exist, Toast.LENGTH_LONG)
						.show();
				break;
			case 112:
				Toast.makeText(RegisterActivity.this,
						R.string.account_register_error_no112, Toast.LENGTH_LONG)
						.show();
				break;
			case 113:
				Toast.makeText(RegisterActivity.this,
						R.string.account_register_error_no113, Toast.LENGTH_LONG)
						.show();
				break;
			case 114:
				Toast.makeText(RegisterActivity.this,
						R.string.account_register_error_no114, Toast.LENGTH_LONG)
						.show();
				break;
			case 121:
				Toast.makeText(RegisterActivity.this,
						R.string.account_register_error_no121, Toast.LENGTH_LONG)
						.show();
				break;
			case 122:
				Toast.makeText(RegisterActivity.this,
						R.string.account_register_error_no114, Toast.LENGTH_LONG)
						.show();
				break;
			default:
				Toast.makeText(RegisterActivity.this,
						R.string.account_register_error_no112, Toast.LENGTH_LONG)
						.show();
				break;
				
			}
		};
	};
	
	public  boolean checkEmail(String email) {
		if (TextUtils.isEmpty(email)) {
			return false;
		}
		Pattern pattern = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9-_.]+?@([a-zA-Z0-9]+(?:\\.[a-zA-Z0-9-_]+){1,})$");
		return (pattern.matcher(email)).matches();
	}

	private class LoadingItem extends Thread {
		public void run() {
			do {
				for (int j = 0; j < 5; j++) {
					if(!isLoading)
						break;
					try {
						sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mProgressHandler.sendEmptyMessage(j);
				}
			} while (isLoading);
			mProgressHandler.sendEmptyMessage(0);
			handler.sendEmptyMessage(-2);
		}
	}
	
	
	private class RequestThread extends Thread {
		String uname,pwd,email,phoneNum;
		
		public RequestThread(String uname,String pwd,String email, String phoneNum) {
			// TODO Auto-generated constructor stub
			this.uname=uname;
			this.pwd=pwd;
			this.email=email;
			this.phoneNum = phoneNum;
		} 
		public void run() {
			int val = LoginHelper.registerAccount(RegisterActivity.this, uname, email, pwd, phoneNum);
			isLoading=false;
			handler.sendEmptyMessage(val);
		}
	}
	
	

	private Handler mProgressHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			one.setVisibility(View.VISIBLE);
			two.setVisibility(View.VISIBLE);
			three.setVisibility(View.VISIBLE);
			four.setVisibility(View.VISIBLE);
			five.setVisibility(View.VISIBLE);

			one_copy.setVisibility(View.INVISIBLE);
			two_copy.setVisibility(View.INVISIBLE);
			three_copy.setVisibility(View.INVISIBLE);
			four_copy.setVisibility(View.INVISIBLE);
			five_copy.setVisibility(View.INVISIBLE);

			switch (msg.what) {
			case 0:
				one.setVisibility(View.INVISIBLE);
				one_copy.setVisibility(View.VISIBLE);
				break;
			case 1:
				two.setVisibility(View.INVISIBLE);
				two_copy.setVisibility(View.VISIBLE);
				break;
			case 2:
				three.setVisibility(View.INVISIBLE);
				three_copy.setVisibility(View.VISIBLE);
				break;
			case 3:
				four.setVisibility(View.INVISIBLE);
				four_copy.setVisibility(View.VISIBLE);
				break;
			case 4:
				five.setVisibility(View.INVISIBLE);
				five_copy.setVisibility(View.VISIBLE);
				break;

			}
		}
	};

}