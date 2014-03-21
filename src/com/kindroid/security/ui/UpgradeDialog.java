/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.kindroid.security.R;
import com.kindroid.security.service.DownloadService;

public class UpgradeDialog extends Dialog implements View.OnClickListener {
	private View mView;
	private ProgressBar mProgressBar;

	private Button downloadOkBtn;
	private Button downloadCancelBtn;

	private TextView downDes;

	private int currentSize = 0;
	private int totalSize = 0;
	private boolean downloadCanceled = false;
	private boolean downloadFinished = false;
	private Context mContext;

	private String downUrl;

	private Handler mProgressHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mProgressBar.setMax(totalSize);
				mProgressBar.setProgress(currentSize);
				if (!downloadFinished) {
					if (downloadCanceled) {
						dismiss();
					}
				} else {
					// dismiss();
				}
				break;
			case 1:
				dismiss();
				break;
			case 2:
				dismiss();
				break;
			case 3:
				Toast.makeText(mContext, R.string.sdcard_noexist, Toast.LENGTH_LONG).show();
				dismiss();
				break;
			}
		}
	};

	protected UpgradeDialog(Context context) {
		super(context, R.style.Theme_CustomDialog);
		mContext = context;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.downloadOkBtn) {
			View releaseNotesLinear = findViewById(R.id.releasenotes_linear);
			releaseNotesLinear.setVisibility(View.GONE);
			findViewById(R.id.upgrade_prompt_repeat_linear).setVisibility(View.GONE);
			findViewById(R.id.upgrade_confirm_linear).setVisibility(View.GONE);
			View progress_linear = findViewById(R.id.progress_linear);
			progress_linear.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
			downDes.setText(R.string.softmanage_download_recommend_text);
			downDes.setVisibility(View.VISIBLE);
			new DownloadingThread().start();
			downloadOkBtn.setVisibility(View.GONE);
		} else if (v.getId() == R.id.downloadCancelBtn) {
			downloadCanceled = true;
			dismiss();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setLayout(R.layout.download_dialog);
		
		super.onCreate(savedInstanceState);
	}

	private void setLayout(int layoutResId) {
		setContentView(mView = getLayoutInflater().inflate(layoutResId, null));
		onReferenceViews(mView);
	}

	public void reset(String downUrl) {
		this.downUrl = downUrl;
		View progress_linear = findViewById(R.id.progress_linear);
		progress_linear.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.INVISIBLE);
		downloadOkBtn.setVisibility(View.VISIBLE);
		mProgressBar.setProgress(0);
		downloadCanceled = false;
		downloadFinished = false;
		currentSize = 0;
		totalSize = 0;
	}

	public void downUrl(String downUrl) {
		this.downUrl = downUrl;
		View releaseNotesLinear = findViewById(R.id.releasenotes_linear);
		releaseNotesLinear.setVisibility(View.GONE);
		View progress_linear = findViewById(R.id.progress_linear);
		progress_linear.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.VISIBLE);
		
		new DownloadingThread().start();
		downloadOkBtn.setVisibility(View.GONE);
		mProgressBar.setProgress(0);
		downDes.setText(R.string.softmanage_download_recommend_text);
		downloadCanceled = false;
		downloadFinished = false;
		currentSize = 0;
		totalSize = 0;
	}

	private void onReferenceViews(View view) {
		mProgressBar = (ProgressBar) view
				.findViewById(R.id.downloadingProgress);
		downloadOkBtn = (Button) view.findViewById(R.id.downloadOkBtn);
		downloadCancelBtn = (Button) view.findViewById(R.id.downloadCancelBtn);
		downDes = (TextView) view.findViewById(R.id.down_des);
		downloadOkBtn.setOnClickListener(this);
		downloadCancelBtn.setOnClickListener(this);
	}

	public static final int UPDATE_REQUEST_CODE = 1;

	private class DownloadingThread extends Thread {
		public void run() {
			mProgressBar.setVisibility(View.VISIBLE);
			if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				mProgressHandler.sendEmptyMessage(3);
				return;
			}
			File downloadDir = new File(Environment.getExternalStorageDirectory() + DownloadService.DOWNLOAD_DIR);
			if(!downloadDir.exists()){
				downloadDir.mkdirs();
			}
			String fileName = Environment.getExternalStorageDirectory() + DownloadService.DOWNLOAD_DIR
					+ "/KindroidSecurity.apk";
			try {
				FileOutputStream out = new FileOutputStream(fileName, false);

				URL url = new URL(downUrl);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();

				InputStream inputStream = connection.getInputStream();

				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
						out);
				BufferedInputStream bufferedInputStream = new BufferedInputStream(
						inputStream);
				totalSize = connection.getContentLength();
				byte[] buf = new byte[4096];
				int bytesRead = 0;

				while (bytesRead >= 0 && !downloadCanceled) {
					bufferedOutputStream.write(buf, 0, bytesRead);
					bytesRead = bufferedInputStream.read(buf);
					currentSize += bytesRead;
					mProgressHandler.sendEmptyMessage(0);
				}

				bufferedOutputStream.flush();

				if ((currentSize < totalSize - 1) || (totalSize <= 0)) {
					downloadFinished = false;
					if (downloadCanceled) {
						mProgressHandler.sendEmptyMessage(1);
					} else {
						mProgressHandler.sendEmptyMessage(2);
					}
				} else {
					downloadFinished = true;
				}

				if (downloadFinished) {
					dismiss();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(new File(fileName)),
							"application/vnd.android.package-archive");
					getContext().startActivity(intent);
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (ProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				mProgressHandler.sendEmptyMessage(0);
			}
		}
	}

}
