/*
 * RoutingThread.java
 *
 * Created on 08 April 2004, 16:29
 */
// routing thread routes single% and double% and uae&ksa and commas seperator prefix and walletsystem.
package com.hti.user;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.bsfm.ProfileEntry;
import com.hti.bsfm.SpamFilter;
import com.hti.hlr.GlobalVar;
import com.hti.hlr.HlrRequestHandler;
import com.hti.hlr.RouteObject;
import com.hti.objects.DatabaseDumpObject;
import com.hti.objects.DeliverSMExt;
import com.hti.objects.HTIQueue;
import com.hti.objects.ReportLogObject;
import com.hti.objects.RoutePDU;
import com.hti.thread.BearerBox;
import com.hti.thread.UserWiseContent;
import com.hti.user.dto.DlrSettingEntry;
import com.hti.user.dto.UserEntry;
import com.hti.util.Converter;
import com.hti.util.DistributionGroupManager;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalMethods;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.msgContent.MsgContent;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.PDUException;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.WrongLengthOfStringException;
import com.logica.smpp.util.ByteBuffer;
import com.logica.smpp.util.NotEnoughDataInByteBufferException;
import com.logica.smpp.util.Queue;
import com.logica.smpp.util.TerminatingZeroNotFoundException;

public class RoutingThread implements Runnable {
	private HTIQueue processQueue;
	private String systemId;
	private DatabaseDumpObject database_dump_object = null;
	private Queue hlrQueue;
	private boolean stop;
	private boolean isReceiving = true;
	private Logger logger = LoggerFactory.getLogger("userLogger");
	private String last_destination = null;
	private String last_smsc = null;
	private UserEntry user;
	private DlrSettingEntry dlrSetting;
	// private int MIN_DEST_ADDR_LENGTH = GlobalVars.MIN_DESTINATION_LENGTH;
	private Map<String, List<RoutePDU>> holdPDUQueue = new HashMap<String, List<RoutePDU>>(); // pdu parts waiting queue for spam checking
	private Map<String, Long> waitingPartQueue = new HashMap<String, Long>();
	private List<Integer> countryCodeList = null;
	private String randomSourceNumber;
	private String randomNumber;
	private Map<String, Long> waitingQueue = new HashMap<String, Long>(); // waiting PDUs routed to long Broken Smsc
	private HTIQueue contentQueue;
	// ----------- for content replacement -----------------------
	private String english_alphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private String arabic_alphabets = "وهنملكقفغعظطضصشسزرذدخحجثتبا";
	private String numbers = "1234567890";
	private String special_chars = "!@#%^&*()-+<>?=";
	private Random random = new Random();
	private String ASCII_REGEX = "^[A-Za-z0-9 \\r\\n@£$¥èéùìòÇØøÅå\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u00A4\u03A3\u0398\u039EÆæßÉ!\"#$%&'()*+,\\-./:;<=>?¡ÄÖÑÜ§¿äöñüà^{}\\\\\\[~\\]|\u20AC]*$";
	private Random rand = new Random();
	private Logger tracklogger = LoggerFactory.getLogger("trackLogger");
	private Map<String, Map<Integer, RoutePDU>> fixLongWaitingQueue = new HashMap<String, Map<Integer, RoutePDU>>();
	private Map<String, Long> fixLongWaitingTime = new HashMap<String, Long>();
	private long threadId = 0;
	private com.hti.user.dto.SubmitLimitEntry submitLimitEntry = null;

	public RoutingThread(HTIQueue processQueue, String systemId) throws Exception {
		this.processQueue = processQueue;
		this.systemId = systemId;
		this.user = GlobalVars.userService.getUserEntry(systemId);
		this.dlrSetting = GlobalVars.userService.getDlrSettingEntry(systemId);
		this.threadId = Thread.currentThread().getId();
		if (user.isHlr()) {
			this.hlrQueue = GlobalMethods.getHLRQueueProcess(systemId);
			// isHLR = true;
		}
		if (user.isSkipContent()) {
			logger.info(systemId + "_" + threadId + " Skip Content Enabled.");
		} else {
			if (GlobalCache.UserContentQueueObject.containsKey(systemId)) {
				contentQueue = ((UserWiseContent) GlobalCache.UserContentQueueObject.get(systemId)).getQueue();
			} else {
				contentQueue = new HTIQueue();
				UserWiseContent userContent = new UserWiseContent(systemId, contentQueue);
				new Thread(userContent, systemId + "_Content").start();
				GlobalCache.UserContentQueueObject.put(systemId, userContent);
			}
		}
		if (GlobalCache.UserSubmitLimitEntries.containsKey(systemId)) {
			submitLimitEntry = GlobalCache.UserSubmitLimitEntries.get(systemId);
		}
		logger.info(systemId + "_" + threadId + " < Routing Thread Initialized > ");
	}

	public HTIQueue getProcessQueue() {
		return processQueue;
	}

	public void setProcessQueue(HTIQueue processQueue) {
		this.processQueue = processQueue;
	}

	public void setSubmitLimitEntry(com.hti.user.dto.SubmitLimitEntry submitLimitEntry) {
		this.submitLimitEntry = submitLimitEntry;
	}

	/*
	 * public HTIQueue getHlrQueue() { return hlrQueue; }
	 * 
	 * public void setHlrQueue(HTIQueue hlrQueue) { this.hlrQueue = hlrQueue; }
	 */
	public void refresh() {
		last_destination = null;
		last_smsc = null;
	}

	public void setUserEntry(UserEntry entry) {
		this.user = entry;
		if (user.isHlr()) {
			this.hlrQueue = GlobalMethods.getHLRQueueProcess(systemId);
		}
	}

	public void setDlrSettingEntry(DlrSettingEntry entry) {
		this.dlrSetting = entry;
	}

