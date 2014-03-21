/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDBHelper extends SQLiteOpenHelper {
	public static final String historyDBName = "virus_history.db";
	public static final String historyTableName = "scan_history";
	public static final String cleanHistoryTableName = "clean_history";
	private static final int VERSION = 2;

	public SQLiteDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	public SQLiteDBHelper(Context context, String name){
		this(context, name, VERSION);
	}
	public SQLiteDBHelper(Context context, String name, int version){
		this(context, name, null, version);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE IF NOT EXISTS scan_history ("
                + " time Long,"
                + " virus_num INTEGER,"
                + " care_num INTEGER"
                + ");");
		db.execSQL("CREATE TABLE IF NOT EXISTS clean_history (time Long, label TEXT, rank INTEGER, vname TEXT, pname TEXT, pversion TEXT);");
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub	
		db.execSQL("CREATE TABLE IF NOT EXISTS scan_history ("
                + " time Long,"
                + " virus_num INTEGER,"
                + " care_num INTEGER"
                + ");");
		db.execSQL("CREATE TABLE IF NOT EXISTS clean_history (time Long, label TEXT, rank INTEGER, vname TEXT, pname TEXT, pversion TEXT);");
	}	

}
