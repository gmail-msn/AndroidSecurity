/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-11
 * Description:
 */
package com.kindroid.security.data;

import android.content.Context;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;
import android.util.Log;

import com.kindroid.security.R;
import com.kindroid.security.ui.MobileExamActivity;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author heli.zhao
 *
 */
public class CacheClearItem implements MobileExamItem {
	private MobileExamActivity mContext;
	private int mItemStatus = MobileExamItem.SAFE_ITEM;	
	private String mDangerAction;
	private String mSafeAction;
	
	public CacheClearItem(MobileExamActivity context){
		this.mContext = context;
	}
	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getSafeDesp()
	 */
	@Override
	public String getSafeDesp() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.safe_desp_for_cache_clear);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDangerDesp()
	 */
	@Override
	public String getDangerDesp() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.danger_desp_for_cache_clear);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getSafeAction()
	 */
	@Override
	public String getSafeAction() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.mobile_exam_action_complete);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDangerAction()
	 */
	@Override
	public String getDangerAction() {
		// TODO Auto-generated method stub
		return mContext.getString(R.string.mobile_exam_action_clear);
	}

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getItemStatus()
	 */
	@Override
	public int getItemStatus() {
		// TODO Auto-generated method stub
		return mItemStatus;
	}
	public void setItemStatus(int status){
		this.mItemStatus = status;
	}
	
	private static long getEnvironmentSize()  {
		File localFile = Environment.getDataDirectory();
		long l1;
		if (localFile == null)
			l1 = 0L;
		while (true) {
		    
		    String str = localFile.getPath();
		    StatFs localStatFs = new StatFs(str);
		    long l2 = localStatFs.getBlockSize();
		    l1 = localStatFs.getBlockCount() * l2;
		    return l1;
		}
    }

	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#optimizeAction()
	 */
	@Override
	public void optimizeAction() {
		// TODO Auto-generated method stub
		PackageManager pm = mContext.getPackageManager();
		
		try {
			/*Method method = pm.getClass().getMethod("deleteApplicationCacheFiles", String.class, IPackageDataObserver.class);
			method.invoke(pm, pkg, new PkgDataObserver());*/
			
			Class[] arrayOfClass = new Class[2];
			Class localClass2 = Long.TYPE;
			arrayOfClass[0] = localClass2;
			arrayOfClass[1] = IPackageDataObserver.class;
			Method localMethod = pm.getClass().getMethod("freeStorageAndNotify", arrayOfClass);
			Long localLong = Long.valueOf(getEnvironmentSize() - 1L);
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = localLong;
			localMethod.invoke(pm, localLong, new PkgDataObserver());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		setItemStatus(MobileExamItem.SAFE_ITEM);
	}
	//clear cache call back
	class PkgDataObserver extends IPackageDataObserver.Stub {
		@Override
		public void onRemoveCompleted(String packageName, boolean succeeded)
				throws RemoteException {
			
		}
	}
	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#forwardOptimizeAction()
	 */
	@Override
	public void forwardOptimizeAction() {
		// TODO Auto-generated method stub
		optimizeAction();
		mContext.updateExamList(this);
	}
	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getFlag()
	 */
	@Override
	public int getFlag() {
		// TODO Auto-generated method stub
		return MobileExamItem.FLAG_CACHE_CLEAN_ITEM;
	}
	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#setDangerAction(java.lang.String)
	 */
	@Override
	public void setDangerAction(String action) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#setSafeAction(java.lang.String)
	 */
	@Override
	public void setSafeAction(String action) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getIncScore()
	 */
	@Override
	public int getIncScore() {
		// TODO Auto-generated method stub
		return 5;
	}
	/* (non-Javadoc)
	 * @see com.kindroid.security.data.MobileExamItem#getDecScore()
	 */
	@Override
	public int getDecScore() {
		// TODO Auto-generated method stub
		return -5;
	}

}
