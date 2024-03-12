package com.hti.objects;

public class RequestLog {
	private String msgId;
	private int sequence;
	private String content;
	private String destination;
	private String source;
	private byte registered;
	private byte esm;
	private byte dcs;
	private String systemId;
	private String ipAddress;

	public RequestLog(String msgId, int sequence, String content, String destination, String source, byte registered,
			byte esm, byte dcs, String systemId, String ipAddress) {
		this.msgId = msgId;
		this.sequence = sequence;
		this.content = content;
		this.destination = destination;
		this.source = source;
		this.registered = registered;
		this.esm = esm;
		this.dcs = dcs;
		this.systemId = systemId;
		this.ipAddress = ipAddress;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

	public byte getRegistered() {
		return registered;
	}

	public void setRegistered(byte registered) {
		this.registered = registered;
	}

	public byte getEsm() {
		return esm;
	}

	public void setEsm(byte esm) {
		this.esm = esm;
	}

	public byte getDcs() {
		return dcs;
	}

	public void setDcs(byte dcs) {
		this.dcs = dcs;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
}
