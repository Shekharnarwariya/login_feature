package com.hti.bsfm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.Converter;
import com.hti.util.GlobalCache;

public class SpamFilter {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");
	private static Map<Integer, String> hex_mapping = new HashMap<Integer, String>();
	private static Map<Integer, ProfileEntry> rules = Collections.synchronizedMap(new TreeMap<Integer, ProfileEntry>());
	private static ProfileEntry urlFilterEntry = null;
	static {
		hex_mapping.put(65, "41");
		hex_mapping.put(66, "42");
		hex_mapping.put(67, "43");
		hex_mapping.put(68, "44");
		hex_mapping.put(69, "45");
		hex_mapping.put(70, "46");
		hex_mapping.put(71, "47");
		hex_mapping.put(72, "48");
		hex_mapping.put(73, "49");
		hex_mapping.put(74, "4A");
		hex_mapping.put(75, "4B");
		hex_mapping.put(76, "4C");
		hex_mapping.put(77, "4D");
		hex_mapping.put(78, "4E");
		hex_mapping.put(79, "4F");
		hex_mapping.put(80, "50");
		hex_mapping.put(81, "51");
		hex_mapping.put(82, "52");
		hex_mapping.put(83, "53");
		hex_mapping.put(84, "54");
		hex_mapping.put(85, "55");
		hex_mapping.put(86, "56");
		hex_mapping.put(87, "57");
		hex_mapping.put(88, "58");
		hex_mapping.put(89, "59");
		hex_mapping.put(90, "5A");
		hex_mapping.put(97, "61");
		hex_mapping.put(98, "62");
		hex_mapping.put(99, "63");
		hex_mapping.put(100, "64");
		hex_mapping.put(101, "65");
		hex_mapping.put(102, "66");
		hex_mapping.put(103, "67");
		hex_mapping.put(104, "68");
		hex_mapping.put(105, "69");
		hex_mapping.put(106, "6A");
		hex_mapping.put(107, "6B");
		hex_mapping.put(108, "6C");
		hex_mapping.put(109, "6D");
		hex_mapping.put(110, "6E");
		hex_mapping.put(111, "6F");
		hex_mapping.put(112, "70");
		hex_mapping.put(113, "71");
		hex_mapping.put(114, "72");
		hex_mapping.put(115, "73");
		hex_mapping.put(116, "74");
		hex_mapping.put(117, "75");
		hex_mapping.put(118, "76");
		hex_mapping.put(119, "77");
		hex_mapping.put(120, "78");
		hex_mapping.put(121, "79");
		hex_mapping.put(122, "7A");
		hex_mapping.put(48, "30");
		hex_mapping.put(49, "31");
		hex_mapping.put(50, "32");
		hex_mapping.put(51, "33");
		hex_mapping.put(52, "34");
		hex_mapping.put(53, "35");
		hex_mapping.put(54, "36");
		hex_mapping.put(55, "37");
		hex_mapping.put(56, "38");
		hex_mapping.put(57, "39");
		hex_mapping.put(27, "1B");
		hex_mapping.put(12, "1B0A");
		hex_mapping.put(94, "1B14");
		hex_mapping.put(123, "1B28");
		hex_mapping.put(125, "1B29");
		hex_mapping.put(92, "1B2F");
		hex_mapping.put(91, "1B3C");
		hex_mapping.put(126, "1B3D");
		hex_mapping.put(93, "1B3E");
		hex_mapping.put(124, "1B40");
		hex_mapping.put(8364, "1B65");
		hex_mapping.put(37, "25");
		hex_mapping.put(38, "26");
		hex_mapping.put(39, "27");
		hex_mapping.put(40, "28");
		hex_mapping.put(41, "29");
		hex_mapping.put(42, "2A");
		hex_mapping.put(43, "2B");
		hex_mapping.put(44, "2C");
		hex_mapping.put(45, "2D");
		hex_mapping.put(46, "2E");
		hex_mapping.put(47, "2F");
		hex_mapping.put(58, "3A");
		hex_mapping.put(59, "3B");
		hex_mapping.put(60, "3C");
		hex_mapping.put(61, "3D");
		hex_mapping.put(62, "3E");
		hex_mapping.put(63, "3F");
		hex_mapping.put(161, "40");//
		hex_mapping.put(228, "7B");//
		hex_mapping.put(246, "7C");//
		hex_mapping.put(241, "7D");//
		hex_mapping.put(252, "7E");//
		hex_mapping.put(224, "7F");//
		hex_mapping.put(196, "5B");//
		hex_mapping.put(214, "5C");//
		hex_mapping.put(209, "5D");//
		hex_mapping.put(220, "5E");//
		hex_mapping.put(167, "5F");//
		hex_mapping.put(191, "60");
		hex_mapping.put(64, "00"); // @
		hex_mapping.put(163, "01");//
		hex_mapping.put(36, "02");//
		hex_mapping.put(165, "03");
		hex_mapping.put(232, "04");
		hex_mapping.put(233, "05");
		hex_mapping.put(249, "06");
		hex_mapping.put(236, "07");
		hex_mapping.put(242, "08");
		hex_mapping.put(199, "09");
		hex_mapping.put(216, "0B");
		hex_mapping.put(248, "0C");
		hex_mapping.put(13, "0D");
		hex_mapping.put(197, "0E");
		hex_mapping.put(229, "0F");
		hex_mapping.put(95, "11");
		hex_mapping.put(198, "1C");
		hex_mapping.put(230, "1D");
		hex_mapping.put(223, "1E");
		hex_mapping.put(201, "1F");
		hex_mapping.put(10, "0A");
		hex_mapping.put(20, "32");
		hex_mapping.put(916, "10");
		hex_mapping.put(934, "12");
		hex_mapping.put(915, "13");// Correct Encoding
		hex_mapping.put(923, "14");
		hex_mapping.put(937, "15");// Correct Encoding
		hex_mapping.put(928, "16");// Correct Encoding
		hex_mapping.put(936, "17");// Correct Encoding
		hex_mapping.put(931, "18");
		hex_mapping.put(920, "19");
		hex_mapping.put(926, "1A");
		hex_mapping.put(33, "21");
		hex_mapping.put(34, "22");
		hex_mapping.put(35, "23");
		hex_mapping.put(164, "24");
		hex_mapping.put(32, "20");
	}

