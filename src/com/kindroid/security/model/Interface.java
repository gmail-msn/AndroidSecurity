/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.model;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.kindroid.security.model.DatabaseHelper.Counters;
import com.kindroid.security.model.DatabaseHelper.DailyCounter;
import com.kindroid.security.model.DatabaseHelper.NetCounter;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.UtilShareprefece;

public class Interface extends AbstractModel implements IModelListener {

	private static final String[] INTERFACE_SUM = {
			"sum(" + DailyCounter.RX + ")", "sum(" + DailyCounter.TX + ")" };

	private static final int DAY_OF_YEAR = Calendar.DAY_OF_YEAR;

	private long mId;

	private final String mName;

	private final String mPrettyName;

	private final int mIcon;

	private List<TrafficCounter> mCounters;

	private final long[] mBytes = { 0, 0 };

	private final long[] mDelta = { 0, 0 };

	private Calendar mLastUpdate;

	private String mLastUpdateAsString;

	private Calendar mLastReset;

	private boolean mIsReset = false;

	private boolean mUpdateOnly = false;

	private final String mWhere;

	private final String mWhereInterface;

	public Interface(String name) {
		mName = name;
		mPrettyName = Device.getDevice().getPrettyName(name);
		mIcon = Device.getDevice().getIcon(name);
		mWhere = NetCounter.INTERFACE + "='" + name + "'";
		mWhereInterface = DailyCounter.INTERFACE + "='" + name + "'";
	}

	public synchronized long getId() {
		return mId;
	}

	public synchronized String getName() {
		return mName;
	}

	public synchronized String getPrettyName() {
		return mPrettyName;
	}

	public synchronized int getIcon() {
		return mIcon;
	}

	/**
	 * Checks if the time of the argument "now" represents another day than the
	 * last update field.
	 * 
	 * @param now
	 *            A {@link Calendar}.
	 * @return <code>true</code> if "now" if another day is a different day than
	 *         the last update field. <code>false</code> otherwise.
	 */
	private boolean isNewDay(Calendar now) {
		if (mLastUpdate == null) {
			return false;
		} else {
			return mLastUpdate.get(DAY_OF_YEAR) != now.get(DAY_OF_YEAR);
		}
	}

	public synchronized void updateBytes(long rx, long tx) {
		// Checks the parameters.

		// Log.d("type", getPrettyName());

		if (rx < 0) {
			throw new IllegalArgumentException("rx may not be smaller than 0: "
					+ rx);
		} else if (tx < 0) {
			throw new IllegalArgumentException("tx may not be smaller than 0: "
					+ tx);
		}

		Calendar now = Calendar.getInstance();
		boolean isNewDay = isNewDay(now);
		// Log.d("rx,tx", rx+";"+tx+";"+mBytes[0]+":"+mBytes[1]+";"+isNewDay);

		if (isNewDay) {
			int day = now.get(Calendar.DAY_OF_MONTH);
			SharedPreferences sh = KindroidSecurityApplication.sh;
			String day_str = sh.getString(UtilShareprefece.CAL_DATE_INT, "1");
			int day_mon = Integer.parseInt(day_str);
			if (mLastUpdate != null) {
				int ac_day = now.getActualMaximum(Calendar.DAY_OF_MONTH);
				if (ac_day >= day_mon) {
					if (day == day_mon) {
						UtilShareprefece.getShareprefece().storeMessage(sh,
								UtilShareprefece.LEFT_PER_MON_INT, "0");
					}
				} else {
					if (day == ac_day) {
						UtilShareprefece.getShareprefece().storeMessage(sh,
								UtilShareprefece.LEFT_PER_MON_INT, "0");
					}
				}

			}
		}
	//	if (mLastUpdate == null) {
			if ("Cell".equals(this.getPrettyName())) {
//				if (!KindroidSecurityApplication.sh
//						.contains(Constant.SHAREDPREFERENCES_NETWORKRX)){
					if (!KindroidSecurityApplication.sh
							.contains(getName())) {
						Editor editor = KindroidSecurityApplication.sh.edit();
						editor.putLong(getName(), rx);
						editor.commit();
						mBytes[0] = rx;
						mBytes[1] = tx;	
					}
//				}				
			} else if ("Wi-Fi".equals(this.getPrettyName())) {
				if (!KindroidSecurityApplication.sh
						.contains(Constant.SHAREDPREFERENCES_NETWORKWIFIRX)) {
					Editor editor = KindroidSecurityApplication.sh.edit();
					editor.putLong(Constant.SHAREDPREFERENCES_NETWORKWIFIRX, rx);
					editor.commit();
					mBytes[0] = rx;
					mBytes[1] = tx;

				}
			}
	//	}

		// Last update time.
		mLastUpdate = now;
		mLastUpdateAsString = DatabaseHelper.getLocaleDateTime(mLastUpdate);

		if (rx == mBytes[0] && tx == mBytes[1] && !isNewDay) {
			// mUpdateOnly = true;
			mDelta[0] = 0;
			mDelta[1] = 0;
			setDirty(true);
			fireModelChanged();
			for (TrafficCounter counter : getCounters()) {
				if (isNewDay || needsUpdate(counter)) {
					counter.setDirty(true);
					fireModelChanged(counter);
				}
			}
		} else {

			if (rx < mBytes[0]) {
				mDelta[0] = rx;
			} else {
				mDelta[0] = rx - mBytes[0];
			}
			// Computes the delta for tx.
			if (tx < mBytes[1]) {
				mDelta[1] = tx;
			} else {
				mDelta[1] = tx - mBytes[1];
			}
			// The current byte values.
			mBytes[0] = rx;
			mBytes[1] = tx;
			setDirty(true);
			fireModelChanged();
			//
			for (TrafficCounter counter : getCounters()) {
				if (isNewDay || needsUpdate(counter)) {
					counter.setDirty(true);
					fireModelChanged(counter);
				}
			}

		}
	}

