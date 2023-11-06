package com.hti.smpp.common.request;

public class SmscBsfmRequest  {
	private int[] id;
	private int[] smscId;
	private String[] source;
	private String[] content;

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

	public String[] getSource() {
		return source;
	}

	public void setSource(String[] source) {
		this.source = source;
	}

	public String[] getContent() {
		return content;
	}

	public void setContent(String[] content) {
		this.content = content;
	}
}
