package com.hti.smpp.common.sms.service;

import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.sms.request.SmsRequest;

import jakarta.servlet.http.HttpSession;

public interface SmsService {
	
	public SmsResponse sendSms(SmsRequest smsRequest,HttpSession session);

}
