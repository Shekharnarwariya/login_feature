package com.hti.smpp.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hti.smpp.common.request.TemplatesRequest;
import com.hti.smpp.common.responce.TemplatesResponse;
import com.hti.smpp.common.service.TemplatesService;

import java.util.List;

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
	public ResponseEntity<String> createTemplate(@Valid @RequestBody TemplatesRequest request) {
		TemplatesResponse response = templatesService.createTemplate(request);
		return ResponseEntity.ok("Template created successfully");
	}

	@GetMapping("/{id}")
	public ResponseEntity<TemplatesResponse> getTemplateById(@PathVariable int id) {
		TemplatesResponse response = templatesService.getTemplateById(id);
		if (response != null) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping
	public ResponseEntity<List<TemplatesResponse>> getAllTemplates() {
		List<TemplatesResponse> templates = templatesService.getAllTemplates();
		return ResponseEntity.ok(templates);
	}

	@PutMapping("/{id}")
	public ResponseEntity<String> updateTemplate(@PathVariable int id, @Valid @RequestBody TemplatesRequest request) {
		boolean success = templatesService.updateTemplate(id, request);
		if (success) {
			return ResponseEntity.ok("Template updated successfully");
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteTemplate(@PathVariable int id) {
		boolean success = templatesService.deleteTemplate(id);
		if (success) {
			return ResponseEntity.ok("Template deleted successfully");
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
