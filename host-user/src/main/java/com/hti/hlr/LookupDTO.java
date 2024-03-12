/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.hlr;

/**
 *
 * @author Administrator
 */
public class LookupDTO extends ExtLookupDTO {
	private String msgid;
	private String hlrid;
	private String username;
	private String destination;
	private String flag;
	private int seqNum;
	private String submitTime;
	private String respTime;
	private String dlrTime;
	private String queryType;

	public LookupDTO() {
	}

	public LookupDTO(String msgid, String username, String destination, String flag, String submitTime, int seqNum,
			String queryType) {
		this.msgid = msgid;
		this.username = username;
		this.destination = destination;
		this.flag = flag;
		this.submitTime = submitTime;
		this.seqNum = seqNum;
		this.queryType = queryType;
	}

	public LookupDTO(String msgid, String flag, String status, String errorCode, String error, String respTime,
			String hlrid, String queryType) {
		super(status, errorCode, error);
		this.msgid = msgid;
		this.flag = flag;
		this.respTime = respTime;
		this.hlrid = hlrid;
		this.queryType = queryType;
	}

	public LookupDTO(String msgid, String flag, String status, String nnc, boolean ported, String portedNNC,
			boolean roaming, String roamingNNC, String errorCode, String error, String dlrTime, String queryType) {
		super(status, nnc, ported, portedNNC, roaming, roamingNNC, errorCode, error);
		this.msgid = msgid;
		this.flag = flag;
		this.dlrTime = dlrTime;
		this.queryType = queryType;
	}

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getHlrid() {
		return hlrid;
	}

	public void setHlrid(String hlrid) {
		this.hlrid = hlrid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public String getRespTime() {
		return respTime;
	}

	public void setRespTime(String respTime) {
		this.respTime = respTime;
	}

	public String getDlrTime() {
		return dlrTime;
	}

	public void setDlrTime(String dlrTime) {
		this.dlrTime = dlrTime;
	}

	public String getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(String submitTime) {
		this.submitTime = submitTime;
	}

	public String getQueryType() {
		return queryType;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
}
