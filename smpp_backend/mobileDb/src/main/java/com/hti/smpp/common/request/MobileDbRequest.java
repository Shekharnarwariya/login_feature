package com.hti.smpp.common.request;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;

public class MobileDbRequest {

	private int mob_id;
    private String age_temp;
    private int ageMin;
    private int ageMax;
    private String viewAs;
    private int subareaArrCount;
    private String areaArr[];
    private String professionArr[];
    private String classTypeArr[];
    private String subareaArr[];
    private String areaWiseNumber[];
    private String mobileNumber;
    private String sex;
    private String vip;
    private String area;
    private String subarea;
    private String classType;
    private String profession;
    private MultipartFile mobileNumbersList;
    private String listType;
    private String actionReq;
    private String actionDo;
    private String sendNowMsgCount;
    private String daysPart;
    private String oldMobileNumber;
    private int age;
    private String select;
    private long delay;
    private int areaArrCount;
    private int professionArrCount;
    private Map<?,?> numberMap;
    private List<?> numberList;
    
    
	public int getMob_id() {
		return mob_id;
	}
	public void setMob_id(int mob_id) {
		this.mob_id = mob_id;
	}
	public String getAge_temp() {
		return age_temp;
	}
	public void setAge_temp(String age_temp) {
		this.age_temp = age_temp;
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
	public String getViewAs() {
		return viewAs;
	}
	public void setViewAs(String viewAs) {
		this.viewAs = viewAs;
	}
	public int getSubareaArrCount() {
		return subareaArrCount;
	}
	public void setSubareaArrCount(int subareaArrCount) {
		this.subareaArrCount = subareaArrCount;
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
	public String[] getAreaWiseNumber() {
		return areaWiseNumber;
	}
	public void setAreaWiseNumber(String[] areaWiseNumber) {
		this.areaWiseNumber = areaWiseNumber;
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
	public String getSubarea() {
		return subarea;
	}
	public void setSubarea(String subarea) {
		this.subarea = subarea;
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
    
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getSelect() {
		return select;
	}
	public void setSelect(String select) {
		this.select = select;
	}
	public long getDelay() {
		return delay;
	}
	public void setDelay(long delay) {
		this.delay = delay;
	}
	public int getAreaArrCount() {
		return areaArrCount;
	}
	public void setAreaArrCount(int areaArrCount) {
		this.areaArrCount = areaArrCount;
	}
	public int getProfessionArrCount() {
		return professionArrCount;
	}
	public void setProfessionArrCount(int professionArrCount) {
		this.professionArrCount = professionArrCount;
	}
	public Map<?, ?> getNumberMap() {
		return numberMap;
	}
	public void setNumberMap(Map<?, ?> numberMap) {
		this.numberMap = numberMap;
	}
	public List<?> getNumberList() {
		return numberList;
	}
	public void setNumberList(List<?> numberList) {
		this.numberList = numberList;
	}
    
	
	
	
	
}
