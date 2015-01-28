/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery;

import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.ruswizards.rwgallery.RecyclerView.RecyclerViewFragment;

import java.util.List;
import java.util.Map;

/**
 * Main activity of the app
 */
public class MainActivity extends ActionBarActivity {
	public static final String EXTRA_CURRENT_POSITION = "current_item_position";
	public static final String EXTRA_OLD_POSITION = "old_item_position";
	private static final String LOG_TAG = "MainActivity";
	// TODO: add min height to previewImageView

	public void setReentering(boolean isReentering) {
		isReentering_ = isReentering;
	}

	private boolean isReentering_;
	private Bundle tempState_;
	private RecyclerViewFragment recyclerViewFragment_;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onActivityReenter(int resultCode, Intent data) {
		super.onActivityReenter(resultCode, data);
		isReentering_ = true;
		tempState_ = new Bundle(data.getExtras());
		int oldPosition = tempState_.getInt(EXTRA_OLD_POSITION);
		int currentPosition = tempState_.getInt(EXTRA_CURRENT_POSITION);
		if (oldPosition != currentPosition){
			recyclerViewFragment_.getRecyclerView().scrollToPosition(currentPosition);
		}
		postponeEnterTransition();
		recyclerViewFragment_.getRecyclerView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				recyclerViewFragment_.getRecyclerView().getViewTreeObserver().removeOnPreDrawListener(this);
				recyclerViewFragment_.getRecyclerView().requestLayout();
				startPostponedEnterTransition();
				return true;
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null){
			// Adding RecyclerView fragment
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			recyclerViewFragment_ = new RecyclerViewFragment();
			fragmentTransaction.replace(R.id.content_fragment, recyclerViewFragment_);
			fragmentTransaction.commit();
		} else {
			recyclerViewFragment_ = (RecyclerViewFragment) getFragmentManager().findFragmentById(R.id.content_fragment);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			SharedElementCallback sharedElementCallback = initSharedElementCallback(recyclerViewFragment_);
			setExitSharedElementCallback(sharedElementCallback);
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private SharedElementCallback initSharedElementCallback(final RecyclerViewFragment recyclerViewFragment) {
		SharedElementCallback sharedElementCallback = new SharedElementCallback() {
			@Override
			public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
				if (isReentering_){
					int oldPosition = tempState_.getInt(EXTRA_OLD_POSITION);
					int currentPosition = tempState_.getInt(EXTRA_CURRENT_POSITION);
					if (currentPosition != oldPosition){
						String newTransitionName = recyclerViewFragment.getDataSet().get(currentPosition).getSource();
						View newSharedView = recyclerViewFragment.getRecyclerView().findViewWithTag(newTransitionName);
						if (newSharedView != null){
							names.clear();
							names.add(newTransitionName);
							sharedElements.clear();
							sharedElements.put(newTransitionName, newSharedView);
						}
					}
					tempState_ = null;
				}

				View decor = getWindow().getDecorView();
				View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
				View statusBar = decor.findViewById(android.R.id.statusBarBackground);
				int actionBarId = getResources().getIdentifier("action_bar_container", "id", "android");
				View actionBar = decor.findViewById(actionBarId);

				if (!isReentering_){
					if (navigationBar != null){
						names.add(navigationBar.getTransitionName());
						sharedElements.put(navigationBar.getTransitionName(), navigationBar);
					}
					if (statusBar != null){
						names.add(statusBar.getTransitionName());
						sharedElements.put(statusBar.getTransitionName(), navigationBar);
					}
					if (actionBar != null){
						names.add(actionBar.getTransitionName());
						sharedElements.put(actionBar.getTransitionName(), navigationBar);
					}
				} else {
					names.remove(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
					sharedElements.remove(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);

					names.remove(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
					sharedElements.remove(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);

					names.remove("actionBar");
					sharedElements.remove("actionBar");
				}
			}

			@Override
			public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
				Log.d(LOG_TAG, "onSharedElementStart");
			}

			@Override
			public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
				Log.d(LOG_TAG, "onSharedElementStart");
			}
		};
		return sharedElementCallback;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static boolean isLollipop(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}
}
