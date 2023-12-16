package com.hti.smpp.common.addressbook.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import com.hti.smpp.common.login.dto.Role;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.user.dto.MultiUserEntry;
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
public class GroupEntryServiceImpl implements GroupEntryService {

	private static final Logger logger = LoggerFactory.getLogger(GroupEntryServiceImpl.class.getName());

	@Autowired
	private GroupEntryDTORepository groupEntryDTORepository;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private WebMasterEntryRepository webMasterRepo;

	@Autowired
	private UserRepository userLoginRepo;

	@Autowired
	private GroupDataEntryRepository groupDataEntryRepository;

	@Autowired
	private ContactRepository contactRepo;

	@Autowired
	private WebMenuAccessEntryRepository webMenuRepo;

	@Autowired
	private MultiUserEntryRepository multiUserEntryRepository;

	// WIP TODO
	@Override
	@Transactional
	public ResponseEntity<?> saveGroupEntry(GroupEntryRequest form, String username) {

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}

		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		User getUser = null;
		if (user.isPresent()) {
			getUser = user.get();
		} else {
			throw new NotFoundException("User not found.");
		}
		Set<Role> role = user.get().getRoles();

		logger.info(systemId + "[" + role + "]" + " Add Contact Group Request");
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
					MultiUserEntry multiUserEntry = multiUserEntryRepository
							.findByUserId(getUser.getUserId().intValue());
					WebMasterEntry webEntry = this.webMasterRepo.findByUserId(getUser.getUserId().intValue());

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
					logger.info("[" + systemId + "]" + " Invalid Group Name: " + names[i]);
					continue;
				}
			}
			if (list.isEmpty()) {
				logger.info("[" + systemId + "]" + " No Valid Entry Found! ");
				return new ResponseEntity<>(target, HttpStatus.NO_CONTENT);
			} else {
				this.groupEntryDTORepository.saveAll(list);
				target = IConstants.SUCCESS_KEY;
				logger.info(systemId + " Add Contact Group Target:" + target);
				return new ResponseEntity<>(target, HttpStatus.CREATED);
			}
		} catch (Exception e) {
			logger.error(systemId, e.getLocalizedMessage());
			return new ResponseEntity<>(target, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@Transactional
	public ResponseEntity<?> modifyGroupEntryUpdate(GroupEntryRequest form, String username) {
		String target = IConstants.FAILURE_KEY;

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}

		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found.");
		}

		GroupEntryDTO entry = null;
		logger.info(systemId + "[" + role + "]" + " Modify Contact Group Request");
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
						logger.info(systemId + "[" + role + "]" + " Invalid Group Name: " + names[i]);
						continue;
					}
				}

				if (!list.isEmpty()) {
					this.groupEntryDTORepository.saveAll(list);
					target = IConstants.SUCCESS_KEY;
				}

			} catch (Exception e) {
				logger.error(systemId, e.toString());
				return new ResponseEntity<>(target, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.info(systemId + " No Records Selected");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		logger.info(systemId + " modify Contact Group Target:" + target);
		return new ResponseEntity<>(target, HttpStatus.CREATED);
	}

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
				}
			}
		}
	}

	@Override
	@Transactional
	public ResponseEntity<?> modifyGroupEntryDelete(GroupEntryRequest form, String username) {
		String target = IConstants.FAILURE_KEY;

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}

		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found.");
		}

		GroupEntryDTO entry = null;
		logger.info(systemId + "[" + role + "]" + " Remove Contact Group Request");
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
				}

			} catch (Exception e) {
				logger.error(systemId, e.toString());
				return new ResponseEntity<>(target, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.info(systemId + " No Records Selected");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		logger.info(systemId + " Remove Contact Group Target:" + target);

		return new ResponseEntity<>(target, HttpStatus.OK);
	}

	private List<GroupEntryDTO> listGroupByCriteria(String masterid, boolean groupData) {
		List<GroupEntryDTO> list = this.groupEntryDTORepository.findByMasterIdAndGroupData(masterid, groupData);
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

	private List<GroupEntryDTO> listGroupByCriteria(String masterid) {
		List<GroupEntryDTO> list = this.groupEntryDTORepository.findByMasterId(masterid);
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

	@Override
	public ResponseEntity<?> listGroup(String purpose, String groupData, String username) {

		String target = IConstants.FAILURE_KEY;
		User user = null;
		Optional<User> optionalUser = userLoginRepo.findBySystemId(username);
		if (optionalUser.isPresent()) {
			user = optionalUser.get();

		} else {
			logger.error("Error: Unable to found user with username: " + username);
			throw new NotFoundException("User not found with the provided username.");
		}
		Long id = user.getUserId();

		WebMenuAccessEntry webEntry = null;
		Optional<WebMenuAccessEntry> webMenu = this.webMenuRepo.findById((int) id.longValue());
		if (webMenu.isPresent()) {
			webEntry = webMenu.get();

		} else {
			logger.error("Error: Unable to found WebMEnuAccessEntry with userId: " + id);
			throw new NotFoundException("Unable to found WebMEnuAccessEntry.");
		}

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}

		logger.info(systemId + " Setup Contacts Group Request");
		boolean proceed = true;
		ListGroupResponse response = new ListGroupResponse();
		try {

			if (purpose != null) {
				if (purpose.equalsIgnoreCase("sms")) {
					if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles()) || webEntry.isMessaging()) {
					} else {
						logger.error(systemId + "[" + user.getRoles() + "]" + " <- Invalid Request ->");
						target = "invalidRequest";
						proceed = false;
					}
				} else {
					if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles()) || webEntry.isAddbook()) {
					} else {
						logger.error(systemId + "[" + user.getRoles() + "]" + " <- Invalid Request ->");
						target = "invalidRequest";
						proceed = false;
					}
				}
			} else {
				if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles()) || webEntry.isAddbook()) {
				} else {
					logger.error(systemId + "[" + user.getRoles() + "]" + " <- Invalid Request ->");
					target = "invalidRequest";
					proceed = false;
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
				}
			}
			response.setTarget(target);
		} catch (Exception e) {
			logger.error(systemId, e.toString());
			return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		logger.info("Setup Contacts Group Target: " + target);

		return ResponseEntity.ok(response);
	}

}