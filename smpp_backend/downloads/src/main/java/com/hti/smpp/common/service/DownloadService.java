package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;

public interface DownloadService {
	
	ResponseEntity<?> downloadPricing(String format,String username);

}
