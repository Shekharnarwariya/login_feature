package com.hti.smpp.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.dto.GroupMemberEntry;
import com.hti.smpp.common.contacts.repository.GroupEntryRepository;
import com.hti.smpp.common.contacts.repository.GroupMemberEntryRepository;
import com.hti.smpp.common.exception.DataAccessError;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.CustomRequest;
import com.hti.smpp.common.request.GroupMemberRequest;
import com.hti.smpp.common.request.GroupRequest;
import com.hti.smpp.common.request.LimitRequest;
import com.hti.smpp.common.request.SmscBsfmEntryRequest;
import com.hti.smpp.common.request.SmscEntryRequest;
import com.hti.smpp.common.request.SmscLoopingRequest;
import com.hti.smpp.common.request.TrafficScheduleRequest;
import com.hti.smpp.common.service.SmscService;
import com.hti.smpp.common.smsc.dto.CustomEntry;
import com.hti.smpp.common.smsc.dto.LimitEntry;
import com.hti.smpp.common.smsc.dto.SmscBsfmEntry;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.dto.SmscLooping;
import com.hti.smpp.common.smsc.dto.TrafficScheduleEntry;
import com.hti.smpp.common.smsc.repository.CustomEntryRepository;
import com.hti.smpp.common.smsc.repository.LimitEntryRepository;
import com.hti.smpp.common.smsc.repository.SmscBsfmEntryRepository;
import com.hti.smpp.common.smsc.repository.SmscEntryRepository;
import com.hti.smpp.common.smsc.repository.SmscLoopingRepository;
import com.hti.smpp.common.smsc.repository.StatusEntryRepository;
import com.hti.smpp.common.smsc.repository.TrafficScheduleEntryRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.MessageResourceBundle;
import com.hti.smpp.common.util.MultiUtility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Transactional
@Service
public class SmscServiceImpl implements SmscService {

	private static final Logger logger = LoggerFactory.getLogger(SmscServiceImpl.class);

	@Autowired
	private SmscEntryRepository smscEntryRepository;

	@Autowired
	private SmscBsfmEntryRepository smscBsfmEntryRepository;

	@Autowired
	private StatusEntryRepository statusEntryRepository;

	@Autowired
	private CustomEntryRepository customEntryRepository;

	@Autowired
	private LimitEntryRepository limitEntryRepository;

	@Autowired
	private GroupEntryRepository groupEntryRepository;

	@Autowired
	private GroupMemberEntryRepository groupMemberEntryRepository;

	@Autowired
	private TrafficScheduleEntryRepository trafficScheduleEntryRepository;

