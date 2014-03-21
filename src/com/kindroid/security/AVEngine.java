/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: longjun.wang
 * Date: 2011-07
 * Description:
 */

package com.kindroid.security;

import android.database.SQLException;

import com.kindroid.security.util.SQLiteHelper;

import java.io.IOException;

public class AVEngine {
	
	public AVEngine() {
		
	}
	
	public static String mErrorMsg;
    public static native boolean testJNIMethod(String file);
    public static native boolean avEngineInit();
    public static native void avEngineClose();
    public static native int avEngineCheck(String file, boolean isCheckSignature, VirusInfo info, ApkSignatureInfo signatureInfo);
    public static native boolean apkSignatureCheck(String file, ApkSignatureInfo info);
    
    static {
        System.loadLibrary("avengine");
        avEngineInit();        
    }    
}
