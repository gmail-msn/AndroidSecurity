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
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.KindroidSecurityApplication;

public class TempActivity extends Activity implements OnCheckedChangeListener {
	String passwd;
	EditText et_line1;
	EditText et_line2;
	CheckBox procCheck;
	int type = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		View view = new View(this);
		view.setBackgroundColor(Color.GRAY);
		setContentView(view);
		String passwd = KindroidSecurityApplication.sh.getString(
				Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD, "");
		type = passwd.equals("") ? 1 : 2;
		showActivityDialog();

	}

	void showActivityDialog() {

		final Dialog promptDialog = new Dialog(TempActivity.this,
				R.style.Theme_CustomDialog) {
			@Override
			public void onBackPressed() {
				// TODO Auto-generated method stub
				super.onBackPressed();
				Intent i = new Intent();
				Bundle b = new Bundle();
				b.putBoolean("logined", false);
				i.putExtras(b);
				setResult(RESULT_OK, i);
				finish();
			}
		};
		View view = LayoutInflater.from(TempActivity.this).inflate(
				R.layout.update_passwd_dialog, null);
		promptDialog.setContentView(view);
		TextView titleText = (TextView) promptDialog
				.findViewById(R.id.title_text_tv);
		LinearLayout linear = (LinearLayout) promptDialog
				.findViewById(R.id.et_linear1);
		LinearLayout linear2 = (LinearLayout) promptDialog
				.findViewById(R.id.et_linear2);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);

		et_line1 = (EditText) promptDialog.findViewById(R.id.et_line1);
		et_line2 = (EditText) promptDialog.findViewById(R.id.et_line2);
		procCheck = (CheckBox) promptDialog.findViewById(R.id.procCheck);
		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		if (type == 1) {
			promptText.setText(R.string.first_open_input_passwd);
		} else {
			promptText.setText(R.string.input_passwd_goto_kindscret);
			et_line2.setVisibility(View.GONE);

		}

		procCheck.setOnCheckedChangeListener(this);

		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkMessage(et_line1.getText().toString().trim(), et_line2
						.getText().toString().trim(), type)) {

					if (type == 1) {
						KindroidSecurityApplication.sh
								.edit()
								.putString(
										Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD,
										ConvertUtils.getMD5(et_line1.getText()
												.toString().trim().getBytes()))
								.commit();
					}
					Intent i = new Intent();
					Bundle b = new Bundle();
					b.putBoolean("logined", true);
					i.putExtras(b);
					setResult(RESULT_OK, i);
					promptDialog.dismiss();
					finish();
				}
			}
		});
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				promptDialog.dismiss();
				Intent i = new Intent();
				Bundle b = new Bundle();
				b.putBoolean("logined", false);
				i.putExtras(b);
				setResult(RESULT_OK, i);
				finish();
			}
		});
		promptDialog.show();
	}

	boolean checkMessage(String str, String str2, int type) {

		if (type == 1) {
			if (str.length() == 0 || str2.length() == 0) {
				Toast.makeText(
						this,
						getResources()
								.getString(R.string.passwd_can_notbe_null),
						Toast.LENGTH_SHORT).show();
				return false;
			}
			if (str.length() > 16 || str2.length() > 16) {
				Toast.makeText(
						this,
						getResources().getString(
								R.string.passwd_length_canot_gt_16),
						Toast.LENGTH_SHORT).show();
				return false;
			}
			if (!str.equals(str2)) {
				Toast.makeText(
						this,
						getResources().getString(
								R.string.passwd_surepasswd_same),
						Toast.LENGTH_SHORT).show();
				return false;
			}
		} else if (type == 2) {
			if (!ConvertUtils
					.getMD5(str.getBytes())
					.equals(KindroidSecurityApplication.sh
							.getString(
									Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD,
									""))) {
				Toast.makeText(
						this,
						getResources().getString(
								R.string.input_corect_passwd_please),
						Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return true;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked) {

			et_line1.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
			et_line2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

		} else {

			et_line1.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);

			et_line2.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		}
		et_line1.setSelection(et_line1.getText().toString().length());
		et_line2.setSelection(et_line2.getText().toString().length());
	}

}