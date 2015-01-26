/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery.RecyclerView;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.ruswizards.rwgallery.GalleryItem;
import com.ruswizards.rwgallery.R;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for RecyclerView fragment
 */
public class RecyclerViewFragment extends Fragment implements View.OnClickListener {
	// TODO: delete LOG_TAG
	private static final String LOG_TAG = "RecyclerView";

	private static final String STATE_LAYOUT_MANAGER_TYPE = "LayoutManagerType";
	private static final String STATE_LAST_PATH = "LastPath";
	public final static float CACHE_MAX_MEMORY_PERCENTAGE = 0.2f;

	private List<GalleryItem> dataSet_;
	private RecyclerView recyclerView_;
	private RecyclerView.LayoutManager layoutManager_;
	private LayoutManagerType layoutManagerType_;
	private CustomRecyclerViewAdapter recyclerViewAdapter_;


	public RecyclerViewFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflates the layout
		View rootView = inflater.inflate(R.layout.fragment_recycler_view, container, false);

		recyclerView_ = (RecyclerView) rootView.findViewById(R.id.recycler_view);
		// TODO: check if following could be deleted
		// Set LayoutManager
		layoutManager_ = new LinearLayoutManager(getActivity());
		layoutManagerType_ = LayoutManagerType.GRID_LAYOUT;
		// Retain saved state
		String path;
		if (savedInstanceState != null) {
			layoutManagerType_ = (LayoutManagerType) savedInstanceState.getSerializable(
					STATE_LAYOUT_MANAGER_TYPE);
			path = savedInstanceState.getString(STATE_LAST_PATH);
		} else {
			// Set path to system default directory
			path = Environment.
					getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
		}
		// Get data from start directory
		dataSet_ = new ArrayList<>();
		fillDataSet(path, dataSet_, true);

		// Fill in RecyclerView
		setLayoutManager(layoutManagerType_);
		recyclerViewAdapter_ = new CustomRecyclerViewAdapter(dataSet_, this);
		recyclerView_.setAdapter(recyclerViewAdapter_);

		RecyclerOnScrollListener listener = new RecyclerOnScrollListener();
		recyclerView_.setOnScrollListener(listener);

		// Set listeners for icons to change LayoutManager type
		ImageView imageView = (ImageView)rootView.findViewById(R.id.switch_to_linear_image_view);
		imageView.setOnClickListener(this);
		imageView = (ImageView)rootView.findViewById(R.id.switch_to_grid_image_view);
		imageView.setOnClickListener(this);
		imageView = (ImageView)rootView.findViewById(R.id.switch_to_staggered_grid_image_view);
		imageView.setOnClickListener(this);

