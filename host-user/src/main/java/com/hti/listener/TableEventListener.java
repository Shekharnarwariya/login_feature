package com.hti.listener;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import com.hti.thread.SmscInLogThread;
import com.hti.util.GlobalVars;

public class TableEventListener implements MessageListener<Map<String, Boolean>> {
	private Logger logger = LoggerFactory.getLogger(TableEventListener.class);

	@Override
	public void onMessage(Message<Map<String, Boolean>> message) {
		logger.info("Table Lock Status: " + message.getMessageObject().toString());
		if (!GlobalVars.MASTER_CLIENT) {
			if (message.getMessageObject().containsKey("SMSC_IN_LOG")) {
				SmscInLogThread.TABLE_LOCKED = message.getMessageObject().get("SMSC_IN_LOG");
			}
		}
	}
}
