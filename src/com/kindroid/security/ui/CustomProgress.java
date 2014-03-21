/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ClipDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kindroid.security.R;

public class CustomProgress extends RelativeLayout {

	private ImageView progressDrawableImageView;
	private ImageView trackDrawableImageView;
	private int max = 100;

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public CustomProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context, attrs);
	}

	protected void setup(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.CustomProgress);

		final String xmlns = "http://schemas.android.com/apk/res/net.kindroid.security";
		int bgResource = attrs.getAttributeResourceValue(xmlns,"progressDrawable", 0);
		progressDrawableImageView = new ImageView(context);
		progressDrawableImageView.setBackgroundResource(bgResource);

		int trackResource = attrs.getAttributeResourceValue(xmlns, "track", 0);
		trackDrawableImageView = new ImageView(context);
		trackDrawableImageView.setBackgroundResource(trackResource);

		int progress = attrs.getAttributeIntValue(xmlns, "progress", 0);
		setProgress(progress);
		int max = attrs.getAttributeIntValue(xmlns, "max", 100);
		setMax(max);

		a.recycle();
	}

	public void setProgress(Integer value) {
		ClipDrawable drawable = (ClipDrawable) progressDrawableImageView.getBackground();
		double percent = (double) value / (double) max;
		int level = (int) Math.floor(percent * 10000);
		drawable.setLevel(level);
	}

}
