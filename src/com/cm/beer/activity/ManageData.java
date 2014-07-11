package com.cm.beer.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.util.Logger;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ManageData extends Activity {

	String TAG;
	ImageView exportDbToSdButton;
	ImageView restoreDbButton;
	TextView lastBackup;
	SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");

	static final int MENU_GROUP = 0;
	static final int SEND_ERROR_REPORT_ID = Menu.FIRST;

	GoogleAnalyticsTracker tracker;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate: ");
		}

		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		tracker.start(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Instantiated");
		}

		this.setContentView(R.layout.manage_data);

		lastBackup = (TextView) this.findViewById(R.id.last_backup);
		lastBackup.setText(getLastBackupTimestamp());

		this.exportDbToSdButton = (ImageView) this
				.findViewById(R.id.exportdbtosdbutton);
		this.exportDbToSdButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				if (ManageData.this.isExternalStorageAvail()) {
					new ExportDatabaseFileTask().execute();
				} else {
					Toast
							.makeText(
									ManageData.this,
									getString(R.string.external_storage_not_available_message),
									Toast.LENGTH_SHORT).show();
				}
			}
		});

		this.restoreDbButton = (ImageView) this
				.findViewById(R.id.restoreDbButton);
		this.restoreDbButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				if (AppConfig.LOGGING_ENABLED) {
					if (Logger.isLogEnabled())  Logger.log("Restore Database");
				}
				if (ManageData.this.isExternalStorageAvail()) {
					if (AppConfig.LOGGING_ENABLED) {
						Log.i(TAG,
								"Restore Database:External Storage Available");
					}
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(
							ManageData.this);
					alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
					alertDialog.setTitle(R.string.restore_note_dialog_title);
					alertDialog
							.setMessage(R.string.restore_note_dialog_message);
					alertDialog.setPositiveButton(R.string.yes_label,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked OK so do some stuff */
									new RestoreDatabaseFileTask().execute();
								}
							});
					alertDialog.setNegativeButton(R.string.no_label,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									/* User clicked Cancel so do some stuff */
								}
							});
					alertDialog.create();
					alertDialog.show();

				} else {
					Toast
							.makeText(
									ManageData.this,
									getString(R.string.external_storage_not_available_message),
									Toast.LENGTH_SHORT).show();
				}
			}
		});
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
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreateOptionsMenu");
		}
		super.onCreateOptionsMenu(menu);
		int position = 0;
		if (AppConfig.DEFAULT_APPSTORE.equals(AppConfig.GOOGLE_APPSTORE)) {
			menu.add(MENU_GROUP, SEND_ERROR_REPORT_ID, position++,
					R.string.menu_send_error_report);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onMenuItemSelected");
		}
		switch (item.getItemId()) {
		case SEND_ERROR_REPORT_ID:
			sendErrorReport();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * 
	 */
	private void sendErrorReport() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("Send Error Report");
		}
		Intent intent = new Intent(ManageData.this.getApplication(),
				CollectAndSendLog.class);
		// intent.putExtra("LOCATION", selectedLocation);
		startActivity(intent);

	}

	/**
	 * 
	 * @return
	 */
	private String getLastBackupTimestamp() {
		File dbFile = new File(Environment.getDataDirectory()
				+ AppConfig.DATABASE_INTERNAL_LOCATION
				+ NotesDbAdapter.DATABASE_NAME);

		File backupDir = new File(AppConfig.BACKUP_DIR);

		File backupFile = new File(backupDir, dbFile.getName());
		if (backupFile != null && backupFile.exists()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Last backup was on "
					+ dateFormat.format(backupFile.lastModified()));
			return sb.toString();
		} else {
			return null;
		}
	}

	private boolean isExternalStorageAvail() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	private class ExportDatabaseFileTask extends
			AsyncTask<String, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(
				ManageData.this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog
					.setMessage(getString(R.string.backing_up_database_message));
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {

			File dbFile = new File(Environment.getDataDirectory()
					+ AppConfig.DATABASE_INTERNAL_LOCATION
					+ NotesDbAdapter.DATABASE_NAME);

			File baseDir = new File(AppConfig.BASE_APP_DIR);
			File backupDir = new File(AppConfig.BACKUP_DIR);

			createDirectories(baseDir, backupDir);
			File file = new File(backupDir, dbFile.getName());

			try {
				file.createNewFile();
				this.copyFile(dbFile, file);
				tracker.trackEvent("ManageData", "ExportDatabaseFile", "Y", 0);
				tracker.dispatch();
				return true;
			} catch (IOException e) {
				Log.e(TAG, (e.getMessage() != null) ? e.getMessage().replace(
						" ", "_") : "", e);
				tracker.trackEvent("ManageData", "ExportDatabaseFileError", ((e
						.getMessage() != null) ? e.getMessage().replace(" ",
						"_") : ""), 0);
				tracker.dispatch();
				return false;
			}
		}

		/**
		 * 
		 * @param src
		 * @param dst
		 * @throws IOException
		 */
		void copyFile(File src, File dst) throws IOException {
			FileChannel inChannel = new FileInputStream(src).getChannel();
			FileChannel outChannel = new FileOutputStream(dst).getChannel();
			try {
				inChannel.transferTo(0, inChannel.size(), outChannel);
			} finally {
				if (inChannel != null)
					inChannel.close();
				if (outChannel != null)
					outChannel.close();
			}
		}

		// can use UI thread here
		protected void onPostExecute(final Boolean success) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (success) {
				lastBackup.setText(getLastBackupTimestamp());
				Toast.makeText(ManageData.this,
						R.string.database_backedup_successfully_message,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(ManageData.this,
						R.string.database_backup_failed_message,
						Toast.LENGTH_SHORT).show();
			}
		}

		/**
		 * Creates appropriate directories if necessary
		 * 
		 * @param baseDir
		 * @param picturesDir
		 * @param thumbnailsDir
		 */
		private void createDirectories(File baseDir, File backupDir) {
			// 1. Base dir does not exist
			if (baseDir != null && (!baseDir.exists())) {
				// 1.1 Create new base dir
				if (baseDir.mkdir()) {
					if (AppConfig.LOGGING_ENABLED) {
						if (Logger.isLogEnabled())  Logger.log("doInBackground::Created "
								+ baseDir.getPath());
					}
					// 1.2 Create new backup dir
					if (backupDir.mkdir()) {
						if (AppConfig.LOGGING_ENABLED) {
							if (Logger.isLogEnabled())  Logger.log("doInBackground::Created "
									+ backupDir.getPath());
						}
					} else {
						if (AppConfig.LOGGING_ENABLED) {
							Log.e(TAG, "doInBackground::Unable to create "
									+ backupDir.getPath());
						}
						setResult(RESULT_CANCELED);
						finish();
					}
				} else {
					if (AppConfig.LOGGING_ENABLED) {
						Log.e(TAG, "doInBackground::Unable to create "
								+ baseDir.getPath());
					}
					setResult(RESULT_CANCELED);
					finish();
				}
			} else if (baseDir != null && baseDir.exists()) {
				// 2. Base dir exists
				// 2.1 Backup dir does not exist
				if (backupDir != null && (!backupDir.exists())) {
					// 2.1.1 create new backup dir
					if (backupDir.mkdir()) {
						if (AppConfig.LOGGING_ENABLED) {
							if (Logger.isLogEnabled())  Logger.log("doInBackground::Created "
									+ backupDir.getPath());
						}
					} else {
						if (AppConfig.LOGGING_ENABLED) {
							Log.e(TAG, "doInBackground::Unable to create "
									+ backupDir.getPath());
						}
						setResult(RESULT_CANCELED);
						finish();
					}
				}
			}

		}

	}

	private class RestoreDatabaseFileTask extends
			AsyncTask<String, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(
				ManageData.this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog
					.setMessage(getString(R.string.restoring_database_message));
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {

			File dbFile = new File(Environment.getDataDirectory()
					+ AppConfig.DATABASE_INTERNAL_LOCATION
					+ NotesDbAdapter.DATABASE_NAME);

			File backupDir = new File(AppConfig.BACKUP_DIR);

			File backupFile = new File(backupDir, dbFile.getName());

			try {
				dbFile.createNewFile();
				this.copyFile(backupFile, dbFile);
				tracker.trackEvent("ManageData", "RestoreDatabaseFile", "Y", 0);
				tracker.dispatch();
				return true;
			} catch (IOException e) {
				Log.e(TAG, (e.getMessage() != null) ? e.getMessage().replace(
						" ", "_") : "", e);
				tracker.trackEvent("ManageData", "RestoreDatabaseFileError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : ""), 0);
				tracker.dispatch();
				return false;
			}
		}

		// can use UI thread here
		protected void onPostExecute(final Boolean success) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (success) {
				Toast.makeText(ManageData.this,
						R.string.database_restored_successfully_message,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(ManageData.this,
						R.string.database_restore_failed_message,
						Toast.LENGTH_SHORT).show();
			}
		}

		void copyFile(File src, File dst) throws IOException {
			FileChannel inChannel = new FileInputStream(src).getChannel();
			FileChannel outChannel = new FileOutputStream(dst).getChannel();
			try {
				inChannel.transferTo(0, inChannel.size(), outChannel);
			} finally {
				if (inChannel != null)
					inChannel.close();
				if (outChannel != null)
					outChannel.close();
			}
		}
	}

}