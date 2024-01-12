package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.schedule.dto.ScheduleEntryExt;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface ScheduleReportService {

	public List<ScheduleEntryExt> ScheduleReport(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);

}
