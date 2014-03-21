/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.util;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.ui.RecommendAppDetail;
import com.kindroid.security.ui.SoftCenterTopicListActivity;
import com.kindroid.security.ui.SoftCenterTopicsActivity;
import com.kindroid.security.ui.TopicAdClickListActivity;
import com.kindroid.security.util.ADProtoc.Advert;
import com.kindroid.security.util.ADProtoc.BannerResponse;
import com.kindroid.security.util.ADProtoc.ClickResponse;
import com.kindroid.security.util.AppProtoc.App;
import com.kindroid.security.util.AppProtoc.AppSearchResponse;
import com.kindroid.security.util.CommonProtoc.BannerType;
import com.kindroid.security.util.CommonProtoc.TargetType;
import com.kindroid.security.util.ResponseProtoc.ResponseContext;
import com.kindroid.security.util.TopicProtoc.Topic;
import com.kindroid.security.util.TopicProtoc.TopicAppResponse;

public class RefreshAnimationThread extends Thread {
	private String animPath;
	private Activity mActivity;
	private AnimationDrawable mAnimDrawable = null;
	private Handler mHandler;
	private static int REFRESH_DURATION = 4000;
	private BannerType mType;
	private int mTopicID;
	private int mHandlerMsg;
	private ImageView mAdView;
	private boolean mCompleteUpdate = false;
	private boolean mRefreshing = false;
	private boolean mBufferExist = true;
	private Map<Drawable, Integer> mAdMap;

	public RefreshAnimationThread(Activity activity, String resPath,
			ImageView adview, Handler handler, BannerType type, int topicId,
			int handlerMsg) {
		this.mActivity = activity;
		this.animPath = resPath;
		this.mType = type;
		this.mTopicID = topicId;
		this.mHandlerMsg = handlerMsg;
		this.mAdView = adview;
		this.mHandler = handler;
		mAdMap = new HashMap<Drawable, Integer>();
		
	}

	public static void setRefreshDuration(int duration) {
		REFRESH_DURATION = duration;
	}

