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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/templates")
@Validated // Add this annotation to enable method-level validation
public class TemplatesController {

	private final TemplatesService templatesService;

	@Autowired
	public TemplatesController(TemplatesService templatesService) {
		this.templatesService = templatesService;
	}

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

	@GetMapping
	public ResponseEntity<List<TemplatesResponse>> getAllTemplates(@RequestHeader("username") String username) {
		List<TemplatesResponse> templates = templatesService.getAllTemplates(username);
		return ResponseEntity.ok(templates);
	}

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