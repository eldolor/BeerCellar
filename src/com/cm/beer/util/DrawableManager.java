package com.cm.beer.util;

/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.    
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class DrawableManager {
	private final Map<String, SoftReference<Drawable>> mDrawableCache;
	/**
	 * Used as a locking mechanism to prevent multithreaded fetches for the same
	 * image
	 */
	private final Map<String, String> mLockCache;
	private static DrawableManager mDrawableManager;

	private DrawableManager() {
		mDrawableCache = new HashMap<String, SoftReference<Drawable>>();
		mLockCache = new HashMap<String, String>();
	}

	/**
	 * 
	 * @return
	 */
	public static synchronized DrawableManager getInstance() {
		if (mDrawableManager == null) {
			mDrawableManager = new DrawableManager();
		}
		return mDrawableManager;
	}

	/**
	 * Removes the specific content from cache
	 * 
	 * @param urlString
	 */
	public void removeContent(String urlString) {
		if (mDrawableCache != null) {
			if (Logger.isLogEnabled())  Logger.log( "Content Cache Cleared for "
					+ urlString);
			mDrawableCache.remove(urlString);
		}
	}


	/**
	 * 
	 */
	public void clear() {
		if (mDrawableCache != null) {
			mDrawableCache.clear();
			if (Logger.isLogEnabled())  Logger.log( "Drawable Cache Cleared!");
		}
		if (mLockCache != null) {
			mLockCache.clear();
			if (Logger.isLogEnabled())  Logger.log( "Lock Cache Cleared!");
		}
	}

	/**
	 * 
	 * @param urlString
	 * @return
	 */
	public Drawable fetchDrawable(String urlString) {
		if (mDrawableCache.containsKey(urlString)) {
			if (Logger.isLogEnabled())  Logger.log( "Returning Drawable from Cache:"
					+ urlString);
			SoftReference<Drawable> softReference = mDrawableCache
					.get(urlString);
			if ((softReference==null)||(softReference.get() == null)) {
				mDrawableCache.remove(urlString);
				if (Logger.isLogEnabled())  Logger.log( "fetchDrawable():Soft Reference has been Garbage Collected:"
						+ urlString);
			} else {
				return softReference.get();
			}
		}

		if (Logger.isLogEnabled())  Logger.log( "image url:" + urlString);
		try {
			// prevents multithreaded fetches for the same image
			mLockCache.put(urlString, urlString);
			if (Logger.isLogEnabled())  Logger.log( "Begin Downloading:" + urlString);
			InputStream is = fetch(urlString);
			if (Logger.isLogEnabled())  Logger.log( "End Downloading:" + urlString);
			Drawable drawable = Drawable.createFromStream(is, "src");
			mDrawableCache
					.put(urlString, new SoftReference<Drawable>(drawable));
			mLockCache.remove(urlString);
			if (Logger.isLogEnabled())  Logger.log( "got a thumbnail drawable: "
					+ drawable.getBounds() + ", "
					+ drawable.getIntrinsicHeight() + ","
					+ drawable.getIntrinsicWidth() + ", "
					+ drawable.getMinimumHeight() + ","
					+ drawable.getMinimumWidth());
			return drawable;
		} catch (Throwable e) {
			Log.e(this.getClass().getName(), "fetchDrawable failed", e);
			return null;
		}
	}

	
	/**
	 * 
	 * @param urlString
	 * @return
	 */
	public byte[] fetchDrawableAsByteArray(String urlString) {
		byte[] bitmapdata = null;
		
		if (mDrawableCache.containsKey(urlString)) {
			if (Logger.isLogEnabled())  Logger.log( "Returning Drawable from Cache:"
					+ urlString);
			SoftReference<Drawable> softReference = mDrawableCache
					.get(urlString);
			if ((softReference==null)||(softReference.get() == null)) {
				mDrawableCache.remove(urlString);
				if (Logger.isLogEnabled())  Logger.log( "fetchDrawable():Soft Reference has been Garbage Collected:"
						+ urlString);
			} else {
				Drawable drawable =  softReference.get();
				Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				bitmapdata = stream.toByteArray();
			}
		}

		if (Logger.isLogEnabled())  Logger.log( "image url:" + urlString);
		try {
			// prevents multithreaded fetches for the same image
			mLockCache.put(urlString, urlString);
			if (Logger.isLogEnabled())  Logger.log( "Begin Downloading:" + urlString);
			InputStream is = fetch(urlString);
			if (Logger.isLogEnabled())  Logger.log( "End Downloading:" + urlString);
			Drawable drawable = Drawable.createFromStream(is, "src");
			mDrawableCache
					.put(urlString, new SoftReference<Drawable>(drawable));
			mLockCache.remove(urlString);
			if (Logger.isLogEnabled())  Logger.log( "got a thumbnail drawable: "
					+ drawable.getBounds() + ", "
					+ drawable.getIntrinsicHeight() + ","
					+ drawable.getIntrinsicWidth() + ", "
					+ drawable.getMinimumHeight() + ","
					+ drawable.getMinimumWidth());
			
			Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			bitmapdata = stream.toByteArray();
			
		} catch (Throwable e) {
			Log.e(this.getClass().getName(), "fetchDrawable failed", e);
			return null;
		}
		return bitmapdata;
	}

	/**
	 * 
	 * @param urlString
	 * @param imageView
	 */
	public void fetchDrawableOnThread(final String urlString,
			final ImageView imageView) {
		if (mDrawableCache.containsKey(urlString)) {
			if (Logger.isLogEnabled())  Logger.log( "Returning Drawable from Cache:"
					+ urlString);
			SoftReference<Drawable> softReference = mDrawableCache
					.get(urlString);
			if ((softReference==null)||(softReference.get() == null)) {
				mDrawableCache.remove(urlString);
				if (Logger.isLogEnabled())  Logger.log( "fetchDrawableOnThread():Soft Reference has been Garbage Collected:"
						+ urlString);
			} else {
				imageView.setImageDrawable(softReference.get());
				return;
			}

		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				imageView.setImageDrawable((Drawable) message.obj);
			}
		};

		Thread thread = new Thread() {
			@Override
			public void run() {
				while (mLockCache.containsKey(urlString)) {
					if (Logger.isLogEnabled())  Logger.log(
							"URI download request in progress:" + urlString);
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						Log.e(this.getClass().getName(), e.getMessage());
					}
				}
				Drawable drawable = fetchDrawable(urlString);
				Message message = handler.obtainMessage(1, drawable);
				handler.sendMessage(message);
			}
		};
		thread.start();
	}

	/**
	 * 
	 * @param urlString
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private InputStream fetch(String urlString) throws MalformedURLException,
			IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet(urlString);
		HttpResponse response = httpClient.execute(request);
		return response.getEntity().getContent();
	}

}
