/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:heli.zhao
 * Date:2011.09
 * Description:
 */
package com.kindroid.security.ui;

import com.kindroid.security.R;
import com.kindroid.security.adapter.AddBlackListAdapter;
import com.kindroid.security.model.BlackListItem;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.InterceptDataBase;
import com.kindroid.security.util.LoadListAdapterThread;
import com.kindroid.security.util.NativeCursor;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AddListFromContactsActivity extends ListActivity {
	private TextView mContactsSum;
	private TextView mActivityTitle;
	private TextView mActionText;
	private CheckBox mSelectAllCheckBox;
	private View mLoadingLinear;
	private View mAddActionLinear;
	
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
	
	private boolean isLoadingData = false;
	private int mType;
	private int mSourceType;
	private AddBlackListAdapter mListAdapter;
	public static final int FINISH_LOAD_FROM_CONTACTS = 1;
	public static final int LOAD_FROM_SIM_ERROR = 2;
	public static final int FINISH_LOAD_FROM_SIM = 3;
	public static final int FINISH_LOAD_FROM_CALL_LOG = 4;
	public static final int FINISH_LOAD_FROM_SMS = 5;
	public static final int NO_SIM_EXIST = 6;
	
	private boolean mOnlyChangeState = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_list_from_contacts);
		mType = getIntent().getIntExtra(AddBlackWhiteListActivity.BLACK_OR_WHITE, 1);
		mSourceType = getIntent().getIntExtra(AddBlackWhiteListActivity.SOURCE_TYPE, 1);
		findView();
		loadListAdapter();
	}
	private void findView(){
		mContactsSum = (TextView)findViewById(R.id.contacts_sum);
		mContactsSum.setVisibility(View.GONE);
		mActivityTitle = (TextView)findViewById(R.id.activity_title_tv);
		mActionText = (TextView)findViewById(R.id.add_list_action_tv);
		if(mType == 1){
			mActivityTitle.setText(R.string.add_black_list);
			mActionText.setText(R.string.add_black_list);
		}else{
			mActivityTitle.setText(R.string.add_white_list);
			mActionText.setText(R.string.add_white_list);
		}
		mSelectAllCheckBox = (CheckBox)findViewById(R.id.select_al_cb);		
		mSelectAllCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(!mOnlyChangeState){
					mListAdapter.selectAllItem(isChecked);
					mListAdapter.notifyDataSetChanged();
				}
			}
			
		});
		mLoadingLinear = findViewById(R.id.loading_linear);
		mAddActionLinear = findViewById(R.id.add_list_linear);
		mAddActionLinear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				addListFromAdapter();
			}
		});
	}
	private void addListFromAdapter(){
		List<BlackListItem> items = new ArrayList<BlackListItem>();
		for(int i = 0; i < mListAdapter.getCount(); i++){
			BlackListItem item = (BlackListItem)mListAdapter.getItem(i);
			if(item.isSelected()){
				items.add(item);
			}
		}
		if(items.size() == 0){
			Toast.makeText(this, R.string.add_list_no_select, Toast.LENGTH_LONG).show();
			return;
		}
		addListToDB(items);		
		//sendBroadcast(new Intent(Constant.BROACTUPDATEINTERCEPT));
		finish();
	}
	private void addListToDB(List<BlackListItem> items){
		InterceptDataBase mInterDB = InterceptDataBase.get(this);
		boolean allIsExist = true;
		for(BlackListItem item : items){
			NativeCursor nc = new NativeCursor();
			nc.setmRequestType(mType);
			nc.setmContactName(item.getContactName());
			nc.setmPhoneNum(item.getPhoneNumber());			
			nc = InterceptDataBase.get(this).selectIsExists(nc);
			if (!nc.ismIsExists()) {
				allIsExist = false;
				String contactName = item.getContactName();
				if(contactName != null && contactName.equals(item.getPhoneNumber())){
					contactName = "";
				}
				mInterDB.insertBlackWhitList(mType, contactName, 
						item.getPhoneNumber(), true, true, 1);
			}
		}
		if(allIsExist){
			Toast.makeText(this, R.string.all_exist_when_addlist, Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(this, R.string.add_list_complete, Toast.LENGTH_LONG).show();
		}

	}
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case FINISH_LOAD_FROM_CONTACTS:
				setListAdapter(mListAdapter);
				mContactsSum.setText(String.format(getString(R.string.contacts_exist), mListAdapter.getCount()));
				mContactsSum.setVisibility(View.VISIBLE);
				isLoadingData = false;
				mLoadingLinear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				break;
			case LOAD_FROM_SIM_ERROR:
				mContactsSum.setText(String.format(getString(R.string.contacts_exist), 0));
				mContactsSum.setVisibility(View.VISIBLE);
				isLoadingData = false;
				mLoadingLinear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				Toast.makeText(AddListFromContactsActivity.this, R.string.load_from_sim_error, Toast.LENGTH_LONG).show();
				break;
			case FINISH_LOAD_FROM_SIM:
				setListAdapter(mListAdapter);
				mContactsSum.setText(String.format(getString(R.string.sim_contacts_exist), mListAdapter.getCount()));
				mContactsSum.setVisibility(View.VISIBLE);
				isLoadingData = false;
				mLoadingLinear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				break;
			case FINISH_LOAD_FROM_CALL_LOG:
				setListAdapter(mListAdapter);
				mContactsSum.setText(String.format(getString(R.string.call_log_contacts_exist), mListAdapter.getCount()));
				mContactsSum.setVisibility(View.VISIBLE);
				isLoadingData = false;
				mLoadingLinear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				break;
			case FINISH_LOAD_FROM_SMS:
				setListAdapter(mListAdapter);
				mContactsSum.setText(String.format(getString(R.string.sms_contacts_exist), mListAdapter.getCount()));
				mContactsSum.setVisibility(View.VISIBLE);
				isLoadingData = false;
				mLoadingLinear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				break;
			}
		}
	};
	private void loadListAdapter(){
		getListView().setVisibility(View.GONE);
		mLoadingLinear.setVisibility(View.VISIBLE);
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
		TextView prompt_dialog_text = (TextView)findViewById(R.id.prompt_dialog_text);
		prompt_dialog_text.setText(R.string.loading_virus_history_prompt_text);
		mListAdapter = new AddBlackListAdapter(this);
		LoadListAdapterThread loadThread = new LoadListAdapterThread(this, handler, mSourceType, mListAdapter);
		isLoadingData = true;
		loadThread.start();
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
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		CheckBox select_cb = (CheckBox)v.findViewById(R.id.select_cb);
		select_cb.setChecked(!select_cb.isChecked());
		BlackListItem item = (BlackListItem)mListAdapter.getItem(position);
		item.setSelected(select_cb.isChecked());
		boolean mAllSelected = true;
		for(int i = 0; i < mListAdapter.getCount(); i++){
			item = (BlackListItem)mListAdapter.getItem(i);
			if(!item.isSelected()){
				mAllSelected = false;
			}
		}
		mOnlyChangeState = true;
		mSelectAllCheckBox.setChecked(mAllSelected);
		mOnlyChangeState = false;
		//super.onListItemClick(l, v, position, id);
		
	}

	

}
