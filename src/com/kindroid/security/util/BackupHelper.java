package com.kindroid.security.util;

import java.io.File;
import android.os.Environment;

public class BackupHelper {
	public static boolean checkExternalStorageState(){
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state))
	    	return true;
	    else
	    	return false;
	}
	public static String getDraftMessageAddress(long paramLong){    	
    	return "Test Backup";
		
    }
	
	public static String getLatestFile(String backupRootPath)
	{
		String ret = null;
		long l = 0L;
	    try
	    {
	    	String path = backupRootPath;
	      
		    String[] files = new File(path).list();
		    if(files == null || files.length <= 0)
		        return null;
		    for(int i = 0; i < files.length; i++){
		        if(!(files[i].contains("_")) || (files[i].contains("part")))
		    	    continue;
			    String[] fileNames = files[i].split("_");
			    Long localLong = Long.valueOf(fileNames[fileNames.length - 1]);
			    if(localLong.longValue() <= l)
			      continue;
			    l = localLong.longValue();
		        ret = files[i];
		    }	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    return ret;
	}	
	
	public static String getLatestFile(String backupRootPath, String paramString)
	{
		String ret = null;
		long l = 0L;
	    try
	    {
	    	String path = backupRootPath + paramString + "/";
	      
		    String[] files = new File(path).list();
		    if(files == null || files.length <= 0)
		        return null;
		    for(int i = 0; i < files.length; i++){
		        if(!(files[i].contains("_")) || (files[i].contains("part")))
		    	    continue;
			    String[] fileNames = files[i].split("_");
			    Long localLong = Long.valueOf(fileNames[fileNames.length - 1]);
			    if(localLong.longValue() <= l)
			      continue;
			    l = localLong.longValue();
		        ret = files[i];
		    }	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    return ret;
	}	

}
