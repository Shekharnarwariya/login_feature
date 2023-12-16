package com.hti.smpp.common.addressbook.request;

public class GroupEntryRequest {

	private int[] id;
	private String[] name;
	private boolean[] groupData;

	public GroupEntryRequest() {

	}

	public GroupEntryRequest(int[] id, String[] name, boolean[] groupData) {
		super();
		this.id = id;
		this.name = name;
		this.groupData = groupData;
	}

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