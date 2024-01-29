package com.hti.smpp.common.response;

import java.util.ArrayList;

public class QueryMobileRecordResponse {

	ArrayList<String> professionList;
	ArrayList<String> areaList;
	ArrayList<String> subAreaList;
	long professionListSize;
	long areaListSize;
	long subareaListSize;
	String target;
	
	public ArrayList<String> getProfessionList() {
		return professionList;
	}
	public void setProfessionList(ArrayList<String> professionList) {
		this.professionList = professionList;
	}
	public ArrayList<String> getAreaList() {
		return areaList;
	}
	public void setAreaList(ArrayList<String> areaList) {
		this.areaList = areaList;
	}
	public ArrayList<String> getSubAreaList() {
		return subAreaList;
	}
	public void setSubAreaList(ArrayList<String> subAreaList) {
		this.subAreaList = subAreaList;
	}
	public long getProfessionListSize() {
		return professionListSize;
	}
	public void setProfessionListSize(long professionListSize) {
		this.professionListSize = professionListSize;
	}
	public long getAreaListSize() {
		return areaListSize;
	}
	public void setAreaListSize(long areaListSize) {
		this.areaListSize = areaListSize;
	}
	public long getSubareaListSize() {
		return subareaListSize;
	}
	public void setSubareaListSize(long subareaListSize) {
		this.subareaListSize = subareaListSize;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	
	
	
}
