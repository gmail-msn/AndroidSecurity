/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.model.IModel;
import com.kindroid.security.model.IModelListener;
import com.kindroid.security.model.IOperation;
import com.kindroid.security.model.Interface;
import com.kindroid.security.model.NetTrafficModel;
import com.kindroid.security.model.TrafficCounter;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.MemoryUtil;
import com.kindroid.security.util.UtilShareprefece;

public class NetTrafficSummary extends Activity implements IModelListener,
		IOperation {

	private KindroidSecurityApplication mApp;
	private NetTrafficModel mModel = null;
	/**
	 * cellular data fill view
	 */
	private TextView mTodayCellularTraffic;

	private TextView mTodayCellularTrafficUsed;
	private TextView mTodayCellularTrafficRemainder;
	private TextView mTodayCellularTrafficRemainderPercent;
	private TextView mMonthCellularLimit;

	private TextView enable_net_traffic_tv;

	private ProgressBar mCellularProgress;

	/**
	 * WiFi data fill view
	 */
	private TextView mTodayWiFiTraffic;
	private TextView mMonthWifiTrafficUsed;
	private TextView mAllWifiTrafficUsed;
	private TextView mBillDay;
	private TextView net_traffic_month_remainder_des_tv;

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (NetTrafficSettings.NET_TRAFFIC_UPDATE_SETTINGS.equals(action)) {
				fillData();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.net_traffic_summary);
		findViews();
		fillData();
		registerReceiver(broadcastReceiver, new IntentFilter(
				NetTrafficSettings.NET_TRAFFIC_UPDATE_SETTINGS));
	}

	private void findViews() {
		/**
		 * cellular data fill view
		 */
		mTodayCellularTraffic = (TextView) findViewById(R.id.todayCellularTraffic);
		mTodayCellularTrafficUsed = (TextView) findViewById(R.id.todayCellularTrafficUsed);
		mTodayCellularTrafficRemainder = (TextView) findViewById(R.id.todayCellularTrafficRemainder);
		mTodayCellularTrafficRemainderPercent = (TextView) findViewById(R.id.todayCellularTrafficRemainderPercent);
		mMonthCellularLimit = (TextView) findViewById(R.id.monthCellularLimit);
		mCellularProgress = (ProgressBar) findViewById(R.id.todayCellularProgress);
		net_traffic_month_remainder_des_tv = (TextView) findViewById(R.id.net_traffic_month_remainder_des_tv);

		/**
		 * WiFi data fill view
		 */
		mTodayWiFiTraffic = (TextView) findViewById(R.id.todayWiFiTraffic);
		mMonthWifiTrafficUsed = (TextView) findViewById(R.id.monthWifiTrafficUsed);
		mAllWifiTrafficUsed = (TextView) findViewById(R.id.allWifiTrafficUsed);
		mBillDay = (TextView) findViewById(R.id.bill_day);

		enable_net_traffic_tv = (TextView) findViewById(R.id.enable_net_traffic_tv);

		mApp = (KindroidSecurityApplication) getApplication();
		mModel = mApp.getAdapter(NetTrafficModel.class);
	}

	private void fillData() {
		

		List<Interface> list = mModel.getInterfaces();

		String monthLimit = mApp.getAdapter(SharedPreferences.class).getString(
				UtilShareprefece.LIMIT_PER_MON_INT, "30");
		String monthLeft = mApp.getAdapter(SharedPreferences.class).getString(
				UtilShareprefece.LEFT_PER_MON_INT, "0");
		long limit_per_month = Long.parseLong(monthLimit);
		long left_per_month = (long) (Double.parseDouble(monthLeft) * 1024 * 1024);
		long userTotal = limit_per_month * 1024 * 1024;
		boolean hasCell = false;
		boolean hasWifi = false;
		
		long todayByteUi = 0;
		long totalMonthGprsUi = 0;
		// 3g/2g
		for (int i = 0; i < list.size(); i++) {
			Interface trafficCounter = list.get(i);
			
			Log.d("Test", trafficCounter.getName() + ";" + trafficCounter.getPrettyName());
			if (getString(R.string.interfaceTypeCell).equals(trafficCounter.getPrettyName()) && trafficCounter.getCounters().size() > 3) {
				TrafficCounter todayCellular = trafficCounter.getCounters()
						.get(3);

				long[] todayBytes = todayCellular.getBytes();
				todayByteUi += todayBytes[0] + todayBytes[1];
				
				// this month
				TrafficCounter monthCellular = trafficCounter.getCounters().get(1);
				long[] totalMonthGprsArray = monthCellular.getBytes();
				totalMonthGprsUi += totalMonthGprsArray[0] + totalMonthGprsArray[1];
				
				hasCell = true;
				//break;
			}
		}
		
		totalMonthGprsUi += left_per_month;
		
		if (hasCell){
			mTodayCellularTraffic.setText(MemoryUtil.formatMemorySize(1,
					todayByteUi) + "");

			
			mTodayCellularTrafficUsed.setText(MemoryUtil.formatMemorySize(
					2, totalMonthGprsUi) + "");

			long monthRemainder = userTotal - totalMonthGprsUi;
			if (monthRemainder >= 0) {
				mTodayCellularTrafficRemainder.setText(MemoryUtil
						.formatMemorySize(2, monthRemainder));
			} else {
				mTodayCellularTrafficRemainder.setText("0");
			}

			double remainderPercent = (double) monthRemainder / userTotal;
			double usedPercent = (double) totalMonthGprsUi / userTotal;
			mCellularProgress.setProgress((int) (usedPercent * 100));
			mMonthCellularLimit.setText((userTotal >> 20) + "");
			if (monthRemainder >= 0) {
				mTodayCellularTrafficRemainderPercent
						.setText((int) (remainderPercent * 100) + "%");
				mTodayCellularTrafficRemainderPercent
						.setTextColor(getResources()
								.getColor(R.color.green));
				net_traffic_month_remainder_des_tv
						.setText(R.string.net_traffice_month_remaider_percent);
			} else {
				net_traffic_month_remainder_des_tv
						.setText(R.string.over_limit);
				mTodayCellularTrafficRemainderPercent.setText(MemoryUtil
						.formatMemorySize(2, -monthRemainder) + "M");
				mTodayCellularTrafficRemainderPercent
						.setTextColor(Color.RED);
			}
		} else {
			mTodayCellularTraffic.setText("0.00");
			mTodayCellularTrafficUsed.setText(monthLeft);
			long monthRemainder = userTotal - left_per_month;
			if (monthRemainder > 0) {
				mTodayCellularTrafficRemainder.setText((monthRemainder >> 20)
						+ "");
			} else {
				mTodayCellularTrafficRemainder.setText("0");
			}
			double remainderPercent = (double) monthRemainder / userTotal;
			double usedPercent = (double) left_per_month / userTotal;
			mCellularProgress.setProgress((int) (usedPercent * 100));
			mMonthCellularLimit.setText((userTotal >> 20) + "");
			mTodayCellularTrafficRemainderPercent
					.setText((int) (remainderPercent * 100) + "%");
		}
		for (int i = 0; i < list.size(); i++) {
			Interface trafficCounter = list.get(i);
			if (getString(R.string.interfaceTypeWifi).equals(
					trafficCounter.getPrettyName())) {
				List<TrafficCounter> trafficCounterList = trafficCounter
						.getCounters();
				if (trafficCounterList.size() > 3) {
					TrafficCounter todayWiFi = trafficCounterList.get(3);
					long[] todayBytes = todayWiFi.getBytes();
					long todayByte = todayBytes[0] + todayBytes[1];

					mTodayWiFiTraffic.setText(MemoryUtil.formatMemorySize(1,
							todayByte));

					TrafficCounter monthWiFi = trafficCounterList.get(1);
					long[] monthTotal = monthWiFi.getBytes();
					long monthTotalByte = monthTotal[0] + monthTotal[1];
					mMonthWifiTrafficUsed.setText(MemoryUtil.formatMemorySize(
							2, monthTotalByte));

					TrafficCounter allWiFi = trafficCounterList.get(0);
					long[] allTotal = allWiFi.getBytes();
					long allTotalByte = allTotal[0] + allTotal[1];
					mAllWifiTrafficUsed.setText(MemoryUtil.formatMemorySize(2,
							allTotalByte));

					SharedPreferences preferences = mApp
							.getAdapter(SharedPreferences.class);
					String day_str = preferences.getString(
							UtilShareprefece.CAL_DATE_INT, "1");
					mBillDay.setText(day_str);
					hasWifi = true;
					break;
				}
			}
		}
		if (!hasWifi) {
			mTodayWiFiTraffic.setText("0.00");
			mMonthWifiTrafficUsed.setText("0");
			mAllWifiTrafficUsed.setText("0");
			mBillDay.setText("1");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		mModel.removeModelListener(this);
		mModel.removeOperationListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();

		boolean enableNetWork = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		if (enableNetWork) {
		

			mApp.startService();

			mModel.addModelListener(this);
			mModel.addOperationListener(this);
		}

		boolean isTrue = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		enable_net_traffic_tv
				.setText(isTrue ? R.string.net_traffice_settings_enable
						: R.string.net_traffice_settings_disable);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(broadcastReceiver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(NetTrafficSummary.this,
					DefenderTabMain.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

			startActivity(intent);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void modelLoaded(IModel object) {

	}

	public void modelChanged(IModel object) {
		runOnUiThread(new Runnable() {
			public void run() {
				fillData();
			}
		});
	}

	public void operationStarted() {
		runOnUiThread(new Runnable() {
			public void run() {
				getWindow().setFeatureInt(
						Window.FEATURE_INDETERMINATE_PROGRESS,
						Window.PROGRESS_VISIBILITY_ON);
			}
		});
	}

	public void operationEnded() {
		runOnUiThread(new Runnable() {
			public void run() {
				getWindow().setFeatureInt(
						Window.FEATURE_INDETERMINATE_PROGRESS,
						Window.PROGRESS_VISIBILITY_OFF);
			}
		});
	}

}
