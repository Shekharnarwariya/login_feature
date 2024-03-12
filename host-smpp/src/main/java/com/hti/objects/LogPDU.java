/*
 * LogPDU.java
 *
 * Created on 12 March 2004, 15:38
 */
package com.hti.objects;

import java.io.Serializable;

/**
 *
 * @author administrator
 */
public class LogPDU implements Serializable, Cloneable {
	private String msgid;
	private String responseid;
	private String route;
	private String username;
	private byte registerdlr;
	private String oprCountry;
	private String destination;
	private String source;
	private String origSource;
	private double cost;
	private boolean refund;
	private String submitOn;
	private int serverId;
	private int groupId;
	private boolean rerouted;
	private String routedSmsc;

	public LogPDU() {
	}

	public LogPDU(String msgid, String route, String username, byte registerdlr, String destination, String source,
			String origSource, String oprCountry, String submitOn, double cost, boolean refund, int serverId,
			int groupId, boolean rerouted, String routedSmsc) {
		this.msgid = msgid;
		this.route = route;
		this.username = username;
		this.registerdlr = registerdlr;
		this.destination = destination;
		this.source = source;
		this.origSource = origSource;
		this.oprCountry = oprCountry;
		this.submitOn = submitOn;
		this.cost = cost;
		this.refund = refund;
		this.serverId = serverId;
		this.groupId = groupId;
		this.rerouted = rerouted;
		this.routedSmsc = routedSmsc;
	}

	public LogPDU(String msgid, String route, String username, String destination, String source, String submitOn,
			double cost, boolean refund, int serverId, int groupId) {
		this.msgid = msgid;
		this.route = route;
		this.username = username;
		this.registerdlr = 1;
		this.destination = destination;
		this.source = source;
		this.submitOn = submitOn;
		this.cost = cost;
		this.refund = refund;
		this.groupId = groupId;
	}

	public int getGroupId() {
		return groupId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getOrigSource() {
		return origSource;
	}

	public void setOrigSource(String origSource) {
		this.origSource = origSource;
	}

	public String getOprCountry() {
		return oprCountry;
	}

	public void setOprCountry(String oprCountry) {
		this.oprCountry = oprCountry;
	}

	public String getSubmitOn() {
		return submitOn;
	}

	public void setSubmitOn(String submitOn) {
		this.submitOn = submitOn;
	}

	public boolean isRefund() {
		return refund;
	}

	public void setRefund(boolean refund) {
		this.refund = refund;
	}

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getResponseid() {
		return responseid;
	}

	public void setResponseid(String responseid) {
		this.responseid = responseid;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public byte getRegisterdlr() {
		return registerdlr;
	}

	public void setRegisterdlr(byte registerdlr) {
		this.registerdlr = registerdlr;
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

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public boolean isRerouted() {
		return rerouted;
	}

	public void setRerouted(boolean rerouted) {
		this.rerouted = rerouted;
	}

	public String getRoutedSmsc() {
		return routedSmsc;
	}

	public void setRoutedSmsc(String routedSmsc) {
		this.routedSmsc = routedSmsc;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	/*
	 * public boolean isIsCached() { return isCached; }
	 * 
	 * public void setIsCached(boolean isCached) { this.isCached = isCached; }
	 */
}
