package com.hti.smpp.common.impl;

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
import org.springframework.stereotype.Service;

import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.dto.GroupMemberEntry;
import com.hti.smpp.common.contacts.repository.GroupEntryRepository;
import com.hti.smpp.common.contacts.repository.GroupMemberEntryRepository;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.SmscDataAccessException;
import com.hti.smpp.common.exception.SmscInternalServerException;
import com.hti.smpp.common.exception.SmscNotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.request.CustomRequest;
import com.hti.smpp.common.request.GroupMemberRequest;
import com.hti.smpp.common.request.GroupRequest;
import com.hti.smpp.common.request.LimitRequest;
import com.hti.smpp.common.request.SmscEntryRequest;
import com.hti.smpp.common.request.SmscLoopingRequest;
import com.hti.smpp.common.request.TrafficScheduleRequest;
import com.hti.smpp.common.service.SmscDAO;
import com.hti.smpp.common.smsc.dto.CustomEntry;
import com.hti.smpp.common.smsc.dto.LimitEntry;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.dto.SmscLooping;
import com.hti.smpp.common.smsc.dto.StatusEntry;
import com.hti.smpp.common.smsc.dto.TrafficScheduleEntry;
import com.hti.smpp.common.smsc.repository.CustomEntryRepository;
import com.hti.smpp.common.smsc.repository.LimitEntryRepository;
import com.hti.smpp.common.smsc.repository.SmscEntryRepository;
import com.hti.smpp.common.smsc.repository.SmscLoopingRepository;
import com.hti.smpp.common.smsc.repository.StatusEntryRepository;
import com.hti.smpp.common.smsc.repository.TrafficScheduleEntryRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Transactional
@Service
public class SmscDAOImpl implements SmscDAO {

	private static final Logger logger = LoggerFactory.getLogger(SmscDAOImpl.class);

