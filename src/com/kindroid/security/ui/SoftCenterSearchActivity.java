/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.kindroid.security.data.SearchAppData;
import com.kindroid.security.util.Appstore.App;
import com.kindroid.security.util.Appstore.Response;
import org.apache.commons.codec.binary.Base64;
import com.kindroid.security.R;
import com.kindroid.security.util.Appstore.HotKeyWordsRequest;
import com.kindroid.security.util.Appstore.Request;
import com.kindroid.security.util.Appstore.RequestContext;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.HttpRequest;
import com.kindroid.security.util.Utilis;

public class SoftCenterSearchActivity extends Activity {

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

	private GridView mSearchListView;
	private static boolean isLoadingData = false;
	private List<String> Hot_Key_Words = new ArrayList<String>();
	private List<App> pageList;
	private static SearchItemAdapter adapter;
	
	private static final int LOADING_FINISH = 0;
	private static final int NETWORK_ERROR = 1;
	
	public static final String SEARCH_KEY_WORD = "softcenter_search_keyword";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.softcenter_search);
		View search_action_linear = findViewById(R.id.search_action_linear);
		search_action_linear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TextView search_edit_text = (TextView)findViewById(R.id.search_edit_text);
				startSearch(search_edit_text.getText().toString());
			}

		});
		mSearchListView = (GridView) findViewById(R.id.hotAppListView);
		
		mSearchListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(adapter != null){
					String[] words = (String[])adapter.getItem(position);
					if(words != null){
						startSearch(words[0].trim());
					}else{
						Toast.makeText(SoftCenterSearchActivity.this, R.string.search_keyword_error, Toast.LENGTH_LONG).show();
					}
				}
			}
			
		});		
	}
	private void startSearch(String keyWord){
		if(keyWord == null || keyWord.trim().equals("")){
			Toast.makeText(this, R.string.search_keyword_error, Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(this, SearchResultListActivity.class);
		intent.putExtra(SEARCH_KEY_WORD, keyWord.trim());
		startActivityForResult(intent, 99);
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 99 && resultCode == Activity.RESULT_OK) {
			finish();
		}else{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!isLoadingData && (adapter == null || adapter.isEmpty())) {
			isLoadingData = true;
			loadHotKeyWords();
		}else if(adapter != null && !adapter.isEmpty()){
			Utilis.randomList(adapter.getWords());
			mSearchListView.setAdapter(adapter);
		}
	}

	private void loadHotKeyWords() {
		if (!Utilis.checkNetwork(this)) {
			isLoadingData = false;
			Toast.makeText(this,
					R.string.bakcup_remote_network_unabailable_text,
					Toast.LENGTH_LONG).show();
			return;
		}
		mSearchListView.setVisibility(View.GONE);
		View loading_linear = findViewById(R.id.loading_linear);
		loading_linear.setVisibility(View.VISIBLE);
		
		TextView prompt_dialog_text = (TextView)findViewById(R.id.prompt_dialog_text);
		prompt_dialog_text.setText(R.string.softcenter_search_get_hotkey_text);
		
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
		
		new LoadHotKeyWords().start();
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
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case LOADING_FINISH:
				isLoadingData = false;				
				mSearchListView.setAdapter(adapter);
				mSearchListView.setVisibility(View.VISIBLE);
				View loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				break;
			case NETWORK_ERROR:
				Toast.makeText(SoftCenterSearchActivity.this, R.string.bakcup_remote_network_unabailable_text, Toast.LENGTH_LONG).show();
				
				isLoadingData = false;
				mSearchListView.setVisibility(View.VISIBLE);
				loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				break;
			}

		}
	};

	private class LoadHotKeyWords extends Thread {

		public void run() {
			if(!Utilis.checkNetwork(SoftCenterSearchActivity.this)){
				mHandler.sendEmptyMessage(NETWORK_ERROR);
				return;
			}
			SearchAppData listData = new SearchAppData(
					SoftCenterSearchActivity.this);
			pageList = listData.getTopResult(0, 6);
			List<String[]> words = new ArrayList<String[]>();
			
			Request.Builder request=Request.newBuilder();
			HotKeyWordsRequest.Builder appRequest=HotKeyWordsRequest.newBuilder();
			RequestContext.Builder context=RequestContext.newBuilder();
			request.setContext(context.build());
			request.setHotkeyWordsRequest(appRequest.build());
			
			try{
				InputStream in = HttpRequest.postData(Constant.HOT_KEYWORD_URL, request);
				if(in!=null){
					Response resp = Response.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
					List<String> respWords = resp.getHotkeyWordsResponse().getWordList();
					int len = respWords.size()>12?12:respWords.size();
					for(int i=0;i<len;i++){
						words.add(new String[]{respWords.get(i),""+(len-i-1)});
					}
					Utilis.randomList(words);
				}
				
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
			for(int i=0;i<words.size();i++){
				Hot_Key_Words.add(words.get(i)[0]);
			}

			adapter = new SearchItemAdapter(SoftCenterSearchActivity.this, words);			
			mHandler.sendEmptyMessage(LOADING_FINISH);
		}
	}

}
