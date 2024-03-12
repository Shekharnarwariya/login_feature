/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.msgContent;

import java.io.Serializable;

public class MsgContent implements Serializable {
	private String msg_id;
	private String username;
	private String msg_content;
	private String time;
	private int esm;
	private int dcs;
	private int total;
	private int partNumber;
	private int referenceNumber;

	public MsgContent(String msg_id, String username, String msg_content, String time, int esm, int dcs, int total,
			int partNumber, int referenceNumber) {
		this.msg_id = msg_id;
		this.username = username;
		this.msg_content = msg_content;
		this.time = time;
		this.esm = esm;
		this.dcs = dcs;
		this.total = total;
		this.partNumber = partNumber;
		this.referenceNumber = referenceNumber;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the msg_id
	 */
	public String getMsg_id() {
		return msg_id;
	}

	/**
	 * @param msg_id
	 *            the msg_id to set
	 */
	public void setMsg_id(String msg_id) {
		this.msg_id = msg_id;
	}
	/**
	 * @return the concate
	 */
	/*
	 * public ConcatUnicode getConcate() { return concate; }
	 */
	/**
	 * @param concate
	 *            the concate to set
	 */

	/*
	 * public void setConcate(ConcatUnicode concate) { this.concate = concate; }
	 */
	/**
	 * @return the msg_content
	 */
	public String getMsg_content() {
		return msg_content;
	}

	/**
	 * @param msg_content
	 *            the msg_content to set
	 */
	public void setMsg_content(String msg_content) {
		this.msg_content = msg_content;
	}

	/**
	 * @return the msg_type
	 */
	/*
	 * public String getMsg_type() { return msg_type; }
	 */
	/**
	 * @param msg_type
	 *            the msg_type to set
	 */
	/*
	 * public void setMsg_type(String msg_type) { this.msg_type = msg_type; }
	 */
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
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

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}

	public int getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(int referenceNumber) {
		this.referenceNumber = referenceNumber;
	}
}
