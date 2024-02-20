package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.ScheduleReportRequest;
import com.hti.smpp.common.schedule.dto.ScheduleEntryExt;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface ScheduleReportService {

	public ResponseEntity<?> ScheduleReport(String username, ScheduleReportRequest customReportForm);

}
