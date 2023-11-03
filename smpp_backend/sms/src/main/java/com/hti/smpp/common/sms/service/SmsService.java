package com.hti.smpp.common.sms.service;

import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.response.BulkResponse;
import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.sms.request.BulkRequest;
import com.hti.smpp.common.sms.request.SmsRequest;

public interface SmsService {

	public SmsResponse sendSms(SmsRequest smsRequest, String username);

	public BulkResponse sendBulkSms(BulkRequest bulkRequest, String username, MultipartFile destinationNumberFile);

	public BulkResponse sendBulkCustome(BulkRequest bulkRequest, String username, MultipartFile destinationNumberFile);
}
