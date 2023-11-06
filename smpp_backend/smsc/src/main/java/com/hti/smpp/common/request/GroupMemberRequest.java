package com.hti.smpp.common.request;

public class GroupMemberRequest {
	private int[] id;
	private int groupId;
	private int[] smscId;
	private int[] percent;

	public int[] getId() {
		return id;
	}

	public void setId(int[] id) {
		this.id = id;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int[] getSmscId() {
		return smscId;
	}

	public void setSmscId(int[] smscId) {
		this.smscId = smscId;
	}

	public int[] getPercent() {
		return percent;
	}

	public void setPercent(int[] percent) {
		this.percent = percent;
	}
}
