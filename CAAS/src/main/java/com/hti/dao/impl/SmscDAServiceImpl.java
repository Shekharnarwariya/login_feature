package com.hti.dao.impl;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.SmscDAO;
import com.hti.dao.SmscDAService;
import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.dto.GroupMemberEntry;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.dto.TrafficScheduleEntry;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalVar;

public class SmscDAServiceImpl implements SmscDAService {
	private SmscDAO smscDAO;
	private Logger logger = LoggerFactory.getLogger(SmscDAServiceImpl.class);

	public SmscDAServiceImpl() {
		this.smscDAO = GlobalVar.context.getBean(SmscDAO.class);
	}

	@Override
	public SmscEntry getEntry(int smsc_id) {
		SmscEntry entry = smscDAO.getEntry(smsc_id);
		if (entry.getEnforceSmsc() != null) {
			if (entry.getEnforceSmsc().length() <= 0) {
				entry.setEnforceSmsc(null);
			}
		}
		if (entry.isReplaceContent()) {
			String replacement = entry.getReplaceContentText();
			if (replacement != null && replacement.trim().length() > 0 && replacement.contains("|")) {
				String replacement_text = "";
				StringTokenizer tokens = new StringTokenizer(replacement, ",");
				while (tokens.hasMoreTokens()) {
					String next_token = tokens.nextToken();
					try {
						if (next_token.contains("|")) {
							String key = uniHexToCharMsg(next_token.substring(0, next_token.indexOf("|")));
							String value = uniHexToCharMsg(
									next_token.substring(next_token.indexOf("|") + 1, next_token.length()));
							replacement_text += key + "|" + value + ",";
						}
					} catch (Exception e) {
						logger.error(next_token, e);
					}
				}
				if (replacement_text.length() > 0) {
					replacement_text = replacement_text.substring(0, replacement_text.length() - 1);
					entry.setReplaceContentText(replacement_text);
				}
			} else {
				entry.setReplaceContent(false);
				entry.setReplaceContentText(null);
			}
		} else {
			entry.setReplaceContentText(null);
		}
		return entry;
	}

	@Override
	public Map<Integer, SmscEntry> list() {
		Map<Integer, SmscEntry> map = new HashMap<Integer, SmscEntry>();
		List<SmscEntry> list = smscDAO.list();
		for (SmscEntry entry : list) {
			if (entry != null && entry.getName() != null && entry.getName().length() > 0) {
				if (entry.getEnforceSmsc() != null) {
					if (entry.getEnforceSmsc().length() <= 0) {
						entry.setEnforceSmsc(null);
					}
				}
				if (entry.isReplaceContent()) {
					String replacement = entry.getReplaceContentText();
					if (replacement != null && replacement.trim().length() > 0 && replacement.contains("|")) {
						String replacement_text = "";
						StringTokenizer tokens = new StringTokenizer(replacement, ",");
						while (tokens.hasMoreTokens()) {
							String next_token = tokens.nextToken();
							try {
								if (next_token.contains("|")) {
									String key = uniHexToCharMsg(next_token.substring(0, next_token.indexOf("|")));
									String value = uniHexToCharMsg(
											next_token.substring(next_token.indexOf("|") + 1, next_token.length()));
									replacement_text += key + "|" + value + ",";
								}
							} catch (Exception e) {
								logger.error(next_token, e);
							}
						}
						if (replacement_text.length() > 0) {
							replacement_text = replacement_text.substring(0, replacement_text.length() - 1);
							entry.setReplaceContentText(replacement_text);
						}
					} else {
						entry.setReplaceContent(false);
						entry.setReplaceContentText(null);
					}
				} else {
					entry.setReplaceContentText(null);
				}
				map.put(entry.getId(), entry);
			}
		}
		return map;
	}

	private String uniHexToCharMsg(String msg) {
		if (msg == null || msg.length() == 0) {
			msg = "0020";
		}
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		try {
			if (msg.substring(0, 2).compareTo("00") == 0) {
				reqNULL = true;
			}
			charsByt = new BigInteger(msg, 16).toByteArray();
			if (charsByt[0] == '\0') {
				var = new byte[charsByt.length - 1];
				for (int q = 1; q < charsByt.length; q++) {
					var[q - 1] = charsByt[q];
				}
				charsByt = var;
			}
			if (reqNULL) {
				var = new byte[charsByt.length + 1];
				x = 0;
				var[0] = '\0';
				reqNULL = false;
			} else {
				var = new byte[charsByt.length];
				x = -1;
			}
			for (int l = 0; l < charsByt.length; l++) {
				var[++x] = charsByt[l];
			}
			msg = new String(var, "UTF-16");
		} catch (Exception ex) {
			logger.error(msg, ex);
		}
		return msg;
	}

	@Override
	public List<GroupMemberEntry> listGroupMember() {
		return smscDAO.listGroupMember();
	}

