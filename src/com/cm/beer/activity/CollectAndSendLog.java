package com.cm.beer.activity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.Logger;

public class CollectAndSendLog extends Activity {
	String TAG;

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

		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Instantiated");
		}

		setContentView(R.layout.send_error_report);

		findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (AppConfig.LOGGING_ENABLED) {
					if (Logger.isLogEnabled())  Logger.log("Cancel");
				}
				finish();
			}
		});
		collectAndSendLog();
	}

	private void collectAndSendLog() {
		final PackageManager packageManager = getPackageManager();
		final Intent intent = new Intent(AppConfig.ACTION_SEND_LOG);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		final boolean isInstalled = list.size() > 0;

		if (!isInstalled) {
			new AlertDialog.Builder(this)
					.setTitle(getString(R.string.app_name)).setIcon(
							android.R.drawable.ic_dialog_info).setMessage(
							R.string.install_log_collector_message)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									try {
										Intent marketIntent = new Intent(
												Intent.ACTION_VIEW,
												Uri
														.parse(AppConfig.LOG_COLLECTOR_DETAILS_PAGE_MARKET_URI));
										marketIntent
												.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										startActivity(marketIntent);
										CollectAndSendLog.this.finish();
									} catch (Throwable e) {
										Log.e(TAG, (e.getMessage() != null) ? e
												.getMessage().replace(" ", "_")
												: "", e);
									}

								}
							}).setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									CollectAndSendLog.this.finish();
								}
							}).show();
		} else {
			new AlertDialog.Builder(this)
					.setTitle(getString(R.string.app_name)).setIcon(
							android.R.drawable.ic_dialog_info).setMessage(
							R.string.send_error_report_message)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									intent
											.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.putExtra(
											AppConfig.EXTRA_SEND_INTENT_ACTION,
											Intent.ACTION_SENDTO);
									final String email = AppConfig.ERROR_REPORT_EMAIL;
									intent.putExtra(AppConfig.EXTRA_DATA, Uri
											.parse("mailto:" + email));

									String extraInfo = getString(
											R.string.device_info_fmt,
											getVersionNumber(CollectAndSendLog.this),
											Build.MODEL, Build.VERSION.RELEASE,
											getFormattedKernelVersion(),
											Build.DISPLAY);
									intent.putExtra(
											AppConfig.EXTRA_ADDITIONAL_INFO,
											extraInfo);
									intent
											.putExtra(
													Intent.EXTRA_SUBJECT,
													getString(R.string.app_name)
															+ " Application Failure Report");

									intent.putExtra(AppConfig.EXTRA_FORMAT,
											"time");

									// The log can be filtered to contain data
									// relevant only to your app
									/*
									 * String[] filterSpecs = new String[3];
									 * filterSpecs[0] = "AndroidRuntime:E";
									 * filterSpecs[1] = TAG + ":V";
									 * filterSpecs[2] = "*:S";
									 * intent.putExtra(AppConfig
									 * .EXTRA_FILTER_SPECS, filterSpecs);
									 */

									startActivity(intent);
									CollectAndSendLog.this.finish();
								}
							}).setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									CollectAndSendLog.this.finish();
								}
							}).show();
		}
	}

	private static String getVersionNumber(Context context) {
		String version = "?";
		try {
			PackageInfo packagInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			version = packagInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
		}
		;

		return version;
	}

	private String getFormattedKernelVersion() {
		String procVersionStr;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"/proc/version"), 256);
			try {
				procVersionStr = reader.readLine();
			} finally {
				reader.close();
			}

			final String PROC_VERSION_REGEX = "\\w+\\s+" + /* ignore: Linux */
			"\\w+\\s+" + /* ignore: version */
			"([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
			"\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /*
														 * group 2:
														 * (xxxxxx@xxxxx
														 * .constant)
														 */
			"\\([^)]+\\)\\s+" + /* ignore: (gcc ..) */
			"([^\\s]+)\\s+" + /* group 3: #26 */
			"(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
			"(.+)"; /* group 4: date */

			Pattern p = Pattern.compile(PROC_VERSION_REGEX);
			Matcher m = p.matcher(procVersionStr);

			if (!m.matches()) {
				Log.e(TAG, "Regex did not match on /proc/version: "
						+ procVersionStr);
				return "Unavailable";
			} else if (m.groupCount() < 4) {
				Log.e(TAG, "Regex match on /proc/version only returned "
						+ m.groupCount() + " groups");
				return "Unavailable";
			} else {
				return (new StringBuilder(m.group(1)).append("\n").append(
						m.group(2)).append(" ").append(m.group(3)).append("\n")
						.append(m.group(4))).toString();
			}
		} catch (IOException e) {
			Log
					.e(
							TAG,
							"IO Exception when getting kernel version for Device Info screen",
							e);

			return "Unavailable";
		}
	}

}
