/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery.RecyclerView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Custom class for preview icon which contents link to current working AsyncTask
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

	/**
	 * Gets AsyncTask linked with this icon preview ImageView
	 *
	 * @return Reference to ImageLoadAsyncTask or null if no ImageLoadAsyncTask linked
	 */
	public ImageLoadAsyncTask getImageLoadAsyncTask() {
		ImageLoadAsyncTask imageLoadAsyncTask;
		try {
			imageLoadAsyncTask = imageLoadAsyncTask_.get();
		} catch (NullPointerException e){
			return null;
		}
		return imageLoadAsyncTask;
	}

	/**
	 * Links PreviewImageView and ImageLoadAsyncTask
	 */
	public synchronized void setImageLoadAsyncTask(ImageLoadAsyncTask imageLoadAsyncTask) {
		imageLoadAsyncTask_ = new WeakReference<ImageLoadAsyncTask>(imageLoadAsyncTask);
	}
}
