/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery.RecyclerView;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.ruswizards.rwgallery.ViewPager.ImagePagerActivity;
import com.ruswizards.rwgallery.ViewPager.ImagesViewingActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for RecyclerView class
 */
public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {
	// TODO: delete LOG_TAG
	private static final String LOG_TAG = "CustomRecyclerViewAdapter";

	private List<GalleryItem> dataSet_;
	private RecyclerViewFragment recyclerViewFragment_;

	public CustomRecyclerViewAdapter(List<GalleryItem> dataSet, RecyclerViewFragment recyclerViewFragment){
		dataSet_ = dataSet;
		recyclerViewFragment_ = recyclerViewFragment;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View newView = LayoutInflater.from(viewGroup.getContext()).
				inflate(R.layout.recycler_view_item, viewGroup, false);
		// Return ViewHolder with set up item click listener
		return new ViewHolder(newView, new ViewHolder.ViewHolderClicksInterface() {
			@Override
			public void itemClicked(int position) {
				GalleryItem item = dataSet_.get(position);
				switch (item.getItemType()){
					case PARENT:
						// Navigate up
						recyclerViewFragment_.modifyDataSet(item.getSource());
						break;
					case LOCAL_ITEM:
						ImageLoader.getInstance().destroy();
						Intent openImageIntent = new Intent(recyclerViewFragment_.getActivity(), ImagesViewingActivity.class);
						int directories = 0;
						int j = 0;
						while (j < position){
							if (dataSet_.get(j).getItemType() == GalleryItem.ItemType.PARENT || dataSet_.get(j).getItemType() == GalleryItem.ItemType.DIRECTORY){
								directories++;
							}
							j++;
						}
						openImageIntent.putExtra(ImagePagerActivity.EXTRA_ITEM_NUMBER, position - directories);
						String sourceDirectory;
						if (dataSet_.get(0) instanceof GalleryItem.ParentDirectory){
							sourceDirectory = ((GalleryItem.ParentDirectory)dataSet_.get(0)).getPath();
						} else {
							sourceDirectory = "/";
						}
						openImageIntent.putExtra(ImagePagerActivity.EXTRA_SOURCE_DIRECTORY, sourceDirectory);
						recyclerViewFragment_.getActivity().startActivity(openImageIntent);
						break;
					case DIRECTORY:
						// Navigate to selected directory
						recyclerViewFragment_.modifyDataSet(item.getSource());
						break;
				}
			}
		});
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int i) {
		// Reset views
		PreviewImageView previewImageView = viewHolder.getPreviewImageView();
		previewImageView.setImageResource(android.R.color.holo_green_light);
		viewHolder.getTitleTextView().setVisibility(View.GONE);
		// Fill views
		GalleryItem item = dataSet_.get(i);
		if (item.getItemType() == GalleryItem.ItemType.PARENT){
			// Fill views
			previewImageView.setImageBitmap(null);
			previewImageView.setImageDrawable(recyclerViewFragment_.getActivity().
					getResources().getDrawable(android.R.drawable.stat_sys_upload));
			viewHolder.getTitleTextView().setVisibility(View.VISIBLE);
			viewHolder.getTitleTextView().setText(item.getTitle());
			return;
		} else if (item.getItemType() == GalleryItem.ItemType.DIRECTORY){
			// Fill views
			previewImageView.setImageDrawable(recyclerViewFragment_.getActivity().
					getResources().getDrawable(android.R.drawable.ic_dialog_dialer));
			viewHolder.getTitleTextView().setVisibility(View.VISIBLE);
			viewHolder.getTitleTextView().setText(item.getTitle());
			return;
		}
		ImageLoader.getInstance().displayImage("file://" + item.getSource(), previewImageView);
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
					clickListener.itemClicked(getPosition());
				}
			});
			titleTextView_ = (TextView) itemView.findViewById(R.id.title_text_view);
			previewImageView_ = (PreviewImageView)itemView.findViewById(R.id.preview_image_view);
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
	}
}
