package com.hti.smpp.common.service;


import org.springframework.http.ResponseEntity;
import com.hti.smpp.common.request.TemplatesRequest;

public interface TemplatesService {

	public ResponseEntity<?> createTemplate(TemplatesRequest request, String userName);

	public ResponseEntity<?> getTemplateById(int id, String userName);

	public ResponseEntity<?> getAllTemplates(String userName);

	public ResponseEntity<?> updateTemplate(int id, TemplatesRequest request,String userName);

	public ResponseEntity<?> deleteTemplate(int id, String userName);

	public ResponseEntity<?> RecentUseTemplate(String username);

	public ResponseEntity<?> searchRecentTemplates(String username, String search);
}
