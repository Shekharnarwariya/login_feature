/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.objects;

/**
 *
 * @author Administrator
 */
public class ReportLogObject {
	private String msgid;
	private String oprCountry;
	private String username;
	private String smsc;
	private String cost;
	private String status;
	private String time;
	private String deliverTime;
	private String destination;
	private String senderid;
	private String remarks;
	private int profileId;

	public ReportLogObject(String msgid, String username, String status, String time) {
		this.msgid = msgid;
		this.status = status;
		this.username = username;
		this.deliverTime = time;
	}

	public ReportLogObject(String msgid, String username, String status, String time, String smsc) {
		this.msgid = msgid;
		this.status = status;
		this.username = username;
		this.deliverTime = time;
		this.smsc = smsc;
	}

	public ReportLogObject(String msgid, String oprCountry, String username, String smsc, String cost, String status,
			String time, String destination, String senderid) {
		this.msgid = msgid;
		this.oprCountry = oprCountry;
		this.username = username;
		this.smsc = smsc;
		this.cost = cost;
		this.status = status;
		this.time = time;
		this.destination = destination;
		this.senderid = senderid;
	}

	public ReportLogObject(String msgid, String oprCountry, String username, String smsc, String cost, String time,
			String destination, String senderid, int profileId, String remarks) {
		this.msgid = msgid;
		this.oprCountry = oprCountry;
		this.username = username;
		this.smsc = smsc;
		this.cost = cost;
		this.time = time;
		this.destination = destination;
		this.senderid = senderid;
		this.profileId = profileId;
		this.remarks = remarks;
	}

	public String getDeliverTime() {
		return deliverTime;
	}

	public String getDestination() {
		return destination;
	}

	public String getSenderid() {
		return senderid;
	}

	public String getTime() {
		return time;
	}

	public String getMsgid() {
		return msgid;
	}

	public String getOprCountry() {
		return oprCountry;
	}

	public String getUsername() {
		return username;
	}

	public String getSmsc() {
		return smsc;
	}

	public String getCost() {
		return cost;
	}

	public String getStatus() {
		return status;
	}

	public String getRemarks() {
		return remarks;
	}

	public int getProfileId() {
		return profileId;
	}
}
