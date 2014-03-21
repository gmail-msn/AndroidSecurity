/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.kindroid.security.R;
import com.kindroid.security.adapter.GridListAdapter;
import com.kindroid.security.adapter.ViewFlowAdapter;
import com.kindroid.security.util.KindroidSecurityApplication;
import com.kindroid.security.widget.CircleFlowIndicator;
import com.kindroid.security.widget.ViewFlow;

public class UseFucActivity extends Activity implements OnItemClickListener {
	GridView gridView;
	int id[] = new int[] {R.drawable.fuc_02,  R.drawable.fuc_06, R.drawable.fuc_01,
			R.drawable.fuc_05,R.drawable.fuc_04,  R.drawable.power,R.drawable.fuc_10,
			R.drawable.fuc_07, R.drawable.func_cache_clear, R.drawable.func_optimize,
			R.drawable.mobile_exam, R.drawable.fuc_03};
	String text[];
	private ViewFlowAdapter mAdapter;
	private ViewFlow viewFlow;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.use_fuc);
		text = getResources().getStringArray(R.array.fuc_list_string);
		mAdapter = new ViewFlowAdapter(this, text, id);
		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
		viewFlow.setAdapter(mAdapter, 0);
		CircleFlowIndicator indic = (com.kindroid.security.widget.CircleFlowIndicator) findViewById(R.id.viewflowindic);
		viewFlow.setFlowIndicator(indic);
		/*
		gridView = (GridView) findViewById(R.id.gridView);
		gridView.setAdapter((new GridListAdapter(this, text, id)));
		gridView.setOnItemClickListener(this);
		gridView.setSelector(R.drawable.use_func_linear);
		*/
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		viewFlow.onConfigurationChanged(newConfig);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch (arg2) {
		case 0:
			startActivity(new Intent(UseFucActivity.this,
					NetTrafficTabMain.class));
		
			break;
		case 1:
			startActivity(new Intent(UseFucActivity.this,
					BlockTabMain.class));
			
			
			break;
		case 2:
			startActivity(new Intent(UseFucActivity.this,
					VirusScanTabActivity.class));
			
			break;
		case 3:

			startActivity(new Intent(UseFucActivity.this,
					SoftManageTabActivity.class));

			break;
		case 4:
			startActivity(new Intent(UseFucActivity.this,
					TaskManageTabActivity.class));
			

			break;

		case 5:
			
			try {
				Intent i = new Intent();
				ComponentName com = new ComponentName("com.android.settings",
						"com.android.settings.fuelgauge.PowerUsageSummary");
				i.setComponent(com);
				startActivity(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			break;
		case 6:
			startActivity(new Intent(UseFucActivity.this,
					BackupTabActivity.class));

			break;
		case 7:
			startActivity(new Intent(UseFucActivity.this,
					RemoteSecurityTabActivity.class));
			break;
		case 8:
			startActivity(new Intent(UseFucActivity.this, CacheClearActivity.class));
			break;
		case 9:
			startActivity(new Intent(this, StartManageActivity.class));
			break;
		case 10:
			startActivity(new Intent(this, MobileExamActivity.class));
			break;
		case 11:
			startActivity(new Intent(this, FirewallActivity.class));
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (DefenderTabMain.backTimes < 1) {
				Toast.makeText(this, R.string.quit_app_tips, Toast.LENGTH_SHORT)
						.show();
				DefenderTabMain.backTimes = DefenderTabMain.backTimes + 1;
			} else {
				KindroidSecurityApplication app=(KindroidSecurityApplication) getApplication();
				app.setAppIsActive(false);
				DefenderTabMain.backTimes = 0;
				finish();
			}
			return true;
		}
		return false;
	}

}
