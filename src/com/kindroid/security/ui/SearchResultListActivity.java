/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.util.Appstore;
import com.kindroid.security.util.Appstore.Icon;
import com.kindroid.security.util.AppProtoc;
import com.kindroid.security.util.AppProtoc.App;
import com.kindroid.security.util.Base64Handler;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.HttpRequestUtil;
import com.kindroid.security.util.RequestProtoc;
import com.kindroid.security.util.ResponseProtoc;
import com.kindroid.security.util.Utilis;
import com.kindroid.security.util.ResponseProtoc.ResponseContext;

public class SearchResultListActivity extends ListActivity {
	private SoftCenterListAdapter listAdapter;
	private boolean isLoadingData = false;
	// progress animation image
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.softcenter_search_result_list);
		
		listAdapter = new SoftCenterListAdapter(this);
		if (!isLoadingData) {
			if (!Utilis.checkNetwork(this)) {
				Toast.makeText(this,
						R.string.bakcup_remote_network_unabailable_text,
						Toast.LENGTH_LONG).show();
			} else {				
				listAdapter.clearItems();
				listAdapter.notifyDataSetChanged();
				loadListAdapter();
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();	
		if(listAdapter != null && listAdapter.getCount() > 0){
			refreshListState();
		}
		
	}
	private void refreshListState() {
		PackageManager pm = getPackageManager();
		for (int i = 0; i < listAdapter.getCount(); i++) {
			AppInfoForManage aifm = (AppInfoForManage) listAdapter.getItem(i);	
			
			try {
				PackageInfo pi = pm.getPackageInfo(aifm.getPackageName(),
						PackageManager.GET_ACTIVITIES);
				if (pi != null)
					aifm.setInstalled(true);
			} catch (NameNotFoundException e) {
				aifm.setInstalled(false);
			}
		}
		listAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();				
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		AppInfoForManage appInfoForManage = (AppInfoForManage) listAdapter
				.getItem(position);
		RecommendAppDetail.mAppInfoForManage = appInfoForManage;
		Intent intent = new Intent(this, RecommendAppDetail.class);
		startActivity(intent);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FINISH_LOADING:
				isLoadingData = false;
				View loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				if(listAdapter.getCount() > 0){
					findViewById(R.id.search_empty_text).setVisibility(View.GONE);
					SearchResultListActivity.this.getListView().setVisibility(View.VISIBLE);
					setListAdapter(listAdapter);
					listAdapter.notifyDataSetChanged();
				}else{
					findViewById(R.id.search_empty_text).setVisibility(View.VISIBLE);
					SearchResultListActivity.this.getListView().setVisibility(View.GONE);
				}
				
				break;
			case NETWORK_ERROR:
				isLoadingData = false;
				loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				Toast.makeText(SearchResultListActivity.this,
						R.string.bakcup_remote_network_unabailable_text,
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	private class LoadAdapterThread extends Thread {

		public void run() {
			String keyWord = getIntent().getStringExtra(
					SoftCenterSearchActivity.SEARCH_KEY_WORD);
			
			if (keyWord == null || keyWord.trim().equals("")) {
				mHandler.sendEmptyMessage(FINISH_LOADING);
				return;
			}
			AppProtoc.AppSearchRequest.Builder searchRequestBuilder = AppProtoc.AppSearchRequest
					.newBuilder();
			searchRequestBuilder.setQuery(keyWord);
			searchRequestBuilder.setStartIndex(0);
			searchRequestBuilder.setEntriesCount(15);
			RequestProtoc.Request.Builder request = RequestProtoc.Request
					.newBuilder();
			request.setAppSearchRequest(searchRequestBuilder);

			Base64Handler base64 = new Base64Handler();
			BufferedReader mReader = null;
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet();
			int ret = -1;
			try {
				/*
				URI url = new URI(Constant.SEARCH_URL_NEW
						+ base64.encode(request.build().toByteArray()));
				httpGet.setURI(url);
				HttpResponse response = client.execute(httpGet);

				StringBuffer mBuffer = new StringBuffer();
				mReader = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				String line = mReader.readLine();
				while (line != null) {
					mBuffer.append(line);
					line = mReader.readLine();
				}
				 */
				HashMap<String, String> param = new HashMap<String, String>();
				param.put("request", base64.encode(request.build().toByteArray()));
				Map<String, Object> res = HttpRequestUtil.postData(
						Constant.SEARCH_URL_NEW, param, 10000);
				
				ResponseProtoc.Response resp = ResponseProtoc.Response
						.parseFrom(base64.decodeBuffer(new String((byte[])res.get("Content"))));
				if (resp.hasContext()) {
					ResponseContext rc = resp.getContext();
					ret = rc.getResult();
				}
				if (ret == 0) {
					PackageManager pm = getPackageManager();
					List<PackageInfo> mPackInfos = getPackageManager().getInstalledPackages(PackageManager.GET_SIGNATURES);
					
					AppProtoc.AppSearchResponse searchResponse = resp
							.getAppSearchResponse();
					int entriesCount = searchResponse.getEntriesCount();
					
					if (entriesCount > 0) {
						List<App> appsList = searchResponse.getAppList();
						for (App app : appsList) {
							AppInfoForManage aifm = new AppInfoForManage();
							aifm.setAppId(String.valueOf(app.getId()));
							aifm.setSize(app.getInstallSize());
							aifm.setPackageName(app.getPackageName());
							aifm.setPartnerId(app.getPartnerID());
							aifm.setLabel(app.getTitle());
							aifm.setVersion(app
									.getVersionname()
									.concat(getString(R.string.softmanage_version_title)));
							if (app.getPrice() > 0) {
								aifm.setUnit(app.getPrice()
										+ app.getPriceUnit());
							} else {
								aifm.setUnit(getString(R.string.softcenter_search_my_free));
							}
							aifm.setDescription(app.getDescription());
							aifm.setPackagePath(app.getDownloadUrl());
							aifm.setIcon(Drawable.createFromStream(new ByteArrayInputStream(app.getIconData().toByteArray()), null));
							aifm.setFlag(app.getVersioncode());							
							try {
								PackageInfo pi = pm.getPackageInfo(aifm.getPackageName(),
										PackageManager.GET_ACTIVITIES);
								if (pi != null)
									aifm.setInstalled(true);
							} catch (NameNotFoundException e) {
								aifm.setInstalled(false);
							}
							
							listAdapter.addItem(aifm);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (mReader != null) {
					try {
						mReader.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			mHandler.sendEmptyMessage(FINISH_LOADING);
		}
	}

	private void loadListAdapter() {
		getListView().setVisibility(View.GONE);
		View loading_linear = findViewById(R.id.loading_linear);
		loading_linear.setVisibility(View.VISIBLE);
		
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
		tv.setText(R.string.searching_prompt_text);
		isLoadingData = true;
		LoadAdapterThread mLoadingThread = new LoadAdapterThread();
		mLoadingThread.start();
		new LoadingItem().start();

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
