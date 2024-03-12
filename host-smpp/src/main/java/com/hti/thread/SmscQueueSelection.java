/*
 * SmscQueueSelection.java
 *
 * Created on 07 April 2004, 13:11
 */
package com.hti.thread;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dlt.DltFilter;
import com.hti.objects.LogPDU;
import com.hti.objects.PriorityQueue;
import com.hti.objects.RoutePDU;
import com.hti.objects.SmscInObj;
import com.hti.objects.SmscLimit;
import com.hti.smsc.SpecialSMSCSetting;
import com.hti.util.Converter;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.util.ByteBuffer;
import com.hti.tw.filter.FilterService;
import com.hti.util.Constants;

/**
 * @author administrator Thread Responsible for selecting Queue according to prefered and secondry smsc and their corrosponding flag values uses(SmscStatus.HtiConnectionFlagHashtable) also enqueue in
 *         ClientBackLog Queue and if SmscStatus.SMSC_DOWN_QUEUE
 */
public class SmscQueueSelection implements Runnable {
	private PriorityQueue send_queue;
	private volatile int priority = 3;
	private int STon;
	private int SNpi;
	private RoutePDU route = null;
	private Request request = null;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Logger tracklogger = LoggerFactory.getLogger("trackLogger");
	// private Map<String, Map<String, Map<Long, Integer>>> SmscWiseOutgoingPacket = new HashMap<String, Map<String, Map<Long, Integer>>>();
	private boolean stop;
	// private Calendar nextClearTime;

