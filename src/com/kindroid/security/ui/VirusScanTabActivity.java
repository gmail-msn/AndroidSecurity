/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.kindroid.security.R;
import com.kindroid.security.model.TaskInfo;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.DownloadInvoker;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.MultiThreadDownload;
import com.kindroid.security.util.UtilShareprefece;

public class VirusScanTabActivity extends TabActivity implements
		View.OnClickListener, DownloadInvoker {

	private int current;
	public static int step;

	public static TabHost mTabHost;
	private LinearLayout virus_scan_linear;
	private LinearLayout virus_update_linear;
	private LinearLayout virus_history_linear;
	public static TabHost.TabSpec scanSpec;
	public static TabHost.TabSpec historySpec;

	private Dialog checkUpdateDialog = null;
	private static final int CHECK_VIRUS_UPDATE = 1;
	private static final int UPDATE_DOWNLOAD_PERCENT = 3;
	private static final int COMPLETE_DOWNLOAD = 4;
	private static final int DOWNLOAD_ERROR = 5;
	private int newVirusVersion;
	private String virusUpdateDownloadUrl;
	private TaskInfo updateVirusTask;	

	private MultiThreadDownload mtd = null;
	private Dialog updateDialog;
	private ProgressBar updateProgressBar;
	private static boolean isUpdating = false;
	private static boolean mCurrentDisplay = false;
	
	private ImageView one;
	private ImageView two;
	private ImageView three;
	private ImageView four;
	private ImageView five;

	private ImageView one_copy;
	private ImageView two_copy;
	private ImageView three_copy;
	private ImageView four_copy;
	private ImageView five_copy;
	private static boolean isCheckingUpdate = false;
	private boolean mForOptimize = false;
	private static View home_page;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.virus_scan_tab);
		mForOptimize = getIntent().getBooleanExtra(MobileExamActivity.MOBILE_EXAM_OPTIMIZE_INTENT, false);
		home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub	
				if(!mForOptimize){
					Intent homepage = new Intent(VirusScanTabActivity.this,
							DefenderTabMain.class);
					homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(homepage);
					finish();
				}else{
					setResult(MobileExamActivity.RESULT_CODE_FOR_FINISH);
					finish();
				}
			}
		});
		mTabHost = getTabHost();
		scanSpec = mTabHost.newTabSpec("virus_scan");
		scanSpec.setIndicator(getString(R.string.virus_scan_title));
		Intent intentScan = new Intent(this, VirusScanFirstActivity.class);		
		scanSpec.setContent(intentScan);
		mTabHost.addTab(scanSpec);
		current = R.id.virus_scan_linear;
		virus_scan_linear = (LinearLayout) findViewById(R.id.virus_scan_linear);
		virus_update_linear = (LinearLayout) findViewById(R.id.virus_update_linear);
		virus_history_linear = (LinearLayout) findViewById(R.id.virus_history_linear);
		current = R.id.virus_scan_linear;
		virus_scan_linear.setOnClickListener(this);
		virus_update_linear.setOnClickListener(this);
		virus_history_linear.setOnClickListener(this);
		historySpec = mTabHost.newTabSpec("virus_history");
		historySpec.setIndicator(getString(R.string.virus_scan_history_tab));
		Intent intentHistory = new Intent(this, VirusHistoryActivity.class);
		historySpec.setContent(intentHistory);
		mTabHost.addTab(historySpec);
		step = 0;
	}
	public static void showHomeIcon(){
		home_page.setVisibility(View.VISIBLE);
	}
	public static void hideHomeIcon(){
		home_page.setVisibility(View.GONE);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		current = v.getId();
		switch (v.getId()) {
		case R.id.virus_scan_linear:
			virus_scan_linear.setBackgroundResource(R.drawable.linear_focuse);
			virus_update_linear
					.setBackgroundResource(R.drawable.linear_unfocuse);
			virus_history_linear
					.setBackgroundResource(R.drawable.linear_unfocuse);
			Intent intentScan = new Intent(this, VirusScanFirstActivity.class);
			scanSpec.setContent(intentScan);
			mTabHost.setCurrentTab(0);
			if (VirusScanFirstActivity.getStep() > 1) {
				VirusScanFirstActivity.backToPrev();
			}
			break;
		case R.id.virus_update_linear:
			if (VirusScanFirstActivity.isScanning){
				VirusScanFirstActivity.changeTab(this, v);
				break;
			}
			if (!VirusScanFirstActivity.isScanning && !isUpdating) {
				virus_scan_linear
						.setBackgroundResource(R.drawable.linear_unfocuse);
				virus_update_linear
						.setBackgroundResource(R.drawable.linear_focuse);
				virus_history_linear
						.setBackgroundResource(R.drawable.linear_unfocuse);
				checkUpdateDialog = new Dialog(this, R.style.softDialog);
				View view = LayoutInflater.from(this).inflate(
						R.layout.softmanage_prompt_dialog, null);
				one = (ImageView) view.findViewById(R.id.pr_one);
				two = (ImageView) view.findViewById(R.id.pr_two);
				three = (ImageView) view.findViewById(R.id.pr_three);
				four = (ImageView) view.findViewById(R.id.pr_four);
				five = (ImageView) view.findViewById(R.id.pr_five);

				one_copy = (ImageView) view.findViewById(R.id.pr_one_copy);
				two_copy = (ImageView) view.findViewById(R.id.pr_two_copy);
				three_copy = (ImageView) view.findViewById(R.id.pr_three_copy);
				four_copy = (ImageView) view.findViewById(R.id.pr_four_copy);
				five_copy = (ImageView) view.findViewById(R.id.pr_five_copy);
				checkUpdateDialog.setContentView(view);
				TextView prompt_dialog_text = (TextView)checkUpdateDialog.findViewById(R.id.prompt_dialog_text);
				prompt_dialog_text.setText(R.string.checking_update);
				checkUpdateDialog.show();

				CheckUpdateThread cut = new CheckUpdateThread(
						CHECK_VIRUS_UPDATE);
				cut.start();
				new LoadingItem().start();
			} else if(isUpdating){
				if (updateDialog != null) {
					updateDialog.show();
				}
			}else{
				Toast.makeText(this, R.string.virus_update_when_scanning,
						Toast.LENGTH_LONG);
			}

			break;
		case R.id.virus_history_linear:
			if (VirusScanFirstActivity.isScanning){
				VirusScanFirstActivity.changeTab(this, v);
				break;
			}
			virus_scan_linear.setBackgroundResource(R.drawable.linear_unfocuse);
			virus_update_linear.setBackgroundResource(R.drawable.linear_unfocuse);
			virus_history_linear
					.setBackgroundResource(R.drawable.linear_focuse);
			mTabHost.setCurrentTab(1);
			break;
		}
	}
	private class LoadingItem extends Thread {
		public void run() {
			do {
				for (int j = 0; j < 5; j++) {
					try {
						sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mProgressHandler.sendEmptyMessage(j);
				}
			} while (isCheckingUpdate);

		}
	}

	private Handler mProgressHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			one.setVisibility(View.VISIBLE);
			two.setVisibility(View.VISIBLE);
			three.setVisibility(View.VISIBLE);
			four.setVisibility(View.VISIBLE);
			five.setVisibility(View.VISIBLE);
			
			one_copy.setVisibility(View.INVISIBLE);
			two_copy.setVisibility(View.INVISIBLE);
			three_copy.setVisibility(View.INVISIBLE);
			four_copy.setVisibility(View.INVISIBLE);
			five_copy.setVisibility(View.INVISIBLE);

			switch (msg.what) {
			case 0:
				one.setVisibility(View.INVISIBLE);
				one_copy.setVisibility(View.VISIBLE);
				break;
			case 1:
				two.setVisibility(View.INVISIBLE);
				two_copy.setVisibility(View.VISIBLE);
				break;
			case 2:
				three.setVisibility(View.INVISIBLE);
				three_copy.setVisibility(View.VISIBLE);
				break;
			case 3:
				four.setVisibility(View.INVISIBLE);
				four_copy.setVisibility(View.VISIBLE);
				break;
			case 4:
				five.setVisibility(View.INVISIBLE);
				five_copy.setVisibility(View.VISIBLE);
				break;

			}
		}
	};

	private void startVirusUpdate() {
		if(isUpdating)
			return;
		Dialog d = getPromotDialog(this, 4);
		d.show();
		updateVirusTask = new TaskInfo();
		updateVirusTask.setUrlStr(virusUpdateDownloadUrl);
		updateVirusTask.setFileName("Malware" + newVirusVersion + ".db");
		File dir = new File("/data/data/com.kindroid.security/tmp/");
		if(dir.exists())
			dir.delete();
		
		updateVirusTask.setSavePath("/data/data/com.kindroid.security/tmp/");
		updateVirusTask.setThreadNum(1);
		isUpdating = true;
		startDownload(updateVirusTask);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub		
		super.onResume();
		mCurrentDisplay = true;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		mCurrentDisplay = false;
		super.onPause();
	}

	private void startDownload(TaskInfo taskInfo) {
		mtd = new MultiThreadDownload(taskInfo, this);
		mtd.start();
	}

	public Dialog getPromotDialog(final Context context, int type) {

		switch (type) {
		case 1:
			final Dialog promptDialog = new Dialog(this, R.style.softDialog);
			View view = LayoutInflater.from(this).inflate(
					R.layout.soft_uninstall_prompt_dialog, null);
			promptDialog.setContentView(view);

			TextView promptText = (TextView) promptDialog
					.findViewById(R.id.prompt_text);
			promptText.setText(R.string.update_virus_no_new_version_text);
			View button_ok = promptDialog.findViewById(R.id.button_ok);
			View button_cancel = promptDialog.findViewById(R.id.button_cancel);
			button_cancel.setVisibility(View.GONE);
			button_ok.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog.dismiss();

				}
			});
			
			updateDialog = promptDialog;
			break;
		case 2:
			final Dialog promptDialog1 = new Dialog(this, R.style.softDialog);
			view = LayoutInflater.from(this).inflate(
					R.layout.soft_uninstall_prompt_dialog, null);
			promptDialog1.setContentView(view);

			promptText = (TextView) promptDialog1
					.findViewById(R.id.prompt_text);
			promptText.setText(R.string.update_virus_has_new_version_text);
			button_ok = promptDialog1.findViewById(R.id.button_ok);
			button_cancel = promptDialog1.findViewById(R.id.button_cancel);
			button_ok.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog1.dismiss();
					startVirusUpdate();

				}
			});
			button_cancel.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					promptDialog1.dismiss();
				}
			});
			updateDialog = promptDialog1;
			break;
		case 3:
			final Dialog promptDialog2 = new Dialog(this, R.style.softDialog);
			view = LayoutInflater.from(this).inflate(
					R.layout.soft_uninstall_prompt_dialog, null);
			promptDialog2.setContentView(view);

			promptText = (TextView) promptDialog2
					.findViewById(R.id.prompt_text);
			promptText.setText(R.string.virus_update_network_error_text);
			button_ok = promptDialog2.findViewById(R.id.button_ok);
			button_cancel = promptDialog2.findViewById(R.id.button_cancel);
			button_ok.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog2.dismiss();					
				}
			});
			button_cancel.setVisibility(View.GONE);
			
			updateDialog = promptDialog2;
			break;
		case 4:
			final Dialog promptDialog3 = new Dialog(this, R.style.softDialog){
				@Override
				public void onBackPressed() {
					// TODO Auto-generated method stub
					if (isUpdating) {
						final Dialog promptDialog1 = new Dialog(
								VirusScanTabActivity.this, R.style.softDialog);
						View view = LayoutInflater
								.from(VirusScanTabActivity.this)
								.inflate(
										R.layout.soft_uninstall_prompt_dialog,
										null);
						promptDialog1.setContentView(view);

						TextView promptText = (TextView) promptDialog1
								.findViewById(R.id.prompt_text);

						promptText.setText(R.string.cancel_virus_update);

						Button button_ok = (Button) promptDialog1
								.findViewById(R.id.button_ok);
						Button button_cancel = (Button) promptDialog1
								.findViewById(R.id.button_cancel);
						button_cancel
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										promptDialog1.dismiss();
									}
								});
						button_ok
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										stopDownload();
										isUpdating = false;
										dismiss();
										promptDialog1.dismiss();
									}
								});
						promptDialog1.show();
					} else {
						super.onBackPressed();
					}
				}

			};
			view = LayoutInflater.from(this).inflate(
					R.layout.softmanage_downloading_prompt, null);
			promptDialog3.setContentView(view);

			promptText = (TextView) promptDialog3
					.findViewById(R.id.prompt_dialog_text);
			promptText.setText(R.string.virus_update_prompt_text);
			updateProgressBar = (ProgressBar) promptDialog3.findViewById(R.id.prompt_dialog_progress);
			
			updateDialog = promptDialog3;
			break;
		case 5:
			final Dialog promptDialog5 = new Dialog(this, R.style.softDialog);
			view = LayoutInflater.from(this).inflate(
					R.layout.soft_uninstall_prompt_dialog, null);
			promptDialog5.setContentView(view);

			promptText = (TextView) promptDialog5
					.findViewById(R.id.prompt_text);
			promptText.setText(R.string.download_error_prompt);
			button_ok = promptDialog5.findViewById(R.id.button_ok);
			button_cancel = promptDialog5.findViewById(R.id.button_cancel);
			button_cancel.setVisibility(View.GONE);
			button_ok.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog5.dismiss();

				}
			});
			
			updateDialog = promptDialog5;
			break;
		}

		return updateDialog;
	}

	private void stopDownload() {
		if (mtd != null)
			mtd.stopDownload();
		
	}

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (checkUpdateDialog != null)
				checkUpdateDialog.dismiss();
			switch (msg.arg1) {
			case CHECK_VIRUS_UPDATE:
				checkUpdateDialog.dismiss();
				if (msg.arg2 == 0 && mCurrentDisplay) {
					SharedPreferences sp = KindroidSecurityApplication.sh;
					Editor editor = sp.edit();
					editor.putLong(UtilShareprefece.LAST_UPDATE_TIME, System.currentTimeMillis());
					editor.commit();
					VirusScanFirstActivity.completeUpdate();
					Dialog d = getPromotDialog(VirusScanTabActivity.this, 1);
					d.show();
				} else if (msg.arg2 == 1 && mCurrentDisplay) {
					Dialog d = getPromotDialog(VirusScanTabActivity.this, 2);
					d.show();
				} else if (msg.arg2 == 3 && mCurrentDisplay) {
					Dialog d = getPromotDialog(VirusScanTabActivity.this, 3);
					d.show();
				}				
				break;
			case UPDATE_DOWNLOAD_PERCENT:
				updateProgressBar.setProgress(msg.arg2);
				break;
			case COMPLETE_DOWNLOAD:
				File src = new File("/data/data/com.kindroid.security/tmp/Malware"
						+ newVirusVersion + ".db");
				if (src.exists()) {
					boolean sucess = true;
					File dbFile = getApplication()
							.getDatabasePath("Malware.db");
					String dbPath = "/data/data/net.kindroid.security/databases/Malware.db";
					if (dbFile != null && dbFile.exists()) {
						dbPath = getApplication().getDatabasePath("Malware.db")
								.getAbsolutePath();
					}
					String path = dbPath.substring(0, dbPath.lastIndexOf('/'));
					File dest = new File(path, src.getName());
					FileInputStream fis = null;
					FileOutputStream fos = null;
					try {
						fis = new FileInputStream(src);
						fos = new FileOutputStream(dest);
						byte buffer[] = new byte[1024];
						int len = fis.read(buffer);
						while (len != -1) {
							fos.write(buffer);
							len = fis.read(buffer);
						}
					} catch (Exception e) {
						sucess = false;
						e.printStackTrace();
					} finally {
						if (fis != null) {
							try {
								fis.close();
							} catch (Exception e) {

							}
						}
						if (fos != null) {
							try {
								fos.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					if (sucess) {
						try{
							if (dbFile.exists())
								dbFile.delete();
							dest.renameTo(new File(dbPath));
							src.delete();
						}catch(Exception e){
							e.printStackTrace();
						}
						SharedPreferences sp = KindroidSecurityApplication.sh;
						Editor editor = sp.edit();
						editor.putInt("virusVersion", newVirusVersion);
						editor.putLong(UtilShareprefece.LAST_UPDATE_TIME, System.currentTimeMillis());
						editor.commit();
						VirusScanFirstActivity.completeUpdate();
					}
				}
				if (updateDialog != null) {
					updateDialog.dismiss();
				}
				isUpdating = false;
				break;
			case DOWNLOAD_ERROR:
				if (updateDialog != null) {
					updateDialog.dismiss();
				}
				if(mCurrentDisplay){
					Dialog d = getPromotDialog(VirusScanTabActivity.this, 5);
					d.show();
				}
				break;
			}

		}
	};

	private class CheckUpdateThread extends Thread {

		private int flag;

		CheckUpdateThread(int flag) {
			this.flag = flag;
		}

		public void run() {
			isCheckingUpdate = true;
			if (flag == CHECK_VIRUS_UPDATE) {
				HttpURLConnection urlConn = null;
				InputStreamReader in = null;
				try {
					URL url = new URL(Constant.virusUpdateCheckUrl);
					urlConn = (HttpURLConnection) url.openConnection();
					urlConn.setConnectTimeout(10000);
					urlConn.setReadTimeout(10000);
					in = new InputStreamReader(urlConn.getInputStream());
					BufferedReader buffer = new BufferedReader(in);
					String result = buffer.readLine();
					
					buffer.close();
					in.close();
					urlConn.disconnect();
					JSONObject jso = new JSONObject(result);
					boolean hasNewVersion = false;
					SharedPreferences sp = KindroidSecurityApplication.sh;
					int oldVirusVersion = sp.getInt("virusVersion", 201100805);

					if (jso.getInt("version") > oldVirusVersion) {
						newVirusVersion = jso.getInt("version");
						virusUpdateDownloadUrl = jso.getString("upgradePath");
						hasNewVersion = true;
					}
					
					Message msg = new Message();
					msg.arg1 = CHECK_VIRUS_UPDATE;

					if (hasNewVersion)
						msg.arg2 = 1;
					else
						msg.arg2 = 0;
					handler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
					Message msg = new Message();
					msg.arg1 = CHECK_VIRUS_UPDATE;
					msg.arg2 = 3;
					handler.sendMessage(msg);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (urlConn != null) {
						try {
							urlConn.disconnect();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				isCheckingUpdate = false;
				return;

			}

		}
	}

	@Override
	public void updateProgress(MultiThreadDownload mtd, int downloadPercent) {
		// TODO Auto-generated method stub
		Message msg = new Message();
		msg.arg1 = UPDATE_DOWNLOAD_PERCENT;
		msg.arg2 = downloadPercent;
		handler.sendMessage(msg);
	}

	@Override
	public void downloadError(MultiThreadDownload mtd, String message) {
		// TODO Auto-generated method stub
		Message msg = new Message();
		msg.arg1 = DOWNLOAD_ERROR;
		handler.sendMessage(msg);
	}

	@Override
	public void downloadCompleted(MultiThreadDownload mtd) {
		// TODO Auto-generated method stub
		Message msg = new Message();
		msg.arg1 = COMPLETE_DOWNLOAD;
		handler.sendMessage(msg);
	}

}
