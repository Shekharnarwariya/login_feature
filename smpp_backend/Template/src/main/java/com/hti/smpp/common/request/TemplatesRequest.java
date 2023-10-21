package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TemplatesRequest {
	@NotBlank(message = "Message is required")
	private String message;

	@NotBlank(message = "Title is required")
	@Size(max = 255, message = "Title cannot exceed 255 characters")
	private String title;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public TemplatesRequest(String message, String title) {
		super();
		this.message = message;
		this.title = title;
	}

	public TemplatesRequest() {
		super();
	}

	@Override
	public String toString() {
		return "TemplatesRequest [message=" + message + ", title=" + title + "]";
	}

}
