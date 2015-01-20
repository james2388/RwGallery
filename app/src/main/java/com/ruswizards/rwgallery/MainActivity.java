/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
package com.ruswizards.rwgallery;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedVignetteBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.ruswizards.rwgallery.RecyclerView.RecyclerViewFragment;

import java.io.File;
import java.io.IOException;

/**
 * Main activity of the app
 */
public class MainActivity extends ActionBarActivity {
	private final static float CACHE_MAX_MEMORY_PERCENTAGE = 0.2f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.considerExifParams(true)
				.showImageOnLoading(android.R.drawable.ic_menu_crop)
						.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.resetViewBeforeLoading(true)
				.displayer(new SimpleBitmapDisplayer())
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
		File cacheDir = StorageUtils.getCacheDirectory(this);

		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		int width = size.x / 3;
		int height = size.y / 3 * 2;

		ImageLoaderConfiguration configuration = null;
		try {
			configuration = new ImageLoaderConfiguration.Builder(this)
					.memoryCache(new LRULimitedMemoryCache((int) (Runtime.getRuntime().maxMemory() * CACHE_MAX_MEMORY_PERCENTAGE)))
					.memoryCacheExtraOptions(width, height)
					.diskCache(new LruDiscCache(cacheDir, new HashCodeFileNameGenerator(), 1024 * 1024 * 45))
//					.diskCacheExtraOptions(width, height, null)
					.writeDebugLogs()
					.defaultDisplayImageOptions(defaultOptions)
					.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ImageLoader.getInstance().init(configuration);

		if (savedInstanceState == null){
			// Adding RecyclerView fragment
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
			fragmentTransaction.replace(R.id.content_fragment, recyclerViewFragment);
			fragmentTransaction.commit();
		}
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
}
