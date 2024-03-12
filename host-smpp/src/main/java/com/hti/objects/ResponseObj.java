package com.hti.objects;

import java.io.Serializable;
import java.util.Date;

public class ResponseObj implements Serializable, Cloneable {
	private String msgid;
	private String responseid;
	private String status;
	private String flag;
	private boolean mappedid;
	private boolean mis;
	private int commandid;
	private Date time;
	private String errorCode;
	private String oldRoute;
	private int delayDlr;

	public ResponseObj(String msgid, String responseid, String status, String flag, boolean mappedid, boolean mis,
			int commandid, String oldRoute) {
		this.msgid = msgid;
		this.responseid = responseid;
		this.status = status;
		this.flag = flag;
		this.mappedid = mappedid;
		this.mis = mis;
		this.commandid = commandid;
		this.oldRoute = oldRoute;
	}

	public ResponseObj(String msgid, String responseid, String status, Date time, String errorCode, int commandid) {
		this.msgid = msgid;
		this.responseid = responseid;
		this.status = status;
		this.time = time;
		this.errorCode = errorCode;
		this.commandid = commandid;
	}

	public int getDelayDlr() {
		return delayDlr;
	}

	public void setDelayDlr(int delayDlr) {
		this.delayDlr = delayDlr;
	}

	public String getOldRoute() {
		return oldRoute;
	}

	public void setOldRoute(String oldRoute) {
		this.oldRoute = oldRoute;
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

	public String getStatus() {
		return status;
	}

	public String getFlag() {
		return flag;
	}

	public boolean isMappedid() {
		return mappedid;
	}

	public boolean isMis() {
		return mis;
	}

	public int getCommandid() {
		return commandid;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public String toString() {
		return "msgid: " + msgid + ", responseid: " + responseid + ", status:" + status + " flag: " + flag
				+ ", commandid: " + commandid;
	}
}
