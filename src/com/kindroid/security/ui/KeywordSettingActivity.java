/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-09
 * Description:
 */
package com.kindroid.security.ui;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.security.R;
import com.kindroid.security.adapter.InterceptKeywordAdapter;
import com.kindroid.security.util.InterceptDataBase;

/**
 * @author heli.zhao
 *
 */
public class KeywordSettingActivity extends Activity implements View.OnClickListener{
	private InterceptKeywordAdapter mAdapter;
	private Cursor mCursor;
	
	private View mAddActionLinear;
	private ListView mListView;
	private EditText mKeywordInputText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intercept_keyword_setting);
		mCursor = InterceptDataBase.get(this).selectKeyWordList(1);
		mAdapter = new InterceptKeywordAdapter(this, mCursor);
		mListView = (ListView)findViewById(R.id.keyword_listview);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new ListItemClickListener());
		mKeywordInputText = (EditText)findViewById(R.id.key_word_edit_text);
		mAddActionLinear = findViewById(R.id.add_action_linear);
		mAddActionLinear.setOnClickListener(this);
	}
	private class ListItemClickListener implements OnItemClickListener{

		/* (non-Javadoc)
		 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
		 */
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			final Dialog promptDialog1 = new Dialog(KeywordSettingActivity.this, R.style.softDialog);
			View view1 = LayoutInflater.from(KeywordSettingActivity.this).inflate(
					R.layout.soft_uninstall_prompt_dialog, null);
			promptDialog1.setContentView(view1);

			TextView promptText = (TextView) promptDialog1
					.findViewById(R.id.prompt_text);
			promptText.setText(R.string.confirm_delete_keyword);
			View button_ok = promptDialog1.findViewById(R.id.button_ok);
			View button_cancel = promptDialog1.findViewById(R.id.button_cancel);
			button_ok.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog1.dismiss();
					String keyword = mCursor.getString(mCursor.getColumnIndex(InterceptDataBase.KEYWORDZH));
					InterceptDataBase.get(KeywordSettingActivity.this).deleteKeyword(keyword, 1);
					mCursor.requery();
				}
			});
			button_cancel.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					promptDialog1.dismiss();
				}
			});
			promptDialog1.show();
		
	
		}
		
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.add_action_linear:
			String kw = mKeywordInputText.getText().toString();
			if(TextUtils.isEmpty(kw)){
				Toast.makeText(this, R.string.search_keyword_error, Toast.LENGTH_LONG).show();
				return;
			}
			boolean isExist = InterceptDataBase.get(this).selectKeyWord(kw, 1);
			if(isExist){
				Toast.makeText(this, R.string.intercept_keyword_exist, Toast.LENGTH_LONG).show();
				return;
			}
			InterceptDataBase.get(this).insertKeyWord(kw, 1);
			mCursor.requery();
			mKeywordInputText.setText("");
			break;
		}
	}
	

}
