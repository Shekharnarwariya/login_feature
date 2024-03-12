package com.hti.hlr;

public class RouteObject {
	private String msgId;
	private String smsc;
	private int groupId;
	private double cost;
	private int partNumber;
	private String sourceAddress;
	private String destAddress;
	private boolean rerouted;
	private boolean mnp;
	private int networkId;
	private boolean mms;

	public RouteObject(String msgId, String smsc, int groupId, double cost, int partNumber, String sourceAddress,
			String destAddress, boolean rerouted, boolean mnp, int networkId, boolean mms) {
		this.msgId = msgId;
		this.smsc = smsc;
		this.groupId = groupId;
		this.cost = cost;
		this.partNumber = partNumber;
		this.sourceAddress = sourceAddress;
		this.destAddress = destAddress;
		this.rerouted = rerouted;
		this.mnp = mnp;
		this.networkId = networkId;
		this.mms = mms;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public int getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}

	public String getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public String getDestAddress() {
		return destAddress;
	}

	public void setDestAddress(String destAddress) {
		this.destAddress = destAddress;
	}

	public boolean isRerouted() {
		return rerouted;
	}

	public void setRerouted(boolean rerouted) {
		this.rerouted = rerouted;
	}

	public boolean isMnp() {
		return mnp;
	}

	public void setMnp(boolean mnp) {
		this.mnp = mnp;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public boolean isMms() {
		return mms;
	}

	public void setMms(boolean mms) {
		this.mms = mms;
	}
	
	
}
