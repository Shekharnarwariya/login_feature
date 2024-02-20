package com.hti.smpp.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.request.BalanceReportRequest;
import com.hti.smpp.common.request.BlockedReportRequest;
import com.hti.smpp.common.request.CampaignReportRequest;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperPrint;

@Service
public interface ReportService {
	public ResponseEntity<?> BalanceReportView(String username, BalanceReportRequest customReportForm);

	public ResponseEntity<?> BalanceReportxls(String username, BalanceReportRequest customReportForm, HttpServletResponse response);

	public ResponseEntity<?> balanceReportPdf(String username, BalanceReportRequest customReportForm, HttpServletResponse response);
	public ResponseEntity<?> BalanceReportDoc(String username, BalanceReportRequest customReportForm, HttpServletResponse response);

	public ResponseEntity<?> BlockedReportView(String username, BlockedReportRequest customReportForm);

	public ResponseEntity<?> BlockedReportxls(String username, BlockedReportRequest customReportForm, HttpServletResponse response);

	public ResponseEntity<?> BlockedReportPdf(String username, BlockedReportRequest customReportForm, HttpServletResponse response);

	public ResponseEntity<?> BlockedReportDoc(String username, BlockedReportRequest customReportForm, HttpServletResponse response);





	
}
