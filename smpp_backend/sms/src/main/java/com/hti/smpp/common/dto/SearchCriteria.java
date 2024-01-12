package com.hti.smpp.common.dto;

public class SearchCriteria {
	private String[] company;
	private String[] profession;
	private String[] area;
	private String[] gender;
	private long[] number;
	private int minAge;
	private int maxAge;
	private Integer  groupId;

	public String[] getCompany() {
		return company;
	}

	public void setCompany(String[] company) {
		this.company = company;
	}

	public String[] getProfession() {
		return profession;
	}

	public void setProfession(String[] profession) {
		this.profession = profession;
	}

	public String[] getArea() {
		return area;
	}

	public void setArea(String[] area) {
		this.area = area;
	}

	public String[] getGender() {
		return gender;
	}

	public void setGender(String[] gender) {
		this.gender = gender;
	}

	public int getMinAge() {
		return minAge;
	}

	public void setMinAge(int minAge) {
		this.minAge = minAge;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	public Integer  getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer  groupId) {
		this.groupId = groupId;
	}

	public long[] getNumber() {
		return number;
	}

	public void setNumber(long[] number) {
		this.number = number;
	}
}
