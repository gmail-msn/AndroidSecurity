/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Contacts.People;

import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKTokenizer;
import org.wltea.analyzer.dic.Dictionary;

import com.kindroid.security.R;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.InterceptDataBase;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.NativeCursor;
import com.kindroid.security.util.PhoneType;
import com.kindroid.security.util.UpdateStaticsThread;

public class SmsReciver extends BroadcastReceiver {

	Context context;
	public static Map<String, Double> spamProbProps = null;
	private static List<String> defaultPhones = new ArrayList<String>();
	private static final String keyWords = "赠送,精装,贵宾专线,赏车,订车,枪支,要账,暗杀,免抵押,绝对震撼,多重优惠,订座,公关,窃听,办证,豪宅,机打,别墅,本公司有,特价,预定,各类发票,火爆进行,限时抢,限量,开盘,实惠,便宜,特卖会,回馈,火热进行,缺现金,中奖,抽奖,投资,公寓,楼盘,赠好礼,折上折,发钱,好礼,现房,独享,聚划算,清仓,抢购,欲购从速,抵用券,促销,BocIncom,Bocedcom,boczucom,boczbcom,bocgmtk,10086qqcom,qq9com,139gdX6ws6,3ggqqin;经理,生,小姐,本,提供,优惠,折,万,欢迎,电,售,款,询,咨,汇,钱,账,帐,拨,回复,热线,户名,查收,申,奖,地,购,房,最,邀";
	static{
		defaultPhones.add("10086");
		defaultPhones.add("10060");
		defaultPhones.add("10011");
		defaultPhones.add("10012");
		defaultPhones.add("10010");
		defaultPhones.add("10000");
		defaultPhones.add("10050");
		defaultPhones.add("10070");
		defaultPhones.add("10015");
		defaultPhones.add("13800138000");
		defaultPhones.add("10001");
		
		defaultPhones.add("11158");
		defaultPhones.add("11185");
		defaultPhones.add("95105366");
		defaultPhones.add("4008861888");
		defaultPhones.add("4008209868");
		defaultPhones.add("4008208388");
		defaultPhones.add("4008111111");
		defaultPhones.add("4006789000");
		defaultPhones.add("4008108000");
		
		defaultPhones.add("95555");
		defaultPhones.add("95588");
		defaultPhones.add("95566");
		defaultPhones.add("95533");
		defaultPhones.add("95512");
		defaultPhones.add("95599");
		defaultPhones.add("95559");
		defaultPhones.add("95528");
		defaultPhones.add("95561");
		defaultPhones.add("95595");
		defaultPhones.add("95501");
		defaultPhones.add("95568");
		defaultPhones.add("95558");
		defaultPhones.add("95508");
		defaultPhones.add("95577");		
		defaultPhones.add("962888");
		defaultPhones.add("95516");
		defaultPhones.add("96169");		
		defaultPhones.add("95580");
		defaultPhones.add("95500");
		defaultPhones.add("95502");
		defaultPhones.add("95505");
		defaultPhones.add("95510");
		defaultPhones.add("95511");
		defaultPhones.add("95515");
		defaultPhones.add("95518");
		defaultPhones.add("95519");
		defaultPhones.add("95522");
		defaultPhones.add("95567");
		defaultPhones.add("95569");
		defaultPhones.add("95585");
		defaultPhones.add("95589");
		defaultPhones.add("95590");
		defaultPhones.add("95596");
		defaultPhones.add("95105768");
		
	}
	
