package com.hti.smpp.common.messages.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity bean with JPA annotations
 */
@Entity
@Table(name = "batch_process")
public class HlrBulkEntry implements Serializable {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "server_id")
	private int serverId;
	@Column(name = "username")
	private String systemId;
	@Column(name = "batchid")
	private String batchid;
	@Column(name = "total")
	private int total;

	public HlrBulkEntry() {
	}

	public HlrBulkEntry(String batchid, String systemId, int serverId, int total) {
		this.batchid = batchid;
		this.systemId = systemId;
		this.serverId = serverId;
		this.total = total;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getBatchid() {
		return batchid;
	}

	public void setBatchid(String batchid) {
		this.batchid = batchid;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public String toString() {
		return "id=" + id + ",systemId=" + systemId + ",batchid=" + batchid + ",total=" + total;
	}
}
