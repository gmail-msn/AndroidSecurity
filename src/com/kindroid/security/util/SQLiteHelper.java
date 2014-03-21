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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper{
    private static String DB_PATH = "/data/data/com.kindroid.security/databases/";
    private static String DB_NAME = "Malware.db";
    private static String CERT_TABLE_NAME = "CertBlackList";
    private SQLiteDatabase myDataBase; 
    private final Context myContext;

    public SQLiteHelper(Context context) {
 
    	super(context, DB_NAME, null, 1);
        this.myContext = context;
    }	

    public void createDataBase() throws IOException{
 
    	boolean dbExist = checkDataBase();
 
    	if (!dbExist) {
        	this.getReadableDatabase();
 
        	try {
    			copyDataBase();
 
    		} catch (IOException e) {
 
        		throw new Error("Error copying database");
 
        	}
    	}
 
    }

    private boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
    		//database does't exist yet.
    	}
 
    	if(checkDB != null){
    		checkDB.close();
    	}

    	return checkDB != null ? true : false;
    }

    private void copyDataBase() throws IOException{ 
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME); 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME; 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName); 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
 
    }
 
    public void openDataBase() throws SQLException{
 
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    }
    
    public ArrayList<String> getBlackList() {
    	ArrayList<String> certList = new ArrayList<String>();
       	
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		query.setTables(CERT_TABLE_NAME);

		Cursor cursor = query.query(myDataBase, null, null, null, null, null, null);
		for (int i = 0; i < cursor.getCount(); i++)  {
			cursor.moveToNext();
			String hash = cursor.getString(cursor.getColumnIndex("hash"));

			certList.add(hash);
		}
		cursor.close();
		
		return certList;
    }
    
    @Override
	public synchronized void close() {
    	    if(myDataBase != null)
    		    myDataBase.close();
 
    	    super.close();
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
	}
}