/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery.RecyclerView;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		try {
			Drawable drawable = getDrawable();
			if (drawable == null){
				setMeasuredDimension(0, 0);
			} else {
				int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
				int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
				if (measuredHeight == 0 && measuredWidth == 0) { //Height and width set to wrap_content
					setMeasuredDimension(measuredWidth, measuredHeight);
				} else if (measuredHeight == 0) { //Height set to wrap_content
					int width = measuredWidth;
					int height = width *  drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
					setMeasuredDimension(width, height);
				} else if (measuredWidth == 0){ //Width set to wrap_content
					int height = measuredHeight;
					int width = height * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
					setMeasuredDimension(width, height);
				} else { //Width and height are explicitly set (either to match_parent or to exact value)
					setMeasuredDimension(measuredWidth, measuredHeight);
				}
			}
		} catch (Exception e){
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
