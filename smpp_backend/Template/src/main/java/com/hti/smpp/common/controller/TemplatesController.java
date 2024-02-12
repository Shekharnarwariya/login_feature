package com.hti.smpp.common.controller;
//Import statements for required classes and annotations
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

import java.time.LocalDate;


import com.hti.smpp.common.request.TemplatesRequest;
import com.hti.smpp.common.service.TemplatesService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
//Controller class for handling SMPP Templates-related operations
@RestController
@RequestMapping("/templates")
@Validated // Add this annotation to enable method-level validation
@OpenAPIDefinition(info = @Info(title = "SMPP Templates API", version = "1.0", description = "API for managing SMPP Templates"))
@Tag(name = "TemplatesController", description = "API's for templates")
public class TemplatesController {

	private final TemplatesService templatesService;
	// Autowired constructor for injecting TemplatesService
	@Autowired
	public TemplatesController(TemplatesService templatesService) {
		this.templatesService = templatesService;
	}
	 // Create a new template endpoint
	@Operation(summary = "Create a new template", description = "To save a new Template")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Template created successfully"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error Creating Template"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request") })
	@PostMapping("/create-template")
	public ResponseEntity<?> createTemplate(@Valid @RequestBody TemplatesRequest request,
			@Parameter(description = "Username in header") @RequestHeader("username") String username) {
		return this.templatesService.createTemplate(request, username);
	}
	 // Get a template by ID endpoint
	@Operation(summary = "Get a template by ID", description = "Find a template by giving id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Template retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error getting data"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request") })
	@GetMapping("/get-template/{id}")
	public ResponseEntity<?> getTemplateById(@Parameter(description = "Template Id") @PathVariable int id,
			@Parameter(description = "Username in header") @RequestHeader("username") String username) {
		return this.templatesService.getTemplateById(id, username);
	}
	  // Get all templates endpoint
	@Operation(summary = "Get all templates", description = "Find all the templates")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "All templates retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error getting data"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request") })
	@GetMapping("/get-all-templates")
	public ResponseEntity<?> getAllTemplates(

	    @Parameter(description = "Username in header") @RequestHeader("username") String username,
	    // Add the new parameters for date range
	    @Parameter(description = "From date in yyyy-MM-dd format") @RequestParam(name = "fromDate", required = false) String fromDate,
	    @Parameter(description = "To date in yyyy-MM-dd format") @RequestParam(name = "toDate", required = false) String toDate,
	    // Add a single parameter for search
	    @Parameter(description = "Search term") @RequestParam(name = "searchTerm", required = false) String searchTerm) {

	    // Parse the date strings into LocalDate objects
	    LocalDate fromLocalDate = (fromDate != null && !fromDate.isEmpty()) ? LocalDate.parse(fromDate) : null;
	    LocalDate toLocalDate = (toDate != null && !toDate.isEmpty()) ? LocalDate.parse(toDate) : null;

	    // Call the service method with the updated parameters
	    return this.templatesService.getAllTemplates(username, fromLocalDate, toLocalDate, searchTerm);
		
	}
	// Update a template by ID endpoint
	@Operation(summary = "Update a template by ID", description = "To update a template by id")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Template updated successfully"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error getting data"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request") })
	@PutMapping("/update-template/{id}")
	public ResponseEntity<?> updateTemplate(@Parameter(description = "Id") @PathVariable int id,
			@Valid @RequestBody TemplatesRequest request,
			@Parameter(description = "Username in header") @RequestHeader("username") String username) {
		return this.templatesService.updateTemplate(id, request, username);
	}
	  // Delete a template by ID endpoint
	@Operation(summary = "Delete a template by ID", description = "Delete's a template by id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Template deleted successfully"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error getting data"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request") })
	@DeleteMapping("/delete-template/{id}")
	public ResponseEntity<?> deleteTemplate(@Parameter(description = "Id") @PathVariable int id,
			@Parameter(description = "Username in header") @RequestHeader("username") String username) {
		return templatesService.deleteTemplate(id, username);
	}
	 // Recently Used Template endpoint
	@Operation(summary = " Recently Used Template ", description = "Template that was recently used ")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Recently used template  successfully"),
			@ApiResponse(responseCode = "404", description = "Template not found"),
			@ApiResponse(responseCode = "502", description = "Error while processing the request"),
			@ApiResponse(responseCode = "401", description = "Unauthorized request") })
	@GetMapping("/recent-use-template")
	public ResponseEntity<?> RecentUseTemplate(
			@Parameter(description = "Username provided in the request header") @RequestHeader("username") String username) {
		return templatesService.RecentUseTemplate(username);
	}

}
