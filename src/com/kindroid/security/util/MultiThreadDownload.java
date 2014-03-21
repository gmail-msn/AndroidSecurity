/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util; 

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.kindroid.security.model.TaskInfo;

public class MultiThreadDownload extends Thread {	
	private int blockSize;	
	private int threadNum = 5;	
	private int fileSize;	
	private int downloadedSize;	
	private String urlStr, fileName;	
	private String savePath;	
	private int downloadPercent = 0;
	private DownloadInvoker invoker;
	private boolean isStop = false;
	public static Map<MultiThreadDownload, Integer> percentSum = new HashMap<MultiThreadDownload, Integer>();

	public MultiThreadDownload(TaskInfo taskInfo, 
				DownloadInvoker invoker){
		percentSum.put(this, 0);
		this.urlStr = taskInfo.getUrlStr();	
		this.savePath = taskInfo.getSavePath();		
		this.fileName = taskInfo.getFileName();
		this.threadNum = taskInfo.getThreadNum();
		this.invoker = invoker;
		File path = new File(savePath);
		if(!path.exists())
			path.mkdirs();
	}
	 
	
	@Override	
	public void run() {		
		FileDownloadThread[] fds = new FileDownloadThread[threadNum];
		
		try {	
			URL url = new URL(urlStr);
			
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Accept", "*/*"); 
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setConnectTimeout(10000);   
            conn.setReadTimeout(10000); 
            conn.setAllowUserInteraction(true);
           
            fileSize = conn.getContentLength();
            
			if(fileSize <= 0){
				
				throw new Exception("Network error!");
			}
			File f = new File(savePath + "/" + fileName);
			
			if(f.exists()){
				f.delete();
			}
			
			blockSize = fileSize / threadNum;
			for (int i = 0; i < threadNum; i++) {
				FileDownloadThread fdt = new FileDownloadThread(urlStr, savePath, fileName, i,
						i * blockSize, (i + 1) != threadNum ? ((i + 1) * blockSize - 1) : fileSize);			
				fdt.setName("Thread--" + i);	
				
				fdt.start();				
				fds[i] = fdt;			
			}
			
			boolean finished = false;

			while (!isStop && !finished) {									
				finished = true;
				downloadedSize = 0;

				for (int i = 0; i < fds.length; i++) {				
					downloadedSize += fds[i].getDownloadSize();					
					if (!fds[i].isFinished()) {					
						finished = false;					
					}					
				}
				
				downloadPercent = (Double.valueOf((downloadedSize * 1.0 / fileSize * 100))).intValue();
				
				if(downloadPercent < 100){
					percentSum.put(this, downloadPercent);
					invoker.updateProgress(this, downloadPercent);
				}
				sleep(1000);
			
			}
			if(isStop){
				for(int i = 0; i < fds.length; i++){
					fds[i].stopDownload();
				}
				return;
			}			
			if(downloadedSize == fileSize){
				tempFileToTargetFile();
				this.invoker.downloadCompleted(this);				
			}else if(!isStop){
				this.invoker.downloadError(this,"�������ӳ���,���Ժ����Ի���������������Ϣ! ");
			}
			percentSum.put(this, 100);
			invoker.updateProgress(this, 100);
		}catch(MalformedURLException mue){
			this.invoker.downloadError(this,"������������Ӹ�ʽ����,����������!");
			mue.printStackTrace();
		}catch (Exception e) {		
			// TODO: handle exception
			this.invoker.downloadError(this,"���ع�̳��ִ����쳣:" + e.getMessage());
			e.printStackTrace();
		}		
		percentSum.put(this, 100);
		invoker.updateProgress(this, 100);
		this.invoker.downloadCompleted(this);
	}
	
	public void stopDownload(){
		this.isStop = true;		
	}
	public long getFileSize(){
		return this.fileSize;
	}
	
	private void tempFileToTargetFile(){
		try {   
            BufferedOutputStream outputStream = new BufferedOutputStream(   
                    new FileOutputStream(savePath + fileName));   
      
            for (int i = 0; i < threadNum; i++) { 
            	File tempFile = new File(savePath + fileName + "_" + i);
                BufferedInputStream inputStream = new BufferedInputStream(   
                        new FileInputStream(tempFile)); 
                
                int len = 0;   
                long count = 0;   
                byte[] b = new byte[1024];   
                while ((len = inputStream.read(b)) != -1) {   
                    count += len;   
                    outputStream.write(b, 0, len);   
                    if ((count % 4096) == 0) {   
                        outputStream.flush();   
                    }    
                }   
      
                inputStream.close();
                tempFile.delete();  
                
            }       
            outputStream.flush();   
            outputStream.close();   
        } catch (FileNotFoundException e) {  
        	e.printStackTrace();   
        } catch (IOException e) {
        	e.printStackTrace();   
        } catch(Exception e){
        	e.printStackTrace();
        }
	}

}

