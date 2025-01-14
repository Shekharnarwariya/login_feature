package com.hti.smpp.common.addressbook.request;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
/**
 * Represents a request for managing contact entries.
 */
public class ContactEntryRequest {
	private int[] id;
	private String[] name;
	private String[] email;
	private long[] number;
	private String type;
	private int groupId;
	@Schema(hidden = true)
	private MultipartFile file;
	
//Default constructor for the ContactEntryRequest class.
	public ContactEntryRequest() {

	}
//Parameterized constructor for the ContactEntryRequest class.
	public ContactEntryRequest(int[] id, String[] name, String[] email, long[] number, String type, int groupId,
			MultipartFile file) {
		super();
		this.id = id;
		this.name = name;
		this.email = email;
		this.number = number;
		this.type = type;
		this.groupId = groupId;
		this.file = file;
	}
//getter and setter
	public int[] getId() {
		return id;
	}

	public void setId(int[] id) {
		this.id = id;
	}

	public String[] getName() {
		return name;
	}

	public void setName(String[] name) {
		this.name = name;
	}

	public String[] getEmail() {
		return email;
	}

	public void setEmail(String[] email) {
		this.email = email;
	}

	public long[] getNumber() {
		return number;
	}

	public void setNumber(long[] number) {
		this.number = number;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

}
