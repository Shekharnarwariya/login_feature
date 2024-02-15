package com.hti.smpp.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.schedule.dto.ScheduleEntry;
import com.hti.smpp.common.schedule.dto.ScheduleEntryExt;
import com.hti.smpp.common.schedule.dto.ScheduleHistory;
import com.hti.smpp.common.schedule.repository.ScheduleEntryRepository;
import com.hti.smpp.common.schedule.repository.ScheduleHistoryRepository;
import com.hti.smpp.common.service.SendSmsService;
import com.hti.smpp.common.service.impl.SmsServiceImpl;

@Component
public class ScheduleProcess {

	@Autowired
	private ScheduleEntryRepository scheduleEntryRepository;

	@Autowired
	private ScheduleHistoryRepository scheduleHistoryRepository;

	@Autowired
	private SmsServiceImpl smsServiceImpl;

	private BulkSmsDTO smsDTO = null;

	private static final Logger logger = LoggerFactory.getLogger(ScheduleProcess.class);

	private int process_day = 0;

	static {
		logger.info("ScheduleProcess Thread creating");
		checkScheduleFolder();
	}

	// Run the task every 10 second
	@Scheduled(cron = "*/10 * * * * *")
	public void processScheduledTasks() {
		System.out.println("<---- Schedule Check Running -----> ");
		if (process_day != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
			try {
				smsServiceImpl.addSchedule();
			} catch (Exception ex) {
				logger.error("", ex.fillInStackTrace());
			}
			logger.info("Total Schedule For Today: " + GlobalVarsSms.ScheduledBatches);
			String current_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			process_day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		}
		try {
			String Return = new SimpleDateFormat("HHmm").format(new Date());
			if (GlobalVarsSms.ScheduledBatches.containsKey(Return)) {
				logger.info("Today's Schedule: " + GlobalVarsSms.ScheduledBatches);
				Set<Integer> schedules = GlobalVarsSms.ScheduledBatches.remove(Return);
				Iterator<Integer> itr = schedules.iterator();
				while (itr.hasNext()) {
					int schedule_id = itr.next();
					System.out.println(schedule_id);
					ScheduleEntry entry = scheduleEntryRepository.findById(schedule_id).get();
					if (entry == null) {
						logger.error("Schedule[" + schedule_id + "]: Entry Missing");
						continue;
					}
					String file = entry.getFileName();
					try {
						if (GlobalVarsSms.RepeatedSchedules.contains(schedule_id)) {
							GlobalVarsSms.RepeatedSchedules.remove(schedule_id);
							Date serverDate = null;
							Date clientDate = null;
							try {
								serverDate = new SimpleDateFormat("yyyy-MM-dd")
										.parse(entry.getServerTime().split(" ")[0]);
							} catch (Exception ex) {
								serverDate = new Date();
							}
							try {
								clientDate = new SimpleDateFormat("dd-MM-yyyy")
										.parse(entry.getClientTime().split(" ")[0]);
							} catch (Exception ex) {
								clientDate = new Date();
							}
							Calendar serverCalendar = Calendar.getInstance();
							Calendar clientCalendar = Calendar.getInstance();
							serverCalendar.setTime(serverDate);
							clientCalendar.setTime(clientDate);
							String repeated = entry.getRepeated();
							smsDTO = readScheduleFile(file);
							logger.info(file + " Repeated Schedule :-> " + repeated);
							if (repeated.equalsIgnoreCase("Daily")) {
								serverCalendar.add(Calendar.DATE, 1);
								clientCalendar.add(Calendar.DATE, 1);
							} else if (repeated.equalsIgnoreCase("Weekly")) {
								serverCalendar.add(Calendar.DATE, 7);
								clientCalendar.add(Calendar.DATE, 7);
							} else if (repeated.equalsIgnoreCase("Monthly")) {
								serverCalendar.add(Calendar.MONTH, 1);
								clientCalendar.add(Calendar.MONTH, 1);
							} else if (repeated.equalsIgnoreCase("Yearly")) {
								serverCalendar.add(Calendar.YEAR, 1);
								clientCalendar.add(Calendar.YEAR, 1);
							}
							try {
								smsDTO.setDate(new SimpleDateFormat("yyyy-MM-dd").format(serverCalendar.getTime()));
							} catch (Exception e) {
								logger.error(entry.getId() + " Scheduled Date Parse Error: " + e);
							}
							try {
								smsDTO.setTimestart(new SimpleDateFormat("dd-MM-yyyy").format(clientCalendar.getTime())
										+ " " + entry.getClientTime().split(" ")[1]);
							} catch (Exception e) {
								logger.error(entry.getId() + " Client Scheduled Date Parse Error: " + e);
							}
							SendSmsService service = new SendSmsService();
							String filename = service.createScheduleFile(smsDTO);
							ScheduleEntry sch = new ScheduleEntry();
							sch.setClientGmt(smsDTO.getGmt());
							sch.setClientTime(smsDTO.getTimestart());
							sch.setFileName(filename);
							sch.setRepeated(smsDTO.getRepeat());
							sch.setScheduleType(smsDTO.getReqType());
							sch.setServerId(IConstants.SERVER_ID);
							sch.setServerTime(smsDTO.getTime());
							sch.setStatus("false");
							sch.setUsername(smsDTO.getSystemId());
							sch.setWebId(entry.getWebId());
							sch.setDate(smsDTO.getDate());
							int generated_id = scheduleEntryRepository.save(sch).getId();

							if (generated_id > 0) {
								logger.info(file + " Rescheduled As: " + filename);
							}
						}
						// ************************Added by Amit_vish for repeated Schedule
						// *******************
						String status = null;
						try {
							if (entry != null) {
								entry.setServerTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
							}

							status = smsServiceImpl.sendScheduleSms(file);
						} catch (Exception e) {
							if (e.getMessage() != null && e.getMessage().length() > 0) {
								if (e.getMessage().length() > 49) {
									status = e.getMessage().substring(0, 49);
								} else {
									status = e.getMessage();
								}
							} else {
								status = "Error";
							}
							logger.error(file, e.fillInStackTrace());
						}
						if (entry != null) {
							entry.setStatus(status);
							try {
								Date client_date_time = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
										.parse(entry.getClientTime());
								entry.setClientTime(
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(client_date_time));
							} catch (Exception ex) {
								logger.error(entry.getClientTime(), ex);
							}
							BulkSmsDTO bulkSmsDTO = readScheduleFile(file);
							ScheduleEntryExt ext = new ScheduleEntryExt(entry);
							ext.setMessageType(bulkSmsDTO.getMessageType());
							ext.setTotalNumbers(bulkSmsDTO.getDestinationList().size());
							ext.setSenderId(bulkSmsDTO.getSenderId());
							ext.setCampaign(bulkSmsDTO.getCampaignName());

							ScheduleHistory schHistory = new ScheduleHistory();
							schHistory.setCampaignName(ext.getCampaign());
							schHistory.setClientGmt(ext.getClientGmt());
							schHistory.setClientTime(ext.getClientTime());
							schHistory.setId(ext.getId());
							schHistory.setMsgType(ext.getMessageType());
							schHistory.setRemarks(ext.getStatus());
							schHistory.setRepeated(ext.getRepeated());
							schHistory.setSchType(ext.getScheduleType());
							schHistory.setSenderId(ext.getSenderId());
							schHistory.setServerId(ext.getServerId());
							schHistory.setServerTime(ext.getServerTime());
							schHistory.setTotalNumber(ext.getTotalNumbers());
							schHistory.setUsername(ext.getUsername());
							schHistory.setCreatedOn(ext.getCreatedOn());
							scheduleHistoryRepository.save(schHistory);

						}
						scheduleEntryRepository.deleteById(schedule_id);
						File schedulefile = new File(IConstants.HOME_DIR + "schedule//" + file);
						if (schedulefile.exists()) {
							logger.info(file + " Schedule Deleted: " + schedulefile.delete());
						}
					} catch (Exception e) {
						logger.error(file, e.fillInStackTrace());
					}
				}
			}
			Return = null;
		} catch (Exception e) {
			logger.error("", e.fillInStackTrace());
		}

	}

	public static void checkScheduleFolder() {
		File f = new File(IConstants.HOME_DIR + "schedule//");
		if (f.exists()) {
			if (f.isDirectory()) {
				System.out.println("Schedule Directory Exist: " + f.getAbsolutePath());
			}
		} else {
			if (f.mkdir()) {
				System.out.println("Schedule Directory Created: " + f.getAbsolutePath());
			}
		}
	}

	public BulkSmsDTO readScheduleFile(String filename) {
		ObjectInputStream fobj = null;
		BulkSmsDTO bulk = null;
		try {
			fobj = new ObjectInputStream(new FileInputStream(IConstants.HOME_DIR + "schedule//" + filename));
			bulk = (BulkSmsDTO) fobj.readObject();
			System.out.println("this is bukl dto" + bulk);
		} catch (Exception e) {
			logger.error(filename, e.fillInStackTrace());
		} finally {
			if (fobj != null) {
				try {
					fobj.close();
				} catch (IOException ex) {
				}
			}
		}
		return bulk;
	}

}
