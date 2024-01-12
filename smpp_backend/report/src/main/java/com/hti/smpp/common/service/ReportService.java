package com.hti.smpp.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperPrint;

@Service
public interface ReportService {

	public List<BulkEntry> abortBatchReport(String username, CustomReportForm customReportForm,String lang);

	public Map<String, List<DeliveryDTO>> BalanceReportView(String username, CustomReportForm customReportForm,String lang);

	public String BalanceReportxls(String username, CustomReportForm customReportForm, HttpServletResponse response,String lang);

	public String balanceReportPdf(String username, CustomReportForm customReportForm, HttpServletResponse response,String lang);

	public String BalanceReportDoc(String username, CustomReportForm customReportForm, HttpServletResponse response,String lang);

	public List<DeliveryDTO> BlockedReportView(String username, CustomReportForm customReportForm,String lang);

	public String BlockedReportxls(String username, CustomReportForm customReportForm, HttpServletResponse response,String lang);

	public String BlockedReportPdf(String username, CustomReportForm customReportForm, HttpServletResponse response,String lang);

	public String BlockedReportDoc(String username, CustomReportForm customReportForm, HttpServletResponse response,String lang);

	public JasperPrint CampaignReportview(String username, CustomReportForm customReportForm,String lang);

	public JasperPrint CampaignReportxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);

	public JasperPrint CampaignReportPdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);

	public JasperPrint CampaignReportDoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);

	/////////////////// ContentReport////////////////////////
	public List<DeliveryDTO> ContentReportView(String username, CustomReportForm customReportForm,String lang);

	public String ContentReportxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);

	public String ContentReportPdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);

	public String ContentReportDoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);

	
}
