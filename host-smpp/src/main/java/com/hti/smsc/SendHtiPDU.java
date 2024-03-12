/*
 * SendHtiPDU.java
 *
 * Created on 06 April 2004, 18:19
 */
package com.hti.smsc;

import java.util.Collection;
import java.util.Collections;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dlt.DltFilter;
import com.hti.exception.EntryNotFoundException;
import com.hti.objects.PriorityQueue;
import com.hti.objects.RoutePDU;
import com.hti.objects.SubmittedObj;
import com.hti.smsc.dto.SmscEntry;
import com.hti.smsc.dto.SmscLooping;
import com.hti.user.UserBalance;
import com.hti.util.Constants;
import com.hti.util.Converter;
import com.hti.util.FixedLengthMap;
import com.hti.util.GlobalCache;//done by Rajniprabha 120706
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.TimeoutException;
import com.logica.smpp.WrongSessionStateException;
import com.logica.smpp.pdu.EnquireLink;
import com.logica.smpp.pdu.PDUException;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.ValueNotSetException;
import com.logica.smpp.util.ByteBuffer;
import com.logica.smpp.util.ProcessingThread;

/**
 *
 * @author administrator Class Responsible for Sending PDU to Corrosponding SMSC send PDU and enqueue DatabaseDumpObject in SmscStatus.SMSC_OUT_DATABESE_DUMP if connection Break then Enqeue Response
 *         in to SmscStatus.SMSC_DOWN_QUEUE see ThreadQueueChange
 */
public class SendHtiPDU extends ProcessingThread {
	private Session session;
	private PriorityQueue QUEUE_OUT;
	private Request request;
	private String smscName;
	// private int smscId;
	private boolean connectionBreaked;
	private int NO_OF_QUEUE = Constants.noofqueue;
	private long next_enquire = System.currentTimeMillis() + 5000;
	private Logger logger = LoggerFactory.getLogger(SendHtiPDU.class);
	private Logger tracklogger = LoggerFactory.getLogger("trackLogger");
	private boolean stop;
	private Map<Integer, String> sequence_map;
	private String destPrefix;
	private String tempPrefix;
	private SmscEntry smscEntry;
	private Map<String, Map<Integer, RoutePDU>> waitingQueue = new HashMap<String, Map<Integer, RoutePDU>>();
	private Map<String, Long> waitingQueueTime = new HashMap<String, Long>();
	private Map<String, Long> firstPartSubmitTime = new HashMap<String, Long>();
	// private Map<String, Long> submitPduTime = null;
	private Set<String> waitingParts = new HashSet<String>();
	private Iterator<Map.Entry<String, Long>> waitingIterator = null;
	private int loop_counter = 0;
	private SmscLooping loopingRule;
	// private SmscLooping destSourceContentloopingRule;
	private Map<String, Map<Long, Integer>> LoopingCache = new HashMap<String, Map<Long, Integer>>();
	// private Map<String, Map<Long, Integer>> DestSourceContentLooping = new HashMap<String, Map<Long, Integer>>();
	private Calendar nextLoopingCacheClearTime;
	private static Map<String, FixedLengthMap<String, String>> RecentTemplateMapping = Collections
			.synchronizedMap(new HashMap<String, FixedLengthMap<String, String>>());
	private String DLT_ENABLED_CC = new String(Constants.DLT_PREFIX);

	public SendHtiPDU(Session getsession, PriorityQueue getQUEUE_OUT, SmscEntry smscEntry) {
		this.session = getsession;
		this.smscEntry = smscEntry;
		this.QUEUE_OUT = getQUEUE_OUT;
		this.smscName = smscEntry.getName();
		// this.smscId = smscEntry.getId();
		this.sequence_map = GlobalCache.smscwisesequencemap.get(smscName);
		if (GlobalCache.SmscLoopingRules.containsKey(smscName)) {
			this.loopingRule = GlobalCache.SmscLoopingRules.get(smscName);
		}
		/*
		 * if (GlobalCache.smscLoopApplyFlag.containsKey(smscName)) { this.destSourceContentloopingRule = GlobalCache.smscLoopApplyFlag.get(smscName); }
		 */
		// --------- destination Prefix/Suffix ------------------
		if (smscEntry.getDestPrefix() != null && smscEntry.getDestPrefix().length() > 0) {
			this.destPrefix = smscEntry.getDestPrefix();
			if (destPrefix.contains("+^")) {
				tempPrefix = destPrefix.replace("+^", "");
			} else if (destPrefix.contains("-^")) {
				tempPrefix = destPrefix.replace("-^", "");
			} else if (destPrefix.contains("+$")) {
				tempPrefix = destPrefix.replace("+$", "");
			} else if (destPrefix.contains("-$")) {
				tempPrefix = destPrefix.replace("-$", "");
			} else {
				tempPrefix = smscEntry.getDestPrefix();
			}
		}
		setThreadName(smscName + "-Submission");
		if (loopingRule != null) {
			nextLoopingCacheClearTime = Calendar.getInstance();
			if (loopingRule.getClearCacheOn() > 0) {
				nextLoopingCacheClearTime.add(Calendar.MINUTE, +loopingRule.getClearCacheOn());
			} else {
				nextLoopingCacheClearTime.add(Calendar.MINUTE, +60);
			}
		}
	}

	public SendHtiPDU() {
		setThreadName(smscName + "-Submission");
	}

	public void setSmscEntry(SmscEntry smscEntry) {
		this.smscEntry = smscEntry;
		System.out.println(smscName + " Multipart Support: " + smscEntry.isMultipart());
	}

	public void resetLoopingRule() {
		logger.info(smscName + " Reset LoopingRule: " + loopingRule);
		if (GlobalCache.SmscLoopingRules.containsKey(smscName)) {
			this.loopingRule = GlobalCache.SmscLoopingRules.get(smscName);
		} else {
			this.loopingRule = null;
		}
		logger.info(smscName + " After Reset LoopingRule: " + loopingRule);
	}

