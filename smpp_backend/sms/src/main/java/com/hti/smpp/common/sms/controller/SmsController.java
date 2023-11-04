package com.hti.smpp.common.sms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.response.BulkResponse;
import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.sms.request.BulkRequest;
import com.hti.smpp.common.sms.request.SmsRequest;
import com.hti.smpp.common.sms.service.SmsService;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.repository.BalanceEntryRepository;

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

	@PostMapping(value = "/sendbulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BulkResponse> sendBulk(
			@RequestParam("destinationNumberFile") MultipartFile destinationNumberFile,
			@RequestHeader("username") String username,
			@RequestParam(name = "bulkRequest", required = false) String bulkRequest) {
		BulkRequest readValue;
		BulkResponse bulkResponse;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			readValue = objectMapper.readValue(bulkRequest, BulkRequest.class);
			if (readValue.isCustomContent()) {
				System.out.println("custom conten  is true.......");
				bulkResponse = smsService.sendBulkCustome(readValue, username, destinationNumberFile);
			} else {
				bulkResponse = smsService.sendBulkSms(readValue, username, destinationNumberFile);
			}
			return ResponseEntity.ok(bulkResponse);
		} catch (JsonProcessingException e) {
			// Log the exception properly and handle it accordingly
			System.err.println("Error occurred while processing JSON: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (Exception ex) {
			// Handle any other exception that might occur during the process
			System.err.println("An unexpected error occurred: " + ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}
}
