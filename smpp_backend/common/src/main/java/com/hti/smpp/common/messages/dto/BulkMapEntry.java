package com.hti.smpp.common.messages.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity bean with JPA annotations
 */
@Entity
@Table(name = "campaign_mapping")
public class BulkMapEntry implements Serializable {
	@Id
	@Column(name = "msg_id", length = 20)
	private long msgid;
	@Column(name = "system_id", length = 10)
	private String systemId;
	@Column(name = "campaign")
	private String name;

	public BulkMapEntry() {
	}

	public BulkMapEntry(long msgid, String systemId, String name) {
		this.msgid = msgid;
		this.systemId = systemId;
		this.name = name;
	}

	public long getMsgid() {
		return msgid;
	}

	public void setMsgid(long msgid) {
		this.msgid = msgid;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return "BulkMapEntry: msgId" + msgid + ",systemId=" + systemId + ",name=" + name;
	}
}
