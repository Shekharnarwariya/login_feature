package com.hti.smpp.common.contacts.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Represents an entry in the addgroup table with attributes such as ID, masterId, name, groupData, createdBy, and a
 * transient field members.
 */

@Entity
@Table(name = "addgroup")
public class GroupEntryDTO {
	
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
	
	public GroupEntryDTO() {
	}
	
	public GroupEntryDTO(int id, String masterId, String name, boolean groupData, String createdBy, long members) {
		super();
		this.id = id;
		this.masterId = masterId;
		this.name = name;
		this.groupData = groupData;
		this.createdBy = createdBy;
		this.members = members;
	}
	
	public GroupEntryDTO(String name, String masterId, boolean groupData) {
		this.name = name;
		this.masterId = masterId;
		this.groupData = groupData;
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
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public long getMembers() {
		return members;
	}
	public void setMembers(long members) {
		this.members = members;
	}

}
