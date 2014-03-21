/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

public class Base64Handler {
	
	public String encode (byte[] info) {
		return new String(Base64.encodeBase64(info));
	}
	
	public byte[] decodeBuffer (String singedStr) throws IOException {
		return Base64.decodeBase64(singedStr.getBytes());
	}
	
	
}
