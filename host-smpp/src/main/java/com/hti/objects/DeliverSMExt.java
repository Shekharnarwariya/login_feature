package com.hti.objects;

import java.io.Serializable;

public class DeliverSMExt implements Serializable {
	private String msgid;
	private String username;
	private String submitOn;
	private String deliverOn;
	private String source;
	private String destination;
	private String status;
	private String errorCode;
	private int serverId;

	public DeliverSMExt(String msgid, String username, String submitOn, String deliverOn, String source,
			String destination, String status, String errorCode, int serverId) {
		this.msgid = msgid;
		this.username = username;
		this.submitOn = submitOn;
		this.deliverOn = deliverOn;
		this.source = source;
		this.destination = destination;
		this.status = status;
		this.errorCode = errorCode;
		this.serverId = serverId;
	}

	public DeliverSMExt(String msgid, String username, String submitOn, String deliverOn, String source,
			String destination, String status, String errorCode) {
		this.msgid = msgid;
		this.username = username;
		this.submitOn = submitOn;
		this.deliverOn = deliverOn;
		this.source = source;
		this.destination = destination;
		this.status = status;
		this.errorCode = errorCode;
	}

	public int getServerId() {
		return serverId;
	}

	public String getMsgid() {
		return msgid;
	}

	public String getUsername() {
		return username;
	}

	public String getSubmitOn() {
		return submitOn;
	}

	public String getDeliverOn() {
		return deliverOn;
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
