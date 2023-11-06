package com.hti.smpp.common.contacts.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "smsc_group")
public class GroupEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "name")
	private String name;
	@Column(name = "duration")
	private int duration;
	@Column(name = "check_duration")
	private int checkDuration;
	@Column(name = "check_volume")
	private int checkVolume;
	@Column(name = "no_of_repeat")
	private int noOfRepeat;
	@Column(name = "keep_repeat_day")
	private int keepRepeatDays;
	@Column(name = "prime_member")
	private int primeMember;
	@Column(name = "remarks")
	private String remarks;
	@Transient
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
