package com.hti.smpp.common.service;

import java.util.List;

import com.hti.smpp.common.request.TemplatesRequest;
import com.hti.smpp.common.responce.TemplatesResponse;

public interface TemplatesService {

	public TemplatesResponse createTemplate(TemplatesRequest request);

	public TemplatesResponse getTemplateById(int id);

	public List<TemplatesResponse> getAllTemplates();

	public TemplatesResponse updateTemplate(int id, TemplatesRequest request);

	public boolean deleteTemplate(int id);
}
