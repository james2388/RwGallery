/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 21.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery.ViewPager;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.ruswizards.rwgallery.GalleryItem;
import com.ruswizards.rwgallery.MainActivity;
import com.ruswizards.rwgallery.R;
import com.ruswizards.rwgallery.RecyclerView.RecyclerViewFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for fullscreen image watching
 */
public class ImagesViewingActivity extends FragmentActivity {
	public static final String EXTRA_SOURCE_DIRECTORY = "SourceDirectory";
	public static final String EXTRA_ITEM_NUMBER = "ItemNumber";
	private static final long TOOLBAR_HIDE_ANIM_DURATION = 200;
	private static final int HIDE_DELAY_FIRST_RUN = 1000;

	private boolean isUiHidden_;
	private Handler handler_;
	private List<GalleryItem> dataSet_;
	private int openedItemNumber_;
	private ViewPager imagePager_;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set up initialization parameters for hiding UI elements
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			View decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(uiOptions);
		} else {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		}
		setContentView(R.layout.activity_images_viewing);
		handler_ = new Handler();
		delayedHide(HIDE_DELAY_FIRST_RUN);							// Auto hide UI after some time
		if (savedInstanceState == null) {
			// Get data from parent activity
			dataSet_ = new ArrayList<>();
			openedItemNumber_ = getIntent().getIntExtra(EXTRA_ITEM_NUMBER, -1);
			String sourceDirectory = getIntent().getStringExtra(EXTRA_SOURCE_DIRECTORY);
			// Fill data in a list
			RecyclerViewFragment.fillDataSet(sourceDirectory, dataSet_, false);
		}
		setTitle(dataSet_.get(openedItemNumber_).getTitle());
		// Set up ViewPager and adapter
		ImagePagerAdapter imagePagerAdapter_ =
				new ImagePagerAdapter(getSupportFragmentManager(), dataSet_.size(), dataSet_);
		imagePager_ = (ViewPager) findViewById(R.id.images_view_pager);
		imagePager_.setAdapter(imagePagerAdapter_);
		imagePager_.setCurrentItem(openedItemNumber_);
		imagePager_.setOffscreenPageLimit(2);
		imagePager_.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			@Override
			public void onPageSelected(int position) {
				// Change title after image is opened
				setTitle(dataSet_.get(position).getTitle());
			}
			@Override
			public void onPageScrollStateChanged(int state) {}
		});
	}

	/**
	 * Initialization of ImageLoader
	 */
	private void initializeImageLoader() {
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(false)
				.cacheOnDisk(false)
				.considerExifParams(true)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.displayer(new SimpleBitmapDisplayer())
				.build();
		File cacheDir = new File(StorageUtils.getCacheDirectory(this).getAbsolutePath() + "/ImagesPager/");
		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		int width = size.x;
		int height = size.y;
		int side;
		if (width > height){
			side = width;
		} else {
			side = height;
		}
		ImageLoaderConfiguration configuration = null;
		try {
			configuration = new ImageLoaderConfiguration.Builder(this)
					.memoryCache(new LRULimitedMemoryCache(
							(int) (Runtime.getRuntime().maxMemory() * RecyclerViewFragment.CACHE_MAX_MEMORY_PERCENTAGE)))
//					.memoryCacheExtraOptions(width, height)
					.memoryCacheExtraOptions(side, side)
					.diskCache(new LruDiscCache(cacheDir, new HashCodeFileNameGenerator(), 1024 * 1024 * 100))
//					.diskCacheExtraOptions(width, height, null)
					.diskCacheExtraOptions(side, side, null)
					.writeDebugLogs()
					.defaultDisplayImageOptions(defaultOptions)
					.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ImageLoader.getInstance().init(configuration);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Runnable to toggle UI elements
	 */
	Runnable toggleRunnable_ = new Runnable() {
		@Override
		public void run() {
			toggleUi();
		}
	};

	/**
	 * Schedules a call to {@link #toggleUi()}.
	 */
	private void delayedHide(int delayMillis) {
		handler_.removeCallbacks(toggleRunnable_);
		handler_.postDelayed(toggleRunnable_, delayMillis);
	}

	/**
	 * Toggles UI visibility with animation
	 */
	public void toggleUi() {
		View toolBarView = findViewById(R.id.bottom_toolbar_layout);
		float toolBarTranslation;
		// Toggle action bar and set up parameters for toggling bottom bar depending on Android API level
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			int uiOptions;
			if (isUiHidden_){
				getActionBar().show();
				uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
				toolBarTranslation = 0;
			} else {
				getActionBar().hide();
				uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN |
						View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
				toolBarTranslation = toolBarView.getHeight();
			}
			View decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(uiOptions);
		} else {
			if (isUiHidden_){
				getActionBar().show();
				toolBarTranslation = 0;
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				getActionBar().hide();
				toolBarTranslation = toolBarView.getHeight();
				getWindow().setFlags(
						WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		}
		// Animate toolbar toggling
		toolBarView.animate().translationY(toolBarTranslation).setDuration(TOOLBAR_HIDE_ANIM_DURATION);
		isUiHidden_ = !isUiHidden_;
	}

	public void onClick(View view) {
		switch (view.getId()){
			case R.id.share_image_button:
				// Opens app choosing dialog for sharing image
				File sharingItem = new File(dataSet_.get(imagePager_.getCurrentItem()).getSource());
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sharingItem));
				intent.setType("image/jpeg");
				startActivity(Intent.createChooser(intent, getResources().getText(R.string.share)));
				break;
		}
	}

	@Override
	protected void onDestroy() {
		if (ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().destroy();
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeImageLoader();
	}
}
