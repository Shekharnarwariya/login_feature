package com.hti.smpp.common.addressbook.request;

import org.springframework.web.multipart.MultipartFile;

public class GroupDataEntryRequest {
	
	private int[] id;
	private String[] initials;
	private String[] email;
	private long[] number;
	private String[] firstName;
	private String[] middleName;
	private String[] lastName;
	private int[] age;
	private String[] company;
	private String[] profession;
	private String[] area;
	private String[] gender;
	private int groupId;
	private String type;
	private MultipartFile contactNumberFile;
	private int minAge;
	private int maxAge;
	public GroupDataEntryRequest(int[] id, String[] initials, String[] email, long[] number, String[] firstName,
			String[] middleName, String[] lastName, int[] age, String[] company, String[] profession, String[] area,
			String[] gender, int groupId, String type, MultipartFile contactNumberFile, int minAge, int maxAge) {
		super();
		this.id = id;
		this.initials = initials;
		this.email = email;
		this.number = number;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.age = age;
		this.company = company;
		this.profession = profession;
		this.area = area;
		this.gender = gender;
		this.groupId = groupId;
		this.type = type;
		this.contactNumberFile = contactNumberFile;
		this.minAge = minAge;
		this.maxAge = maxAge;
	}
	public GroupDataEntryRequest() {
		
	}
	public int[] getId() {
		return id;
	}
	public void setId(int[] id) {
		this.id = id;
	}
	public String[] getInitials() {
		return initials;
	}
	public void setInitials(String[] initials) {
		this.initials = initials;
	}
	public String[] getEmail() {
		return email;
	}
	public void setEmail(String[] email) {
		this.email = email;
	}
	public long[] getNumber() {
		return number;
	}
	public void setNumber(long[] number) {
		this.number = number;
	}
	public String[] getFirstName() {
		return firstName;
	}
	public void setFirstName(String[] firstName) {
		this.firstName = firstName;
	}
	public String[] getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String[] middleName) {
		this.middleName = middleName;
	}
	public String[] getLastName() {
		return lastName;
	}
	public void setLastName(String[] lastName) {
		this.lastName = lastName;
	}
	public int[] getAge() {
		return age;
	}
	public void setAge(int[] age) {
		this.age = age;
	}
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
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public MultipartFile getContactNumberFile() {
		return contactNumberFile;
	}
	public void setContactNumberFile(MultipartFile contactNumberFile) {
		this.contactNumberFile = contactNumberFile;
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

}
