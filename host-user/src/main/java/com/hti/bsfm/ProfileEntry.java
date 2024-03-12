package com.hti.bsfm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ProfileEntry {
	private int id;
	private Set<String> smsc;
	private Set<String> username;
	private boolean reverse;
	private String reroute;
	private Set<String> source;
	private Set<String> prefix;
	private Set<String> content;
	private boolean schedule;
	private boolean activeOnSchedule;
	private Map<Integer, Map<String, String>> schedules;
	private String remarks;
	private String forceSenderId;
	private int rerouteGroupId;
	private int msgLength;
	private int lengthOpr;

	public ProfileEntry() {
	}

	public ProfileEntry(int id, boolean reverse, String reroute, String remarks, String forceSenderId,
			int rerouteGroupId) {
		this.id = id;
		this.reverse = reverse;
		this.reroute = reroute;
		this.remarks = remarks;
		this.forceSenderId = forceSenderId;
		this.rerouteGroupId = rerouteGroupId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Set<String> getSmsc() {
		if (smsc == null) {
			smsc = new HashSet<String>();
		}
		return smsc;
	}

	public void setSmsc(Set<String> smsc) {
		this.smsc = smsc;
	}

	public Set<String> getUsername() {
		if (username == null) {
			username = new HashSet<String>();
		}
		return username;
	}

	public void setUsername(Set<String> username) {
		this.username = username;
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

	public Set<String> getSource() {
		if (source == null) {
			source = new HashSet<String>();
		}
		return source;
	}

	public void setSource(Set<String> source) {
		this.source = source;
	}

	public Set<String> getPrefix() {
		if (prefix == null) {
			prefix = new HashSet<String>();
		}
		return prefix;
	}

	public void setPrefix(Set<String> prefix) {
		this.prefix = prefix;
	}

	public Set<String> getContent() {
		if (content == null) {
			content = new HashSet<String>();
		}
		return content;
	}

	public void setContent(Set<String> content) {
		this.content = content;
	}

	public boolean isSchedule() {
		return schedule;
	}

	public void setSchedule(boolean schedule) {
		this.schedule = schedule;
	}

	public boolean isActiveOnSchedule() {
		return activeOnSchedule;
	}

	public void setActiveOnSchedule(boolean activeOnSchedule) {
		this.activeOnSchedule = activeOnSchedule;
	}

	public Map<Integer, Map<String, String>> getSchedules() {
		if (schedules == null) {
			schedules = new TreeMap<Integer, Map<String, String>>();
		}
		return schedules;
	}

	public void setSchedules(Map<Integer, Map<String, String>> schedules) {
		this.schedules = schedules;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
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

	public String toString() {
		return "Profile: id=" + id + ",reverse=" + reverse + ",reroute=" + reroute + ",schedule=" + schedule+",forceSenderId="+forceSenderId;
	}
}
