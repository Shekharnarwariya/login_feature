package com.hti.smpp.common.response;

public class DltResponse {

private int id;
	
	private String username;
	
	private String sender;

	private String peId;

	private String telemarketerId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getPeId() {
		return peId;
	}

	public void setPeId(String peId) {
		this.peId = peId;
	}

	public String getTelemarketerId() {
		return telemarketerId;
	}

	public void setTelemarketerId(String telemarketerId) {
		this.telemarketerId = telemarketerId;
	}

	public DltResponse(int id, String username, String sender, String peId, String telemarketerId) {
		super();
		this.id = id;
		this.username = username;
		this.sender = sender;
		this.peId = peId;
		this.telemarketerId = telemarketerId;
	}

	public DltResponse() {
		super();
	}
	
	
	
	
}
