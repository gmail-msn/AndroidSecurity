/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.model;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModel implements IModel {

	private List<IModelListener> mListeners;

	private boolean mIsDirty = false;

	private boolean mIsNew = false;

	private boolean mIsDeleted = false;

	public void addModelListener(IModelListener listener) {
		if (mListeners == null) {
			mListeners = new ArrayList<IModelListener>();
		}
		mListeners.add(listener);
	}

	public void removeModelListener(IModelListener listener) {
		if (mListeners != null) {
			mListeners.remove(listener);
		}
	}

	protected void fireModelLoaded() {
		if (mListeners != null) {
			for (IModelListener listener : mListeners) {
				listener.modelLoaded(this);
			}
		}
	}

	protected void fireModelChanged() {
		if (mListeners != null) {
			for (IModelListener listener : mListeners) {
				listener.modelChanged(this);
			}
		}
	}

	protected void fireModelChanged(IModel object) {
		if (mListeners != null) {
			for (IModelListener listener : mListeners) {
				listener.modelChanged(object);
			}
		}
	}

	public boolean isDirty() {
		return mIsDirty;
	}

	protected void setDirty(boolean isDirty) {
		mIsDirty = isDirty;
	}

	public boolean isNew() {
		return mIsNew;
	}

	protected void setNew(boolean isNew) {
		mIsNew = isNew;
	}

	public boolean isDeleted() {
		return mIsDeleted;
	}

	protected void setDeleted(boolean isDeleted) {
		mIsDeleted = isDeleted;
	}

}
