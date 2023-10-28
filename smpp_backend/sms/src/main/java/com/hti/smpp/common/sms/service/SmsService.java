package com.hti.smpp.common.sms.service;

import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.sms.request.SmsRequest;

public interface SmsService {

	public SmsResponse sendSms(SmsRequest smsRequest, String username);

}
