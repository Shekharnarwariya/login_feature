package com.hti.smpp.common.management.dto;

/**
 * Entity bean with JPA annotations representing an entry related to bulk
 * management.
 */

public class BulkMgmtEntry {

	private String userMode;
	private long msgCount;
	private double totalCost;
	private String origMessage;
	private String campaignType;
	private String status;
	private String remarks;
	private String updateBy;
	private String updateOn;

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
	 * @return the totalCost
	 */
	public double getTotalCost() {
		return totalCost;
	}

	/**
	 * @param totalCost the totalCost to set
	 */
	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}

	/**
	 * @return the origMessage
	 */
	public String getOrigMessage() {
		return origMessage;
	}

	/**
	 * @param origMessage the origMessage to set
	 */
	public void setOrigMessage(String origMessage) {
		this.origMessage = origMessage;
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
	public String getUpdateOn() {
		return updateOn;
	}

	/**
	 * @param updateOn the updateOn to set
	 */
	public void setUpdateOn(String updateOn) {
		this.updateOn = updateOn;
	}

	@Override
	public String toString() {
		return "BulkMgmtEntry [userMode=" + userMode + ", msgCount=" + msgCount + ", totalCost=" + totalCost
				+ ", origMessage=" + origMessage + ", campaignType=" + campaignType + ", status=" + status
				+ ", remarks=" + remarks + ", updateBy=" + updateBy + ", updateOn=" + updateOn + "]";
	}

	/**
	 * @param userMode
	 * @param msgCount
	 * @param totalCost
	 * @param origMessage
	 * @param campaignType
	 * @param status
	 * @param remarks
	 * @param updateBy
	 * @param updateOn
	 */
	public BulkMgmtEntry(String userMode, long msgCount, double totalCost, String origMessage, String campaignType,
			String status, String remarks, String updateBy, String updateOn) {
		super();
		this.userMode = userMode;
		this.msgCount = msgCount;
		this.totalCost = totalCost;
		this.origMessage = origMessage;
		this.campaignType = campaignType;
		this.status = status;
		this.remarks = remarks;
		this.updateBy = updateBy;
		this.updateOn = updateOn;
	}

	/**
	 * 
	 */
	public BulkMgmtEntry() {
		super();

	}

}