	public SmscQueueSelection() {
		logger.info("SmscQueueSelection Thread Starting");
		// nextClearTime = Calendar.getInstance();
		// nextClearTime.add(Calendar.HOUR, +1);
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				if (GlobalQueue.interProcessManage.isEmpty()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ie) {
					}
				} else {
					while (!GlobalQueue.interProcessManage.isEmpty()) {
						route = (RoutePDU) GlobalQueue.interProcessManage.dequeue();
						tracklogger.debug(route.getHtiMsgId() + " IntermanageIn: " + route.getUsername() + " ["
								+ route.getSmsc() + "] " + ((SubmitSM) route.getRequestPDU()).getDestAddr().getAddress()
								+ " " + ((SubmitSM) route.getRequestPDU()).getSourceAddr().getAddress());
						request = route.getRequestPDU();
						if ((route.getSmsc() != null)) {
							request.assignSequenceNumber(true);
							route.setSequence_no(request.getSequenceNumber());
							priority = route.getPriority();
							if (GlobalCache.SmscSubmitLimit.containsKey(route.getSmsc() + "#" + route.getNetworkId())) {
								tracklogger.debug(route.getHtiMsgId() + " SubmitLimitCheck: " + route.getUsername()
										+ " [" + route.getSmsc() + "]");
								// check submit counter and
								SmscLimit submitlimit = GlobalCache.SmscSubmitLimit
										.get(route.getSmsc() + "#" + route.getNetworkId());
								int submitCounter = 0;
								if (GlobalCache.SmscSubmitCounter.containsKey(submitlimit.getId())) {
									submitCounter = GlobalCache.SmscSubmitCounter.get(submitlimit.getId());
								}
								if (submitCounter >= submitlimit.getLimit()) {
									// limit exceed. reroute to another
									System.out.println(route.getSmsc() + "#" + route.getNetworkId() + " Rerouted: "
											+ submitlimit.getRerouteSmsc());
									route.setSmsc(submitlimit.getRerouteSmsc());
									if (!GlobalCache.SmscSubmitLimitNotified.contains(submitlimit.getId())) {
										new Thread(new SubmitLimitAlert(submitlimit)).start();
										GlobalCache.SmscSubmitLimitNotified.add(submitlimit.getId());
									}
								} else {
									GlobalCache.SmscSubmitCounter.put(submitlimit.getId(), ++submitCounter);
								}
							}
							if (GlobalCache.smscGreekEncodeApply.contains(route.getSmsc())
									&& ((SubmitSM) request).getDataCoding() == 0) {
								tracklogger.debug(route.getHtiMsgId() + " GreekEncodeCheck: " + route.getUsername()
										+ " [" + route.getSmsc() + "]");
								// logger.info("msg: " + ((SubmitSM) request).getShortMessage());
								boolean header = false;
								if (((SubmitSM) request).getEsmClass() == (byte) Data.SM_UDH_GSM
										|| ((SubmitSM) request).getEsmClass() == Data.SM_UDH_GSM_2) // multipart
								{
									header = true;
								}
								String content = replaceEncoding(((SubmitSM) request).getShortMessage(), header);
								((SubmitSM) request).setShortMessage(content);
								// logger.info("replaced: " + ((SubmitSM) request).getShortMessage());
							}
							if (!route.isRegisterSender()) {
								if (GlobalCache.specailSMSCsetting.containsKey(route.getSmsc())) {
									tracklogger.debug(route.getHtiMsgId() + " specailSMSCsettingCheck: "
											+ route.getUsername() + " [" + route.getSmsc() + "]");
									String source_No = ((SubmitSM) request).getSourceAddr().getAddress();
									boolean checkAlfa = isAlphaNumeric(source_No);
									if (!checkAlfa) {
										SpecialSMSCSetting ss = (SpecialSMSCSetting) GlobalCache.specailSMSCsetting
												.get(route.getSmsc());
										int length = source_No.length();
										if (length <= ss.getLength()) {
											((SubmitSM) request).setSourceAddr((byte) ss.getL_ston(),
													(byte) ss.getL_snpi(), source_No);
										} else {
											((SubmitSM) request).setSourceAddr((byte) ss.getG_ston(),
													(byte) ss.getG_snpi(), source_No);
										}
									}
								} else if (GlobalCache.SmscSenderId.containsKey(route.getSmsc())) {
									tracklogger.debug(route.getHtiMsgId() + " SmscSenderIdCheck: " + route.getUsername()
											+ " [" + route.getSmsc() + "]");
									String source_no = getCheckSenderId(
											((SubmitSM) request).getSourceAddr().getAddress(), route.getSmsc());
									try {
										STon = ((SubmitSM) request).getSourceAddr().getTon();
										SNpi = ((SubmitSM) request).getSourceAddr().getNpi();
										((SubmitSM) request).setSourceAddr((byte) STon, (byte) SNpi, source_no);
									} catch (com.logica.smpp.pdu.WrongLengthOfStringException wl) {
										// logger.info("Error in writing User Configuration in Default Sender Id values");
									}
								}
							}
							if (!FilterService.filter(((SubmitSM) request).getDestAddr().getAddress(),
									((SubmitSM) request).getSourceAddr().getAddress())) {
								tracklogger.debug(route.getHtiMsgId() + " TWFilterCheck: " + route.getUsername() + " ["
										+ route.getSmsc() + "]");
								route.setSmsc(Constants.TW_DUMP_SMSC);
								System.out.println(
										route.getHtiMsgId() + "#" + ((SubmitSM) request).getDestAddr().getAddress()
												+ " TWFilter Rerouted: " + Constants.TW_DUMP_SMSC);
							}
							if ((GlobalCache.SMSCConnectionStatus.containsKey(route.getSmsc()))
									&& (GlobalCache.SMSCConnectionStatus.get(route.getSmsc()))) {
								GlobalCache.ResponseLogCache.put(route.getHtiMsgId(),
										new LogPDU(route.getHtiMsgId(), route.getSmsc(), route.getUsername(),
												((SubmitSM) request).getRegisteredDelivery(),
												((SubmitSM) request).getDestAddr().getAddress(),
												((SubmitSM) request).getSourceAddr().getAddress(),
												route.getOriginalSourceAddr(), route.getNetworkId() + "",
												route.getTime(), route.getCost(), route.isRefund(),
												GlobalVars.SERVER_ID, route.getGroupId(), route.isRerouted(),
												route.getRoutedSmsc()));
								// ---------------------------------------------------------
								send_queue = (PriorityQueue) GlobalCache.SmscQueueCache.get(route.getSmsc());
								if (priority == 1) {
									send_queue.PQueue[1].enqueue(route);
								} else {
									if (send_queue.PQueue[1].isEmpty()) {
										send_queue.PQueue[1].enqueue(route);
									} else if (send_queue.PQueue[2].isEmpty()) {
										send_queue.PQueue[2].enqueue(route);
									} else {
										send_queue.PQueue[3].enqueue(route);
									}
								}
								tracklogger.debug(route.getHtiMsgId() + "[" + route.getSequence_no()
										+ "] IntermanageOut: " + route.getUsername() + " [" + route.getSmsc() + "]");
							} else {
								GlobalQueue.smsc_in_update_Queue.enqueue(new SmscInObj(route.getHtiMsgId(), "Q",
										route.getSmsc(), route.getGroupId(), route.getUsername()));
								tracklogger.debug(route.getHtiMsgId() + " downQueue: " + route.getUsername() + " ["
										+ route.getSmsc() + "]");
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("", e.fillInStackTrace());
			}
		}
		logger.info("SmscQueueSelection Thread Stopped.Queue: " + GlobalQueue.interProcessManage.size());
	}

	private String getCheckSenderId(String received_sender_id, String smsc_name) {
		String senderList = null;
		String return_sender_id = (String) GlobalCache.SmscSenderId.get(smsc_name);
		if (GlobalCache.SenderIdList.containsKey(smsc_name)) {
			senderList = (String) GlobalCache.SenderIdList.get(smsc_name);
		}
		try {
			StringTokenizer token = new StringTokenizer(senderList, ",");
			while (token.hasMoreTokens()) {
				String sender_token = token.nextToken();
				if (sender_token != null && sender_token.trim().equalsIgnoreCase(received_sender_id.trim())) {
					return_sender_id = received_sender_id;
					break;
				}
			}
		} catch (Exception ex) {
			logger.error("getCheckSenderId(" + received_sender_id + "," + smsc_name + ")", ex.fillInStackTrace());
		}
		return return_sender_id;
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
				if ((chr[l] >= 'A' && chr[l] <= 'Z') || (chr[l] >= 'a' && chr[l] <= 'z') || chr[l] == ' ') {
					blnAlpha = true;
					break;
				}
			}
		} else {
			blnAlpha = true;
		}
		return (blnAlpha);
	}

	private String replaceEncoding1(String content, boolean header) {
		String charhex = "", hex = "";
		char[] chars = content.toCharArray();
		int i = 0;
		int header_length = 0;
		for (char c : chars) {
			charhex = getHexDump(String.valueOf(c));
			System.out.println(c + ":" + charhex);
			if (header) {
				if (i == 0) {
					header_length = Integer.parseInt(charhex);
				}
				if (i > header_length) {
					if (GlobalCache.SpecialEncoding.containsKey(charhex)) {
						charhex = GlobalCache.SpecialEncoding.get(charhex);
					}
				}
				i++;
			} else {
				if (GlobalCache.SpecialEncoding.containsKey(charhex)) {
					charhex = GlobalCache.SpecialEncoding.get(charhex);
					System.out.println("Found " + charhex);
				} else {
					System.out.println("Not Found " + charhex);
				}
			}
			hex += charhex;
		}
		// logger.info("Hex: " + hex);
		return Converter.getUnicode(hex.toCharArray());
	}

	private String replaceEncoding(String content, boolean isheader) throws Exception {
		String dump = "";
		String header = "";
		// int dataLen = getString.length();
		byte[] buffer = content.getBytes(Data.ENC_UTF16_BE);
		for (int i = 0; i < buffer.length; i++) {
			dump += Character.forDigit((buffer[i] >> 4) & 0x0f, 16);
			dump += Character.forDigit(buffer[i] & 0x0f, 16);
			// System.out.println(dump);
		}
		buffer = null;
		dump = dump.toUpperCase();
		// System.out.println("dump: " + dump);
		if (isheader) {
			int header_length = Integer.parseInt(dump.substring(0, 4));
			if (header_length == 5) {
				header = dump.substring(0, 24);
				dump = dump.substring(24, dump.length());
			} else if (header_length == 6) {
				header = dump.substring(0, 28);
				dump = dump.substring(28, dump.length());
			}
		}
		for (String key : GlobalCache.SpecialEncoding.keySet()) {
			String charhex = GlobalCache.SpecialEncoding.get(key);
			if (key.length() == 8) {
				if (dump.contains(key)) {
					dump = dump.replaceAll(key, charhex);
				}
			}
		}
		int len = dump.length();
		// System.out.println("len: " + len);
		int temp = 0, chars = 4;
		String[] equalStr = new String[len / chars];
		for (int i = 0; i < len; i = i + chars) {
			equalStr[temp] = dump.substring(i, i + chars);
			temp++;
		}
		dump = "";
		for (int i = 0; i < equalStr.length; i++) {
			if (GlobalCache.SpecialEncoding.containsKey(equalStr[i])) {
				dump += GlobalCache.SpecialEncoding.get(equalStr[i]);
			} else {
				dump += equalStr[i];
			}
		}
		// System.out.println("Replaced: " + dump);
		dump = header + dump;
		return Converter.getUnicode(dump.toCharArray());
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
		stop = true;
		logger.info("SmscQueueSelection Thread Stopping.Queue: " + GlobalQueue.interProcessManage.size());
	}
}
