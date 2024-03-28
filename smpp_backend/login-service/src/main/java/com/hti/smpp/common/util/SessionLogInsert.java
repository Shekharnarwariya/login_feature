package com.hti.smpp.common.util;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.user.dto.AccessLogEntry;
import com.hti.smpp.common.user.repository.AccessLogEntryRepository;
import com.logica.smpp.util.Queue;

@Service
public class SessionLogInsert implements Runnable {

	@Autowired
	private AccessLogEntryRepository accessLog;

	private Logger logger = org.slf4j.LoggerFactory.getLogger(SessionLogInsert.class);
	private boolean stop;
	public static Queue logQueue = new Queue();

	public SessionLogInsert() {
		logger.info("<-- SessionLogInsert Starting -->");
		new Thread(this, "SessionLogInsert").start();
	}

	@Override
	public void run() {
		while (!stop) {
			if (logQueue.isEmpty()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			} else {
				int counter = 0;
				while (!logQueue.isEmpty()) {
					//accessLog.save((AccessLogEntry) logQueue.dequeue());
					if (++counter > 100) {
						break;
					}
				}
			}
		}
		logger.info("<-- SessionLogInsert Stopped -->");
	}

	public void stop() {
		logger.info("<-- SessionLogInsert Stopping -->");
	}
}
