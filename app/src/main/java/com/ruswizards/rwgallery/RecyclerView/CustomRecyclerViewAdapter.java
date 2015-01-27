/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery.RecyclerView;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.ruswizards.rwgallery.GalleryItem;
import com.ruswizards.rwgallery.R;
import com.ruswizards.rwgallery.ViewPager.ImagesViewingActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Adapter for RecyclerView class
 */
public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {
	// TODO: delete LOG_TAG
	private static final String LOG_TAG = "CustomRecyclerViewAdapter";

	private List<GalleryItem> dataSet_;
	private RecyclerViewFragment recyclerViewFragment_;
	private ImageLoader imageLoader_;

	public CustomRecyclerViewAdapter(List<GalleryItem> dataSet, RecyclerViewFragment recyclerViewFragment){
		dataSet_ = dataSet;
		recyclerViewFragment_ = recyclerViewFragment;
		imageLoader_ = ImageLoader.getInstance();
	}

	@Override
	public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
		final View newView = LayoutInflater.from(viewGroup.getContext()).
				inflate(R.layout.recycler_view_item, viewGroup, false);
		// Return ViewHolder with set up item click listener
		return new ViewHolder(newView, new ViewHolder.ViewHolderClicksInterface() {
			@Override
			public void itemClicked(int position, PreviewImageView previewImageView) {
				GalleryItem item = dataSet_.get(position);
				switch (item.getItemType()){
					case PARENT:
						// Navigate up
						recyclerViewFragment_.modifyDataSet(item.getSource());
						break;
					case LOCAL_ITEM:
						if (imageLoader_.isInited()) {
							imageLoader_.destroy();
						}
						Intent openImageIntent = new Intent(recyclerViewFragment_.getActivity(), ImagesViewingActivity.class);
						int directories = 0;
						int j = 0;
						while (j < position){
							if (dataSet_.get(j).getItemType() == GalleryItem.ItemType.PARENT || dataSet_.get(j).getItemType() == GalleryItem.ItemType.DIRECTORY){
								directories++;
							}
							j++;
						}
						openImageIntent.putExtra(ImagesViewingActivity.EXTRA_ITEM_NUMBER, position - directories);
						String sourceDirectory;
						if (dataSet_.get(0) instanceof GalleryItem.ParentDirectory){
							sourceDirectory = ((GalleryItem.ParentDirectory)dataSet_.get(0)).getPath();
						} else {
							sourceDirectory = "/";
						}
						openImageIntent.putExtra(ImagesViewingActivity.EXTRA_SOURCE_DIRECTORY, sourceDirectory);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
									recyclerViewFragment_.getActivity(),
									new Pair<View, String>(newView.findViewById(R.id.preview_image_view), ImagesViewingActivity.IMAGE_VIEW_TRANSITION_NAME)
							);
							ActivityCompat.startActivity(recyclerViewFragment_.getActivity(), openImageIntent, activityOptions.toBundle());
//							recyclerViewFragment_.getActivity().startActivity(openImageIntent, ActivityOptions.makeSceneTransitionAnimation(recyclerViewFragment_.getActivity(), previewImageView, previewImageView.getTransitionName()).toBundle());
						} else {
							recyclerViewFragment_.getActivity().startActivity(openImageIntent);
						}
						break;
					case DIRECTORY:
						// Navigate to selected directory
						recyclerViewFragment_.modifyDataSet(item.getSource());
						break;
				}
			}
		});
	}

	private void initializeImageLoader() {
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(true)
//				.considerExifParams(true)
//				.showImageOnLoading(android.R.drawable.ic_menu_crop)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.resetViewBeforeLoading(true)
				.displayer(new FadeInBitmapDisplayer(200))
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
		File cacheDir = StorageUtils.getCacheDirectory(recyclerViewFragment_.getActivity());

		Point size = new Point();
		recyclerViewFragment_.getActivity().getWindowManager().getDefaultDisplay().getSize(size);

		int side = size.x > size.y ? size.x : size.y;
		side = side / 5	;

		ImageLoaderConfiguration configuration = null;
		try {
			configuration = new ImageLoaderConfiguration.Builder(recyclerViewFragment_.getActivity())
					.memoryCache(new LRULimitedMemoryCache((int) (Runtime.getRuntime().maxMemory() * RecyclerViewFragment.CACHE_MAX_MEMORY_PERCENTAGE)))
					.memoryCacheExtraOptions(side, side)
					.diskCache(new LruDiscCache(cacheDir, new HashCodeFileNameGenerator(), 1024 * 1024 * 85))
					.diskCacheExtraOptions(side, side, null)
//					.writeDebugLogs()
					.defaultDisplayImageOptions(defaultOptions)
					.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ImageLoader.getInstance().init(configuration);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int i) {
		if (!imageLoader_.isInited()){
			initializeImageLoader();
		}
		// Reset views
		PreviewImageView previewImageView = viewHolder.getPreviewImageView();
		previewImageView.setImageResource(android.R.color.holo_green_light);
//		viewHolder.getTitleTextView().setVisibility(View.GONE);
		// Fill views
		GalleryItem item = dataSet_.get(i);
		if (item.getItemType() == GalleryItem.ItemType.PARENT){
			viewHolder.setIsRecyclable(false);
			// Fill views
			viewHolder.getTitleTextView().setVisibility(View.VISIBLE);
			viewHolder.getTitleTextView().setText(item.getTitle());
			previewImageView.setImageDrawable(recyclerViewFragment_.getActivity().
					getResources().getDrawable(android.R.drawable.stat_sys_upload));
			return;
		} else if (item.getItemType() == GalleryItem.ItemType.DIRECTORY){
			viewHolder.setIsRecyclable(false);
			// Fill views
			viewHolder.getTitleTextView().setVisibility(View.VISIBLE);
			viewHolder.getTitleTextView().setText(item.getTitle());
			previewImageView.setImageDrawable(recyclerViewFragment_.getActivity().
					getResources().getDrawable(android.R.drawable.ic_dialog_dialer));
			return;
		}
		imageLoader_.displayImage("file://" + item.getSource(), previewImageView);
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

		public ViewHolder(final View itemView, final ViewHolderClicksInterface clickListener) {
			super(itemView);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO: if needed check view that was clicked and call appropriate method from interface
					clickListener.itemClicked(getPosition(), previewImageView_);
				}
			});
			titleTextView_ = (TextView) itemView.findViewById(R.id.title_text_view);
			previewImageView_ = (PreviewImageView)itemView.findViewById(R.id.preview_image_view);
		}

		/**
		 * Interface to handle item clicks
		 */
		public interface ViewHolderClicksInterface{
			public void itemClicked(int position, PreviewImageView previewImageView_);
			// TODO: add here handle click on selection checkbox;
		}

		public TextView getTitleTextView(){
			return titleTextView_;
		}

		public PreviewImageView getPreviewImageView(){
			return previewImageView_;
		}
	}
}
