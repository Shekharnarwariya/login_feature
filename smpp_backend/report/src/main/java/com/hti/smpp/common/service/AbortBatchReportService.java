package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.request.AbortBatchReportRequest;
import com.hti.smpp.common.request.CustomReportForm;
@Service
public interface AbortBatchReportService {
	
	public List<BulkEntry> abortBatchReport(String username, AbortBatchReportRequest customReportForm);


}
