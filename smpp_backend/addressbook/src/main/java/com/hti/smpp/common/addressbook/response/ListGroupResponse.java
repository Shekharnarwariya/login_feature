package com.hti.smpp.common.addressbook.response;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hti.smpp.common.contacts.dto.GroupEntryDTO;

import io.micrometer.observation.transport.Propagator.Getter;

@Component
/**
 * Represents the response for listing groups.
 */
public class ListGroupResponse {

	private List<GroupEntryDTO> list;
	private String target;
	private String criteria;
	
//Default constructor for ListGroupResponse.
	public ListGroupResponse() {
		super();
	}
//Constructor for ListGroupResponse with parameters
	public ListGroupResponse(List<GroupEntryDTO> list, String target, String criteria) {
		super();
		this.list = list;
		this.target = target;
		this.criteria = criteria;
	}
//Getter and setter
	public List<GroupEntryDTO> getList() {
		return list;
	}

	public void setList(List<GroupEntryDTO> list) {
		this.list = list;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getCriteria() {
		return criteria;
	}

	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

}
