package com.ruswizards.rwgallery.RecyclerView;


import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class RecyclerViewFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

	private static final String STATE_LAYOUT_MANAGER_TYPE = "LayoutManagerType";
	private static final String LOG_TAG = "RecyclerView";
	private List<GalleryItem> dataSet_;
	private RecyclerView recyclerView_;
	private RecyclerView.LayoutManager layoutManager_;
	private LayoutManagerType layoutManagerType_;
	private CustomRecyclerViewAdapter recyclerViewAdapter_;


	public RecyclerViewFragment() {
		// Required empty public constructor
		String defaultPath = Environment.
				getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
		// TODO: at the end remove fixed default pass
		defaultPath = "/storage/sdcard1/DCIM/Camera";
		initialiseDataSet(defaultPath);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflates the layout
		View rootView = inflater.inflate(R.layout.fragment_recycler_view, container, false);
		recyclerView_ = (RecyclerView) rootView.findViewById(R.id.recycler_view);

		// TODO: check if following could be deleted
		layoutManager_ = new LinearLayoutManager(getActivity());
		layoutManagerType_ = LayoutManagerType.GRID_LAYOUT;

		if (savedInstanceState != null) {
			layoutManagerType_ = (LayoutManagerType) savedInstanceState.getSerializable(STATE_LAYOUT_MANAGER_TYPE);
		}
		setLayoutManager(layoutManagerType_);

		recyclerViewAdapter_ = new CustomRecyclerViewAdapter(dataSet_, getActivity());
		recyclerView_.setAdapter(recyclerViewAdapter_);

		ImageView imageView = (ImageView)rootView.findViewById(R.id.switch_to_linear_image_view);
		imageView.setOnClickListener(this);

		imageView = (ImageView)rootView.findViewById(R.id.switch_to_grid_image_view);
		imageView.setOnClickListener(this);

		return rootView;
	}


	private void initialiseDataSet(String path) {
		/*dataSet_ = new String[20];
		for (int i = 0; i < 20; i++) {
			dataSet_[i] = "Test item #" + i;
		}*/
		dataSet_ = new ArrayList<>();

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

		for (File file : files){
			if (file.isDirectory()){
				dataSet_.add(new GalleryItem(file.getName(), file.getAbsolutePath(), GalleryItem.ItemType.DIRECTORY));
			} else {
				dataSet_.add(new GalleryItem(file.getName(), file.getAbsolutePath(), GalleryItem.ItemType.LOCAL_ITEM));
			}
		}



	}

	public void setLayoutManager(LayoutManagerType layoutManagerType) {
		int position;
		if (recyclerView_.getLayoutManager() != null){
			position = ((LinearLayoutManager) recyclerView_.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
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
		}
	}

	public enum LayoutManagerType {
		GRID_LAYOUT, LINEAR_LAYOUT
	}

}
