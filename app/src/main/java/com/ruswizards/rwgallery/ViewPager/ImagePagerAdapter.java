package com.ruswizards.rwgallery.ViewPager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ruswizards.rwgallery.GalleryItem;
import com.ruswizards.rwgallery.R;

import java.util.List;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 21.01.2015
 * Vladimir Farafonov
 */
public class ImagePagerAdapter extends FragmentStatePagerAdapter {
	private int size_;
	private List<GalleryItem> dataSet_;

	public ImagePagerAdapter(FragmentManager fm, int size, List<GalleryItem> dataSet) {
		super(fm);
		size_ = size;
		dataSet_ = dataSet;
	}

	@Override
	public Fragment getItem(int position) {
		return ImageFragment.newInstance(dataSet_.get(position).getSource());
	}

	@Override
	public int getCount() {
		return size_;
	}

	public static class ImageFragment extends Fragment {
		public static final String ITEM_SOURCE = "ItemSource";

		private String source_;
		private ImageView fullscreenImageView_;

		static ImageFragment newInstance(String source){
			ImageFragment fragment = new ImageFragment();
			Bundle arguments = new Bundle();
			arguments.putString(ITEM_SOURCE, source);
			fragment.setArguments(arguments);
			return fragment;
		}

		public ImageFragment() {}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if (getArguments() != null){
				source_ = getArguments().getString(ITEM_SOURCE);
			} else {
				source_ = "";
			}
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			ImageLoader.getInstance().displayImage("file://" + source_, fullscreenImageView_);
		}

		@Nullable
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			View view = inflater.inflate(R.layout.fragment_images_pager_item, container, false);
			fullscreenImageView_ = (ImageView) view.findViewById(R.id.fullscreen_image_view);
			return view;
		}
	}
}
