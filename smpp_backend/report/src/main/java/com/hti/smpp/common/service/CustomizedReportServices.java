package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface CustomizedReportServices {
///////////////////// CustomizedReport////////////////////////
	public List<DeliveryDTO> CustomizedReportView(String username, CustomReportForm customReportForm,String lang);

	public String CustomizedReportdoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);

	public String CustomizedReportxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);

	public String CustomizedReportpdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);
	
//	public List<UserEntryExt> CustomizedReportDetails(String username, CustomReportForm customReportForm,
//			HttpServletResponse response);
	
	

}
