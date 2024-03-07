package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

public interface DownloadService {
	
	ResponseEntity<?> downloadPricing(String format,String username);
	public ResponseEntity<List<Object>> downloadPricingInList(String username,String startDate,String endDate);

}
