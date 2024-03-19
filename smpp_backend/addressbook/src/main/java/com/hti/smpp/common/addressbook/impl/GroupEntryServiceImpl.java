package com.hti.smpp.common.addressbook.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.addressbook.request.GroupEntryRequest;
import com.hti.smpp.common.addressbook.response.ListGroupResponse;
import com.hti.smpp.common.addressbook.services.GroupEntryService;
import com.hti.smpp.common.addressbook.utils.Converters;
import com.hti.smpp.common.contacts.dto.ContactEntry;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;
import com.hti.smpp.common.contacts.dto.GroupEntryDTO;
import com.hti.smpp.common.contacts.repository.ContactRepository;
import com.hti.smpp.common.contacts.repository.GroupDataEntryRepository;
import com.hti.smpp.common.contacts.repository.GroupEntryDTORepository;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.user.dto.MultiUserEntry;
import com.hti.smpp.common.user.dto.User;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.dto.WebMenuAccessEntry;
import com.hti.smpp.common.user.repository.MultiUserEntryRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.user.repository.WebMenuAccessEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.transaction.Transactional;

@Service
/**
 * Implementation of the GroupEntryService interface providing methods for
 * managing contact groups.
 */
public class GroupEntryServiceImpl implements GroupEntryService {

	private static final Logger logger = LoggerFactory.getLogger(GroupEntryServiceImpl.class.getName());

	@Autowired
	private GroupEntryDTORepository groupEntryDTORepository;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private WebMasterEntryRepository webMasterRepo;

	@Autowired
	private GroupDataEntryRepository groupDataEntryRepository;

	@Autowired
	private ContactRepository contactRepo;

	@Autowired
	private WebMenuAccessEntryRepository webMenuRepo;

