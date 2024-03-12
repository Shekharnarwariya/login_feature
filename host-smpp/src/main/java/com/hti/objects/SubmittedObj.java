package com.hti.objects;

import java.util.Date;

public class SubmittedObj {
	private String msgId;
	private int sequenceNumber;
	private String smsc;
	private String username;
	private String source;
	private String destination;
	private Date submitTime;
	private String deliverTime;

	public SubmittedObj(String msgId, int sequenceNumber, String smsc, String username, String source,
			String destination, Date submitTime) {
		this.msgId = msgId;
		this.sequenceNumber = sequenceNumber;
		this.smsc = smsc;
		this.username = username;
		this.source = source;
		this.destination = destination;
		this.submitTime = submitTime;
	}

	public SubmittedObj(String msgId, String smsc, String source, String destination, Date submitTime) {
		this.msgId = msgId;
		this.smsc = smsc;
		this.source = source;
		this.destination = destination;
		this.submitTime = submitTime;
	}

	public SubmittedObj(String msgId, String smsc, Date submitTime) {
		this.msgId = msgId;
		this.smsc = smsc;
		this.submitTime = submitTime;
	}

	public SubmittedObj(String msgId, String deliverTime) {
		this.msgId = msgId;
		this.deliverTime = deliverTime;
	}

	public String getMsgId() {
		return msgId;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public String getSmsc() {
		return smsc;
	}

	public String getUsername() {
		return username;
	}

	public String getSource() {
		return source;
	}

	public String getDestination() {
		return destination;
	}

	public Date getSubmitTime() {
		return submitTime;
	}

	public String getDeliverTime() {
		return deliverTime;
	}

	public void setDeliverTime(String deliverTime) {
		this.deliverTime = deliverTime;
	}
}
