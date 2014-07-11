package com.cm.beer.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

public class MediaScanner {
	private MediaScannerConnection mScanner;
	private static String TAG = MediaScanner.class.getName();
	
	/**
	 * 
	 * @param context
	 * @param root
	 */
	public void initScannerService(Context context, final File root) {
		mScanner = new MediaScannerConnection(context,
				new MediaScannerConnection.MediaScannerConnectionClient() {

					@Override
					public void onScanCompleted(String path, Uri uri) {
						if (Logger.isLogEnabled())  Logger.log("onScanCompleted: path: " + path + " uri: "
								+ uri);
					}

					@Override
					public void onMediaScannerConnected() {
						if (Logger.isLogEnabled())  Logger.log("onMediaScannerConnected");
						try {
							scanMedia(root);
						} catch (Throwable e) {
							Log.e(TAG, e.getMessage(), e);
						}
					}
				});
		mScanner.connect();

	}

	private void scanMedia(File root) throws IOException {
		File[] sdImageArray = root.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return ((name.endsWith(".jpg")) || (name.endsWith(".png")));
			}
		});
		if (Logger.isLogEnabled())  Logger.log("total files: " + sdImageArray.length);
		for (int i = 0; i < sdImageArray.length; i++) {
			mScanner.scanFile(sdImageArray[i].getAbsolutePath(), null);
		}
		
//		List<File> files = FileListing.getFileListing(root);
//		if (Logger.isLogEnabled())  Logger.log("total files: " + files.size());
//		String absPath = null;
//		for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
//			File file = iterator.next();
//			absPath = file.getAbsolutePath();
//			if ((absPath.endsWith(".jpg")) || (absPath.endsWith(".png"))) {
//				if (Logger.isLogEnabled())  Logger.log("scan file: " + absPath);
//				mScanner.scanFile(absPath, null);
//			}
//		}
	}

}
