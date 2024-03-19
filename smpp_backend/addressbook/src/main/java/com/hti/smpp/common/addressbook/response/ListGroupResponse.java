package com.hti.smpp.common.addressbook.response;



import java.util.List;
import org.springframework.stereotype.Component;

import com.hti.smpp.common.contacts.dto.GroupEntryDTO;


import jakarta.validation.constraints.Min;

@Component
/**
 * Represents the response for listing groups.
 */
public class ListGroupResponse {

	private List<GroupEntryDTO> list;
	
	private String target;
	private String criteria;
	
	@Min(0)
	private int pageNumber;
	
	@Min(1)
	private int pageSize;
	
	private int totalPages;
	
	private boolean firstPage;
	
	private boolean lastPage;
	
//Default constructor for ListGroupResponse.
	public ListGroupResponse() {
		super();
	}
	
//Constructor for ListGroupResponse with parameters
	public ListGroupResponse(List<GroupEntryDTO> list, String target, String criteria,int pageNumber,int pageSize,int totalPages,boolean firstPage,boolean lastPage) {
		super();
		this.list = list;
		this.target = target;
		this.criteria = criteria;
		this.pageNumber=pageNumber;
		this.pageSize=pageSize;
		this.firstPage=firstPage;
		this.lastPage=lastPage;
		this.totalPages=totalPages;
	}
	
//Getter and setter
	public List<GroupEntryDTO> getList() {
		return list;
	}

	public void setList(List<GroupEntryDTO> list) {
		this.list = list;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getCriteria() {
		return criteria;
	}

	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public boolean isFirstPage() {
		return firstPage;
	}

	public void setFirstPage(boolean firstPage) {
		this.firstPage = firstPage;
	}

	public boolean isLastPage() {
		return lastPage;
	}

	public void setLastPage(boolean lastPage) {
		this.lastPage = lastPage;
	}


}
