package com.hti.smpp.common.dto;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class SmscInDTO {
	private String msgid;
	private String time;
	private String seqno;
	private String content;
	private int dest_ton;
	private int dest_npi;
	private String destination;
	private String oprCountry;
	private int sour_ton;
	private int sour_npi;
	private String sourceno;
	private String registered;
	private int esm;
	private int dcs;
	private String cost;
	private String username;
	private String sflag;
	private String smsc;
	private String priority;
	private String sessionid;
	private String secondrysmscid;
	private String comment;

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getSeqno() {
		return seqno;
	}

	public void setSeqno(String seqno) {
		this.seqno = seqno;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getDest_ton() {
		return dest_ton;
	}

	public void setDest_ton(int dest_ton) {
		this.dest_ton = dest_ton;
	}

	public int getDest_npi() {
		return dest_npi;
	}

	public void setDest_npi(int dest_npi) {
		this.dest_npi = dest_npi;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getOprCountry() {
		return oprCountry;
	}

	public void setOprCountry(String oprCountry) {
		this.oprCountry = oprCountry;
	}

	public int getSour_ton() {
		return sour_ton;
	}

	public void setSour_ton(int sour_ton) {
		this.sour_ton = sour_ton;
	}

	public int getSour_npi() {
		return sour_npi;
	}

	public void setSour_npi(int sour_npi) {
		this.sour_npi = sour_npi;
	}

	public String getSourceno() {
		return sourceno;
	}

	public void setSourceno(String sourceno) {
		this.sourceno = sourceno;
	}

	public String getRegistered() {
		return registered;
	}

	public void setRegistered(String registered) {
		this.registered = registered;
	}

	public int getEsm() {
		return esm;
	}

	public void setEsm(int esm) {
		this.esm = esm;
	}

	public int getDcs() {
		return dcs;
	}

	public void setDcs(int dcs) {
		this.dcs = dcs;
	}

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSflag() {
		return sflag;
	}

	public void setSflag(String sflag) {
		this.sflag = sflag;
	}

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String Priority) {
		this.priority = Priority;
	}

	public String getSessionid() {
		return sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}

	public String getSecondrysmscid() {
		return secondrysmscid;
	}

	public void setSecondrysmscid(String Secondrysmscid) {
		this.secondrysmscid = Secondrysmscid;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
