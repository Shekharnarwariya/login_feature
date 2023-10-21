package com.hti.smpp.common.payload.response;

import java.time.LocalDateTime;

public class ExceptionResponse {

	private String message;

	private LocalDateTime date;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public ExceptionResponse(String message, LocalDateTime date) {
		super();
		this.message = message;
		this.date = date;
	}

	public ExceptionResponse() {
		super();
	}

}
