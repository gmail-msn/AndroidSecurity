/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:zili.chen
 * Date:2011.07
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import org.achartengine.GraphicalView;
import org.achartengine.chart.PieChart;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.kindroid.security.R;
import com.kindroid.security.model.IModel;
import com.kindroid.security.model.IModelListener;
import com.kindroid.security.model.IOperation;
import com.kindroid.security.model.Interface;
import com.kindroid.security.model.NetTrafficModel;
import com.kindroid.security.model.TrafficCounter;
import com.kindroid.security.util.ApkManager;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.HistoryNativeCursor;
import com.kindroid.security.util.HttpRequest;
import com.kindroid.security.util.InterceptDataBase;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.MemoryUtil;
import com.kindroid.security.util.ProcInfo;
import com.kindroid.security.util.TaskUtil;
import com.kindroid.security.util.UtilShareprefece;
import com.kindroid.security.util.Utilis;

public class MobileExpActivity extends Activity implements View.OnClickListener {

	public final static int UPGRADE_VERSION = 1001;
	public final static int UPGRADE_VERSION_NO = 1002;

	private UpgradeDialog upgradeDialog;
	private static String downUrl;

	private final int SETPBTOZERO = 0;
	private final int UPDATEPB = 1;
	private final int FILLDATATOVIEW = 2;
	private final int CLOSEPROCE = 3;
	private final int SHOWONEKEYLINEAR = 4;

	private LinearLayout linear01;
	private LinearLayout linear02;
	private LinearLayout linear03;
	private LinearLayout linear04;
	private LinearLayout memory_container_linear;

	TextView left_memory_tv;
	TextView install_soft_tv;
	TextView left_space_tv;

	TextView hasuse_stream_tv;
	TextView left_stream_tv;

	private TextView mInterceptSmsTv;
	private TextView mInterceptPhoneTv;

	private TextView mExamPromptTv;
	private View mLastExamSummaryLinear;
	private TextView mLastExamPromptTv;
	private TextView mLastExamScoreTv;
	private RatingBar mExamRatingBar;
	private View mExamMeLinear;
	private TextView mLastUnoptimizedPromptTv;
	
	ProgressBar left_network_pb;
	List<PackageInfo> packs = new ArrayList<PackageInfo>();
	ArrayList<ProcInfo> mProcList = new ArrayList<ProcInfo>();
	private KindroidSecurityApplication mApp;
	private NetTrafficModel mModel = null;
	ActivityManager activityManager;
	PackageManager pckMan;
	int score;
	int leftMemoryProcess;

	Thread onekeyThread;
	boolean isActive = true;
	private static String releaseNotes;
	private static int newVersion;
	private boolean mComeFromCreate = false;
	
	private static final long MILLTIMES_PER_MONTH = 2592000000L;
	private static final long MILLTIMES_PER_DAY = 86400000L;

