/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.kindroid.security.AVEngine;
import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;

public class ApkManager {
	private static final String LOG_TAG = "KindroidSecurity";
    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F' };
    
    private static boolean mCancelLoadApk = false;
    /*
    public static void cancelLoadApk(){
    	mCancelLoadApk = true;
    }
    public static void setCancel(boolean mCancel){
    	mCancelLoadApk = mCancel;
    }
    */
    
	public static boolean RootCommand(String command)
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
            process.waitFor();
        } 
        catch (Exception e) {
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
        
        return true;
    }
	public static String toHexString(byte[] b) {
	    //String to  byte
	    StringBuilder sb = new StringBuilder(b.length * 2);  
	    for (int i = 0; i < b.length; i++) {  
	        sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);  
	        sb.append(HEX_DIGITS[b[i] & 0x0f]);  
	    }  
	    return sb.toString();  
	}
	
	public static String md5(byte[] content) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(content);
	        byte messageDigest[] = digest.digest();
	                                
	        return toHexString(messageDigest);
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	                        
	    return "";
	}
	static public void getInstalledPackage(Context context) {
		PackageManager pckMan = context.getPackageManager();
		
		String issuerDN = null;
		String subjectDN = null;
		String publickey = null;
		boolean isSystemApp = false;
		
		AVEngine.avEngineInit();
		List<PackageInfo> packs = pckMan.getInstalledPackages(PackageManager.GET_SIGNATURES);
		
		int count = packs.size();
		for (int i = 0; i < count; i++) { 
			isSystemApp = false;
		    PackageInfo pi = packs.get(i); 
			if (pi.versionName == null) 
				continue; 
			
			if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 || (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
				isSystemApp = true;
			}
			
			if (!isSystemApp) {
				String apkname = pi.applicationInfo.sourceDir;
				String description = new String();

			}

		}
	}
	    
	public static ArrayList<PackageInfo> getInstalledPackages(Context context, boolean containSystem) {
		ArrayList<PackageInfo> localArrayList = new ArrayList<PackageInfo>();
		List localList = context.getPackageManager().getInstalledPackages(0);
		for (int i = 0; i < localList.size(); i++) {
			PackageInfo packageinfo = (PackageInfo) localList.get(i);
			
			ApplicationInfo applicationinfo = packageinfo.applicationInfo;
			if (containSystem
					|| !applicationinfo.sourceDir.contains("/system/app")
					&& !applicationinfo.sourceDir.contains("/system/framework")) {
				String as[] = packageinfo.applicationInfo.sourceDir.split("/");
				StringBuilder stringbuilder = new StringBuilder("/data/app/");
				stringbuilder.append(as[as.length - 1]);
				File packageFile = new File(stringbuilder.toString());
				if (packageFile.exists())
					localArrayList.add(packageinfo);
			}
		}
		return localArrayList;

	}
	static List<String> getNotinstalledPackages(Context context, Handler handler){
		List<String> apkFiles = new ArrayList<String>();
		
		File sdPath = new File("/mnt/sdcard/");
		if(!sdPath.exists()){
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				sdPath = Environment.getExternalStorageDirectory();
				
			}
		}
		if(sdPath.exists()){
			getApkFilesFromPath(sdPath.getAbsolutePath(), apkFiles,handler);
		}
		
		return apkFiles;
	}
	
	private static void getApkFilesFromPath(String path, List<String> fileList, Handler handler){	
		if(mCancelLoadApk){
			return;
		}
		File dir = new File(path);
		File[] files = dir.listFiles(new FileFilter(){

			@Override
			public boolean accept(File f) {
				// TODO Auto-generated method stub
				if(f.isDirectory() || f.getName().endsWith(".apk") || f.getName().endsWith(".APK"))
					return true;
				return false;
			}
			
		});
		if(files == null)
			return;
		if(mCancelLoadApk){
			return;
		}
		for(int i = 0; i < files.length; i++){
			File f = files[i];
			if(mCancelLoadApk){				
				break;
			}
			if(f.isDirectory()){
				getApkFilesFromPath(f.getAbsolutePath(), fileList,  handler);
				
			}else{
				if(f.getName().endsWith(".apk") || f.getName().endsWith(".APK")){
					fileList.add(f.getAbsolutePath());
				}
			}
			
		}
	}
	
	static List<Map<String, String>> getApkinfosForInstall(String url){
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		
		return list;
	}
	
	static void installApkFromLocal(Context context, String apkPath){
		installApk(context, Uri.fromFile(new File(apkPath)));
	}
	
	static void installApkFromRemote(Context context, String url){
		installApk(context, Uri.parse(url));
	}
	
	public static void uninstallPackage(Context context, Uri packageURI){
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);   
		context.startActivity(uninstallIntent);
	}
		
	public static void installApk(Context context, Uri apkUri){
		Intent intent = new Intent(Intent.ACTION_VIEW); 
		intent.setDataAndType(apkUri, "application/vnd.android.package-archive"); 
		context.startActivity(intent);
	}
	public static AppInfoForManage getAppInfoFromApk(Context ctx, String apkPath){
		AppInfoForManage aifm = null;
		File apkFile = new File(apkPath);  
        if (!apkFile.exists() || (!apkPath.toLowerCase().endsWith(".apk") && !apkPath.toLowerCase().endsWith(".APK"))) {  
            
            return null;  
        }  
        String PATH_PackageParser = "android.content.pm.PackageParser";  
        String PATH_AssetManager = "android.content.res.AssetManager";  
        try {  
            Class<?> pkgParserCls = Class.forName(PATH_PackageParser);  
            Class<?>[] typeArgs = {String.class};  
            Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);  
            Object[] valueArgs = {apkPath};  
            Object pkgParser = pkgParserCt.newInstance(valueArgs);  
            
            DisplayMetrics metrics = new DisplayMetrics();  
            metrics.setToDefaults();  
            typeArgs = new Class<?>[]{File.class,String.class,  
                                    DisplayMetrics.class,int.class};  
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(  
                    "parsePackage", typeArgs);  
              
            valueArgs=new Object[]{new File(apkPath),apkPath,metrics,0};  
           
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,  
                    valueArgs);  
                 
            if (pkgParserPkg==null) {  
                return null;  
            }  
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(  
                    "applicationInfo");  
          
            if (appInfoFld.get(pkgParserPkg)==null) {  
                return null;  
            }  
            ApplicationInfo info = (ApplicationInfo) appInfoFld  
                    .get(pkgParserPkg);           
           
            Class<?> assetMagCls = Class.forName(PATH_AssetManager);            
            Object assetMag = assetMagCls.newInstance();  
            
            typeArgs = new Class[1];  
            typeArgs[0] = String.class;  
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(  
                    "addAssetPath", typeArgs);  
            valueArgs = new Object[1];  
            valueArgs[0] = apkPath;  
           
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);  
             
            Resources res = ctx.getResources();  
            typeArgs = new Class[3];  
            typeArgs[0] = assetMag.getClass();  
            typeArgs[1] = res.getDisplayMetrics().getClass();  
            typeArgs[2] = res.getConfiguration().getClass();  
            Constructor<Resources> resCt = Resources.class  
                    .getConstructor(typeArgs);  
            valueArgs = new Object[3];  
            valueArgs[0] = assetMag;  
            valueArgs[1] = res.getDisplayMetrics();  
            valueArgs[2] = res.getConfiguration();  
            res = (Resources) resCt.newInstance(valueArgs);               
                     
            if (info!=null) {  
            	aifm = new AppInfoForManage();
            	aifm.setPackagePath(apkPath);
                if (info.icon != 0) {
                    Drawable icon = res.getDrawable(info.icon);  
                    aifm.setIcon(icon);  
                }  
                if (info.labelRes != 0) {  
                    String name = (String) res.getText(info.labelRes);
                    aifm.setLabel(name); 
                }else {  
                    String apkName=apkFile.getName();  
                    aifm.setLabel(apkName.substring(0,apkName.lastIndexOf(".")));  
                }  
                aifm.setSize(apkFile.length());
            }else {  
                return null;  
            }             
            PackageManager pm = ctx.getPackageManager();  
            PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNATURES);
            aifm.setPackageName(packageInfo.packageName);
            if (packageInfo != null) {  
            	if(packageInfo.versionName != null){
            		aifm.setVersion(packageInfo.versionName.concat(ctx.getString(R.string.softmanage_version_title)));//ç‰ˆæœ¬å�·  
            	}else{
            		
            	}
                
                try{
					PackageInfo pi = pm.getPackageInfo(packageInfo.packageName, PackageManager.GET_SIGNATURES);
					if(pi != null) {						
						aifm.setInstalled(true);						
					}
				}catch(NameNotFoundException e){
					aifm.setInstalled(false);
				}
            }               
        } catch (Exception e) {   
            e.printStackTrace(); 
            return null;
        }  
		return aifm;
	}
}
