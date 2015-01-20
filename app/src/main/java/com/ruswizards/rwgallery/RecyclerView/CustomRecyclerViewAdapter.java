/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery.RecyclerView;

import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ruswizards.rwgallery.GalleryItem;
import com.ruswizards.rwgallery.R;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Adapter for RecyclerView class
 */
public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {
	// TODO: delete LOG_TAG
	private static final String LOG_TAG = "CustomRecyclerViewAdapter";

	private final static float CACHE_MAX_MEMORY_PERCENTAGE = 0.2f;

	private List<GalleryItem> dataSet_;
	private LruCache<String, Bitmap> bitmapLruCache_;
	private RecyclerViewFragment recyclerView_;

	public CustomRecyclerViewAdapter(List<GalleryItem> dataSet, RecyclerViewFragment recyclerView){
		dataSet_ = dataSet;
		recyclerView_ = recyclerView;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View newView = LayoutInflater.from(viewGroup.getContext()).
				inflate(R.layout.recycler_view_item, viewGroup, false);
		// Get retained cache
		RetainFragment retainFragment =
				RetainFragment.getInstance(recyclerView_.getActivity().getFragmentManager());
		bitmapLruCache_ = retainFragment.getRetainedCache();
		if (bitmapLruCache_ == null){
			// Get memory for LruCache
			final int cacheSize =
					(int)(Runtime.getRuntime().maxMemory() / 1024 * CACHE_MAX_MEMORY_PERCENTAGE);
			// Initialize LruCache
			bitmapLruCache_ = new LruCache<String, Bitmap>(cacheSize){
				@Override
				protected int sizeOf(String key, Bitmap value) {
					return value.getByteCount() / 1024;
				}
			};
			retainFragment.setRetainedCache(bitmapLruCache_);
		}
		// Return ViewHolder with set up item click listener
		return new ViewHolder(newView, new ViewHolder.ViewHolderClicksInterface() {
			@Override
			public void itemClicked(int position) {
				GalleryItem item = dataSet_.get(position);
				switch (item.getItemType()){
					case PARENT:
						// Navigate up
						recyclerView_.modifyDataSet(item.getSource());
						break;
					case LOCAL_ITEM:
						Log.d(LOG_TAG, "Clicked local");
						break;
					case DIRECTORY:
						// Navigate to selected directory
						recyclerView_.modifyDataSet(item.getSource());
						break;
				}
			}
		});
	}

	/**
	 * Fragment for retaining cache on config changes
	 */
	public static class RetainFragment extends android.app.Fragment{
		private final static String TAG = "RetainFragment";

		private LruCache<String, Bitmap> retainedCache_;

		public RetainFragment() {}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
		}

		/**
		 * Returns instance of RetainFragment
		 */
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


	/**
	 * Adds bitmap to cache with specified key
	 */
	public void addBitmapToLruCache(String key, Bitmap bitmap){
		synchronized (bitmapLruCache_) {
			if (getBitmapFromCache(key) == null){
				bitmapLruCache_.put(key, bitmap);
			}
		}
	}

	/**
	 * Gets bitmap with specified key from cache
	 */
	private Bitmap getBitmapFromCache(String key) {
		Bitmap resultBitmap;
		synchronized (bitmapLruCache_){
			resultBitmap = bitmapLruCache_.get(key);
		}
		return resultBitmap;
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int i) {
		// Reset views
		PreviewImageView previewImageView = viewHolder.getPreviewImageView();
		previewImageView.setImageResource(android.R.color.holo_green_light);
//		ProgressBar progressBar = viewHolder.getProgressBar();
		viewHolder.getTitleTextView().setVisibility(View.GONE);
		// Fill views
		GalleryItem item = dataSet_.get(i);
//		cancelPotentialWork(item.getSource(), previewImageView);
		if (item.getItemType() == GalleryItem.ItemType.PARENT){
			// Cancel task linked to previewImageView
//			cancelPotentialWork(item.getSource(), previewImageView);
			// Fill views
			previewImageView.setImageBitmap(null);
			previewImageView.setImageDrawable(recyclerView_.getActivity().
					getResources().getDrawable(android.R.drawable.stat_sys_upload));
//			progressBar.setVisibility(View.GONE);
			viewHolder.getTitleTextView().setVisibility(View.VISIBLE);
			viewHolder.getTitleTextView().setText(item.getTitle());
			return;
		} else if (item.getItemType() == GalleryItem.ItemType.DIRECTORY){
			// Cancel task linked to previewImageView
//			cancelPotentialWork(item.getSource(), previewImageView);
			// Fill views
			previewImageView.setImageDrawable(recyclerView_.getActivity().
					getResources().getDrawable(android.R.drawable.ic_dialog_dialer));
//			progressBar.setVisibility(View.GONE);
			viewHolder.getTitleTextView().setVisibility(View.VISIBLE);
			viewHolder.getTitleTextView().setText(item.getTitle());
			return;
		}
//		progressBar.setVisibility(View.VISIBLE);
//		loadBitmap(item.getSource(), previewImageView, progressBar);
		int width = recyclerView_.getLayoutManager().getWidth();
		ImageLoader.getInstance().displayImage("file://" + item.getSource(), previewImageView);
	}

	/**
	 * Loads bitmap from given source
	 */
	private void loadBitmap(
			String source, PreviewImageView previewImageView, ProgressBar progressBar) {
		boolean isCancelled = cancelPotentialWork(source, previewImageView);
		// First tries to get bitmap from a cache. If item is not in a cache, starts new task
		final Bitmap bitmap = getBitmapFromCache(source);
		if (bitmap != null) {
			progressBar.setVisibility(View.INVISIBLE);
			previewImageView.setImageBitmap(bitmap);
		} else if (!isCancelled){
			ImageLoadAsyncTask task = new ImageLoadAsyncTask(
					previewImageView, progressBar, recyclerView_.getActivity(), this);
			previewImageView.setImageLoadAsyncTask(task);
			task.execute(source);
		}
	}

	/**
	 * Checks if potential task should be cancelled (it is already linked to this view) and cancels
	 * already running task linked to view.
	 *
	 * @return True if task should be cancelled, false otherwise
	 */
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

	/**
	 *  Class for view holder
	 */
	public static class ViewHolder extends RecyclerView.ViewHolder {
		// TODO: delete LOG_TAG
		private static final String LOG_TAG = "CustomRecyclerViewAdapter.ViewHolder";

		private TextView titleTextView_;
		private PreviewImageView previewImageView_;
		// TODO: later find out how to use ContentLoadingProgressBar
		private ProgressBar progressBar_;

		public ViewHolder(final View itemView, final ViewHolderClicksInterface clickListener) {
			super(itemView);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO: if needed check view that was clicked and call appropriate method from interface
					clickListener.itemClicked(getPosition());
				}
			});
			titleTextView_ = (TextView) itemView.findViewById(R.id.title_text_view);
			previewImageView_ = (PreviewImageView)itemView.findViewById(R.id.preview_image_view);
//			progressBar_ = (ProgressBar)itemView.findViewById(R.id.progress_bar);
		}

		/**
		 * Interface to handle item clicks
		 */
		public interface ViewHolderClicksInterface{
			public void itemClicked(int position);
			// TODO: add here handle click on selection checkbox;
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
