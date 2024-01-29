package com.hti.smpp.common.response;

import java.util.ArrayList;

public class EditDataResponse {

	private ArrayList<String> professionList;
	private ArrayList<String> areaList;
	private ArrayList<String> subAreaList;
	private String professionListSize;
	private String areaListSize;
	private String subareaListSize;
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
	public String getProfessionListSize() {
		return professionListSize;
	}
	public void setProfessionListSize(String professionListSize) {
		this.professionListSize = professionListSize;
	}
	public String getAreaListSize() {
		return areaListSize;
	}
	public void setAreaListSize(String areaListSize) {
		this.areaListSize = areaListSize;
	}
	public String getSubareaListSize() {
		return subareaListSize;
	}
	public void setSubareaListSize(String subareaListSize) {
		this.subareaListSize = subareaListSize;
	}
	
	
	
	
}