		return rootView;
	}

	public static void fillDataSet(String path, List<GalleryItem> dataSet, boolean includeDirectories) {
		dataSet.clear();
		// Get images and directories
		File directory = new File(path);
		File[] files  = new File(path).listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory()
						| file.getAbsolutePath().endsWith(".jpg")
						| file.getAbsolutePath().endsWith(".png");
			}
		});
		//Set first dataSet_ item for navigating to parent directory
		if (directory.getParent() != null && includeDirectories) {
			dataSet.add(new GalleryItem.ParentDirectory(
							directory.getParentFile().getName(),
							directory.getParent(),
							GalleryItem.ItemType.PARENT,
							directory.getAbsolutePath())
			);
		}
		if (files == null){
			return;
		}
		// Copy selected files to dataSet_
		for (File file : files){
			if (file.isDirectory()){
				if (includeDirectories) {
					dataSet.add(new GalleryItem(
									file.getName(),
									file.getAbsolutePath(),
									GalleryItem.ItemType.DIRECTORY)
					);
				}
			} else {
				dataSet.add(new GalleryItem(
								file.getName(),
								file.getAbsolutePath(),
								GalleryItem.ItemType.LOCAL_ITEM)
				);
			}
		}
	}

	/**
	 * Updates data set and notifies adapter
	 *
	 * @param path Source directory path
	 */
	public void modifyDataSet(String path){
		fillDataSet(path, dataSet_, true);
		recyclerViewAdapter_.notifyDataSetChanged();
//		startCaching();
	}
	// TODO: think about caching here
	/*private void startCaching() {
		ImageLoader.getInstance().loadImage("file://" + dataSet_.get(0).getSource(), new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {

			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {

			}
		});
	}

	private static class CachingImagesListener implements ImageLoadingListener{
		@Override
		public void onLoadingStarted(String imageUri, View view) {}

		@Override
		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {}

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {

		}
	}*/



	/**
	 * Sets new LayoutManager type and applies changes
	 */
	public void setLayoutManager(LayoutManagerType layoutManagerType) {
		int position;
		// Get current scroll position or set it to 0
		if (recyclerView_.getLayoutManager() != null) {
			if (!(recyclerView_.getLayoutManager() instanceof StaggeredGridLayoutManager)){
				position = ((LinearLayoutManager)
						recyclerView_.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
			} else {
				position = ((StaggeredGridLayoutManager) recyclerView_.getLayoutManager()).
						findFirstCompletelyVisibleItemPositions(null)[0];
			}
		} else position = 0;
		// Set up layout manager
		Point size = new Point();
		getActivity().getWindowManager().getDefaultDisplay().getSize(size);
		switch (layoutManagerType){
			case LINEAR_LAYOUT:
//				layoutManager_ = new LinearLayoutManager(getActivity());
				layoutManager_ = new CachingLinearLayoutManager(getActivity());
				((CachingLinearLayoutManager) layoutManager_).setExtraLayoutSpace(size.y);
				layoutManagerType_ = layoutManagerType;
				break;
			case GRID_LAYOUT:
				// TODO: add span count change (do not forget to save state)
//				layoutManager_ = new GridLayoutManager(getActivity(), 3);
				layoutManager_ = new CachingGridLayoutManager(getActivity(), 3);
				((CachingGridLayoutManager) layoutManager_).setExtraLayoutSpace(size.y);
				layoutManagerType_ = layoutManagerType;
				break;
			case STAGGERED_GRID_LAYOUT:
				// TODO: add span count change (do not forget to save state)
				layoutManager_ =
						new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
				((StaggeredGridLayoutManager)layoutManager_).setGapStrategy(
						StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
				layoutManagerType_ = layoutManagerType;
				break;
			default:
				layoutManager_ = new LinearLayoutManager(getActivity());
				layoutManagerType_ = layoutManagerType;
				break;
		}
		// Apply changes
		recyclerView_.setLayoutManager(layoutManager_);
		recyclerView_.scrollToPosition(position);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Save LayoutManager type
		outState.putSerializable(STATE_LAYOUT_MANAGER_TYPE, layoutManagerType_);
		// Save current directory path
		String lastPath;
		if (dataSet_.get(0) != null && dataSet_.get(0) instanceof GalleryItem.ParentDirectory){
			lastPath = ((GalleryItem.ParentDirectory) dataSet_.get(0)).getPath();
		} else {
			lastPath = "/";											// Upper level of navigation
		}
		outState.putString(STATE_LAST_PATH, lastPath);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.switch_to_linear_image_view:
				setLayoutManager(LayoutManagerType.LINEAR_LAYOUT);
				break;
			case R.id.switch_to_grid_image_view:
				setLayoutManager(LayoutManagerType.GRID_LAYOUT);
				break;
			case R.id.switch_to_staggered_grid_image_view:
				setLayoutManager(LayoutManagerType.STAGGERED_GRID_LAYOUT);
				break;
		}
	}

	public static class CachingLinearLayoutManager extends LinearLayoutManager {
		private int extraLayoutSpace_ = -1;

		public CachingLinearLayoutManager(Context context) {
			super(context);
		}

		public CachingLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
			super(context, orientation, reverseLayout);
		}

		public void setExtraLayoutSpace(int spaceSize) {
			extraLayoutSpace_ = spaceSize;
		}

		@Override
		protected int getExtraLayoutSpace(RecyclerView.State state) {
			if (extraLayoutSpace_ > 0) {
				return extraLayoutSpace_;
			} else {
				return super.getExtraLayoutSpace(state);
			}
		}
	}

	public static class CachingGridLayoutManager extends GridLayoutManager{
		private int extraLayoutSpace_ = -1;

		public CachingGridLayoutManager(Context context, int spanCount) {
			super(context, spanCount);
		}

		public CachingGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
			super(context, spanCount, orientation, reverseLayout);
		}


		public void setExtraLayoutSpace(int spaceSize){
			extraLayoutSpace_ = spaceSize;
		}

		@Override
		protected int getExtraLayoutSpace(RecyclerView.State state) {

			if (extraLayoutSpace_ > 0){
				return extraLayoutSpace_;
			} else {
				return super.getExtraLayoutSpace(state);
			}
		}
	}

	public static class CachingStaggeredGridLayoutManager extends StaggeredGridLayoutManager{
		private int extraLayoutSpace_ = -1;

		/**
		 * Creates a StaggeredGridLayoutManager with given parameters.
		 *
		 * @param spanCount   If orientation is vertical, spanCount is number of columns. If
		 *                    orientation is horizontal, spanCount is number of rows.
		 * @param orientation {@link #VERTICAL} or {@link #HORIZONTAL}
		 */
		public CachingStaggeredGridLayoutManager(int spanCount, int orientation) {
			super(spanCount, orientation);
		}

		public void setExtraLayoutSpace(int spaceSize){
			extraLayoutSpace_ = spaceSize;
		}

		/*@Override
		protected int getExtraLayoutSpace(RecyclerView.State state) {
			if (extraLayoutSpace_ > 0){
				return extraLayoutSpace_;
			} else {
				return super.getExtraLayoutSpace(state);
			}
		}*/
	}

	public enum LayoutManagerType {
		GRID_LAYOUT, LINEAR_LAYOUT, STAGGERED_GRID_LAYOUT
	}
}
