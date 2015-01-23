package com.ruswizards.rwgallery.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 23.01.2015
 * Vladimir Farafonov
 */
public class RecyclerOnScrollListener extends RecyclerView.OnScrollListener {
	public static final int SWIPE_TO_HIDE_VERTICAL_LIMIT = 60;


	private ImageLoader imageLoader_;
	boolean pauseOnScroll_;
	boolean pauseOnFling_;

	private int initialY_ = -1;
	private int watchingPosition_ = 0;
	private boolean isVisible;


	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);

//		CustomRecyclerViewAdapter.ViewHolder viewHolder = (CustomRecyclerViewAdapter.ViewHolder) recyclerView.findViewHolderForPosition(0);

		if (initialY_ < 0) {
			int position = getFirstCompletelyVisiblePosition(recyclerView)+5;
			CustomRecyclerViewAdapter.ViewHolder viewHolder = (CustomRecyclerViewAdapter.ViewHolder) recyclerView.findViewHolderForPosition(position);
			int coordinates[] = new int[2];
			viewHolder.getTitleTextView().getLocationOnScreen(coordinates);
			int currentY_ = coordinates[1] + viewHolder.getTitleTextView().getHeight();
			initialY_ = currentY_;
			watchingPosition_ = position;
		} else {
			// Start watching movement
			CustomRecyclerViewAdapter.ViewHolder viewHolder = (CustomRecyclerViewAdapter.ViewHolder) recyclerView.findViewHolderForPosition(watchingPosition_);
			int deltaY;
			if (viewHolder == null){
				deltaY = SWIPE_TO_HIDE_VERTICAL_LIMIT;
			} else {
				int coordinates[] = new int[2];
				viewHolder.getTitleTextView().getLocationOnScreen(coordinates);
				int currentY_ = coordinates[1] + viewHolder.getTitleTextView().getHeight();
				deltaY = initialY_ - currentY_;
			}
			if (deltaY > SWIPE_TO_HIDE_VERTICAL_LIMIT ){
				if (isVisible){
					Log.d("---", "HIDING");
					isVisible = !isVisible;
				}
			} else if (deltaY < -SWIPE_TO_HIDE_VERTICAL_LIMIT ){
				if (!isVisible){
					Log.d("---", "SHOWING");
					isVisible = !isVisible;
				}
			}
		}
	}

	public RecyclerOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
		imageLoader_ = imageLoader;
		pauseOnScroll_ = pauseOnScroll;
		pauseOnFling_ = pauseOnFling;
	}

	public RecyclerOnScrollListener() {
		super();
	}

	@Override
	public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
		switch (newState) {
			case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
//				imageLoader_.resume();
				Log.d("----", "IDLE");
				initialY_ = -1;
				break;
			case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				/*if (pauseOnScroll_) {
					imageLoader_.pause();
				}*/

				break;
			case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
				/*if (pauseOnFling_) {
					imageLoader_.pause();
				}*/
				break;
		}
	}

	private int getFirstCompletelyVisiblePosition(RecyclerView recyclerView) {
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		if (layoutManager instanceof RecyclerViewFragment.CachingLinearLayoutManager){
			return ((RecyclerViewFragment.CachingLinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
		} else if (layoutManager instanceof RecyclerViewFragment.CachingGridLayoutManager){
			return ((RecyclerViewFragment.CachingGridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
		} else if (layoutManager instanceof StaggeredGridLayoutManager){
			return ((StaggeredGridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPositions(null)[0];
		}
		return 0;
	}
}
