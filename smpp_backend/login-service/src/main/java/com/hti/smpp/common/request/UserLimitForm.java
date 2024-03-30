package com.hti.smpp.common.request;

public class UserLimitForm {
	private int userId;
	private int rerouteSmscId;
	private int count;
	private int duration;
	private String alertNumber;
	private String alertEmail;
	private String alertSender;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getRerouteSmscId() {
		return rerouteSmscId;
	}

	public void setRerouteSmscId(int rerouteSmscId) {
		this.rerouteSmscId = rerouteSmscId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getAlertNumber() {
		return alertNumber;
	}

	public void setAlertNumber(String alertNumber) {
		this.alertNumber = alertNumber;
	}

	public String getAlertEmail() {
		return alertEmail;
	}

	public void setAlertEmail(String alertEmail) {
		this.alertEmail = alertEmail;
	}

	public String getAlertSender() {
		return alertSender;
	}

	public void setAlertSender(String alertSender) {
		this.alertSender = alertSender;
	}
}
