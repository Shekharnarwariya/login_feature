package com.hti.smpp.common.response;

import java.time.LocalDateTime;

public class ExceptionResponse {

	private LocalDateTime timestamp;
	private int status;
	private String error;
	private String message;

	public ExceptionResponse(String message, LocalDateTime timestamp, int status, String error) {
		this.message = message;
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}