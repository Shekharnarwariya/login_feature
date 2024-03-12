package com.hti.listener;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import com.hti.thread.DLRLoader;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalVars;

public class FlagEventListener implements MessageListener<Map<String, String>> {
	private Logger logger = LoggerFactory.getLogger(FlagEventListener.class);

	@Override
	public void onMessage(Message<Map<String, String>> message) {
		logger.info("Flag Changed: " + message.getMessageObject().toString());
		String FLAG_VALUE = null;
		if (message.getMessageObject().containsKey("NETWORK_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("NETWORK_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				GlobalVars.RELOAD_NETWORK_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("SMSC_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("SMSC_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				GlobalVars.RELOAD_SMSC_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("BSFM_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("BSFM_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				GlobalVars.RELOAD_BSFM_CONFIG = true;
			}
		}
		
		if (message.getMessageObject().containsKey("USER_BSFM_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("USER_BSFM_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				GlobalVars.RELOAD_USER_BSFM_CONFIG = true;
			}
		}
		
		if (message.getMessageObject().containsKey("USER_LT_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("USER_LT_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				GlobalVars.RELOAD_USER_LIMIT_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("NETWORK_BSFM_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("NETWORK_BSFM_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				GlobalVars.RELOAD_NETWORK_BSFM_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("DLT_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("DLT_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				GlobalVars.RELOAD_DLT_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("CLIENT_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("CLIENT_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				GlobalVars.RELOAD_USER_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("TW_FILTER_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("TW_FILTER_FLAG");
			GlobalVars.RELOAD_TW_FILTER = true;
		}
		if (message.getMessageObject().containsKey("UNICODE_REPL_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("UNICODE_REPL_FLAG");
			GlobalVars.RELOAD_UNICODE_ENCODING = true;
		}
		if (message.getMessageObject().containsKey("RESEND_DLR_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("RESEND_DLR_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				new Thread(new DLRLoader(FlagStatus.REFRESH), "DLRLoader").start();
			} else if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.RESEND)) {
				new Thread(new DLRLoader(FlagStatus.RESEND), "DLRLoader").start();
			}
		}
		if (message.getMessageObject().containsKey("DGM_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("DGM_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				GlobalVars.RELOAD_DGM_CONFIG = true;
				if (!GlobalVars.DISTRIBUTION) {
					GlobalVars.DISTRIBUTION = true;
				}
			} else if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.BLOCKED)) {
				if (GlobalVars.DISTRIBUTION) {
					GlobalVars.DISTRIBUTION = false;
				}
			}
		}
	}
}
