/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author: heli.zhao
 * Date: 2011-09
 * Description:
 */
package com.kindroid.security.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.service.PkgInstallMonitorService;


/**
 * @author heli.zhao
 *
 */
public class NewInstalledPkgWarn extends Activity implements View.OnClickListener{
	private TextView mRiskNotice;
	private TextView mVirusDesp;
	private String mInstalledPkgName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.dialog_activity);
	    this.mRiskNotice = (TextView)findViewById(R.id.risk_notice);
	    this.mVirusDesp = (TextView)findViewById(R.id.virus_desp);
	    Intent intent = getIntent();
	    String appName = intent.getStringExtra(PkgInstallMonitorService.PARCEL_NAME_APP);
	    String virusDesp = intent.getStringExtra(PkgInstallMonitorService.PARCEL_NAME_VIRUS_DESP);
	    this.mInstalledPkgName = intent.getStringExtra(PkgInstallMonitorService.PARCEL_NAME_PKG);
	    
	    Object[] arrayOfObject = new Object[1];
		arrayOfObject[0] = appName;
		this.mRiskNotice.setText(getString(R.string.install_scan_trojan, arrayOfObject));
		this.mVirusDesp.setText(virusDesp);
		View okButton = findViewById(R.id.uninstallOkBtn);
		okButton.setOnClickListener(this);
		View cancelButton = findViewById(R.id.uninstallCancelBtn);
		cancelButton.setOnClickListener(this);
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.uninstallOkBtn:
			Uri pUri = Uri.parse("package:" + this.mInstalledPkgName);
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, pUri);   
			startActivity(uninstallIntent);
			break;
		case R.id.uninstallCancelBtn:
			
			break;
		}
		finish();
	}
	
	

}
