/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ActivityManager.MemoryInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.model.DownloadTask;
import com.kindroid.security.util.Appstore.Response;
import com.kindroid.security.service.DownloadService;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.HttpRequest;
import com.kindroid.security.util.UnitsConversion;
import com.kindroid.security.util.Utilis;

/**
 * @author jie.li
 * 
 */
public class RecommendAppDetail extends Activity implements OnClickListener {

	public static AppInfoForManage mAppInfoForManage;
	private boolean isDownloading = false;
	
	private AppInfoForManage mAppInfo;
	private ImageView mAppIcon;
	private TextView mAppName;
	private TextView mAppVersion;
	private TextView mAppSizeNum;
	private TextView mAppSizeUnit;
	private TextView mAppDescription;
	private ImageView mScreenshotOne;
	private ImageView mScreenshotTwo;
	private LinearLayout mInstallApk;

	private ProgressBar mProgressBar;
	private TextView mDownloadingText;

	private ImageView shotCut;
	private TextView textDesc;

	private int tempCount = 0;
	private Bitmap bitMapOne;
	private Bitmap bitMapTwo;
	private List<Bitmap> bitmapList = new ArrayList<Bitmap>();
	private LoadScreenShot mLoadScreenShotOne;
	private LoadScreenShot mLoadScreenShotTwo;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 2:
				if (mAppInfoForManage == null) {
					return;
				}
				String packageName = mAppInfoForManage.getPackageName();
				int size = DownloadService.downtask.size();

				boolean found = false;
				for (int i = 0; i < size; i++) {
					DownloadTask task = DownloadService.downtask.get(i);
					if (task.getPackageName().equals(packageName)) {
						found = true;
						int percent = 0;

						int totalSize = task.getTotalSize();
						int currentSize = task.getCurrentSize();

						mProgressBar.setMax(totalSize);
						mProgressBar.setProgress(currentSize);

						if (totalSize > 0) {
							percent = currentSize * 100 / totalSize;
						}

						String text = String.format("%d%% %dKB/%dKB", percent,
								currentSize / 1000, totalSize / 1000);
						mDownloadingText.setText(text);

						break;
					}
				}

