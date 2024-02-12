package com.hti.smpp.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.hti.smpp.common.exception.ExceptionResponse;
import com.hti.smpp.common.request.SubscribeEntryForm;
import com.hti.smpp.common.service.SubscribeService;
import com.hti.smpp.common.util.dto.SubscribeEntry;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(info = @Info(title = "SMPP Subscription API", version = "1.0", description = "API for managing SMPP Subscription"))
@RestController
@RequestMapping("/subscription")
@Tag(name = "Subscibe Controller", description = "API's for Subscription.")
public class SubscribeController {
	
	@Autowired
	private SubscribeService subscribeService;
	
	@Operation(summary = "Save Subscription Entry", description = "Save a new subscription entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "SubscribeEntry Saved Successfully."),
			@ApiResponse(responseCode = "413", description = "Payload to large."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) 
	})
	@PostMapping(value="/addSubscriptionEntry",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveSubscribe(@Parameter(description = "SubscribeEntryForm Request", content = @Content(schema = @Schema(implementation = SubscribeEntryForm.class))) @RequestParam(value = "subscribeEntryForm", required = true) String subscribeEntryForm,@RequestPart(value = "headerFile", required = false) MultipartFile headerFile,@RequestPart(value = "footerFile", required = false) MultipartFile footerFile, @Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username){
		return this.subscribeService.saveSubscribe(subscribeEntryForm,headerFile,footerFile,username);
	}
	
	@Operation(summary = "View Subscription Entry", description = "To view an existing subscription entry by id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "SubscribeEntry Fetched Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubscribeEntry.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) 
	})
	@GetMapping("/viewSubscibeEntry/{id}")
	public ResponseEntity<SubscribeEntry> viewSubscribeEntry(@Parameter(description = "Id") @PathVariable(value = "id", required = true) int id, @Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username){
		return this.subscribeService.viewSubscribeEntry(id, username);
	}
	
	@Operation(summary = "List Subscription Entry", description = "To view all the subscription entries")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "SubscribeEntries Fetched Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubscribeEntry.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) 
	})
	@GetMapping("/listSubscibeEntry")
	public ResponseEntity<List<SubscribeEntry>> listSubscribeEntry(@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username){
		return this.subscribeService.listSubscribeEntry(username);
	}
	
	@Operation(summary = "Update Subscription Entry", description = "Update an existing subscription entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "SubscribeEntry Updated Successfully."),
			@ApiResponse(responseCode = "413", description = "Payload to large."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) 
	})
	@PutMapping(value = "/updateSubscribeEntry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> updateSubscribeEntry(@Parameter(description = "SubscribeEntryForm Request", content = @Content(schema = @Schema(implementation = SubscribeEntryForm.class))) @RequestParam(value = "subscribeEntryForm", required = true) String subscribeEntryForm,@RequestPart(value = "headerFile", required = false) MultipartFile headerFile,@RequestPart(value = "footerFile", required = false) MultipartFile footerFile, @Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username){
		return this.subscribeService.updateSubscribe(subscribeEntryForm,headerFile,footerFile,username);
	}
	
	@Operation(summary = "Delete Subscription Entry", description = "To delete an existing subscription entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "SubscribeEntry Deleted Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) 
	})
	@DeleteMapping("/deleteSubscribeEntry/{id}")
	public ResponseEntity<?> deleteSubscribeEntry(@Parameter(description = "Id") @PathVariable(value = "id", required = true) int id, @Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username){
		return this.subscribeService.deleteSubscribe(id, username);
	}

}
