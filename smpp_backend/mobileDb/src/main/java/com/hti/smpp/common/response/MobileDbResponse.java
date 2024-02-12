package com.hti.smpp.common.response;

import org.springframework.web.multipart.MultipartFile;

public class MobileDbResponse {

	
	private String mobileNumber;
    private String select;
    private String sex;
    private String vip;
    private String area;
    private String classType;
    private String profession;
    private MultipartFile mobileNumbersList;
    private String listType;
    private String actionReq;
    private String actionDo;
    private String sendNowMsgCount;
    private String daysPart;
    private String oldMobileNumber;
    private String subarea;
    private int age;
    private int areaArrCount;
    private int subareaArrCount;
    private int professionArrCount;
    private String viewAs = "NoPopUp";
    private long delay;
    private int ageMin;
    private int ageMax;
    private String areaArr[];
    private String subareaArr[];
    private String professionArr[];
    private String classTypeArr[];
    private String areaWiseNumber[];
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getSelect() {
		return select;
	}
	public void setSelect(String select) {
		this.select = select;
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
	public MultipartFile getMobileNumbersList() {
		return mobileNumbersList;
	}
	public void setMobileNumbersList(MultipartFile mobileNumbersList) {
		this.mobileNumbersList = mobileNumbersList;
	}
	public String getListType() {
		return listType;
	}
	public void setListType(String listType) {
		this.listType = listType;
	}
	public String getActionReq() {
		return actionReq;
	}
	public void setActionReq(String actionReq) {
		this.actionReq = actionReq;
	}
	public String getActionDo() {
		return actionDo;
	}
	public void setActionDo(String actionDo) {
		this.actionDo = actionDo;
	}
	public String getSendNowMsgCount() {
		return sendNowMsgCount;
	}
	public void setSendNowMsgCount(String sendNowMsgCount) {
		this.sendNowMsgCount = sendNowMsgCount;
	}
	public String getDaysPart() {
		return daysPart;
	}
	public void setDaysPart(String daysPart) {
		this.daysPart = daysPart;
	}
	public String getOldMobileNumber() {
		return oldMobileNumber;
	}
	public void setOldMobileNumber(String oldMobileNumber) {
		this.oldMobileNumber = oldMobileNumber;
	}
	public String getSubarea() {
		return subarea;
	}
	public void setSubarea(String subarea) {
		this.subarea = subarea;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public int getAreaArrCount() {
		return areaArrCount;
	}
	public void setAreaArrCount(int areaArrCount) {
		this.areaArrCount = areaArrCount;
	}
	public int getSubareaArrCount() {
		return subareaArrCount;
	}
	public void setSubareaArrCount(int subareaArrCount) {
		this.subareaArrCount = subareaArrCount;
	}
	public int getProfessionArrCount() {
		return professionArrCount;
	}
	public void setProfessionArrCount(int professionArrCount) {
		this.professionArrCount = professionArrCount;
	}
	public String getViewAs() {
		return viewAs;
	}
	public void setViewAs(String viewAs) {
		this.viewAs = viewAs;
	}
	public long getDelay() {
		return delay;
	}
	public void setDelay(long delay) {
		this.delay = delay;
	}
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
	public String[] getSubareaArr() {
		return subareaArr;
	}
	public void setSubareaArr(String[] subareaArr) {
		this.subareaArr = subareaArr;
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
	public String[] getAreaWiseNumber() {
		return areaWiseNumber;
	}
	public void setAreaWiseNumber(String[] areaWiseNumber) {
		this.areaWiseNumber = areaWiseNumber;
	}
    
    
    
}
