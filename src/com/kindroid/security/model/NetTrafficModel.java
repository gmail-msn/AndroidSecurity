/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.kindroid.security.model.DatabaseHelper.DailyCounter;
import com.kindroid.security.model.DatabaseHelper.NetCounter;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;
import android.util.Log;

public class NetTrafficModel extends AbstractModel implements IModelListener {

	private static final char FIELD_SEPARATOR = ',';

	private final SQLiteOpenHelper mHelper;

	private List<Interface> mInterface;

	private final List<IModel> mQueue = new ArrayList<IModel>();

	private List<IOperation> mListeners;

	private volatile boolean mLoaded = false;

	public NetTrafficModel(Context context) {
		mHelper = new DatabaseHelper(context);
	}

	public void addOperationListener(IOperation listener) {
		if (mListeners == null) {
			mListeners = new ArrayList<IOperation>();
		}
		mListeners.add(listener);
	}

	public void removeOperationListener(IOperation listener) {
		if (mListeners != null) {
			mListeners.remove(listener);
		}
	}

	private void addInterface(Interface inter) {
		if (mInterface == null) {
			mInterface = new ArrayList<Interface>();
		}
		inter.setNew(true);
		mInterface.add(inter);
		inter.addModelListener(this);
		modelChanged(inter);
	}

	@SuppressWarnings("unchecked")
	public List<Interface> getInterfaces() {
		if (mInterface == null) {
			return Collections.EMPTY_LIST;
		}
		return Collections.unmodifiableList(mInterface);
	}

	public Interface getInterface(long id) {
		for (Interface inter : getInterfaces()) {
			if (inter.getId() == id) {
				return inter;
			}
		}
		return null;
	}

	public synchronized Interface getInterface(String name) {
		for (Interface inter : getInterfaces()) {
			if (inter.getName().equals(name)) {
				return inter;
			}
		}
		// New interface found. Adds some defaults.
		Interface inter = new Interface(name);
		addInterface(inter);
		TrafficCounter counter = new TrafficCounter(inter);
		counter.setType(TrafficCounter.TOTAL);
		inter.addCounter(counter);
		counter = new TrafficCounter(inter);
		counter.setType(TrafficCounter.MONTHLY);
		inter.addCounter(counter);
		counter = new TrafficCounter(inter);
		counter.setType(TrafficCounter.LAST_DAYS);
		counter.setProperty(TrafficCounter.NUMBER, "0");
		inter.addCounter(counter);
		counter = new TrafficCounter(inter);
		counter.setType(TrafficCounter.SINGLE_DAY);
		counter.setProperty(TrafficCounter.NUMBER, "0");
		inter.addCounter(counter);
		return inter;
	}

	public void load() {
		modelChanged(this);
		commit();
		fireModelLoaded();
		mLoaded = true;
	
	}

	public boolean isLoaded() {
		return mLoaded;
	}

	public void modelLoaded(IModel object) {
		// Nothing to do.
	}

	public void modelChanged(IModel object) {
		mQueue.add(object);
	}

	@Override
	public boolean isDirty() {
		return !mQueue.isEmpty();
	}

	public void commit() {
		operationStarted();

		SQLiteDatabase db = mHelper.getWritableDatabase();
		boolean modelChanged = !mQueue.isEmpty();

		do {
			List<IModel> tmp = new ArrayList<IModel>(mQueue);
			
			mQueue.clear();

			for (IModel object : tmp) {
				try {
					
					if (object.isNew()) {
						object.insert(db);
					} else if (object.isDirty()) {
						object.update(db);
					} else if (object.isDeleted()) {
						object.remove(db);
					} else {
						object.load(db);
					}
				} catch (Exception e) {
					//Log.e(getClass().getName(), "Database error", e);
				}
				
				if (object instanceof AbstractModel) {
					AbstractModel am = (AbstractModel) object;
					if (am.isNew()) {
						am.setNew(false);
					} else if (am.isDirty()) {
						am.setDirty(false);
					} else if (am.isNew()) {
						am.setNew(false);
					}
				}
			}
		} while (isDirty());

		operationEnded();

		if (modelChanged) {
			fireModelChanged();
		}
	}

	public void rollback() {
		mQueue.clear();
	}

	public void insert(SQLiteDatabase db) {
	}

