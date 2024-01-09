package com.hti.smpp.common.request;

import org.springframework.web.multipart.MultipartFile;


public class DltTempRequest {

	private int id;

	private String template;

	private String templateId;

	private String peId;
	
	private MultipartFile templateFile;

	public DltTempRequest() {
		
    }
	
	
	public DltTempRequest(int id, String template, String templateId, String peId, MultipartFile templateFile) {
		super();
		this.id = id;
		this.template = template;
		this.templateId = templateId;
		this.peId = peId;
		this.templateFile = templateFile;
	}

	public MultipartFile getTemplateFile() {
		return templateFile;
	}

	public void setTemplateFile(MultipartFile templateFile) {
		this.templateFile = templateFile;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getPeId() {
		return peId;
	}

	public void setPeId(String peId) {
		this.peId = peId;
	}
	
	
}
