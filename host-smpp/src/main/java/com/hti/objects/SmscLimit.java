package com.hti.objects;

public class SmscLimit {
	private int id;
	private int smscId;
	private int limit;
	private int networkId;
	private int rerouteSmscId;
	private String smsc;
	private String rerouteSmsc;
	private String resetTime;
	private String alertNumber;
	private String alertEmail;
	private String alertSender;

	public SmscLimit() {
	}

	public SmscLimit(int id, int smscId, int limit, int networkId, String rerouteSmsc, String resetTime,
			String alertNumber, String alertEmail, String alertSender) {
		this.id = id;
		this.smscId = smscId;
		this.limit = limit;
		this.networkId = networkId;
		this.rerouteSmsc = rerouteSmsc;
		this.resetTime = resetTime;
		this.alertEmail = alertEmail;
		this.alertNumber = alertNumber;
		this.alertSender = alertSender;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public String getRerouteSmsc() {
		return rerouteSmsc;
	}

	public void setRerouteSmsc(String rerouteSmsc) {
		this.rerouteSmsc = rerouteSmsc;
	}

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public int getRerouteSmscId() {
		return rerouteSmscId;
	}

	public void setRerouteSmscId(int rerouteSmscId) {
		this.rerouteSmscId = rerouteSmscId;
	}

	public String getResetTime() {
		return resetTime;
	}

	public void setResetTime(String resetTime) {
		this.resetTime = resetTime;
	}

	public String getAlertNumber() {
		return alertNumber;
	}

	public void setAlertNumber(String alertNumber) {
		this.alertNumber = alertNumber;
	}

	public String getAlertEmail() {
		return alertEmail;
	}

	public void setAlertEmail(String alertEmail) {
		this.alertEmail = alertEmail;
	}

	public String getAlertSender() {
		return alertSender;
	}

	public void setAlertSender(String alertSender) {
		this.alertSender = alertSender;
	}

	@Override
	public String toString() {
		return "SmscLimit: id=" + id + ",smsc_id=" + smscId + ",network_id=" + networkId + ",RerouteTo=" + rerouteSmsc
				+ ",limit=" + limit + ",alertNumber=" + alertNumber + ",alertEmail=" + alertEmail + ",alertSender="
				+ alertSender;
	}
}
