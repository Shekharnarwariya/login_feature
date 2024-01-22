package com.hti.smpp.common.mobileDb.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mobileuserdata")
public class MobileDbEntity {

	@Id
	@Column(name = "mob_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int mobile_id;
	@Column(name = "mobNumber")
	private String mobileNumber;
	@Column(name = "sex")
	private String sex;
	@Column(name = "age")
	private int age;
	@Column(name = "vip")
	private String vip;
	@Column(name = "area")
	private String area;
	@Column(name = "classType")
	private String classType;
	@Column(name = "profession")
	private String profession;
	@Column(name = "subArea")
	private String subArea;
	
	
	public int getMobile_id() {
		return mobile_id;
	}
	public void setMobile_id(int mobile_id) {
		this.mobile_id = mobile_id;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getVip() {
		return vip;
	}
	public void setVip(String vip) {
		this.vip = vip;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = classType;
	}
	public String getProfession() {
		return profession;
	}
	public void setProfession(String profession) {
		this.profession = profession;
	}
	public String getSubArea() {
		return subArea;
	}
	public void setSubArea(String subArea) {
		this.subArea = subArea;
	}
	
	
	@Override
	public String toString() {
		return "MobileDbEntity [mobile_id=" + mobile_id + ", mobileNumber=" + mobileNumber + ", sex=" + sex + ", age="
				+ age + ", vip=" + vip + ", area=" + area + ", classType=" + classType + ", profession=" + profession
				+ ", subArea=" + subArea + "]";
	}
	
	

	
}
