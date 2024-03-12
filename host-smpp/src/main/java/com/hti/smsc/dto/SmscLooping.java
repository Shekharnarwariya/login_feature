package com.hti.smsc.dto;

public class SmscLooping {
	private int smscId;
	private String senderId; // optional
	private int duration; // seconds
	private int count;
	private int rerouteSmscId;
	private boolean content; // optional if include content
	private int clearCacheOn; // minutes
	private boolean active;
	private String smsc;
	private String rerouteSmsc;

	public SmscLooping(int smscId, String senderId, int duration, int count, int rerouteSmscId, boolean content,
			int clearCacheOn) {
		this.smscId = smscId;
		this.senderId = senderId;
		this.duration = duration;
		this.count = count;
		this.rerouteSmscId = rerouteSmscId;
		this.content = content;
		this.clearCacheOn = clearCacheOn;
	}

	public SmscLooping(int duration, int count) {
		this.duration = duration;
		this.count = count;
	}

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

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public String getRerouteSmsc() {
		return rerouteSmsc;
	}

	public void setRerouteSmsc(String rerouteSmsc) {
		this.rerouteSmsc = rerouteSmsc;
	}

	public boolean isContent() {
		return content;
	}

	public void setContent(boolean content) {
		this.content = content;
	}

	public int getClearCacheOn() {
		return clearCacheOn;
	}

	public void setClearCacheOn(int clearCacheOn) {
		this.clearCacheOn = clearCacheOn;
	}

	public String toString() {
		return "SmscLooping: smscId=" + smscId + ",senderId=" + senderId + ",loopCount=" + count + ",duration="
				+ duration + ",rerouteSmscId=" + rerouteSmscId + ",active=" + active;
	}
}
