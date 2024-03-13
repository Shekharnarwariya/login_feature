package com.hti.smpp.common.httpclient;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.contacts.dto.ContactEntry;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;
import com.hti.smpp.common.contacts.dto.GroupEntryDTO;
import com.hti.smpp.common.contacts.repository.ContactRepository;
import com.hti.smpp.common.contacts.repository.GroupDataEntryRepository;
import com.hti.smpp.common.contacts.repository.GroupEntryDTORepository;
import com.hti.smpp.common.exception.InternalServerException;

@Service
public class ContactDAO {

	private Logger logger = LoggerFactory.getLogger(ContactDAO.class);

	@Autowired
	private GroupEntryDTORepository groupEntryDTORepository;

	@Autowired
	private GroupDataEntryRepository groupDataEntryRepository;

	@Autowired
	private ContactRepository contactEntryRepository;

	public List<GroupEntryDTO> listGroup(String masterId) {
		logger.info("Listing Contacts Group for: {}", masterId);
		try {
			List<GroupEntryDTO> list = groupEntryDTORepository.findByMasterId(masterId);
			logger.info("{} GroupEntry list: {}", masterId, list.size());
			return list;
		} catch (Exception e) {
			logger.error("Failed to list GroupEntry for masterId: {}, due to: {}", masterId, e.getMessage());
			throw new InternalServerException(
					"Failed to list GroupEntry for masterId: " + masterId + e.getLocalizedMessage());
		}

	}

	public long countGroupData(int groupId) {
		logger.info("Checking GroupData Count For Group: {}", groupId);
		try {
			long count = groupDataEntryRepository.countByGroupId(groupId);
			logger.info("{} GroupData Count: {}", groupId, count);
			return count;
		} catch (Exception e) {
			logger.error("Failed to count GroupData for groupId: {}", groupId, e);
			return 0;
		}
	}

	public long countContact(int groupId) {
		logger.info("Checking Contact Count For Group: {}", groupId);
		try {
			long count = contactEntryRepository.countByGroupId(groupId);
			logger.info("{} Contact Count: {}", groupId, count);
			return count;
		} catch (Exception e) {
			logger.error("Failed to count contacts for group {}: ", groupId, e);
			return 0;
		}
	}

	public List<GroupDataEntry> listGroupData(int groupId, int start, int limit) {
		logger.info("Listing GroupData for Group: {} from={} limit={}", groupId, start, limit);
		List<GroupDataEntry> list = null;
		try {
			PageRequest pageRequest = PageRequest.of(start / limit, limit, Sort.by("id").ascending());
			list = groupDataEntryRepository.findByGroupId(groupId, pageRequest);
			logger.info("{} GroupData list: {}", groupId, list.size());
		} catch (Exception e) {
			logger.error("Failed to Listing GroupData forr group {}: ", groupId, e);
			throw new InternalServerException("Failed to Listing GroupData forr group" + groupId);
		}
		return list;
	}

	public List<GroupDataEntry> listGroupData(int groupId) {
		logger.info("Listing all GroupData for Group: {}", groupId);
		List<GroupDataEntry> list;
		try {
			list = groupDataEntryRepository.findByGroupId(groupId);
			logger.info("{} GroupData list: {}", groupId, list.size());
		} catch (Exception e) {
			logger.error("Failed to list all GroupData for group {}: ", groupId, e);
			throw new InternalServerException(
					"Failed to list all GroupData for group " + groupId + e.getLocalizedMessage());
		}
		return list;
	}

	public List<ContactEntry> listContact(int groupId, int start, int limit) {
		logger.info("Listing Contacts for Group: {} from={} limit={}", groupId, start, limit);
		try {
			PageRequest pageRequest = PageRequest.of(start / limit, limit);
			List<ContactEntry> list = contactEntryRepository.findByGroupId(groupId, pageRequest);
			logger.info("{} Contact list: {}", groupId, list.size());
			return list;
		} catch (Exception e) {
			logger.error("Failed to list Contacts for group {}: ", groupId, e);
			throw new InternalServerException("Failed to list Contacts for group " + groupId + e.getLocalizedMessage());
		}

	}

	public List<ContactEntry> listContact(int groupId) {
		logger.info("Listing Contacts for Group : {}", groupId);
		try {
			List<ContactEntry> list = contactEntryRepository.findByGroupId(groupId);
			logger.info("{} Contact list: {}", groupId, list.size());
			return list;
		} catch (Exception e) {
			logger.error("Failed to list Contacts for group {}: ", groupId, e);
			throw new InternalServerException("Failed to list Contacts for group " + groupId + e.getLocalizedMessage());
		}
	}

}
