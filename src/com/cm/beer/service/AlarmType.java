package com.cm.beer.service;

public enum AlarmType {
	NEW_BEER_REVIEW_ACTION(
			"com.cm.beer.NEW_BEER_REVIEW_ACTION"), NEW_BEER_REVIEW_FROM_FOLLOWING_ACTION(
					"com.cm.beer.NEW_BEER_REVIEW_FROM_FOLLOWING_ACTION"), BEER_OF_THE_DAY_ACTION(
			"com.cm.beer.BEER_OF_THE_DAY_ACTION");

	private String type;

	private AlarmType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}