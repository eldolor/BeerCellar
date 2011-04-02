package com.cm.beer.transfer;

public class ReviewCount {
	private String userIdIdx;
	private String userId;
	private String userName;
	private String userNameIdx;
	private String userLink;

	private int count;

	public String getUserIdIdx() {
		return userIdIdx;
	}

	public void setUserIdIdx(String userIdIdx) {
		this.userIdIdx = userIdIdx;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserNameIdx() {
		return userNameIdx;
	}

	public void setUserNameIdx(String userNameIdx) {
		this.userNameIdx = userNameIdx;
	}

	public String getUserLink() {
		return userLink;
	}

	public void setUserLink(String userLink) {
		this.userLink = userLink;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
