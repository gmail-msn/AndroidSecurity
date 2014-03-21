/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.kindroid.security.R;
import com.kindroid.security.util.AccountProtoc;
import com.kindroid.security.util.AccountProtoc.Account;
import com.kindroid.security.util.AccountProtoc.AccountResponse;
import com.kindroid.security.util.Base64Handler;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.EmailProtoc.EmailResponse;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.RequestProtoc;
import com.kindroid.security.util.ResponseProtoc;
import com.kindroid.security.util.ResponseProtoc.ResponseContext;
import com.kindroid.security.util.Utilis;
import com.kindroid.security.util.CommonProtoc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AccountManageLoginedActivity extends Activity {

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
	
	private Dialog promptDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_manage_logined);
		//init complete dialog
		promptDialog = new Dialog(this, R.style.softDialog);
		
		TextView user_name_text = (TextView) findViewById(R.id.user_name_text);
		user_name_text.setText(Utilis.getUserName());

		View home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent homepage = new Intent(AccountManageLoginedActivity.this,
						DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
			}
		});
		View logoff_linear = findViewById(R.id.logoff_linear);
		logoff_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getLogoffPromptDialog();
			}
		});
		View backup_linear = findViewById(R.id.backup_linear);
		backup_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AccountManageLoginedActivity.this,
						BackupRestoreActivity.class);
				startActivity(intent);
				finish();
			}
		});
		View anti_theft_linear = findViewById(R.id.anti_theft_linear);
		anti_theft_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AccountManageLoginedActivity.this,
						RemoteSecurityTabActivity.class);
				startActivity(intent);
				finish();
			}
		});
		View email_linear = findViewById(R.id.email_linear);
		email_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				changeEmailDialog();
			}
		});
		View change_pass_linear = findViewById(R.id.change_pass_linear);
		change_pass_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				changePwdDialog();
			}
		});
		View change_phone_linear = findViewById(R.id.change_phone_linear);
		change_phone_linear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				changePhoneDialog();
			}
		});
	}
	private void changePhoneDialog() {
		final Dialog promptDialog = new Dialog(this, R.style.softDialog);
		View view = LayoutInflater.from(this).inflate(
				R.layout.change_phone_dialog, null);
		promptDialog.setContentView(view);
		final EditText et = (EditText) promptDialog.findViewById(R.id.et_line);

		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String newPhone = et.getText().toString().trim();
				if (Utilis.checkPhoneNum(newPhone)) {
					changePhone(newPhone);
					promptDialog.dismiss();
				} else {
					Toast.makeText(AccountManageLoginedActivity.this,
							R.string.account_register_phone_error,
							Toast.LENGTH_LONG).show();
					et.setText("");
				}

			}
		});
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				promptDialog.dismiss();

			}
		});
		promptDialog.show();
	}
	
	private void changePhone(String newPhone) {
		String mToken = Utilis.getToken();
		AccountProtoc.Account.Builder account = AccountProtoc.Account
				.newBuilder();
		account.setPhone(newPhone);
		AccountProtoc.AccountRequest.Builder accountRequ = AccountProtoc.AccountRequest
				.newBuilder();
		accountRequ.setAccount(account);		
		accountRequ.setRequestType(CommonProtoc.RequestType.EDIT);

		RequestProtoc.RequestContext.Builder context = RequestProtoc.RequestContext
				.newBuilder();
		context.setAuthToken(mToken);

		RequestProtoc.Request.Builder request = RequestProtoc.Request
				.newBuilder();
		request.setAccountRequest(accountRequ);
		request.setContext(context);
		int ret = -1;
		Base64Handler base64 = new Base64Handler();
		BufferedReader mReader = null;
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet();
		try {
			URI url = new URI(Constant.ACCOUNT_MANAGE_URL
					+ base64.encode(request.build().toByteArray()));
			httpGet.setURI(url);
			HttpResponse response = client.execute(httpGet);

			StringBuffer mBuffer = new StringBuffer();
			mReader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			String line = mReader.readLine();
			while (line != null) {
				mBuffer.append(line);
				line = mReader.readLine();
			}

			ResponseProtoc.Response resp = ResponseProtoc.Response
					.parseFrom(base64.decodeBuffer(mBuffer.toString()));
			if (resp.hasContext()) {
				ResponseContext rc = resp.getContext();
				ret = rc.getResult();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ret = -1;
		} finally {
			if (mReader != null) {
				try {
					mReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}		
		getCompleteDialog(2, ret);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		startRefreshEmail();
	}

	private void startRefreshEmail() {
		final TextView email_address_text = (TextView) findViewById(R.id.email_address_text);
		email_address_text.setText(R.string.account_man_obtain_email);
		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0:
					email_address_text.setText(msg.obj.toString());
					break;
				}
			}

		};
		new Thread() {
			public void run() {
				String email = refreshEmail();
				Message msg = new Message();
				msg.what = 0;
				if (email == null) {
					email = getString(R.string.account_man_obtain_email_error);
				}
				msg.obj = email;
				handler.sendMessage(msg);
			}
		}.start();
	}

	private String refreshEmail() {
		String mToken = Utilis.getToken();
		AccountProtoc.AccountRequest.Builder accountRequ = AccountProtoc.AccountRequest
				.newBuilder();
		accountRequ.setRequestType(CommonProtoc.RequestType.READ);
		RequestProtoc.RequestContext.Builder context = RequestProtoc.RequestContext
				.newBuilder();
		context.setAuthToken(mToken);
		RequestProtoc.Request.Builder request = RequestProtoc.Request
				.newBuilder();
		request.setAccountRequest(accountRequ);
		request.setContext(context);
		Base64Handler base64 = new Base64Handler();

		BufferedReader mReader = null;
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet();
		try {
			URI url = new URI(Constant.ACCOUNT_MANAGE_URL
					+ base64.encode(request.build().toByteArray()));
			httpGet.setURI(url);
			HttpResponse response = client.execute(httpGet);

			StringBuffer mBuffer = new StringBuffer();
			mReader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			String line = mReader.readLine();
			while (line != null) {
				mBuffer.append(line);				
				line = mReader.readLine();
			}

			ResponseProtoc.Response resp = ResponseProtoc.Response
					.parseFrom(base64.decodeBuffer(mBuffer.toString()));
			if (resp.hasContext()) {
				ResponseContext rc = resp.getContext();
				if (rc.getResult() != 0) {
					return null;
				}
			}
			if (resp.hasAccountResponse()) {
				AccountResponse ar = resp.getAccountResponse();
				Account a = ar.getAccount();
				String email = a.getEmail();
				return email;
			} else {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (mReader != null) {
				try {
					mReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void changeEmailDialog() {
		final Dialog promptDialog = new Dialog(this, R.style.softDialog);
		View view = LayoutInflater.from(this).inflate(
				R.layout.change_email_dialog, null);
		promptDialog.setContentView(view);
		final EditText et = (EditText) promptDialog.findViewById(R.id.et_line1);

		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String newEmail = et.getText().toString().trim();
				if (valid(newEmail)) {
					changeEmail(newEmail);
					promptDialog.dismiss();
				} else {
					Toast.makeText(AccountManageLoginedActivity.this,
							R.string.account_man_change_email_input_error_text,
							Toast.LENGTH_LONG).show();
					et.setText("");
				}

			}
		});
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				promptDialog.dismiss();

			}
		});
		promptDialog.show();
	}

	private boolean valid(String email) {
		Pattern p = Pattern
				.compile("^[a-zA-Z0-9][a-zA-Z0-9-_.]+?@([a-zA-Z0-9]+(?:\\.[a-zA-Z0-9-_]+){1,})$");
		Matcher m = p.matcher(email);
		return m.matches();
	}

	private void changeEmail(String newEmail) {
		String mToken = Utilis.getToken();
		AccountProtoc.Account.Builder account = AccountProtoc.Account
				.newBuilder();
		account.setEmail(newEmail);
		AccountProtoc.AccountRequest.Builder accountRequ = AccountProtoc.AccountRequest
				.newBuilder();
		accountRequ.setAccount(account);
		accountRequ.setRequestType(CommonProtoc.RequestType.EDIT);

		RequestProtoc.RequestContext.Builder context = RequestProtoc.RequestContext
				.newBuilder();
		context.setAuthToken(mToken);

		RequestProtoc.Request.Builder request = RequestProtoc.Request
				.newBuilder();
		request.setAccountRequest(accountRequ);
		request.setContext(context);
		int ret = -1;
		Base64Handler base64 = new Base64Handler();
		BufferedReader mReader = null;
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet();
		try {
			URI url = new URI(Constant.ACCOUNT_MANAGE_URL
					+ base64.encode(request.build().toByteArray()));
			httpGet.setURI(url);
			HttpResponse response = client.execute(httpGet);

			StringBuffer mBuffer = new StringBuffer();
			mReader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			String line = mReader.readLine();
			while (line != null) {
				mBuffer.append(line);
				line = mReader.readLine();
			}

			ResponseProtoc.Response resp = ResponseProtoc.Response
					.parseFrom(base64.decodeBuffer(mBuffer.toString()));
			if (resp.hasContext()) {
				ResponseContext rc = resp.getContext();
				ret = rc.getResult();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ret = -1;
		} finally {
			if (mReader != null) {
				try {
					mReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		startRefreshEmail();
		getCompleteDialog(0, ret);
	}

	private void getCompleteDialog(int type, int ret) {
		if (type == 0) {
			// change email
//			final Dialog promptDialog = new Dialog(this, R.style.softDialog);
			View view = LayoutInflater.from(this).inflate(
					R.layout.soft_uninstall_prompt_dialog, null);
			promptDialog.setContentView(view);

			TextView promptText = (TextView) promptDialog
					.findViewById(R.id.prompt_text);
			if (ret == 0) {
				promptText.setText(R.string.account_man_change_email_succ_text);
			} else {
				promptText.setText(R.string.account_man_change_email_fail_text);
			}
			Button button_ok = (Button) promptDialog
					.findViewById(R.id.button_ok);
			Button button_cancel = (Button) promptDialog
					.findViewById(R.id.button_cancel);
			button_ok.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog.dismiss();

				}
			});
			button_cancel.setVisibility(View.GONE);
			if (!promptDialog.isShowing()) {
				
				promptDialog.show();
			}
			return;
		}
		if (type == 1) {
			// change pwd
			View view = LayoutInflater.from(this).inflate(
					R.layout.soft_uninstall_prompt_dialog, null);
			promptDialog.setContentView(view);

			TextView promptText = (TextView) promptDialog
					.findViewById(R.id.prompt_text);
			switch (ret) {
			case 0:
				promptText.setText(R.string.account_man_change_pwd_succ_text);
				break;
			case 131:
				promptText.setText(R.string.account_manage_old_pass_invalid);
				break;
			case 132:
				promptText.setText(R.string.account_manage_new_pass_invalid);
				break;
			case 133:
				promptText.setText(R.string.account_manage_old_pass_err);
				break;
			default:
				promptText.setText(R.string.account_man_change_pwd_fail_text);
				break;
			}

			Button button_ok = (Button) promptDialog
					.findViewById(R.id.button_ok);
			Button button_cancel = (Button) promptDialog
					.findViewById(R.id.button_cancel);
			button_ok.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog.dismiss();

				}
			});
			button_cancel.setVisibility(View.GONE);
			if (!promptDialog.isShowing()) {
				promptDialog.show();
			}
			return;
		}
		if (type == 2) {
			// change phone
//			final Dialog promptDialog = new Dialog(this, R.style.softDialog);
			View view = LayoutInflater.from(this).inflate(
					R.layout.soft_uninstall_prompt_dialog, null);
			promptDialog.setContentView(view);

			TextView promptText = (TextView) promptDialog
					.findViewById(R.id.prompt_text);
			switch (ret) {
			case 0:
				promptText.setText(R.string.account_change_phone_succ);
				break;
			case 114:
				promptText.setText(R.string.account_change_phone_error);
				break;
			case 122:
				promptText.setText(R.string.account_change_phone_error);
				break;			
			}

			Button button_ok = (Button) promptDialog
					.findViewById(R.id.button_ok);
			Button button_cancel = (Button) promptDialog
					.findViewById(R.id.button_cancel);
			button_ok.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog.dismiss();

				}
			});
			button_cancel.setVisibility(View.GONE);
			if (!promptDialog.isShowing()) {
				
				promptDialog.show();
			}
			return;
		}
	}

	private void changePwdDialog() {
		final Dialog promptDialog = new Dialog(this, R.style.softDialog);
		View view = LayoutInflater.from(this).inflate(
				R.layout.change_pass_dialog, null);
		promptDialog.setContentView(view);
		final EditText et = (EditText) promptDialog.findViewById(R.id.et_line1);
		final EditText et2 = (EditText) promptDialog
				.findViewById(R.id.et_line2);
		final EditText old_et = (EditText) promptDialog
				.findViewById(R.id.et_old_pass);

		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String old_pass = old_et.getText().toString().trim();
				if (old_pass.equals("")) {
					Toast.makeText(AccountManageLoginedActivity.this,
							R.string.account_manage_input_old_pass_confirm,
							Toast.LENGTH_LONG).show();
					return;
				}
				String pwd1 = et.getText().toString().trim();
				String pwd2 = et2.getText().toString().trim();
				if (!pwd1.equals(pwd2)) {
					Toast.makeText(AccountManageLoginedActivity.this,
							R.string.account_man_change_pwd_input_error_text,
							Toast.LENGTH_LONG).show();
					et.setText("");
					et2.setText("");
				} else if (pwd1.length() < 4) {
					Toast.makeText(AccountManageLoginedActivity.this,
							R.string.password_notlessthan4, Toast.LENGTH_LONG)
							.show();
				} else {
					changePwd(old_pass, pwd1);
					promptDialog.dismiss();
				}

			}
		});
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				promptDialog.dismiss();

			}
		});
		final CheckBox procCheck = (CheckBox) promptDialog
				.findViewById(R.id.procCheck);
		procCheck
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (procCheck.isChecked()) {
							old_et.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
							et.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
							et2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
						} else {
							old_et.setInputType(InputType.TYPE_CLASS_TEXT
									| InputType.TYPE_TEXT_VARIATION_PASSWORD);
							et.setInputType(InputType.TYPE_CLASS_TEXT
									| InputType.TYPE_TEXT_VARIATION_PASSWORD);

							et2.setInputType(InputType.TYPE_CLASS_TEXT
									| InputType.TYPE_TEXT_VARIATION_PASSWORD);
						}
					}

				});
		promptDialog.show();
	}

	private void changePwd(String oldPwd, String newPwd) {
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
		isLoading = true;
		ChangePassThread cpt = new ChangePassThread(oldPwd, newPwd);
		loadingProgressDialog.show();
		cpt.start();
		new LoadingItem().start();
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
		}
	}
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:
				if(loadingProgressDialog != null){
					loadingProgressDialog.dismiss();
				}
				getCompleteDialog(1, msg.arg1);
				break;
			}
		}
	};
	private class ChangePassThread extends Thread {
		private String oldPwd;
		private String newPwd;
		public ChangePassThread(String oldPwd, String newPwd){
			this.oldPwd = oldPwd;
			this.newPwd = newPwd;
		}
		public void run() {

			String mToken = Utilis.getToken();
			
			AccountProtoc.PasswordRequest.Builder pwdBuilder = AccountProtoc.PasswordRequest
					.newBuilder();
			pwdBuilder.setOldPassword(oldPwd);
			pwdBuilder.setNewPassword(newPwd);
			RequestProtoc.RequestContext.Builder context = RequestProtoc.RequestContext
					.newBuilder();
			context.setAuthToken(mToken);

			RequestProtoc.Request.Builder request = RequestProtoc.Request
					.newBuilder();
			// request.setAccountRequest(accountRequ);
			request.setPasswordRequest(pwdBuilder);
			request.setContext(context);
			int ret = -1;
			Base64Handler base64 = new Base64Handler();
			BufferedReader mReader = null;
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet();
			try {
				URI url = new URI(Constant.ACCOUNT_CHANGE_PWD
						+ base64.encode(request.build().toByteArray()));
				httpGet.setURI(url);
				HttpResponse response = client.execute(httpGet);

				StringBuffer mBuffer = new StringBuffer();
				mReader = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				String line = mReader.readLine();
				while (line != null) {
					mBuffer.append(line);
					line = mReader.readLine();
				}

				ResponseProtoc.Response resp = ResponseProtoc.Response
						.parseFrom(base64.decodeBuffer(mBuffer.toString()));

				if (resp.hasContext()) {
					ResponseContext rc = resp.getContext();
					ret = rc.getResult();
					if (ret != 0) {
						ret = rc.getErrNO();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				ret = -1;
			} finally {
				if (mReader != null) {
					try {
						mReader.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			isLoading = false;
			Message msg = new Message();
			msg.what = 0;
			msg.arg1 = ret;
			handler.sendMessage(msg);
		}
	}

	private void getLogoffPromptDialog() {
		final Dialog promptDialog = new Dialog(this, R.style.softDialog);
		View view = LayoutInflater.from(this).inflate(
				R.layout.soft_uninstall_prompt_dialog, null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);
		promptText.setText(R.string.account_log_off_prompt_text);
		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				promptDialog.dismiss();
				Editor editor = KindroidSecurityApplication.sh.edit();

				editor.putString(Constant.SHAREDPREFERENCES_TOKEN, "");
				editor.putString(Constant.SHAREDPREFERENCES_USERNAME, "");
				editor.commit();
				Intent intent = new Intent(AccountManageLoginedActivity.this,
						AccountManageActivity.class);
				startActivity(intent);
				finish();

			}
		});
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				promptDialog.dismiss();

			}
		});
		promptDialog.show();
	}
}
