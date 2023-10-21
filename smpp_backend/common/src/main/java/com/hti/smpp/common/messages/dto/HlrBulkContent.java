package com.hti.smpp.common.messages.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "batch_content_id")
public class HlrBulkContent implements Serializable {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "destination", nullable = false, updatable = false)
	private long destination;
	@Column(name = "flag")
	private String flag;

	public HlrBulkContent() {
	}

	public HlrBulkContent(long destination, String flag) {
		this.destination = destination;
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

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String toString() {
		return "HlrContent: id=" + id + ",destination=" + destination;
	}
}