	public static synchronized int loadProfiles() {
		Connection con = null;
		Statement statement = null;
		ResultSet rs = null;
		String sql = "select * from bsfmaster order by priority";
		try {
			con = GlobalCache.connection_pool_proc.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				boolean active = rs.getBoolean("active");
				int profile_id = rs.getInt("id");
				if (!active) {
					if (rs.getString("profilename") != null
							&& rs.getString("profilename").equalsIgnoreCase("URL_FILTER")) {
						urlFilterEntry = new ProfileEntry();
						urlFilterEntry.setId(profile_id);
					}
					continue;
				}
				int priority = rs.getInt("priority");
				String username = rs.getString("username");
				String smsc = rs.getString("smsc");
				String source = rs.getString("sourceid");
				String prefix = rs.getString("prefixes");
				String networks = rs.getString("networks");
				String temp_content = rs.getString("contents");
				boolean reverse = rs.getBoolean("reverse");
				String reroute = rs.getString("reroute");
				boolean schedule = rs.getBoolean("schedule");
				int rerouteGroupId = rs.getInt("reroute_group_id");
				String forceSenderId = rs.getString("force_sender");
				int msgLength = rs.getInt("msg_length");
				int msgLengthOpr = rs.getInt("msg_length_opr");
				if (reroute != null && reroute.trim().length() == 0) {
					reroute = null;
				}
				if (forceSenderId != null && forceSenderId.trim().length() == 0) {
					forceSenderId = null;
				}
				if (smsc != null && smsc.trim().length() == 0) {
					smsc = null;
				}
				if (username != null && username.trim().length() == 0) {
					username = null;
				}
				if (reverse) {
					if (smsc == null) {
						if (username == null) {
							logger.error(profile_id + " <-- Invalid Spam Rule Configured[reverse] --> ");
							continue;
						}
					}
				}
				Set<String> network_prefixes = new HashSet<String>();
				if (networks != null && networks.length() > 0) {
					for (String network : networks.split(",")) {
						try {
							int network_id = Integer.parseInt(network);
							if (network_id > 0) {
								if (GlobalCache.IdPrefixMapping.containsKey(network_id)) {
									network_prefixes.addAll(GlobalCache.IdPrefixMapping.get(network_id));
								}
							}
						} catch (Exception e) {
							logger.info(profile_id + " Invalid Network_id: " + network);
						}
					}
				}
				if (prefix != null && prefix.length() > 0) {
					for (String prefix_token : prefix.replaceAll("\\s+", "").split(",")) {
						if (prefix_token != null && prefix_token.length() > 0) {
							try {
								Long.parseLong(prefix_token);
								network_prefixes.add(prefix_token);
							} catch (Exception e) {
								logger.info(profile_id + " Invalid Prefix: " + prefix_token);
							}
						}
					}
				}
				System.out.println(profile_id + " Prefixes: " + network_prefixes);
				if (prefix != null && prefix.trim().length() == 0) {
					prefix = null;
				}
				if (source != null) {
					if (source.trim().length() > 0) {
						source = source.toLowerCase();
					} else {
						source = null;
					}
				}
				String content = "";
				if (temp_content != null) {
					if (temp_content.trim().length() > 0) {
						StringTokenizer tokens = new StringTokenizer(temp_content, ",");
						while (tokens.hasMoreTokens()) {
							String content_token = Converter.getUnicode(tokens.nextToken().toCharArray());
							if (content_token.matches(
									"^[A-Za-z0-9 \\r\\n@£$¥èéùìòÇØøÅå\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u00A4\u03A3\u0398\u039EÆæßÉ!\"#$%&'()*+,\\-./:;<=>?¡ÄÖÑÜ§¿äöñüà^{}\\\\\\[~\\]|\u20AC]*$")) {
								String hex_token = "";
								for (char c : content_token.toCharArray()) {
									if (hex_mapping.containsKey((int) c)) {
										hex_token += hex_mapping.get((int) c);
									} else {
										hex_token += (int) c;
									}
								}
								content_token = getContent(hex_token.toCharArray());
							}
							content += content_token + ",";
						}
						if (content.length() > 0) {
							content = content.substring(0, content.length() - 1);
						} else {
							content = null;
						}
					} else {
						content = null;
					}
				}
				if (smsc == null && username == null && source == null && prefix == null && content == null) {
					logger.error(profile_id + " <-- Invalid Spam Rule Configured --> ");
					continue;
				}
				// System.out.println("Content: " + content);
				ProfileEntry entry = new ProfileEntry(profile_id, reverse, reroute, null, forceSenderId,
						rerouteGroupId);
				entry.setMsgLength(msgLength);
				entry.setLengthOpr(msgLengthOpr);
				if (smsc != null && smsc.length() > 0) {
					Set<String> smsc_set = new HashSet<String>();
					for (String smsc_token : smsc.replaceAll("\\s+", "").split(",")) {
						if (smsc_token != null && smsc_token.length() > 0) {
							smsc_set.add(smsc_token);
						}
					}
					entry.setSmsc(smsc_set);
				}
				if (username != null && username.length() > 0) {
					Set<String> user_set = new HashSet<String>();
					for (String user_token : username.replaceAll("\\s+", "").split(",")) {
						if (user_token != null && user_token.length() > 0) {
							user_set.add(user_token);
						}
					}
					entry.setUsername(user_set);
				}
				if (source != null && source.length() > 0) {
					Set<String> source_set = new HashSet<String>();
					for (String source_token : source.split(",")) {
						if (source_token != null && source_token.length() > 0) {
							source_set.add(source_token);
						}
					}
					entry.setSource(source_set);
				}
				if (!network_prefixes.isEmpty()) {
					entry.setPrefix(network_prefixes);
				}
				if (content != null && content.length() > 0) {
					Set<String> content_set = new HashSet<String>();
					for (String content_token : content.split("\\s*,\\s*")) {
						if (content_token != null && content_token.length() > 0) {
							content_set.add(content_token);
						}
					}
					entry.setContent(content_set);
				}
				if (schedule) {
					String day_time = rs.getString("day_time");
					if (day_time != null && day_time.length() > 0) {
						Map<Integer, Map<String, String>> schedules = new TreeMap<Integer, Map<String, String>>();
						for (String day_time_token : day_time.split(",")) {
							if (day_time_token.length() == 19 && day_time_token.contains("F")
									&& day_time_token.contains("T")) {
								try {
									int day = Integer
											.parseInt(day_time_token.substring(0, day_time_token.indexOf("F")));
									String from = day_time_token.substring(day_time_token.indexOf("F") + 1,
											day_time_token.indexOf("T"));
									String to = day_time_token.substring(day_time_token.indexOf("T") + 1,
											day_time_token.length());
									Map<String, String> day_time_map = null;
									if (schedules.containsKey(day)) {
										day_time_map = schedules.get(day);
									} else {
										day_time_map = new TreeMap<String, String>();
									}
									day_time_map.put(from, to);
									schedules.put(day, day_time_map);
								} catch (Exception ex) {
									logger.error(profile_id + " Invalid Schedule token: " + day_time_token, ex);
									continue;
								}
							}
						}
						logger.info(profile_id + " Schedules: " + schedules);
						if (!schedules.isEmpty()) {
							entry.setSchedule(true);
							entry.setActiveOnSchedule(rs.getBoolean("active_on_sch_time"));
							entry.setSchedules(schedules);
						}
					}
				}
				rules.put(priority, entry);
			}
			logger.info("Bsfm Active Profile:" + rules.size());
			logger.info("Bsfm Active Priorities:" + rules.keySet());
		} catch (Exception e) {
			logger.error("loadProfiles()", e.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connection_pool_proc.putConnection(con);
		}
		return rules.size();
	}

