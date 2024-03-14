package com.hti.smpp.common.httpclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.dto.LoginDTO;
import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.messages.dto.QueueBackupExt;
import com.hti.smpp.common.schedule.dto.ScheduleEntry;
import com.hti.smpp.common.schedule.repository.ScheduleEntryRepository;
import com.hti.smpp.common.service.RouteDAService;
import com.hti.smpp.common.service.SendSmsService;
import com.hti.smpp.common.service.impl.SmsServiceImpl;
import com.hti.smpp.common.session.UserSession;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.util.Body;
import com.hti.smpp.common.util.Converter;
import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.GlobalVarsSms;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.TLVOctets;
import com.hti.smpp.common.util.Validation;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.TimeoutException;
import com.logica.smpp.WrongSessionStateException;
import com.logica.smpp.pdu.PDUException;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.SubmitSMResp;
import com.logica.smpp.util.ByteBuffer;

@Service
public class Client {

	@Autowired
	private SmsServiceImpl smsServiceImpl;

	private int commandid = 0;

	private Session session = null;

	@Autowired
	private RouteDAService routeService;

	@Autowired
	private ScheduleEntryRepository scheduleEntryRepository;

	private Logger logger = LoggerFactory.getLogger("sessionLogger");

	private static Map<String, String> hashTabOne = new HashMap<String, String>();
	private Random rand = new Random();
	static {
		hashTabOne.put("A", "41");
		hashTabOne.put("B", "42");
		hashTabOne.put("C", "43");
		hashTabOne.put("D", "44");
		hashTabOne.put("E", "45");
		hashTabOne.put("F", "46");
		hashTabOne.put("G", "47");
		hashTabOne.put("H", "48");
		hashTabOne.put("I", "49");
		hashTabOne.put("J", "4A");
		hashTabOne.put("K", "4B");
		hashTabOne.put("L", "4C");
		hashTabOne.put("M", "4D");
		hashTabOne.put("N", "4E");
		hashTabOne.put("O", "4F");
		hashTabOne.put("P", "50");
		hashTabOne.put("Q", "51");
		hashTabOne.put("R", "52");
		hashTabOne.put("S", "53");
		hashTabOne.put("T", "54");
		hashTabOne.put("U", "55");
		hashTabOne.put("V", "56");
		hashTabOne.put("W", "57");
		hashTabOne.put("X", "58");
		hashTabOne.put("Y", "59");
		hashTabOne.put("Z", "5A");
		hashTabOne.put("a", "61");
		hashTabOne.put("b", "62");
		hashTabOne.put("c", "63");
		hashTabOne.put("d", "64");
		hashTabOne.put("e", "65");
		hashTabOne.put("f", "66");
		hashTabOne.put("g", "67");
		hashTabOne.put("h", "68");
		hashTabOne.put("i", "69");
		hashTabOne.put("j", "6A");
		hashTabOne.put("k", "6B");
		hashTabOne.put("l", "6C");
		hashTabOne.put("m", "6D");
		hashTabOne.put("n", "6E");
		hashTabOne.put("o", "6F");
		hashTabOne.put("p", "70");
		hashTabOne.put("q", "71");
		hashTabOne.put("r", "72");
		hashTabOne.put("s", "73");
		hashTabOne.put("t", "74");
		hashTabOne.put("u", "75");
		hashTabOne.put("v", "76");
		hashTabOne.put("w", "77");
		hashTabOne.put("x", "78");
		hashTabOne.put("y", "79");
		hashTabOne.put("z", "7A");
		hashTabOne.put("0", "30");
		hashTabOne.put("1", "31");
		hashTabOne.put("2", "32");
		hashTabOne.put("3", "33");
		hashTabOne.put("4", "34");
		hashTabOne.put("5", "35");
		hashTabOne.put("6", "36");
		hashTabOne.put("7", "37");
		hashTabOne.put("8", "38");
		hashTabOne.put("9", "39");
		hashTabOne.put("~", "1B3D"); // hashTabOne.put("\\", "1B2E");
		hashTabOne.put("|", "1B40");
		hashTabOne.put("]", "1B3E");
		hashTabOne.put("[", "1B3C");
		hashTabOne.put("}", "1B29");
		hashTabOne.put("{", "1B28");
		hashTabOne.put("^", "1B14");
		hashTabOne.put("@", "00");
		hashTabOne.put("$", "02");
		hashTabOne.put("_", "11");
		hashTabOne.put("!", "21");
		hashTabOne.put("\"", "22");
		hashTabOne.put("#", "23");
		hashTabOne.put("%", "25");
		hashTabOne.put("&", "26");
		hashTabOne.put("'", "27");
		hashTabOne.put("(", "28");
		hashTabOne.put(")", "29");
		hashTabOne.put("\n", "0A");
		hashTabOne.put("*", "2A");
		hashTabOne.put("+", "2B");
		hashTabOne.put(",", "2C");
		hashTabOne.put("-", "2D");
		hashTabOne.put(".", "2E");
		hashTabOne.put("/", "2F");
		hashTabOne.put(":", "3A");
		hashTabOne.put(";", "3B");
		hashTabOne.put("<", "3C");
		hashTabOne.put("=", "3D");
		hashTabOne.put(">", "3E");
		hashTabOne.put("?", "3F");
	}

