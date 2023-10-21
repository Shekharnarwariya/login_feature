package com.hti.smpp.common.contacts.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "groupcontacts")
public class GroupDataEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "group_id")
	private int groupId;
	@Column(name = "initials")
	private String initials;
	@Column(name = "email")
	private String email;
	@Column(name = "number")
	private long number;
	@Column(name = "firstName")
	private String firstName;
	@Column(name = "middleName")
	private String middleName;
	@Column(name = "lastName")
	private String lastName;
	@Column(name = "age")
	private int age;
	@Column(name = "company")
	private String company;
	@Column(name = "profession")
	private String profession;
	@Column(name = "area")
	private String area;
	@Column(name = "gender")
	private String gender;

	public GroupDataEntry() {
	}

	public GroupDataEntry(int groupId, String initials, String firstName, String middleName, String lastName,
			long number, String email, int age, String profession, String company, String area, String gender) {
		this.groupId = groupId;
		this.initials = initials;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.email = email;
		this.number = number;
		this.age = age;
		this.company = company;
		this.profession = profession;
		this.area = area;
		this.gender = gender;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getProfession() {
		return profession;
	}

	public void setProfession(String profession) {
		this.profession = profession;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String toString() {
		return "GroupDataEntry: id=" + id + ",groupId=" + groupId + ",name=" + firstName + ",Email=" + email
				+ ",number=" + number + ",age=" + age + ",profession=" + profession + ",company=" + company + ",area="
				+ area + ",gender=" + gender;
	}
}
