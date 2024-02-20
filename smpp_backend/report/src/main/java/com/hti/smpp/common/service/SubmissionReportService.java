package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import com.hti.smpp.common.request.SubmissionReportRequest;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface SubmissionReportService {
	
	public ResponseEntity<?>execute(String username,SubmissionReportRequest customReportForm,HttpServletResponse response);
			
	
}
