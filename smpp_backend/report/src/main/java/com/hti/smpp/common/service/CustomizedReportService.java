package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomizedReportRequest;
import com.hti.smpp.common.request.SmsReportRequest;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface CustomizedReportService {
	public ResponseEntity<?> CustomizedReportView(String username, CustomizedReportRequest customReportForm);

	

	public ResponseEntity<?> SmsReport(String username, SmsReportRequest smsReportRequest, String searchParameter) ;

}
