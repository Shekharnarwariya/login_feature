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
	
	public ResponseEntity<?> PerformanceReportview(String username, PerformanceReportRequest customReportForm, String lang);

	public ResponseEntity<?> PerformanceReportxls(String username, PerformanceReportRequest customReportForm, String lang,
			HttpServletResponse response);

	public ResponseEntity<?> PerformanceReportPdf(String username, PerformanceReportRequest customReportForm, String lang,
			HttpServletResponse response);

	public ResponseEntity<?> PerformanceReportDoc(String username, PerformanceReportRequest customReportForm, String lang,
			HttpServletResponse response);
	

	

}