	public void run() {
		ADProtoc.BannerRequest.Builder bRequestBuilder = ADProtoc.BannerRequest
				.newBuilder();
		bRequestBuilder.setBannerType(mType);
		bRequestBuilder.setTopicID(mTopicID);
		RequestProtoc.Request.Builder request = RequestProtoc.Request
				.newBuilder();
		request.setBannerRequest(bRequestBuilder);
		Base64Handler base64 = new Base64Handler();

		HttpClient client = new DefaultHttpClient();
		// HttpGet httpGet = new HttpGet();
		HttpPost httpPost = new HttpPost();
		int ret = -1;

		try {
			/*
			 * URI url = new URI(Constant.AD_DRAWABLE_URL +
			 * URLEncoder.encode(base64.encode(request.build().toByteArray())));
			 * httpGet.setURI(url);
			 * 
			 * System.out.println("url :" + url); HttpResponse response =
			 * client.execute(httpGet);
			 */

			URI url = new URI(Constant.AD_DRAWABLE_URL);
			httpPost.setURI(url);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("request", base64
					.encode(request.build().toByteArray())));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(httpPost);

			ResponseProtoc.Response resp = ResponseProtoc.Response
					.parseFrom(Base64.decodeBase64(ConvertUtils
							.InputStreamToByte(response.getEntity()
									.getContent())));
			ResponseContext rc = resp.getContext();
			ret = rc.getResult();
			if (ret == 0) {
				BannerResponse bResp = resp.getBannerResponse();
				List<Advert> adList = bResp.getAdvertList();
				if (adList.size() > 0) {
					// delete old files
					File resFile = new File(animPath);
					File[] files = resFile.listFiles();
					for (File f : files) {
						try {
							f.delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
				int c = 0;
				for (Advert ad : adList) {
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(animPath + "/" + ad.getId());
						byte[] bytes = ad.getImage().toByteArray();
						fos.write(bytes);
						fos.flush();
						c++;
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (fos != null) {
							try {
								fos.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				mCompleteUpdate = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mCompleteUpdate) {
			mHandler.sendEmptyMessage(mHandlerMsg);
		}
	}

	public void startRefresh(Activity act) {
		// TODO Auto-generated method stub
		File resFile = new File(animPath);
		File[] files = resFile.listFiles();
		if (files.length == 0) {
			return;
		}

		if (mAnimDrawable == null || mAnimDrawable.getNumberOfFrames() == 0
				|| mCompleteUpdate) {
			mAnimDrawable = new AnimationDrawable();
			mAnimDrawable.setOneShot(false);
			// create animation drawable
			mAdMap.clear();

			for (File file : files) {
				try {
					Drawable drw = Drawable.createFromPath(file
							.getAbsolutePath());
					mAnimDrawable.addFrame(drw, REFRESH_DURATION);
					int id = Integer.parseInt(file.getName());
					mAdMap.put(drw, id);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			try {
				mAdView.setBackgroundDrawable(mAnimDrawable);
				mAnimDrawable.setCallback(mAdView);
				mAnimDrawable.start();
				mAdView.setOnClickListener(new AdOnClickListener());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (mAnimDrawable != null) {
			mAnimDrawable.start();
		}

	}

	private class AdOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mAdView.setClickable(false);
			Drawable drw = mAnimDrawable.getCurrent();
			if (mAdMap.get(drw) == null) {
				return;
			}
			int id = mAdMap.get(drw);			
			clickAdvert(id);			
			//mAdView.setClickable(true);
		}

	}

	private void clickAdvert(int id) {
		ADProtoc.ClickRequest.Builder cRequestBuilder = ADProtoc.ClickRequest
				.newBuilder();
		cRequestBuilder.setAdvertID(id);
		RequestProtoc.Request.Builder request = RequestProtoc.Request
				.newBuilder();
		request.setClickRequest(cRequestBuilder);
		Base64Handler base64 = new Base64Handler();

		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet();

		int ret = -1;

		try {
			URI url = new URI(Constant.AD_CLICK_URL
					+ URLEncoder.encode(base64.encode(request.build()
							.toByteArray())));
			httpGet.setURI(url);
			HttpResponse response = client.execute(httpGet);

			ResponseProtoc.Response resp = ResponseProtoc.Response
					.parseFrom(Base64.decodeBase64(ConvertUtils
							.InputStreamToByte(response.getEntity()
									.getContent())));
			ResponseContext rc = resp.getContext();
			ret = rc.getResult();
			if (ret == 0) {
				ClickResponse cResponse = resp.getClickResponse();
				TargetType targetType = cResponse.getTargetType();
				if (targetType.equals(TargetType.TARGET_APP)) {
					App app = cResponse.getApp();
					toTargetActivity(app);
				} else if (targetType.equals(TargetType.TARGET_TOPIC)) {
					Topic topic = cResponse.getTopic();
					toTargetActivity(topic);
				}
			}

		} catch (Exception e) {

		}
	}

	private void toTargetActivity(App app) {
		if (app == null) {
			return;
		}
		AppInfoForManage aifm = new AppInfoForManage();
		aifm.setAppId(String.valueOf(app.getId()));
		aifm.setSize(app.getInstallSize());
		aifm.setPackageName(app.getPackageName());
		aifm.setLabel(app.getTitle());
		aifm.setVersion(app.getVersionname().concat(
				mActivity.getString(R.string.softmanage_version_title)));
		if (app.getPrice() > 0) {
			aifm.setUnit(app.getPrice() + app.getPriceUnit());
		} else {
			aifm.setUnit(mActivity
					.getString(R.string.softcenter_search_my_free));
		}
		aifm.setDescription(app.getDescription());
		aifm.setPackagePath(app.getDownloadUrl());
		aifm.setFlag(app.getVersioncode());
		try {
			PackageManager pm = mActivity.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(aifm.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null)
				aifm.setInstalled(true);
		} catch (NameNotFoundException e) {
			aifm.setInstalled(false);
		}
		byte[] bytes = app.getIconData().toByteArray();
		aifm.setIcon(new BitmapDrawable(BitmapFactory
										.decodeByteArray(bytes, 0,
												bytes.length)));
		RecommendAppDetail.mAppInfoForManage = aifm;
		Intent intent = new Intent(mActivity, RecommendAppDetail.class);
		//mActivity.startActivity(intent);
		mActivity.startActivityForResult(intent, 19);
	}

	private void toTargetActivity(Topic topic) {
		Intent intent = new Intent(mActivity, TopicAdClickListActivity.class);
		intent.putExtra(SoftCenterTopicsActivity.TOPIC_ID, topic.getId());
		intent.putExtra(SoftCenterTopicsActivity.TOPIC_NAME, topic.getName());
		//mActivity.startActivity(intent);
		mActivity.startActivityForResult(intent, 19);
	}
	

}
