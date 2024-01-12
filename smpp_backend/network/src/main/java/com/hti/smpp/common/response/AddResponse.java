package com.hti.smpp.common.response;

import java.util.ArrayList;

import com.hti.smpp.common.dto.MccMncDTO;

public class AddResponse {
	
	private String responseMessage;
	private int totalRecords;
	private ArrayList<MccMncDTO> list;
	public AddResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	public AddResponse(String responseMessage, int totalRecords, ArrayList<MccMncDTO> list) {
		super();
		this.responseMessage = responseMessage;
		this.totalRecords = totalRecords;
		this.list = list;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	public int getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
	public ArrayList<MccMncDTO> getList() {
		return list;
	}
	public void setList(ArrayList<MccMncDTO> list) {
		this.list = list;
	}
	
	
}