	public static ProfileEntry getUrlFilterEntry() {
		return urlFilterEntry;
	}

	public static synchronized void clearCache() {
		rules.clear();
		logger.info("***** Bsfm cache Cleared *****");
	}

	public static synchronized ProfileEntry filter(String... params) {
		if (rules.isEmpty()) {
			return null;
		} else {
			String smsc = params[0], username = params[1], source = params[2], destination = params[3],
					content = params[4];
			// ProfileEntry result = null;
			// boolean matched = false;
			for (ProfileEntry entry : rules.values()) {
				String remarks = "profile matched";
				boolean block = false;
				if (!entry.getSmsc().isEmpty()) {
					if (!entry.getSmsc().contains(smsc)) {
						continue; // check next rule
					} else {
						// logger.info(entry.getId() + " matched smsc: " + smsc);
						remarks = "Smsc matched " + smsc;
					}
				} else {
					// logger.info(entry.getId() + " applicable for all smsc");
				}
				if (!entry.getUsername().isEmpty()) {
					if (!entry.getUsername().contains(username)) {
						continue; // check next rule
					} else {
						// logger.info(entry.getId() + " matched User: " + username);
						remarks = "Username Matched " + username;
					}
				} else {
					// logger.info(entry.getId() + " applicable for all Users");
				}
				if (entry.isReverse()) {
					if (!entry.getSource().isEmpty()) {
						boolean source_matched = false;
						for (String source_key : entry.getSource()) {
							// System.out.println(source_key + " Matching Try: " + source);
							if (Pattern.compile(source_key).matcher(source).find()) {
								logger.debug(entry.getId() + "-> " + source_key + " matched Source: " + source);
								source_matched = true;
								break;
							}
						}
						if (!source_matched) {
							logger.info(entry.getId() + "[Reverse]:-> Unmatched Source: " + source);
							remarks = "Unmatched Source " + source;
							block = true;
						} else {
							// logger.info(entry.getId() + " matched Source: " + source);
						}
					} else {
						// logger.info(entry.getId() + " applicable for all Sources");
					}
					if (!block) {
						if (!entry.getPrefix().isEmpty()) {
							boolean prefix_matched = false;
							for (String prefix : entry.getPrefix()) {
								if (destination.startsWith(prefix)) {
									prefix_matched = true;
									break;
								}
							}
							if (!prefix_matched) {
								logger.info(entry.getId() + "[Reverse]:-> Unmatched Prefix: " + destination);
								remarks = "Unmatched Prefix " + destination;
								block = true;
							} else {
								// logger.info(entry.getId() + " matched Prefix: " + destination);
							}
						} else {
							// logger.info(entry.getId() + " applicable for all Prefixes");
						}
						if (!block) {
							if (!entry.getContent().isEmpty()) {
								boolean content_matched = false;
								for (String spam_word : entry.getContent()) {
									if (spam_word.contains("#variable#")) {
										if (isEqual(content, spam_word)) {
											logger.debug(
													" <- Configured Rule <" + spam_word + "> Equal <" + content + ">");
											content_matched = true;
											break;
										}
									} else {
										if (isContain(content, spam_word)) {
											logger.debug(" <- Spam Word Found in Content <" + spam_word + ">");
											content_matched = true;
											break;
										}
									}
								}
								if (!content_matched) {
									logger.info(
											entry.getId() + "[Reverse]:-> Unmatched Content: " + entry.getContent());
									remarks = "Unmatched Content ";
								}
							} else {
								// logger.info(entry.getId() + " applicable for all contents");
							}
						}
						if (!block) {
							if (entry.getMsgLength() > 0 && (entry.getLengthOpr() > 0 && entry.getLengthOpr() < 4)) {
								if (entry.getLengthOpr() == 1) { // less then content length
									if (content.length() < entry.getMsgLength()) {
										logger.debug(entry.getId() + "[Reverse]:-> Matched Content Length less then ["
												+ entry.getMsgLength() + "]: " + content.length());
									} else {
										logger.info(entry.getId() + "[Reverse]:-> Unmatched Content Length less then["
												+ entry.getMsgLength() + "]: " + content.length());
										block = true;
									}
								} else if (entry.getLengthOpr() == 2) { // equal content length
									if (content.length() == entry.getMsgLength()) {
										logger.debug(entry.getId() + "[Reverse]:-> Matched Content Length Equal["
												+ entry.getMsgLength() + "]: " + content.length());
									} else {
										logger.info(entry.getId() + "[Reverse]:-> Unmatched Content Length Equal["
												+ entry.getMsgLength() + "]: " + content.length());
										block = true;
									}
								} else if (entry.getLengthOpr() == 3) { // greater then content length
									if (content.length() > entry.getMsgLength()) {
										logger.debug(
												entry.getId() + "[Reverse]:-> Matched Content Length Greater then ["
														+ entry.getMsgLength() + "]: " + content.length());
									} else {
										logger.info(
												entry.getId() + "[Reverse]:-> Unmatched Content Length Greater then["
														+ entry.getMsgLength() + "]: " + content.length());
										block = true;
									}
								}
							}
						}
					}
				} else {
					if (!entry.getSource().isEmpty()) {
						boolean source_matched = false;
						for (String source_key : entry.getSource()) {
							// System.out.println(source_key + " Matching Try: " + source);
							if (Pattern.compile(source_key).matcher(source).find()) {
								logger.debug(entry.getId() + "-> " + source_key + " matched Source: " + source);
								remarks = "Source Matched " + source;
								source_matched = true;
								break;
							}
						}
						if (!source_matched) {
							continue; // check next rule
						} else {
							// logger.info(entry.getId() + " matched Source: " + source);
						}
					} else {
						// logger.info(entry.getId() + " applicable for all Sources");
					}
					if (!entry.getPrefix().isEmpty()) {
						boolean prefix_matched = false;
						for (String prefix : entry.getPrefix()) {
							if (destination.startsWith(prefix)) {
								remarks = "Prefix Matched " + prefix;
								prefix_matched = true;
								break;
							}
						}
						if (!prefix_matched) {
							continue; // check next rule
						} else {
							// logger.info(entry.getId() + " matched Prefix: " + destination);
						}
					} else {
						// logger.info(entry.getId() + " applicable for all Prefixes");
					}
					if (!entry.getContent().isEmpty()) {
						boolean content_matched = false;
						for (String spam_word : entry.getContent()) {
							if (spam_word.contains("#variable#")) {
								if (isEqual(content, spam_word)) {
									logger.debug(" <- Configured Rule <" + spam_word + "> Equal <" + content + ">");
									remarks = "word Matched " + spam_word;
									content_matched = true;
									break;
								}
							} else {
								if (isContain(content, spam_word)) {
									logger.debug(" <- Spam Word Found in Content <" + spam_word + ">");
									remarks = "word Matched " + spam_word;
									content_matched = true;
									break;
								}
							}
						}
						if (!content_matched) {
							continue; // check next rule
						} 
					} else {
						// logger.info(entry.getId() + " applicable for all contents");
					}
					if (entry.getMsgLength() > 0 && (entry.getLengthOpr() > 0 && entry.getLengthOpr() < 4)) {
						if (entry.getLengthOpr() == 1) { // less then content length
							if (content.length() < entry.getMsgLength()) {
								logger.info(entry.getId() + " Matched Content Length less then [" + entry.getMsgLength()
										+ "]: " + content.length());
							} else {
								logger.debug(entry.getId() + " Unmatched Content Length less then["
										+ entry.getMsgLength() + "]: " + content.length());
								continue;
							}
						} else if (entry.getLengthOpr() == 2) { // equal content length
							if (content.length() == entry.getMsgLength()) {
								logger.info(entry.getId() + " Matched Content Length Equal[" + entry.getMsgLength()
										+ "]: " + content.length());
							} else {
								logger.debug(entry.getId() + " Unmatched Content Length Equal[" + entry.getMsgLength()
										+ "]: " + content.length());
								continue;
							}
						} else if (entry.getLengthOpr() == 3) { // greater then content length
							if (content.length() > entry.getMsgLength()) {
								logger.info(entry.getId() + " Matched Content Length Greater then ["
										+ entry.getMsgLength() + "]: " + content.length());
							} else {
								logger.debug(entry.getId() + " Unmatched Content Length Greater then["
										+ entry.getMsgLength() + "]: " + content.length());
								continue;
							}
						}
					}
				}
				if (entry.isSchedule()) {
					boolean time_matched = false;
					if (entry.getSchedules().containsKey(0)) { // everyday
						for (Map.Entry<String, String> timing_entry : entry.getSchedules().get(0).entrySet()) {
							if ((LocalTime.now().isAfter(LocalTime.parse(timing_entry.getKey())))
									&& (LocalTime.now().isBefore(LocalTime.parse(timing_entry.getValue())))) {
								// logger.info(entry.getId() + " Current Time[0] between [" + timing_entry.getKey() + " & "
								// + timing_entry.getValue() + "]");
								time_matched = true;
								break;
							}
						}
					}
					if (!time_matched) {
						if (entry.getSchedules().containsKey(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))) {
							int current_day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
							for (Map.Entry<String, String> timing_entry : entry.getSchedules().get(current_day)
									.entrySet()) {
								if ((LocalTime.now().isAfter(LocalTime.parse(timing_entry.getKey())))
										&& (LocalTime.now().isBefore(LocalTime.parse(timing_entry.getValue())))) {
									// logger.info(entry.getId() + " Current Time[" + current_day + "] between ["
									// + timing_entry.getKey() + " & " + timing_entry.getValue() + "]");
									time_matched = true;
									break;
								}
							}
						}
					}
					if (time_matched) {
						if (!entry.isActiveOnSchedule()) {
							logger.debug(entry.getId() + " Rule is Disabled for Scheduled Time");
							continue; // check next rule
						} else {
							logger.debug(entry.getId() + " Rule is Enabled for Scheduled Time");
							remarks = "Enabled for Scheduled Time";
						}
					} else {
						if (!entry.isActiveOnSchedule()) {
							logger.debug(entry.getId() + " Rule is Enabled Except Scheduled Time");
							remarks = "Enabled Except Scheduled Time";
						} else {
							logger.debug(entry.getId() + " Rule is Disabled Except Scheduled Time");
							continue; // check next rule
						}
					}
				}
				if (block) {
					return new ProfileEntry(entry.getId(), false, null, remarks, null, 0);
				} else {
					return new ProfileEntry(entry.getId(), entry.isReverse(), entry.getReroute(), remarks,
							entry.getForceSenderId(), entry.getRerouteGroupId());
				}
			}
			return null;
		}
	}

	private static boolean isContain(String content, String word) {
		// logger.info("isContain: " + content);
		return Pattern.compile("\\b" + "(?i)" + word + "\\b").matcher(content).find();
	}

	private static boolean isEqual(String content, String filter) {
		filter = filter.replaceAll("#variable#", "[\\\\p{L}\\\\u0000-\\\\u00FF]*");
		// logger.info("isEqual: " + content);
		return Pattern.matches("(?i)" + filter, content);
	}

	private static String getContent(char[] buffer) {
		String unicode = "";
		int code = 0;
		int j = 0;
		char[] unibuffer = new char[buffer.length / 2];
		try {
			for (int i = 0; i < buffer.length; i += 2) {
				code += Character.digit(buffer[i], 16) * 16;
				code += Character.digit(buffer[i + 1], 16);
				unibuffer[j++] = (char) code;
				code = 0;
			}
			unicode = new String(unibuffer);
		} catch (Exception e) {
			logger.info("getContent(): " + e);
		}
		return unicode;
	}
}
