package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public interface TransactionReportService {
	public ResponseEntity<?> executeTransaction(String username);
}
