package com.hti.smpp.common.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.request.TemplatesRequest;
import com.hti.smpp.common.responce.TemplatesResponse;
import com.hti.smpp.common.service.TemplatesService;
import com.hti.smpp.common.templates.dto.TemplatesDTO;
import com.hti.smpp.common.templates.repository.TemplatesRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Converter;

import jakarta.transaction.Transactional;

@Service
public class TemplatesServiceImpl implements TemplatesService {
	
	private static final Logger logger = LoggerFactory.getLogger(TemplatesServiceImpl.class.getName());
	
	private final TemplatesRepository templatesRepository;

	@Autowired
	public TemplatesServiceImpl(TemplatesRepository templatesRepository) {
		this.templatesRepository = templatesRepository;
	}

	@Autowired
	private UserRepository userRepository;

	@Override
	public ResponseEntity<?> createTemplate(TemplatesRequest request, String username) {

		Optional<User> userOptional = userRepository.findBySystemId(username);
		System.out.println(userOptional.get());
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		TemplatesDTO template = new TemplatesDTO();
		template.setMessage(Converter.UTF16(request.getMessage()));
		userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			template.setMasterId(userOptional.get().getUserId());
		}
		template.setTitle(Converter.UTF16(request.getTitle()));
		TemplatesDTO savedTemplate = null;
		try {
			savedTemplate = templatesRepository.save(template);
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		if (savedTemplate.getMessage() != null && savedTemplate.getMessage().length() > 0) {
			savedTemplate.setMessage(Converter.hexCodePointsToCharMsg(savedTemplate.getMessage()));
		}
		if (savedTemplate.getTitle() != null && savedTemplate.getTitle().length() > 0) {
			savedTemplate.setTitle(Converter.hexCodePointsToCharMsg(savedTemplate.getTitle()));
		}
		
		if(mapToResponse(savedTemplate) != null) {
			return new ResponseEntity<>("Template created successfully",HttpStatus.CREATED);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		
	}

	@Override
	public ResponseEntity<?> getTemplateById(int id, String username) {
		Optional<User> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		userOptional = userRepository.findBySystemId(username);
		Long system_id = null;
		if (userOptional.isPresent()) {
			system_id = userOptional.get().getUserId();
		}
		TemplatesDTO template = templatesRepository.findByIdAndMasterId(id, system_id).orElseThrow(()->new NotFoundException("Template with id: "+id+" not found."));
		if (template != null) {
			if (template.getMessage() != null && template.getMessage().length() > 0) {
				template.setMessage(Converter.hexCodePointsToCharMsg(template.getMessage()));
			}
			if (template.getTitle() != null && template.getTitle().length() > 0) {
				template.setTitle(Converter.hexCodePointsToCharMsg(template.getTitle()));
			}
		}
		return (template != null) ? new ResponseEntity<>(mapToResponse(template),HttpStatus.OK) : ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
	}

	@Override
	public ResponseEntity<?> getAllTemplates(String username) {
		Optional<User> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		userOptional = userRepository.findBySystemId(username);
		Long system_id = null;
		if (userOptional.isPresent()) {
			system_id = userOptional.get().getUserId();
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

		if(!templates.stream().map(this::mapToResponse).collect(Collectors.toList()).isEmpty() && templates.stream().map(this::mapToResponse).collect(Collectors.toList())!=null) {
			return ResponseEntity.ok(templates.stream().map(this::mapToResponse).collect(Collectors.toList()));
		}else {
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		}
		
	}

	@Override
	public ResponseEntity<?> updateTemplate(int id, TemplatesRequest request, String username) {
		Optional<User> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		userOptional = userRepository.findBySystemId(username);
		Long system_id = null;
		if (userOptional.isPresent()) {
			system_id = userOptional.get().getUserId();
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
		if(mapToResponse(updatedTemplate)!=null) {
			return new ResponseEntity<>(mapToResponse(updatedTemplate),HttpStatus.CREATED);
		}else {
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		}
	}

	@Transactional
	@Override
	public ResponseEntity<?> deleteTemplate(int id, String username) {
		boolean isDone = false;
		Optional<User> userOptional = userRepository.findBySystemId(username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		userOptional = userRepository.findBySystemId(username);
		Long system_id = null;
		if (userOptional.isPresent()) {
			system_id = userOptional.get().getUserId();
		}

		try {
			templatesRepository.deleteByIdAndMasterId(id, system_id);
			isDone = true; // Return true if the deletion was successful.
			return ResponseEntity.ok("Template deleted successfully");
		} catch (EmptyResultDataAccessException e) {
			// The template with the given ID was not found, return false.
			isDone = false;
			logger.error(e.toString());
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
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
