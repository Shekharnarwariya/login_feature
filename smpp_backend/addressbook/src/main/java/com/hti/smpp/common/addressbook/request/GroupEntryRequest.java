package com.hti.smpp.common.addressbook.request;
/**
 * Represents a request for a group entry.
 */
public class GroupEntryRequest {

	private int[] id;
	private String[] name;
	private boolean[] groupData;
	
//Default constructor for GroupEntryRequest.
	public GroupEntryRequest() {

	}
//Parameterized constructor for GroupEntryRequest
	public GroupEntryRequest(int[] id, String[] name, boolean[] groupData) {
		super();
		this.id = id;
		this.name = name;
		this.groupData = groupData;
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

	public boolean[] getGroupData() {
		return groupData;
	}

	public void setGroupData(boolean[] groupData) {
		this.groupData = groupData;
	}

}
