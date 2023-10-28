package com.hti.smpp.common.service;

import java.util.List;

import com.hti.smpp.common.request.TemplatesRequest;
import com.hti.smpp.common.responce.TemplatesResponse;

public interface TemplatesService {

	public TemplatesResponse createTemplate(TemplatesRequest request, String username);

	public TemplatesResponse getTemplateById(int id, String username);

	public List<TemplatesResponse> getAllTemplates( String username);

	public TemplatesResponse updateTemplate(int id, TemplatesRequest request,String username);

	public boolean deleteTemplate(int id, String username);
}
