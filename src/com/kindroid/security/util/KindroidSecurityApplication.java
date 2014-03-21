/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.wltea.analyzer.dic.Dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.kindroid.security.R;
import com.kindroid.security.exception.CrashHandler;
import com.kindroid.security.model.NetTrafficModel;
import com.kindroid.security.service.AppTrafficService;
import com.kindroid.security.service.MemMonitorService;
import com.kindroid.security.service.NetTrafficService;
import com.kindroid.security.service.OutCallReceiver;
import com.kindroid.security.service.PhoneReciver;
import com.kindroid.security.service.SmsReciver;

public class KindroidSecurityApplication extends Application {

	public static final boolean LOG_ENABLED = false;

	public static final int NOTIFICATION_DEBUG = -1234;

	public static CharSequence[] BYTE_UNITS;

	public static int[] BYTE_VALUES;

	public static CharSequence[] COUNTER_TYPES;

	public static int[] COUNTER_TYPES_POS;

	public static CharSequence[] COUNTER_SINGLE_DAY;

	public static CharSequence[] COUNTER_LAST_MONTH;

	public static CharSequence[] COUNTER_LAST_DAYS;

	public static CharSequence[] COUNTER_MONTHLY;

	public static CharSequence[] COUNTER_WEEKLY;

	public static final String SERVICE_POLLING = "polling";

	public static final int SERVICE_LOW = 0;

	public static final int SERVICE_HIGH = 1;

	public static final int SERVICE_MID = 2;

	public static final String INTENT_EXTRA_INTERFACE = "interface";

	private static Resources RESOURCES;

	private NotificationManager mNotification;

	private HandlerContainer mHandlerContainer;

	private NetTrafficModel mModel;

	private SharedPreferences mPreferences;

	private static int sUpdatePolicy = SERVICE_LOW;
	public static SharedPreferences sh;
	private boolean appIsActive = false;
	
	private static final String LAST_UPDATE_PROB_TIME = "last_update_prob_time";
	private static final long UPDTA_PROB_PERIOD = 518400000L;
	
	private static final String APP_SHORT_CUT = "kindroid_short_cut";
	private final String ACTION_ADD_SHORTCUT =  "com.android.launcher.action.INSTALL_SHORTCUT";  

	private static View mPhoneAddressView;
	private static WindowManager sWindowManager;
	private static WindowManager.LayoutParams sLayoutParams;
	private static float sTouchStartX;
	private static float sTouchStartY;
	private static float sLastStartX = 0;
	private static float sLastStartY = 0;;
	
	private static String[] pCodes = { "130", "131", "132", "133", "134",
			"135", "136", "137", "138", "139", "150", "151", "152", "153",
			"154", "155", "156", "157", "158", "159", "186", "187", "188",
			"189" };

	public boolean isAppIsActive() {
		return appIsActive;
	}

	public void setAppIsActive(boolean appIsActive) {
		this.appIsActive = appIsActive;
	}

	// use of firewall after reinstall
	public static boolean mFirstOfReinstall = false;

	public static Resources resources() {
		return RESOURCES;
	}

