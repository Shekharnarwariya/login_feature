package com.hti.smpp.common.response;

import java.util.List;

import com.hti.smpp.common.contacts.dto.GroupEntryDTO;

public class ScheduleEditResponse {

	private List<GroupEntryDTO> groupList;

	private String listSizeStr;

	private List<String> tempList;

	private String gmt;

	private String gmtValue;

	private String schaduleTime;

	private String msg;

	private String senderId;

	private String filename;

	private String reqType;

	private String delay;

	private String repeat;

	private String username;

	private String expiry;

	/**
	 * @return the groupList
	 */
	public List<GroupEntryDTO> getGroupList() {
		return groupList;
	}

	/**
	 * @param groupList the groupList to set
	 */
	public void setGroupList(List<GroupEntryDTO> groupList) {
		this.groupList = groupList;
	}

	/**
	 * @return the listSizeStr
	 */
	public String getListSizeStr() {
		return listSizeStr;
	}

	/**
	 * @param listSizeStr the listSizeStr to set
	 */
	public void setListSizeStr(String listSizeStr) {
		this.listSizeStr = listSizeStr;
	}

	/**
	 * @return the tempList
	 */
	public List<String> getTempList() {
		return tempList;
	}

	/**
	 * @param tempList the tempList to set
	 */
	public void setTempList(List<String> tempList) {
		this.tempList = tempList;
	}

	/**
	 * @return the gmt
	 */
	public String getGmt() {
		return gmt;
	}

	/**
	 * @param gmt the gmt to set
	 */
	public void setGmt(String gmt) {
		this.gmt = gmt;
	}

	/**
	 * @return the gmtValue
	 */
	public String getGmtValue() {
		return gmtValue;
	}

	/**
	 * @param gmtValue the gmtValue to set
	 */
	public void setGmtValue(String gmtValue) {
		this.gmtValue = gmtValue;
	}

	/**
	 * @return the schaduleTime
	 */
	public String getSchaduleTime() {
		return schaduleTime;
	}

	/**
	 * @param schaduleTime the schaduleTime to set
	 */
	public void setSchaduleTime(String schaduleTime) {
		this.schaduleTime = schaduleTime;
	}

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
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
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
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
	 * @return the delay
	 */
	public String getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(String delay) {
		this.delay = delay;
	}

	/**
	 * @return the repeat
	 */
	public String getRepeat() {
		return repeat;
	}

	/**
	 * @param repeat the repeat to set
	 */
	public void setRepeat(String repeat) {
		this.repeat = repeat;
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
	 * @return the expiry
	 */
	public String getExpiry() {
		return expiry;
	}

	/**
	 * @param expiry the expiry to set
	 */
	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

}
