package com.hti.smpp.common.httpclient;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.contacts.dto.ContactEntry;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;
import com.hti.smpp.common.contacts.dto.GroupEntryDTO;
import com.hti.smpp.common.util.Converter;

@Service
public class ContactService {

	@Autowired
	private ContactDAO contactDAO;;

	public Map<String, GroupEntryDTO> listGroupNames(String masterid) {
		Map<String, GroupEntryDTO> map = new java.util.HashMap<String, GroupEntryDTO>();
		List<GroupEntryDTO> list = listGroup(masterid);
		for (GroupEntryDTO entry : list) {
			map.put(entry.getName().toLowerCase(), entry);
		}
		return map;
	}

	public List<GroupEntryDTO> listGroup(String masterid) {
		List<GroupEntryDTO> list = contactDAO.listGroup(masterid);
		if (list != null && !list.isEmpty()) {
			for (GroupEntryDTO entry : list) {
				if (entry.getName() != null && entry.getName().length() > 0) {
					entry.setName(Converter.hexCodePointsToCharMsg(entry.getName()));
				}
				long count = 0;
				if (entry.isGroupData()) {
					count = contactDAO.countGroupData(entry.getId());
				} else {
					count = contactDAO.countContact(entry.getId());
				}
				entry.setMembers(count);
			}
		}
		return list;
	}

	public List<GroupDataEntry> listGroupData(int groupId, int start, int limit) {
		return contactDAO.listGroupData(groupId, start, limit);
	}

	public List<GroupDataEntry> listGroupData(int groupId) {
		return contactDAO.listGroupData(groupId);
	}

	public List<ContactEntry> listContact(int groupId, int start, int limit) {
		return contactDAO.listContact(groupId, start, limit);
	}

	public List<ContactEntry> listContact(int groupId) {
		return contactDAO.listContact(groupId);
	}

}
