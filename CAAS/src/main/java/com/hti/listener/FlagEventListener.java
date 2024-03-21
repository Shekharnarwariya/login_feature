package com.hti.listener;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import com.hti.service.CacheService;
import com.hti.util.Constants;
import com.hti.util.FileUtil;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalVar;

public class FlagEventListener implements MessageListener<Map<String, String>> {
	private Logger logger = LoggerFactory.getLogger(FlagEventListener.class);

	@Override
	public void onMessage(Message<Map<String, String>> message) {
		if (message.getPublishingMember() != null && !message.getPublishingMember().localMember()) {
			logger.info("Flag Changed: " + message.getMessageObject().toString());
			String FLAG_VALUE = null;
			if (message.getMessageObject().containsKey("CLIENT_FLAG")) {
				FLAG_VALUE = message.getMessageObject().get("CLIENT_FLAG");
				if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
					for (Entry<String, String> map_entry : GlobalVar.user_flag_status.entrySet()) {
						String system_id = map_entry.getKey();
						String flag = map_entry.getValue();
						if (flag != null && flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
							FileUtil.setBlocked(Constants.USER_FLAG_DIR + system_id + ".txt");
						} else {
							FileUtil.setDefaultFlag(Constants.USER_FLAG_DIR + system_id + ".txt");
						}
					}
				}
			}
			if (message.getMessageObject().containsKey("SMSC_SH_FLAG")) {
				FLAG_VALUE = message.getMessageObject().get("SMSC_SH_FLAG");
				if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
					FileUtil.setDefaultFlag(Constants.SMSC_SH_FLAG_FILE);
					CacheService.loadSmscSchedule();
				}
			}
			if (message.getMessageObject().containsKey("SMSC_FLAG")) {
				FLAG_VALUE = message.getMessageObject().get("SMSC_FLAG");
				if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
					for (Entry<String, Properties> map_entry : GlobalVar.smsc_flag_status.entrySet()) {
						String smsc = map_entry.getKey();
						String flag = map_entry.getValue().getProperty("FLAG");
						if (flag != null && flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
							FileUtil.setSmscBlocked(Constants.SMSC_FLAG_DIR + smsc + ".txt");
						} else {
							FileUtil.setSmscDefault(Constants.SMSC_FLAG_DIR + smsc + ".txt");
						}
					}
				}
			}
		} else {
			logger.info("*** Local Member Published the topic ***");
		}
	}
}
