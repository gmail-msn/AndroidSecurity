/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.os.Handler;

public class HandlerContainer {

	private final Handler mGuiHandler;

	private final Handler mSlowHandler;

	public HandlerContainer(Handler gui, Handler slow) {
		mGuiHandler = gui;
		mSlowHandler = slow;
	}

	public Handler getGuiHandler() {
		return mGuiHandler;
	}

	public Handler getSlowHandler() {
		return mSlowHandler;
	}

}
