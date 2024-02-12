package com.hti.smpp.common.responce;

import java.util.Date;

public class TemplatesResponse {
	private int id;
	private String message;
	private String masterId;
	private String title;
	 private  String createdOn;
	    private String updatedOn;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	  public String getCreatedOn() {
	        return createdOn;
	    }

	    public void setCreatedOn(String createdOn) {
	        this.createdOn = createdOn;
	    }

	    public String getUpdatedOn() {
	        return updatedOn;
	    }

	    public void setUpdatedOn(String updatedOn) {
	        this.updatedOn = updatedOn;
	    }
	    
	}