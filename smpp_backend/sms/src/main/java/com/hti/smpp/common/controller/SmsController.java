package com.hti.smpp.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.exception.ExceptionResponse;
import com.hti.smpp.common.request.BulkContactRequest;
import com.hti.smpp.common.request.SmsRequest;
import com.hti.smpp.common.response.BulkResponse;
import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.service.SmsService;
import com.hti.smpp.common.util.ObjectConverter;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/sms")
@OpenAPIDefinition(info = @Info(title = "SMPP Sms API", version = "1.0", description = "API for managing SMPP Sms"))
public class SmsController {

	@Autowired
	private SmsService smsService;

	@Operation(summary = "Send a single SMS", description = "This endpoint allows users to send a single SMS message.", tags = {
			"SMS" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "SMS sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SmsResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping("/sendSms")
	public ResponseEntity<SmsResponse> sendSms(@RequestBody @Valid SmsRequest smsRequest,
			@RequestHeader(name = "username", required = true) String username) {
		SmsResponse response = smsService.sendSms(smsRequest, username);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Send Bulk SMS", description = "This endpoint allows users to send bulk SMS messages.", tags = {
			"SMS" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Bulk SMS sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SmsResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping(value = "/sendbulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BulkResponse> sendBulk(
			@RequestParam("destinationNumberFile") List<MultipartFile> destinationNumberFile,
			@RequestHeader("username") String username,
			@RequestParam(name = "bulkRequest", required = false) String bulkRequest, HttpSession session) {
		BulkResponse bulkResponse = smsService.sendBulkSms(ObjectConverter.jsonMapperBulkRequest(bulkRequest), username,
				destinationNumberFile, session);
		return ResponseEntity.ok(bulkResponse);
	}

	@Operation(summary = "Send Bulk Custom SMS", description = "This endpoint allows users to send bulk custom SMS messages.", tags = {
			"SMS" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Bulk Custom SMS sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SmsResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping(value = "/sendbulk/custom", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BulkResponse> sendBulkCustome(
			@RequestPart("destinationNumberFile") MultipartFile destinationNumberFile,
			@RequestHeader("username") String username,
			@RequestParam(name = "bulkRequest", required = false) String bulkRequest, HttpSession session) {
		BulkResponse bulkResponse = smsService.sendBulkCustome(ObjectConverter.jsonMapperBulkRequest(bulkRequest),
				username, destinationNumberFile, session);
		return ResponseEntity.ok(bulkResponse);
	}

	@Operation(summary = "Send Bulk Contacts SMS", description = "This endpoint allows users to send bulk Contacts SMS messages.", tags = {
			"SMS" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Bulk Contacts SMS sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SmsResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })

	@PostMapping("/sendByContacts")
	public ResponseEntity<?> sendSmsByContacts(@RequestBody BulkContactRequest bulkContactRequest,
			@RequestHeader("username") String username) {
		return smsService.sendSmsByContacts(bulkContactRequest, username);
	}

	@Operation(summary = "Send SMS to a Group of Contacts", description = "This API endpoint allows you to send SMS to a group of contacts specified in a file.", responses = {
			@ApiResponse(responseCode = "200", description = "SMS sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseEntity.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseEntity.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseEntity.class))) })
	@PostMapping("/sendSmsGroupData")
	public ResponseEntity<?> sendSmsGroupData(@RequestBody(required = true) BulkContactRequest bulkContactRequest,
			@RequestHeader("username") String username) {
		return smsService.sendSmsGroupData(bulkContactRequest, username);
	}

}