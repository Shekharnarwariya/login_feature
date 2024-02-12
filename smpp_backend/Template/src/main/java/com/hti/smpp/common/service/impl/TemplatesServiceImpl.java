package com.hti.smpp.common.service.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
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
import com.hti.smpp.common.messages.repository.SummaryReportRepository;
import com.hti.smpp.common.request.TemplatesRequest;
import com.hti.smpp.common.responce.TemplatesResponse;
import com.hti.smpp.common.service.TemplatesService;
import com.hti.smpp.common.templates.dto.TemplatesDTO;
import com.hti.smpp.common.templates.repository.TemplatesRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Converter;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.transaction.Transactional;

@Service
public class TemplatesServiceImpl implements TemplatesService {

	private static final Logger logger = LoggerFactory.getLogger(TemplatesServiceImpl.class.getName());

	private final TemplatesRepository templatesRepository;

	@Autowired
	private SummaryReportRepository summaryReportRepository;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Autowired
	public TemplatesServiceImpl(TemplatesRepository templatesRepository) {
		this.templatesRepository = templatesRepository;
	}

	@Autowired
	private UserEntryRepository userRepository;

	// Method for creating a new template
	@Override
	public ResponseEntity<?> createTemplate(TemplatesRequest request, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		logger.info(messageResourceBundle.getLogMessage("add.template.req"), user.getId(), request.getTitle(), request.getMessage());


		TemplatesDTO template = new TemplatesDTO();
		template.setMessage(Converter.UTF16(request.getMessage()));
		if (userOptional.isPresent()) {
			template.setMasterId(user.getSystemId());
		}
		template.setTitle(Converter.UTF16(request.getTitle()));
		// Set createdOn field automatically
		template.setCreatedOn(new Date());

		TemplatesDTO savedTemplate = null;
		try {
			savedTemplate = templatesRepository.save(template);
		} catch (Exception e) {
			logger.error(userOptional.get().getId() + " " + e.fillInStackTrace());
			logger.error(messageResourceBundle.getLogMessage("process.error"), e.getMessage(), e.getCause());

			throw new InternalServerException(e.getLocalizedMessage());
		}
		if (savedTemplate.getMessage() != null && savedTemplate.getMessage().length() > 0) {
			savedTemplate.setMessage(Converter.hexCodePointsToCharMsg(savedTemplate.getMessage()));
		}
		if (savedTemplate.getTitle() != null && savedTemplate.getTitle().length() > 0) {
			savedTemplate.setTitle(Converter.hexCodePointsToCharMsg(savedTemplate.getTitle()));
		}

		if (mapToResponse(savedTemplate) != null) {
			logger.info(messageResourceBundle.getLogMessage("add.template.success"), userOptional.get().getId(), request.getTitle(), request.getMessage());

			return new ResponseEntity<>(messageResourceBundle.getMessage("template.created.success"), HttpStatus.CREATED);
		} else {
			logger.error(messageResourceBundle.getLogMessage("processing.error"));

			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

	}

	// Method for retrieving a template by ID
	@Override
	public ResponseEntity<?> getTemplateById(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		String system_id = user.getSystemId();

		logger.info(messageResourceBundle.getLogMessage("get.template.req"), userOptional.get().getId(), id);

		TemplatesDTO template = templatesRepository.findByIdAndMasterId(id, system_id)
				.orElseThrow(() -> new NotFoundException("Template with id: " + id + ": "
						+ messageResourceBundle.getExMessage(ConstantMessages.TEMPLATE_NOT_FOUND)));
		if (template != null) {
			if (template.getMessage() != null && template.getMessage().length() > 0) {
				template.setMessage(Converter.hexCodePointsToCharMsg(template.getMessage()));
			}
			if (template.getTitle() != null && template.getTitle().length() > 0) {
				template.setTitle(Converter.hexCodePointsToCharMsg(template.getTitle()));
			}
		}

		if (template != null) {
			logger.info(messageResourceBundle.getLogMessage("get.template.success"), userOptional.get().getId(), id);

			return new ResponseEntity<>(mapToResponse(template), HttpStatus.OK);
		} else {
			logger.error(messageResourceBundle.getLogMessage("error.processing.template"), id);

			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		}

	}

	// Method for retrieving all templates
	@Transactional
	@Override
	public ResponseEntity<?> getAllTemplates(String username, LocalDate fromDate, LocalDate toDate, String search) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		String system_id = user.getSystemId();

		logger.info(messageResourceBundle.getLogMessage("get.all.templates.req"), system_id);


		List<TemplatesDTO> templates = null;
		try {
			if (fromDate != null && toDate != null) {
				templates = templatesRepository.findByMasterIdAndCreatedOnBetween(system_id, fromDate, toDate);
			} else {
				templates = templatesRepository.findByMasterId(system_id);
			}
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("error.processing.templates"), e.toString());

			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.TEMPLATE_NOT_FOUND)
					+ "for system id: " + system_id);
		}

		if (!templates.stream().map(this::mapToResponse).collect(Collectors.toList()).isEmpty()
				&& templates.stream().map(this::mapToResponse).collect(Collectors.toList()) != null) {
			List<TemplatesResponse> collect = templates.stream().map(this::mapToResponse).collect(Collectors.toList());
			collect.forEach(template -> {
				if (template.getMessage() != null && !template.getMessage().isEmpty()) {
					template.setMessage(Converter.hexCodePointsToCharMsg(template.getMessage()));
				}
				if (template.getTitle() != null && !template.getTitle().isEmpty()) {
					template.setTitle(Converter.hexCodePointsToCharMsg(template.getTitle()));
				}
			});

			if (search != null && !search.isEmpty()) {
				collect = collect.stream()
						.filter(template -> (template.getMessage() != null
								&& template.getMessage().toLowerCase().contains(search.toLowerCase()))
								|| (template.getTitle() != null
										&& template.getTitle().toLowerCase().contains(search.toLowerCase())))
						.collect(Collectors.toList());
			}

			logger.info(messageResourceBundle.getLogMessage("get.all.templates.success"), system_id);

			return ResponseEntity.ok(collect);
		} else {
			logger.error(messageResourceBundle.getLogMessage("error.processing.get.all.templates"));

			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		}

	}

	// Method for updating a template by ID
	@Override
	public ResponseEntity<?> updateTemplate(int id, TemplatesRequest request, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		String system_id = user.getSystemId();
		logger.info(messageResourceBundle.getLogMessage("update.template.req"), system_id, request.getTitle(), request.getMessage());


		TemplatesDTO template = templatesRepository.findByIdAndMasterId(id, system_id)
				.orElseThrow(() -> new NotFoundException("Template with id: " + id + ": "
						+ messageResourceBundle.getExMessage(ConstantMessages.TEMPLATE_NOT_FOUND)));
		// Error handling statement
		TemplatesDTO updatedTemplate = null;
		if (template != null) {
			template.setMessage(Converter.UTF16(request.getMessage()));
			// template.setMasterId(request.getMasterId());
			template.setTitle(Converter.UTF16(request.getTitle()));

			// Set updatedOn field automatically
			template.setUpdatedOn(new Date());

			updatedTemplate = templatesRepository.save(template);
			if (updatedTemplate.getMessage() != null && !updatedTemplate.getMessage().isEmpty()) {
				updatedTemplate.setMessage(Converter.hexCodePointsToCharMsg(updatedTemplate.getMessage()));
			}
			if (updatedTemplate.getTitle() != null && !updatedTemplate.getTitle().isEmpty()) {
				updatedTemplate.setTitle(Converter.hexCodePointsToCharMsg(updatedTemplate.getTitle()));
			}

		} else {
			logger.info(messageResourceBundle.getLogMessage("no.template.to.update"), system_id);

		}
		if (mapToResponse(updatedTemplate) != null) {
			logger.info(messageResourceBundle.getLogMessage("update.template.success"), userOptional.get().getId(), request.getTitle(), request.getMessage());

			return new ResponseEntity<>(mapToResponse(updatedTemplate), HttpStatus.CREATED);
		} else {
			logger.error(messageResourceBundle.getLogMessage("processing.error"));
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		}
	}

	// Transactional method for deleting a template by ID
	@Transactional
	@Override
	public ResponseEntity<?> deleteTemplate(int id, String username) {
		boolean isDone = false;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		String system_id = user.getSystemId();
		logger.info(messageResourceBundle.getLogMessage("delete.template.info"), system_id, id);

		if (!templatesRepository.existsById(id))
			throw new NotFoundException(" templateId: " + id + " :"
					+ messageResourceBundle.getExMessage(ConstantMessages.TEMPLATE_NOT_FOUND));

		try {
			templatesRepository.deleteByIdAndMasterId(id, system_id);
			isDone = true; // Return true if the deletion was successful.
			logger.info(messageResourceBundle.getLogMessage("template.deleted.success"), id);

			return ResponseEntity.ok(messageResourceBundle.getMessage("template.deleted.success"));
		} catch (EmptyResultDataAccessException e) {
			// The template with the given ID was not found, return false.
			isDone = false;
			logger.error(messageResourceBundle.getLogMessage("delete.template.error"), id);

			logger.error("Error: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		}
	}


    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return dateFormat.format(date);
    }
	// Method for mapping TemplatesDTO to TemplatesResponse
	private TemplatesResponse mapToResponse(TemplatesDTO template) {
		TemplatesResponse response = new TemplatesResponse();
		response.setId(template.getId());
		response.setMessage(template.getMessage());
		response.setMasterId(template.getMasterId());
		response.setTitle(template.getTitle());
		  response.setCreatedOn(formatDate(template.getCreatedOn()));
		    response.setUpdatedOn(formatDate(template.getUpdatedOn()));
		return response;
	}

	// Method for retrieving recently used template
	@Override
	public ResponseEntity<?> RecentUseTemplate(String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			List<Object[]> recentContent = summaryReportRepository.getRecentContent(username);
			logger.info(messageResourceBundle.getLogMessage("recent.use.template.success"), username);

			Set<String> convertedRecentContent = recentContent.stream()
					.filter(e -> e != null && e.length > 0 && e[0] != null).map(e -> e[0].toString())
					.map(Converter::hexCodePointsToCharMsg).map(String::toLowerCase).collect(Collectors.toSet());

			return ResponseEntity.ok(convertedRecentContent);
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("unexpected.error"), e.getMessage(), e);

			throw new InternalServerException(
					messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
		}
	}

}
