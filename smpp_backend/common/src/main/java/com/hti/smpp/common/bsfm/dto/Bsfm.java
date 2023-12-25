
package com.hti.smpp.common.bsfm.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents the Bsfm entity with attributes such as ID, profilename, username, content, smsc, prefixes, sourceid,
 * active, reverse, reroute, editBy, editOn, schedule, dayTime, activeOnScheduleTime, priority, senderType,
 * masterId, forceSenderId, rerouteGroupId, rerouteGroupName, msgLength, lengthOpr, and networks.
 */

@Entity
@Table(name = "bsfm_master")
public class Bsfm {
	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String profilename;
	private String username;
	private String content;
	private String smsc;
	private String prefixes;
	private String sourceid;
	private boolean active;
	private boolean reverse;
	private String reroute;
	private String editBy;
	private String editOn;
	private boolean schedule;
	private String dayTime;
	private boolean activeOnScheduleTime;
	private int priority;
	private String senderType;
	private String masterId;
	private String forceSenderId;
	private int rerouteGroupId;
	private String rerouteGroupName;
	private int msgLength;
	private int lengthOpr;
	private String networks;

	public String getRerouteGroupName() {
		return rerouteGroupName;
	}

	public void setRerouteGroupName(String rerouteGroupName) {
		this.rerouteGroupName = rerouteGroupName;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getForceSenderId() {
		return forceSenderId;
	}

	public void setForceSenderId(String forceSenderId) {
		this.forceSenderId = forceSenderId;
	}

	public int getRerouteGroupId() {
		return rerouteGroupId;
	}

	public void setRerouteGroupId(int rerouteGroupId) {
		this.rerouteGroupId = rerouteGroupId;
	}

	public boolean isActiveOnScheduleTime() {
		return activeOnScheduleTime;
	}

	public void setActiveOnScheduleTime(boolean activeOnScheduleTime) {
		this.activeOnScheduleTime = activeOnScheduleTime;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public String getReroute() {
		return reroute;
	}

	public void setReroute(String reroute) {
		this.reroute = reroute;
	}

	/**
	 * @return the profilename
	 */
	public String getProfilename() {
		return profilename;
	}

	/**
	 * @param profilename the profilename to set
	 */
	public void setProfilename(String profilename) {
		this.profilename = profilename;
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
	 * @return the smsc
	 */
	public String getSmsc() {
		return smsc;
	}

	/**
	 * @param smsc the smsc to set
	 */
	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	/**
	 * @return the prefixes
	 */
	public String getPrefixes() {
		return prefixes;
	}

	/**
	 * @param prefixes the prefixes to set
	 */
	public void setPrefixes(String prefixes) {
		this.prefixes = prefixes;
	}

	/**
	 * @return the sourceid
	 */
	public String getSourceid() {
		return sourceid;
	}

	/**
	 * @param sourceid the sourceid to set
	 */
	public void setSourceid(String sourceid) {
		this.sourceid = sourceid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getEditBy() {
		return editBy;
	}

	public void setEditBy(String editBy) {
		this.editBy = editBy;
	}

	public String getEditOn() {
		return editOn;
	}

	public void setEditOn(String editOn) {
		this.editOn = editOn;
	}

	public boolean isSchedule() {
		return schedule;
	}

	public void setSchedule(boolean schedule) {
		this.schedule = schedule;
	}

	public String getDayTime() {
		return dayTime;
	}

	public void setDayTime(String dayTime) {
		this.dayTime = dayTime;
	}

	public String getSenderType() {
		return senderType;
	}

	public void setSenderType(String senderType) {
		this.senderType = senderType;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public int getMsgLength() {
		return msgLength;
	}

	public void setMsgLength(int msgLength) {
		this.msgLength = msgLength;
	}

	public int getLengthOpr() {
		return lengthOpr;
	}

	public void setLengthOpr(int lengthOpr) {
		this.lengthOpr = lengthOpr;
	}

	public String getNetworks() {
		return networks;
	}

	public void setNetworks(String networks) {
		this.networks = networks;
	}

	public String toString() {
		return "filter: id=" + id + ",name=" + profilename + ",active=" + active + ",reverse=" + reverse + ",schedule="
				+ schedule;
	}
}
