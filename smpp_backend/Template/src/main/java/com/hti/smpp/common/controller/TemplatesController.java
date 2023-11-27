package com.hti.smpp.common.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.request.TemplatesRequest;
import com.hti.smpp.common.responce.TemplatesResponse;
import com.hti.smpp.common.service.TemplatesService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/templates")
@Validated // Add this annotation to enable method-level validation
@OpenAPIDefinition(info = @Info(title = "SMPP Templates API", version = "1.0", description = "API for managing SMPP Templates"))
public class TemplatesController {

	private final TemplatesService templatesService;

	@Autowired
	public TemplatesController(TemplatesService templatesService) {
		this.templatesService = templatesService;
	}

	@Operation(summary = "Create a new template")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Template created successfully"),
			@ApiResponse(responseCode = "404", description = "Template not found") })
	@PostMapping
	public ResponseEntity<String> createTemplate(@Valid @RequestBody TemplatesRequest request,
			@RequestHeader("username") String username) {
		TemplatesResponse response = templatesService.createTemplate(request, username);
		if (response != null) {
			return ResponseEntity.ok("Template created successfully");
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Operation(summary = "Get a template by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Template retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Template not found") })
	@GetMapping("/{id}")
	public ResponseEntity<TemplatesResponse> getTemplateById(@PathVariable int id,
			@RequestHeader("username") String username) {
		TemplatesResponse response = templatesService.getTemplateById(id, username);
		if (response != null) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Operation(summary = "Get all templates")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "All templates retrieved successfully") })
	@GetMapping
	public ResponseEntity<List<TemplatesResponse>> getAllTemplates(@RequestHeader("username") String username) {
		List<TemplatesResponse> templates = templatesService.getAllTemplates(username);
		return ResponseEntity.ok(templates);
	}

	@Operation(summary = "Update a template by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Template updated successfully"),
			@ApiResponse(responseCode = "404", description = "Template not found") })
	@PutMapping("/{id}")
	public ResponseEntity<String> updateTemplate(@PathVariable int id, @Valid @RequestBody TemplatesRequest request,
			@RequestHeader("username") String username) {
		TemplatesResponse response = templatesService.updateTemplate(id, request, username);
		if (!(response == null)) {
			return ResponseEntity.ok("Template updated successfully");
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Operation(summary = "Delete a template by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Template deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Template not found") })
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteTemplate(@PathVariable int id, @RequestHeader("username") String username) {
		boolean success = templatesService.deleteTemplate(id, username);
		if (success) {
			return ResponseEntity.ok("Template deleted successfully");
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