	/**
	 * Checks if the counter needs to be update. This is the case if the
	 * activity is in the foreground or if the counter has an alarm.
	 * 
	 * @param counter
	 *            A {@link TrafficCounter}
	 * @return <code>true</code> if the counter has to be update,
	 *         <code>false</code> otherwise.
	 */
	private boolean needsUpdate(TrafficCounter counter) {
		return (KindroidSecurityApplication.getUpdatePolicy() == KindroidSecurityApplication.SERVICE_HIGH)
				|| (KindroidSecurityApplication.getUpdatePolicy() == KindroidSecurityApplication.SERVICE_MID)
				|| (counter.getProperty(TrafficCounter.ALERT_BYTES) != null);
	}

	public synchronized Calendar getLastUpdate() {
		return mLastUpdate;
	}

	public synchronized String getLastUpdateAsString() {
		return mLastUpdateAsString;
	}

	public synchronized Calendar getLastReset() {
		return mLastReset;
	}

	public synchronized void reset() {
		mIsReset = true;
		setDirty(true);
		fireModelChanged();
		//
		for (TrafficCounter counter : getCounters()) {
			counter.setDirty(true);
			fireModelChanged(counter);
		}
	}

	public synchronized void addCounter(TrafficCounter counter) {
		if (mCounters == null) {
			mCounters = new ArrayList<TrafficCounter>();
		}
		counter.setNew(true);
		mCounters.add(counter);
		counter.addModelListener(this);
		fireModelChanged(counter);
	}

	public synchronized void removeCounter(TrafficCounter counter) {
		if (mCounters != null) {
			mCounters.remove(counter);
			counter.setDeleted(true);
			counter.removeModelListener(this);
			fireModelChanged(counter);
		}
	}

	public synchronized TrafficCounter getCounter(long id) {
		for (TrafficCounter counter : mCounters) {
			if (counter.getId() == id) {
				return counter;
			}
		}
		return null;
	}

