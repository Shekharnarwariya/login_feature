package com.hti.init;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.Constants;
import com.hti.util.FileUtil;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalVar;

public class BearerBox implements Runnable {
	private boolean stop;
	private Logger logger = LoggerFactory.getLogger(BearerBox.class);
	private int process_day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
	private Thread thread;
	public static boolean RELOAD = true;
	private Map<Integer, Map<String, String>> SmscScheduleConfig = new HashMap<Integer, Map<String, String>>();

	public BearerBox() {
		logger.info("BearerBox Starting");
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	@Override
	public void run() {
		while (!stop) {
			if (process_day != Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
				logger.info("****** Smsc Scheduling Day Changed ****** ");
				process_day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
				RELOAD = true;
			}
			if (RELOAD) {
				RELOAD = false;
				SmscScheduleConfig.clear();
				logger.info("****** Reloading Smsc Scheduling ****** ");
				if (!GlobalVar.SmscScheduleConfig.isEmpty()) {
					for (Map.Entry<Integer, Map<Integer, Map<String, String>>> entry : GlobalVar.SmscScheduleConfig
							.entrySet()) {
						int smsc_id = entry.getKey();
						// logger.info(smsc_id + " adding to local cache: " + entry.getValue());
						if (entry.getValue().containsKey(process_day)) {
							Map<String, String> smsc_time_map = new HashMap<String, String>();
							for (Map.Entry<String, String> in_entry : entry.getValue().get(process_day).entrySet()) {
								String time = in_entry.getKey();
								String flag = in_entry.getValue();
								// logger.info("Current Time: " + LocalTime.now() + " Config Time: " + LocalTime.parse(time));
								if (LocalTime.now().isBefore(LocalTime.parse(time))) {
									smsc_time_map.put(time, flag);
								} else {
									logger.info(smsc_id + " Event Lost: " + time + " = " + flag);
								}
							}
							if (smsc_time_map.isEmpty()) {
								logger.info(smsc_id + " No Event For Today");
							} else {
								SmscScheduleConfig.put(smsc_id, smsc_time_map);
							}
						}
					}
				}
				logger.info("Today's SmscScheduling: " + SmscScheduleConfig);
			}
			// --------- check for smsc scheduling --------------
			if (!SmscScheduleConfig.isEmpty()) {
				// process_day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
				logger.info("****** Checking For Smsc Scheduling ****** ");
				int proceed_counter = 0;
				for (Map.Entry<Integer, Map<String, String>> entry : SmscScheduleConfig.entrySet()) {
					int smsc_id = entry.getKey();
					String smsc = GlobalVar.smsc_entries.get(smsc_id).getName();
					logger.info("Checking For Smsc Scheduling: " + smsc);
					Iterator<Map.Entry<String, String>> itr = entry.getValue().entrySet().iterator();
					while (itr.hasNext()) {
						Map.Entry<String, String> in_entry = itr.next();
						// logger.info(smsc + "Scheduling[" + process_day + "]: " + in_entry);
						String time = in_entry.getKey();
						String flag = in_entry.getValue();
						// logger.info("Current Time: " + LocalTime.now() + " Config Time: " + LocalTime.parse(time));
						if (LocalTime.now().isAfter(LocalTime.parse(time))) {
							String current_flag = FileUtil.readFlag(Constants.SMSC_FLAG_DIR + smsc + ".txt", true);
							if (flag.equalsIgnoreCase(current_flag)) {
								logger.info(smsc + "Scheduled Flag[" + flag + "] is Equal To Current Flag["
										+ current_flag + "]");
							} else {
								logger.info(smsc + "Scheduled Flag[" + flag + "] is Not Equal To Current Flag["
										+ current_flag + "]");
								if (flag.equalsIgnoreCase(FlagStatus.DEFAULT)
										&& !current_flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
									logger.info(smsc + " Skip Flag Changes");
								} else {
									if (flag.equalsIgnoreCase(FlagStatus.DEFAULT)) {
										FileUtil.setSmscDefault(Constants.SMSC_FLAG_DIR + smsc + ".txt");
										proceed_counter++;
									} else if (flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
										FileUtil.setSmscBlocked(Constants.SMSC_FLAG_DIR + smsc + ".txt");
										proceed_counter++;
									} else {
										System.err.println(smsc + " Schedule. Invalid Flag Configured: " + flag);
									}
								}
							}
							itr.remove();
						}
					}
				}
				if (proceed_counter > 0) {
					logger.info("Total Actions Performed: " + proceed_counter);
					FileUtil.setRefreshFlag(Constants.SMSC_FLAG_FILE);
				}
			}
			// --------- end for smsc scheduling ----------------
			try {
				Thread.sleep(60 * 1000);
			} catch (InterruptedException ie) {
				// no harm
			}
		}
		logger.info("BearerBox Stopped");
	}

	public void stop() {
		stop = true;
		thread.interrupt();
		logger.info("BearerBox Stopping");
	}
}
