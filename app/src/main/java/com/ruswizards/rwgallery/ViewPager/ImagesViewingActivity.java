package com.ruswizards.rwgallery.ViewPager;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.WindowManager;

import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.ruswizards.rwgallery.GalleryItem;
import com.ruswizards.rwgallery.MainActivity;
import com.ruswizards.rwgallery.R;
import com.ruswizards.rwgallery.RecyclerView.RecyclerViewFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ImagesViewingActivity extends FragmentActivity {
	public static final String EXTRA_SOURCE_DIRECTORY = "SourceDirectory";
	public static final String EXTRA_ITEM_NUMBER = "ItemNumber";
	private static final long TOOLBAR_HIDE_ANIM_DURATION = 200;

	private GestureDetectorCompat gestureDetector_;
	private boolean isUiHidden_;
	private Handler handler_;
	private List<GalleryItem> dataSet_;
	private int openedItemNumber_;
	private ImagePagerAdapter imagePagerAdapter_;
	private ViewPager imagePager_;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			View decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(uiOptions);
		} else {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		}
		setContentView(R.layout.activity_images_viewing);



		gestureDetector_ = new GestureDetectorCompat(this, new ImagesViewingGestureDetector());
		handler_ = new Handler();
		delayedHide(1000);

		initializeImageLoader();

		if (savedInstanceState == null) {
			dataSet_ = new ArrayList<>();
			openedItemNumber_ = getIntent().getIntExtra(EXTRA_ITEM_NUMBER, -1);
			String sourceDirectory = getIntent().getStringExtra(EXTRA_SOURCE_DIRECTORY);
			RecyclerViewFragment.fillDataSet(sourceDirectory, dataSet_, false);
		} else {
			// TODO: add saving state
		}
		setTitle(dataSet_.get(openedItemNumber_).getTitle());

		imagePagerAdapter_ = new ImagePagerAdapter(getSupportFragmentManager(), dataSet_.size(), dataSet_);
		imagePager_ = (ViewPager) findViewById(R.id.images_view_pager);
		imagePager_.setAdapter(imagePagerAdapter_);
		imagePager_.setCurrentItem(openedItemNumber_);
		imagePager_.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

			@Override
			public void onPageSelected(int position) {
				setTitle(dataSet_.get(position).getTitle());
			}

			@Override
			public void onPageScrollStateChanged(int state) {}
		});
	}

	private void initializeImageLoader() {
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(false)
				.cacheOnDisk(false)
				.considerExifParams(true)
//				.showImageOnLoading(android.R.drawable.ic_menu_crop)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.displayer(new FadeInBitmapDisplayer(200))
				.build();
		File cacheDir = new File(StorageUtils.getCacheDirectory(this).getAbsolutePath() + "/ImagesPager/");
		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		int width = size.x;
		int height = size.y;

//		int side = width > height ? width : height;
		int side;
		if (width > height){
			side = width;
		} else {
			side = height;
		}
		ImageLoaderConfiguration configuration = null;
		try {
			configuration = new ImageLoaderConfiguration.Builder(this)
					.memoryCache(new LRULimitedMemoryCache((int) (Runtime.getRuntime().maxMemory() * MainActivity.CACHE_MAX_MEMORY_PERCENTAGE)))
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
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	Runnable toggleRunnable_ = new Runnable() {
		@Override
		public void run() {
			toggleUi();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		handler_.removeCallbacks(toggleRunnable_);
		handler_.postDelayed(toggleRunnable_, delayMillis);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		Log.d("---", "OnDispatchTouchevent");
		gestureDetector_.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	private void toggleUi() {
		View toolBarView;
		toolBarView = findViewById(R.id.fullscreen_content_controls);
		float toolBarTranslation;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			int uiOptions;
			if (isUiHidden_){
				getActionBar().show();
				uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
				toolBarTranslation = 0;
			} else {
				getActionBar().hide();
				uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
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
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		}
		toolBarView.animate().translationY(toolBarTranslation).setDuration(TOOLBAR_HIDE_ANIM_DURATION);
		isUiHidden_ = !isUiHidden_;
	}

	public class ImagesViewingGestureDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			toggleUi();
			return false;
		}
	}
}
