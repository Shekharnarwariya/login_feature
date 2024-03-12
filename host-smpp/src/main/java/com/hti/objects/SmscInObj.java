package com.hti.objects;

public class SmscInObj {
	private String msgid;
	private String flag;
	private String smsc;
	private String username;
	private int groupId;

	public SmscInObj(String msgid, String flag, String smsc, int group_id, String username) {
		this.msgid = msgid;
		this.flag = flag;
		this.smsc = smsc;
		this.username = username;
		this.groupId = group_id;
	}

	public int getGroupId() {
		return groupId;
	}

	public String getMsgid() {
		return msgid;
	}

	public String getFlag() {
		return flag;
	}

	public String getSmsc() {
		return smsc;
	}

	public String getUsername() {
		return username;
	}
}
