package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.ProfitReportRequest;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperPrint;

@Service
public interface ProfitReportService {

	public ResponseEntity<?> ProfitReportview(String username, ProfitReportRequest customReportForm, int page, int size);

	public ResponseEntity<?>  ProfitReportxls(String username, ProfitReportRequest customReportForm,
			HttpServletResponse response);

	public ResponseEntity<?>  ProfitReportpdf(String username, ProfitReportRequest customReportForm,
			HttpServletResponse response);

	public ResponseEntity<?>  ProfitReportdoc(String username, ProfitReportRequest customReportForm, HttpServletResponse response);
	

}
