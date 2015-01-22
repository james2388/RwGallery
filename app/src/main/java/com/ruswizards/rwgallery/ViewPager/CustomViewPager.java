package com.ruswizards.rwgallery.ViewPager;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 22.01.2015
 * Vladimir Farafonov
 */
public class CustomViewPager extends ViewPager {
	private GestureDetectorCompat gestureDetector_;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (gestureDetector_ == null){
			gestureDetector_ = new GestureDetectorCompat(getContext(), new ViewPagerGestureDetector());
		}
		gestureDetector_.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	public CustomViewPager(Context context) {
		this(context, null);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public class ViewPagerGestureDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (getContext() instanceof ImagesViewingActivity){
				ImagesViewingActivity activity = (ImagesViewingActivity) getContext();
				activity.toggleUi();
			}
			return false;
		}
	}
}