	@Autowired
	private SmscLoopingRepository smscLoopingRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private MessageResourceBundle messageResourceBundle;
	// Method to save an SMS entry
	@Override
	public String smscEntrySave(SmscEntryRequest smscEntryRequest, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			// Logging the username
			System.out.println("Username: " + username);

			// Converting the request
			SmscEntry convertedRequest = ConvertRequest(smscEntryRequest);
			setConvertedRequestFields(convertedRequest, userEntry);
			// Saving the SMS entry
			SmscEntry savedEntry = smscEntryRepository.save(convertedRequest);
			logger.info(messageResourceBundle.getLogMessage("smscentry.saved.successfully"), savedEntry.getId());

			MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
			return "Successfully saved this id: " + savedEntry.getId();
		} catch (NotFoundException e) {
			System.out.println("run not found exception...");
			logger.error(messageResourceBundle.getLogMessage("smscentry.save.error"), e.getMessage());

			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.FAILED_TO_SAVE_SMSC_ENTRY) + e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("smscentry.save.error"), e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.FAILED_TO_SAVE_SMSC_ENTRY) + e.getMessage());
		}
	}
	// Helper method to set fields for the converted request
	private void setConvertedRequestFields(SmscEntry convertedRequest, UserEntry userEntry) {
		convertedRequest.setSystemId(String.valueOf(userEntry.getSystemId()));
		convertedRequest.setSystemType(userEntry.getSystemType());
		convertedRequest.setMasterId(userEntry.getMasterId());
	}
	// Method to update an SMS entry
	@Override
	public String smscupdate(int smscId, SmscEntryRequest smscEntryRequest, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			if (smscEntryRepository.existsById(smscId)) {
				SmscEntry convertRequest = ConvertRequest(smscEntryRequest);
				convertRequest.setId(smscId);
				smscEntryRepository.save(convertRequest);
				logger.info(messageResourceBundle.getLogMessage("smscentry.updated.successfully"), smscId);

				MultiUtility.changeFlag(Constants.SMSC_FLAG_DIR + "" + convertRequest.getName() + ".txt", "505");
				MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
				return "SmscEntry with ID " + smscId + " has been successfully updated.";
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SMS_ENTRY_NOT_FOUND_UPDATE_FAILED, new Object[] {smscId}));



 
			}
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("update.error"), e.getMessage(), e);

			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.FAILED_TO_UPDATE_SMSC_ENTRY) +e.getMessage());




		} catch (Exception e) {
			logger.error("An unexpected error occurred during the update operation: " + e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SMS_ENTRY_UPDATE_FAILED_UNEXPECTED_ERROR));




		}
	}
	// Method to delete an SMS entry
	@Override
	public String smscdelete(int smscId, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			Optional<SmscEntry> smscEntryOptional = smscEntryRepository.findById(smscId);
			if (!smscEntryOptional.isPresent()) {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SMS_ENTRY_NOT_FOUND, new Object[] {smscId}));


			}
			smscEntryRepository.deleteById(smscId);
			logger.info(messageResourceBundle.getLogMessage("smscentry.deleted.successfully"), smscId);

			MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
			return "SmscEntry with ID " + smscId + " has been successfully deleted.";
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("update.error"), e.getMessage());

			throw new NotFoundException(e.getMessage());
		} catch (Exception ex) {
			logger.error(messageResourceBundle.getLogMessage("delete.error"), ex.getLocalizedMessage());

			throw new InternalServerException("Failed to delete SmscEntry: " + ex.getLocalizedMessage());
		}
	}
	
	// Method to retrieve a custom entry for an SMS entry

	@Override
	public CustomEntry getCustomEntry(int smscId, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			Optional<CustomEntry> optionalEntry = customEntryRepository.findById(smscId);
			if (optionalEntry.isPresent()) {
				CustomEntry entry = optionalEntry.get();
				return entry;
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.CUSTOM_ENTRY_NOT_FOUND, new Object[] {smscId}));

			}
		} catch (NotFoundException e) {
			// Log the exception for debugging purposes
			logger.error("CustomEntryNotFoundException: " + e.getMessage());
			throw new NotFoundException("CustomEntryNotFoundException: " + e.getMessage());
		} catch (DataAccessException e) {
			// Log the exception for debugging purposes
			logger.error("DataAccessError: " + e.getMessage());
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.FAILED_RETRIEVE_CUSTOM_ENTRY, new Object[] {smscId}));


		} catch (Exception e) {
			// Log the exception for debugging purposes
			logger.error(messageResourceBundle.getLogMessage("retrieve.customentry.error"), e.getMessage());

			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.FAILED_RETRIEVE_CUSTOM_ENTRY, new Object[] {smscId}));
		}
	}

	// Method to save a custom entry for an SMS entry
	@Override
	public String saveCustom(CustomRequest customRequest, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			CustomEntry convertedRequest = ConvertRequest(customRequest);
			customEntryRepository.save(convertedRequest);
			logger.info("CustomEntry saved successfully");
			MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
			return "Successfully saved the CustomEntry.";
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("save.customentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.FAILED_SAVE_CUSTOM_ENTRY));



		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("save.customentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.FAILED_SAVE_CUSTOM_ENTRY_UN));

		}
	}
	// Method to update a CustomEntry with the provided ID, based on user authorization
	@Override
	public String updateCustom(int customId, CustomRequest customRequest, String username) {
		 // Check user authorization
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		 // Check if the user is present
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			if (customEntryRepository.existsById(customId)) {
				CustomEntry convertRequest = ConvertRequest(customRequest);
				convertRequest.setSmscId(customId); // Ensure the ID is set
				customEntryRepository.save(convertRequest);
				logger.info(messageResourceBundle.getLogMessage("customentry.updated.successfully"));

				MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
				return "CustomEntry updated successfully";
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.CUSTOM_ENTRY_NOT_FOUND, new Object[]{customId}));

			}
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("update.customentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.UPDATE_CUSTOM_ENTRY_DATA_ACCESS_ERROR));

		} catch (NotFoundException e) {
			logger.error("CustomEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("update.customentry.unexpected.error"), e.getMessage(), e);

			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.UPDATE_CUSTOM_ENTRY_UNEXPECTED_ERROR));


		}
	}
	// Method to delete a CustomEntry with the provided ID, based on user authorization
	// and ensuring the user has the required roles for the operation.
	@Override
	public String deleteCustom(int customId, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			Optional<CustomEntry> optionalCustomEntry = customEntryRepository.findById(customId);
			if (optionalCustomEntry.isPresent()) {
				CustomEntry entry = optionalCustomEntry.get();
				customEntryRepository.delete(entry);
				logger.info(messageResourceBundle.getLogMessage("customentry.deleted.successfully"));
				MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
				return "CustomEntry deleted successfully";
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.CUSTOM_ENTRY_NOT_FOUND, new Object[]{customId}));

			}
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("delete.customentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.DELETE_CUSTOM_ENTRY_ERROR));

		} catch (NotFoundException e) {
			logger.error("CustomEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("delete.customentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.DELETE_CUSTOM_ENTRY_ERROR));

		}
	}
	
	// Method to save a list of LimitEntry objects based on user authorization
	// and ensuring the user has the required roles for the operation.
	@Override
	public String saveLimit(LimitRequest limitRequest, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			List<LimitEntry> convertRequest = ConvertRequest(limitRequest);
			limitEntryRepository.saveAll(convertRequest);
			MultiUtility.changeFlag(Constants.SMSC_LT_FLAG_FILE, "707");
			logger.info(messageResourceBundle.getLogMessage("limitentry.saved.successfully"));

			return "LimitEntry saved successfully.";
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("save.limitentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.SAVE_LIMIT_ENTRY_ERROR));



		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("save.limitentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SAVE_LIMIT_ENTRY_ERROR_UN));




		}
	}
	
	// Method to update a LimitEntry with the provided ID, based on user authorization
	// and ensuring the user has the required roles for the operation.

	@Override
	public String updateLimit(int limitId, LimitRequest limitRequest, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			if (limitEntryRepository.existsById(limitId)) {
				List<LimitEntry> convertRequest = ConvertRequest(limitRequest);
				for (LimitEntry entry : convertRequest) {
					if (entry.getId() == limitId) {
						limitEntryRepository.save(entry);
						MultiUtility.changeFlag(Constants.SMSC_LT_FLAG_FILE, "707");
						logger.info(messageResourceBundle.getLogMessage("limitid.updated.successfully"), limitId);

						return "Limit updated successfully";
					}
				}
				throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.UPDATE_LIMIT_ENTRY_ERROR));

			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.LIMIT_NOT_FOUND));

			}
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("update.limitentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.UPDATE_LIMIT_ENTRY_ERROR));





		} catch (NotFoundException e) {
			logger.error("LimitEntryNotFoundException: {}", e.getMessage(), e);
			throw new NotFoundException("LimitEntryNotFoundException: " + e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("update.limitentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.UPDATE_LIMIT_ENTRY_UNEXPECTED_ERROR));
		}
	}
	
	
	// Method to delete a LimitEntry with the provided ID, based on user authorization
	// and ensuring the user has the required roles for the operation.
	@Override
	public String deleteLimit(int limitId, String username) {
		 // Check user authorization
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			if (limitEntryRepository.existsById(limitId)) {
				limitEntryRepository.deleteById(limitId);
				logger.info(messageResourceBundle.getLogMessage("limitentry.deleted.successfully"), limitId);

				MultiUtility.changeFlag(Constants.SMSC_LT_FLAG_FILE, "707");
				return "LimitEntry with ID " + limitId + " deleted successfully";
			} else {
				throw new NotFoundException("LimitEntry with ID " + limitId + " not found");
			}
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("delete.limitentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.DELETE_LIMIT_ENTRY_DATA_ACCESS_ERROR));



		} catch (NotFoundException e) {
			logger.error("LimitEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("delete.limitentry.unexpected.error"), e.getMessage(), e);

			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.DELETE_LIMIT_ENTRY_UNEXPECTED_ERROR));
		}
	}
	
	// Method to list all LimitEntries based on user authorization
	// and ensuring the user has the required roles for the operation.

	@Override
	public List<LimitEntry> listLimit(String username) {
	    // Check user authorization

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			List<LimitEntry> list = limitEntryRepository.findAll();
			return list;
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("fetch.limitentries.data.access.error"), e.getMessage(), e);

			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.LIST_SMSC_LIMIT_ENTRIES_DATA_ACCESS_ERROR));

		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("fetch.limitentries.unexpected.error"), e.getMessage(), e);

			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.LIST_SMSC_LIMIT_ENTRIES_UNEXPECTED_ERROR));
		}
	}


	// Method to save a GroupEntry based on user authorization
	// and ensuring the user has the required roles for the operation.
	@Override
	public String saveGroup(GroupRequest groupRequest, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			List<GroupEntry> convertedRequest = ConvertRequest(groupRequest);
			groupEntryRepository.saveAll(convertedRequest);
			MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
			logger.info("GroupEntry saved successfully");
			return "GroupEntry saved successfully";
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("save.groupentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.SAVE_GROUP_ENTRY_DATA_ACCESS_ERROR));

		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("save.groupentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SAVE_GROUP_ENTRY_UNEXPECTED_ERROR));

		}
	}
	// Method to update a GroupEntry based on user authorization
	// and ensuring the user has the required roles for the operation.

	@Override
	public String updateGroup(GroupRequest groupRequest, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			List<GroupEntry> convertRequest = ConvertRequest(groupRequest);
			for (GroupEntry group : convertRequest) {
				if (groupEntryRepository.existsById(group.getId())) {
					groupEntryRepository.save(group);
					MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
					logger.info(messageResourceBundle.getLogMessage("groupentry.updated.successfully"), group.getId());
				} else {
					logger.info(messageResourceBundle.getLogMessage("group.not.found"), group.getId());

					throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.GROUP_NOT_FOUND, new Object[]{group.getId()}));




				}
			}
			return "Group updated successfully.";
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("update.groupentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.FAILED_UPDATE_GROUP_ENTRY));
		} catch (NotFoundException e) {
			logger.error("GroupEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("update.groupentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.FAILED_UPDATE_GROUP_ENTRY_UN));



		}
	}
	
	// Method to delete a GroupEntry with the provided ID, based on user authorization
	// and ensuring the user has the required roles for the operation.

	@Override
	public String deleteGroup(int groupId, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			Optional<GroupEntry> groupEntryOptional = groupEntryRepository.findById(groupId);
			if (!groupEntryOptional.isPresent()) {
				throw new NotFoundException(messageResourceBundle.getMessage(ConstantMessages.GROUP_NOT_FOUND, new Object[]{groupId}));




			}
			groupEntryRepository.deleteById(groupId);
			logger.info(messageResourceBundle.getLogMessage("group.deleted.successfully"), groupId);
			MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
			return "Group with ID " + groupId + " has been deleted successfully.";
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("delete.groupentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getMessage(ConstantMessages.GROUP_DELETE_ERROR));
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("group.not.found.e"), e.getMessage(), e);

			throw new NotFoundException("Group not found: " + e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("delete.groupentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getMessage(ConstantMessages.GROUP_DELETE_UNEXPECTED_ERROR));




		}
	}

	// Method to list all GroupEntries based on user authorization
	// and ensuring the user has the required roles for the operation.
	@Override
	public List<GroupEntry> listGroup(String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			List<GroupEntry> list = groupEntryRepository.findAll();
			return list;
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("delete.groupentry.unexpected.error"), e.getMessage(), e);
					
			throw new DataAccessError(messageResourceBundle.getMessage(ConstantMessages.GROUP_LIST_DATA_ACCESS_ERROR));
        } catch (Exception e) {
        	logger.error(messageResourceBundle.getLogMessage("fetch.groupentries.unexpected.error"), e.getMessage(), e);

			throw new InternalServerException(messageResourceBundle.getMessage(ConstantMessages.GROUP_LIST_UNEXPECTED_ERROR));

		}
	}

	// Method to save a list of GroupMemberEntry objects based on user authorization
	// and ensuring the user has the required roles for the operation.
	@Override
	public String saveGroupMember(GroupMemberRequest groupMemberRequest, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			// Convert request and save all GroupMemberEntry objects
			List<GroupMemberEntry> convertRequest = ConvertRequest(groupMemberRequest);
			for (GroupMemberEntry entry : convertRequest) {
				groupMemberEntryRepository.save(entry);

			}
			logger.info(messageResourceBundle.getLogMessage("groupmemberentry.saved.successfully"));
			MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
			MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
			return "Group members saved successfully.";
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("save.groupmemberentry.data.access.error"), e.getMessage(), e);

			throw new DataAccessError(messageResourceBundle.getMessage(ConstantMessages.GROUP_MEMBER_SAVE_DATA_ACCESS_ERROR));
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("save.groupmemberentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getMessage(ConstantMessages.GROUP_MEMBER_SAVE_UNEXPECTED_ERROR));




		}
	}
	
	// Method to update a list of GroupMemberEntry objects based on user authorization
	// and ensuring the user has the required roles for the operation.

	@Override
	public String updateGroupMember(GroupMemberRequest groupMemberRequest, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			List<GroupMemberEntry> convertRequest = ConvertRequest(groupMemberRequest);

			for (GroupMemberEntry entry : convertRequest) {
				if (groupMemberEntryRepository.existsById(entry.getId())) {
					groupMemberEntryRepository.save(entry);
					MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
					logger.info("Group member updated with ID: " + entry.getId());
				} else {
					throw new NotFoundException(messageResourceBundle.getMessage(ConstantMessages.GROUP_MEMBER_NOT_FOUND, new Object[]{entry.getId()}));




				}
			}
			return "Group members updated successfully.";
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("update.groupmemberentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getMessage(ConstantMessages.UPDATE_GROUP_MEMBER_ENTRY_ERROR));



		} catch (NotFoundException e) {
			logger.error("GroupMemberNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("update.groupmemberentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getMessage(ConstantMessages.UPDATE_GROUP_MEMBER_ENTRY_UNEXPECTED_ERROR));



		}
	}

	 //Deletes a group member with the specified ID and performs authorization checks.
	
	@Override
	public String deleteGroupMember(int groupMemberId, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			if (groupMemberEntryRepository.existsById(groupMemberId)) {
				groupMemberEntryRepository.deleteById(groupMemberId);
				logger.info(messageResourceBundle.getLogMessage("groupmember.deleted.successfully"), groupMemberId);
				MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
				return "Group member with ID " + groupMemberId + " has been deleted successfully.";
			} else {
				String errorMessage = "Group member with ID " + groupMemberId + " not found.";
				logger.error(errorMessage);
				throw new NotFoundException(errorMessage);
			}
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("groupmember.not.found.exception"), e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			String errorMessage = "An unexpected error occurred while deleting the GroupMemberEntry: " + e.getMessage();
			logger.error(errorMessage, e);
			throw new InternalServerException(errorMessage);
		}
	}

	// Saves traffic schedule entries based on the provided TrafficScheduleRequest.
	@Override
	public String saveSchedule(TrafficScheduleRequest trafficScheduleRequest) {
		try {
			List<TrafficScheduleEntry> convertedEntries = ConvertRequest(trafficScheduleRequest);
			for (TrafficScheduleEntry entry : convertedEntries) {
				trafficScheduleEntryRepository.save(entry);
				logger.info(messageResourceBundle.getLogMessage("trafficschedule.saved.successfully"), entry.getId());
				MultiUtility.changeFlag(Constants.SMSC_SH_FLAG_FILE, "707");
			}
			return "Traffic schedule saved successfully.";
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("save.trafficscheduleentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getMessage(ConstantMessages.SAVE_TRAFFIC_SCHEDULE_ENTRY_DATA_ACCESS_ERROR));


		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("save.trafficscheduleentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getMessage(ConstantMessages.SAVE_TRAFFIC_SCHEDULE_ENTRY_UNEXPECTED_ERROR));
		}
	}
   // Updates traffic schedule entries based on the provided TrafficScheduleRequest and performs authorization checks.

	@Override
	public String updateSchedule(TrafficScheduleRequest trafficScheduleRequest, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			List<TrafficScheduleEntry> convertRequest = ConvertRequest(trafficScheduleRequest);
			for (TrafficScheduleEntry entry : convertRequest) {
				if (trafficScheduleEntryRepository.existsById(entry.getId())) {
					trafficScheduleEntryRepository.save(entry);
					logger.info(messageResourceBundle.getLogMessage("trafficschedule.saved.successfully"), entry.getId());

					MultiUtility.changeFlag(Constants.SMSC_SH_FLAG_FILE, "707");
				} else {
					throw new NotFoundException(messageResourceBundle.getMessage(ConstantMessages.TRAFFIC_SCHEDULE_NOT_FOUND, new Object[]{entry.getId()}));

				}
			}
			return "Traffic schedule updated successfully.";
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("update.trafficscheduleentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getMessage(ConstantMessages.TRAFFIC_SCHEDULE_UPDATE_ERROR));
		} catch (NotFoundException e) {
			logger.error("TrafficScheduleNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("update.trafficscheduleentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getMessage(ConstantMessages.TRAFFIC_SCHEDULE_UPDATE_ERROR));


		}
	}

	//Deletes a traffic schedule entry with the specified ID and performs authorization checks
	@Override
	public String deleteSchedule(int scheduleId, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			if (trafficScheduleEntryRepository.existsById(scheduleId)) {
				trafficScheduleEntryRepository.deleteById(scheduleId);
				MultiUtility.changeFlag(Constants.SMSC_SH_FLAG_FILE, "707");
				return "Traffic schedule with ID " + scheduleId + " deleted successfully.";
			} else {
				logger.error(messageResourceBundle.getLogMessage("trafficschedule.not.exist"), scheduleId);
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.TRAFFIC_SCHEDULE_NOT_FOUND, new Object[]{scheduleId}));
			}
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("delete.trafficscheduleentry.data.access.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.TRAFFIC_SCHEDULE_DELETE_ERROR));




		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("delete.trafficscheduleentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.TRAFFIC_SCHEDULE_DELETE_UNEXPECTED_ERROR));




		}
	}
	
	
	// Lists all traffic schedule entries, sets day names, SMS gateway names, and returns a map.
	 

	public Map<String, TrafficScheduleEntry> listSchedule() {
		try {
			List<TrafficScheduleEntry> list = trafficScheduleEntryRepository.findAll();
			Map<String, TrafficScheduleEntry> map = new HashMap<>();
			for (TrafficScheduleEntry entry : list) {
				setDayName(entry);
				setSmscNameAndAddToMap(entry, map);
			}
			return map;
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("fetch.trafficscheduleentries.data.access.error"), e.getMessage(), e);
					
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.TRAFFIC_SCHEDULE_LIST_DATA_ACCESS_ERROR));



		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("fetch.trafficscheduleentries.unexpected.error"), e.getMessage(), e);
					
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.TRAFFIC_SCHEDULE_LIST_UNEXPECTED_ERROR));




		}
	}

	//Sets the day name for a TrafficScheduleEntry based on the day of the week.
	
	private void setDayName(TrafficScheduleEntry entry) {
		Map<Integer, String> daysMap = Map.of(0, "EveryDay", 1, "Sunday", 2, "Monday", 3, "Tuesday", 4, "Wednesday", 5,
				"Thursday", 6, "Friday", 7, "Saturday");
		entry.setDayName(daysMap.getOrDefault(entry.getDay(), "Unknown"));
	}

	//  Sets the SMS gateway name for a TrafficScheduleEntry and adds it to the provided map.

	private void setSmscNameAndAddToMap(TrafficScheduleEntry entry, Map<String, TrafficScheduleEntry> map) {
		Optional<SmscEntry> smscEntry = smscEntryRepository.findById(entry.getSmscId());
		if (smscEntry.isPresent()) {
			entry.setSmscName(smscEntry.get().getName());
			map.put(entry.getSmscId() + "#" + entry.getDay(), entry);
		}
	}
 //Saves an SmscLooping entry based on the provided SmscLoopingRequest.
	@Override
	public String saveLoopingRule(SmscLoopingRequest smscLoopingRequest) {
		try {
			SmscLooping convertRequest = ConvertRequest(smscLoopingRequest);
			smscLoopingRepository.save(convertRequest);
			MultiUtility.changeFlag(Constants.SMSC_LOOP_FLAG_FILE, "707");
			return "SmscLooping entry saved successfully";
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("save.smscloopingentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError("Failed to save SmscLooping entry. Data access error occurred.");
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("save.smscloopingentry.unexpected.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.SMS_LOOPING_SAVE_DATA_ACCESS_ERROR));


		}
	}
	
	//  Updates an SmscLooping entry based on the provided SmscLoopingRequest and performs authorization checks.


	@Override
	public String loopingRuleupdate(SmscLoopingRequest smscLoopingRequest, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			// Check if the user is authorized as a super admin and system user
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			  // Convert the request to a SmscLooping object
			SmscLooping convertRequest = ConvertRequest(smscLoopingRequest);
			// Check if the SmscLooping entry exists
			if (smscLoopingRepository.existsById(convertRequest.getSmscId())) {
				smscLoopingRepository.save(convertRequest);
				logger.info(messageResourceBundle.getLogMessage("smscloopingentry.updated.successfully"), convertRequest.getSmscId());
				MultiUtility.changeFlag(Constants.SMSC_LOOP_FLAG_FILE, "707");
				return "SmscLooping entry updated successfully";
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SMS_LOOPING_ENTRY_NOT_FOUND));




			}
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("update.smscloopingentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.FAILED_UPDATE_SMS_LOOPING_ENTRY));



		} catch (NotFoundException e) {
			logger.error("NotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("update.smscloopingentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.FAILED_UPDATE_SMS_LOOPING_ENTRY));

		}
	}
	
	// * Deletes an SmscLooping entry with the specified ID and performs authorization checks.


	@Override
	public String loopingRuledelete(int smscId, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		
		// Check if the user exists
		if (userOptional.isPresent()) {
			user = userOptional.get();
			
			  // Check if the user is authorized as a super admin and system user
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			if (smscLoopingRepository.existsById(smscId)) {
	            // Delete the SmscLooping entry, trigger a flag change, and return success message

				smscLoopingRepository.deleteById(smscId);
				logger.info(messageResourceBundle.getLogMessage("smscloopingentry.deleted.successfully"), smscId);
				MultiUtility.changeFlag(Constants.SMSC_LOOP_FLAG_FILE, "707");
				return "SmscLooping entry deleted successfully";
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SMS_LOOPING_ENTRY_NOT_FOUND));



			}
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("delete.smscloopingentry.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.FAILED_TO_DELETE_SMS_LOOPING_ENTRY));

		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("smscloopingentry.not.found"), e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("delete.smscloopingentry.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.FAILED_TO_DELETE_SMS_LOOPING_ENTRY));




		}
	}

	// * Retrieves an SmscLooping entry with the specified ID and performs authorization checks.

	@Override
	public SmscLooping getLoopingRule(int smscId, String username) {
		
		 // Retrieve user information based on the provided username
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			Optional<SmscLooping> loopingRule = smscLoopingRepository.findBySmscId((long) smscId);
			if (loopingRule.isPresent()) {
				return loopingRule.get();
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SMS_LOOPING_RULE_NOT_FOUND, new Object[]{smscId}));
			}
		} catch (DataAccessException e) {
			logger.error(messageResourceBundle.getLogMessage("retrieve.smscloopingrule.data.access.error"), e.getMessage(), e);
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.FAILED_TO_RETRIEVE_SMS_LOOPING_RULE));



		} catch (NotFoundException e) {
			logger.error("NotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("retrieve.smscloopingrule.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.FAILED_TO_RETRIEVE_SMS_LOOPING_RULE));


		}
	}
	
	//Lists all SmscLooping entries and performs authorization checks.

	@Override
	public List<SmscLooping> listLoopingRule(String username) {
		// Retrieve user information based on the provided username
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		
		// Check if the user exists
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			return smscLoopingRepository.findAll();
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("list.smscloopingrules.unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.FAILED_TO_LIST_SMS_LOOPING_RULES));




		}
	}

	public SmscEntry ConvertRequest(SmscEntryRequest smscEntryRequest) {
		try {
			SmscEntry smsc = new SmscEntry();
			
			 // Set properties based on SmscEntryRequest
			smsc.setAlertUrl(smscEntryRequest.getAlertUrl());
			smsc.setAllowedSources(smscEntryRequest.getAllowedSources());
			smsc.setBackupIp(smscEntryRequest.getBackupIp());
			smsc.setBackupIp1(smscEntryRequest.getBackupIp1());
			smsc.setBackupPort(smscEntryRequest.getBackupPort());
			smsc.setBackupPort1(smscEntryRequest.getBackupPort1());
			smsc.setBindMode(smscEntryRequest.getBindMode());
			smsc.setCategory(smscEntryRequest.getCategory());
			smsc.setCreatePartDlr(smscEntryRequest.isCreatePartDlr());
			smsc.setCustomDlrTime(smscEntryRequest.getCustomDlrTime());
			smsc.setDefaultSource(smscEntryRequest.getDefaultSource());
			smsc.setDelayedDlr(smscEntryRequest.getDelayedDlr());
			smsc.setDeliveryWaitTime(smscEntryRequest.getDeliveryWaitTime());
			smsc.setDestPrefix(smscEntryRequest.getDestPrefix());
			smsc.setDestRestrict(smscEntryRequest.isDestRestrict());
			smsc.setDndSource(smscEntryRequest.getDndSource());
			smsc.setDnpi(smscEntryRequest.getDnpi());
			smsc.setDownAlert(smscEntryRequest.isDownAlert());
			smsc.setDownEmail(smscEntryRequest.getDownEmail());
			smsc.setDownNumber(smscEntryRequest.getDownNumber());
			smsc.setDton(smscEntryRequest.getDton());
			smsc.setEnforceDefaultEsm(smscEntryRequest.isEnforceDefaultEsm());
			smsc.setEnforceDlr(smscEntryRequest.isEnforceDlr());
			smsc.setEnforceSmsc(smscEntryRequest.getEnforceSmsc());
			smsc.setEnforceTonNpi(smscEntryRequest.isEnforceTonNpi());
			smsc.setExpireLongBroken(smscEntryRequest.isExpireLongBroken());
			smsc.setGreekEncode(smscEntryRequest.isGreekEncode());
			smsc.setHexResponse(smscEntryRequest.isHexResponse());
			smsc.setIp(smscEntryRequest.getIp());
			smsc.setLoopCount(smscEntryRequest.getLoopCount());
			smsc.setLoopDuration(smscEntryRequest.getLoopDuration());
			smsc.setLooping(smscEntryRequest.isLooping());
			smsc.setMaxLatency(smscEntryRequest.getMaxLatency());
			smsc.setMinDestTime(smscEntryRequest.getMinDestTime());
			smsc.setMultipart(smscEntryRequest.isMultipart());
			smsc.setName(smscEntryRequest.getName());
			smsc.setPassword(smscEntryRequest.getPassword());
			smsc.setPort(smscEntryRequest.getPort());
			smsc.setPriorSender(smscEntryRequest.getPriorSender());
			smsc.setRemark(smscEntryRequest.getRemark());
			smsc.setReplaceContent(smscEntryRequest.isReplaceContent());
			smsc.setReplaceContentText(smscEntryRequest.getReplaceContentText());
			smsc.setReplaceSource(smscEntryRequest.isReplaceSource());
			smsc.setResend(smscEntryRequest.isResend());
			smsc.setRlzRespId(smscEntryRequest.isRlzRespId());
			smsc.setSkipDlt(smscEntryRequest.isSkipDlt());
			smsc.setSkipHlrSender(smscEntryRequest.getSkipHlrSender());
			smsc.setSleep(smscEntryRequest.getSleep());
			smsc.setSnpi(smscEntryRequest.getSnpi());
			smsc.setSourceAsDest(smscEntryRequest.isSourceAsDest());
			smsc.setSton(smscEntryRequest.getSton());

			logger.info(messageResourceBundle.getLogMessage("convert.smscentryrequest.successfully"));
			return smsc;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("convert.smscentryrequest.error"), e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_CONVERTING_REQUEST_TO_ENTRY)+ e.getMessage());


		}
	}

	public CustomEntry ConvertRequest(CustomRequest customRequest) {

		try {
			CustomEntry custom = new CustomEntry();

			 // Set properties based on CustomRequest
			
			custom.setGsnpi(customRequest.getGsnpi());
			custom.setGston(customRequest.getGston());
			custom.setLsnpi(customRequest.getLsnpi());
			custom.setLston(customRequest.getSmscId());
			custom.setSmscId(customRequest.getSmscId());
			custom.setSourceLength(customRequest.getSourceLength());
			logger.info(messageResourceBundle.getLogMessage("convert.customrequest.successfully"));
			return custom;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("convert.customrequest.error"), e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_CONVERTING_REQUEST_TO_CUSTOM_ENTRY )+ e.getMessage());




		}
	}

	public List<GroupEntry> ConvertRequest(GroupRequest groupRequest) {
		GroupEntry entry = null;
		List<GroupEntry> list = new ArrayList<>();
		try {
			if (groupRequest.getId() != null && groupRequest.getId().length > 0) {
				int[] id = groupRequest.getId();
				String[] name = groupRequest.getName();
				String[] remarks = groupRequest.getRemarks();
				int[] duration = groupRequest.getDuration();
				int[] primeDuration = groupRequest.getCheckDuration();
				int[] primeVolume = groupRequest.getCheckVolume();
				int[] noOfRepeat = groupRequest.getNoOfRepeat();
				int[] keepRepeatDays = groupRequest.getKeepRepeatDays();
				int[] primaryMember = groupRequest.getPrimeMember();
				for (int i = 0; i < id.length; i++) {
					entry = new GroupEntry();
					entry.setId(id[i]);
					entry.setName(name[i]);
					entry.setRemarks(remarks[i]);
					entry.setDuration(duration[i]);
					entry.setCheckDuration(primeDuration[i]);
					entry.setCheckVolume(primeVolume[i]);
					entry.setNoOfRepeat(noOfRepeat[i]);
					entry.setKeepRepeatDays(keepRepeatDays[i]);
					entry.setPrimeMember(primaryMember[i]);
					list.add(entry);
				}
			}

			logger.info(messageResourceBundle.getLogMessage("convert.grouprequest.successfully"));
			return list;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("convert.grouprequest.error"), e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_CONVERTING_REQUEST_TO_GROUP_ENTRY) + e.getMessage());




		}
	}

	public List<GroupMemberEntry> ConvertRequest(GroupMemberRequest groupMemberRequest) {
		try {
			// Set properties based on GroupMemberRequest
			int[] smsc = groupMemberRequest.getSmscId();
			int[] percent = groupMemberRequest.getPercent();
			List<GroupMemberEntry> list = new ArrayList<>();
			List<GroupMemberEntry> listGroup = groupMemberEntryRepository
					.findByGroupId(groupMemberRequest.getGroupId());
			Map<Integer, GroupMemberEntry> map = listGroup.stream()
					.collect(Collectors.toMap(GroupMemberEntry::getSmscId, entry -> entry));

			for (int i = 0; i < smsc.length; i++) {
				if (map.containsKey(smsc[i])) {
					GroupMemberEntry existEntry = map.get(smsc[i]);
					 // Set properties based on GroupMemberRequest
					existEntry.setGroupId(groupMemberRequest.getGroupId());
					existEntry.setSmscId(smsc[i]);
					existEntry.setPercent(percent[i]);
					list.add(existEntry);
				} else {
					GroupMemberEntry entry = new GroupMemberEntry();
					 // Set properties based on GroupMemberRequest
					
					entry.setGroupId(groupMemberRequest.getGroupId());
					entry.setSmscId(smsc[i]);
					entry.setPercent(percent[i]);
					list.add(entry);
					logger.info(entry.toString());
				}
			}
			logger.info(messageResourceBundle.getLogMessage("convert.groupmemberrequest.successfully"));
			return list;
		} catch (Exception e) {
			logger.error("Error occurred while converting GroupMemberRequest to GroupMemberEntry: {}", e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_CONVERTING_REQUEST_TO_GROUP_MEMBER_ENTRY) + e.getMessage());


		}
	}

	public List<LimitEntry> ConvertRequest(LimitRequest limitRequest) {

		LimitEntry limit = null;
		try {
			limit = new LimitEntry();
			List<LimitEntry> list = new java.util.ArrayList<LimitEntry>();
			int networkId[] = limitRequest.getNetworkId();
			for (int i = 0; i < networkId.length; i++) {
				limit.setSmscId(limitRequest.getSmscId());
				limit.setNetworkId(networkId[i]);
				limit.setLimit(limitRequest.getLimit());
				limit.setRerouteId(limitRequest.getRerouteId());
				limit.setResetTime(limitRequest.getResetTime());
				limit.setAlertEmail(limitRequest.getAlertEmail());
				limit.setAlertNumber(limitRequest.getAlertNumber());
				limit.setAlertSender(limitRequest.getAlertSender());
				list.add(limit);
			}

			logger.info(messageResourceBundle.getLogMessage("convert.limitrequest.successfully"));
			return list;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("convert.groupmemberrequest.error"), e.getMessage());

			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_CONVERTING_REQUEST_TO_LIMIT_ENTRY) + e.getMessage());

		}
	}

	public SmscLooping ConvertRequest(SmscLoopingRequest smscLoopingRequest) {

		SmscLooping smscLooping = null;
		try {
			smscLooping = new SmscLooping();
			// Set properties based on SmscLoopingRequest
			smscLooping.setActive(smscLoopingRequest.isActive());
			smscLooping.setCount(smscLoopingRequest.getCount());
			smscLooping.setDuration(smscLoopingRequest.getDuration());
			// smscLooping.setRerouteSmsc(smscLoopingRequest.);
			smscLooping.setRerouteSmscId(smscLoopingRequest.getRerouteSmscId());
			smscLooping.setSenderId(smscLoopingRequest.getSenderId());
			// smscLooping.setSmsc(smscLoopingRequest.);
			smscLooping.setSmscId(smscLoopingRequest.getSmscId());
			logger.info(messageResourceBundle.getLogMessage("convert.smscloopingrequest.successfully"));
			return smscLooping;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("convert.smscloopingrequest.error"), e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_CONVERTING_REQUEST_TO_SMSC_LOOPING) + e.getMessage());




		}
	}

	public List<TrafficScheduleEntry> ConvertRequest(TrafficScheduleRequest trafficScheduleRequest) {

		try {
			 // Set properties based on TrafficScheduleRequest
			int[] smscId = trafficScheduleRequest.getSmscId();
			String[] gmt = trafficScheduleRequest.getGmt();
			int[] day = trafficScheduleRequest.getDay();
			String[] duration = trafficScheduleRequest.getDuration();
			String[] downTime = trafficScheduleRequest.getDownTime();

			Map<String, TrafficScheduleEntry> map = listSchedule();

			List<TrafficScheduleEntry> list = new ArrayList<TrafficScheduleEntry>();
			for (int i = 0; i < smscId.length; i++) {
				if (map != null && !map.isEmpty()) {
					if (map.containsKey(smscId[i] + "#" + day[i])) {
						logger.info(smscId[i] + "#" + day[i] + " Entry Already Exist. Skipping");
						continue;
					}
				}
				list.add(new TrafficScheduleEntry(smscId[i], gmt[i], day[i], duration[i], downTime[i]));

			}
			logger.info(messageResourceBundle.getLogMessage("convert.trafficschedulerequest.successfully"));

			return list;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("convert.trafficschedulerequest.error"), e.getMessage());
					
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_CONVERTING_REQUEST_TO_TRAFFIC_SCHEDULE) + e.getMessage());


	}
	}
	
	/// Saves a SmscBsfmEntry based on the provided SmscBsfmEntryRequest and username
	// Only users with the required roles are authorized to perform this operation

	@Override
	public String saveSmscBsfm(SmscBsfmEntryRequest smscBsfmEntryRequest, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}

		try {
			SmscBsfmEntry smscBsfmEntry = new SmscBsfmEntry();
			ConvertRequest(smscBsfmEntryRequest, smscBsfmEntry);
			smscBsfmEntryRepository.save(smscBsfmEntry);
			logger.info(messageResourceBundle.getLogMessage("smscbsfmentry.saved.successfully"));
			MultiUtility.changeFlag(Constants.SMSC_BSFM_FLAG_FILE, "707");
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			throw new InternalServerException("Unable to save SmscBsfmEntry: " + e.getLocalizedMessage());
		}
		return "saccessfully save....";

	}
	
	// Converts a SmscBsfmEntryRequest to SmscBsfmEntry and sets its properties

	private void ConvertRequest(SmscBsfmEntryRequest smscBsfmEntryRequest, SmscBsfmEntry smscBsfmEntry) {
		smscBsfmEntry.setContent(smscBsfmEntryRequest.getContent());
		smscBsfmEntry.setSmscId(smscBsfmEntryRequest.getSmscId());
		smscBsfmEntry.setSmscName(smscBsfmEntryRequest.getSmscName());
		smscBsfmEntry.setSource(smscBsfmEntryRequest.getSmscId() + "");

	}
	
	// Retrieves a list of TrafficScheduleEntry for the provided username
	// Only users with the required roles are authorized to perform this operation

	@Override
	public List<TrafficScheduleEntry> listTrafficSchedule(String username) {
		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			UserEntry user = null;
			if (userOptional.isPresent()) {
				user = userOptional.get();
				if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
					throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
				}
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
			}

			List<TrafficScheduleEntry> trafficScheduleEntries = trafficScheduleEntryRepository.findAll();

			// Log the successful retrieval of traffic schedule entries
			logger.info(messageResourceBundle.getLogMessage("retrieve.trafficscheduleentries.successfully"), username);


			// Process the list or return it directly based on your requirements
			return trafficScheduleEntries;
		} catch (NotFoundException | UnauthorizedException ex) {
			// Log the exception with appropriate level (info, warn, error, etc.)
			logger.error(messageResourceBundle.getLogMessage("listtrafficschedule.error"), username, ex.getMessage(), ex);

			// Re-throw the exception for higher-level handling if needed
			throw ex;
		} catch (Exception ex) {
			// Log unexpected exceptions with error level
			logger.error(messageResourceBundle.getLogMessage("listtrafficschedule.unexpectederror"), username, ex.getMessage(), ex);

			// Wrap and throw a generic exception for higher-level handling if needed
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.UNEXPECTED_ERROR_PROCESSING_REQUEST) + ex.getLocalizedMessage());




		}
	}

	// Retrieves a list of SMS/C BSFM entries for the provided username
	// Only users with the required roles are authorized to perform this operation
	@Override
	public List<SmscBsfmEntry> listSmscBsfm(String username) {
		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			UserEntry user = null;
			if (userOptional.isPresent()) {
				user = userOptional.get();
				if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
					throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
				}
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
			}

			List<SmscBsfmEntry> smscBsfmEntries = smscBsfmEntryRepository.findAll();

			// Log the successful retrieval of SMS/C BSFM entries
			logger.info(messageResourceBundle.getLogMessage("retrieve.smscbsfmentries.successfully"), username);

			// Process the list or return it directly based on your requirements
			return smscBsfmEntries;
		} catch (NotFoundException ex) {
			// Log the exception with appropriate level (info, warn, error, etc.)
			logger.error(messageResourceBundle.getLogMessage("listsmscbsfm.error"), username, ex.getMessage(), ex);


			// Re-throw the exception for higher-level handling if needed
			throw new NotFoundException(ex.getMessage());
		} catch (UnauthorizedException ex) {
			// Log the exception with appropriate level (info, warn, error, etc.)
			logger.error(messageResourceBundle.getLogMessage("listsmscbsfm.error"), username, ex.getMessage(), ex);

			// Re-throw the exception for higher-level handling if needed
			throw new UnauthorizedException(ex.getMessage());
		} catch (Exception ex) {
			// Log unexpected exceptions with error level
			logger.error(messageResourceBundle.getLogMessage("listsmscbsfm.unexpectederror"), username, ex.getMessage(), ex);

			// Wrap and throw a generic exception for higher-level handling if needed
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.UNEXPECTED_ERROR, new Object[]{ex.getLocalizedMessage()}));




		}

	}

	// Updates a SMS/C BSFM entry based on the provided SmscBsfmEntryRequest and username
	// Only users with the required roles are authorized to perform this operation
	@Override
	public ResponseEntity<String> bsfmupdate(SmscBsfmEntryRequest smscBsfmEntryRequest, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			if (smscBsfmEntryRepository.existsById(smscBsfmEntryRequest.getId())) {
				SmscBsfmEntry smscBsfmEntry = new SmscBsfmEntry();
				smscBsfmEntry.setId(smscBsfmEntryRequest.getId());
				smscBsfmEntry.setContent(smscBsfmEntryRequest.getContent());
				smscBsfmEntry.setSmscId(smscBsfmEntryRequest.getSmscId());
				smscBsfmEntry.setSmscName(smscBsfmEntryRequest.getSmscName());
				smscBsfmEntry.setSource(smscBsfmEntryRequest.getSource());
				smscBsfmEntryRepository.save(smscBsfmEntry);
				logger.info("Entry updated with id: " + smscBsfmEntryRequest.getId());
				MultiUtility.changeFlag(Constants.SMSC_BSFM_FLAG_FILE, "707");
				return new ResponseEntity<>("Entry updated successfully", HttpStatus.OK);
			} else {
				throw new NotFoundException("Entry not found with ID: " + smscBsfmEntryRequest.getId());
			}
		} catch (NotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Deletes a SMS/C BSFM entry based on the provided ID and username
	// Only users with the required roles are authorized to perform this operation
	@Override
	public ResponseEntity<String> bsfmdelete(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			if (smscBsfmEntryRepository.existsById(id)) {
				smscBsfmEntryRepository.deleteById(id);
				logger.info(messageResourceBundle.getLogMessage("entry.deleted.successfully"), id);
				MultiUtility.changeFlag(Constants.SMSC_BSFM_FLAG_FILE, "707");
				return new ResponseEntity<>("Entry deleted successfully", HttpStatus.OK);
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ENTRY_NOT_FOUND) +id);




			}
		} catch (NotFoundException e) {
			logger.error("Entry not found: " + e.getLocalizedMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			logger.error("Unexpected Exception: " + e.getLocalizedMessage());
			return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Retrieves a SMS entry based on the provided ID and username
	// Only users with the required roles are authorized to perform this operation
	@Override
	public ResponseEntity<SmscEntry> getSmscEntry(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			Optional<SmscEntry> optionalSmscEntry = smscEntryRepository.findById(id);

			if (optionalSmscEntry.isPresent()) {
				SmscEntry smscEntry = optionalSmscEntry.get();
				logger.info(messageResourceBundle.getLogMessage("entry.retrieved.successfully"), id);
				return new ResponseEntity<>(smscEntry, HttpStatus.OK);
			} else {
				logger.error(messageResourceBundle.getLogMessage("sms.entry.notfound"), id);
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SMS_ENTRY_NOT_FOUND, new Object[]{id}));

			}
		} catch (NotFoundException e) {
			logger.error("NotFoundException: " + e.getLocalizedMessage());
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected Exception: " + e.getLocalizedMessage());
			throw new InternalServerException(e.getMessage());
		}
	}
	
	// Retrieves a group member based on the provided ID and username
	// Only users with the required roles are authorized to perform this operation

	@Override
	public ResponseEntity<?> getGroupMember(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			Optional<GroupMemberEntry> optionalGroupMember = groupMemberEntryRepository.findById(id);

			if (optionalGroupMember.isPresent()) {
				GroupMemberEntry groupMember = optionalGroupMember.get();
				return new ResponseEntity<>(groupMember, HttpStatus.OK);
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.GROUP_MEMBER_NOT_FOUND, new Object[]{id}));




			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException(e.getMessage());
		}
	}
}