	public synchronized List<TrafficCounter> getCounters() {
		if (mCounters == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(mCounters);
	}

	public void modelLoaded(IModel object) {
		// Nothing to do.
	}

	public void modelChanged(IModel object) {
		fireModelChanged(object);
	}

	public synchronized void load(SQLiteDatabase db) {
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		query.setTables(NetCounter.TABLE_NAME);

		Cursor c = query.query(db, null, mWhere, null, null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			mId = c.getLong(c.getColumnIndex(NetCounter._ID));
			// Last update.
			String date = c.getString(c.getColumnIndex(NetCounter.LAST_UPDATE));
			mLastUpdate = DatabaseHelper.parseDateTime(date);
			mLastUpdateAsString = DatabaseHelper.getLocaleDateTime(mLastUpdate);
			// Last reset.
			date = c.getString(c.getColumnIndex(NetCounter.LAST_RESET));
			mLastReset = DatabaseHelper.parseDateTime(date);
			mBytes[0] = c.getLong(c.getColumnIndex(NetCounter.LAST_RX));
			mBytes[1] = c.getLong(c.getColumnIndex(NetCounter.LAST_TX));
			// Log.d("inter", "lastupdate"+mLastUpdate);
			// Log.d("inter", "lastupdate"+mLastUpdateAsString);
			// Log.d("inter", "lastupdate"+mLastUpdate);
			// Log.d("inter", "lastupdate"+mBytes[0]);

		}
		// Log.d("inter", "load");
		c.close();

		// Loads counters.
		query = new SQLiteQueryBuilder();
		query.setTables(Counters.TABLE_NAME);

		c = query.query(db, null, mWhere, null, null, null, Counters.POS);
		for (int i = 0; i < c.getCount(); i++) {
			c.moveToNext();
			long id = c.getLong(c.getColumnIndex(Counters._ID));
			TrafficCounter counter = new TrafficCounter(id, this);
			addCounter(counter);
			counter.setNew(false);
		}
		c.close();
	}

	public synchronized void insert(SQLiteDatabase db) {
		Cursor cs = db.query(NetCounter.TABLE_NAME,
				new String[] { NetCounter.INTERFACE }, NetCounter.INTERFACE
						+ "='" + mName + "'", null, null, null, null);
		if (cs.getCount() > 0) {
			return;
		}
		Calendar c = Calendar.getInstance();
		ContentValues values = new ContentValues();
		values.put(NetCounter.INTERFACE, mName);
		values.put(NetCounter.LAST_RX, 0);
		values.put(NetCounter.LAST_TX, 0);
		String now = DatabaseHelper.getDateTime(c);
		values.put(NetCounter.LAST_UPDATE, now);
		values.put(NetCounter.LAST_RESET, now);

		db.insert(NetCounter.TABLE_NAME, null, values);
		mLastReset = c;
	}

	public void remove(SQLiteDatabase db) {
		// Not needed.
	}

	public synchronized void update(SQLiteDatabase db) {
		// Only update the last update field.
		if (mUpdateOnly) {
			updateLastUpdate(db);

			mUpdateOnly = false;
		} else {
			updateSession(db, mBytes);
			updateDailySession(db, mDelta);

			mDelta[0] = 0;
			mDelta[1] = 0;
		}

		if (mIsReset) {
			Calendar c = Calendar.getInstance();

			String now = DatabaseHelper.getDateTime(c);
			ContentValues values = new ContentValues();
			values.put(NetCounter.INTERFACE, mName);
			values.put(NetCounter.LAST_RESET, now);
			values.put(NetCounter.LAST_UPDATE, now);

			db.update(NetCounter.TABLE_NAME, values, mWhere, null);
			db.delete(DailyCounter.TABLE_NAME, mWhereInterface, null);

			mLastReset = c;
			mIsReset = false;
		}
	}

	private void updateLastUpdate(SQLiteDatabase db) {
		ContentValues values = new ContentValues();

		String now = DatabaseHelper.getDateTime(Calendar.getInstance());
		values.put(NetCounter.LAST_UPDATE, now);

		db.update(NetCounter.TABLE_NAME, values, mWhere, null);
	}

	private void updateSession(SQLiteDatabase db, long[] bytes) {
		ContentValues values = new ContentValues();
		values.put(NetCounter.INTERFACE, mName);
		values.put(NetCounter.LAST_RX, bytes[0]);
		values.put(NetCounter.LAST_TX, bytes[1]);

		String now = DatabaseHelper.getDateTime(Calendar.getInstance());
		values.put(NetCounter.LAST_UPDATE, now);

		db.update(NetCounter.TABLE_NAME, values, mWhere, null);
	}

	private void updateDailySession(SQLiteDatabase db, long[] bytes) {
		ContentValues values = new ContentValues();
		values.put(DailyCounter.INTERFACE, mName);

		Calendar now = Calendar.getInstance();
		Cursor c = getDailySession(db, now);

		if (c.getCount() > 0) {
			c.moveToFirst();
			long rx = c.getLong(c.getColumnIndex(DailyCounter.RX));
			long tx = c.getLong(c.getColumnIndex(DailyCounter.TX));
			values.put(DailyCounter.RX, rx + bytes[0]);
			values.put(DailyCounter.TX, tx + bytes[1]);

			StringBuilder where = new StringBuilder(mWhereInterface);
			where.append(" AND ");
			where.append(DailyCounter.DAY);
			where.append("='");
			where.append(DatabaseHelper.getDate(now));
			where.append("'");

			db.update(DailyCounter.TABLE_NAME, values, where.toString(), null);
		} else {
			values.put(DailyCounter.DAY, DatabaseHelper.getDate(now));
			values.put(DailyCounter.RX, bytes[0]);
			values.put(DailyCounter.TX, bytes[1]);
			db.insert(DailyCounter.TABLE_NAME, null, values);
		}

		c.close();
	}

	private Cursor getDailySession(SQLiteDatabase db, Calendar now) {
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		query.setTables(DailyCounter.TABLE_NAME);

		StringBuilder where = new StringBuilder(mWhereInterface);
		where.append(" AND ");
		where.append(DailyCounter.DAY);
		where.append("='");
		where.append(DatabaseHelper.getDate(now));
		where.append("'");

		return query.query(db, null, where.toString(), null, null, null, null);
	}

	public long[] getInterfaceBytes(SQLiteDatabase db, Calendar from,
			Calendar to) {
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		query.setTables(DailyCounter.TABLE_NAME);

		StringBuilder where = new StringBuilder(mWhereInterface);

		if (from != null) {
			where.append(" AND ");
			where.append(DailyCounter.DAY);
			where.append(">='");
			where.append(DatabaseHelper.getDate(from));
			where.append("'");
		}

		if (to != null) {
			where.append(" AND ");
			where.append(DailyCounter.DAY);
			where.append("<='");
			where.append(DatabaseHelper.getDate(to));
			where.append("'");
		}

		Cursor c = query.query(db, INTERFACE_SUM, where.toString(), null, null,
				null, null);
		try {
			if (c.getCount() > 0) {
				c.moveToFirst();
				return new long[] { c.getLong(0), c.getLong(1) };
			} else {
				return new long[] { 0, 0 };
			}
		} finally {
			c.close();
		}
	}

}
