/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.data;

import android.app.Activity;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import com.kindroid.security.util.Appstore;
import com.kindroid.security.util.Appstore.App;
import com.kindroid.security.util.Appstore.Request;
import com.kindroid.security.util.Appstore.RequestContext;
import com.kindroid.security.util.Appstore.Response;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.ConvertUtils;
import com.kindroid.security.util.HttpRequest;

public class SearchAppData extends BaseData {
	
	public SearchAppData(Activity context) {
		setContext(context);
	}

	public List<App> getTopResult(int startIndex,int orderType){
		return getSearchResult(null, startIndex, orderType);
	}
	
	public List<App> getSearchResult(String key,int startIndex){
		return getSearchResult(key, startIndex, 6);
	}
	
	public List<App> getSearchResult(String key,int startIndex,int orderType){
		RequestContext.Builder context = RequestContext.newBuilder();
		Request.Builder request = Request.newBuilder();
		context.setMarketVersion("001");
		request.setContext(context.build());
		Appstore.AppsRequest.Builder appsRequ = Appstore.AppsRequest.newBuilder();
		if (key != null) {
			appsRequ.setQuery(key);
			appsRequ.setOrderType(2);
		} else {
			appsRequ.setOrderType(orderType);
		}
		appsRequ.setWithExtendedInfo(true);
		appsRequ.setStartIndex(startIndex);
		appsRequ.setEntriesCount(10);

		request.setAppsRequest(appsRequ.build());
		try {
			InputStream in=HttpRequest.postData(Constant.SEARCH_URL, request);
			if(in!=null){
				byte[] bytes = ConvertUtils.InputStreamToByte(in);
				byte[] bytesDecoded = Base64.decodeBase64(bytes);
				Response resp = Response.parseFrom(bytesDecoded);
				List<App> list=resp.getAppsResponse().getAppList();
			 	
				return list;
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
