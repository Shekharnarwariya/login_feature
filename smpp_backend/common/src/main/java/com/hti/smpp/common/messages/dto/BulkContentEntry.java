package com.hti.smpp.common.messages.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "batch_content_id")
public class BulkContentEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "destination")
	private long destination;
	@Column(name = "content")
	private String content;
	@Column(name = "flag", updatable = false)
	private String flag;

	public BulkContentEntry() {
	}

	public BulkContentEntry(long destination, String content, String flag) {
		this.destination = destination;
		this.content = content;
		this.flag = flag;
	}

	public BulkContentEntry(int id, String flag) {
		this.id = id;
		this.flag = flag;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getDestination() {
		return destination;
	}

	public void setDestination(long destination) {
		this.destination = destination;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
}
