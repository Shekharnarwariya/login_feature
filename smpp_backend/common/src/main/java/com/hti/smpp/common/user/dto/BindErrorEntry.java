package com.hti.smpp.common.user.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_bind_error")
public class BindErrorEntry implements Serializable {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "system_id")
	private String systemId;
	@Column(name = "server_id")
	private int serverId;
	@Column(name = "status_code")
	private int commandStatus;
	@Column(name = "request_ip")
	private String requestIp;
	@Column(name = "updateOn")
	private String lastUpdateOn;
	private String remarks;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getCommandStatus() {
		return commandStatus;
	}

	public void setCommandStatus(int commandStatus) {
		this.commandStatus = commandStatus;
	}

	public String getRequestIp() {
		return requestIp;
	}

	public void setRequestIp(String requestIp) {
		this.requestIp = requestIp;
	}

	public String getLastUpdateOn() {
		return lastUpdateOn;
	}

	public void setLastUpdateOn(String lastUpdateOn) {
		this.lastUpdateOn = lastUpdateOn;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String toString() {
		return "Session: systemId=" + systemId + ",requestIp=" + requestIp + ",serverId=" + serverId + ",commandStatus="
				+ commandStatus;
	}
}
