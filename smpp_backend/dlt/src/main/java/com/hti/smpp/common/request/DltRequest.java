package com.hti.smpp.common.request;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;

//DltRequest represents a request object for DLT (Do Not Disturb) entry
//This class is a Plain Old Java Object (POJO) used for data transfer between client and server

public class DltRequest {

	// Unique identifier for the DLT entry
	private int id;
	
	// Username associated with the DLT entry
	 @NotBlank(message = "Username is required")
	private String username;
	
	// Sender information for the DLT entry
	 @NotBlank(message = "sender is required")
	private String sender;

	// Identifier for the PE (Principal Entity) associated with the DLT entry
	 @NotBlank(message = "Principal ID is required")
	private String peId;

	 // Telemarketer ID associated with the DLT entry
	 @NotBlank(message = "telemarketerId is required")
	private String telemarketerId;

	
	// Parameterized constructor to initialize the DltRequest object with values
	public DltRequest(int id, String username, String sender, String peId, String telemarketerId) {
		super();
		this.id = id;
		this.username = username;
		this.sender = sender;
		this.peId = peId;
		this.telemarketerId = telemarketerId;
	}

	// Getter method for retrieving the ID of the DLT entry
	public int getId() {
		return id;
	}

	// Setter method for setting the ID of the DLT entry
	public void setId(int id) {
		this.id = id;
	}

	 // Getter method for retrieving the username associated with the DLT entry
	public String getUsername() {
		return username;
	}

	// Setter method for setting the username associated with the DLT entry
	public void setUsername(String username) {
		this.username = username;
	}

	// Getter method for retrieving the sender information of the DLT entry
	public String getSender() {
		return sender;
	}

	// Setter method for setting the sender information of the DLT entry
	public void setSender(String sender) {
		this.sender = sender;
	}

	// Getter method for retrieving the PE ID associated with the DLT entry
	public String getPeId() {
		return peId;
	}

	// Setter method for setting the PE ID associated with the DLT entry
	public void setPeId(String peId) {
		this.peId = peId;
	}

	// Getter method for retrieving the telemarketer ID associated with the DLT entry
	public String getTelemarketerId() {
		return telemarketerId;
	}

	// Setter method for setting the telemarketer ID associated with the DLT entry
	public void setTelemarketerId(String telemarketerId) {
		this.telemarketerId = telemarketerId;
	}
	

}
