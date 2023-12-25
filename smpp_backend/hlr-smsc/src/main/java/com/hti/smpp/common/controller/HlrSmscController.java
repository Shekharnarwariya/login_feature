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
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.hlr.dto.HlrSmscEntry;
import com.hti.smpp.common.request.HlrSmscEntryRequest;
import com.hti.smpp.common.service.HlrSmscService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/hlr")
@OpenAPIDefinition(info = @Info(title = "SMPP Hlr Smsc API..", version = "1.0", description = "API for managing SMPP Hlr Smsc..."))
public class HlrSmscController {

	// Service to handle HLR SMS entry operations
	private final HlrSmscService hlrSmscService;
	

	// Constructor injection for HlrSmscService

	@Autowired
	public HlrSmscController(HlrSmscService hlrSmscService) {
		this.hlrSmscService = hlrSmscService;
	}
	
	// Endpoint to save a new HLR SMS entry

	@PostMapping
	@Operation(summary = "Save HLR SMS Entry", description = "Save a new HLR SMS entry")
	public ResponseEntity<?> saveHlrSmscEntry(@RequestBody @Valid HlrSmscEntryRequest hlrSmscEntryRequest,
			@RequestHeader("username") String username) {
		return ResponseEntity.ok(hlrSmscService.save(hlrSmscEntryRequest, username));
	}
    // Endpoint to update an existing HLR SMS entry


	@PutMapping("/{id}")
	@Operation(summary = "Update HLR SMS Entry", description = "Update an existing HLR SMS entry")
	public ResponseEntity<?> updateHlrSmscEntry(@PathVariable int id,
			@RequestBody @Valid HlrSmscEntryRequest hlrSmscEntryRequest, @RequestHeader("username") String username) {
		return ResponseEntity.ok(hlrSmscService.update(id, hlrSmscEntryRequest, username));
	}
	 // Endpoint to delete an existing HLR SMS entry
	@DeleteMapping("/{id}")
	@Operation(summary = "Delete HLR SMS Entry", description = "Delete an existing HLR SMS entry")
	public ResponseEntity<?> deleteHlrSmscEntry(@PathVariable int id, @RequestHeader("username") String username) {
		hlrSmscService.delete(id, username);
		return ResponseEntity.noContent().build();
	}
	// Endpoint to get details of a specific HLR SMS entry

	@GetMapping("/{id}")
	@Operation(summary = "Get HLR SMS Entry", description = "Get details of a specific HLR SMS entry")
	@ApiResponse(responseCode = "200", description = "Successful retrieval of HLR SMS entry", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HlrSmscEntry.class)))
	public ResponseEntity<HlrSmscEntry> getHlrSmscEntry(@PathVariable int id,
			@RequestHeader("username") String username) {
		return hlrSmscService.getEntry(id, username);
	}
	 // Endpoint to get a list of all HLR SMS entries
	@GetMapping
	@Operation(summary = "List HLR SMS Entries", description = "Get a list of all HLR SMS entries")
	@ApiResponse(responseCode = "200", description = "Successful retrieval of HLR SMS entries", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HlrSmscEntry.class)))
	public ResponseEntity<List<HlrSmscEntry>> listHlrSmscEntries(@RequestHeader("username") String username) {
		return ResponseEntity.ok(hlrSmscService.list(username));
	}
}
