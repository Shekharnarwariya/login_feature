package com.hti.smpp.common.util.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "addgroup")
public class GroupEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "masterid", updatable = false)
	private String masterId;
	@Column(name = "name")
	private String name;
	@Column(name = "groupData")
	private boolean groupData;
	@Column(name = "created_by")
	private String createdBy;
	@Transient
	private long members;

	public GroupEntry(String name, String masterId, boolean groupData) {
		this.name = name;
		this.masterId = masterId;
		this.groupData = groupData;
	}

	public GroupEntry() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isGroupData() {
		return groupData;
	}

	public void setGroupData(boolean groupData) {
		this.groupData = groupData;
	}

	public long getMembers() {
		return members;
	}

	public void setMembers(long members) {
		this.members = members;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String toString() {
		return "addGroup: id=" + id + ",master=" + masterId + ",name=" + name + ",groupData=" + groupData;
	}
}