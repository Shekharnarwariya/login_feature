package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface UserDeliveryReportService {

	public List<DeliveryDTO> UserDeliveryReportView(String username, CustomReportForm customReportForm);

	public String UserDeliveryReportxls(String username, CustomReportForm customReportForm, HttpServletResponse response);

	public String UserDeliveryReportPdf(String username, CustomReportForm customReportForm, HttpServletResponse response);

	public String UserDeliveryReportDoc(String username, CustomReportForm customReportForm, HttpServletResponse response);
}
