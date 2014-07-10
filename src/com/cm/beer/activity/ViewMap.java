package com.cm.beer.activity;

public class ViewMap {
	/*
	 * import java.util.ArrayList; import java.util.List;
	 * 
	 * import android.graphics.drawable.Drawable; import android.os.Bundle;
	 * import android.util.Log;
	 * 
	 * import com.google.android.apps.analytics.GoogleAnalyticsTracker; import
	 * com.google.android.maps.GeoPoint; import
	 * com.google.android.maps.MapActivity; import
	 * com.google.android.maps.MapView; import com.google.android.maps.Overlay;
	 * import com.google.android.maps.OverlayItem; import
	 * com.vvw.config.AppConfig;
	 * 
	 * public class ViewMap extends MapActivity { String TAG; ViewMap
	 * mMainActivity; GoogleAnalyticsTracker mTracker; Bundle mExtras;
	 * 
	 * List<Overlay> mMapOverlays; Drawable mDrawable; ViewMapItemizedOverlay
	 * mItemizedOverlay;
	 * 
	 * @Override protected void onCreate(Bundle icicle) { // TODO Auto-generated
	 * method stub super.onCreate(icicle); // setup TAG TAG =
	 * this.getString(R.string.app_name) + "::" + this.getClass().getName(); if
	 * (AppConfig.LOGGING_ENABLED) { Log.i(TAG, "onCreate"); } mMainActivity =
	 * this; mTracker = GoogleAnalyticsTracker.getInstance(); // Start the
	 * mTracker with dispatch interval
	 * mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
	 * Log.i(TAG, "onCreate:Google Tracker Instantiated");
	 * 
	 * mExtras = getIntent().getExtras(); String latitude =
	 * mExtras.getString("LATITUDE"); String longitude =
	 * mExtras.getString("LONGITUDE");
	 * 
	 * setContentView(R.layout.view_map);
	 * 
	 * MapView mapView = (MapView) findViewById(R.id.map);
	 * mapView.setBuiltInZoomControls(true);
	 * 
	 * mMapOverlays = mapView.getOverlays(); mDrawable =
	 * this.getResources().getDrawable(R.drawable.glass_button);
	 * mItemizedOverlay = new ViewMapItemizedOverlay(mDrawable);
	 * 
	 * GeoPoint point = new GeoPoint((int) ((Float.valueOf(latitude)) *
	 * 1000000), (int) ((Float.valueOf(longitude)) * 1000000)); String title =
	 * mExtras.getString("TITLE"); String snippet =
	 * mExtras.getString("SNIPPET"); OverlayItem overlayItem = new
	 * OverlayItem(point, title, snippet);
	 * 
	 * mItemizedOverlay.addOverlay(overlayItem);
	 * mMapOverlays.add(mItemizedOverlay);
	 * 
	 * // fly to ! mapView.getController().animateTo(point);
	 * mapView.getController().setZoom(AppConfig.GOOGLE_MAPS_ZOOM_LEVEL);
	 * 
	 * 
	 * }
	 * 
	 * @Override protected void onDestroy() { if (AppConfig.LOGGING_ENABLED) {
	 * Log.i(TAG, "onDestroy"); } // Stop the mTracker when it is no longer
	 * needed. mTracker.stop(); if (AppConfig.LOGGING_ENABLED) { Log.i(TAG,
	 * "onCreate:Google Tracker Stopped!"); } super.onDestroy(); }
	 * 
	 * @Override protected boolean isRouteDisplayed() { // TODO Auto-generated
	 * method stub return false; }
	 * 
	 * class ViewMapItemizedOverlay extends
	 * com.google.android.maps.ItemizedOverlay{
	 * 
	 * private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	 * 
	 * public ViewMapItemizedOverlay(Drawable drawable){
	 * super(boundCenterBottom(drawable)); }
	 * 
	 * @Override protected OverlayItem createItem(int i) { return
	 * mOverlays.get(i); }
	 * 
	 * @Override public int size() { return mOverlays.size(); }
	 * 
	 * public void addOverlay(OverlayItem overlay) { mOverlays.add(overlay);
	 * populate(); } } }
	 */
}