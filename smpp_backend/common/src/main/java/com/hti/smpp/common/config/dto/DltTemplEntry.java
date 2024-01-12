package com.hti.smpp.common.config.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/**
 * Represents the DLT (Do Not Disturb) template entry entity with attributes such as ID, template, templateId, and peId.
 */

@Entity
@Table(name = "dlt_templ")
public class DltTemplEntry {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", length = 15)
	private int id;
	@Column(name = "template", columnDefinition = "VARCHAR(5000)")
	private String template;
	@Column(name = "temp_id", length = 20)
	private String templateId;
	@Column(name = "pe_id")
	private String peId;

	public DltTemplEntry(String peId, String templateId, String template) {
		this.peId = peId;
		this.templateId = templateId;
		this.template = template;
	}

	public DltTemplEntry() {
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

	public String toString() {
		return "DltTemplEntry: id=" + id + ",template=" + template + ",PeId=" + peId + ",templateId=" + templateId;
	}
}
