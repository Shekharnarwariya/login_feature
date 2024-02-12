package com.hti.smpp.common.schedule.dto;

import jakarta.persistence.Column;
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

	@Column(name = "username")
	private String username;

	@Column(name = "time")
	private String serverTime;

	@Column(name = "client_gmt")
	private String clientGmt;

	@Column(name = "client_time")
	private String clientTime;

	@Column(name = "server_id")
	private int serverId;

	@Column(name = "status")
	private String status;

	@Column(name = "filename")
	private String fileName;

	@Column(name = "repeated")
	private String repeated;

	@Column(name = "schType")
	private String scheduleType;

	@Column(name = "createdOn")
	private String createdOn;

	@Column(name = "web_id")
	private String webId;

	@Column(name = "date")
	private String date;

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the serverTime
	 */
	public String getServerTime() {
		return serverTime;
	}

	/**
	 * @param serverTime the serverTime to set
	 */
	public void setServerTime(String serverTime) {
		this.serverTime = serverTime;
	}

	/**
	 * @return the clientGmt
	 */
	public String getClientGmt() {
		return clientGmt;
	}

	/**
	 * @param clientGmt the clientGmt to set
	 */
	public void setClientGmt(String clientGmt) {
		this.clientGmt = clientGmt;
	}

	/**
	 * @return the clientTime
	 */
	public String getClientTime() {
		return clientTime;
	}

	/**
	 * @param clientTime the clientTime to set
	 */
	public void setClientTime(String clientTime) {
		this.clientTime = clientTime;
	}

	/**
	 * @return the serverId
	 */
	public int getServerId() {
		return serverId;
	}

	/**
	 * @param serverId the serverId to set
	 */
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the repeated
	 */
	public String getRepeated() {
		return repeated;
	}

	/**
	 * @param repeated the repeated to set
	 */
	public void setRepeated(String repeated) {
		this.repeated = repeated;
	}

	/**
	 * @return the scheduleType
	 */
	public String getScheduleType() {
		return scheduleType;
	}

	/**
	 * @param scheduleType the scheduleType to set
	 */
	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	/**
	 * @return the createdOn
	 */
	public String getCreatedOn() {
		return createdOn;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * @return the webId
	 */
	public String getWebId() {
		return webId;
	}

	/**
	 * @param webId the webId to set
	 */
	public void setWebId(String webId) {
		this.webId = webId;
	}

	public ScheduleEntry(String username, String serverTime, String clientGmt, String clientTime, int serverId,
			String status, String fileName, String repeated, String scheduleType, String createdOn) {
		super();
		this.username = username;
		this.serverTime = serverTime;
		this.clientGmt = clientGmt;
		this.clientTime = clientTime;
		this.serverId = serverId;
		this.status = status;
		this.fileName = fileName;
		this.repeated = repeated;
		this.scheduleType = scheduleType;
		this.createdOn = createdOn;
		this.webId = webId;

	}

	public ScheduleEntry() {
		super();

	}

	@Override
	public String toString() {
		return "ScheduleEntry [id=" + id + ", username=" + username + ", serverTime=" + serverTime + ", clientGmt="
				+ clientGmt + ", clientTime=" + clientTime + ", serverId=" + serverId + ", status=" + status
				+ ", fileName=" + fileName + ", repeated=" + repeated + ", scheduleType=" + scheduleType
				+ ", createdOn=" + createdOn + ", webId=" + webId + "]";
	}

}
