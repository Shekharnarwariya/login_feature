package com.hti.smpp.common.controller;

import java.util.Collection;

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

import com.hti.smpp.common.request.SalesEntryForm;
import com.hti.smpp.common.response.ViewSalesEntry;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.service.SalesService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@OpenAPIDefinition(info = @Info(title = "SMPP Sales API", version = "1.0", description = "API for managing SMPP Sales"))
@RestController
@RequestMapping("/sales")
@Tag(name = "SalesController", description = "API's for Sales")
public class SalesController {

	@Autowired
	private SalesService salesService;

	@Operation(summary = "Save Sales Entry", description = "Save a new sales entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "SalesEntry Saved Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})
	@PostMapping("/save")
	public ResponseEntity<String> saveSalesEntry(@Valid @RequestBody SalesEntryForm salesEntry,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.save(salesEntry, username);
	}

	@Operation(summary = "Update Sales Entry", description = "Update's an existing sales entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "SalesEntry Updated Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})
	@PutMapping("/update")
	public ResponseEntity<String> update(@Valid @RequestBody SalesEntryForm form,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.update(form, username);
	}

	@Operation(summary = "Delete Sales Entry", description = "Delete's an existing sales entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "SalesEntry Deleted Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteSalesEntry(
			@Parameter(description = "Id") @PathVariable(value = "id", required = true) int id,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.delete(id, username);

	}

	@Operation(summary = "List Sales Users", description = "Returns Collection view of the values contained in SalesEntry map")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List Sales Users Successful.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})
	@GetMapping("/list-sales-users")
	public ResponseEntity<Collection<SalesEntry>> listSalesUsers(
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.listSalesUsers(username);
	}

	@Operation(summary = "View Sales Entry", description = "Returns the ViewSalesEntry as response")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "ViewSalesEntry response Successful.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ViewSalesEntry.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})
	@GetMapping("/view-sales-entry/{id}")
	public ResponseEntity<?> viewSalesEntry(
			@Parameter(description = "Id") @PathVariable(value = "id", required = true) int id,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.viewSalesEntry(id, username);
	}

	@Operation(summary = "Setup SalesEntry", description = "Returns a Collection view of the values contained in the SalesEntry map.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Collection view of values Successful.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@GetMapping("/setup-sales-entry")
	public ResponseEntity<?> setupSalesEntry(
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.setupSalesEntry(username);
	}

}
