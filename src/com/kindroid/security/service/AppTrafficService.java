/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.kindroid.security.R;
import com.kindroid.security.model.Device;
import com.kindroid.security.model.Interface;
import com.kindroid.security.model.NetTrafficModel;
import com.kindroid.security.model.TrafficCounter;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.PopViewAid;

public class AppTrafficService extends Service {
	private NetTrafficModel mModel;
	private KindroidSecurityApplication mApp;
	private Timer timer;
	private Handler handler = new Handler();

	@Override
	public void onCreate() {
		super.onCreate();

		PopViewAid.initPopView(this);
		mApp = (KindroidSecurityApplication) getApplication();
		mModel = mApp.getAdapter(NetTrafficModel.class);
		timer = new Timer();
		timer.schedule(new TraficTimerTask(), 10, 3000);

	}

	@Override
	public void onStart(Intent intent, final int startId) {
		super.onStart(intent, startId);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		PopViewAid.removePopView(this);
		timer.cancel();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	class TraficTimerTask extends TimerTask {
		private long preTotal;
		private long curTotal;
		private boolean firstUse = false;;

		public TraficTimerTask() {
			firstUse = true;
		}
/*
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (!mModel.isLoaded()) {
				return;
			}
			Device device = Device.getDevice();
			String bluetooth = device.getBluetooth();
			String[] interfaces = device.getInterfaces();

			for (int i = 0; i < interfaces.length; i++) {
				String inter = interfaces[i];
				// Skip the Bluetooth interface is not available.
				if (inter.equals(bluetooth) && !SysClassNet.isUp(inter)) {

					continue;
				}
				
				
				try {
					Interface tmp = mModel.getInterface(inter);
					if (tmp.getPrettyName().equals(
							getString(R.string.interfaceTypeCell))) {
						long rx = SysClassNet.getRxBytes(inter);
						long tx = SysClassNet.getTxBytes(inter);
						tmp.updateBytes(rx, tx);
						mModel.commit();
						List<TrafficCounter> couters = tmp.getCounters();
						if (couters != null && couters.size() > 3) {
							long[] totalGprsArray = couters.get(3).getBytes();
							

							curTotal = totalGprsArray[0] + totalGprsArray[1];
							if (firstUse) {
								preTotal = curTotal;
								firstUse = false;
							}

							boolean isTrue = false;
							long total = 0;
							if (curTotal == preTotal) {
								isTrue = false;
								total = curTotal;

							} else if (curTotal > preTotal) {
								isTrue = true;
								total = curTotal - preTotal;

								preTotal = curTotal;
							} else {
								isTrue = false;
								preTotal = curTotal;
								total = curTotal;

							}
							final long total_copy = total;
							final boolean type = isTrue;
							handler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									PopViewAid.setText(total_copy, type);
								}
							});
						}
						break;
					}

				} catch (IOException e) {
					Log.e(getClass().getName(), "I/O Error", e);
				}
			}

		}
*/
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (!mModel.isLoaded()) {
				return;
			}
			curTotal = 0;
			/*
			Device device = Device.getDevice();
			String bluetooth = device.getBluetooth();
			String[] interfaces = device.getInterfaces();
			
			for (int i = 0; i < interfaces.length; i++) {
				String inter = interfaces[i];
				// Skip the Bluetooth interface is not available.
				if (inter.equals(bluetooth) || !SysClassNet.isUp(inter)) {

					continue;
				}
			*/
			List<Interface> interfaces = mModel.getInterfaces();
			for(Interface tmp : interfaces){
				
				try {
//					Interface tmp = mModel.getInterface(inter);
					if (tmp.getPrettyName().equals(
							getString(R.string.interfaceTypeCell))) {
//						long rx = SysClassNet.getRxBytes(inter);
//						long tx = SysClassNet.getTxBytes(inter);
//						tmp.updateBytes(rx, tx);
//						mModel.commit();
						List<TrafficCounter> couters = tmp.getCounters();
						if (couters != null && couters.size() > 3) {
							long[] totalGprsArray = couters.get(3).getBytes();
							

							curTotal += totalGprsArray[0] + totalGprsArray[1];
							
						}						
					}

				} catch (Exception e) {
					Log.e(getClass().getName(), "I/O Error", e);
				}
			}

			if (firstUse) {
				preTotal = curTotal;
				firstUse = false;
			}
			
			boolean isTrue = false;
			long total = 0;
			if (curTotal == preTotal) {
				isTrue = false;
				total = curTotal;

			} else if (curTotal > preTotal) {
				isTrue = true;
				total = curTotal - preTotal;

				preTotal = curTotal;
			} else {
				isTrue = false;
				preTotal = curTotal;
				total = curTotal;

			}
			final long total_copy = total;
			final boolean type = isTrue;
			
			try{				
				handler.post(new Runnable() {
	
					@Override
					public void run() {
						// TODO Auto-generated method stub
						PopViewAid.setText(total_copy, type);
					}
				});
			}catch(Exception e){
				e.printStackTrace();
			}

		}
	}

}
