/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.09
 * Description:
 */
package com.kindroid.security.ui;

import com.kindroid.security.R;
import com.kindroid.security.adapter.BlackWhiteAdapter;
import com.kindroid.security.service.DownloadService;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.InterceptDataBase;

import android.app.Activity;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import android.os.Bundle;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class InterceptWhiteList extends Activity implements
		View.OnClickListener, OnItemClickListener {

	/** Called when the activity is first created. */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */

	private TextView mBlackListNumLabT;
	private TextView mBlackListNumT;
	private LinearLayout mAddBlackListL;
	private TextView mAddBlackListLabT;
	private TextView mExplainT;

	private ListView mBlackListLV;

	private BroadcastReceiver mReceiver;
	private BlackWhiteAdapter mBlackWhiteAdapter;
	Cursor c;
	private int preCount;

	private View mListMenuLinear;
	private boolean mShowMenu = false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.intercept_blacklist);

		findView();
		mBlackListNumLabT.setText(R.string.white_list_num);
		mAddBlackListLabT.setText(R.string.add_white_list);
		//mExplainT.setText(R.string.black_list_explain);
		c = InterceptDataBase.get(this).selectAllList(2);
		if (c != null) {
			startManagingCursor(c);
			mBlackListNumT.setText(c.getCount() + "");
			preCount=c.getCount();
			mBlackWhiteAdapter = new BlackWhiteAdapter(this, c, false, 2);
			mBlackListLV.setAdapter(mBlackWhiteAdapter);
		} else {
			mBlackListNumT.setText("0");
		}

		initReciver();
		IntentFilter mIt = new IntentFilter(Constant.BROACTUPDATEINTERCEPT);

		registerReceiver(mReceiver, mIt);

	}

	private void findView() {
		mBlackListNumLabT = (TextView) findViewById(R.id.list_num_lab_tv);
		mBlackListNumT = (TextView) findViewById(R.id.list_num_tv);
		mExplainT = (TextView) findViewById(R.id.introdct_tv);
		mExplainT.setVisibility(View.GONE);
		mAddBlackListL = (LinearLayout) findViewById(R.id.add_list_linear);
		mAddBlackListLabT = (TextView) findViewById(R.id.add_list_tv);
		mBlackListLV = (ListView) findViewById(R.id.black_listview);
		mListMenuLinear = findViewById(R.id.intercept_black_white_list_menu);
		bindListerToView();
	}

	private void bindListerToView() {
		mAddBlackListL.setOnClickListener(this);
		mBlackListLV.setOnItemClickListener(this);
		mListMenuLinear.setOnClickListener(this);
	}

	void initReciver() {
		mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub

				if (intent == null
						|| !intent.getAction().equals(
								Constant.BROACTUPDATEINTERCEPT))
					return;
				refreshAdapter();
			}
		};
	}
	private void clearList(){
		if(mBlackWhiteAdapter.getCount() == 0){
			return;
		}
		//show confirm dialog
		final Dialog promptDialog = new Dialog(
				this.getParent(), R.style.softDialog);
		View view = LayoutInflater.from(this)
				.inflate(R.layout.soft_uninstall_prompt_dialog,
						null);
		promptDialog.setContentView(view);

		TextView promptText = (TextView) promptDialog
				.findViewById(R.id.prompt_text);
		promptText.setText(R.string.confirm_to_clear_list);
		Button button_ok = (Button) promptDialog
				.findViewById(R.id.button_ok);
		Button button_cancel = (Button) promptDialog
				.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				InterceptDataBase mBase = InterceptDataBase.get(InterceptWhiteList.this);
				mBase.ClearBlackWhiteList(2);
				refreshAdapter();
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
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.add_list_linear:
			Intent intent = new Intent(InterceptWhiteList.this,
					AddBlackWhiteListActivity.class);
			intent.putExtra("black_or_white", 2);
			startActivity(intent);
			break;
		case R.id.intercept_black_white_list_menu:
			//clear white list
			clearList();
			BlockTabMain.showBottomLinear();
			mListMenuLinear.setVisibility(View.GONE);
			mShowMenu = false;
			break;
		}

	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(!mShowMenu){
			super.onBackPressed();
			sendBroadcast(new Intent(Constant.BROACTUPDATEINFINISHBLOCK));
		}else{
			BlockTabMain.showBottomLinear();
			mListMenuLinear.setVisibility(View.GONE);
			mShowMenu = false;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU:
			if(mShowMenu){
				BlockTabMain.showBottomLinear();
				mListMenuLinear.setVisibility(View.GONE);
				mShowMenu = false;
			}else{
				BlockTabMain.hideBottomLinear();
				mListMenuLinear.setVisibility(View.VISIBLE);
				mShowMenu = true;
			}
			break;
			/*
		case KeyEvent.KEYCODE_BACK:
			sendBroadcast(new Intent(Constant.BROACTUPDATEINFINISHBLOCK));
			return true;
			*/
			
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (mBlackWhiteAdapter.getmPosition() == arg2) {
			mBlackWhiteAdapter.setmPosition(-1);
		} else {
			mBlackWhiteAdapter.setmPosition(arg2);
		}
		mBlackWhiteAdapter.notifyDataSetChanged();

	}

	public void refreshAdapter() {
		c.requery();
		if (c != null) {
			if(c.getCount()<preCount){
				mBlackWhiteAdapter.setmPosition(-1);
				preCount=c.getCount();
			}
			mBlackListNumT.setText(c.getCount() + "");
		} else {
			mBlackListNumT.setText("0");
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		refreshAdapter();
		/*
		if (c != null) {
			mBlackListNumT.setText(c.getCount() + "");
		} else {
			mBlackListNumT.setText("0");
		}
		*/
		BlockTabMain.showBottomLinear();
		mListMenuLinear.setVisibility(View.GONE);
	}

}