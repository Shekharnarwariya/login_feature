package com.hti.objects;

import java.io.Serializable;
import java.util.Date;

public class DeliverObj implements Serializable {
	private String responseId;
	private Date time;
	private String source;
	private String destination;
	private String status;
	private String errorCode;
	private String smsc;

	public DeliverObj(String responseId, String smsc, Date time, String source, String destination, String status,
			String errorCode) {
		this.responseId = responseId;
		this.smsc = smsc;
		this.time = time;
		this.source = source;
		this.destination = destination;
		this.status = status;
		this.errorCode = errorCode;
	}

	public String getSmsc() {
		return smsc;
	}

	public String getResponseId() {
		return responseId;
	}

	public Date getTime() {
		return time;
	}

	public String getSource() {
		return source;
	}

	public String getDestination() {
		return destination;
	}

	public String getStatus() {
		return status;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
