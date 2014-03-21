/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.net.URLEncoder;

public class SystemData {

	private static String upgradePath = "";
	private static int version;
	private static String token = null;
	private static String captcha = "";
	private static String captchaKey = "";
	private static String guestToken = "MHx8MHx8MHx8MjAxMS0wNS0yMyAyMDowNDowMXx8TXNnZ0FZVC9NUG9jVW1jUUZQbFlBOE1zWUFxNm40RTFkVTMvWGhXdGxrSmdpWmpQT3FpVjVPTWN3TzFUSU9oNWJiakYreERmSU4xcQ0KYXpUZlM3bEpqQVhERkdyZTB4QU9acTBONTVGUjZtc1VBOXRXVWhKVXhOMVh2dk9wM1hRMTltWkZpOE9BNFluZkhFL0N0UUFrU3BWbw0KSHB3aWZTQzhsTVZscXNqUGQrST0=";
	private static String loginTag = "";
	
	public static void setCaptchaAndKey(String c,String key){
		captcha = c;
		captchaKey = key;
	}
	
	public static String getGuestToken(){
		return guestToken;
	}
	
	public static String getCaptcha(){
		return captcha;
	}
	public static String getCaptchaKey(){
		return captchaKey;
	}
	
	public static String getUpgradePath(){
		return upgradePath;
	}

	public static void setToken(String token,Context ctx){
		SharedPreferences.Editor share = ctx.getSharedPreferences("eMarketShare",2).edit();
		share.putString("token", token);
		share.commit();
		initToken(ctx);
	}
	
	public static void initToken(Context ctx){
		token = getToken(ctx);
	}
	
	public static String getToken(){
		return token;
	}
	
	public static String getToken(Context ctx) {
		SharedPreferences share = ctx.getSharedPreferences("eMarketShare", 1);
		String token = share.getString("token", null);
		if (token != null) {
			try {
				token = URLEncoder.encode(token, "UTF-8");
			} catch (Exception e) {
				token = null;
				e.printStackTrace();
			}
		}
		return token;
	}
	
	public static String getLoginTag(Context ctx) {
		SharedPreferences share = ctx.getSharedPreferences("eMarketShare", 1);
		String loginTag = share.getString("loginTag", "share");
		return loginTag;
	}
	
	public static String getLoginTag() {
		return loginTag;
	}
	
	public static void setLoginTag(Context ctx, String tag) {
		SharedPreferences.Editor share = ctx.getSharedPreferences("eMarketShare", 2).edit();
		
		share.putString("loginTag", tag);
		share.commit();
		initLoginTag(ctx);
	}
	
	public static String initLoginTag(Context ctx) {
		SharedPreferences share = ctx.getSharedPreferences("eMarketShare", 1);
		loginTag = share.getString("loginTag", "share");
		return loginTag;
	}
	
	public static boolean getAutoShareWeiboTag(Context ctx) {
		SharedPreferences share = ctx.getSharedPreferences("eMarketShare", 1);
		boolean loginTag = share.getBoolean("autoShareWeiboTag", false);
		return loginTag;
	}
	
	public static void setAutoShareWeiboTag(Context ctx, boolean tag) {
		SharedPreferences.Editor share = ctx.getSharedPreferences("eMarketShare", 2).edit();
		share.putBoolean("autoShareWeiboTag", tag);
		share.commit();
	}
	public static boolean getBind(Context ctx) {
		SharedPreferences share = ctx.getSharedPreferences("eMarketShare", 1);
		boolean loginTag = share.getBoolean("bind", false);
		return loginTag;
	}
	
	public static void setBind(Context ctx, boolean tag) {
		SharedPreferences.Editor share = ctx.getSharedPreferences("eMarketShare", 2).edit();
		share.putBoolean("bind", tag);
		share.commit();
	}

}
