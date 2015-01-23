package com.ruswizards.rwgallery.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.AbsListView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 23.01.2015
 * Vladimir Farafonov
 */
public class RecyclerOnScrollListener extends RecyclerView.OnScrollListener {
	private ImageLoader imageLoader_;
	boolean pauseOnScroll_;
	boolean pauseOnFling_;

	public RecyclerOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
		imageLoader_ = imageLoader;
		pauseOnScroll_ = pauseOnScroll;
		pauseOnFling_ = pauseOnFling;
	}

	@Override
	public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
		switch (newState) {
			case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
				imageLoader_.resume();
				Log.d("---", "IDLE");
				break;
			case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				Log.d("---", "SCROLL");
				if (pauseOnScroll_) {
					Log.d("---", "pause");
					imageLoader_.pause();
				}
				break;
			case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
				Log.d("---", "FLing");

				if (pauseOnFling_) {
					Log.d("---", "pause");
					imageLoader_.pause();
				}
				break;
		}
	}
}
