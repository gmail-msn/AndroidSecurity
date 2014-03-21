/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.09
 * Description:
 */

package com.kindroid.security.ui;

import java.util.Locale;

import org.json.JSONObject;

import com.kindroid.security.R;

import com.kindroid.security.adapter.InterceptTreatModeListAdapter;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.HttpRequest;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.UtilDailog;
import com.kindroid.security.util.UtilShareprefece;
import com.kindroid.security.util.Utilis;

import android.app.Activity;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.Toast;

import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class InterceptSetting extends Activity implements View.OnClickListener {
	private View mInterceptModeLinear;
	private View mTreatModeLinear;
	private View mDefineKeywordsLinear;
	private View mNotifyInterceptLinear;
	private View mNoDisturbLinear;
	private TextView mCurrentTreatMode;
	private TextView mCurrentInterceptMode;
	private TextView mCurrentNodisturbMode;
	private CheckBox mNotifyInterceptCb;
	private String[] mInterceptModeNames;
	private String[] mInterceptTreatModes;
	private Dialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intercept_setting);
		mInterceptTreatModes = getResources().getStringArray(
				R.array.intercept_tream_mode);
		mInterceptModeNames = getResources().getStringArray(
				R.array.intercept_mode_name);
		findView();
		bindListenerToView();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// refresh UI
		SharedPreferences sp = KindroidSecurityApplication.sh;
		int mDefaultInterceptMode = sp.getInt(
				Constant.SHAREDPREFERENCES_BLOCKINGRULES, 1);
		mCurrentInterceptMode
				.setText(mInterceptModeNames[mDefaultInterceptMode]);
		int mDefaultTreatMode = sp.getInt(Constant.INTERCEPT_TREAT_MODE, 1);
		mCurrentTreatMode.setText(mInterceptTreatModes[mDefaultTreatMode - 1]);
		int mNodisturbMode = sp.getInt(
				Constant.SHAREDPREFERENCES_NIGHTBLOCKINGRULES, 0);
		mCurrentNodisturbMode.setText(mInterceptModeNames[mNodisturbMode]);
		int mDefaultNotifyIntercept = sp.getInt(Constant.INTERCEPT_NOTIFY_INFO,
				1);
		if (mDefaultNotifyIntercept == 0) {
			mNotifyInterceptCb.setChecked(false);
		} else {
			mNotifyInterceptCb.setChecked(true);
		}

	}

	private void findView() {
		mInterceptModeLinear = findViewById(R.id.intercept_mode_linear);
		mTreatModeLinear = findViewById(R.id.treat_mode_linear);
		mDefineKeywordsLinear = findViewById(R.id.key_word_linear);
		mNotifyInterceptLinear = findViewById(R.id.notify_intercept_info_linear);
		mNoDisturbLinear = findViewById(R.id.nodisturb_linear);

		mCurrentTreatMode = (TextView) findViewById(R.id.current_treat_mode);
		mCurrentInterceptMode = (TextView) findViewById(R.id.current_intercept_mode);
		mCurrentNodisturbMode = (TextView) findViewById(R.id.current_nodisturb_mode);
		mNotifyInterceptCb = (CheckBox) findViewById(R.id.notify_intercept_cb);

	}

	private void bindListenerToView() {
		mInterceptModeLinear.setOnClickListener(this);
		mTreatModeLinear.setOnClickListener(this);
		mNotifyInterceptLinear.setOnClickListener(this);
		mNoDisturbLinear.setOnClickListener(this);
		mDefineKeywordsLinear.setOnClickListener(this);
	}

	private int saveTreatMode(Dialog dialog) {
		SharedPreferences sp = KindroidSecurityApplication.sh;
		Editor editor = sp.edit();
		ListView mListView = (ListView) dialog
				.findViewById(R.id.treat_mode_list);
		InterceptTreatModeListAdapter mListAdapter = (InterceptTreatModeListAdapter) mListView
				.getAdapter();
		int mSelectedMode = mListAdapter.getSelected();
		editor.putInt(Constant.INTERCEPT_TREAT_MODE, mSelectedMode + 1);
		editor.commit();
		mCurrentTreatMode.setText(mListAdapter
				.getSelectedModeName(mSelectedMode));
		return mSelectedMode + 1;
	}

	private void loadTreatModeListAdapter(Dialog dialog, int defaultMode) {
		final InterceptTreatModeListAdapter mListAdapter = new InterceptTreatModeListAdapter(
				this);
		mListAdapter.setPosition(defaultMode);
		ListView mListView = (ListView) dialog
				.findViewById(R.id.treat_mode_list);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				mListAdapter.setPosition(position);
				mListAdapter.notifyDataSetChanged();
			}

		});
	}

	private Uri getPhoneUri(int type) {
		Uri uri = null;
		if (type == 1 || type == 2) {
			uri = Uri.parse("tel:" + "%23%2367%23");
		} else if (type == 3) {
			uri = Uri.parse("tel:" + "**67*13810538911%23");
		} else if (type == 4) {
			uri = Uri.parse("tel:" + "**67*13701110216%23");
		} else if (type == 5) {
			uri = Uri.parse("tel:" + "**67*13800000000%23");
		}
		return uri;

	}
	private Uri getCDMAUri(int type){
		Uri uri = null;
		if (type == 1 || type == 2) {
			uri = Uri.parse("tel:" + "*900");
		} else if (type == 3) {
			uri = Uri.parse("tel:" + "*90*13810538911%23");
		} else if (type == 4) {
			uri = Uri.parse("tel:" + "*90*13701110216%23");
		} else if (type == 5) {
			uri = Uri.parse("tel:" + "*90*13800000000%23");
		}
		return uri;
	}
	private String getCDMAString(int type){
		String ret = null;
		if (type == 1 || type == 2) {
			ret = "tel:" + "*900";
		} else if (type == 3) {
			ret = "tel:" + "*90*13810538911%23";
		} else if (type == 4) {
			ret = "tel:" + "*90*13701110216%23";
		} else if (type == 5) {
			ret = "tel:" + "*90*13800000000%23";
		}
		return ret;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == 100){
			saveTreatMode(dialog);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.intercept_mode_linear:
			Intent intent = new Intent(this,
					InterceptModeSettingListActivity.class);
			startActivity(intent);
			break;
		case R.id.treat_mode_linear:
			SharedPreferences sp = KindroidSecurityApplication.sh;
			final int mDefaultTreatMode = sp.getInt(
					Constant.INTERCEPT_TREAT_MODE, 1);
			dialog = UtilDailog.getInterceptTreadDialog(this);
			loadTreatModeListAdapter(dialog, mDefaultTreatMode - 1);
			Button mBtOk = (Button) dialog.findViewById(R.id.button_ok);
			mBtOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					if (tm.getSimState() != TelephonyManager.SIM_STATE_READY) {
						Toast.makeText(InterceptSetting.this,
								R.string.insert_sim_to_update_status,
								Toast.LENGTH_SHORT).show();
						return;

					}
					// int tSelectNum = saveTreatMode(dialog);
					ListView mListView = (ListView) dialog
							.findViewById(R.id.treat_mode_list);
					InterceptTreatModeListAdapter mListAdapter = (InterceptTreatModeListAdapter) mListView
							.getAdapter();
					int tSelectNum = mListAdapter.getSelected();
					dialog.dismiss();
					if ((mDefaultTreatMode-1) != tSelectNum) {
						Intent localIntent = new Intent();
						
						Uri uri = null;
						if(tm.getPhoneType() == tm.PHONE_TYPE_CDMA){							
							uri = getCDMAUri(tSelectNum + 1);							
						}else{
							uri = getPhoneUri(tSelectNum + 1);
						}						
						if (uri != null) {
							localIntent.setAction("android.intent.action.CALL");
							localIntent.setData(uri);
							startActivityForResult(localIntent, 100);
							
						}else{
							saveTreatMode(dialog);
						}
					}
				}
			});
			dialog.show();
			break;
		case R.id.key_word_linear:
			intent = new Intent(this, KeywordSettingActivity.class);
			startActivity(intent);
			break;
		case R.id.notify_intercept_info_linear:
			CheckBox notify_intercept_cb = (CheckBox) findViewById(R.id.notify_intercept_cb);
			boolean mNotifyStatus = notify_intercept_cb.isChecked();
			notify_intercept_cb.setChecked(!mNotifyStatus);
			sp = KindroidSecurityApplication.sh;
			Editor editor = sp.edit();
			if (notify_intercept_cb.isChecked()) {
				editor.putInt(Constant.INTERCEPT_NOTIFY_INFO, 1);
			} else {
				editor.putInt(Constant.INTERCEPT_NOTIFY_INFO, 0);
			}
			editor.commit();
			break;
		case R.id.nodisturb_linear:
			intent = new Intent(this, InterceptTimeSettingActivity.class);
			startActivity(intent);
			break;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(InterceptSetting.this,
					DefenderTabMain.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

			startActivity(intent);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}