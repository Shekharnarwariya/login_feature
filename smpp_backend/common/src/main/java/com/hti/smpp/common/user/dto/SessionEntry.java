package com.hti.smpp.common.user.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Entity bean with JPA annotations
 */
@Entity
@Table(name = "user_session")
public class SessionEntry implements Serializable {
	@Id
	@Column(name = "id", length = 11)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "system_id", length = 15)
	private String systemId;
	@Column(name = "server_id", length = 3)
	private int serverId;
	@Column(name = "total", length = 3)
	private int sessionCount;
	@Column(name = "request_ip")
	private String requestIp;
	@Column(name = "rx", length = 3)
	private int receiver;
	@Column(name = "tx", length = 3)
	private int transmitter;
	@Column(name = "trx", length = 3)
	private int tranciever;
	@Column(name = "updateOn")
	private String lastUpdateOn;
	@Transient
	private String remarks;

	public SessionEntry() {
	}

	public SessionEntry(String systemId, String requestIp, int serverId, int sessionCount, int receiver,
			int transmitter, int tranciever) {
		this.systemId = systemId;
		this.requestIp = requestIp;
		this.serverId = serverId;
		this.sessionCount = sessionCount;
		this.receiver = receiver;
		this.transmitter = transmitter;
		this.tranciever = tranciever;
	}

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

	public int getSessionCount() {
		return sessionCount;
	}

	public void setSessionCount(int sessionCount) {
		this.sessionCount = sessionCount;
	}

	public int getReceiver() {
		return receiver;
	}

	public void setReceiver(int receiver) {
		this.receiver = receiver;
	}

	public int getTransmitter() {
		return transmitter;
	}

	public void setTransmitter(int transmitter) {
		this.transmitter = transmitter;
	}

	public int getTranciever() {
		return tranciever;
	}

	public void setTranciever(int tranciever) {
		this.tranciever = tranciever;
	}

	public String getRequestIp() {
		return requestIp;
	}

	public void setRequestIp(String requestIp) {
		this.requestIp = requestIp;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
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
		return "Session: systemId=" + systemId + ",requestIp=" + requestIp + ",serverId=" + serverId + ",count="
				+ sessionCount + ",receiver=" + receiver + ",transmitter=" + transmitter + ",tranciever=" + tranciever;
	}
}
