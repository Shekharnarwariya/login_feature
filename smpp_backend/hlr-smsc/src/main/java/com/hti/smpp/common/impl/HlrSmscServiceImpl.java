package com.hti.smpp.common.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.hlr.dto.HlrSmscEntry;
import com.hti.smpp.common.hlr.repository.HlrSmscRepository;
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

	// Method to save a new HLR SMS entry
	@Override
	public ResponseEntity<HlrSmscEntry> save(HlrSmscEntryRequest hlrSmscEntryRequest, String username) {
		   // Fetch user information
		
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			
			 // Check if user has required authorization
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			 // Convert and save the HLR SMS entry
			HlrSmscEntry entry = convertToHlrSmscEntry(hlrSmscEntryRequest);
			entry.setSystemId(String.valueOf(user.getSystemId()));
			entry.setSystemType(user.getSystemType());

			HlrSmscEntry savedEntry = hlrSmscRepository.save(entry);
			logger.info("HlrSmscEntry saved successfully with ID: {}", savedEntry.getId());
			return ResponseEntity.ok(savedEntry);
		} catch (Exception e) {
			String errorMessage = "Error occurred while saving HlrSmscEntry: " + e.getMessage();
			logger.error(errorMessage, e);
			throw new InternalServerException("Error occurred while saving HlrSmscEntry: " + e.getMessage());
		}
	}

	// Method to update an existing HLR SMS entry
	@Override
	public ResponseEntity<HlrSmscEntry> update(int id, HlrSmscEntryRequest hlrSmscEntryRequest, String username) {
		// Fetch user information
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		try {
			// Check if the HLR SMS entry exists
			Optional<HlrSmscEntry> existingEntry = hlrSmscRepository.findByIdAndSystemId(id, username);
			if (existingEntry.isEmpty()) {
				logger.warn("HlrSmscEntry with ID {} and systemId {} not found", id, username);
				return ResponseEntity.notFound().build();
			}

			  // Update and save the HLR SMS entry
			HlrSmscEntry updatedEntry = convertToHlrSmscEntry(hlrSmscEntryRequest);
			updatedEntry.setId(id);
			hlrSmscRepository.save(updatedEntry);
			logger.info("HlrSmscEntry updated successfully with ID: {}", id);
			return ResponseEntity.ok(updatedEntry);
		} catch (DataAccessException e) {
			logger.error("DataAccessError occurred while updating HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error("Error occurred while updating HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	  // Method to delete an existing HLR SMS entry
	@Override
	public ResponseEntity<Void> delete(int id, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			
			String systemId = user.getSystemId();
			Optional<HlrSmscEntry> existingEntry = hlrSmscRepository.findByIdAndSystemId(id, systemId);
			if (existingEntry.isEmpty()) {
				logger.warn("HlrSmscEntry with ID {} and systemId {} not found", id, systemId);
				return ResponseEntity.notFound().build();
			}

			   // Delete the HLR SMS entry
			hlrSmscRepository.deleteById(id);
			logger.info("HlrSmscEntry with ID {} deleted successfully", id);
			return ResponseEntity.noContent().build();
		} catch (DataAccessException e) {
			logger.error("DataAccessError occurred while deleting HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error("Error occurred while deleting HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// Method to get details of a specific HLR SMS entry
	@Override
	public ResponseEntity<HlrSmscEntry> getEntry(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			String systemId = user.getSystemId();

			Optional<HlrSmscEntry> existingEntry = hlrSmscRepository.findByIdAndSystemId(id, systemId);
			if (existingEntry.isEmpty()) {
				logger.warn("HlrSmscEntry with ID {} and systemId {} not found", id, systemId);
				return ResponseEntity.notFound().build();
			}

			return ResponseEntity.ok(existingEntry.get());
		} catch (DataAccessException e) {
			logger.error("DataAccessError occurred while fetching HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error("Error occurred while fetching HlrSmscEntry with ID {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	 // Method to list all HLR SMS entries for a user

	@Override
	public List<HlrSmscEntry> list(String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		try {
			String systemId = user.getSystemId();

			List<HlrSmscEntry> existingEntries = hlrSmscRepository.findBySystemId(systemId);
			return existingEntries;
		} catch (DataAccessException e) {
			logger.error("DataAccessError occurred while listing HlrSmscEntries for systemId {}: {}", username,
					e.getMessage());
			return Collections.emptyList();
		} catch (Exception e) {
			logger.error("Error occurred while listing HlrSmscEntries for systemId {}: {}", username, e.getMessage());
			return Collections.emptyList();
		}
	}
	
	// Helper method to convert HlrSmscEntryRequest to HlrSmscEntry

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
