package com.hti.smpp.common.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.dto.GroupMemberEntry;
import com.hti.smpp.common.contacts.repository.GroupEntryRepository;
import com.hti.smpp.common.contacts.repository.GroupMemberEntryRepository;
import com.hti.smpp.common.request.CustomRequest;
import com.hti.smpp.common.request.GroupMemberRequest;
import com.hti.smpp.common.request.GroupRequest;
import com.hti.smpp.common.request.SmscEntryRequest;
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

	@Override
	public String save(SmscEntryRequest smscEntryRequest, String username) {
		System.out.println("Username: " + username);
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		SmscEntry convertedRequest = ConvertRequest(smscEntryRequest);

		if (userOptional.isPresent()) {
			UserEntry userEntry = userOptional.get();
			convertedRequest.setSystemId(String.valueOf(userEntry.getSystemId()));
			convertedRequest.setSystemType(userEntry.getSystemType());
			convertedRequest.setMasterId(userEntry.getMasterId());
		}

		try {
			SmscEntry savedEntry = smscEntryRepository.save(convertedRequest);
			return "successfully  save this id :{}" + savedEntry.getId();
		} catch (Exception e) {
			logger.error("An error occurred while saving the SmscEntry: {}", e.getMessage());
			throw new RuntimeException("Failed to save SmscEntry", e);
		}
	}

	@Override
	public void update(SmscEntry entry) {
		try {
			smscEntryRepository.save(entry);
		} catch (Exception e) {
			logger.error("An error occurred during the update operation: " + e.getMessage(), e);
			throw new RuntimeException("Failed to update SmscEntry: " + e.getMessage());
		}
	}

	@Override
	public void delete(SmscEntry entry) {
		try {
			smscEntryRepository.delete(entry);
		} catch (Exception e) {
			logger.error("An error occurred during the delete operation: " + e.getMessage(), e);
			throw new RuntimeException("Failed to delete SmscEntry: " + e.getMessage());
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

	
	@Override
	public List<StatusEntry> listBound(boolean bound) {
		try {
			List<StatusEntry> list;
			if (bound) {
				list = statusEntryRepository.findByBound(bound);
			} else {
				list = statusEntryRepository.findAll();
			}
			return list;
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to list Smsc Status Records", e);
		}
	}

	@Override
	public List<CustomEntry> listCustom() {
		try {
			List<CustomEntry> list = customEntryRepository.findAll();
			return list;
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to list Smsc Custom Entry Records", e);
		}
	}

	@Override

	public CustomEntry getCustomEntry(int smscId) {
		try {
			Optional<CustomEntry> optionalEntry = customEntryRepository.findById(smscId);
			if (optionalEntry.isPresent()) {
				CustomEntry entry = optionalEntry.get();
				return entry;
			} else {
				throw new Exception("Smsc CustomEntry with ID " + smscId + " not found");
			}
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to retrieve Smsc CustomEntry with ID: " + smscId, e);
		}
	}

	@Override
	public String saveCustom(CustomRequest customRequest) {
		try {
			CustomEntry convertedRequest = ConvertRequest(customRequest);
			customEntryRepository.save(convertedRequest);
			logger.info("CustomEntry saved successfully");
			return "successfully saved....";
		} catch (Exception e) {
			logger.error("An error occurred while saving the CustomEntry: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save CustomEntry", e);
		}
	}

	@Override
	public void updateCustom(CustomEntry entry) {
		try {
			customEntryRepository.save(entry);
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to update CustomEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public void deleteCustom(CustomEntry entry) {
		try {
			customEntryRepository.delete(entry);
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to delete CustomEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public void saveLimit(LimitEntry entry) {
		try {
			limitEntryRepository.save(entry);
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to save LimitEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public void updateLimit(List<LimitEntry> list) {
		try {
			int i = 0;
			for (LimitEntry entry : list) {
				limitEntryRepository.save(entry);
				if (++i % 10 == 0) {
					// flush a batch of inserts and release memory:
					limitEntryRepository.flush();
					entityManager.clear();
				}
			}
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to update LimitEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public void deleteLimit(List<LimitEntry> list) {
		try {
			int i = 0;
			for (LimitEntry entry : list) {
				limitEntryRepository.delete(entry);
				if (++i % 10 == 0) {
					// flush a batch of deletes and release memory:
					limitEntryRepository.flush();
					entityManager.clear();
				}
			}
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to delete LimitEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public List<LimitEntry> listLimit() {
		try {
			List<LimitEntry> list = limitEntryRepository.findAll();
			return list;
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to list Smsc limit Entries: " + e.getMessage(), e);
		}
	}

	@Override
	public String saveGroup(GroupRequest groupRequest) {
		try {
			int i = 0;
			List<GroupEntry> convertedRequest = ConvertRequest(groupRequest);

			groupEntryRepository.saveAll(convertedRequest);
			if (++i % 10 == 0) {
				groupEntryRepository.flush();
				entityManager.clear();
			}
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to save GroupEntry: " + e.getMessage(), e);
		}
		return "save successfully ...";
	}

	@Override
	public void updateGroup(List<GroupEntry> list) {
		try {
			int i = 0;
			for (GroupEntry entry : list) {
				groupEntryRepository.save(entry);
				if (++i % 10 == 0) {
					// flush a batch of inserts and release memory:
					groupEntryRepository.flush();
					entityManager.clear();
				}
			}
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to update GroupEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public void deleteGroup(GroupEntry entry) {
		try {
			groupEntryRepository.delete(entry);
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to delete GroupEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public List<GroupEntry> listGroup() {
		try {
			List<GroupEntry> list = groupEntryRepository.findAll();
			return list;
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to list Group Entries: " + e.getMessage(), e);
		}
	}

	@Override
	public String saveGroupMember(GroupMemberRequest groupMemberRequest) {
		try {
			List<GroupMemberEntry> convertRequest = ConvertRequest(groupMemberRequest);
			int batchSize = 10;
			int count = 0;

			for (GroupMemberEntry entry : convertRequest) {
				groupMemberEntryRepository.save(entry);
				if (++count % batchSize == 0) {
					groupMemberEntryRepository.flush();
					entityManager.clear();
				}
			}
			return "Group members saved successfully.";
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to save GroupMemberEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public void updateGroupMember(List<GroupMemberEntry> list) {
		try {
			int i = 0;
			for (GroupMemberEntry entry : list) {
				groupMemberEntryRepository.save(entry);
				if (++i % 10 == 0) {
					// flush a batch of inserts and release memory:
					groupMemberEntryRepository.flush();
					entityManager.clear();
				}
			}
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to update GroupMemberEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public void deleteGroupMember(Collection<GroupMemberEntry> list) {
		try {
			int i = 0;
			for (GroupMemberEntry entry : list) {
				groupMemberEntryRepository.delete(entry);
				if (++i % 10 == 0) {
					// flush a batch of deletes and release memory:
					groupMemberEntryRepository.flush();
					entityManager.clear();
				}
			}
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to delete GroupMemberEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public List<GroupMemberEntry> listGroupMember(int groupId) {
		try {
			List<GroupMemberEntry> list = groupMemberEntryRepository.findByGroupId(groupId);
			return list;
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to list Group Member Entries: " + e.getMessage(), e);
		}
	}

	@Override
	public void saveSchedule(List<TrafficScheduleEntry> list) {
		try {
			int i = 0;
			for (TrafficScheduleEntry entry : list) {
				trafficScheduleEntryRepository.save(entry);
				if (++i % 10 == 0) {
					// flush a batch of inserts and release memory:
					trafficScheduleEntryRepository.flush();
					entityManager.clear();
				}
			}
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to save TrafficScheduleEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public void updateSchedule(List<TrafficScheduleEntry> list) {
		try {
			int i = 0;
			for (TrafficScheduleEntry entry : list) {
				trafficScheduleEntryRepository.save(entry);
				if (++i % 10 == 0) {
					// flush a batch of inserts and release memory:
					trafficScheduleEntryRepository.flush();
					entityManager.clear();
				}
			}
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to update TrafficScheduleEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public void deleteSchedule(List<TrafficScheduleEntry> list) {
		try {
			int i = 0;
			for (TrafficScheduleEntry entry : list) {
				trafficScheduleEntryRepository.delete(entry);
				if (++i % 10 == 0) {
					// flush a batch of deletes and release memory:
					trafficScheduleEntryRepository.flush();
					entityManager.clear();
				}
			}
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to delete TrafficScheduleEntry: " + e.getMessage(), e);
		}
	}

	@Override
	public List<TrafficScheduleEntry> listSchedule() {
		try {
			List<TrafficScheduleEntry> list = trafficScheduleEntryRepository.findAll();
			return list;
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to list TrafficScheduleEntries: " + e.getMessage(), e);
		}
	}

	@Override
	public void saveLoopingRule(SmscLooping entry) {
		try {
			smscLoopingRepository.save(entry);
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to save SmscLooping entry: " + e.getMessage(), e);
		}
	}

	@Override
	public void updateLoopingRule(SmscLooping entry) {
		try {
			smscLoopingRepository.save(entry);
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to update SmscLooping entry: " + e.getMessage(), e);
		}
	}

	@Override
	public void deleteLoopingRule(SmscLooping entry) {
		try {
			smscLoopingRepository.delete(entry);
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to delete SmscLooping entry: " + e.getMessage(), e);
		}
	}

	@Override
	public SmscLooping getLoopingRule(int smscId) {
		try {
			return smscLoopingRepository.findBySmscId((long) smscId).orElse(null);
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to retrieve SmscLooping rule: " + e.getMessage(), e);
		}
	}

	@Override
	public List<SmscLooping> listLoopingRule() {
		try {
			return smscLoopingRepository.findAll();
		} catch (Exception e) {
			// Handle exceptions here
			throw new RuntimeException("Failed to list SmscLooping rules: " + e.getMessage(), e);
		}
	}
}
