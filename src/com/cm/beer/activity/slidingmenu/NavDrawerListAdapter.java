package com.cm.beer.activity.slidingmenu;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cm.beer.activity.R;

public class NavDrawerListAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<NavDrawerItem> mNavDrawerItems;

	public NavDrawerListAdapter(Context context,
			ArrayList<NavDrawerItem> navDrawerItems) {
		this.mContext = context;
		this.mNavDrawerItems = navDrawerItems;
	}

	@Override
	public int getCount() {
		return mNavDrawerItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mNavDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) mContext
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.drawer_list_item, null);
		}

		TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
		if (position == 0) {
			// txtTitle.setTypeface(null, Typeface.BOLD);
			txtTitle.setTextColor(this.mContext.getResources().getColor(
					R.color.white));
		}
		// TextView txtCount = (TextView)
		// convertView.findViewById(R.id.counter);

		txtTitle.setText(mNavDrawerItems.get(position).getTitle());

		// displaying count
		// check whether it set visible or not
		// if (mNavDrawerItems.get(position).getCounterVisibility()) {
		// txtCount.setText(mNavDrawerItems.get(position).getCount());
		// } else {
		// // hide the counter view
		// txtCount.setVisibility(View.GONE);
		// }

		return convertView;
	}

}