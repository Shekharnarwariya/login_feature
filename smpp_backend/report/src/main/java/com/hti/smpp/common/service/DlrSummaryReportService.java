package com.hti.smpp.common.service;

import java.util.List;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;

public interface DlrSummaryReportService {

	public List<DeliveryDTO> DlrSummaryReportview(String username, CustomReportForm customReportForm,String lang);

	public String DlrSummaryReportdoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);

	public String DlrSummaryReportdpdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);

	public String DlrSummaryReportdxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);
	

}
