package com.hti.smpp.common.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaginationRequest {

	@Min(0)
	private int pageNumber;
	
	@Min(1)
	private int pageSize;

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

	@Override
	public String toString() {
		return "PaginationRequest [pageNumber=" + pageNumber + ", pageSize=" + pageSize + "]";
	}
	

}