package com.kindroid.security.util;

import java.lang.reflect.Method;

import android.telephony.TelephonyManager;

public class PhoneUtils {    
    /**  
     * 从TelephonyManager中实例化ITelephony，并返回  
     */    
    static public com.android.internal.telephony.ITelephony getITelephony(TelephonyManager telMgr) throws Exception {    
        Method getITelephonyMethod = telMgr.getClass().getDeclaredMethod("getITelephony");    
        getITelephonyMethod.setAccessible(true);//私有化函数也能使用    
        return (com.android.internal.telephony.ITelephony)getITelephonyMethod.invoke(telMgr);    
    }    
        
//    static public void printAllInform(Class clsShow) {      
//        try {      
//            // 取得所有方法      
//            Method[] hideMethod = clsShow.getDeclaredMethods();      
//            int i = 0;      
//            for (; i < hideMethod.length; i++) {      
//                Log.e("method name", hideMethod[i].getName());      
//            }      
//            // 取得所有常量      
//            Field[] allFields = clsShow.getFields();      
//            for (i = 0; i < allFields.length; i++) {      
//                Log.e("Field name", allFields[i].getName());      
//            }      
//        } catch (SecurityException e) {      
//            // throw new RuntimeException(e.getMessage());      
//            e.printStackTrace();      
//        } catch (IllegalArgumentException e) {      
//            // throw new RuntimeException(e.getMessage());      
//            e.printStackTrace();      
//        } catch (Exception e) {      
//            // TODO Auto-generated catch block      
//            e.printStackTrace();      
//        }      
//    }      
}    