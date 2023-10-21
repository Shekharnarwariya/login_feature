package com.hti.smpp.common.responce;

public class TemplatesResponse {
	private int id;
	private String message;
	private String masterId;
	private String title;

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

	@Override
	public String toString() {
		return "TemplatesResponse [id=" + id + ", message=" + message + ", masterId=" + masterId + ", title=" + title
				+ "]";
	}

	public TemplatesResponse(int id, String message, String masterId, String title) {
		super();
		this.id = id;
		this.message = message;
		this.masterId = masterId;
		this.title = title;
	}

	public TemplatesResponse() {
		super();

	}

}