	@Autowired
	private SmscEntryRepository smscEntryRepository;

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
	public String save(SmscEntryRequest smscEntryRequest, String username) {

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
				throw new SmscNotFoundException("User not found. Please enter a valid username.");
			}
			// Saving the SMS entry
			SmscEntry savedEntry = smscEntryRepository.save(convertedRequest);
			return "Successfully saved this id: " + savedEntry.getId();
		} catch (SmscNotFoundException e) {
			System.out.println("run not found exception...");
			logger.error("An error occurred while saving the SmscEntry: {}", e.getMessage());
			throw new SmscNotFoundException("Failed to save SmscEntry. Error: " + e.getMessage());
		} catch (DataAccessException e) {
			logger.error("A DataAccessException occurred while saving the SmscEntry: {}", e.getMessage());
			throw new SmscDataAccessException("Failed to save SmscEntry. Data access error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("An error occurred while saving the SmscEntry: {}", e.getMessage());
			throw new SmscInternalServerException("Failed to save SmscEntry. Error: " + e.getMessage());
		}
	}

	private void setConvertedRequestFields(SmscEntry convertedRequest, UserEntry userEntry) {
		convertedRequest.setSystemId(String.valueOf(userEntry.getSystemId()));
		convertedRequest.setSystemType(userEntry.getSystemType());
		convertedRequest.setMasterId(userEntry.getMasterId());
	}

	@Override
	public String update(int smscId, SmscEntryRequest smscEntryRequest, String username) {

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
				return "SmscEntry with ID " + smscId + " has been successfully updated.";
			} else {
				throw new SmscNotFoundException("SmscEntry with ID " + smscId + " not found. Update operation failed.");
			}
		} catch (SmscNotFoundException e) {
			logger.error("An error occurred during the update operation: " + e.getMessage(), e);
			throw new SmscNotFoundException("Failed to update SmscEntry: " + e.getMessage());
		} catch (DataAccessException e) {
			logger.error("A DataAccessException occurred during the update operation: " + e.getMessage(), e);
			throw new SmscDataAccessException("Failed to update SmscEntry: Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred during the update operation: " + e.getMessage(), e);
			throw new SmscInternalServerException("Failed to update SmscEntry: Unexpected error occurred.");
		}
	}

	@Override
	public String delete(int smscId, String username) {

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
				throw new SmscNotFoundException("SmscEntry with ID " + smscId + " was not found in the database.");
			}
			smscEntryRepository.deleteById(smscId);
			System.out.println("run delete ...");
			return "SmscEntry with ID " + smscId + " has been successfully deleted.";
		} catch (DataAccessException ex) {
			logger.error("An error occurred during the delete operation: " + ex.getMessage(), ex);
			throw new SmscDataAccessException("Failed to delete SmscEntry: " + ex.getMessage());
		} catch (SmscNotFoundException e) {
			logger.error("An error occurred during the update operation: " + e.getMessage(), e);
			throw e;
		} catch (Exception ex) {
			logger.error("An error occurred during the delete operation: " + ex.getMessage(), ex);
			throw new SmscInternalServerException("Failed to delete SmscEntry: " + ex.getMessage());
		}
	}

	@Override
	public List<StatusEntry> listBound(boolean bound, String username) {

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
			List<StatusEntry> list;
			if (bound) {
				list = statusEntryRepository.findByBound(bound);
			} else {
				list = statusEntryRepository.findAll();
			}
			return list;
		} catch (DataAccessException e) {
			// Log the exception for debugging purposes
			logger.error("Error occurred while fetching StatusEntry records: " + e.getMessage());
			throw new SmscDataAccessException("Failed to list Smsc Status Records");
		} catch (Exception e) {
			// Log the exception for debugging purposes
			logger.error("Unknown error occurred while fetching StatusEntry records: " + e.getMessage());
			throw new SmscInternalServerException("Failed to list Smsc Status Records");
		}
	}

	@Override
	public List<CustomEntry> listCustom(String username) {

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
			List<CustomEntry> list = customEntryRepository.findAll();
			return list;
		} catch (DataAccessException e) {
			// Log the exception for debugging purposes
			logger.error("Error occurred while fetching CustomEntry records: " + e.getMessage());
			throw new SmscDataAccessException("Failed to list Smsc Custom Entry Records");
		} catch (Exception e) {
			// Log the exception for debugging purposes
			logger.error("Unknown error occurred while fetching CustomEntry records: " + e.getMessage());
			throw new SmscInternalServerException("Failed to list Smsc Custom Entry Records");
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
				throw new SmscNotFoundException("Smsc CustomEntry with ID " + smscId + " not found");
			}
		} catch (SmscNotFoundException e) {
			// Log the exception for debugging purposes
			logger.error("CustomEntryNotFoundException: " + e.getMessage());
			throw e;
		} catch (DataAccessException e) {
			// Log the exception for debugging purposes
			logger.error("DataAccessException: " + e.getMessage());
			throw new SmscDataAccessException("Failed to retrieve Smsc CustomEntry with ID: " + smscId);
		} catch (Exception e) {
			// Log the exception for debugging purposes
			logger.error("Unknown error occurred while retrieving CustomEntry: " + e.getMessage());
			throw new SmscInternalServerException("Failed to retrieve Smsc CustomEntry with ID: " + smscId);
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
			return "Successfully saved the CustomEntry.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the CustomEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to save CustomEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the CustomEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to save CustomEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String updateCustom(int customId, CustomRequest customRequest) {
		try {
			if (customEntryRepository.existsById(customId)) {
				CustomEntry convertRequest = ConvertRequest(customRequest);
				convertRequest.setSmscId(customId); // Ensure the ID is set
				customEntryRepository.save(convertRequest);
				return "CustomEntry updated successfully";
			} else {
				throw new SmscNotFoundException("CustomEntry not found with id: " + customId);
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the CustomEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to update CustomEntry. Data access error occurred.");
		} catch (SmscNotFoundException e) {
			logger.error("CustomEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the CustomEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to update CustomEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String deleteCustom(int customId) {
		try {
			Optional<CustomEntry> optionalCustomEntry = customEntryRepository.findById(customId);
			if (optionalCustomEntry.isPresent()) {
				CustomEntry entry = optionalCustomEntry.get();
				customEntryRepository.delete(entry);
				return "CustomEntry deleted successfully";
			} else {
				throw new SmscNotFoundException("CustomEntry not found with id: " + customId);
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while deleting the CustomEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to delete CustomEntry. Data access error occurred.");
		} catch (SmscNotFoundException e) {
			logger.error("CustomEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while deleting the CustomEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to delete CustomEntry. Unexpected error occurred.");
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
			return "LimitEntry saved successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the LimitEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to save LimitEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the LimitEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to save LimitEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String updateLimit(int limitId, LimitRequest limitRequest) {
		try {
			if (limitEntryRepository.existsById(limitId)) {
				List<LimitEntry> convertRequest = ConvertRequest(limitRequest);
				for (LimitEntry entry : convertRequest) {
					if (entry.getId() == limitId) {
						limitEntryRepository.save(entry);
						return "Limit updated successfully";
					}
				}
				throw new RuntimeException("Failed to update LimitEntry: No matching ID found in the request");
			} else {
				throw new SmscNotFoundException("Limit with the provided ID not found");
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the LimitEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to update LimitEntry. Data access error occurred.");
		} catch (SmscNotFoundException e) {
			logger.error("LimitEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the LimitEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to update LimitEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String deleteLimit(int limitId) {
		try {
			if (limitEntryRepository.existsById(limitId)) {
				limitEntryRepository.deleteById(limitId);
				return "LimitEntry with ID " + limitId + " deleted successfully";
			} else {
				throw new SmscNotFoundException("LimitEntry with ID " + limitId + " not found");
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while deleting the LimitEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to delete LimitEntry. Data access error occurred.");
		} catch (SmscNotFoundException e) {
			logger.error("LimitEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while deleting the LimitEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to delete LimitEntry. Unexpected error occurred.");
		}
	}

	@Override
	public List<LimitEntry> listLimit() {
		try {
			List<LimitEntry> list = limitEntryRepository.findAll();
			return list;
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while fetching the list of Limit Entries: {}", e.getMessage(),
					e);
			throw new SmscDataAccessException("Failed to list Smsc limit Entries. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while fetching the list of Limit Entries: {}", e.getMessage(),
					e);
			throw new SmscInternalServerException("Failed to list Smsc limit Entries. Unexpected error occurred.");
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
			logger.info("GroupEntry saved successfully");
			return "GroupEntry saved successfully";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the GroupEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to save GroupEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the GroupEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to save GroupEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String updateGroup(GroupRequest groupRequest) {
		try {
			List<GroupEntry> convertRequest = ConvertRequest(groupRequest);
			for (GroupEntry group : convertRequest) {
				if (groupEntryRepository.existsById(group.getId())) {
					groupEntryRepository.save(group);
				} else {
					logger.info("Group not found with id: {}", group.getId());
					throw new SmscNotFoundException("Group not found with id: " + group.getId());
				}
			}
			return "Group updated successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the GroupEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to update GroupEntry. Data access error occurred.");
		} catch (SmscNotFoundException e) {
			logger.error("GroupEntryNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the GroupEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to update GroupEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String deleteGroup(int groupId) {
		try {
			Optional<GroupEntry> groupEntryOptional = groupEntryRepository.findById(groupId);
			if (!groupEntryOptional.isPresent()) {
				throw new SmscNotFoundException("Group with ID " + groupId + " was not found in the database.");
			}
			groupEntryRepository.deleteById(groupId);
			return "Group with ID " + groupId + " has been deleted successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while deleting the GroupEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to delete GroupEntry. Data access error occurred.");
		} catch (SmscNotFoundException e) {
			logger.error("Group not found: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while deleting the GroupEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to delete GroupEntry. Unexpected error occurred.");
		}
	}

	@Override
	public List<GroupEntry> listGroup() {
		try {
			List<GroupEntry> list = groupEntryRepository.findAll();
			return list;
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while fetching the list of Group Entries: {}", e.getMessage(),
					e);
			throw new SmscDataAccessException("Failed to list Group Entries. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while fetching the list of Group Entries: {}", e.getMessage(),
					e);
			throw new SmscInternalServerException("Failed to list Group Entries. Unexpected error occurred.");
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
			return "Group members saved successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the GroupMemberEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to save GroupMemberEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the GroupMemberEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to save GroupMemberEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String updateGroupMember(GroupMemberRequest groupMemberRequest) {
		try {
			List<GroupMemberEntry> convertRequest = ConvertRequest(groupMemberRequest);

			for (GroupMemberEntry entry : convertRequest) {
				if (groupMemberEntryRepository.existsById(entry.getId())) {
					groupMemberEntryRepository.save(entry);
				} else {
					throw new SmscNotFoundException("Group member not found with ID: " + entry.getId());
				}
			}
			return "Group members updated successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the GroupMemberEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to update GroupMemberEntry. Data access error occurred.");
		} catch (SmscNotFoundException e) {
			logger.error("GroupMemberNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the GroupMemberEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to update GroupMemberEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String deleteGroupMember(int groupMemberId) {
		try {
			if (groupMemberEntryRepository.existsById(groupMemberId)) {
				groupMemberEntryRepository.deleteById(groupMemberId);
				return "Group member with ID " + groupMemberId + " has been deleted successfully.";
			} else {
				String errorMessage = "Group member with ID " + groupMemberId + " not found.";
				logger.error(errorMessage);
				throw new SmscNotFoundException(errorMessage);
			}
		} catch (SmscNotFoundException e) {
			// Handle the case when the group member with the provided ID does not exist
			logger.error("Group member not found exception: {}", e.getMessage());
			throw e; // rethrowing the caught exception
		} catch (DataAccessException e) {
			String errorMessage = "A data access error occurred while deleting the GroupMemberEntry: " + e.getMessage();
			logger.error(errorMessage, e);
			throw new SmscDataAccessException(errorMessage);
		} catch (Exception e) {
			String errorMessage = "An unexpected error occurred while deleting the GroupMemberEntry: " + e.getMessage();
			logger.error(errorMessage, e);
			throw new SmscInternalServerException(errorMessage);
		}
	}

	@Override
	public List<GroupMemberEntry> listGroupMember(int groupId) {
		try {
			List<GroupMemberEntry> list = groupMemberEntryRepository.findByGroupId(groupId);
			return list;
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while fetching the list of Group Member Entries: {}",
					e.getMessage(), e);
			throw new SmscDataAccessException("Failed to list Group Member Entries. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while fetching the list of Group Member Entries: {}",
					e.getMessage(), e);
			throw new SmscInternalServerException("Failed to list Group Member Entries. Unexpected error occurred.");
		}
	}

	@Override
	public String saveSchedule(TrafficScheduleRequest trafficScheduleRequest) {
		try {
			List<TrafficScheduleEntry> convertedEntries = ConvertRequest(trafficScheduleRequest);
			for (TrafficScheduleEntry entry : convertedEntries) {
				trafficScheduleEntryRepository.save(entry);
			}
			return "Traffic schedule saved successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to save TrafficScheduleEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to save TrafficScheduleEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String updateSchedule(TrafficScheduleRequest trafficScheduleRequest) {
		try {
			List<TrafficScheduleEntry> convertRequest = ConvertRequest(trafficScheduleRequest);
			for (TrafficScheduleEntry entry : convertRequest) {
				if (trafficScheduleEntryRepository.existsById(entry.getId())) {
					trafficScheduleEntryRepository.save(entry);
				} else {
					throw new SmscNotFoundException("Traffic schedule not found with ID: " + entry.getId());
				}
			}
			return "Traffic schedule updated successfully.";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to update TrafficScheduleEntry. Data access error occurred.");
		} catch (SmscNotFoundException e) {
			logger.error("TrafficScheduleNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to update TrafficScheduleEntry. Unexpected error occurred.");
		}
	}

	@Override
	public String deleteSchedule(int scheduleId) {
		try {
			if (trafficScheduleEntryRepository.existsById(scheduleId)) {
				trafficScheduleEntryRepository.deleteById(scheduleId);
				return "Traffic schedule with ID " + scheduleId + " deleted successfully.";
			} else {
				logger.error("Traffic schedule with ID {} does not exist", scheduleId);
				throw new SmscNotFoundException("Traffic schedule with ID " + scheduleId + " not found.");
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while deleting the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to delete TrafficScheduleEntry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while deleting the TrafficScheduleEntry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to delete TrafficScheduleEntry. Unexpected error occurred.");
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
			throw new SmscDataAccessException("Failed to list TrafficScheduleEntries. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while fetching the list of TrafficScheduleEntries: {}",
					e.getMessage(), e);
			throw new SmscInternalServerException("Failed to list TrafficScheduleEntries. Unexpected error occurred.");
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
			return "SmscLooping entry saved successfully";
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while saving the SmscLooping entry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to save SmscLooping entry. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while saving the SmscLooping entry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to save SmscLooping entry. Unexpected error occurred.");
		}
	}

	@Override
	public String updateLoopingRule(SmscLoopingRequest smscLoopingRequest) {
		try {
			SmscLooping convertRequest = ConvertRequest(smscLoopingRequest);
			if (smscLoopingRepository.existsById(convertRequest.getSmscId())) {
				smscLoopingRepository.save(convertRequest);
				return "SmscLooping entry updated successfully";
			} else {
				throw new SmscNotFoundException("SmscLooping entry with the provided ID not found");
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while updating the SmscLooping entry", e);
			throw new SmscDataAccessException("Failed to update SmscLooping entry. Data access error occurred.");
		} catch (SmscNotFoundException e) {
			logger.error("SmscNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while updating the SmscLooping entry", e);
			throw new SmscInternalServerException("Failed to update SmscLooping entry. Unexpected error occurred.");
		}
	}

	@Override
	public String deleteLoopingRule(int smscId) {
		try {
			if (smscLoopingRepository.existsById(smscId)) {
				smscLoopingRepository.deleteById(smscId);
				return "SmscLooping entry deleted successfully";
			} else {
				throw new SmscNotFoundException("SmscLooping entry with the provided ID not found");
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while deleting the SmscLooping entry: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to delete SmscLooping entry. Data access error occurred.");
		} catch (SmscNotFoundException e) {
			logger.error("SmscLooping entry not found: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while deleting the SmscLooping entry: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to delete SmscLooping entry. Unexpected error occurred.");
		}
	}

	@Override
	public SmscLooping getLoopingRule(int smscId) {
		try {
			Optional<SmscLooping> loopingRule = smscLoopingRepository.findBySmscId((long) smscId);
			if (loopingRule.isPresent()) {
				return loopingRule.get();
			} else {
				throw new SmscNotFoundException("SmscLooping rule not found with ID: " + smscId);
			}
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while retrieving the SmscLooping rule: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to retrieve SmscLooping rule. Data access error occurred.");
		} catch (SmscNotFoundException e) {
			logger.error("SmscNotFoundException: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while retrieving the SmscLooping rule: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to retrieve SmscLooping rule. Unexpected error occurred.");
		}
	}

	@Override
	public List<SmscLooping> listLoopingRule() {
		try {
			return smscLoopingRepository.findAll();
		} catch (DataAccessException e) {
			logger.error("A data access error occurred while listing SmscLooping rules: {}", e.getMessage(), e);
			throw new SmscDataAccessException("Failed to list SmscLooping rules. Data access error occurred.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while listing SmscLooping rules: {}", e.getMessage(), e);
			throw new SmscInternalServerException("Failed to list SmscLooping rules. Unexpected error occurred.");
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
			throw new RuntimeException("Error occurred while converting SmscEntryRequest to SmscEntry", e);

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
			throw new RuntimeException("Error occurred while converting CustomRequest to CustomEntry", e);

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
			throw new RuntimeException("Error occurred while converting GroupRequest to GroupEntry", e);

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
			throw new RuntimeException("Error occurred while converting GroupMemberRequest to GroupMemberEntry", e);

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
			throw new RuntimeException("Error occurred while converting LimitRequest to LimitEntry", e);

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
			throw new RuntimeException("Error occurred while converting SmscLoopingRequest to SmscLooping", e);

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
			throw new RuntimeException("Error occurred while converting TrafficScheduleRequest to TrafficScheduleEntry",
					e);

		}
	}

}
