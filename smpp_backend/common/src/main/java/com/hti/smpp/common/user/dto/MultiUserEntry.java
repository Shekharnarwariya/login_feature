package com.hti.smpp.common.user.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "multi_user_access")
public class MultiUserEntry {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "user_id")
	private int userId;

	@Column(name = "access_name")
	private String accessName;

	@Column(name = "mobile")
	private String mobile;

	@Column(name = "email")
	private String email;

	public MultiUserEntry() {
	}

	public MultiUserEntry(int id) {
		this.id = id;
	}

	public MultiUserEntry(int userId, String accessName, String mobile, String email) {
		this.userId = userId;
		this.accessName = accessName;
		this.mobile = mobile;
		this.email = email;
	}

	public MultiUserEntry(int id, int userId, String accessName, String mobile, String email) {
		this.id = id;
		this.userId = userId;
		this.accessName = accessName;
		this.mobile = mobile;
		this.email = email;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getAccessName() {
		return accessName;
	}

	public void setAccessName(String accessName) {
		this.accessName = accessName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String toString() {
		return "MultiUserEntry:id=" + id + ",userId=" + userId + ",accessname=" + accessName + ",mobile=" + mobile
				+ ",email=" + email;
	}
}
