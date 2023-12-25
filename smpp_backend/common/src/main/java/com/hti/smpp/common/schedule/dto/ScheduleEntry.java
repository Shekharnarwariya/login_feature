package com.hti.smpp.common.schedule.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedulesms")
public class ScheduleEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String username;
	private String serverTime;
	private String clientGmt;
	private String clientTime;
	private int serverId;
	private String status;
	private String fileName;
	private String repeated;
	private String scheduleType;
	private String createdOn;
	private String webId;
	/**
     * Default constructor for the ScheduleEntry class.
     */
	public ScheduleEntry() {
	}
	 /**
     * Parameterized constructor for creating a ScheduleEntry instance with initial values.
     */
	public ScheduleEntry(String username, String serverTime, String clientGmt, String clientTime, int serverId,
			String status, String fileName, String repeated, String scheduleType, String webId) {
		this.username = username;
		this.serverTime = serverTime;
		this.clientGmt = clientGmt;
		this.clientTime = clientTime;
		this.serverId = serverId;
		this.status = status;
		this.fileName = fileName;
		this.repeated = repeated;
		this.scheduleType = scheduleType;
		this.webId = webId;
	}

	public int getId() {
		return id;
	}
	 /**
     * Set the unique identifier of the scheduled SMS entry.
     */
	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getServerTime() {
		return serverTime;
	}

	public void setServerTime(String serverTime) {
		this.serverTime = serverTime;
	}

	public String getClientGmt() {
		return clientGmt;
	}

	public void setClientGmt(String clientGmt) {
		this.clientGmt = clientGmt;
	}

	public String getClientTime() {
		return clientTime;
	}

	public void setClientTime(String clientTime) {
		this.clientTime = clientTime;
	}
	 /**
     * Get the server ID associated with the scheduled SMS entry.
     */
	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getRepeated() {
		return repeated;
	}

	public void setRepeated(String repeated) {
		this.repeated = repeated;
	}

	public String getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getWebId() {
		return webId;
	}

	public void setWebId(String webId) {
		this.webId = webId;
	}

	public String toString() {
		return "ScheduleEntry: id=" + id + ",username=" + username + ",serverTime=" + serverTime + ",clientTime="
				+ clientTime + ",WebId=" + webId + ",filename=" + fileName;
	}
}
