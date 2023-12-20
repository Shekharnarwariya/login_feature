package com.hti.smpp.common.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.dto.BatchObject;
import com.hti.smpp.common.dto.BulkListInfo;
import com.hti.smpp.common.dto.BulkMgmtContent;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.ScheduledTimeException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.management.dto.BulkMgmtEntry;
import com.hti.smpp.common.management.repository.BulkMgmtEntryRepository;
import com.hti.smpp.common.messages.dto.BulkContentEntry;
import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.messages.dto.QueueBackupExt;
import com.hti.smpp.common.messages.dto.SummaryReport;
import com.hti.smpp.common.messages.repository.BulkEntryRepository;
import com.hti.smpp.common.messages.repository.SummaryReportRepository;
import com.hti.smpp.common.response.BulkResponse;
import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.schedule.dto.ScheduleEntry;
import com.hti.smpp.common.schedule.repository.ScheduleEntryRepository;
import com.hti.smpp.common.service.RouteDAService;
import com.hti.smpp.common.service.SendSmsService;
import com.hti.smpp.common.service.SmsService;
import com.hti.smpp.common.sms.request.BulkRequest;
import com.hti.smpp.common.sms.request.SmsRequest;
import com.hti.smpp.common.sms.session.SessionHandler;
import com.hti.smpp.common.sms.session.UserSession;
import com.hti.smpp.common.sms.util.Body;
import com.hti.smpp.common.sms.util.Converter;
import com.hti.smpp.common.sms.util.GlobalVars;
import com.hti.smpp.common.sms.util.ProgressEvent;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.BalanceEntryRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.IConstants;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.SubmitSMResp;
import com.logica.smpp.pdu.WrongDateFormatException;
import com.logica.smpp.util.ByteBuffer;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

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

	@Autowired
	private BulkMgmtEntryRepository bulkMgmtEntryRepository;

	private ProgressEvent event;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private BulkEntryRepository bulkEntryRepository;

	@Autowired
	private SummaryReportRepository summaryReportRepository;

	private Random rand = new Random();

	public SmsResponse sendSms(SmsRequest smsRequest, String username) {
		Optional<User> userOptional = userRepository.findBySystemId(username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		userOptional = userRepository.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
		}
		Optional<UserEntry> userEntryOptional = userEntryRepository.findBySystemId(user.getSystemId());
		UserEntry userEntry = null;
		if (userEntryOptional.isPresent()) {
			userEntry = userEntryOptional.get();
		}
		SmsResponse smsResponse = new SmsResponse();
		String target = IConstants.FAILURE_KEY;
		BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
		bulkSmsDTO.setClientId(userEntry.getSystemId());
		bulkSmsDTO.setSystemId(userEntry.getSystemId());
		bulkSmsDTO.setPassword(userEntry.getPassword());
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
		int total_msg = 0;
		try {
			// check wallet balance
			Optional<BalanceEntry> masterBalanceOptional = balanceEntryRepository
					.findBySystemId((userEntry.getMasterId()));
			Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findBySystemId(user.getSystemId());
			String wallet_flag = null;
			double wallet = 0;
			double adminWallet = 0;
			BalanceEntry balanceEntry = null;
			BalanceEntry masterbalance = null;
			if (balanceOptional.isPresent() && masterBalanceOptional.isPresent()) {
				masterbalance = masterBalanceOptional.get();
				balanceEntry = balanceOptional.get();
				wallet_flag = balanceEntry.getWalletFlag();
				wallet = balanceEntry.getWalletAmount();
				adminWallet = masterbalance.getWalletAmount();

			}
			int no_of_msg = bulkSmsDTO.getSmsParts();
			if (smsRequest.getMessageType().equalsIgnoreCase("Unicode")) {
				bulkSmsDTO.setMessage(UTF16(smsRequest.getMessage()));
				bulkSmsDTO.setOrigMessage(UTF16(smsRequest.getMessage()));
				bulkSmsDTO.setDistinct("yes");
			} else {
				String sp_msg = smsRequest.getMessage();
				String hexValue = getHexValue(sp_msg);
				unicodeMsg = Converter.getContent(hexValue.toCharArray());
				bulkSmsDTO.setMessage(unicodeMsg);
				bulkSmsDTO.setMessageType("SpecialChar");
				bulkSmsDTO.setOrigMessage(UTF16(smsRequest.getMessage()));
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
						throw new ScheduledTimeException(bulkSessionId + " Scheduled Time is before Current Time");
					}
					String server_date = schedule_time.split(" ")[0];
					String server_time = schedule_time.split(" ")[1];
					bulkSmsDTO.setDate(server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-"
							+ server_date.split("-")[0]);
					bulkSmsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);

				} catch (ScheduledTimeException e) {
					logger.error(bulkSessionId, e.getMessage());
					throw new ScheduledTimeException(e.getMessage());
				} catch (Exception e) {
					logger.error(bulkSessionId, e);
					throw new InternalServerException(e.getMessage());
				}
			}
			bulkSmsDTO.setReqType("bulk");
			// ************** Making Number List *******************
			int total = 0;
			ArrayList<String> destinationList = new ArrayList<String>();
			String destination = bulkSmsDTO.getDestinationNumber();
			WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user.getUserId().intValue());
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
					System.out.println("Number(2) : " + numToken);
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
						throw new InternalServerException(
								bulkSessionId + " Invalid Destination Found => " + numToken + ex.getMessage());
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
				totalcost = routeService.calculateRoutingCost(user.getUserId().intValue(), destinationList, no_of_msg);
				if (destinationList.size() > 0) {
					boolean amount = false;
					if (userEntry.isAdminDepend()) {
						Optional<User> masterOptional = userRepository.findBySystemId(userEntry.getMasterId());
						if (!masterOptional.isPresent()) {
							throw new NotFoundException("User not found with the provided username.");
						}
						adminCost = routeService.calculateRoutingCost(masterOptional.get().getUserId().intValue(),
								destinationList, no_of_msg);

						if ((adminWallet >= adminCost)) {
							if (wallet >= totalcost) {
								adminWallet = adminWallet - adminCost;
								masterbalance.setWalletAmount(adminWallet);
								balanceEntryRepository.save(masterbalance);
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
								masterbalance.setCredits(adminCredit);
								credits = credits - total_msg;
								balanceEntryRepository.save(masterbalance);
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
		} catch (NotFoundException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (ScheduledTimeException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new ScheduledTimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(bulkSessionId, e);
			throw new InternalServerException(e.getMessage());
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
			addSummaryReport(backupExt);
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

	@Override
	public BulkResponse sendBulkSms(BulkRequest bulkRequest, String username, MultipartFile destinationNumberFile,
			HttpSession session) {
		Optional<User> userOptional = userRepository.findBySystemId(username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		double totalcost = 0, adminCost = 0;// total_defcost = 0;
		String unicodeMsg = "";
		String target = IConstants.FAILURE_KEY;
		List<String> destinationList = null;
		List<String> temp_number_list = new ArrayList<String>();
		ProgressEvent progressEvent = new ProgressEvent(session);
		userOptional = userRepository.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
		}
		BulkResponse bulkResponse = new BulkResponse();
		String bulkSessionId = user.getUserId() + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

		try {

			Optional<UserEntry> userEntryOptional = userEntryRepository.findBySystemId(user.getSystemId());
			UserEntry userEntry = null;
			if (userEntryOptional.isPresent()) {
				userEntry = userEntryOptional.get();
			}

			BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
			bulkSmsDTO.setSenderId(bulkRequest.getSenderId());
			bulkSmsDTO.setSton(bulkRequest.getSton());
			bulkSmsDTO.setSnpi(bulkRequest.getSnpi());
			bulkSmsDTO.setDton(bulkRequest.getDton());
			bulkSmsDTO.setDnpi(bulkRequest.getDnpi());
			bulkSmsDTO.setMessage(bulkRequest.getMessage());
			bulkSmsDTO.setEsmClass(bulkRequest.getEsmClass());
			bulkSmsDTO.setHeader(bulkRequest.getHeader());
			bulkSmsDTO.setDcsValue(bulkRequest.getDcsValue());
			bulkSmsDTO.setFrom(bulkRequest.getFrom());
			bulkSmsDTO.setSyear(bulkRequest.getSyear());
			bulkSmsDTO.setSday(bulkRequest.getSday());
			bulkSmsDTO.setSmonth(bulkRequest.getSmonth());
			bulkSmsDTO.setHours(bulkRequest.getHours());
			bulkSmsDTO.setMinutes(bulkRequest.getMinutes());
			bulkSmsDTO.setTime(bulkRequest.getTime());
			bulkSmsDTO.setDate(bulkRequest.getDate());
			bulkSmsDTO.setGreet(bulkRequest.getGreet());
			bulkSmsDTO.setAsciiList(bulkRequest.getAsciiList());
			bulkSmsDTO.setTemp(bulkRequest.getTemp());
			bulkSmsDTO.setSmscName(bulkRequest.getSmscName());
			bulkSmsDTO.setGmt(bulkRequest.getGmt());
			bulkSmsDTO.setSmscList(bulkRequest.getSmscList());
			bulkSmsDTO.setNumberlist(bulkRequest.getNumberlist());
			bulkSmsDTO.setSmscount(bulkRequest.getSmscount());
			bulkSmsDTO.setReqType(bulkRequest.getReqType());
			bulkSmsDTO.setCustomContent(bulkRequest.isCustomContent());
			bulkSmsDTO.setTotalSmsParDay(bulkRequest.getTotalSmsParDay());
			bulkSmsDTO.setTotalNumbers(bulkRequest.getTotalNumbers());
			bulkSmsDTO.setUploadedNumbers(bulkRequest.getUploadedNumbers());
			bulkSmsDTO.setTimestart(bulkRequest.getTimestart());
			bulkSmsDTO.setDestinationNumber(bulkRequest.getDestinationNumber());
			bulkSmsDTO.setClientId(bulkRequest.getClientId());
			bulkSmsDTO.setId(bulkRequest.getId());
			bulkSmsDTO.setUser(bulkRequest.getUser());
			bulkSmsDTO.setDelay(bulkRequest.getDelay());
			bulkSmsDTO.setRepeat(bulkRequest.getRepeat());
			bulkSmsDTO.setFileName(bulkRequest.getFileName());
			bulkSmsDTO.setSchedule(bulkRequest.isSchedule());
			bulkSmsDTO.setAlert(bulkRequest.isAlert());
			bulkSmsDTO.setAllowDuplicate(bulkRequest.isAllowDuplicate());
			bulkSmsDTO.setOrigMessage(bulkRequest.getOrigMessage());
			bulkSmsDTO.setMessageType(bulkRequest.getMessageType());
			bulkSmsDTO.setSmsParts(bulkRequest.getSmsParts());
			bulkSmsDTO.setCharCount(bulkRequest.getCharCount());
			bulkSmsDTO.setCharLimit(bulkRequest.getCharLimit());
			bulkSmsDTO.setExclude(bulkRequest.getExclude());
			bulkSmsDTO.setStatus(bulkRequest.getStatus());
			bulkSmsDTO.setExpiryHour(bulkRequest.getExpiryHour());
			bulkSmsDTO.setCampaignName(bulkRequest.getCampaignName());
			bulkSmsDTO.setPeId(bulkRequest.getPeId());
			bulkSmsDTO.setTemplateId(bulkRequest.getTemplateId());
			bulkSmsDTO.setTelemarketerId(bulkRequest.getTelemarketerId());
			bulkSmsDTO.setClientId("testUser1");
			bulkSmsDTO.setSystemId("testUser1");
			bulkSmsDTO.setPassword("1");
			// String fileName = IConstants.WEBAPP_DIR + "upload" + "//" + bulkSessionId;
			// bulkSmsDTO.writeToFile(fileName);
			if (bulkSmsDTO.isSchedule()) {
				logger.info(bulkSessionId + " Bulk Schedule Request <" + destinationNumberFile.getSize() + ">");
			} else {
				logger.info(bulkSessionId + " Bulk Upload Request <" + destinationNumberFile.getSize() + ">");
			}
			// ------ merge uploaded files into a list ---------------
			Map<String, Integer> errors = new HashMap<String, Integer>();
			int invalidCount = 0;
			int total = 0;
			logger.info(bulkSessionId + " Start Processing Uploaded Files.");
			String fileMode = null;
			String fileName = destinationNumberFile.getOriginalFilename();
			logger.info(bulkSessionId + " Processing File: " + fileName);

			if (fileName.endsWith(".txt")) {
				fileMode = "txt";
			} else if (fileName.endsWith(".csv")) {
				fileMode = "csv";
			} else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
				fileMode = "xls";
			} else {
				logger.warn(bulkSessionId + " Invalid File Uploaded: " + fileName);
			}

			Set<String> excludeSet = new HashSet<>();

			if (bulkRequest.getExclude() != null && bulkRequest.getExclude().length() > 0) {
				String seperator = ",";
				if (bulkRequest.getExclude().contains(",")) {
					seperator = ",";
				} else {
					seperator = "\n";
				}
				StringTokenizer tokens = new StringTokenizer(bulkRequest.getExclude(), seperator);
				while (tokens.hasMoreTokens()) {
					String next = tokens.nextToken();
					if (next != null && next.length() > 0) {
						next = next.replaceAll("\\s+", ""); // Replace all the spaces in the String with empty
															// character.
						try {
							long num = Long.parseLong(next);
							excludeSet.add(String.valueOf(num));
						} catch (NumberFormatException ne) {
							logger.info("Invalid Exclude Number Found: " + next);
						}
					}
				}
			}
			/*
			 * try { String savedExcludeNumbers =
			 * com.hti.webems.util.MultiUtility.readExcludeNumbers(systemId); if
			 * (savedExcludeNumbers != null) { for (String excluded :
			 * savedExcludeNumbers.split("\n")) { try { long num = Long.parseLong(excluded);
			 * excludeSet.add(String.valueOf(num)); } catch (NumberFormatException ne) {
			 * System.out .println(bulkSessionId + " Invalid Exclude Number Found: " +
			 * excluded); } } } } catch (Exception ex) { System.out.println(bulkSessionId +
			 * " " + ex); }
			 */
			if (!excludeSet.isEmpty()) {
				try {
					writeExcludeNumbers(String.valueOf(user.getUserId()), String.join("\n", excludeSet));
				} catch (Exception ex) {
					System.out.println(bulkSessionId + " " + ex);
				}
			} else {
				try {
					removeExcludeNumbers(String.valueOf(user.getUserId()));
				} catch (Exception ex) {
					System.out.println(bulkSessionId + " " + ex);
				}
			}
			if (fileMode != null) {
				InputStream stream = destinationNumberFile.getInputStream();
				int file_total_counter = 0;
				if (fileMode.equalsIgnoreCase("txt")) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int bytesRead = 0;
					byte[] buffer = new byte[8192];
					while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
						baos.write(buffer, 0, bytesRead);
					}
					String data = new String(baos.toByteArray());
					StringTokenizer st = new StringTokenizer(data, "\n");
					// total = st.countTokens();
					int row = 0;
					while (st.hasMoreTokens()) {
						row++;
						String destinationNumber = st.nextToken();
						if (destinationNumber != null && destinationNumber.trim().length() > 0) {
							total++;
							file_total_counter++;
							destinationNumber = destinationNumber.replaceAll("\\s+", ""); // Replace all the spaces in
																							// the String with empty
																							// character.
							try {
								long num = Long.parseLong(destinationNumber);
								if (!excludeSet.contains(String.valueOf(num))) {
									temp_number_list.add(String.valueOf(num));
								} else {
									logger.info("<------ " + num + " Excluded ------> ");
								}
							} catch (NumberFormatException nfe) {
								int counter = 0;
								if (errors.containsKey("Invalid Destination")) {
									counter = errors.get("Invalid Destination");
								}
								errors.put("Invalid Destination", ++counter);
								logger.info("Invalid Destination Number -> " + destinationNumber);
								invalidCount++;
							}
						} else {
							int counter = 0;
							if (errors.containsKey("Empty Row")) {
								counter = errors.get("Empty Row");
							}
							errors.put("Empty Row", ++counter);
							logger.info("<-- Empty Row Found[" + row + "] --> ");
							invalidCount++;
						}
					}
				} else {
					try {
						Workbook workbook = null;
						if (destinationNumberFile.getOriginalFilename() != null
								&& destinationNumberFile.getOriginalFilename().endsWith(".xlsx")) {
							workbook = new XSSFWorkbook(stream);
						} else {
							workbook = new HSSFWorkbook(stream);
						}
						int numberOfSheets = workbook.getNumberOfSheets();
						for (int i = 0; i < numberOfSheets; i++) {
							Sheet firstSheet = workbook.getSheetAt(i);
							int total_rows = firstSheet.getPhysicalNumberOfRows();
							logger.info(destinationNumberFile.getName() + " Total Rows[" + i + "]: " + total_rows);
							if (total_rows == 0) {
								continue;
							}
							Iterator<org.apache.poi.ss.usermodel.Row> iterator = firstSheet.iterator();
							while (iterator.hasNext()) {
								org.apache.poi.ss.usermodel.Row nextRow = iterator.next();
								// logger.info(nextRow.getRowNum() + " Total Cells: " +
								// nextRow.getPhysicalNumberOfCells());
								Iterator<Cell> cellIterator = nextRow.cellIterator();
								String destination = null;
								int cell_number = 0;
								while (cellIterator.hasNext()) {
									Cell cell = cellIterator.next();
									if (cell_number > 0) {
										logger.info((nextRow.getRowNum() + 1) + " <- Invalid Column Found -> ");
										break;
									}
									total++;
									file_total_counter++;
									destination = new DataFormatter().formatCellValue(cell);
									// logger.info((nextRow.getRowNum() + 1) + " -> " + destination);
									if (destination != null && destination.length() > 0) {
										destination = destination.replaceAll("\\s+", ""); // Replace all the spaces in
																							// the String with empty
																							// character.
										try {
											long num = Long.parseLong(destination);
											if (!excludeSet.contains(String.valueOf(num))) {
												temp_number_list.add(String.valueOf(num));
											} else {
												logger.info("<------ " + num + " Excluded ------> ");
											}
										} catch (NumberFormatException nfe) {
											int counter = 0;
											if (errors.containsKey("Invalid Destination")) {
												counter = (Integer) errors.get("Invalid Destination");
											}
											errors.put("Invalid Destination", ++counter);
											logger.info("Invalid Destination Number -> " + destination);
											invalidCount++;
										}
									} else {
										int counter = 0;
										if (errors.containsKey("Empty Row")) {
											counter = (Integer) errors.get("Empty Row");
										}
										errors.put("Empty Row", ++counter);
										invalidCount++;
									}
									cell_number++;
								}
							}
							logger.info(destinationNumberFile.getName() + " Sheet[" + i + "] Processed");
						}
						// *********************************************************
					} catch (Exception ex) {
						logger.error("Parsing File: " + destinationNumberFile.getName(), ex);
					}
				}
				logger.info(destinationNumberFile.getName() + " NumberCounter: " + file_total_counter);
			}

			logger.info(bulkSessionId + " End Processing Uploaded Files. Numbers Found: " + temp_number_list.size());
			Set<String> hashSet = new HashSet<String>(temp_number_list);
			if (bulkRequest.isAllowDuplicate()) {
				destinationList = new ArrayList<String>(temp_number_list);
			} else {
				destinationList = new ArrayList<String>(hashSet);
			}
			Collections.sort(destinationList);
			BulkListInfo listInfo = new BulkListInfo();
			listInfo.setTotal(total);
			listInfo.setValidCount(destinationList.size());
			listInfo.setInvalidCount(invalidCount);
			listInfo.setErrors(errors);
			int dup = temp_number_list.size() - hashSet.size();
			listInfo.setDuplicate(dup);
			bulkSmsDTO.setDestinationList(destinationList);
			// -------------------------------------------------------
			logger.info(bulkSessionId + " Total:" + listInfo.getTotal() + " Valid:" + listInfo.getValidCount()
					+ " Invalid:" + listInfo.getInvalidCount() + " Duplicate: " + listInfo.getDuplicate()
					+ " DuplicateAllowed: " + bulkSmsDTO.isAllowDuplicate());
			// ISendSmsService sendSmsService = new SendSmsService();
			// String userExparyDate = (userSessionObject.getExpiry()).toString();
			// String adminId = userSessionObject.getMasterId();
			Optional<BalanceEntry> masterBalanceOptional = balanceEntryRepository
					.findBySystemId(userEntry.getMasterId());
			Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findBySystemId(user.getSystemId());
			String wallet_flag = null;
			double wallet = 0;
			double adminWallet = 0;
			BalanceEntry balanceEntry = null;
			BalanceEntry masterbalance = null;
			if (balanceOptional.isPresent() && masterBalanceOptional.isPresent()) {
				masterbalance = masterBalanceOptional.get();
				balanceEntry = balanceOptional.get();
				wallet_flag = balanceEntry.getWalletFlag();
				wallet = balanceEntry.getWalletAmount();
				adminWallet = masterbalance.getWalletAmount();

			}
			int no_of_msg = bulkSmsDTO.getSmsParts();
			if (bulkRequest.getMessageType().equalsIgnoreCase("Unicode")) {
				bulkSmsDTO.setDistinct("yes");
			} else {
				String sp_msg = bulkRequest.getMessage();
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
					logger.info(bulkSessionId + " client_gmt: " + client_gmt + " client_time: " + client_time
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
				if (!valid_sch_time) {

					logger.error("error.schedule.time");

					bulkResponse.setStatus(target);
					return bulkResponse;
				}
			}
			if (wallet_flag.equalsIgnoreCase("yes")) {
				bulkSmsDTO.setUserMode("wallet");
				if (destinationList.size() > 0) {
					totalcost = routeService.calculateRoutingCost(user.getUserId().intValue(), destinationList,
							no_of_msg);
					logger.info(bulkSessionId + " Balance:" + wallet + " Calculated Cost: " + totalcost);
					boolean amount = false;
					// boolean inherit = false;
					if (userEntry.isAdminDepend()) {
						adminCost = routeService.calculateRoutingCost(Integer.parseInt(userEntry.getMasterId()),
								destinationList, no_of_msg);
						logger.info(bulkSessionId + " Admin[" + userEntry.getMasterId() + "] Balance:" + adminWallet
								+ " Calculated Cost: " + adminCost);
						if ((adminWallet >= adminCost)) {
							if (wallet >= totalcost) {
								adminWallet = adminWallet - adminCost;
								masterbalance.setWalletAmount(adminWallet);
								balanceEntryRepository.save(masterbalance);
								wallet = wallet - totalcost;
								balanceEntry.setWalletAmount(wallet);
								balanceEntryRepository.save(balanceEntry);
								amount = true;
							} else {
								logger.info(bulkSessionId + " <-- Insufficient Balance -->");
							}
						} else {
							// Insufficient Admin balance
							logger.info(bulkSessionId + " <-- Insufficient Admin(" + userEntry.getMasterId()
									+ ") Balance -->");
						}
					} else {
						if (wallet > 0 && wallet >= totalcost) {
							wallet = wallet - totalcost;
							balanceEntry.setWalletAmount(wallet);
							balanceEntryRepository.save(balanceEntry);
							amount = true;
						} else {
							// Insufficient balance
							logger.info(bulkSessionId + " <-- Insufficient Balance -->");
						}
					}
					WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user.getUserId().intValue());
					if (amount) {
						// String applicationName = request.getContextPath();
						bulkSmsDTO.setMsgCount(destinationList.size() * no_of_msg);
						bulkSmsDTO.setTotalCost(totalcost);
						SendSmsService service = new SendSmsService();
						if (bulkSmsDTO.isSchedule()) {
							bulkSmsDTO.setTotalWalletCost(totalcost);
							String filename = service.createScheduleFile(bulkSmsDTO);
							int generated_id = 0;
							if (filename != null) {

								generated_id = scheduleEntryRepository
										.save(new ScheduleEntry(String.valueOf(user.getUserId()),
												bulkSmsDTO.getDate() + " " + bulkSmsDTO.getTime(), bulkSmsDTO.getGmt(),
												bulkSmsDTO.getTimestart(), IConstants.SERVER_ID, "false", filename,
												bulkSmsDTO.getRepeat(), bulkSmsDTO.getReqType(), null))
										.getId();
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
									logger.error("error.scheduleError");
								}
							} else {
								// already Scheduled
								logger.error("error.duplicateSchedule");
							}
						} else {

							String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
									user.getUserId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info("message.batchSuccess");
							} else {
								// Submission Error
								logger.error("error.batchError");
							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setBulkListInfo(listInfo);
							bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
							bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
							bulkResponse.setBulkSessionId(bulkSessionId);
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
					long adminCredit = masterbalance.getCredits();
					boolean amount = false;
					// boolean inherit = false;
					if (userEntry.isAdminDepend()) {
						if (adminCredit >= (destinationList.size() * no_of_msg)) {
							if (credits >= (destinationList.size() * no_of_msg)) {
								adminCredit = adminCredit - (destinationList.size() * no_of_msg);
								masterbalance.setCredits(adminCredit);
								balanceEntryRepository.save(masterbalance);
								credits = credits - (destinationList.size() * no_of_msg);
								balanceEntry.setCredits(credits);
								balanceEntryRepository.save(balanceEntry);
								amount = true;
							} else {
								logger.info(bulkSessionId + " <-- Insufficient Credits -->");
							}
						} else {
							logger.info(bulkSessionId + " <-- Insufficient Admin(" + userEntry.getMasterId()
									+ ") Credits -->");
						}
					} else {
						if (credits >= (destinationList.size() * no_of_msg)) {
							credits = credits - (destinationList.size() * no_of_msg);
							balanceEntry.setCredits(credits);
							balanceEntryRepository.save(balanceEntry);
							amount = true;
						} else {
							logger.info(bulkSessionId + " <-- Insufficient Credits -->");
						}
					}
					if (amount) {
						long deductCredits = destinationList.size() * no_of_msg;
						// String applicationName = request.getContextPath();
						bulkSmsDTO.setMsgCount(deductCredits);
						SendSmsService service = new SendSmsService();
						if (bulkSmsDTO.isSchedule()) {
							String filename = service.createScheduleFile(bulkSmsDTO);
							int generated_id = 0;
							if (filename != null) {

								generated_id = scheduleEntryRepository
										.save(new ScheduleEntry(String.valueOf(user.getUserId()),
												bulkSmsDTO.getDate() + " " + bulkSmsDTO.getTime(), bulkSmsDTO.getGmt(),
												bulkSmsDTO.getTimestart(), IConstants.SERVER_ID, "false", filename,
												bulkSmsDTO.getRepeat(), bulkSmsDTO.getReqType(), null))
										.getId();
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
									logger.error("error.scheduleError");
								}
							} else {
								logger.error("error.duplicateSchedule");
							}
						} else {
							WebMasterEntry webEntry = webMasterEntryRepository
									.findByUserId(user.getUserId().intValue());

							String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
									user.getUserId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info("message.batchSuccess");
							} else {
								// Submission Error
								logger.error("error.batchError");
							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setBulkListInfo(listInfo);
							bulkResponse.setCredits(Long.toString(credits));
							bulkResponse.setDeductcredits(deductCredits + "");
							bulkResponse.setBulkSessionId(bulkSessionId);
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

		return bulkResponse;
	}

	public void writeExcludeNumbers(String systemId, String content) throws Exception {
		PrintStream printStream = null;
		File dir = new File("bin//" + "numbers//exclude//");
		if (!dir.exists()) {
			if (dir.mkdir()) {
				logger.info(systemId + " Exclude Numbers Dir Created");
			} else {
				logger.error(systemId + " Exclude Numbers Dir Creation Failed");
			}
		}
		File numberfile = new File("bin//" + "numbers//exclude//" + systemId + ".txt");
		if (!numberfile.exists()) {
			try {
				if (numberfile.createNewFile()) {
					logger.info(systemId + " Exclude Numbers file Created:" + numberfile.getPath());
				} else {
					logger.info(systemId + " Exclude Numbers file Creation failed: " + numberfile.getPath());
				}
			} catch (IOException e) {
				logger.info(systemId + " Exclude Numbers file Creation IOError");
				throw new IOException();
			}
		}
		try {
			printStream = new PrintStream(new FileOutputStream(numberfile), false);
			printStream.print(content);
		} finally {
			if (printStream != null) {
				printStream.close();
			}
		}
	}

	public boolean removeExcludeNumbers(String systemId) throws Exception {
		File dir = new File("bin//" + "numbers//exclude//");
		if (dir.exists()) {
			File numberfile = new File("bin//" + "numbers//exclude//" + systemId + ".txt");
			if (numberfile.exists()) {
				return numberfile.delete();
			} else {
				logger.info(systemId + " Exclude Numbers File Not Exist");
			}
		} else {
			logger.info(systemId + " Exclude Numbers Dir Not Exist");
		}
		return false;
	}

	public void setProgressEvent(ProgressEvent progressEvent) {
		this.event = progressEvent;
	}

	public String sendBulkSms(BulkSmsDTO bulkSmsDTO, ProgressEvent progressEvent, boolean waitForApprove,
			Long System_Id) {

		String response = "";
		try {
			// bulkSmsDTO.setSystemId(userSessionObject.getSystemId());
			// bulkSmsDTO.setPassword(userSessionObject.getPassword());

			setProgressEvent(progressEvent);
			response = sendBulkMsg(bulkSmsDTO, waitForApprove, System_Id);
		} catch (Exception e) {
			logger.error(bulkSmsDTO.getSystemId(), e.fillInStackTrace());
		}
		return response;
	}

	public String sendBulkMsg(BulkSmsDTO bulkSmsDTO, boolean waitForApprove, Long System_Id) {

		logger.info(bulkSmsDTO.getSystemId() + " " + bulkSmsDTO.getReqType() + " isAlert: " + bulkSmsDTO.isAlert()
				+ " Number: " + bulkSmsDTO.getDestinationNumber());
		String user = bulkSmsDTO.getSystemId();
		// QueueBackup backupObject = null;
		BulkEntry entry = null;
		int ston = 5;
		int snpi = 0;
		String ret = "";
		int user_id = System_Id.intValue();
		WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user_id);
		if (bulkSmsDTO.getFrom().compareToIgnoreCase("Mobile") == 0) {
			ston = 1;
			snpi = 1;
		}
		int batch_id = 0;
		try {
			List<String> noList = bulkSmsDTO.getDestinationList();
			String firstNumber = (String) noList.get(0);
			if (firstNumber.indexOf(";") > 0) {
				firstNumber = firstNumber.substring(firstNumber.indexOf(";") + 1, firstNumber.length());
			}
			entry = new BulkEntry();
			entry.setCampaignName(bulkSmsDTO.getCampaignName());
			entry.setSystemId(user);
			entry.setMessageType(bulkSmsDTO.getMessageType());
			entry.setContent(bulkSmsDTO.getOrigMessage());
			entry.setSenderId(bulkSmsDTO.getSenderId());
			entry.setSton(ston);
			entry.setSnpi(snpi);
			entry.setDelay(bulkSmsDTO.getDelay());
			entry.setReqType(bulkSmsDTO.getReqType());
			entry.setFirstNumber(Long.parseLong(firstNumber));
			entry.setTotal(noList.size());
			entry.setServerId(IConstants.SERVER_ID);
			System.out.println("PeId: " + bulkSmsDTO.getPeId());
			if (bulkSmsDTO.getPeId() != null && bulkSmsDTO.getPeId().length() > 0) {
				entry.setPeId(bulkSmsDTO.getPeId());
			} else {
				entry.setPeId(null);
			}
			if (bulkSmsDTO.getTemplateId() != null && bulkSmsDTO.getTemplateId().length() > 0) {
				entry.setTemplateId(bulkSmsDTO.getTemplateId());
			} else {
				entry.setTemplateId(null);
			}
			if (bulkSmsDTO.getTelemarketerId() != null && bulkSmsDTO.getTelemarketerId().length() > 0) {
				entry.setTelemarketerId(bulkSmsDTO.getTelemarketerId());
			} else {
				entry.setTelemarketerId(null);
			}
			if (bulkSmsDTO.isAlert()) {
				Set<String> alertNumbers = new java.util.HashSet<String>();
				for (String alertNumber : bulkSmsDTO.getDestinationNumber().split(",")) {
					try {
						Long.parseLong(alertNumber);
						alertNumbers.add(alertNumber);
					} catch (Exception ex) {
						logger.info("Invalid Alert Number: " + alertNumber);
					}
				}
				if (!alertNumbers.isEmpty()) {
					entry.setAlertNumbers(String.join(",", alertNumbers));
					entry.setAlert(true);
				} else {
					logger.info("Invalid Alert Numbers: " + bulkSmsDTO.getDestinationNumber());
					entry.setAlert(false);
				}
			}
			entry.setExpiryHour(bulkSmsDTO.getExpiryHour());
			// ---------------- For Batch Content --------------------
			List<BulkContentEntry> bulk_list = new ArrayList<BulkContentEntry>();
			logger.info(bulkSmsDTO.getSystemId() + " Preparing batch content list");
			if (bulkSmsDTO.isCustomContent()) {
				for (Map.Entry<String, List<String>> map_entry : bulkSmsDTO.getMapTable().entrySet()) {
					long destination = 0;
					try {
						destination = Long.parseLong(map_entry.getKey());
						if (webEntry.isPrefixApply()) {
							String destination_str = String.valueOf(destination);
							if (destination_str.length() < webEntry.getNumberLength()) {
								System.out.println(
										destination_str + " length is less then " + webEntry.getNumberLength());
								destination_str = webEntry.getPrefixToApply() + destination_str;
								destination = Long.parseLong(destination_str);
								System.out.println("Final Number:" + destination);
							}
						}
						for (String content : map_entry.getValue()) {
							if (bulkSmsDTO.getMessageType().equalsIgnoreCase("SpecialChar")) {
								bulk_list.add(new BulkContentEntry(destination, UTF16(content), "F"));
							} else {
								bulk_list.add(new BulkContentEntry(destination, content, "F"));
							}
						}
					} catch (Exception ex) {
						logger.info("Invalid Number: " + map_entry.getKey());
					}
				}
			} else {
				for (String destination_loop : bulkSmsDTO.getDestinationList()) {
					long destination = 0;
					try {
						destination = Long.parseLong(destination_loop);
						if (webEntry.isPrefixApply()) {
							String destination_str = String.valueOf(destination);
							if (destination_str.length() < webEntry.getNumberLength()) {
								System.out.println(
										destination_str + " length is less then " + webEntry.getNumberLength());
								destination_str = webEntry.getPrefixToApply() + destination_str;
								destination = Long.parseLong(destination_str);
								System.out.println("Final Number:" + destination);
							}
						}
						if (bulkSmsDTO.getMessageType().equalsIgnoreCase("SpecialChar")) {
							bulk_list.add(new BulkContentEntry(destination, UTF16(bulkSmsDTO.getMessage()), "F"));
						} else {
							bulk_list.add(new BulkContentEntry(destination, bulkSmsDTO.getMessage(), "F"));
						}
					} catch (Exception ex) {
						logger.info("Invalid Number: " + destination_loop);
					}
				}
			}
			logger.info(bulkSmsDTO.getSystemId() + " End Preparing batch content list");
			if (waitForApprove) {
				BulkMgmtEntry bulkMgmtEntry = new BulkMgmtEntry();
				bulkMgmtEntry.setTotalCost(bulkSmsDTO.getTotalCost());
				bulkMgmtEntry.setUserMode(bulkSmsDTO.getUserMode());
				bulkMgmtEntry.setMsgCount(bulkSmsDTO.getMsgCount());
				bulkMgmtEntry.setCampaignType(bulkSmsDTO.getCampaignType());
				try {
					String usermode = bulkMgmtEntry.getUserMode();
					if (usermode.equalsIgnoreCase("credit")) {
						// logger.info("MessageCount: "+backupExt.getMsgCount());
						bulkMgmtEntry.setTotalCost(bulkMgmtEntry.getMsgCount());
					} else if (usermode.equalsIgnoreCase("wallet")) {
						// logger.info("TotalCost: "+backupExt.getTotalCost());
						String cost = new DecimalFormat("0.00000").format(bulkMgmtEntry.getTotalCost());
						bulkMgmtEntry.setTotalCost(Double.parseDouble(cost));
					} else {
						bulkMgmtEntry.setTotalCost(0);
					}
					bulkEntryRepository.save(entry);

					int wait_id = bulkMgmtEntryRepository.save(bulkMgmtEntry).getId();

					if (wait_id > 0) {
						if (event != null) {

							saveBulkMgmtContent(wait_id, bulk_list, event);
						} else {
							saveBulkMgmtContent(wait_id, bulk_list);
						}
					}
				} catch (Exception ex) {
					logger.error(user + " Error Adding Bulk Entry: " + ex);
				}
				logger.info(user + " Batch Added To Waiting For Approve.");
			} else {
				// ---------------- For Summary Report --------------------
				QueueBackupExt backupExt = new QueueBackupExt();
				backupExt.setBulkEntry(entry);
				backupExt.setTotalCost(bulkSmsDTO.getTotalCost());
				backupExt.setUserMode(bulkSmsDTO.getUserMode());
				backupExt.setMsgCount(bulkSmsDTO.getMsgCount());
				if (bulkSmsDTO.getReqType() != null && bulkSmsDTO.getReqType().equalsIgnoreCase("http")) {
					backupExt.setOrigMessage(UTF16(bulkSmsDTO.getOrigMessage()));
				} else {
					backupExt.setOrigMessage(bulkSmsDTO.getOrigMessage());
				}
				backupExt.setCampaignType(bulkSmsDTO.getCampaignType());
				// ------ add to database ------------

				batch_id = bulkEntryRepository.save(entry).getId();
				if (batch_id > 0) {
					logger.info("Entry Added: " + entry.toString());
					if (event != null) {
						saveBulkMgmtContent(batch_id, bulk_list, event);
					} else {
						saveBulkMgmtContent(batch_id, bulk_list);
					}
					try {
						addSummaryReport(backupExt);
					} catch (Exception ex) {
						logger.error(user + " Error Adding To Summary Report: " + ex);
					}
					GlobalVars.BatchQueue.put(batch_id, new BatchObject(batch_id, user, IConstants.SERVER_ID, true));
					logger.info(user + " Batch Added To Processing: " + entry.getId());
				} else {
					logger.info(user + " Entry Not Added: " + entry.toString());
				}
			}
		} catch (Exception ex) {
			ret = "Error: " + ex.getMessage();
			logger.error("Start Error: " + ex.toString() + "  -> User : " + user + " | Batch : " + batch_id);
		}
		return ret;
	}

	public String UTF16(String utf16TA) {
		byte[] byteBuff;
		StringBuffer strBuff = new StringBuffer();
		String tempS;
		try {
			utf16TA = new String(utf16TA.getBytes("UTF-16"), "UTF-16");
			if (utf16TA != null && utf16TA.compareTo("") != 0) {
				byteBuff = utf16TA.getBytes("UTF-16");
				for (int l = 0; l < byteBuff.length; l++) {
					tempS = byteToHex(byteBuff[l]);
					if (!tempS.equalsIgnoreCase("0D")) {
						strBuff.append(tempS);
					} else {
						strBuff.delete(strBuff.length() - 2, strBuff.length());
					}
				}
				utf16TA = strBuff.toString();
				utf16TA = utf16TA.substring(4, utf16TA.length());
				strBuff = null;
			}
		} catch (Exception ex) {
			System.out.println("EXCEPTION FROM UTF16 method :: " + ex);
		}
		return utf16TA;
	}

	public String byteToHex(byte data) {
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));
		return (buf.toString()).toUpperCase();
	}

	public char toHexChar(int i) {
		if ((i >= 0) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' + (i - 10));
		}
	}

	public int saveBulkMgmtContent(int batch_id, List<BulkContentEntry> list, ProgressEvent progressEvent)
			throws Exception {
		System.out.println("this is new batch id......" + batch_id);
		int insertCounter = 0;
		int upload_percent = 0;
		try {

			List<BulkMgmtContent> bulkMgmtContentList = new ArrayList<>();
			for (BulkContentEntry entry : list) {
				BulkMgmtContent content = new BulkMgmtContent();
				content.setDestination(entry.getDestination());
				content.setContent(entry.getContent());
				content.setFlag(entry.getFlag());
				bulkMgmtContentList.add(content);
			}
			createBulkMgmtContentTable(batch_id, bulkMgmtContentList);
			// List<BulkMgmtContent> savedEntities =
			// bulkMgmtContentRepository.saveAll(bulkMgmtContentList);
			// insertCounter = savedEntities.size();
			// upload_percent = (insertCounter * 100) / total;
			progressEvent.updateProgress(upload_percent);
		} catch (Exception e) {
			throw new Exception("saveBulkMgmtContent(2)", e);
		}

		return insertCounter;
	}

	public int saveBulkMgmtContent(int batch_id, List<BulkContentEntry> list) throws Exception {
		int insertCounter = 0;
		System.out.println("this is new batch id......" + batch_id);
		try {
			// db.createBulkMgmtContentTable(batch_id);

			List<BulkMgmtContent> bulkMgmtContentList = new ArrayList<>();
			for (BulkContentEntry entry : list) {
				BulkMgmtContent content = new BulkMgmtContent();
				content.setDestination(entry.getDestination());
				content.setContent(entry.getContent());
				content.setFlag(entry.getFlag());
				bulkMgmtContentList.add(content);
			}
			createBulkMgmtContentTable(batch_id, bulkMgmtContentList);
			// List<BulkMgmtContent> savedEntities =
			// bulkMgmtContentRepository.saveAll(bulkMgmtContentList);
			// insertCounter = savedEntities.size();
		} catch (Exception e) {
			throw new Exception("saveBulkMgmtContent(2)", e);
		}

		return insertCounter;
	}

	public int addSummaryReport(QueueBackupExt backupExt) {
		BulkEntry queueBackup = backupExt.getBulkEntry();
		SummaryReport summaryReport = new SummaryReport();
		summaryReport.setUsername(queueBackup.getSystemId());
		summaryReport.setSender(queueBackup.getSenderId());
		summaryReport.setMsgcount(backupExt.getMsgCount());
		String usermode = backupExt.getUserMode();
		if (usermode.equalsIgnoreCase("credit")) {
			summaryReport.setCost(String.valueOf(backupExt.getMsgCount()));
		} else if (usermode.equalsIgnoreCase("wallet")) {
			String cost = new DecimalFormat("0.00000").format(backupExt.getTotalCost());
			summaryReport.setCost(cost);
		} else {
			summaryReport.setCost("0");
		}
		summaryReport.setContent(backupExt.getOrigMessage());
		summaryReport.setUsermode(usermode);
		summaryReport.setNumbercount(queueBackup.getTotal());
		summaryReport.setReqtype(queueBackup.getReqType());
		summaryReport.setMsgtype(queueBackup.getMessageType());
		summaryReport.setCampaign_name(queueBackup.getCampaignName());
		summaryReport.setCampaign_type(backupExt.getCampaignType());

		SummaryReport savedReport = summaryReportRepository.save(summaryReport);

		return savedReport.getId();
	}

	@Transactional
	public boolean createBulkMgmtContentTable(int batchId, List<BulkMgmtContent> bulkMgmtContentList) {
		try {
			String tableName = "bulk_mgmt_content_" + batchId;
			{
				createTable(tableName);
				persistEntities(bulkMgmtContentList, tableName);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error creating table or persisting entities: {}", e.getMessage());
			return false;
		}
	}

	private void createTable(String tableName) {
		jdbcTemplate.update("CALL CreateTableIfNotExists(?)", new Object[] { tableName });

	}

	private void persistEntities(List<BulkMgmtContent> bulkMgmtContentList, String tableName) {
		for (BulkMgmtContent content : bulkMgmtContentList) {
			jdbcTemplate.update("CALL InsertDataIntoTable(?, ?, ?, ?)", content.getDestination(), content.getContent(),
					content.getFlag(), tableName);
		}
	}

	@Override
	public BulkResponse sendBulkCustome(BulkRequest bulkRequest, String username, MultipartFile destinationNumberFile,
			HttpSession session) {
		Optional<User> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		double totalcost = 0, adminCost = 0;// total_defcost = 0;
		String unicodeMsg = "";
		int no_of_msg = 0;
		String target = IConstants.FAILURE_KEY;
		ArrayList destinationList = null;
		List<String> temp_number_list = new ArrayList<String>();
		ProgressEvent progressEvent = new ProgressEvent(session);
		userOptional = userRepository.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
		}
		boolean allowDuplicate = false;
		Map errors = new HashMap();
		BulkResponse bulkResponse = new BulkResponse();
		String bulkSessionId = user.getUserId() + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		WebMasterEntry webEntry = null;
		BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
		UserEntry userEntry = null;
		try {

			Optional<UserEntry> userEntryOptional = userEntryRepository.findBySystemId(user.getSystemId());

			if (userEntryOptional.isPresent()) {
				userEntry = userEntryOptional.get();
			}

			bulkSmsDTO.setSenderId(bulkRequest.getSenderId());
			bulkSmsDTO.setSton(bulkRequest.getSton());
			bulkSmsDTO.setSnpi(bulkRequest.getSnpi());
			bulkSmsDTO.setDton(bulkRequest.getDton());
			bulkSmsDTO.setDnpi(bulkRequest.getDnpi());
			bulkSmsDTO.setMessage(bulkRequest.getMessage());
			bulkSmsDTO.setEsmClass(bulkRequest.getEsmClass());
			bulkSmsDTO.setHeader(bulkRequest.getHeader());
			bulkSmsDTO.setDcsValue(bulkRequest.getDcsValue());
			bulkSmsDTO.setFrom(bulkRequest.getFrom());
			bulkSmsDTO.setSyear(bulkRequest.getSyear());
			bulkSmsDTO.setSday(bulkRequest.getSday());
			bulkSmsDTO.setSmonth(bulkRequest.getSmonth());
			bulkSmsDTO.setHours(bulkRequest.getHours());
			bulkSmsDTO.setMinutes(bulkRequest.getMinutes());
			bulkSmsDTO.setTime(bulkRequest.getTime());
			bulkSmsDTO.setDate(bulkRequest.getDate());
			bulkSmsDTO.setGreet(bulkRequest.getGreet());
			bulkSmsDTO.setAsciiList(bulkRequest.getAsciiList());
			bulkSmsDTO.setTemp(bulkRequest.getTemp());
			bulkSmsDTO.setSmscName(bulkRequest.getSmscName());
			bulkSmsDTO.setGmt(bulkRequest.getGmt());
			bulkSmsDTO.setSmscList(bulkRequest.getSmscList());
			bulkSmsDTO.setNumberlist(bulkRequest.getNumberlist());
			bulkSmsDTO.setSmscount(bulkRequest.getSmscount());
			bulkSmsDTO.setReqType(bulkRequest.getReqType());
			bulkSmsDTO.setCustomContent(bulkRequest.isCustomContent());
			bulkSmsDTO.setTotalSmsParDay(bulkRequest.getTotalSmsParDay());
			bulkSmsDTO.setTotalNumbers(bulkRequest.getTotalNumbers());
			bulkSmsDTO.setUploadedNumbers(bulkRequest.getUploadedNumbers());
			bulkSmsDTO.setTimestart(bulkRequest.getTimestart());
			bulkSmsDTO.setDestinationNumber(bulkRequest.getDestinationNumber());
			bulkSmsDTO.setClientId(bulkRequest.getClientId());
			bulkSmsDTO.setId(bulkRequest.getId());
			bulkSmsDTO.setUser(bulkRequest.getUser());
			bulkSmsDTO.setDelay(bulkRequest.getDelay());
			bulkSmsDTO.setRepeat(bulkRequest.getRepeat());
			bulkSmsDTO.setFileName(bulkRequest.getFileName());
			bulkSmsDTO.setSchedule(bulkRequest.isSchedule());
			bulkSmsDTO.setAlert(bulkRequest.isAlert());
			bulkSmsDTO.setAllowDuplicate(bulkRequest.isAllowDuplicate());
			bulkSmsDTO.setOrigMessage(bulkRequest.getOrigMessage());
			bulkSmsDTO.setMessageType(bulkRequest.getMessageType());
			bulkSmsDTO.setSmsParts(bulkRequest.getSmsParts());
			bulkSmsDTO.setCharCount(bulkRequest.getCharCount());
			bulkSmsDTO.setCharLimit(bulkRequest.getCharLimit());
			bulkSmsDTO.setExclude(bulkRequest.getExclude());
			bulkSmsDTO.setStatus(bulkRequest.getStatus());
			bulkSmsDTO.setExpiryHour(bulkRequest.getExpiryHour());
			bulkSmsDTO.setCampaignName(bulkRequest.getCampaignName());
			bulkSmsDTO.setPeId(bulkRequest.getPeId());
			bulkSmsDTO.setTemplateId(bulkRequest.getTemplateId());
			bulkSmsDTO.setTelemarketerId(bulkRequest.getTelemarketerId());
			bulkSmsDTO.setClientId("testUser1");
			bulkSmsDTO.setSystemId("testUser1");
			bulkSmsDTO.setPassword("1");
		} catch (Exception ex) {
			System.out.println(bulkSessionId + " " + ex);
		}
		if (bulkSmsDTO.isSchedule()) {
			logger.info(bulkSessionId + " Custom[" + bulkRequest.isTracking() + "] Schedule Request <"
					+ destinationNumberFile.getName() + ">");
		} else {
			logger.info(bulkSessionId + " Custom[" + bulkRequest.isTracking() + "] Upload Request <"
					+ destinationNumberFile.getName() + ">");
		}
		BulkListInfo listInfo = new BulkListInfo();

		String exclude = bulkSmsDTO.getExclude();
		Set<String> excludeSet = new HashSet<String>();
		if (exclude != null && exclude.length() > 0) {
			String seperator = ",";
			if (exclude.contains(",")) {
				seperator = ",";
			} else {
				seperator = "\n";
			}
			StringTokenizer tokens = new StringTokenizer(exclude, seperator);
			while (tokens.hasMoreTokens()) {
				String next = tokens.nextToken();
				if (next != null && next.length() > 0) {
					next = next.replaceAll("\\s+", ""); // Replace all the spaces in the String with empty character.
					if (next.startsWith("+")) {
						next = next.substring(1, next.length());
					}
					try {
						long num = Long.parseLong(next);
						excludeSet.add(String.valueOf(num));
					} catch (NumberFormatException ne) {
						logger.info(bulkSessionId + " Invalid Exclude Number Found: " + next);
					}
				}
			}
		}
		if (!excludeSet.isEmpty()) {
			try {
				writeExcludeNumbers(String.valueOf(user.getUserId()), String.join("\n", excludeSet));
			} catch (Exception ex) {
				System.out.println(bulkSessionId + " " + ex);
			}
		} else {
			try {
				removeExcludeNumbers(String.valueOf(user.getUserId()));
			} catch (Exception ex) {
				System.out.println(bulkSessionId + " " + ex);
			}
		}
		logger.info(bulkSessionId + " Exclude Count: " + excludeSet.size());
		if (bulkSmsDTO.getMessageType().equalsIgnoreCase("7bit")) {
			String sp_msg = bulkRequest.getMessage();
			unicodeMsg = Converter.getContent(sp_msg.toCharArray());
			bulkSmsDTO.setMessage(unicodeMsg);
			bulkSmsDTO.setMessageType("SpecialChar");
		}
		String msgType = bulkSmsDTO.getMessageType();
		try {
			// String userExparyDate = (userSessionObject.getExpiry()).toString();
			// String adminId = userSessionObject.getMasterId();
			Optional<BalanceEntry> masterBalanceOptional = balanceEntryRepository
					.findByUserId(Integer.parseInt(userEntry.getMasterId()));
			Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findByUserId(user.getUserId().intValue());
			System.out.println(balanceOptional.get());
			String wallet_flag = null;
			double wallet = 0;
			double adminWallet = 0;
			BalanceEntry balanceEntry = null;
			BalanceEntry masterbalance = null;
			if (balanceOptional.isPresent() && masterBalanceOptional.isPresent()) {
				masterbalance = masterBalanceOptional.get();
				balanceEntry = balanceOptional.get();
				wallet_flag = balanceEntry.getWalletFlag();
				wallet = balanceEntry.getWalletAmount();
				adminWallet = masterbalance.getWalletAmount();

			}
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
				if (!valid_sch_time) {
					logger.error("error.schedule.time");

					bulkResponse.setStatus(target);
					return bulkResponse;
				}
			}
			// ********************Start Calculating message Length for Each Number
			// ***************
			Hashtable mapTable = new Hashtable();
			Map<String, Integer> msgLengthTable = new HashMap<String, Integer>();
			int msg_length = 0;
			int totalMsg = 0;
			// System.out.println("1====> "+new SimpleDateFormat("HH:mm:ss").format(new
			// Date()));
			String content = "";
			String fileMode = null;
			String uploadedFile = destinationNumberFile.getOriginalFilename();
			System.out.println("Filename: " + uploadedFile);

			if (uploadedFile.endsWith(".txt")) {
				fileMode = "txt";
			} else if (uploadedFile.endsWith(".csv")) {
				fileMode = "csv";
			} else if (uploadedFile.endsWith(".xls") || uploadedFile.endsWith(".xlsx")) {
				fileMode = "xls";
			}

			int total_numbers = 0;
			int invalid_count = 0;
			List<String[]> param_list = new ArrayList<>();
			InputStream inputStream = destinationNumberFile.getInputStream();
			boolean isValidFile = true;
			int column_count = 0;
			System.out.println("this is file type....." + fileMode);
			if (fileMode != null) {
				try {
					if (fileMode.equalsIgnoreCase("txt")) {
						try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
							int row = 0;
							while ((content = bufferedReader.readLine()) != null) {
								row++;
								if (row == 1) {
									StringTokenizer strToken = new StringTokenizer(content, ";");
									column_count = strToken.countTokens();
								} else {
									total_numbers++;
									if (content.contains(";")) {
										// ------------- Getting Parameters ----
										StringTokenizer strToken = new StringTokenizer(content, ";");
										String[] params = new String[strToken.countTokens()];
										int i = 0;
										while (strToken.hasMoreTokens()) {
											String param_value = strToken.nextToken();
											params[i] = param_value;
											i++;
										}
										param_list.add(params);
									} else {
										invalid_count++;
										int counter = (Integer) errors.get("Invalid Seperator");
										errors.put("Invalid Separator", ++counter);
										logger.info(
												bulkSessionId + " Invalid Separator Found [" + row + "]:" + content);
									}
								}
							}
						}
					} else if (fileMode.equalsIgnoreCase("xls")) {
						Workbook workbook;
						if (uploadedFile.endsWith(".xlsx")) {
							workbook = new XSSFWorkbook(inputStream);
						} else {
							workbook = new HSSFWorkbook(inputStream);
						}
						int numberOfSheets = workbook.getNumberOfSheets();
						for (int i = 0; i < numberOfSheets; i++) {
							Sheet firstSheet = workbook.getSheetAt(i);
							logger.info(bulkSessionId + " Sheet[" + i + "] Total Rows: "
									+ firstSheet.getPhysicalNumberOfRows());
							for (Row nextRow : firstSheet) {
								if (nextRow.getRowNum() == 0) {
									column_count = nextRow.getPhysicalNumberOfCells();
									logger.info(bulkSessionId + " Total Columns: " + column_count);
								} else {
									total_numbers++;
									String cell_value = null;
									String[] params = new String[column_count];
									int cell_number = 0;
									for (Cell cell : nextRow) {
										cell_value = new DataFormatter().formatCellValue(cell);
										if (cell_number == 0) {
											if (cell_value == null || cell_value.length() == 0) {
												System.out.println(
														nextRow.getRowNum() + " Invalid Destination: " + cell_value);
												break;
											}
										}
										if (cell.getColumnIndex() < column_count) {
											params[cell.getColumnIndex()] = cell_value;
										} else {
											logger.info(bulkSessionId + " Invalid Column[" + cell.getColumnIndex()
													+ "] -> " + cell_value);
										}
										cell_number++;
									}
									param_list.add(params);
								}
							}
							break;
						}
					}
				} catch (IOException ex) {
					logger.info(bulkSessionId, ex);
				}
			} else {
				isValidFile = false;
				logger.info(bulkSessionId + " <--- Unsupported File Format --->");
			}
			System.out.println("file is valid or not....... " + isValidFile);
			if (isValidFile) {
				// --------------------------Checking Numbers & Creating Message Content
				// ------------------
				logger.info(bulkSessionId + " Columns: " + column_count);
				String temp_msg = bulkSmsDTO.getMessage();
				String param = "param";
				Map<Integer, String> param_map = new HashMap<Integer, String>();
				if (temp_msg.contains("Param")) {
					param = "Param";
				}
				if (msgType.equalsIgnoreCase("Unicode")) {
					// temp_msg = Converter.getUTF8toHexDig(temp_msg);
					temp_msg = temp_msg.toLowerCase();
					if (column_count > 0) {
						for (int p = 1; p < column_count; p++) {
							logger.info(bulkSessionId + " Converting " + (param + p) + ": Unicode ");
							param_map.put(p, UTF16(param + p).toLowerCase());
						}
					}
				}
				logger.info(bulkSessionId + " params: " + param_map);
				int duplicate_count = 0;
				Map<String, List<String>> entry_map = new LinkedHashMap<String, List<String>>();
				int row_number = 0;
				while (!param_list.isEmpty()) {
					row_number++;
					String entries[] = (String[]) param_list.remove(0);
					System.out.println("Processing[" + row_number + "]: " + Arrays.toString(entries));
					boolean proceed = true;
					// ---- check number -------------
					String destNumber = entries[0];
					if (destNumber == null || destNumber.length() == 0) {
						logger.error("Invalid Destination[" + row_number + "]: " + destNumber);
						continue;
					}
					if (destNumber.startsWith("+")) {
						destNumber = destNumber.substring(1, destNumber.length());
					}
					destNumber = destNumber.replaceAll("\\s+", ""); // Replace all the spaces in the String with empty
																	// character.
					try {
						long num = Long.parseLong(destNumber);
						if (!excludeSet.contains(String.valueOf(num))) {
							destNumber = String.valueOf(num);
						} else {
							System.out.println("<------ " + num + " Excluded ------> ");
							proceed = false;
						}
					} catch (Exception ne) {
						invalid_count++;
						int counter = 0;
						if (errors.containsKey("Invalid Destination")) {
							counter = (Integer) errors.get("Invalid Destination");
						}
						errors.put("Invalid Destination", ++counter);
						System.out.println("Invalid Destination Number -> " + entries[0]);
						proceed = false;
						// break;
					}
					// -------------------------------
					if (proceed) {
						String msg = new String(temp_msg);
						for (int e = entries.length - 1; e > 0; e--) {
							String entry = entries[e];
							// System.out.println("param" + e + " -> " + entry);
							if (entry == null || entry.length() == 0) {
								entry = " ";
							}
							if (msgType.equalsIgnoreCase("Unicode")) {
								String encoded_value = UTF16(entry).toLowerCase();
								String encoded_param = (String) param_map.get(e);
								if (encoded_param != null) {
									try {
										msg = msg.replaceAll(encoded_param, quoteReplacement(encoded_value));
									} catch (Exception ex) {
										System.out.println(ex + " ==> " + destNumber + ": " + entry);
										System.out.println(encoded_param + " ==> " + encoded_value);
									}
								}
							} else {
								String asciilist = "";
								for (int h = 0; h < entry.length(); h++) {
									int asciiNum = entry.charAt(h);
									asciilist += asciiNum + ",";
								}
								String hex_param = getHexValue(asciilist);
								entry = Converter.getContent(hex_param.toCharArray());
								msg = msg.replaceAll("\\b" + param + e + "\\b", quoteReplacement(entry));
							}
						}
						// put to map number,list<msg>
						List<String> list = null;
						if (entry_map.containsKey(destNumber)) {
							duplicate_count++;
							if (allowDuplicate) {
								list = entry_map.get(destNumber);
							} else {
								list = new ArrayList<String>();
							}
						} else {
							list = new ArrayList<String>();
						}
						list.add(msg);
						webEntry = webMasterEntryRepository.findByUserId(user.getUserId().intValue());
						if (webEntry.isPrefixApply()) {
							if (destNumber.length() < webEntry.getNumberLength()) {
								System.out.println(destNumber + " length is less then " + webEntry.getNumberLength());
								destNumber = webEntry.getPrefixToApply() + destNumber;
							}
						}
						entry_map.put(destNumber, list);
					}
					System.out.println("End: " + Arrays.toString(entries));
				}
				System.out.println(
						bulkSessionId + " isTracking:" + bulkRequest.isTracking() + " Entries: " + entry_map.size());
				if (bulkRequest.isTracking()) {
					// --------- for tracking -------------------
					String campaign_name = bulkRequest.getCampaignName();
					System.out.println("Received Web Links: " + bulkRequest.getWeblink().length);
					List<String> web_links_list = new ArrayList<String>();
					for (String link : bulkRequest.getWeblink()) {
						if (link != null && link.length() > 0) {
							web_links_list.add(link);
						}
					}
					System.out.println("Final Web Links: " + web_links_list);
//					Map<String, String> campaign_mapping = getCampaignId(String.valueOf(user.getSystem_id()),
//							bulkSmsDTO.getSenderId(), "GWYM2", web_links_list, String.join(",", entry_map.keySet()),
//							campaign_name);
					String web_link_hex_param = null;
					if (bulkSmsDTO.getMessageType().equalsIgnoreCase("Unicode")) {
						web_link_hex_param = "005B007700650062005F006C0069006E006B005F0074007200610063006B0069006E0067005F00750072006C005D"
								.toLowerCase();
					} else {
						web_link_hex_param = getHexValue(
								"91,119,101,98,95,108,105,110,107,95,116,114,97,99,107,105,110,103,95,117,114,108,93,");
						web_link_hex_param = Converter.getContent(web_link_hex_param.toCharArray());
					}
					int number_serial = 1;
					for (Map.Entry<String, List<String>> map_entry : entry_map.entrySet()) {
						String destNumber = map_entry.getKey();
						// String appending_url = " http://1l.ae/" + campaignid + "/r=" + number_serial;
						// String msg = "";
						int msg_count_per_number = 0;
						List msgList = new ArrayList();
						for (String msg : map_entry.getValue()) {
							if (msgType.equalsIgnoreCase("Unicode")) {
								for (int i = 0; i < web_links_list.size(); i++) {
//									if (campaign_mapping.containsKey(web_links_list.get(i))) {
//										String appending_url = "http://1l.ae/"
//												+ campaign_mapping.get(web_links_list.get(i)) + "/r=" + number_serial;
//										msg = msg.replaceFirst(web_link_hex_param, UTF16(appending_url).toLowerCase());
//									}
								}
								msg_length = msg.length();
								if (msg_length > 280) {
									int rem = msg_length % 268;
									int qot = msg_length / 268;
									if (rem > 0) {
										no_of_msg = qot + 1;
									} else {
										no_of_msg = qot;
									}
								} else {
									no_of_msg = 1;
								}
							} else {
								for (int i = 0; i < web_links_list.size(); i++) {
//									if (campaign_mapping.containsKey(web_links_list.get(i))) {
//										String appending_url = "http://1l.ae/"
//												+ campaign_mapping.get(web_links_list.get(i)) + "/r=" + number_serial;
//										msg = msg.replaceFirst(web_link_hex_param, appending_url);
//									}
								}
								// System.out.println(destNumber + " " + msg);
								msg_length = msg.length();
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
							totalMsg = totalMsg + no_of_msg; // FOR CREDIT CALCULATION
							msg_count_per_number += no_of_msg;
							msgList.add(msg);
						}
						mapTable.put(destNumber, msgList);
						msgLengthTable.put(destNumber, msg_count_per_number); // FOR WALLET CALCULATION
						number_serial++;
					}
					// ------------------------------------------
				} else {
					for (Map.Entry<String, List<String>> map_entry : entry_map.entrySet()) {
						String destNumber = map_entry.getKey();
						int msg_count_per_number = 0;
						List msgList = new ArrayList();
						for (String msg : map_entry.getValue()) {
							if (msgType.equalsIgnoreCase("Unicode")) {
								msg_length = msg.length();
								if (msg_length > 280) {
									int rem = msg_length % 268;
									int qot = msg_length / 268;
									if (rem > 0) {
										no_of_msg = qot + 1;
									} else {
										no_of_msg = qot;
									}
								} else {
									no_of_msg = 1;
								}
							} else {
								msg_length = msg.length();
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
							totalMsg = totalMsg + no_of_msg; // FOR CREDIT CALCULATION
							msg_count_per_number += no_of_msg;
							msgList.add(msg);
						}
						mapTable.put(destNumber, msgList);
						msgLengthTable.put(destNumber, msg_count_per_number); // FOR WALLET CALCULATION
					}
				}
				// logger.info(bulkSessionId + " mapping: " + mapTable);
				logger.info(bulkSessionId + " numbers: " + msgLengthTable.keySet().size());
				if (!msgLengthTable.isEmpty()) {
					destinationList = new ArrayList(msgLengthTable.keySet());
					listInfo.setTotal(total_numbers);
					listInfo.setValidCount(msgLengthTable.keySet().size());
					listInfo.setDuplicate(duplicate_count);
					listInfo.setInvalidCount(invalid_count);
					listInfo.setErrors(errors);
					// ----------------------------------------------------------------------
					if (msgType.compareToIgnoreCase("Unicode") == 0) {
						bulkSmsDTO.setDistinct("yes");
					}
					bulkSmsDTO.setDestinationList(destinationList);
					System.out.println("map table ...." + mapTable);
					bulkSmsDTO.setMapTable(mapTable);
					// ArrayList destinationList = bulkSmsDTO.getDestinationList(listInfo);
					// ********************Calculating message Length for Each Number
					// ***************
					// **********************************Start Wallet
					// Calculation*********************
					if (wallet_flag.equalsIgnoreCase("yes")) {
						bulkSmsDTO.setUserMode("wallet");

						totalcost = routeService.calculateRoutingCost(user.getUserId().intValue(), msgLengthTable);
						logger.info(bulkSessionId + " Balance:" + wallet + " Calculated Cost: " + totalcost);
						boolean amount = false;
						if (userEntry.isAdminDepend()) {
							adminCost = routeService.calculateRoutingCost(Integer.parseInt(userEntry.getMasterId()),
									msgLengthTable);
							logger.info(bulkSessionId + " Admin[" + Integer.parseInt(userEntry.getMasterId())
									+ "] Balance:" + adminWallet + " Calculated Cost: " + adminCost);
							if ((adminWallet >= adminCost)) {
								if (wallet >= totalcost) {
									adminWallet = adminWallet - adminCost;
									masterbalance.setWalletAmount(adminWallet);
									wallet = wallet - totalcost;
									balanceEntry.setWalletAmount(wallet);
									amount = true;
									balanceEntryRepository.save(masterbalance);
									balanceEntryRepository.save(balanceEntry);
								} else {
									logger.info(bulkSessionId + " <-- Insufficient Balance --> " + wallet);
								}
							} else {
								// Insufficient Admin balance
								logger.info(bulkSessionId + " <-- Insufficient Admin("
										+ Integer.parseInt(userEntry.getMasterId()) + ") Balance --> " + adminWallet);
							}
						} else {
							if (wallet >= totalcost) {
								wallet = wallet - totalcost;
								balanceEntry.setWalletAmount(wallet);
								balanceEntryRepository.save(balanceEntry);
								amount = true;
							} else {
								// Insufficient balance
								logger.info(bulkSessionId + " <-- Insufficient Balance --> " + wallet);
							}
						}
						if (amount) {
							// String applicationName = request.getContextPath();
							bulkSmsDTO.setMsgCount(totalMsg);
							bulkSmsDTO.setTotalCost(totalcost);
							if (bulkSmsDTO.isSchedule()) {
								bulkSmsDTO.setTotalWalletCost(totalcost);
								String filename = createScheduleFile(bulkSmsDTO);
								int generated_id = 0;
								if (filename != null) {
									generated_id = scheduleEntryRepository.save(new ScheduleEntry(
											bulkSmsDTO.getSystemId(), bulkSmsDTO.getDate() + " " + bulkSmsDTO.getTime(),
											bulkSmsDTO.getGmt(), bulkSmsDTO.getTimestart(), IConstants.SERVER_ID,
											"false", filename, bulkSmsDTO.getRepeat(), bulkSmsDTO.getReqType(), null))
											.getId();
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
										logger.error("error.scheduleError");
									}
								} else {
									// already Scheduled
									logger.error("error.duplicateSchedule");
								}
							} else {
								String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
										user.getUserId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									logger.info("message.batchSuccess");
								} else {
									// Submission Error
									logger.error("error.batchError");
								}
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
								bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
								bulkResponse.setBulkSessionId(bulkSessionId);

								logger.info(
										bulkSessionId + " Processed :-> Balance: " + wallet + " Cost: " + totalcost);
							} else {
								logger.info(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							// insufficient balance
							logger.error("error.insufficientWallet");

						}
					} // ******************************End Wallet Calculation*********************
						// ******************************Start Credit Calculation*********************
					else if (wallet_flag.equalsIgnoreCase("no")) {
						bulkSmsDTO.setUserMode("credit");
						long credits = balanceEntry.getCredits();
						long adminCredit = masterbalance.getCredits();
						boolean amount = false;
						if (userEntry.isAdminDepend()) {
							if (adminCredit >= totalMsg) {
								if (credits >= totalMsg) {
									adminCredit = adminCredit - totalMsg;
									masterbalance.setCredits(adminCredit);
									credits = credits - totalMsg;
									balanceEntry.setCredits(credits);
									amount = true;
									balanceEntryRepository.save(balanceEntry);
									balanceEntryRepository.save(masterbalance);
								} else {
									System.out.println(user.getUserId() + " <-- Insufficient Credits -->");
								}
							} else {
								System.out.println(user.getUserId() + " <-- Insufficient Admin("
										+ userEntry.getMasterId() + ") Credits -->");
							}
						} else {
							if (credits >= totalMsg) {
								credits = credits - totalMsg;
								balanceEntry.setCredits(credits);
								balanceEntryRepository.save(balanceEntry);
								amount = true;
							} else {
								System.out.println(user.getUserId() + " <-- Insufficient Credits -->");
							}
						}
						if (amount) {
							long deductCredits = totalMsg;
							// String applicationName = request.getContextPath();
							bulkSmsDTO.setMsgCount(deductCredits);
							if (bulkSmsDTO.isSchedule()) {
								String filename = createScheduleFile(bulkSmsDTO);
								int generated_id = 0;
								if (filename != null) {
									generated_id = scheduleEntryRepository.save(new ScheduleEntry(
											bulkSmsDTO.getSystemId(), bulkSmsDTO.getDate() + " " + bulkSmsDTO.getTime(),
											bulkSmsDTO.getGmt(), bulkSmsDTO.getTimestart(), IConstants.SERVER_ID,
											"false", filename, bulkSmsDTO.getRepeat(), bulkSmsDTO.getReqType(), null))
											.getId();
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
										logger.error("error.scheduleError");
									}
								} else {
									logger.error("error.duplicateSchedule");
								}
							} else {
								String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
										user.getUserId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									logger.info("message.batchSuccess");
								} else {
									// Submission Error
									logger.error("error.batchError");
								}
							}

							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {

								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(Long.toString(credits));
								bulkResponse.setDeductcredits(deductCredits + "");
								bulkResponse.setBulkSessionId(bulkSessionId);
								logger.info(bulkSessionId + " Processed :-> Credits: " + credits + " Deduct: "
										+ deductCredits);
							} else {
								logger.info(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							// insufficient Credits
							logger.error("error.insufficientCredit");
						}
					} else if (wallet_flag.equalsIgnoreCase("MIN")) {
						// insufficient balance
						logger.error("error.insufficientWallet");
					}
				} else {
					logger.info(bulkSessionId + "<-- No Valid Number Found --> ");
					logger.error("error.novalidNumber");
					// No valid Number Found
				}
			} else {
				logger.error("error.fileFormat");
				// invalid File
			}
		}

		catch (Exception e) {
			logger.error(bulkSessionId, e.fillInStackTrace());
			logger.error("error.processError");
		}
		bulkResponse.setStatus(target);
		return bulkResponse;
	}

	public static String quoteReplacement(String s) {
		if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))
			return s;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\' || c == '$') {
				sb.append('\\');
			}
			sb.append(c);
		}
		return sb.toString();
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
				String hexv = (String) GlobalVars.hashTabOne.get(character);
				if (hexv != null) {
					HexMessage += hexv;
				} else {
					HexMessage += "";
				}
			}
		}
		return HexMessage;
	}

	private Map<String, String> getCampaignId(String username, String sender, String gateway, List<String> links,
			String numbers, String campaign) throws IOException {
		Map<String, String> result_map = new HashMap<String, String>();
		JSONObject jsonobj = new JSONObject();
		jsonobj.put("url", "http://broadnet.me/");
		jsonobj.put("user", username);
		jsonobj.put("sender", sender);
		jsonobj.put("Cname", campaign);
		jsonobj.put("Gateway", gateway);
		jsonobj.put("mobiles", numbers.split(","));
		JSONArray linkObj = new JSONArray();
		for (String link : links) {
			linkObj.put(link);
		}
		jsonobj.put("link", linkObj);
		logger.info("Posting Tracking Details: " + jsonobj.toString());
		URL url = new URL("http://1l.ae/broadnetimport.php");
		URLConnection con = url.openConnection();
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		con.setDefaultUseCaches(false);
		con.setRequestProperty("Content-Type", "text");
		// ****************** Submitting To Server ********************
		OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
		// String msgcontent = URLEncoder.encode(jsonobj.toString(), "UTF-8");
		writer.write(jsonobj.toString());
		writer.flush();
		writer.close();
		// ****************** Reading the response From Server ********************
		InputStreamReader reader = new InputStreamReader(con.getInputStream());
		StringBuffer buf = new StringBuffer();
		char[] cbuf = new char[2048];
		int num;
		while (-1 != (num = reader.read(cbuf))) {
			buf.append(cbuf, 0, num);
		}
		if (links.size() == 1) {
			logger.info("Response For CampaignId: " + buf.toString());
			result_map.put(links.get(0), buf.toString());
		} else {
			JSONObject resultJson = new JSONObject(buf.toString());
			logger.info("Response For CampaignId: " + resultJson.toString());
			JSONArray jsonArray = (JSONArray) resultJson.get("Response");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				result_map.put(String.valueOf(jsonObject.get("Link")), String.valueOf(jsonObject.get("ID")));
			}
		}
		return result_map;
	}

	public String createScheduleFile(BulkSmsDTO bulkDTO) {
		String filename = null;
		File f = null;
		FileOutputStream fin = null;
		ObjectOutputStream fobj = null;
		bulkDTO.setCampaignType("Scheduled");
		System.out.println("Org: " + bulkDTO.getOrigMessage());
		try {
			filename = bulkDTO.getSystemId() + "_" + new SimpleDateFormat("ddMMyyyyHHmmssSSS").format(new Date());
			f = new File(IConstants.SCHEDULE_DIR + filename);
			if (!f.exists()) {
				fin = new FileOutputStream(f);
				fobj = new ObjectOutputStream(fin);
				fobj.writeObject(bulkDTO);
				logger.info(bulkDTO.getSystemId() + ":" + f.getName() + " Schedule Created");
			} else {
				filename = null;
				logger.error(bulkDTO.getSystemId() + ":" + f.getName() + " Schedule Exist");
			}

		} catch (IOException e) {
			filename = null;
			logger.error(bulkDTO.getSystemId(), e.fillInStackTrace());
		} finally {
			if (fobj != null) {
				try {
					fobj.close();
				} catch (IOException ex) {
					fobj = null;
				}
			}
		}
		return filename;
	}

}
