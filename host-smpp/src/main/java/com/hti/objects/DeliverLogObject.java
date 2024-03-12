package com.hti.objects;

import java.util.Date;

public class DeliverLogObject implements java.io.Serializable {
	private String responseId;
	private Date receivedOn;
	private String shortMessage;
	private String smsc;
	private String source;
	private String destination;

	public DeliverLogObject() {
	}

	public DeliverLogObject(String responseId, Date receivedOn, String shortMessage, String smsc, String source,
			String destination) {
		this.responseId = responseId;
		this.receivedOn = receivedOn;
		this.shortMessage = shortMessage;
		this.smsc = smsc;
		this.source = source;
		this.destination = destination;
	}

	public String getResponseId() {
		return responseId;
	}

	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public Date getReceivedOn() {
		return receivedOn;
	}

	public void setReceivedOn(Date receivedOn) {
		this.receivedOn = receivedOn;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}
}
