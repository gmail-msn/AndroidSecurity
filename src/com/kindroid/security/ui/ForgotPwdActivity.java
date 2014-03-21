/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.07
 * Description:
 */
package com.kindroid.security.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import com.kindroid.security.R;
import com.kindroid.security.util.LoginHelper;

public class ForgotPwdActivity extends Activity implements View.OnClickListener {
	EditText et_username;
	EditText et_email;

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
	boolean isLoading = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.forgot_pwd);
		findView();
		login_bt.setOnClickListener(this);
		cancel_bt.setOnClickListener(this);

	}

	void findView() {
		et_username = (EditText) findViewById(R.id.user_name_input);

		et_email = (EditText) findViewById(R.id.email_input);
		login_bt = (Button) findViewById(R.id.button_ok);
		cancel_bt = (Button) findViewById(R.id.button_cancel);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_cancel:
			finish();
			break;
		case R.id.button_ok:
			if (!checkNetwork()) {
				Toast.makeText(ForgotPwdActivity.this,
						R.string.bakcup_remote_network_unabailable_text,
						Toast.LENGTH_LONG).show();
				return;
			}

			String uname = et_username.getText().toString().trim();
			String email = et_email.getText().toString().trim();

			if (uname.length() < 3) {
				Toast.makeText(ForgotPwdActivity.this,
						R.string.username_can_not_less_3, Toast.LENGTH_LONG)
						.show();
				return;

			}
			else if (!checkEmail(email)) {
				Toast.makeText(ForgotPwdActivity.this, R.string.email_invalid,
						Toast.LENGTH_LONG).show();
				return;
			}
			isLoading = true;
			showDialog();
			new RequestThread(uname,email).start();

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

		if (loadingProgressDialog == null) {
			loadingProgressDialog = new Dialog(this, R.style.softDialog) {
				public void onBackPressed() {
					isLoading = false;

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

	private Handler handler = new Handler() {
		public void dispatchMessage(Message msg) {
			switch (msg.what) {

			case -2:
				loadingProgressDialog.cancel();
				return;

			case -1:
				Toast.makeText(ForgotPwdActivity.this, R.string.network_tip,
						Toast.LENGTH_LONG).show();
				break;
			case 0:
				loadingProgressDialog.cancel();
				Toast.makeText(ForgotPwdActivity.this, R.string.password_has_sendto_your_email,
						Toast.LENGTH_LONG).show();
				finish();
				break;
			case 501:
				Toast.makeText(ForgotPwdActivity.this,
						R.string.username_invalid, Toast.LENGTH_LONG).show();
				break;
			
			case 502:
				Toast.makeText(ForgotPwdActivity.this, R.string.email_invalid,
						Toast.LENGTH_LONG).show();
				break;
			case 503:
				Toast.makeText(ForgotPwdActivity.this, R.string.account_not_exist,
						Toast.LENGTH_LONG).show();
				
				break;
			default:
				Toast.makeText(ForgotPwdActivity.this, R.string.network_tip,
						Toast.LENGTH_LONG).show();
				break;

			}
		};
	};

	public boolean checkEmail(String email) {
		if (TextUtils.isEmpty(email)) {
			return false;
		}
		Pattern pattern = Pattern
				.compile("^[a-zA-Z0-9][a-zA-Z0-9-_.]+?@([a-zA-Z0-9]+(?:\\.[a-zA-Z0-9-_]+){1,})$");
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
		String uname, email;

		public RequestThread(String uname, String email) {
			// TODO Auto-generated constructor stub
			this.uname = uname;

			this.email = email;
		}

		public void run() {
			int val = LoginHelper.forgotPwd(uname, email);
			isLoading = false;
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