/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.ActivityManager;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.kindroid.security.R;
import com.kindroid.security.adapter.AppTraficItemAdapter;
import com.kindroid.security.util.AppItemDetail;
import com.kindroid.security.util.AppNetWorkDataBase;


public class NetTrafficAppTabMain extends TabActivity implements
		OnClickListener {

	private LinearLayout netTraffic;
	private LinearLayout netTraffic_app;
	private BroadcastReceiver broadcast;

	private ListView day_listView;
	private ListView month_listView;
	private Map<String, AppItemDetail> map_day = new HashMap<String, AppItemDetail>();
	private List<Map.Entry<String, AppItemDetail>> infoIds_day;
	private AppTraficItemAdapter day_adapter;

	private Map<String, AppItemDetail> map_month = new HashMap<String, AppItemDetail>();

	private AppTraficItemAdapter month_adapter;
	private List<Map.Entry<String, AppItemDetail>> infoIds_month;
	
	private PackageManager mPm;
	
	private Handler handler=new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.net_traffic_app_tab_main);
		findViews();
		addTab();
		mPm=getPackageManager();
		Cursor c = AppNetWorkDataBase.get(this).lookAllAppData(
				Calendar.getInstance(), null);
		initDayData(c, 0);
		day_adapter = new AppTraficItemAdapter(this, infoIds_day);
		day_listView.setAdapter(day_adapter);

		IntentFilter ifilter = new IntentFilter();
		broadcast = new UiUpdateBroadcast();
		ifilter.addAction("ehoo.com.update.ui.traffic_app");
		registerReceiver(broadcast, ifilter);

		new LoadThread(1).start();

	}

	void initDayData(Cursor c, int type) {
		if (c == null || c.getCount() == 0)
			return;
		Map<String, AppItemDetail> map = null;
		if (type == 0) {
			map = map_day;
		} else {
			map = map_month;
		}
		while (c.moveToNext()) {
			String pkg = c.getString(c.getColumnIndex("pkg"));
			try{
				mPm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES);
			}catch(Exception e){
				continue;
			}
			long rx = c.getLong(c.getColumnIndex("rx"));
			long tx = c.getLong(c.getColumnIndex("tx"));
			if (map.containsKey(pkg)) {
				AppItemDetail app = map.get(pkg);
				app.setRx(app.getRx() + rx);
				app.setTx(app.getTx() + tx);
			} else {
				AppItemDetail app = new AppItemDetail();
				app.setPkg(pkg);
				app.setRx(rx);
				app.setTx(tx);
				map.put(pkg, app);
			}
		}
		if (type == 0) {
			removeLittleData(map_day);
			infoIds_day = new ArrayList<Map.Entry<String, AppItemDetail>>(
					map_day.entrySet());

		} else {
			removeLittleData(map_month);
			infoIds_month = new ArrayList<Map.Entry<String, AppItemDetail>>(
					map_month.entrySet());
		}

		Collections.sort(type == 0 ? infoIds_day : infoIds_month,
				new Comparator<Map.Entry<String, AppItemDetail>>() {

					@Override
					public int compare(Entry<String, AppItemDetail> o1,
							Entry<String, AppItemDetail> o2) {
						// TODO Auto-generated method stub
						return (int) (o2.getValue().getTotal() - o1.getValue()
								.getTotal());

					}
				});
		if (c != null) {
			c.close();
		}
	}

	class LoadThread extends Thread {
		private int type;

		public LoadThread(int type) {
			this.type = type;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Cursor c = null;
			if (type == 1) {
				
				
				Calendar begin = Calendar.getInstance();
				begin.set(Calendar.DAY_OF_MONTH, 1);
				c = AppNetWorkDataBase.get(NetTrafficAppTabMain.this)
						.lookAllAppData(begin, Calendar.getInstance());
			} else {
				c = AppNetWorkDataBase.get(NetTrafficAppTabMain.this)
						.lookAllAppData(Calendar.getInstance(), null);
			}

			initDayData(c, type);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (type == 1) {
						month_adapter = new AppTraficItemAdapter(
								NetTrafficAppTabMain.this, infoIds_month);
						month_listView.setAdapter(month_adapter);
					} else {
						day_adapter = new AppTraficItemAdapter(
								NetTrafficAppTabMain.this, infoIds_day);
						day_listView.setAdapter(day_adapter);
					}

				}
			});
		}

	}

	private void findViews() {
		netTraffic = (LinearLayout) findViewById(R.id.net_traffic);
		day_listView = (ListView) findViewById(R.id.day_listview);
		month_listView = (ListView) findViewById(R.id.month_listview);

		netTraffic.setOnClickListener(this);
		netTraffic_app = (LinearLayout) findViewById(R.id.net_traffic_app);

		netTraffic_app.setOnClickListener(this);

	}

	private void addTab() {
		TabHost host = getTabHost();
		TabSpec summaryTab = host.newTabSpec("tab1").setIndicator("tab1")
				.setContent(R.id.day_network_lieanr);
		TabSpec appTab = host.newTabSpec("tab2").setIndicator("tab2")
				.setContent(R.id.month_network_lieanr);

		host.addTab(summaryTab);
		host.addTab(appTab);
		getTabHost().setCurrentTab(0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.net_traffic:
			netTraffic.setBackgroundColor(Color.rgb(238, 238, 238));

			netTraffic_app.setBackgroundColor(Color.rgb(210, 210, 210));
			getTabHost().setCurrentTab(0);
			break;
		case R.id.net_traffic_app:
			netTraffic.setBackgroundColor(Color.rgb(210, 210, 210));

			netTraffic_app.setBackgroundColor(Color.rgb(238, 238, 238));
			getTabHost().setCurrentTab(1);
			break;

		}
	}

	private void removeLittleData(Map<String, AppItemDetail> maps) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, AppItemDetail> entry : maps.entrySet()) {
			String key = entry.getKey().toString();
			if(entry.getValue().getTotal()<10486){
				sb.append(key+";");
			}
		}
		String str=sb.toString();
		if(str.length()==0)
			return;
		String []key=str.split(";");
		for (int i = 0; i < key.length; i++) {
			maps.remove(key[i]);
		}

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
	}

	class UiUpdateBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (!intent.getAction().equals("ehoo.com.update.ui.traffic_app"))
				return;
			handler.postDelayed(new UpdateRunable(), 2500);
		}

	}
	class UpdateRunable implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int tab = getTabHost().getCurrentTab();
			map_day.clear();
			map_month.clear();
			if (tab == 0) {
				Cursor c = AppNetWorkDataBase.get(NetTrafficAppTabMain.this)
						.lookAllAppData(Calendar.getInstance(), null);

				initDayData(c, 0);
				day_adapter = new AppTraficItemAdapter(
						NetTrafficAppTabMain.this, infoIds_day);
				day_listView.setAdapter(day_adapter);
				day_adapter.notifyDataSetChanged();
				new LoadThread(1).start();
			} else {
				
				
				Calendar begin = Calendar.getInstance();
				begin.set(Calendar.DAY_OF_MONTH,1);
				Cursor c = AppNetWorkDataBase.get(NetTrafficAppTabMain.this)
						.lookAllAppData(begin, Calendar.getInstance());
				initDayData(c, 1);
				month_adapter = new AppTraficItemAdapter(
						NetTrafficAppTabMain.this, infoIds_month);
				month_listView.setAdapter(month_adapter);
				new LoadThread(0).start();

			}
	
		}
		
	}
	
	
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(NetTrafficAppTabMain.this,
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
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(broadcast);
	}

}