	public synchronized <T> T getAdapter(Class<T> clazz) {
		if (NetTrafficModel.class == clazz) {
			if (mModel == null) {

				mModel = new NetTrafficModel(this);
				// mModel.load();;
				Handler handler = getAdapter(HandlerContainer.class)
						.getSlowHandler();
				handler.post(new Runnable() {
					public void run() {
						mModel.load();

					}
				});
			}

			return clazz.cast(mModel);
		} else if (HandlerContainer.class == clazz) {
			if (mHandlerContainer == null) {
				HandlerThread looper = new HandlerThread("NetCounter Handler");
				looper.start();
				Handler handler = new Handler(looper.getLooper());
				mHandlerContainer = new HandlerContainer(new Handler(), handler);
			}
			return clazz.cast(mHandlerContainer);
		} else if (SharedPreferences.class == clazz) {
			if (mPreferences == null) {
				mPreferences = PreferenceManager
						.getDefaultSharedPreferences(this);
			}
			return clazz.cast(mPreferences);
		} else if (NotificationManager.class == clazz) {
			if (mNotification == null) {
				mNotification = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			}
			return clazz.cast(mNotification);

		}
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		BYTE_UNITS = getResources().getTextArray(R.array.byteUnits);
		BYTE_VALUES = getResources().getIntArray(R.array.byteValues);
		sh = PreferenceManager.getDefaultSharedPreferences(this);

		IntentFilter it = new IntentFilter();

		it.setPriority(2147483647);
		it.addAction("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(new SmsReciver(), it);

		IntentFilter screen_it = new IntentFilter();

		screen_it.addAction(Intent.ACTION_SCREEN_ON);
		screen_it.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(new ScreenBroacastReciver(), screen_it);
		addReceiver();

		// Reorder the counter types.
		CharSequence[] temp = getResources().getTextArray(R.array.counterTypes);
		COUNTER_TYPES = new CharSequence[temp.length];
		COUNTER_TYPES_POS = getResources().getIntArray(R.array.counterTypesPos);
		for (int i = 0; i < temp.length; i++) {
			COUNTER_TYPES[COUNTER_TYPES_POS[i]] = temp[i];
		}

		COUNTER_SINGLE_DAY = getResources().getTextArray(
				R.array.counterSingleDay);
		COUNTER_LAST_MONTH = getResources().getTextArray(
				R.array.counterLastMonth);
		COUNTER_LAST_DAYS = getResources()
				.getTextArray(R.array.counterLastDays);
		COUNTER_MONTHLY = getResources().getTextArray(R.array.counterMonthly);
		COUNTER_WEEKLY = getResources().getTextArray(R.array.counterWeekly);

		RESOURCES = getResources();
		boolean enableNetWork = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		
		if (enableNetWork){
			KindroidSecurityApplication.setUpdatePolicy(KindroidSecurityApplication.SERVICE_MID);
			
			startService();
		}
			
		boolean appNetWork = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLEAPPTRAFFICMOITER, true);
		NetworkInfo localNetworkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();
		if (appNetWork && localNetworkInfo != null
				&& localNetworkInfo.getType() == 0) {
			Intent intent_copy = new Intent(KindroidSecurityApplication.this,
					AppTrafficService.class);
			startService(intent_copy);

		}

		if (KindroidSecurityApplication.LOG_ENABLED) {

		}
		File fireWallPrefile = new File(
				"/data/data/com.kindroid.security/shared_prefs/DroidWallPrefs.xml");
		if (!fireWallPrefile.exists()) {
			mFirstOfReinstall = true;
		}
		reportApkInfo();
		registerCrashHandler();
		loadProbProps();
		// added shortcut
//		addShortCut();
		addTelStateListener();
		updateProb();
//		startMemUsageService();

	}
	private void startMemUsageService(){
		Intent intent = new Intent(this, MemMonitorService.class);
		startService(intent);
	}
	private void addShortCut(){
		boolean shortCutExist = sh.getBoolean(APP_SHORT_CUT, false);
		if(!shortCutExist){
			Intent addShortcut = new Intent(ACTION_ADD_SHORTCUT);  
			addShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));  
		    Intent intent = new Intent();
		    intent.setComponent(new ComponentName(this.getPackageName(),  ".ui.AppEngActivity"));
		    intent.setAction("android.intent.action.MAIN");
		    addShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT,intent);
		    addShortcut.putExtra("duplicate", false);
		    addShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.icon));  
		    sendBroadcast(addShortcut);  
		    Editor editor = sh.edit();
		    editor.putBoolean(APP_SHORT_CUT, true);
		    editor.commit();
		}
		
	}
	private void updateProb(){
		long currentTime = System.currentTimeMillis();
		long lastUpdateTime = sh.getLong(LAST_UPDATE_PROB_TIME, currentTime);
		if((lastUpdateTime == currentTime) || (currentTime - lastUpdateTime) >= UPDTA_PROB_PERIOD){
			if(lastUpdateTime == currentTime){
				lastUpdateTime = 0;
			}
			updateStatics(lastUpdateTime);
			Editor editor = sh.edit();
			editor.putLong(LAST_UPDATE_PROB_TIME, currentTime);
			editor.commit();
		}
	}
	private void updateStatics(long lastUpdateTime){
		StringBuilder sb = new StringBuilder();
		try{
			Uri localUri = Uri.parse("content://sms/inbox");
			Cursor localCursor = getContentResolver().query(localUri, new String[] { "body", "date", "read"},  "date>='" + lastUpdateTime + "' AND read='1'",
					null,  "date desc");
			if (localCursor == null || localCursor.getCount() <= 0) {
				return;
			}
			while(localCursor.moveToNext()){
				String body = localCursor.getString(localCursor.getColumnIndex("body"));
				if(!TextUtils.isEmpty(body)){
					sb.append(body).append("\n");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(sb.length() > 0){
			UpdateStaticsThread ust = new UpdateStaticsThread(sb.toString(), 0, this, true);
			ust.start();
		}
	}

	private class LoadPhoneAddressThread extends Thread {
		public void run() {
			ConvertUtils.mCodeMap.clear();
			for (String mCode : pCodes) {
				if (ConvertUtils.mCodeMap.get(mCode) != null) {
					continue;
				}
				BufferedReader br = null;
				String map_str = null;
				try {
					InputStream is = getAssets().open(mCode + ".dat");
					br = new BufferedReader(new InputStreamReader(is, "utf-8"));
					StringBuilder sb = new StringBuilder();
					String line = br.readLine();
					while (line != null) {
						sb.append(line);
						line = br.readLine();
					}
					map_str = sb.toString();

					is.close();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (Exception e) {

						}
					}
				}
				if (map_str != null) {
					Map<String, String> codeMap = new HashMap<String, String>();
					String[] arrayStr = map_str.split(";");
					for (String st : arrayStr) {
						try {
							String[] sa = st.split(" ");
							if (sa.length == 2) {
								codeMap.put(sa[0], sa[1]);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					ConvertUtils.mCodeMap.put(mCode, codeMap);

				}

			}
		}
	}

	private void addTelStateListener() {
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		TelStateListener tsl = new TelStateListener();
		tm.listen(tsl, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private class TelStateListener extends PhoneStateListener {		

		@Override
		public void onMessageWaitingIndicatorChanged(boolean mwi) {
			// TODO Auto-generated method stub
			super.onMessageWaitingIndicatorChanged(mwi);
		}

		@Override
		public void onCallForwardingIndicatorChanged(boolean cfi) {
			// TODO Auto-generated method stub
			super.onCallForwardingIndicatorChanged(cfi);
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			if (state == TelephonyManager.CALL_STATE_RINGING) {
				// interceptHandle(incomingNumber);
				if (incomingNumber != null) {
					displayPhoneAddress(incomingNumber);
				}
			} else if (state == TelephonyManager.CALL_STATE_IDLE) {
				removePopView();
			} else {
				super.onCallStateChanged(state, incomingNumber);
			}
		}

	}

	private void removePopView() {
		if (mPhoneAddressView == null) {
			return;
		}

		 WindowManager wm=(WindowManager)getSystemService(Context.WINDOW_SERVICE); 
		 try{
			 wm.removeView(mPhoneAddressView); 
		 }catch(Exception e){
			 e.printStackTrace(); 
		 }
		 
	}

	private void displayPhoneAddress(String number) {

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPhoneAddressView = inflater.inflate(R.layout.popview, null);
		// mPhoneAddressView.getBackground().setAlpha(10);		
		sWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);		
		sLayoutParams = new WindowManager.LayoutParams();
		sLayoutParams.width = sLayoutParams.WRAP_CONTENT;
		sLayoutParams.height = sLayoutParams.WRAP_CONTENT;
		sLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		sLayoutParams.format = 1;
		sLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		
		sLayoutParams.x = (int)sLastStartX;
		sLayoutParams.y = (int)sLastStartY;
		sLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		

		TextView tv = (TextView) mPhoneAddressView
				.findViewById(R.id.phone_city);
		PhoneType pt = ConvertUtils.getPhonetype(number);
		
		boolean hasName = false;
		if (pt.getPhonetype() == 2) {
			String pNumber = pt.getPhoneNo();
			String cityName = null;
			try {
				cityName = ConvertUtils.getCityNameByCode(this,
						pNumber.substring(0, 3));
			} catch (Exception e) {
				cityName = null;
				e.printStackTrace();
			}
			if (cityName != null) {
				tv.setText(cityName);
				hasName = true;
			} else {
				try {
					cityName = ConvertUtils.getCityNameByCode(this,
							pNumber.substring(0, 4));
				} catch (Exception e) {
					cityName = null;
					e.printStackTrace();
				}
				if (cityName != null) {
					tv.setText(cityName);
					hasName = true;
				} 
			}

		} else if (pt.getPhonetype() == 1) {
			// String regionCode = ConvertUtils.getCode(this, pt.getPhoneNo());
			String cityName = null;
			try {
				// cityName = ConvertUtils.getCityNameByCode(this, regionCode);
				cityName = ConvertUtils.getPhoneCity(this, pt.getPhoneNo());
			} catch (Exception e) {
				cityName = null;
				e.printStackTrace();
			}
			if (cityName != null) {
				tv.setText(cityName);
				hasName = true;
			}
		}else if (pt.getPhonetype() == 0) {
			String cityName = ConvertUtils.getAddrForPhone(pt.getPhoneNo());
			if(cityName != null){
				tv.setText(cityName);
				hasName = true;
			}
		}
		if(hasName){
			sWindowManager.addView(mPhoneAddressView, sLayoutParams);
			mPhoneAddressView.setOnTouchListener(new PopViewOnTouchListener());
		}
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
				
				break;
			case MotionEvent.ACTION_MOVE:	
				updateViewPosition(x, y, sWindowManager);
				break;
			case MotionEvent.ACTION_UP:
				updateViewPosition(x, y, sWindowManager);
				sLastStartX = x - sTouchStartX;
				sLastStartY = y - sTouchStartY;
				sTouchStartX = sTouchStartY = 0;
				
				break;
			}

			return false;
		}
		
	}
	private static void updateViewPosition(float x, float y, WindowManager wm) {
		// 更新浮动窗口位置参数
		sLayoutParams.x = (int) (x - sTouchStartX);
		sLayoutParams.y = (int) (y - sTouchStartY);
		wm.updateViewLayout(mPhoneAddressView, sLayoutParams);
		
	}

	private void interceptHandle(String incomingNumber) {
		int night_model = ConvertUtils.betweenNightModel(this);

		if (night_model == 0) {
			int model = KindroidSecurityApplication.sh.getInt(
					Constant.SHAREDPREFERENCES_BLOCKINGRULES, 1);
			blockRulesHandle(model, incomingNumber);
		} else {
			blockRulesHandle(night_model, incomingNumber);
		}
	}

	private void blockRulesHandle(int model, String address) {
		
		PhoneType ph = ConvertUtils.getPhonetype(address);
		if (ph.getPhoneNo() == null) {
			return;
		}
		int i = 0;
		boolean isTrue = false;
		switch (model) {
		case 1:
			NativeCursor nc = new NativeCursor();
			nc.setmRequestType(1);
			nc = ConvertUtils.isBlackOrWhiteList(this, nc, ph);
			if (nc.ismIsExists() && nc.ismRingStatus()) {
				nc = new NativeCursor();
				nc = ConvertUtils.isBlackOrWhiteList(this, nc, ph);
				if (!nc.ismIsExists()) {
					isTrue = true;
					InterceptHandleStyle(isTrue);
					InterceptDataBase.get(this).insertMmsPhone(4,
							nc.getmPhoneNum(), Calendar.getInstance(), "", 0,
							"");
				}
			}
			i = 1;

			break;
		case 2:
			boolean hasContact = ConvertUtils.hasNumInContact(ph.getPhoneNo(),
					this);
			if (!hasContact) {
				NativeCursor n = new NativeCursor();
				n.setmRequestType(2);
				n = ConvertUtils.isBlackOrWhiteList(this, n, ph);
				if (!n.ismIsExists()) {
					isTrue = true;
					InterceptHandleStyle(isTrue);
					InterceptDataBase.get(this)
							.insertMmsPhone(4, n.getmPhoneNum(),
									Calendar.getInstance(), "", 0, "");
				}
			}

			i = 2;

			break;
		case 3:
			NativeCursor n = new NativeCursor();
			n.setmRequestType(2);
			n = ConvertUtils.isBlackOrWhiteList(this, n, ph);
			if (!n.ismIsExists()) {
				isTrue = true;
				InterceptHandleStyle(isTrue);
				InterceptDataBase.get(this).insertMmsPhone(4, n.getmPhoneNum(),
						Calendar.getInstance(), "", 0, "");
			}
			i = 3;
			break;
		case 4:
			i = 4;
			break;
		case 5:
			i = 5;

		case 6:
			i = 6;
			isTrue = true;
			InterceptHandleStyle(isTrue);
			InterceptDataBase.get(this).insertMmsPhone(4, ph.getPhoneNo(),
					Calendar.getInstance(), "", 0, "");

			break;

		default:
			break;
		}

		if (isTrue) {
			SmsReciver.sendBroact(this, 4);
		}

	}

	private void InterceptHandleStyle(boolean isTrue) {
		if (!isTrue)
			return;
		int type = KindroidSecurityApplication.sh.getInt(
				Constant.INTERCEPT_TREAT_MODE, 1);
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		switch (type) {
		case 2:
			try {				
				PhoneUtils.getITelephony(tm).silenceRinger();// 静铃
				// AudioManager am =
				// (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				// am.setMode(AudioManager.MODE_NORMAL);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				try {
					Method getITelephonyMethod = TelephonyManager.class
							.getDeclaredMethod("getITelephony", (Class[]) null);
					getITelephonyMethod.setAccessible(true);
					TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					Object mITelephony = getITelephonyMethod.invoke(
							mTelephonyManager, (Object[]) null);
					// 挂断 - mITelephony.endCall();
					mITelephony.getClass()
							.getMethod("silenceRinger", new Class[] {})
							.invoke(mITelephony, new Object[] {});
					// 取消通知 - mITelephony.cancelMissedCallsNotification()
					mITelephony
							.getClass()
							.getMethod("cancelMissedCallsNotification",
									new Class[] {})
							.invoke(mITelephony, new Object[] {});
				} catch (Exception ne) {
					ne.printStackTrace();
				}
				e.printStackTrace();
			}

			break;
		default:

			try {
				PhoneUtils.getITelephony(tm).endCall();// 静铃
			} catch (Exception e) {
				// TODO Auto-generated catch block
				try {
					Method getITelephonyMethod = TelephonyManager.class
							.getDeclaredMethod("getITelephony", (Class[]) null);
					getITelephonyMethod.setAccessible(true);
					TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					Object mITelephony = getITelephonyMethod.invoke(
							mTelephonyManager, (Object[]) null);
					// 挂断 - mITelephony.endCall();
					mITelephony.getClass().getMethod("endCall", new Class[] {})
							.invoke(mITelephony, new Object[] {});
					// 取消通知 - mITelephony.cancelMissedCallsNotification()
					mITelephony
							.getClass()
							.getMethod("cancelMissedCallsNotification",
									new Class[] {})
							.invoke(mITelephony, new Object[] {});
				} catch (Exception ne) {
					e.printStackTrace();
				}
				e.printStackTrace();
			}
			break;
		}

	}

	private void loadProbProps() {
		if (SmsReciver.spamProbProps == null) {
			new Thread() {
				public void run() {
					BufferedReader br = null;
					BufferedWriter bw = null;
					boolean mExist = true;
					try {
						File probPath = getDir("files", Context.MODE_PRIVATE);
						File probFile = new File(probPath, "prob.dat");
						if(probFile.exists()){
							br = new BufferedReader(
									new InputStreamReader(new FileInputStream(probFile), "utf-8"));
						}else{
							mExist = false;
							InputStream is = getAssets().open("prob.dat");
							br = new BufferedReader(
									new InputStreamReader(is, "utf-8"));
							bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(probFile), "utf-8"));
						}						
						
						SmsReciver.spamProbProps = new HashMap<String, Double>();
						String line = br.readLine();
						while (line != null) {
							if (line.contains("=")) {
								String[] tokens = line.split("=");
								try {
									SmsReciver.spamProbProps.put(tokens[0],
											Double.parseDouble(tokens[1]));
								} catch (Exception e) {

								}
							}
							if(!mExist){
								bw.write(line);
								bw.write("\n");
							}
							line = br.readLine();
						}
						br.close();
						if(bw != null){
							bw.flush();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}finally{
						if(br != null){
							try{
								br.close();
							}catch(Exception e){
								
							}
						}
						if(bw != null){
							try{
								bw.close();
							}catch(Exception e){
								
							}
						}
					}
				}
			}.start();
		}
	}

	private void registerCrashHandler() {
		CrashHandler crashHandler = CrashHandler.getInstance();
		// 注册crashHandler
		crashHandler.init(getApplicationContext());
		// 发送以前没发送的报告(可选)
		crashHandler.sendPreviousReportsToServer();
	}

	private void reportApkInfo() {
		new ApkReportThread(this).start();
	}

	class ScreenBroacastReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent == null || intent.getAction() == null)
				return;
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				boolean enableNetWork = KindroidSecurityApplication.sh
						.getBoolean(
								Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER,
								true);
				boolean isShowPopView = PopViewAid.linear != null;

				if (enableNetWork && isShowPopView) {
					KindroidSecurityApplication
							.setUpdatePolicy(KindroidSecurityApplication.SERVICE_HIGH);
					startService();
				} else if (enableNetWork) {
					KindroidSecurityApplication
							.setUpdatePolicy(KindroidSecurityApplication.SERVICE_MID);
					startService();
				}

			} else {
				boolean enableNetWork = KindroidSecurityApplication.sh
						.getBoolean(
								Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER,
								true);
				if (enableNetWork) {

					KindroidSecurityApplication
							.setUpdatePolicy(KindroidSecurityApplication.SERVICE_LOW);
					startService();
				}
			}

		}
	}

	@Override
	public void onTerminate() {
		synchronized (this) {
			if (mHandlerContainer != null) {
				mHandlerContainer.getSlowHandler().getLooper().quit();
			}
		}

		BYTE_UNITS = null;
		BYTE_VALUES = null;
		COUNTER_TYPES = null;
		COUNTER_TYPES_POS = null;
		COUNTER_SINGLE_DAY = null;
		COUNTER_LAST_MONTH = null;
		COUNTER_LAST_DAYS = null;
		COUNTER_MONTHLY = null;
		COUNTER_WEEKLY = null;
		RESOURCES = null;

		if (KindroidSecurityApplication.LOG_ENABLED) {

		}
		new ApkReportThread(this).start();
	}

	public void startService() {
		// WakefulService.acquireStaticLock(this);
		boolean enableNetWork = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLETRAFFICMOITER, true);
		boolean enableAppNetWork = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_ENABLEAPPTRAFFICMOITER, true);

		NetworkInfo localNetworkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();
		
		if (localNetworkInfo != null) {
			if (localNetworkInfo.getType() == 0 && enableNetWork
					&& enableAppNetWork) {
				KindroidSecurityApplication.setUpdatePolicy(KindroidSecurityApplication.SERVICE_HIGH);
			}
		}
		
		Intent intent = new Intent(this, NetTrafficService.class);		
		startService(intent);
	}

	public static synchronized void setUpdatePolicy(int updatePolicy) {
		sUpdatePolicy = updatePolicy;
	}

	public static synchronized int getUpdatePolicy() {
		return sUpdatePolicy;
	}

	public void toast(int message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	public void toast(CharSequence message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	void addReceiver() {
		IntentFilter phone_it = new IntentFilter();
		phone_it.setPriority(2147483647);
		phone_it.addAction("android.intent.action.SIM_STATE_CHANGED");
		phone_it.addAction("android.intent.action.ANY_DATA_STATE");
		phone_it.addAction("android.intent.action.RADIO_TECHNOLOGY");
		phone_it.addAction(TelephonyManager.EXTRA_STATE_RINGING);
		phone_it.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		registerReceiver(new PhoneReciver(), phone_it);
		// add call reciver
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		localIntentFilter
				.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		localIntentFilter.setPriority(2147483647);
		registerReceiver(new OutCallReceiver(), localIntentFilter);

	}

}
