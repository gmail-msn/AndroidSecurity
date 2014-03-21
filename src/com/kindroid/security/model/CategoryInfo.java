/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.model;

import android.graphics.drawable.Drawable;

/**
 * @author heli.zhao
 *
 */
public class CategoryInfo {
	private int id;
    private String name;
    private int foreignID;
    private int parentID;
    private Drawable icon;
    private int status;
    
    public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getForeignID() {
		return foreignID;
	}
	public void setForeignID(int foreignID) {
		this.foreignID = foreignID;
	}
	public int getParentID() {
		return parentID;
	}
	public void setParentID(int parentID) {
		this.parentID = parentID;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
    
}
