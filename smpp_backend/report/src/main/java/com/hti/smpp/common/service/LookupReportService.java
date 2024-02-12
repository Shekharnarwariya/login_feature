package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.LookUpReportRequest;

import com.hti.smpp.common.rmi.dto.LookupReport;

import jakarta.servlet.http.HttpServletResponse;
@Service
public interface LookupReportService {

	public ResponseEntity<?>  LookupReportview(String username, LookUpReportRequest customReportForm, String lang);

	public ResponseEntity<?> LookupReportxls(String username, LookUpReportRequest customReportForm, String lang,
			HttpServletResponse response);

	public ResponseEntity<?> LookupReportPdf(String username, LookUpReportRequest customReportForm, String lang,
			HttpServletResponse response);

	public ResponseEntity<?> LookupReportDoc(String username, LookUpReportRequest customReportForm, String lang,
			HttpServletResponse response);
	

	public ResponseEntity<?> LookupReportRecheck(String username, LookUpReportRequest customReportForm, String lang,
			HttpServletResponse response);
	

}
