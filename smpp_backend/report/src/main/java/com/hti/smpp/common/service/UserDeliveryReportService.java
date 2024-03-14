package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.hti.smpp.common.request.UserDeliveryForm;


@Service
public interface UserDeliveryReportService {

	public ResponseEntity<?> UserDeliveryReportView(String username, UserDeliveryForm customReportForm);


	
	
	
}
