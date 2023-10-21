package com.hti.smpp.common.user.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity bean with JPA annotations
 */
@Entity
@Table(name = "web_access_log")
public class AccessLogEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "system_id")
	private String systemId;
	@Column(name = "time")
	private String time;
	@Column(name = "ip_address")
	private String ipAddress;
	@Column(name = "session_id")
	private int sessionId;
	@Column(name = "status")
	private String status;
	@Column(name = "remarks")
	private String remarks;

	public AccessLogEntry() {
	}

	public AccessLogEntry(String systemId, String time, String ipAddress, int sessionId, String status,
			String remarks) {
		this.systemId = systemId;
		this.time = time;
		this.ipAddress = ipAddress;
		this.sessionId = sessionId;
		this.status = status;
		this.remarks = remarks;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String toString() {
		return "accesslog:system_id=" + systemId + ",ip=" + ipAddress + ",sessionid=" + sessionId + ",status=" + status
				+ ",time=" + time + ",remarks=" + remarks;
	}
}
