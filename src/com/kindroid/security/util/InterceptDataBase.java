/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.kindroid.security.R;

public class InterceptDataBase {

	private final String DATABASE_NAME = "intercept.db";
	private final int DATABASE_VERSION = 2;
	// table name for intercepting sms
	private final String TABLE_NAME_INTERCEPT_MMS = "intercept_mms";
	// table name for intercepting phone
	private final String TABLE_NAME_INTERCEPT_PHONE = "intercept_phone";

	// table name of whitelist
	private final String TABLE_NAME_INTERCEPT_WHITELIST = "intercept_whitelist";

	// table name blacklist
	private final String TABLE_NAME_INTERCEPT_BLACKLIST = "intercept_blacklist";

	private final String TABLE_NAME_INTERCEPT_KEYWORD = "intercept_keyword";

	// private final String TABLE_NAME_INTERCEPT_RULES = "intercept_rules";

	public static final String ID = "_id";
	public static final String CONTACTNAME = "contact_name";
	public static final String PHONENUM = "phone_num";

	public static final String SMSSTATUS = "sms_status";
	public static final String RINGSTATUS = "ring_status";

	public static final String PHONETYPE = "phone_type";

	public static final String KEYWORDZH = "keyword_zh";

	public static final String ADDRESS = "address";
	public static final String DATE = "date";
	public static final String BODY = "body";
	public static final String READ = "read";
	public static final String MARK = "mark";

	public static final String KEYWORDTYPE = "keyword_type";

	// public static final String RULES = "rules";

	public static final DateFormat DF_DATE = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	private static SQLiteDatabase mDb;

	private static InterceptDataBase mDataBase;

	private InterceptDataBase(Context context) {
		mDb = new DatabaseHelper(context).getWritableDatabase();
	}

	/** instance of InterceptDataBase,the object is single */
	public synchronized static InterceptDataBase get(Context context) {
		if (mDataBase == null) {
			mDataBase = new InterceptDataBase(context);

		}
		return mDataBase;
	}

	/** close database */
	public synchronized static void close() {
		try {
			mDb.close();
			mDataBase = null;
		} catch (Exception e) {
			e.printStackTrace();
			mDataBase = null;

		}

	}

