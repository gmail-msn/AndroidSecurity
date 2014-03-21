/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;

public class RemoteSecuritySetupActivity extends Activity implements
		View.OnClickListener {

	private TextView mobile_number_des_tv;
	private TextView lock_mobile_des_tv;
	private Button nextstep_to_step_three_bt;
	private Button sms_content_bt;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.remote_security_setup);
		findView();
		bindListerToView();
		setUpFillDate();
		Editor editor = KindroidSecurityApplication.sh.edit();
		editor.putBoolean(
				Constant.SHAREDPREFERENCES_FIRSTINSTALLREMOTESECURITY, false);
		editor.commit();

	}

	void setUpFillDate() {
		String str_mobile_des = "<font color=#64BD45>" + "1."
				+ getResources().getString(R.string.setting_safe_mobile_number)
				+ "</font>";
		str_mobile_des += "<font color=#202020>"
				+ getResources().getString(
						R.string.setting_safe_mobile_number_des) + "</font>";

		mobile_number_des_tv.setText(Html.fromHtml(str_mobile_des));

		String str_lock_mobile_des = "<font color=#64BD45>"
				+ "2."
				+ getResources().getString(
						R.string.update_sim_lock_mobile_no_space) + "</font>";
		str_lock_mobile_des += "<font color=#202020>"
				+ getResources().getString(R.string.update_sim_lock_mobile_des)
				+ "</font>";

		lock_mobile_des_tv.setText(Html.fromHtml(str_lock_mobile_des));

	}

	void findView() {
		/* step two use var */
		mobile_number_des_tv = (TextView) findViewById(R.id.safe_mobile_des_tv);
		lock_mobile_des_tv = (TextView) findViewById(R.id.lock_mobile_des_tv);
		nextstep_to_step_three_bt = (Button) findViewById(R.id.nextstep_to_step_three_bt);
		sms_content_bt = (Button) findViewById(R.id.sms_content_bt);
	}

	void bindListerToView() {

		nextstep_to_step_three_bt.setOnClickListener(this);
		sms_content_bt.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nextstep_to_step_three_bt:
			startActivityForResult(new Intent(RemoteSecuritySetupActivity.this,
					RemoteSecuritySetupWizardActivity.class), 3);
			break;
		case R.id.sms_content_bt:
			Intent intent = new Intent("ehoo.com.update.remote.ui");
			intent.putExtra("upid", 0);
			sendBroadcast(intent);

			break;

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 3 && resultCode == RESULT_OK) {
			Intent intent = new Intent("ehoo.com.update.remote.ui");
			intent.putExtra("upid", 0);
			sendBroadcast(intent);
		}

	}

}