/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.Utilis;

public class BackupRestoreActivity extends Activity {
	public static final String LAST_BACK_CONTACTS_TIME = "last_back_contacts_time";
	private static final int LOGIN_REQUTEST_CODE = 9;
	private static final int RETURN_HOME_REQUEST_CODE = 99;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backup_restore);
		View home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent homepage = new Intent(BackupRestoreActivity.this,
						DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
			}
		});
		View backup_linear = findViewById(R.id.backup_linear);
		View restore_linear = findViewById(R.id.restore_linear);
		View browlist_linear = findViewById(R.id.browlist_linear);
		backup_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!Utilis.hasLogined()) {
					toLoginActivity();
				} else {
					Intent intent = new Intent();
					intent.setClass(BackupRestoreActivity.this,
							BackupActivity.class);
					startActivityForResult(intent, RETURN_HOME_REQUEST_CODE);
				}
			}
		});
		restore_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!Utilis.hasLogined()) {
					toLoginActivity();
				} else {
					Intent intent = new Intent();
					intent.setClass(BackupRestoreActivity.this,
							RestoreActivity.class);
					startActivityForResult(intent, RETURN_HOME_REQUEST_CODE);
				}
			}
		});
		browlist_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!Utilis.hasLogined()) {
					toLoginActivity();
				} else {
					Intent intent = new Intent();
					intent.setClass(BackupRestoreActivity.this,
							AppBackupDetailActivity.class);
					startActivityForResult(intent, RETURN_HOME_REQUEST_CODE);
				}
			}
		});

	}

	private void toLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivityForResult(intent, LOGIN_REQUTEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == LOGIN_REQUTEST_CODE
				&& resultCode == Activity.RESULT_OK) {
			refreshExpData();
		} else if (requestCode == RETURN_HOME_REQUEST_CODE
				&& resultCode == Activity.RESULT_OK) {
			finish();
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		refreshExpData();
	}

	private void refreshExpData() {
		TextView local_contacts_sum_text = (TextView) findViewById(R.id.local_contacts_sum_text);
		Uri localUri = ContactsContract.Contacts.CONTENT_URI;
		int contactsExist = 0;
		try {
			Cursor localCursor = getContentResolver().query(localUri, null,
					null, null, null);
			if (localCursor != null)
				contactsExist = localCursor.getCount();
			localCursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		local_contacts_sum_text.setText(contactsExist + "");

		SharedPreferences sh = KindroidSecurityApplication.sh;
		long last_backup_contacts_time = sh
				.getLong(LAST_BACK_CONTACTS_TIME, 0L);
		View last_backup_time_linear = findViewById(R.id.last_backup_time_linear);
		View last_backup_date_title = findViewById(R.id.last_backup_date_title);
		TextView last_backup_time_text = (TextView) findViewById(R.id.last_backup_time_text);
		if (last_backup_contacts_time == 0) {
			// last_backup_time_linear.setVisibility(View.GONE);
			last_backup_date_title.setVisibility(View.GONE);
			last_backup_time_text
					.setText(R.string.backup_restore_norecord_prompt_text);
			last_backup_time_text.setVisibility(View.VISIBLE);
		} else {
			Date dt = new Date(last_backup_contacts_time);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			last_backup_time_text.setText(sdf.format(dt));
			last_backup_time_linear.setVisibility(View.VISIBLE);
			last_backup_date_title.setVisibility(View.VISIBLE);
		}
		TextView user_name_text = (TextView) findViewById(R.id.user_name_text);
		String userName = Utilis.getUserName();
		if (userName != null && !userName.equals("")) {
			user_name_text.setText(userName);
		}
		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0:
					TextView remote_contacts_sum_text = (TextView) findViewById(R.id.remote_contacts_sum_text);
					remote_contacts_sum_text.setText(msg.arg1 + "");
					View remote_contacts_sum_linear = findViewById(R.id.remote_contacts_sum_linear);
					remote_contacts_sum_linear.setVisibility(View.VISIBLE);
					
					break;
				}
			}

		};
		if (Utilis.hasLogined()) {
			new Thread() {
				public void run() {					
					// access cloud server to obtain remote backuped
					// contacts,default 0
					int remoteContacts = 0;
					int remoteSms = 0;
					int remoteApplist = 0;
					String mToken = Utilis.getToken();
					if (mToken == null) {
						return;
					}
					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet();
					BufferedReader mReader = null;
					try {
						request.setURI(new URI(Constant.BACKUP_GETINFO_URL
								+ URLEncoder.encode(mToken, "utf-8")));
						HttpResponse response = client.execute(request);
						mReader = new BufferedReader(new InputStreamReader(
								response.getEntity().getContent()));
						StringBuilder sb = new StringBuilder();
						String line = mReader.readLine();
						while (line != null) {
							sb.append(line);
							line = mReader.readLine();
						}
						JSONObject jso = new JSONObject(sb.toString());
						int result = jso.getInt("result");
						if (result == 0) {
							JSONArray jArray = jso.getJSONArray("info");
							for (int i = 0; i < jArray.length(); i++) {
								JSONObject jObj = jArray.getJSONObject(i);
								if (jObj.getInt("fileType") == 1) {
									remoteContacts = jObj
											.getInt("entriesCount");

								}else
								if (jObj.getInt("fileType") == 2) {
									remoteSms = jObj
											.getInt("entriesCount");

								}else
								if (jObj.getInt("fileType") == 3) {
									remoteApplist = jObj
											.getInt("entriesCount");

								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						remoteContacts = 0;
					} finally {
						if (mReader != null) {
							try {
								mReader.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					Message msg = new Message();
					msg.what = 0;
					msg.arg1 = remoteContacts;
					msg.arg2 = remoteSms;
					msg.obj = Integer.valueOf(remoteApplist);
					handler.sendMessage(msg);

				}
			}.start();
		}
	}

}
