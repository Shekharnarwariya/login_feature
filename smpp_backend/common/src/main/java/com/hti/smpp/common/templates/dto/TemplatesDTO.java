package com.hti.smpp.common.templates.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * Entity class representing templates with JPA annotations.
 */
@Entity
@Table(name = "templatesmaster")
public class TemplatesDTO {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String message;
	private String masterId;
	private String title;
	@Column(name = "created_On")
	private Date createdOn; // New field for creation date

	@Column(name = "Updated_On")
	private Date updatedOn; // New field for update date

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	@Override
	public String toString() {
		return "TemplatesDTO [id=" + id + ", message=" + message + ", masterId=" + masterId + ", title=" + title
				+ ", createdOn=" + createdOn + ", updatedOn=" + updatedOn + "]";
	}

}
