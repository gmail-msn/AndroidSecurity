/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class FileDownloadThread extends Thread {
	private String url;
	private int tid;
	private long startPosition;
	private long endPosition;
	private long curPosition;
	private boolean finished = false;
	private long downloadSize = 0;
	private File tempFile;
	private boolean isStop = false;

	public FileDownloadThread(String url, String fileDir, String fileName,
			int tid, long startPos, long endPos) {
		this.url = url;
		this.tid = tid;
		this.startPosition = startPos;
		this.curPosition = startPosition;
		this.endPosition = endPos;
		this.tempFile = new File(fileDir, fileName);
		try {
			tempFile = new File(fileDir + fileName + "_" + tid);
			if (tempFile.exists()) {
				tempFile.delete();
			}

			tempFile.createNewFile();

			this.curPosition = startPosition + tempFile.length();
			downloadSize = tempFile.length();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	}

	public void run() {		
		this.finished = false;
		HttpURLConnection con = null;
		BufferedOutputStream fos = null;
		InputStream bis = null;
		int retry = 0;
		while (!isStop && !finished && retry < 10) {
			retry++;
			try {
				URL urldownload = new URL(url);
				con = (HttpURLConnection) urldownload.openConnection();

				fos = new BufferedOutputStream(new FileOutputStream(
						tempFile.getPath(), true));
				bis = con.getInputStream();

				int len = 0;
				long count = 0;
				byte[] buf = new byte[1024];
				while ((curPosition < endPosition) && !isStop) {

					long readsize = endPosition - curPosition;
					if (readsize <= 1024) {
						len = bis.read(buf, 0, (int) readsize);
					} else {
						len = bis.read(buf, 0, 1024);
					}
					if (len == -1 && (curPosition >= endPosition)) {
						break;
					} else if (len == -1) {
						int rt = 10;
						int a = 0;

						while ((len == -1) && (a < rt)) {
							try {
								bis.close();
								urldownload = null;
								urldownload = new URL(url);
								con = (HttpURLConnection) urldownload
										.openConnection();
								con.setAllowUserInteraction(true);

								con.setRequestProperty("Range", "bytes="
										+ curPosition + "-" + endPosition);
								con.setAllowUserInteraction(true);
								con.setConnectTimeout(10000);
								con.setReadTimeout(10000);
								bis = new BufferedInputStream(
										con.getInputStream());
								len = bis.read(buf, 0, 1024);
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								if (bis != null) {
									try {
										bis.close();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								con = null;
							}
							a++;
						}
					}
					if (len > 0) {
						fos.write(buf, 0, len);
						count += len;
						curPosition = curPosition + len;
						if (curPosition > endPosition) {
							downloadSize += len - (curPosition - endPosition)
									+ 1;
						} else {
							downloadSize += len;
						}
					}
					if (count >= 4096) {
						fos.flush();
					}

				}

				this.finished = true;
				fos.flush();
				fos.close();
				bis.close();
				
				break;
			} catch (IOException e) {
				
				e.printStackTrace();
				
				try {
					sleep(200);
				} catch (InterruptedException ie) {

				}
				continue;
			} catch (Exception e) {
				
				e.printStackTrace();
				
				try {
					sleep(200);
				} catch (InterruptedException ie) {

				}
				continue;
			} finally {
				
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception ce) {

					}
				}
				if (bis != null) {
					try {
						bis.close();
					} catch (Exception e) {

					}
				}

			}

		}

	}

	public void stopDownload() {
		this.isStop = true;
	}

	public boolean isFinished() {
		return finished;
	}

	public long getDownloadSize() {

		return tempFile.length();
	}

}
