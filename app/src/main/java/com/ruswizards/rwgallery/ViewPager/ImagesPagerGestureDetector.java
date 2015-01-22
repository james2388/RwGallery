package com.ruswizards.rwgallery.ViewPager;

import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.ruswizards.rwgallery.ViewPager.util.SystemUiHider;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 22.01.2015
 * Vladimir Farafonov
 */
public class ImagesPagerGestureDetector extends GestureDetector.SimpleOnGestureListener {
	private SystemUiHider systemUiHider_;

	public ImagesPagerGestureDetector(SystemUiHider systemUiHider) {
		super();
		systemUiHider_ = systemUiHider;
	}



	public ImagesPagerGestureDetector(Handler handler) {
		super();
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
//		systemUiHider_.toggle();
		Log.d("---", "Single tap");
		return super.onSingleTapUp(e);
	}
}
