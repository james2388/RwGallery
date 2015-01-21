package com.ruswizards.rwgallery.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.ruswizards.rwgallery.R;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 20.01.2015
 * Vladimir Farafonov
 */
public class TouchListener implements RecyclerView.OnItemTouchListener {

	private static final long ANIMATION_DURATION = 500;

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

	public TouchListener(int slop) {
		slop_ = slop;
	}

	@Override
	public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
		Log.d(LOG_TAG, "INTERCEPT");
		int action = e.getActionMasked();
		View menu = rv.findViewById(R.id.expanding_menu_layout);
		switch (action){
			case MotionEvent.ACTION_DOWN:
				initialX_ = e.getX();
				initialY_ = e.getY();
				swipeDirection_ = Direction.STAND;
				isMoving_ = true;
				break;
			case MotionEvent.ACTION_MOVE:


				float deltaX = e.getX() - initialX_;
				float deltaY = e.getY() - initialY_;
				if (swipeDirection_ == Direction.STAND) {
					if (Math.abs(deltaY) > slop_) {
						if (deltaY > 0) {
							swipeDirection_ = Direction.UP;
						} else {
							swipeDirection_ = Direction.DOWN;
						}
					}
					if (Math.abs(deltaX) > slop_) {
						swipeDirection_ = Direction.SIDE;
					}
				}

				int maxTranslation = menu.getHeight();
				if (swipeDirection_ == Direction.UP && !isHidden_){
					if (Math.abs(deltaY) >= maxTranslation){
						isHidden_ = true;
						isMoving_ = false;
						menu.setTranslationY(maxTranslation);
					} else {
						menu.setTranslationY(deltaY);
					}
				} else if (swipeDirection_ == Direction.DOWN && isHidden_){
					if (Math.abs(deltaY) >= menu.getHeight()){
						isHidden_ = false;
						isMoving_ = false;
						menu.setTranslationY(0);
					} else {
						menu.setTranslationY(maxTranslation - deltaY);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if (swipeDirection_ == Direction.UP || swipeDirection_ == Direction.DOWN) {
					if (isMoving_) {
						int multiplier;
						if (isHidden_) {
							multiplier = 1;
						} else {
							multiplier = 0;
						}
						menu.animate().translationY(multiplier * menu.getHeight()).setDuration(ANIMATION_DURATION);
					}
				}
				break;
		}
		return false;
	}

	@Override
	public void onTouchEvent(RecyclerView rv, MotionEvent e) {
	}
}
