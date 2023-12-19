package com.hti.smpp.common.controller;


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

@RestController
@RequestMapping("/templates")
@Validated // Add this annotation to enable method-level validation
@OpenAPIDefinition(info = @Info(title = "SMPP Templates API", version = "1.0", description = "API for managing SMPP Templates"))
@Tag(name = "TemplatesController", description = "API's for templates")
public class TemplatesController {

	private final TemplatesService templatesService;

	@Autowired
	public TemplatesController(TemplatesService templatesService) {
		this.templatesService = templatesService;
	}

	@Operation(summary = "Create a new template", description = "To save a new Template")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "201", description = "Template created successfully"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error Creating Template"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request")})
	@PostMapping("/create-template")
	public ResponseEntity<?> createTemplate(@Valid @RequestBody TemplatesRequest request,
			@Parameter(description = "Username in header") @RequestHeader("username") String username) {
		return this.templatesService.createTemplate(request, username);
	}

	@Operation(summary = "Get a template by ID", description = "Find a template by giving id")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Template retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error getting data"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request")})
	@GetMapping("/get-template/{id}")
	public ResponseEntity<?> getTemplateById(@Parameter(description = "Template Id") @PathVariable int id, @Parameter(description = "Username in header") @RequestHeader("username") String username) {
		return this.templatesService.getTemplateById(id, username);
	}

	@Operation(summary = "Get all templates", description = "Find all the templates")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "All templates retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error getting data"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request")
			})
	@GetMapping("/get-all-templates")
	public ResponseEntity<?> getAllTemplates(@Parameter(description = "Username in header") @RequestHeader("username") String username) {
		return this.templatesService.getAllTemplates(username);
	}

	@Operation(summary = "Update a template by ID", description = "To update a template by id")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "201", description = "Template updated successfully"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error getting data"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request")
			})
	@PutMapping("/update-template/{id}")
	public ResponseEntity<?> updateTemplate(@Parameter(description = "Id") @PathVariable int id, @Valid @RequestBody TemplatesRequest request,
			@Parameter(description = "Username in header") @RequestHeader("username") String username) {
		return this.templatesService.updateTemplate(id, request, username);
	}

	@Operation(summary = "Delete a template by ID", description = "Delete's a template by id")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Template deleted successfully"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error getting data"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request") 
			})
	@DeleteMapping("/delete-template/{id}")
	public ResponseEntity<?> deleteTemplate(@Parameter(description = "Id") @PathVariable int id, @Parameter(description = "Username in header") @RequestHeader("username") String username) {
		return templatesService.deleteTemplate(id, username);
	}
	
	@Operation(summary = " Recently Used Template ", description = "Template that was recently used ")
	@ApiResponses(value = { 
	        @ApiResponse(responseCode = "200", description = "Recently used template  successfully"),
	        @ApiResponse(responseCode = "404", description = "Template not found"),
	        @ApiResponse(responseCode = "502", description = "Error while processing the request"),
	        @ApiResponse(responseCode = "401", description = "Unauthorized request") 
	})
	@GetMapping("/recent-use-template")
	public ResponseEntity<?> RecentUseTemplate(
	    @Parameter(description = "Username provided in the request header") @RequestHeader("username") String username) {
	    return templatesService.RecentUseTemplate(username);
	}

	
	
	@Operation(summary = "Search Recently Used Templates", description = "Search for templates that were recently used")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "Recently used templates successfully retrieved"),
	        @ApiResponse(responseCode = "404", description = "Templates not found"),
	        @ApiResponse(responseCode = "502", description = "Error while processing the request"),
	        @ApiResponse(responseCode = "401", description = "Unauthorized request")
	})
	@GetMapping("/search-recent-use-template")
	public ResponseEntity<?> searchRecentTemplates(
	        @Parameter(description = "Username provided in the request header") @RequestHeader("username") String username,
	        @Parameter(description = "Number of recent templates to retrieve") @RequestParam(defaultValue = "a") String search) {
	    return templatesService.searchRecentTemplates(username, search);
	}

}