	public void run() {
		logger.info(systemId + "_" + threadId + " < Routing Thread Started > ");
		int loop_counter = 0;
		while (!stop) {
			try {
				int counter = 0;
				if (!processQueue.isEmpty()) {
					tracklogger.debug(systemId + " processQueue[1]: " + processQueue.size());
					while (!processQueue.isEmpty()) {
						RoutePDU route = (RoutePDU) processQueue.dequeue();
						if (submitLimitEntry != null) {// check submit counter
							Map<Long, Integer> count_map = null;
							long start_time = 0;
							int received_counter = 0;
							if (GlobalCache.UserSubmitCounter.containsKey(systemId)) {
								count_map = GlobalCache.UserSubmitCounter.get(systemId);
								for (Map.Entry<Long, Integer> timeEntry : count_map.entrySet()) {
									start_time = timeEntry.getKey();
									received_counter = timeEntry.getValue();
								}
								if ((System.currentTimeMillis() - start_time) <= (submitLimitEntry.getDuration() * 60
										* 1000)) {
									if (++received_counter <= submitLimitEntry.getCount()) {
										// proceed further
									} else {
										logger.info(systemId + " limit exhausted[" + submitLimitEntry.getCount()
												+ "] duration:" + submitLimitEntry.getDuration());
										if (!GlobalCache.SubmitLimitNotified.contains(user.getId())) {
											new Thread(new com.hti.thread.SubmitLimitAlert(user, submitLimitEntry))
													.start();
											GlobalCache.SubmitLimitNotified.add(user.getId());
										} else {
											// already notified
										}
										if (submitLimitEntry.getRerouteSmscId() > 0) {
											route.setSmsc(submitLimitEntry.getRerouteSmsc());
											route.setGroupId(0);
											route.setRerouted(true);
										} else {
											block(route);
											continue;
										}
									}
								} else {
									logger.info(systemId + " limit Resetting[" + submitLimitEntry.getCount()
											+ "] duration:" + submitLimitEntry.getDuration());
									received_counter = 1;
									start_time = System.currentTimeMillis();
									count_map.clear();
									if (GlobalCache.SubmitLimitNotified.contains(user.getId())) {
										GlobalCache.SubmitLimitNotified.remove(user.getId());
									}
								}
							} else {
								count_map = new HashMap<Long, Integer>();
								received_counter = 1;
								start_time = System.currentTimeMillis();
							}
							count_map.put(start_time, received_counter);
							GlobalCache.UserSubmitCounter.put(systemId, count_map);
						}
						if (user.isOptOut()) {
							String short_code = ((SubmitSM) route.getRequestPDU()).getSourceAddr().getAddress();
							if (short_code.chars().allMatch(x -> Character.isDigit(x))) {
								if (user.getShortCode().contains(short_code)) {
									if (GlobalCache.OptOutFilter.containsKey(short_code)) {
										if (GlobalCache.OptOutFilter.get(short_code).contains(Long.parseLong(
												((SubmitSM) route.getRequestPDU()).getDestAddr().getAddress()))) {
											logger.info(systemId + " Optout Destination Found: "
													+ ((SubmitSM) route.getRequestPDU()).getDestAddr().getAddress()
													+ " For " + short_code);
											block(route);
											continue;
										}
									}
								}
							}
						}
						if (!route.getSmsc().equalsIgnoreCase(GlobalVars.INVALID_DEST_SMSC)) {
							if (user.isFixLongSms()) {
								String destination = ((SubmitSM) route.getRequestPDU()).getDestAddr().getAddress();
								if (((SubmitSM) route.getRequestPDU()).hasSarMsgRefNum()
										&& ((SubmitSM) route.getRequestPDU()).hasSarTotalSegments()
										&& ((SubmitSM) route.getRequestPDU()).hasSarSegmentSeqnum()) {
									String key = destination + "#"
											+ ((SubmitSM) route.getRequestPDU()).getSarMsgRefNum();
									int totalParts = ((SubmitSM) route.getRequestPDU()).getSarTotalSegments();
									int part_number = ((SubmitSM) route.getRequestPDU()).getSarSegmentSeqnum();
									tracklogger.debug(systemId + "[" + route.getHtiMsgId() + "]OutQueue: " + key
											+ " Total:" + totalParts + " Part:" + part_number);
									Map<Integer, RoutePDU> list = null;
									if (fixLongWaitingQueue.containsKey(key)) {
										list = fixLongWaitingQueue.remove(key);
									} else {
										list = new TreeMap<Integer, RoutePDU>();
									}
									list.put(part_number, route);
									if (totalParts == 0 || totalParts == list.size()) {
										tracklogger.debug(systemId + "[" + key + "] All Parts[" + totalParts
												+ "] Received To Build.");
										List<RoutePDU> preparedlist = buildRoutePDU(list.values());
										tracklogger.debug(
												systemId + "[" + key + "] Prepared Parts: " + preparedlist.size());
										for (RoutePDU part : preparedlist) {
											routeAlgo(part);
										}
										fixLongWaitingTime.remove(key);
									} else {
										fixLongWaitingQueue.put(key, list);
										fixLongWaitingTime.put(key,
												System.currentTimeMillis() + GlobalVars.FIX_LONG_WAIT_TIME * 1000);
									}
								} else {
									routeAlgo(route);
								}
							} else {
								routeAlgo(route);
							}
						} else {
							proceed(route, 0);
						}
						if (++counter > 100) {
							tracklogger.debug(systemId + " processQueue[2]: " + processQueue.size());
							break;
						}
					}
				} else {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ex) {
					}
				}
				if (!fixLongWaitingTime.isEmpty()) {
					Iterator<Map.Entry<String, Long>> itr = fixLongWaitingTime.entrySet().iterator();
					while (itr.hasNext()) {
						Map.Entry<String, Long> entry = itr.next();
						if (System.currentTimeMillis() > entry.getValue()) {
							tracklogger.debug(systemId + "[" + entry.getKey() + "] Waiting Time Over to build");
							if (fixLongWaitingQueue.containsKey(entry.getKey())) {
								Map<Integer, RoutePDU> map = fixLongWaitingQueue.remove(entry.getKey());
								for (RoutePDU route : map.values()) {
									routeAlgo(route);
								}
							}
							itr.remove();
						}
					}
				}
				if (processQueue.isEmpty() && !isReceiving) {
					clearHoldQueue(false);
					releaseWaitingQueue();
					stop = true;
				} else {
					// tracklogger.debug("loop_counter: " + loop_counter);
					if (!processQueue.isEmpty()) {
						tracklogger.debug(systemId + " processQueue[3]: " + processQueue.size());
					}
					if (!fixLongWaitingQueue.isEmpty()) {
						tracklogger.debug(systemId + " fixLongWaitingQueue:" + fixLongWaitingQueue.size());
					}
					if (++loop_counter > 100) {
						clearHoldQueue(true);
						checkWaitingQueue();
						loop_counter = 0;
					}
				}
			} catch (Exception e) {
				logger.error(systemId + "_" + threadId, e.fillInStackTrace());
			}
		}
		/*
		 * if (GlobalVar.HlrRequestHandlers.containsKey(systemId)) { GlobalVar.HlrRequestHandlers.remove(systemId).stop(); }
		 */
		logger.info("Routing Thread Stopped for <" + systemId + "_" + threadId + " > Queue:" + processQueue.size()
				+ " waiting:" + waitingQueue.size());
	}

	private List<RoutePDU> buildRoutePDU(java.util.Collection<RoutePDU> set) {
		List<RoutePDU> preparedList = new ArrayList<RoutePDU>();
		String msg_id_list = " ";
		try {
			String joincontent = "";
			boolean unicode = false;
			for (RoutePDU routePDU : set) {
				tracklogger.debug(systemId + " build[" + routePDU.getHtiMsgId() + "]");
				msg_id_list += routePDU.getHtiMsgId() + " ";
				if ((((SubmitSM) routePDU.getRequestPDU()).getDataCoding() == (byte) 8)
						|| (((SubmitSM) routePDU.getRequestPDU()).getDataCoding() == (byte) 245)) {
					unicode = true;
					try {
						joincontent += ((SubmitSM) routePDU.getRequestPDU()).getShortMessage(Data.ENC_UTF16_BE);
					} catch (UnsupportedEncodingException une) {
					}
				} else {
					joincontent += ((SubmitSM) routePDU.getRequestPDU()).getShortMessage();
				}
			}
			tracklogger.debug(systemId + msg_id_list + " Content Length: " + joincontent.length());
			if (joincontent.contains("\r\n")) {
				tracklogger.debug(systemId + msg_id_list + " <- Carriege Return Found ->");
				joincontent = joincontent.replaceAll("\r\n", "\n");
			}
			if (unicode && joincontent.length() > 70) { // check for garbage chars to replace for multipart unicode
				if (!GlobalCache.UnicodeReplacement.isEmpty()) {
					String hexdump = "";
					byte[] buffer = joincontent.getBytes(Data.ENC_UTF16_BE);
					for (int i = 0; i < buffer.length; i++) {
						hexdump += Character.forDigit((buffer[i] >> 4) & 0x0f, 16);
						hexdump += Character.forDigit(buffer[i] & 0x0f, 16);
					}
					hexdump = hexdump.toUpperCase();
					for (String key : GlobalCache.UnicodeReplacement.keySet()) {
						String charhex = GlobalCache.UnicodeReplacement.get(key);
						if (key.length() == 8) {
							if (hexdump.contains(key)) {
								hexdump = hexdump.replaceAll(key, charhex);
							}
						}
					}
					int len = hexdump.length();
					int temp = 0, chars = 4;
					String[] equalStr = new String[len / chars];
					for (int i = 0; i < len; i = i + chars) {
						equalStr[temp] = hexdump.substring(i, i + chars);
						temp++;
					}
					hexdump = "";
					for (int i = 0; i < equalStr.length; i++) {
						if (GlobalCache.UnicodeReplacement.containsKey(equalStr[i])) {
							tracklogger.debug(systemId + msg_id_list + " Unicode Repl Found:" + equalStr[i]);
							hexdump += GlobalCache.UnicodeReplacement.get(equalStr[i]);
						} else {
							hexdump += equalStr[i];
						}
					}
					String prepared_content = Converter.getUnicode(hexdump.toCharArray());
					if (!prepared_content.equalsIgnoreCase(joincontent)) {
						if (!Pattern
								.compile(
										"^[A-Za-z0-9 \\r\\n@£$¥èéùìòÇØøÅå\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u00A4\u03A3\u0398\u039EÆæßÉ!\"#$%&'()*+,\\-./:;<=>?¡ÄÖÑÜ§¿äöñüà^{}\\\\\\[~\\]|\u20AC]*$")
								.matcher(prepared_content).find()) {
							// still unicode
						} else {
							unicode = false;
							joincontent = new String(prepared_content);
							tracklogger.debug(systemId + msg_id_list + " Content Type Changed to 7-bit");
						}
					}
				}
			}
			// create parts as per standard length
			tracklogger.debug(systemId + msg_id_list + " <- Creating Parts as per standard length ->");
			List<String> msg_list = new ArrayList<String>();
			if (unicode) {
				if (joincontent.length() > 70) {
					while (joincontent.length() > 67) {
						msg_list.add(joincontent.substring(0, 67));
						joincontent = joincontent.substring(67, joincontent.length());
					}
				}
				msg_list.add(joincontent);
			} else {
				if (joincontent.length() > 160) {
					while (joincontent.length() > 153) {
						msg_list.add(joincontent.substring(0, 153));
						joincontent = joincontent.substring(153, joincontent.length());
					}
				}
				msg_list.add(joincontent);
			}
			tracklogger.debug(systemId + msg_id_list + " Creating Parts :" + msg_list.size());
			// rebuild submit_sm as per part numbers
			if (msg_list.size() == 1) {
				for (RoutePDU routePDU : set) {
					String short_message = msg_list.remove(0);
					try {
						((SubmitSM) routePDU.getRequestPDU()).setEsmClass((byte) 0);
						if (unicode) {
							((SubmitSM) routePDU.getRequestPDU()).setShortMessage(short_message, Data.ENC_UTF16_BE);
						} else {
							((SubmitSM) routePDU.getRequestPDU()).setShortMessage(short_message, "ISO8859_1");
							((SubmitSM) routePDU.getRequestPDU()).setDataCoding((byte) 0);
						}
						preparedList.add(routePDU);
					} catch (WrongLengthOfStringException e) {
						logger.error(systemId + " " + routePDU.getHtiMsgId(), e);
					} catch (UnsupportedEncodingException e) {
						logger.error(systemId + " " + routePDU.getHtiMsgId(), e);
					} catch (Exception e) {
						logger.error(systemId + " " + routePDU.getHtiMsgId(), e.fillInStackTrace());
					}
					break;
				}
			} else {
				int nofmessage = msg_list.size();
				int rn = rand.nextInt((255 - 10) + 1) + 10;
				int i = 1;
				for (RoutePDU routePDU : set) {
					String short_message = msg_list.remove(0);
					ByteBuffer byteMessage = new ByteBuffer();
					byteMessage.appendByte((byte) 0x05);
					byteMessage.appendByte((byte) 0x00);
					byteMessage.appendByte((byte) 0x03);
					byteMessage.appendByte((byte) rn);
					byteMessage.appendByte((byte) nofmessage);
					byteMessage.appendByte((byte) i);
					if (unicode) {
						try {
							byteMessage.appendString(short_message, Data.ENC_UTF16_BE);
						} catch (UnsupportedEncodingException e) {
							logger.error(routePDU.getHtiMsgId(), e);
						}
					} else {
						byteMessage.appendString(short_message);
						((SubmitSM) routePDU.getRequestPDU()).setDataCoding((byte) 0);
					}
					try {
						((SubmitSM) routePDU.getRequestPDU()).setShortMessage(byteMessage);
						((SubmitSM) routePDU.getRequestPDU()).setEsmClass((byte) 0x40);
						((SubmitSM) routePDU.getRequestPDU()).setSarMsgRefNum((short) rn);
						((SubmitSM) routePDU.getRequestPDU()).setSarSegmentSeqnum((short) i);
						((SubmitSM) routePDU.getRequestPDU()).setSarTotalSegments((short) nofmessage);
					} catch (WrongLengthOfStringException e) {
						logger.error(systemId + " " + routePDU.getHtiMsgId(), e);
					} catch (PDUException e) {
						logger.error(systemId + " " + routePDU.getHtiMsgId(), e);
					} catch (NotEnoughDataInByteBufferException e) {
						logger.error(systemId + " " + routePDU.getHtiMsgId(), e);
					} catch (TerminatingZeroNotFoundException e) {
						logger.error(systemId + " " + routePDU.getHtiMsgId(), e);
					} catch (UnsupportedEncodingException e) {
						logger.error(systemId + " " + routePDU.getHtiMsgId(), e);
					} catch (Exception e) {
						logger.error(systemId + " " + routePDU.getHtiMsgId(), e.fillInStackTrace());
					}
					preparedList.add(routePDU);
					if (msg_list.isEmpty()) {
						break;
					}
					i++;
				}
			}
		} catch (Exception e) {
			logger.error(systemId + " buildRoutePDU(" + msg_id_list + ")", e.fillInStackTrace());
		}
		return preparedList;
	}

	private void releaseWaitingQueue() {
		try {
			if (!waitingQueue.isEmpty()) {
				Iterator<String> wait_itr = waitingQueue.keySet().iterator();
				while (wait_itr.hasNext()) {
					String entry = wait_itr.next();
					if (holdPDUQueue.containsKey(entry)) {
						tracklogger.debug(systemId + " releaseWaitingQueue[" + entry + "]");
						List<RoutePDU> dump_pdu_list = holdPDUQueue.remove(entry);
						for (RoutePDU dump_pdu : dump_pdu_list) {
							dump_pdu.setSmsc(GlobalVars.REJECT_SMSC);
							dump_pdu.setGroupId(0);
							logger.info(dump_pdu.getHtiMsgId() + " Marked As Rejected");
							try {
								proceed(dump_pdu, 0);
							} catch (Exception ex) {
								logger.info(dump_pdu.getHtiMsgId(), ex.fillInStackTrace());
							}
						}
					}
					wait_itr.remove();
				}
			}
		} catch (Exception e) {
			logger.error(systemId + " releaseWaitingQueue", e.fillInStackTrace());
		}
	}

	private void checkWaitingQueue() {
		try {
			if (!waitingQueue.isEmpty()) {
				// logger.info("WaitingQueue :---> " + waitingQueue);
				Iterator<Map.Entry<String, Long>> wait_itr = waitingQueue.entrySet().iterator();
				while (wait_itr.hasNext()) {
					Map.Entry<String, Long> entry = wait_itr.next();
					if (System.currentTimeMillis() >= entry.getValue()) {
						if (holdPDUQueue.containsKey(entry.getKey())) {
							List<RoutePDU> dump_pdu_list = holdPDUQueue.remove(entry.getKey());
							for (RoutePDU dump_pdu : dump_pdu_list) {
								dump_pdu.setSmsc(GlobalVars.REJECT_SMSC);
								dump_pdu.setGroupId(0);
								logger.info(dump_pdu.getHtiMsgId() + " Marked As Rejected");
								try {
									proceed(dump_pdu, 0);
								} catch (Exception ex) {
									logger.info(dump_pdu.getHtiMsgId(), ex.fillInStackTrace());
								}
							}
						}
						wait_itr.remove();
					}
				}
			}
		} catch (Exception e) {
			logger.error(systemId + " checkWaitingQueue", e.fillInStackTrace());
		}
	}

	private void clearHoldQueue(boolean waiting) {
		if (!holdPDUQueue.isEmpty()) // have incomplete parts
		{
			Iterator<Map.Entry<String, List<RoutePDU>>> itr = holdPDUQueue.entrySet().iterator();
			if (waiting) {
				try {
					while (itr.hasNext()) {
						Map.Entry<String, List<RoutePDU>> entry = itr.next();
						tracklogger.debug(systemId + " clearHoldQueue[" + entry.getKey() + "]");
						boolean wait = false;
						for (RoutePDU route : entry.getValue()) {
							if (GlobalCache.LongBrokenExpireRoute.contains(route.getSmsc())) {
								wait = true;
								break;
							}
						}
						if (wait) {
							if (!waitingQueue.containsKey(entry.getKey())) {
								waitingQueue.put(entry.getKey(), System.currentTimeMillis() + (5 * 60 * 1000)); // 5 minutes Waiting
								tracklogger.debug(systemId + " waiting ExpireLongBroken[" + entry.getKey() + "]");
							}
						} else {
							if (waitingPartQueue.containsKey(entry.getKey())) {
								if (System.currentTimeMillis() > waitingPartQueue.get(entry.getKey())) {
									tracklogger.debug(systemId + "[" + entry.getKey() + "] Waiting Time Over");
									waitingPartQueue.remove(entry.getKey());
									try {
										releasePDUs(entry.getValue());
									} catch (Exception ex) {
										logger.error("clearHoldQueue1(" + systemId + ")", ex.fillInStackTrace());
									}
									itr.remove();
								} else {
									tracklogger.debug(systemId + "[" + entry.getKey() + "] Still Waiting");
								}
							} else {
								try {
									releasePDUs(entry.getValue());
								} catch (Exception ex) {
									logger.error("clearHoldQueue2(" + systemId + ")", ex.fillInStackTrace());
								}
								itr.remove();
							}
						}
					}
				} catch (Exception e) {
					logger.error("clearHoldQueue3(" + systemId + ")", e.fillInStackTrace());
				}
			} else {
				while (itr.hasNext()) {
					Map.Entry<String, List<RoutePDU>> entry = itr.next();
					try {
						releasePDUs(entry.getValue());
					} catch (Exception ex) {
						logger.error("clearHoldQueue4(" + systemId + ")", ex.fillInStackTrace());
					}
					itr.remove();
				}
			}
		}
	}

	public void routeAlgo(RoutePDU route) {
		Request request = route.getRequestPDU();
		tracklogger.debug(systemId + " processing[" + ((SubmitSM) request).getEsmClass() + "][" + route.getSmsc()
				+ "]: " + route.getHtiMsgId());
		try {
			String destination = ((SubmitSM) request).getDestAddr().getAddress();
			String senderid = ((SubmitSM) request).getSourceAddr().getAddress();
			// String smsc = route.getSmsc();
			if (route.getSmsLength() > 0) { // to check if message length restriction applied for the route
				tracklogger.debug(systemId + " message length applied: " + route.getHtiMsgId());
				if (((SubmitSM) request).getEsmClass() == (byte) Data.SM_UDH_GSM
						|| ((SubmitSM) request).getEsmClass() == (byte) Data.SM_UDH_GSM_2) {
					route.setSmsc(GlobalVars.REJECT_SMSC);
					route.setGroupId(0);
					// secondry_smsc = AccumulatorBox.REJECT_SMSC;
					logger.error(systemId + "<- Invalid Message Length [ESM: " + ((SubmitSM) request).getEsmClass()
							+ "] -> ");
				} else {
					String content = "";
					if ((((SubmitSM) request).getDataCoding() == (byte) 8)
							|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
						try {
							content = ((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE);
						} catch (UnsupportedEncodingException une) {
						}
					} else {
						content = ((SubmitSM) request).getShortMessage();
					}
					if (content.length() > route.getSmsLength()) {
						route.setSmsc(GlobalVars.REJECT_SMSC);
						route.setGroupId(0);
						logger.error(systemId + "<- Invalid Message Length [" + content.length() + "] -> ");
					}
				}
			}
			tracklogger.debug(route.getHtiMsgId() + " Checking For Distribution ");
			if (!route.getSmsc().equalsIgnoreCase(GlobalVars.INVALID_DEST_SMSC)
					&& !route.getSmsc().equalsIgnoreCase(GlobalVars.REJECT_SMSC)) {
				if (route.getRegisterSmsc() == null || route.getRegisterSmsc().length() <= 0) {
					if (route.getGroupId() > 0) {
						if (GlobalVars.DISTRIBUTION) {
							String smsc = DistributionGroupManager.findMember(route.getGroupId());
							if (smsc != null) {
								route.setSmsc(smsc);
							}
						}
					}
				} else {
					tracklogger
							.debug(route.getHtiMsgId() + "<- Registered Route [" + route.getRegisterSmsc() + "] -> ");
				}
				// route.setChecked(true);
				if (((SubmitSM) request).getEsmClass() == (byte) Data.SM_UDH_GSM
						|| ((SubmitSM) request).getEsmClass() == Data.SM_UDH_GSM_2) // multipart
				{
					List<RoutePDU> pdu_list = null;
					if (holdPDUQueue.containsKey(destination)) {
						pdu_list = holdPDUQueue.remove(destination);
					} else {
						pdu_list = new ArrayList<RoutePDU>();
					}
					int[] parts = getPartDescription(route.getRequestPDU());
					route.getPartDescription().setTotal(parts[0]);
					route.getPartDescription().setPartNumber(parts[1]);
					route.getPartDescription().setReferenceNumber(parts[2]);
					pdu_list.add(route);
					if (parts[0] == 0 || parts[0] == pdu_list.size()) { // either total part not found or All Parts Received
						tracklogger.debug(route.getHtiMsgId() + "[" + destination + "] Received Parts. Total["
								+ parts[0] + "] Number[" + parts[1] + "] Ref[" + parts[2] + "]");
						releasePDUs(pdu_list);
						waitingPartQueue.remove(destination);
					} else { // Wait for other Parts
						tracklogger.debug(route.getHtiMsgId() + "[" + destination + "] Waiting Parts. Total[" + parts[0]
								+ "] Number[" + parts[1] + "] Ref[" + parts[2] + "]");
						holdPDUQueue.put(destination, pdu_list);
						if (!waitingPartQueue.containsKey(destination)) {
							waitingPartQueue.put(destination, System.currentTimeMillis() + 30 * 1000);
						}
					}
				} else {
					if (route.getContentAppender() != null) { // append configured text to content
						tracklogger.debug(route.getHtiMsgId() + " Checking For Content Appender ");
						for (String appender : route.getContentAppender().split(",")) {
							String content = "";
							if ((((SubmitSM) request).getDataCoding() == (byte) 8)
									|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
								try {
									content = ((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE);
								} catch (UnsupportedEncodingException une) {
								}
								if ((content.length() + appender.length()) > 70) { // reject
									route.setSmsc(GlobalVars.REJECT_SMSC);
									route.setGroupId(0);
									logger.info(
											systemId + "[" + route.getHtiMsgId() + "]: Append Content Length Exeeded: "
													+ (content.length() + appender.length()));
								} else {
									logger.debug(systemId + "[" + route.getHtiMsgId() + "]: Content Length["
											+ content.length() + "] Appender Length[" + appender.length() + "]");
									if (appender.contains("^")) {
										content = appender.replaceAll("^.", "") + content;
									} else if (appender.contains("$")) {
										content = content + appender.replaceAll(".$", "");
									}
									((SubmitSM) request).setShortMessage(content, Data.ENC_UTF16_BE);
								}
							} else {
								content = ((SubmitSM) request).getShortMessage();
								if ((content.length() + appender.length()) > 160) { // reject
									route.setSmsc(GlobalVars.REJECT_SMSC);
									route.setGroupId(0);
									logger.info(
											systemId + "[" + route.getHtiMsgId() + "]: Append Content Length Exeeded: "
													+ (content.length() + appender.length()));
								} else {
									if (Pattern.compile(ASCII_REGEX).matcher(appender).find()) {
										logger.debug(systemId + "[" + route.getHtiMsgId() + "]: Content Length["
												+ content.length() + "] Appender Length[" + appender.length() + "]");
										if (appender.contains("^")) {
											content = appender.replaceAll("^.", "") + content;
										} else if (appender.contains("$")) {
											content = content + appender.replaceAll(".$", "");
										}
										((SubmitSM) request).setShortMessage(content);
									} else {
										logger.info(systemId + "[" + route.getHtiMsgId()
												+ "]: Append Content is Non-7bit: " + appender);
									}
								}
							}
						}
					}
					if (!route.getSmsc().equalsIgnoreCase(GlobalVars.REJECT_SMSC)) {
						if (BearerBox.BULKFILTER) {
							tracklogger.debug(route.getHtiMsgId() + " Checking For Spamming ");
							String content = "";
							if ((((SubmitSM) request).getDataCoding() == (byte) 8)
									|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
								try {
									content = ((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE).toLowerCase();
								} catch (UnsupportedEncodingException e) {
									logger.error(e + " Error while spam checking:" + route.getHtiMsgId());
								}
							} else {
								content = ((SubmitSM) request).getShortMessage().toLowerCase();
							}
							ProfileEntry spam_result = SpamFilter.filter(route.getSmsc(), systemId,
									senderid.toLowerCase(), destination, content);
							boolean block = false;
							if (spam_result != null) {
								String rerouteSmsc = null;
								if (spam_result.getRerouteGroupId() > 0) {
									rerouteSmsc = DistributionGroupManager.findRoute(spam_result.getRerouteGroupId());
								} else {
									rerouteSmsc = spam_result.getReroute();
								}
								if (rerouteSmsc != null) {
									route.setSmsc(rerouteSmsc);
									route.setGroupId(0);
									route.setRerouted(true);
									if (spam_result.getForceSenderId() != null) {
										((SubmitSM) route.getRequestPDU()).setSourceAddr((byte) 5, (byte) 0,
												spam_result.getForceSenderId());
										logger.info(route.getHtiMsgId() + " Spam Rule[" + spam_result.getId()
												+ "] Matched. Rerouted: " + rerouteSmsc + " Forced SenderId: "
												+ spam_result.getForceSenderId());
									} else {
										logger.info(route.getHtiMsgId() + " Spam Rule[" + spam_result.getId()
												+ "] Matched. Rerouted: " + rerouteSmsc);
									}
								} else {
									if (spam_result.isReverse()) {
										logger.info(route.getHtiMsgId() + " Spam Rule[" + spam_result.getId()
												+ "] Reverse.");
									} else {
										logger.info(route.getHtiMsgId() + " Spam Rule[" + spam_result.getId()
												+ "] Blocked.");
										block = true;
									}
								}
							}
							if (!block) {
								if (GlobalCache.UserBasedBsfm.contains(user.getId())) {
									tracklogger.debug(route.getHtiMsgId() + " Checking For Malicious Link.");
									if (isMaliciousLink(content)) {
										spam_result = SpamFilter.getUrlFilterEntry();
										block = true;
									}
								}
							}
							if (block) {
								block(route, spam_result);
							} else {
								proceed(route, 0);
							}
						} else {
							proceed(route, 0);
						}
					} else {
						proceed(route, 0);
					}
				}
			} else {
				proceed(route, 0);
			}
		} catch (Exception e) {
			logger.error(systemId + "[" + route.getHtiMsgId() + "]", e.fillInStackTrace());
		}
	}

	private void releasePDUs(List<RoutePDU> pdu_list) throws Exception {
		try {
			// boolean blocked = false;
			// ********** check for content appender **************
			boolean reject = false;
			boolean block = false;
			boolean isReplaceContent = false;
			boolean isRegisterSender = false;
			int registerGroupId = 0;
			String registerSmsc = null;
			boolean isUnicode = false;
			String routed_smsc = null;
			String senderid = null;
			Map<String, String> routingBasedreplacement = null;
			Map<Integer, String> final_parts = null;
			ProfileEntry spam_result = null;
			int total_parts = 0;
			int i = 0;
			for (RoutePDU part_pdu : pdu_list) {
				tracklogger.debug(systemId + " releasing[" + part_pdu.getSmsc() + "]: " + part_pdu.getHtiMsgId());
				Request part_request = part_pdu.getRequestPDU();
				if (++i == 1) {
					isReplaceContent = part_pdu.isReplaceContent();
					if (isReplaceContent) {
						routingBasedreplacement = part_pdu.getReplacement();
					}
					senderid = ((SubmitSM) part_request).getSourceAddr().getAddress().toLowerCase();
					if ((part_pdu.getRegisterSenderId() != null && !part_pdu.getRegisterSenderId().isEmpty())
							&& (part_pdu.getRegisterSmsc() != null && part_pdu.getRegisterSmsc().length() > 0)) {
						if (part_pdu.getRegisterSenderId().contains(senderid)) {
							isRegisterSender = true;
							registerGroupId = part_pdu.getRegGroupId();
							registerSmsc = part_pdu.getRegisterSmsc();
						}
					}
					routed_smsc = part_pdu.getSmsc();
					if ((((SubmitSM) part_request).getDataCoding() == (byte) 8)
							|| (((SubmitSM) part_request).getDataCoding() == (byte) 245)) {
						isUnicode = true;
					}
				}
				// System.out.println(part_pdu.getHtiMsgId() + ": " + ((SubmitSM) part_request).getDestAddr().getAddress());
				if (part_pdu.getContentAppender() == null) {
					break;
				} else {
					// String[] appender = part_pdu.getContentAppender().split(",");
					for (String appender : part_pdu.getContentAppender().split(",")) {
						String content = "";
						if (appender.contains("^")) {
							if (part_pdu.getPartDescription().getPartNumber() == 1) {
							}
						} else if (appender.contains("$")) {
							if (part_pdu.getPartDescription().getPartNumber() == part_pdu.getPartDescription()
									.getTotal()) {
								if (isUnicode) {
									try {
										content = ((SubmitSM) part_request).getShortMessage(Data.ENC_UTF16_BE);
									} catch (UnsupportedEncodingException une) {
									}
									if ((content.length() + appender.length()) > 70) { // reject
										reject = true;
										logger.info(systemId + "[" + part_pdu.getHtiMsgId()
												+ "]: Append Content Length Exeeded: "
												+ (content.length() + appender.length()));
									} else { // append at last
										content = content + appender.replaceAll(".$", "");
										((SubmitSM) part_request).setShortMessage(content, Data.ENC_UTF16_BE);
									}
								} else { // 7-bit
									content = ((SubmitSM) part_request).getShortMessage();
									if ((content.length() + appender.length()) > 159) { // reject
										reject = true;
										logger.info(systemId + "[" + part_pdu.getHtiMsgId()
												+ "]: Append Content Length Exeeded: "
												+ (content.length() + appender.length()));
									} else { // append at last
										if (Pattern.compile(ASCII_REGEX).matcher(appender).find()) {
											logger.debug(systemId + "[" + part_pdu.getHtiMsgId() + "]: Content Length["
													+ content.length() + "] Appender Length[" + appender.length()
													+ "]");
											content = content + appender.replaceAll(".$", "");
											((SubmitSM) part_request).setShortMessage(content);
										} else {
											logger.info(systemId + "[" + part_pdu.getHtiMsgId()
													+ "]: Append Content is Non-7bit: " + appender);
										}
									}
								}
							}
						}
					}
				}
			}
			if (reject) {
				for (RoutePDU part_pdu : pdu_list) {
					part_pdu.setSmsc(GlobalVars.REJECT_SMSC);
					part_pdu.setGroupId(0);
					logger.info(systemId + "[" + part_pdu.getHtiMsgId() + "]: Append Content Length Exeeded");
				}
			} else {
				if (BearerBox.BULKFILTER) {
					// Iterator list_iterator = pdu_list.iterator();
					String part_smsc = "", part_source = "", part_destination = "", part_content = "";
					int x = 0;
					// Request part_request = null;
					for (RoutePDU part_pdu : pdu_list) {
						tracklogger.debug(
								systemId + " Spam Checking[" + part_pdu.getSmsc() + "]: " + part_pdu.getHtiMsgId());
						// part_request = part_pdu.getRequestPDU();
						// ****************************************************
						String content = null;
						if (x == 0) {
							if (part_pdu.getPartDescription().getTotal() == 0) {
								break; // check individual parts
							} else {
								total_parts = part_pdu.getPartDescription().getTotal();
								final_parts = new TreeMap<Integer, String>();
							}
							part_smsc = part_pdu.getSmsc();
							part_source = ((SubmitSM) part_pdu.getRequestPDU()).getSourceAddr().getAddress()
									.toLowerCase();
							part_destination = ((SubmitSM) part_pdu.getRequestPDU()).getDestAddr().getAddress()
									.toLowerCase();
						}
						if (isUnicode) {
							content = ((SubmitSM) part_pdu.getRequestPDU()).getShortMessage(Data.ENC_UTF16_BE);
						} else {
							content = ((SubmitSM) part_pdu.getRequestPDU()).getShortMessage();
						}
						String hex_converted = splitPart(content, isUnicode);
						if (hex_converted != null) {
							if (!final_parts.containsKey(part_pdu.getPartDescription().getPartNumber())) {
								final_parts.put(part_pdu.getPartDescription().getPartNumber(),
										part_pdu.getHtiMsgId() + "#" + hex_converted);
							} else {
								logger.info("Final Parts Already contains "
										+ part_pdu.getPartDescription().getPartNumber());
								final_parts = null;
								break; // check individual parts
							}
						} else {
							logger.info("hex Converted Null ");
							final_parts = null;
							break; // check individual parts
						}
						// ****************************************************
						x++;
					}
					boolean proceed_seperate = true;
					if (final_parts != null && !final_parts.isEmpty()) {
						proceed_seperate = false;
						for (String entry : final_parts.values()) {
							try {
								part_content += Converter.getUnicode(entry.split("#")[1].toCharArray());
							} catch (Exception e) {
								logger.error(systemId, entry + ": " + e);
								proceed_seperate = true;
								break;
							}
						}
						if (!isReplaceContent) {
							final_parts.clear();
							final_parts = null;
						}
					}
					if (proceed_seperate) {
						for (RoutePDU part_pdu : pdu_list) {
							if (isUnicode) {
								part_content += ((SubmitSM) part_pdu.getRequestPDU())
										.getShortMessage(Data.ENC_UTF16_BE);
							} else {
								part_content += ((SubmitSM) part_pdu.getRequestPDU()).getShortMessage();
							}
						}
					}
					spam_result = SpamFilter.filter(part_smsc, systemId, part_source, part_destination, part_content);
					if (spam_result != null) {
						String rerouteSmsc = null;
						if (spam_result.getRerouteGroupId() > 0) {
							rerouteSmsc = DistributionGroupManager.findRoute(spam_result.getRerouteGroupId());
						} else {
							rerouteSmsc = spam_result.getReroute();
						}
						if (rerouteSmsc != null) {
							for (RoutePDU route : pdu_list) {
								route.setGroupId(0);
								route.setSmsc(rerouteSmsc);
								route.setRerouted(true);
								routed_smsc = rerouteSmsc;
								if (spam_result.getForceSenderId() != null) {
									((SubmitSM) route.getRequestPDU()).setSourceAddr((byte) 5, (byte) 0,
											spam_result.getForceSenderId());
									logger.info(route.getHtiMsgId() + " Part Spam Rule[" + spam_result.getId()
											+ "] Matched. Rerouted: " + routed_smsc + " ForcedSenderId: "
											+ spam_result.getForceSenderId());
								} else {
									logger.info(route.getHtiMsgId() + " Part Spam Rule[" + spam_result.getId()
											+ "] Matched. Rerouted: " + routed_smsc);
								}
							}
						} else {
							if (spam_result.isReverse()) {
								logger.info(systemId + " Part Spam Rule[" + spam_result.getId() + "] Reverse.");
							} else {
								block = true;
							}
						}
					}
					if (!block) {
						if (GlobalCache.UserBasedBsfm.contains(user.getId())) {
							tracklogger.debug(systemId + " Part Checking For Malicious Link.");
							if (isMaliciousLink(part_content)) {
								spam_result = SpamFilter.getUrlFilterEntry();
								block = true;
							}
						}
					}
				}
			}
			if (block) {
				final_parts = null;
				while (!pdu_list.isEmpty()) {
					block(pdu_list.remove(0), spam_result);
				}
			} else {
				if (reject) {
					final_parts = null;
					int part_number = 1;
					while (!pdu_list.isEmpty()) {
						proceed(pdu_list.remove(0), part_number);
						part_number++;
					}
				} else {
					if (routed_smsc != null) {
						// ------- Check for smsc based replacement ----
						if (GlobalCache.SmscBasedReplacement.containsKey(routed_smsc)) {
							Map<String, String> SmscBasedReplacement = GlobalCache.SmscBasedReplacement
									.get(routed_smsc);
							if (SmscBasedReplacement != null) {
								if (routingBasedreplacement != null) {
									routingBasedreplacement.putAll(SmscBasedReplacement);
								} else {
									routingBasedreplacement = new HashMap<String, String>(SmscBasedReplacement);
									isReplaceContent = true;
								}
							}
						}
					}
					// logger.info(" " + routingBasedreplacement);
					// ---------------- Check For final replacement -----------
					if (isReplaceContent) {
						if (final_parts != null && !final_parts.isEmpty() && final_parts.size() == total_parts) {
							List<String> msg_id_list = new ArrayList<String>();
							String join_content = "";
							for (String entry : final_parts.values()) {
								String[] id_content = entry.split("#");
								String msg_id = id_content[0];
								join_content += id_content[1];
								msg_id_list.add(msg_id);
							}
							join_content = Converter.getUnicode(join_content.toCharArray());
							boolean isReplaced = false;
							for (String received : routingBasedreplacement.keySet()) {
								if (join_content.toLowerCase().contains(received.toLowerCase())) {
									String to_be_replace = routingBasedreplacement.get(received);
									try {
										to_be_replace = checkReplacement(to_be_replace);
										join_content = join_content.replaceAll("(?i)" + received, to_be_replace);
										isReplaced = true;
										logger.info(systemId + " Replaced[" + isUnicode + "]: " + getHexDump(received)
												+ " -> " + getHexDump(to_be_replace));
									} catch (Exception e) {
										logger.info(e + ":" + received + "-> " + to_be_replace);
									}
								}
							}
							if (isReplaced) {
								int j = 1;
								int reference_number = rand.nextInt((255 - 10) + 1) + 10;
								Map<String, ByteBuffer> msg_id_pdu = new HashMap<String, ByteBuffer>();
								for (String msg_id : msg_id_list) {
									String msg_part = null;
									// System.out.println(msg_id + " join_content[1]:-> " + join_content.length());
									if (j == msg_id_list.size()) {
										if (join_content.trim().length() == 0) {
											join_content = " ";
											// System.out.println(msg_id + " join_content[2]:-> " + join_content.length());
										}
										// System.out.println(msg_id + " join_content[3]:-> " + join_content.length());
										msg_part = join_content;
									} else {
										int index = 0;
										if (isUnicode) {
											index = 67;
										} else {
											index = 153;
										}
										if (join_content.length() >= index) {
											msg_part = join_content.substring(0, index);
											join_content = join_content.substring(index, join_content.length());
										} else {
											// System.out.println(msg_id + " join_content[4]:-> " + join_content.length());
											msg_part = join_content.substring(0, join_content.length());
											join_content = join_content.substring(join_content.length());
										}
									}
									// System.out.println(msg_id + " msgpart:-> " + msg_part.length());
									ByteBuffer byteMessage = new ByteBuffer();
									if (isUnicode) {
										byteMessage.appendByte((byte) 0x05);
										byteMessage.appendByte((byte) 0x00);
										byteMessage.appendByte((byte) 0x03);
										byteMessage.appendByte((byte) reference_number);
										byteMessage.appendByte((byte) total_parts);
										byteMessage.appendByte((byte) j);
										byteMessage.appendString(msg_part, Data.ENC_UTF16_BE);
									} else {
										byteMessage.appendByte((byte) 5);
										byteMessage.appendByte((byte) 0);
										byteMessage.appendByte((byte) 3);
										byteMessage.appendByte((byte) reference_number);
										byteMessage.appendByte((byte) total_parts);
										byteMessage.appendByte((byte) j);
										byteMessage.appendString(msg_part);
									}
									msg_id_pdu.put(msg_id, byteMessage);
									j++;
								}
								for (RoutePDU route : pdu_list) {
									route.setReplaceContent(false);
									route.setSmscContentReplacement(false);
									if (msg_id_pdu.containsKey(route.getHtiMsgId())) {
										((SubmitSM) route.getRequestPDU())
												.setShortMessage(msg_id_pdu.get(route.getHtiMsgId()));
									}
								}
							} else {
								// logger.info("No Replacement Found");
							}
							final_parts.clear();
							final_parts = null;
						} else {
							// logger.info("final parts not Found");
						}
					} else {
						// logger.info("replaceContent is not applicable");
					}
					// ---------------- End Check For replacement -----------
					int part_number = 1;
					if (isRegisterSender) {
						if (registerGroupId > 0) {
							String nextRegisterSmsc = DistributionGroupManager.findRoute(registerGroupId);
							if (nextRegisterSmsc != null) {
								registerSmsc = nextRegisterSmsc;
							}
						}
					}
					while (!pdu_list.isEmpty()) {
						RoutePDU part_pdu = pdu_list.remove(0);
						if (isRegisterSender) {
							part_pdu.setSmsc(registerSmsc);
							part_pdu.setGroupId(0);
							part_pdu.setRegisterSender(true);
							tracklogger.debug(systemId + " Part Registered Sender [" + senderid + "-> "
									+ part_pdu.getSmsc() + " ]: " + part_pdu.getHtiMsgId());
						}
						proceed(part_pdu, part_number);
						part_number++;
					}
				}
			}
		} catch (Exception e) {
			logger.error(systemId + " releasePDUs", e.fillInStackTrace());
		}
	}

	private void proceed(RoutePDU route, int part_number) throws Exception {
		Request request = route.getRequestPDU();
		String destination = ((SubmitSM) request).getDestAddr().getAddress();
		String senderid = ((SubmitSM) request).getSourceAddr().getAddress();
		tracklogger.debug(systemId + " proceed[" + part_number + "]: " + route.getHtiMsgId() + " " + route.getSmsc()
				+ " " + destination + " " + senderid);
		try {
			/*
			 * System.out.println( route.getHtiMsgId() + ":[4]: " + ((SubmitSM) route.getRequestPDU()).getDestAddr().getAddress());
			 */
			if (((SubmitSM) request).getRegisteredDelivery() == 0) {
				if (dlrSetting.isEnforceDlr()) {
					((SubmitSM) request).setRegisteredDelivery((byte) 1);
				}
			}
			if (!route.getSmsc().equalsIgnoreCase(GlobalVars.INVALID_DEST_SMSC)
					&& !route.getSmsc().equalsIgnoreCase(GlobalVars.REJECT_SMSC)) {
				boolean proceedFurther = true;
				if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")) {
					String service_type = ((SubmitSM) request).getServiceType();
					// System.out.println("ServiceType: " + service_type);
					if (service_type != null && service_type.equalsIgnoreCase("CTEST")) {
						proceedFurther = false;
						String smsc = new String(((SubmitSM) request).getDestSubaddress().getBuffer());
						// ((SubmitSM) request).setDestSubaddress(null);
						((SubmitSM) request).setServiceType(null);
						System.out.println("Coverage Testing <" + destination + " " + senderid + "> From -> " + smsc);
						route.setSmsc(smsc);
						route.setGroupId(0);
					}
				}
				if (proceedFurther) { // Normal Routing
					boolean checkAlfa = isAlphaNumeric(((SubmitSM) request).getSourceAddr().getAddress());
					if (!route.isRerouted()) {
						// *********** Check For Numeric Route **************
						if (!checkAlfa) {
							String NumSmsc = route.getNumsmsc();
							if ((NumSmsc != null) && (NumSmsc.length() > 0) && (!NumSmsc.equalsIgnoreCase("NUM"))
									&& (!NumSmsc.equalsIgnoreCase("null"))) {
								route.setSmsc(NumSmsc);
								route.setGroupId(0);
							}
						}
						if (part_number == 0) {
							if ((route.getRegisterSenderId() != null && !route.getRegisterSenderId().isEmpty())
									&& (route.getRegisterSmsc() != null && route.getRegisterSmsc().length() > 0)) {
								if (route.getRegisterSenderId().contains(senderid.toLowerCase())) {
									if (route.getRegGroupId() > 0) {
										String registerSmsc = DistributionGroupManager.findRoute(route.getRegGroupId());
										if (registerSmsc != null) {
											route.setSmsc(registerSmsc);
										} else {
											route.setSmsc(route.getRegisterSmsc());
										}
									} else {
										route.setSmsc(route.getRegisterSmsc());
									}
									route.setRegisterSender(true);
									route.setGroupId(0);
									tracklogger.debug(systemId + " Registered Sender [" + senderid + "-> "
											+ route.getSmsc() + " ]: " + route.getHtiMsgId());
								}
							}
						}
					} else {
						logger.info(systemId + "[" + route.getHtiMsgId() + "]" + "<---- Rerouted By Filter ["
								+ route.getSmsc() + " ]----->");
					}
					if (route.getSenderReplacement() != null) {
						if (route.getSenderReplacement()
								.containsKey(((SubmitSM) request).getSourceAddr().getAddress().toLowerCase())) {
							logger.info(route.getHtiMsgId() + " received Sender["
									+ ((SubmitSM) request).getSourceAddr().getAddress() + "] need Replacement");
							try {
								Long.parseLong(route.getSenderReplacement()
										.get(((SubmitSM) request).getSourceAddr().getAddress().toLowerCase()));
								((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, route.getSenderReplacement()
										.get(((SubmitSM) request).getSourceAddr().getAddress().toLowerCase()));
							} catch (Exception ex) {
								((SubmitSM) request).setSourceAddr((byte) 5, (byte) 0, route.getSenderReplacement()
										.get(((SubmitSM) request).getSourceAddr().getAddress().toLowerCase()));
							}
						}
					}
					if (!route.isRegisterSender()) {
						// ******************* Append Configured Text to SenderId **************
						if (route.getSourceAppender() != null) {
							String appender = route.getSourceAppender();
							String sender = ((SubmitSM) request).getSourceAddr().getAddress();
							if (appender.contains("^")) {
								appender = appender.replaceAll("^.", "");
								sender = appender + sender;
								if (sender.length() > 11) {
									sender = sender.substring(0, 11);
								}
							} else if (appender.contains("$")) {
								appender = appender.replaceAll(".$", "");
								if (sender.length() + appender.length() > 11) {
									int remainingCount = 11 - sender.length();
									int truncateCount = appender.length() - remainingCount;
									sender = sender.substring(0, sender.length() - truncateCount);
								}
								sender = sender + appender;
							}
							logger.debug(
									systemId + "[" + route.getHtiMsgId() + "]" + " Modified SourceAddress: " + sender);
							if (!isAlphaNumeric(sender)) {
								((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, sender);
							} else {
								((SubmitSM) request).setSourceAddr((byte) 5, (byte) 0, sender);
							}
						} else {
							// ********** Force to Replace Sender ID *********************
							String ranNum = "";
							if (checkAlfa) {
								String ForceIDAlpha = route.getForceSIDAlpha();
								if ((ForceIDAlpha != null) && (ForceIDAlpha.length() > 0)
										&& (!ForceIDAlpha.equalsIgnoreCase("SIDALPHA"))
										&& (!ForceIDAlpha.equalsIgnoreCase("null"))) {
									if (ForceIDAlpha.equalsIgnoreCase("UK")) {
										if (part_number == 0) {
											ranNum = "44" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "44" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("US")) {
										if (part_number == 0) {
											ranNum = "1" + generateRandomNumericNumber(11);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(11);
											}
											ranNum = "1" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("SE")) {
										if (part_number == 0) {
											ranNum = "46" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "46" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("IT")) {
										if (part_number == 0) {
											ranNum = "39" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "39" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("FR")) {
										if (part_number == 0) {
											ranNum = "33" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "33" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("DK")) { // Denmark
										if (part_number == 0) {
											ranNum = "45" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "45" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("BR")) { // Brazil
										if (part_number == 0) {
											ranNum = "55" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "55" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("AU")) { // Australia
										if (part_number == 0) {
											ranNum = "61" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "61" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("SG")) { // Singapore
										if (part_number == 0) {
											ranNum = "65" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "65" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("TR")) { // Turkey
										if (part_number == 0) {
											ranNum = "90" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "90" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("IN")) { // India
										if (part_number == 0) {
											ranNum = "91" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "91" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("AF")) { // Afghanistan
										if (part_number == 0) {
											ranNum = "93" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "93" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("BG")) { // Bulgaria
										if (part_number == 0) {
											ranNum = "359" + generateRandomNumericNumber(9);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(9);
											}
											ranNum = "359" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("DE")) {
										if (part_number == 0) {
											ranNum = "49" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "49" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("CM")) { // Cambodia
										checkAlfa = false;
										if (part_number == 0) {
											ranNum = "855" + generateRandomNumericNumber(9);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(9);
											}
											ranNum = "855" + randomNumber;
										}
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("ID")) { // Indonesia
										if (part_number == 0) {
											ranNum = "62" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "62" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("MX")) { // Mexico
										if (part_number == 0) {
											ranNum = "52" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "52" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDAlpha.equalsIgnoreCase("WW")) {
										if (part_number == 0) {
											ranNum = getRandomCountryCode() + "" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomSourceNumber = getRandomCountryCode() + ""
														+ generateRandomNumericNumber(10);
											}
											ranNum = randomSourceNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else {
										boolean forceID = isAlphaNumeric(ForceIDAlpha);
										if (!forceID) {
											checkAlfa = false;
											((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ForceIDAlpha);
										} else {
											((SubmitSM) request).setSourceAddr((byte) 5, (byte) 0, ForceIDAlpha);
										}
									}
								}
							} else {
								String ForceIDNum = route.getForceSIDNum();
								if ((ForceIDNum != null) && (ForceIDNum.length() > 0)
										&& (!ForceIDNum.equalsIgnoreCase("SIDNUM"))
										&& (!ForceIDNum.equalsIgnoreCase("null"))) {
									if (ForceIDNum.equalsIgnoreCase("US")) { // USA
										if (part_number == 0) {
											ranNum = "1" + generateRandomNumericNumber(11);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(11);
											}
											ranNum = "1" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("FR")) { // France
										if (part_number == 0) {
											ranNum = "33" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "33" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("IT")) { // Italy
										if (part_number == 0) {
											ranNum = "39" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "39" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("UK")) { // United Kingdom
										checkAlfa = false;
										if (part_number == 0) {
											ranNum = "44" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "44" + randomNumber;
										}
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("DK")) { // Denmark
										checkAlfa = false;
										if (part_number == 0) {
											ranNum = "45" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "45" + randomNumber;
										}
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("SE")) { // Sweden
										if (part_number == 0) {
											ranNum = "46" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "46" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("DE")) { // Germany
										if (part_number == 0) {
											ranNum = "49" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "49" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("BR")) { // Brazil
										checkAlfa = false;
										if (part_number == 0) {
											ranNum = "55" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "55" + randomNumber;
										}
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("AU")) { // Australia
										checkAlfa = false;
										if (part_number == 0) {
											ranNum = "61" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "61" + randomNumber;
										}
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("SG")) { // Singapore
										checkAlfa = false;
										if (part_number == 0) {
											ranNum = "65" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "65" + randomNumber;
										}
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("TR")) { // Turkey
										checkAlfa = false;
										if (part_number == 0) {
											ranNum = "90" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "90" + randomNumber;
										}
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("IN")) { // India
										checkAlfa = false;
										if (part_number == 0) {
											ranNum = "91" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "91" + randomNumber;
										}
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("AF")) { // Afghanistan
										checkAlfa = false;
										if (part_number == 0) {
											ranNum = "93" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "93" + randomNumber;
										}
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("BG")) { // Bulgaria
										checkAlfa = false;
										if (part_number == 0) {
											ranNum = "359" + generateRandomNumericNumber(9);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(9);
											}
											ranNum = "359" + randomNumber;
										}
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("CM")) { // Cambodia
										checkAlfa = false;
										if (part_number == 0) {
											ranNum = "855" + generateRandomNumericNumber(9);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(9);
											}
											ranNum = "855" + randomNumber;
										}
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("ID")) { // Indonesia
										if (part_number == 0) {
											ranNum = "62" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "62" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("MX")) { // Mexico
										if (part_number == 0) {
											ranNum = "52" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomNumber = "" + generateRandomNumericNumber(10);
											}
											ranNum = "52" + randomNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else if (ForceIDNum.equalsIgnoreCase("WW")) {
										if (part_number == 0) {
											ranNum = getRandomCountryCode() + "" + generateRandomNumericNumber(10);
										} else {
											if (part_number == 1) {
												randomSourceNumber = getRandomCountryCode() + ""
														+ generateRandomNumericNumber(10);
											}
											ranNum = randomSourceNumber;
										}
										checkAlfa = false;
										((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
									} else {
										boolean forceID = isAlphaNumeric(ForceIDNum);
										if (!forceID) {
											checkAlfa = false;
											try {
												int force_sid_num = Integer.parseInt(ForceIDNum);
												if (force_sid_num >= 4 && force_sid_num <= 9) { // assign random number
													// logger.debug(route.getHtiMsgId() + " [" + part_number
													// + "]: Assign Random Number of length " + force_sid_num);
													if (part_number == 0) {
														ranNum = String
																.valueOf(generateRandomNumericNumber(force_sid_num));
													} else {
														if (part_number == 1) {
															randomSourceNumber = String.valueOf(
																	generateRandomNumericNumber(force_sid_num));
														}
														ranNum = randomSourceNumber;
													}
													// logger.debug(route.getHtiMsgId() + " [" + part_number
													// + "]: Assigned Source " + ranNum);
													((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ranNum);
												} else {
													((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ForceIDNum);
												}
											} catch (Exception ex) {
												((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ForceIDNum);
											}
										} else {
											((SubmitSM) request).setSourceAddr((byte) 5, (byte) 0, ForceIDNum);
										}
									}
								}
							}
						}
						if (GlobalVars.DISTRIBUTION && route.getGroupId() > 0) {
							// *********** Distribution For Smsc **************
							String next_smsc = null;
							if (last_destination != null && last_smsc != null) {
								if (last_destination.equalsIgnoreCase(destination)) {
									next_smsc = last_smsc; // Using same connection for same destination
								} else {
									next_smsc = DistributionGroupManager.findRoute(route.getGroupId());
									if (next_smsc == null) {
										next_smsc = last_smsc;
									}
								}
							} else {
								next_smsc = DistributionGroupManager.findRoute(route.getGroupId());
								if (next_smsc == null) {
									next_smsc = last_smsc;
								}
							}
							route.setSmsc(next_smsc);
							last_destination = destination;
							last_smsc = next_smsc;
						}
					}
				}
				boolean isReplaceContent = route.isReplaceContent();
				Map<String, String> routingBasedreplacement = route.getReplacement();
				// ------- Check for smsc based replacement ----
				if (route.isSmscContentReplacement()) {
					if (GlobalCache.SmscBasedReplacement.containsKey(route.getSmsc())) {
						Map<String, String> SmscBasedReplacement = GlobalCache.SmscBasedReplacement
								.get(route.getSmsc());
						if (SmscBasedReplacement != null) {
							if (isReplaceContent) {
								logger.debug(route.getHtiMsgId() + " " + route.getSmsc()
										+ " Routing based Replacement Enabled");
								routingBasedreplacement.putAll(SmscBasedReplacement);
							} else {
								logger.debug(route.getHtiMsgId() + " " + route.getSmsc()
										+ " Routing based Replacement disabled");
								routingBasedreplacement = new HashMap<String, String>(SmscBasedReplacement);
								isReplaceContent = true;
							}
						}
					}
				}
				if (isReplaceContent) {
					// logger.debug(systemId + ":" + routingBasedreplacement);
					String content = null;
					if ((((SubmitSM) request).getDataCoding() == (byte) 8)
							|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
						content = ((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE);
						for (String received : routingBasedreplacement.keySet()) {
							// logger.info(systemId + " received:-> " + getHexDump(received) + " content:-> "
							// + getHexDump(content));
							if (content.toLowerCase().contains(received.toLowerCase())) {
								String to_be_replace = routingBasedreplacement.get(received);
								try {
									to_be_replace = checkReplacement(to_be_replace);
									content = content.replaceAll("(?i)" + received, to_be_replace);
									logger.info(systemId + "[" + route.getHtiMsgId() + "]" + " Replaced: "
											+ getHexDump(received) + " -> " + getHexDump(to_be_replace));
								} catch (Exception e) {
									logger.info(e + "[" + route.getHtiMsgId() + "]" + ":" + received + "-> "
											+ to_be_replace);
								}
							}
						}
						((SubmitSM) request).setShortMessage(content, Data.ENC_UTF16_BE);
					} else {
						content = ((SubmitSM) request).getShortMessage();
						// logger.info("COntent: " + content);
						for (String received : routingBasedreplacement.keySet()) {
							if (content.toLowerCase().contains(received.toLowerCase())) {
								String to_be_replace = routingBasedreplacement.get(received);
								try {
									to_be_replace = checkReplacement(to_be_replace);
									content = content.replaceAll("(?i)" + received, to_be_replace);
									logger.info(systemId + "[" + route.getHtiMsgId() + "]" + " Replaced: " + received
											+ " -> " + to_be_replace);
								} catch (Exception e) {
									logger.info(e + "[" + route.getHtiMsgId() + "]" + ":" + received + "-> "
											+ to_be_replace);
								}
							}
						}
						((SubmitSM) request).setShortMessage(content);
					}
				}
			} else {
				route.setHlr(false);
			}
			route.setReplacement(null);
			if (((SubmitSM) request).getDestAddr().getAddress().startsWith("91")) {
				if (user.getDltDefaultSender() != null && user.getDltDefaultSender().length() > 0) {
					if (!com.hti.dlt.DltFilter.findSender(((SubmitSM) request).getSourceAddr().getAddress())) {
						logger.info(user.getSystemId() + "[" + route.getHtiMsgId() + "] Source Received: "
								+ ((SubmitSM) request).getSourceAddr().getAddress() + " Replaced: "
								+ user.getDltDefaultSender());
						((SubmitSM) request).setSourceAddr((byte) 5, (byte) 0, user.getDltDefaultSender());
					}
				}
			}
			if ((((SubmitSM) request).getDataCoding() == (byte) 8)
					|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
				String content = getHexDump(((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE));
				database_dump_object = new DatabaseDumpObject(content, ((SubmitSM) request).getSourceAddr(),
						((SubmitSM) request).getDestAddr(), ((SubmitSM) request).getSequenceNumber(),
						((SubmitSM) request).getRegisteredDelivery(), ((SubmitSM) request).getEsmClass(),
						((SubmitSM) request).getDataCoding(), route);
			} else {
				database_dump_object = new DatabaseDumpObject(((SubmitSM) request).getShortMessage(),
						((SubmitSM) request).getSourceAddr(), ((SubmitSM) request).getDestAddr(),
						((SubmitSM) request).getSequenceNumber(), ((SubmitSM) request).getRegisteredDelivery(),
						((SubmitSM) request).getEsmClass(), ((SubmitSM) request).getDataCoding(), route);
			}
			/*
			 * System.out.println( route.getHtiMsgId() + ":[5]: " + ((SubmitSM) route.getRequestPDU()).getDestAddr().getAddress());
			 */
			// -------------- put to processing Queues -----------------------
			if (contentQueue != null) {
				contentQueue.enqueue(new MsgContent(route.getHtiMsgId(), route.getUsername(),
						database_dump_object.getContent(), route.getTime(), database_dump_object.getEsm(),
						database_dump_object.getDcs(), route.getPartDescription().getTotal(),
						route.getPartDescription().getPartNumber(), route.getPartDescription().getReferenceNumber()));
			}
			GlobalQueue.smsc_in_log_Queue.enqueue(database_dump_object);
			// omqlogger.debug(systemId + " smsc_in_log: " + database_dump_object.getRoute().getHtiMsgId());
			if (route.isWalletFlag() && route.getCost() <= 0) {
				GlobalQueue.reportLogQueue.enqueue(
						new ReportLogObject(route.getHtiMsgId(), route.getNetworkId() + "", route.getUsername(),
								route.getSmsc(), route.getCost() + "", "M", route.getTime(), destination, senderid));
				database_dump_object.setS_flag("M");
				GlobalQueue.smsc_in_Queue.enqueue(database_dump_object);
				// omqlogger.debug(systemId + " smsc_in_Queue: " + database_dump_object.getRoute().getHtiMsgId());
				logger.info(systemId + " [" + route.getHtiMsgId() + "] M Flagged. Cost:" + route.getCost());
			} else {
				// logger.info(GlobalCache.SkipHlrSenderRoute.toString());
				if (GlobalCache.SkipHlrSenderRoute.containsKey(route.getSmsc())) {
					boolean source_matched = false;
					for (String source_key : GlobalCache.SkipHlrSenderRoute.get(route.getSmsc())) {
						if (Pattern.compile(source_key).matcher(senderid.toLowerCase()).find()) {
							source_matched = true;
							tracklogger.debug(route.getHtiMsgId() + "[" + source_key + "][" + route.getSmsc() + ":"
									+ senderid + "] Skipped Hlr.");
							break;
						}
					}
					if (source_matched) {
						route.setHlr(false);
					}
				}
				if (route.isHlr() && user.isHlr()) {
					tracklogger.debug(systemId + " Processing For HLR: " + route.getHtiMsgId() + " " + route.getSmsc()
							+ " " + destination + " " + senderid);
					database_dump_object.setS_flag("H");
					GlobalQueue.smsc_in_temp_Queue.enqueue(database_dump_object);
					// omqlogger.debug(
					// systemId + " smsc_in_temp_Queue: " + database_dump_object.getRoute().getHtiMsgId());
					if (hlrQueue == null) {
						this.hlrQueue = GlobalMethods.getHLRQueueProcess(systemId);
					}
					hlrQueue.enqueue(new RouteObject(route.getHtiMsgId(), route.getSmsc(), route.getGroupId(),
							route.getCost(), route.getPartDescription().getPartNumber(), senderid, destination,
							route.isRerouted(), route.isMnp(), route.getNetworkId(), route.isMms()));
				} else {
					if (GlobalVars.SMPP_STATUS && GlobalVars.OMQ_PDU_STATUS) {
						if (GlobalVars.HOLD_ON_TRAFFIC) {
							tracklogger.debug(systemId + " Traffic hlodOn: " + route.getHtiMsgId() + " "
									+ route.getSmsc() + " " + destination + " " + senderid);
							database_dump_object.setS_flag("C");
							GlobalQueue.smsc_in_temp_Queue.enqueue(database_dump_object);
						} else {
							tracklogger.debug(systemId + " Interprocess enqueue: " + route.getHtiMsgId() + " "
									+ route.getSmsc() + " " + destination + " " + senderid);
							GlobalQueue.reportLogQueue.enqueue(new ReportLogObject(route.getHtiMsgId(),
									route.getNetworkId() + "", route.getUsername(), route.getSmsc(),
									route.getCost() + "", null, route.getTime(), destination, senderid));
							database_dump_object.setS_flag("C");
							GlobalQueue.smsc_in_Queue.enqueue(database_dump_object);
							// omqlogger.debug(
							// systemId + " smsc_in_Queue: " + database_dump_object.getRoute().getHtiMsgId());
							GlobalQueue.interProcessRequest.enqueue(route);
							// omqlogger.debug(systemId + " Interprocess: " + route.getHtiMsgId());
						}
					} else {
						tracklogger.debug(systemId + " Temporary processing: " + route.getHtiMsgId() + " "
								+ route.getSmsc() + " " + destination + " " + senderid);
						database_dump_object.setS_flag("C");
						GlobalQueue.smsc_in_temp_Queue.enqueue(database_dump_object);
						// omqlogger.debug(
						// systemId + " smsc_in_temp_Queue: " + database_dump_object.getRoute().getHtiMsgId());
					}
				}
				// ---------- put to accepted dlr Queue if configured ----------------
				if (dlrSetting != null && dlrSetting.isAccepted()) {
					String time = new SimpleDateFormat("yyMMddHHmm").format(new Date());
					GlobalQueue.DeliverProcessQueue.enqueue(new DeliverSMExt(route.getHtiMsgId(), route.getUsername(),
							time, time, route.getOriginalSourceAddr(), destination, "ACCEPTD", "000",
							GlobalVars.SERVER_ID));
				}
			}
		} catch (Exception e) {
			logger.error(systemId + " proceed()", e.fillInStackTrace());
		}
		tracklogger.debug(systemId + " end proceed[" + part_number + "]: " + route.getHtiMsgId() + " " + route.getSmsc()
				+ " " + destination + " " + senderid);
	}

	private void block(RoutePDU route) throws Exception {
		try {
			Request request = route.getRequestPDU();
			String destination = ((SubmitSM) request).getDestAddr().getAddress();
			String senderid = ((SubmitSM) request).getSourceAddr().getAddress();
			logger.info(systemId + " Blocking <" + destination + "> <" + senderid + ">");
			if ((((SubmitSM) request).getDataCoding() == (byte) 8)
					|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
				String content = getHexDump(((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE));
				database_dump_object = new DatabaseDumpObject(content, ((SubmitSM) request).getSourceAddr(),
						((SubmitSM) request).getDestAddr(), ((SubmitSM) request).getSequenceNumber(),
						((SubmitSM) request).getRegisteredDelivery(), ((SubmitSM) request).getEsmClass(),
						((SubmitSM) request).getDataCoding(), route);
			} else {
				database_dump_object = new DatabaseDumpObject(((SubmitSM) request).getShortMessage(),
						((SubmitSM) request).getSourceAddr(), ((SubmitSM) request).getDestAddr(),
						((SubmitSM) request).getSequenceNumber(), ((SubmitSM) request).getRegisteredDelivery(),
						((SubmitSM) request).getEsmClass(), ((SubmitSM) request).getDataCoding(), route);
			}
			database_dump_object.setS_flag("B");
			// ------- put to processing Queues -----------------------------------------------
			if (contentQueue != null) {
				contentQueue.enqueue(new MsgContent(route.getHtiMsgId(), route.getUsername(),
						database_dump_object.getContent(), route.getTime(), database_dump_object.getEsm(),
						database_dump_object.getDcs(), route.getPartDescription().getTotal(),
						route.getPartDescription().getPartNumber(), route.getPartDescription().getReferenceNumber()));
			}
			GlobalQueue.smsc_in_log_Queue.enqueue(database_dump_object);
			if (GlobalVars.SMPP_STATUS) {
				GlobalQueue.reportLogQueue.enqueue(
						new ReportLogObject(route.getHtiMsgId(), route.getNetworkId() + "", route.getUsername(),
								route.getSmsc(), route.getCost() + "", "B", route.getTime(), destination, senderid));
				GlobalQueue.smsc_in_Queue.enqueue(database_dump_object);
				// omqlogger.debug(systemId + " smsc_in_Queue: " + database_dump_object.getRoute().getHtiMsgId());
			} else {
				GlobalQueue.smsc_in_temp_Queue.enqueue(database_dump_object);
				// omqlogger.debug(systemId + " smsc_in_temp_Queue: " + database_dump_object.getRoute().getHtiMsgId());
			}
			// ---------- put to accepted dlr Queue if configured ----------------
			if (dlrSetting != null && dlrSetting.isAccepted()) {
				GlobalQueue.DeliverProcessQueue.enqueue(
						new DeliverSMExt(route.getHtiMsgId(), route.getUsername(), route.getTime(), route.getTime(),
								route.getOriginalSourceAddr(), destination, "ACCEPTD", "000", GlobalVars.SERVER_ID));
			}
		} catch (Exception e) {
			logger.error(systemId + " block()", e.fillInStackTrace());
		}
	}

	private void block(RoutePDU route, ProfileEntry spamEntry) throws Exception {
		try {
			Request request = route.getRequestPDU();
			String destination = ((SubmitSM) request).getDestAddr().getAddress();
			String senderid = ((SubmitSM) request).getSourceAddr().getAddress();
			logger.info(systemId + " Spam Detected <" + destination + "> <" + senderid + ">");
			if ((((SubmitSM) request).getDataCoding() == (byte) 8)
					|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
				String content = getHexDump(((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE));
				database_dump_object = new DatabaseDumpObject(content, ((SubmitSM) request).getSourceAddr(),
						((SubmitSM) request).getDestAddr(), ((SubmitSM) request).getSequenceNumber(),
						((SubmitSM) request).getRegisteredDelivery(), ((SubmitSM) request).getEsmClass(),
						((SubmitSM) request).getDataCoding(), route);
			} else {
				database_dump_object = new DatabaseDumpObject(((SubmitSM) request).getShortMessage(),
						((SubmitSM) request).getSourceAddr(), ((SubmitSM) request).getDestAddr(),
						((SubmitSM) request).getSequenceNumber(), ((SubmitSM) request).getRegisteredDelivery(),
						((SubmitSM) request).getEsmClass(), ((SubmitSM) request).getDataCoding(), route);
			}
			database_dump_object.setS_flag("B");
			// ------- put to processing Queues -----------------------------------------------
			if (contentQueue != null) {
				contentQueue.enqueue(new MsgContent(route.getHtiMsgId(), route.getUsername(),
						database_dump_object.getContent(), route.getTime(), database_dump_object.getEsm(),
						database_dump_object.getDcs(), route.getPartDescription().getTotal(),
						route.getPartDescription().getPartNumber(), route.getPartDescription().getReferenceNumber()));
			}
			GlobalQueue.smsc_in_log_Queue.enqueue(database_dump_object);
			if (spamEntry != null) {
				GlobalQueue.spamReportQueue.enqueue(new ReportLogObject(route.getHtiMsgId(), route.getNetworkId() + "",
						route.getUsername(), route.getSmsc(), route.getCost() + "", route.getTime(), destination,
						senderid, spamEntry.getId(), spamEntry.getRemarks()));
			}
			if (GlobalVars.SMPP_STATUS) {
				GlobalQueue.reportLogQueue.enqueue(
						new ReportLogObject(route.getHtiMsgId(), route.getNetworkId() + "", route.getUsername(),
								route.getSmsc(), route.getCost() + "", "B", route.getTime(), destination, senderid));
				GlobalQueue.smsc_in_Queue.enqueue(database_dump_object);
				// omqlogger.debug(systemId + " smsc_in_Queue: " + database_dump_object.getRoute().getHtiMsgId());
			} else {
				GlobalQueue.smsc_in_temp_Queue.enqueue(database_dump_object);
				// omqlogger.debug(systemId + " smsc_in_temp_Queue: " + database_dump_object.getRoute().getHtiMsgId());
			}
			// ---------- put to accepted dlr Queue if configured ----------------
			if (dlrSetting != null && dlrSetting.isAccepted()) {
				GlobalQueue.DeliverProcessQueue.enqueue(
						new DeliverSMExt(route.getHtiMsgId(), route.getUsername(), route.getTime(), route.getTime(),
								route.getOriginalSourceAddr(), destination, "ACCEPTD", "000", GlobalVars.SERVER_ID));
			}
		} catch (Exception e) {
			logger.error(systemId + " block()", e.fillInStackTrace());
		}
	}

	private long generateRandomNumericNumber(int length) {
		long UKNumber = 0;
		try {
			// StringBuffer sb = new StringBuffer();
			String mappedid = "";
			for (int i = length; i > 0; i -= 12) {
				int n = Math.min(12, Math.abs(i));
				mappedid = "" + Math.round(Math.random() * Math.pow(36, n));
				// sb.append(StringUtils.leftPad(Long.toString(Math.round(Math.random() * Math.pow(36, n)), 36), n, '0'));
			}
			if (mappedid.length() > length) {
				mappedid = mappedid.substring(0, length);
			}
			UKNumber = Long.parseLong(mappedid);
		} catch (NumberFormatException e) {
			logger.error(systemId + " generateRandomNumericNumber", e.fillInStackTrace());
		}
		return UKNumber;
	}

	private int getRandomCountryCode() {
		int cc = 91;
		try {
			if (countryCodeList == null) {
				Set<Integer> countryCodeSet = new HashSet<Integer>();
				countryCodeSet.add(1);
				countryCodeSet.add(7);
				countryCodeSet.add(20);
				countryCodeSet.add(27);
				countryCodeSet.add(30);
				countryCodeSet.add(31);
				countryCodeSet.add(32);
				countryCodeSet.add(33);
				countryCodeSet.add(34);
				countryCodeSet.add(36);
				countryCodeSet.add(39);
				countryCodeSet.add(40);
				countryCodeSet.add(41);
				countryCodeSet.add(43);
				countryCodeSet.add(44);
				countryCodeSet.add(45);
				countryCodeSet.add(46);
				countryCodeSet.add(47);
				countryCodeSet.add(48);
				countryCodeSet.add(49);
				countryCodeSet.add(51);
				countryCodeSet.add(52);
				countryCodeSet.add(53);
				countryCodeSet.add(54);
				countryCodeSet.add(55);
				countryCodeSet.add(56);
				countryCodeSet.add(57);
				countryCodeSet.add(58);
				countryCodeSet.add(60);
				countryCodeSet.add(61);
				countryCodeSet.add(62);
				countryCodeSet.add(63);
				countryCodeSet.add(64);
				countryCodeSet.add(65);
				countryCodeSet.add(66);
				countryCodeSet.add(81);
				countryCodeSet.add(82);
				countryCodeSet.add(84);
				countryCodeSet.add(86);
				countryCodeSet.add(90);
				countryCodeSet.add(91);
				countryCodeSet.add(92);
				countryCodeSet.add(93);
				countryCodeSet.add(94);
				countryCodeSet.add(95);
				countryCodeSet.add(98);
				countryCodeSet.add(211);
				countryCodeSet.add(212);
				countryCodeSet.add(213);
				countryCodeSet.add(216);
				countryCodeSet.add(218);
				countryCodeSet.add(220);
				countryCodeSet.add(221);
				countryCodeSet.add(222);
				countryCodeSet.add(223);
				countryCodeSet.add(224);
				countryCodeSet.add(225);
				countryCodeSet.add(226);
				countryCodeSet.add(227);
				countryCodeSet.add(228);
				countryCodeSet.add(229);
				countryCodeSet.add(230);
				countryCodeSet.add(231);
				countryCodeSet.add(232);
				countryCodeSet.add(233);
				countryCodeSet.add(234);
				countryCodeSet.add(235);
				countryCodeSet.add(236);
				countryCodeSet.add(237);
				countryCodeSet.add(238);
				countryCodeSet.add(239);
				countryCodeSet.add(240);
				countryCodeSet.add(241);
				countryCodeSet.add(242);
				countryCodeSet.add(243);
				countryCodeSet.add(244);
				countryCodeSet.add(245);
				countryCodeSet.add(248);
				countryCodeSet.add(249);
				countryCodeSet.add(250);
				countryCodeSet.add(251);
				countryCodeSet.add(252);
				countryCodeSet.add(253);
				countryCodeSet.add(254);
				countryCodeSet.add(255);
				countryCodeSet.add(256);
				countryCodeSet.add(257);
				countryCodeSet.add(258);
				countryCodeSet.add(260);
				countryCodeSet.add(261);
				countryCodeSet.add(262);
				countryCodeSet.add(263);
				countryCodeSet.add(264);
				countryCodeSet.add(265);
				countryCodeSet.add(266);
				countryCodeSet.add(267);
				countryCodeSet.add(268);
				countryCodeSet.add(269);
				countryCodeSet.add(291);
				countryCodeSet.add(297);
				countryCodeSet.add(298);
				countryCodeSet.add(299);
				countryCodeSet.add(350);
				countryCodeSet.add(351);
				countryCodeSet.add(352);
				countryCodeSet.add(353);
				countryCodeSet.add(354);
				countryCodeSet.add(355);
				countryCodeSet.add(356);
				countryCodeSet.add(357);
				countryCodeSet.add(358);
				countryCodeSet.add(359);
				countryCodeSet.add(370);
				countryCodeSet.add(371);
				countryCodeSet.add(372);
				countryCodeSet.add(373);
				countryCodeSet.add(374);
				countryCodeSet.add(375);
				countryCodeSet.add(376);
				countryCodeSet.add(377);
				countryCodeSet.add(378);
				countryCodeSet.add(380);
				countryCodeSet.add(381);
				countryCodeSet.add(382);
				countryCodeSet.add(385);
				countryCodeSet.add(386);
				countryCodeSet.add(387);
				countryCodeSet.add(389);
				countryCodeSet.add(420);
				countryCodeSet.add(421);
				countryCodeSet.add(423);
				countryCodeSet.add(500);
				countryCodeSet.add(501);
				countryCodeSet.add(502);
				countryCodeSet.add(503);
				countryCodeSet.add(504);
				countryCodeSet.add(505);
				countryCodeSet.add(506);
				countryCodeSet.add(507);
				countryCodeSet.add(508);
				countryCodeSet.add(509);
				countryCodeSet.add(590);
				countryCodeSet.add(591);
				countryCodeSet.add(592);
				countryCodeSet.add(593);
				countryCodeSet.add(594);
				countryCodeSet.add(595);
				countryCodeSet.add(597);
				countryCodeSet.add(598);
				countryCodeSet.add(599);
				countryCodeSet.add(670);
				countryCodeSet.add(673);
				countryCodeSet.add(674);
				countryCodeSet.add(675);
				countryCodeSet.add(676);
				countryCodeSet.add(677);
				countryCodeSet.add(678);
				countryCodeSet.add(679);
				countryCodeSet.add(680);
				countryCodeSet.add(681);
				countryCodeSet.add(682);
				countryCodeSet.add(684);
				countryCodeSet.add(685);
				countryCodeSet.add(686);
				countryCodeSet.add(687);
				countryCodeSet.add(689);
				countryCodeSet.add(691);
				countryCodeSet.add(692);
				countryCodeSet.add(850);
				countryCodeSet.add(852);
				countryCodeSet.add(853);
				countryCodeSet.add(855);
				countryCodeSet.add(856);
				countryCodeSet.add(880);
				countryCodeSet.add(882);
				countryCodeSet.add(886);
				countryCodeSet.add(960);
				countryCodeSet.add(961);
				countryCodeSet.add(962);
				countryCodeSet.add(963);
				countryCodeSet.add(964);
				countryCodeSet.add(965);
				countryCodeSet.add(966);
				countryCodeSet.add(967);
				countryCodeSet.add(968);
				countryCodeSet.add(970);
				countryCodeSet.add(971);
				countryCodeSet.add(972);
				countryCodeSet.add(973);
				countryCodeSet.add(974);
				countryCodeSet.add(975);
				countryCodeSet.add(976);
				countryCodeSet.add(977);
				countryCodeSet.add(992);
				countryCodeSet.add(993);
				countryCodeSet.add(994);
				countryCodeSet.add(995);
				countryCodeSet.add(996);
				countryCodeSet.add(998);
				countryCodeList = new ArrayList<Integer>(countryCodeSet);
			}
			cc = countryCodeList.remove(0);
			countryCodeList.add(cc); // put to last position
		} catch (Exception e) {
			logger.error(systemId + " getRandomCountryCode", e.fillInStackTrace());
		}
		return cc;
	}

	private String getHexDump(String getString) {
		String dump = "";
		try {
			// int dataLen = getString.length();
			byte[] buffer = getString.getBytes(Data.ENC_UTF16_BE);
			for (int i = 0; i < buffer.length; i++) {
				dump += Character.forDigit((buffer[i] >> 4) & 0x0f, 16);
				dump += Character.forDigit(buffer[i] & 0x0f, 16);
			}
			buffer = null;
		} catch (Throwable t) {
			dump = "Throwable caught when dumping = " + t;
		}
		return dump;
	}

	public void stop() {
		logger.info("<-- Routing Thread Stopping for <" + systemId + "_" + threadId + " > " + processQueue.size());
		GlobalCache.UserRoutingThread.remove(systemId);
		logger.info(systemId + "_" + threadId + " Routing Thread Object Removed");
		if (GlobalVar.HlrRequestHandlers.containsKey(systemId)) {
			logger.info(systemId + "_" + threadId + " HLR RequestHandler Removing");
			GlobalVar.HlrRequestHandlers.remove(systemId).stop();
		} else {
			logger.info(systemId + "_" + threadId + " No HLR sessions to Block");
		}
		isReceiving = false;
	}

	private boolean isAlphaNumeric(String str) {
		boolean blnAlpha = false;
		char chr[] = null;
		if (str != null) {
			chr = str.toCharArray();
			for (int l = 0; l < chr.length; l++) {
				if (chr[l] >= '0' && chr[l] <= '9') {
					break;
				}
			}
			for (int l = 0; l < chr.length; l++) {
				if ((chr[l] >= 'A' && chr[l] <= 'Z') || (chr[l] >= 'a' && chr[l] <= 'z') || chr[l] == ' ') { // System.out.println(" CHECK for the
																												// alphanumeric encounter >>"+chr[i]);
					blnAlpha = true;
					break;
				}
			}
		} else {
			blnAlpha = true;
		}
		chr = null;
		return (blnAlpha);
	}

	private String splitPart(String content, boolean isUnicode) {
		String to_return = null;
		try {
			String hex_dump = getHexDump(content);
			int header_length = 0;
			if (isUnicode) {
				header_length = Integer.parseInt(hex_dump.substring(0, 2));
			} else {
				header_length = Integer.parseInt(hex_dump.substring(0, 4));
			}
			if (isUnicode) {
				if (header_length == 5) {
					to_return = hex_dump.substring(12, hex_dump.length());
				} else if (header_length == 6) {
					to_return = hex_dump.substring(14, hex_dump.length());
				}
			} else {
				if (header_length == 5) {
					to_return = hex_dump.substring(24, hex_dump.length());
				} else if (header_length == 6) {
					to_return = hex_dump.substring(28, hex_dump.length());
				}
			}
		} catch (Exception une) {
			logger.error(systemId + " splitPart", une.fillInStackTrace());
		}
		return to_return;
	}

	private int[] getPartDescription(Request request) {
		int parts[] = new int[3];
		try {
			parts[0] = ((SubmitSM) request).getSarTotalSegments();
			parts[1] = ((SubmitSM) request).getSarSegmentSeqnum();
			parts[2] = ((SubmitSM) request).getSarMsgRefNum();
		} catch (Exception vlex) {
			String hex_dump = null;
			try {
				hex_dump = getHexDump(((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE));
				int header_length = Integer.parseInt(hex_dump.substring(0, 2));
				// System.out.println("Header Length:" + header_length);
				if (header_length == 5) {
					try {
						parts[0] = Integer.parseInt(hex_dump.substring(8, 10));
					} catch (Exception ex) {
						try {
							parts[0] = Integer.parseInt(hex_dump.substring(8, 10), 16);
						} catch (Exception exx) {
							parts[0] = 0;
						}
					}
					try {
						parts[1] = Integer.parseInt(hex_dump.substring(10, 12));
					} catch (Exception ex) {
						try {
							parts[1] = Integer.parseInt(hex_dump.substring(10, 12), 16);
						} catch (Exception exx) {
							parts[1] = -1;
						}
					}
					try {
						parts[2] = Integer.parseInt(hex_dump.substring(6, 8));
					} catch (Exception ex) {
						try {
							parts[2] = Integer.parseInt(hex_dump.substring(6, 8), 16);
						} catch (Exception exx) {
							parts[2] = 0;
						}
					}
				} else if (header_length == 6) {
					try {
						parts[0] = Integer.parseInt(hex_dump.substring(10, 12));
					} catch (Exception ex) {
						try {
							parts[0] = Integer.parseInt(hex_dump.substring(10, 12), 16);
						} catch (Exception exx) {
							parts[0] = 0;
						}
					}
					try {
						parts[1] = Integer.parseInt(hex_dump.substring(12, 14));
					} catch (Exception ex) {
						try {
							parts[1] = Integer.parseInt(hex_dump.substring(12, 14), 16);
						} catch (Exception exx) {
							parts[1] = -1;
						}
					}
					try {
						parts[2] = Integer.parseInt(hex_dump.substring(8, 10));
					} catch (Exception ex) {
						try {
							parts[2] = Integer.parseInt(hex_dump.substring(8, 10), 16);
						} catch (Exception exx) {
							parts[2] = 0;
						}
					}
				} else {
					System.out.println(systemId + "Unknown Header Found:" + hex_dump.substring(0, 14));
					parts[0] = 0;
					parts[1] = -1;
					parts[2] = 0;
				}
			} catch (Exception une) {
				parts[0] = 0;
				parts[1] = -1;
				parts[2] = 0;
			}
			/*
			 * if (parts[0] == 0 || parts[1] == -1) { logger.error(systemId + " PDU Part Description Error: " + "[" + hex_dump.substring(0, 14) + "]" + " Total: " + parts[0] + " Part_Number:" +
			 * parts[1]); }
			 */
		}
		return parts;
	}

	private boolean isMaliciousLink(String content) {
		if (Pattern.compile("\\b" + "(?i)" + "(http://|https://|www)" + "\\b").matcher(content).find()) {
			for (String word : content.split(" ")) {
				if (Pattern.compile("\\b" + "(?i)" + "(http://|https://|www)" + "\\b").matcher(word).find()) {
					if (Pattern.compile("\\b" + "(?i)" + "(http://|https://)" + "\\b").matcher(word).find()) {
						word = word.substring(word.indexOf("http"));
						if (word.contains("\r\n")) {
							word = word.substring(0, word.indexOf("\r\n"));
						} else if (word.contains("\n")) {
							word = word.substring(0, word.indexOf("\n"));
						}
						/*
						 * try { URL url = new URL(word); word = url.getProtocol() + "://" + url.getHost(); } catch (MalformedURLException e) { // logger.error(systemId + " " + word + " " + e); return
						 * false; }
						 */
					} else {
						if (word.contains("www.")) {
							word = word.substring(word.indexOf("www"));
							if (word.contains("\r\n")) {
								word = word.substring(0, word.indexOf("\r\n"));
							} else if (word.contains("\n")) {
								word = word.substring(0, word.indexOf("\n"));
							}
						} else {
							return false;
						}
						/*
						 * if (word.length() > 3) { try { word = new URL("http://" + word).getHost(); } catch (MalformedURLException e) { // logger.error(systemId + " " + word + " " + e); return
						 * false; } } else { // logger.error(systemId + " Invalid Url:" + word); return false; }
						 */
					}
					if (GlobalCache.ContentWebLinks.containsKey(word)) {
						if (GlobalCache.ContentWebLinks.get(word)) { // Malicious link
							logger.info(systemId + " Malicious link Found: " + word);
							return true;
						}
					} else { // check in table
						com.hti.bsfm.LinkFilter.insertLink(word);
						try {
							Thread.sleep(GlobalVars.URL_CHECK_WAIT_TIME);
						} catch (InterruptedException e) {
						}
						if (com.hti.bsfm.LinkFilter.isMaliciousLink(word)) {
							logger.info(systemId + " Malicious link Found: " + word);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private String checkReplacement(String to_be_replace) {
		String return_text = "";
		while (to_be_replace.contains("(") && to_be_replace.contains("{") && to_be_replace.contains("}")
				&& to_be_replace.contains(")")) {
			String part = to_be_replace.substring(0, to_be_replace.indexOf(")") + 1);
			to_be_replace = to_be_replace.substring(to_be_replace.indexOf(")") + 1);
			String first_part = part.substring(0, part.indexOf("("));
			String last_part = part.substring(part.indexOf(")") + 1, part.length());
			String target = part.substring(part.indexOf("(") + 1, part.indexOf("{"));
			String indicator = part.substring(part.indexOf("{") + 1, part.indexOf("}"));
			char letter = ' ';
			if (indicator.equalsIgnoreCase("C")) { // random charater
				if (target.matches("[" + english_alphabets + "]")) {
					letter = english_alphabets.charAt(random.nextInt(english_alphabets.length()));
				} else if (target.matches("[" + english_alphabets.toLowerCase() + "]")) {
					letter = english_alphabets.toLowerCase().charAt(random.nextInt(english_alphabets.length()));
				}
			} else if (indicator.equalsIgnoreCase("N")) { // random Number
				letter = numbers.charAt(random.nextInt(numbers.length()));
			} else if (indicator.equalsIgnoreCase("A")) { // random Arabic charater
				letter = arabic_alphabets.charAt(random.nextInt(arabic_alphabets.length()));
			} else if (indicator.equalsIgnoreCase("I")) { // random Special char
				letter = special_chars.charAt(random.nextInt(special_chars.length()));
			}
			// System.out.println("character: " + letter);
			if (letter != ' ') {
				part = first_part + String.valueOf(letter) + last_part;
			} else {
				part = first_part + target + last_part;
			}
			return_text += part;
		}
		if (to_be_replace.length() > 0) {
			return_text += to_be_replace;
		}
		return return_text;
	}

	public static ByteBuffer getHead() {
		ByteBuffer bfm = new ByteBuffer();
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 1);
		bfm.appendByte((byte) 57);
		bfm.appendByte((byte) 49);
		bfm.appendByte((byte) 57);
		bfm.appendByte((byte) 56);
		bfm.appendByte((byte) 50);
		bfm.appendByte((byte) 55);
		bfm.appendByte((byte) 51);
		bfm.appendByte((byte) 57);
		bfm.appendByte((byte) 56);
		bfm.appendByte((byte) 56);
		bfm.appendByte((byte) 57);
		bfm.appendByte((byte) 51);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 64);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		return bfm;
	}

	public static ByteBuffer getHeader(int random, int total, int current) {
		ByteBuffer bf = new ByteBuffer();
		bf.appendByte((byte) 5);
		bf.appendByte((byte) 0);
		bf.appendByte((byte) 3);
		bf.appendByte((byte) random);
		bf.appendByte((byte) total);
		bf.appendByte((byte) current);
		return bf;
	}
}
