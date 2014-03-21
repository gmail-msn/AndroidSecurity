/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.09
 * Description:
 */
package com.kindroid.security.ui;

import com.kindroid.security.R;
import com.kindroid.security.adapter.AddBlackWhiteAdapter;
import com.kindroid.security.adapter.BlackWhiteAdapter;
import com.kindroid.security.util.InterceptDataBase;
import com.kindroid.security.util.LoadListAdapterThread;
import com.kindroid.security.util.NativeCursor;
import com.kindroid.security.util.UtilDailog;

import android.app.Activity;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.database.Cursor;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class AddBlackWhiteListActivity extends Activity implements
		View.OnClickListener,OnItemClickListener {

	/** Called when the activity is first created. */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
	
	private ListView mListView;
	private TextView mfucTv;
	private int mType;
	
	private String []mText;
	private int[]mId;
	private AddBlackWhiteAdapter mAddBlackWhiteAdapter;

	public static final String BLACK_OR_WHITE = "black_or_white";
	public static final String SOURCE_TYPE = "source_type";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.add_black_whitelist_activity);
		findView();
		Bundle bundle=getIntent().getExtras();

		if(bundle!=null&&bundle.getInt(BLACK_OR_WHITE)==1){
			mType=1;
			mfucTv.setText(R.string.add_black_list);
			mId=new int[]{R.string.manually_add,R.string.add_from_mobile_contact,R.string.add_from_log,R.string.add_from_sms,R.string.add_black_area};
		}else{
			mType=2;
			mId=new int[]{R.string.manually_add,R.string.add_from_mobile_contact,R.string.add_from_log,R.string.add_from_sms,R.string.add_white_area};
			mfucTv.setText(R.string.add_white_list);
		}
		initText();
		mAddBlackWhiteAdapter=new AddBlackWhiteAdapter(this, mText);
		mListView.setAdapter(mAddBlackWhiteAdapter);
		
	}
	private void initText(){
		mText=new String[mId.length];
		for (int i = 0; i < mId.length; i++) {
			mText[i]=getString(mId[i]);
		}
	}

	private void findView() {
		mListView=(ListView) findViewById(R.id.add_listview);
		mfucTv=(TextView) findViewById(R.id.function_title_tv);
		bindListerToView();
	}
	private void bindListerToView(){
		mListView.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch (arg2) {
		case 0:
			NativeCursor nc = new NativeCursor();
			nc.setmRequestType(mType);
			Dialog dialog=UtilDailog.getInsertBlackWhiteDialog(AddBlackWhiteListActivity.this,1,nc);
			dialog.show();
			
			break;


		case 1:
			Intent intent = new Intent(this, AddListFromContactsActivity.class);
			intent.putExtra(BLACK_OR_WHITE, mType);
			intent.putExtra(SOURCE_TYPE, LoadListAdapterThread.CONTACTS_SOURCE_TYPE);
			startActivity(intent);
			break;
		/*
		case 2:
			intent = new Intent(this, AddListFromContactsActivity.class);
			intent.putExtra(BLACK_OR_WHITE, mType);
			intent.putExtra(SOURCE_TYPE, LoadListAdapterThread.SIM_CONTACTS_SOURCE_TYPE);
			startActivity(intent);
			break;
		*/
		case 2:
			intent = new Intent(this, AddListFromContactsActivity.class);
			intent.putExtra(BLACK_OR_WHITE, mType);
			intent.putExtra(SOURCE_TYPE, LoadListAdapterThread.CALL_LOG_SOURCE_TYPE);
			startActivity(intent);
			break;
		case 3:
			intent = new Intent(this, AddListFromContactsActivity.class);
			intent.putExtra(BLACK_OR_WHITE, mType);
			intent.putExtra(SOURCE_TYPE, LoadListAdapterThread.SMS_SOURCE_TYPE);
			startActivity(intent);
			break;
		case 4:
			nc = new NativeCursor();
			nc.setmRequestType(mType);
			dialog=UtilDailog.getInceptBlackWhiteDialog(AddBlackWhiteListActivity.this,1,nc);
			dialog.show();
			
			break;

		default:
			break;
		}
		
	}

}