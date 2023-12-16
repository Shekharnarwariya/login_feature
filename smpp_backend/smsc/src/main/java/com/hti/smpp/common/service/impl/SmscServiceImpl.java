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
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
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
import com.hti.smpp.common.util.Constants;
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
	private UserRepository loginRepository;

	@Override
	public String smscEntrySave(SmscEntryRequest smscEntryRequest, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		try {
			// Logging the username
			System.out.println("Username: " + username);

			// Finding the user by system ID
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

			// Converting the request
			SmscEntry convertedRequest = ConvertRequest(smscEntryRequest);

			// Handling the optional user entry
			if (userOptional.isPresent()) {
				UserEntry userEntry = userOptional.get();
				setConvertedRequestFields(convertedRequest, userEntry);
			} else {
				throw new NotFoundException("User not found. Please enter a valid username.");
			}
			// Saving the SMS entry
			SmscEntry savedEntry = smscEntryRepository.save(convertedRequest);
			MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
			return "Successfully saved this id: " + savedEntry.getId();
		} catch (NotFoundException e) {
			System.out.println("run not found exception...");
			logger.error("An error occurred while saving the SmscEntry: {}", e.getMessage());
			throw new NotFoundException("Failed to save SmscEntry. Error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("An error occurred while saving the SmscEntry: {}", e.getMessage());
			throw new InternalServerException("Failed to save SmscEntry. Error: " + e.getMessage());
		}
	}

	private void setConvertedRequestFields(SmscEntry convertedRequest, UserEntry userEntry) {
		convertedRequest.setSystemId(String.valueOf(userEntry.getSystemId()));
		convertedRequest.setSystemType(userEntry.getSystemType());
		convertedRequest.setMasterId(userEntry.getMasterId());
	}

	@Override
	public String smscupdate(int smscId, SmscEntryRequest smscEntryRequest, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		try {
			if (smscEntryRepository.existsById(smscId)) {
				SmscEntry convertRequest = ConvertRequest(smscEntryRequest);
				convertRequest.setId(smscId);
				smscEntryRepository.save(convertRequest);
				MultiUtility.changeFlag(Constants.SMSC_FLAG_DIR + "" + convertRequest.getName() + ".txt", "505");
				MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
				return "SmscEntry with ID " + smscId + " has been successfully updated.";
			} else {
				throw new NotFoundException("SmscEntry with ID " + smscId + " not found. Update operation failed.");
			}
		} catch (NotFoundException e) {
			logger.error("An error occurred during the update operation: " + e.getMessage(), e);
			throw new NotFoundException("Failed to update SmscEntry: " + e.getMessage());
		} catch (Exception e) {
			logger.error("An unexpected error occurred during the update operation: " + e.getMessage(), e);
			throw new InternalServerException("Failed to update SmscEntry: Unexpected error occurred.");
		}
	}

	@Override
	public String smscdelete(int smscId, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		try {
			Optional<SmscEntry> smscEntryOptional = smscEntryRepository.findById(smscId);
			if (!smscEntryOptional.isPresent()) {
				throw new NotFoundException("SmscEntry with ID " + smscId + " was not found in the database.");
			}
			smscEntryRepository.deleteById(smscId);
			MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
			System.out.println("run delete ...");
			return "SmscEntry with ID " + smscId + " has been successfully deleted.";
		} catch (NotFoundException e) {
			logger.error("An error occurred during the update operation: " + e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (Exception ex) {
			logger.error("An error occurred during the delete operation: " + ex.getLocalizedMessage());
			throw new InternalServerException("Failed to delete SmscEntry: " + ex.getLocalizedMessage());
		}
	}

	@Override
	public CustomEntry getCustomEntry(int smscId, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		try {
			Optional<CustomEntry> optionalEntry = customEntryRepository.findById(smscId);
			if (optionalEntry.isPresent()) {
				CustomEntry entry = optionalEntry.get();
				return entry;
			} else {
				throw new NotFoundException("Smsc CustomEntry with ID " + smscId + " not found");
			}
		} catch (NotFoundException e) {
			// Log the exception for debugging purposes
			logger.error("CustomEntryNotFoundException: " + e.getMessage());
			throw e;
		} catch (DataAccessException e) {
			// Log the exception for debugging purposes
			logger.error("DataAccessError: " + e.getMessage());
			throw new DataAccessError("Failed to retrieve Smsc CustomEntry with ID: " + smscId);
		} catch (Exception e) {
			// Log the exception for debugging purposes
			logger.error("Unknown error occurred while retrieving CustomEntry: " + e.getMessage());
			throw new InternalServerException("Failed to retrieve Smsc CustomEntry with ID: " + smscId);
		}
	}

	@Override
	public String saveCustom(CustomRequest customRequest, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		try {
			CustomEntry convertedRequest = ConvertRequest(customRequest);
			customEntryRepository.save(convertedRequest);
			logger.info("CustomEntry saved successfully");
			MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
			return "Successfully saved the CustomEntry.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the CustomEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to save CustomEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the CustomEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to save CustomEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String updateCustom(int customId, CustomRequest customRequest, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			if (customEntryRepository.existsById(customId)) {
				CustomEntry convertRequest = ConvertRequest(customRequest);
				convertRequest.setSmscId(customId); // Ensure the ID is set
				customEntryRepository.save(convertRequest);
				MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
				return "CustomEntry updated successfully";
			} else {
				throw new NotFoundException("CustomEntry not found with id: " + customId);
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the CustomEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to update CustomEntry. Data access error occurred.");
		} catch (NotFoundException e) {
			logger.error("CustomEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the CustomEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to update CustomEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String deleteCustom(int customId, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			Optional<CustomEntry> optionalCustomEntry = customEntryRepository.findById(customId);
			if (optionalCustomEntry.isPresent()) {
				CustomEntry entry = optionalCustomEntry.get();
				customEntryRepository.delete(entry);
				MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
				return "CustomEntry deleted successfully";
			} else {
				throw new NotFoundException("CustomEntry not found with id: " + customId);
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while deleting the CustomEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to delete CustomEntry. Data access error occurred.");
		} catch (NotFoundException e) {
			logger.error("CustomEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while deleting the CustomEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to delete CustomEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String saveLimit(LimitRequest limitRequest, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		try {
			List<LimitEntry> convertRequest = ConvertRequest(limitRequest);
			limitEntryRepository.saveAll(convertRequest);
			MultiUtility.changeFlag(Constants.SMSC_LT_FLAG_FILE, "707");
			return "LimitEntry saved successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the LimitEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to save LimitEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the LimitEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to save LimitEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String updateLimit(int limitId, LimitRequest limitRequest, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			if (limitEntryRepository.existsById(limitId)) {
				List<LimitEntry> convertRequest = ConvertRequest(limitRequest);
				for (LimitEntry entry : convertRequest) {
					if (entry.getId() == limitId) {
						limitEntryRepository.save(entry);
						MultiUtility.changeFlag(Constants.SMSC_LT_FLAG_FILE, "707");
						return "Limit updated successfully";
					}
				}
				throw new InternalServerException("Failed to update LimitEntry: No matching ID found in the request");
			} else {
				throw new NotFoundException("Limit with the provided ID not found");
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the LimitEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to update LimitEntry. Data access error occurred.");
		} catch (NotFoundException e) {
			logger.error("LimitEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the LimitEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to update LimitEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String deleteLimit(int limitId, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			if (limitEntryRepository.existsById(limitId)) {
				limitEntryRepository.deleteById(limitId);
				MultiUtility.changeFlag(Constants.SMSC_LT_FLAG_FILE, "707");
				return "LimitEntry with ID " + limitId + " deleted successfully";
			} else {
				throw new NotFoundException("LimitEntry with ID " + limitId + " not found");
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while deleting the LimitEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to delete LimitEntry. Data access error occurred.");
		} catch (NotFoundException e) {
			logger.error("LimitEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while deleting the LimitEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to delete LimitEntry. Unexpected error occurred.");
		}
	}

	@Override
	public List<LimitEntry> listLimit(String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			List<LimitEntry> list = limitEntryRepository.findAll();
			return list;
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while fetching the list of Limit Entries: {}", e.getMessage(),
					e);
			throw new DataAccessError("Failed to list Smsc limit Entries. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while fetching the list of Limit Entries: {}", e.getMessage(),
					e);
			throw new InternalServerException("Failed to list Smsc limit Entries. Unexpected error occurred.");
		}
	}

	@Override
	public String saveGroup(GroupRequest groupRequest, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		try {
			List<GroupEntry> convertedRequest = ConvertRequest(groupRequest);
			groupEntryRepository.saveAll(convertedRequest);
			MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
			logger.info("GroupEntry saved successfully");
			return "GroupEntry saved successfully";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the GroupEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to save GroupEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the GroupEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to save GroupEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String updateGroup(GroupRequest groupRequest, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			List<GroupEntry> convertRequest = ConvertRequest(groupRequest);
			for (GroupEntry group : convertRequest) {
				if (groupEntryRepository.existsById(group.getId())) {
					groupEntryRepository.save(group);
					MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
				} else {
					logger.info("Group not found with id: {}", group.getId());
					throw new NotFoundException("Group not found with id: " + group.getId());
				}
			}
			return "Group updated successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the GroupEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to update GroupEntry. Data access error occurred.");
		} catch (NotFoundException e) {
			logger.error("GroupEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the GroupEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to update GroupEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String deleteGroup(int groupId, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			Optional<GroupEntry> groupEntryOptional = groupEntryRepository.findById(groupId);
			if (!groupEntryOptional.isPresent()) {
				throw new NotFoundException("Group with ID " + groupId + " was not found in the database.");
			}
			groupEntryRepository.deleteById(groupId);
			MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
			return "Group with ID " + groupId + " has been deleted successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while deleting the GroupEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to delete GroupEntry. Data access error occurred.");
		} catch (NotFoundException e) {
			logger.error("Group not found: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while deleting the GroupEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to delete GroupEntry. Unexpected error occurred.");
		}
	}

	@Override
	public List<GroupEntry> listGroup(String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			List<GroupEntry> list = groupEntryRepository.findAll();
			return list;
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while fetching the list of Group Entries: {}", e.getMessage(),
					e);
			throw new DataAccessError("Failed to list Group Entries. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while fetching the list of Group Entries: {}", e.getMessage(),
					e);
			throw new InternalServerException("Failed to list Group Entries. Unexpected error occurred.");
		}
	}

	@Override
	public String saveGroupMember(GroupMemberRequest groupMemberRequest, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		try {
			List<GroupMemberEntry> convertRequest = ConvertRequest(groupMemberRequest);
			for (GroupMemberEntry entry : convertRequest) {
				groupMemberEntryRepository.save(entry);

			}
			MultiUtility.changeFlag(Constants.SMSC_FLAG_FILE, "707");
			MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
			return "Group members saved successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the GroupMemberEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to save GroupMemberEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the GroupMemberEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to save GroupMemberEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String updateGroupMember(GroupMemberRequest groupMemberRequest, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			List<GroupMemberEntry> convertRequest = ConvertRequest(groupMemberRequest);

			for (GroupMemberEntry entry : convertRequest) {
				if (groupMemberEntryRepository.existsById(entry.getId())) {
					groupMemberEntryRepository.save(entry);
					MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
				} else {
					throw new NotFoundException("Group member not found with ID: " + entry.getId());
				}
			}
			return "Group members updated successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the GroupMemberEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to update GroupMemberEntry. Data access error occurred.");
		} catch (NotFoundException e) {
			logger.error("GroupMemberNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the GroupMemberEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to update GroupMemberEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String deleteGroupMember(int groupMemberId, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			if (groupMemberEntryRepository.existsById(groupMemberId)) {
				groupMemberEntryRepository.deleteById(groupMemberId);
				MultiUtility.changeFlag(Constants.DGM_FLAG_FILE, "707");
				return "Group member with ID " + groupMemberId + " has been deleted successfully.";
			} else {
				String errorMessage = "Group member with ID " + groupMemberId + " not found.";
				logger.error(errorMessage);
				throw new NotFoundException(errorMessage);
			}
		} catch (NotFoundException e) {
			logger.error("Group member not found exception: {}", e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			String errorMessage = "An unexpected error occurred while deleting the GroupMemberEntry: " + e.getMessage();
			logger.error(errorMessage, e);
			throw new InternalServerException(errorMessage);
		}
	}

	@Override
	public String saveSchedule(TrafficScheduleRequest trafficScheduleRequest) {
		try {
			List<TrafficScheduleEntry> convertedEntries = ConvertRequest(trafficScheduleRequest);
			for (TrafficScheduleEntry entry : convertedEntries) {
				trafficScheduleEntryRepository.save(entry);
				MultiUtility.changeFlag(Constants.SMSC_SH_FLAG_FILE, "707");
			}
			return "Traffic schedule saved successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to save TrafficScheduleEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to save TrafficScheduleEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String updateSchedule(TrafficScheduleRequest trafficScheduleRequest, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			List<TrafficScheduleEntry> convertRequest = ConvertRequest(trafficScheduleRequest);
			for (TrafficScheduleEntry entry : convertRequest) {
				if (trafficScheduleEntryRepository.existsById(entry.getId())) {
					trafficScheduleEntryRepository.save(entry);
					MultiUtility.changeFlag(Constants.SMSC_SH_FLAG_FILE, "707");
				} else {
					throw new NotFoundException("Traffic schedule not found with ID: " + entry.getId());
				}
			}
			return "Traffic schedule updated successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to update TrafficScheduleEntry. Data access error occurred.");
		} catch (NotFoundException e) {
			logger.error("TrafficScheduleNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to update TrafficScheduleEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String deleteSchedule(int scheduleId, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			if (trafficScheduleEntryRepository.existsById(scheduleId)) {
				trafficScheduleEntryRepository.deleteById(scheduleId);
				MultiUtility.changeFlag(Constants.SMSC_SH_FLAG_FILE, "707");
				return "Traffic schedule with ID " + scheduleId + " deleted successfully.";
			} else {
				logger.error("Traffic schedule with ID {} does not exist", scheduleId);
				throw new NotFoundException("Traffic schedule with ID " + scheduleId + " not found.");
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while deleting the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to delete TrafficScheduleEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while deleting the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to delete TrafficScheduleEntry. Unexpected error occurred.");
		}
	}

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
			logger.error("A data access error occurred while fetching the list of TrafficScheduleEntries: {}",
					e.getMessage(), e);
			throw new DataAccessError("Failed to list TrafficScheduleEntries. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while fetching the list of TrafficScheduleEntries: {}",
					e.getMessage(), e);
			throw new InternalServerException("Failed to list TrafficScheduleEntries. Unexpected error occurred.");
		}
	}

	private void setDayName(TrafficScheduleEntry entry) {
		Map<Integer, String> daysMap = Map.of(0, "EveryDay", 1, "Sunday", 2, "Monday", 3, "Tuesday", 4, "Wednesday", 5,
				"Thursday", 6, "Friday", 7, "Saturday");
		entry.setDayName(daysMap.getOrDefault(entry.getDay(), "Unknown"));
	}

	private void setSmscNameAndAddToMap(TrafficScheduleEntry entry, Map<String, TrafficScheduleEntry> map) {
		Optional<SmscEntry> smscEntry = smscEntryRepository.findById(entry.getSmscId());
		if (smscEntry.isPresent()) {
			entry.setSmscName(smscEntry.get().getName());
			map.put(entry.getSmscId() + "#" + entry.getDay(), entry);
		}
	}

	@Override
	public String saveLoopingRule(SmscLoopingRequest smscLoopingRequest) {
		try {
			SmscLooping convertRequest = ConvertRequest(smscLoopingRequest);
			smscLoopingRepository.save(convertRequest);
			MultiUtility.changeFlag(Constants.SMSC_LOOP_FLAG_FILE, "707");
			return "SmscLooping entry saved successfully";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the SmscLooping entry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to save SmscLooping entry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the SmscLooping entry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to save SmscLooping entry. Unexpected error occurred.");
		}
	}

	@Override
	public String loopingRuleupdate(SmscLoopingRequest smscLoopingRequest, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			SmscLooping convertRequest = ConvertRequest(smscLoopingRequest);
			if (smscLoopingRepository.existsById(convertRequest.getSmscId())) {
				smscLoopingRepository.save(convertRequest);
				MultiUtility.changeFlag(Constants.SMSC_LOOP_FLAG_FILE, "707");
				return "SmscLooping entry updated successfully";
			} else {
				throw new NotFoundException("SmscLooping entry with the provided ID not found");
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the SmscLooping entry", e);
			throw new DataAccessError("Failed to update SmscLooping entry. Data access error occurred.");
		} catch (NotFoundException e) {
			logger.error("NotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the SmscLooping entry", e);
			throw new InternalServerException("Failed to update SmscLooping entry. Unexpected error occurred.");
		}
	}

	@Override
	public String loopingRuledelete(int smscId, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		try {
			if (smscLoopingRepository.existsById(smscId)) {
				smscLoopingRepository.deleteById(smscId);
				MultiUtility.changeFlag(Constants.SMSC_LOOP_FLAG_FILE, "707");
				return "SmscLooping entry deleted successfully";
			} else {
				throw new NotFoundException("SmscLooping entry with the provided ID not found");
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while deleting the SmscLooping entry: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to delete SmscLooping entry. Data access error occurred.");
		} catch (NotFoundException e) {
			logger.error("SmscLooping entry not found: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while deleting the SmscLooping entry: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to delete SmscLooping entry. Unexpected error occurred.");
		}
	}

	@Override
	public SmscLooping getLoopingRule(int smscId, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			Optional<SmscLooping> loopingRule = smscLoopingRepository.findBySmscId((long) smscId);
			if (loopingRule.isPresent()) {
				return loopingRule.get();
			} else {
				throw new NotFoundException("SmscLooping rule not found with ID: " + smscId);
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while retrieving the SmscLooping rule: {}", e.getMessage(), e);
			throw new DataAccessError("Failed to retrieve SmscLooping rule. Data access error occurred.");
		} catch (NotFoundException e) {
			logger.error("NotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while retrieving the SmscLooping rule: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to retrieve SmscLooping rule. Unexpected error occurred.");
		}
	}

	@Override
	public List<SmscLooping> listLoopingRule(String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			return smscLoopingRepository.findAll();
		} catch (Exception e) {
			logger.error("An unexpected error occurred while listing SmscLooping rules: {}", e.getMessage(), e);
			throw new InternalServerException("Failed to list SmscLooping rules. Unexpected error occurred.");
		}
	}

	public SmscEntry ConvertRequest(SmscEntryRequest smscEntryRequest) {
		try {
			SmscEntry smsc = new SmscEntry();
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

			logger.info("Converted SmscEntryRequest to SmscEntry successfully");
			return smsc;
		} catch (Exception e) {
			logger.error("Error occurred while converting SmscEntryRequest to SmscEntry: {}", e.getMessage());
			throw new InternalServerException("Error occurred while converting SmscEntryRequest to SmscEntry" + e);

		}
	}

	public CustomEntry ConvertRequest(CustomRequest customRequest) {

		try {
			CustomEntry custom = new CustomEntry();

			custom.setGsnpi(customRequest.getGsnpi());
			custom.setGston(customRequest.getGston());
			custom.setLsnpi(customRequest.getLsnpi());
			custom.setLston(customRequest.getSmscId());
			custom.setSmscId(customRequest.getSmscId());
			custom.setSourceLength(customRequest.getSourceLength());
			logger.info("Converted CustomRequest to CustomEntry successfully");
			return custom;
		} catch (Exception e) {
			logger.error("Error occurred while converting CustomRequest to CustomEntry: {}", e.getMessage());
			throw new InternalServerException("Error occurred while converting CustomRequest to CustomEntry" + e);

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

			logger.info("Converted GroupRequest to GroupEntry successfully");
			return list;
		} catch (Exception e) {
			logger.error("Error occurred while converting GroupRequest to GroupEntry: {}", e.getMessage());
			throw new InternalServerException("Error occurred while converting GroupRequest to GroupEntry" + e);

		}
	}

	public List<GroupMemberEntry> ConvertRequest(GroupMemberRequest groupMemberRequest) {
		try {
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
					existEntry.setGroupId(groupMemberRequest.getGroupId());
					existEntry.setSmscId(smsc[i]);
					existEntry.setPercent(percent[i]);
					list.add(existEntry);
				} else {
					GroupMemberEntry entry = new GroupMemberEntry();
					entry.setGroupId(groupMemberRequest.getGroupId());
					entry.setSmscId(smsc[i]);
					entry.setPercent(percent[i]);
					list.add(entry);
					logger.info(entry.toString());
				}
			}
			logger.info("Converted GroupMemberRequest to GroupMemberEntry successfully");
			return list;
		} catch (Exception e) {
			logger.error("Error occurred while converting GroupMemberRequest to GroupMemberEntry: {}", e.getMessage());
			throw new InternalServerException(
					"Error occurred while converting GroupMemberRequest to GroupMemberEntry" + e);

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

			logger.info("Converted LimitRequest to LimitEntry successfully");
			return list;
		} catch (Exception e) {
			logger.error("Error occurred while converting LimitRequest to LimitEntry: {}", e.getMessage());
			throw new InternalServerException("Error occurred while converting LimitRequest to LimitEntry" + e);

		}
	}

	public SmscLooping ConvertRequest(SmscLoopingRequest smscLoopingRequest) {

		SmscLooping smscLooping = null;
		try {
			smscLooping = new SmscLooping();
			smscLooping.setActive(smscLoopingRequest.isActive());
			smscLooping.setCount(smscLoopingRequest.getCount());
			smscLooping.setDuration(smscLoopingRequest.getDuration());
			// smscLooping.setRerouteSmsc(smscLoopingRequest.);
			smscLooping.setRerouteSmscId(smscLoopingRequest.getRerouteSmscId());
			smscLooping.setSenderId(smscLoopingRequest.getSenderId());
			// smscLooping.setSmsc(smscLoopingRequest.);
			smscLooping.setSmscId(smscLoopingRequest.getSmscId());
			logger.info("Converted SmscLoopingRequest to SmscLooping successfully");
			return smscLooping;
		} catch (Exception e) {
			logger.error("Error occurred while converting SmscLoopingRequest to SmscLooping: {}", e.getMessage());
			throw new InternalServerException("Error occurred while converting SmscLoopingRequest to SmscLooping" + e);

		}
	}

	public List<TrafficScheduleEntry> ConvertRequest(TrafficScheduleRequest trafficScheduleRequest) {

		try {
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
			logger.info("Converted TrafficScheduleRequest to TrafficScheduleEntry successfully");

			return list;
		} catch (Exception e) {
			logger.error("Error occurred while converting TrafficScheduleRequest to TrafficScheduleEntry: {}",
					e.getMessage());
			throw new InternalServerException(
					"Error occurred while converting TrafficScheduleRequest to TrafficScheduleEntry" + e);

		}
	}

	@Override
	public String saveSmscBsfm(SmscBsfmEntryRequest smscBsfmEntryRequest, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		SmscBsfmEntry smscBsfmEntry = new SmscBsfmEntry();
		ConvertRequest(smscBsfmEntryRequest, smscBsfmEntry);
		smscBsfmEntryRepository.save(smscBsfmEntry);
		MultiUtility.changeFlag(Constants.SMSC_BSFM_FLAG_FILE, "707");
		return "saccessfully save....";

	}

	private void ConvertRequest(SmscBsfmEntryRequest smscBsfmEntryRequest, SmscBsfmEntry smscBsfmEntry) {
		smscBsfmEntry.setContent(smscBsfmEntryRequest.getContent());
		smscBsfmEntry.setSmscId(smscBsfmEntryRequest.getSmscId());
		smscBsfmEntry.setSmscName(smscBsfmEntryRequest.getSmscName());
		smscBsfmEntry.setSource(smscBsfmEntryRequest.getSmscId() + "");

	}

	@Override
	public List<TrafficScheduleEntry> listTrafficSchedule(String username) {
		try {
			User user = loginRepository.findBySystemId(username)
					.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

			List<TrafficScheduleEntry> trafficScheduleEntries = trafficScheduleEntryRepository.findAll();

			// Log the successful retrieval of traffic schedule entries
			logger.info("Successfully retrieved traffic schedule entries for user: {}", username);

			// Process the list or return it directly based on your requirements
			return trafficScheduleEntries;
		} catch (NotFoundException | UnauthorizedException ex) {
			// Log the exception with appropriate level (info, warn, error, etc.)
			logger.error("Error in listTrafficSchedule for user {}: {}", username, ex.getMessage(), ex);

			// Re-throw the exception for higher-level handling if needed
			throw ex;
		} catch (Exception ex) {
			// Log unexpected exceptions with error level
			logger.error("Unexpected error in listTrafficSchedule for user {}: {}", username, ex.getMessage(), ex);

			// Wrap and throw a generic exception for higher-level handling if needed
			throw new InternalServerException(
					"An unexpected error occurred while processing the request." + ex.getLocalizedMessage());
		}
	}

	@Override
	public List<SmscBsfmEntry> listSmscBsfm(String username) {
		try {
			User user = loginRepository.findBySystemId(username)
					.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

			List<SmscBsfmEntry> smscBsfmEntries = smscBsfmEntryRepository.findAll();

			// Log the successful retrieval of SMS/C BSFM entries
			logger.info("Successfully retrieved SMS/C BSFM entries for user: {}", username);

			// Process the list or return it directly based on your requirements
			return smscBsfmEntries;
		} catch (NotFoundException | UnauthorizedException ex) {
			// Log the exception with appropriate level (info, warn, error, etc.)
			logger.error("Error in listSmscBsfm for user {}: {}", username, ex.getMessage(), ex);

			// Re-throw the exception for higher-level handling if needed
			throw new NotFoundException(ex.getMessage());
		} catch (Exception ex) {
			// Log unexpected exceptions with error level
			logger.error("Unexpected error in listSmscBsfm for user {}: {}", username, ex.getMessage(), ex);

			// Wrap and throw a generic exception for higher-level handling if needed
			throw new InternalServerException(
					"An unexpected error occurred while processing the request." + ex.getLocalizedMessage());
		}
	}

	@Override
	public ResponseEntity<String> bsfmupdate(SmscBsfmEntryRequest smscBsfmEntryRequest, String username) {
		try {
			if (smscBsfmEntryRepository.existsById(smscBsfmEntryRequest.getId())) {
				SmscBsfmEntry smscBsfmEntry = new SmscBsfmEntry();
				smscBsfmEntry.setId(smscBsfmEntryRequest.getId());
				smscBsfmEntry.setContent(smscBsfmEntryRequest.getContent());
				smscBsfmEntry.setSmscId(smscBsfmEntryRequest.getSmscId());
				smscBsfmEntry.setSmscName(smscBsfmEntryRequest.getSmscName());
				smscBsfmEntry.setSource(smscBsfmEntryRequest.getSource());
				smscBsfmEntryRepository.save(smscBsfmEntry);
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

	@Override
	public ResponseEntity<String> bsfmdelete(int id, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			if (smscBsfmEntryRepository.existsById(id)) {
				smscBsfmEntryRepository.deleteById(id);
				MultiUtility.changeFlag(Constants.SMSC_BSFM_FLAG_FILE, "707");
				return new ResponseEntity<>("Entry deleted successfully", HttpStatus.OK);
			} else {
				throw new NotFoundException("Entry not found with ID: " + id);
			}
		} catch (NotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<SmscEntry> getSmscEntry(int id, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			Optional<SmscEntry> optionalSmscEntry = smscEntryRepository.findById(id);

			if (optionalSmscEntry.isPresent()) {
				SmscEntry smscEntry = optionalSmscEntry.get();
				return new ResponseEntity<>(smscEntry, HttpStatus.OK);
			} else {
				throw new NotFoundException("SMS entry not found with ID: " + id);
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException(e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> getGroupMember(int id, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		try {
			Optional<GroupMemberEntry> optionalGroupMember = groupMemberEntryRepository.findById(id);

			if (optionalGroupMember.isPresent()) {
				GroupMemberEntry groupMember = optionalGroupMember.get();
				return new ResponseEntity<>(groupMember, HttpStatus.OK);
			} else {
				throw new NotFoundException("Group member not found with ID: " + id);
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException(e.getMessage());
		}
	}
}