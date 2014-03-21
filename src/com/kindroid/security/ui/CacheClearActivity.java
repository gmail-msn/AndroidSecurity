package com.kindroid.security.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.security.R;
import com.kindroid.security.adapter.CacheClearListAdapter;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.service.OutCallReceiver;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.util.LoadingAppsThread;
import com.kindroid.security.util.PopViewAid;
import com.kindroid.security.util.UnitsConversion;

public class CacheClearActivity extends ListActivity {
	public static String TAG = "CacheClearActivity";
	public static final int LOAD_INSTALLED_CACHE = 4;
	//load progress image
	private ImageView one;
	private ImageView two;
	private ImageView three;
	private ImageView four;
	private ImageView five;

	private ImageView one_copy;
	private ImageView two_copy;
	private ImageView three_copy;
	private ImageView four_copy;
	private ImageView five_copy;
	
	private TextView cacheAppSizeTextView;
	private TextView totalCacheTextView;
	private LinearLayout noCacheLayout;
	
	private LinearLayout installed_action_linear;
	
	private CacheClearListAdapter listAdapter;
	private boolean isLoadingData = false;
	public boolean mSizeSortDesc = true;
	private boolean mNameSortDesc = true;
	private boolean isScreenOff = false;
	
	private PkgSizeObserver pkgObserver;
	private ScreenBroacastReciver screenReceiver;
	
