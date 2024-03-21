package com.hti.listener;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;

public class TableEventListener implements MessageListener<Map<String, Boolean>> {
	private Logger logger = LoggerFactory.getLogger(TableEventListener.class);

	@Override
	public void onMessage(Message<Map<String, Boolean>> message) {
		logger.info("Table Lock Status: " + message.getMessageObject().toString());
	}
}
