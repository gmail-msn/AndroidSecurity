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
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;

import com.kindroid.security.R;
import com.kindroid.security.util.Base64Handler;
import com.kindroid.security.util.CommonProtoc.EmailType;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.EmailProtoc.Email;
import com.kindroid.security.util.EmailProtoc.EmailRequest;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.LoginHelper;
import com.kindroid.security.util.RequestProtoc;
import com.kindroid.security.util.ResponseProtoc;
import com.kindroid.security.util.ResponseProtoc.Response;
import com.kindroid.security.util.Utilis;

public class RemoteSecuritySetupWizardActivity extends Activity implements
		View.OnClickListener {

	private EditText et_username;
	private EditText et_passwd;
	private Button register_account_bt;
	private Button next_step_to_step_two_tv;
	private TextView find_pwd_tv;

	private TextView safe_mobile_input_et;
	private CheckBox checkbox_lock_mobile;
	private TextView mobile_number_des_tv;
	private TextView lock_mobile_des_tv;
	private Button nextstep_to_step_three_bt;
	private Button sms_content_bt;
	private EditText et_line1;
	Dialog promoteDialog;

	private TextView stepover_update_sim_tv;
	private TextView stepover_lock_mobile_tv;
	private Button nextstep_to_over;
	private EditText email_et;

	private LinearLayout step_one_lieanr;
	private LinearLayout step_two_lieanr;
	private LinearLayout step_three_lieanr;

	private TextView first_step_tv;
	private TextView second_step_tv;
	private TextView three_step_tv;
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

	private int curPosition = 1;
	private String safeMobileNumber;
	private boolean isTrueToUpdateLockMobile = true;
	private String update_sim_send_sms_content;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.remote_security_setup_wizard_activity);
		findView();
		bindListerToView();
		setUpLogin();

	}

	void setUpLogin() {
		curPosition = 1;
		step_one_lieanr.setVisibility(View.VISIBLE);
		step_two_lieanr.setVisibility(View.GONE);
		step_three_lieanr.setVisibility(View.GONE);
		first_step_tv
				.setTextColor(getResources().getColor(R.color.light_black));
		second_step_tv.setTextColor(Color.argb(255, 153, 153, 153));
		three_step_tv.setTextColor(Color.argb(255, 153, 153, 153));
		find_pwd_tv.setText(Html.fromHtml("<a href=>"
				+ getString(R.string.press_here_back_pwd) + "</a>"));

	}

	void setUpFillDate() {
		curPosition = 2;
		step_one_lieanr.setVisibility(View.GONE);
		step_two_lieanr.setVisibility(View.VISIBLE);
		step_three_lieanr.setVisibility(View.GONE);

		first_step_tv.setTextColor(Color.argb(255, 153, 153, 153));
		second_step_tv.setTextColor(getResources()
				.getColor(R.color.light_black));
		three_step_tv.setTextColor(Color.argb(255, 153, 153, 153));
		String str_mobile_des = "<font color=#64BD45>" + "1"
				+ getString(R.string.chiness_dun) + " "
				+ getResources().getString(R.string.setting_safe_mobile_number)
				+ "</font>";
		str_mobile_des += "<font color=#202020>"
				+ getResources().getString(
						R.string.setting_safe_mobile_number_des) + "</font>";

		mobile_number_des_tv.setText(Html.fromHtml(str_mobile_des));

		String str_lock_mobile_des = "<font color=#64BD45>"
				+ "2 "
				+ getString(R.string.chiness_dun)
				+ " "
				+ getResources().getString(
						R.string.update_sim_lock_mobile_no_space) + "</font>";
		str_lock_mobile_des += "<font color=#202020>"
				+ getResources().getString(R.string.update_sim_lock_mobile_des)
				+ "</font>";

		lock_mobile_des_tv.setText(Html.fromHtml(str_lock_mobile_des));

	}

	void setUpOver() {
		curPosition = 3;
		step_one_lieanr.setVisibility(View.GONE);
		step_two_lieanr.setVisibility(View.GONE);
		step_three_lieanr.setVisibility(View.VISIBLE);

		first_step_tv.setTextColor(Color.argb(255, 153, 153, 153));
		second_step_tv.setTextColor(Color.argb(255, 153, 153, 153));
		three_step_tv
				.setTextColor(getResources().getColor(R.color.light_black));

		String str_step_over_update_sim = String.format(
				getString(R.string.step_over_update_sim), safeMobileNumber);
		stepover_update_sim_tv.setText(str_step_over_update_sim);
		stepover_lock_mobile_tv
				.setText(isTrueToUpdateLockMobile ? R.string.step_over_set_lock_mobile
						: R.string.step_over_noset_lock_mobile);

	}

	void findView() {
		/* step one use var */
		et_username = (EditText) findViewById(R.id.user_name_input);
		et_passwd = (EditText) findViewById(R.id.password_input);
		register_account_bt = (Button) findViewById(R.id.register_account_bt);
		next_step_to_step_two_tv = (Button) findViewById(R.id.nextstep_to_step_two_bt);
		find_pwd_tv = (TextView) findViewById(R.id.find_pwd_tv);

		/* step two use var */

		safe_mobile_input_et = (TextView) findViewById(R.id.safe_mobile_input_et);
		checkbox_lock_mobile = (CheckBox) findViewById(R.id.procCheck);
		mobile_number_des_tv = (TextView) findViewById(R.id.safe_mobile_des_tv);
		lock_mobile_des_tv = (TextView) findViewById(R.id.lock_mobile_des_tv);
		nextstep_to_step_three_bt = (Button) findViewById(R.id.nextstep_to_step_three_bt);
		sms_content_bt = (Button) findViewById(R.id.sms_content_bt);
		/* step three use var */
		stepover_update_sim_tv = (TextView) findViewById(R.id.stepover_update_sim_tv);
		stepover_lock_mobile_tv = (TextView) findViewById(R.id.stepover_lock_mobile_tv);
		nextstep_to_over = (Button) findViewById(R.id.nextstep_to_over);
		email_et = (EditText) findViewById(R.id.email_et);

		first_step_tv = (TextView) findViewById(R.id.first_step_tv);
		second_step_tv = (TextView) findViewById(R.id.second_step_tv);
		three_step_tv = (TextView) findViewById(R.id.three_step_tv);

		step_one_lieanr = (LinearLayout) findViewById(R.id.step_one_linear);
		step_two_lieanr = (LinearLayout) findViewById(R.id.step_two_linear);
		step_three_lieanr = (LinearLayout) findViewById(R.id.step_three_linear);

	}

	void bindListerToView() {
		register_account_bt.setOnClickListener(this);
		next_step_to_step_two_tv.setOnClickListener(this);
		nextstep_to_step_three_bt.setOnClickListener(this);
		sms_content_bt.setOnClickListener(this);
		nextstep_to_over.setOnClickListener(this);
		find_pwd_tv.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.nextstep_to_step_three_bt:
			String mobile = safe_mobile_input_et.getText().toString().trim();
			if (!ConvertUtils.isCellphone(mobile)) {
				Toast.makeText(this,
						getResources().getString(R.string.input_valide_mobile),
						Toast.LENGTH_SHORT).show();
				return;
			}
			safeMobileNumber = mobile;
			isTrueToUpdateLockMobile = checkbox_lock_mobile.isChecked();
			setUpOver();
			break;
		case R.id.sms_content_bt:
			showUpdatePasswdDialog();

			break;

		case R.id.register_account_bt:
			startActivityForResult(new Intent(
					RemoteSecuritySetupWizardActivity.this,
					RegisterActivity.class), 1);

			break;
		case R.id.find_pwd_tv:
			startActivityForResult(new Intent(
					RemoteSecuritySetupWizardActivity.this,
					ForgotPwdActivity.class), 2);

			break;
		case R.id.nextstep_to_over:
			String email = email_et.getText().toString().trim();

			if (email.length() != 0) {

				if (!checkNetwork()) {
					Toast.makeText(RemoteSecuritySetupWizardActivity.this,
							R.string.bakcup_remote_network_unabailable_text,
							Toast.LENGTH_LONG).show();
					return;
				}

				String token = Utilis.getToken();
				checkAndSendMessage(token, email);
			} else {
				saveSettingMessage();
				setResult(RESULT_OK);
				finish();
			}
			break;

		case R.id.nextstep_to_step_two_bt:
			if (!checkNetwork()) {
				Toast.makeText(RemoteSecuritySetupWizardActivity.this,
						R.string.bakcup_remote_network_unabailable_text,
						Toast.LENGTH_LONG).show();
				return;
			}

			String uname = et_username.getText().toString().trim();
			String pwd = et_passwd.getText().toString().trim();
			if (uname.equals("")) {
				Toast.makeText(RemoteSecuritySetupWizardActivity.this,
						R.string.backup_login_user_name_empty,
						Toast.LENGTH_LONG).show();
				return;
			}
			if (pwd.equals("")) {
				Toast.makeText(RemoteSecuritySetupWizardActivity.this,
						R.string.backup_login_password_empty, Toast.LENGTH_LONG)
						.show();
				return;
			}
			if (uname.length() < 3 || uname.length() > 45) {
				Toast.makeText(RemoteSecuritySetupWizardActivity.this,
						R.string.backup_login_error_uname, Toast.LENGTH_LONG)
						.show();
				return;
			}
			if (pwd.length() < 4) {
				Toast.makeText(RemoteSecuritySetupWizardActivity.this,
						R.string.password_notlessthan4, Toast.LENGTH_LONG)
						.show();
				return;
			}
			isLoading = true;
			showDialog();
			new RequestThread(uname, pwd).start();

		}
	}

	void checkAndSendMessage(String token, String email) {
		if (!checkEmail(email)) {
			Toast.makeText(this, R.string.email_invalid, Toast.LENGTH_LONG)
					.show();
			return;
		}
		if (token == null)
			return;
		new SendEmailThread(token, email, safeMobileNumber).start();
		isLoading = true;
		showDialog();

	}

	public boolean checkEmail(String email) {
		if (TextUtils.isEmpty(email)) {
			return false;
		}
		Pattern pattern = Pattern
				.compile("^[a-zA-Z0-9][a-zA-Z0-9-_.]+?@([a-zA-Z0-9]+(?:\\.[a-zA-Z0-9-_]+){1,})$");
		return (pattern.matcher(email)).matches();
	}

	void saveSettingMessage() {
		String pwd = et_passwd.getText().toString().trim();
		Editor editor = KindroidSecurityApplication.sh.edit();
		editor.putString(Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD,
				ConvertUtils.getMD5(pwd.getBytes()));
		editor.putBoolean(Constant.SHAREDPREFERENCES_REMOTESECURITY, true);
		editor.putString(Constant.SHAREDPREFERENCES_SAFEMOBILENUMBER,
				safeMobileNumber);
		editor.putBoolean(
				Constant.SHAREDPREFERENCES_AFTERUPDATESIMTOLOCKMOBILE,
				isTrueToUpdateLockMobile);
		if (update_sim_send_sms_content != null)
			editor.putString(Constant.SHAREDPREFERENCES_AFTERUPDATESIMSENDMES,
					update_sim_send_sms_content);

		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String sbuStr = tm.getSubscriberId();
		if (sbuStr != null)
			editor.putString(Constant.SHAREDPREFERENCES_SIMUNIQUETAG, sbuStr);
		editor.commit();

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
				Toast.makeText(RemoteSecuritySetupWizardActivity.this,
						R.string.backup_login_name_pwd_err, Toast.LENGTH_LONG)
						.show();
				loginResult = false;
				return;
			}

			if (!loginResult || msg.what != 0) {
				Toast.makeText(RemoteSecuritySetupWizardActivity.this,
						R.string.backup_login_fail_prompt, Toast.LENGTH_LONG)
						.show();

				return;
			}
			loadingProgressDialog.cancel();
			Toast.makeText(RemoteSecuritySetupWizardActivity.this,
					R.string.login_suc, Toast.LENGTH_LONG).show();

			setUpFillDate();

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

	void showUpdatePasswdDialog() {

		promoteDialog = new Dialog(this, R.style.Theme_CustomDialog) {
			@Override
			public void onBackPressed() {
				// TODO Auto-generated method stub
				super.onBackPressed();
				promoteDialog.cancel();
			}
		};
		View view = LayoutInflater.from(this).inflate(
				R.layout.update_passwd_dialog, null);
		promoteDialog.setContentView(view);

		TextView titleText = (TextView) promoteDialog
				.findViewById(R.id.title_text_tv);

		TextView promptText = (TextView) promoteDialog
				.findViewById(R.id.prompt_text);

		et_line1 = (EditText) promoteDialog.findViewById(R.id.et_line1);
		EditText et_line2 = (EditText) promoteDialog
				.findViewById(R.id.et_line2);
		CheckBox procCheck = (CheckBox) promoteDialog
				.findViewById(R.id.procCheck);
		Button button_ok = (Button) promoteDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promoteDialog
				.findViewById(R.id.button_cancel);

		titleText.setText(getResources().getString(R.string.sms_content));
		promptText.setVisibility(View.GONE);
		et_line2.setVisibility(View.GONE);
		procCheck.setVisibility(View.GONE);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) et_line1
				.getLayoutParams();
		params.height = 130;

		et_line1.setTextSize(12);
		et_line1.setSingleLine(false);
		et_line1.setHint(R.string.you_can_write_70c);
		et_line1.setGravity(Gravity.TOP);
		et_line1.setInputType(InputType.TYPE_CLASS_TEXT);
		et_line1.setSingleLine(false);
		et_line1.setLayoutParams(params);
		String str = KindroidSecurityApplication.sh.getString(
				Constant.SHAREDPREFERENCES_AFTERUPDATESIMSENDMES,
				getResources().getString(R.string.your_mobile_maybe_lost));
		et_line1.setText(str);
		et_line1.setSelection(str.length());
		button_ok.setOnClickListener(smsListener);

		button_cancel.setOnClickListener(smsListener);

		promoteDialog.show();
	}

	android.view.View.OnClickListener smsListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.button_ok:
				String str = et_line1.getText().toString().trim();
				if (str.length() == 0) {
					Toast.makeText(RemoteSecuritySetupWizardActivity.this,
							getResources().getString(R.string.sms_lengh_gt_0),
							Toast.LENGTH_SHORT).show();
					return;
				}
				update_sim_send_sms_content = str;
				promoteDialog.cancel();
				break;
			case R.id.button_cancel:
				promoteDialog.cancel();
				break;
			}

		}
	};

	class SendEmailThread extends Thread {
		String token;
		String email;
		String safeNumber;

		public SendEmailThread(String token, String email, String safeNumber) {
			this.token = token;
			this.email = email;
			this.safeNumber = safeNumber;
		}

		@Override
		public void run() {
			Response reponse = sendIntoductionByEmail(token, email, safeNumber);

			isLoading = false;
			handler.sendEmptyMessage(-2);
			if (reponse == null || !reponse.hasContext()) {
				sendEmailHandler.sendEmptyMessage(-2);
			} else {
				int result = reponse.getContext().getResult();
				if (result == 0)
					sendEmailHandler.sendEmptyMessage(0);
				else {
					if (reponse.getContext().hasErrNO()) {
						sendEmailHandler.sendEmptyMessage(reponse.getContext()
								.getErrNO());
					} else
						sendEmailHandler.sendEmptyMessage(-2);
				}

			}

		}
	}

	private Handler sendEmailHandler = new Handler() {
		public void dispatchMessage(Message msg) {

			switch (msg.what) {
			case -2:

				Toast.makeText(RemoteSecuritySetupWizardActivity.this,
						R.string.network_tip, Toast.LENGTH_LONG).show();
				break;
			case 0:
				Toast.makeText(RemoteSecuritySetupWizardActivity.this,
						R.string.send_email_suc, Toast.LENGTH_LONG).show();
				saveSettingMessage();
				setResult(RESULT_OK);
				finish();
				break;
			case 601:
			case 602:
				Toast.makeText(RemoteSecuritySetupWizardActivity.this,
						R.string.email_invalid, Toast.LENGTH_LONG).show();
				break;

			default:
				Toast.makeText(RemoteSecuritySetupWizardActivity.this,
						R.string.send_faile, Toast.LENGTH_LONG).show();
				break;
			}

		};
	};

	Response sendIntoductionByEmail(String token, String str_email,
			String safeNumber) {

		RequestProtoc.Request.Builder request = RequestProtoc.Request
				.newBuilder();
		RequestProtoc.RequestContext.Builder context = RequestProtoc.RequestContext
				.newBuilder();
		EmailRequest.Builder emailRequest = EmailRequest.newBuilder();
		Email.Builder email = Email.newBuilder();
		email.setEmailType(EmailType.TUTORIAL);
		email.setReceiver(str_email);
		emailRequest.setEmail(email);
		emailRequest.setSafePhone(safeNumber);

		context.setAuthToken(token);
		request.setContext(context);
		request.setEmailRequest(emailRequest);
		try {
			String request_str = new String(Base64.encodeBase64(request.build()
					.toByteArray(), true));
			request_str = URLEncoder.encode(request_str);
			InputStream in = postData(Constant.SENDINTRODUCTIONBYEMAIL_URL,
					request_str, true);
			Base64Handler base64 = new Base64Handler();
			ResponseProtoc.Response resp = ResponseProtoc.Response
					.parseFrom(base64.decodeBuffer(ConvertUtils
							.inputSreamToString(in)));

			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public InputStream postData(String urlStr, String request,
			boolean useRequest) throws ParserConfigurationException,
			IOException, ConnectTimeoutException, UnknownHostException {
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setDoOutput(true);
		// connection.setUseCaches(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		connection.setConnectTimeout(8000);
		connection.setReadTimeout(5000);
		if (useRequest) {
			String requestContent = "request=" + request;
			connection.getOutputStream().write(requestContent.getBytes());
			connection.getOutputStream().flush();
			connection.getOutputStream().close();
		}

		return connection.getInputStream();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (curPosition == 3) {
			setUpFillDate();
			return true;

		} else if (curPosition == 2) {
			setUpLogin();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

}