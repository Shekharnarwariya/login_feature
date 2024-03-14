package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.DlrSummaryReport;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;
@Service
public interface DlrSummaryReportService {

	public ResponseEntity<?> DlrSummaryReportview(String username, DlrSummaryReport  customReportForm);

	
}
