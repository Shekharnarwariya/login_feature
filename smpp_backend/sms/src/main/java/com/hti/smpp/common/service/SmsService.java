package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.BulkAutoScheduleRequest;
import com.hti.smpp.common.request.BulkContactRequest;
import com.hti.smpp.common.request.BulkMmsRequest;
import com.hti.smpp.common.request.BulkRequest;
import com.hti.smpp.common.request.BulkUpdateRequest;
import com.hti.smpp.common.request.SendBulkScheduleRequest;
import com.hti.smpp.common.request.SmsRequest;
import com.hti.smpp.common.response.BulkResponse;
import com.hti.smpp.common.response.SmsResponse;

import jakarta.servlet.http.HttpSession;

@Service
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

	public ResponseEntity<?> autoSchedule(MultipartFile destinationNumberFile, String username,
			BulkAutoScheduleRequest bulkAutoScheduleRequest);

	public ResponseEntity<?> editBulk(String username, int batchId);

	public ResponseEntity<?> pauseBulk(String username, int batchId);

	public ResponseEntity<?> abortBulk(String username, int batchId);

	public ResponseEntity<?> resumeBulk(String username, int batchId);

	public ResponseEntity<?> sendModifiedBulk(String username, BulkUpdateRequest bulkUpdateRequest);

	public ResponseEntity<?> listBulk(String username);

	public ResponseEntity<?> listSchedule(String username);

	public ResponseEntity<?> abortSchedule(String username, int schedule_Id);

	public ResponseEntity<?> editSchedule(String username, int schedule_Id);

	public ResponseEntity<?> sendNowSchedule(String username, SendBulkScheduleRequest sendBulkScheduleRequest,
			MultipartFile destinationNumberFile);

	public ResponseEntity<?> modifiedSchedule(String username, SendBulkScheduleRequest sendBulkScheduleRequest,
			MultipartFile destinationNumberFile);

	public ResponseEntity<?> identifyMessage(String username, String message);

	public ResponseEntity<?> getExcludeNumbers(String username);

	public ResponseEntity<?> getSenderId(String username);

}