	// 插入黑白名单数据，type类型1代表黑名单，2白名单，contactname备注名称，phonenum插入中手机号
	public synchronized void insertBlackWhitList(int type, String contactName,
			String phoneNum, boolean smsStatus, boolean ringStatus,
			int phone_type) {

		try {
			String str = getInsertUrl(type);
			if (contactName == null)
				contactName = "";
			mDb.execSQL(str, new Object[] { contactName, phoneNum, smsStatus,
					ringStatus, phone_type });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 插入拦截的短信或者是电话，type3代表mms，4phone，address信息或来电的地址，calender来电/短信日期，body短信内容，是否已经读取
	public synchronized void insertMmsPhone(int type, String address,
			Calendar calendar, String body, int read, String mark) {
		try {
			String str_calendar = DF_DATE.format(calendar.getTime());
			String str = getInsertUrl(type);
			Object object[] = null;
			if (type == 3)
				object = new Object[] { address, str_calendar, body, read };
			else
				object = new Object[] { address, str_calendar, read, mark };
			mDb.execSQL(str, object);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 查看黑白名单是否存在
	public synchronized NativeCursor selectIsExists(NativeCursor nc) {
		try {
			Cursor c = mDb.query(
					nc.getmRequestType() == 1 ? TABLE_NAME_INTERCEPT_BLACKLIST
							: TABLE_NAME_INTERCEPT_WHITELIST, new String[] {
							SMSSTATUS, RINGSTATUS }, PHONENUM + " LIKE '%"+ nc.getmPhoneNum() + "%'", null, null, null, null);
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				nc.setmSmsStatus(c.getInt(c.getColumnIndex(SMSSTATUS)) > 0);
				nc.setmRingStatus(c.getInt(c.getColumnIndex(RINGSTATUS)) > 0);
				nc.setmIsExists(true);
			}
			if (c != null)
				c.close();
		} catch (Exception e) {
			e.printStackTrace();
			nc.setmIsExists(false);
		}
		return nc;

	}

	public synchronized NativeCursor selectIsExistsFuzzy(NativeCursor nc) {
		try {
			Cursor c = mDb.query(
					nc.getmRequestType() == 1 ? TABLE_NAME_INTERCEPT_BLACKLIST
							: TABLE_NAME_INTERCEPT_WHITELIST, new String[] {
							SMSSTATUS, RINGSTATUS },
					PHONENUM + " LIKE '%" + nc.getmPhoneNum() + "%'", null,
					null, null, null);
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				nc.setmSmsStatus(c.getInt(c.getColumnIndex(SMSSTATUS)) > 0);
				nc.setmRingStatus(c.getInt(c.getColumnIndex(RINGSTATUS)) > 0);
				nc.setmIsExists(true);
			}
			if (c != null)
				c.close();
		} catch (Exception e) {
			e.printStackTrace();
			nc.setmIsExists(false);
		}
		return nc;

	}

	public synchronized Cursor selectAllList(int type) {
		try {
			Cursor c = mDb.query(type == 1 ? TABLE_NAME_INTERCEPT_BLACKLIST
					: TABLE_NAME_INTERCEPT_WHITELIST, new String[] { ID,
					CONTACTNAME, PHONENUM, SMSSTATUS, RINGSTATUS, PHONETYPE },
					null, null, null, null, PHONETYPE + " asc");
			return c;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;

	}

	public synchronized Cursor selectAllInterceptHistoryList(int type) {
		try {
			Cursor c = mDb.query(type == 3 ? TABLE_NAME_INTERCEPT_MMS
					: TABLE_NAME_INTERCEPT_PHONE, new String[] { ID, ADDRESS,
					DATE, READ, type == 3 ? BODY : MARK }, null, null, null,
					null, "date DESC");
			return c;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;
	}

	public synchronized void changeReadStatus(int type) {
		try {
			Cursor c = mDb.query(type == 3 ? TABLE_NAME_INTERCEPT_MMS
					: TABLE_NAME_INTERCEPT_PHONE, new String[] { ID }, READ
					+ "=?", new String[] { "0" }, null, null, null);
			if (c != null && c.getCount() > 0) {
				while (c.moveToNext()) {
					int id = c.getInt(c.getColumnIndex(InterceptDataBase.ID));
					updateReadStatus(type, id);
				}
			}
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	public synchronized void updateReadStatus(int type, int id) {
		try {
			String updateTab = type == 3 ? TABLE_NAME_INTERCEPT_MMS
					: TABLE_NAME_INTERCEPT_PHONE;
			ContentValues mValues = new ContentValues();

			mValues.put(READ, 1);

			mDb.update(updateTab, mValues, ID + "=?", new String[] { id + "" });
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public synchronized void UpdateBlackWhiteList(NativeCursor nc) {
		try {
			String updateTab = nc.getmRequestType() == 1 ? TABLE_NAME_INTERCEPT_BLACKLIST
					: TABLE_NAME_INTERCEPT_WHITELIST;
			ContentValues mValues = new ContentValues();
			if (nc.getmContactName() != null) {
				mValues.put(CONTACTNAME, nc.getmContactName());
			}
			if (nc.getmPhoneNum() != null) {
				mValues.put(PHONENUM, nc.getmPhoneNum());
			}
			mValues.put(SMSSTATUS, nc.ismSmsStatus());
			mValues.put(RINGSTATUS, nc.ismRingStatus());
			mValues.put(PHONETYPE, nc.getmPhoneType());

			mDb.update(updateTab, mValues, ID + "=?",
					new String[] { nc.getmId() + "" });
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public synchronized void ClearBlackWhiteList(int mType) {
		try {
			String updateTab = mType == 1 ? TABLE_NAME_INTERCEPT_BLACKLIST
					: TABLE_NAME_INTERCEPT_WHITELIST;
			mDb.delete(updateTab, null, null);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public synchronized void DelBlackWhiteList(NativeCursor nc) {
		try {
			String updateTab = nc.getmRequestType() == 1 ? TABLE_NAME_INTERCEPT_BLACKLIST
					: TABLE_NAME_INTERCEPT_WHITELIST;
			mDb.delete(updateTab, ID + "=?", new String[] { nc.getmId() + "" });
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public synchronized void DelHistory(HistoryNativeCursor hnc) {
		try {
			String updateTab = hnc.getmRequestType() == 3 ? TABLE_NAME_INTERCEPT_MMS
					: TABLE_NAME_INTERCEPT_PHONE;
			mDb.delete(updateTab, ID + "=?", new String[] { hnc.getmId() + "" });
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public synchronized int getHistoryNum(HistoryNativeCursor hnc) {
		int sum = 0;
		try {
			String updateTab = hnc.getmRequestType() == 3 ? TABLE_NAME_INTERCEPT_MMS
					: TABLE_NAME_INTERCEPT_PHONE;
			Cursor c = mDb.query(updateTab, new String[] { ID }, "read=?",
					new String[] { "0" }, null, null, null);
			if (c != null) {
				sum = c.getCount();
				c.close();
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		return sum;
	}

	/**
	 * type 1 select user add keyword type 2 select all keywords unless user add
	 * 
	 * */
	public synchronized Cursor selectKeyWordList(int type) {

		Cursor c = mDb.query(TABLE_NAME_INTERCEPT_KEYWORD, new String[] { ID,
				KEYWORDZH }, KEYWORDTYPE + "=?", new String[] { type + "" },
				null, null, null);
		return c;

	}

	public synchronized void insertKeyWord(String keyword_zh, int type) {
		try {

			mDb.execSQL(getInsertUrl(5), new Object[] { keyword_zh, type });

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public synchronized void deleteKeyword(String keyword, int type){	
		String whereClause = KEYWORDZH + "=? AND " + KEYWORDTYPE + "=?";
		try {
			mDb.delete(TABLE_NAME_INTERCEPT_KEYWORD, whereClause, new String[] { keyword, String.valueOf(type) });

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * type 1 select user add keyword,the keywork is inside it type 2 select all
	 * keywords,the keywork is inside it
	 * 
	 * */
	public synchronized boolean selectKeyWord(String keyword, int type) {
		boolean isExists = false;
		try {
			if (type == 1) {
				Cursor c = mDb.query(TABLE_NAME_INTERCEPT_KEYWORD,
						new String[] { ID }, KEYWORDTYPE + "=? and "
								+ KEYWORDZH + "=?", new String[] { type + "",
								keyword }, null, null, null);
				if (c != null && c.getCount() > 0) {
					isExists = true;
				}
				if (c != null) {
					c.close();
				}
			} else {
				Cursor c = mDb.query(TABLE_NAME_INTERCEPT_KEYWORD,
						new String[] { ID }, KEYWORDZH + "=?",
						new String[] { keyword }, null, null, null);
				if (c != null && c.getCount() > 0) {
					isExists = true;
				}
				if (c != null) {
					c.close();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return isExists;
	}

	private String getInsertUrl(int type) {
		String str = "";
		switch (type) {
		case 1:
			str = "insert into " + TABLE_NAME_INTERCEPT_BLACKLIST + "("
					+ CONTACTNAME + "," + PHONENUM + "," + SMSSTATUS + ","
					+ RINGSTATUS + "," + PHONETYPE + ") values(?,?,?,?,?)";

			break;
		case 2:
			str = "insert into " + TABLE_NAME_INTERCEPT_WHITELIST + "("
					+ CONTACTNAME + "," + PHONENUM + "," + SMSSTATUS + ","
					+ RINGSTATUS + "," + PHONETYPE + ") values(?,?,?,?,?)";

			break;
		case 3:

			str = "insert into " + TABLE_NAME_INTERCEPT_MMS + "(" + ADDRESS
					+ "," + DATE + "," + BODY + "," + READ
					+ ") values(?,?,?,?)";

			break;
		case 4:
			str = "insert into " + TABLE_NAME_INTERCEPT_PHONE + "(" + ADDRESS
					+ "," + DATE + "," + READ + "," + MARK
					+ ") values(?,?,?,?)";
			break;
		case 5:
			str = "insert into " + TABLE_NAME_INTERCEPT_KEYWORD + "("
					+ KEYWORDZH + "," + KEYWORDTYPE + ") values(?,?)";

			break;

		}
		return str;

	}

	class DatabaseHelper extends SQLiteOpenHelper {
		private Context context;

		public DatabaseHelper(Context context) {

			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createTable(db);
		}

		private void createTable(SQLiteDatabase db) {

			for (int i = 1; i < 6; i++) {
				db.execSQL(getCreateTabStr(i));
			}
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			for (int i = 1; i < 6; i++) {
				db.execSQL(getCreateTabStr(i));
			}
			

		}

//		private void initKeyWord() {
//			String strAr[] = context.getResources().getStringArray(
//					R.array.intercept_keywords);
//			for (String str : strAr) {
//				boolean isExist = selectKeyWord(str, 2);
//				if (!isExist) {
//					insertKeyWord(str, 2);
//				}
//			}
//		}

		private String getCreateTabStr(int type) {
			String str = "";
			switch (type) {
			case 1:
				str = "CREATE TABLE IF NOT EXISTS "
						+ TABLE_NAME_INTERCEPT_BLACKLIST + "(" + ID
						+ " INTEGER PRIMARY KEY AUTOINCREMENT," + CONTACTNAME
						+ " TEXT," + PHONENUM + " TEXT NOT NULL," + SMSSTATUS
						+ " BOOLEAN," + RINGSTATUS + " BOOLEAN," + PHONETYPE
						+ " INTEGER)";

				break;
			case 2:
				str = "CREATE TABLE IF NOT EXISTS "
						+ TABLE_NAME_INTERCEPT_WHITELIST + "(" + ID
						+ " INTEGER PRIMARY KEY AUTOINCREMENT," + CONTACTNAME
						+ " TEXT," + PHONENUM + " TEXT NOT NULL," + SMSSTATUS
						+ " BOOLEAN," + RINGSTATUS + " BOOLEAN," + PHONETYPE
						+ " INTEGER)";

				break;
			case 3:

				str = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_INTERCEPT_MMS
						+ "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ ADDRESS + " TEXT NOT NULL," + DATE + " DATE," + BODY
						+ " TEXT," + READ + " INTEGER )";

				break;
			case 4:
				str = "CREATE TABLE IF NOT EXISTS "
						+ TABLE_NAME_INTERCEPT_PHONE + "(" + ID
						+ " INTEGER PRIMARY KEY AUTOINCREMENT," + ADDRESS
						+ " TEXT NOT NULL," + DATE + " DATE," + READ
						+ " INTEGER," + MARK + " TEXT)";
				break;
			case 5:
				str = "CREATE TABLE IF NOT EXISTS "
						+ TABLE_NAME_INTERCEPT_KEYWORD + "(" + ID
						+ " INTEGER PRIMARY KEY AUTOINCREMENT," + KEYWORDZH
						+ " TEXT," + KEYWORDTYPE + " INTEGER)";

				break;

			}
			return str;

		}

	}
}
