package com.hti.smpp.common.sms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.sms.request.SmsRequest;
import com.hti.smpp.common.sms.service.SmsService;

@RestController
@RequestMapping("/sms")
public class SmsController {

	@Autowired
	private SmsService smsService;

	@PostMapping("/sendSms")
	public ResponseEntity<SmsResponse> sendSms(@RequestBody SmsRequest smsRequest,
			@RequestHeader("username") String username) {
		SmsResponse response = smsService.sendSms(smsRequest, username);
		return ResponseEntity.ok(response);
	}

}
