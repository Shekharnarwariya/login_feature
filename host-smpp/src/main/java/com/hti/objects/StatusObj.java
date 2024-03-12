/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.objects;

/**
 *
 * @author Administrator
 */
public class StatusObj extends LogPDU implements Cloneable {
	private String deliverOn;
	private String status;
	private String errorCode;
	private String flag;

	public StatusObj(LogPDU logPDU) {
		setMsgid(logPDU.getMsgid());
		setResponseid(logPDU.getResponseid());
		setRoute(logPDU.getRoute());
		setUsername(logPDU.getUsername());
		setCost(logPDU.getCost());
		// setRefund(logPDU.isRefund());
		setSource(logPDU.getSource());
		setOrigSource(logPDU.getOrigSource());
		setDestination(logPDU.getDestination());
		setOprCountry(logPDU.getOprCountry());
		setSubmitOn(logPDU.getSubmitOn());
	}

	public String getDeliverOn() {
		return deliverOn;
	}

	public void setDeliverOn(String deliverOn) {
		this.deliverOn = deliverOn;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFlag() {
		return flag;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public String toString() {
		return "StatusObj: Msgid=" + getMsgid() + ",username=" + getUsername() + ",Route=" + getRoute() + ",responseId="
				+ getResponseid() + ",status=" + status;
	}
}
