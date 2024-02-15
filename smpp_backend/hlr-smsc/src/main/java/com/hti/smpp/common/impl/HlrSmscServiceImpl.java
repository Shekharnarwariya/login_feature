package com.hti.smpp.common.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.exception.DataAccessError;
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
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.MessageResourceBundle;

@Service
public class HlrSmscServiceImpl implements HlrSmscService {

	private static final Logger logger = LoggerFactory.getLogger(HlrSmscServiceImpl.class);

	@Autowired
	private HlrSmscRepository hlrSmscRepository;

	@Autowired
	private UserEntryRepository userRepository;
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	// Method to save a new HLR SMS entry
	@Override
	public ResponseEntity<?> save(HlrSmscEntryRequest hlrSmscEntryRequest, String username) {
		// Fetch user information

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			// Check if user has required authorization
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
        
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
      
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		try {
			// Convert and save the HLR SMS entry
			HlrSmscEntry entry = convertToHlrSmscEntry(hlrSmscEntryRequest);
			entry.setSystemId(String.valueOf(user.getSystemId()));
			entry.setSystemType(user.getSystemType());

			HlrSmscEntry savedEntry = hlrSmscRepository.save(entry);
      
			logger.info(messageResourceBundle.getMessage("hlr.smsc.entry.saved.successfully.message"), savedEntry.getId());

			return ResponseEntity.ok(savedEntry);
		} catch (Exception e) {
			//String errorMessage = "Error occurred while saving HlrSmscEntry: " + e.getMessage();
			logger.error(messageResourceBundle.getMessage("hlr.smsc.entry.save.error.message"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_SAVING_HLR_SMSC_ENTRY_MESSAGE, new Object[] {e.getMessage()}));
			logger.info(messageResourceBundle.getLogMessage("hlr.smsc.saved.successfully.info"), savedEntry.getId());
			return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.HLR_SMSC_SAVED_SUCCESS, new Object[] {savedEntry.getId()}),HttpStatus.CREATED);
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("hlr.smsc.save.error"), e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.HLR_SMSC_SAVE_ERROR,
					new Object[] { e.getMessage() }));
		}
	}

	// Method to update an existing HLR SMS entry
	@Override
	public ResponseEntity<?> update(int id, HlrSmscEntryRequest hlrSmscEntryRequest, String username) {
		// Fetch user information
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

		try {
			// Check if the HLR SMS entry exists
			Optional<HlrSmscEntry> existingEntry = hlrSmscRepository.findByIdAndSystemId(id, username);
			if (existingEntry.isEmpty()) {
				logger.warn(messageResourceBundle.getMessage("entry.not.found.message"), id, username);

				return ResponseEntity.notFound().build();
				logger.warn(messageResourceBundle.getLogMessage("hlr.smsc.not.found.warn"), id, username);
				throw new NotFoundException(messageResourceBundle
						.getExMessage(ConstantMessages.HLR_SMSC_ENTRY_NOT_FOUND, new Object[] { id, username }));
			}
			
			// Update and save the HLR SMS entry
			HlrSmscEntry updatedEntry = convertToHlrSmscEntry(hlrSmscEntryRequest);
			updatedEntry.setId(id);
			updatedEntry.setSystemId(String.valueOf(user.getSystemId()));
			updatedEntry.setSystemType(user.getSystemType());
			
			hlrSmscRepository.save(updatedEntry);
			logger.info(messageResourceBundle.getMessage("update.success.message"), username);

			return ResponseEntity.ok(updatedEntry);
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getMessage("data.access.update.error.message"), username, e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error(messageResourceBundle.getMessage("update.error.message"), username, e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			logger.info(messageResourceBundle.getLogMessage("hlr.smsc.updated.successfully.info"), id);
			return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.HLR_SMSC_UPDATED_SUCCESS, new Object[] {id}),HttpStatus.CREATED);
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("hlr.smsc.update.data.access.error"), id, e.getMessage());
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.HLR_SMSC_DATA_ACCESS_ERROR,
					new Object[] { id, e.getMessage() }));
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("hlr.smsc.update.error"), id, e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.HLR_SMSC_UPDATE_ERROR,
					new Object[] { id, e.getMessage() }));
		}

	}

	// Method to delete an existing HLR SMS entry
	@Override
	public ResponseEntity<?> delete(int id, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		try {

			String systemId = user.getSystemId();
			Optional<HlrSmscEntry> existingEntry = hlrSmscRepository.findByIdAndSystemId(id, systemId);
			if (existingEntry.isEmpty()) {
				logger.warn(messageResourceBundle.getMessage("entry.not.found.message"), username, systemId);

				return ResponseEntity.notFound().build();
				logger.warn(messageResourceBundle.getLogMessage("hlr.smsc.not.found.warn"), id, username);
				throw new NotFoundException(messageResourceBundle
						.getExMessage(ConstantMessages.HLR_SMSC_ENTRY_NOT_FOUND, new Object[] { id, username }));
			}

			// Delete the HLR SMS entry
			hlrSmscRepository.deleteById(id);
			logger.info(messageResourceBundle.getMessage("delete.success.message"), username);

			return ResponseEntity.noContent().build();
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getMessage("data.access.delete.error.message"), username, e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error(messageResourceBundle.getMessage("delete.error.message"), username, e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			logger.info(messageResourceBundle.getLogMessage("hlr.smsc.deleted.successfully.info"), id);

			return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.HLR_SMSC_DELETE_SUCCESS, new Object[] {id}),HttpStatus.OK);
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("hlr.smsc.update.data.access.error"), id, e.getMessage());
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.HLR_SMSC_DATA_ACCESS_ERROR,
					new Object[] { id, e.getMessage() }));
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("hlr.smsc.update.error"), id, e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.HLR_SMSC_UPDATE_ERROR,
					new Object[] { id, e.getMessage() }));
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
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		try {
			String systemId = user.getSystemId();

			Optional<HlrSmscEntry> existingEntry = hlrSmscRepository.findByIdAndSystemId(id, systemId);
			if (existingEntry.isEmpty()) {
				logger.warn(messageResourceBundle.getMessage("hlr.smsc.entry.not.found.message"), id, systemId);

				return ResponseEntity.notFound().build();
				logger.warn(messageResourceBundle.getLogMessage("hlr.smsc.not.found.warn"), id, username);
				throw new NotFoundException(messageResourceBundle
						.getExMessage(ConstantMessages.HLR_SMSC_ENTRY_NOT_FOUND, new Object[] { id, username }));
			}

			return ResponseEntity.ok(existingEntry.get());
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getMessage("data.access.fetch.error.message"), id, e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error(messageResourceBundle.getMessage("fetch.error.message"), id, e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			logger.error(messageResourceBundle.getLogMessage("hlr.smsc.update.data.access.error"), id, e.getMessage());
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.HLR_SMSC_DATA_ACCESS_ERROR,
					new Object[] { id, e.getMessage() }));
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("hlr.smsc.update.error"), id, e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.HLR_SMSC_UPDATE_ERROR,
					new Object[] { id, e.getMessage() }));
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
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

		try {
			String systemId = user.getSystemId();

			List<HlrSmscEntry> existingEntries = hlrSmscRepository.findBySystemId(systemId);
			if(!existingEntries.isEmpty()) {
				return existingEntries;
			} else {
				throw new NotFoundException("No HLR SMSC Found!");
			}
			
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getMessage("data.access.error.message"), username, e.getMessage());

			return Collections.emptyList();
		} catch (Exception e) {
			logger.error(messageResourceBundle.getMessage("listing.error.message"), username, e.getMessage());

			return Collections.emptyList();
			logger.error(messageResourceBundle.getLogMessage("hlr.smsc.update.data.access.error"), username, e.getMessage());
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.HLR_SMSC_DATA_ACCESS_ERROR,
					new Object[] { username, e.getMessage() }));
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("hlr.smsc.update.error"), username, e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.HLR_SMSC_UPDATE_ERROR,
					new Object[] { username, e.getMessage() }));
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
			logger.info(messageResourceBundle.getMessage("conversion.success.message"));

			return entry;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getMessage("conversion.error.message"), e.getMessage());

			throw new RuntimeException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_CONVERTING_HLR_SMSC_ENTRY_REQUEST_MESSAGE,new Object[] {e}));
			logger.info(messageResourceBundle.getLogMessage("hlr.smsc.convert.success"));
			return entry;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("hlr.smsc.convert.error"), e.getMessage());
			throw new InternalServerException(
					messageResourceBundle.getExMessage(ConstantMessages.HLR_SMSC_CONVERT_ERROR));
		}
	}

}
