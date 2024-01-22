package com.hti.smpp.common.dto;

public class UpdateMobileInfoDto {

	private int[] mobile_id;
    String[] vip;
    String[] mobileNumber;
    String[] sex;
    int[] age;
    int checkedC;
    private String[] oldMobileNumber;
    String[] area;
    String[] subarea;
    String[] profession;
    String[] select;
    String classType[];
    
    
	public int[] getMobile_id() {
		return mobile_id;
	}
	public void setMobile_id(int[] mobile_id) {
		this.mobile_id = mobile_id;
	}
	public String[] getVip() {
		return vip;
	}
	public void setVip(String[] vip) {
		this.vip = vip;
	}
	public String[] getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String[] mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String[] getSex() {
		return sex;
	}
	public void setSex(String[] sex) {
		this.sex = sex;
	}
	public int[] getAge() {
		return age;
	}
	public void setAge(int[] age) {
		this.age = age;
	}
	public int getCheckedC() {
		return checkedC;
	}
	public void setCheckedC(int checkedC) {
		this.checkedC = checkedC;
	}
	public String[] getOldMobileNumber() {
		return oldMobileNumber;
	}
	public void setOldMobileNumber(String[] oldMobileNumber) {
		this.oldMobileNumber = oldMobileNumber;
	}
	public String[] getArea() {
		return area;
	}
	public void setArea(String[] area) {
		this.area = area;
	}
	public String[] getSubarea() {
		return subarea;
	}
	public void setSubarea(String[] subarea) {
		this.subarea = subarea;
	}
	public String[] getProfession() {
		return profession;
	}
	public void setProfession(String[] profession) {
		this.profession = profession;
	}
	public String[] getSelect() {
		return select;
	}
	public void setSelect(String[] select) {
		this.select = select;
	}
	public String[] getClassType() {
		return classType;
	}
	public void setClassType(String[] classType) {
		this.classType = classType;
	}
    
    
}
