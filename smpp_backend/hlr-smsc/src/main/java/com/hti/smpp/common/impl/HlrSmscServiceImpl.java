package com.hti.smpp.common.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.hlr.dto.HlrSmscEntry;
import com.hti.smpp.common.hlr.repository.HlrSmscRepository;
import com.hti.smpp.common.login.dto.Role;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.request.HlrSmscEntryRequest;
import com.hti.smpp.common.service.HlrSmscService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;

@Service
public class HlrSmscServiceImpl implements HlrSmscService {

	private static final Logger logger = LoggerFactory.getLogger(HlrSmscServiceImpl.class);

	@Autowired
	private HlrSmscRepository hlrSmscRepository;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private UserRepository loginRepository;

	@Override
	public ResponseEntity<HlrSmscEntry> save(HlrSmscEntryRequest hlrSmscEntryRequest, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndUser(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			HlrSmscEntry entry = convertToHlrSmscEntry(hlrSmscEntryRequest);

			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

			if (userOptional.isPresent()) {
				UserEntry userEntry = userOptional.get();
				entry.setSystemId(String.valueOf(userEntry.getSystemId()));
				entry.setSystemType(userEntry.getSystemType());
			}

			HlrSmscEntry savedEntry = hlrSmscRepository.save(entry);
			logger.info("HlrSmscEntry saved successfully with ID: {}", savedEntry.getId());
			return ResponseEntity.ok(savedEntry);
		} catch (Exception e) {
			String errorMessage = "Error occurred while saving HlrSmscEntry: " + e.getMessage();
			logger.error(errorMessage, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public ResponseEntity<HlrSmscEntry> update(int id, HlrSmscEntryRequest hlrSmscEntryRequest, String username) {
		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			String systemId = userOptional.map(user -> String.valueOf(user.getSystemId())).orElse(null);

			Optional<HlrSmscEntry> existingEntry = hlrSmscRepository.findByIdAndSystemId(id, systemId);
			if (existingEntry.isEmpty()) {
				logger.warn("HlrSmscEntry with ID {} and systemId {} not found", id, systemId);
				return ResponseEntity.notFound().build();
			}

			HlrSmscEntry updatedEntry = convertToHlrSmscEntry(hlrSmscEntryRequest);
			updatedEntry.setId(id);
			hlrSmscRepository.save(updatedEntry);
			logger.info("HlrSmscEntry updated successfully with ID: {}", id);
			return ResponseEntity.ok(updatedEntry);
		} catch (DataAccessException e) {
			logger.error("DataAccessException occurred while updating HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error("Error occurred while updating HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public ResponseEntity<Void> delete(int id, String username) {
		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			String systemId = userOptional.map(user -> String.valueOf(user.getSystemId())).orElse(null);

			Optional<HlrSmscEntry> existingEntry = hlrSmscRepository.findByIdAndSystemId(id, systemId);
			if (existingEntry.isEmpty()) {
				logger.warn("HlrSmscEntry with ID {} and systemId {} not found", id, systemId);
				return ResponseEntity.notFound().build();
			}

			hlrSmscRepository.deleteById(id);
			logger.info("HlrSmscEntry with ID {} deleted successfully", id);
			return ResponseEntity.noContent().build();
		} catch (DataAccessException e) {
			logger.error("DataAccessException occurred while deleting HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error("Error occurred while deleting HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public ResponseEntity<HlrSmscEntry> getEntry(int id, String username) {
		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			String systemId = userOptional.map(user -> String.valueOf(user.getSystemId())).orElse(null);

			Optional<HlrSmscEntry> existingEntry = hlrSmscRepository.findByIdAndSystemId(id, systemId);
			if (existingEntry.isEmpty()) {
				logger.warn("HlrSmscEntry with ID {} and systemId {} not found", id, systemId);
				return ResponseEntity.notFound().build();
			}

			return ResponseEntity.ok(existingEntry.get());
		} catch (DataAccessException e) {
			logger.error("DataAccessException occurred while fetching HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error("Error occurred while fetching HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public List<HlrSmscEntry> list(String username) {
		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			String systemId = userOptional.map(user -> String.valueOf(user.getSystemId())).orElse(null);

			List<HlrSmscEntry> existingEntries = hlrSmscRepository.findBySystemId(systemId);
			return existingEntries;
		} catch (DataAccessException e) {
			logger.error("DataAccessException occurred while listing HlrSmscEntries for systemId {}: {}", username,
					e.getMessage());
			return Collections.emptyList();
		} catch (Exception e) {
			logger.error("Error occurred while listing HlrSmscEntries for systemId {}: {}", username, e.getMessage());
			return Collections.emptyList();
		}
	}

	private HlrSmscEntry convertToHlrSmscEntry(HlrSmscEntryRequest request) {
		try {
			HlrSmscEntry entry = new HlrSmscEntry();
			entry.setBindmode(request.getBindmode());
			entry.setBound(request.isBound());
			entry.setIp(request.getIp());
			entry.setName(request.getName());
			entry.setPassword(request.getPassword());
			entry.setPort(request.getPort());
			entry.setSleep(request.getSleep());
			logger.info("Converted HlrSmscEntryRequest to HlrSmscEntry successfully");
			return entry;
		} catch (Exception e) {
			logger.error("Error occurred while converting HlrSmscEntryRequest to HlrSmscEntry: {}", e.getMessage());
			throw new RuntimeException("Error occurred while converting HlrSmscEntryRequest to HlrSmscEntry", e);
		}
	}

}
