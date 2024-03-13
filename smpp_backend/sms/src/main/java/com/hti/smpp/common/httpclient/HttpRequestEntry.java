package com.hti.smpp.common.httpclient;

public class HttpRequestEntry {
	private String requestIp;
	private String receivedTime;
	private String requestUrl;
	private String requestMethod;

	public HttpRequestEntry(String requestIp, String receivedTime, String requestUrl, String requestMethod) {
		this.requestIp = requestIp;
		this.receivedTime = receivedTime;
		this.requestUrl = requestUrl;
		this.requestMethod = requestMethod;
	}

	public String getRequestIp() {
		return requestIp;
	}

	public void setRequestIp(String requestIp) {
		this.requestIp = requestIp;
	}

	public String getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(String receivedTime) {
		this.receivedTime = receivedTime;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}
}
