package com.hti.smpp.common.service;

import java.util.List;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;

public interface SmscDlrReportReportService {

	public List<DeliveryDTO> SmscDlrReportview(String username, CustomReportForm customReportForm, String lang);

	public List<DeliveryDTO> SmscDlrReportvxls(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);

	public List<DeliveryDTO> SmscDlrReportvpdf(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);

	public List<DeliveryDTO> SmscDlrReportdoc(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);

}
