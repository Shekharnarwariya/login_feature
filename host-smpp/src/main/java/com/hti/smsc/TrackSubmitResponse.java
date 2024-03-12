package com.hti.smsc;

import java.io.File;
import java.math.BigInteger;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.ResponseObj;
import com.hti.objects.RoutePDU;
import com.hti.objects.SerialQueue;
import com.hti.smsc.dto.SmscEntry;
import com.hti.util.Constants;
import com.hti.util.FileUtil;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.SubmitSMResp;

public class TrackSubmitResponse implements Runnable {
	private int session_id;
	// private String entry.getName();
	private Logger logger = LoggerFactory.getLogger(TrackSubmitResponse.class);
	private Logger errorLogger = LoggerFactory.getLogger("submitErrLogger");
	private SerialQueue responseQueue;
	private boolean stop;
	private Map<Integer, String> sequence_map;
	private Map<String, String> response_map; // To Track Deliver_SM
	// private boolean isHexResponse = false;
	// private boolean isEnforceRoute = false;
	// private String enforcedRoute = null;
	// FileUtil fileUtil = new FileUtil();
	private int processed_counter = 0;
	private int total_processed_counter = 0;
	private boolean clear;
	private SmscEntry entry;

	public TrackSubmitResponse(SmscEntry entry, int session_id, SerialQueue responseQueue) {
		this.session_id = session_id;
		this.entry = entry;
		this.responseQueue = responseQueue;
		setResponseMap();
		setSequencemap();
		logger.info(entry.getName() + "_TrackSubmitResponse Started");
	}

