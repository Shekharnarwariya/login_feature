package com.hti.smpp.common.request;

public class CustomRequest {
	private int smscId;
	private int sourceLength;
	private int lston;
	private int lsnpi;
	private int gston;
	private int gsnpi;

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public int getSourceLength() {
		return sourceLength;
	}

	public void setSourceLength(int sourceLength) {
		this.sourceLength = sourceLength;
	}

	public int getLston() {
		return lston;
	}

	public void setLston(int lston) {
		this.lston = lston;
	}

	public int getLsnpi() {
		return lsnpi;
	}

	public void setLsnpi(int lsnpi) {
		this.lsnpi = lsnpi;
	}

	public int getGston() {
		return gston;
	}

	public void setGston(int gston) {
		this.gston = gston;
	}

	public int getGsnpi() {
		return gsnpi;
	}

	public void setGsnpi(int gsnpi) {
		this.gsnpi = gsnpi;
	}

	public String toString() {
		return "CustomSmscForm:smscId=" + smscId + ",SourceLength=" + sourceLength;
	}
}
