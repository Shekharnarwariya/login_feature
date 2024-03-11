package com.hti.user.dto;

public class SubmitLimitEntry {
	private int userId;
	private long duration;
	private long count;
	private int rerouteSmscId;
	private String rerouteSmsc;
	private String alertNumber;
	private String alertEmail;
	private String alertSender;

	public SubmitLimitEntry(int userId, long duration, long count, int rerouteSmscId, String alertNumber,
			String alertEmail, String alertSender) {
		this.userId = userId;
		this.duration = duration;
		this.count = count;
		this.rerouteSmscId = rerouteSmscId;
		this.alertNumber = alertNumber;
		this.alertEmail = alertEmail;
		this.alertSender = alertSender;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public int getRerouteSmscId() {
		return rerouteSmscId;
	}

	public void setRerouteSmscId(int rerouteSmscId) {
		this.rerouteSmscId = rerouteSmscId;
	}

	public String getRerouteSmsc() {
		return rerouteSmsc;
	}

	public void setRerouteSmsc(String rerouteSmsc) {
		this.rerouteSmsc = rerouteSmsc;
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
