/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.ruswizards.rwgallery.R;

import java.lang.ref.WeakReference;

/**
 * AsyncTask class to get Bitmap from source
 */
public class ImageLoadAsyncTask extends AsyncTask<String, Void, Bitmap> {
	// TODO: delete LOG_TAG
	private static final String LOG_TAG = "ImageLoadAsyncTask";

	private WeakReference<PreviewImageView> imageViewWeakReference_;
	private WeakReference<ProgressBar> progressBarWeakReference_;
	private WeakReference<CustomRecyclerViewAdapter> adapterWeakReference_;

	private String source_;
	private Context context_;

	public ImageLoadAsyncTask(PreviewImageView previewImageView, ProgressBar progressBar,
							  Context context, CustomRecyclerViewAdapter customRecyclerViewAdapter){
		imageViewWeakReference_ = new WeakReference<>(previewImageView);
		progressBarWeakReference_ = new WeakReference<>(progressBar);
		context_ = context;
		adapterWeakReference_ = new WeakReference<>(customRecyclerViewAdapter);
	}

	/**
	 * Decodes bitmap by calling {@link #decodeBitmapFromResource(String, int, int)}
	 * @param params Source to bitmap
	 * @return Decoded bitmap
	 */
	@Override
	protected Bitmap doInBackground(String... params) {
		source_ = params[0];
		// Get desired dimensions (needed to calculate inSampleSize value)
		int width = (int) context_.getResources().getDimension(R.dimen.linear_layout_image_width);
		int height = (int) context_.getResources().getDimension(R.dimen.linear_layout_image_height);
		return decodeBitmapFromResource(source_, width, height);
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		// Check if task was cancelled
		if (isCancelled()){
			bitmap = null;
		}
		// If link to previewImageView is still active, applies change to views
		if (imageViewWeakReference_.get() != null && bitmap != null){
			PreviewImageView imageView = imageViewWeakReference_.get();
			// Check if previewImageView still linked to this ImageLoadAsyncTask
			if (imageView != null && this == imageView.getImageLoadAsyncTask()){
				imageView.setImageBitmap(bitmap);
				ProgressBar progressBar = progressBarWeakReference_.get();
				progressBar.setVisibility(View.INVISIBLE);
			}
			// Adds bitmap to cache
			adapterWeakReference_.get().addBitmapToLruCache(source_, bitmap);
		}
	}

	public String getFilePath() {
		return source_;
	}

	/**
	 * Decodes bitmap with calculated inSampleSize option
 	 */
	private Bitmap decodeBitmapFromResource(String filePath, int requireWidth, int requiredHeight) {
		// Decode only dimensions of bitmap
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		// Calculate inSampleSize value
		options.inSampleSize = calculateInSampleSize(options, requireWidth, requiredHeight);
		// Decode bitmap
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 * Calculates inSampleSize value based on bitmaps' width and height and desired values for it
	 */
	private int calculateInSampleSize(
			BitmapFactory.Options options, int requiredWidth, int requiredHeight) {
		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;
		if (height > requiredHeight || width > requiredWidth){
			height = height / 2;
			width = width / 2;
			while ((height / inSampleSize > requiredHeight)
					&& (width / inSampleSize > requiredWidth)){
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
}
