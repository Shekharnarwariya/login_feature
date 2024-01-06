package com.hti.smpp.common.management.dto;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "bulk_mgmt")
public class BulkManagementEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "system_id")
	private String systemId;

	@Column(name = "sender_id")
	private String senderId;

	@Column(name = "totalNum")
	private long totalNum;

	@Column(name = "firstNum")
	private Long firstNum;

	@Column(name = "delay")
	private Double delay;

	@Column(name = "reqType")
	private String reqType;

	@Column(name = "active")
	private Boolean active;

	@Column(name = "server_id")
	private Integer serverId;

	@Column(name = "ston")
	private Integer ston;

	@Column(name = "snpi")
	private Integer snpi;

	@Column(name = "alert")
	private Boolean alert;

	@Column(name = "alert_number")
	private String alertNumber;

	@Column(name = "expiry_hour")
	private long expiryHour;

	@Column(name = "createdOn")
	private Timestamp createdOn;

	@Lob
	@Column(name = "content")
	private String content;

	@Column(name = "msg_type")
	private String msgType;

	@Column(name = "campaign_name")
	private String campaignName;

	@Column(name = "peId")
	private String peId;

	@Column(name = "templateId")
	private String templateId;

	@Column(name = "telemarketerId")
	private String telemarketerId;

	@Column(name = "msgcount")
	private long msgCount;

	@Column(name = "cost")
	private double cost;

	@Column(name = "usermode")
	private String userMode;

	@Column(name = "campaign_type")
	private String campaignType;

	@Column(name = "status")
	private String status;

	@Column(name = "update_by")
	private String updateBy;

	@Column(name = "update_on")
	private Timestamp updateOn;

	@Column(name = "remarks")
	private String remarks;

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
	 * @return the systemId
	 */
	public String getSystemId() {
		return systemId;
	}

	/**
	 * @param systemId the systemId to set
	 */
	public void setSystemId(String systemId) {
		this.systemId = systemId;
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
	 * @return the totalNum
	 */
	public long getTotalNum() {
		return totalNum;
	}

	/**
	 * @param totalNum the totalNum to set
	 */
	public void setTotalNum(Long totalNum) {
		this.totalNum = totalNum;
	}

	/**
	 * @return the firstNum
	 */
	public Long getFirstNum() {
		return firstNum;
	}

	/**
	 * @param firstNum the firstNum to set
	 */
	public void setFirstNum(Long firstNum) {
		this.firstNum = firstNum;
	}

	/**
	 * @return the delay
	 */
	public Double getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(Double delay) {
		this.delay = delay;
	}

	/**
	 * @return the reqType
	 */
	public String getReqType() {
		return reqType;
	}

	/**
	 * @param reqType the reqType to set
	 */
	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	/**
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	/**
	 * @return the serverId
	 */
	public Integer getServerId() {
		return serverId;
	}

	/**
	 * @param serverId the serverId to set
	 */
	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}

	/**
	 * @return the ston
	 */
	public Integer getSton() {
		return ston;
	}

	/**
	 * @param ston the ston to set
	 */
	public void setSton(Integer ston) {
		this.ston = ston;
	}

	/**
	 * @return the snpi
	 */
	public Integer getSnpi() {
		return snpi;
	}

	/**
	 * @param snpi the snpi to set
	 */
	public void setSnpi(Integer snpi) {
		this.snpi = snpi;
	}

	/**
	 * @return the alert
	 */
	public Boolean getAlert() {
		return alert;
	}

	/**
	 * @param alert the alert to set
	 */
	public void setAlert(Boolean alert) {
		this.alert = alert;
	}

	/**
	 * @return the alertNumber
	 */
	public String getAlertNumber() {
		return alertNumber;
	}

	/**
	 * @param alertNumber the alertNumber to set
	 */
	public void setAlertNumber(String alertNumber) {
		this.alertNumber = alertNumber;
	}

	/**
	 * @return the expiryHour
	 */
	public long getExpiryHour() {
		return expiryHour;
	}

	/**
	 * @param expiryHour the expiryHour to set
	 */
	public void setExpiryHour(long expiryHour) {
		this.expiryHour = expiryHour;
	}

	/**
	 * @return the createdOn
	 */
	public Timestamp getCreatedOn() {
		return createdOn;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
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
	 * @return the peId
	 */
	public String getPeId() {
		return peId;
	}

	/**
	 * @param peId the peId to set
	 */
	public void setPeId(String peId) {
		this.peId = peId;
	}

	/**
	 * @return the templateId
	 */
	public String getTemplateId() {
		return templateId;
	}

	/**
	 * @param templateId the templateId to set
	 */
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	/**
	 * @return the telemarketerId
	 */
	public String getTelemarketerId() {
		return telemarketerId;
	}

	/**
	 * @param telemarketerId the telemarketerId to set
	 */
	public void setTelemarketerId(String telemarketerId) {
		this.telemarketerId = telemarketerId;
	}

	/**
	 * @return the msgCount
	 */
	public long getMsgCount() {
		return msgCount;
	}

	/**
	 * @param msgCount the msgCount to set
	 */
	public void setMsgCount(long msgCount) {
		this.msgCount = msgCount;
	}

	/**
	 * @return the cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * @param cost the cost to set
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * @return the userMode
	 */
	public String getUserMode() {
		return userMode;
	}

	/**
	 * @param userMode the userMode to set
	 */
	public void setUserMode(String userMode) {
		this.userMode = userMode;
	}

	/**
	 * @return the campaignType
	 */
	public String getCampaignType() {
		return campaignType;
	}

	/**
	 * @param campaignType the campaignType to set
	 */
	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
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
	 * @return the updateBy
	 */
	public String getUpdateBy() {
		return updateBy;
	}

	/**
	 * @param updateBy the updateBy to set
	 */
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	/**
	 * @return the updateOn
	 */
	public Timestamp getUpdateOn() {
		return updateOn;
	}

	/**
	 * @param updateOn the updateOn to set
	 */
	public void setUpdateOn(Timestamp updateOn) {
		this.updateOn = updateOn;
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
	 * @param id
	 * @param systemId
	 * @param senderId
	 * @param totalNum
	 * @param firstNum
	 * @param delay
	 * @param reqType
	 * @param active
	 * @param serverId
	 * @param ston
	 * @param snpi
	 * @param alert
	 * @param alertNumber
	 * @param expiryHour
	 * @param createdOn
	 * @param content
	 * @param msgType
	 * @param campaignName
	 * @param peId
	 * @param templateId
	 * @param telemarketerId
	 * @param msgCount
	 * @param cost
	 * @param userMode
	 * @param campaignType
	 * @param status
	 * @param updateBy
	 * @param updateOn
	 * @param remarks
	 */
	public BulkManagementEntity(int id, String systemId, String senderId, long totalNum, Long firstNum, Double delay,
			String reqType, Boolean active, Integer serverId, Integer ston, Integer snpi, Boolean alert,
			String alertNumber, long expiryHour, Timestamp createdOn, String content, String msgType,
			String campaignName, String peId, String templateId, String telemarketerId, long msgCount, double cost,
			String userMode, String campaignType, String status, String updateBy, Timestamp updateOn, String remarks) {
		super();
		this.id = id;
		this.systemId = systemId;
		this.senderId = senderId;
		this.totalNum = totalNum;
		this.firstNum = firstNum;
		this.delay = delay;
		this.reqType = reqType;
		this.active = active;
		this.serverId = serverId;
		this.ston = ston;
		this.snpi = snpi;
		this.alert = alert;
		this.alertNumber = alertNumber;
		this.expiryHour = expiryHour;
		this.createdOn = createdOn;
		this.content = content;
		this.msgType = msgType;
		this.campaignName = campaignName;
		this.peId = peId;
		this.templateId = templateId;
		this.telemarketerId = telemarketerId;
		this.msgCount = msgCount;
		this.cost = cost;
		this.userMode = userMode;
		this.campaignType = campaignType;
		this.status = status;
		this.updateBy = updateBy;
		this.updateOn = updateOn;
		this.remarks = remarks;
	}

	/**
	 * 
	 */
	public BulkManagementEntity() {
		super();
	}

	@Override
	public String toString() {
		return "BulkManagementEntity [id=" + id + ", systemId=" + systemId + ", senderId=" + senderId + ", totalNum="
				+ totalNum + ", firstNum=" + firstNum + ", delay=" + delay + ", reqType=" + reqType + ", active="
				+ active + ", serverId=" + serverId + ", ston=" + ston + ", snpi=" + snpi + ", alert=" + alert
				+ ", alertNumber=" + alertNumber + ", expiryHour=" + expiryHour + ", createdOn=" + createdOn
				+ ", content=" + content + ", msgType=" + msgType + ", campaignName=" + campaignName + ", peId=" + peId
				+ ", templateId=" + templateId + ", telemarketerId=" + telemarketerId + ", msgCount=" + msgCount
				+ ", cost=" + cost + ", userMode=" + userMode + ", campaignType=" + campaignType + ", status=" + status
				+ ", updateBy=" + updateBy + ", updateOn=" + updateOn + ", remarks=" + remarks + "]";
	}

}
