package com.ruswizards.rwgallery.ViewPager;

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
import com.ruswizards.rwgallery.RecyclerView.RecyclerViewFragment;
import com.ruswizards.rwgallery.ViewPager.util.SystemUiHider;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.ruswizards.rwgallery.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ImagePagerActivity extends FragmentActivity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = false;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 *//*
	private SystemUiHider mSystemUiHider;*/

	public static final String EXTRA_SOURCE_DIRECTORY = "SourceDirectory";
	public static final String EXTRA_IMAGE_SOURCE = "ImageSource";
	public static final String EXTRA_ITEM_NUMBER = "ItemNumber";
	public static final String EXTRA_DATA_SET = "DataSet";

	private int openedItemNumber_;
	private List<GalleryItem> dataSet_;
	private ImagePagerAdapter imagePagerAdapter_;
	private ViewPager imagePager_;
	private SystemUiHider systemUiHider_;
	private GestureDetectorCompat gestureDetector_;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_pager);
		setupActionBar();

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

		final View toolbarView = findViewById(R.id.fullscreen_content_controls);
//		final View contentView = findViewById(R.id.root_layout);
		// TODO: can replace contentview with imagePager_
		final View contentView = imagePager_;
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		systemUiHider_ = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
		systemUiHider_.setup();
		systemUiHider_
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int toolbarHeight;
					int shortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (toolbarHeight == 0) {
								toolbarHeight = toolbarView.getHeight();
							}
							if (shortAnimTime == 0) {
								shortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							toolbarView.animate()
									.translationY(visible ? 0 : toolbarHeight)
									.setDuration(shortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							toolbarView.setVisibility(visible ? View.VISIBLE : View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		/*// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d("---", "onClick");
				if (TOGGLE_ON_CLICK) {
					systemUiHider_.toggle();
				} else {
					systemUiHider_.show();
				}
			}
		});*/

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		gestureDetector_ = new GestureDetectorCompat(this, new ImagesPagerGestureDetector(systemUiHider_));

		findViewById(R.id.dummy_button).setOnTouchListener(delayHideTouchListener_);
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
					.memoryCache(new LRULimitedMemoryCache((int) (Runtime.getRuntime().maxMemory() * RecyclerViewFragment.CACHE_MAX_MEMORY_PERCENTAGE)))
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
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(1000);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener delayHideTouchListener_ = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler hideHandler_ = new Handler();
	Runnable hideRunnable_ = new Runnable() {
		@Override
		public void run() {
			systemUiHider_.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		hideHandler_.removeCallbacks(hideRunnable_);
		hideHandler_.postDelayed(hideRunnable_, delayMillis);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		Log.d("---", "OnDispatchTouchevent");
		gestureDetector_.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}
}
