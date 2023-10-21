package com.hti.smpp.common.smsc.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "smsc_limit")
public class LimitEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "smsc_id", updatable = false)
	private int smscId;
	@Column(name = "network_id", updatable = false)
	private int networkId;
	@Column(name = "reroute_id")
	private int rerouteId;
	@Column(name = "sms_limit")
	private int limit;
	@Column(name = "reset_time")
	private String resetTime;
	@Column(name = "alert_number")
	private String alertNumber;
	@Column(name = "alert_email")
	private String alertEmail;
	@Column(name = "alert_sender")
	private String alertSender;
	@Transient
	private String smsc;
	@Transient
	private String network;
	@Transient
	private String reroute;

	public LimitEntry() {
	}

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

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
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

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getReroute() {
		return reroute;
	}

	public void setReroute(String reroute) {
		this.reroute = reroute;
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

	public String toString() {
		return "SmscLimit: id=" + id + ",smscId=" + smscId + ",networkId=" + networkId + ",rerouteId=" + rerouteId
				+ ",limit=" + limit;
	}
}