	protected Handler mGlobalHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (!isActive)
				return;
			switch (msg.what) {
			case UPGRADE_VERSION:
				showUpgradeDialog();
				break;
			case UPGRADE_VERSION_NO:

				break;
			default:
				break;
			}
		}
	};

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			//String action = intent.getAction();
			//if (NetTrafficSettings.NET_TRAFFIC_UPDATE_SETTINGS.equals(action)) {

				MemoryInfo memoryInfo = new MemoryInfo();

				activityManager.getMemoryInfo(memoryInfo);

				long totalMemory = MemoryUtil.getTotalMemory();
				initMemoryLinearData(totalMemory, memoryInfo.availMem);
				fillDataToView();
				fillMemory(new double[] { totalMemory - memoryInfo.availMem,
						memoryInfo.availMem });

		//	}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.mobile_exp);
		mComeFromCreate = true;
		findView();
		bindListenerToView();

		MemoryInfo memoryInfo = new MemoryInfo();

		activityManager.getMemoryInfo(memoryInfo);
		pckMan = getPackageManager();
		packs.clear();
		mProcList.clear();
		packs.addAll(pckMan.getInstalledPackages(PackageManager.GET_SIGNATURES));
		mProcList.addAll(TaskUtil.getRunningApp(this));
		long totalMemory = MemoryUtil.getTotalMemory();
		initMemoryLinearData(totalMemory, memoryInfo.availMem);
		fillDataToView();
		fillMemory(new double[] { totalMemory - memoryInfo.availMem,
				memoryInfo.availMem });
		IntentFilter it=new IntentFilter();
		it.addAction(NetTrafficSettings.NET_TRAFFIC_UPDATE_SETTINGS);
		it.addAction(Constant.BROACTUPDATEINTERCEPTHISTORY);
		registerReceiver(broadcastReceiver,it);

		upgradeDialog = new UpgradeDialog(this);
		new UpdatingThread().start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}

	void findView() {
		linear01 = (LinearLayout) findViewById(R.id.ll_01);
		linear02 = (LinearLayout) findViewById(R.id.ll_02);
		linear03 = (LinearLayout) findViewById(R.id.ll_03);
		linear04 = (LinearLayout) findViewById(R.id.ll_04);

		left_memory_tv = (TextView) findViewById(R.id.left_memory_tv);

		install_soft_tv = (TextView) findViewById(R.id.install_soft_tv);
		left_space_tv = (TextView) findViewById(R.id.left_space_tv);

		hasuse_stream_tv = (TextView) findViewById(R.id.hasuse_stream_tv);
		left_stream_tv = (TextView) findViewById(R.id.left_stream_tv);
		mInterceptSmsTv = (TextView) findViewById(R.id.intercept_sms_tv);
		mInterceptPhoneTv = (TextView) findViewById(R.id.intercept_phone_tv);
		// danger_soft_tv = (TextView) findViewById(R.id.danger_soft_tv);
		// lasttime_scan_tv = (TextView) findViewById(R.id.last_time_scaner_tv);
		mExamPromptTv = (TextView) findViewById(R.id.exam_status);
		mLastExamSummaryLinear = findViewById(R.id.last_exam_summary);
		mLastExamPromptTv = (TextView) findViewById(R.id.last_exam_prompt);
		mLastExamScoreTv = (TextView) findViewById(R.id.exam_score_tv);
		mExamRatingBar = (RatingBar) findViewById(R.id.examRatingBar);
		mExamMeLinear = findViewById(R.id.exam_me_linear);
		mExamMeLinear.setOnClickListener(this);
		mLastUnoptimizedPromptTv = (TextView)findViewById(R.id.unoptimized_prompt_tv);

		mApp = (KindroidSecurityApplication) getApplication();
		mModel = mApp.getAdapter(NetTrafficModel.class);
		memory_container_linear = (LinearLayout) findViewById(R.id.memory_container);
		activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		left_network_pb = (ProgressBar) findViewById(R.id.left_network_pb);

	}

	public void showUpgradeDialog() {
		SharedPreferences sp = KindroidSecurityApplication.sh;
		int mShowUpgradePrompt = sp.getInt(
				UtilShareprefece.SHOW_UPGRADE_PROMPT, 1);
		int mOldVersion = sp.getInt(UtilShareprefece.LAST_UPGRADE_VERSION, -1);
		if ((newVersion > mOldVersion) || (mShowUpgradePrompt == 1)) {
			upgradeDialog.show();
			View releaseNotesLinear = upgradeDialog
					.findViewById(R.id.releasenotes_linear);
			releaseNotesLinear.setVisibility(View.VISIBLE);
			TextView releaseNotesText = (TextView) upgradeDialog
					.findViewById(R.id.releasenotes_text);
			releaseNotesText.setText(releaseNotes);
			releaseNotesText.setMovementMethod(ScrollingMovementMethod
					.getInstance());
			upgradeDialog.findViewById(R.id.down_des).setVisibility(View.GONE);
			// TextView upgrade_version =
			// (TextView)upgradeDialog.findViewById(R.id.upgrade_version);
			// upgrade_version.setText(newVersion + "");
			CheckBox upgrade_prompt_repeat = (CheckBox) upgradeDialog
					.findViewById(R.id.upgrade_prompt_repeat);
			upgrade_prompt_repeat
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub
							SharedPreferences sp = KindroidSecurityApplication.sh;
							Editor editor = sp.edit();

							if (isChecked) {
								editor.putInt(
										UtilShareprefece.SHOW_UPGRADE_PROMPT, 1);
							} else {
								editor.putInt(
										UtilShareprefece.SHOW_UPGRADE_PROMPT, 0);
								editor.putInt(
										UtilShareprefece.LAST_UPGRADE_VERSION,
										newVersion);
							}
							editor.commit();
						}

					});
			upgradeDialog.reset(downUrl);
		}
	}

	void fillDataToView() {
		mProcList.clear();
		mProcList.addAll(TaskUtil.getRunningApp(this));
		List<PackageInfo> pcks = ApkManager.getInstalledPackages(this, false);
		install_soft_tv.setText(pcks.size() + "");
		List<Interface> list = mModel.getInterfaces();
		String str = mApp.getAdapter(SharedPreferences.class).getString(
				UtilShareprefece.LIMIT_PER_MON_INT, "30");
		String monthLeft = mApp.getAdapter(SharedPreferences.class).getString(
				UtilShareprefece.LEFT_PER_MON_INT, "0");
		long limit_per_month = Long.parseLong(str);
		long userTotal = limit_per_month * 1024 * 1024;
		long left_per_month = (long) (Double.parseDouble(monthLeft) * 1024 * 1024);
/*
		if (list.size() >= 1) {
			boolean isTrue = false;
			Interface interfaceCounter = null;

			for (int i = 0; i < list.size(); i++) {
				if (getString(R.string.interfaceTypeCell).equals(
						list.get(i).getPrettyName())) {
					isTrue = true;
					interfaceCounter = list.get(i);
					break;
				}
			}

			if (isTrue && interfaceCounter.getCounters().size() > 1) {
				TrafficCounter trafficCounter = interfaceCounter.getCounters()
						.get(1);
				long[] totalGprsArray = trafficCounter.getBytes();
				long totalGprs = totalGprsArray[0] + totalGprsArray[1]
						+ left_per_month;

				initNetWorkState(totalGprs, userTotal);

			} else {
				initNetWorkState(left_per_month, userTotal);
			}
		} else {
			initNetWorkState(left_per_month, userTotal);
		}
*/
		if (list.size() >= 1) {
			long totalGprs = 0;
			for (int i = 0; i < list.size(); i++) {
				if (getString(R.string.interfaceTypeCell).equals(
						list.get(i).getPrettyName())) {
					Interface interfaceCounter = list.get(i);
					if(interfaceCounter.getCounters().size() > 1){
						TrafficCounter trafficCounter = interfaceCounter.getCounters().get(1);
						long[] totalGprsArray = trafficCounter.getBytes();
						totalGprs += totalGprsArray[0] + totalGprsArray[1];
					}			
				}
			}
			totalGprs = totalGprs + left_per_month;
			initNetWorkState(totalGprs, userTotal);

		} else {
			initNetWorkState(left_per_month, userTotal);
		}
		
		HistoryNativeCursor hnc = new HistoryNativeCursor();
		hnc.setmRequestType(3);
		mInterceptSmsTv.setText(InterceptDataBase.get(this).getHistoryNum(hnc)
				+ "");
		hnc.setmRequestType(4);
		mInterceptPhoneTv.setText(InterceptDataBase.get(this)
				.getHistoryNum(hnc) + "");
		// Date date = new Date();
		SharedPreferences sp = KindroidSecurityApplication.sh;
		// long last_scan_time = sp.getLong(UtilShareprefece.LAST_SCAN_TIME,
		// 0L);
		// if (last_scan_time != 0) {
		// lasttime_scan_tv.setText((date.getTime() - last_scan_time)
		// / (86400000L) + "");
		// } else {
		// lasttime_scan_tv.setText("0");
		// }
		// int vnum = sp.getInt(UtilShareprefece.LAST_VIRUS_SUM, 0);
		// if (vnum == 0) {
		// danger_soft_tv.setText("0");
		// danger_soft_tv.setTextColor(this.getResources().getColor(
		// R.color.light_green));
		// } else {
		// danger_soft_tv.setText(String.valueOf(vnum));
		// danger_soft_tv.setTextColor(Color.RED);
		// }

		left_space_tv.setText(sp.getInt(UtilShareprefece.LAST_APK_SUM, 0) + "");
		
		left_memory_tv.setText((100 - leftMemoryProcess) + "%");

	}

	void fillMemory(double values[]) {
		int colors[] = new int[] { Color.argb(255, 108, 189, 69),
				Color.argb(255, 210, 210, 210) };
		BudgetPieChart bpc = new BudgetPieChart();
		bpc.setShowLabels(false);
		bpc.setShowLegend(false);
		bpc.setColors(colors);
		bpc.setValues(values);

		PieChart pc = (PieChart) bpc.execute(this);

		GraphicalView gv = new GraphicalView(this, pc);

		LinearLayout.LayoutParams lp = new LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		memory_container_linear.removeAllViews();
		memory_container_linear.addView(gv, lp);

	}

	void initNetWorkState(long totalGprs, long userTotal) {

		BigDecimal totalGprsBigDecimal = new BigDecimal(totalGprs);
		BigDecimal userTotalBigDecimal = new BigDecimal(userTotal == 0 ? 1
				: userTotal);

		double networkResult = totalGprsBigDecimal.divide(userTotalBigDecimal,
				2, RoundingMode.HALF_UP).doubleValue();
		int hasusenetwork = (int) (networkResult * 100);
		hasuse_stream_tv.setText(MemoryUtil.formatMemorySize(2, totalGprs));

		if (totalGprs > userTotal)
			left_stream_tv.setText("0.0");
		else

			left_stream_tv.setText(MemoryUtil.formatMemorySize(2, userTotal
					- totalGprs));
		left_network_pb.setProgress(hasusenetwork);

	}

	void bindListenerToView() {
		linear01.setOnClickListener(linearListener);
		linear02.setOnClickListener(linearListener);
		linear03.setOnClickListener(linearListener);
		linear04.setOnClickListener(linearListener);
		
	}

	View.OnClickListener linearListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.ll_01:
				startActivity(new Intent(MobileExpActivity.this,
						TaskManageTabActivity.class));
				break;
			case R.id.ll_02:
				Intent intent=new Intent(MobileExpActivity.this,
						SoftManageTabActivity.class);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				break;
			case R.id.ll_03:
				startActivity(new Intent(MobileExpActivity.this,
						NetTrafficTabMain.class));
				break;
			case R.id.ll_04:
				startActivity(new Intent(MobileExpActivity.this,
						BlockTabMain.class));
				break;
			}

		}
	};
	Runnable updateRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			DataProce();
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		/*
		if (onekeyThread != null)
			return;
		onekeyThread = new Thread(updateRunnable);
		onekeyThread.start();
		*/
		switch(v.getId()){
		case R.id.exam_me_linear:
			startActivity(new Intent(this, MobileExamActivity.class));
			break;
		}

	}

	void DataProce() {
		handler.sendEmptyMessage(SETPBTOZERO);
		handler.sendEmptyMessage(SHOWONEKEYLINEAR);
		List<ProcInfo> procList = TaskUtil.getRunningApp(this);

		MemoryInfo memoryInfoOld = new MemoryInfo();
		activityManager.getMemoryInfo(memoryInfoOld);

		long oldMemory = memoryInfoOld.availMem;

		int count = 0;
		for (ProcInfo procInfo : procList) {
			TaskUtil.killProcess(procInfo.getPackageName(), this);
			count++;
		}
		// add jezz
		
		// add jezz
		MemoryInfo memoryInfoNew = new MemoryInfo();
		activityManager.getMemoryInfo(memoryInfoNew);
		long currentMemory = memoryInfoNew.availMem;
		long saveMemory = currentMemory - oldMemory;

		packs.clear();
		mProcList.clear();
		packs.addAll(pckMan.getInstalledPackages(PackageManager.GET_SIGNATURES));
		mProcList.addAll(TaskUtil.getRunningApp(this));
		long totalMemory = MemoryUtil.getTotalMemory();

		// initMemoryLinearData(totalMemory, currentMemory);

		float totalMemory_copy = (totalMemory * 0.5f);

		BigDecimal totalReason = new BigDecimal(totalMemory_copy == 0 ? 1
				: totalMemory_copy);
		BigDecimal total = new BigDecimal(totalMemory == 0 ? 1 : totalMemory);
		BigDecimal left = new BigDecimal(currentMemory);
		double currentResult = left.divide(total, 2, RoundingMode.HALF_UP)
				.doubleValue();

		leftMemoryProcess = (int) (currentResult * 100);

		int process = 100 - leftMemoryProcess;
		if (process < 0)
			process = 0;
		int processcount = 0;
		while (processcount < count) {
			processcount += 1;
			Message mes = Message.obtain();
			mes.what = UPDATEPB;
			mes.arg1 = processcount;
			handler.sendMessage(mes);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Message mes = Message.obtain();
		mes.what = UPDATEPB;
		mes.arg1 = process;
		handler.sendMessage(mes);

		activityManager.getMemoryInfo(memoryInfoNew);
		currentMemory = memoryInfoNew.availMem;
		saveMemory = currentMemory - oldMemory;
		left = new BigDecimal(currentMemory);

		double divideResult = left.divide(totalReason, 2, RoundingMode.HALF_UP)
				.doubleValue();
		divideResult = 0.4 + 0.6 * (divideResult);
		double score = divideResult * 100 >= 98 ? 98 : divideResult * 100;
		if ((int) score > this.score) {
			this.score = (int) score;
		}
		handler.sendEmptyMessage(FILLDATATOVIEW);

		Message mes_closeProc = Message.obtain();
		mes_closeProc.what = CLOSEPROCE;
		mes_closeProc.arg1 = count;

		mes_closeProc.obj = MemoryUtil.formatFileSize(2,
				saveMemory > 0 ? saveMemory : 0);
		handler.sendMessage(mes_closeProc);
	}

	void initMemoryLinearData(long totalMemory, long currentMemory) {

		float totalMemory_copy = (totalMemory * 0.5f);

		BigDecimal totalReason = new BigDecimal(totalMemory_copy == 0 ? 1
				: totalMemory_copy);

		BigDecimal total = new BigDecimal(totalMemory == 0 ? 1 : totalMemory);
		BigDecimal left = new BigDecimal(currentMemory);
		double currentResult = left.divide(total, 2, RoundingMode.HALF_UP)
				.doubleValue();
		double divideResult = left.divide(totalReason, 2, RoundingMode.HALF_UP)
				.doubleValue();
		divideResult = 0.4 + 0.6 * (divideResult);
		double score = divideResult * 100 >= 100 ? 98 : divideResult * 100;

		this.score = (int) score;
		leftMemoryProcess = (int) (currentResult * 100);

	}

	private Handler handler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			case SETPBTOZERO:
				
				break;
			case FILLDATATOVIEW:
				fillDataToView();

				MemoryInfo memoryInfo = new MemoryInfo();

				activityManager.getMemoryInfo(memoryInfo);

				long totalMemory = MemoryUtil.getTotalMemory();

				fillMemory(new double[] { totalMemory - memoryInfo.availMem,
						memoryInfo.availMem });
				break;
			case CLOSEPROCE:
				
				break;
			case UPDATEPB:
				
				break;
			case SHOWONEKEYLINEAR:
				
				break;
			}

		};
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (DefenderTabMain.backTimes < 1) {
				Toast.makeText(this, R.string.quit_app_tips, Toast.LENGTH_SHORT)
						.show();
				DefenderTabMain.backTimes = DefenderTabMain.backTimes + 1;
			} else {
				KindroidSecurityApplication app = (KindroidSecurityApplication) getApplication();
				app.setAppIsActive(false);
				DefenderTabMain.backTimes = 0;
				finish();
			}
			return true;
		}

		return false;
	}

	class UpdatingThread extends Thread {
		public void run() {
			if (!Utilis.checkNetwork(MobileExpActivity.this)) {
				Log.d("KindroidSecurity", " network error");
				return;
			}
			int versionCode = 0;
			PackageManager manager = getPackageManager();
			try {
				PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
				versionCode = info.versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			if (hasNewVersion(versionCode, MobileExpActivity.this)) {
				mGlobalHandle.sendEmptyMessage(UPGRADE_VERSION);
			} else {
				mGlobalHandle.sendEmptyMessage(UPGRADE_VERSION_NO);
			}
		}
	}

	public static boolean hasNewVersion(int oldVersionCode, Context ctx) {
		boolean b = false;
		try {
			String url = null;
			if (Locale.getDefault().getLanguage().startsWith("zh")) {
				url = Constant.UPGRADE_ZH_URL;
			} else {
				url = Constant.UPGRADE_EN_URL;
			}
			String str = HttpRequest.getData(url);
			JSONObject jobj = new JSONObject(str);
			if (jobj != null) {
				int result = jobj.getInt("result");
				if (result == 0) {
					downUrl = jobj.getString("upgradePath");
					newVersion = jobj.getInt("version");
					if (newVersion > oldVersionCode) {
						b = true;
						releaseNotes = jobj.getString("releaseNote");
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return b;
	}

	@Override
	protected void onPause() {
		super.onPause();
		isActive = false;

	}

	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;
		SharedPreferences sp = KindroidSecurityApplication.sh;
		long mLastExamTime = sp.getLong(MobileExamActivity.MOBILE_EXAM_LAST_TIME, 0);
		long mCurrentTime = System.currentTimeMillis();
		Calendar mLastExamCalendar = Calendar.getInstance();
		mLastExamCalendar.setTimeInMillis(mLastExamTime);
		Calendar mCurrentCalendar = Calendar.getInstance();
		mCurrentCalendar.setTimeInMillis(mCurrentTime);
		int mLastDay = mLastExamCalendar.get(Calendar.DAY_OF_YEAR);
		int mLastMonth = mLastExamCalendar.get(Calendar.MONTH);
		int mLastYear = mLastExamCalendar.get(Calendar.YEAR);
		int mCurrentDay = mCurrentCalendar.get(Calendar.DAY_OF_YEAR);
		int mCurrentMonth = mCurrentCalendar.get(Calendar.MONTH);
		int mCurrentYear = mCurrentCalendar.get(Calendar.YEAR);
		boolean mIsToday = false;
		if(mLastDay == mCurrentDay && mLastMonth == mCurrentMonth && mLastYear == mCurrentYear){
			mIsToday = true;
		}
				
		if(mLastExamTime == 0 || Math.abs(mCurrentDay - mLastDay) >= 30 || Math.abs(mCurrentMonth - mLastMonth) > 0 || Math.abs(mCurrentYear - mLastYear) > 0){
			mLastExamSummaryLinear.setVisibility(View.GONE);
			mExamPromptTv.setText(R.string.mobile_exp_exam_prompt_text);
			mLastUnoptimizedPromptTv.setText(R.string.exam_first_prompt);
			mLastUnoptimizedPromptTv.setVisibility(View.VISIBLE);
		}else{
			mLastExamSummaryLinear.setVisibility(View.VISIBLE);
			mExamPromptTv.setText(R.string.mobile_exp_last_exam_prompt);
			
			if(!mIsToday){
				mLastExamPromptTv.setText(String.format(getString(R.string.mobile_exp_last_exam_result), Math.abs(mCurrentDay - mLastDay)));
			}else{
				mLastExamPromptTv.setText(R.string.mobile_exp_today_exam_result);
			}
			int mLastScore = sp.getInt(MobileExamActivity.MOBILE_EXAM_LAST_SCORE, 100);		
			
			mLastExamScoreTv.setText(mLastScore + "");
			mExamRatingBar.setRating((float) (mLastScore * 1.0 / 100 * 5));
			if(mLastScore < 100){
				mLastUnoptimizedPromptTv.setVisibility(View.VISIBLE);
				mLastUnoptimizedPromptTv.setText(R.string.mobile_exp_unoptimized_prompt_text);
			}else{
				mLastUnoptimizedPromptTv.setVisibility(View.GONE);
			}
		}
		
		boolean enableNetWork = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		if (enableNetWork) {
			
			KindroidSecurityApplication app = (KindroidSecurityApplication) getApplication();
			app.startService();
		}
		if (mComeFromCreate) {
			mComeFromCreate = false;
			return;
		}
		
		
		sendBroadcast(new Intent(NetTrafficSettings.NET_TRAFFIC_UPDATE_SETTINGS));

	}

}
