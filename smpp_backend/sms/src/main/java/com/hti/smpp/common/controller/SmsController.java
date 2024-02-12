package com.hti.smpp.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.hti.smpp.common.request.BulkUpdateRequest;
import com.hti.smpp.common.request.MessageRequest;
import com.hti.smpp.common.request.SmsRequest;
import com.hti.smpp.common.response.BulkProccessResponse;
import com.hti.smpp.common.response.BulkResponse;
import com.hti.smpp.common.response.MessageIdentiryResponse;
import com.hti.smpp.common.response.ScheduleEditResponse;
import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.schedule.dto.ScheduleEntryExt;
import com.hti.smpp.common.service.MediaUploadService;
import com.hti.smpp.common.service.SmsService;
import com.hti.smpp.common.util.ObjectConverter;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/sms")
@OpenAPIDefinition(info = @Info(title = "SMPP Sms API", version = "1.0", description = "API for managing SMPP Sms"))
@Tag(name = "SMS", description = "Operations related to SMS")
public class SmsController {

	private final SmsService smsService;

	private final MediaUploadService mediaUploadService;

	@Autowired
	public SmsController(SmsService smsService, MediaUploadService mediaUploadService) {
		this.smsService = smsService;
		this.mediaUploadService = mediaUploadService;
	}

	@Operation(summary = "Send a single SMS", description = "This endpoint allows users to send a single SMS message.")
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

