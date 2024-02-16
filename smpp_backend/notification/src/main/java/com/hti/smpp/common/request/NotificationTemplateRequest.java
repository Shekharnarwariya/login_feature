package com.hti.smpp.common.request;

public class NotificationTemplateRequest {

	private String subject;
	private String templateContext;
	private String additionalSetting;
	private String templateType;
	private String eventName;

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTemplateContext() {
		return templateContext;
	}

	public void setTemplateContext(String templateContext) {
		this.templateContext = templateContext;
	}

	public String getAdditionalSetting() {
		return additionalSetting;
	}

	public void setAdditionalSetting(String additionalSetting) {
		this.additionalSetting = additionalSetting;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

}
