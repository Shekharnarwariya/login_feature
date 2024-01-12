package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface LatencyReportService {

	public List<DeliveryDTO> LatencyReportView(String username, CustomReportForm customReportForm, String lang);

	public String LatencyReportxls(String username, CustomReportForm customReportForm, HttpServletResponse response,
			String lang);

	public String LatencyReportpdf(String username, CustomReportForm customReportForm, HttpServletResponse response,
			String lang);

	public String LatencyReportdoc(String username, CustomReportForm customReportForm, HttpServletResponse response,
			String lang);

}
