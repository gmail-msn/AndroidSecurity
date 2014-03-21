/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.security.R;
import com.kindroid.security.util.LoginHelper;

public class LoginActivity extends Activity implements View.OnClickListener {
	EditText et_username;
	EditText et_passwd;
	TextView forgotpsd_tv;
	TextView register_tv;
	TextView login_status_tv;
	Button login_bt;

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
		setContentView(R.layout.login);
		findView();
		login_bt.setOnClickListener(this);
		forgotpsd_tv.setOnClickListener(this);
		register_tv.setOnClickListener(this);
		String str_register = "<font color=#009acc><a href=''>"
				+ getResources().getString(R.string.backup_register_text)
				+ "</a></font>";
		String str_forgotpsd = "<font color=#009acc><a href=''>"
				+ getResources().getString(R.string.forgot_passwd)
				+ "</a></font>";
		forgotpsd_tv.setText(Html.fromHtml(str_forgotpsd));
		register_tv.setText(Html.fromHtml(str_register));

	}

	void findView() {
		et_username = (EditText) findViewById(R.id.user_name_input);
		et_passwd = (EditText) findViewById(R.id.password_input);
		register_tv = (TextView) findViewById(R.id.register_tv);
		forgotpsd_tv = (TextView) findViewById(R.id.forgotpsd_tv);
		login_status_tv = (TextView) findViewById(R.id.login_status);
		login_bt = (Button) findViewById(R.id.button_ok);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.forgotpsd_tv:
			startActivity(new Intent(LoginActivity.this,
					ForgotPwdActivity.class));
			break;
		case R.id.register_tv:
			startActivityForResult(new Intent(LoginActivity.this,
					RegisterActivity.class), 1);

			break;
		case R.id.button_ok:
			if (!checkNetwork()) {
				Toast.makeText(LoginActivity.this,
						R.string.bakcup_remote_network_unabailable_text,
						Toast.LENGTH_LONG).show();
				return;
			}

			String uname = et_username.getText().toString().trim();
			String pwd = et_passwd.getText().toString().trim();
			if (uname.equals("")) {
				Toast.makeText(LoginActivity.this,
						R.string.backup_login_user_name_empty,
						Toast.LENGTH_LONG).show();
				return;
			}
			if (pwd.equals("")) {
				Toast.makeText(LoginActivity.this,
						R.string.backup_login_password_empty, Toast.LENGTH_LONG)
						.show();
				return;
			}
			if (uname.length() < 3 || uname.length() > 45) {
				Toast.makeText(LoginActivity.this,
						R.string.backup_login_error_uname, Toast.LENGTH_LONG)
						.show();
				return;
			}
			if (pwd.length() < 4) {
				Toast.makeText(LoginActivity.this,
						R.string.password_notlessthan4, Toast.LENGTH_LONG)
						.show();
				return;
			}
			isLoading = true;
			showDialog();
			new RequestThread(uname, pwd).start();

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
			boolean loginResult = true;

			switch (msg.what) {

			case -2:
				loadingProgressDialog.cancel();
				return;

			case -1:
				loginResult = false;
				break;
			case 103:
				Toast.makeText(LoginActivity.this,
						R.string.backup_login_name_pwd_err, Toast.LENGTH_LONG)
						.show();
				loginResult = false;
				return;
			}

			if (!loginResult || msg.what != 0) {
				Toast.makeText(LoginActivity.this,
						R.string.backup_login_fail_prompt, Toast.LENGTH_LONG)
						.show();

				return;
			}
			loadingProgressDialog.cancel();
			Toast.makeText(LoginActivity.this, R.string.login_suc,
					Toast.LENGTH_LONG).show();
			Intent intent = new Intent();
			intent.putExtra("passwd", et_passwd.getText().toString().trim());
			setResult(RESULT_OK, intent);
			finish();

		};
	};

	private class LoadingItem extends Thread {
		public void run() {

			do {
				for (int j = 0; j < 5; j++) {
					if (!isLoading)
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
		String uname, pwd;

		public RequestThread(String uname, String pwd) {
			// TODO Auto-generated constructor stub
			this.uname = uname;
			this.pwd = pwd;
		}

		public void run() {
			int val = LoginHelper.validateUser(uname, pwd);
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && resultCode == RESULT_OK) {

			if (data != null & data.getExtras() != null) {
				Intent intent = new Intent();
				intent.putExtras(data.getExtras());
				setResult(RESULT_OK, intent);
				finish();
			}

		}
	};

}