	@Override
	public void run() {
		checkBackFolder();
		while (!stop) {
			if (responseQueue.isEmpty()) {
				if (clear) {
					stop = true;
				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			} else {
				int counter = 0;
				SubmitSMResp resp = null;
				while (!responseQueue.isEmpty()) {
					resp = (SubmitSMResp) responseQueue.dequeue();
					processResponse(resp);
					total_processed_counter++;
					if (++processed_counter >= 25000) {
						processed_counter = 0;
						logger.info(entry.getName() + " Submit Response Processed Count: " + total_processed_counter
								+ " ProcessingQueue: " + responseQueue.size());
					}
					if (clear) {
						continue;
					} else {
						if (++counter > 1000) {
							break;
						}
					}
				}
			}
		}
		if (!responseQueue.isEmpty()) {
			logger.info(entry.getName() + " Submit Response Processed Count: " + total_processed_counter
					+ " ProcessingQueue: " + responseQueue.size());
			writeQueue();
		}
		logger.info(entry.getName() + "_TrackSubmitResponse Stopped:" + responseQueue.size());
	}

	private void setResponseMap() {
		this.response_map = GlobalCache.smscwiseResponseMap.get(session_id);
	}

	private void setSequencemap() {
		this.sequence_map = GlobalCache.smscwisesequencemap.get(entry.getName());
	}

	private void processResponse(SubmitSMResp recieved_submit_response) {
		// LogPDU log_pdu = null;
		String message_id = null;
		String oldRoute = null;
		try {
			boolean insert_mis = true, insert_mappedid = true;
			Integer to_be_cheaked = recieved_submit_response.getSequenceNumber();
			if (sequence_map.containsKey(to_be_cheaked)) {
				message_id = sequence_map.remove(to_be_cheaked);
			}
			if (message_id != null) {
				GlobalCache.SmscSubmitTime.remove(message_id);
				if (recieved_submit_response.getCommandStatus() == Data.ESME_ROK) {
					String recieved_msg_id = recieved_submit_response.getMessageId();
					if (GlobalCache.enforcedlist.contains(message_id)) {
						GlobalCache.enforcedlist.remove(message_id);
						insert_mis = false; // Already inserted to mis record as ATES
					} else {
						GlobalQueue.smsc_in_delete_Queue.enqueue(message_id);
					}
					if (entry.isHexResponse()) {
						try {
							recieved_msg_id = String.valueOf(new BigInteger(recieved_msg_id.toUpperCase(), 16));
						} catch (Exception ex) {
							logger.info(ex + " While Converting Response Hex: " + recieved_msg_id);
						}
					}
					try {
						if (entry.getEnforceSmsc() != null) {
							if (GlobalCache.ResendPDUCache.containsKey(message_id)) {
								RoutePDU routePDU = GlobalCache.ResendPDUCache.get(message_id);
								oldRoute = routePDU.getSmsc();
								routePDU.getRequestPDU().setSequenceNumber(to_be_cheaked);
								routePDU.setSmsc(entry.getEnforceSmsc());
								routePDU.setRerouted(true);
								routePDU.setRoutedSmsc(entry.getName());
								routePDU.setSequence_no(to_be_cheaked);
								GlobalQueue.interProcessManage.enqueue(routePDU);
								GlobalCache.enforcedlist.add(message_id);
								GlobalCache.ResendPDUCache.remove(message_id);
								routePDU = null;
								insert_mappedid = false;
							}
						}
					} catch (Exception ex) {
					}
					if (insert_mappedid) {
						response_map.put(recieved_msg_id, message_id);
					}
					GlobalQueue.processResponseQueue.enqueue(new ResponseObj(message_id, recieved_msg_id, "ATES", "T",
							insert_mappedid, insert_mis, Data.SUBMIT_SM_RESP, oldRoute));
					if (recieved_msg_id.length() > 10) {
						recieved_msg_id = recieved_msg_id.substring(0, 10);
					}
					System.out.println("< " + recieved_msg_id + " >" + "<---Submit Response[" + message_id + "]--->"
							+ entry.getName() + "---->");
				} else {
					// status = "ERR_RESP";
					String Err_resp = getStringStatusErrorCode(recieved_submit_response.getCommandStatus());
					String flag_symbol = "E";
					if (GlobalCache.EsmeErrorCode.containsKey(Err_resp)) {
						flag_symbol = GlobalCache.EsmeErrorCode.get(Err_resp);
					}
					GlobalQueue.processResponseQueue.enqueue(new ResponseObj(message_id, "", "ERR_RESP", flag_symbol,
							false, insert_mis, Data.SUBMIT_SM_RESP, oldRoute));
					/*
					 * System.out.println(entry.getName() + " <--- Submit Error[" + message_id + "] ---> " + Err_resp + "<" + flag_symbol + ">" + "<" + recieved_submit_response.getCommandStatus() +
					 * ">");
					 */
					String content = entry.getName() + "(seq: " + to_be_cheaked + ")(msgid: " + message_id + ")(Err: "
							+ Err_resp + " (comm: " + recieved_submit_response.getCommandStatus() + "))";
					errorLogger.info(content);
				}
			} else {
				// System.out.println(entry.getName() + " Mismatched Response : " + recieved_submit_response.debugString());
				errorLogger.info(entry.getName() + " Mismatched Response : " + recieved_submit_response.debugString());
			}
		} catch (Exception e) {
			logger.error("processResponse:" + recieved_submit_response.debugString(), e.fillInStackTrace());
		}
	}

	private String getStringStatusErrorCode(int commandStatus) {
		String toReturn = "";
		switch (commandStatus) {
		case Data.ESME_RINVMSGLEN:
			toReturn = "ESME_RINVMSGLEN";
			break;
		case Data.ESME_RINVCMDLEN:
			toReturn = "ESME_RINVCMDLEN";
			break;
		case Data.ESME_RINVCMDID:
			toReturn = "ESME_RINVCMDID";
			break;
		case Data.ESME_RINVBNDSTS:
			toReturn = "ESME_RINVBNDSTS";
			break;
		case Data.ESME_RALYBND:
			toReturn = "ESME_RALYBND";
			break;
		case Data.ESME_RINVPRTFLG:
			toReturn = "ESME_RINVPRTFLG";
			break;
		case Data.ESME_RINVREGDLVFLG:
			toReturn = "ESME_RINVREGDLVFLG";
			break;
		case Data.ESME_RSYSERR:
			toReturn = "ESME_RSYSERR";
			break;
		case Data.ESME_RINVSRCADR:
			toReturn = "ESME_RINVSRCADR";
			break;
		case Data.ESME_RINVDSTADR:
			toReturn = "ESME_RINVDSTADR";
			break;
		case Data.ESME_RINVMSGID:
			toReturn = "ESME_RINVMSGID";
			break;
		case Data.ESME_RBINDFAIL:
			toReturn = "ESME_RBINDFAIL";
			break;
		case Data.ESME_RINVPASWD:
			toReturn = "ESME_RINVPASWD";
			break;
		case Data.ESME_RINVSYSID:
			toReturn = "ESME_RINVSYSID";
			break;
		case Data.ESME_RCANCELFAIL:
			toReturn = "ESME_RCANCELFAIL";
			break;
		case Data.ESME_RREPLACEFAIL:
			toReturn = "ESME_RREPLACEFAIL";
			break;
		case Data.ESME_RMSGQFUL:
			toReturn = "ESME_RMSGQFUL";
			break;
		case Data.ESME_RINVSERTYP:
			toReturn = "ESME_RINVSERTYP";
			break;
		case Data.ESME_RADDCUSTFAIL:
			toReturn = "ESME_RADDCUSTFAIL";
			break;
		case Data.ESME_RDELCUSTFAIL:
			toReturn = "ESME_RDELCUSTFAIL";
			break;
		case Data.ESME_RMODCUSTFAIL:
			toReturn = "ESME_RMODCUSTFAIL";
			break;
		case Data.ESME_RENQCUSTFAIL:
			toReturn = "ESME_RENQCUSTFAIL";
			break;
		case Data.ESME_RINVCUSTID:
			toReturn = "ESME_RINVCUSTID";
			break;
		case Data.ESME_RINVCUSTNAME:
			toReturn = "ESME_RINVCUSTNAME";
			break;
		case Data.ESME_RINVCUSTADR:
			toReturn = "ESME_RINVCUSTADR";
			break;
		case Data.ESME_RINVADR:
			toReturn = "ESME_RINVADR";
			break;
		case Data.ESME_RCUSTEXIST:
			toReturn = "ESME_RCUSTEXIST";
			break;
		case Data.ESME_RCUSTNOTEXIST:
			toReturn = "ESME_RCUSTNOTEXIST";
			break;
		case Data.ESME_RADDDLFAIL:
			toReturn = "ESME_RADDDLFAIL";
			break;
		case Data.ESME_RMODDLFAIL:
			toReturn = "ESME_RMODDLFAIL";
			break;
		case Data.ESME_RDELDLFAIL:
			toReturn = "ESME_RDELDLFAIL";
			break;
		case Data.ESME_RVIEWDLFAIL:
			toReturn = "ESME_RVIEWDLFAIL";
			break;
		case Data.ESME_RLISTDLSFAIL:
			toReturn = "ESME_RLISTDLSFAIL";
			break;
		case Data.ESME_RPARAMRETFAIL:
			toReturn = "ESME_RPARAMRETFAIL";
			break;
		case Data.ESME_RINVPARAM:
			toReturn = "ESME_RINVPARAM";
			break;
		case Data.ESME_RINVNUMDESTS:
			toReturn = "ESME_RINVNUMDESTS";
			break;
		case Data.ESME_RINVDLNAME:
			toReturn = "ESME_RINVDLNAME";
			break;
		case Data.ESME_RINVDLMEMBDESC:
			toReturn = "ESME_RINVDLMEMBDESC";
			break;
		case Data.ESME_RINVDLMEMBTYP:
			toReturn = "ESME_RINVDLMEMBTYP";
			break;
		case Data.ESME_RINVDLMODOPT:
			toReturn = "ESME_RINVDLMODOPT";
			break;
		case Data.ESME_RINVDESTFLAG:
			toReturn = "ESME_RINVDESTFLAG";
			break;
		case Data.ESME_RINVSUBREP:
			toReturn = "ESME_RINVSUBREP";
			break;
		case Data.ESME_RINVESMCLASS:
			toReturn = "ESME_RINVESMCLASS";
			break;
		case Data.ESME_RCNTSUBDL:
			toReturn = "ESME_RCNTSUBDL";
			break;
		case Data.ESME_RSUBMITFAIL:
			toReturn = "ESME_RSUBMITFAIL";
			break;
		case Data.ESME_RINVSRCTON:
			toReturn = "ESME_RINVSRCTON";
			break;
		case Data.ESME_RINVSRCNPI:
			toReturn = "ESME_RINVSRCNPI";
			break;
		case Data.ESME_RINVDSTTON:
			toReturn = "ESME_RINVDSTTON";
			break;
		case Data.ESME_RINVDSTNPI:
			toReturn = "ESME_RINVDSTNPI";
			break;
		case Data.ESME_RINVSYSTYP:
			toReturn = "ESME_RINVSYSTYP";
			break;
		case Data.ESME_RINVREPFLAG:
			toReturn = "ESME_RINVREPFLAG";
			break;
		case Data.ESME_RINVNUMMSGS:
			toReturn = "ESME_RINVNUMMSGS";
			break;
		case Data.ESME_RTHROTTLED:
			toReturn = "ESME_RTHROTTLED";
			break;
		case Data.ESME_RPROVNOTALLWD:
			toReturn = "ESME_RPROVNOTALLWD";
			break;
		case Data.ESME_RINVSCHED:
			toReturn = "ESME_RINVSCHED";
			break;
		case Data.ESME_RINVEXPIRY:
			toReturn = "ESME_RINVEXPIRY";
			break;
		case Data.ESME_RINVDFTMSGID:
			toReturn = "ESME_RINVDFTMSGID";
			break;
		case Data.ESME_RX_T_APPN:
			toReturn = "ESME_RX_T_APPN";//// ESME Receiver Temporary App Error Code
			break;
		case Data.ESME_RX_P_APPN:
			toReturn = "ESME_RX_P_APPN";
			break;
		case Data.ESME_RX_R_APPN:
			toReturn = "ESME_RX_R_APPN";/// ESME Receiver Reject Message Error Code
			break;
		case Data.ESME_RQUERYFAIL:
			toReturn = "ESME_RQUERYFAIL";
			break;
		case Data.ESME_RINVPGCUSTID:
			toReturn = "ESME_RINVPGCUSTID";
			break;
		case Data.ESME_RINVPGCUSTIDLEN:
			toReturn = "ESME_RINVPGCUSTIDLEN";
			break;
		case Data.ESME_RINVCITYLEN:
			toReturn = "ESME_RINVCITYLEN";
			break;
		case Data.ESME_RINVSTATELEN:
			toReturn = "ESME_RINVSTATELEN";
			break;
		case Data.ESME_RINVZIPPREFIXLEN:
			toReturn = "ESME_RINVZIPPREFIXLEN";
			break;
		case Data.ESME_RINVZIPPOSTFIXLEN:
			toReturn = "ESME_RINVZIPPOSTFIXLEN";
			break;
		case Data.ESME_RINVMINLEN:
			toReturn = "ESME_RINVMINLEN";
			break;
		case Data.ESME_RINVMIN:
			toReturn = "ESME_RINVMIN";
			break;
		case Data.ESME_RINVPINLEN:
			toReturn = "ESME_RINVPINLEN";
			break;
		case Data.ESME_RINVTERMCODELEN:
			toReturn = "ESME_RINVTERMCODELEN";
			break;
		case Data.ESME_RINVCHANNELLEN:
			toReturn = "ESME_RINVCHANNELLEN";
			break;
		case Data.ESME_RINVCOVREGIONLEN:
			toReturn = "ESME_RINVCOVREGIONLEN";
			break;
		case Data.ESME_RINVCAPCODELEN:
			toReturn = "ESME_RINVCAPCODELEN";
			break;
		case Data.ESME_RINVMDTLEN:
			toReturn = "ESME_RINVMDTLEN";
			break;
		case Data.ESME_RINVPRIORMSGLEN:
			toReturn = "ESME_RINVPRIORMSGLEN";
			break;
		case Data.ESME_RINVPERMSGLEN:
			toReturn = "ESME_RINVPERMSGLEN";
			break;
		case Data.ESME_RINVPGALERTLEN:
			toReturn = "ESME_RINVPGALERTLEN";
			break;
		case Data.ESME_RINVSMUSERLEN:
			toReturn = "ESME_RINVSMUSERLEN";
			break;
		case Data.ESME_RINVRTDBLEN:
			toReturn = "ESME_RINVRTDBLEN";
			break;
		case Data.ESME_RINVREGDELLEN:
			toReturn = "ESME_RINVREGDELLEN";
			break;
		case Data.ESME_RINVMSGDISTLEN:
			toReturn = "ESME_RINVMSGDISTLEN";
			break;
		case Data.ESME_RINVPRIORMSG:
			toReturn = "ESME_RINVPRIORMSG";
			break;
		case Data.ESME_RINVMDT:
			toReturn = "ESME_RINVMDT";
			break;
		case Data.ESME_RINVPERMSG:
			toReturn = "ESME_RINVPERMSG";
			break;
		case Data.ESME_RINVMSGDIST:
			toReturn = "ESME_RINVMSGDIST";
			break;
		case Data.ESME_RINVPGALERT:
			toReturn = "ESME_RINVPGALERT";
			break;
		case Data.ESME_RINVSMUSER:
			toReturn = "ESME_RINVSMUSER";
			break;
		case Data.ESME_RINVRTDB:
			toReturn = "ESME_RINVRTDB";
			break;
		case Data.ESME_RINVREGDEL:
			toReturn = "ESME_RINVREGDEL";
			break;
		case Data.ESME_RINVOPTPARSTREAM:
			toReturn = "ESME_RINVOPTPARSTREAM";
			break;
		case Data.ESME_ROPTPARNOTALLWD:
			toReturn = "ESME_ROPTPARNOTALLWD";
			break;
		case Data.ESME_RINVOPTPARLEN:
			toReturn = "ESME_RINVOPTPARLEN";
			break;
		case Data.ESME_RMISSINGOPTPARAM:
			toReturn = "ESME_RMISSINGOPTPARAM";
			break;
		case Data.ESME_RINVOPTPARAMVAL:
			toReturn = "ESME_RINVOPTPARAMVAL";
			break;
		case Data.ESME_RDELIVERYFAILURE:
			toReturn = "ESME_RDELIVERYFAILURE";
			break;
		case Data.ESME_RUNKNOWNERR:
			toReturn = "ESME_RUNKNOWNERR";
			break;
		default:
			toReturn = "UNKNOWNERR";
			break;
		}
		// logger.info("Getting Error Response (" + entry.getName() + ") for Submit PDU :" + toReturn);
		return toReturn;
	}

	private void writeQueue() {
		// logger.info(entry.getName() + " Response Process Queue : " + responseQueue.size());
		try {
			FileUtil.writeObject(Constants.resp_backup_dir + entry.getName() + "-responseQueue.ser", responseQueue);
		} catch (Exception ex) {
			logger.error(ex + " While Writing responseQueue Object of " + entry.getName());
		}
	}

	private void checkBackFolder() {
		logger.info("<- Checking for Response Backup File -> " + entry.getName());
		File file = new File(Constants.resp_backup_dir + entry.getName() + "-responseQueue.ser");
		if (file.exists()) {
			logger.info("<- Response Backup File Found -> " + entry.getName());
			try {
				SerialQueue tempQueue = (SerialQueue) FileUtil
						.readObject(Constants.resp_backup_dir + entry.getName() + "-responseQueue.ser", true);
				if (!tempQueue.isEmpty()) {
					logger.info(entry.getName() + " Response Backup Queue Size ---> " + tempQueue.size());
					while (!tempQueue.isEmpty()) {
						responseQueue.enqueue((SubmitSMResp) tempQueue.dequeue());
					}
				}
			} catch (Exception e) {
				logger.error(e + " While Reading responseQueue Object of " + entry.getName());
			}
		}
	}

	public void stop() {
		clear = true;
		logger.info(entry.getName() + "_TrackSubmitResponse Stopping.Queue: " + responseQueue.size());
	}
}
