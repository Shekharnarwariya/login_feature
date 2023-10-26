package com.hti.smpp.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.request.TemplatesRequest;
import com.hti.smpp.common.responce.TemplatesResponse;
import com.hti.smpp.common.service.TemplatesService;

@RestController
@RequestMapping("/template")
public class TemplatesController {

	private final TemplatesService templatesService;

	@Autowired
	public TemplatesController(TemplatesService templatesService) {
		this.templatesService = templatesService;
	}

	@MutationMapping("createTemplate")
	public TemplatesResponse createTemplate(@Argument TemplatesRequest request) {
		TemplatesResponse response = templatesService.createTemplate(request);
		return response;
	}

	@QueryMapping("getTemplateById")
	public ResponseEntity<TemplatesResponse> getTemplateById(@Argument int id) {
		TemplatesResponse response = templatesService.getTemplateById(id);
		if (response != null) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@QueryMapping("allTemplate")
	public List<TemplatesResponse> getAllTemplates() {
		List<TemplatesResponse> templates = templatesService.getAllTemplates();
		return (templates);
	}

	@MutationMapping("updateTemplateById")
	public TemplatesResponse updateTemplate(@Argument("id") int id, @Argument("request") TemplatesRequest request) {

		return templatesService.updateTemplate(id, request);
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
