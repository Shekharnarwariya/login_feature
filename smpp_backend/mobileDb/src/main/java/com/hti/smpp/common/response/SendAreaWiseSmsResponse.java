package com.hti.smpp.common.response;

import java.util.List;

public class SendAreaWiseSmsResponse {

	 List numberList;
	 long TotalRecords;
	 String target;
	public List getNumberList() {
		return numberList;
	}
	public void setNumberList(List numberList) {
		this.numberList = numberList;
	}
	public long getTotalRecords() {
		return TotalRecords;
	}
	public void setTotalRecords(long totalRecords) {
		TotalRecords = totalRecords;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	 
	 
}
