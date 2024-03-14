package com.hti.smpp.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import com.hti.smpp.common.request.BalanceReportRequest;
import com.hti.smpp.common.request.BlockedReportRequest;


@Service
public interface ReportService {
	public ResponseEntity<?> BalanceReportView(String username, BalanceReportRequest customReportForm);


	public ResponseEntity<?> BlockedReportView(String username, BlockedReportRequest customReportForm);




	
}
