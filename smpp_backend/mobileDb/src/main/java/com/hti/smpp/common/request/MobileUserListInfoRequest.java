package com.hti.smpp.common.request;

public class MobileUserListInfoRequest {

	  private int ageMin;
	    private int ageMax;
	    private String areaArr[];
	    private String professionArr[];
	    private String classTypeArr[];
	    private String subareaArr[];
	    private String sex;
	    private String vip;
		public int getAgeMin() {
			return ageMin;
		}
		public void setAgeMin(int ageMin) {
			this.ageMin = ageMin;
		}
		public int getAgeMax() {
			return ageMax;
		}
		public void setAgeMax(int ageMax) {
			this.ageMax = ageMax;
		}
		public String[] getAreaArr() {
			return areaArr;
		}
		public void setAreaArr(String[] areaArr) {
			this.areaArr = areaArr;
		}
		public String[] getProfessionArr() {
			return professionArr;
		}
		public void setProfessionArr(String[] professionArr) {
			this.professionArr = professionArr;
		}
		public String[] getClassTypeArr() {
			return classTypeArr;
		}
		public void setClassTypeArr(String[] classTypeArr) {
			this.classTypeArr = classTypeArr;
		}
		public String[] getSubareaArr() {
			return subareaArr;
		}
		public void setSubareaArr(String[] subareaArr) {
			this.subareaArr = subareaArr;
		}
		public String getSex() {
			return sex;
		}
		public void setSex(String sex) {
			this.sex = sex;
		}
		public String getVip() {
			return vip;
		}
		public void setVip(String vip) {
			this.vip = vip;
		}
	    
}