	int count = 0; //
	int installedAppsCount = 0;
	long total_cache_size = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cache_clear);
		noCacheLayout = (LinearLayout) findViewById(R.id.no_cache_linear);
		installed_action_linear = (LinearLayout) findViewById(R.id.installed_action_linear);
		installed_action_linear.setOnClickListener(new OnLinearClick());
		
		View home_page = findViewById(R.id.home_icon);
		home_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent homepage = new Intent(CacheClearActivity.this, DefenderTabMain.class);
				homepage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homepage);
				finish();
			}
		});
		
		IntentFilter screen_it = new IntentFilter();
		screen_it.addAction(Intent.ACTION_SCREEN_OFF);
		screenReceiver = new ScreenBroacastReciver();
		registerReceiver(screenReceiver, screen_it);
	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(screenReceiver);
	}



	@Override
	protected void onResume() {
		super.onResume();
		count = 0;
		total_cache_size = 0;
		if (!isLoadingData && (listAdapter == null || listAdapter.isEmpty())) {	
			if (!isScreenOff) {
				loadListAdapter();
			} else {
				isScreenOff = false;
			}
		} else if (isLoadingData) {
			getListView().setVisibility(View.GONE);
			View loading_linear = findViewById(R.id.loading_linear);
			loading_linear.setVisibility(View.VISIBLE);
			noCacheLayout.setVisibility(View.GONE);
		} else {
			if (!isScreenOff) {
				loadListAdapter();
			} else {
				isScreenOff = false;
			}
		}
		
	}
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case LOAD_INSTALLED_CACHE:				
				if(listAdapter != null && listAdapter.getCount() > 0){
					listAdapter.sortItems(CacheClearListAdapter.SORT_BY_SIZE, mNameSortDesc);
				}
				setListAdapter(listAdapter);	
				cacheAppSizeTextView = (TextView) findViewById(R.id.installed_sum_tv);
				cacheAppSizeTextView.setText(listAdapter.getCount() + "");
				totalCacheTextView = (TextView) findViewById(R.id.cache_total_size_text_view);
				
				if (total_cache_size > 0) {
//					totalCacheTextView.setText(Html.fromHtml(new UnitsConversion().defaultConversionForHtml(total_cache_size)));
					totalCacheTextView.setText(Html.fromHtml(UnitsConversion.formatCacheSize(1, total_cache_size)));
					noCacheLayout.setVisibility(View.GONE);
					getListView().setVisibility(View.VISIBLE);
				} else {
					totalCacheTextView.setText(Html.fromHtml("<font color=\"#64BD45\"><big>0</big></font>"));
					noCacheLayout.setVisibility(View.VISIBLE);
					getListView().setVisibility(View.GONE);
				}
				
				isLoadingData = false;
				View loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				
				break;
			case 1:
				loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				noCacheLayout.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				break;
			case LoadingAppsThread.LOAD_INSTALLED_PROGRESS:
				TextView prompt_progress_text = (TextView)findViewById(R.id.prompt_progress_text);
				prompt_progress_text.setText(Double.valueOf((msg.arg1 * 1.0)/msg.arg2*100).intValue() + "%");
				break;
			case 5:
				//update refresh progress
				prompt_progress_text = (TextView)findViewById(R.id.prompt_progress_text);
				prompt_progress_text.setText(msg.arg1 + "%");
				break;
			case 6:
				//complete refresh list state
				if (listAdapter != null && listAdapter.getCount() > 0) {
					listAdapter.sortItems(1, mNameSortDesc);
				}
				setListAdapter(listAdapter);
				listAdapter.notifyDataSetChanged();
				cacheAppSizeTextView = (TextView) findViewById(R.id.installed_sum_tv);
				cacheAppSizeTextView.setText(listAdapter.getCount() + "");
				totalCacheTextView = (TextView) findViewById(R.id.cache_total_size_text_view);
				if (total_cache_size > 0) {
					totalCacheTextView.setText(Html.fromHtml(UnitsConversion.formatCacheSize(1, total_cache_size)));
					noCacheLayout.setVisibility(View.GONE);
				} else {
					totalCacheTextView.setText(Html.fromHtml("<font color=\"#64BD45\"><big>0</big></font>"));
					noCacheLayout.setVisibility(View.VISIBLE);
				}
				isLoadingData = false;
				loading_linear = findViewById(R.id.loading_linear);
				loading_linear.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				break;
			}
		}
	};
	
	class OnListItemClick implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			AppInfoForManage aifm = (AppInfoForManage) listAdapter.getItem(position);
			
			if(Build.VERSION.SDK_INT >= 9){
				Intent intent = new Intent();
				intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
				intent.setData(Uri.fromParts("package", aifm.getPackageName(), null));
				startActivityForResult(intent, position);
				return;
			}
			if(Build.VERSION.SDK_INT == 8){
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
				intent.putExtra("pkg", aifm.getPackageName());
				startActivityForResult(intent, position);
				return;
			}
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			intent.putExtra("com.android.settings.ApplicationPkgName", aifm.getPackageName());
			startActivityForResult(intent, position);
		}
		
	}
	
	class OnLinearClick implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			if (!isLoadingData) {
				
				clearCache(null);
				listAdapter.clearItems();
				refreshListAdapter();
			} else {
				Toast.makeText(CacheClearActivity.this, getResources().getString(R.string.backup_loading_tip_text), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	private Handler mProgressHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			one.setVisibility(View.VISIBLE);
			two.setVisibility(View.VISIBLE);
			three.setVisibility(View.VISIBLE);
			four.setVisibility(View.VISIBLE);
			five.setVisibility(View.VISIBLE);
			
			one_copy.setVisibility(View.INVISIBLE);
			two_copy.setVisibility(View.INVISIBLE);
			three_copy.setVisibility(View.INVISIBLE);
			four_copy.setVisibility(View.INVISIBLE);
			five_copy.setVisibility(View.INVISIBLE);

			switch (msg.what) {
			case 0:
				one.setVisibility(View.INVISIBLE);
				one_copy.setVisibility(View.VISIBLE);
				break;
			case 1:
				two.setVisibility(View.INVISIBLE);
				two_copy.setVisibility(View.VISIBLE);
				break;
			case 2:
				three.setVisibility(View.INVISIBLE);
				three_copy.setVisibility(View.VISIBLE);
				break;
			case 3:
				four.setVisibility(View.INVISIBLE);
				four_copy.setVisibility(View.VISIBLE);
				break;
			case 4:
				five.setVisibility(View.INVISIBLE);
				five_copy.setVisibility(View.VISIBLE);
				break;

			}
			
		}
	};
	
	private void loadListAdapter() {
		isLoadingData = true;
		listAdapter = new CacheClearListAdapter(this);
		getListView().setOnItemClickListener(new OnListItemClick());
		getListView().setVisibility(View.GONE);
		View loading_linear = findViewById(R.id.loading_linear);
		loading_linear.setVisibility(View.VISIBLE);
		noCacheLayout.setVisibility(View.GONE);
		
		one = (ImageView) findViewById(R.id.pr_one);
		two = (ImageView) findViewById(R.id.pr_two);
		three = (ImageView) findViewById(R.id.pr_three);
		four = (ImageView) findViewById(R.id.pr_four);
		five = (ImageView) findViewById(R.id.pr_five);

		one_copy = (ImageView) findViewById(R.id.pr_one_copy);
		two_copy = (ImageView) findViewById(R.id.pr_two_copy);
		three_copy = (ImageView) findViewById(R.id.pr_three_copy);
		four_copy = (ImageView) findViewById(R.id.pr_four_copy);
		five_copy = (ImageView) findViewById(R.id.pr_five_copy);
		
		TextView prompt_progress_text = (TextView)findViewById(R.id.prompt_progress_text);
		prompt_progress_text.setText("0%");
		prompt_progress_text.setVisibility(View.VISIBLE);
		
		//start loading progress
		new LoadingItem().start();
		
		List<PackageInfo> installedCacheAppList = getPackageManager().getInstalledPackages(0);
		//init total installed apps
		installedAppsCount = installedCacheAppList.size();
		for (PackageInfo pInfo : installedCacheAppList) {
			getPkgInfo(pInfo.packageName);
		}
		
	}
	
	private void refreshListAdapter() {
		isLoadingData = true;
		
		getListView().setVisibility(View.GONE);
		View loading_linear = findViewById(R.id.loading_linear);
		loading_linear.setVisibility(View.GONE);
		noCacheLayout.setVisibility(View.GONE);
		
		one = (ImageView) findViewById(R.id.pr_one);
		two = (ImageView) findViewById(R.id.pr_two);
		three = (ImageView) findViewById(R.id.pr_three);
		four = (ImageView) findViewById(R.id.pr_four);
		five = (ImageView) findViewById(R.id.pr_five);

		one_copy = (ImageView) findViewById(R.id.pr_one_copy);
		two_copy = (ImageView) findViewById(R.id.pr_two_copy);
		three_copy = (ImageView) findViewById(R.id.pr_three_copy);
		four_copy = (ImageView) findViewById(R.id.pr_four_copy);
		five_copy = (ImageView) findViewById(R.id.pr_five_copy);
		
		TextView prompt_progress_text = (TextView)findViewById(R.id.prompt_progress_text);
		prompt_progress_text.setText("0%");
		prompt_progress_text.setVisibility(View.VISIBLE);		
		new RefreshListAdapterThread().start();
		new LoadingItem().start();
	}
	
	private class RefreshListAdapterThread extends Thread {
		public void run() {
			refreshListState();
			handler.sendEmptyMessage(6);
		}
	}
	
	private class LoadingItem extends Thread {
		public void run() {
			do {
				for (int j = 0; j < 5; j++) {
					try {
						sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mProgressHandler.sendEmptyMessage(j);
				}
			} while (isLoadingData);

		}
	}
	
	private void refreshListState() {
		List<PackageInfo> mList = getPackageManager().getInstalledPackages(0);
		count = 0;
		total_cache_size = 0;
		installedAppsCount = mList.size();
		List<PackageInfo> mInstalledList = new ArrayList<PackageInfo>();
		List<AppInfoForManage> mDeletedList = new ArrayList<AppInfoForManage>();
		int len = listAdapter.getCount();
		for (int i = 0; i < len; i++) {
			AppInfoForManage aifm = (AppInfoForManage)listAdapter.getItem(i);
			boolean installed = false;
			for (PackageInfo pi : mList) {
				if(pi.packageName.equals(aifm.getPackageName())){
					installed = true;
					mInstalledList.add(pi);
				}
			}
			if (!installed) {
				mDeletedList.add(aifm);
			}
			Message msg = new Message();
			msg.what = 5;
			msg.arg1 = Double.valueOf((i * 1.0)/len*80).intValue();
			handler.sendMessage(msg);
		}
		
		for (int i = 0; i < mDeletedList.size(); i++) {
			AppInfoForManage aifm = mDeletedList.get(i);
			listAdapter.delteItem(aifm);
			Message msg = new Message();
			msg.what = 5;
			msg.arg1 = Double.valueOf((i * 1.0)/mDeletedList.size()*6).intValue() + 80;
			handler.sendMessage(msg);
		}
		Message msg = new Message();
		msg.what = 5;
		msg.arg1 = 86;
		handler.sendMessage(msg);
		
		for (int i = 0; i < mInstalledList.size(); i++) {
			PackageInfo pi = mInstalledList.get(i);
			mList.remove(pi);
			msg = new Message();
			msg.what = 5;
			msg.arg1 = Double.valueOf((i * 1.0)/mInstalledList.size()*6).intValue() + 86;
			handler.sendMessage(msg);
		}
		msg = new Message();
		msg.what = 5;
		msg.arg1 = 92;
		handler.sendMessage(msg);
		
		msg = new Message();
		msg.what = 5;
		msg.arg1 = 100;
		handler.sendMessage(msg);
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
	
	public void getPkgInfo(String pkg) {
		PackageManager pm = getPackageManager();
		
		try {
			if (pkgObserver == null) {
				pkgObserver = new PkgSizeObserver();
			}
			Method getPackageSizeInfo = pm.getClass().getDeclaredMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
			getPackageSizeInfo.setAccessible(true);
			getPackageSizeInfo.invoke(pm, pkg, pkgObserver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//clear cache method
	public void clearCache(String pkg) {
		PackageManager pm = getPackageManager();
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
	}
	
	//clear cache call back
	class PkgDataObserver extends IPackageDataObserver.Stub {
		@Override
		public void onRemoveCompleted(String packageName, boolean succeeded)
				throws RemoteException {
			Log.d(TAG, packageName + ":" + succeeded);
			
		}
	}

	//access cache call back
	class PkgSizeObserver extends IPackageStatsObserver.Stub {

		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
			try {
				PackageManager pm = getPackageManager();
				
				PackageInfo pi = pm.getPackageInfo(pStats.packageName, PackageManager.GET_PERMISSIONS);
				AppInfoForManage aifm = new AppInfoForManage();					
				aifm.setPackageName(pi.packageName);
				
				aifm.setLabel(pi.applicationInfo.loadLabel(pm));
				CharSequence version = pi.versionName;
				if (version != null) {
					aifm.setVersion(pi.versionName.concat(getString(R.string.softmanage_version_title)));
				}
				aifm.setIcon(pi.applicationInfo.loadIcon(pm));
				aifm.setCacheSize(pStats.cacheSize);
				
				if (pStats.cacheSize > 0) {
					total_cache_size += pStats.cacheSize;
					listAdapter.addItem(aifm);
				}
				aifm.setFlag(LOAD_INSTALLED_CACHE);
				count++;
				Message msg = new Message();
				msg.what = LoadingAppsThread.LOAD_INSTALLED_PROGRESS;
				msg.arg1 = count;
				msg.arg2 = installedAppsCount;
				handler.sendMessage(msg);
				if (count == installedAppsCount) {
					handler.sendEmptyMessage(LOAD_INSTALLED_CACHE);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class ScreenBroacastReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null)
				return;
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				isScreenOff = true;
			} 
		}
	}
}
