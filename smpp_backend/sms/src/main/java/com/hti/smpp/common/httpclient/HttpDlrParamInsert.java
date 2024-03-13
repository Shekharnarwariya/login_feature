package com.hti.smpp.common.httpclient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.smpp.util.Queue;

public class HttpDlrParamInsert implements Runnable {
	public static Queue paramQueue = new Queue();
	private boolean isStop = false;
	private IDatabaseService dbService =new  IDatabaseService();
	public static boolean clear = false;
	private Logger logger = LoggerFactory.getLogger(HttpRequestLog.class);

	public HttpDlrParamInsert() {
		logger.info("<-- HttpDlrParamInsert Starting --> ");
		new Thread(this, "HttpDlrParamInsert").start();
	}

	@Override
	public void run() {
		while (!isStop) {
			if (clear) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, -3);
				String max_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime());
				max_msg_id = max_msg_id + "0000000000000";
				int count = dbService.removeHttpDlrParamLog(
						"delete from http_dlr_param where msg_id < " + max_msg_id + " limit 25000");
				if (count < 25000) {
					clear = false;
				}
			}
			if (!paramQueue.isEmpty()) {
				int counter = 0;
				List<HttpDlrParamEntry> list = new java.util.ArrayList<HttpDlrParamEntry>();
				while (!paramQueue.isEmpty()) {
					list.add((HttpDlrParamEntry) paramQueue.dequeue());
					try {
						dbService.saveHttpDlrParamLog(list);
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
		logger.info("<-- HttpRequestLog Stopped. Queue: " + paramQueue.size() + " ----> ");
	}

	public void stop() {
		logger.info("<-- HttpRequestLog Stopping. Queue: " + paramQueue.size() + " ----> ");
		isStop = true;
	}
}
