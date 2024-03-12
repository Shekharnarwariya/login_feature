package com.hti.listener;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import com.hti.thread.AlertHandler;
import com.hti.thread.ClearNonResponding;
import com.hti.util.Constants;
import com.hti.util.FlagStatus;

public class FlagEventListener implements MessageListener<Map<String, String>> {
	private Logger logger = LoggerFactory.getLogger(FlagEventListener.class);

	@Override
	public void onMessage(Message<Map<String, String>> message) {
		logger.info("Flag Changed: " + message.getMessageObject().toString());
		String FLAG_VALUE = null;
		if (message.getMessageObject().containsKey("DGM_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("DGM_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				Constants.RELOAD_DGM_CONFIG = true;
				if (!Constants.DISTRIBUTION) {
					Constants.DISTRIBUTION = true;
				}
			} else if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.BLOCKED)) {
				if (Constants.DISTRIBUTION) {
					Constants.DISTRIBUTION = false;
				}
			}
		}
		if (message.getMessageObject().containsKey("SMSC_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("SMSC_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				Constants.RELOAD_SMSC_CONFIG = true;
			} else if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.CLEAR_NONRESP)) {
				logger.info("*** Got Command To Clear Non Responding for All Smsc ***");
				new Thread(new ClearNonResponding(), "ClearNonResponding").start();
			}
		}
		if (message.getMessageObject().containsKey("SMSC_LT_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("SMSC_LT_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				Constants.RELOAD_SMSC_LT_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("DLT_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("DLT_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				Constants.RELOAD_DLT_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("SMSC_LOOP_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("SMSC_LOOP_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				Constants.RELOAD_SMSC_LOOP_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("CLIENT_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("CLIENT_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				Constants.RELOAD_USER_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("SMSC_SPCL_SETTING")) {
			FLAG_VALUE = message.getMessageObject().get("SMSC_SPCL_SETTING");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				Constants.RELOAD_SMSC_SPCL_SETTING = true;
			}
		}
		if (message.getMessageObject().containsKey("SMSC_BSFM")) {
			FLAG_VALUE = message.getMessageObject().get("SMSC_BSFM");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				Constants.RELOAD_SMSC_BSFM = true;
			}
		}
		if (message.getMessageObject().containsKey("ESME_ERROR_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("ESME_ERROR_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				Constants.RELOAD_ESME_ERROR_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("SIGNAL_ERROR_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("SIGNAL_ERROR_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				Constants.RELOAD_SIGNAL_ERROR_CONFIG = true;
			}
		}
		if (message.getMessageObject().containsKey("SPCL_ENCODING_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("SPCL_ENCODING_FLAG");
			if (FLAG_VALUE.equalsIgnoreCase(FlagStatus.REFRESH)) {
				Constants.RELOAD_SPCL_ENCODING = true;
			}
		}
		if (message.getMessageObject().containsKey("PERFORM_ALERT_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("PERFORM_ALERT_FLAG");
			AlertHandler.PERFORM_ALERT_FLAG = FLAG_VALUE;
		}
		if (message.getMessageObject().containsKey("TW_FILTER_FLAG")) {
			FLAG_VALUE = message.getMessageObject().get("TW_FILTER_FLAG");
			Constants.RELOAD_TW_FILTER = true;
		}
	}
}