				if (found) {
					this.sendEmptyMessageDelayed(2, 500);
				} else {
					tempCount += 1;
					this.sendEmptyMessage(7);
				}
				break;
			case 7:
				mInstallApk.setClickable(true);
				mDownloadingText.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.GONE);
				TextView textDesc = (TextView)findViewById(R.id.textDesc);
				textDesc.setText(R.string.softmanage_install_now_text);
				isDownloading = false;
				updateButton();				
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.recommen_app_detail);
		findViews();
		fillData();
		tempCount = 0;
		mAppInfo = mAppInfoForManage;
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		
	}

	private void findViews() {
		mAppIcon = (ImageView) findViewById(R.id.appIcon);
		mAppName = (TextView) findViewById(R.id.appName);
		mAppVersion = (TextView) findViewById(R.id.appVersion);
		mAppSizeNum = (TextView) findViewById(R.id.appSizeNum);
		mAppSizeUnit = (TextView) findViewById(R.id.appSizeUnit);
		mAppDescription = (TextView) findViewById(R.id.appDescription);

		mScreenshotOne = (ImageView) findViewById(R.id.appScreenshotOne);
		mScreenshotTwo = (ImageView) findViewById(R.id.appScreenshotTwo);

		mInstallApk = (LinearLayout) findViewById(R.id.installApk);
		mInstallApk.setClickable(true);
		mInstallApk.setOnClickListener(this);

		mProgressBar = (ProgressBar) findViewById(R.id.downloadingBar);
		mDownloadingText = (TextView) findViewById(R.id.downloadingText);

		shotCut = (ImageView) findViewById(R.id.installShotcut);
		textDesc = (TextView) findViewById(R.id.textDesc);
	}

	private void fillData() {
		if (mAppInfoForManage != null) {
			if(mAppInfoForManage.getIcon() != null){
				mAppIcon.setImageDrawable(mAppInfoForManage.getIcon());
			}else{
				mAppIcon.setImageDrawable(getPackageManager().getDefaultActivityIcon());
			}
			mAppName.setText(mAppInfoForManage.getLabel());
			mAppVersion.setText(mAppInfoForManage.getVersion());
			String sizeStr = new UnitsConversion().defaultConversion(mAppInfoForManage.getSize());
			String[] size = sizeStr.split(" ");
			if (size.length > 1) {
				mAppSizeNum.setText(size[0]);
				mAppSizeUnit.setText(size[1]);
			}
			mAppDescription.setText(mAppInfoForManage.getDescription());
			if(mAppInfoForManage.getPartnerId() == 0){
				mScreenshotOne.setVisibility(View.VISIBLE);
				mScreenshotTwo.setVisibility(View.VISIBLE);
				mLoadScreenShotOne = new LoadScreenShot(0);
				mLoadScreenShotTwo = new LoadScreenShot(1);
				mLoadScreenShotOne.start();
				mLoadScreenShotTwo.start();
			}else{
				mScreenshotOne.setVisibility(View.GONE);
				mScreenshotTwo.setVisibility(View.GONE);
			}
		}
	}

	private void updateButton() {
		if(isDownloading){
			TextView textDesc = (TextView)findViewById(R.id.textDesc);
			textDesc.setText(R.string.softcenter_install_cancel_text);
			return;
		}
		int isInstalled = isInstalled();
		if (isInstalled == 0) {
			shotCut.setImageBitmap(null);
			mInstallApk.setClickable(false);
			textDesc.setText(R.string.had_install);
		}else if(isInstalled == 1){
			shotCut.setImageResource(R.drawable.icon_anzhuang);
			mInstallApk.setClickable(true);
			textDesc.setText(R.string.softcenter_search_my_upgrade);
		}else{
			shotCut.setImageResource(R.drawable.icon_anzhuang);
			mInstallApk.setClickable(true);
			textDesc.setText(R.string.softmanage_install_now_text);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		updateButton();
	}

	private int isInstalled() {
		if (mAppInfo != null) {			
			return Utilis.isInstalled(mAppInfo.getPackageName(), mAppInfo.getFlag(), getPackageManager().getInstalledPackages(PackageManager.GET_SIGNATURES));
		}
		return -1;
	}

	private class LoadScreenShot extends Thread {
		private int which;
		byte[] image;
		private boolean mCancel = false;
		public LoadScreenShot(int which) {
			this.which = which;
			mCancel = false;
		}
		public void cancelLoad(){
			mCancel = true;
		}

		@Override
		public void run() {
			if(mCancel){
				return;
			}
			try {
				Response response = Response.parseFrom(Base64
						.decodeBase64(ConvertUtils
								.InputStreamToByte(HttpRequest.request(
										mAppInfoForManage.getAppId(), which))));
				if(mCancel){
					return;
				}
				image = response.getImageResponse().getImageData()
						.toByteArray();
				if(mCancel){
					return;
				}
				if (which == 0) {
					runOnUiThread(new Runnable() {

						public void run() {
							if(mCancel){
								return;
							}
							try{
								try{
									bitMapOne = BitmapFactory.decodeByteArray(image, 0,
										image.length);
								}catch(OutOfMemoryError oome){
                            		oome.printStackTrace();
                            	}
								
								if(mCancel){
									bitmapList.add(bitMapOne);
									return;
								}
								
								mScreenshotOne.setImageBitmap(bitMapOne);								
								image = null;
							}catch(Exception e){
								e.printStackTrace();
							}
							

						}
					});
				} else if (which == 1) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(mCancel){
								return;
							}
                              try{
                            	
                            	try{
                            		bitMapTwo = BitmapFactory.decodeByteArray(image, 0,
											image.length); 
                            	}catch(OutOfMemoryError oome){
                            		oome.printStackTrace();
                            	}
                            	
                            	if(mCancel){
									bitmapList.add(bitMapTwo);
									return;
								}
                            	
      							mScreenshotTwo.setImageBitmap(bitMapTwo);
      							image = null;
                              }catch(Exception e){
                            	  e.printStackTrace();
                              }
							

						}
					});
				}

			} catch (ConnectTimeoutException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		/*
		if(isDownloading){
			//cancel downloading
			DownloadService.cancelDownloadTask(mAppInfoForManage.getAppId());
			isDownloading = false;
		}
		*/
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mLoadScreenShotOne != null){
			mLoadScreenShotOne.cancelLoad();
			if(bitMapOne != null){
				bitMapOne.recycle();
			}
			
		}
		if(mLoadScreenShotTwo != null){
			mLoadScreenShotTwo.cancelLoad();
			if(bitMapTwo != null){
				bitMapTwo.recycle();
			}
		}
		
		for(Bitmap bp : bitmapList){
			if(bp != null && !bp.isRecycled()){
				bp.recycle();
				bitmapList.remove(bp);
			}
		}
	}
	private void cancelDownloading(){
		final Dialog promptDialog = new Dialog(
				this, R.style.softDialog);
		View view = LayoutInflater.from(this)
				.inflate(R.layout.soft_uninstall_prompt_dialog,
						null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);
		promptText.setText(R.string.cancel_download_apk);
		Button button_ok = (Button) promptDialog
				.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DownloadService.cancelDownloadTask(mAppInfoForManage.getAppId());
				isDownloading = false;
				promptDialog.dismiss();
			}
		});
		button_cancel
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						promptDialog.dismiss();
					}
				});
		promptDialog.show();
	}
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.installApk) {
			if(isDownloading){
				//cancel downloading
				cancelDownloading();				
			}else{				
				isDownloading = true;
				DownloadTask task = new DownloadTask();
				task.setTitle(mAppInfoForManage.getLabel().toString());
				task.setId(mAppInfoForManage.getAppId());
				if(mAppInfoForManage.getIcon() != null){
					Bitmap bitmap = ((BitmapDrawable) mAppInfoForManage.getIcon())
							.getBitmap();
					task.setIcon(bitmap);
				}
				task.setPackageName(mAppInfoForManage.getPackageName());
				task.setDownUrl(mAppInfoForManage.getPackagePath());
				task.setTotalSize((int)mAppInfoForManage.getSize());
	
				if (DownloadService.addDownTask(task)) {
					Intent i = new Intent(DownloadService.UPDATE_DOWN_TASK);
					i.setClass(this, DownloadService.class);
					startService(i);
				}
				mProgressBar.setVisibility(View.VISIBLE);
				mDownloadingText.setVisibility(View.VISIBLE);
				mHandler.sendEmptyMessage(2);
			}
			updateButton();
		}
	}

	@Override
	protected void onResume() {	
		super.onResume();
		updateButton();				
	}

}
