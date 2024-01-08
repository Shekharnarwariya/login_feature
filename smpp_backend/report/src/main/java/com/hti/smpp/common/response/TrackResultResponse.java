package com.hti.smpp.common.response;

import java.util.List;

public class TrackResultResponse {

	private String req_msgid;
	private String req_user;
	private String req_dest;
	private String req_sender;
	private String req_time;
	private String req_status;
	private String req_smsc;
	private List smscinlist;
	private List mislist;
	private int smscinsize;
	private int missize;
	private List routelist;
	private int routesize;
	private String status;

	/**
	 * @return the req_msgid
	 */
	public String getReq_msgid() {
		return req_msgid;
	}

	/**
	 * @param req_msgid the req_msgid to set
	 */
	public void setReq_msgid(String req_msgid) {
		this.req_msgid = req_msgid;
	}

	/**
	 * @return the req_user
	 */
	public String getReq_user() {
		return req_user;
	}

	/**
	 * @param req_user the req_user to set
	 */
	public void setReq_user(String req_user) {
		this.req_user = req_user;
	}

	/**
	 * @return the req_dest
	 */
	public String getReq_dest() {
		return req_dest;
	}

	/**
	 * @param req_dest the req_dest to set
	 */
	public void setReq_dest(String req_dest) {
		this.req_dest = req_dest;
	}

	/**
	 * @return the req_sender
	 */
	public String getReq_sender() {
		return req_sender;
	}

	/**
	 * @param req_sender the req_sender to set
	 */
	public void setReq_sender(String req_sender) {
		this.req_sender = req_sender;
	}

	/**
	 * @return the req_time
	 */
	public String getReq_time() {
		return req_time;
	}

	/**
	 * @param req_time the req_time to set
	 */
	public void setReq_time(String req_time) {
		this.req_time = req_time;
	}

	/**
	 * @return the req_status
	 */
	public String getReq_status() {
		return req_status;
	}

	/**
	 * @param req_status the req_status to set
	 */
	public void setReq_status(String req_status) {
		this.req_status = req_status;
	}

	/**
	 * @return the req_smsc
	 */
	public String getReq_smsc() {
		return req_smsc;
	}

	/**
	 * @param req_smsc the req_smsc to set
	 */
	public void setReq_smsc(String req_smsc) {
		this.req_smsc = req_smsc;
	}

	/**
	 * @return the smscinlist
	 */
	public List getSmscinlist() {
		return smscinlist;
	}

	/**
	 * @param smscinlist the smscinlist to set
	 */
	public void setSmscinlist(List smscinlist) {
		this.smscinlist = smscinlist;
	}

	/**
	 * @return the mislist
	 */
	public List getMislist() {
		return mislist;
	}

	/**
	 * @param mislist the mislist to set
	 */
	public void setMislist(List mislist) {
		this.mislist = mislist;
	}

	/**
	 * @return the smscinsize
	 */
	public int getSmscinsize() {
		return smscinsize;
	}

	/**
	 * @param smscinsize the smscinsize to set
	 */
	public void setSmscinsize(int smscinsize) {
		this.smscinsize = smscinsize;
	}

	/**
	 * @return the missize
	 */
	public int getMissize() {
		return missize;
	}

	/**
	 * @param missize the missize to set
	 */
	public void setMissize(int missize) {
		this.missize = missize;
	}

	/**
	 * @return the routelist
	 */
	public List getRoutelist() {
		return routelist;
	}

	/**
	 * @param routelist the routelist to set
	 */
	public void setRoutelist(List routelist) {
		this.routelist = routelist;
	}

	/**
	 * @return the routesize
	 */
	public int getRoutesize() {
		return routesize;
	}

	/**
	 * @param routesize the routesize to set
	 */
	public void setRoutesize(int routesize) {
		this.routesize = routesize;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

}
