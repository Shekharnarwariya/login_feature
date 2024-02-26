package com.hti.smpp.common.twoway.request;

public class SearchCriteria {
	
	private String search;
	private String start;
	private String end;
	private String type;
	
	public SearchCriteria() {
		super();
	}
	public SearchCriteria(String search, String start, String end, String type) {
		super();
		this.search = search;
		this.start = start;
		this.end = end;
		this.type = type;
	}
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	}
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
