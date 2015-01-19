package com.ruswizards.rwgallery.RecyclerView;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ruswizards.rwgallery.GalleryItem;
import com.ruswizards.rwgallery.R;

import java.util.List;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {

	private final static float CACHE_MAX_MEMORY_PERCENTAGE = 0.2f;

	private static final String LOG_TAG = "CustomRecyclerViewAdapter";
	private List<GalleryItem> dataSet_;
	private LruCache<String, Bitmap> bitmapLruCache_;
	private Activity context_;

	public CustomRecyclerViewAdapter(List<GalleryItem> dataSet, Activity context){
		dataSet_ = dataSet;
		context_ = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View newView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_item, viewGroup, false);
		RetainFragment retainFragment = RetainFragment.getInstance(context_.getFragmentManager());
		bitmapLruCache_ = retainFragment.getRetainedCache();
		if (bitmapLruCache_ == null){
			// Get memory for LruCache
			final int cacheSize = (int)(Runtime.getRuntime().maxMemory() / 1024 * CACHE_MAX_MEMORY_PERCENTAGE);
			// Initialize LruCache
			bitmapLruCache_ = new LruCache<String, Bitmap>(cacheSize){
				@Override
				protected int sizeOf(String key, Bitmap value) {
					return value.getByteCount() / 1024;
				}
			};
			retainFragment.setRetainedCache(bitmapLruCache_);
		}

		return new ViewHolder(newView);
	}

	public static class RetainFragment extends android.app.Fragment{
		private final static String TAG = "RetainFragment";

		private LruCache<String, Bitmap> retainedCache_;

		public RetainFragment() {}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
		}

		public static RetainFragment getInstance (FragmentManager fragmentManager){
			RetainFragment retainFragment = (RetainFragment)fragmentManager.findFragmentByTag(TAG);
			if (retainFragment == null){
				retainFragment = new RetainFragment();
				fragmentManager.beginTransaction().add(retainFragment, TAG).commit();
			}
			return retainFragment;
		}

		public LruCache<String, Bitmap> getRetainedCache() {
			return retainedCache_;
		}

		public void setRetainedCache(LruCache<String, Bitmap> retainedCache) {
			retainedCache_ = retainedCache;
		}

	}


	public void addBitmapToLruCache(String key, Bitmap bitmap){
		synchronized (bitmapLruCache_) {
			if (getBitmapFromCache(key) == null){
				bitmapLruCache_.put(key, bitmap);
			}
		}
	}

	private Bitmap getBitmapFromCache(String key) {
		Bitmap resultBitmap;
		synchronized (bitmapLruCache_){
			resultBitmap = bitmapLruCache_.get(key);
		}
		return resultBitmap;
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int i) {
		PreviewImageView previewImageView = viewHolder.getPreviewImageView();
		previewImageView.setImageResource(android.R.color.holo_green_light);

		ProgressBar progressBar = viewHolder.getProgressBar();
		progressBar.setVisibility(View.VISIBLE);

		GalleryItem item = dataSet_.get(i);
		viewHolder.getTitleTextView().setText(item.getTitle());
//		new ImageLoadAsyncTask(previewImageView, progressBar, context_).execute(item.getSource());
		loadBitmap(item.getSource(), previewImageView, progressBar);
	}

	private void loadBitmap(String source, PreviewImageView previewImageView, ProgressBar progressBar) {
		boolean isCancelled = cancelPotentialWork(source, previewImageView);
		final Bitmap bitmap = getBitmapFromCache(source);
		if (bitmap != null) {
			progressBar.setVisibility(View.INVISIBLE);
			previewImageView.setImageBitmap(bitmap);
		} else if (!isCancelled){
			ImageLoadAsyncTask task = new ImageLoadAsyncTask(previewImageView, progressBar, context_, this);
			previewImageView.setImageLoadAsyncTask(task);
			task.execute(source);
		}
	}

	private boolean cancelPotentialWork(String source, PreviewImageView previewImageView) {
		ImageLoadAsyncTask imageLoadAsyncTask = previewImageView.getImageLoadAsyncTask();
		if (imageLoadAsyncTask != null){
			if (!source.equals(imageLoadAsyncTask.getFilePath())){
				imageLoadAsyncTask.cancel(true);
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getItemCount() {
		return dataSet_.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder{
		private TextView titleTextView_;
		private PreviewImageView previewImageView_;
		// TODO: later find out how to use ContentLoadingProgressBar
		private ProgressBar progressBar_;
		private static final String LOG_TAG = "CustomRecyclerViewAdapter.ViewHolder";

		public ViewHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(LOG_TAG, "Clicked on: " + getPosition());
				}
			});
			titleTextView_ = (TextView) itemView.findViewById(R.id.title_text_view);
			previewImageView_ = (PreviewImageView)itemView.findViewById(R.id.preview_image_view);
			progressBar_ = (ProgressBar)itemView.findViewById(R.id.progress_bar);
		}

		public TextView getTitleTextView(){
			return titleTextView_;
		}

		public PreviewImageView getPreviewImageView(){
			return previewImageView_;
		}

		public ProgressBar getProgressBar(){
			return progressBar_;
		}
	}
}
