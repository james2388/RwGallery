package com.ruswizards.rwgallery.RecyclerView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
public class PreviewImageView extends ImageView {

	private WeakReference<ImageLoadAsyncTask> imageLoadAsyncTask_;

	public PreviewImageView(Context context) {
		super(context);
	}

	public PreviewImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PreviewImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public ImageLoadAsyncTask getImageLoadAsyncTask() {
		return imageLoadAsyncTask_.get();
	}

	public synchronized void setImageLoadAsyncTask(ImageLoadAsyncTask imageLoadAsyncTask) {
		imageLoadAsyncTask_ = new WeakReference<ImageLoadAsyncTask>(imageLoadAsyncTask);
	}
}
