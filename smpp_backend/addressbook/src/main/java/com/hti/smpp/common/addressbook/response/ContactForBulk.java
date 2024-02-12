package com.hti.smpp.common.addressbook.response;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.hti.smpp.common.templates.dto.TemplatesDTO;

@Component
/**
 * Represents a contact for bulk operations.
 */
public class ContactForBulk {

	private List<Long> uploadedNumbers;
	private int totalNumbers;
	private List<TemplatesDTO> templates;
	private Set<String> senders;
	private String status;
	private int groupId;

	//Default constructor for the ContactForBulk class.
	public ContactForBulk() {

	}
    //getter and setter
	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
//Constructor for the ContactForBulk class.

	public ContactForBulk(List<Long> uploadedNumbers, int totalNumbers, List<TemplatesDTO> templates, Set<String> senders,
			String status, int groupId) {
		super();
		this.uploadedNumbers = uploadedNumbers;
		this.totalNumbers = totalNumbers;
		this.templates = templates;
		this.senders = senders;
		this.status = status;
		this.groupId = groupId;
	}
//getter and setter
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Long> getUploadedNumbers() {
		return uploadedNumbers;
	}
	public void setUploadedNumbers(List<Long> uploadedNumbers) {
		this.uploadedNumbers = uploadedNumbers;
	}
	public int getTotalNumbers() {
		return totalNumbers;
	}

	public void setTotalNumbers(int totalNumbers) {
		this.totalNumbers = totalNumbers;
	}

	public List<TemplatesDTO> getTemplates() {
		return templates;
	}

	public void setTemplates(List<TemplatesDTO> templates) {
		this.templates = templates;
	}

	public Set<String> getSenders() {
		return senders;
	}

	public void setSenders(Set<String> senders) {
		this.senders = senders;
	}

}
