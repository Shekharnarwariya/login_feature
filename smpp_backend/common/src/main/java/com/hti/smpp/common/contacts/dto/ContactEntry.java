package com.hti.smpp.common.contacts.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/**
 * Represents a contact entry with attributes such as ID, groupId, name, email, and number.
 */
@Entity
@Table(name = "addbook")
public class ContactEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "group_id")
	private int groupId;
	@Column(name = "name")
	private String name;
	@Column(name = "email")
	private String email;
	@Column(name = "number")
	private long number;
	@Column(name = "createdOn")
	private String createdOn;

	public ContactEntry() {
	}

	public ContactEntry(String name, long number, String email, int groupId, String createdOn) {
		this.name = name;
		this.number = number;
		this.email = email;
		this.groupId = groupId;
		this.createdOn = createdOn;
	}
	
	public ContactEntry(String name, long number, String email, int groupId) {
		this.name = name;
		this.number = number;
		this.email = email;
		this.groupId = groupId;
	}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}
	
	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	@Override
	public String toString() {
		return "ContactEntry [id=" + id + ", groupId=" + groupId + ", name=" + name + ", email=" + email + ", number="
				+ number + ", createdOn=" + createdOn + "]";
	}

}
