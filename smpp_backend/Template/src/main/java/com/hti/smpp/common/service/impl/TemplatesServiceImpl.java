package com.hti.smpp.common.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.TemplatesRequest;
import com.hti.smpp.common.responce.TemplatesResponse;
import com.hti.smpp.common.service.TemplatesService;
import com.hti.smpp.common.templates.dto.TemplatesDTO;
import com.hti.smpp.common.templates.repository.TemplatesRepository;
import com.hti.smpp.common.util.Converter;

@Service
public class TemplatesServiceImpl implements TemplatesService {

	private final TemplatesRepository templatesRepository;

	@Autowired
	public TemplatesServiceImpl(TemplatesRepository templatesRepository) {
		this.templatesRepository = templatesRepository;
	}

	@Override
	public TemplatesResponse createTemplate(TemplatesRequest request) {
		TemplatesDTO template = new TemplatesDTO();
		template.setMessage(Converter.UTF16(request.getMessage()));
		template.setMasterId("usertest1");
		template.setTitle(Converter.UTF16(request.getTitle()));
		TemplatesDTO savedTemplate = templatesRepository.save(template);
		return mapToResponse(savedTemplate);
	}

	@Override
	public TemplatesResponse getTemplateById(int id) {
		TemplatesDTO template = templatesRepository.findById(id).orElse(null);

		if (template.getMessage() != null && template.getMessage().length() > 0) {
			template.setMessage(Converter.hexCodePointsToCharMsg(template.getMessage()));
		}
		if (template.getTitle() != null && template.getTitle().length() > 0) {
			template.setTitle(Converter.hexCodePointsToCharMsg(template.getTitle()));
		}
		return (template != null) ? mapToResponse(template) : null;
	}

	@Override
	public List<TemplatesResponse> getAllTemplates() {
		List<TemplatesDTO> templates = (List<TemplatesDTO>) templatesRepository.findAll();
		templates.forEach(template -> {
			if (template.getMessage() != null && !template.getMessage().isEmpty()) {
				template.setMessage(Converter.hexCodePointsToCharMsg(template.getMessage()));
			}
			if (template.getTitle() != null && !template.getTitle().isEmpty()) {
				template.setTitle(Converter.hexCodePointsToCharMsg(template.getTitle()));
			}
		});
		return templates.stream().map(this::mapToResponse).collect(Collectors.toList());
	}

	@Override
	public TemplatesResponse updateTemplate(int id, TemplatesRequest request) {
		TemplatesDTO template = templatesRepository.findById(id).orElse(null);
		TemplatesDTO updatedTemplate = null;
		if (template != null) {
			template.setMessage(Converter.UTF16(request.getMessage()));
			// template.setMasterId(request.getMasterId());
			template.setTitle(Converter.UTF16(request.getTitle()));
			updatedTemplate = templatesRepository.save(template);
			if (updatedTemplate.getMessage() != null && !updatedTemplate.getMessage().isEmpty()) {
				updatedTemplate.setMessage(Converter.hexCodePointsToCharMsg(updatedTemplate.getMessage()));
			}
			if (updatedTemplate.getTitle() != null && !updatedTemplate.getTitle().isEmpty()) {
				updatedTemplate.setTitle(Converter.hexCodePointsToCharMsg(updatedTemplate.getTitle()));
			}

		}
		return mapToResponse(updatedTemplate);
	}

	@Override
	public boolean deleteTemplate(int id) {
		try {
			templatesRepository.deleteById(id);
			return true; // Return true if the deletion was successful.
		} catch (EmptyResultDataAccessException e) {
			// The template with the given ID was not found, return false.
			return false;
		}
	}

	private TemplatesResponse mapToResponse(TemplatesDTO template) {
		TemplatesResponse response = new TemplatesResponse();
		response.setId(template.getId());
		response.setMessage(template.getMessage());
		response.setMasterId(template.getMasterId());
		response.setTitle(template.getTitle());
		return response;
	}

}
