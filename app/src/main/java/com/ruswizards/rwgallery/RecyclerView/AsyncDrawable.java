package com.ruswizards.rwgallery.RecyclerView;

import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
public class AsyncDrawable extends BitmapDrawable {
	private WeakReference<ImageLoadAsyncTask> asyncTaskWeakReference_;

	public AsyncDrawable(Resources resources, String path, ImageLoadAsyncTask asyncTask){
		super(resources, path);
		asyncTaskWeakReference_ = new WeakReference<ImageLoadAsyncTask>(asyncTask);
	}

	public ImageLoadAsyncTask getAsyncTask() {
		return asyncTaskWeakReference_.get();
	}
}
