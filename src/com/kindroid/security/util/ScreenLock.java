package com.kindroid.security.util;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.PowerManager;

public class ScreenLock {
	
	/**
	 * 唤醒屏幕
	 */
	public static void wakeup(Context context){
		PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Gank");
		//唤醒
		wakeLock.acquire();
	}
	/**
	 * 使屏幕休眠
	 */
	public static void sleep(Context context){
		PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Gank");
		//休眠
		wakeLock.release();
	}
	/**
	 * 判断屏幕的状态
	 * @return ：  
	 */
	public static boolean isScreenOn(Context context){
		PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return mPowerManager.isScreenOn();
	}

	/**
	 * 锁屏
	 */
	public static void lockKeyguard(Context context){
		KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardLock mKeyguardLock = mKeyguardManager.newKeyguardLock("");
		mKeyguardLock.reenableKeyguard();//锁
	}
	/**
	 * 解锁 
	 */
	public static void unLockKeyguard(Context context){
		KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardLock mKeyguardLock = mKeyguardManager.newKeyguardLock("");
		mKeyguardLock.disableKeyguard();
	}
}