	public List<String> submitMmsRequest(ApiRequestDTO bulkSmsDTO) {
		List<String> response_list = new ArrayList<String>();
		String messageType = bulkSmsDTO.getMessageType();
		String message = bulkSmsDTO.getText();
		List<String> numberlist = bulkSmsDTO.getReceipients();
		String user = bulkSmsDTO.getUsername();
		String pwd = bulkSmsDTO.getPassword();
		String sender = bulkSmsDTO.getSender();
		int ston = bulkSmsDTO.getSourceTon();
		int snpi = bulkSmsDTO.getSourceNpi();
		int count = 0;
		String destination_no;
		UserSession userSession = null;
		try {
			userSession = smsServiceImpl.getUserSession(user, pwd);
			commandid = userSession.getCommandStatus();
			if (commandid != Data.ESME_ROK) {
				if (commandid == Data.ESME_RINVSYSID) {
					response_list.add(ResponseCode.INVALID_LOGIN + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
							+ ") Connection Error. < Invalid System ID >");
				} else if (commandid == Data.ESME_RINVPASWD) {
					response_list.add(ResponseCode.INVALID_LOGIN + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
							+ ") Connection Error. < Invalid System ID/Password >");
				} else if (commandid == 1035) {
					response_list.add(ResponseCode.INSUF_BALANCE + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
							+ ") Connection Error. < Insufficient Balance >");
				} else if (commandid == Data.ESME_RINVEXPIRY) {
					response_list.add(ResponseCode.ACCOUNT_EXPIRED + ",0,0");
					logger.error(
							user + " (" + bulkSmsDTO.getRequestFormat() + ") Connection Error < Account Expired >");
				} else {
					response_list.add(ResponseCode.SYSTEM_ERROR + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Connection Error. Commandid: "
							+ commandid);
				}
			} else {
				session = userSession.getSession();
				while (!numberlist.isEmpty()) {
					// boolean exit = false;
					int CommandStatus = Data.ESME_ROK;
					boolean stopProcess = false;
					destination_no = (String) numberlist.remove(0);
					SubmitSM msg = null;
					msg = new SubmitSM();
					try {
						if (messageType.equalsIgnoreCase("SpecialChar")) {
							msg.setShortMessage(message, "ISO8859_1");
							msg.setDataCoding((byte) 0);
							msg.setEsmClass((byte) 0);
						} else if (messageType.equalsIgnoreCase("Unicode")) {
							System.out.println("************** Unicode Message*******************");
							msg.setShortMessage(message, Data.ENC_UTF16_BE);
							msg.setEsmClass((byte) 0);
							msg.setDataCoding((byte) 8);
						}
						msg.setRegisteredDelivery((byte) 1);
						msg.setSourceAddr((byte) ston, (byte) snpi, sender);
						msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
						msg.setExtraOptional((short) 0x1500,
								new ByteBuffer(("mms-" + bulkSmsDTO.getMmsType()).getBytes()));
						if (bulkSmsDTO.getCaption() != null) {
							msg.setExtraOptional(
									new TLVOctets((short) 0x1501, new ByteBuffer(bulkSmsDTO.getCaption().getBytes())));
						}
						if (bulkSmsDTO.getPeId() != null) {
							System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
									+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
							msg.setExtraOptional((short) 0x1400, new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
						}
						if (bulkSmsDTO.getTemplateId() != null) {
							msg.setExtraOptional((short) 0x1401, new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
						}
						if (bulkSmsDTO.getTelemarketerId() != null) {
							msg.setExtraOptional((short) 0x1402,
									new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
						}
						SubmitSMResp submitResponse = session.submit(msg);
						if (submitResponse != null) {
							count++;
							if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
								response_list.add(ResponseCode.NO_ERROR + "," + submitResponse.getMessageId() + ","
										+ destination_no);
								System.out.println(user + "(" + bulkSmsDTO.getRequestFormat() + ") Message submitted < "
										+ destination_no + " > ");
							} else {
								if (submitResponse.getCommandStatus() == 1035) {
									logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
											+ ") Insufficient balance -> ");
									response_list.add(ResponseCode.INSUF_BALANCE + ",0" + "," + destination_no);
									stopProcess = true;
									CommandStatus = submitResponse.getCommandStatus();
									break;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
									logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
											+ ") Invalid Message Length -> ");
									response_list.add(ResponseCode.INVALID_TEXT + ",0" + "," + destination_no);
									stopProcess = true;
									break;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
									logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
											+ ") Invalid Destination Address <" + destination_no + ">");
									response_list.add(ResponseCode.INVALID_DEST_ADDR + ",0" + "," + destination_no);
									break;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
									logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
											+ ") Invalid Source Address <" + sender + ">");
									response_list.add(ResponseCode.INVALID_SENDER + ",0" + "," + destination_no);
									stopProcess = true;
									break;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RSYSERR) {
									logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
											+ ") Submit System Error -> ");
									response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
									// exit = true;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
									logger.error(
											user + " <-(" + bulkSmsDTO.getRequestFormat() + ") Account Expired -> ");
									response_list.add(ResponseCode.ACCOUNT_EXPIRED + ",0" + "," + destination_no);
									CommandStatus = submitResponse.getCommandStatus();
									stopProcess = true;
									break;
								} else {
									response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
									logger.error(user + " (" + bulkSmsDTO.getRequestFormat() + ") Submit failed <"
											+ submitResponse.getCommandStatus() + ":" + destination_no + " >");
								}
							}
						} else {
							response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
							logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
									+ ") No Response On Submit <" + destination_no + ">");
							CommandStatus = -1;
							stopProcess = true;
							break;
						}
					} catch (TimeoutException | PDUException | WrongSessionStateException e) {
						response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
						logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Submit Error: " + e);
					} catch (Exception ioe) {
						CommandStatus = -1;
						response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
						logger.error(
								"<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Connection Error: " + ioe);
						stopProcess = true;
						break;
					}
					if (CommandStatus != Data.ESME_ROK) {
						userSession.setCommandStatus(CommandStatus);
					}
					if (stopProcess) {
						break;
					}
					if (count >= IConstants.HTTPsmsCount) {
						try {
							Thread.sleep(IConstants.HTTPSleepTime);
						} catch (InterruptedException ie) {
						}
						count = 0;
					}
				}
			}
		} catch (Exception e) {
			response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + ",0");
			logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") System Error: " + e);
		}
		smsServiceImpl.putUserSession(userSession);
		return response_list;
	}

	public List<String> submitRequest(ApiRequestDTO bulkSmsDTO) {
		List<String> response_list = new ArrayList<String>();
		int no_of_msg = 0;
		String messageType = bulkSmsDTO.getMessageType();
		String message = bulkSmsDTO.getText();
		int msg_length = message.length();
		List<String> numberlist = bulkSmsDTO.getReceipients();
		// int processed_number = numberlist.size();
		// String summary_message = bulkSmsDTO.getText();
		if (messageType.compareToIgnoreCase("Arabic") == 0) {
			message = new Converters().UTF16(message);
			// summary_message = new Converters().UTF16(summary_message);
			// System.out.println("Message: " + message);
			messageType = "Unicode";
			msg_length = message.length();
			if (msg_length > 280) {
				int rem = msg_length % 280;
				no_of_msg = msg_length / 280;
				if (rem > 0) {
					no_of_msg = no_of_msg + 1;
				}
			} else {
				no_of_msg = 1;
			}
		} else if (messageType.compareToIgnoreCase("Unicode") == 0) {
			if (msg_length > 280) {
				int rem = msg_length % 280;
				no_of_msg = msg_length / 280;
				if (rem > 0) {
					no_of_msg = no_of_msg + 1;
				}
			} else {
				no_of_msg = 1;
			}
		} else if (messageType.compareToIgnoreCase("SpecialChar") == 0) {
			// summary_message = new Converters().UTF16(summary_message);
			if (msg_length > 160) {
				int rem = msg_length % 153;
				int qot = msg_length / 153;
				if (rem > 0) {
					no_of_msg = qot + 1;
				} else {
					no_of_msg = qot;
				}
			} else {
				no_of_msg = 1;
			}
		}
		// System.out.println("no_of_msg -> " + no_of_msg);
		String user = bulkSmsDTO.getUsername();
		String pwd = bulkSmsDTO.getPassword();
		String sender = bulkSmsDTO.getSender();
		int ston = bulkSmsDTO.getSourceTon();
		int snpi = bulkSmsDTO.getSourceNpi();
		int count = 0;
		String destination_no;
		String temp_msg = "";
		// commandid = connection(user, pwd);
		// SessionHandler sessionHandler = null;
		UserSession userSession = null;
		try {
			userSession = smsServiceImpl.getUserSession(user, pwd);
			commandid = userSession.getCommandStatus();
			if (commandid != Data.ESME_ROK) {
				if (commandid == Data.ESME_RINVSYSID) {
					response_list.add(ResponseCode.INVALID_LOGIN + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
							+ ") Connection Error. < Invalid System ID >");
				} else if (commandid == Data.ESME_RINVPASWD) {
					response_list.add(ResponseCode.INVALID_LOGIN + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
							+ ") Connection Error. < Invalid System ID/Password >");
				} else if (commandid == 1035) {
					response_list.add(ResponseCode.INSUF_BALANCE + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
							+ ") Connection Error. < Insufficient Balance >");
				} else if (commandid == Data.ESME_RINVEXPIRY) {
					response_list.add(ResponseCode.ACCOUNT_EXPIRED + ",0,0");
					logger.error(
							user + " (" + bulkSmsDTO.getRequestFormat() + ") Connection Error < Account Expired >");
				} else {
					response_list.add(ResponseCode.SYSTEM_ERROR + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Connection Error. Commandid: "
							+ commandid);
				}
			} else {
				session = userSession.getSession();
				if (messageType.equalsIgnoreCase("SpecialChar")) {
					message = getHexValue(message);
					message = getContent(message.toCharArray());
				}
				while (!numberlist.isEmpty()) {
					// boolean exit = false;
					int CommandStatus = Data.ESME_ROK;
					boolean stopProcess = false;
					destination_no = (String) numberlist.remove(0);
					SubmitSM msg = null;
					if (messageType.equalsIgnoreCase("Unicode") && no_of_msg > 1) {
						List<String> parts = Body.getUnicodeno(message);
						int counts = parts.size();
						int i = 1;
						int reference_number = rand.nextInt((255 - 10) + 1) + 10;
						while (!parts.isEmpty()) {
							String part = parts.remove(0);
							part = Converter.getUnicode(part.toCharArray());
							msg = new SubmitSM();
							// System.out.println("Part Number :=========> "+i);
							try {
								ByteBuffer byteMessage = new ByteBuffer();
								byteMessage.appendByte((byte) 0x05);
								byteMessage.appendByte((byte) 0x00);
								byteMessage.appendByte((byte) 0x03);
								byteMessage.appendByte((byte) reference_number);
								byteMessage.appendByte((byte) counts);
								byteMessage.appendByte((byte) i);
								byteMessage.appendString(part, Data.ENC_UTF16_BE);
								msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
								msg.setSourceAddr((byte) ston, (byte) snpi, sender);
								msg.setShortMessage(byteMessage);
								msg.setEsmClass((byte) 0x40);
								msg.setDataCoding((byte) 8);
								msg.setRegisteredDelivery((byte) 1);
								if (bulkSmsDTO.getPeId() != null) {
									System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
											+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
									msg.setExtraOptional((short) 0x1400,
											new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
								}
								if (bulkSmsDTO.getTemplateId() != null) {
									msg.setExtraOptional((short) 0x1401,
											new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
								}
								if (bulkSmsDTO.getTelemarketerId() != null) {
									msg.setExtraOptional((short) 0x1402,
											new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
								}
								/*
								 * msg.setSarMsgRefNum((short) 100); msg.setSarSegmentSeqnum((short) i);
								 * msg.setSarTotalSegments((short) counts);
								 */
								SubmitSMResp submitResponse = session.submit(msg);
								if (submitResponse != null) {
									count++;
									if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
										response_list.add(ResponseCode.NO_ERROR + "," + submitResponse.getMessageId()
												+ "," + destination_no);
										System.out.println(user + "(" + bulkSmsDTO.getRequestFormat()
												+ ") Message submitted < " + destination_no + " >");
									} else {
										if (submitResponse.getCommandStatus() == 1035) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Insufficient balance -> ");
											response_list.add(ResponseCode.INSUF_BALANCE + ",0" + "," + destination_no);
											stopProcess = true;
											CommandStatus = submitResponse.getCommandStatus();
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Message Length -> ");
											response_list.add(ResponseCode.INVALID_TEXT + ",0" + "," + destination_no);
											stopProcess = true;
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Destination Address <" + destination_no + ">");
											response_list
													.add(ResponseCode.INVALID_DEST_ADDR + ",0" + "," + destination_no);
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Source Address <" + sender + ">");
											response_list
													.add(ResponseCode.INVALID_SENDER + ",0" + "," + destination_no);
											stopProcess = true;
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RSYSERR) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Submit System Error -> ");
											response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
											// exit = true;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
											logger.error(user + " <-(" + bulkSmsDTO.getRequestFormat()
													+ ") Account Expired -> ");
											response_list
													.add(ResponseCode.ACCOUNT_EXPIRED + ",0" + "," + destination_no);
											CommandStatus = submitResponse.getCommandStatus();
											stopProcess = true;
											break;
										} else {
											response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Submit failed <" + submitResponse.getCommandStatus() + ":"
													+ destination_no + " >");
										}
									}
								} else {
									response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
									logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
											+ ") No Response On Submit <" + destination_no + ">");
									CommandStatus = -1;
									stopProcess = true;
									break;
								}
							} catch (TimeoutException | PDUException | WrongSessionStateException e) {
								response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
								logger.error(
										"<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Submit Error: " + e);
							} catch (Exception ioe) {
								CommandStatus = -1;
								response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
								logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
										+ ") Connection Error: " + ioe);
								stopProcess = true;
								break;
							}
							i++;
						}
					} else if (messageType.equalsIgnoreCase("SpecialChar") && no_of_msg > 1) {
						List parts = Body.getEnglishno(message);
						// StringTokenizer stkmsg = new StringTokenizer(parts,
						// "##");
						int counts = parts.size();
						int i = 1;
						int reference_number = rand.nextInt((255 - 10) + 1) + 10;
						while (!parts.isEmpty()) {
							String engmsg = (String) parts.remove(0);
							// int rn = 100;
							int length1 = 0;
							length1 = (engmsg.length() + 6);
							ByteBuffer bf = new ByteBuffer();
							bf.appendBuffer(Body.getHead());
							bf.appendByte((byte) length1);
							bf.appendBuffer(Body.getHeader(reference_number, counts, i));
							bf.appendString(engmsg);
							msg = new SubmitSM();
							try {
								msg.setBody(bf);
								msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
								msg.setSourceAddr((byte) ston, (byte) snpi, sender);
								msg.setDataCoding((byte) 0);
								msg.setEsmClass((byte) 0x40);
								msg.setRegisteredDelivery((byte) 1);
								if (bulkSmsDTO.getPeId() != null) {
									System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
											+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
									msg.setExtraOptional((short) 0x1400,
											new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
								}
								if (bulkSmsDTO.getTemplateId() != null) {
									msg.setExtraOptional((short) 0x1401,
											new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
								}
								if (bulkSmsDTO.getTelemarketerId() != null) {
									msg.setExtraOptional((short) 0x1402,
											new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
								}
								/*
								 * msg.setSarMsgRefNum((short) rn); msg.setSarSegmentSeqnum((short) i);
								 * msg.setSarTotalSegments((short) counts);
								 */
								SubmitSMResp submitResponse = session.submit(msg);
								if (submitResponse != null) {
									count++;
									if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
										response_list.add(ResponseCode.NO_ERROR + "," + submitResponse.getMessageId()
												+ "," + destination_no);
										System.out.println(user + "(" + bulkSmsDTO.getRequestFormat()
												+ ") Message submitted < " + destination_no + " >");
									} else {
										if (submitResponse.getCommandStatus() == 1035) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Insufficient balance -> ");
											response_list.add(ResponseCode.INSUF_BALANCE + ",0" + "," + destination_no);
											stopProcess = true;
											CommandStatus = submitResponse.getCommandStatus();
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Message Length -> ");
											response_list.add(ResponseCode.INVALID_TEXT + ",0" + "," + destination_no);
											stopProcess = true;
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Destination Address <" + destination_no + ">");
											response_list
													.add(ResponseCode.INVALID_DEST_ADDR + ",0" + "," + destination_no);
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Source Address <" + sender + ">");
											response_list
													.add(ResponseCode.INVALID_SENDER + ",0" + "," + destination_no);
											stopProcess = true;
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RSYSERR) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Submit System Error -> ");
											response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
											// exit = true;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
											logger.error(user + " <-(" + bulkSmsDTO.getRequestFormat()
													+ ") Account Expired -> ");
											response_list
													.add(ResponseCode.ACCOUNT_EXPIRED + ",0" + "," + destination_no);
											CommandStatus = submitResponse.getCommandStatus();
											stopProcess = true;
											break;
										} else {
											response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Submit failed <" + submitResponse.getCommandStatus() + ":"
													+ destination_no + " >");
										}
									}
								} else {
									response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
									logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
											+ ") No Response On Submit <" + destination_no + ">");
									CommandStatus = -1;
									stopProcess = true;
									break;
								}
							} catch (TimeoutException | PDUException | WrongSessionStateException e) {
								response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
								logger.error(
										"<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Submit Error: " + e);
							} catch (Exception ioe) {
								CommandStatus = -1;
								response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
								logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
										+ ") Connection Error: " + ioe);
								stopProcess = true;
								break;
							}
							i++;
						}
					} else {
						msg = new SubmitSM();
						try {
							if (messageType.equalsIgnoreCase("SpecialChar")) {
								msg.setShortMessage(message, "ISO8859_1");
								msg.setDataCoding((byte) 0);
								msg.setEsmClass((byte) 0);
							} else if (messageType.equalsIgnoreCase("Unicode")) {
								temp_msg = Converter.getUnicode(message.toCharArray());
								System.out.println("************** Unicode Message*******************");
								msg.setShortMessage(temp_msg, Data.ENC_UTF16_BE);
								msg.setEsmClass((byte) 0);
								msg.setDataCoding((byte) 8);
							}
							msg.setRegisteredDelivery((byte) 1);
							msg.setSourceAddr((byte) ston, (byte) snpi, sender);
							msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
							if (bulkSmsDTO.getPeId() != null) {
								System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
										+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
								msg.setExtraOptional((short) 0x1400, new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
							}
							if (bulkSmsDTO.getTemplateId() != null) {
								msg.setExtraOptional((short) 0x1401,
										new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
							}
							if (bulkSmsDTO.getTelemarketerId() != null) {
								msg.setExtraOptional((short) 0x1402,
										new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
							}
							SubmitSMResp submitResponse = session.submit(msg);
							if (submitResponse != null) {
								count++;
								if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
									response_list.add(ResponseCode.NO_ERROR + "," + submitResponse.getMessageId() + ","
											+ destination_no);
									System.out.println(user + "(" + bulkSmsDTO.getRequestFormat()
											+ ") Message submitted < " + destination_no + " > ");
								} else {
									if (submitResponse.getCommandStatus() == 1035) {
										logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
												+ ") Insufficient balance -> ");
										response_list.add(ResponseCode.INSUF_BALANCE + ",0" + "," + destination_no);
										stopProcess = true;
										CommandStatus = submitResponse.getCommandStatus();
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
										logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
												+ ") Invalid Message Length -> ");
										response_list.add(ResponseCode.INVALID_TEXT + ",0" + "," + destination_no);
										stopProcess = true;
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
										logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
												+ ") Invalid Destination Address <" + destination_no + ">");
										response_list.add(ResponseCode.INVALID_DEST_ADDR + ",0" + "," + destination_no);
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
										logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
												+ ") Invalid Source Address <" + sender + ">");
										response_list.add(ResponseCode.INVALID_SENDER + ",0" + "," + destination_no);
										stopProcess = true;
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RSYSERR) {
										logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
												+ ") Submit System Error -> ");
										response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
										// exit = true;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
										logger.error(user + " <-(" + bulkSmsDTO.getRequestFormat()
												+ ") Account Expired -> ");
										response_list.add(ResponseCode.ACCOUNT_EXPIRED + ",0" + "," + destination_no);
										CommandStatus = submitResponse.getCommandStatus();
										stopProcess = true;
										break;
									} else {
										response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
										logger.error(user + " (" + bulkSmsDTO.getRequestFormat() + ") Submit failed <"
												+ submitResponse.getCommandStatus() + ":" + destination_no + " >");
									}
								}
							} else {
								response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
								logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
										+ ") No Response On Submit <" + destination_no + ">");
								CommandStatus = -1;
								stopProcess = true;
								break;
							}
						} catch (TimeoutException | PDUException | WrongSessionStateException e) {
							response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
							logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Submit Error: " + e);
						} catch (Exception ioe) {
							CommandStatus = -1;
							response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination_no);
							logger.error(
									"<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Connection Error: " + ioe);
							stopProcess = true;
							break;
						}
					}
					if (CommandStatus != Data.ESME_ROK) {
						userSession.setCommandStatus(CommandStatus);
					}
					if (stopProcess) {
						break;
					}
					if (count >= IConstants.HTTPsmsCount) {
						try {
							Thread.sleep(IConstants.HTTPSleepTime);
						} catch (InterruptedException ie) {
						}
						count = 0;
					}
				}
			}
		} catch (Exception e) {
			response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + ",0");
			logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") System Error: " + e);
		}
		smsServiceImpl.putUserSession(userSession);
		return response_list;
	}

	public List<String> submitCustomRequest(ApiRequestDTO bulkSmsDTO) {
		System.out.println("Custom API Request: " + bulkSmsDTO);
		List<String> response_list = new ArrayList<String>();
		String user = bulkSmsDTO.getUsername();
		String pwd = bulkSmsDTO.getPassword();
		String sender = bulkSmsDTO.getSender();
		int no_of_msg = 0;
		// String messageType = bulkSmsDTO.getMessageType();
		List<String[]> customlist = bulkSmsDTO.getCustomReceipients();
		int ston = bulkSmsDTO.getSourceTon();
		int snpi = bulkSmsDTO.getSourceNpi();
		int count = 0;
		UserSession userSession = null;
		try {
			userSession = smsServiceImpl.getUserSession(user, pwd);
			commandid = userSession.getCommandStatus();
			if (commandid != Data.ESME_ROK) {
				if (commandid == Data.ESME_RINVSYSID) {
					response_list.add(ResponseCode.INVALID_LOGIN + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
							+ ") Connection Error. < Invalid System ID >");
				} else if (commandid == Data.ESME_RINVPASWD) {
					response_list.add(ResponseCode.INVALID_LOGIN + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
							+ ") Connection Error. < Invalid System ID/Password >");
				} else if (commandid == 1035) {
					response_list.add(ResponseCode.INSUF_BALANCE + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
							+ ") Connection Error. < Insufficient Balance >");
				} else if (commandid == Data.ESME_RINVEXPIRY) {
					response_list.add(ResponseCode.ACCOUNT_EXPIRED + ",0,0");
					logger.error(
							user + " (" + bulkSmsDTO.getRequestFormat() + ") Connection Error < Account Expired >");
				} else {
					response_list.add(ResponseCode.SYSTEM_ERROR + ",0,0");
					logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Connection Error. Commandid: "
							+ commandid);
				}
			} else {
				session = userSession.getSession();
				for (String[] custom_arr : customlist) {
					String messageType = bulkSmsDTO.getMessageType();
					String destination = custom_arr[0];
					String message = custom_arr[1];
					System.out.println("Custom :" + destination + " Message: " + message);
					int msg_length = message.length();
					if (messageType.compareToIgnoreCase("Arabic") == 0) {
						message = new Converters().UTF16(message);
						// summary_message = new
						// Converters().UTF16(summary_message);
						// System.out.println("Message: " + message);
						messageType = "Unicode";
						msg_length = message.length();
						if (msg_length > 280) {
							int rem = msg_length % 280;
							no_of_msg = msg_length / 280;
							if (rem > 0) {
								no_of_msg = no_of_msg + 1;
							}
						} else {
							no_of_msg = 1;
						}
					} else if (messageType.compareToIgnoreCase("Unicode") == 0) {
						if (msg_length > 280) {
							int rem = msg_length % 280;
							no_of_msg = msg_length / 280;
							if (rem > 0) {
								no_of_msg = no_of_msg + 1;
							}
						} else {
							no_of_msg = 1;
						}
					} else if (messageType.compareToIgnoreCase("SpecialChar") == 0) {
						// summary_message = new
						// Converters().UTF16(summary_message);
						if (msg_length > 160) {
							int rem = msg_length % 153;
							int qot = msg_length / 153;
							if (rem > 0) {
								no_of_msg = qot + 1;
							} else {
								no_of_msg = qot;
							}
						} else {
							no_of_msg = 1;
						}
					}
					if (messageType.equalsIgnoreCase("SpecialChar")) {
						message = getHexValue(message);
						message = getContent(message.toCharArray());
					}
					// boolean exit = false;
					int CommandStatus = Data.ESME_ROK;
					boolean stopProcess = false;
					SubmitSM msg = null;
					if (messageType.equalsIgnoreCase("Unicode") && no_of_msg > 1) {
						List<String> parts = Body.getUnicodeno(message);
						int counts = parts.size();
						int i = 1;
						int reference_number = rand.nextInt((255 - 10) + 1) + 10;
						while (!parts.isEmpty()) {
							String part = parts.remove(0);
							part = Converter.getUnicode(part.toCharArray());
							msg = new SubmitSM();
							// System.out.println("Part Number :=========> "+i);
							try {
								ByteBuffer byteMessage = new ByteBuffer();
								byteMessage.appendByte((byte) 0x05);
								byteMessage.appendByte((byte) 0x00);
								byteMessage.appendByte((byte) 0x03);
								byteMessage.appendByte((byte) reference_number);
								byteMessage.appendByte((byte) counts);
								byteMessage.appendByte((byte) i);
								byteMessage.appendString(part, Data.ENC_UTF16_BE);
								msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
								msg.setSourceAddr((byte) ston, (byte) snpi, sender);
								msg.setShortMessage(byteMessage);
								msg.setEsmClass((byte) 0x40);
								msg.setDataCoding((byte) 8);
								msg.setRegisteredDelivery((byte) 1);
								if (bulkSmsDTO.getPeId() != null) {
									System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
											+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
									msg.setExtraOptional((short) 0x1400,
											new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
								}
								if (bulkSmsDTO.getTemplateId() != null) {
									msg.setExtraOptional((short) 0x1401,
											new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
								}
								if (bulkSmsDTO.getTelemarketerId() != null) {
									msg.setExtraOptional((short) 0x1402,
											new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
								}
								/*
								 * msg.setSarMsgRefNum((short) 100); msg.setSarSegmentSeqnum((short) i);
								 * msg.setSarTotalSegments((short) counts);
								 */
								SubmitSMResp submitResponse = session.submit(msg);
								if (submitResponse != null) {
									count++;
									if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
										response_list.add(ResponseCode.NO_ERROR + "," + submitResponse.getMessageId()
												+ "," + destination);
										System.out.println(user + "(" + bulkSmsDTO.getRequestFormat()
												+ ") Message submitted < " + destination + " >");
									} else {
										if (submitResponse.getCommandStatus() == 1035) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Insufficient balance -> ");
											response_list.add(ResponseCode.INSUF_BALANCE + ",0" + "," + destination);
											stopProcess = true;
											CommandStatus = submitResponse.getCommandStatus();
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Message Length -> ");
											response_list.add(ResponseCode.INVALID_TEXT + ",0" + "," + destination);
											stopProcess = true;
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Destination Address <" + destination + ">");
											response_list
													.add(ResponseCode.INVALID_DEST_ADDR + ",0" + "," + destination);
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Source Address <" + sender + ">");
											response_list.add(ResponseCode.INVALID_SENDER + ",0" + "," + destination);
											stopProcess = true;
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RSYSERR) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Submit System Error -> ");
											response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
											// exit = true;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
											logger.error(user + " <-(" + bulkSmsDTO.getRequestFormat()
													+ ") Account Expired -> ");
											response_list.add(ResponseCode.ACCOUNT_EXPIRED + ",0" + "," + destination);
											CommandStatus = submitResponse.getCommandStatus();
											stopProcess = true;
											break;
										} else {
											response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Submit failed <" + submitResponse.getCommandStatus() + ":"
													+ destination + " >");
										}
									}
								} else {
									response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
									logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
											+ ") No Response On Submit <" + destination + ">");
									CommandStatus = -1;
									stopProcess = true;
									break;
								}
							} catch (TimeoutException | PDUException | WrongSessionStateException e) {
								response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
								logger.error(
										"<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Submit Error: " + e);
							} catch (Exception ioe) {
								CommandStatus = -1;
								response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
								logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
										+ ") Connection Error: " + ioe);
								stopProcess = true;
								break;
							}
							i++;
						}
					} else if (messageType.equalsIgnoreCase("SpecialChar") && no_of_msg > 1) {
						List parts = Body.getEnglishno(message);
						// StringTokenizer stkmsg = new StringTokenizer(parts,
						// "##");
						int counts = parts.size();
						int i = 1;
						int reference_number = rand.nextInt((255 - 10) + 1) + 10;
						while (!parts.isEmpty()) {
							String engmsg = (String) parts.remove(0);
							// int rn = 100;
							int length1 = 0;
							length1 = (engmsg.length() + 6);
							ByteBuffer bf = new ByteBuffer();
							bf.appendBuffer(Body.getHead());
							bf.appendByte((byte) length1);
							bf.appendBuffer(Body.getHeader(reference_number, counts, i));
							bf.appendString(engmsg);
							msg = new SubmitSM();
							try {
								msg.setBody(bf);
								msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
								msg.setSourceAddr((byte) ston, (byte) snpi, sender);
								msg.setDataCoding((byte) 0);
								msg.setEsmClass((byte) 0x40);
								msg.setRegisteredDelivery((byte) 1);
								if (bulkSmsDTO.getPeId() != null) {
									System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
											+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
									msg.setExtraOptional((short) 0x1400,
											new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
								}
								if (bulkSmsDTO.getTemplateId() != null) {
									msg.setExtraOptional((short) 0x1401,
											new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
								}
								if (bulkSmsDTO.getTelemarketerId() != null) {
									msg.setExtraOptional((short) 0x1402,
											new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
								}
								/*
								 * msg.setSarMsgRefNum((short) rn); msg.setSarSegmentSeqnum((short) i);
								 * msg.setSarTotalSegments((short) counts);
								 */
								SubmitSMResp submitResponse = session.submit(msg);
								if (submitResponse != null) {
									count++;
									if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
										response_list.add(ResponseCode.NO_ERROR + "," + submitResponse.getMessageId()
												+ "," + destination);
										System.out.println(user + "(" + bulkSmsDTO.getRequestFormat()
												+ ") Message submitted < " + destination + " >");
									} else {
										if (submitResponse.getCommandStatus() == 1035) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Insufficient balance -> ");
											response_list.add(ResponseCode.INSUF_BALANCE + ",0" + "," + destination);
											stopProcess = true;
											CommandStatus = submitResponse.getCommandStatus();
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Message Length -> ");
											response_list.add(ResponseCode.INVALID_TEXT + ",0" + "," + destination);
											stopProcess = true;
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Destination Address <" + destination + ">");
											response_list
													.add(ResponseCode.INVALID_DEST_ADDR + ",0" + "," + destination);
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Invalid Source Address <" + sender + ">");
											response_list.add(ResponseCode.INVALID_SENDER + ",0" + "," + destination);
											stopProcess = true;
											break;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RSYSERR) {
											logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
													+ ") Submit System Error -> ");
											response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
											// exit = true;
										} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
											logger.error(user + " <-(" + bulkSmsDTO.getRequestFormat()
													+ ") Account Expired -> ");
											response_list.add(ResponseCode.ACCOUNT_EXPIRED + ",0" + "," + destination);
											CommandStatus = submitResponse.getCommandStatus();
											stopProcess = true;
											break;
										} else {
											response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
											logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
													+ ") Submit failed <" + submitResponse.getCommandStatus() + ":"
													+ destination + " >");
										}
									}
								} else {
									response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
									logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
											+ ") No Response On Submit <" + destination + ">");
									CommandStatus = -1;
									stopProcess = true;
									break;
								}
							} catch (TimeoutException | PDUException | WrongSessionStateException e) {
								response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
								logger.error(
										"<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Submit Error: " + e);
							} catch (Exception ioe) {
								CommandStatus = -1;
								response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
								logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
										+ ") Connection Error: " + ioe);
								stopProcess = true;
								break;
							}
							i++;
						}
					} else {
						msg = new SubmitSM();
						try {
							if (messageType.equalsIgnoreCase("SpecialChar")) {
								msg.setShortMessage(message, "ISO8859_1");
								msg.setDataCoding((byte) 0);
								msg.setEsmClass((byte) 0);
							} else if (messageType.equalsIgnoreCase("Unicode")) {
								String temp_msg = Converter.getUnicode(message.toCharArray());
								System.out.println("************** Unicode Message*******************");
								msg.setShortMessage(temp_msg, Data.ENC_UTF16_BE);
								msg.setEsmClass((byte) 0);
								msg.setDataCoding((byte) 8);
							}
							msg.setRegisteredDelivery((byte) 1);
							msg.setSourceAddr((byte) ston, (byte) snpi, sender);
							msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
							if (bulkSmsDTO.getPeId() != null) {
								System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
										+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
								msg.setExtraOptional((short) 0x1400, new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
							}
							if (bulkSmsDTO.getTemplateId() != null) {
								msg.setExtraOptional((short) 0x1401,
										new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
							}
							if (bulkSmsDTO.getTelemarketerId() != null) {
								msg.setExtraOptional((short) 0x1402,
										new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
							}
							SubmitSMResp submitResponse = session.submit(msg);
							if (submitResponse != null) {
								count++;
								if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
									response_list.add(ResponseCode.NO_ERROR + "," + submitResponse.getMessageId() + ","
											+ destination);
									System.out.println(user + "(" + bulkSmsDTO.getRequestFormat()
											+ ") Message submitted < " + destination + " >");
								} else {
									if (submitResponse.getCommandStatus() == 1035) {
										logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
												+ ") Insufficient balance -> ");
										response_list.add(ResponseCode.INSUF_BALANCE + ",0" + "," + destination);
										stopProcess = true;
										CommandStatus = submitResponse.getCommandStatus();
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
										logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
												+ ") Invalid Message Length -> ");
										response_list.add(ResponseCode.INVALID_TEXT + ",0" + "," + destination);
										stopProcess = true;
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
										logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
												+ ") Invalid Destination Address <" + destination + ">");
										response_list.add(ResponseCode.INVALID_DEST_ADDR + ",0" + "," + destination);
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
										logger.error(user + " (" + bulkSmsDTO.getRequestFormat()
												+ ") Invalid Source Address <" + sender + ">");
										response_list.add(ResponseCode.INVALID_SENDER + ",0" + "," + destination);
										stopProcess = true;
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RSYSERR) {
										logger.error(user + " <- (" + bulkSmsDTO.getRequestFormat()
												+ ") Submit System Error -> ");
										response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
										// exit = true;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
										logger.error(user + " <-(" + bulkSmsDTO.getRequestFormat()
												+ ") Account Expired -> ");
										response_list.add(ResponseCode.ACCOUNT_EXPIRED + ",0" + "," + destination);
										CommandStatus = submitResponse.getCommandStatus();
										stopProcess = true;
										break;
									} else {
										response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
										logger.error(user + " (" + bulkSmsDTO.getRequestFormat() + ") Submit failed <"
												+ submitResponse.getCommandStatus() + ":" + destination + " >");
									}
								}
							} else {
								response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
								logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat()
										+ ") No Response On Submit <" + destination + ">");
								CommandStatus = -1;
								stopProcess = true;
								break;
							}
						} catch (TimeoutException | PDUException | WrongSessionStateException e) {
							response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
							logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Submit Error: " + e);
						} catch (Exception ioe) {
							CommandStatus = -1;
							response_list.add(ResponseCode.SYSTEM_ERROR + ",0" + "," + destination);
							logger.error(
									"<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") Connection Error: " + ioe);
							stopProcess = true;
							break;
						}
					}
					if (CommandStatus != Data.ESME_ROK) {
						userSession.setCommandStatus(CommandStatus);
					}
					if (stopProcess) {
						break;
					}
					if (count >= IConstants.HTTPsmsCount) {
						try {
							Thread.sleep(IConstants.HTTPSleepTime);
						} catch (InterruptedException ie) {
						}
						count = 0;
					}
				}
			}
		} catch (Exception e) {
			response_list.add(ResponseCode.SYSTEM_ERROR + ",0,0");
			logger.error("<- " + user + " (" + bulkSmsDTO.getRequestFormat() + ") System Error: " + e);
		}
		smsServiceImpl.putUserSession(userSession);
		return response_list;
	}

	public String scheduleURL(BulkSmsDTO smsDTO, String batchId) {
		// RouteDAService routeService = new RouteDAServiceImpl();
		String ret = "";
		int no_of_msg = 0;
		String msgType = smsDTO.getMessageType();
		int msg_length = (smsDTO.getMessage()).length();
		if (msgType.compareToIgnoreCase("Arabic") == 0) {
			String msg = smsDTO.getMessage();
			msg = new Converters().UTF16(msg);
			smsDTO.setDistinct("yes");
			smsDTO.setMessageType("Unicode");
			smsDTO.setMessage(msg);
			msg_length = msg.length();
			if (msg_length > 280) {
				int rem = msg_length % 280;
				no_of_msg = msg_length / 280;
				if (rem > 0) {
					no_of_msg = no_of_msg + 1;
				}
			} else {
				no_of_msg = 1;
			}
		} else if (msgType.compareToIgnoreCase("Unicode") == 0) {
			if (msg_length % 4 != 0) {
				return ret + IConstants.ERROR_HTTP17;
			} else {
				if (msg_length > 280) {
					int rem = msg_length % 280;
					no_of_msg = msg_length / 280;
					if (rem > 0) {
						no_of_msg = no_of_msg + 1;
					}
				} else {
					no_of_msg = 1;
				}
			}
		} else if (msgType.compareToIgnoreCase("SpecialChar") == 0) {
			if (msg_length > 160) {
				int rem = msg_length % 153;
				int qot = msg_length / 153;
				if (rem > 0) {
					no_of_msg = qot + 1;
				} else {
					no_of_msg = qot;
				}
			} else {
				no_of_msg = 1;
			}
			// String msg = smsDTO.getMessage();
			String msg = getHexValue(smsDTO.getMessage());
			msg = getContent(msg.toCharArray());
			smsDTO.setMessage(msg);
			smsDTO.setOrigMessage(new Converters().UTF16(smsDTO.getOrigMessage()));
		}
		// System.out.println("no_of_msg -> " + no_of_msg);
		if (no_of_msg > 0) {
			List<String> destinationList = smsDTO.getDestinationList();
			// boolean proceedFurther = true;
			LoginDTO loginDTO = new LoginDTO();
			loginDTO.setSystemId(smsDTO.getSystemId());
			UserEntry userEntry = null;
			UserService userService = new UserService();
			userEntry = userService.getUserEntry(loginDTO.getSystemId());
			if (userEntry != null) {
				double totalcost = 0, wallet = 0;
				try {
					// String userExparyDate = (userSessionObject.getExpiry()).toString();
					// String adminId = userSessionObject.getMasterId();
					BalanceEntry balanceEntry = GlobalVars.BalanceEntries.get(userEntry.getId());
					String wallet_flag = balanceEntry.getWalletFlag();
					wallet = balanceEntry.getWalletAmount();
					int master_id = GlobalVars.UserMapping.get(userEntry.getMasterId());
					BalanceEntry masterBalanceEntry = GlobalVars.BalanceEntries.get(master_id);
					double adminWallet = masterBalanceEntry.getWalletAmount();
					if (destinationList.size() > 0 && wallet_flag.equalsIgnoreCase("yes")) {
						smsDTO.setUserMode("wallet");
						totalcost = routeService.calculateRoutingCost(userEntry.getId(), destinationList, no_of_msg);
						System.out.println("Estimated cost For Sheduled File       : " + totalcost);
						smsDTO.setTotalCost(totalcost);
						boolean amount = false;
						if (userEntry.isAdminDepend() && (adminWallet >= totalcost)) {
							amount = true;
						} else if (wallet >= totalcost) {
							amount = true;
						}
						if (!amount) {
							ret = IConstants.ERROR_HTTP18;
						} else {
							smsDTO.setMsgCount(destinationList.size() * no_of_msg);
							smsDTO.setTotalCost(totalcost);
							int generated_id = 0;
							SendSmsService service = new SendSmsService();
							String filename = service.createScheduleFile(smsDTO);
							if (filename != null) {
								System.out.println(
										"Scheduled Date -> " + smsDTO.getDate() + " Time -> " + smsDTO.getTime());
								generated_id = scheduleEntryRepository.save(new ScheduleEntry(userEntry.getSystemId(),
										smsDTO.getDate() + " " + smsDTO.getTime(), smsDTO.getGmt(),
										smsDTO.getTimestart(), IConstants.SERVER_ID, "false", filename,
										smsDTO.getRepeat(), smsDTO.getReqType(), batchId)).getId();
							}
							if (generated_id > 0) {
								String today = Validation.getTodayDateFormat();
								if (today.equalsIgnoreCase(smsDTO.getDate().trim())) {
									Set<Integer> set = null;
									if (GlobalVarsSms.ScheduledBatches.containsKey(smsDTO.getTime())) {
										set = GlobalVarsSms.ScheduledBatches.get(smsDTO.getTime());
									} else {
										set = new LinkedHashSet<Integer>();
									}
									set.add(generated_id);
									GlobalVarsSms.ScheduledBatches.put(smsDTO.getTime(), set);
								}
								if (!smsDTO.getRepeat().equalsIgnoreCase("no")) {
									GlobalVarsSms.RepeatedSchedules.add(generated_id);
								}
								long totalMsg = destinationList.size() * no_of_msg;
								ret = "Total Message Scheduled -> " + totalMsg;
							}
						}
					} else if (destinationList.size() > 0 && wallet_flag.equalsIgnoreCase("no")) {
						smsDTO.setUserMode("credit");
						long credits = balanceEntry.getCredits();
						long adminCredit = masterBalanceEntry.getCredits();
						boolean amount = false;
						if (userEntry.isAdminDepend() && (adminCredit >= (destinationList.size() * no_of_msg))) {
							amount = true;
						} else if (credits >= (destinationList.size() * no_of_msg)) {
							amount = true;
						}
						if (amount) {
							smsDTO.setMsgCount(destinationList.size() * no_of_msg);
							SendSmsService service = new SendSmsService();
							String filename = service.createScheduleFile(smsDTO);
							int generated_id = 0;
							if (filename != null) {
								System.out.println(
										"Scheduled Date -> " + smsDTO.getDate() + " Time -> " + smsDTO.getTime());
								generated_id = scheduleEntryRepository.save(new ScheduleEntry(userEntry.getSystemId(),
										smsDTO.getDate() + " " + smsDTO.getTime(), smsDTO.getGmt(),
										smsDTO.getTimestart(), IConstants.SERVER_ID, "false", filename,
										smsDTO.getRepeat(), smsDTO.getReqType(), batchId)).getId();
							}
							if (generated_id > 0) {
								String today = Validation.getTodayDateFormat();
								if (today.equalsIgnoreCase(smsDTO.getDate().trim())) {
									Set<Integer> set = null;
									if (GlobalVarsSms.ScheduledBatches.containsKey(smsDTO.getTime())) {
										set = GlobalVarsSms.ScheduledBatches.get(smsDTO.getTime());
									} else {
										set = new LinkedHashSet<Integer>();
									}
									set.add(generated_id);
									GlobalVarsSms.ScheduledBatches.put(smsDTO.getTime(), set);
								}
								if (!smsDTO.getRepeat().equalsIgnoreCase("no")) {
									GlobalVarsSms.RepeatedSchedules.add(generated_id);
								}
								long totalMsg = destinationList.size() * no_of_msg;
								ret = "Total Message Scheduled -> " + totalMsg;
							}
						} else {
							ret = IConstants.ERROR_HTTP18;
						}
					}
					if (wallet_flag.equalsIgnoreCase("MIN") || wallet_flag == null) {
						ret = IConstants.ERROR_HTTP18;
					}
				} catch (Exception ex) {
					System.out.println("<---- Exception While Scheduling URL ---->");
					ex.printStackTrace();
					ret = IConstants.ERROR_HTTP03;
				}
			} else {
				ret = IConstants.ERROR_HTTP03;
			}
		} else {
			ret = IConstants.ERROR_HTTP10;
		}
		return ret;
	}

	public String sendCoverageURL(BulkSmsDTO bulkSmsDTO) {
		String ret = "Response: ";
		// int no_of_msg = 0;
		String messageType = bulkSmsDTO.getMessageType();
		String message = bulkSmsDTO.getMessage();
		int msg_length = message.length();
		List<String> numberlist = new ArrayList<String>(bulkSmsDTO.getDestinationList());
		if (messageType.compareToIgnoreCase("Arabic") == 0) {
			message = new Converters().UTF16(message);
			messageType = "Unicode";
			msg_length = message.length();
			if (msg_length > 280) {
				return ret + IConstants.ERROR_HTTP17;
			}
		} else if (messageType.compareToIgnoreCase("Unicode") == 0) {
			if (msg_length % 4 != 0) {
				return ret + IConstants.ERROR_HTTP17;
			} else {
				if (msg_length > 280) {
					return ret + IConstants.ERROR_HTTP17;
				}
			}
		} else if (messageType.compareToIgnoreCase("SpecialChar") == 0) {
			if (msg_length > 160) {
				return ret + IConstants.ERROR_HTTP17;
			}
		}
		// System.out.println("no_of_msg -> " + no_of_msg);
		String user = bulkSmsDTO.getSystemId();
		String pwd = bulkSmsDTO.getPassword();
		String sender = bulkSmsDTO.getSenderId();
		int ston = 5;
		int snpi = 0;
		if (bulkSmsDTO.getFrom().compareToIgnoreCase("Mobile") == 0) {
			ston = 1;
			snpi = 1;
		}
		int count = 0;
		String temp_msg = "";
		UserSession userSession = smsServiceImpl.getUserSession(user, pwd);
		commandid = userSession.getCommandStatus();
		if (commandid != Data.ESME_ROK) {
			if (commandid == Data.ESME_RINVSYSID) {
				ret = IConstants.ERROR_HTTP05;
			} else if (commandid == Data.ESME_RINVPASWD) {
				ret = IConstants.ERROR_HTTP04;
			} else if (commandid == 1035) {
				ret = IConstants.ERROR_HTTP18;
			} else {
				ret = IConstants.ERROR_HTTP14;
			}
		} else {
			session = userSession.getSession();
			if (messageType.equalsIgnoreCase("SpecialChar")) {
				message = getHexValue(message);
				message = getContent(message.toCharArray());
			}
			for (String smsc : bulkSmsDTO.getSmscList()) {
				for (String destination_no : numberlist) {
					logger.info(user + " [" + destination_no + "] Coverage Test From: " + smsc);
					int CommandStatus = Data.ESME_ROK;
					boolean stopProcess = false;
					SubmitSM msg = new SubmitSM();
					try {
						if (messageType.equalsIgnoreCase("SpecialChar")) {
							temp_msg = message + " " + smsc;
							msg.setShortMessage(temp_msg, "ISO8859_1");
							msg.setDataCoding((byte) 0);
							msg.setEsmClass((byte) 0);
						} else if (messageType.equalsIgnoreCase("Unicode")) {
							temp_msg = Converter.getUnicode(message.toCharArray());
							temp_msg = temp_msg + " " + smsc;
							System.out.println("************** Unicode Message*******************");
							msg.setShortMessage(temp_msg, Data.ENC_UTF16_BE);
							msg.setDataCoding((byte) 8);
						}
						msg.setRegisteredDelivery((byte) 1);
						msg.setSourceAddr((byte) ston, (byte) snpi, sender);
						msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
						msg.setServiceType("CTEST");
						msg.setDestSubaddress(new com.logica.smpp.util.ByteBuffer(smsc.getBytes()));
						if (bulkSmsDTO.getPeId() != null) {
							System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
									+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
							msg.setExtraOptional((short) 0x1400, new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
						}
						if (bulkSmsDTO.getTemplateId() != null) {
							msg.setExtraOptional((short) 0x1401, new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
						}
						if (bulkSmsDTO.getTelemarketerId() != null) {
							msg.setExtraOptional((short) 0x1402,
									new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
						}
						SubmitSMResp submitResponse = session.submit(msg);
						if (submitResponse != null) {
							count++;
							if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
								ret += submitResponse.getMessageId() + "\n";
								System.out.println(user + "(Http) Message submitted < " + destination_no + " >");
							} else {
								if (submitResponse.getCommandStatus() == 1035) {
									System.out.println(user + " <- (Http) Insufficient balance -> ");
									ret += IConstants.ERROR_HTTP18 + "\n";
									CommandStatus = submitResponse.getCommandStatus();
									stopProcess = true;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
									System.out.println(user + " <- (Http) Invalid Message Length -> ");
									ret += IConstants.ERROR_HTTP17;
									stopProcess = true;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
									System.out.println(
											user + " (Http) Invalid Destination Address <" + destination_no + ">");
									ret += IConstants.ERROR_HTTP09 + "\n";
									// exit = true;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
									System.out.println(user + " (Http) Invalid Source Address <" + sender + ">");
									ret += IConstants.ERROR_HTTP06;
									stopProcess = true;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RSYSERR) {
									System.out.println(user + " <- (Http) System Error -> ");
									ret += IConstants.ERROR_HTTP12 + "\n";
									// exit = true;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
									System.out.println(user + " <-(Http) Account Expired -> ");
									ret += IConstants.ERROR_HTTP26;
									CommandStatus = submitResponse.getCommandStatus();
									stopProcess = true;
								} else {
									ret += IConstants.ERROR_HTTP12 + "\n";
									System.out.println(user + " (Http) Submit failed <"
											+ submitResponse.getCommandStatus() + ":" + destination_no + " >");
								}
							}
						} else {
							ret += IConstants.ERROR_HTTP14 + "\n";
							System.out.println("<- " + user + " (HTTP) No Response On Submit <" + destination_no + ">");
							CommandStatus = -1;
							stopProcess = true;
						}
					} catch (TimeoutException | PDUException | WrongSessionStateException e) {
						ret += IConstants.ERROR_HTTP03 + "\n";
						System.out.println("<- " + user + " (HTTP) Submit Error: " + e);
					} catch (Exception ioe) {
						CommandStatus = -1;
						ret += IConstants.ERROR_HTTP14;
						System.out.println("<- " + user + " (HTTP) Connection Error: " + ioe);
						stopProcess = true;
					}
					if (CommandStatus != Data.ESME_ROK) {
						userSession.setCommandStatus(CommandStatus);
					}
					if (stopProcess) {
						break;
					}
					if (count >= IConstants.HTTPsmsCount) {
						try {
							Thread.sleep(IConstants.HTTPSleepTime);
						} catch (InterruptedException ie) {
						}
						count = 0;
					}
				}
			}
		}
		smsServiceImpl.putUserSession(userSession);
		return ret;
	}

	public String sendURL(BulkSmsDTO bulkSmsDTO, String web_dlr_param) throws Exception {
		String ret = "";
		int no_of_msg = 0;
		String messageType = bulkSmsDTO.getMessageType();
		String message = bulkSmsDTO.getMessage();
		int msg_length = message.length();
		List numberlist = bulkSmsDTO.getDestinationList();
		int processed_number = numberlist.size();
		String summary_message = bulkSmsDTO.getMessage();
		if (messageType.compareToIgnoreCase("Arabic") == 0) {
			message = new Converters().UTF16(message);
			summary_message = new Converters().UTF16(summary_message);
			// System.out.println("Message: " + message);
			messageType = "Unicode";
			msg_length = message.length();
			if (msg_length > 280) {
				int rem = msg_length % 280;
				no_of_msg = msg_length / 280;
				if (rem > 0) {
					no_of_msg = no_of_msg + 1;
				}
			} else {
				no_of_msg = 1;
			}
		} else if (messageType.compareToIgnoreCase("Unicode") == 0) {
			if (msg_length % 4 != 0) {
				return ret + IConstants.ERROR_HTTP17;
			} else {
				if (msg_length > 280) {
					int rem = msg_length % 280;
					no_of_msg = msg_length / 280;
					if (rem > 0) {
						no_of_msg = no_of_msg + 1;
					}
				} else {
					no_of_msg = 1;
				}
			}
		} else if (messageType.compareToIgnoreCase("SpecialChar") == 0) {
			summary_message = new Converters().UTF16(summary_message);
			if (msg_length > 160) {
				int rem = msg_length % 153;
				int qot = msg_length / 153;
				if (rem > 0) {
					no_of_msg = qot + 1;
				} else {
					no_of_msg = qot;
				}
			} else {
				no_of_msg = 1;
			}
		}
		// System.out.println("no_of_msg -> " + no_of_msg);
		String user = bulkSmsDTO.getSystemId();
		String pwd = bulkSmsDTO.getPassword();
		String sender = bulkSmsDTO.getSenderId();
		String destination_no;
		int ston = 5;
		int snpi = 0;
		if (bulkSmsDTO.getFrom().compareToIgnoreCase("Mobile") == 0) {
			ston = 1;
			snpi = 1;
		}
		int count = 0;
		String temp_msg = "";
		UserSession userSession = smsServiceImpl.getUserSession(user, pwd);
		commandid = userSession.getCommandStatus();
		if (commandid != Data.ESME_ROK) {
			if (commandid == Data.ESME_RINVSYSID) {
				ret = IConstants.ERROR_HTTP05;
				// System.out.println("<- " + user + " (HTTP) Connection Error.
				// < Invalid System ID >");
			} else if (commandid == Data.ESME_RINVPASWD) {
				ret = IConstants.ERROR_HTTP04;
				// System.out.println("<- " + user + " (HTTP) Connection Error.
				// < Invalid System ID/Password >");
			} else if (commandid == 1035) {
				ret = IConstants.ERROR_HTTP18;
				// System.out.println("<- " + user + " (HTTP) Connection Error.
				// < Insufficient Balance >");
			} else {
				ret = IConstants.ERROR_HTTP14;
				// System.out.println("<- " + user + " (HTTP) Connection Error.
				// Commandid: " + commandid);
			}
		} else {
			session = userSession.getSession();
			if (messageType.equalsIgnoreCase("SpecialChar")) {
				message = getHexValue(message);
				message = getContent(message.toCharArray());
			}
			while (!numberlist.isEmpty()) {
				// boolean exit = false;
				int CommandStatus = Data.ESME_ROK;
				boolean stopProcess = false;
				destination_no = (String) numberlist.remove(0);
				SubmitSM msg = null;
				if (messageType.equalsIgnoreCase("Unicode") && no_of_msg > 1) {
					// int rn = 100;
					List parts = Body.getUnicodeno(message);
					// StringTokenizer stkmsg = new StringTokenizer(parts,
					// "##");
					int counts = parts.size();
					int i = 1;
					int reference_number = rand.nextInt((255 - 10) + 1) + 10;
					while (!parts.isEmpty()) {
						String part = (String) parts.remove(0);
						part = Converter.getUnicode(part.toCharArray());
						msg = new SubmitSM();
						// System.out.println("Part Number :=========> "+i);
						try {
							ByteBuffer byteMessage = new ByteBuffer();
							byteMessage.appendByte((byte) 0x05);
							byteMessage.appendByte((byte) 0x00);
							byteMessage.appendByte((byte) 0x03);
							byteMessage.appendByte((byte) reference_number);
							byteMessage.appendByte((byte) counts);
							byteMessage.appendByte((byte) i);
							byteMessage.appendString(part, Data.ENC_UTF16_BE);
							msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
							msg.setSourceAddr((byte) ston, (byte) snpi, sender);
							msg.setShortMessage(byteMessage);
							msg.setEsmClass((byte) 0x40);
							msg.setDataCoding((byte) 8);
							msg.setRegisteredDelivery((byte) 1);
							if (bulkSmsDTO.getPeId() != null) {
								System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
										+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
								msg.setExtraOptional((short) 0x1400, new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
							}
							if (bulkSmsDTO.getTemplateId() != null) {
								msg.setExtraOptional((short) 0x1401,
										new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
							}
							if (bulkSmsDTO.getTelemarketerId() != null) {
								msg.setExtraOptional((short) 0x1402,
										new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
							}
							/*
							 * msg.setSarMsgRefNum((short) 100); msg.setSarSegmentSeqnum((short) i);
							 * msg.setSarTotalSegments((short) counts);
							 */
							SubmitSMResp submitResponse = session.submit(msg);
							if (submitResponse != null) {
								count++;
								if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
									ret += submitResponse.getMessageId() + "\n";
									if (web_dlr_param != null) {
										GlobalVars.HttpDlrParam.put(submitResponse.getMessageId(), web_dlr_param);
										HttpDlrParamInsert.paramQueue
												.enqueue(new HttpDlrParamEntry(submitResponse.getMessageId(),
														web_dlr_param.split("=")[0], web_dlr_param.split("=")[1]));
									}
									System.out.println(user + "(Http) Message submitted < " + destination_no + " >");
								} else {
									if (submitResponse.getCommandStatus() == 1035) {
										System.out.println(user + " <- (Http) Insufficient balance -> ");
										ret += IConstants.ERROR_HTTP18 + "\n";
										stopProcess = true;
										CommandStatus = submitResponse.getCommandStatus();
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
										System.out.println(user + " <- (Http) Invalid Message Length -> ");
										ret += IConstants.ERROR_HTTP17;
										stopProcess = true;
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
										System.out.println(
												user + " (Http) Invalid Destination Address <" + destination_no + ">");
										ret += IConstants.ERROR_HTTP09 + "\n";
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
										System.out.println(user + " (Http) Invalid Source Address <" + sender + ">");
										ret += IConstants.ERROR_HTTP06;
										stopProcess = true;
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RSYSERR) {
										System.out.println(user + " <- (Http) System Error -> ");
										ret += IConstants.ERROR_HTTP12 + "\n";
										// exit = true;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
										System.out.println(user + " <-(Http) Account Expired -> ");
										ret += IConstants.ERROR_HTTP26;
										CommandStatus = submitResponse.getCommandStatus();
										stopProcess = true;
										break;
									} else {
										ret += IConstants.ERROR_HTTP12 + "\n";
										System.out.println(user + " (Http) Submit failed <"
												+ submitResponse.getCommandStatus() + ":" + destination_no + " >");
									}
								}
							} else {
								ret += IConstants.ERROR_HTTP14 + "\n";
								System.out.println(
										"<- " + user + " (HTTP) No Response On Submit <" + destination_no + ">");
								CommandStatus = -1;
								stopProcess = true;
								break;
							}
						} catch (TimeoutException | PDUException | WrongSessionStateException e) {
							ret += IConstants.ERROR_HTTP03 + "\n";
							System.out.println("<- " + user + " (HTTP) Submit Error: " + e);
						} catch (Exception ioe) {
							CommandStatus = -1;
							ret += IConstants.ERROR_HTTP14;
							System.out.println("<- " + user + " (HTTP) Connection Error: " + ioe);
							stopProcess = true;
							break;
						}
						i++;
					}
				} else if (messageType.equalsIgnoreCase("SpecialChar") && no_of_msg > 1) {
					List parts = Body.getEnglishno(message);
					// StringTokenizer stkmsg = new StringTokenizer(parts,
					// "##");
					int counts = parts.size();
					int i = 1;
					int reference_number = rand.nextInt((255 - 10) + 1) + 10;
					while (!parts.isEmpty()) {
						String engmsg = (String) parts.remove(0);
						int length1 = 0;
						length1 = (engmsg.length() + 6);
						ByteBuffer bf = new ByteBuffer();
						bf.appendBuffer(Body.getHead());
						bf.appendByte((byte) length1);
						bf.appendBuffer(Body.getHeader(reference_number, counts, i));
						bf.appendString(engmsg);
						msg = new SubmitSM();
						try {
							msg.setBody(bf);
							msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
							msg.setSourceAddr((byte) ston, (byte) snpi, sender);
							msg.setDataCoding((byte) 0);
							msg.setEsmClass((byte) 0x40);
							msg.setRegisteredDelivery((byte) 1);
							if (bulkSmsDTO.getPeId() != null) {
								System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
										+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
								msg.setExtraOptional((short) 0x1400, new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
							}
							if (bulkSmsDTO.getTemplateId() != null) {
								msg.setExtraOptional((short) 0x1401,
										new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
							}
							if (bulkSmsDTO.getTelemarketerId() != null) {
								msg.setExtraOptional((short) 0x1402,
										new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
							}
							/*
							 * msg.setSarMsgRefNum((short) rn); msg.setSarSegmentSeqnum((short) i);
							 * msg.setSarTotalSegments((short) counts);
							 */
							SubmitSMResp submitResponse = session.submit(msg);
							if (submitResponse != null) {
								count++;
								if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
									ret += submitResponse.getMessageId() + "\n";
									if (web_dlr_param != null) {
										GlobalVars.HttpDlrParam.put(submitResponse.getMessageId(), web_dlr_param);
										HttpDlrParamInsert.paramQueue
												.enqueue(new HttpDlrParamEntry(submitResponse.getMessageId(),
														web_dlr_param.split("=")[0], web_dlr_param.split("=")[1]));
									}
									System.out.println(user + "(Http) Message submitted < " + destination_no + " >");
								} else {
									if (submitResponse.getCommandStatus() == 1035) {
										System.out.println(user + " <- (Http) Insufficient balance -> ");
										ret += IConstants.ERROR_HTTP18 + "\n";
										CommandStatus = submitResponse.getCommandStatus();
										stopProcess = true;
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
										System.out.println(user + " <- (Http) Invalid Message Length -> ");
										ret += IConstants.ERROR_HTTP17;
										stopProcess = true;
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
										System.out.println(
												user + " (Http) Invalid Destination Address <" + destination_no + ">");
										ret += IConstants.ERROR_HTTP09 + "\n";
										break;
										// exit = true;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
										System.out.println(user + " (Http) Invalid Source Address <" + sender + ">");
										ret += IConstants.ERROR_HTTP06;
										stopProcess = true;
										break;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RSYSERR) {
										System.out.println(user + " <- (Http) System Error -> ");
										ret += IConstants.ERROR_HTTP12 + "\n";
										// exit = true;
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
										System.out.println(user + " <-(Http) Account Expired -> ");
										ret += IConstants.ERROR_HTTP26;
										CommandStatus = submitResponse.getCommandStatus();
										stopProcess = true;
										break;
									} else {
										ret += IConstants.ERROR_HTTP12 + "\n";
										System.out.println(user + " (Http) Submit failed <"
												+ submitResponse.getCommandStatus() + ":" + destination_no + " >");
									}
								}
							} else {
								ret += IConstants.ERROR_HTTP14 + "\n";
								System.out.println(
										"<- " + user + " (HTTP) No Response On Submit <" + destination_no + ">");
								CommandStatus = -1;
								stopProcess = true;
								break;
							}
						} catch (TimeoutException | PDUException | WrongSessionStateException e) {
							ret += IConstants.ERROR_HTTP03 + "\n";
							System.out.println("<- " + user + " (HTTP) Submit Error: " + e);
						} catch (Exception ioe) {
							CommandStatus = -1;
							ret += IConstants.ERROR_HTTP14;
							System.out.println("<- " + user + " (HTTP) Connection Error: " + ioe);
							stopProcess = true;
							break;
						}
						i++;
					}
				} else {
					msg = new SubmitSM();
					try {
						if (messageType.equalsIgnoreCase("SpecialChar")) {
							msg.setShortMessage(message, "ISO8859_1");
							msg.setDataCoding((byte) 0);
							msg.setEsmClass((byte) 0);
						} else if (messageType.equalsIgnoreCase("Unicode")) {
							temp_msg = Converter.getUnicode(message.toCharArray());
							System.out.println("************** Unicode Message*******************");
							msg.setShortMessage(temp_msg, Data.ENC_UTF16_BE);
							// msg.setEsmClass((byte) 0);
							msg.setDataCoding((byte) 8);
						}
						msg.setRegisteredDelivery((byte) 1);
						msg.setSourceAddr((byte) ston, (byte) snpi, sender);
						msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
						if (bulkSmsDTO.getPeId() != null) {
							System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
									+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
							msg.setExtraOptional((short) 0x1400, new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
						}
						if (bulkSmsDTO.getTemplateId() != null) {
							msg.setExtraOptional((short) 0x1401, new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
						}
						if (bulkSmsDTO.getTelemarketerId() != null) {
							msg.setExtraOptional((short) 0x1402,
									new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
						}
						SubmitSMResp submitResponse = session.submit(msg);
						if (submitResponse != null) {
							count++;
							if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
								ret += submitResponse.getMessageId() + "\n";
								if (web_dlr_param != null) {
									GlobalVars.HttpDlrParam.put(submitResponse.getMessageId(), web_dlr_param);
									HttpDlrParamInsert.paramQueue
											.enqueue(new HttpDlrParamEntry(submitResponse.getMessageId(),
													web_dlr_param.split("=")[0], web_dlr_param.split("=")[1]));
								}
								System.out.println(user + "(Http) Message submitted < " + destination_no + " >");
							} else {
								if (submitResponse.getCommandStatus() == 1035) {
									System.out.println(user + " <- (Http) Insufficient balance -> ");
									ret += IConstants.ERROR_HTTP18 + "\n";
									CommandStatus = submitResponse.getCommandStatus();
									stopProcess = true;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
									System.out.println(user + " <- (Http) Invalid Message Length -> ");
									ret += IConstants.ERROR_HTTP17;
									stopProcess = true;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
									System.out.println(
											user + " (Http) Invalid Destination Address <" + destination_no + ">");
									ret += IConstants.ERROR_HTTP09 + "\n";
									// exit = true;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
									System.out.println(user + " (Http) Invalid Source Address <" + sender + ">");
									ret += IConstants.ERROR_HTTP06;
									stopProcess = true;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RSYSERR) {
									System.out.println(user + " <- (Http) System Error -> ");
									ret += IConstants.ERROR_HTTP12 + "\n";
									// exit = true;
								} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
									System.out.println(user + " <-(Http) Account Expired -> ");
									ret += IConstants.ERROR_HTTP26;
									CommandStatus = submitResponse.getCommandStatus();
									stopProcess = true;
								} else {
									ret += IConstants.ERROR_HTTP12 + "\n";
									System.out.println(user + " (Http) Submit failed <"
											+ submitResponse.getCommandStatus() + ":" + destination_no + " >");
								}
							}
						} else {
							ret += IConstants.ERROR_HTTP14 + "\n";
							System.out.println("<- " + user + " (HTTP) No Response On Submit <" + destination_no + ">");
							CommandStatus = -1;
							stopProcess = true;
						}
					} catch (TimeoutException | PDUException | WrongSessionStateException e) {
						ret += IConstants.ERROR_HTTP03 + "\n";
						System.out.println("<- " + user + " (HTTP) Submit Error: " + e);
					} catch (Exception ioe) {
						CommandStatus = -1;
						ret += IConstants.ERROR_HTTP14;
						System.out.println("<- " + user + " (HTTP) Connection Error: " + ioe);
						stopProcess = true;
					}
				}
				if (CommandStatus != Data.ESME_ROK) {
					userSession.setCommandStatus(CommandStatus);
				}
				if (stopProcess) {
					break;
				}
				if (count >= IConstants.HTTPsmsCount) {
					try {
						Thread.sleep(IConstants.HTTPSleepTime);
					} catch (InterruptedException ie) {
					}
					count = 0;
				}
			}
			// ********** Adding To Summary Report ****************
			if (!numberlist.isEmpty()) {
				processed_number = processed_number - numberlist.size();
			}
			BulkEntry entry = new BulkEntry();
			entry.setSystemId(user);
			entry.setMessageType(bulkSmsDTO.getMessageType());
			entry.setContent(summary_message);
			entry.setSenderId(bulkSmsDTO.getSenderId());
			entry.setReqType("http");
			entry.setTotal(processed_number);
			QueueBackupExt backupExt = new QueueBackupExt();
			backupExt.setBulkEntry(entry);
			backupExt.setTotalCost(0);
			backupExt.setUserMode("-");
			backupExt.setMsgCount(processed_number * no_of_msg);
			backupExt.setOrigMessage(summary_message);
			backupExt.setCampaignType(bulkSmsDTO.getCampaignType());
			try {
				smsServiceImpl.addSummaryReport(backupExt);
			} catch (Exception ex) {
				System.out.println(user + " Error Adding To (Http)Summary Report: " + ex);
			}
			// ********** End For Summary Report ****************
		}
		smsServiceImpl.putUserSession(userSession);
		return ret;
	}

	public static String getHexValue(String msg) {
		char[] charArray;
		String HexMessage = "";
		charArray = msg.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			String character = ("" + charArray[i]).trim();
			// System.out.println(character.getBytes());
			int ascii = (int) charArray[i];
			// System.out.println("For "+i+" char Ascii = "+ ascii);
			if (ascii == 226) {
				int nextOne = (int) charArray[i + 1];
				// System.out.println("After 226 nextOne : "+nextOne);
				if (nextOne == 128) {
					nextOne = (int) charArray[i + 2];
					// System.out.println("After 1 nextOne : "+nextOne);
					if (nextOne == 153) {
						HexMessage += "27";
						i += 2;
						// System.out.println("After 226 i : "+i);
					}
				}
			}
			if (ascii == 32) {
				HexMessage += "20";
			} else if (ascii == 34) {
				HexMessage += "22";
			} else if (ascii == 10) {
				HexMessage += "0A";
			} else if (ascii == 13) {
				HexMessage += "0D";
			} else if (ascii == 2747 || ascii == 92) {
				HexMessage += "1B2F";
			} else if (ascii == 128 || ascii == 164) {
				HexMessage += "1B65";
			} else if (ascii == 96) {
				HexMessage += "";
			} else if (ascii == 172) {
				HexMessage += "07";
			} else if (ascii == 199) {
				HexMessage += "09";
			} else if (ascii == 228) {
				HexMessage += "7B";
			} else if (ascii == 246) {
				HexMessage += "7C";
			} else if (ascii == 241) {
				HexMessage += "7D";
			} else if (ascii == 252) {
				HexMessage += "7E";
			} else if (ascii == 178) {
				HexMessage += "08";
			} else if (ascii == 185) {
				HexMessage += "06";
			} else if (ascii == 168) {
				HexMessage += "04";
			} else if (ascii == 169) {
				HexMessage += "05";
			} else if (ascii == 198) {
				HexMessage += "1C";
			} else if (ascii == 230) {
				HexMessage += "1D";
			} else if (ascii == 216) {
				HexMessage += "0B";
			} else if (ascii == 248) {
				HexMessage += "0C";
			} else if (ascii == 197) {
				HexMessage += "0E";
			} else if (ascii == 196) {
				HexMessage += "5B";
			} else if (ascii == 229) {
				HexMessage += "0F";
			} else if (ascii == 163) {
				HexMessage += "01";
			} else if (ascii == 214) {
				HexMessage += "5C";
			} else if (ascii == 163) {
				HexMessage += "01";
			} else if (ascii == 165) {
				HexMessage += "03";
			} else if (ascii == 232) {
				HexMessage += "04";
			} else if (ascii == 233) {
				HexMessage += "05";
			} else if (ascii == 242) {
				HexMessage += "08";
			} else if (ascii == 95) {
				HexMessage += "11";
			} else if (ascii == 223) {
				HexMessage += "1E";
			} else if (ascii == 201) {
				HexMessage += "1F";
			} else if (ascii == 161) {
				HexMessage += "40";
			} else if (ascii == 209) {
				HexMessage += "5D";
			} else if (ascii == 220) {
				HexMessage += "5E";
			} else if (ascii == 167) {
				HexMessage += "5F";
			} else if (ascii == 191) {
				HexMessage += "60";
			} else if (ascii == 201) {
				HexMessage += "1F";
			} else if (ascii == 224 || ascii == 160) {
				HexMessage += "7F";
			} else if (ascii == 8217)// for the appostrophe from word pad
			{
				HexMessage += "27";
			} else {
				String hexv = (String) hashTabOne.get(character);
				if (hexv != null) {
					HexMessage += hexv;
				} else {
					HexMessage += "";
				}
			}
		}
		return HexMessage;
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
			System.out.println("Excepiton in getContent222 " + e);
		}
		return unicode;
	}

}
