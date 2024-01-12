package com.hti.smpp.common.service;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.TrackResultResponse;

@Service
public interface TrackResultService {

	public TrackResultResponse TrackResultReport(String username, CustomReportForm customReportForm,String lang);
}
