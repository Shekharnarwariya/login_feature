package com.hti.user.dto;

import java.io.Serializable;

public class SessionEntry implements Serializable {
	private String systemId;
	private int sessionCount;
	private int receiver;
	private int transmitter;
	private int tranciever;
	private String requestIp;
	private int serverId;
	private int commandStatus;
	private int port;

	public SessionEntry(String systemId, String requestIp, int serverId, int sessionCount, int receiver,
			int transmitter, int tranciever, int port) {
		this.systemId = systemId;
		this.requestIp = requestIp;
		this.serverId = serverId;
		this.sessionCount = sessionCount;
		this.receiver = receiver;
		this.transmitter = transmitter;
		this.tranciever = tranciever;
		this.port = port;
	}

	public SessionEntry(String systemId, String requestIp, int serverId, int commandStatus, int port) {
		this.systemId = systemId;
		this.requestIp = requestIp;
		this.serverId = serverId;
		this.commandStatus = commandStatus;
		this.port = port;
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

	public int getCommandStatus() {
		return commandStatus;
	}

	public void setCommandStatus(int commandStatus) {
		this.commandStatus = commandStatus;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String toString() {
		return "Session: systemId=" + systemId + ",requestIp=" + requestIp + ",localPort=" + port + ",serverId="
				+ serverId + ",count=" + sessionCount + ",receiver=" + receiver + ",transmitter=" + transmitter
				+ ",tranciever=" + tranciever;
	}
}
