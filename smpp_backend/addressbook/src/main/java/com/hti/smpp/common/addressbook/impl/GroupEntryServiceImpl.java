package com.hti.smpp.common.addressbook.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.hti.smpp.common.util.IConstants;

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
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String systemId = user.getSystemId();

		logger.info(systemId + "[" + user.getRole() + "]" + " Add Contact Group Request");
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
					logger.info(entry.toString());
				} else {
					logger.warn("[" + systemId + "]" + " Invalid Group Name: " + names[i]);
					continue;
				}
			}
			if (list.isEmpty()) {
				logger.error("[" + systemId + "]" + " No Valid Entry Found! ");
				throw new NotFoundException("[" + systemId + "]" + " No Valid Entry Found! ");
			} else {
				this.groupEntryDTORepository.saveAll(list);
				target = IConstants.SUCCESS_KEY;
				logger.info(systemId + " Add Contact Group Target:" + target);
				return new ResponseEntity<>(target, HttpStatus.CREATED);
			}
		} catch (NotFoundException e) {
			logger.error(systemId, e.getLocalizedMessage());
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(systemId, e.getLocalizedMessage());
			throw new InternalServerException(e.getLocalizedMessage());
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
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;

		String systemId = user.getSystemId();
		GroupEntryDTO entry = null;
		logger.info(systemId + "[" + user.getRole() + "]" + " Modify Contact Group Request");
		List<GroupEntryDTO> list = new ArrayList<GroupEntryDTO>();
		int[] id = form.getId();
		String[] names = form.getName();
		boolean[] groupData = form.getGroupData();
		// ContactDAService service = new ContactDAServiceImpl();
		if (id != null && id.length > 0) {
			try {
				for (int i = 0; i < id.length; i++) {
					if (names[i] != null && names[i].length() > 0) {
						entry = new GroupEntryDTO(new Converters().UTF16(names[i]), systemId, groupData[i]);
						entry.setId(id[i]);
						list.add(entry);
						logger.info(entry.toString());
					} else {
						logger.info(systemId + "[" + user.getRole() + "]" + " Invalid Group Name: " + names[i]);
						continue;
					}
				}

				if (!list.isEmpty()) {
					this.groupEntryDTORepository.saveAll(list);
					target = IConstants.SUCCESS_KEY;
				} else {
					throw new InternalServerException("GroupEntry Save Unsuccessful Exception");
				}

			} catch (InternalServerException e) {
				logger.error(systemId, e.toString());
				throw new InternalServerException(e.getLocalizedMessage());
			} catch (Exception e) {
				logger.error(systemId, e.toString());
				throw new InternalServerException(e.getLocalizedMessage());
			}
		} else {
			logger.error(systemId + " No Records Selected");
			throw new NotFoundException(systemId + " No Records Selected");
		}
		logger.info(systemId + " modify Contact Group Target:" + target);
		return new ResponseEntity<>(target, HttpStatus.CREATED);
	}

	/**
	 * Deletes group entries and associated data from repositories.
	 * 
	 * @param list
	 */
	private void deleteGroup(List<GroupEntryDTO> list) {
		for (GroupEntryDTO entry : list) {
			try {
				this.groupEntryDTORepository.delete(entry);
			} catch (Exception e) {
				throw new InternalServerException("Unable to delete GroupEntry.");
			}
			if (entry.isGroupData()) {
				List<GroupDataEntry> contacts = this.groupDataEntryRepository.findByGroupId(entry.getId());
				if (!contacts.isEmpty()) {
					try {
						this.groupDataEntryRepository.deleteAll(contacts);
					} catch (Exception e) {
						throw new InternalServerException("Unable to delete GroupDataEntry.");
					}
				}
			} else {
				List<ContactEntry> contacts = this.contactRepo.findByGroupId(entry.getId());
				if (!contacts.isEmpty()) {
					try {
						this.contactRepo.deleteAll(contacts);
					} catch (Exception e) {
						throw new InternalServerException("Unable to delete ContactEntry.");
					}
				} else {
					throw new NotFoundException("Unable to find ContactEntry.");
				}
			}
		}
	}

	/**
	 * Removes contact group entries based on the provided form data and username.
	 */
	@Override
	@Transactional
	public ResponseEntity<?> modifyGroupEntryDelete(GroupEntryRequest form, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = IConstants.FAILURE_KEY;

		String systemId = user.getSystemId();

		GroupEntryDTO entry = null;
		logger.info(systemId + "[" + user.getRole() + "]" + " Remove Contact Group Request");
		List<GroupEntryDTO> list = new ArrayList<GroupEntryDTO>();
		int[] id = form.getId();
		String[] names = form.getName();
		boolean[] groupData = form.getGroupData();

		if (id != null && id.length > 0) {
			try {
				for (int i = 0; i < id.length; i++) {
					entry = new GroupEntryDTO(names[i], systemId, groupData[i]);
					entry.setId(id[i]);
					list.add(entry);
					logger.info(entry.toString());
				}

				if (!list.isEmpty()) {
					deleteGroup(list);
					target = IConstants.SUCCESS_KEY;
				} else {
					throw new InternalServerException("Unable to find Entries. List is empty!");
				}

			} catch (InternalServerException e) {
				logger.error(systemId, e.toString());
				throw new InternalServerException(e.getLocalizedMessage());
			} catch (Exception e) {
				logger.error(systemId, e.toString());
				throw new InternalServerException(e.getLocalizedMessage());
			}
		} else {
			logger.info(systemId + " No Records Selected");
			throw new NotFoundException(systemId + " No Records Selected");
		}
		logger.info(systemId + " Remove Contact Group Target:" + target);

		return new ResponseEntity<>(target, HttpStatus.OK);
	}

	/**
	 * Retrieves a list of GroupEntryDTO objects based on the provided criteria.
	 * 
	 * @param masterid
	 * @param groupData
	 * @return
	 */
	private List<GroupEntryDTO> listGroupByCriteria(String masterid, boolean groupData) {
		List<GroupEntryDTO> list = null;
		try {
			list = this.groupEntryDTORepository.findByMasterIdAndGroupData(masterid, groupData);
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
	private List<GroupEntryDTO> listGroupByCriteria(String masterid) {
		List<GroupEntryDTO> list = null;
		try {
			list = this.groupEntryDTORepository.findByMasterId(masterid);
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
	public ResponseEntity<?> listGroup(String purpose, String groupData, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = IConstants.FAILURE_KEY;

		WebMenuAccessEntry webEntry = null;
		Optional<WebMenuAccessEntry> webMenu = this.webMenuRepo.findById(user.getId());
		if (webMenu.isPresent()) {
			webEntry = webMenu.get();

		} else {
			logger.error("Error: Unable to found WebMenuAccessEntry with userId: " + user.getId());
			throw new NotFoundException("Unable to found WebMenuAccessEntry.");
		}

		String systemId = user.getSystemId();

		logger.info(systemId + " Setup Contacts Group Request");
		boolean proceed = true;
		ListGroupResponse response = new ListGroupResponse();
		try {

			if (purpose != null) {
				if (purpose.equalsIgnoreCase("sms")) {
					if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")
							|| webEntry.isMessaging()) {
					} else {
						logger.error(systemId + "[" + user.getRole() + "]" + " <- Invalid Request ->");
						target = "invalidRequest";
						proceed = false;
						throw new UnauthorizedException("User does not have the required roles for this operation.");
					}
				} else {
					if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")
							|| webEntry.isAddbook()) {
					} else {
						logger.error(systemId + "[" + user.getRole() + "]" + " <- Invalid Request ->");
						target = "invalidRequest";
						proceed = false;
						throw new UnauthorizedException("User does not have the required roles for this operation.");
					}
				}
			} else {
				if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem") || webEntry.isAddbook()) {
				} else {
					logger.error(systemId + "[" + user.getRole() + "]" + " <- Invalid Request ->");
					target = "invalidRequest";
					proceed = false;
					throw new UnauthorizedException("User does not have the required roles for this operation.");
				}
			}
			List<GroupEntryDTO> list = null;
			if (proceed) {

				if (groupData != null) {
					if (groupData.equalsIgnoreCase("yes")) {
						list = listGroupByCriteria(systemId, true);
						if (purpose != null && purpose.equalsIgnoreCase("sms")) {
							response.setCriteria("yes");
						}
						target = "GroupData";
					} else {
						list = listGroupByCriteria(systemId, false);
						if (purpose != null && purpose.equalsIgnoreCase("sms")) {
							target = "ContactSms";
						} else {
							target = "Contact";
						}
					}
				} else {
					list = listGroupByCriteria(systemId);
					if (purpose.equalsIgnoreCase("add")) {
						target = "AddGroup";
					} else {
						target = "ViewGroup";
					}
				}

				if (list != null && !list.isEmpty()) {
					response.setList(list);
				} else {
					logger.info(systemId + " No Contact Groups Found.");
					throw new NotFoundException("No Contact Groups Found");
				}
			}
			response.setTarget(target);
		} catch (UnauthorizedException e) {
			logger.error(systemId, e.toString());
			throw new UnauthorizedException(e.getLocalizedMessage());
		} catch (NotFoundException e) {
			logger.error(systemId, e.toString());
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(systemId, e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		logger.info("Setup Contacts Group Target: " + target);

		return ResponseEntity.ok(response);
	}
}
