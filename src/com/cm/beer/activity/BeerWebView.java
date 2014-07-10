package com.cm.beer.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.Logger;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class BeerWebView extends Activity {
	String TAG;
	GoogleAnalyticsTracker tracker;
	ProgressDialog dialog;
	WebView webview;
	Activity activity;
	int ACTIVE_DIALOG;
	String title;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate");
		}
		activity = this;

		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		tracker.start(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Instantiated");
		}

		// Let's display the progress in the activity title bar, like the
		// browser app does.
		getWindow().requestFeature(Window.FEATURE_PROGRESS);

		// Start a new thread that will download all the data
		setContentView(R.layout.beer_web_view);
		Bundle extras = getIntent().getExtras();
		String url = (extras != null) ? extras.getString("URL") : null;
		title = (extras != null) ? extras.getString("TITLE") : null;
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate::" + title + "::" + url);
		}

		ImageView backButton = (ImageView) this.findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				finish();
			}
		});

		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new BeerCellarWebViewClient());
		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Activities and WebViews measure progress with different
				// scales.
				// The progress meter will automatically disappear when we reach
				// 100%
				activity.setProgress(progress * 100);
			}
		});
		webview.loadUrl(url);
		showDialog(AppConfig.DIALOG_LOADING_ID);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.app.Activity#onConfigurationChanged(android.content.res.Configuration
	 * )
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// DO NOTHING
		super.onConfigurationChanged(newConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onDestroy");
		}
		// Stop the tracker when it is no longer needed.
		tracker.stop();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Stopped!");
		}
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreateDialog");
		}
		String dialogMessage = null;
		String dialogTitle = null;
		if (id == AppConfig.DIALOG_LOADING_ID) {
			dialogMessage = this.getString(R.string.progress_loading_message);
			ACTIVE_DIALOG = AppConfig.DIALOG_LOADING_ID;
			dialogTitle = title;
		}
		dialog = ProgressDialog.show(this, dialogTitle, dialogMessage, true,
				true);
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// ----------------------------------------------------------------------//
	private class BeerCellarWebViewClient extends WebViewClient {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit
		 * .WebView, java.lang.String)
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onPageStarted::" + url);
			}
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onPageFinished::" + url);
			}
			removeDialog(ACTIVE_DIALOG);
			super.onPageFinished(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onReceivedError::" + failingUrl + "::" + errorCode
						+ "::" + description);
			}
			Toast.makeText(activity, description, Toast.LENGTH_LONG).show();
			tracker.trackEvent("MoreAboutThisBeer", "onReceivedError",
					description.replace(" ", "_"), 0);
			tracker.dispatch();
			super.onReceivedError(view, errorCode, description, failingUrl);
			finish();
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onLoadResource::" + url);
			}
			super.onLoadResource(view, url);
		}
	}
}
