package com.hti.smpp.common.service;



import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import com.hti.smpp.common.request.SummaryReportForm;


import jakarta.servlet.http.HttpServletResponse;

@Service
public interface SummaryReportService {
	
	public ResponseEntity<?> SummaryReportview(String username, SummaryReportForm customReportForm, String lang);
	
	public ResponseEntity<?>  SummaryReportxls(String username, SummaryReportForm customReportForm,
			HttpServletResponse response,String lang);
	
	public ResponseEntity<?>  SummaryReportpdf(String username, SummaryReportForm customReportForm,
			HttpServletResponse response,String lang);
	
	public ResponseEntity<?>  SummaryReportdoc(String username, SummaryReportForm customReportForm,
			HttpServletResponse response,String lang);
	
	

}
