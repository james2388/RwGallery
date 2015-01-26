/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 23.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery.RecyclerView;

import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.ruswizards.rwgallery.MainActivity;
import com.ruswizards.rwgallery.R;

/**
 * Scroll listener for RecyclerView
 */
public class RecyclerOnScrollListener extends RecyclerView.OnScrollListener {
	public static final int SWIPE_TO_HIDE_VERTICAL_LIMIT = 60;
	private static final int ANIMATION_DURATION = 300;
	private static final int LIMIT_MULTIPLIER_WHEN_FIRST_VISIBLE = 3;

	private int overallScroll_ = 0;
	private boolean isVisible = true;

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);
		// Count movement only for traceable direction
		if ((isVisible && dy > 0) | (!isVisible && dy < 0)){
			overallScroll_ = overallScroll_ + dy;
		}
		// Check if scrolling is strong enough to hide menu
		if ((isVisible && overallScroll_ > SWIPE_TO_HIDE_VERTICAL_LIMIT) |
				(!isVisible && overallScroll_ < -SWIPE_TO_HIDE_VERTICAL_LIMIT)){
			// Also add multiplier if first item is visible
			if (!(getFirstVisiblePosition(recyclerView) == 0
					&& isVisible
					&& overallScroll_ < LIMIT_MULTIPLIER_WHEN_FIRST_VISIBLE * SWIPE_TO_HIDE_VERTICAL_LIMIT)){
				toggleToolbar(recyclerView);
				isVisible = !isVisible;
				overallScroll_ = 0;
			}
		}
	}

	/**
	 * Toggles bottom toolbar visibility with hiding animation
	 */
	private void toggleToolbar(RecyclerView recyclerView) {
		if (recyclerView.getContext() instanceof MainActivity){
			View toolBar =
					((MainActivity) recyclerView.getContext()).findViewById(R.id.expanding_menu_layout);
			int translation;
			if (isVisible){
				translation = toolBar.getHeight();
			} else {
				translation = 0;
			}
			toolBar.animate().translationY(translation).setDuration(ANIMATION_DURATION);
		}
	}

	@Override
	public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
		switch (newState) {
			case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				// Reset overall scrolling on a new swipe
				overallScroll_ = 0;
				break;
			case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
				ImageLoader.getInstance().resume();
				// Set adapter's extra space to one screen height
				Point size = new Point();
				((MainActivity) recyclerView.getContext()).getWindowManager().getDefaultDisplay().getSize(size);
				changeAdapterExtraSpace(recyclerView, size.y);
				break;
			case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
				ImageLoader.getInstance().pause();
				// Set adapter's extra space to 0
				changeAdapterExtraSpace(recyclerView, 0);
				break;
		}
	}

	/**
	 * Changes RecyclerView adapter's extra space.
	 */
	private void changeAdapterExtraSpace(RecyclerView recyclerView, int extraSpace) {
		if (recyclerView.getLayoutManager() instanceof RecyclerViewFragment.CachingLinearLayoutManager){
			((RecyclerViewFragment.CachingLinearLayoutManager) recyclerView.getLayoutManager()).setExtraLayoutSpace(extraSpace);
		} else if (recyclerView.getLayoutManager() instanceof RecyclerViewFragment.CachingGridLayoutManager){
			((RecyclerViewFragment.CachingGridLayoutManager) recyclerView.getLayoutManager()).setExtraLayoutSpace(extraSpace);
		} else if (recyclerView.getLayoutManager() instanceof RecyclerViewFragment.CachingStaggeredGridLayoutManager){
			((RecyclerViewFragment.CachingStaggeredGridLayoutManager) recyclerView.getLayoutManager()).setExtraLayoutSpace(extraSpace);
		}
	}

	/**
	 * Returns first visible item position from a RecyclerView adapter in consideration of adapter
	 * type.
	 * @return First item index in an adapter. Returns -1 if adapter not identified.
	 */
	private int getFirstVisiblePosition(RecyclerView recyclerView) {
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		if (layoutManager instanceof RecyclerViewFragment.CachingLinearLayoutManager){
			return ((RecyclerViewFragment.CachingLinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
		} else if (layoutManager instanceof RecyclerViewFragment.CachingGridLayoutManager){
			return ((RecyclerViewFragment.CachingGridLayoutManager) layoutManager).findFirstVisibleItemPosition();
		} else if (layoutManager instanceof StaggeredGridLayoutManager){
			return ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null)[0];
		} else {
			return -1;
		}
	}
}
