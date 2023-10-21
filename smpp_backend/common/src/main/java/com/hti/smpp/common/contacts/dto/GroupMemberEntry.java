package com.hti.smpp.common.contacts.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "smsc_group_member")
public class GroupMemberEntry implements Serializable {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "group_id", nullable = false, updatable = false)
	private int groupId;
	@Column(name = "smsc_id")
	private int smscId;
	@Column(name = "percent")
	private int percent;
	@Transient
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
