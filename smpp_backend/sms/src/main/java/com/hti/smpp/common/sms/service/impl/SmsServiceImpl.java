package com.hti.smpp.common.sms.service.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.messages.dto.QueueBackupExt;
import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.schedule.dto.ScheduleEntry;
import com.hti.smpp.common.schedule.repository.ScheduleEntryRepository;
import com.hti.smpp.common.sms.request.SmsRequest;
import com.hti.smpp.common.sms.service.RouteDAService;
import com.hti.smpp.common.sms.service.SendSmsService;
import com.hti.smpp.common.sms.service.SmsService;
import com.hti.smpp.common.sms.session.SessionHandler;
import com.hti.smpp.common.sms.session.UserSession;
import com.hti.smpp.common.sms.util.Body;
import com.hti.smpp.common.sms.util.Converter;
import com.hti.smpp.common.sms.util.GlobalVars;
import com.hti.smpp.common.sms.util.IConstants;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.BalanceEntryRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.SubmitSMResp;
import com.logica.smpp.pdu.WrongDateFormatException;
import com.logica.smpp.util.ByteBuffer;

@Service
public class SmsServiceImpl implements SmsService {

	private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);
	private int commandid = 0;
	private Session session = null;

	@Autowired
	private ScheduleEntryRepository scheduleEntryRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RouteDAService routeService;

	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;

	@Autowired
	private BalanceEntryRepository balanceEntryRepository;

	@Autowired
	private UserEntryRepository userEntryRepository;

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

	public SmsResponse sendSms(SmsRequest smsRequest, String username) {
		Optional<User> userOptional = userRepository.findByUsername(username);

		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
		}
		Optional<UserEntry> userEntryOptional = userEntryRepository.findBySystemId(String.valueOf(user.getSystem_id()));
		UserEntry userEntry = null;
		if (userEntryOptional.isPresent()) {
			userEntry = userEntryOptional.get();
		}
		SmsResponse smsResponse = new SmsResponse();
		String target = IConstants.FAILURE_KEY;
		BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
		bulkSmsDTO.setClientId("testUser1");
		bulkSmsDTO.setSystemId("testUser1");
		bulkSmsDTO.setPassword("1");
		// if sms schedule the requre parameter
		bulkSmsDTO.setSchedule(false);
		bulkSmsDTO.setTimestart("");
		bulkSmsDTO.setGmt("");

		String bulkSessionId = bulkSmsDTO.getSystemId() + "_" + Long.toString(System.currentTimeMillis());
		bulkSmsDTO.setSenderId(smsRequest.getSenderId());
		bulkSmsDTO.setFrom("mobile");
		List<String> list = new ArrayList<String>();
		String[] split = smsRequest.getDestinationNumber().split(",");
		// VALIDATE AND PUT TO ARRAYLIST
		for (String number : split) {
			if (isValidNumber(number)) {
				list.add(number);
			}
		}
		bulkSmsDTO.setDestinationList(list);
		bulkSmsDTO.setMessageType(smsRequest.getMessageType());
		bulkSmsDTO.setMessage(smsRequest.getMessage());
		bulkSmsDTO.setDestinationNumber(smsRequest.getDestinationNumber());
		double totalcost = 0, adminCost = 0;// total_defcost = 0;
		String unicodeMsg = "";
		bulkSmsDTO.setReqType("Single");
		BulkListInfo listInfo = new BulkListInfo();

		if (bulkSmsDTO.isSchedule()) {
			logger.info(bulkSessionId + " Single Schedule Request <" + bulkSmsDTO.getDestinationNumber() + ">");
		} else {
			logger.info(bulkSessionId + " Single Sms Request <" + bulkSmsDTO.getDestinationNumber() + ">");
		}
		// ISendSmsService sendSmsService = new SendSmsService();
		int total_msg = 0;
		try {
			// String userExparyDate = (userSessionObject.getExpiry()).toString();
			// String adminId = userSessionObject.getMasterId();

			// check wallet balance
			Optional<BalanceEntry> balanceOptional = balanceEntryRepository
					.findBySystemId(String.valueOf(user.getSystem_id()));
			String wallet_flag = null;
			double wallet = 0;
			double adminWallet = 0;
			BalanceEntry balanceEntry = null;
			if (balanceOptional.isPresent()) {
				balanceEntry = balanceOptional.get();
				wallet_flag = balanceEntry.getWalletFlag();
				wallet = balanceEntry.getWalletAmount();
				adminWallet = balanceEntry.getWalletAmount();

			}
			int no_of_msg = bulkSmsDTO.getSmsParts();
			if (smsRequest.getMessageType().equalsIgnoreCase("Unicode")) {
				bulkSmsDTO.setDistinct("yes");
			} else {
				String sp_msg = smsRequest.getMessage();
				unicodeMsg = Converter.getContent(sp_msg.toCharArray());
				bulkSmsDTO.setMessage(unicodeMsg);
				bulkSmsDTO.setMessageType("SpecialChar");
			}
			logger.info(bulkSessionId + " Message Type: " + bulkSmsDTO.getMessageType() + " Parts: " + no_of_msg);
			if (bulkSmsDTO.isSchedule()) {
				boolean valid_sch_time = false;
				String client_time = bulkSmsDTO.getTimestart();
				String client_gmt = bulkSmsDTO.getGmt();
				SimpleDateFormat client_formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				client_formatter.setTimeZone(TimeZone.getTimeZone(client_gmt));
				SimpleDateFormat local_formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				String schedule_time = null;
				try {
					schedule_time = local_formatter.format(client_formatter.parse(client_time));
					System.out.println(bulkSessionId + " client_gmt: " + client_gmt + " client_time: " + client_time
							+ " server_time: " + schedule_time);
					if (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(schedule_time).after(new Date())) {
						valid_sch_time = true;
					} else {
						logger.error(bulkSessionId + " Scheduled Time is before Current Time");
					}
					String server_date = schedule_time.split(" ")[0];
					String server_time = schedule_time.split(" ")[1];
					bulkSmsDTO.setDate(server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-"
							+ server_date.split("-")[0]);
					bulkSmsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);
				} catch (Exception e) {
					logger.error(bulkSessionId, e);
				}
			}
			bulkSmsDTO.setReqType("bulk");
			// ************** Making Number List *******************
			int total = 0;
			ArrayList<String> destinationList = new ArrayList<String>();
			String destination = bulkSmsDTO.getDestinationNumber();

			WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user.getSystem_id().intValue());

			if (destination != null) {
				String[] tokens;
				if (destination.contains(",")) {
					tokens = destination.split(",");
				} else {
					tokens = destination.split("\n");
				}
				total = tokens.length;
				for (String numToken : tokens) {
					// System.out.println("Number(1) : " + numToken);
					numToken = numToken.replaceAll("\\s+", ""); // Replace all the spaces in the String with empty
																// character.
					if (numToken.startsWith("+")) {
						numToken = numToken.substring(numToken.lastIndexOf("+") + 1); // Remove +
					}
					// System.out.println("Number(2) : " + numToken);
					try {
						long number = Long.parseLong(numToken);
						if (webEntry.isPrefixApply()) {
							String destination_str = String.valueOf(number);
							if (destination_str.length() < webEntry.getNumberLength()) {
								System.out.println(
										destination_str + " length is less then " + webEntry.getNumberLength());
								destination_str = webEntry.getPrefixToApply() + destination_str;
								number = Long.parseLong(destination_str);
								System.out.println("Final Number:" + number);
							}
						}
						numToken = String.valueOf(number);
						// System.out.println("Number(3) : " + numToken);
						if (!destinationList.contains(numToken)) {
							destinationList.add(numToken);
						}
					} catch (Exception ex) {
						logger.error(bulkSessionId + " Invalid Destination Found => " + numToken);
					}
				}
			}
			listInfo.setTotal(total);
			listInfo.setValidCount(destinationList.size());
			bulkSmsDTO.setDestinationList(destinationList);
			// ************** End Number List *******************
			total_msg = destinationList.size() * no_of_msg;
			if (wallet_flag.equalsIgnoreCase("yes")) {
				bulkSmsDTO.setUserMode("wallet");

				totalcost = routeService.calculateRoutingCost(user.getSystem_id().intValue(), destinationList,
						no_of_msg);

				if (destinationList.size() > 0) {
					boolean amount = false;
					if (userEntry.isAdminDepend()) {

						adminCost = routeService.calculateRoutingCost(Integer.parseInt(userEntry.getMasterId()),
								destinationList, no_of_msg);

						if ((adminWallet >= adminCost)) {
							if (wallet >= totalcost) {
								adminWallet = adminWallet - adminCost;
								balanceEntry.setWalletAmount(adminWallet);

								wallet = wallet - totalcost;
								balanceEntry.setWalletAmount(wallet);
								amount = true;
								balanceEntryRepository.save(balanceEntry);
							} else {
								logger.info(bulkSessionId + " <-- Insufficient Balance -->");
							}
						} else {
							// Insufficient Admin balance
							logger.info(bulkSessionId + " <-- Insufficient Admin(" + userEntry.getMasterId()
									+ ") Balance -->");
						}
					} else {
						if (wallet >= totalcost) {
							wallet = wallet - totalcost;
							balanceEntry.setWalletAmount(wallet);
							amount = true;
							balanceEntryRepository.save(balanceEntry);
						} else {
							// Insufficient balance
							logger.info(bulkSessionId + " <-- Insufficient Balance -->");
						}
					}
					if (amount) {
						bulkSmsDTO.setMsgCount(total_msg);
						bulkSmsDTO.setTotalCost(totalcost);
						SendSmsService service = new SendSmsService();
						String respMsgId = "";
						if (bulkSmsDTO.isSchedule()) {
							bulkSmsDTO.setTotalWalletCost(totalcost);
							String filename = service.createScheduleFile(bulkSmsDTO);
							int generated_id = 0;
							if (filename != null) {

								generated_id = scheduleEntryRepository.save(new ScheduleEntry(bulkSmsDTO.getSystemId(),
										bulkSmsDTO.getDate() + " " + bulkSmsDTO.getTime(), bulkSmsDTO.getGmt(),
										bulkSmsDTO.getTimestart(), IConstants.SERVER_ID, "false", filename,
										bulkSmsDTO.getRepeat(), bulkSmsDTO.getReqType(), null)).getId();
								if (generated_id > 0) {
									String today = getTodayDateFormat();
									if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
										Set<Integer> set = null;
										if (GlobalVars.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
											set = GlobalVars.ScheduledBatches.get(bulkSmsDTO.getTime());
										} else {
											set = new LinkedHashSet<Integer>();
										}
										set.add(generated_id);
										GlobalVars.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
									}
									if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
										GlobalVars.RepeatedSchedules.add(generated_id);
									}
									target = IConstants.SUCCESS_KEY;
									logger.info("message.scheduleSuccess");
								} else {
									// Scheduling Error
									logger.error("error.singlescheduleError");
								}
							} else {
								// already Scheduled
								logger.error("error.duplicateSchedule");
							}
						} else {
							respMsgId = sendSingleMsg(bulkSmsDTO);

							if (respMsgId.contains("Error") || respMsgId.contains("SERVER NOT RESPONDING")) {
								// Submission Error
								if (respMsgId.contains("SERVER NOT RESPONDING")) {
									logger.error("error.hostconnection");
								} else {
									logger.error("error.smsError");
								}
							} else {
								target = IConstants.SUCCESS_KEY;
								logger.info("message.smsSuccess");
							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {

							smsResponse.setRespMsgId(respMsgId);
							smsResponse.setMsgCount(total_msg + "");
							smsResponse.setBulkListInfo(listInfo);
							smsResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
							smsResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
							logger.info(bulkSessionId + " Processed :-> Balance: " + wallet + " Cost: " + totalcost);
						} else {
							logger.info(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// insufficient balance
						logger.error("error.insufficientWallet");
					}
				} else {
					// Number File Error
					logger.error("error.novalidNumber");
				}
			} else if (wallet_flag.equalsIgnoreCase("no")) {
				bulkSmsDTO.setUserMode("credit");
				if (destinationList.size() > 0) {
					long credits = balanceEntry.getCredits();
					long adminCredit = balanceEntry.getCredits();
					boolean amount = false;
					if (userEntry.isAdminDepend()) {
						if (adminCredit >= total_msg) {
							if (credits >= total_msg) {
								adminCredit = adminCredit - total_msg;
								balanceEntry.setCredits(adminCredit);
								credits = credits - total_msg;
								balanceEntry.setCredits(credits);
								amount = true;
								balanceEntryRepository.save(balanceEntry);
							} else {
								logger.info(bulkSessionId + " <-- Insufficient Credits -->");
							}
						} else {
							logger.info(bulkSessionId + " <-- Insufficient Admin(" + userEntry.getMasterId()
									+ ") Credits -->");
						}
					} else {
						if (credits >= total_msg) {
							credits = credits - total_msg;
							balanceEntry.setCredits(credits);
							amount = true;
							balanceEntryRepository.save(balanceEntry);
						} else {
							logger.info(bulkSessionId + " <-- Insufficient Credits -->");
						}
					}
					if (amount) {
						long deductCredits = total_msg;
						bulkSmsDTO.setMsgCount(total_msg);
						SendSmsService service = new SendSmsService();
						String respMsgId = "";
						if (bulkSmsDTO.isSchedule()) {
							String filename = service.createScheduleFile(bulkSmsDTO);
							int generated_id = 0;
							if (filename != null) {

								generated_id = scheduleEntryRepository.save(new ScheduleEntry(bulkSmsDTO.getSystemId(),
										bulkSmsDTO.getDate() + " " + bulkSmsDTO.getTime(), bulkSmsDTO.getGmt(),
										bulkSmsDTO.getTimestart(), IConstants.SERVER_ID, "false", filename,
										bulkSmsDTO.getRepeat(), bulkSmsDTO.getReqType(), null)).getId();
								if (generated_id > 0) {
									String today = getTodayDateFormat();
									if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
										Set<Integer> set = null;
										if (GlobalVars.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
											set = GlobalVars.ScheduledBatches.get(bulkSmsDTO.getTime());
										} else {
											set = new LinkedHashSet<Integer>();
										}
										set.add(generated_id);
										GlobalVars.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
									}
									if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
										GlobalVars.RepeatedSchedules.add(generated_id);
									}
									target = IConstants.SUCCESS_KEY;
									logger.info("message.scheduleSuccess");
								} else {
									logger.error("error.singlescheduleError");
								}
							} else {
								logger.error("error.duplicateSchedule");
							}
						} else {
							respMsgId = sendSingleMsg(bulkSmsDTO);
							if (respMsgId.contains("Error") || respMsgId.contains("SERVER NOT RESPONDING")) {
								// Submission Error
								if (respMsgId.contains("SERVER NOT RESPONDING")) {
									logger.error("error.hostconnection");
								} else {
									logger.error("error.smsError");
								}
							} else {
								target = IConstants.SUCCESS_KEY;
								logger.info("message.smsSuccess");
							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							smsResponse.setRespMsgId(respMsgId);
							smsResponse.setMsgCount(total_msg + "");
							smsResponse.setBulkListInfo(listInfo);
							smsResponse.setCredits(Long.toString(credits));
							smsResponse.setDeductcredits(deductCredits + "");

							logger.info(
									bulkSessionId + " Processed :-> Credits: " + credits + " Deduct: " + deductCredits);
						} else {
							logger.info(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// insufficient Credits
						logger.error("error.insufficientCredit");
					}
				} else {
					// Number File Error
					logger.error("error.novalidNumber");
				}
			} else if (wallet_flag.equalsIgnoreCase("MIN")) {
				// insufficient balance
				logger.error("error.insufficientWallet");
			}
		} catch (Exception e) {
			logger.error(bulkSessionId, e.fillInStackTrace());
			logger.error("error.processError");
		}
		smsResponse.setStatus(target);
		return smsResponse;

	}

	private static boolean isValidNumber(String number) {

		String regex = "^[0-9]{10}$";
		return Pattern.matches(regex, number);
	}

	public static String getTodayDateFormat() {
		String Return = "";
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);

		month++;

		int daya = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		Return += year + "-";

		if (month < 10)
			Return += "0";

		Return += month + "-";

		if (daya < 10)
			Return += "0";

		Return += daya;

		return Return;
	}

	public String sendSingleMsg(BulkSmsDTO bulkSmsDTO) {
		logger.info(bulkSmsDTO.getSystemId() + " sendSingleMsg()" + bulkSmsDTO.getSenderId());
		UserSession userSession = null;
		// IDatabaseService dbService = HtiSmsDB.getInstance();
		// System.out.println("Source Address="+bulkSmsDTO.getSenderId()+ ",
		// String Address=bulkSmsDTO.getDestinationNumber();
		String ret = "";
		String user = bulkSmsDTO.getSystemId();
		String pwd = bulkSmsDTO.getPassword();
		// ---------------- For Summary Report --------------------
		BulkEntry bulkEntry = new BulkEntry();
		bulkEntry.setSystemId(user);
		bulkEntry.setMessageType(bulkSmsDTO.getMessageType());
		bulkEntry.setContent(bulkSmsDTO.getMessage());
		bulkEntry.setSenderId(bulkSmsDTO.getSenderId());
		bulkEntry.setReqType("Single");
		bulkEntry.setTotal(bulkSmsDTO.getDestinationList().size());
		QueueBackupExt backupExt = new QueueBackupExt();
		backupExt.setBulkEntry(bulkEntry);
		backupExt.setTotalCost(bulkSmsDTO.getTotalCost());
		backupExt.setUserMode(bulkSmsDTO.getUserMode());
		backupExt.setMsgCount(bulkSmsDTO.getMsgCount());
		backupExt.setOrigMessage(bulkSmsDTO.getOrigMessage());
		backupExt.setCampaignType(bulkSmsDTO.getCampaignType());
		try {
			// dbService.addSummaryReport(backupExt);
		} catch (Exception ex) {
			logger.error(user + " Error Adding To Summary Report: " + ex);
		}
		// ---------------- For Summary Report --------------------
		String messageType;
		String message;
		String sender;
		String destination_no;
		int ston = 5;
		int snpi = 0;
		if (bulkSmsDTO.getFrom().compareToIgnoreCase("Mobile") == 0) {
			ston = 1;
			snpi = 1;
		}
		List destination_list = bulkSmsDTO.getDestinationList();
		int loopCounter = 0;
		int totalCounter = 0;
		try {
			userSession = getUserSession(user, pwd);
			while (!destination_list.isEmpty()) {
				destination_no = (String) destination_list.remove(0);
				SubmitSM msg = new SubmitSM();
				sender = bulkSmsDTO.getSenderId();
				messageType = bulkSmsDTO.getMessageType();
				message = bulkSmsDTO.getMessage();
				if (messageType.equalsIgnoreCase("Unicode") && ((String) (message)).length() > 280) {
					ret += concateUnicodeMessage(userSession, message, destination_no, sender, ston, snpi,
							bulkSmsDTO.getExpiryHour(), bulkSmsDTO.getPeId(), bulkSmsDTO.getTemplateId(),
							bulkSmsDTO.getTelemarketerId());
				} else if (messageType.equalsIgnoreCase("SpecialChar") && (message.length() > 160)) {
					ret += ConSpecialChar(userSession, message, destination_no, sender, ston, snpi,
							bulkSmsDTO.getExpiryHour(), bulkSmsDTO.getPeId(), bulkSmsDTO.getTemplateId(),
							bulkSmsDTO.getTelemarketerId());
				} else {
					try {
						commandid = userSession.getCommandStatus();
						if (commandid == Data.ESME_ROK) {
							session = userSession.getSession();
							if (messageType.equalsIgnoreCase("SpecialChar")) {
								msg.setShortMessage(message, "ISO8859_1");
								msg.setDataCoding((byte) 0);
								msg.setEsmClass((byte) 0);
							}
							if (messageType.equalsIgnoreCase("Unicode")) {
								message = Converter.getUnicode(message.toCharArray());
								System.out.println("**************Single Unicode Message*******************");
								msg.setShortMessage(message, Data.ENC_UTF16_BE);
								msg.setDataCoding((byte) 8);
							}
							msg.setRegisteredDelivery((byte) 1);
							msg.setSourceAddr((byte) ston, (byte) snpi, sender);
							msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
							if (bulkSmsDTO.getPeId() != null && bulkSmsDTO.getPeId().length() > 0) {
								System.out.println("Optional Params: " + bulkSmsDTO.getPeId() + " "
										+ bulkSmsDTO.getTemplateId() + " " + bulkSmsDTO.getTelemarketerId());
								msg.setExtraOptional((short) 0x1400, new ByteBuffer(bulkSmsDTO.getPeId().getBytes()));
							}
							if (bulkSmsDTO.getTemplateId() != null && bulkSmsDTO.getTemplateId().length() > 0) {
								msg.setExtraOptional((short) 0x1401,
										new ByteBuffer(bulkSmsDTO.getTemplateId().getBytes()));
							}
							if (bulkSmsDTO.getTelemarketerId() != null && bulkSmsDTO.getTelemarketerId().length() > 0) {
								msg.setExtraOptional((short) 0x1402,
										new ByteBuffer(bulkSmsDTO.getTelemarketerId().getBytes()));
							}
							long expiry_hours = bulkSmsDTO.getExpiryHour();
							if (expiry_hours > 0) {
								String setExpiry = "0000";
								long seconds = expiry_hours * 3600;
								long days = seconds / 86400;
								if (days > 0) {
									if (days < 10) {
										setExpiry += "0" + days;
									} else {
										setExpiry += days;
									}
								} else {
									setExpiry += "00";
								}
								long remain = seconds % 86400;
								if (remain > 0) {
									long hours = remain / 3600;
									if (hours > 0) {
										if (hours < 10) {
											setExpiry += "0" + hours;
										} else {
											setExpiry += hours;
										}
									} else {
										setExpiry += "00";
									}
								} else {
									setExpiry += "00";
								}
								setExpiry = setExpiry + "0000000R";
								try {
									msg.setValidityPeriod(setExpiry);
								} catch (WrongDateFormatException ex) {
									// nothing
									// to do
								}
							}
							SubmitSMResp submitResponse = null;
							try {
								submitResponse = session.submit(msg);
							} catch (Exception e) {
								logger.error(user + " Exception on Submit[" + destination_no + "] : " + e);
							}
							if (submitResponse != null) {
								// ret += submitResponse.getMessageId() + "\n";
								if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
									ret += submitResponse.getMessageId() + "\n";
									System.out.println(user + " Message submitted. Status="
											+ submitResponse.getCommandStatus() + " < " + destination_no);
								} else {
									if (submitResponse.getCommandStatus() == 1035) {
										logger.error(user + " Submit failed < Insufficient balance:" + destination_no
												+ " >");
										ret += "SubmitError: Insufficient balance\n";
										userSession.setCommandStatus(submitResponse.getCommandStatus());
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
										logger.error(user + " Submit failed < Invalid Message Length:" + destination_no
												+ " >");
										ret += "SubmitError: Invalid Message Length\n";
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
										logger.error(
												user + " Submit failed < Invalid Destination:" + destination_no + " >");
										ret += "SubmitError: Invalid Destination[" + destination_no + "]\n";
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
										logger.error(user + " Submit failed < Invalid SourceAddress:" + sender + " >");
										ret += "SubmitError: Invalid SourceAddress\n";
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
										logger.error(
												user + " Submit failed < Account Expired:" + destination_no + " >");
										ret += "SubmitError: Account Expired\n";
										userSession.setCommandStatus(submitResponse.getCommandStatus());
									} else {
										ret += "SubmitError: " + submitResponse.getCommandStatus() + "\n";
										logger.error(user + " Submit failed < " + submitResponse.getCommandStatus()
												+ ":" + destination_no + " >");
									}
								}
							} else {
								ret += "Submit Failed\n";
								logger.error(user + " Submit failed < No Response: " + destination_no + " >");
							}
						} else {
							if (commandid == 1035) {
								ret += "Bind Error: Insufficient balance\n";
							} else if (commandid == Data.ESME_RINVEXPIRY) {
								ret += "Bind Error: Account Expired\n";
							} else {
								ret += "Bind Error: " + commandid + "\n";
							}
						}
					} catch (Exception e) {
						ret += "Processing Error\n";
						logger.error(user, e.fillInStackTrace());
					}
				}
				totalCounter++;
				if (++loopCounter > 100) {
					logger.info(user + " Total Submitted: " + totalCounter);
					loopCounter = 0;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
					}
				}
			} // for loop end here
		} catch (Exception e) {
			ret += "Processing Error\n";
			logger.error(user, e.fillInStackTrace());
		}
		putUserSession(userSession);
		return ret;
	}

	private static synchronized UserSession getUserSession(String user, String pwd) {
		UserSession userSession = null;
		if (GlobalVars.UserSessionHandler.containsKey(user + "#" + pwd)) {
			userSession = ((SessionHandler) GlobalVars.UserSessionHandler.get(user + "#" + pwd)).getUserSession();
		} else {
			userSession = new SessionHandler(user, pwd).getUserSession();
		}
		return userSession;
	}

	public String concateUnicodeMessage(UserSession userSession, String first, String destination_no, String sender,
			int ston, int snpi, long expiry_hours, String peId, String templateId, String telemarketerId) {
		String ret = "";
		try {
			commandid = userSession.getCommandStatus();
			if (commandid == Data.ESME_ROK) {
				session = userSession.getSession();
				// int rn = 100;
				List parts = Body.getUnicodeno(first);
				// StringTokenizer stkmsg = new StringTokenizer(first1, "##");
				int nofmessage = parts.size();
				int i = 1;
				int reference_number = rand.nextInt((255 - 10) + 1) + 10;
				while (!parts.isEmpty()) {
					String msg = (String) parts.remove(0);
					msg = Converter.getUnicode(msg.toCharArray());
					ByteBuffer byteMessage = new ByteBuffer();
					byteMessage.appendByte((byte) 0x05);
					byteMessage.appendByte((byte) 0x00);
					byteMessage.appendByte((byte) 0x03);
					byteMessage.appendByte((byte) reference_number);
					byteMessage.appendByte((byte) nofmessage);
					byteMessage.appendByte((byte) i);
					byteMessage.appendString(msg, Data.ENC_UTF16_BE);
					// msg = getMessage(i, rn, nofmessage, msg);
					// String result = Concat.SendUnicodeMessage(session,
					// byteMessage, sender, destination_no, ston, snpi);
					SubmitSM sm_msg = new SubmitSM();
					sm_msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
					sm_msg.setSourceAddr((byte) ston, (byte) snpi, sender);
					sm_msg.setShortMessage(byteMessage);
					sm_msg.setEsmClass((byte) Data.SM_UDH_GSM); // Set UDHI Flag Data.SM_UDH_GSM=0x40
					sm_msg.setDataCoding((byte) 8);
					sm_msg.setRegisteredDelivery((byte) 1);
					if (peId != null && peId.length() > 0) {
						sm_msg.setExtraOptional((short) 0x1400, new ByteBuffer(peId.getBytes()));
					}
					if (templateId != null && templateId.length() > 0) {
						sm_msg.setExtraOptional((short) 0x1401, new ByteBuffer(templateId.getBytes()));
					}
					if (telemarketerId != null && telemarketerId.length() > 0) {
						sm_msg.setExtraOptional((short) 0x1402, new ByteBuffer(telemarketerId.getBytes()));
					}
					if (expiry_hours > 0) {
						String setExpiry = "0000";
						long seconds = expiry_hours * 3600;
						long days = seconds / 86400;
						if (days > 0) {
							if (days < 10) {
								setExpiry += "0" + days;
							} else {
								setExpiry += days;
							}
						} else {
							setExpiry += "00";
						}
						long remain = seconds % 86400;
						if (remain > 0) {
							long hours = remain / 3600;
							if (hours > 0) {
								if (hours < 10) {
									setExpiry += "0" + hours;
								} else {
									setExpiry += hours;
								}
							} else {
								setExpiry += "00";
							}
						} else {
							setExpiry += "00";
						}
						setExpiry = setExpiry + "0000000R";
						try {
							sm_msg.setValidityPeriod(setExpiry);
						} catch (WrongDateFormatException ex) {
							// nothing
							// to do
						}
					}
					/*
					 * sm_msg.setSarMsgRefNum((short) 100); sm_msg.setSarSegmentSeqnum((short) i);
					 * sm_msg.setSarTotalSegments((short) nofmessage);
					 */
					SubmitSMResp submitResponse = session.submit(sm_msg);
					String result = "";
					if (submitResponse != null) {
						if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
							System.out.println("Message submitted<" + destination_no + ">.Status="
									+ submitResponse.getCommandStatus());
							result = submitResponse.getMessageId();
						} else {
							System.out.println("Submit Failed <" + destination_no + ">.Status="
									+ submitResponse.getCommandStatus());
							result = "SubmitError:" + submitResponse.getCommandStatus();
						}
					} else {
						System.out.println("Submit Failed <" + destination_no + "> No Response");
						result = "SubmitError:NoResponse";
					}
					System.out.println(result);
					ret += result + "\n";
					i++;
				}
			} else {
				if (commandid == 1035) {
					ret += "Bind Error: Insufficient balance\n";
				} else if (commandid == Data.ESME_RINVEXPIRY) {
					ret += "Bind Error: Account Expired\n";
				} else {
					ret += "Bind Error: " + commandid + "\n";
				}
			}
		} catch (Exception e) {
			ret += "Processing Error\n";
			logger.error(userSession.getUsername(), e.fillInStackTrace());
		}
		return ret;
	}

	public String ConSpecialChar(UserSession userSession, String first, String destination_no, String sender, int ston,
			int snpi, long expiry_hours, String peId, String templateId, String telemarketerId) {
		String ret = "";
		try {
			commandid = userSession.getCommandStatus();
			if (commandid == Data.ESME_ROK) {
				session = userSession.getSession();
				List parts = Body.getEnglishno(first);
				int nofmessage = parts.size();
				int i = 1;
				// int rn = 100;
				int reference_number = rand.nextInt((255 - 10) + 1) + 10;
				while (!parts.isEmpty()) {
					String engmsg = (String) parts.remove(0);
					// System.out.println("Part[" + i + "]:" + engmsg);
					// String result = Concat.SendConSpecialCharMessage(session,
					// engmsg, sender, destination_no, ston, snpi, i,
					// nofmessage);
					ByteBuffer bf = new ByteBuffer();
					bf.appendBuffer(Body.getHead());
					bf.appendByte((byte) (engmsg.length() + 6));
					bf.appendBuffer(Body.getHeader(reference_number, nofmessage, i));
					bf.appendString(engmsg);
					SubmitSM msg = new SubmitSM();
					msg.setBody(bf);
					msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
					msg.setSourceAddr((byte) ston, (byte) snpi, sender);
					msg.setDataCoding((byte) 0);
					msg.setEsmClass((byte) 0x40);
					msg.setRegisteredDelivery((byte) 1);
					if (peId != null && peId.length() > 0) {
						msg.setExtraOptional((short) 0x1400, new ByteBuffer(peId.getBytes()));
					}
					if (templateId != null && templateId.length() > 0) {
						msg.setExtraOptional((short) 0x1401, new ByteBuffer(templateId.getBytes()));
					}
					if (telemarketerId != null && telemarketerId.length() > 0) {
						msg.setExtraOptional((short) 0x1402, new ByteBuffer(telemarketerId.getBytes()));
					}
					if (expiry_hours > 0) {
						String setExpiry = "0000";
						long seconds = expiry_hours * 3600;
						long days = seconds / 86400;
						if (days > 0) {
							if (days < 10) {
								setExpiry += "0" + days;
							} else {
								setExpiry += days;
							}
						} else {
							setExpiry += "00";
						}
						long remain = seconds % 86400;
						if (remain > 0) {
							long hours = remain / 3600;
							if (hours > 0) {
								if (hours < 10) {
									setExpiry += "0" + hours;
								} else {
									setExpiry += hours;
								}
							} else {
								setExpiry += "00";
							}
						} else {
							setExpiry += "00";
						}
						setExpiry = setExpiry + "0000000R";
						try {
							msg.setValidityPeriod(setExpiry);
						} catch (WrongDateFormatException ex) {
							// nothing
							// to do
						}
					}
					/*
					 * msg.setSarMsgRefNum((short) rn); msg.setSarSegmentSeqnum((short) i);
					 * msg.setSarTotalSegments((short) nofmessage);
					 */
					// resp = session.submit(msg);
					SubmitSMResp submitResponse = session.submit(msg);
					String result = "";
					if (submitResponse != null) {
						if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
							System.out.println("Message submitted<" + destination_no + ">.Status="
									+ submitResponse.getCommandStatus());
							result = submitResponse.getMessageId();
						} else {
							System.out.println("Submit Failed <" + destination_no + ">.Status="
									+ submitResponse.getCommandStatus());
							result = "SubmitError:" + submitResponse.getCommandStatus();
						}
					} else {
						System.out.println("Submit Failed <" + destination_no + "> No Response");
						result = "SubmitError:NoResponse";
					}
					System.out.println(result);
					ret += result + "\n";
					i++;
				}
			} else {
				if (commandid == 1035) {
					ret += "Bind Error: Insufficient balance\n";
				} else if (commandid == Data.ESME_RINVEXPIRY) {
					ret += "Bind Error: Account Expired\n";
				} else {
					ret += "Bind Error: " + commandid + "\n";
				}
			}
		} catch (Exception e) {
			ret += "Processing Error\n";
			System.out.println(e);
		}
		return ret;
	}

	private static synchronized void putUserSession(UserSession userSession) {
		if (GlobalVars.UserSessionHandler.containsKey(userSession.getUsername() + "#" + userSession.getPassword())) {
			((SessionHandler) GlobalVars.UserSessionHandler
					.get(userSession.getUsername() + "#" + userSession.getPassword())).putUserSession(userSession);
		}
	}

}
