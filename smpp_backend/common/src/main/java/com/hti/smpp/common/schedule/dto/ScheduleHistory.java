package com.hti.smpp.common.schedule.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedule_history")
public class ScheduleHistory {

	@Id
	private int id;

	@Column(name = "username", length = 15)
	private String username;

	@Column(name = "server_time", length = 20, nullable = false, columnDefinition = "varchar(20) DEFAULT '0'")
	private String serverTime;

	@Column(name = "schType", length = 10, columnDefinition = "varchar(10) DEFAULT 'bulk'")
	private String schType;

	@Column(name = "repeated", length = 20, columnDefinition = "varchar(20) DEFAULT 'No'")
	private String repeated;

	@Column(name = "server_id", nullable = false, columnDefinition = "int(1) DEFAULT '1'")
	private int serverId;

	@Column(name = "client_gmt", length = 12)
	private String clientGmt;

	@Column(name = "client_time", length = 20)
	private String clientTime;

	@Column(name = "msg_type", length = 12, columnDefinition = "varchar(12) DEFAULT 'SpecialChar'")
	private String msgType;

	@Column(name = "total_number", length = 10)
	private Integer totalNumber;

	@Column(name = "campaign_name", length = 30)
	private String campaignName;

	@Column(name = "remarks", length = 50)
	private String remarks;

	@Column(name = "sender_id", length = 16)
	private String senderId;

	@Column(name = "createdOn", length = 20)
	private String createdOn;

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
	 * @return the schType
	 */
	public String getSchType() {
		return schType;
	}

	/**
	 * @param schType the schType to set
	 */
	public void setSchType(String schType) {
		this.schType = schType;
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
	 * @return the msgType
	 */
	public String getMsgType() {
		return msgType;
	}

	/**
	 * @param msgType the msgType to set
	 */
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	/**
	 * @return the totalNumber
	 */
	public Integer getTotalNumber() {
		return totalNumber;
	}

	/**
	 * @param totalNumber the totalNumber to set
	 */
	public void setTotalNumber(Integer totalNumber) {
		this.totalNumber = totalNumber;
	}

	/**
	 * @return the campaignName
	 */
	public String getCampaignName() {
		return campaignName;
	}

	/**
	 * @param campaignName the campaignName to set
	 */
	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	/**
	 * @return the remarks
	 */
	public String getRemarks() {
		return remarks;
	}

	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	/**
	 * @return the senderId
	 */
	public String getSenderId() {
		return senderId;
	}

	/**
	 * @param senderId the senderId to set
	 */
	public void setSenderId(String senderId) {
		this.senderId = senderId;
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
	 * @param id
	 * @param username
	 * @param serverTime
	 * @param schType
	 * @param repeated
	 * @param serverId
	 * @param clientGmt
	 * @param clientTime
	 * @param msgType
	 * @param totalNumber
	 * @param campaignName
	 * @param remarks
	 * @param senderId
	 * @param createdOn
	 */
	public ScheduleHistory(int id, String username, String serverTime, String schType, String repeated, int serverId,
			String clientGmt, String clientTime, String msgType, Integer totalNumber, String campaignName,
			String remarks, String senderId, String createdOn) {
		super();
		this.id = id;
		this.username = username;
		this.serverTime = serverTime;
		this.schType = schType;
		this.repeated = repeated;
		this.serverId = serverId;
		this.clientGmt = clientGmt;
		this.clientTime = clientTime;
		this.msgType = msgType;
		this.totalNumber = totalNumber;
		this.campaignName = campaignName;
		this.remarks = remarks;
		this.senderId = senderId;
		this.createdOn = createdOn;
	}

	/**
	 * 
	 */
	public ScheduleHistory() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "ScheduleHistory [id=" + id + ", username=" + username + ", serverTime=" + serverTime + ", schType="
				+ schType + ", repeated=" + repeated + ", serverId=" + serverId + ", clientGmt=" + clientGmt
				+ ", clientTime=" + clientTime + ", msgType=" + msgType + ", totalNumber=" + totalNumber
				+ ", campaignName=" + campaignName + ", remarks=" + remarks + ", senderId=" + senderId + ", createdOn="
				+ createdOn + "]";
	}

}