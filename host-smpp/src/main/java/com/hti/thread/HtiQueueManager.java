/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.PriorityQueue;
import com.hti.objects.RepeatedNumberEntry;
import com.hti.objects.RoutePDU;
import com.hti.objects.SmscInObj;
import com.hti.smsc.DistributionGroupManager;
import com.hti.smsc.dto.GroupEntry;
import com.hti.util.Constants;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.SubmitSM;

/**
 *
 * @author Raj
 */
public class HtiQueueManager implements Runnable {
	private String smsc_id;
	private boolean is_smscup = false;
	private Map<String, Map<String, Long>> SmscWiseOutgoingDestination = new HashMap<String, Map<String, Long>>();
	//private Map<String, Map<String, Map<Long, Integer>>> SmscWiseOutgoingPacket = new HashMap<String, Map<String, Map<Long, Integer>>>();
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Logger tracklogger = LoggerFactory.getLogger("trackLogger");
	private Calendar nextClearTime;
	private int SMSC_QUEUE_LIMIT = 20000;
	private Map<String, Integer> unstableRoutes = new HashMap<String, Integer>();
	private Map<Integer, Map<Integer, GroupingCache>> distributionTracking = new HashMap<Integer, Map<Integer, GroupingCache>>();
	private boolean stop;

	public HtiQueueManager() {
		logger.info("HtiQueueManager Starting");
		nextClearTime = Calendar.getInstance();
		nextClearTime.add(Calendar.HOUR, +1);
	}

	private class GroupingCache {
		private long groupingDuration; // total duration of distribution
		private long primeMemberDuration; // only forward to prime member of group in this duration
		private int receiveNumberCount; // received number count to start distribution
		private boolean distribute;
		private Map<String, String> partRoutedCache; // to track smsc of previous sms part of received number

		public long getGroupingDuration() {
			return groupingDuration;
		}

		public void setGroupingDuration(long groupingDuration) {
			this.groupingDuration = groupingDuration;
		}

		public long getPrimeMemberDuration() {
			return primeMemberDuration;
		}

		public void setPrimeMemberDuration(long primeMemberDuration) {
			this.primeMemberDuration = primeMemberDuration;
		}

		public int getReceiveNumberCount() {
			return receiveNumberCount;
		}

		public void setReceiveNumberCount(int receiveNumberCount) {
			this.receiveNumberCount = receiveNumberCount;
		}

		public boolean isDistribute() {
			return distribute;
		}

		public void setDistribute(boolean distribute) {
			this.distribute = distribute;
		}

		public Map<String, String> getPartRoutedCache() {
			if (partRoutedCache == null) {
				this.partRoutedCache = new HashMap<String, String>();
			}
			return partRoutedCache;
		}
	}

