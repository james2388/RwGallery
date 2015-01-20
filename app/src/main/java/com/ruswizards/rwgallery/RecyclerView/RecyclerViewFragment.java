package com.ruswizards.rwgallery.RecyclerView;


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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.ruswizards.rwgallery.GalleryItem;
import com.ruswizards.rwgallery.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecyclerViewFragment extends Fragment implements View.OnClickListener {

	private static final String STATE_LAYOUT_MANAGER_TYPE = "LayoutManagerType";
	private static final String LOG_TAG = "RecyclerView";
	private static final String STATE_LAST_PATH = "LastPath";
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

		String path;
		// TODO: at the end remove fixed default pass
		//defaultPath = "/storage/sdcard1/DCIM/Camera";

		if (savedInstanceState != null) {
			layoutManagerType_ = (LayoutManagerType) savedInstanceState.getSerializable(STATE_LAYOUT_MANAGER_TYPE);
			path = savedInstanceState.getString(STATE_LAST_PATH);
		} else {
			path = Environment.
					getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
		}
		// Get data from start directory
		dataSet_ = new ArrayList<>();
		fillDataSet(path);
		setLayoutManager(layoutManagerType_);
		// Fill in RecyclerView
		recyclerViewAdapter_ = new CustomRecyclerViewAdapter(dataSet_, this);
		recyclerView_.setAdapter(recyclerViewAdapter_);

		// Set listeners for icons to change LayoutManager type
		ImageView imageView = (ImageView)rootView.findViewById(R.id.switch_to_linear_image_view);
		imageView.setOnClickListener(this);
		imageView = (ImageView)rootView.findViewById(R.id.switch_to_grid_image_view);
		imageView.setOnClickListener(this);
		imageView = (ImageView)rootView.findViewById(R.id.switch_to_staggered_grid_image_view);
		imageView.setOnClickListener(this);

		return rootView;
	}

	public void fillDataSet(String path) {
		dataSet_.clear();
		// Get images and directories
		File directory = new File(path);
		File[] files  = new File(path).listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()
						| file.getAbsolutePath().endsWith(".jpg")
						| file.getAbsolutePath().endsWith(".png")){
					return true;
				} else {
					return false;
				}
			}
		});
		//Set first one item for navigating to parent directory
		if (directory.getParent() != null) {
			dataSet_.add(new GalleryItem.Directory(directory.getParentFile().getName(), directory.getParent(), GalleryItem.ItemType.PARENT, directory.getAbsolutePath()));
		}
		if (files == null){
			return;
		}
		// Copy selected files to a dataSet
		for (File file : files){
			if (file.isDirectory()){
				dataSet_.add(new GalleryItem(file.getName(), file.getAbsolutePath(), GalleryItem.ItemType.DIRECTORY));
			} else {
				dataSet_.add(new GalleryItem(file.getName(), file.getAbsolutePath(), GalleryItem.ItemType.LOCAL_ITEM));
			}
		}
	}

	public void modifyDataSet(String path){
		fillDataSet(path);
		recyclerViewAdapter_.notifyDataSetChanged();
	}

	public void setLayoutManager(LayoutManagerType layoutManagerType) {
		int position;
		if (recyclerView_.getLayoutManager() != null) {
			if (!(recyclerView_.getLayoutManager() instanceof StaggeredGridLayoutManager)){
				position = ((LinearLayoutManager) recyclerView_.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
			} else {
				position = ((StaggeredGridLayoutManager) recyclerView_.getLayoutManager()).findFirstCompletelyVisibleItemPositions(null)[0];
			}
		} else position = 0;

		switch (layoutManagerType){
			case LINEAR_LAYOUT:
				layoutManager_ = new LinearLayoutManager(getActivity());
				layoutManagerType_ = layoutManagerType;
				break;
			case GRID_LAYOUT:
				// TODO: add span count change (do not forget to save state)
				layoutManager_ = new GridLayoutManager(getActivity(), 3);
				layoutManagerType_ = layoutManagerType;
				break;
			case STAGGERED_GRID_LAYOUT:
				// TODO: add span count change (do not forget to save state)
				layoutManager_ = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
				((StaggeredGridLayoutManager)layoutManager_).setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
				layoutManagerType_ = layoutManagerType;
				break;
			default:
				layoutManager_ = new LinearLayoutManager(getActivity());
				layoutManagerType_ = layoutManagerType;
				break;
		}
		recyclerView_.setLayoutManager(layoutManager_);
		recyclerView_.scrollToPosition(position);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(STATE_LAYOUT_MANAGER_TYPE, layoutManagerType_);
		String lastPath;
		if (dataSet_.get(0) instanceof GalleryItem.Directory){
			lastPath = ((GalleryItem.Directory) dataSet_.get(0)).getPath();
		} else {
			lastPath = "";
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

	public enum LayoutManagerType {
		GRID_LAYOUT, LINEAR_LAYOUT, STAGGERED_GRID_LAYOUT
	}
}
