/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.util.Appstore;
import com.kindroid.security.util.Appstore.Icon;
import com.kindroid.security.util.AppProtoc.App;
import com.kindroid.security.util.CommonProtoc.BannerType;
import com.kindroid.security.util.Base64Handler;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.RefreshAnimationThread;
import com.kindroid.security.util.RequestProtoc;
import com.kindroid.security.util.ResponseProtoc;
import com.kindroid.security.util.TopicProtoc;
import com.kindroid.security.util.Utilis;
import com.kindroid.security.util.ResponseProtoc.ResponseContext;

public class SoftCenterTopicListActivity extends ListActivity {
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
	private static final int GET_AD_DRAWABLES = 2;
	
	private static boolean sCancelLoading = false;
	private static int topicID = 0;
	public boolean mUpdateBuffer = false;
	
	private static final String AD_DRAWABLE_PATH_PRFIX = "topic_ad_";
	private RefreshAnimationThread mRefreshAnimThread;
	private ImageView recommend_soft_image;
	public boolean hasAdBuffer = false;

	private boolean mIsCategory = false;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.topic_app_list);
		View headerView = getLayoutInflater().inflate(R.layout.ad_layout, null);
		getListView().addHeaderView(headerView, null, false);
		listAdapter = new SoftCenterListAdapter(this);		
		
		TextView function_title_tv = (TextView) findViewById(R.id.function_title_tv);
		// set title from intent
		String topicName = getIntent().getStringExtra(SoftCenterTopicsActivity.TOPIC_NAME);
		if(topicName != null && !topicName.trim().equals("")){
			function_title_tv.setText(topicName);
		}
		topicID = getIntent().getIntExtra(SoftCenterTopicsActivity.TOPIC_ID, 0);
		startDisplayAD();
		
		if (!isLoadingData) {
			if(listAdapter != null){
				listAdapter.clearItems();
				listAdapter.notifyDataSetChanged();
			}
			
			loadListAdapter();
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
	private void startDisplayAD(){
		//get animation resources from server
		File backupPath = getDir("backup", Context.MODE_PRIVATE);
		if (!backupPath.exists()){
			try{
				backupPath.mkdirs();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		File animFile = new File(backupPath, AD_DRAWABLE_PATH_PRFIX + topicID);
		if(!animFile.exists()){
			animFile.mkdirs();
		}
		recommend_soft_image = (ImageView)findViewById(R.id.recommend_soft_image);
		
		mRefreshAnimThread = new RefreshAnimationThread(this, animFile.getAbsolutePath(), recommend_soft_image, mHandler, BannerType.TOPIC_BANNER, topicID, GET_AD_DRAWABLES);
		mRefreshAnimThread.startRefresh(this);
		if(!mUpdateBuffer){
			mRefreshAnimThread.start();
		}
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();		
		if (!isLoadingData && (listAdapter == null || listAdapter.isEmpty())) {			
			listAdapter.clearItems();
			listAdapter.notifyDataSetChanged();
			loadListAdapter();
		}else if(listAdapter.getCount() > 1){
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		AppInfoForManage appInfoForManage = (AppInfoForManage) listAdapter
				.getItem(position - 1);
		RecommendAppDetail.mAppInfoForManage = appInfoForManage;
		Intent intent = new Intent(this, RecommendAppDetail.class);
		startActivity(intent);
	}

	private void loadListAdapter() {
		/*
		if(!Utilis.checkNetwork(this)){
			Toast.makeText(this, R.string.bakcup_remote_network_unabailable_text, Toast.LENGTH_LONG).show();
			return;
		}
		*/
		getListView().setVisibility(View.GONE);
		View loading_linear = findViewById(R.id.loading_linear);
		loading_linear.setVisibility(View.VISIBLE);
		
		sCancelLoading = false;
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
		
		TextView tv = (TextView) findViewById(R.id.prompt_dialog_text);
		tv.setText(R.string.softcenter_get_topics_prompt);
		isLoadingData = true;
		new LoadingItem().start();
		LoadAdapterThread mLoadingThread = new LoadAdapterThread();
		mLoadingThread.start();

	}

	private class LoadAdapterThread extends Thread {

		public void run() {
			TopicProtoc.TopicAppRequest.Builder topicAppRequestBuilder = TopicProtoc.TopicAppRequest
					.newBuilder();
			topicAppRequestBuilder.setTopicID(topicID);
			topicAppRequestBuilder.setStartIndex(0);
			topicAppRequestBuilder.setEntriesCount(20);

			RequestProtoc.Request.Builder request = RequestProtoc.Request
					.newBuilder();
			request.setTopicAppRequest(topicAppRequestBuilder);
			Base64Handler base64 = new Base64Handler();

			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet();
			int ret = -1;
			if(sCancelLoading){
				mHandler.sendEmptyMessage(FINISH_LOADING);
				return;
			}
			try {
				URI url = new URI(Constant.GET_TOPICAPP_URL
						+ base64.encode(request.build().toByteArray()));
				httpGet.setURI(url);
				if(sCancelLoading){
					mHandler.sendEmptyMessage(FINISH_LOADING);
					return;
				}
				HttpResponse response = client.execute(httpGet);
				if(sCancelLoading){
					mHandler.sendEmptyMessage(FINISH_LOADING);
					return;
				}
				ResponseProtoc.Response resp = ResponseProtoc.Response
						.parseFrom(Base64.decodeBase64(ConvertUtils
								.InputStreamToByte(response.getEntity()
										.getContent())));
				ResponseContext rc = resp.getContext();
				ret = rc.getResult();
				if (ret == 0) {
					PackageManager pm = getPackageManager();
					List<PackageInfo> packInfos = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
					if(sCancelLoading){
						mHandler.sendEmptyMessage(FINISH_LOADING);
						return;
					}
					TopicProtoc.TopicAppResponse topicAppResp = resp.getTopicAppResponse();
					List<App> appsList = topicAppResp.getAppList();
					for (App app : appsList) {
						if(sCancelLoading){
							mHandler.sendEmptyMessage(FINISH_LOADING);
							break;
						}
						AppInfoForManage aifm = new AppInfoForManage();
						aifm.setAppId(String.valueOf(app.getId()));
						aifm.setSize(app.getInstallSize());
						aifm.setPackageName(app.getPackageName());
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
						aifm.setFlag(app.getVersioncode());
						try{
							aifm.setIcon(Drawable.createFromStream(
											new ByteArrayInputStream(app.getIconData().toByteArray()), null));
						}catch(Exception e){
							e.printStackTrace();
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
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				ret = -1;
			}
			if (ret == -1) {
				mHandler.sendEmptyMessage(NETWORK_ERROR);
			}
			/*
			if(ret == 0 && listAdapter.getCount() > 0 && !sCancelLoading){
				ret = -1;
				Appstore.IconRequest.Builder iconRequest = Appstore.IconRequest
						.newBuilder();
				for (int j = 0; j < listAdapter.getCount(); j++) {
					AppInfoForManage item = (AppInfoForManage) listAdapter
							.getItem(j);
					iconRequest.addId(Integer.parseInt(item.getAppId()));
				}
				iconRequest.setIconType(Appstore.IconRequest.IconType.APP);

				Appstore.Request.Builder req = Appstore.Request.newBuilder();
				Appstore.RequestContext.Builder context = Appstore.RequestContext
						.newBuilder();
				req.setContext(context.build());
				req.setIconRequest(iconRequest);

				String reqStr = base64.encode(req.build().toByteArray());

				try {
					URI url = new URI(Constant.FETCH_ICON_URL + reqStr);
					httpGet.setURI(url);
					HttpResponse response = client.execute(httpGet);
					
					Appstore.Response resp = Appstore.Response.parseFrom(Base64
							.decodeBase64(ConvertUtils
									.InputStreamToByte(response.getEntity()
											.getContent())));
					Appstore.ResponseContext ctx = resp.getContext();
					ret = ctx.getResult();
					if (ret == 0) {
						Appstore.IconResponse iconResponse = resp
								.getIconResponse();
						List<Icon> iconList = iconResponse.getIconList();
						for (Icon icon : iconList) {

							for (int z = 0; z < listAdapter.getCount(); z++) {
								AppInfoForManage item = (AppInfoForManage) listAdapter
										.getItem(z);
								if (Integer.parseInt(item.getAppId()) == icon
										.getId()) {
									item.setIcon(Drawable.createFromStream(
											new ByteArrayInputStream(icon.getIcon()
													.toByteArray()), null));
									break;
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
				if (ret != 0 || sCancelLoading) {
					listAdapter.clearItems();
				}
			}
			*/
			mHandler.sendEmptyMessage(FINISH_LOADING);

		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FINISH_LOADING:				
				setListAdapter(listAdapter);
				isLoadingData = true;
				View loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				mRefreshAnimThread.startRefresh(SoftCenterTopicListActivity.this);
				break;
			case NETWORK_ERROR:
				isLoadingData = false;
				loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				Toast.makeText(SoftCenterTopicListActivity.this,
						R.string.bakcup_remote_network_unabailable_text,
						Toast.LENGTH_LONG).show();
				break;
			case GET_AD_DRAWABLES:
				mRefreshAnimThread.startRefresh(SoftCenterTopicListActivity.this);
				break;
			}
		}
	};

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
