package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.BulkContactRequest;
import com.hti.smpp.common.request.BulkRequest;
import com.hti.smpp.common.request.BulkMmsRequest;
import com.hti.smpp.common.request.SmsRequest;
import com.hti.smpp.common.response.BulkResponse;
import com.hti.smpp.common.response.SmsResponse;

import jakarta.servlet.http.HttpSession;

public interface SmsService {

	public SmsResponse sendSms(SmsRequest smsRequest, String username);

	public BulkResponse sendBulkSms(BulkRequest bulkRequest, String username, List<MultipartFile> destinationNumberFile,
			HttpSession session);

	public BulkResponse sendBulkCustom(BulkRequest bulkRequest, String username, MultipartFile destinationNumberFile,
			HttpSession session);

	public ResponseEntity<?> sendSmsByContacts(BulkContactRequest bulkContactRequest, String username);

	public ResponseEntity<?> sendSmsGroupData(BulkContactRequest bulkContactRequest, String username);

	public ResponseEntity<?> sendSmsMms(BulkMmsRequest bulkMmsRequest, String username, HttpSession session,
			List<MultipartFile> destinationNumberFile);
}
