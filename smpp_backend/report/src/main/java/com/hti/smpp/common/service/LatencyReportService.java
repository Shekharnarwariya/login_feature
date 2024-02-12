package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.LetencyReportRequest;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface LatencyReportService {

	public ResponseEntity<?> LatencyReportView(String username, LetencyReportRequest customReportForm, String lang);

	public ResponseEntity<?> LatencyReportxls(String username, LetencyReportRequest customReportForm, HttpServletResponse response,
			String lang);

	public ResponseEntity<?> LatencyReportpdf(String username, LetencyReportRequest customReportForm, HttpServletResponse response,
			String lang);

	public ResponseEntity<?> LatencyReportdoc(String username, LetencyReportRequest customReportForm, HttpServletResponse response,
			String lang);

}