	public void load(SQLiteDatabase db) {
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		query.setTables(NetCounter.TABLE_NAME);
		Cursor cursor = query.query(db, new String[] { NetCounter.INTERFACE }, null, null, null,
				null, null);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext();
			// Creates interface.
			Interface inter = new Interface(cursor.getString(0));
			addInterface(inter);
			inter.setNew(false);
		}
		cursor.close();
	}

	public void remove(SQLiteDatabase db) {
	}

	public void update(SQLiteDatabase db) {
	}

	public Cursor getLastDaysCursor(String inter, int days) {
		operationStarted();
		SQLiteDatabase db = mHelper.getReadableDatabase();
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		query.setTables(DailyCounter.TABLE_NAME);
		query.appendWhere(DailyCounter.INTERFACE + "='" + inter + "'");
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_YEAR, -days);
		query.appendWhere(" AND " + DailyCounter.DAY + ">='" + DatabaseHelper.getDate(now) + "'");
		Cursor c = query.query(db, null, null, null, null, null, DailyCounter.DAY);
		// db.close();
		operationEnded();
		return c;
	}

	public Cursor getLastMonthsCursor(String inter, int months) {
		
		operationStarted();
		SQLiteDatabase db = mHelper.getReadableDatabase();
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		query.setTables(DailyCounter.TABLE_NAME);
		query.appendWhere(DailyCounter.INTERFACE + "='" + inter + "'");
		Calendar now = Calendar.getInstance();
		now.set(Calendar.DAY_OF_MONTH, 1);
		now.add(Calendar.MONTH, -months);
		query.appendWhere(" AND " + DailyCounter.DAY + ">='" + DatabaseHelper.getDate(now) + "'");
		String groupBy = "strftime('%Y%m', day)";
		String[] proj = { DailyCounter.INTERFACE,
				"strftime('%Y-%m-01', day) as " + DailyCounter.DAY,
				"sum(rx) as " + DailyCounter.RX, "sum(tx) as " + DailyCounter.TX };
		Cursor c = query.query(db, proj, null, null, groupBy, null, DailyCounter.DAY);
		// db.close();
		operationEnded();
		return c;
	}

	/**
	 * Dumps the database content to a file at the root of the SD card. Uses the
	 * CSV format.
	 * 
	 * @return The filename where the data were dumped.
	 * @throws IOException
	 */
	public String exportDataToCsv() throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append(Environment.getExternalStorageDirectory());
		sb.append("/nettraffic-");
		sb.append(System.currentTimeMillis());
		sb.append(".csv");
		// Runs the export.
		FileWriter writer = null;
		String s = sb.toString();
		try {
			writer = new FileWriter(s);
			exportDataToCsv(writer);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// Ignore.
				}
			}
		}
		return s;
	}

	/**
	 * Dumps the database content to a file. Uses the CSV format.
	 * 
	 * @param writer
	 *            A {@link FileWriter}.
	 * @throws IOException
	 */
	public void exportDataToCsv(FileWriter writer) throws IOException {
		operationStarted();
		SQLiteDatabase db = mHelper.getReadableDatabase();
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		query.setTables(DailyCounter.TABLE_NAME);
		Cursor c = query.query(db, null, null, null, null, null, DailyCounter.DAY + ','
				+ DailyCounter.INTERFACE);
		try {
			// Writes headers.
			StringBuilder sb = new StringBuilder();
			sb.append(DailyCounter.INTERFACE).append(FIELD_SEPARATOR);
			sb.append(DailyCounter.DAY).append(FIELD_SEPARATOR);
			sb.append(DailyCounter.RX).append(FIELD_SEPARATOR);
			sb.append(DailyCounter.TX).append('\n');
			writer.append(sb.toString());
			// Writes data.
			for (int i = 0; i < c.getCount(); i++) {
				c.moveToNext();
				String inter = c.getString(c.getColumnIndex(DailyCounter.INTERFACE));
				String day = c.getString(c.getColumnIndex(DailyCounter.DAY));
				long rx = c.getLong(c.getColumnIndex(DailyCounter.RX));
				long tx = c.getLong(c.getColumnIndex(DailyCounter.TX));
				sb = new StringBuilder();
				sb.append(inter).append(FIELD_SEPARATOR);
				sb.append(day).append(FIELD_SEPARATOR);
				sb.append(rx).append(FIELD_SEPARATOR);
				sb.append(tx).append('\n');
				writer.append(sb.toString());
			}
		} finally {
			c.close();
			// db.close();
			operationEnded();
		}
	}

	/**
	 * Imports data from files stored on the SD card. The file format and
	 * location are the same as the export function.
	 * 
	 * @throws IOException
	 */
	public void importDataFromCsv() throws IOException {
		operationStarted();

		SQLiteDatabase db = mHelper.getReadableDatabase();

		File root = Environment.getExternalStorageDirectory();
		File[] files = root.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String f) {
				return f.startsWith("netcounter-") && f.endsWith(".csv");
			}
		});

		Arrays.sort(files);

		String line;
		String lastDate = "";

		db.beginTransaction();
		try {
			for (File f : files) {
				BufferedReader br = new BufferedReader(new FileReader(f));
				// Skip the first line which contains the headers.
				br.readLine();
				String[] v = { "", "", "", "" };
				while ((line = br.readLine()) != null) {
					v = line.split(Character.toString(FIELD_SEPARATOR));

					boolean update = lastDate.equals(v[1]);

					StringBuilder sb = new StringBuilder();
					sb.append(DailyCounter.INTERFACE);
					sb.append("='");
					sb.append(v[0]);
					sb.append("' AND ");
					sb.append(DailyCounter.DAY);
					sb.append("='");
					sb.append(v[1]);
					sb.append("'");

					SQLiteQueryBuilder q = new SQLiteQueryBuilder();
					q.setTables(DailyCounter.TABLE_NAME);
					q.appendWhere(sb.toString());
					Cursor c = q.query(db, null, null, null, null, null, null);

					if (c.getCount() == 0) {
						ContentValues values = new ContentValues();
						values.put(DailyCounter.INTERFACE, v[0]);
						values.put(DailyCounter.DAY, v[1]);
						values.put(DailyCounter.RX, v[2]);
						values.put(DailyCounter.TX, v[3]);

						db.insert(DailyCounter.TABLE_NAME, null, values);
					} else if (update) {
						ContentValues values = new ContentValues();
						values.put(DailyCounter.INTERFACE, v[0]);
						values.put(DailyCounter.DAY, v[1]);
						values.put(DailyCounter.RX, v[2]);
						values.put(DailyCounter.TX, v[3]);

						String where = sb.toString();
						db.update(DailyCounter.TABLE_NAME, values, where, null);
					}

					c.close();
				}
				lastDate = v[1];
				br.close();
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			// db.close();
			operationEnded();
		}
	}

	private void operationStarted() {
		if (mListeners != null) {
			for (IOperation listener : mListeners) {
				listener.operationStarted();
			}
		}
	}

	private void operationEnded() {
		if (mListeners != null) {
			for (IOperation listener : mListeners) {
				listener.operationEnded();
			}
		}
	}

}
