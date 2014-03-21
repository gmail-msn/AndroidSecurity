/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

public interface DownloadInvoker {
	
	public void updateProgress(MultiThreadDownload mtd, int downloadPercent);
	
	public void downloadError(MultiThreadDownload mtd, String message);
	
	public void downloadCompleted(MultiThreadDownload mtd);
}
