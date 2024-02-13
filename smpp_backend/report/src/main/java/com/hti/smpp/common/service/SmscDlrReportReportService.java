package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.SmscDlrReportRequest;

import jakarta.servlet.http.HttpServletResponse;
@Service
public interface SmscDlrReportReportService {

	public ResponseEntity<?>  SmscDlrReportview(String username, SmscDlrReportRequest customReportForm, String lang);

	public ResponseEntity<?>  SmscDlrReportxls(String username, SmscDlrReportRequest customReportForm, String lang,
			HttpServletResponse response);

	public ResponseEntity<?>  SmscDlrReportpdf(String username, SmscDlrReportRequest customReportForm, String lang,
			HttpServletResponse response);

	public ResponseEntity<?>  SmscDlrReportdoc(String username, SmscDlrReportRequest customReportForm, String lang,
			HttpServletResponse response);

}
