package com.hti.smpp.common.request;
//Import statements for required annotations
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
//Request class for handling template creation/update requests
public class TemplatesRequest {
	@NotBlank(message = "Message is required")
	private String message;
	 // Validation: Message should not be blank
	 // Validation: Title should not be blank and should not exceed 255 characters
	@NotBlank(message = "Title is required")
	@Size(max = 255, message = "Title cannot exceed 255 characters")
	private String title;
	  // Getter for retrieving the message
	public String getMessage() {
		return message;
	}
	  // Setter for setting the message
	public void setMessage(String message) {
		this.message = message;
	}
	 // Getter for retrieving the title
	public String getTitle() {
		return title;
	}
	 // Setter for setting the title
	public void setTitle(String title) {
		this.title = title;
	}
	// Parameterized constructor for initializing with message and title
	public TemplatesRequest(String message, String title) {
		super();
		this.message = message;
		this.title = title;
	}
	 // Default constructor
	public TemplatesRequest() {
		super();
	}
	 // Override toString for easy logging or debugging
	@Override
	public String toString() {
		return "TemplatesRequest [message=" + message + ", title=" + title + "]";
	}

}
