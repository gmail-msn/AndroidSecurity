/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-07
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ContentProviderOperation.Builder;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.kindroid.security.R;
import com.kindroid.security.util.BackupDBHelper;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.Utilis;

public class RestoreActivity extends Activity {
	private boolean mContactsRestore = false;
	private boolean mSmsRestore = false;
	private boolean mRestoreContacts = false;
	private boolean mRestoreSms = false;
	private boolean mContactsRestoreSucc = false;
	private boolean mSmsRestoreSucc = false;
	private static boolean isRestoringContacts = false;
	private static boolean isRestoringSms = false;
	private int contactsExist = 0;
	private int smsExist = 0;
	private CheckBox restore_contacts_cb;
	private CheckBox restore_sms_cb;
	private CheckBox select_al_cb;
	private View restore_progress_linear;
	private TextView restoring_text;
	private TextView restore_progress_text;
	private ProgressBar restore_progress;
	private View restore_action_linear;
	private View restore_cancel_linear;
	// private static boolean mCanCancel = true;
	private RestoreThread mRestoreThread;

	private View home_page;
	private boolean mOnlyChangeState = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restore);
		home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setResult(Activity.RESULT_OK);
				finish();
			}
		});
		restore_progress_linear = findViewById(R.id.restore_progress_linear);
		restoring_text = (TextView) findViewById(R.id.restoring_text);
		restore_progress_text = (TextView) findViewById(R.id.restore_progress_text);
		restore_progress = (ProgressBar) findViewById(R.id.restore_progress);
		restore_action_linear = findViewById(R.id.restore_action_linear);
		restore_cancel_linear = findViewById(R.id.restore_cancel_linear);
		restore_action_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mContactsRestore && !mSmsRestore) {
					Toast.makeText(RestoreActivity.this,
							R.string.recover_select_content_prompt,
							Toast.LENGTH_LONG).show();
					return;
				}
				getRestorePromptDialog();

			}
		});

		View privacy_text = findViewById(R.id.privacy_text);
		privacy_text.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(RestoreActivity.this,
						PrivacyActivity.class);
				startActivity(intent);

			}
		});
		restore_contacts_cb = (CheckBox) findViewById(R.id.restore_contacts_cb);
		restore_contacts_cb
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						mContactsRestore = isChecked;
						if(!isChecked){
							changeAllCbState(isChecked);
						}else{
							if(restore_sms_cb.isChecked()){
								changeAllCbState(isChecked);
							}
						}
					}

				});
		restore_sms_cb = (CheckBox) findViewById(R.id.restore_sms_cb);
		restore_sms_cb
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						mSmsRestore = isChecked;
						if(!isChecked){
							changeAllCbState(isChecked);
						}else{
							if(restore_contacts_cb.isChecked()){
								changeAllCbState(isChecked);
							}
						}
					}

				});
		select_al_cb = (CheckBox) findViewById(R.id.select_al_cb);
		select_al_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (!mOnlyChangeState) {
					restore_contacts_cb.setChecked(isChecked);
					restore_sms_cb.setChecked(isChecked);
				}
			}

		});
		View restore_contacts_out_linear = findViewById(R.id.restore_contacts_out_linear);
		View restore_sms_out_linear = findViewById(R.id.restore_sms_out_linear);
		restore_sms_out_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				restore_sms_cb.setChecked(!restore_sms_cb.isChecked());
			}
		});
		restore_contacts_out_linear
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						restore_contacts_cb.setChecked(!restore_contacts_cb
								.isChecked());
					}
				});

	}

	private void changeAllCbState(boolean state) {
		mOnlyChangeState = true;
		select_al_cb.setChecked(state);
		mOnlyChangeState = false;
	}

	private void getRestorePromptDialog() {
		final Dialog promptDialog = new Dialog(this, R.style.Theme_CustomDialog);
		View view = LayoutInflater.from(this).inflate(
				R.layout.restore_prompt_dialog, null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);

		promptText.setText(R.string.restore_prompt_text);

		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				promptDialog.dismiss();
			}
		});
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startRestore();
				promptDialog.dismiss();
			}
		});
		promptDialog.show();
	}

	private boolean hasBackupData() {
		if (!Utilis.checkNetwork(this)) {
			Toast.makeText(this,
					R.string.bakcup_remote_network_unabailable_text,
					Toast.LENGTH_LONG).show();
			return false;
		}
		int remoteContacts = 0;
		int remoteSms = 0;
		String mToken = Utilis.getToken();
		if (mToken == null) {
			return false;
		}
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet();
		BufferedReader mReader = null;
		try {
			request.setURI(new URI(Constant.BACKUP_GETINFO_URL
					+ URLEncoder.encode(mToken, "utf-8")));
			HttpResponse response = client.execute(request);
			mReader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
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
					if (jObj.get("fileType") != null
							&& jObj.getInt("fileType") == 1) {
						remoteContacts = jObj.getInt("entriesCount");

					}
					if (jObj.get("fileType") != null
							&& jObj.getInt("fileType") == 2) {
						remoteSms = jObj.getInt("entriesCount");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			remoteContacts = 0;
			remoteSms = 0;
		} finally {
			if (mReader != null) {
				try {
					mReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		boolean ret = false;
		boolean ret1 = false;
		if (remoteContacts > 0 && mRestoreContacts) {
			ret = true;
		}
		if (remoteSms > 0 && mRestoreSms) {
			ret1 = true;
		}
		if ((!ret && mRestoreContacts) && (!ret1 && mRestoreSms)) {
			Toast.makeText(this, R.string.recover_has_no_data_prompt,
					Toast.LENGTH_LONG).show();
			select_al_cb.setChecked(false);
		} else {
			if (!ret && mRestoreContacts) {
				Toast.makeText(
						this,
						getString(R.string.backup_contacts_text)
								+ " "
								+ getString(R.string.recover_has_no_data_prompt),
						Toast.LENGTH_LONG).show();
				restore_contacts_cb.setChecked(false);
			}
			if (!ret1 && mRestoreSms) {
				Toast.makeText(
						this,
						getString(R.string.backup_sms_text)
								+ " "
								+ getString(R.string.recover_has_no_data_prompt),
						Toast.LENGTH_LONG).show();
				restore_sms_cb.setChecked(false);
			}
		}
		return ret || ret1;
	}

	private void startRestore() {
		mRestoreContacts = mContactsRestore;
		mRestoreSms = mSmsRestore;
		if (!Utilis.checkNetwork(this)) {
			Toast.makeText(this,
					R.string.bakcup_remote_network_unabailable_text,
					Toast.LENGTH_LONG).show();
			return;
		}
		// obtain backup count
		restore_progress_linear.setVisibility(View.VISIBLE);
		restore_progress.setProgress(0);
		// mCanCancel = true;
		home_page.setVisibility(View.GONE);

		restore_action_linear.setVisibility(View.GONE);
		restore_cancel_linear.setVisibility(View.VISIBLE);
		mRestoreThread = new RestoreThread();
		restore_cancel_linear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getCancelPromptDialog();				
			}
		});

		mRestoreThread.start();
	}

	Handler mHandler = new Handler() {
		// å¤„ç�†æ¶ˆæ�¯
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// start restore contacts
			case 0:
				restoring_text.setText(getString(R.string.restoring_text_new)
						+ " " + getString(R.string.backup_contacts_text));
				restore_progress_text.setText("(0%)");
				restore_progress.setProgress(0);
				break;
			// update restore contacts progress
			case 1:
				restore_progress.setProgress(msg.arg1);
				restore_progress_text.setText("(" + msg.arg1 + "%)");
				break;
			// restore contacts success
			case 2:
				Toast.makeText(
						RestoreActivity.this,
						getString(R.string.backup_contacts_text)
								+ getString(R.string.restore_completed_confirm_prompt_text),
						Toast.LENGTH_LONG).show();
				break;
			// restore contacts fail
			case 3:
				Toast.makeText(
						RestoreActivity.this,
						getString(R.string.backup_contacts_text)
								+ getString(R.string.restore_error_prompt_text),
						Toast.LENGTH_LONG).show();
				break;
			// start restore sms
			case 4:
				restoring_text.setText(getString(R.string.restoring_text_new)
						+ " " + getString(R.string.backup_sms_text));
				restore_progress_text.setText("(0%)");
				restore_progress.setProgress(0);
				break;
			// update restore sms progress
			case 5:
				restore_progress.setProgress(msg.arg1);
				restore_progress_text.setText("(" + msg.arg1 + "%)");
				break;
			// restore sms success
			case 6:
				Toast.makeText(
						RestoreActivity.this,
						getString(R.string.backup_sms_text)
								+ getString(R.string.restore_completed_confirm_prompt_text),
						Toast.LENGTH_LONG).show();
				break;
			// restore sms fail
			case 7:
				Toast.makeText(
						RestoreActivity.this,
						getString(R.string.backup_sms_text)
								+ getString(R.string.restore_error_prompt_text),
						Toast.LENGTH_LONG).show();
				break;

			// backup canceled
			case 23:
				Toast.makeText(RestoreActivity.this,
						R.string.restore_cancel_prompt_text, Toast.LENGTH_LONG)
						.show();
				break;

			// complete restore, refresh ui
			case 60:
				restore_progress_linear.setVisibility(View.GONE);
				restore_cancel_linear.setVisibility(View.GONE);
				restore_action_linear.setVisibility(View.VISIBLE);
				refreshExpData();
				StringBuilder sb = new StringBuilder();
				StringBuilder sb1 = new StringBuilder();
				if (mRestoreContacts) {
					restore_contacts_cb.setChecked(false);
					if (mContactsRestoreSucc) {
						sb.append(getString((R.string.backup_contacts_text)))
								.append(", ");
					} else {
						sb1.append(getString((R.string.backup_contacts_text)))
								.append(", ");
					}
				}
				if (mRestoreSms) {
					restore_sms_cb.setChecked(false);
					if (mSmsRestoreSucc) {
						sb.append(getString((R.string.backup_sms_text)))
								.append(", ");
					} else {
						sb1.append(getString((R.string.backup_sms_text)))
								.append(", ");
					}
				}
				if (select_al_cb.isChecked()) {
					select_al_cb.setChecked(false);
				}
				if (sb.length() > 0) {
					sb.append(getString(R.string.restore_completed_confirm_prompt_text));
				}
				if (sb1.length() > 0) {
					sb1.append(getString(R.string.restore_error_prompt_text));
				}
				sb.append("\n").append(sb1);
				Toast.makeText(RestoreActivity.this, sb.toString(),
						Toast.LENGTH_LONG).show();
				home_page.setVisibility(View.VISIBLE);
				break;
			case 61:
				restore_progress_linear.setVisibility(View.GONE);
				restore_cancel_linear.setVisibility(View.GONE);
				restore_action_linear.setVisibility(View.VISIBLE);
				refreshExpData();
				if (select_al_cb.isChecked()) {
					select_al_cb.setChecked(false);
				}
				if (restore_contacts_cb.isChecked()) {
					restore_contacts_cb.setChecked(false);
				}
				if (restore_sms_cb.isChecked()) {
					restore_sms_cb.setChecked(false);
				}
				Toast.makeText(RestoreActivity.this,
						R.string.restore_cancel_prompt_text, Toast.LENGTH_LONG)
						.show();
				home_page.setVisibility(View.VISIBLE);
				break;
			}
		}
	};

	/**
	 * record restore log
	 * 
	 * @param time
	 *            backup time;
	 * @param flag
	 *            flag of success or fail for backup.0:fail,1:success
	 * @param type
	 *            restore type of contacts or sms.1:contacts backup;2:sms
	 *            backup;3:applist backup;4:contacts restore;5:sms restore
	 * @param num
	 *            number of backed info
	 * @param traf
	 *            net traffic for upload backup file
	 */
	private void backLog(long time, int flag, int type, int num, int traf) {
		BackupDBHelper dh = new BackupDBHelper(this, BackupDBHelper.logDBName);
		SQLiteDatabase db = dh.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(BackupDBHelper.COLUMN_TIME, time);
		values.put(BackupDBHelper.COLUMN_FLAG, flag);
		values.put(BackupDBHelper.COLUMN_TYPE, type);
		values.put(BackupDBHelper.COLUMN_NUM, num);
		values.put(BackupDBHelper.COLUMN_NTRAF, traf);

		db.insert(BackupDBHelper.logTableName, null, values);
		db.close();
	}

	private class threaddata {
		String address;
		Long oldThreadid;
		Long threadid;
	}

	private class getThreadReturnData {
		Boolean isInThread;
		Long threaid;
	}

	public getThreadReturnData getThreadsId(String paramString) {
		getThreadReturnData localgetThreadReturnData1 = new getThreadReturnData();

		long l1 = 0L;
		try {
			ContentResolver localContentResolver1 = getContentResolver();
			Uri localUri1 = Uri.parse("content://sms/");
			String[] arrayOfString1 = new String[1];
			arrayOfString1[0] = "* from threads --";
			Cursor localCursor = localContentResolver1.query(localUri1,
					arrayOfString1, null, null, "_id AESC");
			if (localCursor == null) {
				localgetThreadReturnData1.isInThread = false;
				localgetThreadReturnData1.threaid = 1L;
				return localgetThreadReturnData1;
			}
			if (localCursor.moveToNext()) {
				String column_id = "_id";
				long value_id = localCursor.getLong(localCursor
						.getColumnIndex(column_id));
				String[] arrayOfString2 = new String[1];
				arrayOfString2[0] = "distinct address";
				String str2 = "thread_id='" + value_id + "'";
				Cursor localCursor4 = localContentResolver1.query(localUri1,
						arrayOfString2, str2, null, null);
				if (localCursor4 == null || !localCursor4.moveToNext()) {

				} else {
					localCursor4.close();
					Uri localUri3 = Uri.parse("content://mms");
					String[] arrayOfString3 = new String[1];
					arrayOfString3[0] = "_id";
					String str3 = "thread_id='" + value_id + "'";
					Cursor localCursor5 = localContentResolver1.query(
							localUri3, arrayOfString3, str3, null, null);
					if (localCursor5.moveToNext()) {
						int k = localCursor5.getColumnIndex("_id");
						long l2 = localCursor5.getLong(k);
						ContentResolver localContentResolver4 = getContentResolver();
						Uri localUri4 = Uri.parse("content://mms/" + l2
								+ "/addr");
						Cursor localCursor6 = localContentResolver4.query(
								localUri4, null, null, null, null);
						while (localCursor6.moveToNext()) {
							String column_address = "address";
							String value_address = localCursor6
									.getString(localCursor6
											.getColumnIndex(column_address));
							if (!(value_address.equals("insert-address-token"))) {
								if (value_address.equals(paramString)) {
									localgetThreadReturnData1.isInThread = true;
									localgetThreadReturnData1.threaid = value_id;
									break;
								}
							}
						}
						localCursor6.close();
					}
					localCursor5.close();
				}
			}

		} catch (Exception e) {

		}
		localgetThreadReturnData1.isInThread = false;
		localgetThreadReturnData1.threaid = 1L;
		return localgetThreadReturnData1;
	}

	private class RestoreThread extends Thread {
		private boolean mPause = false;
		private boolean mCancelRestore = false;

		RestoreThread() {
			mPause = false;
			mCancelRestore = false;
		}

		void cancelRestore() {
			mCancelRestore = true;
		}

		public void run() {
			if (mRestoreContacts && !mCancelRestore) {
				isRestoringContacts = true;
				mHandler.sendEmptyMessage(0);
				int ret = restoreContacts();

				if (ret > 0 && !mCancelRestore) {
					// record success log
					File backupPath = getDir("backup", Context.MODE_PRIVATE);
					String fileName = BackupActivity.CONTACTS_BACK_FILE;
					File dbFile = new File(backupPath, fileName);
					long ntraf = dbFile.length();
					backLog(System.currentTimeMillis(), 1, 4, ret, (int) ntraf);
					mContactsRestoreSucc = true;					
				} else if (!mCancelRestore) {
					// record fail log
					backLog(System.currentTimeMillis(), 0, 4, 0, 0);
					mContactsRestoreSucc = false;
					
				} else {
					backLog(System.currentTimeMillis(), 0, 4, -10, 0);
					mContactsRestoreSucc = false;
				}
				// mCanCancel = true;
				isRestoringContacts = false;
			}
			if (mRestoreSms && !mCancelRestore) {
				isRestoringSms = true;
				mHandler.sendEmptyMessage(4);
				int ret = restoreSms();
				if (ret > 0 && !mCancelRestore) {
					// record success log
					File backupPath = getDir("backup", Context.MODE_PRIVATE);
					String fileName = BackupActivity.SMS_BACK_FILE;
					File dbFile = new File(backupPath, fileName);
					long ntraf = dbFile.length();
					backLog(System.currentTimeMillis(), 1, 5, ret, (int) ntraf);
					mSmsRestoreSucc = true;					
				} else if (!mCancelRestore) {
					// record fail log
					backLog(System.currentTimeMillis(), 0, 5, 0, 0);
					mSmsRestoreSucc = false;					
				} else {
					backLog(System.currentTimeMillis(), 0, 5, -10, 0);
					mSmsRestoreSucc = false;
				}

				isRestoringSms = false;
			}
			if (!mCancelRestore) {
				mHandler.sendEmptyMessage(60);
			} else {
				mHandler.sendEmptyMessage(61);
			}
		}

		private boolean downloadSmsBackupFile() {
			if (!Utilis.checkNetwork(RestoreActivity.this)) {
				return false;
			}
			File backupPath = getDir("backup", Context.MODE_PRIVATE);
			if (!backupPath.exists()) {
				try {
					backupPath.mkdirs();
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			String fileName = BackupActivity.SMS_BACK_FILE;
			File dbFile = new File(backupPath, fileName);
			if (!dbFile.exists()) {
				try {
					dbFile.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			String mToken = Utilis.getToken();
			if (mToken == null) {
				return false;
			}
			while (mPause) {

			}
			if (mCancelRestore) {
				if (dbFile.exists()) {
					dbFile.delete();
				}
				return false;
			}
			boolean ret = true;
			// download remote backup file
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(Constant.REMOTE_RESTORE_URL
					+ URLEncoder.encode(mToken) + "/2");
			BufferedInputStream inputStream = null;
			BufferedOutputStream outputStream = null;
			try {
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				Header[] h = response.getAllHeaders();

				if (entity != null) {
					inputStream = new BufferedInputStream(entity.getContent());
					outputStream = new BufferedOutputStream(
							new FileOutputStream(dbFile));
					long cLen = entity.getContentLength();
					if (cLen <= 0) {
						throw new Exception();
					}
					byte[] buffer = new byte[512];
					int len = inputStream.read(buffer);
					int count = 0;
					while (len != -1 && !mCancelRestore) {
						outputStream.write(buffer, 0, len);

						count = count + len;
						Message msg = new Message();
						msg.what = 5;
						msg.arg1 = Double.valueOf((count * 1.0 / cLen) * 30)
								.intValue();
						mHandler.sendMessage(msg);
						len = inputStream.read(buffer);
					}
					outputStream.flush();
					if (mCancelRestore) {
						return false;
					}

				} else {
					ret = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				ret = false;
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				httpclient.getConnectionManager().shutdown();
			}
			return ret;

		}

		private int restoreSms() {
			double grav = 1.0;
			if (downloadSmsBackupFile()) {
				grav = 0.7;
			} else {

				return -1;
			}
			File backupPath = getDir("backup", Context.MODE_PRIVATE);
			String fileName = BackupActivity.SMS_BACK_FILE;
			File dbFile = new File(backupPath, fileName);
			if (!dbFile.exists()) {
				return -2;
			}
			while (mPause) {

			}
			if (mCancelRestore) {

				return -2;
			}
			SQLiteDatabase localSQLiteDatabase = null;
			try {
				localSQLiteDatabase = SQLiteDatabase.openDatabase(
						dbFile.getAbsolutePath(), null, 1);
			} catch (Exception e) {
				e.printStackTrace();
				localSQLiteDatabase = null;
			}

			if (localSQLiteDatabase == null) {

				return -1;
			}
			Cursor localCursor = null;
			try {
				localCursor = localSQLiteDatabase.query("smstable", null, null,
						null, null, null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (localCursor == null || localCursor.getCount() <= 0) {
				try {
					localCursor.close();
					localSQLiteDatabase.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				return -1;
			}
			while (mPause) {

			}
			if (mCancelRestore) {

				return -2;
			}
			ArrayList<threaddata> arraylist = new ArrayList<threaddata>();
			arraylist.clear();
			int c = 0;
			int s = localCursor.getCount();
			while (mPause) {

			}
			if (mCancelRestore) {
				try {
					localCursor.close();
					localSQLiteDatabase.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return -2;
			}
			if (s <= 0) {
				return -1;
			}
			// mCanCancel = false;
			if (s > 0) {
				Uri localUri = Uri.parse("content://sms");
				getContentResolver().delete(localUri, null, null);
			}
			while (localCursor.moveToNext() && !mCancelRestore) {
				String column_threadId = "thread_id";
				long value_threadId = localCursor.getLong(localCursor
						.getColumnIndex(column_threadId));
				String column_address = "address";
				String value_address = localCursor.getString(localCursor
						.getColumnIndex(column_address));
				String column_person = "person";
				long value_person = localCursor.getLong(localCursor
						.getColumnIndex(column_person));
				String column_date = "date";
				long value_date = localCursor.getLong(localCursor
						.getColumnIndex(column_date));
				String column_protocol = "protocol";
				long value_protocol = localCursor.getLong(localCursor
						.getColumnIndex(column_protocol));
				String column_read = "read";
				long value_read = localCursor.getLong(localCursor
						.getColumnIndex(column_read));
				String column_status = "status";
				long value_status = localCursor.getLong(localCursor
						.getColumnIndex(column_status));
				String column_type = "type";
				long value_type = localCursor.getLong(localCursor
						.getColumnIndex(column_type));
				String column_reply_path_present = "reply_path_present";
				long value_reply_path_present = localCursor.getLong(localCursor
						.getColumnIndex(column_reply_path_present));
				String column_subject = "subject";
				String value_subject = localCursor.getString(localCursor
						.getColumnIndex(column_subject));
				String column_body = "body";
				String value_body = localCursor.getString(localCursor
						.getColumnIndex(column_body));
				String column_service_center = "service_center";
				String value_service_center = localCursor.getString(localCursor
						.getColumnIndex(column_service_center));
				String column_locked = "locked";
				long value_locked = localCursor.getLong(localCursor
						.getColumnIndex(column_locked));
				long l11 = 1L;
				
				ContentValues values = new ContentValues();
				if (value_address != null) {
					if (value_type != 3L) {
						values.put(column_address, value_address);
					}
					values.put(column_person, value_person);
					values.put(column_date, value_date);
					values.put(column_protocol, value_protocol);
					values.put(column_read, value_read);
					values.put(column_status, value_status);
					values.put(column_type, value_type);
					values.put(column_reply_path_present,
							value_reply_path_present);
					values.put(column_subject, value_subject);
					values.put(column_body, value_body);
					values.put(column_service_center, value_service_center);
					values.put(column_locked, value_locked);
					Uri localUri = Uri.parse("content://sms");
					getContentResolver().insert(localUri, values);
					c++;
				} else {
					long new_thread_id = 0L;
					for (int j = 0; j < arraylist.size(); j++) {
						threaddata threaddata4 = (threaddata) arraylist.get(j);
						if (threaddata4 == null || threaddata4.address == null
								|| value_address == null) {
							StringBuilder stringbuilder2 = new StringBuilder();
							StringBuilder stringbuilder3 = stringbuilder2
									.append(threaddata4).append("_");
							String s44 = threaddata4.address;
							String s45 = stringbuilder3.append(s44).append("_")
									.append(value_address).toString();
							int l13 = Log.e("B_U_E", s45);
						}
						long l14 = threaddata4.oldThreadid.longValue();
						if (value_threadId == l14
								|| threaddata4.address.equals(value_address))
							new_thread_id = threaddata4.threadid.longValue();

					}
					getThreadReturnData getthreadreturndata = getThreadsId(value_address);
					if (getthreadreturndata.isInThread.booleanValue()) {
						new_thread_id = getthreadreturndata.threaid.longValue();
					} else {
						if (arraylist.size() > 0) {
							new_thread_id = arraylist.get(arraylist.size() - 1).threadid + 1L;
						} else {
							new_thread_id = getthreadreturndata.threaid
									.longValue();
						}
					}
					threaddata tdate = new threaddata();
					tdate.address = value_address;
					tdate.threadid = new_thread_id;
					tdate.oldThreadid = value_threadId;
					arraylist.add(tdate);
				}
				Message msg = new Message();
				msg.what = 5;
				msg.arg1 = Double.valueOf((c * 1.0 / s) * 70).intValue() + 30;
				mHandler.sendMessage(msg);
			}
			try {
				// updateThreads(arraylist);
				localCursor.close();
				localSQLiteDatabase.close();
				dbFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return c;

		}

		private boolean downloadContactsBackupFile() {
			if (!Utilis.checkNetwork(RestoreActivity.this)) {
				return false;
			}
			File backupPath = getDir("backup", Context.MODE_PRIVATE);
			if (!backupPath.exists()) {
				try {
					backupPath.mkdirs();
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			String fileName = BackupActivity.CONTACTS_BACK_FILE;
			File dbFile = new File(backupPath, fileName);

			if (!dbFile.exists()) {
				try {
					dbFile.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			String mToken = Utilis.getToken();
			if (mToken == null) {
				return false;
			}
			while (mPause) {

			}
			if (mCancelRestore) {
				if (dbFile.exists()) {
					dbFile.delete();
				}
				return false;
			}
			boolean ret = true;
			// download remote backup file
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(Constant.REMOTE_RESTORE_URL
					+ URLEncoder.encode(mToken) + "/1");
			BufferedInputStream inputStream = null;
			BufferedOutputStream outputStream = null;
			try {
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				Header[] h = response.getAllHeaders();

				if (entity != null) {
					inputStream = new BufferedInputStream(entity.getContent());
					outputStream = new BufferedOutputStream(
							new FileOutputStream(dbFile));
					long cLen = entity.getContentLength();
					if (cLen <= 0) {
						throw new Exception();
					}
					byte[] buffer = new byte[512];
					int len = inputStream.read(buffer);
					int count = 0;
					while (len != -1 && !mCancelRestore) {
						outputStream.write(buffer, 0, len);

						count = count + len;
						Message msg = new Message();
						msg.what = 1;
						msg.arg1 = Double.valueOf((count * 1.0 / cLen) * 30)
								.intValue();
						mHandler.sendMessage(msg);
						len = inputStream.read(buffer);
					}
					outputStream.flush();
					if (mCancelRestore) {
						return false;
					}

				} else {
					ret = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				ret = false;
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				httpclient.getConnectionManager().shutdown();
			}

			return ret;
		}

		private int restoreContacts() {
			double grav = 1.0;

			if (downloadContactsBackupFile()) {
				grav = 0.7;
			} else {

				return -1;
			}

			File backupPath = getDir("backup", Context.MODE_PRIVATE);

			String fileName = BackupActivity.CONTACTS_BACK_FILE;
			File dbFile = new File(backupPath, fileName);
			if (!dbFile.exists()) {
				return -2;
			}
			while (mPause) {

			}
			if (mCancelRestore) {

				return -2;
			}
			SQLiteDatabase localSQLiteDatabase = null;
			try {
				localSQLiteDatabase = SQLiteDatabase.openDatabase(
						dbFile.getAbsolutePath(), null, 1);
			} catch (Exception e) {
				e.printStackTrace();
				localSQLiteDatabase = null;
			}
			if (localSQLiteDatabase == null) {
				return -1;
			}

			Cursor cursor = localSQLiteDatabase.query(true, "data",
					new String[] { "contact_id", RawContacts.ACCOUNT_TYPE,
							RawContacts.ACCOUNT_NAME }, null, null, null, null,
					null, null);
			if (cursor == null || cursor.getCount() <= 0) {
				isRestoringContacts = false;
				mHandler.sendEmptyMessage(7);
				try {
					if (cursor != null)
						cursor.close();
					localSQLiteDatabase.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return -1;
			}

			int c = 0;
			int s = cursor.getCount();
			while (mPause) {

			}
			if (mCancelRestore) {
				try {
					if (cursor != null)
						cursor.close();
					localSQLiteDatabase.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return -2;
			}

			try {
				Cursor cc = getContentResolver().query(
						ContactsContract.Contacts.CONTENT_URI, null, null,
						null, null);
				int ccs = cc.getCount();
				int cci = 0;
				while (cc.moveToNext() && !mCancelRestore) {

					Uri uri = ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI,
							cc.getLong(cc.getColumnIndex("_id")));

					getContentResolver().delete(uri, null, null);
					cci++;
					Message msg = Message.obtain();
					msg.what = 1;
					msg.arg1 = Double.valueOf((cci * 1.0 / ccs) * 10)
							.intValue() + 30;
					mHandler.sendMessage(msg);
				}

			} catch (Exception e) {
				e.printStackTrace();

			}

			while (cursor.moveToNext() && !mCancelRestore) {
				int contact_id = cursor.getInt(cursor
						.getColumnIndex("contact_id"));
				
				Cursor localCursor = localSQLiteDatabase.query("data", null,
						"contact_id=?",
						new String[] { String.valueOf(contact_id) }, null,
						null, null);
				ContentValues values = new ContentValues();
				getContentResolver().delete(Data.CONTENT_URI, "contact_id=?",
						new String[] { String.valueOf(contact_id) });
				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.RawContacts.CONTENT_URI)
						.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,
								null)
						.withValue(ContactsContract.RawContacts.ACCOUNT_NAME,
								null)
						.withValue(RawContacts.AGGREGATION_MODE,
								RawContacts.AGGREGATION_MODE_SUSPENDED).build());

				while (localCursor.moveToNext()) {
					String mType = localCursor
							.getString(
									localCursor
											.getColumnIndex(ContactsContract.RawContacts.Data.MIMETYPE))
							.trim();
					Builder builder = ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.RawContacts.Data.RAW_CONTACT_ID,
									0)
							.withValue(
									ContactsContract.RawContacts.Data.MIMETYPE,
									mType);

					if (mType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
						if (localCursor.getString(localCursor
								.getColumnIndex(StructuredName.DISPLAY_NAME)) != null)
							builder.withValue(
									StructuredName.DISPLAY_NAME,
									localCursor.getString(localCursor
											.getColumnIndex(StructuredName.DISPLAY_NAME)));
						// Given name
						String givenName = localCursor.getString(localCursor
								.getColumnIndex(StructuredName.GIVEN_NAME));
						if (givenName != null)
							builder.withValue(StructuredName.GIVEN_NAME,
									givenName);
						String familyName = localCursor.getString(localCursor
								.getColumnIndex(StructuredName.FAMILY_NAME));
						if (familyName != null)
							builder.withValue(StructuredName.FAMILY_NAME,
									familyName);
						String prefix = localCursor.getString(localCursor
								.getColumnIndex(StructuredName.PREFIX));
						if (prefix != null)
							builder.withValue(StructuredName.PREFIX, prefix);
						String middle_name = localCursor.getString(localCursor
								.getColumnIndex(StructuredName.MIDDLE_NAME));
						if (middle_name != null)
							builder.withValue(StructuredName.MIDDLE_NAME,
									middle_name);
						String suffix = localCursor.getString(localCursor
								.getColumnIndex(StructuredName.SUFFIX));
						if (suffix != null)
							builder.withValue(StructuredName.SUFFIX, suffix);
						String ph_given_name = localCursor
								.getString(localCursor
										.getColumnIndex(StructuredName.PHONETIC_GIVEN_NAME));
						if (ph_given_name != null)
							builder.withValue(
									StructuredName.PHONETIC_GIVEN_NAME,
									ph_given_name);
						String ph_middle_name = localCursor
								.getString(localCursor
										.getColumnIndex(StructuredName.PHONETIC_MIDDLE_NAME));
						if (ph_middle_name != null)
							builder.withValue(
									StructuredName.PHONETIC_MIDDLE_NAME,
									ph_middle_name);
						String ph_family_name = localCursor
								.getString(localCursor
										.getColumnIndex(StructuredName.PHONETIC_FAMILY_NAME));
						if (ph_family_name != null)
							builder.withValue(
									StructuredName.PHONETIC_FAMILY_NAME,
									ph_family_name);

					} else if (mType.equals(Phone.CONTENT_ITEM_TYPE)) {
						int type = localCursor.getInt(localCursor
								.getColumnIndex(Phone.TYPE));
						builder.withValue(Phone.TYPE, type);
						String number = localCursor.getString(localCursor
								.getColumnIndex(Phone.NUMBER));
						if (number != null)
							builder.withValue(Phone.NUMBER, number);
						String label = localCursor.getString(localCursor
								.getColumnIndex(Phone.LABEL));
						if (label != null)
							builder.withValue(Phone.LABEL, label);

					} else if (mType.equals(Email.CONTENT_ITEM_TYPE)) {
						String address = localCursor.getString(localCursor
								.getColumnIndex(Email.DATA));
						if (address != null)
							builder.withValue(Email.DATA, address);
						int type = localCursor.getInt(localCursor
								.getColumnIndex(Email.TYPE));
						builder.withValue(Email.TYPE, type);
						String label = localCursor.getString(localCursor
								.getColumnIndex(Email.LABEL));
						if (label != null)
							builder.withValue(Email.LABEL, label);
						String display_name = localCursor.getString(localCursor
								.getColumnIndex(Email.DISPLAY_NAME));
						if (display_name != null)
							builder.withValue(Email.DISPLAY_NAME, display_name);

					} else if (mType.equals(Organization.CONTENT_ITEM_TYPE)) {
						String company = localCursor.getString(localCursor
								.getColumnIndex(Organization.COMPANY));
						if (company != null)
							builder.withValue(Organization.COMPANY, company);
						int type = localCursor.getInt(localCursor
								.getColumnIndex(Organization.TYPE));
						builder.withValue(Organization.TYPE, type);
						String label = localCursor.getString(localCursor
								.getColumnIndex(Organization.LABEL));
						if (label != null)
							builder.withValue(Organization.LABEL, label);
						String title = localCursor.getString(localCursor
								.getColumnIndex(Organization.TITLE));
						if (title != null)
							builder.withValue(Organization.TITLE, title);
						String department = localCursor.getString(localCursor
								.getColumnIndex(Organization.DEPARTMENT));
						if (department != null)
							builder.withValue(Organization.DEPARTMENT,
									department);
						String job_desc = localCursor.getString(localCursor
								.getColumnIndex(Organization.JOB_DESCRIPTION));
						if (job_desc != null)
							builder.withValue(Organization.JOB_DESCRIPTION,
									job_desc);
						String symbol = localCursor.getString(localCursor
								.getColumnIndex(Organization.SYMBOL));
						if (symbol != null)
							builder.withValue(Organization.SYMBOL, symbol);
						String ph_name = localCursor.getString(localCursor
								.getColumnIndex(Organization.PHONETIC_NAME));
						if (ph_name != null)
							builder.withValue(Organization.PHONETIC_NAME,
									ph_name);
						String office_location = localCursor
								.getString(localCursor
										.getColumnIndex(Organization.OFFICE_LOCATION));
						if (office_location != null)
							builder.withValue(Organization.OFFICE_LOCATION,
									office_location);

					} else if (mType.equals(Im.CONTENT_ITEM_TYPE)) {
						String data = localCursor.getString(localCursor
								.getColumnIndex(Im.DATA));
						if (data != null)
							builder.withValue(Im.DATA, data);
						int type = localCursor.getInt(localCursor
								.getColumnIndex(Im.TYPE));
						builder.withValue(Im.TYPE, type);
						String label = localCursor.getString(localCursor
								.getColumnIndex(Im.LABEL));
						if (label != null)
							builder.withValue(Im.LABEL, label);
						String protocol = localCursor.getString(localCursor
								.getColumnIndex(Im.PROTOCOL));
						if (protocol != null)
							builder.withValue(Im.PROTOCOL, protocol);
						String custom_protocol = localCursor
								.getString(localCursor
										.getColumnIndex(Im.CUSTOM_PROTOCOL));
						if (custom_protocol != null)
							builder.withValue(Im.CUSTOM_PROTOCOL,
									custom_protocol);

					} else if (mType.equals(Nickname.CONTENT_ITEM_TYPE)) {
						String name = localCursor.getString(localCursor
								.getColumnIndex(Nickname.NAME));
						if (name != null)
							builder.withValue(Nickname.NAME, name);
						int type = localCursor.getInt(localCursor
								.getColumnIndex(Nickname.TYPE));
						builder.withValue(Nickname.TYPE, type);
						String label = localCursor.getString(localCursor
								.getColumnIndex(Nickname.LABEL));
						if (label != null)
							builder.withValue(Nickname.LABEL, label);

					} else if (mType.equals(Note.CONTENT_ITEM_TYPE)) {
						String note = localCursor.getString(localCursor
								.getColumnIndex(Note.NOTE));
						if (note != null)
							builder.withValue(Note.NOTE, note);

					} else if (mType.equals(StructuredPostal.CONTENT_ITEM_TYPE)) {
						String form_address = localCursor
								.getString(localCursor
										.getColumnIndex(StructuredPostal.FORMATTED_ADDRESS));
						if (form_address != null)
							builder.withValue(
									StructuredPostal.FORMATTED_ADDRESS,
									form_address);
						int type = localCursor.getInt(localCursor
								.getColumnIndex(StructuredPostal.TYPE));
						builder.withValue(StructuredPostal.TYPE, type);
						String label = localCursor.getString(localCursor
								.getColumnIndex(StructuredPostal.LABEL));
						if (label != null)
							builder.withValue(StructuredPostal.LABEL, label);
						String street = localCursor.getString(localCursor
								.getColumnIndex(StructuredPostal.STREET));
						if (street != null)
							builder.withValue(StructuredPostal.STREET, street);
						String pobox = localCursor.getString(localCursor
								.getColumnIndex(StructuredPostal.POBOX));
						if (pobox != null)
							builder.withValue(StructuredPostal.POBOX, pobox);
						String neibo = localCursor.getString(localCursor
								.getColumnIndex(StructuredPostal.NEIGHBORHOOD));
						if (neibo != null)
							builder.withValue(StructuredPostal.NEIGHBORHOOD,
									neibo);
						String city = localCursor.getString(localCursor
								.getColumnIndex(StructuredPostal.CITY));
						if (city != null)
							builder.withValue(StructuredPostal.CITY, city);
						String region = localCursor.getString(localCursor
								.getColumnIndex(StructuredPostal.REGION));
						if (region != null)
							builder.withValue(StructuredPostal.REGION, region);
						String postcode = localCursor.getString(localCursor
								.getColumnIndex(StructuredPostal.POSTCODE));
						if (postcode != null)
							builder.withValue(StructuredPostal.POSTCODE,
									postcode);
						String country = localCursor.getString(localCursor
								.getColumnIndex(StructuredPostal.COUNTRY));
						if (country != null)
							builder.withValue(StructuredPostal.COUNTRY, country);

					} else if (mType.equals(Website.CONTENT_ITEM_TYPE)) {
						String url = localCursor.getString(localCursor
								.getColumnIndex(Website.URL));
						if (url != null)
							builder.withValue(Website.URL, url);
						int type = localCursor.getInt(localCursor
								.getColumnIndex(Website.TYPE));
						builder.withValue(Website.TYPE, type);
						String label = localCursor.getString(localCursor
								.getColumnIndex(Website.LABEL));
						if (label != null)
							builder.withValue(Website.LABEL, label);

					} else if (mType.equals(Event.CONTENT_ITEM_TYPE)) {
						String start_date = localCursor.getString(localCursor
								.getColumnIndex(Event.START_DATE));
						if (start_date != null)
							builder.withValue(Event.START_DATE, start_date);
						int type = localCursor.getInt(localCursor
								.getColumnIndex(Event.TYPE));
						builder.withValue(Event.TYPE, type);
						String label = localCursor.getString(localCursor
								.getColumnIndex(Event.LABEL));
						if (label != null)
							builder.withValue(Event.LABEL, label);

					} else if (mType.equals(Relation.CONTENT_ITEM_TYPE)) {
						String name = localCursor.getString(localCursor
								.getColumnIndex(Relation.NAME));
						if (name != null)
							builder.withValue(Relation.NAME, name);
						int type = localCursor.getInt(localCursor
								.getColumnIndex(Relation.TYPE));
						builder.withValue(Relation.TYPE, type);
						String label = localCursor.getString(localCursor
								.getColumnIndex(Relation.LABEL));
						if (label != null)
							builder.withValue(Relation.LABEL, label);

					} else if (mType.equals(GroupMembership.CONTENT_ITEM_TYPE)) {

						long group_row_id = localCursor.getLong(localCursor
								.getColumnIndex(GroupMembership.GROUP_ROW_ID));
						builder.withValue(GroupMembership.GROUP_ROW_ID,
								group_row_id);

					}

					ops.add(builder.build());
				}
				try {
					ContentProviderResult[] result = getContentResolver()
							.applyBatch(ContactsContract.AUTHORITY, ops);
					c++;
				} catch (Exception e) {
					e.printStackTrace();
				}

				localCursor.close();

				Message msg = new Message();
				msg.what = 1;
				msg.arg1 = Double.valueOf((c * 1.0 / s) * 60 * grav).intValue() + 40;
				mHandler.sendMessage(msg);
			}

			try {
				cursor.close();
				localSQLiteDatabase.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Uri localUri = ContactsContract.Contacts.CONTENT_URI;
			try {
				Cursor localCursor = getContentResolver().query(localUri, null,
						null, null, null);
				if (localCursor != null) {
					c = localCursor.getCount();
				}
				localCursor.close();
				dbFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return c;

		}

	}

	private void getCancelPromptDialog() {
		final Dialog promptDialog = new Dialog(this, R.style.softDialog);
		View view = LayoutInflater.from(this).inflate(
				R.layout.soft_uninstall_prompt_dialog, null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);

		promptText.setText(R.string.cancel_recover_text);

		Button button_ok = (Button) promptDialog.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				promptDialog.dismiss();
			}
		});
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mRestoreThread.cancelRestore();
				promptDialog.dismiss();
			}
		});
		promptDialog.show();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (isRestoringContacts || isRestoringSms) {
			getCancelPromptDialog();
		} else {
			super.onBackPressed();
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mContactsRestore = restore_contacts_cb.isChecked();
		mSmsRestore = restore_sms_cb.isChecked();
		refreshExpData();

	}

	private void refreshExpData() {
		Uri localUri = ContactsContract.Contacts.CONTENT_URI;
		try {
			Cursor localCursor = getContentResolver().query(localUri, null,
					null, null, null);
			if (localCursor != null)
				contactsExist = localCursor.getCount();
			localCursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			localUri = Uri.parse("content://sms");
			Cursor localCursor = getContentResolver().query(localUri, null,
					null, null, null);
			if (localCursor != null)
				smsExist = localCursor.getCount();
			localCursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		TextView backup_contacts_exist_text = (TextView) findViewById(R.id.backup_contacts_exist_text);
		TextView backup_sms_exist_text = (TextView) findViewById(R.id.backup_sms_exist_text);
		backup_contacts_exist_text.setText(String.format(
				getString(R.string.backup_contacts_exist_prompt_text),
				contactsExist));
		backup_sms_exist_text.setText(String.format(
				getString(R.string.backup_sms_exist_prompt_text), smsExist));

	}

}
