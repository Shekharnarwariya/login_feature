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
import com.hti.smpp.common.addressbook.services.GroupEntryService;
import com.hti.smpp.common.addressbook.utils.Converters;
import com.hti.smpp.common.contacts.dto.GroupEntryDTO;
import com.hti.smpp.common.contacts.repository.GroupEntryDTORepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.IConstants;

@Service
public class GroupEntryServiceImpl implements GroupEntryService{
	
	private static final Logger logger = LoggerFactory.getLogger(GroupEntryServiceImpl.class.getName());
	
	@Autowired
	private GroupEntryDTORepository groupEntryDTORepository;
	
	@Autowired
	private UserEntryRepository userRepository;
	
	@Override
	public ResponseEntity<?> saveGroupEntry(GroupEntryRequest form, String username) {
		
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
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
					entry.setCreatedBy(systemId);
					list.add(entry);
					logger.info(entry.toString());
				} else {
//					logger.info(systemId + "[" + role + "]" + " Invalid Group Name: " + names[i]);
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
	
	

}
