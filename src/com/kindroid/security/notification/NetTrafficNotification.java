/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.notification;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.model.Interface;
import com.kindroid.security.model.NetTrafficModel;
import com.kindroid.security.model.TrafficCounter;
import com.kindroid.security.util.HistoryNativeCursor;
import com.kindroid.security.util.InterceptDataBase;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.MemoryUtil;
import com.kindroid.security.util.UtilShareprefece;

public class NetTrafficNotification {

	Context mContext;
	public NotificationManager mNotificationMgr;

	private KindroidSecurityApplication mApp;
	private NetTrafficModel mModel = null;

	public NetTrafficNotification(Application application, Context ctx) {
		mContext = ctx;
		mNotificationMgr = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);

		mApp = (KindroidSecurityApplication) application;
		mModel = mApp.getAdapter(NetTrafficModel.class);
	}

	public void showNetTrafficNotification() {
		Notification notification = new Notification(
				R.drawable.status_bar_icon,
				mContext.getString(R.string.app_name),
				System.currentTimeMillis());
		notification.flags = notification.flags
				| Notification.FLAG_ONGOING_EVENT;
		notification.contentView = new RemoteViews(mContext.getPackageName(),
				R.layout.net_notification_layout);

		List<Interface> list = mModel.getInterfaces();

		String monthLimit = mApp.getAdapter(SharedPreferences.class).getString(
				UtilShareprefece.LIMIT_PER_MON_INT, "30");
		String monthLeft = mApp.getAdapter(SharedPreferences.class).getString(
				UtilShareprefece.LEFT_PER_MON_INT, "0");
		long limit_per_month = Long.parseLong(monthLimit);
		long left_per_month = (long) (Double.parseDouble(monthLeft) * 1024 * 1024);
		long userTotal = limit_per_month * 1024 * 1024;
		boolean show_al = false;
		long todayByteUi = 0;
		long totalMonthGprsUi = 0;
		for (int i = 0; i < list.size(); i++) {
			Interface trafficCounter = list.get(i);

			if (mContext.getString(R.string.interfaceTypeCell).equals(
					trafficCounter.getPrettyName())
					&& trafficCounter.getCounters().size() > 3) {
				TrafficCounter monthCellular = trafficCounter.getCounters()
						.get(1);
				long[] totalMonthGprsArray = monthCellular.getBytes();
//				long totalMonthGprs = totalMonthGprsArray[0]
//						+ totalMonthGprsArray[1] + left_per_month;
				totalMonthGprsUi += totalMonthGprsArray[0] + totalMonthGprsArray[1];
				long todayGprsArray[] = trafficCounter.getCounters().get(3)
						.getBytes();
				todayByteUi += todayGprsArray[0] + todayGprsArray[1];
//				fillNetWorkDate(notification, totalMonthGprs, userTotal,
//						todayGprsArray[0] + todayGprsArray[1]);
				// double usedPercent = (double) totalMonthGprs / userTotal;
				// notification.contentView.setProgressBar(
				// R.id.notificationProgress, 100,
				// (int) (usedPercent * 100), false);
				// notification.contentView.setTextViewText(
				// R.id.progressDescription, mContext.getString(
				// R.string.notification_text,
				// MemoryUtil.formatMemorySize(2, totalMonthGprs)
				// + " MB", (userTotal >> 20) + " MB"));
				show_al = true;
				//break;
			}

		}
		fillNetWorkDate(notification, totalMonthGprsUi + left_per_month, userTotal,
				todayByteUi);
		if (!show_al) {

			fillNetWorkDate(notification, left_per_month, userTotal, 0);

			// double usedPercent = (double) left_per_month / userTotal;
			// notification.contentView.setProgressBar(R.id.notificationProgress,
			// 100, (int) (usedPercent * 100), false);
			//
			// notification.contentView.setTextViewText(
			// R.id.progressDescription,
			// mContext.getString(R.string.notification_text,
			// MemoryUtil.formatMemorySize(2, left_per_month)
			// + " MB", (userTotal >> 20) + " MB"));
		}

		Intent i = new Intent();
		ComponentName comp = new ComponentName(mContext.getPackageName(),
				mContext.getPackageName() + ".ui.AppEngActivity");

		i.setComponent(comp);
		i.putExtra("type", 1);
		i.setAction("android.intent.action.VIEW");

		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pending = PendingIntent.getActivity(mContext, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.contentIntent = pending;
		mNotificationMgr.notify(1020, notification);
	}

	public void cancelNotification() {
		mNotificationMgr.cancel(1020);
	}
	public void showInterceptNotification() {

		HistoryNativeCursor hnc = new HistoryNativeCursor();
		hnc.setmRequestType(4);
		int phone = InterceptDataBase.get(mContext).getHistoryNum(hnc);
		hnc.setmRequestType(3);
		int sms = InterceptDataBase.get(mContext).getHistoryNum(hnc);
		
		int drawableId = R.drawable.intercep_sms_phone_icon;
		if (sms == 0 && phone == 0) {
			mNotificationMgr.cancel(1212);
			return;
		}else if(phone == 0){
			drawableId = R.drawable.intercept_sms_title_icon;
		}else if(sms == 0){
			drawableId = R.drawable.intercep_phone_title_icon;
		}
		
		Notification notification = new Notification(
				drawableId,
				mContext.getString(R.string.app_name),
				System.currentTimeMillis());
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
		notification.contentView = new RemoteViews(mContext.getPackageName(),
				R.layout.net_notification_intercept_layout);

		notification.contentView.setTextViewText(R.id.intercet_phone_num_tv,
				phone + "");

		notification.contentView.setTextViewText(R.id.intercet_sms_num_tv, sms
				+ "");

		notification.contentView.setTextViewText(R.id.progressDescription,
				mContext.getString(R.string.safe_your_mobile));

		Intent i = new Intent();
		ComponentName comp = new ComponentName(mContext.getPackageName(),
				mContext.getPackageName() + ".ui.AppEngBlockActivity");

		i.setComponent(comp);
		i.setAction("android.intent.action.VIEW");
		i.putExtra("type", 2);

		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pending = PendingIntent.getActivity(mContext, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.contentIntent = pending;
		mNotificationMgr.notify(1212, notification);
	}

	private void fillNetWorkDate(Notification notification,
			long totalMonthGprs, long userTotal,long todayGprs) {
		double usedPercent = (double) totalMonthGprs / userTotal;
		int tProgressId;
		if (usedPercent < 0.9) {
			tProgressId = R.id.notificationProgress_green;
			notification.contentView.setViewVisibility(R.id.green_linear, View.VISIBLE);
			notification.contentView.setViewVisibility(R.id.yellow_linear, View.GONE);
			notification.contentView.setViewVisibility( R.id.red_linear, View.GONE);
		} else if (usedPercent >= 0.9 && usedPercent < 1) {
			notification.icon=R.drawable.network_warn;
			tProgressId = R.id.notificationProgress_yellow;
			notification.contentView.setViewVisibility(R.id.green_linear, View.GONE);
			notification.contentView.setViewVisibility(R.id.yellow_linear, View.VISIBLE);
			notification.contentView.setViewVisibility( R.id.red_linear, View.GONE);
		} else {
			tProgressId = R.id.notificationProgress_red;
			notification.icon=R.drawable.network_over;
			
			
			notification.contentView.setViewVisibility(R.id.green_linear, View.GONE);
			notification.contentView.setViewVisibility(R.id.yellow_linear, View.GONE);
			notification.contentView.setViewVisibility( R.id.red_linear, View.VISIBLE);
			
			
		}
		notification.contentView.setProgressBar(tProgressId, 100,
				(int) (usedPercent * 100), false);

		notification.contentView.setTextViewText(R.id.progressDescription,
				mContext.getString(R.string.notification_text,
						MemoryUtil.formatMemorySize(2, todayGprs) , MemoryUtil.formatMemorySize(2, totalMonthGprs)));
	}
}
