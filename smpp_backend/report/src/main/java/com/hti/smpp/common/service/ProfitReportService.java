package com.hti.smpp.common.service;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperPrint;

@Service
public interface ProfitReportService {

	public JasperPrint ProfitReportview(String username, CustomReportForm customReportForm, String lang);

	public String ProfitReportxls(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);

	public String ProfitReportpdf(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);

	public String ProfitReportdoc(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);
	

}
