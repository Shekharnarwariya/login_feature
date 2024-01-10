package com.hti.smpp.common.request;
//DltTempRequest represents a request object for DLT (Do Not Disturb) template entry
//This class is a Plain Old Java Object (POJO) used for data transfer between client and server
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;


public class DltTempRequest {

	// Unique identifier for the DLT template entry
	private int id;

	 // Content of the DLT template
	 @NotBlank(message = "template is required")
	private String template;

	 // Identifier for the DLT template
	 @NotBlank(message = "templateId is required")
	private String templateId;

	 // Identifier for the PE (Principal Entity) associated with the DLT template entry
	 @NotBlank(message = "Principal ID is required")
	private String peId;
	
	 // MultipartFile object representing the template file
	private MultipartFile templateFile;

	 // Default constructor
	public DltTempRequest() {
		
    }
	
	// Parameterized constructor to initialize the DltTempRequest object with values
	public DltTempRequest(int id, String template, String templateId, String peId, MultipartFile templateFile) {
		super();
		this.id = id;
		this.template = template;
		this.templateId = templateId;
		this.peId = peId;
		this.templateFile = templateFile;
	}

	 // Getter method for retrieving the MultipartFile representing the template file
	public MultipartFile getTemplateFile() {
		return templateFile;
	}

	 // Setter method for setting the MultipartFile representing the template file
	public void setTemplateFile(MultipartFile templateFile) {
		this.templateFile = templateFile;
	}

	// Getter method for retrieving the ID of the DLT template entry
	public int getId() {
		return id;
	}

	// Setter method for setting the ID of the DLT template entry
	public void setId(int id) {
		this.id = id;
	}

	 // Getter method for retrieving the content of the DLT template
	public String getTemplate() {
		return template;
	}

	 // Setter method for setting the content of the DLT template
	public void setTemplate(String template) {
		this.template = template;
	}

	// Getter method for retrieving the template ID
	public String getTemplateId() {
		return templateId;
	}

	// Setter method for setting the template ID
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	// Getter method for retrieving the PE ID associated with the DLT template entry
	public String getPeId() {
		return peId;
	}

	 // Setter method for setting the PE ID associated with the DLT template entry
	public void setPeId(String peId) {
		this.peId = peId;
	}
	
	
}
