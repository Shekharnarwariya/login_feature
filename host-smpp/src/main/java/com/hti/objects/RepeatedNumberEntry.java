package com.hti.objects;

public class RepeatedNumberEntry {
	private int id;
	private int count;
	private String number;
	private int groupId;

	public RepeatedNumberEntry() {
	}

	public RepeatedNumberEntry(int groupId, String number, int count) {
		this.groupId = groupId;
		this.number = number;
		this.count = count;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String toString() {
		return "Repeated: id=" + id + ",groupId=" + groupId + ",number=" + number + ",count=" + count;
	}
}
