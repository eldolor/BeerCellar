package com.cm.beer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmActionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(
				AlarmType.NEW_BEER_REVIEW_ACTION.getType())) {
			Intent service = new Intent(context, NotificationService.class);
			if (intent.getExtras() != null) {
				intent.putExtra("ALARM_TYPE",
						AlarmType.NEW_BEER_REVIEW_ACTION.getType());
				service.putExtras(intent.getExtras());
			}
			context.startService(service);
		} else if (intent.getAction().equals(
				AlarmType.NEW_BEER_REVIEW_FROM_FOLLOWING_ACTION.getType())) {
			Intent service = new Intent(context, NotificationService.class);
			if (intent.getExtras() != null) {
				intent.putExtra("ALARM_TYPE",
						AlarmType.NEW_BEER_REVIEW_FROM_FOLLOWING_ACTION
								.getType());
				service.putExtras(intent.getExtras());
			}
			context.startService(service);
		} else if (intent.getAction().equals(
				AlarmType.BEER_OF_THE_DAY_ACTION.getType())) {
			Intent service = new Intent(context, NotificationService.class);
			if (intent.getExtras() != null) {
				intent.putExtra("ALARM_TYPE",
						AlarmType.BEER_OF_THE_DAY_ACTION.getType());
				service.putExtras(intent.getExtras());
			}
			context.startService(service);
		}
	}
}
