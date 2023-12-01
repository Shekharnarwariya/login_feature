package com.hti.smpp.common.addressbook.impl;

import java.util.ArrayList;
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
import com.hti.smpp.common.addressbook.services.GroupEntryService;
import com.hti.smpp.common.addressbook.utils.Converters;
import com.hti.smpp.common.contacts.dto.ContactEntry;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;
import com.hti.smpp.common.contacts.dto.GroupEntryDTO;
import com.hti.smpp.common.contacts.repository.ContactRepository;
import com.hti.smpp.common.contacts.repository.GroupDataEntryRepository;
import com.hti.smpp.common.contacts.repository.GroupEntryDTORepository;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.login.dto.Role;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.IConstants;

@Service
public class GroupEntryServiceImpl implements GroupEntryService{
	
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
	
	//WIP TODO
	@Override
	public ResponseEntity<?> saveGroupEntry(GroupEntryRequest form, String username) {
		
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
		
		Optional<User> user = userLoginRepo.findBySystemId(systemId);
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
//					if (userSessionObject.getWebMasterEntry().isMultiUserAccess()
//							&& userSessionObject.getMultiUsername() != null
//							&& userSessionObject.getMultiUsername().length() > 0) {
//						entry.setCreatedBy(userSessionObject.getMultiUsername());
//					} else {
//						entry.setCreatedBy(systemId);
//					}
					// to review and implement like above code
					WebMasterEntry webEntry = this.webMasterRepo.findByUserId(Integer.parseInt(systemId));
					if(webEntry.isMultiUserAccess()) {
						//TODO
					}else {
						entry.setCreatedBy(systemId);
					}
					
					list.add(entry);
					logger.info(entry.toString());
				} else {
					logger.info("["+ systemId + "]" + " Invalid Group Name: " + names[i]);
					continue;
				}
			}
			if (list.isEmpty()) {
				logger.info("[" + systemId + "]" + " No Valid Entry Found! ");
				return new ResponseEntity<>(list, HttpStatus.NO_CONTENT);
			} else {
				this.groupEntryDTORepository.saveAll(list);
				target = IConstants.SUCCESS_KEY;
				logger.info(systemId + " Add Contact Group Target:" + target);
				return ResponseEntity.ok(list);
			}
		} catch (Exception e) {
			logger.error(systemId, e.getLocalizedMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
	}


	@Override
	public ResponseEntity<?> modifyGroupEntryUpdate(GroupEntryRequest form, String username) {
		String target = IConstants.FAILURE_KEY;
		
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
		
		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = user.get().getRoles();
		
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
				
				if(!list.isEmpty()) {
					this.groupEntryDTORepository.saveAll(list);
					target = IConstants.SUCCESS_KEY;
				}
				
				
			} catch (Exception e) {
				logger.error(systemId, e.toString());
				return new ResponseEntity<>(target,HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.info(systemId + " No Records Selected");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		logger.info(systemId + " modify Contact Group Target:" + target);
		return new ResponseEntity<>(target,HttpStatus.CREATED);
	}
	
	private void deleteGroup(List<GroupEntryDTO> list) {
		for (GroupEntryDTO entry : list) {
			try {
				this.groupEntryDTORepository.delete(entry);
			}catch(Exception e) {
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
			}else {
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
	public ResponseEntity<?> modifyGroupEntryDelete(GroupEntryRequest form, String username) {
		String target = IConstants.FAILURE_KEY;
		
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
		
		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = user.get().getRoles();
		
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
				
				if(!list.isEmpty()) {
					deleteGroup(list);
					target = IConstants.SUCCESS_KEY;
				}
				
			} catch (Exception e) {
				logger.error(systemId, e.toString());
				return new ResponseEntity<>(target,HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.info(systemId + " No Records Selected");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		logger.info(systemId + " Remove Contact Group Target:" + target);
		
		
		return new ResponseEntity<>(target,HttpStatus.OK);
	}
	
	

}
