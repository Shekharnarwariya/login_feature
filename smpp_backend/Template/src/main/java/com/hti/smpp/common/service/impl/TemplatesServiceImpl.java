package com.hti.smpp.common.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import com.hti.smpp.common.messages.repository.RecentResponse;
import com.hti.smpp.common.messages.repository.SummaryReportRepository;
import com.hti.smpp.common.request.TemplatesRequest;
import com.hti.smpp.common.responce.TemplatesResponse;
import com.hti.smpp.common.service.TemplatesService;
import com.hti.smpp.common.templates.dto.TemplatesDTO;
import com.hti.smpp.common.templates.repository.TemplatesRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Converter;
import com.hti.smpp.common.util.PasswordConverter;

import jakarta.transaction.Transactional;

@Service
public class TemplatesServiceImpl implements TemplatesService {

	private static final Logger logger = LoggerFactory.getLogger(TemplatesServiceImpl.class.getName());

	private final TemplatesRepository templatesRepository;

	@Autowired
	private SummaryReportRepository summaryReportRepository;

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

		logger.info("Add Template Request By userId: " + userOptional.get().getUserId() + " Title: "
				+ request.getTitle() + " Message: " + request.getMessage());

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
			logger.error(userOptional.get().getUserId().toString(), e.fillInStackTrace());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			throw new InternalServerException(e.getLocalizedMessage());
		}
		if (savedTemplate.getMessage() != null && savedTemplate.getMessage().length() > 0) {
			savedTemplate.setMessage(Converter.hexCodePointsToCharMsg(savedTemplate.getMessage()));
		}
		if (savedTemplate.getTitle() != null && savedTemplate.getTitle().length() > 0) {
			savedTemplate.setTitle(Converter.hexCodePointsToCharMsg(savedTemplate.getTitle()));
		}

		if (mapToResponse(savedTemplate) != null) {
			logger.info("Add Template Request Successful by userId: " + userOptional.get().getUserId() + " Title: "
					+ request.getTitle() + " Message: " + request.getMessage());
			return new ResponseEntity<>("Template created successfully", HttpStatus.CREATED);
		} else {
			logger.error("Processing Error");
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
		logger.info("Get Template Request By userId: " + userOptional.get().getUserId() + " Template Id: " + id);
		TemplatesDTO template = templatesRepository.findByIdAndMasterId(id, system_id)
				.orElseThrow(() -> new NotFoundException("Template with id: " + id + " not found."));
		if (template != null) {
			if (template.getMessage() != null && template.getMessage().length() > 0) {
				template.setMessage(Converter.hexCodePointsToCharMsg(template.getMessage()));
			}
			if (template.getTitle() != null && template.getTitle().length() > 0) {
				template.setTitle(Converter.hexCodePointsToCharMsg(template.getTitle()));
			}
		}

		if (template != null) {
			logger.info("Get Template Request Successful By userId: " + userOptional.get().getUserId()
					+ " Template Id: " + id);
			return new ResponseEntity<>(mapToResponse(template), HttpStatus.OK);
		} else {
			logger.error("Error Processing Template by id: " + id);
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		}

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

		logger.info("Get All Templates Requested by userId: " + system_id);

		List<TemplatesDTO> templates = null;
		try {
			templates = (List<TemplatesDTO>) templatesRepository.findByMasterId(system_id);
		} catch (Exception e) {
			logger.error("Error processing templates: " + e.toString());
			throw new NotFoundException("Template not found for system id: " + system_id);
		}
		templates.forEach(template -> {
			if (template.getMessage() != null && !template.getMessage().isEmpty()) {
				template.setMessage(Converter.hexCodePointsToCharMsg(template.getMessage()));
			}
			if (template.getTitle() != null && !template.getTitle().isEmpty()) {
				template.setTitle(Converter.hexCodePointsToCharMsg(template.getTitle()));
			}
		});

		if (!templates.stream().map(this::mapToResponse).collect(Collectors.toList()).isEmpty()
				&& templates.stream().map(this::mapToResponse).collect(Collectors.toList()) != null) {
			logger.info("Get all templates request successful for userId: " + system_id);
			return ResponseEntity.ok(templates.stream().map(this::mapToResponse).collect(Collectors.toList()));
		} else {
			logger.error("Error Processing Request for Get All Templates.");
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
		logger.info(" Update template request by userId: " + system_id + " title: " + request.getTitle() + " message: "
				+ request.getMessage());

		TemplatesDTO template = templatesRepository.findByIdAndMasterId(id, system_id)
				.orElseThrow(() -> new NotFoundException("Template with id: " + id + " not found."));
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

		} else {
			logger.info(system_id + " <-- No template to update -->");
		}
		if (mapToResponse(updatedTemplate) != null) {
			logger.info("Update Template Request Successful: " + userOptional.get().getUserId() + " Title: "
					+ request.getTitle() + " Message: " + request.getMessage());
			return new ResponseEntity<>(mapToResponse(updatedTemplate), HttpStatus.CREATED);
		} else {
			logger.error("Processing error.");
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
		logger.info("userId: " + system_id + " delete templateId: " + id);
		try {
			templatesRepository.deleteByIdAndMasterId(id, system_id);
			isDone = true; // Return true if the deletion was successful.
			logger.info("Template deleted successful with id: " + id);
			return ResponseEntity.ok("Template deleted successfully");
		} catch (EmptyResultDataAccessException e) {
			// The template with the given ID was not found, return false.
			isDone = false;
			logger.error("delete templateId: " + id + " <-- No template to delete -->");
			logger.error("Error: " + e.getMessage());
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

	@Override
	public ResponseEntity<?> RecentUseTemplate(String username) {

		Optional<User> userOptional = userRepository.findBySystemId(username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			PasswordConverter passwordConverter = new PasswordConverter();
			List<RecentResponse> recentContent = summaryReportRepository
					.getRecentContent(passwordConverter.convertToDatabaseColumn(username));
			System.out.println(recentContent);
			// Logging success
			logger.info("RecentUseTemplate operation succeeded for user: {}", username);

			// Convert each element in the stream and collect the results into a list
//			List<String> convertedRecentContent = recentContent.stream()
//			        .map(entry -> entry.split(",")[0]) // Extract the first value after splitting
//			       // .map(Converter::hexCodePointsToCharMsg) // Convert using hexCodePointsToCharMsg
//					.map(passwordConverter::convertToEntityAttribute) // Convert using passwordConverter
//			        .collect(Collectors.toList());

			return ResponseEntity.ok("");
		} catch (Exception e) {
			// Logging other exceptions
			logger.error("An unexpected error occurred: {}", e.getMessage(), e);
			throw new InternalServerException("An unexpected error occurred. Please try again." + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> searchRecentTemplates(String username, String search) {

		Optional<User> userOptional = userRepository.findBySystemId(username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			Set<String> recentContent = summaryReportRepository.getRecentContentWithSearch(username, search);
			// Logging success
			logger.info("Search RecentUseTemplate operation succeeded for user: {}", username);
			// Return the result
			return ResponseEntity.ok(recentContent);
		} catch (Exception e) {
			// Logging other exceptions
			logger.error("An unexpected error occurred: {}", e.getMessage(), e);
			throw new InternalServerException("An unexpected error occurred. Please try again." + e.getMessage());
		}
	}
}
