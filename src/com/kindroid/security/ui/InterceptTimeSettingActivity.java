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
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.kindroid.security.R;
import com.kindroid.security.adapter.DaySettingListAdapter;
import com.kindroid.security.model.DayListItem;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.UtilDailog;

import java.util.Calendar;

/**
 * @author heli.zhao
 * 
 */
public class InterceptTimeSettingActivity extends Activity implements
		View.OnClickListener {

	private TextView mStartTimeTv;
	private TextView mEndTimeTv;
	private TextView mDaySettingTv;
	private TextView mCurrentModeTv;

	private View mSetStartTimeLinear;
	private View mSetEndTimeLinear;
	private View mSetDayLinear;
	private View mSelectModeLinear;
	private View mEnableNodistModeLinear;
	private TextView mEnableNodistText;
	private CheckBox mEnableNodistCb;

	public static final String WORK_DAY_STR = "1,2,3,4,5";
	public static final String REST_DAY_STR = "6,7";
	public static final String ALL_DAY_STR = "1,2,3,4,5,6,7";
	private String[] dayUnits;
	private String[] mInterceptModeNames;
	private String[] mWeeklyDays;
	private boolean mEnableNodistMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intercept_time_setting);
		dayUnits = getResources().getStringArray(R.array.nodisturb_day_unit);
		mInterceptModeNames = getResources().getStringArray(
				R.array.intercept_mode_name);
		findViews();
	}

	private void findViews() {
		mEnableNodistCb = (CheckBox) findViewById(R.id.enable_nodist_mode_cb);
		mEnableNodistText = (TextView) findViewById(R.id.enable_nodist_mode_text);
		mEnableNodistModeLinear = findViewById(R.id.enable_nodisturb_mode_linear);
		mStartTimeTv = (TextView) findViewById(R.id.start_time);
		mEndTimeTv = (TextView) findViewById(R.id.end_time);
		mDaySettingTv = (TextView) findViewById(R.id.day_time);
		mCurrentModeTv = (TextView) findViewById(R.id.current_mode);
		mSetStartTimeLinear = findViewById(R.id.set_start_time_linear);
		mSetEndTimeLinear = findViewById(R.id.set_end_time_linear);
		mSetDayLinear = findViewById(R.id.set_day_linear);
		mSelectModeLinear = findViewById(R.id.select_mode_linear);
		mSetStartTimeLinear.setOnClickListener(this);
		mSetEndTimeLinear.setOnClickListener(this);
		mSetDayLinear.setOnClickListener(this);
		mSelectModeLinear.setOnClickListener(this);
		mEnableNodistModeLinear.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// refresh UI
		refreshUI();
	}

	private void refreshUI() {
		SharedPreferences sp = KindroidSecurityApplication.sh;
		int interceptMode = sp.getInt(
				Constant.SHAREDPREFERENCES_NIGHTBLOCKINGRULES, 0);
		if (interceptMode == 0) {
			mEnableNodistText.setText(R.string.enable_nodisturb_mode);
			mEnableNodistCb.setChecked(false);
			mEnableNodistMode = false;
			mSetStartTimeLinear.setVisibility(View.GONE);
			mSetEndTimeLinear.setVisibility(View.GONE);
			mSetDayLinear.setVisibility(View.GONE);
			mSelectModeLinear.setVisibility(View.GONE);
		} else {
			mEnableNodistMode = true;
			mEnableNodistText.setText(R.string.disable_nodisturb_mode);
			mEnableNodistCb.setChecked(true);
			String startTime = sp.getString(Constant.NODISTURB_START_TIME,
					"23:30");
			String endTime = sp.getString(Constant.NODISTURB_END_TIME, "6:00");
			String dayStr = sp.getString(Constant.NODISTURB_DAY_TIME,
					"1,2,3,4,5");
			/*
			 * String startTimeFm = String.format(
			 * getString(R.string.intercept_time_setting_start_time),
			 * startTime); String endTimeFm = String.format(
			 * getString(R.string.intercept_time_setting_end_time), endTime);
			 */
			String dayStrFm = formatDayStr(dayStr);
			mStartTimeTv.setText(startTime);
			mEndTimeTv.setText(endTime);
			mDaySettingTv.setText(dayStrFm);
			mCurrentModeTv.setText(mInterceptModeNames[interceptMode]);
			mSetStartTimeLinear.setVisibility(View.VISIBLE);
			mSetEndTimeLinear.setVisibility(View.VISIBLE);
			mSetDayLinear.setVisibility(View.VISIBLE);
			mSelectModeLinear.setVisibility(View.VISIBLE);
		}
	}

	private String formatDayStr(String dayStr) {
		if (TextUtils.isEmpty(dayStr)) {
			return getString(R.string.account_man_obtain_email_error);
		}
		if (dayStr.equals(WORK_DAY_STR)) {
			return getString(R.string.intercept_time_setting_work_day);
		}
		if (dayStr.equals(REST_DAY_STR)) {
			return getString(R.string.intercept_time_setting_rest_day);
		}
		if (dayStr.equals(ALL_DAY_STR)) {
			return getString(R.string.all_weekly_day);
		}
		String[] token = dayStr.split(",");

		StringBuilder ret = new StringBuilder(
				getString(R.string.intercept_time_setting_day_start));

		for (int i = 0; i < token.length; i++) {
			try {
				int nm = Integer.parseInt(token[i]);
				ret.append(dayUnits[nm - 1]);
				if (i < (token.length - 1)) {
					ret.append(getString(R.string.intercept_time_setting_day_separator));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret.toString();
	}

	private void bindListenerForDialog(final Dialog dialog, int id) {
		switch (id) {
		case 0:
			final EditText startEt = (EditText) dialog
					.findViewById(R.id.start_time_et);
			final EditText endEt = (EditText) dialog
					.findViewById(R.id.end_time_et);
			Button mBtOk = (Button) dialog.findViewById(R.id.button_ok);
			mBtOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String startTime = startEt.getText().toString().trim();
					String endTime = endEt.getText().toString().trim();
					if (TextUtils.isEmpty(startTime)) {
						Toast.makeText(
								InterceptTimeSettingActivity.this,
								R.string.intercept_time_setting_start_time_error,
								Toast.LENGTH_LONG).show();
						return;
					}
					if (TextUtils.isEmpty(endTime)) {
						Toast.makeText(InterceptTimeSettingActivity.this,
								R.string.intercept_time_setting_end_time_error,
								Toast.LENGTH_LONG).show();
						return;
					}
					if (startTime.equals(endTime)) {
						Toast.makeText(
								InterceptTimeSettingActivity.this,
								R.string.intercept_time_setting_end_equals_start,
								Toast.LENGTH_LONG).show();
						return;
					}
					SharedPreferences sp = KindroidSecurityApplication.sh;
					Editor editor = sp.edit();
					editor.putString(Constant.NODISTURB_START_TIME, startTime);
					editor.putString(Constant.NODISTURB_END_TIME, endTime);
					editor.commit();
					String startTimeFm = String
							.format(getString(R.string.intercept_time_setting_start_time),
									startTime);
					String endTimeFm = String
							.format(getString(R.string.intercept_time_setting_end_time),
									endTime);
					mStartTimeTv.setText(Html.fromHtml(startTimeFm));
					mEndTimeTv.setText(Html.fromHtml(endTimeFm));
					dialog.dismiss();
				}
			});
			break;
		case 1:
			mBtOk = (Button) dialog.findViewById(R.id.button_ok);
			final ListView mListView = (ListView) dialog
					.findViewById(R.id.day_list_view);
			mBtOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					StringBuilder sb = new StringBuilder();
					ListAdapter mAdapter = mListView.getAdapter();
					boolean hasSelected = false;
					for (int i = 0; i < mAdapter.getCount(); i++) {
						DayListItem item = (DayListItem) mAdapter.getItem(i);
						if (item.isSelected()) {
							hasSelected = true;
							if (sb.length() > 0) {
								sb.append(',').append(item.getId());
							} else {
								sb.append(item.getId());
							}
						}
					}
					
					String dayStr = sb.toString();
					SharedPreferences sp = KindroidSecurityApplication.sh;
					Editor editor = sp.edit();
					editor.putString(Constant.NODISTURB_DAY_TIME, dayStr);
					editor.commit();
					dialog.dismiss();					
					mDaySettingTv.setText(formatDayStr(dayStr));
				}
			});
			break;
		}
	}

	private void loadDaySettingListAdapter(Dialog dialog) {
		ListView mListView = (ListView) dialog.findViewById(R.id.day_list_view);
		final DaySettingListAdapter mListAdapter = new DaySettingListAdapter(
				this);
		mWeeklyDays = getResources().getStringArray(
				R.array.intercept_day_setting);
		SharedPreferences sp = KindroidSecurityApplication.sh;
		String dayStr = sp.getString(Constant.NODISTURB_DAY_TIME, "1,2,3,4,5");
		int i = 1;
		for (String str : mWeeklyDays) {
			DayListItem item = new DayListItem();
			item.setId(i);
			item.setName(str);
			if (dayStr.contains(i + "")) {
				item.setSelected(true);
			} else {
				item.setSelected(false);
			}
			mListAdapter.addItem(item);
			i++;
		}
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				DayListItem item = (DayListItem) mListAdapter.getItem(position);
				item.setSelected(!item.isSelected());
				CheckBox cb = (CheckBox) view.findViewById(R.id.select_cb);
				cb.setChecked(item.isSelected());
			}

		});
	}
	private void setStartTime(){
		final SharedPreferences sp = KindroidSecurityApplication.sh;
		String startTime = sp.getString(Constant.NODISTURB_START_TIME,"23:30");
		String[] token = startTime.split(":");
		String hour = token[0];
		String mini = token[1];
		
		Dialog dialog = new TimePickerDialog(
				this, 
                new TimePickerDialog.OnTimeSetListener(){
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    	Editor editor = sp.edit();
                    	if(minute < 10){
                    		String time = hourOfDay + ":0" + minute;
                    		editor.putString(Constant.NODISTURB_START_TIME, time);
                    		mStartTimeTv.setText(time);
                    	}else{
                    		String time = hourOfDay + ":" + minute;
                    		editor.putString(Constant.NODISTURB_START_TIME, time);
                    		mStartTimeTv.setText(time);
                    	}
    					editor.commit();            	
                    }
                },
                Integer.parseInt(hour),
                Integer.parseInt(mini),
                true
            );
		dialog.show();
	}
	private void setEndTime(){
		final SharedPreferences sp = KindroidSecurityApplication.sh;
		String endTime = sp.getString(Constant.NODISTURB_END_TIME, "6:00");
		String[] token = endTime.split(":");
		String hour = token[0];
		String mini = token[1];
		Dialog dialog = new TimePickerDialog(
				this, 
                new TimePickerDialog.OnTimeSetListener(){
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    	//
                    	String startTime = sp.getString(Constant.NODISTURB_START_TIME,"23:30");
                    	String[] token = startTime.split(":");
                		String hour = token[0];
                		String mini = token[1];
                		try{
                			if(hourOfDay == Integer.parseInt(hour) && minute == Integer.parseInt(mini)){
                				Toast.makeText(InterceptTimeSettingActivity.this, R.string.intercept_time_setting_end_equals_start, Toast.LENGTH_LONG).show();
                				return;
                			}
                		}catch(Exception e){
                			e.printStackTrace();
                		}
                    	
                    	Editor editor = sp.edit();
                    	if(minute < 10){
                    		String time = hourOfDay + ":0" + minute;
                    		editor.putString(Constant.NODISTURB_END_TIME, time);
                    		mEndTimeTv.setText(time);
                    	}else{
                    		String time = hourOfDay + ":" + minute;
                    		editor.putString(Constant.NODISTURB_END_TIME, time);
                    		mEndTimeTv.setText(time);
                    	}
    					editor.commit();             	
                    }
                },
                Integer.parseInt(hour),
                Integer.parseInt(mini),
                true
            );
		dialog.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.set_start_time_linear:
			setStartTime();
			break;
		case R.id.set_end_time_linear:
			setEndTime();
			break;
		case R.id.set_day_linear:
			Dialog dialog = UtilDailog.getDaySettingDialog(this);
			loadDaySettingListAdapter(dialog);
			bindListenerForDialog(dialog, 1);
			dialog.show();
			break;
		case R.id.select_mode_linear:
			Intent intent = new Intent(this,
					NoDisturbModeSettingListActivity.class);
			startActivity(intent);
			break;
		case R.id.enable_nodisturb_mode_linear:
			if (mEnableNodistMode) {
				// disable nodist mode
				SharedPreferences sp = KindroidSecurityApplication.sh;
				Editor editor = sp.edit();
				editor.putInt(Constant.SHAREDPREFERENCES_NIGHTBLOCKINGRULES, 0);
				editor.commit();
				mEnableNodistMode = false;
			} else {
				// enable nodist mode
				SharedPreferences sp = KindroidSecurityApplication.sh;
				Editor editor = sp.edit();
				editor.putInt(Constant.SHAREDPREFERENCES_NIGHTBLOCKINGRULES, 1);
				editor.commit();
				mEnableNodistMode = true;
			}
			refreshUI();
			break;
		}
	}

}
