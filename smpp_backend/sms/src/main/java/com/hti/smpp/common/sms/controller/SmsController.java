package com.hti.smpp.common.sms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.sms.request.SmsRequest;
import com.hti.smpp.common.sms.service.SmsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class SmsController {

	@Autowired
	private SmsService smsService;

	@Autowired
	private HttpServletRequest request;

	@MutationMapping("sendSms")
	public SmsResponse sendSms(@Argument SmsRequest smsRequest) {
		HttpSession session = request.getSession();
		return smsService.sendSms(smsRequest, session);

	}

}
