package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportDTO;
import com.hti.smpp.common.request.CustomReportForm;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface SubmissionReportService {
	
	public List<CustomReportDTO>execute(String username,CustomReportForm customReportForm,HttpServletResponse response);
			
	
}
