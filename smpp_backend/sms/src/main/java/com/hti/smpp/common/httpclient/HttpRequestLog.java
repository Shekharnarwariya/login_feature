package com.hti.smpp.common.httpclient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.logica.smpp.util.Queue;

@Service
public class HttpRequestLog implements Runnable {
	public static Queue logQueue = new Queue();
	private boolean isStop = false;
	@Autowired
	private IDatabaseService dbService;
	public static boolean clear = false;
	private Logger logger = LoggerFactory.getLogger(HttpRequestLog.class);

	public HttpRequestLog() {
		logger.info("<-- HttpRequestLog Starting --> ");
		new Thread(this, "HttpRequestLog").start();
	}

	@Override
	public void run() {
		while (!isStop) {
			if (clear) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, -3);
				int count = dbService.removeHttpRequestLog("delete from http_request_log where DATE(received_time) < '"
						+ new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()) + "' limit 25000");
				if (count < 25000) {
					clear = false;
				}
			}
			if (!logQueue.isEmpty()) {
				int counter = 0;
				List<HttpRequestEntry> list = new java.util.ArrayList<HttpRequestEntry>();
				while (!logQueue.isEmpty()) {
					list.add((HttpRequestEntry) logQueue.dequeue());
					try {
						dbService.saveHttpRequestLog(list);
					} catch (Exception e) {
						logger.error("", e);
					}
					if (++counter > 100) {
						break;
					}
				}
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
		logger.info("<-- HttpRequestLog Stopped. Queue: " + logQueue.size() + " ----> ");
	}

	public void stop() {
		logger.info("<-- HttpRequestLog Stopping. Queue: " + logQueue.size() + " ----> ");
		isStop = true;
	}
}
