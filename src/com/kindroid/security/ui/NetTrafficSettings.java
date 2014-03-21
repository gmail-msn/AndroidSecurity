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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.model.Interface;
import com.kindroid.security.model.NetTrafficModel;
import com.kindroid.security.service.AppTrafficService;
import com.kindroid.security.service.NetTrafficService;
import com.kindroid.security.util.AppNetWorkDataBase;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.UtilShareprefece;

public class NetTrafficSettings extends Activity implements OnClickListener {

	private static final DateFormat DF_DATE = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	public static final String NET_TRAFFIC_UPDATE_SETTINGS = "net_traffic_update_settings";

	KindroidSecurityApplication application;
	SharedPreferences sh;
	private NetTrafficModel mModel = null;

	private TextView mCellularSettingsLimit;
	private TextView mCellularCalDay;
	private TextView mAdjustCellularTrafficUsed;

	private LinearLayout mClearLayout;
	private TextView mLastClearDate;
	private TextView enable_net_traffic_tv;

	private LinearLayout mModifyCellularLimitLayout;
	private LinearLayout mCalDayLayout;
	private LinearLayout mLeftMonthLayout;
	private LinearLayout traffic_monter_linear;
	private LinearLayout popView_linear;
	private TextView traffic_enable_cb;
	private TextView popview_cb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.net_traffic_settings);
		application = (KindroidSecurityApplication) getApplication();
		sh = application.getAdapter(SharedPreferences.class);
		findViews();
		fillData();
		boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		boolean appTrue = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLEAPPTRAFFICMOITER, true);
		traffic_enable_cb
				.setBackgroundResource(isTrue ? R.drawable.checkbox_true
						: R.drawable.checkbox_false);
		popview_cb.setBackgroundResource(appTrue ? R.drawable.checkbox_true
				: R.drawable.checkbox_false);
	}

	private void findViews() {
		mCellularSettingsLimit = (TextView) findViewById(R.id.cellularSettingsLimit);
		mCellularCalDay = (TextView) findViewById(R.id.cellularCalDay);
		mAdjustCellularTrafficUsed = (TextView) findViewById(R.id.adjustCellularTrafficUsed);
		mClearLayout = (LinearLayout) findViewById(R.id.clearDataLayout);
		mClearLayout.setClickable(true);
		mClearLayout.setOnClickListener(this);

		mModifyCellularLimitLayout = (LinearLayout) findViewById(R.id.modifyCellularLimit);
		mModifyCellularLimitLayout.setClickable(true);
		mModifyCellularLimitLayout.setOnClickListener(this);

		mCalDayLayout = (LinearLayout) findViewById(R.id.calDayLayout);
		mCalDayLayout.setClickable(true);
		mCalDayLayout.setOnClickListener(this);

		mLeftMonthLayout = (LinearLayout) findViewById(R.id.leftMonthLayout);
		mLeftMonthLayout.setClickable(true);
		mLeftMonthLayout.setOnClickListener(this);

		mLastClearDate = (TextView) findViewById(R.id.lastClearDate);
		traffic_monter_linear = (LinearLayout) findViewById(R.id.traffic_monter_linear);
		popView_linear = (LinearLayout) findViewById(R.id.popView_linear);
		traffic_monter_linear.setOnClickListener(this);
		popView_linear.setOnClickListener(this);
		

		mModel = application.getAdapter(NetTrafficModel.class);
		enable_net_traffic_tv = (TextView) findViewById(R.id.enable_net_traffic_tv);
		traffic_enable_cb = (TextView) findViewById(R.id.traffic_enable_cb);
		popview_cb = (TextView) findViewById(R.id.popview_cb);

	}

	private void fillData() {
		mCellularSettingsLimit.setText(sh.getString(
				UtilShareprefece.LIMIT_PER_MON_INT, "30"));
		mCellularCalDay.setText(sh
				.getString(UtilShareprefece.CAL_DATE_INT, "1"));
		mAdjustCellularTrafficUsed.setText(sh.getString(
				UtilShareprefece.LEFT_PER_MON_INT, "0"));
		List<Interface> list = mModel.getInterfaces();

		if (list.size() > 0 && !sh.getString("clearlastdate", "").equals("")) {
			mLastClearDate.setText(getString(
					R.string.net_traffice_settings_clear_data_date,
					sh.getString("clearlastdate", "")));
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.clearDataLayout) {
			final Dialog promptDialog = new Dialog(this, R.style.softDialog);
			View view = LayoutInflater.from(this).inflate(
					R.layout.soft_uninstall_prompt_dialog, null);
			promptDialog.setContentView(view);
			TextView promptText = (TextView) promptDialog
					.findViewById(R.id.prompt_text);
			promptText
					.setText(R.string.net_traffice_settings_clear_data_confirm);
			Button button_ok = (Button) promptDialog
					.findViewById(R.id.button_ok);
			Button button_cancel = (Button) promptDialog
					.findViewById(R.id.button_cancel);
			button_ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					clearData();
					UtilShareprefece.getShareprefece().storeMessage(sh,
							UtilShareprefece.LEFT_PER_MON_INT, "0");
					fillData();
					promptDialog.dismiss();
				}
			});
			button_cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					promptDialog.dismiss();
				}
			});
			promptDialog.show();
		} else if (v.getId() == R.id.modifyCellularLimit) {
			Intent intent = new Intent(this, NetTrafficSettingsDialog.class);
			intent.putExtra(NetTrafficSettingsDialog.INPUT_TYPE,
					NetTrafficSettingsDialog.INPUT_MONTH_LIMIT);
			intent.putExtra(NetTrafficSettingsDialog.INPUT_VALUE,
					mCellularSettingsLimit.getText().toString());
			startActivityForResult(intent,
					NetTrafficSettingsDialog.UPDATE_CELLULAR_LIMIT);
		} else if (v.getId() == R.id.calDayLayout) {
			Intent intent = new Intent(this, NetTrafficSettingsDialog.class);
			intent.putExtra(NetTrafficSettingsDialog.INPUT_TYPE,
					NetTrafficSettingsDialog.INPUT_CAL_DAY);
			intent.putExtra(NetTrafficSettingsDialog.INPUT_VALUE,
					mCellularCalDay.getText().toString());
			startActivityForResult(intent,
					NetTrafficSettingsDialog.UPDATE_CELLULAR_LIMIT);
		} else if (v.getId() == R.id.leftMonthLayout) {
			Intent intent = new Intent(this, NetTrafficSettingsDialog.class);
			intent.putExtra(NetTrafficSettingsDialog.INPUT_TYPE,
					NetTrafficSettingsDialog.INPUT_LEFT_LIMIT);
			intent.putExtra(NetTrafficSettingsDialog.INPUT_VALUE,
					mAdjustCellularTrafficUsed.getText().toString());
			startActivityForResult(intent,
					NetTrafficSettingsDialog.UPDATE_CELLULAR_LIMIT);
		} else if (v.getId() == R.id.traffic_monter_linear) {
			boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
					Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
			Editor et = KindroidSecurityApplication.sh.edit();
			if (isTrue) {
				Intent intent = new Intent();
				intent.setClass(this, NetTrafficService.class);
				traffic_enable_cb
						.setBackgroundResource(R.drawable.checkbox_false);
				enable_net_traffic_tv
						.setText(R.string.net_traffice_settings_disable);
				stopService(intent);
				intent.setClass(this, AppTrafficService.class);
				stopService(intent);
				et.putBoolean(
						Constant.SHAREDPREFERENCES_ENABLEAPPTRAFFICMOITER,
						false);
				popview_cb.setBackgroundResource(R.drawable.checkbox_false);
			} else {
				traffic_enable_cb
						.setBackgroundResource(R.drawable.checkbox_true);
				enable_net_traffic_tv
						.setText(R.string.net_traffice_settings_enable);
				Intent intent = new Intent();
				intent.setClass(this, NetTrafficService.class);
				KindroidSecurityApplication.setUpdatePolicy(KindroidSecurityApplication.SERVICE_MID);
				startService(intent);
			}

			et.putBoolean(Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER,
					!isTrue);
			et.commit();
		} else if (v.getId() == R.id.popView_linear) {
			boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
					Constant.SHAREDPREFERENCES_ENABLEAPPTRAFFICMOITER, true);
			Editor et = KindroidSecurityApplication.sh.edit();
			if (isTrue) {
				popview_cb.setBackgroundResource(R.drawable.checkbox_false);
				Intent intent = new Intent();
				intent.setClass(this, AppTrafficService.class);
				stopService(intent);
				/*
				boolean trafficMonitor = KindroidSecurityApplication.sh.getBoolean(
						Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);				
				if(trafficMonitor){
					KindroidSecurityApplication.setUpdatePolicy(KindroidSecurityApplication.SERVICE_MID);
					intent.setClass(this, NetTrafficService.class);
					stopService(intent);
					startService(intent);
				}
				*/
			} else {
				popview_cb.setBackgroundResource(R.drawable.checkbox_true);
				enable_net_traffic_tv
						.setText(R.string.net_traffice_settings_enable);
				traffic_enable_cb
						.setBackgroundResource(R.drawable.checkbox_true);
				et.putBoolean(Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER,
						true);
				Intent intent = new Intent();
				KindroidSecurityApplication.setUpdatePolicy(KindroidSecurityApplication.SERVICE_HIGH);
				intent.setClass(this, NetTrafficService.class);
				startService(intent);
				NetworkInfo localNetworkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
						.getActiveNetworkInfo();
				if (localNetworkInfo != null && localNetworkInfo.getType() == 0) {
					intent.setClass(this, AppTrafficService.class);
					startService(intent);
				}

			}

			et.putBoolean(Constant.SHAREDPREFERENCES_ENABLEAPPTRAFFICMOITER,
					!isTrue);
			et.commit();

		}
	}

	private void clearData() {
		final boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		List<Interface> list = mModel.getInterfaces();
		for (int i = 0; i < list.size(); i++) {
			Interface terface = list.get(i);
			if (terface != null)
				terface.reset();
		}

		application.startService();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
		
				if(!isTrue){
					
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					stopService(new Intent(NetTrafficSettings.this,NetTrafficService.class));
					
				}
				sendBroadcast(new Intent(NET_TRAFFIC_UPDATE_SETTINGS));
			}
		}).start();
		
		

		sh.edit().putString("clearlastdate", DF_DATE.format(new Date()) + "")
				.commit();
		AppNetWorkDataBase.get(this).delAll();
		sh.edit().remove(Constant.SHAREDPREFERENCES_APPFIRST_INSTALL).commit();
		sendBroadcast(new Intent("ehoo.com.update.ui.traffic_app"));

		fillData();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == NetTrafficSettingsDialog.UPDATE_CELLULAR_LIMIT) {
			fillData();
			sendBroadcast(new Intent(NET_TRAFFIC_UPDATE_SETTINGS));
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(NetTrafficSettings.this,
					DefenderTabMain.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

			startActivity(intent);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		enable_net_traffic_tv
				.setText(isTrue ? R.string.net_traffice_settings_enable
						: R.string.net_traffice_settings_disable);

	}

}
