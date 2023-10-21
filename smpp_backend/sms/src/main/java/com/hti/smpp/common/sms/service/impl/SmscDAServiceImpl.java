package com.hti.smpp.common.sms.service.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.repository.GroupEntryRepository;
import com.hti.smpp.common.sms.service.SmscDAService;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.repository.SmscEntryRepository;

public class SmscDAServiceImpl implements SmscDAService {
	private Logger logger = LoggerFactory.getLogger(SmscDAServiceImpl.class);

	@Autowired
	private GroupEntryRepository groupEntryRepository;

	@Autowired
	private SmscEntryRepository SmscEntryRepository;

	@Override
	public Map<Integer, String> listGroupNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		names.put(0, "NONE");
		List<GroupEntry> groups = groupEntryRepository.findAll();
		for (GroupEntry entry : groups) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}

	@Override
	public Map<Integer, String> listNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();

		List<SmscEntry> smscEntry = SmscEntryRepository.findAll();

		for (SmscEntry entry : smscEntry) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}

}
