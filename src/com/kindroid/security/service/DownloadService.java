/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedList;

import com.kindroid.security.R;
import com.kindroid.security.model.DownloadTask;
import com.kindroid.security.notification.DownloadNotification;
import com.kindroid.security.util.HttpRequest;

public class DownloadService extends Service {

	private static boolean mCanceled = false;
	private DownloadNotification mNotifier;
	private static DownloadTask mCurrentTask;
	public static LinkedList<DownloadTask> downtask = new LinkedList<DownloadTask>();
	private static Object lock = new Object();

	public static final String UPDATE_DOWN_TASK = "com.kindroid.security.DOWNLOAD_APK";
	public static final String DOWNLOAD_DIR = "/Kindroid/download/";
	public static boolean addDownTask(DownloadTask task) {
		synchronized (lock) {
			downtask.add(task);
		}
		;
		return true;
	}

	public static void removeDownTask(int index) {
		synchronized (lock) {
			downtask.remove(index);
		}
		;
	}

	public static void cancelDownloadTask(String taskId) {
		if (taskId.equals(mCurrentTask.getId())) {
			mCanceled = true;
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(DownloadService.this, R.string.network_tip,
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(DownloadService.this, R.string.download_error,
						Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(DownloadService.this, R.string.download_error,
						Toast.LENGTH_SHORT).show();
				break;
			case 4:
				//sdcard mounted error
				Toast.makeText(DownloadService.this, R.string.sdcard_noexist,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onStart(Intent intent, int startId) {
		if (null != intent) {
			if (UPDATE_DOWN_TASK.equals(intent.getAction())) {
				new DownloadThread().start();
			}
		}
		super.onStart(intent, startId);
	}

	@Override
	public void onCreate() {
		mNotifier = new DownloadNotification(this);
		super.onCreate();
	}

	private class DownloadThread extends Thread {

		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			if (!downtask.isEmpty()) {
				DownloadTask task = downtask.getLast();
				downloadFile(task);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void dismissNotifaction() {

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(Integer.parseInt(mCurrentTask.getId()));
	}

	private void downloadFile(final DownloadTask downloadTask) {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			downtask.remove(downloadTask);
			mHandler.sendEmptyMessage(4);
			return;
		}		
		
		String fileName = Environment.getExternalStorageDirectory()
				+ DOWNLOAD_DIR + downloadTask.getId() + ".apk";

		try {
			mCanceled = false;
			mCurrentTask = downloadTask;
			boolean downloadFinished = false;

			File buyincFolder = new File(
					Environment.getExternalStorageDirectory()
							+ DOWNLOAD_DIR);
			if (!buyincFolder.exists()) {
				buyincFolder.mkdirs();
			}

			File file = new File(fileName);

			if (file.exists()) {
				file.delete();
			}

			FileOutputStream out = new FileOutputStream(fileName, false);
			String appId = downloadTask.getId();

			URL url = new URL(downloadTask.getDownUrl());

			if (!mCanceled) {

				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setUseCaches(false);
				connection.setRequestProperty("Cache-Control", "no-cache");
				
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(10000);

				int totalSize = connection.getContentLength();

				downloadTask.setTotalSize(totalSize);
				InputStream inputStream = connection.getInputStream();

				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
						out);
				BufferedInputStream bufferedInputStream = new BufferedInputStream(
						inputStream);

				byte[] buf = new byte[4096];
				int bytesRead = 0;
				long timeLastNotification = 0;

				while (bytesRead >= 0 && !mCanceled) {
					long now = System.currentTimeMillis();
					bufferedOutputStream.write(buf, 0, bytesRead);
					bytesRead = bufferedInputStream.read(buf);

					if (bytesRead > 0) {
						downloadTask.setCurrentSize(bytesRead
								+ downloadTask.getCurrentSize());
					}

					if (now - timeLastNotification > 100) {
						mNotifier.updateNotification(downloadTask);
					}

					timeLastNotification = now;
				}

				bufferedOutputStream.flush();

				if ((downloadTask.getCurrentSize() < totalSize)
						|| (totalSize <= 0)) {
					downloadFinished = false;

					if (mCanceled) {
						mHandler.sendEmptyMessage(1);
					} else {
						mHandler.sendEmptyMessage(2);
					}
				} else {
					downloadFinished = true;
				}
			}

			downtask.remove(downloadTask);
			if (downloadFinished) {
				mNotifier.downloadCompletedNotification(mCurrentTask);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(fileName)),
						"application/vnd.android.package-archive");
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			dismissNotifaction();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			dismissNotifaction();
			downtask.remove(downloadTask);
			mHandler.sendEmptyMessage(2);
		} catch (ProtocolException e) {
			e.printStackTrace();
			dismissNotifaction();
			downtask.remove(downloadTask);
			mHandler.sendEmptyMessage(2);
		}catch (FileNotFoundException e) {
			e.printStackTrace();
			dismissNotifaction();
			downtask.remove(downloadTask);
			mHandler.sendEmptyMessage(3);
		}catch (IOException e) {
			e.printStackTrace();
			dismissNotifaction();
			downtask.remove(downloadTask);
			mHandler.sendEmptyMessage(0);
		} catch (Exception e) {
			e.printStackTrace();
			dismissNotifaction();
			downtask.remove(downloadTask);
			mHandler.sendEmptyMessage(2);
		}
	}
}
