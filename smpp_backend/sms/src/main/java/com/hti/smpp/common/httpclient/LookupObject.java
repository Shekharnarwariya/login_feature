/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smpp.common.httpclient;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class LookupObject implements Serializable {
	private String systemid;
	private List<String> list;
	private String batchid;
	private int serverId;
	private String password;

	public String getSystemid() {
		return systemid;
	}

	public void setSystemid(String systemid) {
		this.systemid = systemid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBatchid() {
		return batchid;
	}

	public void setBatchid(String batchid) {
		this.batchid = batchid;
	}

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String toString() {
		return "lookupBatch: id=" + batchid + ",ServerId=" + serverId + ",systemId=" + systemid;
	}
}
