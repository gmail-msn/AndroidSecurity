/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.RemoteViews;

import java.io.File;

import com.kindroid.security.R;
import com.kindroid.security.model.DownloadTask;
import com.kindroid.security.service.DownloadService;

public class DownloadNotification {
	
    Context mContext;
    public NotificationManager mNotificationMgr;
    
	public DownloadNotification(Context ctx) {
		mContext = ctx;
		mNotificationMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	 /**
     * Update the notification ui. 
     */
	public void updateNotification(DownloadTask task){
		Notification notification = new Notification(R.drawable.icon, task.getTitle(), System.currentTimeMillis());
		Intent intent = new Intent(Intent.ACTION_VIEW);
		ComponentName comp = new ComponentName(mContext.getPackageName(),
				mContext.getPackageName() + ".ui.AppEngActivity");

		intent.setComponent(comp);		
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent pending = PendingIntent.getActivity(mContext, 0, intent,0);
		notification.contentView = new RemoteViews(mContext.getPackageName(),R.layout.notification_layout);
		notification.contentView.setTextViewText(R.id.notificationTitle, task.getTitle());
		if(task.getIcon() != null && !task.getIcon().isRecycled()){
			notification.contentView.setImageViewBitmap(R.id.notificationIcon, task.getIcon());
		}else{
			notification.contentView.setImageViewResource(R.id.notificationIcon, R.drawable.icon_default);
		}
		notification.flags = notification.flags| Notification.FLAG_ONGOING_EVENT;
		notification.contentIntent = pending;
		notification.contentView.setProgressBar(R.id.notificationProgress, task.getTotalSize(), task.getCurrentSize(), false);
		mNotificationMgr.notify(Integer.parseInt(task.getId()), notification);
		
	}
	
	public void downloadCompletedNotification(DownloadTask task) {
		Notification notification = new Notification(R.drawable.icon, task.getTitle(), System.currentTimeMillis());
		Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+ DownloadService.DOWNLOAD_DIR + task.getId() + ".apk"));
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		PendingIntent pending = PendingIntent.getActivity(mContext, 0, intent,0);
		notification.setLatestEventInfo(mContext, task.getTitle(), task.getTitle(), pending);
		mNotificationMgr.notify(Integer.parseInt(task.getId()), notification);
	}

}