	/*
	 * public void resetDestSourceContentLoopingRule() { logger.info(smscName + " Reset destSourceContentLoopingRule: " + destSourceContentloopingRule); if
	 * (GlobalCache.smscLoopApplyFlag.containsKey(smscName)) { this.destSourceContentloopingRule = GlobalCache.smscLoopApplyFlag.get(smscName); } else { this.destSourceContentloopingRule = null; }
	 * logger.info(smscName + " After Reset destSourceContentLoopingRule: " + destSourceContentloopingRule); }
	 */
	@Override
	public void process() {
		if (QUEUE_OUT.isEmpty()) {
			if ((System.currentTimeMillis() >= next_enquire) && !connectionBreaked) {
				sendEnquireLink();
			}
			try {
				Thread.sleep(100);// 150
			} catch (InterruptedException ir) {
			}
		} else {
			if (!stop) {
				if (!QUEUE_OUT.PQueue[1].isEmpty()) {
					while (!QUEUE_OUT.PQueue[1].isEmpty()) {
						if (!connectionBreaked) {
							DoProcess(((RoutePDU) (QUEUE_OUT.PQueue[1].dequeue())), 1);
						} else {
							break;
						}
					}
				} else {
					for (int i = 2; i <= NO_OF_QUEUE; i++) {
						if (!connectionBreaked) {
							if (!QUEUE_OUT.PQueue[i].isEmpty()) {
								DoProcess(((RoutePDU) (QUEUE_OUT.PQueue[i].dequeue())), i);
							}
						} else {
							break;
						}
					}
				}
			}
		}
		if (++loop_counter > 10) {
			loop_counter = 0;
			if (!waitingQueueTime.isEmpty()) {
				tracklogger.debug(smscName + " WaitingQueue: " + waitingQueueTime.keySet());
				Iterator<Map.Entry<String, Long>> part_waiting_iterator = waitingQueueTime.entrySet().iterator();
				while (part_waiting_iterator.hasNext()) {
					Map.Entry<String, Long> entry = part_waiting_iterator.next();
					if (System.currentTimeMillis() > entry.getValue()) {
						tracklogger.debug(entry.getKey() + " Waiting Time Over");
						releasePDUs(waitingQueue.remove(entry.getKey()));
						part_waiting_iterator.remove();
					}
				}
			}
			if (!firstPartSubmitTime.isEmpty()) {
				tracklogger.debug(smscName + " firstPartSubmitTimeQueue: " + firstPartSubmitTime.keySet());
				waitingIterator = firstPartSubmitTime.entrySet().iterator();
				while (waitingIterator.hasNext()) {
					Map.Entry<String, Long> entry = waitingIterator.next();
					if (System.currentTimeMillis() >= entry.getValue()) {
						tracklogger.debug(entry.getKey() + " time is over");
						if (GlobalCache.WaitingForDeliveredPart.containsKey(entry.getKey())) {
							List<RoutePDU> list = GlobalCache.WaitingForDeliveredPart.remove(entry.getKey());
							for (RoutePDU part : list) {
								tracklogger
										.debug(entry.getKey() + ":" + " Part[" + entry.getKey() + "] Going To submit");
								submit(part, 1);
							}
						} else {
							tracklogger.debug(entry.getKey() + " part list not found");
						}
						waitingIterator.remove();
					}
				}
			}
			if (loopingRule != null) {
				if (nextLoopingCacheClearTime.getTime().before(new Date())) { // Clear cache on Each Hour
					logger.info("*** Releasing Looping Cache[" + smscName + "] *******");
					LoopingCache.clear();
					nextLoopingCacheClearTime = Calendar.getInstance();
					if (loopingRule.getClearCacheOn() > 0) {
						nextLoopingCacheClearTime.add(Calendar.MINUTE, +loopingRule.getClearCacheOn());
					} else {
						nextLoopingCacheClearTime.add(Calendar.MINUTE, +60);
					}
				}
			}
		}
	}

	private void sendEnquireLink() {
		logger.debug(smscName + " sendEnquireLink() ");
		try {
			session.enquireLink(new EnquireLink());
			next_enquire = System.currentTimeMillis() + 15000;
		} catch (TimeoutException e) {
			logger.info(smscName + " <---- Connection Timeout (Enquire) ----> ");
		} catch (PDUException | WrongSessionStateException e) {
			logger.info(smscName + " <---- " + e + " (Enquire)----> ");
		} catch (IOException | NullPointerException e) {
			logger.error(smscName + " <---- Connection Breaked (Enquire) ----> ");
			connectionBreaked = true;
			super.setTermException(e);
			if (GlobalCache.SmscDisconnectionAlert.contains(smscName)) {
				if (!GlobalCache.SmscDisconnection.containsKey(smscName)) {
					GlobalCache.SmscDisconnection.put(smscName, System.currentTimeMillis());
				}
			}
			if (GlobalCache.SMSCConnectionStatus.containsKey(smscName)) {
				GlobalCache.SMSCConnectionStatus.put(smscName, false);
			}
			GlobalCache.SmscConnectionSet.remove(smscName);
		}
	}

