package com.hti.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.SmscDAService;
import com.hti.objects.PriorityQueue;
import com.hti.objects.SmscLimit;
import com.hti.smsc.SendErrorResponseSMS;
import com.hti.smsc.SmscConnection;
import com.hti.smsc.SpecialSMSCSetting;
import com.hti.smsc.dto.SmscEntry;
import com.hti.smsc.dto.SmscLooping;
import com.hti.thread.ClearNonResponding;
import com.hti.util.Constants;
import com.hti.util.Converter;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;

public class SmscDAServiceImpl implements SmscDAService {
	private Logger logger = LoggerFactory.getLogger(SmscDAServiceImpl.class);
	private Map<Integer, SmscEntry> SmscEntries = GlobalVars.hazelInstance.getMap("smsc_entries");
	private Map<String, Integer> SmscNameMapping = GlobalVars.hazelInstance.getMap("smsc_name_mapping");
	private Map<String, SmscConnection> SmscConnections = new HashMap<String, SmscConnection>();
	private Map<String, Integer> key_session_map = new HashMap<String, Integer>();
	private Set<String> BlockedRoutes = new HashSet<String>();
	private Map<String, Properties> SmscFlagStatus = GlobalVars.hazelInstance.getMap("smsc_flag_status");
	private int MAX_SESSION_ID;
	private static Map<Integer, String> hex_mapping = new HashMap<Integer, String>();
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

	@Override
	public void initializeStatus() {
		logger.info("<--- Initializing Smsc Status -->");
		resetBindStatus();
		initSpecialSetting();
		initEsmeErrorConfig();
		initSignalErrorConfig();
		initSmscBsfm();
		for (Entry<String, Properties> map_entry : SmscFlagStatus.entrySet()) {
			String name = map_entry.getKey();
			String flag = map_entry.getValue().getProperty("FLAG");
			logger.info("Smsc: " + name + " FLAG: " + flag);
			if (flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
				BlockedRoutes.add(name);
				logger.info(name + " Blocked <404>");
			} else {
				editSmscConfig(name);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
			}
		}
		logger.info("<--- End Initializing Smsc Status -->");
	}