	@Override
	public Map<String, Integer> listNames() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		List<SmscEntry> list = smscDAO.listNames();
		for (SmscEntry entry : list) {
			map.put(entry.getName(), entry.getId());
		}
		return map;
	}

	@Override
	public Map<Integer, Map<Integer, Map<String, String>>> listSchedule() {
		Map<Integer, Map<Integer, Map<String, String>>> map = new HashMap<Integer, Map<Integer, Map<String, String>>>();
		List<TrafficScheduleEntry> list = smscDAO.listSchedule();
		for (TrafficScheduleEntry entry : list) {
			int smscId = entry.getSmscId();
			int day = entry.getDay();
			String gmt = entry.getGmt();
			String duration = entry.getDuration();
			String downTime = entry.getDownTime();
			int hours = 0, minutes = 0;
			try {
				if (duration.contains(":")) {
					hours = Integer.parseInt(duration.split(":")[0]);
					minutes = Integer.parseInt(duration.split(":")[1]);
				} else {
					hours = Integer.parseInt(duration);
				}
			} catch (Exception ex) {
				logger.error(entry.getId() + " Invalid Duration: " + duration);
				continue;
			}
			Map<Integer, Map<String, String>> in_map = null;
			if (map.containsKey(smscId)) {
				in_map = map.get(smscId);
			} else {
				in_map = new HashMap<Integer, Map<String, String>>();
			}
			if (day == 0) { // create schedule for all days
				SimpleDateFormat client_formatter = new SimpleDateFormat("HH:mm:ss");
				client_formatter.setTimeZone(TimeZone.getTimeZone(gmt));
				SimpleDateFormat local_formatter = new SimpleDateFormat("HH:mm:ss");
				try {
					String local_down_time = local_formatter.format(client_formatter.parse(downTime));
					Calendar local_cal = local_formatter.getCalendar();
					if (hours > 0) {
						local_cal.add(Calendar.HOUR, hours);
					}
					if (minutes > 0) {
						local_cal.add(Calendar.MINUTE, minutes);
					}
					String local_up_time = null;
					int hh = local_cal.get(Calendar.HOUR_OF_DAY);
					int mm = local_cal.get(Calendar.MINUTE);
					int ss = local_cal.get(Calendar.SECOND);
					if (hh < 10) {
						local_up_time = "0" + hh + ":";
					} else {
						local_up_time = hh + ":";
					}
					if (mm < 10) {
						local_up_time += "0" + mm + ":";
					} else {
						local_up_time += mm + ":";
					}
					if (ss < 10) {
						local_up_time += "0" + ss;
					} else {
						local_up_time += ss;
					}
					Map<String, String> up_down_entry = null;
					for (int i = 1; i <= 7; i++) {
						up_down_entry = new HashMap<String, String>();
						up_down_entry.put(local_down_time, FlagStatus.BLOCKED);
						up_down_entry.put(local_up_time, FlagStatus.DEFAULT);
						in_map.put(i, up_down_entry);
					}
				} catch (Exception e) {
					logger.error(smscId + " DownTime: " + downTime, e);
				}
			} else {
				if (day <= 7) {
					SimpleDateFormat configured_formatter = new SimpleDateFormat("dd HH:mm:ss");
					configured_formatter.setTimeZone(TimeZone.getTimeZone(gmt));
					SimpleDateFormat local_formatter = new SimpleDateFormat("dd HH:mm:ss");
					try {
						Date client_date = configured_formatter.parse("03 " + downTime);
						local_formatter.format(client_date);
						int configured_day = configured_formatter.getCalendar().get(Calendar.DAY_OF_MONTH);
						int local_day = local_formatter.getCalendar().get(Calendar.DAY_OF_MONTH);
						int hh = local_formatter.getCalendar().get(Calendar.HOUR_OF_DAY);
						int mm = local_formatter.getCalendar().get(Calendar.MINUTE);
						int ss = local_formatter.getCalendar().get(Calendar.SECOND);
						if (hh < 10) {
							downTime = "0" + hh + ":";
						} else {
							downTime = hh + ":";
						}
						if (mm < 10) {
							downTime += "0" + mm + ":";
						} else {
							downTime += mm + ":";
						}
						if (ss < 10) {
							downTime += "0" + ss;
						} else {
							downTime += ss;
						}
						Calendar local_cal = local_formatter.getCalendar();
						if (hours > 0) {
							local_cal.add(Calendar.HOUR, hours);
						}
						if (minutes > 0) {
							local_cal.add(Calendar.MINUTE, minutes);
						}
						String local_up_time = null;
						int up_day = local_cal.get(Calendar.DAY_OF_MONTH);
						hh = local_cal.get(Calendar.HOUR_OF_DAY);
						mm = local_cal.get(Calendar.MINUTE);
						ss = local_cal.get(Calendar.SECOND);
						if (hh < 10) {
							local_up_time = "0" + hh + ":";
						} else {
							local_up_time = hh + ":";
						}
						if (mm < 10) {
							local_up_time += "0" + mm + ":";
						} else {
							local_up_time += mm + ":";
						}
						if (ss < 10) {
							local_up_time += "0" + ss;
						} else {
							local_up_time += ss;
						}
						if (configured_day != local_day) {
							if (local_day > configured_day) {
								if (day == 7) { // sunday
									day = 1; // monday
								} else {
									day = day + 1;
								}
							} else {
								if (day == 1) { // monday
									day = 7; // sunday
								} else {
									day = day - 1;
								}
							}
						}
						if (up_day != local_day) {
							up_day = day + 1;
						} else {
							up_day = day;
						}
						Map<String, String> up_down_entry = null;
						if (in_map.containsKey(day)) {
							up_down_entry = in_map.get(day);
						} else {
							up_down_entry = new HashMap<String, String>();
						}
						up_down_entry.put(downTime, FlagStatus.BLOCKED);
						in_map.put(day, up_down_entry);
						if (in_map.containsKey(up_day)) {
							up_down_entry = in_map.get(up_day);
						} else {
							up_down_entry = new HashMap<String, String>();
						}
						up_down_entry.put(local_up_time, FlagStatus.DEFAULT);
						in_map.put(up_day, up_down_entry);
					} catch (Exception e) {
						logger.error(smscId + " DownTime: " + downTime, e);
					}
				}
			}
			map.put(smscId, in_map);
		}
		return map;
	}

	@Override
	public List<GroupEntry> listGroup() {
		return smscDAO.listGroup();
	}
}
