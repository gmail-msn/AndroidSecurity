/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Contacts;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilis {
	
	public static ProgressDialog mProgressDialog;
	
	public static int isInstalled(String packageName, int version, List<PackageInfo> packInfos){
		int ret = -1;
		for(PackageInfo pInfo : packInfos){
			if(packageName.compareToIgnoreCase(pInfo.packageName) == 0 && version <=pInfo.versionCode){
				ret = 0;
				break;
			}else if(packageName.compareToIgnoreCase(pInfo.packageName) == 0 && version > pInfo.versionCode){
				ret = 1;
				break;
			}
		}
		return ret;
	}
	public static boolean checkPhoneNum(String phoneNum){		
		String regular = "1[3,4,5,8]{1}\\d{9}";  
        Pattern pattern = Pattern.compile(regular);  
        Matcher matcher = pattern.matcher(phoneNum);  
        return matcher.matches();
	}
	
	public static boolean checkNetwork(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo != null) {
			if (networkinfo.isConnected()) {
				return true;
			}
		}
		return false;
	}
	public static boolean hasLogined(){
		boolean ret = true;
		SharedPreferences sh = KindroidSecurityApplication.sh;
		String mToken = sh.getString(Constant.SHAREDPREFERENCES_TOKEN, "");
		if(mToken.equals("")){
			ret = false;
		}
		return ret;
	}
	public static String getToken(){
		SharedPreferences sh = KindroidSecurityApplication.sh;
		String mToken = sh.getString(Constant.SHAREDPREFERENCES_TOKEN, "");
		if(mToken.equals("")){
			return null;
		}else{
			return mToken;
		}

	}
	public static String getUserName(){
		if(hasLogined()){
			SharedPreferences sh = KindroidSecurityApplication.sh;
			return sh.getString(Constant.SHAREDPREFERENCES_USERNAME, "");
		}else{
			return null;
		}
	}
	
	public static List<ApplicationInfo> getUserInstalledApp(Context context) {
		List<ApplicationInfo> installedPackageList = new ArrayList<ApplicationInfo>();
		List<ApplicationInfo> list = context.getPackageManager().getInstalledApplications(0);
		for (int i = 0; i < list.size(); i++) {
			int j = list.get(i).flags & 0x1;
			if (j == 0) {
				installedPackageList.add(list.get(i));
			}
		}
		return installedPackageList;
	}
	
	@SuppressWarnings("unchecked")
	public static void randomList(List list) {  
        Collections.sort(list, new Comparator(){  
            HashMap map = new HashMap();  
            public int compare(Object v1, Object v2) {  
                init(v1);  
                init(v2);  
                  
                double n1 = ((Double)map.get(v1)).doubleValue();  
                double n2 = ((Double)map.get(v2)).doubleValue();  
                if(n1 > n2)  
                    return 1;  
                else if(n1 < n2)  
                    return -1;  
                return 0;  
            }  
            private void init(Object v){  
                if(map.get(v) == null){  
                    map.put(v, new Double(Math.random()));  
                }  
            }  
            protected void finalize() throws Throwable {  
                map = null;  
            }  
        });  
    }
	
	public static AnimationSet listViewAnim() {
		AnimationSet animationSet = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(50);
		animationSet.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(10);
		animationSet.addAnimation(animation);
		
		return animationSet;
	}
	
	public static void showProgressDialog(Context ctx, int title, int message) {
		mProgressDialog = new ProgressDialog(ctx);
		mProgressDialog.setTitle(ctx.getResources().getText(title));
		mProgressDialog.setMessage(ctx.getResources().getText(message));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}
	
	public static void hideProgressDialog() {
		if (mProgressDialog.isShowing()) {
			mProgressDialog.hide();
		}
	}
	
	public static float getRatingBarNum(int count) {
		float result = 0;
		BigDecimal bDecimal = new BigDecimal(count);
		result = Float.parseFloat(String.valueOf(bDecimal.divide(new BigDecimal(2), 2)));
		return result;
	}
	
	public static boolean isMobileNumber(String mobile) {
		if (TextUtils.isEmpty(mobile)) {
			return false;
		}
		Pattern p = Pattern.compile("^((13[0-9])|(15[0-9])|(18[0-9]))\\d{8}$");
		Matcher m = p.matcher(mobile);
		return m.matches();
	}

	public static String getMD5(byte[] source) {
		String s = null;
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(source);
			byte tmp[] = md.digest();

			char str[] = new char[16 * 2];

			int k = 0;
			for (int i = 0; i < 16; i++) {
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			s = new String(str);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static String getDisplayNameByTelephone(Context context,String numberStr){
		String nameReturn="";
		Cursor phone = null;
		Cursor cursor = null;
		try {
			String number = "number";
			String name="display_name";
			String contactUri = "content://contacts/people";
			if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 4) {
				number = "data1";
				contactUri = "content://com.android.contacts/contacts";
			}
			cursor = context.getContentResolver().query(Uri.parse(contactUri),null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				nameReturn = cursor.getString(cursor.getColumnIndex(name));
				String cid = cursor.getString(cursor.getColumnIndex("_id"));
				if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 4) {
					phone = context.getContentResolver().query(Uri.parse("content://com.android.contacts/data/phones"),new String[] { number },"contact_id=" + cid, null, null);
				} else {
					Uri personUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, Long.parseLong(cid));
					Uri phonesUri = Uri.withAppendedPath(personUri,Contacts.People.Phones.CONTENT_DIRECTORY);
					phone = context.getContentResolver().query(phonesUri,new String[] { number }, null, null, null);
				}
				String phoneNumber="";
				while (phone.moveToNext()) {
					phoneNumber = phone.getString(0);
					if (phoneNumber.length() >= 11) {
						phoneNumber = phoneNumber.substring(phoneNumber.length() - 11, phoneNumber.length());
					}
				}
				phone.close();
				if (!TextUtils.isEmpty(numberStr) && phoneNumber.equals(numberStr)) {
					break;
				}
				cursor.moveToNext();
			}
		}catch (Exception ex) {
			ex.printStackTrace();
		}finally{
			if (cursor != null) {
				cursor.close();
			}
		}
		return nameReturn;
	}
	
	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}
}
