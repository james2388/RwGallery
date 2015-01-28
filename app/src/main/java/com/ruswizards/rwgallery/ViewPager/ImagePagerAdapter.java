/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 21.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery.ViewPager;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ruswizards.rwgallery.GalleryItem;
import com.ruswizards.rwgallery.R;

import java.util.List;

/**
 * Adapter class for ViewPager
 */
public class ImagePagerAdapter extends FragmentStatePagerAdapter {
	private int size_;
	private List<GalleryItem> dataSet_;
	private ImageFragment currentFragment_;

	public ImagePagerAdapter(FragmentManager fm, int size, List<GalleryItem> dataSet) {
		super(fm);
		size_ = size;
		dataSet_ = dataSet;
	}

	public ImageFragment getCurrentImageFragment(){
		return currentFragment_;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		currentFragment_ = (ImageFragment) object;
	}

	@Override
	public Fragment getItem(int position) {
		return ImageFragment.newInstance(dataSet_.get(position).getSource());
	}

	@Override
	public int getCount() {
		return size_;
	}

	/**
	 * Fragment to use in a ViewPager
	 */
	public static class ImageFragment extends Fragment {
		public static final String ITEM_SOURCE = "ItemSource";

		private String source_;
		private ImageView fullscreenImageView_;

		/**
		 * Creates an instance of a fragment with image
		 * @param source Image's source
		 */
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
//			ImageLoader.getInstance().displayImage("file://" + source_, fullscreenImageView_);
		}

		@Nullable
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			final View view = inflater.inflate(R.layout.fragment_images_pager_item, container, false);
			fullscreenImageView_ = (ImageView) view.findViewById(R.id.fullscreen_image_view);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				fullscreenImageView_.setTransitionName(source_);
				view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
					@TargetApi(Build.VERSION_CODES.LOLLIPOP)
					@Override
					public boolean onPreDraw() {
						view.getViewTreeObserver().removeOnPreDrawListener(this);
						getActivity().startPostponedEnterTransition();
						return true;
					}
				});
			}
			return view;
		}

		public View getSharedElement() {
			View view = getView().findViewById(R.id.fullscreen_image_view);
			if (isViewInBounds(getView().findViewById(R.id.view_pager_item_root_layout), view)){
				return view;
			}
			return null;
		}

		public static boolean isViewInBounds(View container, View view){
			Rect containerBounds = new Rect();
			container.getHitRect(containerBounds);
			return view.getLocalVisibleRect(containerBounds);
		}
	}
}
