/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;
import java.util.List;

import com.ehoo.client.util.Config;
import com.ehoo.client.request.IRequest;
import com.ehoo.client.request.Request;
import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.util.CommonProtoc.BannerType;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.RefreshAnimationThread;
import com.kindroid.security.util.Utilis;

public class SoftCenterRecommendActivity extends ListActivity {
	private SoftCenterListAdapter listAdapter;
	private boolean isLoadingData = false;

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
	private static final int FINISH_LOADING = 0;
	private static final int NETWORK_ERROR = 1;
	private static final int GET_AD_DRAWABLES = 2;

	private boolean sCancelLoading = false;

	public boolean mBufferExist = true;
	public boolean mUpdateBuffer = false;

	public static final String AD_DRAWABLE_PATH = "recommend_ad";
	private RefreshAnimationThread mRefreshAnimThread;
	private ImageView recommend_soft_image;
	public boolean hasAdBuffer = false;
	private boolean mUpdated = false;

	public static int mScreenWidth = 0;
	public static int mScreenHeight = 0;

	private static final String KINDROID_SECURITY_DIR = "/Kindroid/Security/";

	private boolean mCacheUpdated = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.softcenter_recommend);
		View headerView = getLayoutInflater().inflate(R.layout.ad_layout, null);
		getListView().addHeaderView(headerView, null, false);
		listAdapter = new SoftCenterListAdapter(this);
		startDisplayAD();

		loadListAdapter();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		for (int i = 0; i < listAdapter.getCount(); i++) {			
			try {
				AppInfoForManage aifm = (AppInfoForManage) listAdapter.getItem(i);
				if (aifm == null || aifm == RecommendAppDetail.mAppInfoForManage) {
					continue;
				}
				BitmapDrawable drw = (BitmapDrawable) aifm.getIcon();
				if (drw == null) {
					continue;
				}
				if(drw.getBitmap() != null && !drw.getBitmap().isRecycled()){
					drw.getBitmap().recycle();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == 19){
			if(recommend_soft_image != null){
				recommend_soft_image.setClickable(true);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		AppInfoForManage appInfoForManage = (AppInfoForManage) listAdapter
				.getItem(position - 1);
		RecommendAppDetail.mAppInfoForManage = appInfoForManage;
		Intent intent = new Intent(this, RecommendAppDetail.class);
		startActivity(intent);
	}

	private void startDisplayAD() {
		// get animation resources from server
		File backupPath = getDir("backup", Context.MODE_PRIVATE);
		if (!backupPath.exists()) {
			try {
				backupPath.mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		File animFile = new File(backupPath, AD_DRAWABLE_PATH);
		if (!animFile.exists()) {
			animFile.mkdirs();
		}
		recommend_soft_image = (ImageView) findViewById(R.id.recommend_soft_image);

		mRefreshAnimThread = new RefreshAnimationThread(this,
				animFile.getAbsolutePath(), recommend_soft_image, mHandler,
				BannerType.PROMOTION_BANNER, -1, GET_AD_DRAWABLES);
		mRefreshAnimThread.startRefresh(this);
		if (!mUpdated) {
			mRefreshAnimThread.start();
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!isLoadingData && listAdapter.isEmpty()) {
			listAdapter.clearItems();
			listAdapter.notifyDataSetChanged();
			loadListAdapter();
		} 
		/*
		else if(!isLoadingData && mCacheUpdated){
			listAdapter.clearItems();
			listAdapter.notifyDataSetChanged();
			loadListAdapter();		
		}
		*/
		else if(!isLoadingData){
			refreshListState();
		}else if(isLoadingData){
			getListView().setVisibility(View.GONE);
			View loading_linear = findViewById(R.id.loading_linear);
			loading_linear.setVisibility(View.VISIBLE);
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}

	private void refreshListState() {
		runOnUiThread(new Runnable() {
			public void run() {
				PackageManager pm = getPackageManager();
				for (int i = 0; i < listAdapter.getCount(); i++) {
					AppInfoForManage aifm = (AppInfoForManage) listAdapter
							.getItem(i);
					try {
						PackageInfo pi = pm.getPackageInfo(
								aifm.getPackageName(),
								PackageManager.GET_ACTIVITIES);
						if (pi != null)
							aifm.setInstalled(true);
					} catch (NameNotFoundException e) {
						aifm.setInstalled(false);
					}
				}
				listAdapter.notifyDataSetChanged();
			}
		});
	}

	private void loadListAdapter() {		
		getListView().setVisibility(View.GONE);
		View loading_linear = findViewById(R.id.loading_linear);
		loading_linear.setVisibility(View.VISIBLE);

		LoadAdapterThread mLoadingThread = new LoadAdapterThread();
		sCancelLoading = false;
		one = (ImageView) findViewById(R.id.pr_one);
		two = (ImageView) findViewById(R.id.pr_two);
		three = (ImageView) findViewById(R.id.pr_three);
		four = (ImageView) findViewById(R.id.pr_four);
		five = (ImageView) findViewById(R.id.pr_five);

		one_copy = (ImageView) findViewById(R.id.pr_one_copy);
		two_copy = (ImageView) findViewById(R.id.pr_two_copy);
		three_copy = (ImageView) findViewById(R.id.pr_three_copy);
		four_copy = (ImageView) findViewById(R.id.pr_four_copy);
		five_copy = (ImageView) findViewById(R.id.pr_five_copy);

		TextView tv = (TextView) findViewById(R.id.prompt_dialog_text);
		tv.setText(R.string.softmanage_scan_recommend_text);

		isLoadingData = true;
		new LoadingItem().start();
		mLoadingThread.start();
	}

	private class LoadAdapterThread extends Thread {
		private static final String mBufferDBName = "SoftcenterBuffer.db";

		private String getBufferFilePath() {
			String ret = null;
			if (!Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File backupPath = getDir("backup", Context.MODE_PRIVATE);
				if (!backupPath.exists()) {
					return null;
				}
				ret = backupPath.getAbsolutePath() + "/" + mBufferDBName;
			} else {
				File bFile = new File(
						Environment.getExternalStorageDirectory(),
						KINDROID_SECURITY_DIR + mBufferDBName);
				if (!bFile.exists()) {
					try {
						File dir = new File(
								Environment.getExternalStorageDirectory(),
								KINDROID_SECURITY_DIR);
						dir.mkdirs();
						boolean r = bFile.createNewFile();
						if (!r) {
							return null;
						} else {
							ret = bFile.getAbsolutePath();
						}
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				} else {
					ret = bFile.getAbsolutePath();
				}

			}
			return ret;
		}

		private boolean readFromBuffer() {
			boolean ret = false;

			String bFilePath = getBufferFilePath();
			if (bFilePath == null) {
				return false;
			}
			SQLiteDatabase localSQLiteDatabase = null;

			try {
				localSQLiteDatabase = SQLiteDatabase.openDatabase(bFilePath,
						null, 1);
			} catch (Exception e) {
				e.printStackTrace();
				localSQLiteDatabase = null;
			}

			if (localSQLiteDatabase == null) {
				return false;
			}

			Cursor localCursor = localSQLiteDatabase.query("soft_buffer", null,
					null, null, null, null, null);
			if (localCursor.getCount() <= 0 || sCancelLoading) {
				try {
					localCursor.close();
					localSQLiteDatabase.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
			PackageManager pm = getPackageManager();
			List<PackageInfo> packInfos = pm
					.getInstalledPackages(PackageManager.GET_SIGNATURES);
			int c = 0;
			while (localCursor.moveToNext() && !sCancelLoading) {
				AppInfoForManage aifm = new AppInfoForManage();
				aifm.setAppId(localCursor.getString(localCursor
						.getColumnIndex("id")));
				aifm.setLabel(localCursor.getString(localCursor
						.getColumnIndex("label")));
				aifm.setPackagePath(localCursor.getString(localCursor
						.getColumnIndex("packagePath")));
				byte[] iconBlob = localCursor.getBlob(localCursor
						.getColumnIndex("icon"));

				aifm.setIcon(new BitmapDrawable(BitmapFactory.decodeByteArray(
						iconBlob, 0, iconBlob.length)));
				aifm.setVersion(localCursor.getString(
						localCursor.getColumnIndex("version")).concat(
						getString(R.string.softmanage_version_title)));
				aifm.setSize(localCursor.getInt(localCursor
						.getColumnIndex("size")));
				if (localCursor.getColumnIndex("price") != -1
						&& localCursor.getInt(localCursor
								.getColumnIndex("price")) > 0) {
					aifm.setUnit(localCursor.getString(localCursor
							.getColumnIndex("price"))
							+ localCursor.getString(localCursor
									.getColumnIndex("priceunit")));
				} else {
					aifm.setUnit(getString(R.string.softcenter_search_my_free));
				}
				aifm.setPackageName(localCursor.getString(localCursor
						.getColumnIndex("packageName")));
				aifm.setDescription(localCursor.getString(localCursor
						.getColumnIndex("description")));
				if (localCursor.getColumnIndex("versionCode") != -1) {
					aifm.setFlag(localCursor.getInt(localCursor
							.getColumnIndex("versionCode")));
				} else {
					aifm.setFlag(-1);
				}
				try {
					PackageInfo pi = pm.getPackageInfo(aifm.getPackageName(),
							PackageManager.GET_ACTIVITIES);
					if (pi != null)
						aifm.setInstalled(true);
				} catch (NameNotFoundException e) {
					aifm.setInstalled(false);
				}

				listAdapter.addItem(aifm);
				c++;
			}
			try {
				localCursor.close();
				localSQLiteDatabase.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (sCancelLoading) {
				listAdapter.clearItems();
				return false;
			}
			if (c > 0) {
				ret = true;
			} else {
				ret = false;
			}
			return ret;
		}

		private void loadFromBuffer() {
			PackageManager pm = getPackageManager();
			// IRequest request = SoftCenterTabActivity.request;
			synchronized (SoftCenterTabActivity.initReuqest) {
				if (SoftCenterTabActivity.initReuqest == false) {
					SoftCenterTabActivity.request = new Request();
					SoftCenterTabActivity.request.init(true);
					SoftCenterTabActivity.initReuqest = true;
					
				}
			}
			int mIndex = 0;
			int mMaxRetyTimes = 10;
			while (true && !sCancelLoading && mIndex < mMaxRetyTimes) {
				long expiredTime = 0;
				expiredTime = Config.getInt("cache.expired") + (new Date()).getTime();
				byte[] bytes = SoftCenterTabActivity.request.get(
						Constant.RECOMMEND_URL + mIndex * 10 + "/" + 10, 'r');
				if (bytes == null) {
					mMaxRetyTimes = 3;
					mIndex++;
					continue;
				}
				try {
					JSONObject jso = new JSONObject(new String(bytes));
					int result = jso.getInt("result");

					if (result == 0) {
						JSONArray jArray = jso.getJSONArray("app");
						if (jArray.length() <= 0) {
							break;
						}

						for (int i = 0; i < jArray.length(); i++) {
							JSONObject appInfo = jArray.getJSONObject(i);
							String iconStr = appInfo.getString("icon");
							bytes = Base64.decodeBase64(iconStr.getBytes());

							AppInfoForManage aifm = new AppInfoForManage();
							aifm.setAppId(appInfo.getString("appID"));
							aifm.setLabel(appInfo.getString("title"));
							aifm.setPackagePath(appInfo
									.getString("downloadUrl"));

							aifm.setVersion(appInfo
									.getString("versionName")
									.concat(getString(R.string.softmanage_version_title)));
							aifm.setSize(appInfo.getInt("installsize"));
							aifm.setPartnerId(appInfo.getInt("partnerID"));
							if (appInfo.get("price") != null
									&& Integer.parseInt(appInfo.get("price")
											.toString()) > 0) {
								aifm.setUnit(appInfo.getString("price")
										+ appInfo.getString("priceunit"));
							} else {
								aifm.setUnit(getString(R.string.softcenter_search_my_free));
							}
							aifm.setDescription(appInfo
									.getString("description"));

							aifm.setPackageName(appInfo
									.getString("packageName"));
							if (appInfo.isNull("versioncode")) {
								aifm.setFlag(-1);
							} else {
								aifm.setFlag(appInfo.getInt("versioncode"));
							}
							try {
								PackageInfo pi = pm.getPackageInfo(
										aifm.getPackageName(),
										PackageManager.GET_ACTIVITIES);
								if (pi != null)
									aifm.setInstalled(true);
							} catch (NameNotFoundException e) {
								aifm.setInstalled(false);
							}
							aifm.setIcon(new BitmapDrawable(BitmapFactory
									.decodeByteArray(bytes, 0, bytes.length)));
							listAdapter.addItem(aifm);

						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				mIndex++;

			}
			mHandler.sendEmptyMessage(FINISH_LOADING);
		}

		public void run() {
			loadFromBuffer();			
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FINISH_LOADING:
				setListAdapter(listAdapter);
				isLoadingData = false;
				View loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				if (mRefreshAnimThread != null) {
					mHandler.postDelayed(new Runnable() {
						public void run() {
							mRefreshAnimThread
									.startRefresh(SoftCenterRecommendActivity.this);
						}
					}, 1000);
				}
				break;
			case NETWORK_ERROR:
				isLoadingData = false;
				loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				Toast.makeText(SoftCenterRecommendActivity.this,
						R.string.bakcup_remote_network_unabailable_text,
						Toast.LENGTH_LONG).show();
				break;
			case GET_AD_DRAWABLES:
				mUpdated = true;
				mRefreshAnimThread
						.startRefresh(SoftCenterRecommendActivity.this);
				break;
			}
		}
	};

	private class LoadingItem extends Thread {
		public void run() {
			do {
				for (int j = 0; j < 5; j++) {
					if (!isLoadingData) {
						break;
					}
					try {
						sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					mProgressHandler.sendEmptyMessage(j);
				}
			} while (isLoadingData);

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

}
