package com.hti.smsc.dto;

import java.io.Serializable;

public class GroupMemberEntry implements Serializable {
	private int id;
	private int groupId;
	private int smscId;
	private int percent;
	private String smsc;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public String toString() {
		return "SmscGroupMember: id=" + id + ",groupId=" + groupId + ",smscId=" + smscId + ",percent=" + percent;
	}
}
