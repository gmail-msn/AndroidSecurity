/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;

import com.kindroid.security.R;
import com.kindroid.security.adapter.CategoryListAdapter;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.model.CategoryInfo;
import com.kindroid.security.util.AppProtoc.App;
import com.kindroid.security.util.Base64Handler;
import com.kindroid.security.util.CategoryProtoc;
import com.kindroid.security.util.CategoryProtoc.Category;
import com.kindroid.security.util.CommonProtoc;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.HttpRequestUtil;
import com.kindroid.security.util.RefreshAnimationThread;
import com.kindroid.security.util.RequestProtoc;
import com.kindroid.security.util.ResponseProtoc;
import com.kindroid.security.util.Utilis;
import com.kindroid.security.util.CommonProtoc.BannerType;
import com.kindroid.security.util.ResponseProtoc.ResponseContext;
import com.kindroid.security.util.TopicProtoc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author heli.zhao
 * 
 */
public class SoftCenterCategoryListActivity extends ListActivity implements OnScrollListener {
	private BaseAdapter listAdapter;
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
	private static final int CANCEL_LOADING = 3;

	private static boolean sCancelLoading = false;
	private int topicID = 0;
	public boolean mUpdateBuffer = false;

	private static final String AD_DRAWABLE_PATH_PRFIX = "topic_ad_";
	private RefreshAnimationThread mRefreshAnimThread;
	private ImageView recommend_soft_image;
	public boolean hasAdBuffer = false;

	private boolean mIsCategory = false;
	private int mTopicType;

	private int mMoreIndex = 0;
	private static final int PAGE_ENTRIRE_COUNT = 10;
	private boolean mHasMore = false;

	public static final String FOREIGN_ID = "foreignId";
	public static final String PARTNER_ID = "partnerId";
	private int mForeignId;
	private int mPartnerId;
	