	private void loadProbProps(){
		/*
		try{
			InputStream is = this.context.getAssets().open("prob.dat");			
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			spamProbProps = new HashMap<String, Double>();
			String line = br.readLine();
			
			while(line != null){
				if(line.contains("=")){
					String[] tokens = line.split("=");
					try{
						spamProbProps.put(tokens[0], Double.parseDouble(tokens[1]));
					}catch(Exception e){
						
					}
				}
				line = br.readLine();
			}
			//spamProbProps = new Properties();			
			//spamProbProps.load(is);
		}catch(Exception e){
			e.printStackTrace();
		}
		*/
		BufferedReader br = null;
		BufferedWriter bw = null;
		boolean mExist = true;
		try {
			File probPath = this.context.getDir("files", Context.MODE_PRIVATE);
			File probFile = new File(probPath, "prob.dat");
			if(probFile.exists()){
				br = new BufferedReader(
						new InputStreamReader(new FileInputStream(probFile), "utf-8"));
			}else{
				mExist = false;
				InputStream is = this.context.getAssets().open("prob.dat");
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

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context = context;
		/*
		boolean isFirst = intent.getBooleanExtra("isFirst", true);
		if(!isFirst){
			return;
		}
		*/
		String content = "";
		String address = "";
		Bundle bundle = intent.getExtras();

		SmsMessage[] msgs = null;
		if (bundle == null)
			return;
		if (address.length() > 11) {
			address = address.substring(address.length() - 11);
		}
		Object[] pdus = (Object[]) bundle.get("pdus");
		msgs = new SmsMessage[pdus.length];
		for (int i = 0; i < msgs.length; i++) {
			msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			try{
				address = msgs[i].getOriginatingAddress();	
				content += msgs[i].getMessageBody().toString();							
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		boolean isTrue = checkRemoteSecurity(content, address);
		if (isTrue) {
			abortBroadcast();
			return;
		}
		checkBlockingRules(address, content, intent);

	}

	private boolean checkRemoteSecurity(String content, String address) {
		boolean isTrue = false;

		boolean kindSecurityFuc = KindroidSecurityApplication.sh.getBoolean(
				Constant.SHAREDPREFERENCES_REMOTESECURITY, false);
		if (!kindSecurityFuc)
			return isTrue;
		String passwd = KindroidSecurityApplication.sh.getString(
				Constant.SHAREDPREFERENCES_REMOTESECURITYPASSWD, "");
		if (passwd.equals(""))
			return isTrue;
		String mobile = KindroidSecurityApplication.sh.getString(
				Constant.SHAREDPREFERENCES_SAFEMOBILENUMBER, "");
		if (mobile.equals(""))
			return isTrue;

		if (!mobile.equals(address))
			return isTrue;

		String strContent[] = content.split("#");
		if (strContent.length != 3)
			return isTrue;
		if (!ConvertUtils.getMD5(strContent[2].getBytes()).equals(passwd)) {
			return isTrue;
		}
		if (strContent[1].equals(Constant.TAGDEL)) {
			new DelThread().start();

		} else if (strContent[1].equals(Constant.TAGGPS)) {
			getCurLocation(context);
		}
		return true;
	}

	private void checkBlockingRules(String address, String content, Intent intent) {

		int night_model = ConvertUtils.betweenNightModel(context);
		if (night_model == 0) {
			int model = KindroidSecurityApplication.sh.getInt(
					Constant.SHAREDPREFERENCES_BLOCKINGRULES, 1);
			blockRulesHandle(model, address, content, intent);

		} else {
			blockRulesHandle(night_model, address, content, intent);

		}

	}
	private class ResendThread extends Thread{
		private String mAddress;
		private String mContent;
		private Intent mIntent;
		public ResendThread(String address, String content, Intent intent){
			this.mAddress = address;
			this.mContent = content;
			this.mIntent = intent;
		}
		public void run(){
			if(spamProbProps == null){
				loadProbProps();
			}
			
			double normalSum;
			double spamSum;
			List<Double> p = new ArrayList<Double>();
			try{
				IKTokenizer tokenizer = new IKTokenizer(new StringReader(mContent), true);
				while(tokenizer.incrementToken())  
		        {
					try{
						CharTermAttribute ta = tokenizer.getAttribute(CharTermAttribute.class); 
			            String v = ta.toString();
			            if(!spamProbProps.containsKey(v)){
			            	continue;
			            }
			            double pv = spamProbProps.get(v);
			            p.add(pv);
					}catch(Exception e){
						e.printStackTrace();
					}
		        }
				double pup = p.get(0);
				double pdown = 1 - p.get(0);
				for(int j = 1; j < p.size(); j++){
					pup = pup * p.get(j);
					pdown = pdown * (1 - p.get(j));
				}
				if(pup + pdown > 0){
					double pmail = pup / (pup + pdown);
					if(pmail > 0.8){
						
							InterceptDataBase.get(context)
								.insertMmsPhone(3, mAddress,
									Calendar.getInstance(), mContent, 0, "");									
							sendBroact(context, 3);
						
					}else{
						mIntent.putExtra("isFirst", false);
						context.sendBroadcast(mIntent);
					}
		        }else{
		        	mIntent.putExtra("isFirst", false);
					context.sendBroadcast(mIntent);
		        }
			}catch(Throwable e){
				e.printStackTrace();
				mIntent.putExtra("isFirst", false);
				context.sendBroadcast(mIntent);
			}
		}
	}

	private void blockRulesHandle(int model, String address, String content, Intent intent) {
		if(defaultPhones.contains(address)){
			return;
		}

		PhoneType ph = ConvertUtils.getPhonetype(address);
		if (ph.getPhoneNo() == null) {
			return;
		}

		switch (model) {
		case 1:
			NativeCursor nc = new NativeCursor();
			nc.setmRequestType(1);
			nc = ConvertUtils.isBlackOrWhiteList(context, nc, ph);
			if (nc.ismIsExists() && nc.ismSmsStatus()) {
				nc = new NativeCursor();
				nc.setmRequestType(2);
				nc = ConvertUtils.isBlackOrWhiteList(context, nc, ph);
				if (!nc.ismIsExists()) {
					abortBroadcast();					
					InterceptDataBase.get(context).insertMmsPhone(3, address,
							Calendar.getInstance(), content, 0, "");
					sendBroact(context, 3);
				}
			} else {
				nc = new NativeCursor();
				nc.setmRequestType(2);
				nc = ConvertUtils.isBlackOrWhiteList(context, nc, ph);
				if (!nc.ismIsExists()) {
					boolean hasContact = ConvertUtils.hasNumInContact(
							ph.getPhoneNo(), context);
					/*
					try{
						if(!hasContact && address.startsWith("12520")){
							break;
						}
							
					}catch(Exception e){
						
					}
					*/
					
					if (!hasContact && containKeyWork(content)) {

						InterceptDataBase.get(context)
								.insertMmsPhone(3, address,
										Calendar.getInstance(), content, 0, "");
						abortBroadcast();
						sendBroact(context, 3);
					}else if(!hasContact){
						/*
						abortBroadcast();
						ResendThread rt = new ResendThread(address, content, intent);
						rt.start();
						*/
						IKTokenizer tokenizer = new IKTokenizer(new StringReader(content), true);
						double normalSum;
						double spamSum;
						List<Double> p = new ArrayList<Double>();
						try{
							/*
							Set<String> keys = spamProbProps.keySet();
							for(String key : keys){
								if(content.contains(key)){
									p.add(spamProbProps.get(key));
								}
							}
							System.out.println("p size :" + p.size());
							*/
							while(tokenizer.incrementToken())  
					        {
								CharTermAttribute ta = tokenizer.getAttribute(CharTermAttribute.class); 
					            String v = ta.toString();
					            
					            if(!spamProbProps.containsKey(v)){
					            	continue;
					            }
					            if(spamProbProps.get(v) == 0){
					            	continue;
					            }
					            if(spamProbProps.get(v) >= 1 && !keyWords.contains(v)){
					            	continue;
					            }
					            p.add(spamProbProps.get(v));
					        }
							if(p.size() > 0){
								double pup = p.get(0);
								double pdown = 1 - p.get(0);
								for(int j = 1; j < p.size(); j++){
									pup = pup * p.get(j);
									pdown = pdown * (1 - p.get(j));
								}
								if(pup + pdown > 0){
									double pmail = pup / (pup + pdown);
									
									if(pmail != Double.NaN){
										if(pmail > 0.75){											
											InterceptDataBase.get(context)
												.insertMmsPhone(3, address,
													Calendar.getInstance(), content, 0, "");	
											abortBroadcast();
											sendBroact(context, 3);
											
										}else{
											updateStatics(content, context);
										}
									}
						        }
							}
						}catch(Throwable e){
							e.printStackTrace();
							
						}
						
						
					}else{
						updateStatics(content, context);
					}
				}
			}
			break;
		case 2:
			boolean hasContact = ConvertUtils.hasNumInContact(ph.getPhoneNo(),
					context);
			if (!hasContact) {
				NativeCursor n = new NativeCursor();
				n.setmRequestType(2);
				n = ConvertUtils.isBlackOrWhiteList(context, n, ph);
				if (!n.ismIsExists()) {
					abortBroadcast();
					InterceptDataBase.get(context).insertMmsPhone(3, address,
							Calendar.getInstance(), content, 0, "");
					sendBroact(context, 3);
				}else{
					updateStatics(content, context);
				}
			}

			break;
		case 3:
			NativeCursor n = new NativeCursor();
			n.setmRequestType(2);
			n = ConvertUtils.isBlackOrWhiteList(context, n, ph);
			if (!n.ismIsExists()) {
				abortBroadcast();
				InterceptDataBase.get(context).insertMmsPhone(3, address,
						Calendar.getInstance(), content, 0, "");
				sendBroact(context, 3);

			}else{
				updateStatics(content, context);
			}
			break;
		case 4:
			break;
		case 5:
			abortBroadcast();
			InterceptDataBase.get(context).insertMmsPhone(3, address,
					Calendar.getInstance(), content, 0, "");
			sendBroact(context, 3);

			break;

		default:
			break;
		}

	}
	private void updateStatics(String content, Context context){
		UpdateStaticsThread ust = new UpdateStaticsThread(content, 0, context);
		ust.start();
	}

	public static void sendBroact(Context context, int type) {
		int num = KindroidSecurityApplication.sh.getInt(
				Constant.INTERCEPT_NOTIFY_INFO, 1);
		if (num == 1) {
			NotificationService.showType = 1;
			context.startService(new Intent(context, NotificationService.class));
		}
		Intent intent = new Intent(Constant.BROACTUPDATEINTERCEPTHISTORY);
		intent.putExtra("sms_or_phone", type);
		context.sendBroadcast(intent);

	}

	private boolean containKeyWork(String content) {
		boolean mHasKeyWork = false;

		if (content.trim().length() == 0) {
			return mHasKeyWork;
		}
		content = content.replace(" ", "");
		try {
			Cursor c = InterceptDataBase.get(context).selectKeyWordList(1);
			if (c != null && c.getCount() > 0) {
				while (c.moveToNext()) {
					String keyword = c.getString(c
							.getColumnIndex(InterceptDataBase.KEYWORDZH));
					if (content.contains(keyword)) {
						mHasKeyWork = true;
						return mHasKeyWork;
					}
				}
			}
			if (c != null) {
				c.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mHasKeyWork;
	}

	class DelThread extends Thread {
		@Override
		public void run() {

			try {

				Uri uri = Uri.parse("content://icc/adn");
				Cursor cursor = context.getContentResolver().query(uri, null,
						null, null, null);
				if (cursor != null) {
					while (cursor.moveToNext()) {
						String name = cursor.getString(cursor
								.getColumnIndex(People.NAME));
						String phoneNumber = cursor.getString(cursor
								.getColumnIndex(People.NUMBER));
						String where = "tag='" + name + "'";
						where += " AND number='" + phoneNumber + "'";
						context.getContentResolver().delete(uri, where, null);
					}
				}

				context.getContentResolver().delete(CallLog.Calls.CONTENT_URI,
						null, null);

				Cursor cc = context.getContentResolver().query(
						Contacts.CONTENT_URI, null, null, null, null);
				while (cc.moveToNext()) {

					Uri uri_del = ContentUris.withAppendedId(
							Contacts.CONTENT_URI,
							cc.getLong(cc.getColumnIndex("_id")));

					context.getContentResolver().delete(uri_del, null, null);
				}

				String phoneNumber = KindroidSecurityApplication.sh.getString(
						Constant.SHAREDPREFERENCES_SAFEMOBILENUMBER, "");

				final Uri ICC_URI = Uri.parse("content://sms/icc");

				Cursor c = context.getContentResolver().query(ICC_URI, null,
						null, null, null);
				if (c != null) {
					while (c.moveToNext()) {
						String messageIndexString = c.getString(c
								.getColumnIndexOrThrow("index_on_icc"));
						Uri simUri = ICC_URI.buildUpon()
								.appendPath(messageIndexString).build();
						context.getContentResolver().delete(simUri, null, null);
					}
				}

				Uri localUri = Uri.parse("content://sms");
				context.getContentResolver().delete(localUri, null, null);
				String message = context.getResources().getString(
						R.string.data_del_suc);
				sendSMS(phoneNumber, message, context);
			} catch (Exception e) {
				String phoneNumber = KindroidSecurityApplication.sh.getString(
						Constant.SHAREDPREFERENCES_SAFEMOBILENUMBER, "");
				String message = context.getResources().getString(
						R.string.data_del_faile);
				sendSMS(phoneNumber, message, context);
				e.printStackTrace();
			}

		}
	}

	class GpsThread extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(25000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LocationManager locamg = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);

			Location location = locamg
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			locamg.removeUpdates(listener);
			if (location == null)
				location = locamg
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			String phoneNumber = KindroidSecurityApplication.sh.getString(
					Constant.SHAREDPREFERENCES_SAFEMOBILENUMBER, "");
			if (location == null) {
				String message = context.getResources().getString(
						R.string.gps_cannot_get);
				sendSMS(phoneNumber, message, context);
			} else {
				sendSMS(phoneNumber, "gps:" + location.getLatitude() + ":"
						+ location.getLongitude(), context);
			}
		}
	}

	private void sendSMS(final String phoneNumber, final String message,
			Context context) {
		final SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);

	}

	private void getCurLocation(Context context) {
		LocationManager locamg = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		boolean gpsCanUse = locamg
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean netWorkCanuse = locamg
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (gpsCanUse) {
			locamg.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
					0, listener);
		} else if (netWorkCanuse) {
			locamg.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					1000, 0, listener);
		}
		new GpsThread().start();

	}

	final LocationListener listener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub

		}
	};

}
