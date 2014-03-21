package com.kindroid.security.util;

import com.kindroid.security.adapter.AddBlackListAdapter;
import com.kindroid.security.model.BlackListItem;
import com.kindroid.security.ui.AddListFromContactsActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

public class LoadListAdapterThread extends Thread {
	private int mType;
	private Handler mHandler;
	private AddBlackListAdapter mListAdapter;
	private Context mContext;
	public static final int CONTACTS_SOURCE_TYPE = 1;
	public static final int SIM_CONTACTS_SOURCE_TYPE = 2;
	public static final int SMS_SOURCE_TYPE = 3;
	public static final int CALL_LOG_SOURCE_TYPE = 4;

	// query params for caller id lookup
	private static final String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL("
			+ Phone.NUMBER + ",?) AND " + Data.MIMETYPE + "='"
			+ Phone.CONTENT_ITEM_TYPE + "'" + " AND " + Data.RAW_CONTACT_ID
			+ " IN " + "(SELECT raw_contact_id " + " FROM phone_lookup"
			+ " WHERE normalized_number GLOB('+*'))";

	// Utilizing private API
	private static final Uri PHONES_WITH_PRESENCE_URI = Data.CONTENT_URI;

	private static final String[] CALLER_ID_PROJECTION = new String[] {
			Phone.NUMBER, // 0
			Phone.LABEL, // 1
			Phone.DISPLAY_NAME, // 2
			Phone.CONTACT_ID, // 3
			Phone.CONTACT_PRESENCE, // 4
			Phone.CONTACT_STATUS, // 5
	};

	private static final int PHONE_NUMBER_COLUMN = 0;
	private static final int PHONE_LABEL_COLUMN = 1;
	private static final int CONTACT_NAME_COLUMN = 2;
	private static final int CONTACT_ID_COLUMN = 3;
	private static final int CONTACT_PRESENCE_COLUMN = 4;
	private static final int CONTACT_STATUS_COLUMN = 5;

	public LoadListAdapterThread(Context ctx, Handler handler, int type,
			AddBlackListAdapter mListAdapter) {
		this.mContext = ctx;
		this.mHandler = handler;
		this.mType = type;
		this.mListAdapter = mListAdapter;
	}

	public void run() {
		switch (mType) {
		case CONTACTS_SOURCE_TYPE:
			addFromContacts();
			break;
		case SIM_CONTACTS_SOURCE_TYPE:
			addFromSim();
			break;
		case SMS_SOURCE_TYPE:
			addFromSms();
			break;
		case CALL_LOG_SOURCE_TYPE:
			addFromCallLog();
			break;
		}
	}

	private void addFromCallLog() {
		/*
		 * Cursor mCursor =
		 * mContext.getContentResolver().query(CallLog.CONTENT_URI, null, null,
		 * null, null); while(mCursor.moveToNext()){ System.out.println(
		 * "=============================================================");
		 * for(int i = 0; i < mCursor.getCount(); i++){
		 * System.out.println(mCursor.getColumnName(i) + "=" +
		 * mCursor.getString(i)); } System.out.println(
		 * "============================================================="); }
		 * System.out.println(
		 * "*******************************************************************"
		 * );
		 */
		Cursor mCursor = mContext.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, null, null, null, "date DESC");
		// System.out.println("mCursor column count :" +
		// mCursor.getColumnCount());
		if(mCursor == null){
			mHandler.sendEmptyMessage(AddListFromContactsActivity.FINISH_LOAD_FROM_CALL_LOG);
			return;
		}
		while (mCursor.moveToNext()) {
			/*
			 * System.out.println(
			 * "=============================================================");
			 * for(int i = 0; i < mCursor.getColumnCount(); i++){
			 * System.out.println(mCursor.getColumnName(i) + "=" +
			 * mCursor.getString(i)); } System.out.println(
			 * "=============================================================");
			 */
			BlackListItem item = new BlackListItem();
			String displayName = mCursor.getString(mCursor
					.getColumnIndex("name"));
			String phoneNum = mCursor.getString(mCursor
					.getColumnIndex("number"));
			long callDate = mCursor.getLong(mCursor.getColumnIndex("date"));

			if (null != displayName && !"".equals(displayName.trim())) {
				item.setContactName(displayName);
			} else {
				item.setContactName(phoneNum);
			}
			item.setPhoneNumber(phoneNum);
			item.setType(CALL_LOG_SOURCE_TYPE);
			Date date = new Date(callDate);
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			item.setContentDesp(df.format(date));
			mListAdapter.addItem(item);
		}
		try{
			mCursor.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		mHandler.sendEmptyMessage(AddListFromContactsActivity.FINISH_LOAD_FROM_CALL_LOG);
	}

	private void addFromSms() {
		String strUriInbox = "content://sms/inbox";
		Uri uriSms = Uri.parse(strUriInbox);
		Cursor smsCursor = mContext.getContentResolver().query(uriSms,
				new String[] { "person", "body", "address" }, null, null,
				"date DESC");
		if(smsCursor == null){
			mHandler.sendEmptyMessage(AddListFromContactsActivity.FINISH_LOAD_FROM_SMS);
			return;
		}
		while (smsCursor.moveToNext()) {
			//int person = smsCursor.getInt(smsCursor.getColumnIndex("person"));
			String body = smsCursor.getString(smsCursor.getColumnIndex("body"));
			String address = smsCursor.getString(smsCursor
					.getColumnIndex("address"));
			BlackListItem item = new BlackListItem();
			item.setContentDesp(body);
			item.setPhoneNumber(address);
			// get contact info for number
			String selection = CALLER_ID_SELECTION.replace("+",
					PhoneNumberUtils.toCallerIDMinMatch(address));
			Cursor cursor = mContext.getContentResolver().query(
					PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION, selection,
					new String[] { address }, null);
			if (cursor == null) {
				item.setContactName(address);
			}else{
				try {
					if (cursor.moveToFirst()) {
						String cName = cursor.getString(CONTACT_NAME_COLUMN);
						if(cName == null || cName.equals("")){
							item.setContactName(address);
						}else{
							item.setContactName(cName);
						}
					}else{
						item.setContactName(address);
					}
				} finally {
					cursor.close();
				}
			}
			
			item.setType(SMS_SOURCE_TYPE);
			mListAdapter.addItem(item);
		}
		try{
			smsCursor.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		mHandler.sendEmptyMessage(AddListFromContactsActivity.FINISH_LOAD_FROM_SMS);
	}

	private void addFromSim() {
		TelephonyManager mTelephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (!mTelephonyManager.hasIccCard()) {
			mHandler.sendEmptyMessage(AddListFromContactsActivity.FINISH_LOAD_FROM_SIM);
			return;
		}
		int simState = mTelephonyManager.getSimState();
		if (simState != TelephonyManager.SIM_STATE_READY) {
			mHandler.sendEmptyMessage(AddListFromContactsActivity.LOAD_FROM_SIM_ERROR);
			return;
		}
		Uri simUri = Uri.parse("content://icc/adn");
		Cursor mCursor = mContext.getContentResolver().query(simUri, null,
				null, null, null);
		
		if (mCursor == null) {
			mHandler.sendEmptyMessage(AddListFromContactsActivity.LOAD_FROM_SIM_ERROR);
			return;
		}

		while (mCursor.moveToNext()) {
			/*
			 * System.out.println(
			 * "==========================================================");
			 * for(int i = 0; i < mCursor.getColumnCount(); i++){
			 * System.out.println(mCursor.getColumnName(i) + "=" +
			 * mCursor.getString(i)); } System.out.println(
			 * "==========================================================");
			 */
			BlackListItem item = new BlackListItem();
			String displayName = mCursor
					.getString(mCursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			item.setContactName(displayName);
			int cId = mCursor
					.getInt(mCursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
			item.setId(cId);
			item.setType(SIM_CONTACTS_SOURCE_TYPE);
			String pNum = mCursor
					.getString(mCursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			item.setPhoneNumber(pNum);
			item.setContentDesp(pNum);
			mListAdapter.addItem(item);
		}
		mCursor.close();
		mHandler.sendEmptyMessage(AddListFromContactsActivity.FINISH_LOAD_FROM_SIM);
	}

	private void addFromContacts() {
		Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;
		Cursor contactsCursor = mContext.getContentResolver().query(
				contactsUri,
				new String[] { ContactsContract.Contacts.HAS_PHONE_NUMBER,
						ContactsContract.Contacts.IN_VISIBLE_GROUP,
						ContactsContract.Contacts._ID,
						ContactsContract.Contacts.DISPLAY_NAME }, null, null,
						ContactsContract.Contacts.DISPLAY_NAME + " ASC");
		if(contactsCursor == null){
			mHandler.sendEmptyMessage(AddListFromContactsActivity.FINISH_LOAD_FROM_CONTACTS);
			return;
		}
		while (contactsCursor.moveToNext()) {
			int hasPhoneNum = contactsCursor
					.getInt(contactsCursor
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			if (hasPhoneNum != 1) {
				continue;
			}
			int inVisibleGroup = contactsCursor
					.getInt(contactsCursor
							.getColumnIndex(ContactsContract.Contacts.IN_VISIBLE_GROUP));
			if (inVisibleGroup == 0) {
				continue;
			}
			long cId = contactsCursor.getLong(contactsCursor
					.getColumnIndex(ContactsContract.Contacts._ID));
			String displayName = contactsCursor.getString(contactsCursor
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			Uri lookUri = ContactsContract.Data.CONTENT_URI;
			String querySelection = Data.CONTACT_ID + "=? AND "
					+ ContactsContract.CommonDataKinds.Phone.MIMETYPE + "='"
					+ ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
					+ "'";
			Cursor localCursor = mContext
					.getContentResolver()
					.query(lookUri,
							new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
							querySelection,
							new String[] { String.valueOf(cId) }, null);
			
			if (localCursor != null) {
				while (localCursor.moveToNext()) {
					BlackListItem item = new BlackListItem();
					item.setContactName(displayName);
					item.setId((int) cId);
					item.setType(CONTACTS_SOURCE_TYPE);
					String pNum = localCursor
							.getString(localCursor
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					item.setPhoneNumber(pNum);
					item.setContentDesp(pNum);
					mListAdapter.addItem(item);
				}
				try{
					localCursor.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}
		try{
			contactsCursor.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		if(mListAdapter != null && mListAdapter.getCount() > 0){
			mListAdapter.sortItemsByContactName(true);
		}
		mHandler.sendEmptyMessage(AddListFromContactsActivity.FINISH_LOAD_FROM_CONTACTS);
	}

}
