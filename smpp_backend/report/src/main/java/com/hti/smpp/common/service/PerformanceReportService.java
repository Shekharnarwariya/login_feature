package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;


import jakarta.servlet.http.HttpServletResponse;

@Service
public interface PerformanceReportService {
	
	public List<DeliveryDTO> PerformanceReportview(String username, CustomReportForm customReportForm, String lang);

	public String PerformanceReportxls(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);

	public String PerformanceReportPdf(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);

	public String PerformanceReportDoc(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);
	

	

}
