/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smpp.common.util;

import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class BatchObject implements Serializable {
	private int id;
	private String systemId;
	private boolean active;
	private int serverId;

	public BatchObject() {
	}

	public BatchObject(int id, String systemId, int serverId, boolean active) {
		this.id = id;
		this.systemId = systemId;
		this.serverId = serverId;
		this.active = active;
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

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String toString() {
		return "BatchObject:serverId=" + serverId + ",systemId=" + systemId + ",batchId=" + id + " active=" + active;
	}
}
