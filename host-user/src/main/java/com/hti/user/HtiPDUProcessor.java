package com.hti.user;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.exception.EntryNotFoundException;
import com.hti.objects.HTIQueue;
import com.hti.objects.ProfitLogObject;
import com.hti.objects.RoutePDU;
import com.hti.objects.RoutingDTO;
import com.hti.user.dto.SessionEntry;
import com.hti.user.dto.UserEntry;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindResponse;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.SubmitSMResp;
import com.logica.smpp.pdu.ValueNotSetException;
import com.logica.smpp.pdu.WrongDateFormatException;
import com.logica.smpp.pdu.WrongLengthOfStringException;
import com.hti.objects.RequestLog;

/**
 * @author administrator
 */
public class HtiPDUProcessor extends PDUProcessor {
	/**
	 * The session this processor uses for sending of PDUs.
	 */
	private UserSession session = null;
	/**
	 * Indicates if the bound has passed.
	 */
	private boolean bound = false;
	/**
	 * The system id of the bounded ESME.
	 */
	private String systemId = null;
	private String systemType;
	/**
	 * The message id assigned by simulator to submitted messages.
	 */
	// private int intMessageId = 1;
	/**
	 * System id of this simulator sent to the ESME in bind response.
	 */
	private final String SYSTEM_ID = "SMPP SERVER";
	// ---------------------------------------
	private String SID;
	private StringTokenizer stringToken;
	private String str[];
	private String flag_sid = "no";
	private HTIQueue processQueue;
	private RoutePDU routepdu;
	private Logger logger = LoggerFactory.getLogger("userLogger");
	// private Logger pdulogger = LoggerFactory.getLogger("submitLogger");
	// private Logger omqlogger = LoggerFactory.getLogger("omqLogger");
	private Logger tracklogger = LoggerFactory.getLogger("trackLogger");
	private Map<Integer, RoutingDTO> RoutingMap = null;
	private Map<Integer, RoutingDTO> adminRouting = null;
	private Map<Integer, RoutingDTO> mmsRoutingMap = null;
	private Map<Integer, RoutingDTO> mmsAdminRouting = null;
	private Map<String, Integer> NetworkMap = null;
	private Map<Integer, Map<String, Boolean>> NetworkBsfm = null;
	private RoutingThread routingThread = null;
	private UserEntry user;
	private UserBalance balance;
	private UserBalance adminBalance;
	private SessionManager userSession;
	private long in_counter = 0;
	private long out_counter = 0;
	private int inter_count = 0;
	private boolean isExpired;
	private int MAX_SRC_ADDR_LENGTH = 16;
	private boolean isValidRouting;
	private int user_id;
	private int master_user_id;
	private String remoteAddress;
	private RequestLog request_log;
	private int localPort;

	public HtiPDUProcessor(UserSession session) {// ,Table users
		this.session = session;
	}

