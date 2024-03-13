package com.hti.smpp.common.httpclient;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.logica.smpp.util.Queue;

public class ApiStatusInsertThread implements Runnable {
	private Logger logger = LoggerFactory.getLogger(ApiStatusInsertThread.class);
	private Queue procQueue = null;
	private boolean stop;
	@Autowired
	private IDatabaseService dbService ;
	private int process_day = 0;
	private boolean clear = false;
	private long waitForQueueInterval = 1000; // in ms

	public ApiStatusInsertThread(Queue procQueue) {
		logger.info("ApiStatusInsertThread Starting");
		this.procQueue = procQueue;
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				if (procQueue.isEmpty()) {
					if (process_day != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
						process_day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
						clear = true;
					}
					if (clear) {
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.DATE, -30);
						String max_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime());
						max_msg_id = max_msg_id + "0000000000000";
						int count = dbService.removeApiStatus(max_msg_id, 25000);
						if (count < 25000) {
							clear = false;
						}
					}
					try {
						synchronized (procQueue) {
							procQueue.wait(waitForQueueInterval);
						}
					} catch (InterruptedException e) {
					}
				} else {
					ApiRequestDTO requestDTO = null;
					while (!procQueue.isEmpty()) {
						requestDTO = (ApiRequestDTO) procQueue.dequeue();
						dbService.putApiStatus(requestDTO);
					}
				}
			} catch (Exception ex) {
				logger.error("", ex.fillInStackTrace());
			}
		}
		logger.info("ApiStatusInsertThread Stopped.Queue: " + procQueue.size());
	}

	public void stop() {
		logger.info("ApiStatusInsertThread Stopping.Queue: " + procQueue.size());
		stop = true;
	}
}
