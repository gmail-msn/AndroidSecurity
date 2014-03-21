package com.kindroid.security.service;

import com.kindroid.security.R;
import com.kindroid.security.adapter.CleanListAdapter;
import com.kindroid.security.ui.ProcessManagerActivity;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.MemoryUtil;
import com.kindroid.security.util.PopViewAid;
import com.kindroid.security.util.ProcInfo;
import com.kindroid.security.util.TaskUtil;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.MemoryInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.TextView;

public class MemMonitorService extends Service{
	private static View sPopView;
	private static TextView sMemUsageTv;
	private static View sCleanView;
	private static WindowManager.LayoutParams sLayoutParams;
	private static WindowManager.LayoutParams sLayoutParams1;
	private static float sTouchStartX;
	private static float sTouchStartY;
	
	private static float sMoveEndX;
	private static float sMoveEndY;
	
	private DisplayMetrics mDisplayMetrics;
	private LayoutInflater mLayoutFlater;
	private boolean mRefresh;
	private static final int REFRESH_MEM_USAGE = 0;
	private DisplayMetrics mDisPlay;
	private WindowManager wm;
	private GridView mGridView;
	private CleanListAdapter mAdapter;
	private TextView mSummaryText;
	private View mCleanButton;
	private boolean mButtonIsClean = true;
	private TextView mCleanActionTv;
	private View mCloseCleanIv;
	private static final int TOUCH_TIMES = 10;
	private float mScreenDensity = 1;
	private static final int CLEAN_WINDOW_HEIGHT = 140;
	private static final int LEFT_SIDE = 0;
	private static final int RIGHT_SIDE = 1;
	
	
	private float preX;
	private float preY;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mRefresh = false;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mRefresh = true;
		mDisplayMetrics = new DisplayMetrics();
		wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
		mLayoutFlater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        mScreenDensity = metric.density;  
		initPopView();
		new RefreshMemUsageThread().start();
	}
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case REFRESH_MEM_USAGE:
				updateMemUsage();
				break;
			}			
		}
	};
	private class RefreshMemUsageThread extends Thread{
		public void run(){
			while(mRefresh){
				mHandler.sendEmptyMessage(REFRESH_MEM_USAGE);
				try{
					sleep(5000);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	private void initPopView(){
		sPopView = mLayoutFlater.inflate(R.layout.pop_window_layout, null);
		sMemUsageTv = (TextView)sPopView.findViewById(R.id.mem_usage_tv);
		updateMemUsage();
		mDisPlay = getResources().getDisplayMetrics();
		sLayoutParams = new LayoutParams();
		sLayoutParams.width = sLayoutParams.WRAP_CONTENT;
		sLayoutParams.height = sLayoutParams.WRAP_CONTENT;
		
		sLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;

		sLayoutParams.x = mDisPlay.widthPixels;
		sLayoutParams.y = 100;
		sMoveEndY = 100;
		sMoveEndX = mDisPlay.widthPixels;
		sLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		sLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		sLayoutParams.format = 1;
		sPopView.setOnTouchListener(new PopViewOnTouchListener());
		wm.addView(sPopView, sLayoutParams);
	}
	private class PopViewOnTouchListener implements View.OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			float x = event.getRawX();
			
			float y = event.getRawY() - 25; 
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:				
				sTouchStartX = event.getX();
				sTouchStartY = event.getY();
				preX= x;
				preY= y;
				sPopView.setBackgroundResource(R.drawable.traffic_pop_moiter_on);
				break;
			case MotionEvent.ACTION_MOVE:	
				if(Math.abs(x-preX)>5||Math.abs(y-preY)>8){
					updateViewPosition(x, y, wm);
					preX=x;
					preY=y;
				}
				break;
			case MotionEvent.ACTION_UP:
				sPopView.setBackgroundResource(R.drawable.traffic_pop_moiter);
				/*
				if(mTouchTimes > TOUCH_TIMES){
					if((x - sTouchStartX) > (mDisPlay.widthPixels / 2)){
						movePopViewToAside(x, y, 1);
					}else{
						movePopViewToAside(x, y, -1);
					}
				}else{
					sPopView.setVisibility(View.GONE);
					if(sCleanView == null){
						initCleanView();				
					}else{
						updateCleanContent();
					}
				}
				*/
				
				if((x -event.getX()) <=0 || (x - event.getX() + mScreenDensity * 80) >= mDisPlay.widthPixels){
					sPopView.setVisibility(View.GONE);
					if(sCleanView == null){
						initCleanView();				
					}else{
						updateCleanContent();
					}
				}else{
					if((x - sTouchStartX) > (mDisPlay.widthPixels / 2)){
						movePopViewToAside(x, y, 1);
					}else{
						movePopViewToAside(x, y, -1);						
					}
				}
				
				break;
			}

			return false;
		}
		
	}
	private void movePopViewToAside(float x, float y, int flag){
		sMoveEndY = y;
		
		switch(flag){
		case 1:
			while(x < mDisPlay.widthPixels){
				x++;				
				updateViewPosition(x, y, wm);
			}
			sMoveEndX = x;			
			break;
		case -1:
			while(x > 0){
				x--;
				updateViewPosition(x, y, wm);
			}
			sMoveEndX = x;
			break;
		}
		
	}
	private static void updateViewPosition(float x, float y, WindowManager wm) {
		// 更新浮动窗口位置参数
		sLayoutParams.x = (int) (x - sTouchStartX);
		sLayoutParams.y = (int) (y - sTouchStartY);
		wm.updateViewLayout(sPopView, sLayoutParams);
	}
	private int getMemUsage(){
		MemoryInfo memoryInfo = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(memoryInfo);
		long totalMemory = MemoryUtil.getTotalMemory();
		int usage = Double.valueOf(memoryInfo.availMem * 1.0 / totalMemory * 100).intValue();
		return usage;
	}
	
	private void updateMemUsage(){
		int usage = getMemUsage();
		sMemUsageTv.setText(usage + "%");
	}
	private void initCleanView(){
		sCleanView = mLayoutFlater.inflate(R.layout.one_key_clean, null);
		sLayoutParams1 = new LayoutParams();
		sLayoutParams1.width = sLayoutParams1.WRAP_CONTENT;
		sLayoutParams1.height = sLayoutParams1.WRAP_CONTENT;
		
		sLayoutParams1.x = (int)sMoveEndX;
		sLayoutParams1.y = (int)(sMoveEndY - mScreenDensity * CLEAN_WINDOW_HEIGHT);
		sLayoutParams1.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		sLayoutParams1.type = WindowManager.LayoutParams.TYPE_PHONE;
		sLayoutParams1.format = 1;
		mSummaryText = (TextView)sCleanView.findViewById(R.id.summary_text);
		mSummaryText.setText(String.format(getString(R.string.clean_summary_text), getMemUsage()) + "%");
		mCloseCleanIv = sCleanView.findViewById(R.id.close_pop_window_iv);
		mCloseCleanIv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sCleanView.setVisibility(View.GONE);
				sPopView.setVisibility(View.VISIBLE);
			}
		});
		mCleanButton = sCleanView.findViewById(R.id.clean_action_linear);
		mCleanActionTv = (TextView)sCleanView.findViewById(R.id.clean_action_tv);
		mCleanButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mButtonIsClean){
					for(int i = 0; i < mAdapter.getCount(); i++){
						ProcInfo item = (ProcInfo)mAdapter.getItem(i);
						TaskUtil.killProcess(item.getPackageName(), MemMonitorService.this);
					}
					mAdapter.clearItems();
					mAdapter.notifyDataSetChanged();
					mButtonIsClean = false;
					mCleanActionTv.setText(R.string.close);					
				}else{
					sCleanView.setVisibility(View.GONE);
					mCleanActionTv.setText(R.string.cache_management_one_key_clear);	
					sPopView.setVisibility(View.VISIBLE);
					mButtonIsClean = true;
				}
			}
		});
		mGridView = (GridView)sCleanView.findViewById(R.id.app_grid_view);
		mGridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				System.out.println("this is onItemClick");
			}
			
		});
		mAdapter = new CleanListAdapter(this);
		mAdapter.addItemAll(TaskUtil.getRunningApp(this));
		mGridView.setAdapter(mAdapter);
		
		wm.addView(sCleanView, sLayoutParams1);
	}
	
	private void updateCleanContent(){
		mAdapter.clearItems();
		mAdapter.addItemAll(TaskUtil.getRunningApp(this));
		mAdapter.notifyDataSetChanged();
		mSummaryText.setText(String.format(getString(R.string.clean_summary_text), getMemUsage()) + "%");
		sCleanView.setVisibility(View.VISIBLE);
		sLayoutParams1.x = (int)sMoveEndX;
		sLayoutParams1.y = (int)(sMoveEndY - mScreenDensity * CLEAN_WINDOW_HEIGHT);
		wm.updateViewLayout(sCleanView, sLayoutParams1);
	}

	class ScreenBroacastReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent == null || intent.getAction() == null)
				return;
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				if(!mRefresh){
					mRefresh = true;
					new RefreshMemUsageThread().start();
				}
			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				mRefresh = false;
			}

		}
	}
	
}
