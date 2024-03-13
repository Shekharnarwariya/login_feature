package com.hti.smpp.common.httpclient;

public class HttpDlrParamEntry {
	private String msgId;
	private String paramName;
	private String paramValue;

	public HttpDlrParamEntry(String msgId, String paramName, String paramValue) {
		this.msgId = msgId;
		this.paramName = paramName;
		this.paramValue = paramValue;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}
}
