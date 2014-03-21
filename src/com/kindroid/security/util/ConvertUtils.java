/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class ConvertUtils {

	public static Map<String, String> mMaps = null;
	public static Map<String, Map<String, String>> mCodeMap = new HashMap<String, Map<String, String>>();
	public static Map<String, String> mSpMap;
	public static Map<String, String> mPhoneMap = new HashMap<String, String>();
	
	static{
		mSpMap = new HashMap<String, String>();
		mSpMap.put("134", "移动");
		mSpMap.put("135", "移动");
		mSpMap.put("136", "移动");
		mSpMap.put("137", "移动");
		mSpMap.put("138", "移动");
		mSpMap.put("139", "移动");
		mSpMap.put("150", "移动");
		mSpMap.put("151", "移动");
		mSpMap.put("152", "移动");
		mSpMap.put("158", "移动");
		mSpMap.put("159", "移动");
		mSpMap.put("157", "移动");
		mSpMap.put("182", "移动");
		mSpMap.put("187", "移动");
		mSpMap.put("188", "移动");
		
		mSpMap.put("130", "联通");
		mSpMap.put("131", "联通");
		mSpMap.put("132", "联通");
		mSpMap.put("155", "联通");
		mSpMap.put("156", "联通");
		mSpMap.put("185", "联通");
		mSpMap.put("186", "联通");
		
		mSpMap.put("133", "电信");
		mSpMap.put("153", "电信");
		mSpMap.put("180", "电信");
		mSpMap.put("189", "电信");
		
		//add default phone
		mPhoneMap.put("11185", "中国邮政");
		mPhoneMap.put("10086", "中国移动客服");
		mPhoneMap.put("10010", "中国联通客服");
		mPhoneMap.put("10060", "中国联通客服");
		mPhoneMap.put("10000", "中国电信客服");
		
		mPhoneMap.put("95588", "工商银行");
		mPhoneMap.put("95555", "招商银行");
		mPhoneMap.put("95599", "农业银行");
		mPhoneMap.put("95558", "中信银行");
		mPhoneMap.put("95595", "光大银行");
		mPhoneMap.put("95566", "中国银行");
		mPhoneMap.put("95533", "建设银行");
		mPhoneMap.put("95559", "交通银行");
		mPhoneMap.put("95528", "浦发银行");
		mPhoneMap.put("95561", "兴业银行");
		mPhoneMap.put("95516", "中国银联");
		mPhoneMap.put("95511", "平安银行");
		mPhoneMap.put("95501", "深发展银行");
		mPhoneMap.put("95508", "广发银行信用卡专线");
		mPhoneMap.put("95568", "民生银行");
		mPhoneMap.put("95580", "邮政储蓄");
		mPhoneMap.put("95500", "太平洋保险");
		mPhoneMap.put("95502", "永安财险");
		mPhoneMap.put("95505", "天安保险");
		mPhoneMap.put("4006095509", "华泰保险");
		mPhoneMap.put("95510", "阳光保险");
		mPhoneMap.put("95515", "合众人寿");
		mPhoneMap.put("95518", "中国人保财险");
		mPhoneMap.put("95519", "中国人寿");
		mPhoneMap.put("95522", "泰康人寿");
		mPhoneMap.put("95567", "新华人寿");
		mPhoneMap.put("95569", "安邦保险");
		mPhoneMap.put("95585", "中华联合财险");
		mPhoneMap.put("95589", "太平人寿");
		mPhoneMap.put("95590", "大地保险");
		mPhoneMap.put("95596", "民生人寿");
		mPhoneMap.put("4008308003", "广发银行客服中心");
		mPhoneMap.put("4008203588", "友邦保险");
		mPhoneMap.put("4008205555", "招商银行信用卡");
		mPhoneMap.put("4006695599", "农业银行信用卡");
		mPhoneMap.put("4008895558", "中信银行信用卡");
		mPhoneMap.put("4007888888", "光大银行信用卡");
		mPhoneMap.put("4006695566", "中国银行信用卡");
		mPhoneMap.put("4008200588", "建设银行信用卡");
		mPhoneMap.put("4008009888", "交通银行信用卡");
		mPhoneMap.put("4008208788", "浦发银行信用卡");
		mPhoneMap.put("4006695501", "深发展银行信用卡");
		mPhoneMap.put("4006695568", "民生银行信用卡");
		mPhoneMap.put("4008895580", "邮政储蓄银行信用卡");
		
		mPhoneMap.put("4008100999", "国航服务热线");
		mPhoneMap.put("95539", "南航服务热线");
		mPhoneMap.put("4006100099", "中国联航服务热线");
		mPhoneMap.put("4006006222", "春秋航空服务热线");
		mPhoneMap.put("950710", "天津航空服务热线");
		mPhoneMap.put("4008680000", "幸福航空服务热线");
		mPhoneMap.put("4006096777", "山东航空服务热线");
		mPhoneMap.put("95530", "东航服务热线");
		mPhoneMap.put("4007006000", "吉祥航空客户中心");
		mPhoneMap.put("950718", "海南航空订票查询");
		mPhoneMap.put("95557", "厦门航空服务热线");
		mPhoneMap.put("95080", "深圳航空服务热线");
		mPhoneMap.put("4008895080", "深圳航空订票热线");
		mPhoneMap.put("4008300999", "四川航空服务热线");
		mPhoneMap.put("4008986999", "国航热线");
		mPhoneMap.put("4006100666", "国航知音会员服务热线");
		mPhoneMap.put("4006695539", "南航直销服务热线");
		mPhoneMap.put("4008206222", "春秋航空服务热线");
		mPhoneMap.put("950712", "海南航空服务热线");
		mPhoneMap.put("4008808666", "深圳航空尊鹏热线");
		
		mPhoneMap.put("95105366", "中铁快运");
		mPhoneMap.put("400678900", "宅急送");
		mPhoneMap.put("4008111111", "顺丰速运");
		mPhoneMap.put("4008108000", "DHL快运");
		mPhoneMap.put("4008208388", "UPS快运");
		mPhoneMap.put("4008861888", "联邦快递");
		mPhoneMap.put("4008123123", "必胜宅急送");
		
		
		mPhoneMap.put("110", "匪警");
		mPhoneMap.put("112", "电话报障");
		mPhoneMap.put("114", "查号台");
		mPhoneMap.put("119", "火警");
		mPhoneMap.put("120", "急救中心");
		mPhoneMap.put("122", "交通事故报警");
		mPhoneMap.put("999", "红十字会急救台");
		
	}
	
	public static String getAddrForPhone(String num){
		return mPhoneMap.get(num);
	}
	
	private static Random randGen = null;
	private static char[] numbersAndLetters = null;

	public static final String randomString(int length) {
	         if (length < 1) {
	             return null;
	         }
	         if (randGen == null) {
	                randGen = new Random();
	                numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" +
	                   "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
	                  //numbersAndLetters = ("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
	                 }
	         char [] randBuffer = new char[length];
	         for (int i=0; i<randBuffer.length; i++) {
	             randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
	          //randBuffer[i] = numbersAndLetters[randGen.nextInt(35)];
	         }
	         return new String(randBuffer);
	}


	public static byte[] drawableToByte(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static String inputSreamToString(InputStream in) throws IOException {
		StringBuffer put = new StringBuffer();
		byte[] size = new byte[2048];
		for (int length; (length = in.read(size)) != -1;) {
			put.append(new String(size, 0, length));
		}
		return put.toString();
	}

	public static byte[] InputStreamToByte(InputStream is) {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int len;
		try {
			while ((len = is.read(buffer)) > 0) {
				bytestream.write(buffer, 0, len);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		byte imgdata[] = bytestream.toByteArray();
		try {
			bytestream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imgdata;
	}

	public static Drawable bitmapToDrawable(Bitmap bitmap) {
		Drawable drawable = new BitmapDrawable(bitmap);
		return drawable;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return bitmap;
	}
	public static boolean isValidPhoneNumber(String str){
		boolean ret = true;
		
		if(str.startsWith("12520")){
			str = str.substring(5);
		}else if(str.startsWith("+86")){
			str = str.substring(3);
		}else if(str.startsWith("86")){
			str = str.substring(2);
		}
		ret = isCellphone(str);
		
		return ret;
	}

	public static boolean isCellphone(String str) {
		if (str == null)
			return false;
		
		Pattern pattern = Pattern.compile("^1(3[0-9]|5[0-9]|8[0-9])[0-9]{8}$");

		Matcher matcher = pattern.matcher(str);

		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
	}

	public static PhoneType getPhonetype(String str) {
		PhoneType ph = new PhoneType();
		ph.setPhoneNo(str);
		if (str.startsWith("+86"))
			str = str.substring(3);
		if (str.startsWith("12520")) {
			str = str.substring(5);
		}
		if (str.length() >= 12) {
			str = str.substring(str.length() - 11);
			if (isCellphone(str.substring(1))) {
				ph.setPhonetype(1);
				ph.setPhoneNo(str.substring(1));
			} else if (isNative(str)) {
				ph.setPhonetype(2);
				ph.setPhoneNo(str);
			} else if (isNative(str.substring(1))) {
				ph.setPhonetype(2);
				ph.setPhoneNo(str.substring(1));
			}
		} else if (str.length() == 11) {
			if (isCellphone(str)) {
				ph.setPhonetype(1);
				ph.setPhoneNo(str);
			} else if (isNative(str)) {
				ph.setPhonetype(2);
				ph.setPhoneNo(str);
			}
		}
		return ph;
	}

	public static boolean isNative(String str) {
		String regexp = "^(0[0-9]{2,3})?([2-9][0-9]{6,7})$";
		Pattern pattern = Pattern.compile(regexp);

		Matcher matcher = pattern.matcher(str);

		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
	}

	public static NativeCursor isBlackOrWhiteList(Context context,
			NativeCursor nc, PhoneType ph) {
		

		if (ph.getPhonetype() == 0) {
			nc.setmPhoneNum(ph.getPhoneNo());
			nc = InterceptDataBase.get(context).selectIsExists(nc);

		} else if (ph.getPhonetype() == 1) {
			nc.setmPhoneNum(ph.getPhoneNo());
			nc = InterceptDataBase.get(context).selectIsExistsFuzzy(nc);
			if (!nc.ismIsExists()) {
				String num_copy = nc.getmPhoneNum();
				String str = ConvertUtils.getCode(context, nc.getmPhoneNum());
				if (str != null) {
					nc.setmPhoneNum(str);
					nc = InterceptDataBase.get(context).selectIsExists(nc);
					nc.setmPhoneNum(num_copy);
				}

			}
		} else if (ph.getPhonetype() == 2) {
			String str = ph.getPhoneNo();

			if (str.length() > 4) {
				nc.setmPhoneNum(str);
				nc = InterceptDataBase.get(context).selectIsExists(nc);
				if(!nc.ismIsExists()){
					str = str.substring(0, 4);
					nc.setmPhoneNum(str);
					nc = InterceptDataBase.get(context).selectIsExists(nc);
					if (!nc.ismIsExists()) {
						nc.setmPhoneNum(str.substring(0, 3));
						nc = InterceptDataBase.get(context).selectIsExists(nc);
					}
				}
				
				
				
			}

		}

		return nc;

	}

	public static int betweenNightModel(Context context) {
		try {

			int night_model = KindroidSecurityApplication.sh.getInt(
					Constant.SHAREDPREFERENCES_NIGHTBLOCKINGRULES, 0);
			
			if (night_model == 0) {
				return night_model;
			}
			String dateStr = KindroidSecurityApplication.sh.getString(
					Constant.NODISTURB_DAY_TIME, "1,2,3,4,5");
			
			String arrDateStr[] = dateStr.split(",");
			if (arrDateStr == null) {
				return 0;

			}
			Calendar now = Calendar.getInstance();
			int num = now.get(Calendar.DAY_OF_WEEK);
			if (num == 1) {
				num = 7;
			} else {
				num--;
			}
			boolean hasTheDay = false;
			for (int i = 0; i < arrDateStr.length; i++) {
				if (arrDateStr[i].equals(num + "")) {
					hasTheDay = true;
					break;
				}

			}
			if (!hasTheDay) {
				return 0;
			}

			String startTime = KindroidSecurityApplication.sh.getString(
					Constant.NODISTURB_START_TIME, "23:30");
			String endTime = KindroidSecurityApplication.sh.getString(
					Constant.NODISTURB_END_TIME, "6:00");
			
			String arrStartTime[] = startTime.split(":");
			String arrEndTime[] = endTime.split(":");
			if (arrEndTime.length != 2 || arrStartTime.length != 2) {
				return 0;
			}
			
			int startHour = Integer.parseInt(arrStartTime[0]);
			int startMinu = Integer.parseInt(arrStartTime[1]);

			int endHour = Integer.parseInt(arrEndTime[0]);
			int endMinu = Integer.parseInt(arrEndTime[1]);

			Calendar startCalender = Calendar.getInstance();
			Calendar endCalendar = Calendar.getInstance();
			if (endHour >= startHour) {
				startCalender.set(Calendar.HOUR_OF_DAY, startHour);
				startCalender.set(Calendar.MINUTE, startMinu);
				endCalendar.set(Calendar.HOUR_OF_DAY, endHour);
				endCalendar.set(Calendar.MINUTE, endMinu);
				
				if (startCalender.before(now) && now.before(endCalendar)) {
					return night_model;
				}
			} else {
				startCalender.set(Calendar.HOUR_OF_DAY, startHour);
				startCalender.set(Calendar.MINUTE, startMinu);
				endCalendar.set(Calendar.HOUR_OF_DAY, 23);
				endCalendar.set(Calendar.MINUTE, 59);
				if (startCalender.before(now) && now.before(endCalendar)) {
					return night_model;
				}
				startCalender.set(Calendar.HOUR_OF_DAY, 0);
				startCalender.set(Calendar.MINUTE, 0);
				endCalendar.set(Calendar.HOUR_OF_DAY, endHour);
				endCalendar.set(Calendar.MINUTE, endMinu);
				if (startCalender.before(now) && now.before(endCalendar)) {
					return night_model;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		return 0;
	}

	public static String getMD5(byte[] source) {
		String s = "";
		char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
				'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			md.update(source);
			byte tmp[] = md.digest(); // MD5 的计算结果是一个 128 位的长整数，
										// 用字节表示就是 16 个字节
			char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，所以表示成 16
											// 进制需要 32 个字符
			int k = 0; // 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节转换成 16
											// 进制字符的转换
				byte byte0 = tmp[i]; // 取第 i 个字节
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
															// >>>为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
			}
			s = new String(str); // 换后的结果转换为字符串

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	public synchronized static Map<String, String> getMap(Context context) {
		if (mMaps == null) {
			initMap(context);
		}
		return mMaps;
	}

	public static synchronized void initMap(Context context) {
		try {
			if (mMaps != null)
				return;
			InputStream is = context.getAssets().open("000.dat");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			Map<String, String> maps = new HashMap<String, String>();
			String str = br.readLine();
			while (str != null) {
				try {
					String[] str_ar = str.split(";");

					if (str_ar.length != 2){
						str = br.readLine();
						continue;
					}
					maps.put(str_ar[0].trim(), str_ar[1].trim());
					str = br.readLine();
				} catch (Exception e) {
					str = br.readLine();
					e.printStackTrace();
				}

			}
			br.close();
			mMaps = maps;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			mMaps = null;
			e.printStackTrace();
		}

	}
	
	public static synchronized String getKeyWordString(Context context) {
		String final_str = null;
		try {
			InputStream is = context.getAssets().open("key.dat");
			// Log.i("subString", str.substring(0,3));
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len;

			while ((len = is.read(buffer)) > 0) {
				bytestream.write(buffer, 0, len);
			}
			final_str= bytestream.toString();
			
			bytestream.close();
			is.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
		return final_str;
	}
	public static String getPhoneCity(Context context, String phoneNumber){
		String phPref = phoneNumber.substring(0, 3);
		Map<String, String> codeMap = mCodeMap.get(phPref);
		String final_str = null;
		if(codeMap == null){
			String map_str = null;
			BufferedReader br = null;
			try {
				InputStream is = context.getAssets().open(
						phoneNumber.substring(0, 3) + ".dat");				
				// Log.i("subString", str.substring(0,3));
				br = new BufferedReader(new InputStreamReader(is, "utf-8"));
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while(line != null){
					sb.append(line);
					line = br.readLine();
				}
				map_str = sb.toString();
				
				br.close();
			} catch (Throwable e) {
				// TODO Auto-generated catch block

				e.printStackTrace();
			}finally{
				if(br != null){
					try{
						br.close();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			if(map_str != null){
				codeMap = new HashMap<String, String>();
				String[] arrayStr = map_str.split(";");
				for(String st : arrayStr){
					try{
						String[] sa = st.split(" ");
						codeMap.put(sa[0], sa[1]);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				mCodeMap.put(phPref, codeMap);
			}
		}
		if(codeMap != null){			
			final_str = codeMap.get(phoneNumber.substring(1, 7));
		}
		if(final_str != null){
			String cityName = getCityNameByCode(context, final_str);
			if(cityName != null){
				if(cityName.endsWith("市")){
					cityName = cityName.substring(0, cityName.lastIndexOf("市"));
				}
				String pre = mSpMap.get(phPref);
				if(pre != null){
					final_str = cityName + " " + pre;
				}else{
					final_str = null;
				}
			}else{
				final_str = null;
			}
			
		}
		return final_str;
	}
	

	public static synchronized String getCode(Context context, String str) {
		String final_str = null;
		try {
			InputStream is = context.getAssets().open(
					str.substring(0, 3) + ".dat");
			// Log.i("subString", str.substring(0,3));
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			byte[] buffer = new byte[4082];
			int len;

			while ((len = is.read(buffer)) > 0) {
				bytestream.write(buffer, 0, len);
			}
			String str_sum = bytestream.toString();
			// Log.i("subString", str.substring(1,7));
			int num = str_sum.indexOf(str.substring(1, 7));
			// Log.i("num", num+"");
			if (num == -1) {
				final_str = null;
			} else {
				str_sum = str_sum.substring(num, num + 13);
				// Log.i("num", str_sum+";str_");
				String str_array[] = str_sum.split(";")[0].split(" ");
				// Log.i("num", str_array.length+";le_");
				if (str_array.length == 2) {
					final_str = str_array[1];
				} else {
					final_str = null;
				}
			}
			bytestream.close();
			is.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
		return final_str;
	}

	public static synchronized String getCodeByCityName(Context context,
			String cityName) {

		try {
			initMap(context);
			if (mMaps == null)
				return null;
			Iterator it = mMaps.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> obj = (Entry<String, String>) it
						.next();
				if (obj.getValue().equals(cityName)) {
					return obj.getKey();
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static synchronized String getCityNameByCode(Context context,
			String code) {

		try {
			initMap(context);
			if (mMaps == null)
				return null;
			if (mMaps.containsKey(code)) {
				return mMaps.get(code);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static boolean hasNumInContact(String str, Context context) {

		boolean isTrue = false;
		try {

			Uri lookUri = ContactsContract.Data.CONTENT_URI;
			String querySelection = ContactsContract.CommonDataKinds.Phone.NUMBER
					+ " LIKE '%" + str + "%' AND "
					+ ContactsContract.CommonDataKinds.Phone.MIMETYPE
					+ "='"
					+ ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
					+ "'";
			Cursor localCursor = context
					.getContentResolver()
					.query(lookUri,
							new String[] { ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID },
							querySelection, null, null);
			if (localCursor != null && localCursor.getCount() > 0) {
				localCursor.moveToFirst();
				int id = localCursor
						.getInt(localCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID));
				lookUri = RawContacts.CONTENT_URI;
				querySelection = ContactsContract.RawContacts._ID + "=?";
				Cursor c = context.getContentResolver().query(lookUri,
						new String[] { ContactsContract.RawContacts.DELETED },
						querySelection, new String[] { id + "" }, null);
				if (c != null && c.getCount() > 0) {
					c.moveToFirst();
					int deleted = c
							.getInt(c
									.getColumnIndex(ContactsContract.RawContacts.DELETED));
					if (deleted == 0) {
						isTrue = true;
					}
				}
				if (c != null) {
					c.close();
				}
			}
			if (localCursor != null) {
				localCursor.close();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isTrue;
	}

}
