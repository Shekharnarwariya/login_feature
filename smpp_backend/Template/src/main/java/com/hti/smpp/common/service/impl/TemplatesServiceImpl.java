package com.hti.smpp.common.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.request.TemplatesRequest;
import com.hti.smpp.common.responce.TemplatesResponse;
import com.hti.smpp.common.service.TemplatesService;
import com.hti.smpp.common.templates.dto.TemplatesDTO;
import com.hti.smpp.common.templates.repository.TemplatesRepository;
import com.hti.smpp.common.util.Converter;

import jakarta.transaction.Transactional;

@Service
public class TemplatesServiceImpl implements TemplatesService {

	private final TemplatesRepository templatesRepository;

	@Autowired
	public TemplatesServiceImpl(TemplatesRepository templatesRepository) {
		this.templatesRepository = templatesRepository;
	}

	@Autowired
	private UserRepository userRepository;

	@Override
	public TemplatesResponse createTemplate(TemplatesRequest request, String username) {
		TemplatesDTO template = new TemplatesDTO();
		template.setMessage(Converter.UTF16(request.getMessage()));
		Optional<User> userOptional = userRepository.findByUsername(username);
		if (userOptional.isPresent()) {
			template.setMasterId(userOptional.get().getSystem_id());
		}
		template.setTitle(Converter.UTF16(request.getTitle()));
		TemplatesDTO savedTemplate = templatesRepository.save(template);
		if (savedTemplate.getMessage() != null && savedTemplate.getMessage().length() > 0) {
			savedTemplate.setMessage(Converter.hexCodePointsToCharMsg(savedTemplate.getMessage()));
		}
		if (savedTemplate.getTitle() != null && savedTemplate.getTitle().length() > 0) {
			savedTemplate.setTitle(Converter.hexCodePointsToCharMsg(savedTemplate.getTitle()));
		}
		return mapToResponse(savedTemplate);
	}

	@Override
	public TemplatesResponse getTemplateById(int id, String username) {
		Optional<User> userOptional = userRepository.findByUsername(username);
		Long system_id = null;
		if (userOptional.isPresent()) {
			system_id = userOptional.get().getSystem_id();
		}
		TemplatesDTO template = templatesRepository.findByIdAndMasterId(id, system_id).orElse(null);
		if (template != null) {
			if (template.getMessage() != null && template.getMessage().length() > 0) {
				template.setMessage(Converter.hexCodePointsToCharMsg(template.getMessage()));
			}
			if (template.getTitle() != null && template.getTitle().length() > 0) {
				template.setTitle(Converter.hexCodePointsToCharMsg(template.getTitle()));
			}
		}
		return (template != null) ? mapToResponse(template) : null;
	}

	@Override
	public List<TemplatesResponse> getAllTemplates(String username) {
		Optional<User> userOptional = userRepository.findByUsername(username);
		Long system_id = null;
		if (userOptional.isPresent()) {
			system_id = userOptional.get().getSystem_id();
		}
		List<TemplatesDTO> templates = (List<TemplatesDTO>) templatesRepository.findByMasterId(system_id);
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
	public TemplatesResponse updateTemplate(int id, TemplatesRequest request, String username) {
		Optional<User> userOptional = userRepository.findByUsername(username);
		Long system_id = null;
		if (userOptional.isPresent()) {
			system_id = userOptional.get().getSystem_id();
		}
		TemplatesDTO template = templatesRepository.findByIdAndMasterId(id, system_id).orElse(null);
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

	@Transactional
	@Override
	public boolean deleteTemplate(int id, String username) {
		Optional<User> userOptional = userRepository.findByUsername(username);
		Long system_id = null;
		if (userOptional.isPresent()) {
			system_id = userOptional.get().getSystem_id();
		}

		try {
			templatesRepository.deleteByIdAndMasterId(id, system_id);
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