	private int lastItem = 0;
	private int currentPage = 0;
	private boolean isRefresh = false;
	LinearLayout footView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.topic_app_list);
		getListView().setOnScrollListener(this);
		
		TextView function_title_tv = (TextView) findViewById(R.id.function_title_tv);
		// set title from intent
		String topicName = getIntent().getStringExtra(
				SoftCenterTopicsActivity.TOPIC_NAME);
		if (topicName != null && !topicName.trim().equals("")) {
			function_title_tv.setText(topicName);
		}
		topicID = getIntent().getIntExtra(SoftCenterTopicsActivity.TOPIC_ID, 0);
		mTopicType = getIntent().getIntExtra(
				SoftCenterTopicsActivity.TOPIC_TYPE,
				SoftCenterTopicsActivity.TYPE_OF_TOPIC);
		if (mTopicType == SoftCenterTopicsActivity.TYPE_OF_CATEGORY) {
			mForeignId = getIntent().getIntExtra(FOREIGN_ID, 0);
			mPartnerId = getIntent().getIntExtra(PARTNER_ID, 0);
		}
		loadListAdapter();
		addFootView();

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		if (position < listAdapter.getCount()) {
			if (mIsCategory) {
				CategoryInfo ci = (CategoryInfo) listAdapter.getItem(position);
				Intent intent = new Intent(this,
						SoftCenterCategoryListActivity.class);
				intent.putExtra(SoftCenterTopicsActivity.TOPIC_ID, ci.getId());
				intent.putExtra(SoftCenterTopicsActivity.TOPIC_NAME, ci.getName());
				intent.putExtra(SoftCenterTopicsActivity.TOPIC_TYPE,
						SoftCenterTopicsActivity.TYPE_OF_CATEGORY);
				intent.putExtra(FOREIGN_ID, ci.getForeignID());
				intent.putExtra(PARTNER_ID, ci.getParentID());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} else {
				AppInfoForManage appInfoForManage = (AppInfoForManage) listAdapter
				.getItem(position);
				RecommendAppDetail.mAppInfoForManage = appInfoForManage;
				Intent intent = new Intent(this, RecommendAppDetail.class);
				
				startActivity(intent);
			}
		}
	}

	private void loadListAdapter() {
		if (!Utilis.checkNetwork(this)) {
			Toast.makeText(this,
					R.string.bakcup_remote_network_unabailable_text,
					Toast.LENGTH_LONG).show();
			return;
		}
		getListView().setVisibility(View.GONE);
		View loading_linear = findViewById(R.id.loading_linear);
		loading_linear.setVisibility(View.VISIBLE);

		sCancelLoading = false;
//		View view = LayoutInflater.from(this).inflate(
//				R.layout.softmanage_prompt_dialog, null);
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
		if (mTopicType == SoftCenterTopicsActivity.TYPE_OF_TOPIC) {
			tv.setText(R.string.softcenter_get_topics_prompt);
		} else {
			tv.setText(R.string.softcenter_get_category_prompt);
		}
		isLoadingData = true;
		
		new LoadingItem().start();
		LoadAdapterThread mLoadingThread = new LoadAdapterThread();
		mLoadingThread.start();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (!isLoadingData && (listAdapter == null || listAdapter.isEmpty())) {
			loadListAdapter();
		} else if (isLoadingData) {
			getListView().setVisibility(View.GONE);
			View loading_linear = findViewById(R.id.loading_linear);
			loading_linear.setVisibility(View.VISIBLE);
		} else if (listAdapter != null && listAdapter.getCount() > 1
				&& !mIsCategory) {
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

	private class LoadAdapterThread extends Thread {
		
		public void run() {
			if (mTopicType == SoftCenterTopicsActivity.TYPE_OF_TOPIC) {
				loadTopicList();
			} else {
				loadCategoryList();
			}

		}
	}

	private void loadTopicList() {
		TopicProtoc.TopicClickRequest.Builder topicClickRequest = TopicProtoc.TopicClickRequest
				.newBuilder();
		topicClickRequest.setStartIndex(mMoreIndex);
		topicClickRequest.setEntriesCount(PAGE_ENTRIRE_COUNT + 1);
		topicClickRequest.setTopicID(topicID);

		RequestProtoc.Request.Builder request = RequestProtoc.Request
				.newBuilder();
		request.setTopicClickRequest(topicClickRequest);

		Base64Handler base64 = new Base64Handler();

		int ret = -1;
		if (sCancelLoading) {
			mHandler.sendEmptyMessage(CANCEL_LOADING);
			return;
		}
		try {
			HashMap<String, String> param = new HashMap<String, String>();
			param.put("request", base64.encode(request.build().toByteArray()));
			Map<String, Object> res = HttpRequestUtil.postData(
					Constant.TOPIC_CLICK_URL, param, 10000);

			ResponseProtoc.Response resp = ResponseProtoc.Response
					.parseFrom(Base64.decodeBase64((byte[]) res.get("Content")));

			ResponseContext rc = resp.getContext();
			ret = rc.getResult();
			if (ret == 0) {
				if (sCancelLoading) {
					mHandler.sendEmptyMessage(CANCEL_LOADING);
					return;
				}
				TopicProtoc.TopicClickResponse topicClickResponse = resp
						.getTopicClickResponse();
				if (topicClickResponse.getTopicType() == CommonProtoc.TopicType.TOPIC_CATEGORY) {
					mIsCategory = true;
					if (listAdapter == null) {
						listAdapter = new CategoryListAdapter(
								SoftCenterCategoryListActivity.this);
					}
					addListAdapter(topicClickResponse.getCategoryList());
					mMoreIndex = mMoreIndex + PAGE_ENTRIRE_COUNT;
					Message msg = new Message();
					msg.arg1 = topicClickResponse.getCategoryList().size();
					msg.what = FINISH_LOADING;
					mHandler.sendMessage(msg);

				} else {
					mIsCategory = false;
					if (listAdapter == null) {
						listAdapter = new SoftCenterListAdapter(
								SoftCenterCategoryListActivity.this);
					}
					addListAdapter2(topicClickResponse.getAppList());
					mMoreIndex = mMoreIndex + PAGE_ENTRIRE_COUNT;
					Message msg = new Message();
					msg.arg1 = topicClickResponse.getAppList().size();
					msg.what = FINISH_LOADING;
					mHandler.sendMessage(msg);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			mHandler.sendEmptyMessage(FINISH_LOADING);
		}
		
	}

	private void addListAdapter2(List<App> mList) {
		PackageManager pm = getPackageManager();
		// List<PackageInfo> packInfos =
		// pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
		List<AppInfoForManage> tmpList = new ArrayList<AppInfoForManage>();
		int i = 0;
		for (App app : mList) {
			AppInfoForManage aifm = new AppInfoForManage();
			aifm.setAppId(String.valueOf(app.getId()));
			aifm.setSize(app.getInstallSize());
			aifm.setPackageName(app.getPackageName());
			aifm.setLabel(app.getTitle());
			aifm.setVersion(app.getVersionname().concat(
					getString(R.string.softmanage_version_title)));
			if (app.getPrice() > 0) {
				aifm.setUnit(app.getPrice() + app.getPriceUnit());
			} else {
				aifm.setUnit(getString(R.string.softcenter_search_my_free));
			}
			aifm.setDescription(app.getDescription());
			aifm.setPackagePath(app.getDownloadUrl());
			aifm.setFlag(app.getVersioncode());
			aifm.setPartnerId(app.getPartnerID());
			try {
				aifm.setIcon(Drawable.createFromStream(
						new ByteArrayInputStream(app.getIconData()
								.toByteArray()), null));
			} catch (Exception e) {
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
			tmpList.add(aifm);
			i++;
			if(i == 10){
				break;
			}
		}
		((SoftCenterListAdapter) listAdapter).addAll(tmpList);
	}

	private void addListAdapter(List<Category> mList) {
		List<CategoryInfo> tmpList = new ArrayList<CategoryInfo>();
		int i = 0;
		for (Category c : mList) {
			CategoryInfo ci = new CategoryInfo();
			ci.setForeignID(c.getForeignID());
			ci.setIcon(Drawable.createFromStream(new ByteArrayInputStream(c
					.getImage().toByteArray()), null));
			ci.setId(c.getId());
			ci.setName(c.getName());
			ci.setParentID(c.getParentID());
			ci.setStatus(c.getStatus());
			tmpList.add(ci);
			i++;
			if(i >=10){
				break;
			}
			
		}
		((CategoryListAdapter) listAdapter).addAll(tmpList);

	}

	private void loadCategoryList() {
		CategoryProtoc.CategoryClickRequest.Builder categoryClickRequestBuilder = CategoryProtoc.CategoryClickRequest
				.newBuilder();
		categoryClickRequestBuilder.setCategoryID(topicID);
		categoryClickRequestBuilder.setStartIndex(mMoreIndex);
		categoryClickRequestBuilder.setEntriesCount(PAGE_ENTRIRE_COUNT + 1);
		categoryClickRequestBuilder.setForeignID(mForeignId);
		categoryClickRequestBuilder.setPartnerID(mPartnerId);

		RequestProtoc.Request.Builder request = RequestProtoc.Request
				.newBuilder();
		request.setCategoryClickRequest(categoryClickRequestBuilder);
		Base64Handler base64 = new Base64Handler();

		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet();
		int ret = -1;
		if (sCancelLoading) {
			mHandler.sendEmptyMessage(CANCEL_LOADING);
			return;
		}
		try {
			URI url = new URI(Constant.CATEGORY_CLICK_URL
					+ base64.encode(request.build().toByteArray()));
			httpGet.setURI(url);
			if (sCancelLoading) {
				mHandler.sendEmptyMessage(CANCEL_LOADING);
				return;
			}
			HttpResponse response = client.execute(httpGet);
			
			ResponseProtoc.Response resp = ResponseProtoc.Response
					.parseFrom(Base64.decodeBase64(ConvertUtils
							.InputStreamToByte(response.getEntity()
									.getContent())));
			ResponseContext rc = resp.getContext();
			ret = rc.getResult();
			if (ret == 0) {
				if (sCancelLoading) {
					mHandler.sendEmptyMessage(CANCEL_LOADING);
					return;
				}
				CategoryProtoc.CategoryClickResponse categoryClickResponse = resp
						.getCategoryClickResponse();
				if (categoryClickResponse.getResultType() == CommonProtoc.ResultType.RESULT_CATEGORY) {
					mIsCategory = true;
					if(listAdapter == null){
						listAdapter = new CategoryListAdapter(SoftCenterCategoryListActivity.this);
					}
					addListAdapter(categoryClickResponse.getCategoryList());
					mMoreIndex = mMoreIndex + PAGE_ENTRIRE_COUNT;
					Message msg = new Message();
					msg.arg1 = categoryClickResponse.getCategoryList().size();
					msg.what = FINISH_LOADING;
					mHandler.sendMessage(msg);
					
				} else {
					mIsCategory = false;
					if(listAdapter == null){
						listAdapter = new SoftCenterListAdapter(SoftCenterCategoryListActivity.this);
					}
					addListAdapter2(categoryClickResponse.getAppList());
					mMoreIndex = mMoreIndex + PAGE_ENTRIRE_COUNT;
					Message msg = new Message();
					msg.arg1 = categoryClickResponse.getAppList().size();
					msg.what = FINISH_LOADING;
					mHandler.sendMessage(msg);
					
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			mHandler.sendEmptyMessage(FINISH_LOADING);
		}
		
	}
	private void addFootView(){
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		getListView().addFooterView(footView);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FINISH_LOADING:
				if(!isRefresh){
					setListAdapter(listAdapter);
				}else{
					listAdapter.notifyDataSetChanged();
					isRefresh = false;
				}
				isLoadingData = false;
				View loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				if(msg.arg1 <= PAGE_ENTRIRE_COUNT){
					if(getListView().getFooterViewsCount() > 0){
						getListView().removeFooterView(footView);
					}
				}
				getListView().setClickable(true);
				break;
			case NETWORK_ERROR:
				isLoadingData = false;
				loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				Toast.makeText(SoftCenterCategoryListActivity.this,
						R.string.bakcup_remote_network_unabailable_text,
						Toast.LENGTH_LONG).show();
				break;
			case CANCEL_LOADING:
				setListAdapter(listAdapter);
				isLoadingData = false;
				loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				if(getListView().getFooterViewsCount() > 0){
					getListView().removeFooterView(footView);
				}
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
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount - 1;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (!isLoadingData && listAdapter != null && lastItem == listAdapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			getListView().setClickable(false);
			isLoadingData = true;
			isRefresh = true;			
			LoadAdapterThread mLoadingThread = new LoadAdapterThread();
			mLoadingThread.start();
		}
	}

}