	/**
	 * Depending on the <code>commandId</code> of the <code>request</code> creates the proper response. The first request must be a <code>BindRequest</code> with the correct parameters.
	 *
	 * @param request
	 *            the request from client
	 */
	@Override
	public void clientRequest(com.logica.smpp.pdu.Request request) {
		// logger.info(request.debugString());
		Response response;
		int commandStatus;
		// byte registerdlr;// change done by ashish 190606
		// boolean credit = true;
		int commandId = request.getCommandId();
		try {
			if (!bound) { // the first PDU must be bound request
				if ((commandId == Data.BIND_TRANSMITTER || commandId == Data.BIND_RECEIVER
						|| commandId == Data.BIND_TRANSCEIVER)) {
					systemId = ((BindRequest) request).getSystemId();
					systemType = ((BindRequest) request).getSystemType();
					session.setSystemid(systemId);
					commandStatus = checkIdentity((BindRequest) request);
					if (commandStatus == Data.ESME_ROK) {
						BindResponse bindResponse = (BindResponse) request.getResponse();
						if (commandId == Data.BIND_RECEIVER) {
							session.setMode("reciever");
						} else if (commandId == Data.BIND_TRANSCEIVER) {
							session.setMode("tranciever");
						} else if (commandId == Data.BIND_TRANSMITTER) {
							session.setMode("transmitter");
						}
						if (userSession.add(session)) {
							if (systemType != null && systemType.equalsIgnoreCase("BULK")) {
								bindResponse.setSystemId(session.getSessionId());
							} else {
								bindResponse.setSystemId(SYSTEM_ID);
							}
							GlobalVars.userService.updateSession(new SessionEntry(systemId, remoteAddress,
									GlobalVars.SERVER_ID, userSession.getSessionCount(), userSession.getRxCount(),
									userSession.getTxCount(), userSession.getTRxCount(), localPort));
							bound = true;
							session.setPduLog(user.isLogging());
							// logger.info(systemId + " SenderId Length Restriction: " + user.getSenderLength());
							if (user.getSenderLength() > 0) {
								logger.info(systemId + " SenderId Length Restricted: " + user.getSenderLength());
							}
							if (serverResponse(bindResponse)) {
								if (GlobalCache.UserDisconnectionAlert.containsKey(systemId)) {
									GlobalCache.UserDisconnectionAlert.remove(systemId);
								}
								// logger.debug(systemId + " -> " + bindResponse.debugString());
								session.setReceiveTimeout((user.getTimeout() + 5) * 1000); // 5 Seconds more
								session.increaseCloseTime();
								if (flag_sid.equals("yes")) {
									if (SID == null) {
										SID = "";
									}
									stringToken = new StringTokenizer(SID, ",");
									setStringArray(stringToken);
								}
								if (commandId == Data.BIND_RECEIVER || commandId == Data.BIND_TRANSCEIVER) {
									UserDeliverForward userDeliverForward = GlobalVars.userService
											.checkDeliverForward(systemId);
									synchronized (userDeliverForward) {
										if (!userDeliverForward.isStarted()) {
											userDeliverForward.setSession(session);
											new Thread(userDeliverForward, "UserDeliverForward_" + systemId).start();
										} else {
											if (!userDeliverForward.isActive()) {
												userDeliverForward.setSession(session);
											}
										}
									}
								}
								// setSessionIn();
								if (balance.getFlag().equalsIgnoreCase("no")) {
									logger.info(systemId + " C R E D I T S :" + balance.getCredit());
								} else {
									logger.info(systemId + " B A L A N C E :"
											+ new DecimalFormat("0.00000").format(balance.getAmount()));
								}
							}
						} else {
							bindResponse.setCommandStatus(Data.ESME_RBINDFAIL);
							serverResponse(bindResponse);
							session.stop();
							GlobalVars.userService.updateBindError(new SessionEntry(systemId, remoteAddress,
									GlobalVars.SERVER_ID, Data.ESME_RBINDFAIL, localPort));
						}
					} else {
						response = request.getResponse();
						response.setCommandStatus(commandStatus);
						serverResponse(response);
						session.stop();
						logger.info(systemId + " <-- Session Stopped -->");
						if (commandStatus == Data.ESME_RINVBALANCE) {
							GlobalCache.LowBalanceUser.add(systemId);
						}
						GlobalVars.userService.updateBindError(new SessionEntry(systemId, remoteAddress,
								GlobalVars.SERVER_ID, commandStatus, localPort));
					}
				} else {
					logger.error(" Not Bound. Invalid PDU Received. <" + commandId + ">");
					response = request.getResponse();
					response.setCommandStatus(Data.ESME_RINVCMDID);
					if (session.sendResponse(response)) {
						session.stop();
					}
				}
			} else { // already bound, can receive other PDUs
				if (request.canResponse()) {
					response = request.getResponse();
					if (++inter_count > 100) {
						isExpired = isExpired();
						inter_count = 0;
					}
					if (isExpired) {
						logger.error(systemId + " < Account Expired >");
						response.setCommandStatus(Data.ESME_RINVEXPIRY);
						serverResponse(response);
						session.stop();
					} else {
						boolean nextStep = true;
						switch (commandId) { // for selected PDUs do extra steps
						case Data.SUBMIT_SM:
							tracklogger.debug(systemId + " Processing: " + request.debugString());
							if (!(session.getMode()).equals("reciever")) {
								String desti = ((SubmitSM) request).getDestAddr().getAddress();
								// --------- Validating Destination Address
								// ------------------
								if (desti != null && desti.length() > 0) {
									desti = desti.replaceAll("\\s+", ""); // Replace all the spaces in the String with empty character.
									desti = desti.substring(desti.lastIndexOf("+") + 1); // Remove +
									desti = desti.trim();
									try {
										long parsedNumber = Long.parseLong(desti);
										desti = parsedNumber + "";
										((SubmitSM) request).setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164,
												desti);
									} catch (Exception ex) {
										logger.error(ex + " While Parsing Destination[" + systemId + "]: " + desti);
										nextStep = false;
									}
								} else {
									nextStep = false;
								}
								if (nextStep) {
									if (((SubmitSM) request).getShortMessage() == null
											|| ((SubmitSM) request).getShortMessage().length() <= 0) {
										nextStep = false;
										logger.error(systemId + " Invalid Message: [msg:"
												+ ((SubmitSM) request).getShortMessage() + "][esm:"
												+ ((SubmitSM) request).getEsmClass() + "][dcs:"
												+ ((SubmitSM) request).getDataCoding() + "][pdu:"
												+ request.debugString() + "]");
										response.setCommandStatus(Data.ESME_RINVMSGLEN);
										serverResponse(response);
									} else {
										if ((((SubmitSM) request).getDataCoding() == (byte) 8)
												|| (((SubmitSM) request).getDataCoding() == (byte) 245)) { // unicode
											if (((SubmitSM) request).getShortMessage().length() > 140) {
												nextStep = false;
												logger.error(systemId + " Invalid Message Length: [msg:"
														+ ((SubmitSM) request).getShortMessage().length() + "][esm:"
														+ ((SubmitSM) request).getEsmClass() + "][dcs:"
														+ ((SubmitSM) request).getDataCoding() + "][pdu:"
														+ request.debugString() + "]");
												response.setCommandStatus(Data.ESME_RINVMSGLEN);
												serverResponse(response);
											}
										} else {
											if (((SubmitSM) request).getShortMessage().length() > 160) {
												nextStep = false;
												logger.error(systemId + " Invalid Message Length: [msg:"
														+ ((SubmitSM) request).getShortMessage().length() + "][esm:"
														+ ((SubmitSM) request).getEsmClass() + "][dcs:"
														+ ((SubmitSM) request).getDataCoding() + "][pdu:"
														+ request.debugString() + "]");
												response.setCommandStatus(Data.ESME_RINVMSGLEN);
												serverResponse(response);
											}
										}
									}
								} else {
									logger.error(systemId + " Invalid Destination Address: " + desti);
									response.setCommandStatus(Data.ESME_RINVDSTADR);
									serverResponse(response);
								}
								String source = ((SubmitSM) request).getSourceAddr().getAddress();
								if (nextStep) {
									if (source == null || source.length() > MAX_SRC_ADDR_LENGTH) {
										nextStep = false;
										logger.error(systemId + " Invalid SourceAddress: " + source);
										response.setCommandStatus(Data.ESME_RINVSRCADR);
										serverResponse(response);
									} else {
										if (user.getSenderLength() > 0) {
											if (source.length() != user.getSenderLength()) {
												if (source.length() > user.getSenderLength() && user.isSenderTrim()) {
													source = source.substring(0, user.getSenderLength());
													byte ton = ((SubmitSM) request).getSourceAddr().getTon();
													byte npi = ((SubmitSM) request).getSourceAddr().getNpi();
													((SubmitSM) request).setSourceAddr(ton, npi, source);
													logger.info(systemId + " SourceAddr Trim: " + source);
												} else {
													nextStep = false;
													logger.error(systemId + " Invalid SourceAddress Length: " + source
															+ " Configured: " + user.getSenderLength());
													response.setCommandStatus(Data.ESME_RINVSRCADR);
													serverResponse(response);
												}
											}
										}
									}
								}
								if (nextStep) {
									in_counter++;
									SubmitSMResp submitResponse = (SubmitSMResp) response;
									RoutingDTO routing = null;
									String channel_type = null;
									if (((SubmitSM) request).getExtraOptional((short) 0x1500) != null) {
										try {
											channel_type = new String(((SubmitSM) request)
													.getExtraOptional((short) 0x1500).getData().getBuffer())
															.substring(4);
										} catch (ValueNotSetException e) {
											logger.error("Channel Type Not Configured");
										}
									}
									if (channel_type != null && channel_type.startsWith("mms")) {
										routing = getMmsRouting(desti);
									} else {
										routing = getRouting(desti);
									}
									double masterCost = 0;
									if (routing != null) {
										if (NetworkBsfm.containsKey(routing.getNetworkId())) {
											boolean source_matched = false;
											boolean reverse = false;
											int i = 0;
											for (String source_key : NetworkBsfm.get(routing.getNetworkId()).keySet()) {
												if (i == 0) {
													reverse = NetworkBsfm.get(routing.getNetworkId()).get(source_key);
												}
												if (Pattern.compile(source_key).matcher(source.toLowerCase()).find()) {
													source_matched = true;
													break;
												}
												i++;
											}
											if (reverse) {
												if (!source_matched) {
													logger.error(systemId + " Source Mismatched[Reverse]: " + source
															+ " network: " + routing.getNetworkId());
													nextStep = false;
												}
											} else {
												if (source_matched) {
													logger.error(systemId + " Source Matched: " + source + " network: "
															+ routing.getNetworkId());
													nextStep = false;
												}
											}
										}
										if (!nextStep) {
											response.setCommandStatus(Data.ESME_RINVSRCADR);
											serverResponse(response);
										} else {
											if (user.isAdminDepend()) {
												nextStep = false;
												RoutingDTO admin_routing = null;
												if (channel_type != null) {
													admin_routing = getMmsRouting(desti, user.getMasterId());
												} else {
													admin_routing = getRouting(desti, user.getMasterId());
												}
												if (admin_routing != null) {
													if (adminBalance.getFlag().equalsIgnoreCase("yes")) {
														if (adminBalance.deductAmount(admin_routing.getCost())) {
															masterCost = admin_routing.getCost();
															nextStep = true;
														} else {
															logger.error("< " + systemId + " >" + "Insufficient Admin <"
																	+ user.getMasterId() + "> Balance");
															response.setCommandStatus(Data.ESME_RINVBALANCE);
															serverResponse(response);
															session.stop();
														}
													} else if (adminBalance.getFlag().equalsIgnoreCase("MIN")) {
														// credit = false;
														response.setCommandStatus(Data.ESME_RINVBALANCE);
														serverResponse(response);
														logger.error("< " + systemId + " >" + "Insufficient Admin <"
																+ user.getMasterId() + "> Balance MIN");
														session.stop();
													} else if (adminBalance.getFlag().equalsIgnoreCase("no")) {
														if (adminBalance.deductCredit(1)) {
															nextStep = true;
														} else {
															logger.error("< " + systemId + " >" + "Insufficient Admin <"
																	+ user.getMasterId() + "> Credits");
															response.setCommandStatus(Data.ESME_RINVBALANCE);
															serverResponse(response);
															session.stop();
														}
													}
												} else {
													response.setCommandStatus(Data.ESME_RINVDSTADR);
													serverResponse(response);
													logger.error(systemId + " Admin<" + user.getMasterId()
															+ "> Routing Error : "
															+ ((SubmitSM) request).getDestAddr().getAddress());
												}
											} else {
												if (adminBalance.getFlag().equalsIgnoreCase("yes")
														|| adminBalance.getFlag().equalsIgnoreCase("MIN")) {
													RoutingDTO admin_routing = null;
													if (channel_type != null) {
														admin_routing = getMmsRouting(desti, user.getMasterId());
													} else {
														admin_routing = getRouting(desti, user.getMasterId());
													}
													masterCost = admin_routing.getCost();
												}
											}
											if (nextStep) {
												boolean isDoubleCost = false;
												String smsc = routing.getSmsc();
												boolean is_route_hlr = routing.isHlr();
												if (routingThread == null) {
													routingThread = GlobalVars.userService.getRoutingThread(systemId);
													processQueue = routingThread.getProcessQueue();
												}
												if (routing.getCodeLength() > 0) { // to check if route will add some code at their side
													if (((SubmitSM) request).getEsmClass() == (byte) Data.SM_UDH_GSM
															|| ((SubmitSM) request).getEsmClass() == Data.SM_UDH_GSM_2) // multipart
													{
														int[] partDescription = getPartDescription(request);
														int total_parts = partDescription[0];
														int part_number = partDescription[1];
														logger.debug("Total Parts: " + total_parts + " part_number: "
																+ part_number);
														if (total_parts == part_number) { // last part
															String content = "";
															if ((((SubmitSM) request).getDataCoding() == (byte) 8)
																	|| (((SubmitSM) request)
																			.getDataCoding() == (byte) 245)) {
																try {
																	content = ((SubmitSM) request)
																			.getShortMessage(Data.ENC_UTF16_BE);
																} catch (UnsupportedEncodingException une) {
																}
																if ((content.length()
																		+ routing.getCodeLength()) > 140) { // double cost deduction
																	isDoubleCost = true;
																}
															} else {
																content = ((SubmitSM) request).getShortMessage();
																if ((content.length()
																		+ routing.getCodeLength()) > 160) { // double cost deduction
																	isDoubleCost = true;
																}
															}
															logger.debug("Content Length: " + content.length()
																	+ " Code_length: " + routing.getCodeLength()
																	+ " Total Length: "
																	+ (content.length() + routing.getCodeLength()));
														}
													} else {
														String content = "";
														if ((((SubmitSM) request).getDataCoding() == (byte) 8)
																|| (((SubmitSM) request)
																		.getDataCoding() == (byte) 245)) {
															try {
																content = ((SubmitSM) request)
																		.getShortMessage(Data.ENC_UTF16_BE);
															} catch (UnsupportedEncodingException une) {
															}
															if ((content.length() + routing.getCodeLength()) > 140) { // double cost deduction
																isDoubleCost = true;
															}
														} else {
															content = ((SubmitSM) request).getShortMessage();
															if ((content.length() + routing.getCodeLength()) > 160) { // double cost deduction
																isDoubleCost = true;
															}
														}
														logger.debug(
																"Content Length: " + content.length() + " Code_length: "
																		+ routing.getCodeLength() + " Total Length: "
																		+ (content.length() + routing.getCodeLength()));
													}
												}
												if (balance.getFlag().equalsIgnoreCase("yes")) {
													double cost = 0;
													if (isDoubleCost) {
														cost = routing.getCost() * 2;
														logger.debug("Destination<" + desti + "> Double Cost Route <"
																+ routing.getSmsc() + "> " + cost);
													} else {
														cost = routing.getCost();
													}
													if (user.isHlr() && is_route_hlr) {
														if (((SubmitSM) request).getEsmClass() == (byte) Data.SM_UDH_GSM
																|| ((SubmitSM) request)
																		.getEsmClass() == Data.SM_UDH_GSM_2) // multipart
														{
															int[] partDescription = getPartDescription(request);
															int part_number = partDescription[1];
															if (part_number == 1) {
																cost = cost + routing.getHlrCost();
															}
														} else {
															cost = cost + routing.getHlrCost();
														}
													}
													try {
														if (balance.deductAmount(cost)) {
															String temp_msg_id = GlobalVars.assignMessageId();
															tracklogger.debug(systemId + " [" + temp_msg_id + "]: "
																	+ request.getSequenceNumber());
															submitResponse.setMessageId(temp_msg_id);
															serverResponse(response);
															tracklogger.debug(systemId + " [" + temp_msg_id
																	+ "] Response: " + response.getSequenceNumber());
															System.out.println("< " + systemId + " <" + temp_msg_id
																	+ "> Pdu Received <" + desti + ">----" + smsc);
															// ((SubmitSM) request).setRegisteredDelivery((byte) 1);
															if (routing.getExpiry() == null) {
																try {
																	((SubmitSM) request).setValidityPeriod(null);
																} catch (WrongDateFormatException ex) {
																	// nothing to do
																}
															} else {
																if (!routing.getExpiry().equalsIgnoreCase("0")) { // set expiry for pdu
																	try {
																		((SubmitSM) request)
																				.setValidityPeriod(routing.getExpiry()); // either NULL or
																															// YYMMDDhhmmsstnnp
																	} catch (WrongDateFormatException ex) {
																		// nothing
																		// to do
																	}
																}
															}
															// registerdlr = ((SubmitSM) request).getRegisteredDelivery();
															try {
																if ((((SubmitSM) request).getDataCoding() == (byte) 8)
																		|| (((SubmitSM) request)
																				.getDataCoding() == (byte) 245)) {
																	request_log = new RequestLog(temp_msg_id,
																			((SubmitSM) request).getSequenceNumber(),
																			getHexDump(((SubmitSM) request)
																					.getShortMessage(
																							Data.ENC_UTF16_BE)),
																			((SubmitSM) request).getDestAddr()
																					.getAddress(),
																			((SubmitSM) request).getSourceAddr()
																					.getAddress(),
																			((SubmitSM) request)
																					.getRegisteredDelivery(),
																			((SubmitSM) request).getEsmClass(),
																			((SubmitSM) request).getDataCoding(),
																			systemId, remoteAddress);
																} else {
																	request_log = new RequestLog(temp_msg_id,
																			((SubmitSM) request).getSequenceNumber(),
																			getHexDump(((SubmitSM) request)
																					.getShortMessage()),
																			((SubmitSM) request).getDestAddr()
																					.getAddress(),
																			((SubmitSM) request).getSourceAddr()
																					.getAddress(),
																			((SubmitSM) request)
																					.getRegisteredDelivery(),
																			((SubmitSM) request).getEsmClass(),
																			((SubmitSM) request).getDataCoding(),
																			systemId, remoteAddress);
																}
																GlobalQueue.request_log_Queue.enqueue(request_log);
																tracklogger.debug(systemId + " [" + temp_msg_id
																		+ "]: Enqueued To RequestLog");
															} catch (Exception e) {
																logger.error(systemId, e);
															}
															routepdu = new RoutePDU(request, temp_msg_id,
																	((SubmitSM) request).getSequenceNumber(),
																	session.getSessionId(), user.getPriority());
															routepdu.setUsername(systemId);
															routepdu.setCost(cost);
															routepdu.setNetworkId(routing.getNetworkId());
															routepdu.setSmsc(smsc);
															routepdu.setGroupId(routing.getGroupId());
															routepdu.setWalletFlag(true);
															routepdu.setNumsmsc(routing.getNumsmsc());
															routepdu.setForceSIDNum(routing.getForceSIDNum());
															routepdu.setForceSIDAlpha(routing.getForceSIDAlpha());
															routepdu.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
																	.format(new java.sql.Timestamp(
																			System.currentTimeMillis())));
															routepdu.setHlr(is_route_hlr);
															routepdu.setBackupSmsc(routing.getBackupSmsc());
															routepdu.setRefund(routing.isRefund());
															routepdu.setOriginalSourceAddr(
																	((SubmitSM) request).getSourceAddr().getAddress());
															routepdu.setRegisterSenderId(routing.getRegisterSenderId());
															routepdu.setRegisterSmsc(routing.getRegisterSmsc());
															routepdu.setRegGroupId(routing.getRegGroupId());
															routepdu.setReplaceContent(routing.isReplaceContent());
															routepdu.setReplacement(routing.getReplacement());
															routepdu.setSmsLength(routing.getSmsLength());
															routepdu.setSourceAppender(routing.getSourceAppender());
															routepdu.setContentAppender(routing.getContentAppender());
															routepdu.setSenderReplacement(
																	routing.getSenderReplacement());
															routepdu.setMnp(routing.isMnp());
															routepdu.setMms(routing.isMms());
															if (desti.length() < GlobalVars.MIN_DESTINATION_LENGTH) {
																routepdu.setSmsc(GlobalVars.INVALID_DEST_SMSC);
																routepdu.setGroupId(0);
																logger.error(systemId + " Invalid Destination: " + desti
																		+ " Length: " + desti.length());
															} else {
																if (routing.getNumberLength() != null) {
																	if (routing.getNumberLength().contains(",")) {
																		try {
																			if (desti.length() < Integer
																					.parseInt(routing.getNumberLength()
																							.split(",")[0])
																					|| desti.length() > Integer
																							.parseInt(routing
																									.getNumberLength()
																									.split(",")[1])) {
																				routepdu.setSmsc(
																						GlobalVars.INVALID_DEST_SMSC);
																				routepdu.setGroupId(0);
																				logger.error(systemId + " Destination: "
																						+ desti + " Length: "
																						+ desti.length()
																						+ " Out Of Range: "
																						+ routing.getNumberLength());
																			}
																		} catch (Exception ex) {
																		}
																	} else {
																		try {
																			if (desti.length() != Integer.parseInt(
																					routing.getNumberLength())) {
																				routepdu.setSmsc(
																						GlobalVars.INVALID_DEST_SMSC);
																				routepdu.setGroupId(0);
																				logger.error(systemId + " Destination: "
																						+ desti + " Length: "
																						+ desti.length()
																						+ " Not Equal: "
																						+ routing.getNumberLength());
																			}
																		} catch (Exception ex) {
																		}
																	}
																}
															}
															processQueue.enqueue(routepdu);
															tracklogger.debug(systemId + " [" + temp_msg_id
																	+ "]: Enqueued To ProcessQueue");
															if (masterCost > 0 && cost > 0) { // for profit report
																GlobalQueue.profitReportQueue
																		.enqueue(new ProfitLogObject(temp_msg_id,
																				user_id, master_user_id,
																				routing.getNetworkId(), masterCost,
																				cost, true, user.isAdminDepend()));
															}
															// omqlogger.debug(
															// systemId + " RoutingQueue: " + routepdu.getHtiMsgId());
															out_counter++;
															routepdu = null;
														} else {
															response.setCommandStatus(Data.ESME_RINVBALANCE);
															serverResponse(response);
															session.stop();
														}
													} catch (Exception ex) {
														response.setCommandStatus(Data.ESME_RSYSERR);
														serverResponse(response);
													}
													// last_destination = desti;
												} else if (balance.getFlag().equalsIgnoreCase("MIN")) {
													// credit = false;
													response.setCommandStatus(Data.ESME_RINVBALANCE);
													serverResponse(response);
													logger.error("< " + systemId + " >" + "Insufficient Wallet Amount");
													session.stop();
												} else if (balance.getFlag().equalsIgnoreCase("no")) {
													int credits = 1;
													if (isDoubleCost) {
														logger.debug("Destination<" + desti + "> Double Credit Route <"
																+ routing.getSmsc() + "> ");
														credits = 2;
													}
													try {
														if (balance.deductCredit(credits)) {
															String temp_msg_id = GlobalVars.assignMessageId();
															tracklogger.debug(systemId + " [" + temp_msg_id + "]: "
																	+ request.getSequenceNumber());
															submitResponse.setMessageId(temp_msg_id);
															serverResponse(response);
															tracklogger.debug(systemId + " [" + temp_msg_id
																	+ "] Response: " + response.getSequenceNumber());
															System.out.println("< " + systemId + " <" + temp_msg_id
																	+ "> " + "Pdu Received <" + desti + ">----" + smsc);
															// ((SubmitSM) request).setRegisteredDelivery((byte) 1);
															if (routing.getExpiry() == null) {
																try {
																	((SubmitSM) request).setValidityPeriod(null); // either NULL or YYMMDDhhmmsstnnp
																} catch (WrongDateFormatException ex) {
																	// nothing to do
																}
															} else {
																if (!routing.getExpiry().equalsIgnoreCase("0")) { // set expiry for pdu
																	try {
																		((SubmitSM) request)
																				.setValidityPeriod(routing.getExpiry()); // either NULL or
																															// YYMMDDhhmmsstnnp
																	} catch (WrongDateFormatException ex) {
																		// nothing to do
																	}
																}
															}
															// registerdlr = ((SubmitSM) request).getRegisteredDelivery();
															try {
																if ((((SubmitSM) request).getDataCoding() == (byte) 8)
																		|| (((SubmitSM) request)
																				.getDataCoding() == (byte) 245)) {
																	request_log = new RequestLog(temp_msg_id,
																			((SubmitSM) request).getSequenceNumber(),
																			getHexDump(((SubmitSM) request)
																					.getShortMessage(
																							Data.ENC_UTF16_BE)),
																			((SubmitSM) request).getDestAddr()
																					.getAddress(),
																			((SubmitSM) request).getSourceAddr()
																					.getAddress(),
																			((SubmitSM) request)
																					.getRegisteredDelivery(),
																			((SubmitSM) request).getEsmClass(),
																			((SubmitSM) request).getDataCoding(),
																			systemId, remoteAddress);
																} else {
																	request_log = new RequestLog(temp_msg_id,
																			((SubmitSM) request).getSequenceNumber(),
																			getHexDump(((SubmitSM) request)
																					.getShortMessage()),
																			((SubmitSM) request).getDestAddr()
																					.getAddress(),
																			((SubmitSM) request).getSourceAddr()
																					.getAddress(),
																			((SubmitSM) request)
																					.getRegisteredDelivery(),
																			((SubmitSM) request).getEsmClass(),
																			((SubmitSM) request).getDataCoding(),
																			systemId, remoteAddress);
																}
																GlobalQueue.request_log_Queue.enqueue(request_log);
																tracklogger.debug(systemId + " [" + temp_msg_id
																		+ "]: Enqueued To RequestLog");
															} catch (Exception e) {
																logger.error(systemId, e);
															}
															routepdu = new RoutePDU(request, temp_msg_id,
																	((SubmitSM) request).getSequenceNumber(),
																	session.getSessionId(), user.getPriority());
															routepdu.setUsername(systemId);
															routepdu.setCost(0);
															routepdu.setNetworkId(routing.getNetworkId());
															routepdu.setSmsc(smsc);
															routepdu.setGroupId(routing.getGroupId());
															// routepdu.setSecondrySmsc(secondry_smsc);
															routepdu.setNumsmsc(routing.getNumsmsc());
															routepdu.setForceSIDNum(routing.getForceSIDNum());
															routepdu.setForceSIDAlpha(routing.getForceSIDAlpha());
															routepdu.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
																	.format(new java.sql.Timestamp(
																			System.currentTimeMillis())));
															routepdu.setHlr(is_route_hlr);
															routepdu.setBackupSmsc(routing.getBackupSmsc());
															routepdu.setRefund(routing.isRefund());
															routepdu.setOriginalSourceAddr(
																	((SubmitSM) request).getSourceAddr().getAddress());
															routepdu.setRegisterSenderId(routing.getRegisterSenderId());
															routepdu.setRegisterSmsc(routing.getRegisterSmsc());
															routepdu.setRegGroupId(routing.getRegGroupId());
															routepdu.setReplaceContent(routing.isReplaceContent());
															routepdu.setReplacement(routing.getReplacement());
															routepdu.setSmsLength(routing.getSmsLength());
															routepdu.setSourceAppender(routing.getSourceAppender());
															routepdu.setContentAppender(routing.getContentAppender());
															routepdu.setSenderReplacement(
																	routing.getSenderReplacement());
															routepdu.setMms(routing.isMms());
															routepdu.setMnp(routing.isMnp());
															if (desti.length() < GlobalVars.MIN_DESTINATION_LENGTH) {
																routepdu.setSmsc(GlobalVars.INVALID_DEST_SMSC);
																routepdu.setGroupId(0);
																logger.error(systemId + " Invalid Destination: " + desti
																		+ " Length: " + desti.length());
															} else {
																if (routing.getNumberLength() != null) {
																	if (routing.getNumberLength().contains(",")) {
																		try {
																			if (desti.length() < Integer
																					.parseInt(routing.getNumberLength()
																							.split(",")[0])
																					|| desti.length() > Integer
																							.parseInt(routing
																									.getNumberLength()
																									.split(",")[1])) {
																				routepdu.setSmsc(
																						GlobalVars.INVALID_DEST_SMSC);
																				routepdu.setGroupId(0);
																				logger.error(systemId + " Destination: "
																						+ desti + " Length: "
																						+ desti.length()
																						+ " Out Of Range: "
																						+ routing.getNumberLength());
																			}
																		} catch (Exception ex) {
																		}
																	} else {
																		try {
																			if (desti.length() != Integer.parseInt(
																					routing.getNumberLength())) {
																				routepdu.setSmsc(
																						GlobalVars.INVALID_DEST_SMSC);
																				routepdu.setGroupId(0);
																				logger.error(systemId + " Destination: "
																						+ desti + " Length: "
																						+ desti.length()
																						+ " Not Equal: "
																						+ routing.getNumberLength());
																			}
																		} catch (Exception ex) {
																		}
																	}
																}
															}
															processQueue.enqueue(routepdu);
															tracklogger.debug(systemId + " [" + temp_msg_id
																	+ "]: Enqueued To ProcessQueue");
															GlobalQueue.profitReportQueue
																	.enqueue(new ProfitLogObject(temp_msg_id, user_id,
																			master_user_id, routing.getNetworkId(), 0.0,
																			0.0, false, user.isAdminDepend()));
															out_counter++;
															routepdu = null;
														} else {
															response.setCommandStatus(Data.ESME_RINVBALANCE);
															serverResponse(response);
															session.stop();
														}
													} catch (EntryNotFoundException ex) {
														logger.error(
																"< " + systemId + " >" + " BalanceEntry Not Found ");
														response.setCommandStatus(Data.ESME_RSYSERR);
														serverResponse(response);
													} catch (Exception ex) {
														response.setCommandStatus(Data.ESME_RSYSERR);
														serverResponse(response);
													}
												}
											}
										}
									} else {
										response.setCommandStatus(Data.ESME_RINVDSTADR);
										serverResponse(response);
										logger.error(systemId + " Routing Error : "
												+ ((SubmitSM) request).getDestAddr().getAddress());
									}
								}
								if (session.keepReceiving) {
									session.increaseCloseTime();
									// ThreadSleep();
								}
							}
							break;
						case Data.SUBMIT_MULTI:
							logger.error(systemId + " : Multi Request Not Supported.");
							response.setCommandStatus(Data.ESME_RINVCMDID);
							serverResponse(response);
							break;
						case Data.DELIVER_SM:
							logger.error(systemId + " : Deliver Request Not Accepted.");
							response.setCommandStatus(Data.ESME_RINVCMDID);
							serverResponse(response);
							break;
						case Data.DATA_SM:
							logger.error(systemId + " : Data Request Not Supported.");
							response.setCommandStatus(Data.ESME_RINVCMDID);
							serverResponse(response);
							break;
						case Data.QUERY_SM:
							logger.error(systemId + " : Query Request Not Supported.");
							response.setCommandStatus(Data.ESME_RINVCMDID);
							serverResponse(response);
							break;
						case Data.CANCEL_SM:
							logger.error(systemId + " : Cancel Request Not Supported.");
							response.setCommandStatus(Data.ESME_RINVCMDID);
							serverResponse(response);
							break;
						case Data.REPLACE_SM:
							logger.error(systemId + " : Replace Request Not Supported.");
							response.setCommandStatus(Data.ESME_RINVCMDID);
							serverResponse(response);
							break;
						case Data.ENQUIRE_LINK:
							// logger.info("UserEntry: " + user);
							// logger.info("balance[1]: " + GlobalCache.BalanceEntries.get(user_id));
							// balance.deductCredit(1);
							// logger.info("balance[2]: " + GlobalCache.BalanceEntries.get(user_id));
							serverResponse(response);
							session.increaseCloseTime();
							break;
						case Data.UNBIND:
							// do nothing, just respond and after sending
							// the response stop the session
							logger.info(systemId + " <-- Unbind Request Received --> ");
							logger.debug(systemId + ": " + request.debugString());
							serverResponse(response);
							session.stop();
							break;
						default:
							logger.debug(systemId + " [Unsupported Request: " + request.getCommandId() + "]: "
									+ request.debugString());
						}
					}
				}
			}
		} catch (WrongLengthOfStringException e) {
			logger.error(systemId, e);
		} catch (Exception ex) {
			logger.error(systemId, ex);
		}
	}

	public void clientResponse(com.logica.smpp.pdu.Response response) {
	}

	public void serverRequest(com.logica.smpp.pdu.Request request) {
		session.sendPDU(request);
	}

	public boolean serverResponse(com.logica.smpp.pdu.Response response) {
		// logger.info(systemId + "-> serverResponse :" +
		// response.getSequenceNumber());
		if (!session.sendPDU(response)) {
			logger.error(systemId + ":(" + session.getMode() + "-" + session.getSessionId() + ") "
					+ " -> Unable to Send Response: " + response.debugString());
			return false;
		} /*
			 * else { if (bound) { if (user.isPduLog() && pdu_log_Queue != null) { pdu_log_Queue.enqueue("[" + session.getMode() + "-" + session.getSessionId() + "] " + new
			 * SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + ":" + response.debugString()); } else { if (response.getCommandId() == Data.ENQUIRE_LINK_RESP) { System.out.println(systemId
			 * + ":(" + session.getMode() + "-" + session.getSessionId() + ") " + response.debugString()); } } } }
			 */
		return true;
	}

	/**
	 * Returns the session this PDU processor works for.
	 *
	 * @return the session of this PDU processor
	 */
	public UserSession getSession() {
		return session;
	}

	/**
	 * Returns the system id of the client for whose is this PDU processor processing PDUs.
	 *
	 * @return system id of client
	 */
	public String getSystemId() {
		return systemId;
	}

	private RoutingDTO getRouting(String destination, String masterId) {
		RoutingDTO routing = null;
		int networkCode = 0;
		if (NetworkBsfm == null) {
			NetworkBsfm = new HashMap<Integer, Map<String, Boolean>>(GlobalCache.NetworkBsfm);
		}
		if (NetworkMap == null) {
			NetworkMap = new HashMap<String, Integer>(GlobalCache.PrefixMapping);
		}
		if (adminRouting == null) {
			adminRouting = GlobalVars.routeService.listRouting(masterId);
		}
		if (adminRouting != null) {
			int length = destination.length();
			for (int i = length; i >= 1; i--) {
				if (NetworkMap.containsKey(destination.substring(0, i))) {
					networkCode = NetworkMap.get(destination.substring(0, i));
					break;
				}
			}
			if (adminRouting.containsKey(networkCode)) {
				routing = adminRouting.get(networkCode);
			}
			if (routing == null) {
				routing = adminRouting.get(0);
			}
		}
		return routing;
	}

	private RoutingDTO getRouting(String destination) {
		RoutingDTO routing = null;
		int networkCode = 0;
		if (NetworkBsfm == null) {
			NetworkBsfm = new HashMap<Integer, Map<String, Boolean>>(GlobalCache.NetworkBsfm);
		}
		if (NetworkMap == null) {
			NetworkMap = new HashMap<String, Integer>(GlobalCache.PrefixMapping);
		}
		if (RoutingMap == null) {
			RoutingMap = GlobalVars.routeService.listRouting(systemId);
			if (RoutingMap != null && RoutingMap.containsKey(0)) {
				isValidRouting = true;
			} else {
				logger.error(systemId + "<-- Routing Error --> " + destination);
			}
		}
		if (isValidRouting) {
			if (!RoutingMap.isEmpty()) {
				int length = destination.length();
				for (int i = length; i >= 1; i--) {
					if (NetworkMap.containsKey(destination.substring(0, i))) {
						networkCode = NetworkMap.get(destination.substring(0, i));
						// logger.debug(destination + "[" + destination.substring(0, i) + "]" + " Network Code: "
						// + networkCode);
						break;
					}
				}
				if (RoutingMap.containsKey(networkCode)) {
					routing = RoutingMap.get(networkCode);
				}
				if (routing == null) {
					routing = RoutingMap.get(0);
				}
			}
		}
		// System.out.println(destination + " " + routing);
		return routing;
	}

	private RoutingDTO getMmsRouting(String destination) {
		RoutingDTO routing = null;
		int networkCode = 0;
		if (NetworkBsfm == null) {
			NetworkBsfm = new HashMap<Integer, Map<String, Boolean>>(GlobalCache.NetworkBsfm);
		}
		if (NetworkMap == null) {
			NetworkMap = new HashMap<String, Integer>(GlobalCache.PrefixMapping);
		}
		if (mmsRoutingMap == null) {
			mmsRoutingMap = GlobalVars.routeService.listMmsRouting(systemId);
			if (mmsRoutingMap != null && mmsRoutingMap.containsKey(0)) {
				isValidRouting = true;
			} else {
				logger.error(systemId + "<-- Routing Error --> " + destination);
			}
		}
		if (isValidRouting) {
			if (!mmsRoutingMap.isEmpty()) {
				int length = destination.length();
				for (int i = length; i >= 1; i--) {
					if (NetworkMap.containsKey(destination.substring(0, i))) {
						networkCode = NetworkMap.get(destination.substring(0, i));
						// logger.debug(destination + "[" + destination.substring(0, i) + "]" + " Network Code: "
						// + networkCode);
						break;
					}
				}
				if (mmsRoutingMap.containsKey(networkCode)) {
					routing = mmsRoutingMap.get(networkCode);
				}
				if (routing == null) {
					routing = mmsRoutingMap.get(0);
				}
				routing.setMms(true);
			}
		}
		// System.out.println(destination + " " + routing);
		return routing;
	}

	private RoutingDTO getMmsRouting(String destination, String masterId) {
		RoutingDTO routing = null;
		int networkCode = 0;
		if (NetworkBsfm == null) {
			NetworkBsfm = new HashMap<Integer, Map<String, Boolean>>(GlobalCache.NetworkBsfm);
		}
		if (NetworkMap == null) {
			NetworkMap = new HashMap<String, Integer>(GlobalCache.PrefixMapping);
		}
		if (mmsAdminRouting == null) {
			mmsAdminRouting = GlobalVars.routeService.listMmsRouting(masterId);
		}
		if (mmsAdminRouting != null) {
			int length = destination.length();
			for (int i = length; i >= 1; i--) {
				if (NetworkMap.containsKey(destination.substring(0, i))) {
					networkCode = NetworkMap.get(destination.substring(0, i));
					break;
				}
			}
			if (mmsAdminRouting.containsKey(networkCode)) {
				routing = mmsAdminRouting.get(networkCode);
			}
			if (routing == null) {
				routing = mmsAdminRouting.get(0);
			}
		}
		return routing;
	}

	/**
	 * Checks if the bind request contains valid system id and password. For this uses the table of users provided in the constructor of the <code>SimulatorPDUProcessor</code>. If the authentication
	 * fails, i.e. if either the user isn't found or the password is incorrect, the function returns proper status code.
	 *
	 * @param request
	 *            the bind request as received from the client
	 * @return status code of the authentication; ESME_ROK if authentication passed
	 */
	private int checkIdentity(BindRequest request) {
		String password = request.getPassword();
		remoteAddress = session.getConnection().getRemoteAddress();
		localPort = session.getConnection().getLocalPort();
		if (systemId == null || password == null) {
			logger.error(request.getSystemId() + " Invalid System_id Or Password <" + password + ">");
			return Data.ESME_RINVSYSID;
		}
		if (GlobalCache.SystemIdMapping.containsKey(systemId)) {
			user_id = GlobalCache.SystemIdMapping.get(systemId);
		} else {
			logger.error(request.getSystemId() + "[" + remoteAddress + "]" + " Invalid Account <" + password + ">");
			return Data.ESME_RINVSYSID;
		}
		if (GlobalCache.BlockedUser.contains(systemId)) {
			logger.info(" Blocked User Bind Request: " + request.getSystemId() + "[" + remoteAddress + "]");
			return Data.ESME_RBINDFAIL;
		}
		if (GlobalCache.LowBalanceUser.contains(systemId)) {
			logger.info(
					" Insufficient Balance User Bind Request: " + request.getSystemId() + "[" + remoteAddress + "]");
			return Data.ESME_RINVBALANCE;
		}
		logger.info(
				"Bind Request: <" + request.getSystemId() + "> ipAddress: " + session.getConnection().getRemoteAddress()
						+ ":" + session.getConnection().getLocalPort() + "(seq: " + request.getSequenceNumber() + ")");
		user = GlobalVars.userService.getUserEntry(user_id);
		if (user != null) {
			if (!user.getPassword().equals(password)) {
				logger.error(
						request.getSystemId() + "[" + remoteAddress + "]" + " Invalid Password <" + password + ">");
				return Data.ESME_RINVPASWD;
			}
			if (isExpired()) {
				logger.error(
						request.getSystemId() + "[" + remoteAddress + "]" + " < Account Expired > " + user.getExpiry());
				return Data.ESME_RINVEXPIRY;
			}
			if (user.getAccessIp() != null) {
				if (isValidAccess(user.getAccessIp(), remoteAddress)) {
					logger.info(request.getSystemId() + " Valid Bind IPAddress: " + remoteAddress);
				} else {
					logger.error(request.getSystemId() + " Invalid Bind IPAddress: " + remoteAddress + " Allowed: "
							+ user.getAccessIp());
					return Data.ESME_RBINDFAIL;
				}
			} else if (user.getAccessCountry() != null) {
				if (isValidCountry(user.getAccessCountry(), remoteAddress)) {
					logger.info(request.getSystemId() + " Valid Bind IPAddress: " + remoteAddress);
				} else {
					logger.error(request.getSystemId() + " Invalid Bind IPAddress: " + remoteAddress);
					return Data.ESME_RBINDFAIL;
				}
			} else {
				logger.info(request.getSystemId() + "[" + remoteAddress + "]" + " Bind IPAddress Not Configured.");
			}
			if (user.isAdminDepend()) {
				if (GlobalCache.BlockedUser.contains(user.getMasterId())) {
					logger.info(request.getSystemId() + "[" + remoteAddress + "]" + ": Master Account("
							+ user.getMasterId() + ") is Blocked <404> ");
					return Data.ESME_RBINDFAIL;
				}
			}
			// ------------- Session Check ----------
			userSession = GlobalVars.userService.getSessionManager(systemId);
			if (userSession.isSessionUnderConstraint()) {
				// String balanceUser = systemId;
				if (GlobalCache.SystemIdMapping.containsKey(user.getMasterId())) {
					master_user_id = GlobalCache.SystemIdMapping.get(user.getMasterId());
					try {
						adminBalance = GlobalVars.userService.getUserBalance(master_user_id);
						if (adminBalance == null) {
							return Data.ESME_RSYSERR;
						}
					} catch (EntryNotFoundException ne) {
						logger.error(user.getMasterId() + "[" + remoteAddress + "]" + " BalanceEntry Not Found");
						return Data.ESME_RSYSERR;
					}
				} else {
					logger.error(request.getSystemId() + "[" + remoteAddress + "]" + " Invalid Master Account <"
							+ user.getMasterId() + ">");
					return Data.ESME_RINVSYSID;
				}
				try {
					balance = GlobalVars.userService.getUserBalance(user_id);
				} catch (EntryNotFoundException ne) {
					logger.error(user.getSystemId() + " BalanceEntry Not Found");
					return Data.ESME_RSYSERR;
				}
				if (balance != null) {
					if (user.isAdminDepend()) {
						try {
							if (adminBalance.getFlag().equalsIgnoreCase("no")) {
								long credit = adminBalance.getCredit();
								if (credit <= 0) {
									logger.error(systemId + "[" + remoteAddress + "]" + " Insufficient Admin Credits <"
											+ user.getMasterId() + ">: " + credit);
									if (!GlobalCache.LowBalanceUser.contains(user.getMasterId())) {
										GlobalCache.LowBalanceUser.add(user.getMasterId());
									}
									return Data.ESME_RINVBALANCE;
								}
							} else {
								double wallet = adminBalance.getAmount();
								if (adminBalance.getFlag().equalsIgnoreCase("MIN")) {
									logger.error(systemId + "[" + remoteAddress + "]" + " Insufficient Admin Balance <"
											+ user.getMasterId() + ">: " + wallet);
									if (!GlobalCache.LowBalanceUser.contains(user.getMasterId())) {
										GlobalCache.LowBalanceUser.add(user.getMasterId());
									}
									return Data.ESME_RINVBALANCE;
								}
							}
						} catch (EntryNotFoundException e) {
							logger.error(user.getMasterId() + " BalanceEntry Not Found");
							return Data.ESME_RSYSERR;
						}
					}
					try {
						if (balance.getFlag().equalsIgnoreCase("no")) {
							long credit = balance.getCredit();
							logger.info("< " + systemId + " >" + " Credits: " + credit);
							if (credit <= 0) {
								logger.error(systemId + "[" + remoteAddress + "]" + " Insufficient Credits: " + credit);
								return Data.ESME_RINVBALANCE;
							}
						} else {
							double wallet = balance.getAmount();
							logger.info("< " + systemId + " >" + " Wallet: " + wallet);
							if (balance.getFlag().equalsIgnoreCase("MIN") || wallet <= 0.5) {
								logger.error(systemId + "[" + remoteAddress + "]" + " Insufficient Balance: " + wallet);
								return Data.ESME_RINVBALANCE;
							}
						}
					} catch (EntryNotFoundException e) {
						logger.error("< " + systemId + " >" + " BalanceEntry Not Found ");
						return Data.ESME_RSYSERR;
					}
				} else {
					return Data.ESME_RSYSERR;
				}
			} else {
				return Data.ESME_RBINDFAIL;
			}
		} else {
			return Data.ESME_RINVSYSID;
		}
		return Data.ESME_ROK;
	}

	private boolean isExpired() {
		boolean toReturn = false;
		if (user.getExpiry() != null) {
			// logger.info(user.getSystemId() + " Checking Account Expiry: " + user.getExpiry());
			try {
				toReturn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(user.getExpiry() + " 23:59:59")
						.before(new java.util.Date());
			} catch (ParseException ex) {
				logger.info(systemId + " Expiry Parse Error " + user.getExpiry());
			}
		} else {
			logger.warn(systemId + " Invalid Expiry:  " + user.getExpiry());
		}
		// logger.info(user.getSystemId() + " Account Expired: " + toReturn);
		return toReturn;
	}

	@Override
	public void stopThread() {
		if (bound) {
			logger.info(systemId + "[" + remoteAddress + "]" + ":[" + session.getMode() + "-" + session.getSessionId()
					+ "] PDUProcessor Stopping.");
			bound = false;
			userSession.remove(session);
			if (userSession.getSessionCount() == 0) {
				if (routingThread != null) {
					routingThread.stop();
				}
				if (GlobalCache.UserRxObject.containsKey(systemId)) {
					((UserDeliverForward) GlobalCache.UserRxObject.get(systemId)).stop();
				}
				if (user.isBindAlert()) {
					GlobalCache.UserDisconnectionAlert.put(systemId,
							System.currentTimeMillis() + (user.getAlertWaitDuration() * 1000));
				}
				userSession.reset();
				GlobalVars.userService.updateSession(
						new SessionEntry(systemId, remoteAddress, GlobalVars.SERVER_ID, 0, 0, 0, 0, localPort));
			} else {
				GlobalVars.userService.updateSession(new SessionEntry(systemId, remoteAddress, GlobalVars.SERVER_ID,
						userSession.getSessionCount(), userSession.getRxCount(), userSession.getTxCount(),
						userSession.getTRxCount(), localPort));
			}
			if (!session.getMode().equalsIgnoreCase("reciever")) {
				logger.info(systemId + "[" + remoteAddress + "]" + "[" + session.getSessionId() + "]" + " Received : "
						+ in_counter + " Queued: " + out_counter);
			}
			logger.info(systemId + "[" + remoteAddress + "]" + ":[" + session.getMode() + "-" + session.getSessionId()
					+ "] PDUProcessor Stopped.");
		}
		RoutingMap = null;
		mmsRoutingMap = null;
	}

	public void setStringArray(StringTokenizer stringToken) {
		int i = 0;
		str = new String[stringToken.countTokens()];
		while (stringToken.hasMoreElements()) {
			str[i] = stringToken.nextToken();
			i++;
		}
	}

	@Override
	public void reloadRouting() {
		RoutingMap = null;
		mmsRoutingMap = null;
	}

	@Override
	public void removeAdminRouting() {
		adminRouting = null;
		mmsAdminRouting = null;
	}

	@Override
	public void reloadNetwork() {
		NetworkMap = null;
	}

	public void reloadNetworkBsfm() {
		NetworkBsfm = null;
	}

	private String getHexDump(String getString) throws UnsupportedEncodingException {
		String dump = "";
		byte[] buffer = getString.getBytes(Data.ENC_UTF16_BE);
		for (int i = 0; i < buffer.length; i++) {
			dump += Character.forDigit((buffer[i] >> 4) & 0x0f, 16);
			dump += Character.forDigit(buffer[i] & 0x0f, 16);
		}
		buffer = null;
		return dump;
	}

	private int[] getPartDescription(Request request) {
		int parts[] = new int[2];
		try {
			parts[0] = ((SubmitSM) request).getSarTotalSegments();
			parts[1] = ((SubmitSM) request).getSarSegmentSeqnum();
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
						parts[0] = 0;
					}
					try {
						parts[1] = Integer.parseInt(hex_dump.substring(10, 12));
					} catch (Exception ex) {
						parts[1] = -1;
					}
				} else if (header_length == 6) {
					try {
						parts[0] = Integer.parseInt(hex_dump.substring(10, 12));
					} catch (Exception ex) {
						parts[0] = 0;
					}
					try {
						parts[1] = Integer.parseInt(hex_dump.substring(12, 14));
					} catch (Exception ex) {
						parts[1] = -1;
					}
				} else {
					System.out.println(systemId + "Unknown Header Found:" + hex_dump.substring(0, 14));
					parts[0] = 0;
					parts[1] = -1;
				}
			} catch (Exception une) {
				parts[0] = 0;
				parts[1] = -1;
			}
			if (parts[1] == 0) {
				parts[1] = -1;
			}
			/*
			 * if (parts[0] == 0 || parts[1] == -1) { logger.error(systemId + " PDU Part Description Error: " + "[" + hex_dump.substring(0, 14) + "]" + " Total: " + parts[0] + " Part_Number:" +
			 * parts[1]); }
			 */
		}
		return parts;
	}

	private boolean isValidAccess(String allowedlist, String ipaddress) {
		try {
			if (ipaddress.equalsIgnoreCase("0:0:0:0:0:0:0:1") || ipaddress.equalsIgnoreCase("127.0.0.1")) {
				return true;
			} else {
				for (String allowedip : allowedlist.split(",")) {
					if (allowedip.indexOf("/") > 0) {
						if (isInRange(allowedip, ipaddress)) {
							return true;
						}
					} else {
						if (ipaddress.equalsIgnoreCase(allowedip)) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(systemId + " " + ipaddress, e.fillInStackTrace());
		}
		return false;
	}

	private boolean isValidCountry(String allowedlist, String ipaddress) {
		if (ipaddress.equalsIgnoreCase("0:0:0:0:0:0:0:1") || ipaddress.equalsIgnoreCase("127.0.0.1")) {
			return true;
		} else {
			String country = GlobalVars.userService.getCountryname(ipaddress);
			if (country != null) {
				logger.info(systemId + " Access Country Found: " + country);
				for (String allowedCountry : allowedlist.split(",")) {
					if (allowedCountry.equalsIgnoreCase(country)) {
						return true;
					}
				}
			} else {
				logger.info(systemId + " Access Country Not Found.");
			}
		}
		return false;
	}

	private boolean isInRange(String range, String requestip) {
		boolean inRange = false;
		String[] parts = range.split("/");
		String ip = parts[0];
		int prefix;
		if (parts.length < 2) {
			prefix = 0;
		} else {
			prefix = Integer.parseInt(parts[1]);
		}
		Inet4Address a = null;
		Inet4Address a1 = null;
		try {
			a = (Inet4Address) InetAddress.getByName(ip);
			a1 = (Inet4Address) InetAddress.getByName(requestip);
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException : " + e);
		}
		byte[] b = a.getAddress();
		int ipInt = ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | ((b[3] & 0xFF) << 0);
		byte[] b1 = a1.getAddress();
		int ipInt1 = ((b1[0] & 0xFF) << 24) | ((b1[1] & 0xFF) << 16) | ((b1[2] & 0xFF) << 8) | ((b1[3] & 0xFF) << 0);
		int mask = ~((1 << (32 - prefix)) - 1);
		if ((ipInt & mask) == (ipInt1 & mask)) {
			inRange = true;
		}
		return inRange;
	}

	@Override
	public void setUserEntry(UserEntry entry) {
		this.user = entry;
	}
}