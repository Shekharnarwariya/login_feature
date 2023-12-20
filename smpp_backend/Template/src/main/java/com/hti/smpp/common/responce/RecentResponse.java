package com.hti.smpp.common.responce;

import java.sql.Date;

public class RecentResponse {
	private String message;
	private Date date;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public RecentResponse(String message, Date date) {
		super();
		this.message = message;
		this.date = date;
	}

	public RecentResponse() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "RecentResponse [message=" + message + ", date=" + date + "]";
	}

}