	@Operation(summary = "Send Bulk SMS", description = "This endpoint allows users to send bulk SMS messages.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Bulk SMS sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SmsResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping(value = "/sendbulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BulkResponse> sendBulk(
			@RequestParam("destinationNumberFile") List<MultipartFile> destinationNumberFile,
			@RequestHeader("username") String username,
			@RequestParam(name = "bulkRequest", required = true, defaultValue = "{\r\n"
					+ "  \"senderId\": \"YourSenderId\",\r\n" + "  \"message\": \"YourMessage\",\r\n"
					+ "  \"from\": \"YourFromField\",\r\n" + "  \"messageType\": \"YourMessageType\",\r\n"
					+ "  \"smsParts\": 1,\r\n" + "  \"charCount\": 0,\r\n" + "  \"charLimit\": 1,\r\n"
					+ "  \"gmt\": \"\",\r\n" + "  \"smscount\": \"YourSmsCount\",\r\n"
					+ "  \"timestart\": \"YourTimeStart\",\r\n"
					+ "  \"destinationNumber\": \"YourDestinationNumber\",\r\n" + "  \"delay\": 0.0,\r\n"
					+ "  \"repeat\": \"YourRepeat\",\r\n" + "  \"schedule\": true,\r\n" + "  \"alert\": true,\r\n"
					+ "  \"allowDuplicate\": true,\r\n" + "  \"exclude\": \"YourExcludedNumbers\",\r\n"
					+ "  \"expiryHour\": 1,\r\n" + "  \"tracking\": true,\r\n"
					+ "  \"weblink\": [\"Link1\", \"Link2\"], \r\n" + "  \"campaignName\": \"YourCampaignName\",\r\n"
					+ "  \"peId\": \"YourPeId\",\r\n" + "  \"templateId\": \"YourTemplateId\",\r\n"
					+ "  \"telemarketerId\": \"YourTelemarketerId\"\r\n" + "}\r\n" + "") String jsonBulkRequest,
			HttpSession session) {
		BulkResponse bulkResponse = smsService.sendBulkSms(ObjectConverter.jsonMapperBulkRequest(jsonBulkRequest),
				username, destinationNumberFile, session);
		return ResponseEntity.ok(bulkResponse);
	}

	@Operation(summary = "Send Bulk Custom SMS", description = "This endpoint allows users to send bulk custom SMS messages.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Bulk Custom SMS sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SmsResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping(value = "/sendbulk/custom", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BulkResponse> sendBulkCustom(
			@RequestPart("destinationNumberFile") MultipartFile destinationNumberFile,
			@RequestHeader("username") String username,
			@RequestParam(name = "bulkRequest", required = true, defaultValue = "{\r\n"
					+ "  \"senderId\": \"YourSenderId\",\r\n" + "  \"message\": \"YourMessage\",\r\n"
					+ "  \"from\": \"YourFromField\",\r\n" + "  \"messageType\": \"YourMessageType\",\r\n"
					+ "  \"smsParts\": 1,\r\n" + "  \"charCount\": 0,\r\n" + "  \"charLimit\": 1,\r\n"
					+ "  \"gmt\": \"\",\r\n" + "  \"smscount\": \"YourSmsCount\",\r\n"
					+ "  \"timestart\": \"YourTimeStart\",\r\n"
					+ "  \"destinationNumber\": \"YourDestinationNumber\",\r\n" + "  \"delay\": 0.0,\r\n"
					+ "  \"repeat\": \"YourRepeat\",\r\n" + "  \"schedule\": true,\r\n" + "  \"alert\": true,\r\n"
					+ "  \"allowDuplicate\": true,\r\n" + "  \"exclude\": \"YourExcludedNumbers\",\r\n"
					+ "  \"expiryHour\": 1,\r\n" + "  \"tracking\": true,\r\n"
					+ "  \"weblink\": [\"Link1\", \"Link2\"], \r\n" + "  \"campaignName\": \"YourCampaignName\",\r\n"
					+ "  \"peId\": \"YourPeId\",\r\n" + "  \"templateId\": \"YourTemplateId\",\r\n"
					+ "  \"telemarketerId\": \"YourTelemarketerId\"\r\n" + "}\r\n" + "") String jsonBulkRequest,
			HttpSession session) {
		BulkResponse bulkResponse = smsService.sendBulkCustom(ObjectConverter.jsonMapperBulkRequest(jsonBulkRequest),
				username, destinationNumberFile, session);
		return ResponseEntity.ok(bulkResponse);
	}

	@Operation(summary = "Send Bulk Contacts SMS", description = "This endpoint allows users to send bulk Contacts SMS messages.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Bulk Contacts SMS sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SmsResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping("/sendByContacts")
	public ResponseEntity<?> sendSmsByContacts(@RequestBody BulkContactRequest bulkContactRequest,
			@RequestHeader("username") String username) {
		return smsService.sendSmsByContacts(bulkContactRequest, username);
	}

	@Operation(summary = "Send SMS to a Group of Contacts", description = "This API endpoint allows you to send SMS to a group of contacts specified in a file.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "SMS sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SmsResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping("/sendSmsGroupData")
	public ResponseEntity<?> sendSmsGroupData(@RequestBody(required = true) BulkContactRequest bulkContactRequest,
			@RequestHeader("username") String username) {
		return smsService.sendSmsGroupData(bulkContactRequest, username);
	}

	@Operation(summary = "Send SMS Mms", description = "This API endpoint allows you to send SMS Mms specified in a file. The destination phone numbers are provided in a multipart file. Additionally, you can include an optional bulk MMS request in the form of a JSON string. The 'username' header is required for authentication. Successful SMS sending results in a 200 response. In case of a bad request (400) or an internal server error (500), appropriate error responses are returned.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "SMS sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SmsResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping(value = "/sendSmsMms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> sendSmsMms(
			@RequestParam("destinationNumberFile") List<MultipartFile> destinationNumberFile,
			@RequestHeader("username") String username,
			@RequestParam(name = "bulkMmsRequest", required = true, defaultValue = "{\r\n"
					+ "  \"senderId\": \"YourSenderId\",\r\n" + "  \"message\": \"YourMessage\",\r\n"
					+ "  \"from\": \"YourFromField\",\r\n" + "  \"messageType\": \"YourMessageType\",\r\n"
					+ "  \"smsParts\": 1,\r\n" + "  \"charCount\": 0,\r\n" + "  \"charLimit\": 1,\r\n"
					+ "  \"gmt\": \"\",\r\n" + "  \"smscount\": \"YourSmsCount\",\r\n"
					+ "  \"timestart\": \"YourTimeStart\",\r\n"
					+ "  \"destinationNumber\": \"YourDestinationNumber\",\r\n" + "  \"repeat\": \"YourRepeat\",\r\n"
					+ "  \"schedule\": true,\r\n" + "  \"alert\": true,\r\n" + "  \"allowDuplicate\": true,\r\n"
					+ "  \"exclude\": \"YourExcludedNumbers\",\r\n" + "  \"expiryHour\": 1,\r\n"
					+ "  \"campaignName\": \"YourCampaignName\",\r\n" + "  \"peId\": \"YourPeId\",\r\n"
					+ "  \"templateId\": \"YourTemplateId\",\r\n" + "  \"telemarketerId\": \"YourTelemarketerId\",\r\n"
					+ "  \"caption\": \"YourCaption\", \r\n" + "  \"mmsType\": \"YourMmsType\" \r\n" + "}\r\n"
					+ "") String jsonBulkMmsRequest,
			HttpSession session) {
		return smsService.sendSmsMms(ObjectConverter.jsonMapperBulkMmsRequest(jsonBulkMmsRequest), username, session,
				destinationNumberFile);
	}

	@Operation(summary = "Send SMS to a Group of Contacts", description = "This API endpoint allows you to send SMS to a group of contacts specified in a file.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "SMS sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SmsResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping(value = "/auto-schedule", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> autoSchedule(
			@RequestPart(name = "destinationNumberFile", required = true) MultipartFile destinationNumberFile,
			@RequestHeader("username") String username,
			@RequestParam(name = "bulkAutoScheduleRequest") String bulkAutoScheduleRequest) {
		return smsService.autoSchedule(destinationNumberFile, username,
				ObjectConverter.jsonMapperBulkAutoScheduleRequest(bulkAutoScheduleRequest));
	}

	@GetMapping("/edit")
	@Operation(summary = "Edit Bulk Entry", description = "Edit bulk entry for the specified username.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Getting Bulk Entity  successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BulkProccessResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> editBulk(@RequestHeader("username") String username,
			@RequestParam(name = "batch_Id") int batchId) {
		return smsService.editBulk(username, batchId);
	}

	@GetMapping("/pause")
	@Operation(summary = "Pause Bulk Entry", description = "Pause bulk entry for the specified username.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Bulk entry paused successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BulkProccessResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> pauseBulk(@RequestHeader("username") String username,
			@RequestParam(name = "batch_Id") int batchId) {
		return smsService.pauseBulk(username, batchId);
	}

	@DeleteMapping("/abort")
	@Operation(summary = "Abort Bulk Entry", description = "Abort bulk entry for the specified username.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Bulk entry aborted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BulkProccessResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> abortBulk(@RequestHeader("username") String username,
			@RequestParam(name = "batch_Id") int batchId) {
		return smsService.abortBulk(username, batchId);
	}

	@GetMapping("/resume")
	@Operation(summary = "Resume Bulk Entry", description = "Resume bulk entry for the specified username.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Bulk entry resumed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BulkProccessResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> resumeBulk(@RequestHeader("username") String username,
			@RequestParam(name = "batch_Id") int batchId) {
		return smsService.resumeBulk(username, batchId);
	}

	@PostMapping("/sendModified")
	@Operation(summary = "Send Modified Bulk Entry", description = "Send modified bulk entry for the specified username.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Modified bulk entry sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BulkProccessResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> sendModifiedBulk(
			@Parameter(description = "Username of the owner of the bulk entry") @RequestHeader("username") String username,
			@RequestBody BulkUpdateRequest bulkUpdateRequest) {
		return smsService.sendModifiedBulk(username, bulkUpdateRequest);
	}

	@GetMapping("/list")
	@Operation(summary = "List Modified Bulk Entries", description = "Retrieve the list of modified bulk entries for the specified username.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved the list of modified bulk entries.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BulkProccessResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> listBulk(@RequestHeader("username") String username) {
		return smsService.listBulk(username);
	}

	@GetMapping("/listSchedule")
	@Operation(summary = "List Modified Bulk Entries", description = "Retrieve the list of modified bulk entries for the specified username. This API endpoint returns the schedule information associated with the provided username.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved the list of modified bulk entries.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleEntryExt.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> listSchedule(@RequestHeader(name = "username", required = true) String username) {
		return smsService.listSchedule(username);
	}

	@DeleteMapping("/abort/schedule")
	@Operation(summary = "Abort Schedule", description = "Abort the schedule for the specified username and schedule ID. This API endpoint allows users to cancel a scheduled task by providing the username and schedule ID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully aborted the schedule.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> abortSchedule(@RequestHeader(name = "username", required = true) String username,
			@RequestParam(name = "scheduleId", required = true) int scheduleId) {
		return smsService.abortSchedule(username, scheduleId);
	}

	@GetMapping("/edit/schedule")
	@Operation(summary = "edit Schedule", description = "edit the schedule for the specified username and schedule ID. This API endpoint allows users to cancel a scheduled task by providing the username and schedule ID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully aborted the schedule.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleEditResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> editSchedule(@RequestHeader(name = "username", required = true) String username,
			@RequestParam(name = "scheduleId", required = true) int scheduleId) {
		return smsService.editSchedule(username, scheduleId);
	}

	@PostMapping("/identify")
	@Operation(summary = "Identify Message", description = "Identify a message for the specified username and message. This API endpoint allows users to identify a message by providing the username and message content.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully identified the message", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageIdentiryResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> identifyMessage(@RequestHeader(name = "username", required = true) String username,
			@RequestBody MessageRequest messageRequest) {
		return smsService.identifyMessage(username, messageRequest.getMessage());
	}

	@PostMapping(value = "/send-now", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "send Now Schedule", description = "send Now the schedule for the specified username and schedule ID. This API endpoint allows users to cancel a scheduled task by providing the username and schedule ID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully send the schedule. sms", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BulkResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> sendNowSchedule(@RequestHeader(name = "username", required = true) String username,
			@RequestParam(name = "sendBulkScheduleRequest", defaultValue = "{\r\n" + "    \"id\": 0,\r\n"
					+ "    \"senderId\": \"string\",\r\n" + "    \"destinationNumber\": \"string\",\r\n"
					+ "    \"message\": \"string\",\r\n" + "    \"from\": \"string\",\r\n"
					+ "    \"schedTime\": \"string\",\r\n" + "    \"schedDate\": \"string\",\r\n"
					+ "    \"totalNumbers\": \"string\",\r\n" + "    \"uploadedNumbers\": \"string\",\r\n"
					+ "    \"gmt\": \"string\",\r\n" + "    \"smscount\": \"string\",\r\n" + "    \"delay\": 0,\r\n"
					+ "    \"repeat\": \"string\",\r\n" + "    \"messageType\": \"string\",\r\n"
					+ "    \"smsParts\": 0,\r\n" + "    \"charCount\": 0,\r\n" + "    \"charLimit\": 0,\r\n"
					+ "    \"expiryHour\": 0,\r\n" + "    \"reqType\": \"string\",\r\n"
					+ "    \"reCheck\": \"string\",\r\n" + "    \"fileName\": \"string\",\r\n"
					+ "    \"peId\": \"string\",\r\n" + "    \"templateId\": \"string\",\r\n"
					+ "    \"telemarketerId\": \"string\",\r\n" + "    \"alert\": true\r\n"
					+ "  }") String sendBulkScheduleRequest,
			@RequestPart(name = "destinationNumberFile", required = false) MultipartFile destinationNumberFile) {
		return smsService.sendNowSchedule(username,
				ObjectConverter.jsonMapperSendBulkScheduleRequest(sendBulkScheduleRequest), destinationNumberFile);
	}

	@PostMapping(value = "/modify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "modified Schedule", description = "modified the schedule for the specified username and schedule ID. This API endpoint allows users to cancel a scheduled task by providing the username and schedule ID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully modified  the schedule.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BulkResponse.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> modifiedSchedule(@RequestHeader(name = "username", required = true) String username,
			@RequestParam(name = "sendBulkScheduleRequest", defaultValue = "{\r\n" + "    \"id\": 0,\r\n"
					+ "    \"senderId\": \"string\",\r\n" + "    \"destinationNumber\": \"string\",\r\n"
					+ "    \"message\": \"string\",\r\n" + "    \"from\": \"string\",\r\n"
					+ "    \"schedTime\": \"string\",\r\n" + "    \"schedDate\": \"string\",\r\n"
					+ "    \"totalNumbers\": \"string\",\r\n" + "    \"uploadedNumbers\": \"string\",\r\n"
					+ "    \"gmt\": \"string\",\r\n" + "    \"smscount\": \"string\",\r\n" + "    \"delay\": 0,\r\n"
					+ "    \"repeat\": \"string\",\r\n" + "    \"messageType\": \"string\",\r\n"
					+ "    \"smsParts\": 0,\r\n" + "    \"charCount\": 0,\r\n" + "    \"charLimit\": 0,\r\n"
					+ "    \"expiryHour\": 0,\r\n" + "    \"reqType\": \"string\",\r\n"
					+ "    \"reCheck\": \"string\",\r\n" + "    \"fileName\": \"string\",\r\n"
					+ "    \"peId\": \"string\",\r\n" + "    \"templateId\": \"string\",\r\n"
					+ "    \"telemarketerId\": \"string\",\r\n" + "    \"alert\": true\r\n"
					+ "  }") String sendBulkScheduleRequest,
			@RequestPart(name = "destinationNumberFile", required = false) MultipartFile destinationNumberFile) {
		return smsService.modifiedSchedule(username,
				ObjectConverter.jsonMapperSendBulkScheduleRequest(sendBulkScheduleRequest), destinationNumberFile);
	}

	@Operation(summary = "Upload media file ", description = "This endpoint allows users to upload media file")
	@PostMapping(value = "/upload/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "upload file Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> upload(@RequestPart(name = "file", required = true) List<MultipartFile> items,
			@RequestParam String title, @RequestParam List<String> link_urls) {
		return mediaUploadService.UploadMedia(title, link_urls, items);
	}
}
