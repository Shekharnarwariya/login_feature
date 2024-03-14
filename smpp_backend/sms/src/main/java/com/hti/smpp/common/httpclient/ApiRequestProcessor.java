package com.hti.smpp.common.httpclient;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;
import com.logica.smpp.util.Queue;

@Service
public class ApiRequestProcessor implements Runnable {

	private static IDatabaseService dbService;
	private static Logger logger = LoggerFactory.getLogger(ApiRequestProcessor.class);
	public static Queue procQueue = new Queue();
	private boolean stop;
	private Client client = new Client();
	private ApiStatusInsertThread insertThread = null;
	private Queue insertQueue = null;
	private static Map<Integer, Timer> ScheduledTask = new java.util.HashMap<Integer, Timer>();

	public ApiRequestProcessor() {
		logger.info("ApiRequestProcessor Starting");
		new Thread(this, "ApiRequestProcessor").start();
		insertQueue = new Queue();
		insertThread = new ApiStatusInsertThread(insertQueue);
		new Thread(insertThread, "ApiStatusInsertThread").start();
		listSchedules();
	}

	private void listSchedules() {
		try {
			dbService = new IDatabaseService();
			List<BaseApiDTO> list = dbService.listApiSchedule();
			for (BaseApiDTO entry : list) {
				logger.info(
						"Checking for API Scheduled Task: " + entry.getScheduleId() + " " + entry.getScheduleFile());
				Date scheduledDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(entry.getServerScheduleTime());
				if (new Date().before(scheduledDate)) {
					BaseApiDTO scheduleEntry = (BaseApiDTO) MultiUtility
							.readObject(IConstants.WEBSMPP_EXT_DIR + "schedule//" + entry.getScheduleFile(), false);
					scheduleTask(scheduleEntry);
					logger.info("API Scheduled Task Configured: " + scheduleEntry.getScheduleId() + " "
							+ scheduleEntry.getScheduleFile());
				} else {
					logger.info(
							"Api Schedule Expired: " + entry.getScheduleId() + " At " + entry.getServerScheduleTime());
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public static void scheduleTask(BaseApiDTO scheduledEntry) throws ParseException {
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			public void run() {
				System.out.println("Processing API Scheduled Task: " + scheduledEntry.getScheduleId() + " "
						+ scheduledEntry.getScheduleFile());
				try {
					ApiRequestProcessor.procQueue.enqueue(scheduledEntry);
					dbService.removeApiSchedule(scheduledEntry.getScheduleId());
					File schedulefile = new File(
							IConstants.WEBSMPP_EXT_DIR + "schedule//" + scheduledEntry.getScheduleFile());
					if (schedulefile.exists()) {
						System.out.println(schedulefile + " Schedule File Deleted: " + schedulefile.delete());
					}
				} catch (Exception e) {
					System.out.println(scheduledEntry.getWebid() + ": " + e);
				}
				System.out.println("API Scheduled Task Finished: " + scheduledEntry.getId() + " "
						+ scheduledEntry.getScheduleFile());
			}
		}, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(scheduledEntry.getServerScheduleTime()));
		ScheduledTask.put(scheduledEntry.getScheduleId(), t);
	}

	public static boolean removeScheduledTask(int scheduleId) {
		System.out.println("Removing API Scheduled Task: " + scheduleId);
		if (ScheduledTask.containsKey(scheduleId)) {
			try {
				Timer t = ScheduledTask.remove(scheduleId);
				t.cancel();
			} catch (Exception e) {
				logger.error(scheduleId + "", e);
				return false;
			}
		} else {
			System.out.println(" API Scheduled Task Not Found: " + scheduleId);
			return false;
		}
		return true;
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				if (procQueue.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				} else {
					logger.info("API Processing Queue: " + procQueue.size());
					BaseApiDTO apiDTO = null;
					while (!procQueue.isEmpty()) {
						apiDTO = (BaseApiDTO) procQueue.dequeue();
						int type = apiDTO.getType();
						String sender = apiDTO.getSender();
						int sourceTon = 5, sourceNpi = 0;
						boolean isNumeric = true;
						// ************* Checking For Sender Id ******************
						if (sender.startsWith("+")) {
							sender = sender.substring(1, sender.length());
						}
						try {
							Long.parseLong(sender.trim());
						} catch (NumberFormatException ne) {
							isNumeric = false;
						}
						if (isNumeric) {
							sourceTon = 1;
							sourceNpi = 1;
						}
						// ************* Message Type **********
						String messageType = null;
						if (type == 1) {
							messageType = "SpecialChar";
						} else if (type == 2) {
							messageType = "Unicode";
						} else if (type == 3) {
							messageType = "SpecialChar";
						} else if (type == 4) {
							messageType = "Arabic";
						}
						if (apiDTO.getFormat() == 1) {
							List<String> response = null;
							if (apiDTO.isMms()) {
								response = client.submitMmsRequest(
										new ApiRequestDTO(apiDTO.getUsername(), apiDTO.getPassword(), sender, sourceTon,
												sourceNpi, apiDTO.getText(), messageType, apiDTO.getReceipients(),
												apiDTO.getRequestFormat(), apiDTO.getPeId(), apiDTO.getTemplateId(),
												apiDTO.getTelemarketerId(), apiDTO.getMmsType(), apiDTO.getCaption()));
							} else {
								response = client.submitRequest(new ApiRequestDTO(apiDTO.getUsername(),
										apiDTO.getPassword(), sender, sourceTon, sourceNpi, apiDTO.getText(),
										messageType, apiDTO.getReceipients(), apiDTO.getRequestFormat(),
										apiDTO.getPeId(), apiDTO.getTemplateId(), apiDTO.getTelemarketerId()));
							}
							// System.out.println("response: " + response);
							insertQueue.enqueue(new ApiRequestDTO(apiDTO.getWebid(), response, apiDTO.getUsername()));
						} else {
							List<String> response = client.submitCustomRequest(new ApiRequestDTO(apiDTO.getUsername(),
									apiDTO.getPassword(), sender, sourceTon, sourceNpi, messageType,
									apiDTO.getCustomReceipients(), apiDTO.getRequestFormat(), apiDTO.getPeId(),
									apiDTO.getTemplateId(), apiDTO.getTelemarketerId()));
							// System.out.println("response: " + response);
							insertQueue.enqueue(new ApiRequestDTO(apiDTO.getWebid(), response, apiDTO.getUsername()));
						}
					}
				}
			} catch (Exception e) {
				logger.error("", e.fillInStackTrace());
			}
		}
		insertThread.stop();
		logger.info("<-- ApiRequestProcessor Stopped -->");
	}

	public void stop() {
		logger.info("<-- ApiRequestProcessor Stopping -->");
		stop = true;
	}
}
