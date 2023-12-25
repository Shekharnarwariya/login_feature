package com.hti.smpp.common.service;

import java.util.List;
import java.util.Map;

import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;

public interface ReportService {

	public List<BulkEntry> abortBatchReport(String username, CustomReportForm customReportForm);

	public Map<String, List<DeliveryDTO>> BalanceReportView(String username, CustomReportForm customReportForm);

	public String BalanceReportxls(String username, CustomReportForm customReportForm, HttpServletResponse response);

	public String balanceReportPdf(String username, CustomReportForm customReportForm, HttpServletResponse response);

	public String BalanceReportDoc(String username, CustomReportForm customReportForm, HttpServletResponse response);

	public List<DeliveryDTO> BlockedReportView(String username, CustomReportForm customReportForm);

	public String BlockedReportxls(String username, CustomReportForm customReportForm, HttpServletResponse response);

	public String BlockedReportPdf(String username, CustomReportForm customReportForm, HttpServletResponse response);

	public String BlockedReportDoc(String username, CustomReportForm customReportForm, HttpServletResponse response);

}
