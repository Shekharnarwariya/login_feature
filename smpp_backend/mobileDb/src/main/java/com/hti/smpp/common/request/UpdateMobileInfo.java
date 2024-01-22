package com.hti.smpp.common.request;

public class UpdateMobileInfo {

	
	  private int[] mobile_id;
	    private String[] vip;
	    private int checkedC;
	    private String[] oldMobileNumber;
	    private String[] mobileNumber;
	    private String[] sex;
	    private int[] age;
	    private String[] area;
	    private String[] subarea;
	    private String[] profession;
	    private String selectAll;
	    private String totalRecords;
	    
	    
	    
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
		public String getSelectAll() {
			return selectAll;
		}
		public void setSelectAll(String selectAll) {
			this.selectAll = selectAll;
		}
		public String getTotalRecords() {
			return totalRecords;
		}
		public void setTotalRecords(String totalRecords) {
			this.totalRecords = totalRecords;
		}
	    
	    
}
