package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.UserDeliveryForm;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface UserDeliveryReportService {

	public ResponseEntity<?> UserDeliveryReportView(String username, UserDeliveryForm customReportForm);

	public ResponseEntity<?> UserDeliveryReportxls(String username, UserDeliveryForm customReportForm, HttpServletResponse response);

	public ResponseEntity<?> UserDeliveryReportPdf(String username, UserDeliveryForm customReportForm, HttpServletResponse response);

	public ResponseEntity<?> UserDeliveryReportDoc(String username, UserDeliveryForm customReportForm, HttpServletResponse response);
	
	///////////////////////////DownloadAPI//////////////////
	
	
	
}
