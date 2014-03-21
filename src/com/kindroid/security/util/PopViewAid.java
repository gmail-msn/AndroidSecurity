/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

import com.kindroid.security.R;

public class PopViewAid {

	public static LinearLayout linear;
	public static TextView tv;
	public static WindowManager.LayoutParams localLayoutParams1;

	private static float mTouchStartX;
	private static float mTouchStartY;

	public static LinearLayout titleLinear;

	public static void initPopView(Context context) {
		if (linear != null) {
			return;
		}
		final WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics disPlay = context.getResources().getDisplayMetrics();
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(localDisplayMetrics);
		localLayoutParams1 = new LayoutParams();
		localLayoutParams1.width = localLayoutParams1.WRAP_CONTENT;
		localLayoutParams1.height = localLayoutParams1.WRAP_CONTENT;
		
		localLayoutParams1.gravity = Gravity.LEFT | Gravity.TOP;

		localLayoutParams1.x = getWinX(disPlay.widthPixels);
		localLayoutParams1.y = getWinY();
		localLayoutParams1.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		localLayoutParams1.type = WindowManager.LayoutParams.TYPE_PHONE;
		localLayoutParams1.format = 1;
/*
		tv = new TextView(context);
		tv.setTextColor(Color.WHITE);
		tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		tv.setPadding(5, 0, 0, 0);
		linear = new LinearLayout(context);
		linear.setBackgroundResource(R.drawable.traffic_pop_moiter);
		linear.addView(tv, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
*/
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		linear = (LinearLayout)inflater.inflate(R.layout.pop_view_layout, null);
		tv = (TextView)linear.findViewById(R.id.traffic_pop_text);
		
		linear.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				float x = event.getRawX();
				float y = event.getRawY() - 25; 
				switch (event.getAction()) {
				
				case MotionEvent.ACTION_DOWN:
					mTouchStartX = event.getX();
					mTouchStartY = event.getY();
					linear.setBackgroundResource(R.drawable.traffic_pop_moiter_on);
					
					break;
				case MotionEvent.ACTION_MOVE:
					updateViewPosition(x, y, wm);
					break;
				case MotionEvent.ACTION_UP:
					updateViewPosition(x, y, wm);
					Editor editor = KindroidSecurityApplication.sh.edit();
					editor.putInt(Constant.SHAREDPREFERENCES_WINX,
							(int) (x - mTouchStartX));
					editor.putInt(Constant.SHAREDPREFERENCES_WINy,
							(int) (y - mTouchStartY));
					editor.commit();
					linear.setBackgroundResource(R.drawable.traffic_pop_moiter);
					mTouchStartX = mTouchStartY = 0;
					break;
				}

				return false;
			}
		});

		wm.addView(linear, localLayoutParams1);
	}

	public static void addTitleLinear(Context context) {
		if (titleLinear != null)
			return;
		final WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics disPlay = context.getResources().getDisplayMetrics();
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(localDisplayMetrics);
		localLayoutParams1 = new LayoutParams();
		localLayoutParams1.width = localDisplayMetrics.widthPixels;
		localLayoutParams1.height = 25;
		localLayoutParams1.gravity = Gravity.LEFT | Gravity.TOP;

		localLayoutParams1.x = 0;
		localLayoutParams1.y = 0;
		localLayoutParams1.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		localLayoutParams1.type = WindowManager.LayoutParams.TYPE_PHONE;
		localLayoutParams1.format = 1;

		tv = new TextView(context);
		tv.setTextColor(Color.WHITE);
		tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		tv.setPadding(2, 0, 0, 0);
		linear = new LinearLayout(context);
		linear.setBackgroundResource(R.drawable.traffic_pop_moiter);
		linear.addView(tv, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT));

		linear.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				float x = event.getRawX();
				float y = event.getRawY() - 25; // 25是系统状态栏的高度
				
				switch (event.getAction()) {
				// 手指按下时
				case MotionEvent.ACTION_DOWN:
					// 获取相对View的坐标，即以此View左上角为原点
					mTouchStartX = event.getX();
					mTouchStartY = event.getY();
					linear.setBackgroundResource(R.drawable.traffic_pop_moiter_on);
					
					break;
				// 手指移动时
				case MotionEvent.ACTION_MOVE:
					// 更新视图
					updateViewPosition(x, y, wm);
					break;
				// 手指松开时
				case MotionEvent.ACTION_UP:
					updateViewPosition(x, y, wm);
					Editor editor = KindroidSecurityApplication.sh.edit();
					editor.putInt(Constant.SHAREDPREFERENCES_WINX,
							(int) (x - mTouchStartX));
					editor.putInt(Constant.SHAREDPREFERENCES_WINy,
							(int) (y - mTouchStartY));
					editor.commit();
					linear.setBackgroundResource(R.drawable.traffic_pop_moiter);
					mTouchStartX = mTouchStartY = 0;
					break;
				}

				return false;
			}
		});

		wm.addView(linear, localLayoutParams1);

	}

	private static void updateViewPosition(float x, float y, WindowManager wm) {
		// 更新浮动窗口位置参数
		localLayoutParams1.x = (int) (x - mTouchStartX);
		localLayoutParams1.y = (int) (y - mTouchStartY);
		wm.updateViewLayout(linear, localLayoutParams1);
	}

	public static void removePopView(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		if (linear != null) {
			wm.removeView(linear);
			linear = null;
			tv = null;
		}
	}

	public static void setText(long total, boolean type) {
		if (tv != null) {
			if (type) {
				DecimalFormat df = new DecimalFormat("###");
				df.setMinimumFractionDigits(0);
				if (total >= 3072) {
					tv.setText(df.format((double) total / (1024 * 3)) + " K/S");
				} else {
					tv.setText(df.format((double) total / 3) + " B/S");
				}

			} else {
				tv.setText(MemoryUtil.formatMemorySize(1, total) + " M");
			}
		}
	}

	private static int getWinX(int width) {
		return KindroidSecurityApplication.sh.getInt(
				Constant.SHAREDPREFERENCES_WINX, width);

	}

	private static int getWinY() {
		return KindroidSecurityApplication.sh.getInt(
				Constant.SHAREDPREFERENCES_WINy, 0);
	}

}
