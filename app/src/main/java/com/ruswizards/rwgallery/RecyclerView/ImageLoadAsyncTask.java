package com.ruswizards.rwgallery.RecyclerView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ruswizards.rwgallery.R;

import java.lang.ref.WeakReference;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
public class ImageLoadAsyncTask extends AsyncTask<String, Void, Bitmap> {

	private static final String LOG_TAG = "ImageLoadAsyncTask";
	private WeakReference<PreviewImageView> imageViewWeakReference_;
	private WeakReference<ProgressBar> progressBarWeakReference_;

	public String getFilePath() {
		return filePath_;
	}

	private String filePath_;
	private Context context_;

	public ImageLoadAsyncTask(PreviewImageView previewImageView, ProgressBar progressBar, Context context){
		imageViewWeakReference_ = new WeakReference<PreviewImageView>(previewImageView);
		progressBarWeakReference_ = new WeakReference<ProgressBar>(progressBar);
		context_ = context;
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		Log.d(LOG_TAG, "doInBackground--");
		filePath_ = params[0];
		int width = (int) context_.getResources().getDimension(R.dimen.linear_layout_image_width);
		int height = (int) context_.getResources().getDimension(R.dimen.linear_layout_image_height);
		return decodeBitmapFromResource(filePath_, width, height);
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		Log.d(LOG_TAG, "onPostExecute--");
		if (isCancelled()){
			bitmap = null;
		}
		if (imageViewWeakReference_ != null && bitmap != null){
			ProgressBar progressBar = progressBarWeakReference_.get();
			progressBar.setVisibility(View.INVISIBLE);
			PreviewImageView imageView = imageViewWeakReference_.get();
			if (imageView != null && this == imageView.getImageLoadAsyncTask()){
				imageView.setImageBitmap(bitmap);
				Log.d(LOG_TAG, "Setting bitmap--");
			}
		}
	}

	private Bitmap decodeBitmapFromResource(String filePath, int requireWidth, int requiredHeight) {
		// Check dimensions
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		options.inSampleSize = calculateInSampleSize(options, requireWidth, requiredHeight);
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);
	}

	private int calculateInSampleSize(BitmapFactory.Options options, int requireWidth, int requiredHeight) {
		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;

		if (height > requiredHeight || width > requireWidth){
			height = height / 2;
			width = width / 2;

			while ((height / inSampleSize > requiredHeight) && (width / inSampleSize > requireWidth)){
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
}
