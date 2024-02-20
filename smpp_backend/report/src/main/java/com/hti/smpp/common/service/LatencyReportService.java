package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import com.hti.smpp.common.request.LetencyReportRequest;


import jakarta.servlet.http.HttpServletResponse;

@Service
public interface LatencyReportService {

	public ResponseEntity<?> LatencyReportView(String username, LetencyReportRequest customReportForm);

	public ResponseEntity<?> LatencyReportxls(String username, LetencyReportRequest customReportForm, HttpServletResponse response);

	public ResponseEntity<?> LatencyReportpdf(String username, LetencyReportRequest customReportForm, HttpServletResponse response);

	public ResponseEntity<?> LatencyReportdoc(String username, LetencyReportRequest customReportForm, HttpServletResponse response);

}