	@Override
	public void run() {
		while (!stop) {
			if (GlobalQueue.interProcessRequest.isEmpty()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException iex) {
				}
			} else {
				try {
					RoutePDU route = null;
					while (!GlobalQueue.interProcessRequest.isEmpty()) {
						route = (RoutePDU) GlobalQueue.interProcessRequest.dequeue();
						tracklogger.debug(route.getHtiMsgId() + " InterprocessIn: " + route.getUsername() + " ["
								+ route.getSmsc() + "] " + ((SubmitSM) route.getRequestPDU()).getDestAddr().getAddress()
								+ " " + ((SubmitSM) route.getRequestPDU()).getSourceAddr().getAddress());
						smsc_id = route.getSmsc();
						route.setRerouted(false);
						if (route.isRegisterSender()) // no need to check route
						{
							if (Constants.PROCESSING_STATUS) {
								if (GlobalCache.SMSCConnectionStatus.containsKey(smsc_id)
										&& GlobalCache.SMSCConnectionStatus.get(smsc_id)) {
									is_smscup = true;
								} else {
									is_smscup = false;
								}
							} else {
								is_smscup = false;
							}
						} else {
							if (Constants.PROCESSING_STATUS) {
								// -------------- check For smsc connetion if grouping applied ---------------------
								is_smscup = false;
								route.setRoutedSmsc(smsc_id);
								String destination = ((SubmitSM) route.getRequestPDU()).getDestAddr().getAddress();
								// --------- conditional grouping ------------------------
								if (route.getGroupId() > 0
										&& GlobalCache.SmscGroupEntries.containsKey(route.getGroupId())
										&& GlobalCache.SmscGroupEntries.get(route.getGroupId()).getDuration() > 0) {
									Map<Integer, GroupingCache> operatorTracking = null;
									if (distributionTracking.containsKey(route.getGroupId())) {
										operatorTracking = distributionTracking.get(route.getGroupId());
									} else {
										operatorTracking = new HashMap<Integer, GroupingCache>();
									}
									GroupingCache groupingCache = null;
									if (operatorTracking.containsKey(route.getNetworkId())) {
										groupingCache = operatorTracking.get(route.getNetworkId());
									} else {
										groupingCache = new GroupingCache();
									}
									boolean previousRoute = false;
									if (((SubmitSM) route.getRequestPDU()).getEsmClass() == (byte) Data.SM_UDH_GSM
											|| ((SubmitSM) route.getRequestPDU()).getEsmClass() == Data.SM_UDH_GSM_2) // multipart
									{
										if (route.getPartDescription().getReferenceNumber() == 0) {
											int[] parts = getPartDescription(route.getRequestPDU());
											route.getPartDescription().setTotal(parts[0]);
											route.getPartDescription().setPartNumber(parts[1]);
											route.getPartDescription().setReferenceNumber(parts[2]);
										}
										if (groupingCache.getPartRoutedCache().containsKey(
												destination + "#" + route.getPartDescription().getReferenceNumber())) { // part must be forward to same smsc as other parts
											smsc_id = groupingCache.getPartRoutedCache().get(destination + "#"
													+ route.getPartDescription().getReferenceNumber());
											previousRoute = true;
											logger.debug(route.getGroupId() + "[" + route.getNetworkId()
													+ "] Next Part received: " + destination);
										}
									}
									if (!previousRoute) {
										GroupEntry groupEntry = GlobalCache.SmscGroupEntries.get(route.getGroupId());
										int repeatCount = 0;
										if (GlobalCache.GroupWiseRepeatedNumbers
												.containsKey(route.getGroupId() + "#" + destination)) {
											repeatCount = GlobalCache.GroupWiseRepeatedNumbers
													.get(route.getGroupId() + "#" + destination);
										}
										repeatCount++;
										GlobalCache.GroupWiseRepeatedNumbers.put(route.getGroupId() + "#" + destination,
												repeatCount);
										groupingCache.setReceiveNumberCount(groupingCache.getReceiveNumberCount() + 1);
										if (repeatCount >= groupEntry.getNoOfRepeat()) {
											// route to primary member & save number to DB if not exist
											smsc_id = groupEntry.getPrimeMemberName();
											logger.info(route.getGroupId() + "[" + route.getNetworkId() + "] Repeated["
													+ destination + " -> " + repeatCount + "]. Forward To Primary "
													+ route.getHtiMsgId());
											GlobalQueue.RepeatedNumberQueue.enqueue(new RepeatedNumberEntry(
													route.getGroupId(), destination, repeatCount));
										} else {
											if (groupingCache.getReceiveNumberCount() == 1) { // prime duration started
												groupingCache.setPrimeMemberDuration(System.currentTimeMillis());
												smsc_id = groupEntry.getPrimeMemberName();
												logger.info(route.getGroupId() + "[" + route.getNetworkId() + "]"
														+ " First Receiving. Forward To Primary "
														+ route.getHtiMsgId());
											} else {
												if (groupingCache.isDistribute()) { // distribute duration
													long time_interval = (System.currentTimeMillis()
															- groupingCache.getGroupingDuration()) / 1000;
													if (time_interval <= groupEntry.getDuration()) {
														logger.debug(route.getGroupId() + "[" + route.getNetworkId()
																+ "] Under Duration: " + route.getHtiMsgId());
														smsc_id = DistributionGroupManager
																.findPercentRoute(route.getGroupId());
													} else { // prime duration started. forward to primary
														groupingCache.setDistribute(false);
														groupingCache
																.setPrimeMemberDuration(System.currentTimeMillis());
														groupingCache.setReceiveNumberCount(0);
														smsc_id = groupEntry.getPrimeMemberName();
														logger.info(route.getGroupId() + "[" + route.getNetworkId()
																+ "]" + "Duration Completed.Forward To Primary "
																+ route.getHtiMsgId());
													}
												} else { // check for prime or distribute duration
													if (groupingCache.getReceiveNumberCount() >= groupEntry
															.getCheckVolume()) {
														logger.debug(route.getGroupId() + "[" + route.getNetworkId()
																+ "]" + " Required Traffic Received: "
																+ groupingCache.getReceiveNumberCount());
														long time_interval = (System.currentTimeMillis()
																- groupingCache.getPrimeMemberDuration()) / 1000;
														if (time_interval <= groupEntry.getCheckDuration()) {
															logger.info(route.getGroupId() + "[" + route.getNetworkId()
																	+ "] Prime Duration Achieved. Distribute: "
																	+ route.getHtiMsgId());
															groupingCache.setDistribute(true);
															groupingCache
																	.setGroupingDuration(System.currentTimeMillis());
															smsc_id = DistributionGroupManager
																	.findPercentRoute(route.getGroupId());
														} else {
															logger.info(route.getGroupId() + "[" + route.getNetworkId()
																	+ "]" + " Reset Cache. Forward To Primary: "
																	+ route.getHtiMsgId());
															groupingCache
																	.setPrimeMemberDuration(System.currentTimeMillis());
															groupingCache.setReceiveNumberCount(0);
															smsc_id = groupEntry.getPrimeMemberName();
														}
													} else {
														smsc_id = groupEntry.getPrimeMemberName();
														logger.debug(route.getGroupId() + "[" + route.getNetworkId()
																+ "]" + " Forward To Primary: " + route.getHtiMsgId());
													}
												}
											}
										}
										if (((SubmitSM) route.getRequestPDU()).getEsmClass() == (byte) Data.SM_UDH_GSM
												|| ((SubmitSM) route.getRequestPDU())
														.getEsmClass() == Data.SM_UDH_GSM_2) // multipart
										{
											groupingCache.getPartRoutedCache().put(
													destination + "#" + route.getPartDescription().getReferenceNumber(),
													smsc_id);
										}
										operatorTracking.put(route.getNetworkId(), groupingCache);
										distributionTracking.put(route.getGroupId(), operatorTracking);
									}
									logger.debug(route.getGroupId() + "[" + route.getNetworkId() + "] FinalRoute: "
											+ smsc_id);
									route.setSmsc(smsc_id);
								}
								// ---------End conditional grouping ------------------------
								if (isConnected(smsc_id) && isStable(smsc_id, route.getNetworkId() + "")) { // connected & stable
									is_smscup = true;
								} else {
									if (Constants.DISTRIBUTION) {
										if (route.getGroupId() > 0) {
											if (isConnected(smsc_id) && !isStable(smsc_id, route.getNetworkId() + "")) { // connected but not stable
												// we will send 1 out of 10 packets to routed smsc to test stability
												if (unstableRoutes.containsKey(smsc_id + "#" + route.getNetworkId())) {
													int routedCounter = unstableRoutes
															.get(smsc_id + "#" + route.getNetworkId());
													routedCounter++;
													if (routedCounter == 10) {
														logger.info(smsc_id + "#" + route.getNetworkId()
																+ " stability test[" + destination + "]: "
																+ route.getHtiMsgId());
														unstableRoutes.remove(smsc_id + "#" + route.getNetworkId());
														is_smscup = true;
													} else {
														unstableRoutes.put(smsc_id + "#" + route.getNetworkId(),
																routedCounter);
													}
												} else {
													unstableRoutes.put(smsc_id + "#" + route.getNetworkId(), 1); // 1st packet out of 10
												}
											}
											if (!is_smscup) { // check for another route in group
												logger.debug(smsc_id + " Checking for another route [" + destination
														+ "] : 1");
												int members = DistributionGroupManager.memberCount(route.getGroupId());
												int i = 0;
												while (members != i) {
													String next_smsc = DistributionGroupManager
															.findRoute(route.getGroupId());
													if (next_smsc != null && next_smsc != smsc_id) {
														if (isConnected(next_smsc)
																&& isStable(next_smsc, route.getNetworkId() + "")) { // connected & stable
															smsc_id = next_smsc;
															is_smscup = true;
															break;
														}
													}
													i++;
												}
											}
										} else {
											logger.debug(route.getHtiMsgId() + " Distribution Disabled["
													+ route.getSmsc() + "]");
										}
									} else {
										logger.debug(" ********* Distribution Disabled **************");
									}
									if (!is_smscup) { // check for backup route
										tracklogger.debug(smsc_id + " Checking for backup route [" + destination + "]");
										if (route.getBackupSmsc() != null && route.getBackupSmsc().length() > 0) {
											// logger.info(smsc_id + " <-- Backup Route -->" + route.getBackupSmsc());
											if (isConnected(route.getBackupSmsc())
													&& isStable(route.getBackupSmsc(), route.getNetworkId() + "")) {
												logger.info(route.getHtiMsgId() + " < " + smsc_id + " :-> Backup Route:"
														+ route.getBackupSmsc() + " > " + route.getUsername());
												is_smscup = true;
												smsc_id = route.getBackupSmsc();
											}
										}
									}
									// ------------------------------------------------------
									route.setSmsc(smsc_id);
								}
								// -------------------------------------------------------------------
							} else {
								is_smscup = false;
							}
						}
						if (is_smscup) {
							int smsc_live_queue_size = 0;
							try {
								if (route.getPriority() != 1) {
									try {
										if (GlobalCache.SmscPrioritySender.containsKey(smsc_id)) {
											String source = ((SubmitSM) route.getRequestPDU()).getSourceAddr()
													.getAddress();
											String priorSenders = GlobalCache.SmscPrioritySender.get(smsc_id);
											for (String source_key : priorSenders.split(",")) {
												if (source_key != null && source_key.length() > 0) {
													if (Pattern.compile(source_key).matcher(source).find()) {
														logger.info(route.getHtiMsgId() + "-> " + source_key
																+ " matched Priority Source[" + smsc_id + "]: "
																+ source);
														route.setPriority(1);
														break;
													}
												}
											}
										}
									} catch (Exception e) {
										logger.error(route.getHtiMsgId(), e.getMessage());
									}
								}
								if (route.getPriority() == 1
										|| ((SubmitSM) route.getRequestPDU()).getEsmClass() == (byte) Data.SM_UDH_GSM
										|| ((SubmitSM) route.getRequestPDU())
												.getEsmClass() == (byte) Data.SM_UDH_GSM_2) {
									// skip queue limit check
									// System.out.println("Skip Queue Limit Check: " + route.getHtiMsgId());
								} else {
									if (GlobalCache.SmscQueueCache.containsKey(smsc_id)) {
										for (int j = 1; j <= Constants.noofqueue; j++) {
											smsc_live_queue_size += ((PriorityQueue) GlobalCache.SmscQueueCache
													.get(smsc_id)).PQueue[j].size();
										}
									}
								}
								// int max_queue_limit = ((Integer) SmscStatus.SMSC_QUEUE_LIMIT.get(smsc));
								if (smsc_live_queue_size <= SMSC_QUEUE_LIMIT) {
									// if (((SubmitSM) route.getRequestPDU()).getEsmClass() == 0) {
									String smscid = route.getSmsc();
									if (GlobalCache.DestinationSleepApply.containsKey(smscid)) {
										if (((SubmitSM) route.getRequestPDU()).getEsmClass() == (byte) Data.SM_UDH_GSM
												|| ((SubmitSM) route.getRequestPDU())
														.getEsmClass() == (byte) Data.SM_UDH_GSM_2) { // No Criteria Check For
																										// Concatenated messages
											// logger.debug("Ignoring Concatenated For Destination Restriction");
											GlobalQueue.interProcessManage.enqueue(route);
											tracklogger.debug(route.getHtiMsgId() + " InterprocessOut[3]: "
													+ route.getUsername() + " [" + route.getSmsc() + "]");
										} else {
											int time_interval = (Integer) GlobalCache.DestinationSleepApply.get(smscid);
											String destination = ((SubmitSM) route.getRequestPDU()).getDestAddr()
													.getAddress();
											Map<String, Long> destinationOutgoing = null;
											if (SmscWiseOutgoingDestination.containsKey(smscid)) {
												destinationOutgoing = SmscWiseOutgoingDestination.get(smscid);
											} else {
												destinationOutgoing = new HashMap<String, Long>();
											}
											if (destinationOutgoing.containsKey(destination)) {
												long last_out_time = (Long) destinationOutgoing.get(destination);
												if ((System.currentTimeMillis() - last_out_time) >= (time_interval
														* 1000)) {
													GlobalQueue.interProcessManage.enqueue(route);
													tracklogger.debug(route.getHtiMsgId() + " InterprocessOut[4]: "
															+ route.getUsername() + " [" + route.getSmsc() + "]");
													destinationOutgoing.put(destination, System.currentTimeMillis());
													// logger.debug(destination + " Enqueued(Found) -> "+ new SimpleDateFormat("HH:mm:ss").format(new Date()));
												} else {
													// put to delay Queue
													long next_out_time = last_out_time + (time_interval * 1000);
													GlobalCache.DelaySubmition.put(route, next_out_time);
													destinationOutgoing.put(destination, next_out_time);
													tracklogger.debug(route.getHtiMsgId() + " delayQueue: "
															+ route.getUsername() + " [" + route.getSmsc() + "]");
												}
											} else {
												destinationOutgoing.put(destination, System.currentTimeMillis());
												GlobalQueue.interProcessManage.enqueue(route);
												tracklogger.debug(route.getHtiMsgId() + " InterprocessOut[5]: "
														+ route.getUsername() + " [" + route.getSmsc() + "]");
											}
											SmscWiseOutgoingDestination.put(smscid, destinationOutgoing);
										}
									} else {
										GlobalQueue.interProcessManage.enqueue(route);
										tracklogger.debug(route.getHtiMsgId() + " InterprocessOut[6]: "
												+ route.getUsername() + " [" + route.getSmsc() + "]");
									}
								} else {
									GlobalQueue.smsc_in_update_Queue.enqueue(new SmscInObj(route.getHtiMsgId(), "Q",
											route.getSmsc(), route.getGroupId(), route.getUsername()));
									tracklogger.debug(route.getHtiMsgId() + " exceedlimit[" + smsc_live_queue_size
											+ "]:" + route.getUsername() + " [" + route.getSmsc() + "]");
								}
							} catch (Exception ex) {
								logger.error("process(1)", ex.fillInStackTrace());
							}
						} else {
							tracklogger.debug(route.getHtiMsgId() + " downQueue: " + route.getUsername() + " ["
									+ route.getSmsc() + "]");
							GlobalQueue.smsc_in_update_Queue.enqueue(new SmscInObj(route.getHtiMsgId(), "Q",
									route.getSmsc(), route.getGroupId(), route.getUsername()));
						}
						route = null;
					}
				} catch (Exception e) {
					logger.error("process(2)", e.fillInStackTrace());
				}
			}
			if (nextClearTime.getTime().before(new Date())) { // Clear cache on Each Hour
				logger.info("*** Releasing Looping Cache *******");
				checkMemoryUsage(1);
				SmscWiseOutgoingDestination.clear();
				checkMemoryUsage(2);
				//SmscWiseOutgoingPacket.clear();
				checkMemoryUsage(3);
				nextClearTime = Calendar.getInstance();
				nextClearTime.add(Calendar.HOUR, +1);
			}
		}
		logger.info("HtiQueueManager Stopped.Queue: " + GlobalQueue.interProcessRequest.size());
	}

	private boolean isStable(String smsc, String prefix) {
		if (GlobalCache.WorstDeliveryRoute.containsKey(smsc) || GlobalCache.WorstResponseRoute.containsKey(smsc)) // Smsc Performance is worst
		{
			Set<String> networks = null;
			if (GlobalCache.WorstDeliveryRoute.containsKey(smsc)) {
				networks = GlobalCache.WorstDeliveryRoute.get(smsc);
			} else {
				networks = GlobalCache.WorstResponseRoute.get(smsc);
			}
			// logger.info(smsc + "[" + prefix + "] Networks: " + networks);
			if (networks.contains(prefix)) // Performance on this Network is worst
			{
				// logger.info(smsc + "[" + prefix + "] Unstable Route");
				return false;
			}
		}
		return true;
	}

	private boolean isConnected(String smsc_id) {
		if (GlobalCache.SMSCConnectionStatus.containsKey(smsc_id) && GlobalCache.SMSCConnectionStatus.get(smsc_id)) {
			return true;
		}
		return false;
	}

	private void checkMemoryUsage(int i) {
		long mb = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();
		long used_memory = (runtime.totalMemory() - runtime.freeMemory()) / mb;
		logger.info("Memory Used[" + i + "]:---> " + used_memory + " MB");
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
					System.out.println("Unknown Header Found:" + hex_dump.substring(0, 14));
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
		logger.info("HtiQueueManager Stopping.Queue: " + GlobalQueue.interProcessRequest.size());
		SmscWiseOutgoingDestination.clear();
		//SmscWiseOutgoingPacket.clear();
		stop = true;
	}
}