	private void DoProcess(RoutePDU route, int Queueno) {
		tracklogger.debug(route.getHtiMsgId() + " smscDequeue[" + Queueno + "]: " + route.getSmsc());
		request = route.getRequestPDU();
		boolean dlt_enabled_prefix = false;
		for (String dlt_cc : DLT_ENABLED_CC.split(",")) {
			if (((SubmitSM) request).getDestAddr().getAddress().startsWith(dlt_cc)) {
				dlt_enabled_prefix = true;
				break;
			}
		}
		if (dlt_enabled_prefix) {
			try {
				if (((SubmitSM) request).getExtraOptional((short) 0x1400) == null) {
					String PE_ID = DltFilter.findPeID(((SubmitSM) request).getSourceAddr().getAddress().toLowerCase());
					if (PE_ID != null) {
						// logger.info(route.getHtiMsgId() + " PE_ID Found: " + PE_ID);
						((SubmitSM) request).setExtraOptional((short) 0x1400, new ByteBuffer(PE_ID.getBytes()));
					} else {
						// logger.warn(route.getHtiMsgId() + " PE_ID not Configured.");
					}
				} else {
					// logger.info(route.getHtiMsgId() + " Already Have Dlt PE_ID");
				}
			} catch (Exception e) {
				logger.error(route.getHtiMsgId() + " DLT PE_ID Error", e.getMessage());
			}
		}
		if (((SubmitSM) request).getEsmClass() == (byte) Data.SM_UDH_GSM
				|| ((SubmitSM) request).getEsmClass() == Data.SM_UDH_GSM_2) // multipart
		{
			if (waitingParts.contains(route.getHtiMsgId())) {
				tracklogger.debug(route.getHtiMsgId() + " was in waiting state");
				submit(route, Queueno);
			} else {
				String destination = ((SubmitSM) request).getDestAddr().getAddress();
				int total_parts = 0;
				int part_number = 0;
				int reference_number = 0;
				if (route.getPartDescription().getTotal() > 0 && route.getPartDescription().getPartNumber() > 0
						&& route.getPartDescription().getReferenceNumber() > 0) {
					total_parts = route.getPartDescription().getTotal();
					part_number = route.getPartDescription().getPartNumber();
					reference_number = route.getPartDescription().getReferenceNumber();
				} else {
					int[] part_description = getPartDescription(request);
					total_parts = part_description[0];
					part_number = part_description[1];
					reference_number = part_description[2];
					route.getPartDescription().setTotal(total_parts);
					route.getPartDescription().setPartNumber(part_number);
					route.getPartDescription().setReferenceNumber(reference_number);
				}
				Map<Integer, RoutePDU> pdu_list = null;
				if (waitingQueue.containsKey(destination + "#" + reference_number)) {
					pdu_list = waitingQueue.remove(destination + "#" + reference_number);
				} else {
					pdu_list = new TreeMap<Integer, RoutePDU>();
				}
				if (pdu_list.containsKey(part_number)) {
					tracklogger.debug(route.getHtiMsgId() + "[" + destination + "#" + reference_number
							+ "] Already Have Part: " + part_number);
					waitingQueue.put(destination + "#" + reference_number, pdu_list);
					submit(route, 1);
				} else {
					pdu_list.put(part_number, route);
					if (total_parts == 0 || pdu_list.size() == total_parts) { // all parts received
						tracklogger.debug(route.getHtiMsgId() + "[" + destination + "#" + reference_number
								+ "] All Parts Received");
						waitingQueueTime.remove(destination + "#" + reference_number);
						releasePDUs(pdu_list);
					} else {
						tracklogger.debug(route.getHtiMsgId() + "[" + destination + "#" + reference_number
								+ "] Waiting For Other Parts");
						waitingQueue.put(destination + "#" + reference_number, pdu_list);
						waitingQueueTime.put(destination + "#" + reference_number,
								System.currentTimeMillis() + (30 * 1000)); // 30 second waiting for other parts
					}
				}
				if (!QUEUE_OUT.PQueue[Queueno].getFlag()) {
					QUEUE_OUT.PQueue[Queueno].setTrue();
				}
			}
		} else {
			if (dlt_enabled_prefix) {
				tracklogger.debug(
						route.getHtiMsgId() + " dltCheck: " + route.getUsername() + " [" + route.getSmsc() + "]");
				try {
					if (((SubmitSM) request).getExtraOptional((short) 0x1401) == null) {
						if (((SubmitSM) request).getExtraOptional((short) 0x1400) == null) {
							tracklogger.debug(route.getHtiMsgId() + " PE_ID not Found For TemplateId.");
						} else {
							String PE_ID = new String(
									((SubmitSM) request).getExtraOptional((short) 0x1400).getData().getBuffer())
											.substring(4);
							String content = null;
							if ((((SubmitSM) request).getDataCoding() == (byte) 8)
									|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
								try {
									content = ((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE);
								} catch (Exception e) {
									logger.error(e + " Error while dlt checking:" + route.getHtiMsgId());
								}
							} else {
								content = ((SubmitSM) request).getShortMessage();
							}
							if (content != null && PE_ID != null) {
								String templateId = null;
								if (RecentTemplateMapping.containsKey(PE_ID)) {
									if (RecentTemplateMapping.get(PE_ID).containsKey(content)) {
										templateId = RecentTemplateMapping.get(PE_ID).get(content);
										tracklogger.debug(smscName + "[" + route.getHtiMsgId()
												+ "] TemplateId Found[Local]: " + templateId);
									}
								}
								if (templateId == null) {
									templateId = DltFilter.findTemplateID(PE_ID, content);
								}
								if (templateId != null) {
									// logger.info(route.getHtiMsgId() + " TemplateId Found: "+ templateId);
									((SubmitSM) request).setExtraOptional((short) 0x1401,
											new ByteBuffer(templateId.getBytes()));
									if (RecentTemplateMapping.containsKey(PE_ID)) {
										RecentTemplateMapping.get(PE_ID).put(content, templateId);
									} else {
										FixedLengthMap<String, String> recentTemplates = new FixedLengthMap<String, String>(
												100);
										recentTemplates.put(content, templateId);
										RecentTemplateMapping.put(PE_ID, recentTemplates);
									}
								} else {
									// logger.warn(route.getHtiMsgId() + " Template_ID not Configured.");
								}
							}
						}
					} else {
						// logger.info(route.getHtiMsgId() + " Already Have Dlt Template_ID");
					}
				} catch (Exception e) {
					logger.error(route.getHtiMsgId() + " DLT Error", e.getMessage());
				}
				tracklogger.debug(
						route.getHtiMsgId() + " dltCheckEnd: " + route.getUsername() + " [" + route.getSmsc() + "]");
			}
			// ----------- dlt check end ------------------
			boolean looping = false;
			if (loopingRule != null) {
				if (GlobalCache.SkipLoopingUsers.contains(route.getUsername())) {
					tracklogger.debug(route.getHtiMsgId() + " Looping Rule[" + smscName + "] Disabled.["
							+ route.getUsername() + "]");
				} else {
					boolean skiplooping = false;
					String source = ((SubmitSM) request).getSourceAddr().getAddress();
					boolean includeSource = false;
					if (loopingRule.getSenderId() != null && loopingRule.getSenderId().length() > 0) {
						boolean source_matched = false;
						for (String source_key : loopingRule.getSenderId().split(",")) {
							// System.out.println(source_key + " Matching Try: " + source);
							if (Pattern.compile(source_key).matcher(source.toLowerCase()).find()) {
								tracklogger.debug(smscName + "[" + route.getHtiMsgId() + "] " + source_key
										+ " matched Looping Source: " + source);
								source_matched = true; // continue looping
								break;
							}
						}
						if (!source_matched) {
							skiplooping = true;
						} else {
							includeSource = true;
						}
					}
					if (!skiplooping) {
						String same_packet = ((SubmitSM) request).getDestAddr().getAddress();
						if (includeSource) {
							same_packet += source.toLowerCase();
						}
						if (loopingRule.isContent()) {
							String content = "";
							if ((((SubmitSM) request).getDataCoding() == (byte) 8)
									|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
								try {
									content = ((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE);
								} catch (UnsupportedEncodingException e) {
									logger.error(smscName + " Content Error: " + route.getHtiMsgId());
								}
							} else {
								content = ((SubmitSM) request).getShortMessage();
							}
							same_packet += content.toLowerCase();
						}
						Map<Long, Integer> timeCounterMap = null;
						if (LoopingCache.containsKey(same_packet)) {
							timeCounterMap = LoopingCache.get(same_packet);
							long out_time = 0;
							int received_counter = 0;
							for (Map.Entry<Long, Integer> timeEntry : timeCounterMap.entrySet()) {
								out_time = timeEntry.getKey();
								received_counter = timeEntry.getValue();
							}
							long time_limit = (loopingRule.getDuration() * 1000) + out_time;
							// long current_time = System.currentTimeMillis();
							if (System.currentTimeMillis() <= time_limit) { // received in time limit. Need to check counter
								if (++received_counter > loopingRule.getCount()) { // limit exceeded. Need to Dump
									tracklogger.debug(smscName + "[" + route.getHtiMsgId() + "] " + " Looping Found < "
											+ source + " > Rerouted: " + loopingRule.getRerouteSmsc());
									route.setSmsc(loopingRule.getRerouteSmsc());
									route.setGroupId(0);
									GlobalQueue.interProcessManage.enqueue(route);
									looping = true;
								}
								timeCounterMap.put(out_time, received_counter);
							} else { // received after time limit
								timeCounterMap.clear();
								timeCounterMap.put(System.currentTimeMillis(), 1); // Reset info
							}
						} else { // first time received
							timeCounterMap = new HashMap<Long, Integer>();
							timeCounterMap.put(System.currentTimeMillis(), 1);
						}
						LoopingCache.put(same_packet, timeCounterMap);
					}
				}
			}
			if (!looping) {
				String source = ((SubmitSM) request).getSourceAddr().getAddress();
				if (GlobalCache.SmscBasedBSFM.containsKey(smscName + "#" + source.toLowerCase())) {
					tracklogger.debug(
							smscName + "[" + route.getHtiMsgId() + "] " + " Checking For BSFM  < " + source + " >");
					String content = "";
					if ((((SubmitSM) request).getDataCoding() == (byte) 8)
							|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
						try {
							content = ((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE);
						} catch (UnsupportedEncodingException e) {
							logger.error(smscName + " Content Error: " + route.getHtiMsgId());
						}
					} else {
						content = ((SubmitSM) request).getShortMessage();
					}
					boolean submit = false;
					for (String entry_content : GlobalCache.SmscBasedBSFM.get(smscName + "#" + source.toLowerCase())) {
						String filter = entry_content.replaceAll("[()+$^*?{}\\[\\]]", " ").replaceAll("#variable#",
								"[\\\\p{L}\\\\u0000-\\\\u00FF]*");
						if (Pattern.matches("(?i)" + filter, content.replaceAll("[()+$^*?{}\\[\\]]", " "))) {
							submit = true;
							break;
						}
					}
					if (!submit) {
						tracklogger.debug(smscName + "[" + route.getHtiMsgId() + "] " + " Content Not Allowed  < "
								+ source + " >");
						route.setSmsc(Constants.BSFM_DUMP_SMSC);
						route.setGroupId(0);
						GlobalQueue.interProcessManage.enqueue(route);
					} else {
						submit(route, Queueno);
					}
				} else {
					submit(route, Queueno);
				}
			}
		}
	}

	private void releasePDUs(Map<Integer, RoutePDU> pdu_list) {
		// check for smsc_sender_looping if matched block
		// boolean looping = false;
		String source = null;
		String username = null;
		String destination = null;
		// Map<Integer, RoutePDU> part_wise_map = new TreeMap<Integer, RoutePDU>();
		HashSet<String> part_msg_id_set = new HashSet<String>();
		int i = 0;
		boolean checkDltTemplateId = false;
		boolean checkAllowedContent = false;
		for (RoutePDU pdu : pdu_list.values()) {
			tracklogger.debug(smscName + "[" + pdu.getHtiMsgId() + "]: Releasing");
			// part_wise_map.put(pdu.getPartDescription().getPartNumber(), pdu);
			part_msg_id_set.add(pdu.getHtiMsgId());
			if (i == 0) {
				username = pdu.getUsername();
				source = ((SubmitSM) pdu.getRequestPDU()).getSourceAddr().getAddress();
				destination = ((SubmitSM) pdu.getRequestPDU()).getDestAddr().getAddress();
				boolean dlt_enabled_prefix = false;
				for (String dlt_cc : DLT_ENABLED_CC.split(",")) {
					if (((SubmitSM) pdu.getRequestPDU()).getDestAddr().getAddress().startsWith(dlt_cc)) {
						dlt_enabled_prefix = true;
						break;
					}
				}
				if (dlt_enabled_prefix) {
					if (((SubmitSM) pdu.getRequestPDU()).getExtraOptional((short) 0x1401) == null) {
						if (((SubmitSM) pdu.getRequestPDU()).getExtraOptional((short) 0x1400) == null) {
							System.out.println(pdu.getHtiMsgId() + " PE_ID not Found For TemplateId.");
						} else {
							checkDltTemplateId = true;
						}
					}
				}
				if (GlobalCache.SmscBasedBSFM.containsKey(smscName + "#" + source.toLowerCase())) {
					tracklogger.debug(
							smscName + "[" + pdu.getHtiMsgId() + "] " + " Check Enabled For BSFM  < " + source + " >");
					checkAllowedContent = true;
				}
			}
			i++;
		}
		boolean proceed = true;
		// System.out.println("Checking Looping:[" + part_msg_id_set + "] ");
		if (proceed) {
			if (checkDltTemplateId) {
				tracklogger.debug(part_msg_id_set + " dltCheck: [" + smscName + "]");
				try {
					String hex_converted = "";
					String content = "";
					String PE_ID = null;
					int k = 0;
					for (RoutePDU pdu : pdu_list.values()) {
						if (k == 0) {
							try {
								PE_ID = new String(((SubmitSM) pdu.getRequestPDU()).getExtraOptional((short) 0x1400)
										.getData().getBuffer()).substring(4);
							} catch (ValueNotSetException e1) {
								logger.error(pdu.getHtiMsgId(), e1);
							} catch (Exception e1) {
								logger.error(pdu.getHtiMsgId(), e1);
							}
						}
						if ((((SubmitSM) pdu.getRequestPDU()).getDataCoding() == (byte) 8)
								|| (((SubmitSM) pdu.getRequestPDU()).getDataCoding() == (byte) 245)) {
							try {
								content = ((SubmitSM) pdu.getRequestPDU()).getShortMessage(Data.ENC_UTF16_BE);
							} catch (UnsupportedEncodingException e) {
								logger.error(smscName + " Content Error: " + pdu.getHtiMsgId());
							}
							try {
								hex_converted += splitPart(content, true);
							} catch (Exception e) {
								logger.info(smscName + ": " + pdu.getHtiMsgId(), "Content Error " + e);
							}
						} else {
							content = ((SubmitSM) pdu.getRequestPDU()).getShortMessage();
							try {
								hex_converted += splitPart(content, false);
							} catch (Exception e) {
								logger.info(smscName + ": " + pdu.getHtiMsgId(), "Content Error " + e);
							}
						}
						k++;
					}
					if (hex_converted != null && hex_converted.length() > 0 && PE_ID != null) {
						try {
							content = Converter.getUnicode(hex_converted.toCharArray());
							String templateId = null;
							if (RecentTemplateMapping.containsKey(PE_ID)) {
								if (RecentTemplateMapping.get(PE_ID).containsKey(content)) {
									templateId = RecentTemplateMapping.get(PE_ID).get(content);
									tracklogger.debug(smscName + "[" + part_msg_id_set + "] TemplateId Found[Local]: "
											+ templateId);
								}
							}
							if (templateId == null) {
								templateId = DltFilter.findTemplateID(PE_ID, content);
							}
							if (templateId != null) {
								for (RoutePDU pdu : pdu_list.values()) {
									// System.out.println(pdu.getHtiMsgId() + " TemplateId Found: " + templateId);
									((SubmitSM) pdu.getRequestPDU()).setExtraOptional((short) 0x1401,
											new ByteBuffer(templateId.getBytes()));
								}
								if (RecentTemplateMapping.containsKey(PE_ID)) {
									RecentTemplateMapping.get(PE_ID).put(content, templateId);
								} else {
									FixedLengthMap<String, String> recentTemplates = new FixedLengthMap<String, String>(
											100);
									recentTemplates.put(content, templateId);
									RecentTemplateMapping.put(PE_ID, recentTemplates);
								}
							} else {
								// System.out.println(PE_ID + " TemplateId Not Found: " + content);
							}
						} catch (Exception e) {
							logger.info(smscName + "", "Content Error " + e);
						}
					}
				} catch (Exception e) {
					logger.error(part_msg_id_set + " DLT Error", e.getMessage());
				}
				tracklogger.debug(part_msg_id_set + " dltCheckEnd: [" + smscName + "]");
			}
			if (loopingRule != null) {
				if (GlobalCache.SkipLoopingUsers.contains(username)) {
					tracklogger.debug(part_msg_id_set + " Looping Rule[" + smscName + "] Disabled.[" + username + "]");
				} else {
					// ------------
					boolean skiplooping = false;
					boolean includeSource = false;
					if (loopingRule.getSenderId() != null && loopingRule.getSenderId().length() > 0) {
						boolean source_matched = false;
						for (String source_key : loopingRule.getSenderId().split(",")) {
							// System.out.println(source_key + " Matching Try: " + source);
							if (Pattern.compile(source_key).matcher(source.toLowerCase()).find()) {
								tracklogger.debug(smscName + "[" + part_msg_id_set + "] " + source_key
										+ " matched Looping Source: " + source);
								source_matched = true; // continue looping
								break;
							}
						}
						if (!source_matched) {
							skiplooping = true;
						} else {
							includeSource = true;
						}
					}
					// ------------
					if (!skiplooping) {
						String same_packet = destination;
						if (includeSource) {
							same_packet += source.toLowerCase();
						}
						if (loopingRule.isContent()) {
							String hex_converted = "";
							String content = "";
							for (RoutePDU pdu : pdu_list.values()) {
								if ((((SubmitSM) pdu.getRequestPDU()).getDataCoding() == (byte) 8)
										|| (((SubmitSM) pdu.getRequestPDU()).getDataCoding() == (byte) 245)) {
									try {
										content = ((SubmitSM) pdu.getRequestPDU()).getShortMessage(Data.ENC_UTF16_BE);
									} catch (UnsupportedEncodingException e) {
										logger.error(smscName + " Content Error: " + pdu.getHtiMsgId());
									}
									try {
										hex_converted += splitPart(content, true);
									} catch (Exception e) {
										logger.info(smscName + ": " + pdu.getHtiMsgId(), "Content Error " + e);
									}
								} else {
									content = ((SubmitSM) pdu.getRequestPDU()).getShortMessage();
									try {
										hex_converted += splitPart(content, false);
									} catch (Exception e) {
										logger.info(smscName + ": " + pdu.getHtiMsgId(), "Content Error " + e);
									}
								}
							}
							if (hex_converted != null && hex_converted.length() > 0) {
								try {
									content = Converter.getUnicode(hex_converted.toCharArray());
								} catch (Exception e) {
									logger.info(smscName + "", "Content Error " + e);
								}
							}
							same_packet += content.toLowerCase();
						}
						Map<Long, Integer> timeCounterMap = null;
						if (LoopingCache.containsKey(same_packet)) {
							timeCounterMap = LoopingCache.get(same_packet);
							long out_time = 0;
							int received_counter = 0;
							for (Map.Entry<Long, Integer> timeEntry : timeCounterMap.entrySet()) {
								out_time = timeEntry.getKey();
								received_counter = timeEntry.getValue();
							}
							long time_limit = (loopingRule.getDuration() * 1000) + out_time;
							// long current_time = System.currentTimeMillis();
							if (System.currentTimeMillis() <= time_limit) { // received in time limit. Need to check counter
								if (++received_counter > loopingRule.getCount()) { // limit exeeded. Need to Dump
									tracklogger.debug(smscName + "[" + part_msg_id_set + "] " + " Looping Found < "
											+ source + " > Rerouted: " + loopingRule.getRerouteSmsc());
									proceed = false;
									for (RoutePDU pdu : pdu_list.values()) {
										pdu.setSmsc(loopingRule.getRerouteSmsc());
										pdu.setGroupId(0);
										GlobalQueue.interProcessManage.enqueue(pdu);
									}
								}
								timeCounterMap.put(out_time, received_counter);
							} else { // received after time limit
								timeCounterMap.clear();
								timeCounterMap.put(System.currentTimeMillis(), 1); // Reset info
							}
						} else { // first time received
							timeCounterMap = new HashMap<Long, Integer>();
							timeCounterMap.put(System.currentTimeMillis(), 1);
						}
						LoopingCache.put(same_packet, timeCounterMap);
					}
				}
			}
			// end check for smsc_sender_looping
			if (proceed) {
				if (checkAllowedContent) {
					tracklogger.debug(part_msg_id_set + " BsfmCheck: [" + smscName + "]");
					String hex_converted = "";
					String content = "";
					try {
						for (RoutePDU pdu : pdu_list.values()) {
							if ((((SubmitSM) pdu.getRequestPDU()).getDataCoding() == (byte) 8)
									|| (((SubmitSM) pdu.getRequestPDU()).getDataCoding() == (byte) 245)) {
								try {
									content = ((SubmitSM) pdu.getRequestPDU()).getShortMessage(Data.ENC_UTF16_BE);
								} catch (UnsupportedEncodingException e) {
									logger.error(smscName + " Content Error: " + pdu.getHtiMsgId());
								}
								try {
									hex_converted += splitPart(content, true);
								} catch (Exception e) {
									logger.info(smscName + ": " + pdu.getHtiMsgId(), "Content Error " + e);
								}
							} else {
								content = ((SubmitSM) pdu.getRequestPDU()).getShortMessage();
								try {
									hex_converted += splitPart(content, false);
								} catch (Exception e) {
									logger.info(smscName + ": " + pdu.getHtiMsgId(), "Content Error " + e);
								}
							}
						}
						if (hex_converted != null && hex_converted.length() > 0) {
							try {
								content = Converter.getUnicode(hex_converted.toCharArray());
							} catch (Exception e) {
								logger.info(smscName + "", "Content Error " + e);
							}
							boolean submit = false;
							for (String entry_content : GlobalCache.SmscBasedBSFM
									.get(smscName + "#" + source.toLowerCase())) {
								String filter = entry_content.replaceAll("[()+$^*?{}\\[\\]]", " ")
										.replaceAll("#variable#", "[\\\\p{L}\\\\u0000-\\\\u00FF]*");
								if (Pattern.matches("(?i)" + filter, content.replaceAll("[()+$^*?{}\\[\\]]", " "))) {
									submit = true;
									break;
								}
							}
							if (!submit) {
								tracklogger.debug(smscName + "[" + part_msg_id_set + "] " + " Content Not Allowed  < "
										+ source + " >");
								proceed = false;
								for (RoutePDU pdu : pdu_list.values()) {
									pdu.setSmsc(Constants.BSFM_DUMP_SMSC);
									pdu.setGroupId(0);
									GlobalQueue.interProcessManage.enqueue(pdu);
								}
							}
						}
					} catch (Exception e) {
						logger.error(part_msg_id_set + " Bsfm Content check", e.getMessage());
					}
					tracklogger.debug(part_msg_id_set + " BsfmCheck End: [" + smscName + "]");
				}
				if (proceed) {
					if (smscEntry.getDeliveryWaitTime() > 0 || smscEntry.isCreatePartDlr()) {
						if (pdu_list.containsKey(1)) {
							RoutePDU first_part = pdu_list.remove(1);
							part_msg_id_set.remove(first_part.getHtiMsgId());
							if (submit(first_part, 1)) {
								if (smscEntry.getDeliveryWaitTime() > 0) {
									firstPartSubmitTime.put(first_part.getHtiMsgId(),
											System.currentTimeMillis() + (smscEntry.getDeliveryWaitTime() * 1000));
									GlobalCache.WaitingForDeliveredPart.put(first_part.getHtiMsgId(),
											new ArrayList<RoutePDU>(pdu_list.values()));
									if (smscEntry.isCreatePartDlr()) {
										GlobalCache.PartMappingForDlr.put(first_part.getHtiMsgId(), part_msg_id_set);
									}
									waitingParts.addAll(part_msg_id_set);
								} else {
									GlobalCache.PartMappingForDlr.put(first_part.getHtiMsgId(), part_msg_id_set);
									tracklogger.debug(smscName + " Submit Remaining Parts: " + part_msg_id_set);
									for (RoutePDU pdu : pdu_list.values()) {
										submit(pdu, 1);
									}
								}
							} else {
								tracklogger.debug(smscName + " Submit Remaining Parts: " + part_msg_id_set);
								for (RoutePDU pdu : pdu_list.values()) {
									submit(pdu, 1);
								}
							}
						} else {
							tracklogger.debug(smscName + "first part not found: " + part_msg_id_set);
							for (RoutePDU pdu : pdu_list.values()) {
								submit(pdu, 1);
							}
						}
					} else {
						for (RoutePDU pdu : pdu_list.values()) {
							submit(pdu, 1);
						}
					}
				}
			}
		}
	}

	private boolean submit(RoutePDU route, int Queueno) {
		tracklogger.debug(route.getHtiMsgId() + " smscSubmit1[" + Queueno + "]: " + route.getSmsc());
		boolean reroute = false;
		boolean dlt_enabled_prefix = false;
		for (String dlt_cc : DLT_ENABLED_CC.split(",")) {
			if (((SubmitSM) route.getRequestPDU()).getDestAddr().getAddress().startsWith(dlt_cc)) {
				dlt_enabled_prefix = true;
				break;
			}
		}
		if (dlt_enabled_prefix) {
			if (((SubmitSM) route.getRequestPDU()).getExtraOptional((short) 0x1400) == null) {
				if (!smscName.equalsIgnoreCase(Constants.DLT_UNDELIV_SMSC)) {
					tracklogger.warn(route.getHtiMsgId() + " PE_ID missing.");
				}
				reroute = true;
			}
			if (((SubmitSM) route.getRequestPDU()).getExtraOptional((short) 0x1401) == null) {
				if (!smscName.equalsIgnoreCase(Constants.DLT_UNDELIV_SMSC)) {
					tracklogger.warn(route.getHtiMsgId() + " Template_ID missing.");
				}
				reroute = true;
			}
		}
		if (reroute) {
			if (!smscEntry.isSkipDlt()) {
				if (!smscName.equalsIgnoreCase(Constants.DLT_UNDELIV_SMSC)) {
					route.setSmsc(Constants.DLT_UNDELIV_SMSC);
					tracklogger.info(smscName + " RoutedSmsc: " + route.getRoutedSmsc() + " Rerouted To: "
							+ Constants.DLT_UNDELIV_SMSC);
					route.setGroupId(0);
					GlobalQueue.interProcessManage.enqueue(route);
					return true;
				}
			} else {
				tracklogger.debug(route.getHtiMsgId() + " Skip Dlt Enabled[" + smscName + "]");
			}
		}
		tracklogger.debug(route.getHtiMsgId() + " smscSubmit2[" + Queueno + "]: " + route.getSmsc());
		boolean submit = false;
		try {
			request = route.getRequestPDU();
			sequence_map.put(route.getSequence_no(), route.getHtiMsgId());
			if (smscEntry.isEnforceDefaultEsm()) {
				if (((SubmitSM) request).getEsmClass() == (byte) Data.SM_UDH_GSM
						|| ((SubmitSM) request).getEsmClass() == Data.SM_UDH_GSM_2) // multipart
				{
					if (((SubmitSM) request).getEsmClass() == Data.SM_UDH_GSM_2) {
						System.out.println(
								smscName + "[" + route.getHtiMsgId() + "]Esm Set To Default[64]: " + Data.SM_UDH_GSM_2);
						((SubmitSM) request).setEsmClass((byte) Data.SM_UDH_GSM);
					}
				} else {
					if (((SubmitSM) request).getEsmClass() != (byte) 0) {
						System.out.println(smscName + "[" + route.getHtiMsgId() + "]Esm Set To Default[0]: "
								+ ((SubmitSM) request).getEsmClass());
						((SubmitSM) request).setEsmClass((byte) 0);
					}
				}
			}
			if (((SubmitSM) request).getEsmClass() == (byte) Data.SM_UDH_GSM
					|| ((SubmitSM) request).getEsmClass() == Data.SM_UDH_GSM_2) // multipart
			{
				if (!smscEntry.isMultipart()) { // route doesn't support multi part! make it separate pdu
					System.out.println(smscName + " Multipart Found: " + route.getHtiMsgId());
					String content = "";
					boolean isUnicode = false;
					if ((((SubmitSM) request).getDataCoding() == (byte) 8)
							|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
						content = ((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE);
						isUnicode = true;
					} else {
						content = ((SubmitSM) request).getShortMessage();
					}
					try {
						String hex_converted = splitPart(content, isUnicode);
						content = Converter.getUnicode(hex_converted.toCharArray());
						((SubmitSM) request).setEsmClass((byte) 0);
						if (isUnicode) {
							((SubmitSM) request).setShortMessage(content, Data.ENC_UTF16_BE);
						} else {
							((SubmitSM) request).setShortMessage(content);
						}
					} catch (Exception e) {
						logger.info(smscName + "[" + route.getHtiMsgId() + "]", "Multipart Hex Convert Error");
					}
				}
			}
			if (smscEntry.isEnforceTonNpi()) {
				((SubmitSM) request).setSourceAddr((byte) smscEntry.getSton(), (byte) smscEntry.getSnpi(),
						((SubmitSM) request).getSourceAddr().getAddress());
				((SubmitSM) request).setDestAddr((byte) smscEntry.getDton(), (byte) smscEntry.getDnpi(),
						((SubmitSM) request).getDestAddr().getAddress());
			}
			if (smscEntry.isSourceAsDest()) {
				((SubmitSM) request).setSourceAddr((byte) 1, (byte) 1, ((SubmitSM) request).getDestAddr().getAddress());
			}
			if ((((SubmitSM) request).getDataCoding() == 1) || (((SubmitSM) request).getDataCoding() == 0)) {
				((SubmitSM) request).setDataCoding((byte) Constants.DCS);
			}
			if (smscEntry.isEnforceDlr()) {
				((SubmitSM) request).setRegisteredDelivery((byte) 1);
			} else {
				((SubmitSM) request).setRegisteredDelivery((byte) 0);
			}
			if (tempPrefix != null) {
				String destination = ((SubmitSM) request).getDestAddr().getAddress();
				if (destPrefix.contains("+^")) {
					if (!destination.startsWith(tempPrefix)) {
						destination = tempPrefix + destination;
						((SubmitSM) request).setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
						// logger.info(smscID + " Destination[" + destination + "] Prefix Added:" + tempPrefix);
					} else {
						// logger.info(smscID + " Destination[" + destination + "] already have " + tempPrefix);
					}
				} else if (destPrefix.contains("-^")) {
					if (destination.startsWith(tempPrefix)) {
						destination = destination.substring(tempPrefix.length());
						((SubmitSM) request).setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
						// logger.info(smscID + " Destination[" + destination + "] Prefix Removed:" + tempPrefix);
					} else {
						// logger.info(smscID + " Destination[" + destination + "] doesn't have " + tempPrefix);
					}
				} else if (destPrefix.contains("+$")) {
					if (!destination.endsWith(tempPrefix)) {
						destination = destination + tempPrefix;
						((SubmitSM) request).setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
						// logger.info(smscID + " Destination[" + destination + "] Suffix Added: " + tempPrefix);
					} else {
						// logger.info(smscID + " Destination[" + destination + "] already have Suffix " + tempPrefix);
					}
				} else if (destPrefix.contains("-$")) {
					if (destination.endsWith(tempPrefix)) {
						destination = destination.substring(0, destination.length() - tempPrefix.length());
						((SubmitSM) request).setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
						// logger.info(smscID + " Destination[" + destination + "] Suffix Removed: " + tempPrefix);
					} else {
						// logger.info(smscID + " Destination[" + destination + "] doesn't have Suffix " + tempPrefix);
					}
				} else {
					if (!destination.startsWith(tempPrefix)) {
						destination = tempPrefix + destination;
						((SubmitSM) request).setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
						// logger.info(smscID + " Destination[" + destination + "] Added: " + tempPrefix);
					} else {
						// logger.info(smscID + " Destination[" + destination + "] have " + tempPrefix);
					}
				}
			}
			session.submit((SubmitSM) request);
			submit = true;
			GlobalQueue.submitLogQueue.enqueue(new SubmittedObj(route.getHtiMsgId(), smscName, new Date()));
			if (smscEntry.getDndSource() != null && smscEntry.getDndSource().length() > 0) {
				boolean source_matched = false;
				String source = ((SubmitSM) request).getSourceAddr().getAddress();
				for (String source_key : smscEntry.getDndSource().split(",")) {
					if (Pattern.compile(source_key).matcher(source).find()) {
						source_matched = true;
						break;
					}
				}
				if (source_matched) {
					// System.out.println(source+" matched ---> ");
					SubmittedObj submitObj = new SubmittedObj(route.getHtiMsgId(), smscName, source,
							((SubmitSM) request).getDestAddr().getAddress(), new Date());
					GlobalQueue.dndLogQueue.enqueue(submitObj);
					GlobalCache.DndSourceMsgId.put(route.getHtiMsgId(), submitObj);
				}
			}
			tracklogger.debug(route.getHtiMsgId() + "[" + route.getSequence_no() + "] pdu Submitted " + smscName);
			GlobalCache.SmscSubmitTime.put(route.getHtiMsgId(), new Date());
			if (route.isDeduct()) {
				route.setDeduct(false);
				deduct(route.getUsername(), route.getCost());
			}
			if (smscEntry.getEnforceSmsc() != null || smscEntry.isResend()) {
				GlobalCache.ResendPDUCache.put(route.getHtiMsgId(), route);
			}
			/*
			 * GlobalQueue.SubmittedQueue.enqueue(new SubmittedObj(route.getHtiMsgId(), route.getSequence_no(), smscName, route.getUsername(), ((SubmitSM) request).getSourceAddr().getAddress(),
			 * ((SubmitSM) request).getDestAddr().getAddress(), new Date()));
			 */
			if (!QUEUE_OUT.PQueue[Queueno].getFlag()) {
				QUEUE_OUT.PQueue[Queueno].setTrue();
			}
			// total_submit++;
			// inter_count++;
			ThreadSleep();
		} catch (PDUException es) {
			tracklogger.info(smscName + " <" + route.getHtiMsgId() + "> :" + es);
		} catch (TimeoutException | WrongSessionStateException te) {
			logger.info(smscName + " <" + route.getHtiMsgId() + "> :" + te);
			QUEUE_OUT.PQueue[Queueno].enqueue(route);
		} catch (IOException | NullPointerException e) {
			logger.error(smscName + " <---- Connection Breaked(Submission) ----> ");
			connectionBreaked = true;
			super.setTermException(e);
			// sequencemap.remove(route.getSequence_no());
			if (GlobalCache.SmscDisconnectionAlert.contains(smscName)) {
				if (!GlobalCache.SmscDisconnection.containsKey(smscName)) {
					GlobalCache.SmscDisconnection.put(smscName, System.currentTimeMillis());
				}
			}
			if (GlobalCache.SMSCConnectionStatus.containsKey(smscName)) {
				GlobalCache.SMSCConnectionStatus.put(smscName, false);
			}
			GlobalCache.SmscConnectionSet.remove(smscName);
			QUEUE_OUT.PQueue[Queueno].enqueue(route);
		}
		route = null;
		return submit;
	}

	@Override
	public void stop() {
		stop = true;
		super.stop();
	}

	private void ThreadSleep() {
		try {
			if (smscEntry.getSleep() > 10) {
				Thread.sleep(smscEntry.getSleep());
			} else {
				Thread.sleep(10);
			}
		} catch (InterruptedException ir) {
		}
	}

	private boolean deduct(String to, double amount) {
		try {
			UserBalance balance = GlobalVars.userService.getUserBalance(to);
			if (balance != null) {
				logger.debug(to + " Wallet Flag: " + balance.getFlag());
				if (balance.getFlag().equalsIgnoreCase("No")) {
					return balance.deductCredit(1);
				} else {
					return balance.deductAmount(amount);
				}
			} else {
				return false;
			}
		} catch (EntryNotFoundException ex) {
			logger.error(to + " <-- Balance Entry Not Found [" + amount + "] --> ");
			return false;
		} catch (Exception ex) {
			logger.info(to + " <-- Balance refund Error [" + amount + "] --> " + ex);
			return false;
		}
	}

	private String getHexDump(String getString) throws Exception {
		String dump = "";
		// int dataLen = getString.length();
		byte[] buffer = getString.getBytes(Data.ENC_UTF16_BE);
		for (int i = 0; i < buffer.length; i++) {
			dump += Character.forDigit((buffer[i] >> 4) & 0x0f, 16);
			dump += Character.forDigit(buffer[i] & 0x0f, 16);
		}
		buffer = null;
		return dump;
	}

	private String splitPart(String content, boolean isUnicode) throws Exception {
		String to_return = null;
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
				// tracklogger.debug("HEX: " + hex_dump);
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
						// tracklogger.debug("REF: " + hex_dump.substring(6, 8));
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
					System.out.println(smscName + "Unknown Header Found:" + hex_dump.substring(0, 14));
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
}
