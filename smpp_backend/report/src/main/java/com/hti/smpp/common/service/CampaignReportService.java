package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CampaignReportRequest;

import jakarta.servlet.http.HttpServletResponse;
@Service
public interface CampaignReportService {
	
	public ResponseEntity<?> CampaignReportview(String username, CampaignReportRequest customReportForm);

//	public ResponseEntity<?> CampaignReportxls(String username, CampaignReportRequest customReportForm,
//			HttpServletResponse response);
//
//	public ResponseEntity<?> CampaignReportPdf(String username, CampaignReportRequest customReportForm,
//			HttpServletResponse response);
//
//	public ResponseEntity<?> CampaignReportDoc(String username, CampaignReportRequest customReportForm,
//			HttpServletResponse response);
	

}
