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

import com.hti.smpp.common.exception.ExceptionResponse;
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

	/**
	 * Saves a sales entry by processing the provided form data and associating it with the specified username.
	 * Returns a ResponseEntity with a String result from the sales service operation.
	 */
	
	@Operation(summary = "Save Sales Entry", description = "Save a new sales entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "SalesEntry Saved Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) 
	})
	@PostMapping("/save")
	public ResponseEntity<String> saveSalesEntry(@Valid @RequestBody SalesEntryForm salesEntry,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.save(salesEntry, username);
	}
	

	/**
	 * Update a sales entry with the provided form data, associated with the specified Username.
	 * Returns a ResponseEntity with a String result from the sales service update operation.
	 */
	
	@Operation(summary = "Update Sales Entry", description = "Update's an existing sales entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "SalesEntry Updated Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) 
	})
	@PutMapping("/update")
	public ResponseEntity<String> update(@Valid @RequestBody SalesEntryForm form,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.update(form, username);
	}

	/**
	 * Deletes a sales entry with the specified ID, associated with the provided username.
	 * Returns a ResponseEntity with a String result from the sales service delete operation.
	 */
	
	@Operation(summary = "Delete Sales Entry", description = "Delete's an existing sales entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "SalesEntry Deleted Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))), 
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) 
	})
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteSalesEntry(
			@Parameter(description = "Id") @PathVariable(value = "id", required = true) int id,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.delete(id, username);

	}

	/**
	 * Retrieves a collection of sales entries associated with the specified username.
	 * Returns a ResponseEntity with the collection of SalesEntry objects.
	 * With a function name listSalesUser.
	 */
	
	@Operation(summary = "List Sales Users", description = "Returns the list of sales user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List Sales Successful."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) 
	})
	@GetMapping("/list-sales-users")
	public ResponseEntity<Collection<SalesEntry>> listSalesUsers(
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.listSalesUsers(username);
	}

	/**
	 * Retrieves details of a sales entry with the specified ID, associated with the provided username.
	 * Returns a ResponseEntity with the sales entry details or an appropriate response.
	 */
	
	@Operation(summary = "View Sales Entry", description = "View Sales Entry By Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "ViewSalesEntry response Successful.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ViewSalesEntry.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) 
	})
	@GetMapping("/view-sales-entry/{id}")
	public ResponseEntity<ViewSalesEntry> viewSalesEntry(
			@Parameter(description = "Id") @PathVariable(value = "id", required = true) int id,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.viewSalesEntry(id, username);
	}

	
	/**
	 * Initializes and retrieves data for setting up a new sales entry, associated with the provided username.
	 * Returns a ResponseEntity with the setup information or an appropriate response.
	 */
	
	@Operation(summary = "List Sales For Manager", description = "Returns the sales entries for the role manager.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully Fetched the sales entries for the role manager."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) 
	})
	@GetMapping("/setup-sales-entry")
	public ResponseEntity<?> setupSalesEntry(
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.salesService.setupSalesEntry(username);
	}

}
