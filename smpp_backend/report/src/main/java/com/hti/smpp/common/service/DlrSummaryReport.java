package com.hti.smpp.common.service;

import java.util.List;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;

public interface DlrSummaryReport {

	public List<DeliveryDTO> DlrSummaryReportview(String username, CustomReportForm customReportForm);

	public List<DeliveryDTO> DlrSummaryReportdoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response);

	public List<DeliveryDTO> DlrSummaryReportdpdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response);

	public List<DeliveryDTO> DlrSummaryReportdxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response);

}