	@Autowired
	private MultiUserEntryRepository multiUserEntryRepository;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	/**
	 * Saves contact group entries based on the provided GroupEntryRequest and
	 * username.
	 */
	@Override
	public ResponseEntity<?> saveGroupEntry(GroupEntryRequest form, String username) {
		Optional<User> userOptional = userRepository.getUsers(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

		String systemId = user.getSystemId();
		logger.info(messageResourceBundle.getLogMessage("addbook.group.add.contact.request.info"), systemId,
				user.getRole());
		String target = IConstants.FAILURE_KEY;
		GroupEntryDTO entry = null;
		int total = form.getName().length;
		List<GroupEntryDTO> list = new ArrayList<GroupEntryDTO>();
		String[] names = form.getName();
		boolean[] groupData = form.getGroupData();

		try {
			for (int i = 0; i < total; i++) {
				entry = new GroupEntryDTO();
				if (names[i] != null && names[i].length() > 0) {
					entry.setName(new Converters().UTF16(names[i]));
					entry.setGroupData(groupData[i]);
					entry.setMasterId(systemId);
					MultiUserEntry multiUserEntry = null;
					try {
						multiUserEntry = multiUserEntryRepository.findByUserId(user.getUserId());
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage());
						throw new NotFoundException(e.getLocalizedMessage());
					}
					WebMasterEntry webEntry = null;
					try {
						webEntry = this.webMasterRepo.findByUserId(user.getUserId());
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage());
						throw new NotFoundException(e.getLocalizedMessage());
					}

					// Check if multi-user access is enabled and the access name is not null
					if (webEntry.isMultiUserAccess() && multiUserEntry.getAccessName() != null) {
						// Set createdBy to the access name from MultiUserEntry
						entry.setCreatedBy(multiUserEntry.getAccessName());
					} else {
						// Set createdBy to a default value (e.g., systemId) if multi-user access is not
						// enabled or access name is null
						entry.setCreatedBy(systemId);
					}

					list.add(entry);
				} else {
					logger.warn(messageResourceBundle.getLogMessage("addbook.group.invalid.name.warn"), systemId,
							names[i]);
					continue;
				}
			}
			if (list.isEmpty()) {
				logger.error(messageResourceBundle.getLogMessage("addbook.group.no.valid.entry.error"), systemId);
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_EMPTY_DATASET));
			} else {
				this.groupEntryDTORepository.saveAll(list);
				target = IConstants.SUCCESS_KEY;
				logger.info(messageResourceBundle.getLogMessage("addbook.group.add.contact.status.info"), systemId,
						target);
				return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.ADDBOOK_GROUP_SAVED),
						HttpStatus.CREATED);
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (DataIntegrityViolationException e) {
			throw new InternalServerException(
					messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_GROUP_DUPLICATE_ENTRY));
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error(systemId, e.getLocalizedMessage());
			throw new InternalServerException(
					messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_INCOMPLETE_DATA));
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_ERROR_MSG,
					new Object[] { e.getLocalizedMessage() }));
		}

	}

	/**
	 * Modifies contact group entries based on the provided form data and username.
	 */
	@Override
	@Transactional
	public ResponseEntity<?> modifyGroupEntryUpdate(GroupEntryRequest form, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		String target = IConstants.FAILURE_KEY;

		String systemId = user.getSystemId();
		GroupEntryDTO entry = null;
		logger.info(messageResourceBundle.getLogMessage("addbook.group.modify.contact.request.info"), systemId,
				user.getRole());
		List<GroupEntryDTO> list = new ArrayList<GroupEntryDTO>();
		int[] id = form.getId();
		String[] names = form.getName();
		boolean[] groupData = form.getGroupData();
		if (id != null && id.length > 0) {
			try {
				for (int i = 0; i < id.length; i++) {
					if (names[i] != null && names[i].length() > 0) {
						entry = new GroupEntryDTO(new Converters().UTF16(names[i]), systemId, groupData[i]);
						entry.setId(id[i]);
						list.add(entry);
					} else {
						logger.warn(messageResourceBundle.getLogMessage("addbook.group.invalid.name.warn"),
								user.getRole(), names[i]);
						continue;
					}
				}

				if (!list.isEmpty()) {
					this.groupEntryDTORepository.saveAll(list);
					target = IConstants.SUCCESS_KEY;
				} else {
					logger.error(messageResourceBundle.getLogMessage("addbook.update.list.empty.error"));
					throw new InternalServerException(
							messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_CONTACT_UPDATE_ERROR));
				}

			} catch (Exception e) {
				logger.error(systemId, e.toString());
				throw new InternalServerException(e.getLocalizedMessage());
			}
		} else {
			logger.error(messageResourceBundle.getLogMessage("addbook.no.record.found.error"));
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_NORECORD));
		}
		logger.info(messageResourceBundle.getLogMessage("addbook.group.modify.contact.status.info"), systemId, target);
		return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.ADDBOOK_GROUP_UPDATED),
				HttpStatus.CREATED);
	}

	/**
	 * Deletes group entries and associated data from repositories.
	 * 
	 * @param list
	 */
	private void deleteGroup(int id) {
		GroupEntryDTO entry = null;
		try {
			Optional<GroupEntryDTO> groupOption = this.groupEntryDTORepository.findById(id);
			if (groupOption.isPresent()) {
				entry = groupOption.get();
				this.groupEntryDTORepository.deleteById(entry.getId());
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_GROUP_NOTFOUND,
						new Object[] { entry.getId() }));
			}
		} catch (NotFoundException e) {
			logger.error(e.getLocalizedMessage());
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			throw new InternalServerException(
					messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_GROUP_DELETE_FAILED));
		}
		if (entry.isGroupData()) {
			List<GroupDataEntry> contacts = this.groupDataEntryRepository.findByGroupId(entry.getId());
			if (!contacts.isEmpty()) {
				this.groupDataEntryRepository.deleteAll(contacts);

			} else {
				logger.info(messageResourceBundle.getLogMessage("addbook.groupdata.no.entry.found.info"));
			}
		} else {
			List<ContactEntry> contacts = this.contactRepo.findByGroupId(entry.getId());
			if (!contacts.isEmpty()) {
				this.contactRepo.deleteAll(contacts);

			} else {
				logger.info(messageResourceBundle.getLogMessage("addbook.contact.no.entry.found.info"));
			}
		}
	}

	/**
	 * Removes contact group entries based on the provided form data and username.
	 */
	@Override
	@Transactional
	public ResponseEntity<?> modifyGroupEntryDelete(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

		String target = IConstants.FAILURE_KEY;

		String systemId = user.getSystemId();
		logger.info(messageResourceBundle.getLogMessage("addbook.group.remove.contact.request.info"), systemId,
				user.getRole());
		deleteGroup(id);
		target = IConstants.SUCCESS_KEY;
		return ResponseEntity.ok(target);

	}

	/**
	 * Retrieves a list of GroupEntryDTO objects based on the provided criteria.
	 * 
	 * @param masterid
	 * @param groupData
	 * @return
	 */
	private Page<GroupEntryDTO> listGroupByCriteria(String masterid, boolean groupData ,Pageable pageable) {
		Page<GroupEntryDTO> list = null;
		try {
			list = this.groupEntryDTORepository.findByMasterIdAndGroupData(masterid, groupData,pageable);
		} catch (Exception e) {
			logger.error("Error: " + e.getLocalizedMessage());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		if (list != null && !list.isEmpty()) {
			for (GroupEntryDTO entry : list) {
				if (entry.getName() != null && entry.getName().length() > 0) {
					entry.setName(Converters.hexCodePointsToCharMsg(entry.getName()));
				}
				long count = 0;
				if (entry.isGroupData()) {
					count = this.groupDataEntryRepository.countByGroupId(entry.getId());
				} else {
					count = this.contactRepo.countByGroupId(entry.getId());
				}
				entry.setMembers(count);
			}
		}

		return list;
	}

	/**
	 * Retrieves a list of GroupEntryDTO objects based on the provided master ID.
	 * 
	 * @param masterid
	 * @return
	 */
	private Page<GroupEntryDTO> listGroupByCriteria(String masterid,Pageable pageable) {
		Page<GroupEntryDTO> list = null;
		try {
			list = this.groupEntryDTORepository.findByMasterId(masterid,pageable);
		} catch (Exception e) {
			logger.error("Error: " + e.getLocalizedMessage());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		if (list != null && !list.isEmpty()) {
			for (GroupEntryDTO entry : list) {
				if (entry.getName() != null && entry.getName().length() > 0) {
					entry.setName(Converters.hexCodePointsToCharMsg(entry.getName()));
				}
				long count = 0;
				if (entry.isGroupData()) {
					count = this.groupDataEntryRepository.countByGroupId(entry.getId());
				} else {
					count = this.contactRepo.countByGroupId(entry.getId());
				}
				entry.setMembers(count);
			}
		}

		return list;
	}

	/**
	 * Retrieves a list of contact groups based on the specified criteria and user
	 * roles.
	 */
	@Override
	public ResponseEntity<?> listGroup(String purpose, String groupData, String username,Pageable pageable) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		String target = IConstants.FAILURE_KEY;

		WebMenuAccessEntry webEntry = null;
		Optional<WebMenuAccessEntry> webMenu = this.webMenuRepo.findById(user.getId());
		if (!webMenu.isPresent()) {
			logger.error(messageResourceBundle.getLogMessage("addbook.webmaster.entry.notfound.error"),
					user.getSystemId());
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND_WEBMASTER_ERROR));
		}
		webEntry = webMenu.get();
		String systemId = user.getSystemId();
		boolean proceed = true;
		ListGroupResponse response = new ListGroupResponse();
		try {
			if (purpose != null) {
				if (purpose.equalsIgnoreCase("sms")) {
					if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")
							|| webEntry.isMessaging()) {
					} else {
						logger.error(messageResourceBundle.getLogMessage("addbook.group.invalid.request.error"),
								systemId, user.getRole());
						target = "invalidRequest";
						proceed = false;
						throw new UnauthorizedException(messageResourceBundle
								.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
					}
				} else {
					if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")
							|| webEntry.isAddbook()) {
					} else {
						logger.error(messageResourceBundle.getLogMessage("addbook.group.invalid.request.error"),
								systemId, user.getRole());
						target = "invalidRequest";
						proceed = false;
						throw new UnauthorizedException(messageResourceBundle
								.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
					}
				}
			} else {
				if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem") || webEntry.isAddbook()) {
				} else {
					logger.error(messageResourceBundle.getLogMessage("addbook.group.invalid.request.error"), systemId,
							user.getRole());
					target = "invalidRequest";
					proceed = false;
					throw new UnauthorizedException(messageResourceBundle
							.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
				}
			}
			Page<GroupEntryDTO> list = null;
			List<GroupEntryDTO> content = null;
			
			if (proceed) {

				if (groupData != null) {
					if (groupData.equalsIgnoreCase("yes")) {
						list = listGroupByCriteria(systemId, true,pageable);
						content=list.getContent();
						if (purpose != null && purpose.equalsIgnoreCase("sms")) {
							response.setCriteria("yes");
						}
						target = "GroupData";
					} else {
						list = listGroupByCriteria(systemId, false,pageable);
						content=list.getContent();
						if (purpose != null && purpose.equalsIgnoreCase("sms")) {
							target = "ContactSms";
						} else {
							target = "Contact";
						}
					}
				} else {
					list = listGroupByCriteria(systemId,pageable);
					content=list.getContent();
					if (purpose.equalsIgnoreCase("add")) {
						target = "AddGroup";
					} else {
						target = "ViewGroup";
					}
				}

				if (list != null && !list.isEmpty()) {
					content=list.getContent();
					response.setList(content);
					response.setPageNumber(list.getNumber());
					response.setPageSize(list.getSize());
				} else {
					logger.info(messageResourceBundle.getLogMessage("addbook.no.record.found.error"));
					throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_NORECORD));
				}
			}
			response.setTarget(target);
		} catch (UnauthorizedException e) {
			throw new UnauthorizedException(e.getLocalizedMessage());
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_ERROR_MSG,
					new Object[] { e.getLocalizedMessage() }));
		}
		logger.info(messageResourceBundle.getLogMessage("addbook.group.setup.contacts.target.info"), target);

		return ResponseEntity.ok(response);
	}
}
