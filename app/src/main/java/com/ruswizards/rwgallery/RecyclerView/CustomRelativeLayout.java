/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 21.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery.RecyclerView;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import com.ruswizards.rwgallery.R;

/**
 * Custom class to intercept touch events
 */
public class CustomRelativeLayout extends RelativeLayout {
	private static final long ANIMATION_DURATION = 100;
	private static final float SCROLL_SENSITIVITY = 0.3f;


	private static enum Direction {
		SIDE, STAND, UP, DOWN
	}

	private static final String LOG_TAG = "TouchListener";

	private int slop_;
	private float initialX_;
	private float initialY_;
	private Direction swipeDirection_;
	private boolean isHidden_;
	private boolean isMoving_;
	private int scrollPointerId_;

	@Override
	public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		// Do not allow child to disallow intercepting
		if (getParent() != null) {
			getParent().requestDisallowInterceptTouchEvent(disallowIntercept);
		}
	}

	public CustomRelativeLayout(Context context) {
		this(context, null);
	}

	public CustomRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		slop_ = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	/**
	 * Monitors touches inside Recycler View. Checks gestures for any actions.
	 *
	 * @param ev Motion event
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = MotionEventCompat.getActionMasked(ev);
		int actionIndex = MotionEventCompat.getActionIndex(ev);
		View menu = findViewById(R.id.expanding_menu_layout);
		switch (action){
			case MotionEvent.ACTION_DOWN:
				// Set initial parameters
				scrollPointerId_ = MotionEventCompat.getPointerId(ev, 0);
				initialX_ = ev.getX() /*+ 0.5f*/;
				initialY_ = ev.getY() /*+ 0.5f*/;
				swipeDirection_ = Direction.STAND;
				break;
			case MotionEventCompat.ACTION_POINTER_DOWN:
				// Set initial parameters on ACTION_POINTER_DOWN
				scrollPointerId_ = MotionEventCompat.getPointerId(ev, actionIndex);
				initialX_= (int) (MotionEventCompat.getX(ev, actionIndex) /*+ 0.5f*/);
				initialY_ = (int) (MotionEventCompat.getY(ev, actionIndex) /*+ 0.5f*/);
				break;
			case MotionEvent.ACTION_MOVE:
				int index = MotionEventCompat.findPointerIndex(ev, scrollPointerId_);
				if (index < 0) {
					Log.e(LOG_TAG, "Error processing scroll; pointer index for id " +
							scrollPointerId_ + " not found. Did any MotionEvents get skipped?");
					return false;
				}
				int x = (int) (MotionEventCompat.getX(ev, index) /*+ 0.5f*/);
				int y = (int) (MotionEventCompat.getY(ev, index) /*+ 0.5f*/);
				float deltaX = x - initialX_;
//				float deltaY = (initialY_ - y) * SCROLL_SENSITIVITY;
				float deltaY = (initialY_ - y) * SCROLL_SENSITIVITY;
				// Check direction of swiping if user just started gesture
				if (swipeDirection_ == Direction.STAND) {
					if (Math.abs(deltaY) > slop_ * 2) {
						if (deltaY > 0) {
							swipeDirection_ = Direction.UP;
							initialY_ = initialY_ - deltaY;
						} else {
							swipeDirection_ = Direction.DOWN;
							initialY_ = initialY_ + deltaY;
						}
						deltaY = (initialY_ - y) * SCROLL_SENSITIVITY;
					}
					/*if (Math.abs(deltaX) > slop_ * 3 ) {
						swipeDirection_ = Direction.SIDE;
					}*/
				}
				// Continue only with vertical gestures
				if (swipeDirection_ != Direction.UP && swipeDirection_ != Direction.DOWN){
					break;
				}
				// Move menu along swiping direction
				int maxTranslation = menu.getHeight();
				if (swipeDirection_ == Direction.UP && !isHidden_){
					if (deltaY >= maxTranslation){
						isHidden_ = true;
						isMoving_ = false;
						menu.setTranslationY(maxTranslation);
					} else {
						menu.setTranslationY(deltaY);
						isMoving_ = true;
					} if (deltaY < 0){
						menu.setTranslationY(0);
					}
				} else if (swipeDirection_ == Direction.DOWN && isHidden_){
					if (deltaY <= -menu.getHeight()){
						isHidden_ = false;
						isMoving_ = false;
						menu.setTranslationY(0);
					} else if (deltaY > 0){
						menu.setTranslationY(maxTranslation);
					} else {
						menu.setTranslationY(maxTranslation + deltaY);
						isMoving_ = true;
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				// Finish incomplete movement
				if (swipeDirection_ == Direction.UP || swipeDirection_ == Direction.DOWN) {
					if (isMoving_) {
						int multiplier;
						if (isHidden_) {
							multiplier = 0;
						} else {
							multiplier = 1;
						}
						isMoving_ = false;
						isHidden_ = !isHidden_;
						menu.animate().translationY(multiplier * menu.getHeight()).setDuration(ANIMATION_DURATION);
					}
				}
				break;
		}
		return false;
	}
}
