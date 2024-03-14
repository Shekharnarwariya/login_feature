package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.PerformanceReportRequest;
import com.hti.smpp.common.response.DeliveryDTO;


import jakarta.servlet.http.HttpServletResponse;

@Service
public interface PerformanceReportService {
	
	public ResponseEntity<?> PerformanceReportview(String username, PerformanceReportRequest customReportForm);

	

	

}
