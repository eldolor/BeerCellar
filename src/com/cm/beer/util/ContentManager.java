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
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ContentManager {
	private final Map<String, SoftReference<String>> mContentCache;
	/**
	 * Used as a locking mechanism to prevent multithreaded fetches for the same
	 * image
	 */
	private final Map<String, String> mLockCache;
	private static ContentManager mContentManager;

	private ContentManager() {
		mContentCache = new HashMap<String, SoftReference<String>>();
		mLockCache = new HashMap<String, String>();
	}

	/**
	 * 
	 * @return
	 */
	public static synchronized ContentManager getInstance() {
		if (mContentManager == null) {
			mContentManager = new ContentManager();
		}
		return mContentManager;
	}

	/**
	 * 
	 */
	public void clear() {
		if (mLockCache != null) {
			Log.d(this.getClass().getName(), "Lock Cache Cleared!");
			mLockCache.clear();
		}
		if (mContentCache != null) {
			Log.d(this.getClass().getName(), "Content Cache Cleared!");
			mContentCache.clear();
		}
	}

	/**
	 * Removes the specific content from cache
	 * 
	 * @param urlString
	 */
	public void removeContent(String urlString) {
		if (mContentCache != null) {
			Log.d(this.getClass().getName(), "Content Cache Cleared for "
					+ urlString);
			mContentCache.remove(urlString);
		}
	}

	/**
	 * 
	 * @param urlString
	 * @return
	 */
	public String fetchContent(String urlString) {
		if (mContentCache.containsKey(urlString)) {
			Log.d(this.getClass().getName(), "Returning Content from Cache:"
					+ urlString);
			SoftReference<String> softReference = mContentCache.get(urlString);
			if ((softReference==null)||(softReference.get() == null)) {
				mContentCache.remove(urlString);
				Log.d(this.getClass().getName(), "fetchContent():Soft Reference has been Garbage Collected:"
						+ urlString);
			} else {
				return softReference.get();
			}
		}

		Log.d(this.getClass().getName(), "url:" + urlString);
		try {
			// prevents multithreaded fetches for the same url
			mLockCache.put(urlString, urlString);
			Log.d(this.getClass().getName(), "Begin Downloading:" + urlString);
			InputStream is = fetch(urlString);
			Log.d(this.getClass().getName(), "End Downloading:" + urlString);

			String result = Util.convertStreamToString(is, "UTF-8");

			mContentCache.put(urlString, new SoftReference<String>(result) );
			mLockCache.remove(urlString);

			Log.d(this.getClass().getName(), result);

			return result;
		} catch (Throwable e) {
			Log.e(this.getClass().getName(), "fetchDrawable failed", e);
			return null;
		}
	}

	/**
	 * 
	 * @param urlString
	 * @param imageView
	 */
	public void fetchContentOnThread(final String urlString,
			final Handler handler) {
		if (mContentCache.containsKey(urlString)) {
			SoftReference<String> softReference = mContentCache.get(urlString);

			if ((softReference==null)||(softReference.get() == null)) {
				mContentCache.remove(urlString);
				Log.d(this.getClass().getName(), "fetchContentOnThread():Soft Reference has been Garbage Collected:"
						+ urlString);
			} else {
				String jsonStr = softReference.get();
				Log.d(this.getClass().getName(), urlString + "=>" + jsonStr);
				Message message = handler.obtainMessage(1, jsonStr);
				handler.sendMessage(message);
				return;
			}
		}

		Thread thread = new Thread() {
			@Override
			public void run() {
				while (mLockCache.containsKey(urlString)) {
					Log.d(this.getClass().getName(),
							"URI download request in progress:" + urlString);
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						Log.e(this.getClass().getName(), e.getMessage());
					}
				}
				String content = fetchContent(urlString);
				Message message = handler.obtainMessage(1, content);
				handler.sendMessage(message);
			}
		};
		thread.start();
	}

	/**
	 * 
	 * @param urlString
	 * @param imageView
	 */
	public void fetchContentOnThread(final String urlString) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (mLockCache.containsKey(urlString)) {
					Log.d(this.getClass().getName(),
							"URI download request in progress:" + urlString);
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						Log.e(this.getClass().getName(), e.getMessage());
					}
				}
			}
		};
		thread.start();
	}

	/**
	 * 
	 * @param urlStringKey
	 * @param content
	 */
	public void setContent(String urlStringKey, String content) {
		mContentCache.put(urlStringKey, new SoftReference<String>(content));
	}

	/**
	 * 
	 * @param urlString
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public InputStream fetch(String urlString) throws MalformedURLException,
			IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet(urlString);
		HttpResponse response = httpClient.execute(request);
		return response.getEntity().getContent();
	}


}
