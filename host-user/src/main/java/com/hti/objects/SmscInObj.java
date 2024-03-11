package com.hti.objects;

public class SmscInObj {
	private String msgid;
	private String flag;
	private String smsc;
	private int groupId;
	private double cost;
	private String username;
	private int networkId;
	private String time;
	private String destination;
	private String source;
	private int esm;
	private String content;
	private int dcs;

	public SmscInObj(String msgid, String flag, String smsc, int groupId, double cost,int networkId) {
		this.msgid = msgid;
		this.flag = flag;
		this.smsc = smsc;
		this.groupId = groupId;
		this.cost = cost;
		this.networkId = networkId;
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

	public int getGroupId() {
		return groupId;
	}

	public double getCost() {
		return cost;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getEsm() {
		return esm;
	}

	public void setEsm(int esm) {
		this.esm = esm;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getDcs() {
		return dcs;
	}

	public void setDcs(int dcs) {
		this.dcs = dcs;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}
}
