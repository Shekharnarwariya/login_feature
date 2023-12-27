package com.hti.smpp.common.service;


import org.springframework.http.ResponseEntity;
import com.hti.smpp.common.request.TemplatesRequest;
//Service interface for handling template-related operations
public interface TemplatesService {
	 // Method for creating a new template
	public ResponseEntity<?> createTemplate(TemplatesRequest request, String userName);
	 // Method for retrieving a template by ID
	public ResponseEntity<?> getTemplateById(int id, String userName);
	 // Method for retrieving all templates
	public ResponseEntity<?> getAllTemplates(String userName);
	   // Method for updating a template by ID
	public ResponseEntity<?> updateTemplate(int id, TemplatesRequest request,String userName);
	 // Method for deleting a template by ID
	public ResponseEntity<?> deleteTemplate(int id, String userName);
	// Method for retrieving recently used template
	public ResponseEntity<?> RecentUseTemplate(String username);

}
