/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

import java.io.IOException;

import com.kindroid.security.R;
import com.kindroid.security.model.NetTrafficModel;
import com.kindroid.security.util.HandlerContainer;
import com.kindroid.security.util.KindroidSecurityApplication;

/**
 * {@link NetTrafficPreferences} is the preference screen for NetCounter.
 */
public class NetTrafficPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Initializes the preference activity.
		addPreferencesFromResource(R.xml.preferences);

		// Export.
		Preference pref = findPreference("export");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				exportData();
				return true;
			}
		});

		// Import.
		pref = findPreference("import");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// Creates the dialog.
				AlertDialog.Builder d = new AlertDialog.Builder(NetTrafficPreferences.this);
				d.setTitle(R.string.importTitle);
				d.setMessage(R.string.importAlertText);
				d.setNegativeButton(R.string.no, null);
				d.setPositiveButton(R.string.yes, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						importData();
					}
				});
				d.show();
				return true;
			}
		});
	}

	/**
	 * Exports the data in a separate thread.
	 */
	private void exportData() {
		final ProgressDialog pd = ProgressDialog.show(NetTrafficPreferences.this,
				getString(R.string.exportDialogTitle), getString(R.string.exportDialogText), true);
		final KindroidSecurityApplication app = (KindroidSecurityApplication) getApplication();
		final NetTrafficModel m = app.getAdapter(NetTrafficModel.class);
		HandlerContainer hdlr = app.getAdapter(HandlerContainer.class);
		hdlr.getSlowHandler().post(new Runnable() {
			public void run() {
				try {
					String f = m.exportDataToCsv();
					app.toast(getString(R.string.exportSuccessful, f));
				} catch (IOException e) {
					app.toast(R.string.exportFailed);
				} finally {
					pd.dismiss();
				}
			}
		});
	}

	/**
	 * Imports the data in a separate thread.
	 */
	private void importData() {
		final ProgressDialog pd = ProgressDialog.show(NetTrafficPreferences.this,
				getString(R.string.importDialogTitle), getString(R.string.importDialogText), true);
		final KindroidSecurityApplication app = (KindroidSecurityApplication) getApplication();
		final NetTrafficModel m = app.getAdapter(NetTrafficModel.class);
		HandlerContainer hdlr = app.getAdapter(HandlerContainer.class);
		hdlr.getSlowHandler().post(new Runnable() {
			public void run() {
				try {
					m.importDataFromCsv();
					app.toast(R.string.importSuccessful);
				} catch (IOException e) {
					app.toast(R.string.importFailed);
				} finally {
					pd.dismiss();
				}
			}
		});
	}

}