	@Override
	public void reloadStatus() {
		logger.info("<--- Reload Smsc Status -->");
		// initializeLimit();
		for (Entry<String, Properties> map_entry : SmscFlagStatus.entrySet()) {
			String name = map_entry.getKey();
			try {
				String flag = map_entry.getValue().getProperty("FLAG");
				logger.info("Smsc: " + name + " FLAG: " + flag);
				if (flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
					if (!BlockedRoutes.contains(name)) {
						logger.info(name + " Blocked <404>");
						BlockedRoutes.add(name);
						removeSmscConfig(name);
					}
				} else if (flag.equalsIgnoreCase(FlagStatus.ACCOUNT_REMOVED)) {
					logger.info(name + " Removed <303>");
					removeSmscConfig(name);
				} else {
					if (BlockedRoutes.contains(name) || flag.equalsIgnoreCase(FlagStatus.ACCOUNT_REFRESH)) {
						if (BlockedRoutes.contains(name)) {
							BlockedRoutes.remove(name);
							logger.info(name + " < Unblocked >");
						} else {
							logger.info(name + " < Config Refreshed >");
						}
						editSmscConfig(name);
					} else {
						if (flag.contains(FlagStatus.DEFAULT)) {
							// nothing to do
						} else {
							if (flag.contains(FlagStatus.ACCOUNT_ADDED)) {
								logger.info(name + " < New Config Found >");
								editSmscConfig(name);
							} else if (flag.contains(FlagStatus.CLEAR_NONRESP)) {
								logger.info("<-- " + name + " Command to Clear Non Responding --> ");
								String reRoute = map_entry.getValue().getProperty("RR");
								if (reRoute == null || reRoute.equalsIgnoreCase("No")) {
									reRoute = name;
								}
								new Thread(new ClearNonResponding(name, name), "ClearNonResponding").start();
							} else {
								if (GlobalCache.EsmeErrorFlag.containsKey(flag)) {
									String flag_symbol = (String) GlobalCache.EsmeErrorFlag.get(flag);
									String reRoute = map_entry.getValue().getProperty("RR");
									if (reRoute == null || reRoute.equalsIgnoreCase("No")) {
										reRoute = name;
									}
									logger.info("<-- " + name + " Command to Resend Error Responding <" + flag_symbol
											+ "> From <" + reRoute + ">--> ");
									new Thread(new SendErrorResponseSMS(name, reRoute, flag_symbol)).start();
								} else {
									logger.info("<-- " + name + " Unknown Flag Command <" + flag + "> -->");
								}
								// fileUtil.changeSmscParamValue(smscName);
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error(name, e.fillInStackTrace());
			}
		}
		logger.info("<--- End Reload Smsc Status -->");
	}

	@Override
	public Set<String> listNames() {
		return SmscNameMapping.keySet();
	}

	@Override
	public SmscEntry getEntry(int smsc_id) {
		return SmscEntries.get(smsc_id);
	}

	@Override
	public SmscEntry getEntry(String smsc_name) {
		int smsc_id = SmscNameMapping.get(smsc_name);
		return getEntry(smsc_id);
	}

	private String getSmscName(int smsc_id) {
		if (SmscEntries.containsKey(smsc_id)) {
			return SmscEntries.get(smsc_id).getName();
		} else {
			return null;
		}
	}

	public void initializeGlobalCache() {
		logger.info("Initializing Smsc Based Global Vars");
		for (SmscEntry entry : SmscEntries.values()) {
			String name = entry.getName();
			if (entry.getPriorSender() != null && entry.getPriorSender().length() > 0) {
				GlobalCache.SmscPrioritySender.put(name, entry.getPriorSender());
			} else {
				if (GlobalCache.SmscPrioritySender.containsKey(name)) {
					GlobalCache.SmscPrioritySender.remove(name);
				}
			}
			if (entry.isGreekEncode()) {
				GlobalCache.smscGreekEncodeApply.add(name);
			} else {
				if (GlobalCache.smscGreekEncodeApply.contains(name)) {
					GlobalCache.smscGreekEncodeApply.remove(name);
				}
			}
			if (entry.getCustomDlrTime() != null
					&& (entry.getCustomDlrTime().startsWith("+") || entry.getCustomDlrTime().startsWith("-"))) {
				if (entry.getCustomDlrTime().startsWith("-") && entry.getMaxLatency() > 0) {
					GlobalCache.CustomDlrTime.put(name, entry.getCustomDlrTime() + "#" + entry.getMaxLatency());
				} else {
					GlobalCache.CustomDlrTime.put(name, entry.getCustomDlrTime());
				}
			} else {
				if (GlobalCache.CustomDlrTime.containsKey(name)) {
					GlobalCache.CustomDlrTime.remove(name);
				}
			}
			if (entry.getDelayedDlr() > 0) {
				GlobalCache.DelayedDlrRoute.put(name, entry.getDelayedDlr());
				logger.info(name + " DelayedDlr Configured: " + entry.getDelayedDlr());
			} else {
				if (GlobalCache.DelayedDlrRoute.containsKey(name)) {
					GlobalCache.DelayedDlrRoute.remove(name);
				}
			}
			if (entry.getEnforceSmsc() != null && entry.getEnforceSmsc().equalsIgnoreCase("no")) {
				entry.setEnforceSmsc(null);
			}
			if (entry.isDestRestrict()) {
				GlobalCache.DestinationSleepApply.put(name, entry.getMinDestTime());
			} else {
				if (GlobalCache.DestinationSleepApply.containsKey(name)) {
					GlobalCache.DestinationSleepApply.remove(name);
				}
			}
			/*
			 * if (entry.isLooping()) { GlobalCache.smscLoopApplyFlag.put(name, new SmscLooping(entry.getLoopDuration(), entry.getLoopCount())); } else { if
			 * (GlobalCache.smscLoopApplyFlag.containsKey(name)) { GlobalCache.smscLoopApplyFlag.remove(name); } }
			 */
			if (entry.isDownAlert()) {
				GlobalCache.SmscDisconnectionAlert.add(name);
			} else {
				logger.info(name + " Disconnection Alert Disabled ");
				if (GlobalCache.SmscDisconnectionAlert.contains(name)) {
					GlobalCache.SmscDisconnectionAlert.remove(name);
				}
			}
			if (entry.isReplaceSource()) {
				String default_sender_id = entry.getDefaultSource();
				if (default_sender_id != null && default_sender_id.length() > 0) {
					GlobalCache.SmscSenderId.put(name, default_sender_id);
					GlobalCache.SenderIdList.put(name, entry.getAllowedSources());
				}
			} else {
				GlobalCache.SmscSenderId.remove(name);
				GlobalCache.SenderIdList.remove(name);
			}
			// ********** To identify session *************
			int session_id = 0;
			String key = "";
			String system_type = entry.getSystemType();
			if (system_type != null && system_type.trim().length() > 0) {
				key = entry.getIp() + "#" + entry.getPort() + "#" + system_type + "#" + entry.getSystemId() + "#"
						+ entry.getPassword();
			} else {
				key = entry.getIp() + "#" + entry.getPort() + "#" + entry.getSystemId() + "#" + entry.getPassword();
			}
			if (key_session_map.containsKey(key)) {
				session_id = key_session_map.get(key);
			} else {
				session_id = ++MAX_SESSION_ID;
				key_session_map.put(key, session_id);
			}
			// logger.info(name + " sessionId: " + session_id + " MAX: " + MAX_SESSION_ID);
			GlobalCache.SmscSessionId.put(name, session_id);
			Set<String> set = null;
			if (GlobalCache.SessionIdSmscList.containsKey(session_id)) {
				set = GlobalCache.SessionIdSmscList.get(session_id);
			} else {
				set = new HashSet<String>();
			}
			set.add(name);
			GlobalCache.SessionIdSmscList.put(session_id, set);
			// ***********************************************
			if (!GlobalCache.smscwisesequencemap.containsKey(name)) {
				GlobalCache.smscwisesequencemap.put(name, Collections.synchronizedMap(new HashMap<Integer, String>()));
			}
			if (!GlobalCache.smscwiseResponseMap.containsKey(session_id)) {
				GlobalCache.smscwiseResponseMap.put(session_id,
						Collections.synchronizedMap(new HashMap<String, String>()));
			}
		}
		logger.info("Disconnection ALert Enabled: " + GlobalCache.SmscDisconnectionAlert);
		logger.info("End Initializing Smsc Based Global Vars");
	}

	private void editSmscConfig(String name) {
		try {
			if (SmscNameMapping.containsKey(name)) {
				int smsc_id = SmscNameMapping.get(name);
				SmscEntry entry = SmscEntries.get(smsc_id);
				if (entry != null) {
					if (entry.getPriorSender() != null && entry.getPriorSender().length() > 0) {
						GlobalCache.SmscPrioritySender.put(name, entry.getPriorSender());
					} else {
						if (GlobalCache.SmscPrioritySender.containsKey(name)) {
							GlobalCache.SmscPrioritySender.remove(name);
						}
					}
					if (entry.isGreekEncode()) {
						GlobalCache.smscGreekEncodeApply.add(name);
					} else {
						if (GlobalCache.smscGreekEncodeApply.contains(name)) {
							GlobalCache.smscGreekEncodeApply.remove(name);
						}
					}
					if (entry.getCustomDlrTime() != null
							&& (entry.getCustomDlrTime().startsWith("+") || entry.getCustomDlrTime().startsWith("-"))) {
						if (entry.getCustomDlrTime().startsWith("-") && entry.getMaxLatency() > 0) {
							GlobalCache.CustomDlrTime.put(name, entry.getCustomDlrTime() + "#" + entry.getMaxLatency());
						} else {
							GlobalCache.CustomDlrTime.put(name, entry.getCustomDlrTime());
						}
					} else {
						if (GlobalCache.CustomDlrTime.containsKey(name)) {
							GlobalCache.CustomDlrTime.remove(name);
						}
					}
					if (entry.getDelayedDlr() > 0) {
						GlobalCache.DelayedDlrRoute.put(name, entry.getDelayedDlr());
						logger.info(name + " DelayedDlr Configured: " + entry.getDelayedDlr());
					} else {
						if (GlobalCache.DelayedDlrRoute.containsKey(name)) {
							GlobalCache.DelayedDlrRoute.remove(name);
						}
					}
					if (entry.getEnforceSmsc() != null && entry.getEnforceSmsc().equalsIgnoreCase("no")) {
						entry.setEnforceSmsc(null);
					}
					if (entry.isDestRestrict()) {
						GlobalCache.DestinationSleepApply.put(name, entry.getMinDestTime());
					} else {
						if (GlobalCache.DestinationSleepApply.containsKey(name)) {
							GlobalCache.DestinationSleepApply.remove(name);
						}
					}
					/*
					 * if (entry.isLooping()) { GlobalCache.smscLoopApplyFlag.put(name, new SmscLooping(entry.getLoopDuration(), entry.getLoopCount())); } else { if
					 * (GlobalCache.smscLoopApplyFlag.containsKey(name)) { GlobalCache.smscLoopApplyFlag.remove(name); } }
					 */
					/*if (SmscConnections.containsKey(name)) {
						SmscConnections.get(name).resetDestSourceContentLoopingRule();
					}*/
					if (entry.isDownAlert()) {
						GlobalCache.SmscDisconnectionAlert.add(name);
					} else {
						logger.info(name + " Disconnection Alert Disabled ");
						if (GlobalCache.SmscDisconnectionAlert.contains(name)) {
							GlobalCache.SmscDisconnectionAlert.remove(name);
						}
					}
					if (entry.isReplaceSource()) {
						String default_sender_id = entry.getDefaultSource();
						if (default_sender_id != null && default_sender_id.length() > 0) {
							GlobalCache.SmscSenderId.put(name, default_sender_id);
							GlobalCache.SenderIdList.put(name, entry.getAllowedSources());
						}
					} else {
						GlobalCache.SmscSenderId.remove(name);
						GlobalCache.SenderIdList.remove(name);
					}
					// ********** To identify session *************
					int session_id = 0;
					String key = "";
					String system_type = entry.getSystemType();
					if (system_type != null && system_type.trim().length() > 0) {
						key = entry.getIp() + "#" + entry.getPort() + "#" + system_type + "#" + entry.getSystemId()
								+ "#" + entry.getPassword();
					} else {
						key = entry.getIp() + "#" + entry.getPort() + "#" + entry.getSystemId() + "#"
								+ entry.getPassword();
					}
					if (key_session_map.containsKey(key)) {
						session_id = key_session_map.get(key);
					} else {
						session_id = ++MAX_SESSION_ID;
						key_session_map.put(key, session_id);
					}
					// logger.info(name + " sessionId: " + session_id + " MAX: " + MAX_SESSION_ID);
					GlobalCache.SmscSessionId.put(name, session_id);
					Set<String> set = null;
					if (GlobalCache.SessionIdSmscList.containsKey(session_id)) {
						set = GlobalCache.SessionIdSmscList.get(session_id);
					} else {
						set = new HashSet<String>();
					}
					set.add(name);
					GlobalCache.SessionIdSmscList.put(session_id, set);
					// ***********************************************
					if (!GlobalCache.smscwisesequencemap.containsKey(name)) {
						GlobalCache.smscwisesequencemap.put(name,
								Collections.synchronizedMap(new HashMap<Integer, String>()));
					}
					if (!GlobalCache.smscwiseResponseMap.containsKey(session_id)) {
						GlobalCache.smscwiseResponseMap.put(session_id,
								Collections.synchronizedMap(new HashMap<String, String>()));
					}
					// ***********************************************
					if (!SmscConnections.containsKey(name)) {
						SmscConnection smsc = new SmscConnection(entry);
						smsc.setSessionId(session_id);
						if (entry.getBindMode().compareToIgnoreCase("r") != 0) {
							if (!GlobalCache.SmscQueueCache.containsKey(name)) {
								GlobalCache.SmscQueueCache.put(name, new PriorityQueue(Constants.noofqueue));
								smsc.setSMSC_IN((PriorityQueue) GlobalCache.SmscQueueCache.get(name));
							} else {
								smsc.setSMSC_IN((PriorityQueue) GlobalCache.SmscQueueCache.get(name));
							}
						}
						Thread thread = new Thread(smsc);
						smsc.setThread(thread);
						thread.start();
						SmscConnections.put(name, smsc);
					} else {
						SmscConnections.get(name).setSmscEntry(entry);
					}
				} else {
					logger.error(name + " < SmscEntry not found >");
				}
			} else {
				logger.error(name + " < SmscName not found >");
			}
		} catch (Exception e) {
			logger.error(name, e.fillInStackTrace());
		}
	}

	private void removeSmscConfig(String smscName) {
		try {
			if (SmscConnections.containsKey(smscName)) {
				SmscConnection smscConnection = SmscConnections.remove(smscName);
				if (!smscConnection.isTerminated()) {
					smscConnection.stop();
					while (!smscConnection.isTerminated()) {
						logger.info(smscName + " <- Waiting For Connection Termination ->");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}
				} else {
					logger.info("<-- " + smscName + " Connection Inactive --> ");
				}
			} else {
				logger.info("<-- " + smscName + " Connection Unavailable --> ");
			}
			logger.info("Removing Cached Entries For: " + smscName);
			if (GlobalCache.SmscPrioritySender.containsKey(smscName)) {
				GlobalCache.SmscPrioritySender.remove(smscName);
			}
			if (GlobalCache.smscGreekEncodeApply.contains(smscName)) {
				GlobalCache.smscGreekEncodeApply.remove(smscName);
			}
			if (GlobalCache.CustomDlrTime.containsKey(smscName)) {
				GlobalCache.CustomDlrTime.remove(smscName);
			}
			if (GlobalCache.DelayedDlrRoute.containsKey(smscName)) {
				GlobalCache.DelayedDlrRoute.remove(smscName);
			}
			if (GlobalCache.DestinationSleepApply.containsKey(smscName)) {
				GlobalCache.DestinationSleepApply.remove(smscName);
			}
			/*
			 * if (GlobalCache.smscLoopApplyFlag.contains(smscName)) { GlobalCache.smscLoopApplyFlag.remove(smscName); }
			 */
			if (GlobalCache.SmscDisconnectionAlert.contains(smscName)) {
				GlobalCache.SmscDisconnectionAlert.remove(smscName);
			}
			GlobalCache.SmscSenderId.remove(smscName);
			GlobalCache.SenderIdList.remove(smscName);
			if (GlobalCache.SmscSessionId.containsKey(smscName)) {
				int session_id = GlobalCache.SmscSessionId.remove(smscName);
				if (GlobalCache.SessionIdSmscList.containsKey(session_id)) {
					GlobalCache.SessionIdSmscList.get(session_id).remove(smscName);
				}
			}
			if (GlobalCache.smscwisesequencemap.containsKey(smscName)) {
				GlobalCache.smscwisesequencemap.remove(smscName);
			}
			if (GlobalCache.SmscQueueCache.containsKey(smscName)) {
				GlobalCache.SmscQueueCache.remove(smscName);
			}
			logger.info("Removed Cached Entries For: " + smscName);
		} catch (Exception e) {
			logger.error(smscName, e);
		}
	}

	public void stopConnections() {
		logger.info("<-- Stopping Smsc Connections --> ");
		try {
			for (SmscConnection smscConnection : SmscConnections.values()) {
				if (!smscConnection.isTerminated()) {
					smscConnection.stop();
				} else {
					logger.info(smscConnection.getSmscEntry().getName() + " SmscConnection is Inactive ");
				}
			}
			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
			}
			for (SmscConnection smscConnection : SmscConnections.values()) {
				if (!smscConnection.isTerminated()) {
					while (!smscConnection.isTerminated()) {
						logger.info(
								smscConnection.getSmscEntry().getName() + " <- Waiting For Connection Termination ->");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
		}
		SmscConnections.clear();
	}

	@Override
	public void initializeLoopingRules() {
		logger.info("<-- Initializing Smsc Looping Rules -->");
		GlobalCache.SmscLoopingRules.clear();
		String sql = "select * from smsc_looping_rule where active=true";
		PreparedStatement statement = null;
		ResultSet rs = null;
		Connection connection = null;
		SmscLooping entry = null;
		try {
			connection = GlobalCache.connnection_pool_1.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			while (rs.next()) {
				int smscId = rs.getInt("smsc_id");
				String source = rs.getString("sender_id");
				int rerouteSmscId = rs.getInt("reroute_smsc_id");
				boolean includeContent = rs.getBoolean("content");
				int clearCacheOn = rs.getInt("clear_cache_on");
				if (source != null) {
					if (source.trim().length() > 0) {
						source = source.toLowerCase();
					} else {
						source = null;
					}
				}
				if (smscId > 0 && rs.getInt("duration") > 0 && rs.getInt("count") > 0) {
					String smsc_name = getSmscName(smscId);
					if (smsc_name != null) {
						entry = new SmscLooping(smscId, source, rs.getInt("duration"), rs.getInt("count"),
								rerouteSmscId, includeContent, clearCacheOn);
						entry.setSmsc(smsc_name);
						if (rerouteSmscId > 0) {
							entry.setRerouteSmsc(getSmscName(rerouteSmscId));
						}
						GlobalCache.SmscLoopingRules.put(smsc_name, entry);
						if (SmscConnections.containsKey(smsc_name)) {
							SmscConnections.get(smsc_name).resetLoopingRule();
						}
					} else {
						logger.info("Invalid Smsc Looping Rule Configured: " + smscId + " " + source);
					}
				} else {
					logger.info("Invalid Smsc Looping Rule Configured: " + smscId);
				}
			}
		} catch (Exception ex) {
			logger.error(ex + " While Getting smsc_looping_rule entries");
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception ex) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(connection);
		}
		logger.info("Total Looping Rules Configured: " + GlobalCache.SmscLoopingRules.size());
	}

	@Override
	public void initializeLimit() {
		logger.info("Checking For Smsc limit Configuration");
		GlobalCache.SmscSubmitLimit.clear();
		if (!GlobalCache.SubmitLimitResetTime.isEmpty()) {
			try {
				for (Map.Entry<Integer, Timer> t : GlobalCache.SubmitLimitResetTime.entrySet()) {
					t.getValue().cancel();
					logger.info("Submit Limit[" + t.getKey() + "] Reset Task Cancled.");
				}
			} catch (Exception ex) {
				logger.error("", ex.fillInStackTrace());
			}
			GlobalCache.SubmitLimitResetTime.clear();
		}
		java.sql.Statement statement = null;
		java.sql.Connection con = null;
		java.sql.ResultSet rs = null;
		String sql = "select * from smsc_limit";
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			SmscLimit smscLimit = null;
			while (rs.next()) {
				int limit = rs.getInt("sms_limit");
				if (limit > 0) {
					int smsc_id = rs.getInt("smsc_id");
					int reroute_id = rs.getInt("reroute_id");
					String smsc = getSmscName(smsc_id);
					String reroute_smsc = getSmscName(reroute_id);
					String resetTime = rs.getString("reset_time");
					int limit_id = rs.getInt("id");
					if (smsc != null && reroute_smsc != null) {
						smscLimit = new SmscLimit(rs.getInt("id"), smsc_id, limit, rs.getInt("network_id"),
								reroute_smsc, resetTime, rs.getString("alert_number"), rs.getString("alert_email"),
								rs.getString("alert_sender"));
						smscLimit.setSmsc(smsc);
						GlobalCache.SmscSubmitLimit.put(smsc + "#" + rs.getInt("network_id"), smscLimit);
						if (resetTime != null && resetTime.contains(":") && resetTime.length() == 5) {
							logger.info("Submit Limit[" + rs.getInt("id") + "] Reset Time: " + resetTime);
						} else {
							logger.info("Submit Limit[" + rs.getInt("id") + "] Invalid Reset Time: " + resetTime);
							resetTime = "00:05";
						}
						if (LocalTime.now().isBefore(LocalTime.parse(resetTime))) {
							int hour = Integer.parseInt(resetTime.split(":")[0]);
							int minute = Integer.parseInt(resetTime.split(":")[1]);
							java.util.Calendar calendar = java.util.Calendar.getInstance();
							calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
							calendar.set(java.util.Calendar.MINUTE, minute);
							calendar.set(java.util.Calendar.SECOND, 00);
							Timer t = new Timer();
							t.schedule(new TimerTask() {
								public void run() {
									try {
										GlobalCache.SmscSubmitCounter.remove(limit_id);
										GlobalCache.SmscSubmitLimitNotified.remove(limit_id);
										System.out.println("*** SubmitCounter Cache[" + limit_id + "] Cleared["
												+ java.time.LocalTime.now() + "] ***");
									} catch (Exception e) {
										System.out.println(limit_id + ": " + e);
									}
								}
							}, calendar.getTime());
							System.out.println(limit_id + " Reset Submit Counter On -> "
									+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
							GlobalCache.SubmitLimitResetTime.put(limit_id, t);
						} else {
							logger.info("Submit Limit[" + limit_id + "] Reset Time Expired.");
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("initializeLimit()", ex.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
			if (con != null) {
				GlobalCache.connnection_pool_1.putConnection(con);
			}
		}
		logger.info("Smsc limit COnfiguration:" + GlobalCache.SmscSubmitLimit);
	}

	@Override
	public void initSpecialSetting() {
		logger.info("*** Checking For Special Smsc Settings *********");
		java.sql.Connection con = null;
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			String sql = ("select * from specialsmscsetting");
			java.sql.Statement statement = con.createStatement();
			java.sql.ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				SpecialSMSCSetting ss = new SpecialSMSCSetting();
				int smsc_id = rs.getInt("smsc_id");
				String smsc = getSmscName(smsc_id);
				if (smsc != null) {
					ss.setSmsc(smsc);
					ss.setLength(rs.getInt("length"));
					ss.setL_ston(rs.getInt("LSTon"));
					ss.setL_snpi(rs.getInt("LSNpi"));
					ss.setG_ston(rs.getInt("GSTon"));
					ss.setG_snpi(rs.getInt("GSNpi"));
					GlobalCache.specailSMSCsetting.put(smsc, ss);
				} else {
					logger.error("initSpecialSMSCSetting: smscId[" + smsc_id + "] not exist.");
				}
			}
			rs.close();
			statement.close();
		} catch (Exception se) {
			logger.error("initSpecialSMSCSetting()", se.fillInStackTrace());
		} finally {
			GlobalCache.connnection_pool_1.putConnection(con);
			con = null;
		}
		logger.info("Specail Smsc Setting: " + GlobalCache.specailSMSCsetting);
	}

	@Override
	public void initSmscBsfm() {
		logger.info("*** Checking For Smsc Based BSFM *********");
		java.sql.Connection con = null;
		Map<String, Set<String>> SmscBasedBSFM = new HashMap<String, Set<String>>();
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			String sql = ("select * from bsfm_smsc");
			java.sql.Statement statement = con.createStatement();
			java.sql.ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt("id");
				int smsc_id = rs.getInt("smsc_id");
				String smsc = getSmscName(smsc_id);
				String db_source = rs.getString("source");
				String converted_content = null;
				String source = null;
				boolean proceed = false;
				// System.out.println(id + " " + db_source);
				if (smsc != null) {
					if (db_source != null && db_source.length() > 0) {
						source = Converter.getUnicode(db_source.toCharArray());
						String db_content = rs.getString("content");
						if (db_content != null && db_content.length() > 0) {
							converted_content = Converter.getUnicode(db_content.toCharArray());
							if (converted_content.matches(
									"^[A-Za-z0-9 \\r\\n@£$¥èéùìòÇØøÅå\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u00A4\u03A3\u0398\u039EÆæßÉ!\"#$%&'()*+,\\-./:;<=>?¡ÄÖÑÜ§¿äöñüà^{}\\\\\\[~\\]|\u20AC]*$")) {
								String hex_token = "";
								for (char c : converted_content.toCharArray()) {
									if (hex_mapping.containsKey((int) c)) {
										hex_token += hex_mapping.get((int) c);
									} else {
										hex_token += (int) c;
									}
								}
								try {
									converted_content = getContent(hex_token.toCharArray());
									proceed = true;
								} catch (Exception ex) {
									logger.error(
											id + " initSmscBsfm: Invalid content [" + converted_content + "]: " + ex);
									converted_content = null;
								}
							} else {
								proceed = true;
							}
						} else {
							logger.error(id + " initSmscBsfm: Invalid content [" + converted_content + "]");
						}
					} else {
						logger.error(id + " initSmscBsfm: Invalid source [" + db_source + "]");
					}
				} else {
					logger.error(id + " initSmscBsfm: smscId[" + smsc_id + "] not exist.");
				}
				if (proceed) {
					Set<String> set = null;
					if (SmscBasedBSFM.containsKey(smsc + "#" + source.toLowerCase())) {
						set = SmscBasedBSFM.get(smsc + "#" + source.toLowerCase());
					} else {
						set = new HashSet<String>();
					}
					set.add(converted_content.toLowerCase());
					SmscBasedBSFM.put(smsc + "#" + source.toLowerCase(), set);
				}
			}
			rs.close();
			statement.close();
		} catch (Exception se) {
			logger.error("initSmscBsfm()", se.fillInStackTrace());
		} finally {
			GlobalCache.connnection_pool_1.putConnection(con);
			con = null;
		}
		GlobalCache.SmscBasedBSFM.clear();
		GlobalCache.SmscBasedBSFM.putAll(SmscBasedBSFM);
		logger.info("Smsc Based BSFM: " + GlobalCache.SmscBasedBSFM);
	}

	@Override
	public void initEsmeErrorConfig() {
		logger.info("Initializing Esme Error Configuration");
		java.sql.Statement statement = null;
		java.sql.Connection con = null;
		java.sql.ResultSet rs = null;
		String SMSC_Error_code_sql = "select * from smsc_error_code";
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(SMSC_Error_code_sql);
			String Error_name = "", Flag_symbol = "";
			String Flag_val;
			while (rs.next()) {
				Error_name = rs.getString("Error_name");
				Flag_symbol = rs.getString("Flag_symbol");
				Flag_val = rs.getString("Flag_val");
				GlobalCache.EsmeErrorCode.put(Error_name, Flag_symbol);
				GlobalCache.EsmeErrorFlag.put(Flag_val, Flag_symbol);
			}
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqle) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException sqle) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(con);
		}
	}

	@Override
	public void initSignalErrorConfig() {
		logger.info("Initializing Signal Error Codes");
		GlobalCache.SignalingErrorCriteria.clear();
		java.sql.Statement statement = null;
		java.sql.Connection con = null;
		java.sql.ResultSet rs = null;
		String sql = "select * from signal_retry";
		Set<String> signalRoutes = new HashSet<String>();
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				String route = rs.getString("smsc");
				if (route != null && route.length() > 0) {
					int interval = rs.getInt("interval");
					int attempt = rs.getInt("attempt");
					List<Integer> retry_list = new ArrayList<Integer>();
					retry_list.add(attempt);
					retry_list.add(interval);
					String retry_wait_str = rs.getString("retry_wait");
					if (retry_wait_str != null && retry_wait_str.length() > 0) {
						String[] retry_wait_arr = retry_wait_str.split(",");
						for (String retry_wait : retry_wait_arr) {
							try {
								retry_list.add(Integer.parseInt(retry_wait));
							} catch (Exception e) {
								logger.info("Invalid Retry_wait Configured:" + retry_wait);
							}
						}
					}
					String[] users = null;
					if (rs.getString("username") != null && rs.getString("username").length() > 0) {
						users = rs.getString("username").split("\\s*,\\s*");
					}
					if (rs.getString("error_code") != null && rs.getString("error_code").length() > 0) {
						String[] error_codes = rs.getString("error_code").split("\\s*,\\s*");
						for (String error_code : error_codes) {
							if (users != null && users.length > 0) {
								for (String username : users) {
									GlobalCache.SignalingErrorCriteria.put(error_code + "#" + route + "#" + username,
											retry_list.toArray(new Integer[retry_list.size()]));
								}
							} else {
								GlobalCache.SignalingErrorCriteria.put(error_code + "#" + route,
										retry_list.toArray(new Integer[retry_list.size()]));
							}
						}
					}
					signalRoutes.add(route);
				}
			}
			logger.info("SignalingErrorCriteria: " + GlobalCache.SignalingErrorCriteria);
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqle) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException sqle) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(con);
		}
	}

	private void resetBindStatus() {
		logger.info("Reset Bind Status of all routes");
		java.sql.PreparedStatement statement = null;
		java.sql.Connection con = null;
		String sql = "update smsc_status set bound=?,status_code=? where server_id=?";
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			statement = con.prepareStatement(sql);
			statement.setBoolean(1, false);
			statement.setInt(2, Data.ESME_ROK);
			statement.setInt(3, GlobalVars.SERVER_ID);
			statement.executeUpdate();
		} catch (Exception q) {
			logger.error("", q.fillInStackTrace());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(con);
		}
		logger.info("End Reset Bind Status of all routes");
	}

	private String getContent(char[] buffer) throws Exception {
		String unicode = "";
		int code = 0;
		int j = 0;
		char[] unibuffer = new char[buffer.length / 2];
		for (int i = 0; i < buffer.length; i += 2) {
			code += Character.digit(buffer[i], 16) * 16;
			code += Character.digit(buffer[i + 1], 16);
			unibuffer[j++] = (char) code;
			code = 0;
		}
		unicode = new String(unibuffer);
		return unicode;
	}
}
