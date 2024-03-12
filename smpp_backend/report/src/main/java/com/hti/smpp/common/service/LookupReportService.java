package com.hti.smpp.common.service;



import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import com.hti.smpp.common.request.LookUpReportRequest;



import jakarta.servlet.http.HttpServletResponse;
@Service
public interface LookupReportService {

	public ResponseEntity<?>  LookupReportview(String username, LookUpReportRequest customReportForm);


	

	public ResponseEntity<?> LookupReportRecheck(String username, LookUpReportRequest customReportForm, 
			HttpServletResponse response);
	

}
