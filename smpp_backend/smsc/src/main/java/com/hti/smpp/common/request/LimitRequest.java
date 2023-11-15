package com.hti.smpp.common.request;

import java.util.Arrays;

public class LimitRequest {
	private int id;
	private int smscId;
	private int[] networkId;
	private int rerouteId;
	private int limit;
	private String resetTime;
	private String alertNumber;
	private String alertEmail;
	private String alertSender;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public int[] getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int[] networkId) {
		this.networkId = networkId;
	}

	public int getRerouteId() {
		return rerouteId;
	}

	public void setRerouteId(int rerouteId) {
		this.rerouteId = rerouteId;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
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
		return "LimitRequest [id=" + id + ", smscId=" + smscId + ", networkId=" + Arrays.toString(networkId)
				+ ", rerouteId=" + rerouteId + ", limit=" + limit + ", resetTime=" + resetTime + ", alertNumber="
				+ alertNumber + ", alertEmail=" + alertEmail + ", alertSender=" + alertSender + "]";
	}

}
