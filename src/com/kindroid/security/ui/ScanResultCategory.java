/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.content.Context;

import java.util.List;

import com.kindroid.security.R;

public class ScanResultCategory {
	private static final int RANK_NONRISK = 0;
	private static final int RANK_WARNING = 1;
	private static final int RANK_DANGER = 2;
	private static final int RANK_MALWARE = 3;	
	
	private int mRank = RANK_NONRISK;
	private Context mContext;
	private List<ScanResultInfo> mListResult;

	public ScanResultCategory(Context context, int rank, List<ScanResultInfo> listApps)
	{
		mContext = context;
		mListResult = listApps;
		mRank = rank;
	}	
	
	public List<ScanResultInfo> getResultList() {
		return mListResult;
	}

	public int getRank() {
		return mRank;
	}
	
	public String getLabel() {
		String label = "";
		switch (mRank) {
		case 0:
			label = mContext.getString(R.string.scan_norisk);
			break;
		case 1:
			label = mContext.getString(R.string.scan_warn);
			break;
		case 2:
			label = mContext.getString(R.string.scan_risk);
			break;
		case 3:
			label = mContext.getString(R.string.scan_danger);
			break;
		default:
				break;
		}
		
		return label;
	}
}
