package com.hti.dao.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.SmscDAService;
import com.hti.smsc.dto.SmscEntry;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

public class SmscDAServiceImpl implements SmscDAService {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");

	public SmscDAServiceImpl() {
		GlobalCache.SmscEntries = GlobalVars.hazelInstance.getMap("smsc_entries");
		GlobalCache.SmscGroupMember = GlobalVars.hazelInstance.getMultiMap("smsc_group_member");
		// GlobalCache.SmscFlagStatus = GlobalVars.hazelInstance.getMap("smsc_flag_status");
		GlobalCache.SmscNameMapping = GlobalVars.hazelInstance.getMap("smsc_name_mapping");
		logger.info("Smsc Entries: " + GlobalCache.SmscEntries.size());
		logger.info("SmscGroupMember: " + GlobalCache.SmscGroupMember.values());
	}

	public void loadSmscFlagStatus() {
		Map<String, Properties> flags = GlobalVars.hazelInstance.getMap("smsc_flag_status");
		for (Map.Entry<String, Properties> entry : flags.entrySet()) {
			String flag = entry.getValue().getProperty("FLAG");
			if (flag.equalsIgnoreCase(FlagStatus.ACCOUNT_REFRESH)) {
				UpdateSmscConfiguration(entry.getKey());
			} else if (flag.equalsIgnoreCase(FlagStatus.ACCOUNT_REMOVED)) {
				removeSmscConfiguration(entry.getKey());
			}
		}
	}

	private void UpdateSmscConfiguration(String smsc) {
		logger.info("Updating Smsc Configuration: " + smsc);
		if (GlobalCache.SmscNameMapping.containsKey(smsc)) {
			int smsc_id = GlobalCache.SmscNameMapping.get(smsc);
			SmscEntry entry = GlobalCache.SmscEntries.get(smsc_id);
			if (entry.isExpireLongBroken()) {
				if (GlobalCache.LongBrokenExpireRoute.contains(entry.getName())) {
					logger.info(entry.getName() + " Already ExpireLongBroken Route.");
				} else {
					GlobalCache.LongBrokenExpireRoute.add(entry.getName());
					logger.info(entry.getName() + " Marked ExpireLongBroken Route.");
				}
			} else {
				if (GlobalCache.LongBrokenExpireRoute.contains(entry.getName())) {
					GlobalCache.LongBrokenExpireRoute.remove(entry.getName());
					logger.info(entry.getName() + " Removed ExpireLongBroken Route.");
				}
			}
			if (entry.getSkipHlrSender() != null && entry.getSkipHlrSender().length() > 0) {
				Set<String> set = Stream.of(entry.getSkipHlrSender().toLowerCase().split(","))
						.collect(Collectors.toSet());
				logger.info(entry.getName() + " SkipHlrSenders:" + set);
				GlobalCache.SkipHlrSenderRoute.put(entry.getName(), set);
			} else {
				if (GlobalCache.SkipHlrSenderRoute.containsKey(entry.getName())) {
					GlobalCache.SkipHlrSenderRoute.remove(entry.getName());
				}
			}
			if (entry.isReplaceContent()) {
				String replaceContent = entry.getReplaceContentText();
				if (replaceContent != null && replaceContent.length() > 0) {
					Map<String, String> content_key_value = new HashMap<String, String>();
					String[] tokens = replaceContent.split(",");
					try {
						for (String part : tokens) {
							if (part.contains("|")) {
								String key = part.substring(0, part.indexOf("|"));
								String value = part.substring(part.indexOf("|") + 1, part.length());
								content_key_value.put(key, value);
							}
						}
					} catch (Exception e) {
						logger.error(entry.getName() + "[" + replaceContent + "]", e.fillInStackTrace());
					}
					if (!content_key_value.isEmpty()) {
						// logger.info(entry.getName() + ":" + content_key_value);
						GlobalCache.SmscBasedReplacement.put(entry.getName(), content_key_value);
					}
				}
				logger.info(entry.getName() + ": " + GlobalCache.SmscBasedReplacement.get(entry.getName()));
			} else {
				if (GlobalCache.SmscBasedReplacement.containsKey(entry.getName())) {
					GlobalCache.SmscBasedReplacement.remove(entry.getName());
					logger.info(entry.getName() + " Removed SmscBasedReplacement Route.");
				}
			}
		} else {
			logger.error("Smsc Entry Not Found: " + smsc);
		}
	}

	private void removeSmscConfiguration(String smsc) {
		logger.info("Removing Smsc Configuration: " + smsc);
		GlobalCache.LongBrokenExpireRoute.remove(smsc);
		GlobalCache.SmscBasedReplacement.remove(smsc);
		if (GlobalCache.SkipHlrSenderRoute.containsKey(smsc)) {
			GlobalCache.SkipHlrSenderRoute.remove(smsc);
		}
	}

	@Override
	public void loadSmscConfiguration() {
		logger.info("Loading Smsc Configuration");
		for (SmscEntry entry : GlobalCache.SmscEntries.values()) {
			if (entry.isExpireLongBroken()) {
				GlobalCache.LongBrokenExpireRoute.add(entry.getName());
			}
			if (entry.getSkipHlrSender() != null && entry.getSkipHlrSender().length() > 0) {
				Set<String> set = Stream.of(entry.getSkipHlrSender().toLowerCase().split(","))
						.collect(Collectors.toSet());
				logger.info(entry.getName() + " SkipHlrSenders:" + set);
				GlobalCache.SkipHlrSenderRoute.put(entry.getName(), set);
			}
			if (entry.isReplaceContent()) {
				String replaceContent = entry.getReplaceContentText();
				if (replaceContent != null && replaceContent.length() > 0) {
					Map<String, String> content_key_value = new HashMap<String, String>();
					String[] tokens = replaceContent.split(",");
					try {
						for (String part : tokens) {
							if (part.contains("|")) {
								String key = part.substring(0, part.indexOf("|"));
								String value = part.substring(part.indexOf("|") + 1, part.length());
								content_key_value.put(key, value);
							}
						}
					} catch (Exception e) {
						logger.error(entry.getName() + "[" + replaceContent + "]", e.fillInStackTrace());
					}
					if (!content_key_value.isEmpty()) {
						logger.info(entry.getName() + ":" + content_key_value);
						GlobalCache.SmscBasedReplacement.put(entry.getName(), content_key_value);
					}
				}
			}
		}
		logger.info("LongBrokenExpireRoute: " + GlobalCache.LongBrokenExpireRoute);
		logger.info("SmscBasedReplacement: " + GlobalCache.SmscBasedReplacement);
	}
}
