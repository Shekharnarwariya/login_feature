package com.hti.smpp.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "notification_template")
public class NotificationTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Size(min = 1, max = 255)
	@Column(name = "subject")
	private String subject;

	@NotNull
	@Size(min = 1, max = 255)
	@Lob
	@Column(name = "template_context", nullable = false)
	private String templateContext;

	@Column(name = "additional_setting")
	private String additionalSetting;

	@NotNull
	@Size(min = 1, max = 50)
	@Column(name = "template_type")
	private String templateType;

	@NotNull
	@Size(min = 1, max = 255)
	@Column(name = "event_name")
	private String eventName;

	// Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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
