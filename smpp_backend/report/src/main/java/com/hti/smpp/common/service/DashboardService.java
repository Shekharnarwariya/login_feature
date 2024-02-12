package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.DashboardRequest;

@Service
public interface DashboardService {

	public ResponseEntity<?> processRequest(DashboardRequest request, String username);

	// public ResponseEntity<?> processRequest(DashboardDayRequest request,
	// HttpServletResponse response, String username) ;

}
