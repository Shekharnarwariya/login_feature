package com.hti.smpp.common.request;

public class SmscBsfmEntryRequest {
	private int id;
	private int smscId;
	private String source;
	private String content;
	private String smscName;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSmscName() {
		return smscName;
	}

	public void setSmscName(String smscName) {
		this.smscName = smscName;
	}

	public String toString() {
		return "SmscBsfmEntry:id=" + id + ",smscId=" + smscId + ",source=" + source + ",content=" + content;
	}
}
