package com.hti.smpp.common.dto;

public class BsfmDto {
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
	
	public BsfmDto() {
	}

	public BsfmDto(String profilename, String username, String content, String smsc, String prefixes, String sourceid,
			boolean active, boolean reverse, String reroute, String editBy, String editOn, boolean schedule,
			String dayTime, boolean activeOnScheduleTime, int priority, String senderType, String masterId,
			String forceSenderId, int rerouteGroupId, String rerouteGroupName, int msgLength, int lengthOpr,
			String networks) {
		super();
		this.profilename = profilename;
		this.username = username;
		this.content = content;
		this.smsc = smsc;
		this.prefixes = prefixes;
		this.sourceid = sourceid;
		this.active = active;
		this.reverse = reverse;
		this.reroute = reroute;
		this.editBy = editBy;
		this.editOn = editOn;
		this.schedule = schedule;
		this.dayTime = dayTime;
		this.activeOnScheduleTime = activeOnScheduleTime;
		this.priority = priority;
		this.senderType = senderType;
		this.masterId = masterId;
		this.forceSenderId = forceSenderId;
		this.rerouteGroupId = rerouteGroupId;
		this.rerouteGroupName = rerouteGroupName;
		this.msgLength = msgLength;
		this.lengthOpr = lengthOpr;
		this.networks = networks;
	}

	public String getProfilename() {
		return profilename;
	}

	public void setProfilename(String profilename) {
		this.profilename = profilename;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public String getPrefixes() {
		return prefixes;
	}

	public void setPrefixes(String prefixes) {
		this.prefixes = prefixes;
	}

	public String getSourceid() {
		return sourceid;
	}

	public void setSourceid(String sourceid) {
		this.sourceid = sourceid;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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

	public boolean isActiveOnScheduleTime() {
		return activeOnScheduleTime;
	}

	public void setActiveOnScheduleTime(boolean activeOnScheduleTime) {
		this.activeOnScheduleTime = activeOnScheduleTime;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
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

	public String getRerouteGroupName() {
		return rerouteGroupName;
	}

	public void setRerouteGroupName(String rerouteGroupName) {
		this.rerouteGroupName = rerouteGroupName;
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

	@Override
	public String toString() {
		return "BsfmDto [profilename=" + profilename + ", username=" + username + ", content=" + content + ", smsc="
				+ smsc + ", prefixes=" + prefixes + ", sourceid=" + sourceid + ", active=" + active + ", reverse="
				+ reverse + ", reroute=" + reroute + ", editBy=" + editBy + ", editOn=" + editOn + ", schedule="
				+ schedule + ", dayTime=" + dayTime + ", activeOnScheduleTime=" + activeOnScheduleTime + ", priority="
				+ priority + ", senderType=" + senderType + ", masterId=" + masterId + ", forceSenderId="
				+ forceSenderId + ", rerouteGroupId=" + rerouteGroupId + ", rerouteGroupName=" + rerouteGroupName
				+ ", msgLength=" + msgLength + ", lengthOpr=" + lengthOpr + ", networks=" + networks + "]";
	}
	
	
}
