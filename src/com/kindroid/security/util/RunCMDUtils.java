/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author heli.zhao
 *
 */
public class RunCMDUtils {
	public static boolean rootCommand(String command)
    {		
		Process process = null;
        DataOutputStream os = null;
        
        try
        {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            int exitcode = process.waitFor();
            
            InputStream is = process.getErrorStream();
            
            byte[] buffer = new byte[1024];
            int len = is.read(buffer);
            if(exitcode != 0){
            	Log.d("KindroidSecurity", "exitcode :" + exitcode);
            	return false;
            }
            if(len > 0){
            	String ret = new String(buffer);
            	Log.d("KindroidSecurity", "exitcode :" + exitcode + "; " + ret);
            	if(ret.contains("Permission denied") || ret.contains("permission denied") || ret.contains("not allowed to su")){
            		return false;
            	}
            }
        } 
        catch (Exception e) {
            Log.d("DEBUG", "ROOT ERROR " + e.getMessage());
            return false;
        } 
        finally {
            try {
                if (os != null)
                {
                    os.close();
                }
                
                process.destroy();
            } 
            catch (Exception e) {
            }
        }
        
        Log.d("DEBUG", "Root SUCCEEDED");
        return true;
        /*
		ProcessBuilder pb = new ProcessBuilder("su", command + "\nexit\n");
//		pb.redirectErrorStream(false);
		Process process = null;
		InputStream is = null;
		try{
			process = pb.start();
			is = process.getErrorStream();
			byte[] buffer = new byte[512];
            int len = is.read(buffer);
            Log.d("KindroidSecurity", "=========================================================");
            if(len > 0){
            	String ret = new String(buffer);
            	Log.d("KindroidSecurity", ret);
            	if(ret.contains("Permission denied") || ret.contains("permission denied")){
            		return false;
            	}
            }
            Log.d("KindroidSecurity", "=========================================================");
		}catch(Exception e){
			Log.d("KindroidSecurity", e.getMessage());
			return false;
		}finally{
			if(process != null){
				process.destroy();
			}
		}
		return true;
		*/
    }
	
	public static boolean isRooted(Context mContext){
		String apkRoot = "chmod 777 " + mContext.getPackageCodePath();
		return rootCommand(apkRoot);
	}
	
	public static boolean phoneRooted(Context mContext){
		boolean ret = false;
		String[] files = new String[]{"/system/bin/su","/system/xbin/su","/system/sbin/su"};
		for(int i = 0; i < files.length; i++){
			File f = new File(files[i]);
			if(f.exists()){
				ret = true;
				break;
			}
		}
		return ret;
	}
}
