package com.hti.smsc.dto;

import java.io.Serializable;

public class GroupEntry implements Serializable {
	private int id;
	private String name;
	private int duration;
	private int checkDuration;
	private int checkVolume;
	private int noOfRepeat;
	private int keepRepeatDays;
	private int primeMember;
	private String remarks;
	private String primeMemberName;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getCheckDuration() {
		return checkDuration;
	}

	public void setCheckDuration(int checkDuration) {
		this.checkDuration = checkDuration;
	}

	public int getCheckVolume() {
		return checkVolume;
	}

	public void setCheckVolume(int checkVolume) {
		this.checkVolume = checkVolume;
	}

	public int getNoOfRepeat() {
		return noOfRepeat;
	}

	public void setNoOfRepeat(int noOfRepeat) {
		this.noOfRepeat = noOfRepeat;
	}

	public int getKeepRepeatDays() {
		return keepRepeatDays;
	}

	public void setKeepRepeatDays(int keepRepeatDays) {
		this.keepRepeatDays = keepRepeatDays;
	}

	public int getPrimeMember() {
		return primeMember;
	}

	public void setPrimeMember(int primeMember) {
		this.primeMember = primeMember;
	}

	public String getPrimeMemberName() {
		return primeMemberName;
	}

	public void setPrimeMemberName(String primeMemberName) {
		this.primeMemberName = primeMemberName;
	}

	public String toString() {
		return "SmscGroup: id=" + id + ",name=" + name + ",remarks=" + remarks;
	}
}
