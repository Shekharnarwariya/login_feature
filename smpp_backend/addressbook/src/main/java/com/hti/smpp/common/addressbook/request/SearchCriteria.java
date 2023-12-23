package com.hti.smpp.common.addressbook.request;
/**
 * lass representing search criteria for filtering group data.
 */
public class SearchCriteria {

	private String[] company;
	private String[] profession;
	private String[] area;
	private String[] gender;
	private long[] number;
	private int minAge;
	private int maxAge;
	private int groupId;
	
//Default constructor for the SearchCriteria class.
	public SearchCriteria() {

	}
//Constructor for the SearchCriteria class with parameters.
	public SearchCriteria		(String[] company, String[] profession, String[] area, String[] gender, long[] number,
			int minAge, int maxAge, int groupId) {
		super();
		this.company = company;
		this.profession = profession;
		this.area = area;
		this.gender = gender;
		this.number = number;
		this.minAge = minAge;
		this.maxAge = maxAge;
		this.groupId = groupId;
	}
//getter and setter
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

	public long[] getNumber() {
		return number;
	}

	public void setNumber(long[] number) {
		this.number = number;
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

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

}
