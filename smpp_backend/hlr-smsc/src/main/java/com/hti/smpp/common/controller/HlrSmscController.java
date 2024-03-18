package com.hti.smpp.common.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.exception.ExceptionResponse;
import com.hti.smpp.common.hlr.dto.HlrSmscEntry;
import com.hti.smpp.common.request.HlrSmscEntryRequest;
import com.hti.smpp.common.service.HlrSmscService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hlr")
@OpenAPIDefinition(info = @Info(title = "SMPP HLR SMSC API", version = "1.0", description = "API for managing SMPP Hlr Smsc..."))
@Tag(name = "HlrSmscController", description = "API's for HLR SMSC")
public class HlrSmscController {

	// Service to handle HLR SMS entry operations
	private final HlrSmscService hlrSmscService;

	// Constructor injection for HlrSmscService

	@Autowired
	public HlrSmscController(HlrSmscService hlrSmscService) {
		this.hlrSmscService = hlrSmscService;
	}

	// Endpoint to save a new HLR SMS entry
	@PostMapping("/save-hlr-smsc")
	@Operation(summary = "Save HLR SMSC Entry", description = "Save a new HLR SMSC entry")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "HLR SMSC Entry Saved Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> saveHlrSmscEntry(@RequestBody @Valid HlrSmscEntryRequest hlrSmscEntryRequest,
			@RequestHeader("username") String username) {
		return hlrSmscService.save(hlrSmscEntryRequest, username);
	}

	// Endpoint to update an existing HLR SMS entry
	@PutMapping("/update-hlr-smsc/{id}")
	@Operation(summary = "Update HLR SMSC Entry", description = "Update an existing HLR SMSC entry")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "HLR SMSC Entry Updated Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> updateHlrSmscEntry(@PathVariable int id,
			@RequestBody @Valid HlrSmscEntryRequest hlrSmscEntryRequest, @RequestHeader("username") String username) {
		return hlrSmscService.update(id, hlrSmscEntryRequest, username);
	}

	// Endpoint to delete an existing HLR SMS entry
	@DeleteMapping("/delete-hlr-smsc/{id}")
	@Operation(summary = "Delete HLR SMSC Entry", description = "Delete an existing HLR SMSC entry")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "HLR SMSC Entry Deleted Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> deleteHlrSmscEntry(@PathVariable int id, @RequestHeader("username") String username) {
		return hlrSmscService.delete(id, username);
	}

	// Endpoint to get details of a specific HLR SMS entry
	@GetMapping("/get-hlr-smsc/{id}")
	@Operation(summary = "Get HLR SMSC Entry", description = "Get details of a specific HLR SMSC entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval of HLR SMSC entry", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HlrSmscEntry.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<HlrSmscEntry> getHlrSmscEntry(@PathVariable int id,
			@RequestHeader("username") String username) {
		return hlrSmscService.getEntry(id, username);
	}

	// Endpoint to get a list of all HLR SMS entries
	@GetMapping("/get-all")
	@Operation(summary = "List HLR SMSC Entries", description = "Get a list of all HLR SMSC entries")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval of HLR SMSC entries", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HlrSmscEntry.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<List<HlrSmscEntry>> listHlrSmscEntries(@RequestHeader("username") String username,
			@RequestParam String purpose) {
		return ResponseEntity.ok(hlrSmscService.list(username, purpose));
	}
}
