/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.09
 * Description:
 */
package com.kindroid.security.ui;

import java.util.ArrayList;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.adapter.InterceptHistoryAdapter;
import com.kindroid.security.notification.NetTrafficNotification;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.HistoryNativeCursor;
import com.kindroid.security.util.InterceptDataBase;
import com.kindroid.security.util.NativeCursor;

import android.app.Activity;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import android.os.Bundle;
import android.os.Handler;

import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class InterceptHistoryPhone extends Activity implements
		View.OnClickListener ,OnCheckedChangeListener,OnItemClickListener{

	/** Called when the activity is first created. */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */

	private TextView mInterceptLabNumTv;
	private TextView mInterceptNumTv;
	private CheckBox mSelectCb;
	private List<HistoryNativeCursor> mCursors = new ArrayList<HistoryNativeCursor>();

	private InterceptHistoryAdapter mAdapter;
	private ListView mListview;
	private View mHistoryMenuLinear;
	private View mHistoryMenuAddItem;
	private View mHistoryMenuDeleteItem;
	private boolean mShowMenu = false;
	private BroadcastReceiver mReceiver;
	private boolean mCanUse = true;
	private Handler mHandler = new Handler();
	private boolean isActive=false;
	private boolean comFromCreate;
	private boolean hasUnreadSpam = false;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.intercept_history_sms);
		findView();
		isActive=true;
		mInterceptLabNumTv.setText(R.string.phone_block_num);
		loadAdapter();
		mAdapter=new InterceptHistoryAdapter(this,mCursors, 4);
		mListview.setAdapter(mAdapter);
		setNumTv(mCursors.size());
		comFromCreate=true;

		initReciver();
		IntentFilter mIt = new IntentFilter(
				Constant.BROACTUPDATEINTERCEPTHISTORY);

		registerReceiver(mReceiver, mIt);
		
		

	}

	private void findView() {
		mInterceptLabNumTv = (TextView) findViewById(R.id.intercept_lab_num_tv);
		mInterceptNumTv = (TextView) findViewById(R.id.intercept_num_tv);
		mSelectCb = (CheckBox) findViewById(R.id.select_al_cb);
		mListview = (ListView) findViewById(R.id.listproc);
		mHistoryMenuLinear = findViewById(R.id.intercept_history_menu_phone);
		mHistoryMenuAddItem = findViewById(R.id.intercept_history_menu_add);
		mHistoryMenuDeleteItem = findViewById(R.id.intercept_history_menu_delete);
		bindListerToView();
	}

	private void bindListerToView() {
		mListview.setOnItemClickListener(this);
		mSelectCb.setOnCheckedChangeListener(this);
		mHistoryMenuAddItem.setOnClickListener(this);
		mHistoryMenuDeleteItem.setOnClickListener(this);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(hasUnreadSpam){
			try{
				loadAdapter();
				mSelectCb.setChecked(false);
				mAdapter.setmPosition(-1);
				mAdapter.notifyDataSetChanged();
				setNumTv(mCursors.size());
			}catch(Exception e){
				e.printStackTrace();
			}
			hasUnreadSpam = false;
		}
		if(comFromCreate){
			comFromCreate=false;
			
		}else{
			showNotification();
		}
		isActive=true;
		BlockTabMain.showBottomLinear();
		mHistoryMenuLinear.setVisibility(View.GONE);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(!mShowMenu){
			super.onBackPressed();
			sendBroadcast(new Intent(Constant.BROACTUPDATEINFINISHBLOCK));
		}else{
			BlockTabMain.showBottomLinear();
			mHistoryMenuLinear.setVisibility(View.GONE);
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
				mHistoryMenuLinear.setVisibility(View.GONE);
				mShowMenu = false;
			}else{
				BlockTabMain.hideBottomLinear();
				mHistoryMenuLinear.setVisibility(View.VISIBLE);
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
	private void deleteHistory(){
		int count = mAdapter.getCount();
		if(count == 0){
			return;
		}
		InterceptDataBase mBase = InterceptDataBase.get(this);
		boolean deleted = false;
		for(int i = 0; i < count; i++){
			HistoryNativeCursor mCursor = (HistoryNativeCursor)mAdapter.getItem(i);
			if(mCursor.isSelect()){
				//delete this history item
				mBase.DelHistory(mCursor);
				deleted = true;
			}
		}
		if(deleted){
			//refresh UI
			loadAdapter();
			mAdapter=new InterceptHistoryAdapter(this,mCursors, 4);
			mListview.setAdapter(mAdapter);
			setNumTv(mAdapter.getCount());
			if(mSelectCb.isChecked()){
				mSelectCb.setChecked(false);
			}
		}else{
			Toast.makeText(this, R.string.select_content_to_delete, Toast.LENGTH_LONG).show();
		}
		
	}
	private void addToBlackList(){
		int count = mAdapter.getCount();
		if(count == 0){
			return;
		}
		InterceptDataBase mBase = InterceptDataBase.get(this);
		boolean hasSelected = false;
		boolean allHasExist = true;
		for(int i = 0; i < count; i++){
			HistoryNativeCursor mCursor = (HistoryNativeCursor)mAdapter.getItem(i);
			if(mCursor.isSelect()){
				hasSelected = true;
				//add to blacklist
				NativeCursor nc = new NativeCursor();
				nc.setmRequestType(1);
				nc.setmPhoneNum(mCursor.getmAddress());
				nc = mBase.selectIsExists(nc);
				if(nc.ismIsExists()){
					//continue;
				}else{
					allHasExist = false;
					mBase.insertBlackWhitList(1, null, mCursor.getmAddress(), true, true, 1);
					
				}
			}
		}
		if(!hasSelected){
			Toast.makeText(this, R.string.select_phones_to_addlist, Toast.LENGTH_LONG).show();			
		}else{
			if(allHasExist){
				Toast.makeText(this, R.string.all_exist_when_addlist, Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, R.string.add_list_complete, Toast.LENGTH_LONG).show();
			}
			//reset checkbox
			mSelectCb.setChecked(false);
		}
	}
	
	void initReciver() {
		mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				int type=0;
				if(intent!=null){
					type=intent.getIntExtra("sms_or_phone", 0);
				}
				if(type!=4){
					return;
				}
				if(!isActive){
					hasUnreadSpam = true;
					return;
				}
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						loadAdapter();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								mAdapter.setmPosition(-1);
								mSelectCb.setChecked(false);
								mAdapter.notifyDataSetChanged();
								setNumTv(mCursors.size());
							}
						});

					}
				}).start();
			}
		};
	}
	
	
	private boolean hasSelectedItems(){
		boolean ret = false;
		for(int i = 0; i < mAdapter.getCount(); i++){
			HistoryNativeCursor hnc = (HistoryNativeCursor)mAdapter.getItem(i);
			if(hnc.isSelect()){
				ret = true;
			}
		}
		
		return ret;
	}
	private void toConfirmDialog(int id){
		switch(id){
		case 0:				
			//show confirm dialog
			final Dialog promptDialog = new Dialog(
					this.getParent(), R.style.softDialog);			
			promptDialog.setContentView(R.layout.soft_uninstall_prompt_dialog);
			TextView promptText = (TextView) promptDialog
					.findViewById(R.id.prompt_text);
			promptText.setText(R.string.confirm_add_selected_to_blacklist);
			Button button_ok = (Button) promptDialog
					.findViewById(R.id.button_ok);
			Button button_cancel = (Button) promptDialog
					.findViewById(R.id.button_cancel);
			button_ok.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					addToBlackList();			
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
			break;
		case 1:
			final Dialog dialog = new Dialog(
					this.getParent(), R.style.softDialog);			
			dialog.setContentView(R.layout.soft_uninstall_prompt_dialog);
			promptText = (TextView) dialog
					.findViewById(R.id.prompt_text);
			promptText.setText(R.string.confirm_to_delete_history);
			button_ok = (Button) dialog
					.findViewById(R.id.button_ok);
			button_cancel = (Button) dialog
					.findViewById(R.id.button_cancel);
			button_ok.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					deleteHistory();		
					dialog.dismiss();
				}
			});
			button_cancel
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
			dialog.show();
			break;
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.intercept_history_menu_add:
			//add intercept history to blacklist
			if(mAdapter.getCount() != 0){
				if(hasSelectedItems()){
					toConfirmDialog(0);
				}else{
					Toast.makeText(this, R.string.select_phones_to_addlist, Toast.LENGTH_LONG).show();
				}
			}
			
			BlockTabMain.showBottomLinear();
			mHistoryMenuLinear.setVisibility(View.GONE);
			mShowMenu = false;
			break;
		case R.id.intercept_history_menu_delete:
			//delete intercept history of call
			if(mAdapter.getCount() != 0){
				if(hasSelectedItems()){
					toConfirmDialog(1);
				}else{
					Toast.makeText(this, R.string.select_content_to_delete, Toast.LENGTH_LONG).show();
				}
			}			
			BlockTabMain.showBottomLinear();
			mHistoryMenuLinear.setVisibility(View.GONE);
			mShowMenu = false;
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

		if (mAdapter.getmPosition() == arg2) {
			// mAdapter=new InterceptHistoryAdapter(this,mCursors, 3);
			mAdapter.setmPosition(-1);
		} else {
			// mAdapter=new InterceptHistoryAdapter(this,mCursors, 3);
			mAdapter.setmPosition(arg2);
		}
		mAdapter.notifyDataSetChanged();
		// mListview.setAdapter(mAdapter);

	}

	private void loadAdapter() {
		InterceptDataBase.get(this).changeReadStatus(4);
		mCursors.clear();
		Cursor c = InterceptDataBase.get(this).selectAllInterceptHistoryList(4);
		if (c != null && c.getCount() > 0) {
			while (c.moveToNext()) {
				int id = c.getInt(c.getColumnIndex(InterceptDataBase.ID));
				String address = c.getString(c
						.getColumnIndex(InterceptDataBase.ADDRESS));
				String date = c.getString(c
						.getColumnIndex(InterceptDataBase.DATE));
				String remark = c.getString(c
						.getColumnIndex(InterceptDataBase.MARK));
				int read = c.getInt(c.getColumnIndex(InterceptDataBase.READ));
				HistoryNativeCursor hnc = new HistoryNativeCursor();
				hnc.setmId(id);
				hnc.setmAddress(address);
				hnc.setmDate(date);
				hnc.setmRemark(remark);
				hnc.setmRead(read);
				hnc.setmRequestType(4);
				mCursors.add(hnc);
			}
		}
		if (c != null) {
			c.close();
		}
		if(!isActive){
			return;
		}
		showNotification();

		

	}
	private void showNotification(){
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new NetTrafficNotification(getApplication(),
						InterceptHistoryPhone.this)
						.showInterceptNotification();
			}
		});
	}

	public void setNumTv(int num) {
		mInterceptNumTv.setText(num + "");

	}
	public void refreshCheckBox(boolean originTrue) {

		if (mSelectCb.isChecked() && !originTrue) {
			mCanUse = false;
			mSelectCb.setChecked(false);

		} else if (!mSelectCb.isChecked() && originTrue) {
			mCanUse = false;
			boolean isTrue = true;
			for (int i = 0; i < mCursors.size(); i++) {
				if (!mCursors.get(i).isSelect()) {
					isTrue = false;
					break;
				}
			}
			if (!isTrue) {
				mCanUse = true;
			}else{
				mSelectCb.setChecked(isTrue);
			}
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if(!mCanUse){
			mCanUse=true;
			return;
		}
		for (int i = 0; i < mCursors.size(); i++) {
			mCursors.get(i).setSelect(isChecked);
		}
		mAdapter.notifyDataSetChanged();

	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isActive=false;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

}