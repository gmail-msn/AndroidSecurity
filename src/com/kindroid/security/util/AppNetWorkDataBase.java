/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AppNetWorkDataBase {

	private final String DATABASE_NAME = "app_network.db";
	private final int DATABASE_VERSION = 1;
	private final String TABLE_NAME_APPDAY = "app_day";
	private final String TABLE_NAME_LAST = "app_last";

	private final String ID = "appid";
	private final String PKG = "pkg";
	private final String RX = "rx";
	private final String TX = "tx";
	private final String LAST_RX = "last_rx";
	private final String LAST_TX = "last_tx";
	private final String DATE = "date";
	private final String TYPE_ID = "type_id";

	private static final DateFormat DF_DATE = new SimpleDateFormat("yyyy-MM-dd");

	private final String insertVoucherDetailUrl = "insert into "
			+ TABLE_NAME_APPDAY + "(" + PKG + "," + RX + "," + TX + "," + DATE
			+ "," + TYPE_ID + ") values(?,?,?,?,?)";

	private final String updateDetailUrl = "update " + TABLE_NAME_APPDAY
			+ " set " + RX + "=?," + TX + "=?" + " where " + PKG + "=? and "
			+ DATE + "=? and " + TYPE_ID + "=?";

	private final String[] ALL_COLUMNS = new String[] { PKG, RX, TX };

	private static SQLiteDatabase mDb;

	private static AppNetWorkDataBase mDataBase;

	private AppNetWorkDataBase(Context context) {
		mDb = new DatabaseHelper(context).getWritableDatabase();
	}

	public synchronized static AppNetWorkDataBase get(Context context) {
		if (mDataBase == null) {
			mDataBase = new AppNetWorkDataBase(context);
		}
		return mDataBase;
	}

	public synchronized static void close() {
		try {
			mDb.close();
			mDataBase = null;
		} catch (Exception e) {
			e.printStackTrace();
			mDataBase = null;
		}

	}

	public synchronized Cursor searchExit(String pkg, String cac)
			throws Exception {
		Cursor c = mDb.query(TABLE_NAME_APPDAY,
				new String[] { RX, TX, TYPE_ID }, "pkg=? and date=?",
				new String[] { pkg + "", cac + "" }, null, null, null);
		return c;
	}

	public synchronized long[] searchLastUpdate(String pkg, long rx, long tx,
			int addOrdel) throws Exception {
		long[] temp = new long[2];
		if (addOrdel == 1)
			return temp;

		Cursor c = mDb.query(TABLE_NAME_LAST,
				new String[] { LAST_RX, LAST_TX, }, "pkg=?", new String[] { pkg
						+ "" }, null, null, null);
		if (c == null || c.getCount() == 0) {
			String insertLast = "insert into " + TABLE_NAME_LAST + "(" + PKG
					+ "," + LAST_RX + "," + LAST_TX + ") values(?,?,?)";
			mDb.execSQL(insertLast, new Object[] { pkg, rx, tx });

		} else {
			c.moveToFirst();
			temp[0] = c.getLong(c.getColumnIndex(LAST_RX));
			temp[1] = c.getLong(c.getColumnIndex(LAST_TX));

			String updateLastUrl = "update " + TABLE_NAME_LAST + " set "
					+ LAST_RX + "=?," + LAST_TX + "=?" + " where " + PKG + "=?";
			mDb.execSQL(updateLastUrl, new Object[] { rx, tx, pkg });
		}
		if (c != null) {
			c.close();
		}

		return temp;

	}

	public synchronized void insertOrUpdateDetail(String pkg, long rx, long tx,
			Calendar calender, int addOrDelType) {
		String cal = DF_DATE.format(calender.getTime());
		try {
			Cursor c = searchExit(pkg, cal);
			long[] temp = searchLastUpdate(pkg, rx, tx, addOrDelType);
			if (c == null || c.getCount() == 0) {

				if (addOrDelType == 0) {
					long deta[] = new long[2];
					if (rx < temp[0]) {
						deta[0] = rx;
					} else {
						deta[0] = rx - temp[0];
					}
					if (tx < temp[1]) {
						deta[1] = tx;
					} else {
						deta[1] = tx - temp[1];
					}

					mDb.execSQL(insertVoucherDetailUrl, new Object[] { pkg,
							deta[0], deta[1], cal, 0 });
				}

			} else if (c.getCount() == 1 && addOrDelType == 0) {
				c.moveToFirst();

				long rx_sum = c.getLong(c.getColumnIndex(RX));
				long tx_sum = c.getLong(c.getColumnIndex(TX));

				insertOrUpdate(pkg, rx, tx, rx_sum, tx_sum, temp[0], temp[1],
						cal, addOrDelType);

			} else if (c.getCount() == 1 && addOrDelType == 1) {
				c.moveToFirst();

				long rx_sum = c.getLong(c.getColumnIndex(RX));
				long tx_sum = c.getLong(c.getColumnIndex(TX));
				mDb.execSQL(insertVoucherDetailUrl, new Object[] { pkg,
						-rx_sum, -tx_sum, cal, addOrDelType });

			} else if (c.getCount() >= 2 && addOrDelType == 0) {

				while (c.moveToNext()) {
					int type = c.getInt(c.getColumnIndex(TYPE_ID));
					if (type == 0) {
						long rx_sum = c.getLong(c.getColumnIndex(RX));
						long tx_sum = c.getLong(c.getColumnIndex(TX));

						insertOrUpdate(pkg, rx, tx, rx_sum, tx_sum, temp[0],
								temp[1], cal, addOrDelType);
						break;
					}
				}

			} else if (c.getCount() >= 2 && addOrDelType == 1) {
				while (c.moveToNext()) {
					int type = c.getInt(c.getColumnIndex(TYPE_ID));
					if (type == 0) {
						long rx_sum = c.getLong(c.getColumnIndex(RX));
						long tx_sum = c.getLong(c.getColumnIndex(TX));
						mDb.execSQL(updateDetailUrl, new Object[] { -rx_sum,
								-tx_sum, pkg, cal, addOrDelType });
						break;
					}
				}
			}
			if (c != null) {
				c.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	synchronized void insertOrUpdate(String pkg, long rx, long tx, long rx_sum,
			long tx_sum, long last_rx, long last_tx, String cal,
			int addOrDelType) {
		if (rx == last_rx && tx == last_tx) {
			return;
		} else {
			long deta[] = new long[2];
			if (rx < last_rx) {
				deta[0] = rx;
			} else {
				deta[0] = rx - last_rx;
			}
			if (tx < last_tx) {
				deta[1] = tx;
			} else {
				deta[1] = tx - last_tx;
			}
			mDb.execSQL(updateDetailUrl, new Object[] { rx_sum + deta[0],
					tx_sum + deta[1], pkg, cal, addOrDelType });
		}

	}

	public  Cursor lookAllAppData(Calendar from, Calendar to) {
		try {
			String begin = null;
			String end = null;
			if (from == null)
				return null;
			if (to == null) {
				to = from;
			}
			begin = DF_DATE.format(from.getTime());
			end = DF_DATE.format(to.getTime());

			String str_sql = "SELECT pkg,rx,tx from app_day where date BETWEEN '"
					+ begin + "' AND '" + end + "'";

			return mDb.rawQuery(str_sql, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	
	public Cursor getAppDayAndMonthData(Calendar current_date) {
		try {
			String now = null;
			if (current_date == null)
				return null;
			
			now = DF_DATE.format(current_date.getTime());
			
			String str_sql = "select days.[pkg], days.total_day, app_month.total_month " + 
					"from " +
				    "(select day.[pkg], (sum(day.rx) + sum(day.tx)) total_day " +
				     "from app_day day " + 
				     "where date = '" + now + "' " + 
				     "group by day.pkg) days " +
				"left outer join " +
				     "(select day.[pkg], (sum(day.rx) + sum(day.tx)) total_month " + 
				      "from app_day day " + 
				      "where " + 
				            "date between datetime('" + now + "' " + ",'start of month') and '" + now + "' " +   
				      "group by day.pkg) app_month " +
				"on " + 
				   "days.[pkg] = app_month.pkg ";

			return mDb.rawQuery(str_sql, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public synchronized void delAll() {
		try{
			mDb.execSQL("delete from " + TABLE_NAME_APPDAY);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createTable(db);
		}

		private void createTable(SQLiteDatabase db) {
			String str = "CREATE TABLE " + TABLE_NAME_APPDAY + "(" + ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT ," + PKG
					+ " TEXT NOT NULL," + RX + " LONG NOT NULL," + TX
					+ " LONG NOT NULL," + DATE + " DATE," + TYPE_ID
					+ " INTEGER NOT NULL )";
			String str_last = "CREATE TABLE " + TABLE_NAME_LAST + "(" + ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT ," + PKG
					+ " TEXT NOT NULL," + LAST_RX + " LONG," + LAST_TX
					+ " LONG)";

			// Log.d(str, str);
			db.execSQL(str);
			db.execSQL(str_last);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			String str = "CREATE TABLE " + TABLE_NAME_APPDAY + "(" + ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT ," + PKG
					+ " TEXT NOT NULL," + RX + " LONG NOT NULL," + TX
					+ " LONG NOT NULL," + DATE + " DATE," + TYPE_ID
					+ " INTEGER NOT NULL )";
			String str_last = "CREATE TABLE " + TABLE_NAME_LAST + "(" + ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT ," + PKG
					+ " TEXT NOT NULL," + LAST_RX + " LONG," + LAST_TX
					+ " LONG)";

			// Log.d(str, str);
			db.execSQL(str);
			db.execSQL(str_last);

		}
	}
}
