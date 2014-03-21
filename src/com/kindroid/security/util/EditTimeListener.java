/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-09
 * Description:
 */
package com.kindroid.security.util;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * @author heli.zhao
 *
 */
public class EditTimeListener implements OnClickListener {
	private EditText mTimeEt;
	private Context mContext;
	public EditTimeListener(Context context, EditText et){
		mTimeEt = et;
		mContext = context;
	}
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	
	@Override	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Calendar c = Calendar.getInstance();
		Dialog dialog = new TimePickerDialog(
				mContext, 
                new TimePickerDialog.OnTimeSetListener(){
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    	mTimeEt.setText(hourOfDay+":"+minute);                    	
                    }
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            );
		dialog.show();
	}
	
}
