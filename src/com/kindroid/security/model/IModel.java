/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.model;

import android.database.sqlite.SQLiteDatabase;

public interface IModel {

	public boolean isNew();

	public boolean isDirty();

	public boolean isDeleted();

	public void load(SQLiteDatabase db);

	public void insert(SQLiteDatabase db);

	public void update(SQLiteDatabase db);

	public void remove(SQLiteDatabase db);

	public void addModelListener(IModelListener listener);

	public void removeModelListener(IModelListener listener);

}
