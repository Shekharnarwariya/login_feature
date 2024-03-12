package com.hti.smpp.common.request;


public class PasswordLinkEntry {
	private int id;
	private String linkId;
	private String hashId;
	private String expiresOn;
	private String systemId;

	public PasswordLinkEntry(String systemId, String linkId, String hashId, String expiresOn) {
		this.systemId = systemId;
		this.linkId = linkId;
		this.hashId = hashId;
		this.expiresOn = expiresOn;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public String getHashId() {
		return hashId;
	}

	public void setHashId(String hashId) {
		this.hashId = hashId;
	}

	public String getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(String expiresOn) {
		this.expiresOn = expiresOn;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String toString() {
		return "linkEntry: id=" + id + ",linkId=" + linkId + ",hashId=" + hashId + ",systemId=" + systemId
				+ ",expiresOn=" + expiresOn;
	}
}
