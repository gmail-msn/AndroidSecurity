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
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class BackupDBHelper extends SQLiteOpenHelper {
	public static final String logDBName = "BackupLog.db";
	public static final String logTableName = "backup_log";
	private static final int VERSION = 1;
	
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_FLAG = "flag";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_NUM = "num";
	public static final String COLUMN_NTRAF = "nettraffic";
	
	public BackupDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	public BackupDBHelper(Context context, String name){
		this(context, name, VERSION);
	}
	public BackupDBHelper(Context context, String name, int version){
		this(context, name, null, version);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE IF NOT EXISTS backup_log ("
				+ " time Long,"
                + " flag INTEGER,"
                + " type INTEGER,"
                + " num INTEGER,"
                + " nettraffic INTEGER"
                + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
