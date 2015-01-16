package com.ruswizards.rwgallery.RecyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

	private static final String LOG_TAG = "CustomRecyclerViewAdapter";
	private List<GalleryItem> dataSet_;
	private Context context_;

	public CustomRecyclerViewAdapter(List<GalleryItem> dataSet, Context context){
		dataSet_ = dataSet;
		context_ = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		Log.d(LOG_TAG, "onCreateViewHolder");
		View newView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_item, viewGroup, false);
		return new ViewHolder(newView);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int i) {
		Log.d(LOG_TAG, "onBindViewHolder--");
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
		if (cancelPotentiaWork(source, previewImageView)){
			ImageLoadAsyncTask task = new ImageLoadAsyncTask(previewImageView, progressBar, context_);
			previewImageView.setImageLoadAsyncTask(task);
			task.execute(source);
		}
	}

	private boolean cancelPotentiaWork(String source, PreviewImageView previewImageView) {
		ImageLoadAsyncTask imageLoadAsyncTask = previewImageView.getImageLoadAsyncTask();
		if (imageLoadAsyncTask != null){
			if (!source.equals(imageLoadAsyncTask.getFilePath())){
				imageLoadAsyncTask.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}

	/*private void loadBitmap(String source, ImageView previewImageView, ProgressBar progressBar, Context context_) {
		if (cancelPotentiaWork(source, previewImageView)){
			ImageLoadAsyncTask task = new ImageLoadAsyncTask(previewImageView, progressBar, context_);
			AsyncDrawable asyncDrawable = new AsyncDrawable(context_.getResources(), source, task);
			previewImageView.setImageDrawable(asyncDrawable);
			task.execute(source);
		}
	}

	private boolean cancelPotentiaWork(String source, ImageView previewImageView) {
		ImageLoadAsyncTask imageLoadAsyncTask = getImageLoadAsyncTask(previewImageView);
		if (ima)
	}

	private ImageLoadAsyncTask getImageLoadAsyncTask(ImageView previewImageView) {
		if (previewImageView != null){

		}
	}*/

	@Override
	public int getItemCount() {
		Log.d(LOG_TAG, "getItemCount");
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
			Log.d(LOG_TAG, "getPreviewImageView--");
			return previewImageView_;
		}

		public ProgressBar getProgressBar(){
			return progressBar_;
		}
	}
}
