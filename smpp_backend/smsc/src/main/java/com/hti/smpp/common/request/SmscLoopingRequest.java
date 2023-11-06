package com.hti.smpp.common.request;

public class SmscLoopingRequest {
	private int smscId;
	private String senderId;
	private int duration;
	private int count;
	private int rerouteSmscId;
	private boolean active;

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getRerouteSmscId() {
		return rerouteSmscId;
	}

	public void setRerouteSmscId(int rerouteSmscId) {
		this.rerouteSmscId = rerouteSmscId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
