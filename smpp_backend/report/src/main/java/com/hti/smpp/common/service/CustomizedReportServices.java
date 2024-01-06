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
	public List<DeliveryDTO> CustomizedReportView(String username, CustomReportForm customReportForm);

	public List<DeliveryDTO> CustomizedReportdoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response);

	public List<DeliveryDTO> CustomizedReportxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response);

	public List<DeliveryDTO> CustomizedReportpdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response);
	
//	public List<UserEntryExt> CustomizedReportDetails(String username, CustomReportForm customReportForm,
//			HttpServletResponse response);
	
	

}
