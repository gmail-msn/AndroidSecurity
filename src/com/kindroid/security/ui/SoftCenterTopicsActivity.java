/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.util.Base64Handler;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.RequestProtoc;
import com.kindroid.security.util.ResponseProtoc;
import com.kindroid.security.util.TopicProtoc;
import com.kindroid.security.util.TopicProtoc.Topic;
import com.kindroid.security.util.TopicProtoc.TopicResponse;
import com.kindroid.security.util.Utilis;
import com.kindroid.security.util.ResponseProtoc.ResponseContext;

public class SoftCenterTopicsActivity extends ListActivity {
	public static final String TOPIC_ID = "topic_id";
	public static final String TOPIC_NAME = "topic_name";
	public static final String TOPIC_TYPE = "topic_type";
	public static final int TYPE_OF_TOPIC = 0;
	public static final int TYPE_OF_CATEGORY = 1;
	public static final int TYPE_OF_APP = 2;
	private TopicsListAdapter listAdapter;

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
	private static final String mBufferDBName = "SoftCenterTopicsBuffer.db";
	public boolean mBufferExist = true;
	public boolean mUpdateBuffer = false;
	private boolean mCancelLoading = false;
	
	private static final String KINDROID_SECURITY_DIR = "/Kindroid/Security/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.topics_list);
		listAdapter = new TopicsListAdapter(this);	
		isLoadingData = true;
		loadListAdapter();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if (!isLoadingData && listAdapter.isEmpty()) {
			isLoadingData = true;			
			listAdapter.clearItems();
			listAdapter.notifyDataSetChanged();
			loadListAdapter();

		}
		
	}

	private void loadListAdapter() {
		/*
		if (!Utilis.checkNetwork(this)) {
			isLoadingData = false;
			Toast.makeText(this,
					R.string.bakcup_remote_network_unabailable_text,
					Toast.LENGTH_LONG).show();
			return;
		}
		*/
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
		tv.setText(R.string.softcenter_get_topics_prompt);		
				
		new LoadingItem().start();
		LoadAdapterThread mLoadingThread = new LoadAdapterThread();
		mCancelLoading = false;
		mLoadingThread.start();		

	}
	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		TopicItem item = (TopicItem) listAdapter.getItem(position);
		int topicId = item.getTopicId();
		/*
		Intent intent = new Intent(this, SoftCenterTopicListActivity.class);
		intent.putExtra(TOPIC_ID, topicId);
		intent.putExtra(TOPIC_NAME, item.getTopicName());
		startActivity(intent);
		*/
		Intent intent = new Intent(this, SoftCenterCategoryListActivity.class);
		intent.putExtra(TOPIC_ID, topicId);
		intent.putExtra(TOPIC_NAME, item.getTopicName());
		intent.putExtra(TOPIC_TYPE, TYPE_OF_TOPIC);
		startActivity(intent);
	}
	
	private class TopicItem{
		private int mTopicId;
		private String mTopicName;
		private byte[] mImageBytes;
		
		public int getTopicId() {
			return mTopicId;
		}
		public void setTopicId(int mTopicId) {
			this.mTopicId = mTopicId;
		}
		public String getTopicName() {
			return mTopicName;
		}
		public void setTopicName(String mTopicName) {
			this.mTopicName = mTopicName;
		}
		public byte[] getImageBytes() {
			return mImageBytes;
		}
		public void setImageBytes(byte[] mImageBytes) {
			this.mImageBytes = mImageBytes;
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
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FINISH_LOADING:
				isLoadingData = false;
				setListAdapter(listAdapter);
				listAdapter.notifyDataSetChanged();
				View loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				break;
			case NETWORK_ERROR:
				isLoadingData = false;
				loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				Toast.makeText(SoftCenterTopicsActivity.this,
						R.string.bakcup_remote_network_unabailable_text,
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	private class LoadAdapterThread extends Thread {
		
		private boolean readFromBuffer() {
			boolean ret = false;
			
			String bFilePath = getBufferFilePath();
			if(bFilePath == null){
				return false;
			}
			SQLiteDatabase localSQLiteDatabase = null;
			
			try {
				localSQLiteDatabase = SQLiteDatabase.openDatabase(bFilePath, null,
						1);
			} catch (Exception e) {
				e.printStackTrace();
				localSQLiteDatabase = null;
			}

			if (localSQLiteDatabase == null) {
				return false;
			}
			Cursor localCursor = localSQLiteDatabase.query("topics_buffer", null,
					null, null, null, null, null);
			if (localCursor.getCount() <= 0) {
				try {
					localCursor.close();
					localSQLiteDatabase.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
			PackageManager pm = getPackageManager();
			int c = 0;
			while (localCursor.moveToNext()) {
				TopicItem item = new TopicItem();
				item.setImageBytes(localCursor.getBlob(localCursor.getColumnIndex("icon")));
				item.setTopicId(localCursor.getInt(localCursor.getColumnIndex("id")));
				item.setTopicName(localCursor.getString(localCursor.getColumnIndex("name")));
				listAdapter.addItem(item);
				c++;
			}
			try {
				localCursor.close();
				localSQLiteDatabase.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (c > 0) {
				ret = true;
			} else {
				ret = false;
			}
			return ret;
		}

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
				File bFile = new File(Environment.getExternalStorageDirectory(),
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
				}else{
					ret=bFile.getAbsolutePath();
				}

			}
			return ret;
		}
		public void run() {
			mBufferExist = readFromBuffer();
			if (mBufferExist && !mCancelLoading) {
				mHandler.sendEmptyMessage(FINISH_LOADING);
				
			} 
			if (mCancelLoading) {
				isLoadingData = false;
				listAdapter.clearItems();
				mHandler.sendEmptyMessage(FINISH_LOADING);
				return;
			}
			if (!Utilis.checkNetwork(SoftCenterTopicsActivity.this)) {
				if (mBufferExist) {
					mHandler.sendEmptyMessage(FINISH_LOADING);
				} else {
					mHandler.sendEmptyMessage(NETWORK_ERROR);
				}
				return;
			}
			if (mUpdateBuffer) {
				return;
			}
			mUpdateBuffer = true;
			
			TopicProtoc.TopicRequest.Builder topicRequestBuilder = TopicProtoc.TopicRequest
					.newBuilder();
			topicRequestBuilder.setStartIndex(0);
			topicRequestBuilder.setEntriesCount(20);

			RequestProtoc.Request.Builder request = RequestProtoc.Request
					.newBuilder();
			request.setTopicRequest(topicRequestBuilder);
			Base64Handler base64 = new Base64Handler();

			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet();
			int ret = -1;
			
			SQLiteDatabase localSQLiteDatabase = null;
			try {
				URI url = new URI(Constant.GET_TOPICS_URL
						+ base64.encode(request.build().toByteArray()));
				httpGet.setURI(url);
				HttpResponse response = client.execute(httpGet);

				ResponseProtoc.Response resp = ResponseProtoc.Response
						.parseFrom(Base64.decodeBase64(ConvertUtils
								.InputStreamToByte(response.getEntity()
										.getContent())));
				ResponseContext rc = resp.getContext();
				ret = rc.getResult();
				if (ret == 0) {
					String bFilePath = getBufferFilePath();
					
					if (bFilePath != null) {
						File bFile = new File(bFilePath);
						try {
							bFile.delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
						String sql = "CREATE TABLE IF NOT EXISTS topics_buffer ("
								+ " id INTEGER, name TEXT,  icon BLOB);";
						try {
							localSQLiteDatabase = SQLiteDatabase
									.openDatabase(bFilePath, null,
											SQLiteDatabase.CREATE_IF_NECESSARY);

							localSQLiteDatabase.execSQL(sql);
							localSQLiteDatabase.execSQL("DELETE FROM topics_buffer");
						} catch (Exception e) {
							e.printStackTrace();
							mBufferExist = false;
						}

					}
					TopicResponse tResp = resp.getTopicResponse();
					List<Topic> tList = tResp.getTopicList();
					for (Topic topic : tList) {
						byte[] bytes = topic.getImage().toByteArray();
						if (localSQLiteDatabase != null) {
							ContentValues cv = new ContentValues();
							cv.put("id", topic.getId());
							cv.put("name", topic.getName());							
							cv.put("icon", bytes);
							try {
								localSQLiteDatabase.insertOrThrow(
										"topics_buffer", null, cv);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if(!mBufferExist){
							TopicItem item = new TopicItem();
							item.setImageBytes(bytes);
							item.setTopicId(topic.getId());
							item.setTopicName(topic.getName());
							listAdapter.addItem(item);
						}
					}
				}
				localSQLiteDatabase.close();
			} catch (Exception e) {
				e.printStackTrace();
				ret = -1;
				if (localSQLiteDatabase != null) {
					localSQLiteDatabase.close();
				}
			} finally {
				if (localSQLiteDatabase != null) {
					localSQLiteDatabase.close();
				}
				
			}
			if(ret == -1 && !mBufferExist){
				mHandler.sendEmptyMessage(NETWORK_ERROR);
			}
			if (!mBufferExist) {
				if (mCancelLoading) {
					listAdapter.clearItems();
				}
				mHandler.sendEmptyMessage(FINISH_LOADING);
			}
			mUpdateBuffer = false;

		}
	}

	private void toTopicDetail(int id) {
		Intent intent = new Intent(this, SoftCenterTopicListActivity.class);
		intent.putExtra(TOPIC_ID, id);
		startActivity(intent);

	}

	private class TopicsListAdapter extends BaseAdapter {
		private List<TopicItem> mList;
		private Activity mActivity;
		private List<BitmapDrawable> mBmdList;

		public TopicsListAdapter(Activity context) {
			this.mActivity = context;
			mList = new ArrayList<TopicItem>();
			mBmdList = new ArrayList<BitmapDrawable>();
		}
		public List<BitmapDrawable> getBitmapList(){
			return mBmdList;
		}

		public void addItem(TopicItem item) {
			mList.add(item);
		}

		public void deleteItem(int index) {
			mList.remove(index);
		}

		public void deleteItem(TopicItem item) {
			mList.remove(item);
		}

		public void clearItems() {
			mList.clear();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView == null){
				convertView = this.mActivity.getLayoutInflater().inflate(
						R.layout.topics_list_item, null);
			}
			
			ImageView topic_image = (ImageView) convertView
					.findViewById(R.id.topic_image);
			TopicItem item = (TopicItem) getItem(position);
			byte[] bytes = item.getImageBytes();
			Drawable d = topic_image.getDrawable();
			if(d != null){
				try{
					BitmapDrawable bd = (BitmapDrawable)d;
					Bitmap b = bd.getBitmap();
					if(b != null){
						b.recycle();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			BitmapDrawable drw = new BitmapDrawable(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
			topic_image.setImageDrawable(drw);
//			mBmdList.add(drw);
			return convertView;
		}

	}

}
