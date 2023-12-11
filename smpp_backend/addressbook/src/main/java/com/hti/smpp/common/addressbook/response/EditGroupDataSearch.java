package com.hti.smpp.common.addressbook.response;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class EditGroupDataSearch {

	private String target;
	private int groupId;
	private Set<String> professions;
	private Set<String> companies;
	private Set<String> areas;

	public EditGroupDataSearch() {
		super();
	}

	public EditGroupDataSearch(String target, int groupId, Set<String> professions, Set<String> companies,
			Set<String> areas) {
		super();
		this.target = target;
		this.groupId = groupId;
		this.professions = professions;
		this.companies = companies;
		this.areas = areas;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public Set<String> getProfessions() {
		return professions;
	}

	public void setProfessions(Set<String> professions) {
		this.professions = professions;
	}

	public Set<String> getCompanies() {
		return companies;
	}

	public void setCompanies(Set<String> companies) {
		this.companies = companies;
	}

	public Set<String> getAreas() {
		return areas;
	}

	public void setAreas(Set<String> areas) {
		this.areas = areas;
	}

}
