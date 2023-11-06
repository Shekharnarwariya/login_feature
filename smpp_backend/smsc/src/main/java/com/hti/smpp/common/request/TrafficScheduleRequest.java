package com.hti.smpp.common.request;

public class TrafficScheduleRequest {
	private int[] id;
	private int[] smscId;
	private String[] gmt;
	private int[] day;
	private String[] duration;
	private String[] downTime;

	public int[] getId() {
		return id;
	}

	public void setId(int[] id) {
		this.id = id;
	}

	public int[] getSmscId() {
		return smscId;
	}

	public void setSmscId(int[] smscId) {
		this.smscId = smscId;
	}

	public String[] getGmt() {
		return gmt;
	}

	public void setGmt(String[] gmt) {
		this.gmt = gmt;
	}

	public int[] getDay() {
		return day;
	}

	public void setDay(int[] day) {
		this.day = day;
	}

	public String[] getDuration() {
		return duration;
	}

	public void setDuration(String[] duration) {
		this.duration = duration;
	}

	public String[] getDownTime() {
		return downTime;
	}

	public void setDownTime(String[] downTime) {
		this.downTime = downTime;
	}
}
