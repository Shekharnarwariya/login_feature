package com.hti.smpp.common.user.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Entity class representing user-specific delivery receipt (DLR) settings with JPA annotations.
 */
@Entity
@Table(name = "user_limit")
public class UserlimitEntry {
	@Id
	@Column(name = "user_id")
	private int userId;
	@Column(name = "reroute_smsc_id")
	private int rerouteSmscId;
	@Column(name = "count")
	private int count;
	@Column(name = "duration")
	private int duration;
	@Column(name = "alert_number")
	private String alertNumber;
	@Column(name = "alert_email")
	private String alertEmail;
	@Column(name = "alert_sender")
	private String alertSender;
	@Transient
	private String systemId;
	@Transient
	private String reroute;

	public UserlimitEntry() {
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getRerouteSmscId() {
		return rerouteSmscId;
	}

	public void setRerouteSmscId(int rerouteSmscId) {
		this.rerouteSmscId = rerouteSmscId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getReroute() {
		return reroute;
	}

	public void setReroute(String reroute) {
		this.reroute = reroute;
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
		return "UserLimit: userId=" + userId + ",duration=" + duration + ",count=" + count + ",rerouteSmscId="
				+ rerouteSmscId;
	}
}