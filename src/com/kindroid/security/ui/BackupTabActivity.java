/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import com.kindroid.security.R;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;

public class BackupTabActivity extends TabActivity implements
View.OnClickListener {
	
	public static TabHost mTabHost;
	private LinearLayout backup_tab_linear;
	private LinearLayout backlog_tab_linear;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backup_tab);
		
        mTabHost = getTabHost();
        
        TabHost.TabSpec backupSpec = mTabHost.newTabSpec("backup_tab");
        backupSpec.setIndicator(getString(R.string.title_for_backup_restore));
        Intent intentBack = new Intent(this, BackupRestoreActivity.class);
        backupSpec.setContent(intentBack);
        mTabHost.addTab(backupSpec);

        TabHost.TabSpec backlogSpec = mTabHost.newTabSpec("backlog_tab");
        backlogSpec.setIndicator(getString(R.string.backup_log_tab));
        Intent intentRestore = new Intent(this, BacklogActivity.class);
        backlogSpec.setContent(intentRestore);
        mTabHost.addTab(backlogSpec);
        
        backup_tab_linear = (LinearLayout)findViewById(R.id.backup_tab_linear);
        backlog_tab_linear = (LinearLayout)findViewById(R.id.backlog_tab_linear);        
        backup_tab_linear.setOnClickListener(this);
        backlog_tab_linear.setOnClickListener(this);
        
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.backup_tab_linear:				
				backup_tab_linear.setBackgroundResource(R.drawable.linear_focuse);
				backlog_tab_linear.setBackgroundResource(R.drawable.linear_unfocuse);
				mTabHost.setCurrentTab(0);
				break;

			case R.id.backlog_tab_linear:				
				backup_tab_linear.setBackgroundResource(R.drawable.linear_unfocuse);
				backlog_tab_linear.setBackgroundResource(R.drawable.linear_focuse);
				mTabHost.setCurrentTab(1);
				
				break;
			
		}
	}

}
