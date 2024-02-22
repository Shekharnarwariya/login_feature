package com.hti.smpp.common.addressbook.request;

import java.util.Arrays;

/**
 * Represents a request for a group entry.
 */
public class GroupEntryRequest {
	int[] id;
	private String[] name;
	private boolean[] groupData;

//Default constructor for GroupEntryRequest.
	public GroupEntryRequest() {

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

	public int[] getId() {
		return id;
	}

	public void setId(int[] id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "GroupEntryRequest [id=" + Arrays.toString(id) + ", name=" + Arrays.toString(name) + ", groupData="
				+ Arrays.toString(groupData) + "]";
	}

}
