package com.hti.smpp.common.response;

import java.util.List;

import com.hti.smpp.common.dto.MccMncDTO;

public class SearchResponse {
	
	private List<MccMncDTO> responseList;
	private PaginationResponse pagiResponse;
	public SearchResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	public SearchResponse(List<MccMncDTO> responseList, PaginationResponse pagiResponse) {
		super();
		this.responseList = responseList;
		this.pagiResponse = pagiResponse;
	}
	public List<MccMncDTO> getResponseList() {
		return responseList;
	}
	public void setResponseList(List<MccMncDTO> responseList) {
		this.responseList = responseList;
	}
	public PaginationResponse getPagiResponse() {
		return pagiResponse;
	}
	public void setPagiResponse(PaginationResponse pagiResponse) {
		this.pagiResponse = pagiResponse;
	}
	

}
