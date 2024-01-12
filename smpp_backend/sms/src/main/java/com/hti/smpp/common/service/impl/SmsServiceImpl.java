package com.hti.smpp.common.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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

import org.apache.commons.lang.ArrayUtils;
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
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.contacts.dto.GroupDataEntry;
import com.hti.smpp.common.contacts.repository.GroupDataEntryRepository;
import com.hti.smpp.common.dto.BulkMgmtContent;
import com.hti.smpp.common.dto.SearchCriteria;
import com.hti.smpp.common.exception.InsufficientBalanceException;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.InvalidPropertyException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.ScheduledTimeException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.management.dto.BulkManagementEntity;
import com.hti.smpp.common.management.dto.BulkMgmtEntry;
import com.hti.smpp.common.management.repository.BulkMgmtEntryRepository;
import com.hti.smpp.common.messages.dto.BulkContentEntry;
import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.messages.dto.BulkListInfo;
import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.messages.dto.QueueBackupExt;
import com.hti.smpp.common.messages.dto.SummaryReport;
import com.hti.smpp.common.messages.repository.BulkEntryRepository;
import com.hti.smpp.common.messages.repository.SummaryReportRepository;
import com.hti.smpp.common.request.BulkContactRequest;
import com.hti.smpp.common.request.BulkRequest;
import com.hti.smpp.common.request.SmsRequest;
import com.hti.smpp.common.response.BulkResponse;
import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.schedule.dto.ScheduleEntry;
import com.hti.smpp.common.schedule.repository.ScheduleEntryRepository;
import com.hti.smpp.common.service.RouteDAService;
import com.hti.smpp.common.service.SendSmsService;
import com.hti.smpp.common.service.SmsService;
import com.hti.smpp.common.session.SessionHandler;
import com.hti.smpp.common.session.UserSession;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.DriverInfo;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.BalanceEntryRepository;
import com.hti.smpp.common.user.repository.DriverInfoRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.BatchObject;
import com.hti.smpp.common.util.Body;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.GlobalVarsSms;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.PasswordConverter;
import com.hti.smpp.common.util.ProgressEvent;
import com.hti.smpp.common.util.SevenBitChar;
import com.hti.smpp.common.util.SmsConverter;
import com.hti.smpp.common.util.Validation;
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

	@Autowired
	private DriverInfoRepository driverInfoRepository;

	@Autowired
	private GroupDataEntryRepository groupDataEntryRepository;

	public SmsResponse sendSms(SmsRequest smsRequest, String username) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		DriverInfo driverInfo = null;
		Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(userEntry.getId());
		if (OptionalDriverInfo.isPresent()) {
			driverInfo = OptionalDriverInfo.get();
		}
		SmsResponse smsResponse = new SmsResponse();
		String target = IConstants.FAILURE_KEY;
		BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
		bulkSmsDTO.setClientId(userEntry.getSystemId());
		bulkSmsDTO.setSystemId(userEntry.getSystemId());
		bulkSmsDTO.setPassword(new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
		bulkSmsDTO.setSchedule(smsRequest.isSchedule());
		bulkSmsDTO.setTimestart("");
		String bulkSessionId = bulkSmsDTO.getSystemId() + "_" + Long.toString(System.currentTimeMillis());
		bulkSmsDTO.setSenderId(smsRequest.getSenderId());
		bulkSmsDTO.setFrom(smsRequest.getFrom());
		List<String> list = new ArrayList<String>();
		String[] split = smsRequest.getDestinationNumber().split(",");
		// VALIDATE AND PUT TO ARRAYLIST
		for (String number : split) {
			if (Validation.isValidNumber(number)) {
				list.add(number);
			}
		}
		bulkSmsDTO.setDestinationList(list);
		bulkSmsDTO.setMessageType(smsRequest.getMessageType());
		bulkSmsDTO.setMessage(smsRequest.getMessage());
		bulkSmsDTO.setDestinationNumber(smsRequest.getDestinationNumber());
		bulkSmsDTO.setCharCount(smsRequest.getCharCount());
		bulkSmsDTO.setExpiryHour(smsRequest.getExpiryHour());
		bulkSmsDTO.setCharLimit(smsRequest.getCharLimit());
		bulkSmsDTO.setSmsParts(smsRequest.getSmsParts());

		if (smsRequest.isSchedule()) {
			bulkSmsDTO.setTime(smsRequest.getTime());
			bulkSmsDTO.setRepeat(smsRequest.getRepeat());
			bulkSmsDTO.setGmt(smsRequest.getGmt());
		}
		if (smsRequest.getPeId() != null)
			bulkSmsDTO.setPeId(smsRequest.getPeId());
		if (smsRequest.getTelemarketerId() != null)
			bulkSmsDTO.setTelemarketerId(smsRequest.getTelemarketerId());
		if (smsRequest.getTemplateId() != null)
			bulkSmsDTO.setTemplateId(smsRequest.getTemplateId());

		if (smsRequest.getCampaignName() != null)
			bulkSmsDTO.setCampaignName(smsRequest.getCampaignName());

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
			Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findBySystemId(userEntry.getSystemId());
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
				unicodeMsg = SmsConverter.getContent(hexValue.toCharArray());
				bulkSmsDTO.setMessage(unicodeMsg);
				bulkSmsDTO.setMessageType("SpecialChar");
				bulkSmsDTO.setOrigMessage(UTF16(smsRequest.getMessage()));
			}
			String client_time = null;
			String server_date = null;
			logger.info(bulkSessionId + " Message Type: " + bulkSmsDTO.getMessageType() + " Parts: " + no_of_msg);
			if (bulkSmsDTO.isSchedule()) {
				boolean valid_sch_time = false;
				client_time = bulkSmsDTO.getTime();
				System.out.println("this is client time " + bulkSmsDTO.getTime());
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
					server_date = schedule_time.split(" ")[0];
					String server_time = schedule_time.split(" ")[1];
					bulkSmsDTO.setDate(server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-"
							+ server_date.split("-")[0]);
					bulkSmsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);
					System.out.println("this is server time " + bulkSmsDTO.getTime());

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
			WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(userEntry.getId());
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
				totalcost = routeService.calculateRoutingCost(userEntry.getId(), destinationList, no_of_msg);
				if (destinationList.size() > 0) {
					boolean amount = false;
					if (userEntry.isAdminDepend()) {
						Optional<UserEntry> masterOptional = userEntryRepository
								.findBySystemId(userEntry.getMasterId());
						if (!masterOptional.isPresent()) {
							throw new NotFoundException("User not found with the provided username.");
						}
						adminCost = routeService.calculateRoutingCost(masterOptional.get().getId(), destinationList,
								no_of_msg);

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
								logger.error(bulkSessionId + " <-- Insufficient Balance -->");
								throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Balance -->");
							}
						} else {
							// Insufficient Admin balance
							logger.error(bulkSessionId + " <-- Insufficient Admin(" + userEntry.getMasterId()
									+ ") Balance -->");
							throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Admin("
									+ userEntry.getMasterId() + ") Balance -->");
						}
					} else {
						if (wallet >= totalcost) {
							wallet = wallet - totalcost;
							balanceEntry.setWalletAmount(wallet);
							amount = true;
							balanceEntryRepository.save(balanceEntry);
						} else {
							// Insufficient balance
							logger.error(bulkSessionId + " <-- Insufficient Balance -->");
							throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Balance -->");
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
								ScheduleEntry sch = new ScheduleEntry();
								sch.setClientGmt(bulkSmsDTO.getGmt());
								sch.setClientTime(client_time);
								sch.setFileName(filename);
								sch.setRepeated(bulkSmsDTO.getRepeat());
								sch.setScheduleType(bulkSmsDTO.getReqType());
								sch.setServerId(IConstants.SERVER_ID);
								sch.setServerTime(bulkSmsDTO.getTime());
								sch.setStatus("false");
								sch.setUsername(bulkSmsDTO.getSystemId());
								sch.setDate(bulkSmsDTO.getDate());
								sch.setWebId(null);
								generated_id = scheduleEntryRepository.save(sch).getId();

								if (generated_id > 0) {
									String today = Validation.getTodayDateFormat();
									if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
										Set<Integer> set = null;
										if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
											set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
										} else {
											set = new LinkedHashSet<Integer>();
										}
										set.add(generated_id);
										GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
									}
									if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
										GlobalVarsSms.RepeatedSchedules.add(generated_id);
									}
									target = IConstants.SUCCESS_KEY;
									logger.info("message scheduleSuccess.....");
								} else {
									// Scheduling Error
									logger.error("error.singlescheduleError");
									throw new ScheduledTimeException("Error in single schedule: " + bulkSessionId);
								}
							} else {
								// already Scheduled
								logger.error("error.duplicateSchedule");
								throw new ScheduledTimeException("Duplicate schedule error: " + bulkSessionId);
							}
						} else {
							respMsgId = sendSingleMsg(bulkSmsDTO);

							if (respMsgId.contains("Error") || respMsgId.contains("SERVER NOT RESPONDING")) {
								// Submission Error
								if (respMsgId.contains("SERVER NOT RESPONDING")) {
									logger.error("error.hostconnection");
									throw new InternalServerException("Error in host connection: " + bulkSessionId);

								} else {
									logger.error("error.smsError");
									throw new InternalServerException("SMS error: " + bulkSessionId);

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
						throw new InsufficientBalanceException("Insufficient wallet balance: " + bulkSessionId);
					}
				} else {
					// Number File Error
					logger.error("error.novalidNumber");
					throw new InternalServerException("No valid numbers found: " + bulkSessionId);

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
								logger.error(bulkSessionId + " <-- Insufficient Credits -->");
								throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Credits -->");
							}
						} else {
							logger.error(bulkSessionId + " <-- Insufficient Admin(" + userEntry.getMasterId()
									+ ") Credits -->");
							throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Admin("
									+ userEntry.getMasterId() + ") Credits -->");
						}
					} else {
						if (credits >= total_msg) {
							credits = credits - total_msg;
							balanceEntry.setCredits(credits);
							amount = true;
							balanceEntryRepository.save(balanceEntry);
						} else {
							logger.error(bulkSessionId + " <-- Insufficient Credits -->");
							throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Credits -->");
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

								ScheduleEntry sch = new ScheduleEntry();
								sch.setClientGmt(bulkSmsDTO.getGmt());
								sch.setClientTime(client_time);
								sch.setFileName(filename);
								sch.setRepeated(bulkSmsDTO.getRepeat());
								sch.setScheduleType(bulkSmsDTO.getReqType());
								sch.setServerId(IConstants.SERVER_ID);
								sch.setServerTime(bulkSmsDTO.getTime());
								sch.setStatus("false");
								sch.setUsername(bulkSmsDTO.getSystemId());
								sch.setWebId(null);
								sch.setDate(bulkSmsDTO.getDate());
								generated_id = scheduleEntryRepository.save(sch).getId();
								if (generated_id > 0) {
									String today = Validation.getTodayDateFormat();
									if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
										Set<Integer> set = null;
										if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
											set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
										} else {
											set = new LinkedHashSet<Integer>();
										}
										set.add(generated_id);
										GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
									}
									if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
										GlobalVarsSms.RepeatedSchedules.add(generated_id);
									}
									target = IConstants.SUCCESS_KEY;
									logger.info("message.scheduleSuccess");
								} else {
									logger.error("error.singlescheduleError");
									throw new ScheduledTimeException("Error in single schedule: " + bulkSessionId);

								}
							} else {
								logger.error("error.duplicateSchedule");
								throw new ScheduledTimeException("Duplicate schedule error: " + bulkSessionId);

							}
						} else {
							respMsgId = sendSingleMsg(bulkSmsDTO);
							if (respMsgId.contains("Error") || respMsgId.contains("SERVER NOT RESPONDING")) {
								// Submission Error
								if (respMsgId.contains("SERVER NOT RESPONDING")) {
									logger.error("error.hostconnection");
									throw new InternalServerException("Error in host connection: " + bulkSessionId);

								} else {
									logger.error("error.smsError");
									throw new InternalServerException("SMS error: " + bulkSessionId);

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
							logger.error(bulkSessionId + "<-- Process Failed --> ");
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// Insufficient Credits
						logger.error("error.insufficientCredit");
						throw new InsufficientBalanceException(
								"Insufficient credits to perform the operation" + bulkSessionId);

					}
				} else {
					// Number File Error
					logger.error("error.novalidNumber");
					throw new InternalServerException("No valid numbers found: " + bulkSessionId);
				}
			} else if (wallet_flag.equalsIgnoreCase("MIN")) {
				// insufficient balance
				logger.error("error.insufficientWallet");
				throw new InsufficientBalanceException(
						"Insufficient wallet balance for minimum transaction" + bulkSessionId);

			}
		} catch (NotFoundException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (InsufficientBalanceException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InsufficientBalanceException(e.getMessage());
		} catch (InternalServerException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InternalServerException(e.getMessage());
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

	public String sendSingleMsg(BulkSmsDTO bulkSmsDTO) {
		logger.info(bulkSmsDTO.getSystemId() + " sendSingleMsg()" + bulkSmsDTO.getSenderId());
		UserSession userSession = null;
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
			logger.error(user + " Error Adding To Summary Report: " + ex.getMessage());
			throw new InternalServerException(user + " Error Adding To Summary Report: " + ex.getMessage());
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
								message = SmsConverter.getUnicode(message.toCharArray());
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
								logger.error(user + " Exception on Submit[" + destination_no + "] : " + e.getMessage());
								throw new InternalServerException(
										user + " Exception on Submit[" + destination_no + "] : " + e.getMessage());
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
						logger.error(user, e.getMessage());
						throw new InternalServerException("username :{}" + user + e.getMessage());
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
			logger.error(user + e.getMessage());
			throw new InternalServerException("user name : {}" + user + e.getMessage());
		}
		putUserSession(userSession);
		return ret;
	}

	private static synchronized UserSession getUserSession(String user, String pwd) {
		UserSession userSession = null;
		if (GlobalVarsSms.UserSessionHandler.containsKey(user + "#" + pwd)) {
			userSession = ((SessionHandler) GlobalVarsSms.UserSessionHandler.get(user + "#" + pwd)).getUserSession();
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
					msg = SmsConverter.getUnicode(msg.toCharArray());
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
		if (GlobalVarsSms.UserSessionHandler.containsKey(userSession.getUsername() + "#" + userSession.getPassword())) {
			((SessionHandler) GlobalVarsSms.UserSessionHandler
					.get(userSession.getUsername() + "#" + userSession.getPassword())).putUserSession(userSession);
		}
	}

	@Override
	public BulkResponse sendBulkSms(BulkRequest bulkRequest, String username, List<MultipartFile> destinationNumberFile,
			HttpSession session) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		DriverInfo driverInfo = null;
		Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(userEntry.getId());
		if (OptionalDriverInfo.isPresent()) {
			driverInfo = OptionalDriverInfo.get();
		} else
			throw new NotFoundException("drive info  not found with the provided username.");
		WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(userEntry.getId());
		double totalcost = 0, adminCost = 0;// total_defcost = 0;
		String unicodeMsg = "";
		String target = IConstants.FAILURE_KEY;
		List<String> destinationList = null;
		List<String> temp_number_list = new ArrayList<String>();
		ProgressEvent progressEvent = new ProgressEvent(session);
		BulkResponse bulkResponse = new BulkResponse();
		String bulkSessionId = userEntry.getId() + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
		bulkSmsDTO.setMessage(bulkRequest.getMessage());
		bulkSmsDTO.setFrom(bulkRequest.getFrom());
		bulkSmsDTO.setSmscount(bulkRequest.getSmscount());
		bulkSmsDTO.setDelay(bulkRequest.getDelay());
		bulkSmsDTO.setSchedule(bulkRequest.isSchedule());
		bulkSmsDTO.setAlert(bulkRequest.isAlert());
		bulkSmsDTO.setAllowDuplicate(bulkRequest.isAllowDuplicate());
		bulkSmsDTO.setMessageType(bulkRequest.getMessageType());
		bulkSmsDTO.setSmsParts(bulkRequest.getSmsParts());
		bulkSmsDTO.setCharCount(bulkRequest.getCharCount());
		bulkSmsDTO.setCharLimit(bulkRequest.getCharLimit());
		bulkSmsDTO.setExclude(bulkRequest.getExclude());
		bulkSmsDTO.setExpiryHour(bulkRequest.getExpiryHour());
		bulkSmsDTO.setCampaignName(bulkRequest.getCampaignName());
		bulkSmsDTO.setClientId(userEntry.getSystemId());
		bulkSmsDTO.setSystemId(userEntry.getSystemId());
		bulkSmsDTO.setPassword(new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
		bulkSmsDTO.setSenderId(bulkRequest.getSenderId());

		if (bulkRequest.getDestinationNumber() != null)
			bulkSmsDTO.setDestinationNumber(bulkRequest.getDestinationNumber());
		if (bulkRequest.isSchedule()) {
			bulkSmsDTO.setTimestart(bulkRequest.getTimestart());
			bulkSmsDTO.setRepeat(bulkRequest.getRepeat());
			bulkSmsDTO.setGmt(bulkRequest.getGmt());
		}
		if (bulkRequest.getPeId() != null)
			bulkSmsDTO.setPeId(bulkRequest.getPeId());
		if (bulkRequest.getTelemarketerId() != null)
			bulkSmsDTO.setTelemarketerId(bulkRequest.getTelemarketerId());
		if (bulkRequest.getTemplateId() != null)
			bulkSmsDTO.setTemplateId(bulkRequest.getTemplateId());
		if (bulkRequest.isTracking()) {
			return BulkTracking(bulkRequest, bulkSmsDTO, webEntry, driverInfo, userEntry, progressEvent,
					destinationNumberFile);
		}

		else {
			try {

				if (bulkSmsDTO.isSchedule()) {
					logger.info(bulkSessionId + " Bulk Schedule Request <" + destinationNumberFile.size() + ">");
				} else {
					logger.info(bulkSessionId + " Bulk Upload Request <" + destinationNumberFile.size() + ">");
				}
				// ------ merge uploaded files into a list ---------------
				Map<String, Integer> errors = new HashMap<String, Integer>();
				int invalidCount = 0;
				int total = 0;
				logger.info(bulkSessionId + " Start Processing Uploaded Files.");
				for (MultipartFile uploaded_file : destinationNumberFile) {
					String fileName = uploaded_file.getOriginalFilename();
					logger.info(bulkSessionId + " Processing File: " + fileName);
					String fileMode = null;
					if (fileName.endsWith(".txt")) {
						fileMode = "txt";
					} else if (fileName.endsWith(".csv")) {
						fileMode = "csv";
					} else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
						fileMode = "xls";
					} else {
						logger.warn(bulkSessionId + " Invalid File Uploaded: " + fileName);
						throw new InvalidPropertyException(bulkSessionId + " Invalid File Uploaded: " + fileName);
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

					if (!excludeSet.isEmpty()) {
						try {
							writeExcludeNumbers(String.valueOf(userEntry.getId()), String.join("\n", excludeSet));
						} catch (Exception ex) {
							System.out.println(bulkSessionId + " " + ex);
						}
					} else {
						try {
							removeExcludeNumbers(String.valueOf(userEntry.getId()));
						} catch (Exception ex) {
							System.out.println(bulkSessionId + " " + ex);
						}
					}
					if (fileMode != null) {
						InputStream stream = uploaded_file.getInputStream();
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
									destinationNumber = destinationNumber.replaceAll("\\s+", ""); // Replace all the
																									// spaces
																									// in
																									// the String with
																									// empty
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
								if (uploaded_file.getOriginalFilename() != null
										&& uploaded_file.getOriginalFilename().endsWith(".xlsx")) {
									workbook = new XSSFWorkbook(stream);
								} else {
									workbook = new HSSFWorkbook(stream);
								}
								int numberOfSheets = workbook.getNumberOfSheets();
								for (int i = 0; i < numberOfSheets; i++) {
									Sheet firstSheet = workbook.getSheetAt(i);
									int total_rows = firstSheet.getPhysicalNumberOfRows();
									logger.info(uploaded_file.getName() + " Total Rows[" + i + "]: " + total_rows);
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
												destination = destination.replaceAll("\\s+", ""); // Replace all the
																									// spaces
																									// in
																									// the String with
																									// empty
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
									logger.info(uploaded_file.getName() + " Sheet[" + i + "] Processed");
								}
								// *********************************************************
							} catch (Exception ex) {
								logger.error("Parsing File: " + uploaded_file.getName(), ex.getLocalizedMessage());
								throw new InternalServerException(
										"Parsing File: " + uploaded_file.getName() + ex.getMessage());
							}
						}
						logger.info(uploaded_file.getName() + " NumberCounter: " + file_total_counter);
					}
				}
				logger.info(
						bulkSessionId + " End Processing Uploaded Files. Numbers Found: " + temp_number_list.size());
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

				Optional<BalanceEntry> masterBalanceOptional = balanceEntryRepository
						.findBySystemId(userEntry.getMasterId());
				Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findBySystemId(userEntry.getSystemId());
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
					bulkSmsDTO.setMessage(UTF16(bulkRequest.getMessage()));
					bulkSmsDTO.setOrigMessage(UTF16(bulkRequest.getMessage()));
					bulkSmsDTO.setDistinct("yes");
				} else {
					String sp_msg = bulkRequest.getMessage();
					String hexValue = getHexValue(sp_msg);
					unicodeMsg = SmsConverter.getContent(hexValue.toCharArray());
					bulkSmsDTO.setMessage(unicodeMsg);
					bulkSmsDTO.setMessageType("SpecialChar");
					bulkSmsDTO.setOrigMessage(UTF16(bulkRequest.getMessage()));
				}
				logger.info(bulkSessionId + " Message Type: " + bulkSmsDTO.getMessageType() + " Parts: " + no_of_msg);
				String client_time = null;
				if (bulkSmsDTO.isSchedule()) {
					boolean valid_sch_time = false;
					client_time = bulkSmsDTO.getTimestart();
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
						logger.error(bulkSessionId, e.getMessage());
						throw new InternalServerException("Error: getting error in parssing time with bulkSesssionId"
								+ bulkSessionId + e.getMessage());
					}
					if (!valid_sch_time) {

						logger.error("error.schedule.time");
						bulkResponse.setStatus(target);
						throw new ScheduledTimeException(
								"Error: getting error in Scheduler bulkSessionId " + bulkSessionId);
						// return bulkResponse;
					}
				}
				if (wallet_flag.equalsIgnoreCase("yes")) {
					bulkSmsDTO.setUserMode("wallet");
					if (destinationList.size() > 0) {
						totalcost = routeService.calculateRoutingCost(userEntry.getId(), destinationList, no_of_msg);
						logger.info(bulkSessionId + " Balance:" + wallet + " Calculated Cost: " + totalcost);
						boolean amount = false;
						// boolean inherit = false;
						if (userEntry.isAdminDepend()) {
							Optional<UserEntry> masterOptional = userEntryRepository
									.findBySystemId(userEntry.getMasterId());
							if (!masterOptional.isPresent()) {
								throw new NotFoundException("User not found with the provided username.");
							}
							adminCost = routeService.calculateRoutingCost(masterOptional.get().getId(), destinationList,
									no_of_msg);
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
									logger.error(bulkSessionId + " <-- Insufficient Balance -->");
									throw new InsufficientBalanceException(
											bulkSessionId + " <-- Insufficient Balance -->");
								}
							} else {
								// Insufficient Admin balance
								logger.error(bulkSessionId + " <-- Insufficient Admin(" + userEntry.getMasterId()
										+ ") Balance -->");
								throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Admin("
										+ userEntry.getMasterId() + ") Balance -->");
							}
						} else {
							if (wallet > 0 && wallet >= totalcost) {
								wallet = wallet - totalcost;
								balanceEntry.setWalletAmount(wallet);
								balanceEntryRepository.save(balanceEntry);
								amount = true;
							} else {
								// Insufficient balance
								logger.error(bulkSessionId + " <-- Insufficient Balance -->");
								throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Balance -->");

							}
						}
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

									ScheduleEntry sch = new ScheduleEntry();
									sch.setClientGmt(bulkSmsDTO.getGmt());
									sch.setClientTime(client_time);
									sch.setFileName(filename);
									sch.setRepeated(bulkSmsDTO.getRepeat());
									sch.setScheduleType(bulkSmsDTO.getReqType());
									sch.setServerId(IConstants.SERVER_ID);
									sch.setServerTime(bulkSmsDTO.getTime());
									sch.setStatus("false");
									sch.setUsername(bulkSmsDTO.getSystemId());
									sch.setDate(bulkSmsDTO.getDate());
									sch.setWebId(null);
									generated_id = scheduleEntryRepository.save(sch).getId();
									if (generated_id > 0) {
										String today = Validation.getTodayDateFormat();
										if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
											Set<Integer> set = null;
											if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
												set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
											} else {
												set = new LinkedHashSet<Integer>();
											}
											set.add(generated_id);
											GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
										}
										if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
											GlobalVarsSms.RepeatedSchedules.add(generated_id);
										}
										target = IConstants.SUCCESS_KEY;
										logger.info("message.scheduleSuccess");
									} else {
										logger.error(
												"Error: Unable to schedule the task. An error occurred while processing the schedule request.");
										throw new ScheduledTimeException(
												"Error: Unable to schedule the task. An error occurred while processing the schedule request for username "
														+ username);

									}
								} else {
									// Already Scheduled
									logger.error(
											"Error: The task is already scheduled. Duplicate schedule request received.");
									throw new ScheduledTimeException(
											"Error: The task is already scheduled. Duplicate schedule request received for username "
													+ username);
								}
							} else {

								String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
										userEntry.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									logger.info("Bulk SMS sent successfully. Message: " + value);
								} else {
									// Submission Error
									logger.error("Error sending bulk SMS. Details: " + value);
									throw new InternalServerException(
											"Error sending bulk SMS. Please check the details.");

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
								throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							logger.error(
									"Error: Insufficient balance to perform the operation. Please add funds to your wallet.");
							throw new InsufficientBalanceException(
									"Error: Insufficient balance to perform the operation. Please add funds to your wallet.");

						}
					} else {
						String errorMessage = "Error: No valid numbers found in the provided file. Please check the file and try again.";
						logger.error(errorMessage);
						throw new InternalServerException(errorMessage);
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
									String errorMessage = bulkSessionId + " Error: Insufficient Credits.";
									logger.error(errorMessage);
									throw new InsufficientBalanceException(errorMessage);
								}
							} else {
								String errorMessage = bulkSessionId + " Error: Insufficient Admin("
										+ userEntry.getMasterId() + ") Credits.";
								logger.error(errorMessage);
								throw new InsufficientBalanceException(errorMessage);
							}
						} else {
							if (credits >= (destinationList.size() * no_of_msg)) {
								credits = credits - (destinationList.size() * no_of_msg);
								balanceEntry.setCredits(credits);
								balanceEntryRepository.save(balanceEntry);
								amount = true;
							} else {
								String errorMessage = bulkSessionId + " Error: Insufficient Credits.";
								logger.info(errorMessage);
								throw new InsufficientBalanceException(errorMessage);
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

									ScheduleEntry sch = new ScheduleEntry();
									sch.setClientGmt(bulkSmsDTO.getGmt());
									sch.setClientTime(client_time);
									sch.setFileName(filename);
									sch.setRepeated(bulkSmsDTO.getRepeat());
									sch.setScheduleType(bulkSmsDTO.getReqType());
									sch.setServerId(IConstants.SERVER_ID);
									sch.setServerTime(bulkSmsDTO.getTime());
									sch.setStatus("false");
									sch.setUsername(bulkSmsDTO.getSystemId());
									sch.setDate(bulkSmsDTO.getDate());
									sch.setWebId(null);
									generated_id = scheduleEntryRepository.save(sch).getId();

									if (generated_id > 0) {
										String today = Validation.getTodayDateFormat();
										if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
											Set<Integer> set = null;
											if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
												set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
											} else {
												set = new LinkedHashSet<Integer>();
											}
											set.add(generated_id);
											GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
										}
										if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
											GlobalVarsSms.RepeatedSchedules.add(generated_id);
										}
										target = IConstants.SUCCESS_KEY;
										logger.info("message.scheduleSuccess");
									} else {
										// Schedule Error
										String errorMessage = "Error: Unable to schedule the task. An error occurred while processing the schedule request.";
										logger.error(errorMessage);
										throw new ScheduledTimeException(errorMessage);
									}
								} else {
									// Duplicate Schedule Error
									String errorMessage = "Error: The task is already scheduled. Duplicate schedule request received.";
									logger.error(errorMessage);
									throw new ScheduledTimeException(errorMessage);
								}
							} else {

								String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
										userEntry.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									logger.info("message.batchSuccess");
								} else {
									// Submission Error
									String errorMessage = "Error: Unable to process batch submission. An error occurred during the submission process.";
									logger.error(errorMessage);
									throw new InternalServerException(errorMessage);
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
								// Process Failed
								String errorMessage = bulkSessionId + " Error: The process failed.";
								logger.error(errorMessage);
								throw new InternalServerException(errorMessage);
							}
						} else {
							logger.error("Error: Insufficient Credits.");
							throw new InsufficientBalanceException("Error: Insufficient Credits.");

						}
					} else {
						// Number File Error
						String errorMessage = "Error: No valid numbers found in the provided file. Please check the file and try again.";
						logger.error(errorMessage);
						throw new InternalServerException(errorMessage);
					}
				} else if (wallet_flag.equalsIgnoreCase("MIN")) {
					logger.error("Error: Insufficient Wallet Balance.");
					throw new InsufficientBalanceException("Error: Insufficient Wallet Balance.");

				}
			} catch (InvalidPropertyException e) {
				logger.error(bulkSessionId, e.getMessage());
				throw new InvalidPropertyException(e.getMessage());
			} catch (NotFoundException e) {
				logger.error(bulkSessionId, e.getMessage());
				throw new NotFoundException(e.getMessage());
			} catch (InsufficientBalanceException e) {
				logger.error(bulkSessionId, e.getMessage());
				throw new InsufficientBalanceException(e.getMessage());
			} catch (InternalServerException e) {
				logger.error(bulkSessionId, e.getMessage());
				throw new InternalServerException(e.getMessage());
			} catch (ScheduledTimeException e) {
				logger.error(bulkSessionId, e.getMessage());
				throw new ScheduledTimeException(e.getMessage());
			} catch (Exception e) {
				logger.error(bulkSessionId, e);
				throw new InternalServerException(e.getMessage());
			}
			bulkResponse.setStatus(target);
			return bulkResponse;
		}
	}

	private BulkResponse BulkTracking(BulkRequest bulkRequest, BulkSmsDTO bulkSmsDTO, WebMasterEntry webEntry,
			DriverInfo driverInfo, UserEntry userEntry, ProgressEvent progressEvent,
			List<MultipartFile> destinationNumberFile) {
		BulkResponse bulkResponse = new BulkResponse();
		String target = IConstants.FAILURE_KEY;
		double totalcost = 0, adminCost = 0;// total_defcost = 0;
		String unicodeMsg = "";
		String systemId = userEntry.getSystemId();
		bulkSmsDTO.setCustomContent(true);
		String bulkSessionId = systemId + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		if (bulkSmsDTO.isSchedule()) {
			logger.info(bulkSessionId + " Tracking Schedule Request <" + destinationNumberFile.size() + ">");
		} else {
			logger.info(bulkSessionId + " Tracking Upload Request <" + destinationNumberFile.size() + ">");
		}
		int no_of_msg = bulkSmsDTO.getSmsParts();
		try {
			// ------ merge uploaded files into a list ---------------
			List<String> destinationList = null;
			List<String> temp_number_list = new ArrayList<String>();
			Map<String, Integer> errors = new HashMap<String, Integer>();
			int invalidCount = 0;
			int total = 0;
			for (MultipartFile uploaded_file : destinationNumberFile) {
				String fileName = uploaded_file.getOriginalFilename();
				logger.info(bulkSessionId + " Processing File: " + fileName);
				String fileMode = null;
				if (fileName.endsWith(".txt")) {
					fileMode = "txt";
				} else if (fileName.endsWith(".csv")) {
					fileMode = "csv";
				} else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
					fileMode = "xls";
				} else {
					logger.warn(bulkSessionId + " Invalid File Uploaded: " + fileName);
					throw new InvalidPropertyException(bulkSessionId + " Invalid File Uploaded: " + fileName);
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

				if (!excludeSet.isEmpty()) {
					try {
						writeExcludeNumbers(String.valueOf(userEntry.getId()), String.join("\n", excludeSet));
					} catch (Exception ex) {
						System.out.println(bulkSessionId + " " + ex);
					}
				} else {
					try {
						removeExcludeNumbers(String.valueOf(userEntry.getId()));
					} catch (Exception ex) {
						System.out.println(bulkSessionId + " " + ex);
					}
				}
				if (fileMode != null) {
					InputStream stream = uploaded_file.getInputStream();
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
								destinationNumber = destinationNumber.replaceAll("\\s+", ""); // Replace all the
																								// spaces
																								// in
																								// the String with
																								// empty
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
							if (uploaded_file.getOriginalFilename() != null
									&& uploaded_file.getOriginalFilename().endsWith(".xlsx")) {
								workbook = new XSSFWorkbook(stream);
							} else {
								workbook = new HSSFWorkbook(stream);
							}
							int numberOfSheets = workbook.getNumberOfSheets();
							for (int i = 0; i < numberOfSheets; i++) {
								Sheet firstSheet = workbook.getSheetAt(i);
								int total_rows = firstSheet.getPhysicalNumberOfRows();
								logger.info(uploaded_file.getName() + " Total Rows[" + i + "]: " + total_rows);
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
											destination = destination.replaceAll("\\s+", ""); // Replace all the
																								// spaces
																								// in
																								// the String with
																								// empty
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
								logger.info(uploaded_file.getName() + " Sheet[" + i + "] Processed");
							}
							// *********************************************************
						} catch (Exception ex) {
							logger.error("Parsing File: " + uploaded_file.getName(), ex.getLocalizedMessage());
							throw new InternalServerException(
									"Parsing File: " + uploaded_file.getName() + ex.getMessage());
						}
					}
					logger.info(uploaded_file.getName() + " NumberCounter: " + file_total_counter);
				}
			}
			Set<String> hashSet = new HashSet<String>(temp_number_list);
			if (webEntry.isPrefixApply()) {
				destinationList = new ArrayList<String>();
				for (String number : hashSet) {
					if (number.length() < webEntry.getNumberLength()) {
						logger.info(number + " length is less then " + webEntry.getNumberLength());
						number = webEntry.getPrefixToApply() + number;
					}
					destinationList.add(number);
				}
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
			logger.info(bulkSessionId + " Total:" + listInfo.getTotal() + " Valid:" + listInfo.getValidCount()
					+ " Invalid:" + listInfo.getInvalidCount() + " Duplicate: " + listInfo.getDuplicate()
					+ " DuplicateAllowed: " + bulkSmsDTO.isAllowDuplicate());
			// ------------------------End merge-------------------------------
			Optional<BalanceEntry> masterBalanceOptional = balanceEntryRepository
					.findBySystemId(userEntry.getMasterId());
			Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findBySystemId(userEntry.getSystemId());
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
			if (bulkRequest.getMessageType().equalsIgnoreCase("Unicode")) {
				bulkSmsDTO.setMessage(UTF16(bulkRequest.getMessage()));
				bulkSmsDTO.setOrigMessage(UTF16(bulkRequest.getMessage()));
				bulkSmsDTO.setDistinct("yes");
			} else {
				String sp_msg = bulkRequest.getMessage();
				String hexValue = getHexValue(sp_msg);
				unicodeMsg = SmsConverter.getContent(hexValue.toCharArray());
				bulkSmsDTO.setMessage(unicodeMsg);
				bulkSmsDTO.setMessageType("SpecialChar");
				bulkSmsDTO.setOrigMessage(UTF16(bulkRequest.getMessage()));
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
					throw new ScheduledTimeException(
							"Error: getting error in Scheduler bulkSessionId " + bulkSessionId);
				}
			}
			// ---------------- url appending & message calculation --------------------
			Hashtable<String, List<String>> msgmapping = new Hashtable<String, List<String>>(); // for mapping number
																								// with message content
			Map<String, Integer> msgLengthTable = new HashMap<String, Integer>(); // for balance calculation
			int totalMsg = 0; // FOR CREDIT CALCULATION
			String campaign_name = bulkRequest.getCampaignName();
			logger.info("Received Web Links: " + bulkRequest.getWeblink().length);
			List<String> web_links_list = new ArrayList<String>();
			for (String link : bulkRequest.getWeblink()) {
				if (link != null && link.length() > 0) {
					web_links_list.add(link);
				}
			}
			logger.info("Final Web Links: " + web_links_list);
			Map<String, String> campaign_mapping = getCampaignId(systemId, bulkSmsDTO.getSenderId(),
					IConstants.GATEWAY_NAME, web_links_list, String.join(",", destinationList), campaign_name);
			System.out.println("campaign mapping " + campaign_mapping);
			String web_link_hex_param = null;
			if (bulkSmsDTO.getMessageType().equalsIgnoreCase("Unicode")) {
				web_link_hex_param = "005B007700650062005F006C0069006E006B005F0074007200610063006B0069006E0067005F00750072006C005D"
						.toLowerCase();
			} else {
				web_link_hex_param = SevenBitChar.getHexValue(
						"91,119,101,98,95,108,105,110,107,95,116,114,97,99,107,105,110,103,95,117,114,108,93,");
				web_link_hex_param = SmsConverter.getContent(web_link_hex_param.toCharArray());
				System.out.println("web_link_hex_param" + web_link_hex_param);
			}
			// logger.info("web_link_hex_param: " + web_link_hex_param);
			int number_serial = 1;
			for (String destination : destinationList) {
				String msg_content = bulkSmsDTO.getMessage();
				logger.info("content[1]: " + msg_content);
				for (int i = 0; i < web_links_list.size(); i++) {
					if (campaign_mapping.containsKey(web_links_list.get(i))) {
						String appending_url = "http://1l.ae/" + campaign_mapping.get(web_links_list.get(i)) + "/r="
								+ number_serial;
						if (bulkSmsDTO.getMessageType().equalsIgnoreCase("Unicode")) {
							msg_content = msg_content.replaceFirst(web_link_hex_param,
									UTF16(appending_url).toLowerCase());
						} else {
							msg_content = msg_content.replaceFirst(web_link_hex_param, appending_url);
							System.out.println("msg content" + msg_content);
						}
					}
				}
				logger.info("content[2]: " + msg_content);
				int msg_length = msg_content.length();
				if (bulkSmsDTO.getMessageType().equalsIgnoreCase("Unicode")) {
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
				List<String> msgList = new ArrayList<String>();
				msgList.add(msg_content);
				msgmapping.put(destination, msgList);
				msgLengthTable.put(destination, no_of_msg);
				totalMsg = totalMsg + no_of_msg;
				number_serial++;
			}
			bulkSmsDTO.setDestinationList(destinationList);
			bulkSmsDTO.setMapTable(msgmapping);
			Optional<UserEntry> masterOptional = userEntryRepository.findBySystemId(userEntry.getMasterId());
			// -----------------------------------------------------------------
			if (wallet_flag.equalsIgnoreCase("yes")) {
				bulkSmsDTO.setUserMode("wallet");
				if (!destinationList.isEmpty()) {
					totalcost = routeService.calculateRoutingCost(userEntry.getId(), msgLengthTable);
					logger.info(bulkSessionId + " Balance:" + wallet + " Calculated Cost: " + totalcost);
					boolean amount = false;
					// boolean inherit = false;
					if (userEntry.isAdminDepend()) {
						adminCost = routeService.calculateRoutingCost(masterOptional.get().getId(), msgLengthTable);
						logger.info(bulkSessionId + " Admin[" + masterOptional.get().getId() + "] Balance:"
								+ adminWallet + " Calculated Cost: " + adminCost);
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
								logger.error(bulkSessionId + " <-- Insufficient Balance -->");
								throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Balance -->");
							}
						} else {
							// Insufficient Admin balance
							logger.error(bulkSessionId + " <-- Insufficient Admin(" + masterOptional.get().getId()
									+ ") Balance -->");
							throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Admin("
									+ masterOptional.get().getId() + ") Balance -->");
						}
					} else {
						if (wallet > 0 && wallet >= totalcost) {
							wallet = wallet - totalcost;
							balanceEntry.setWalletAmount(wallet);
							amount = true;
							balanceEntryRepository.save(balanceEntry);
						} else {
							// Insufficient balance
							logger.info(bulkSessionId + " <-- Insufficient Balance -->");
							throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Balance -->");
						}
					}
					if (amount) {
						// String applicationName = request.getContextPath();
						bulkSmsDTO.setMsgCount(totalMsg);
						bulkSmsDTO.setTotalCost(totalcost);
						SendSmsService service = new SendSmsService();
						if (bulkSmsDTO.isSchedule()) {
							bulkSmsDTO.setTotalWalletCost(totalcost);
							String filename = service.createScheduleFile(bulkSmsDTO);
							int generated_id = 0;
							if (filename != null) {
								ScheduleEntry sch = new ScheduleEntry();
								sch.setUsername(systemId);
								sch.setDate(bulkSmsDTO.getDate());
								sch.setServerTime(bulkSmsDTO.getTime());
								sch.setClientGmt(bulkSmsDTO.getGmt());
								sch.setClientTime(bulkSmsDTO.getTimestart());
								sch.setServerId(IConstants.SERVER_ID);
								sch.setStatus("false");
								sch.setFileName(filename);
								sch.setRepeated(bulkSmsDTO.getRepeat());
								sch.setScheduleType(bulkSmsDTO.getReqType());
								sch.setWebId(null);
								generated_id = scheduleEntryRepository.save(sch).getId();

								if (generated_id > 0) {
									String today = Validation.getTodayDateFormat();
									if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
										Set<Integer> set = null;
										if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
											set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
										} else {
											set = new LinkedHashSet<Integer>();
										}
										set.add(generated_id);
										GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
									}
									if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
										GlobalVarsSms.RepeatedSchedules.add(generated_id);
									}
									target = IConstants.SUCCESS_KEY;
									logger.info("message.scheduleSuccess");
								} else {
									logger.error(
											"Error: Unable to schedule the task. An error occurred while processing the schedule request.");
									throw new ScheduledTimeException(
											"Error: Unable to schedule the task. An error occurred while processing the schedule request for username "
													+ userEntry.getSystemId());

								}
							} else {
								// Already Scheduled
								logger.error(
										"Error: The task is already scheduled. Duplicate schedule request received.");
								throw new ScheduledTimeException(
										"Error: The task is already scheduled. Duplicate schedule request received for username "
												+ userEntry.getSystemId());
							}
						} else {
							String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
									userEntry.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info("Bulk SMS sent successfully. Message: " + value);
							} else {
								// Submission Error
								logger.error("Error sending bulk SMS. Details: " + value);
								throw new InternalServerException("Error sending bulk SMS. Please check the details.");

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
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// insufficient balance
						String insufficientBalanceMessage = "Error: Insufficient funds in the wallet.";
						logger.error(insufficientBalanceMessage);
						throw new InsufficientBalanceException(insufficientBalanceMessage);
					}
				} else {
					// Number File Error
					String invalidNumberMessage = "Error: The provided number is not valid.";
					logger.error(invalidNumberMessage);
					throw new InternalServerException(invalidNumberMessage);
				}
			} else if (wallet_flag.equalsIgnoreCase("no")) {
				bulkSmsDTO.setUserMode("credit");
				if (!destinationList.isEmpty()) {
					long credits = balanceEntry.getCredits();
					long adminCredit = masterbalance.getCredits();
					boolean amount = false;
					// boolean inherit = false;
					if (userEntry.isAdminDepend()) {
						if (adminCredit >= totalMsg) {
							if (credits >= totalMsg) {
								adminCredit = adminCredit - totalMsg;
								balanceEntry.setCredits(adminCredit);
								credits = credits - totalMsg;
								masterbalance.setCredits(credits);
								amount = true;
								balanceEntryRepository.save(balanceEntry);
								balanceEntryRepository.save(masterbalance);
							} else {
								logger.error(bulkSessionId + " <-- Insufficient Credits -->");
								throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Credits -->");
							}
						} else {
							logger.error(bulkSessionId + " <-- Insufficient Admin(" + masterOptional.get().getSystemId()
									+ ") Credits -->");
							throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Admin("
									+ masterOptional.get().getSystemId() + ") Credits -->");
						}
					} else {
						if (credits >= totalMsg) {
							credits = credits - totalMsg;
							balanceEntry.setCredits(credits);
							amount = true;
							balanceEntryRepository.save(balanceEntry);
						} else {
							logger.error(bulkSessionId + " <-- Insufficient Credits -->");
							throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Credits -->");

						}
					}
					if (amount) {
						// long deductCredits = numberslist.size() * no_of_msg;
						// String applicationName = request.getContextPath();
						bulkSmsDTO.setMsgCount(totalMsg);
						SendSmsService service = new SendSmsService();
						if (bulkSmsDTO.isSchedule()) {
							String filename = service.createScheduleFile(bulkSmsDTO);
							int generated_id = 0;
							if (filename != null) {
								ScheduleEntry sch = new ScheduleEntry();
								sch.setUsername(systemId);
								sch.setDate(bulkSmsDTO.getDate());
								sch.setServerTime(bulkSmsDTO.getTime());
								sch.setClientGmt(bulkSmsDTO.getGmt());
								sch.setClientTime(bulkSmsDTO.getTimestart());
								sch.setServerId(IConstants.SERVER_ID);
								sch.setStatus("false");
								sch.setFileName(filename);
								sch.setRepeated(bulkSmsDTO.getRepeat());
								sch.setScheduleType(bulkSmsDTO.getReqType());
								sch.setWebId(null);
								generated_id = scheduleEntryRepository.save(sch).getId();
								if (generated_id > 0) {
									String today = Validation.getTodayDateFormat();
									if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
										Set<Integer> set = null;
										if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
											set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
										} else {
											set = new LinkedHashSet<Integer>();
										}
										set.add(generated_id);
										GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
									}
									if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
										GlobalVarsSms.RepeatedSchedules.add(generated_id);
									}
									target = IConstants.SUCCESS_KEY;
									// Schedule success
									String scheduleSuccessMessage = "Success: The task has been scheduled successfully.";
									logger.info(scheduleSuccessMessage);
								} else {
									// Schedule error
									String scheduleErrorMessage = "Error: There was an issue scheduling the task.";
									logger.error(scheduleErrorMessage);
									throw new ScheduledTimeException(scheduleErrorMessage);
								}
							} else {
								// Duplicate schedule error
								String duplicateScheduleMessage = "Error: The task has already been scheduled at this time.";
								logger.error(duplicateScheduleMessage);
								throw new ScheduledTimeException(duplicateScheduleMessage);
							}
						} else {
							String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
									userEntry.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info("Bulk SMS sent successfully. Message: " + value);
							} else {
								// Submission Error
								logger.error("Error sending bulk SMS. Details: " + value);
								throw new InternalServerException("Error sending bulk SMS. Please check the details.");

							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setBulkListInfo(listInfo);
							bulkResponse.setCredits(Long.toString(credits));
							bulkResponse.setDeductcredits(totalMsg + "");
							bulkResponse.setBulkSessionId(bulkSessionId);
							logger.info(bulkSessionId + " Processed :-> Credits: " + credits + " Deduct: " + totalMsg);
						} else {
							logger.error(bulkSessionId + "<-- Process Failed --> ");
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// Insufficient Credits
						String insufficientCreditMessage = "Error: Insufficient credits for the operation.";
						logger.error(insufficientCreditMessage);
						throw new InsufficientBalanceException(insufficientCreditMessage);
					}
				} else {
					// Number File Error
					String noValidNumberMessage = "Error: The provided number is not valid.";
					logger.error(noValidNumberMessage);
					throw new InternalServerException(noValidNumberMessage);
				}
			} else if (wallet_flag.equalsIgnoreCase("MIN")) {
				String insufficientWalletMessage = "Error: Insufficient wallet balance for the operation.";
				logger.error(insufficientWalletMessage);
				throw new InsufficientBalanceException(insufficientWalletMessage);
			}
		} catch (InvalidPropertyException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InvalidPropertyException(e.getMessage());
		} catch (NotFoundException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (InsufficientBalanceException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InsufficientBalanceException(e.getMessage());
		} catch (InternalServerException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InternalServerException(e.getMessage());
		} catch (ScheduledTimeException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new ScheduledTimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(bulkSessionId, e);
			throw new InternalServerException(e.getMessage());
		}
		bulkResponse.setStatus(target);
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
			int System_Id) {

		String response = "";
		try {
			setProgressEvent(progressEvent);
			response = sendBulkMsg(bulkSmsDTO, waitForApprove, System_Id);
		} catch (Exception e) {
			logger.error(bulkSmsDTO.getSystemId(), e.fillInStackTrace());
		}
		return response;
	}

	public String sendBulkMsg(BulkSmsDTO bulkSmsDTO, boolean waitForApprove, int System_Id) {

		logger.info(bulkSmsDTO.getSystemId() + " " + bulkSmsDTO.getReqType() + " isAlert: " + bulkSmsDTO.isAlert()
				+ " Number: " + bulkSmsDTO.getDestinationNumber());
		String user = bulkSmsDTO.getSystemId();
		// QueueBackup backupObject = null;
		BulkEntry entry = null;
		int ston = 5;
		int snpi = 0;
		String ret = "";
		int user_id = System_Id;
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
					System.out.println("this is map table data " + bulkSmsDTO.getMapTable());
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
								System.out.println("this is final content" + content);
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

					BulkManagementEntity bulkMgmt = new BulkManagementEntity();

					bulkMgmt.setSystemId(entry.getSystemId());
					bulkMgmt.setSenderId(entry.getSenderId());
					bulkMgmt.setTotalNum(entry.getTotal());
					bulkMgmt.setFirstNum(entry.getFirstNumber());
					bulkMgmt.setDelay(entry.getDelay());
					bulkMgmt.setReqType(entry.getReqType());
					bulkMgmt.setSton(entry.getSton());
					bulkMgmt.setSnpi(entry.getSnpi());
					bulkMgmt.setAlert(entry.isAlert());
					bulkMgmt.setAlertNumber(entry.getAlertNumbers());
					bulkMgmt.setExpiryHour(entry.getExpiryHour());
					bulkMgmt.setContent(entry.getContent());
					bulkMgmt.setMsgType(entry.getMessageType());
					bulkMgmt.setCampaignName(entry.getCampaignName());
					bulkMgmt.setPeId(entry.getPeId());
					bulkMgmt.setTemplateId(entry.getTemplateId());
					bulkMgmt.setTelemarketerId(entry.getTelemarketerId());
					bulkMgmt.setMsgCount(bulkMgmtEntry.getMsgCount());
					bulkMgmt.setCost(bulkMgmtEntry.getTotalCost());
					bulkMgmt.setUserMode(bulkMgmtEntry.getUserMode());
					bulkMgmt.setCampaignType(bulkMgmtEntry.getCampaignType());
					bulkMgmt.setActive(Boolean.TRUE);
					LocalDate localDate = LocalDate.now();
					Timestamp timestamp = Timestamp.valueOf(localDate.atStartOfDay());
					bulkMgmt.setCreatedOn(timestamp);
					bulkMgmt.setServerId(entry.getServerId());
					bulkMgmt.setStatus("PENDING");
					int wait_id = bulkMgmtEntryRepository.save(bulkMgmt).getId();

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
			List<BulkMgmtContent> bulkMgmtContentList = new ArrayList<>();
			for (BulkContentEntry entry : list) {
				BulkMgmtContent content = new BulkMgmtContent();
				content.setDestination(entry.getDestination());
				content.setContent(entry.getContent());
				content.setFlag(entry.getFlag());
				bulkMgmtContentList.add(content);
			}
			createBulkMgmtContentTable(batch_id, bulkMgmtContentList);
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
			String tableName = "batch_content_" + batchId;
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
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
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
		boolean allowDuplicate = false;
		Map errors = new HashMap();
		BulkResponse bulkResponse = new BulkResponse();
		String bulkSessionId = user.getId() + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		WebMasterEntry webEntry = null;
		BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
		try {
			bulkSmsDTO.setSenderId(bulkRequest.getSenderId());
			bulkSmsDTO.setMessage(bulkRequest.getMessage());
			bulkSmsDTO.setFrom(bulkRequest.getFrom());
			bulkSmsDTO.setSmscount(bulkRequest.getSmscount());
			bulkSmsDTO.setDelay(bulkRequest.getDelay());
			bulkSmsDTO.setSchedule(bulkRequest.isSchedule());
			bulkSmsDTO.setAlert(bulkRequest.isAlert());
			bulkSmsDTO.setAllowDuplicate(bulkRequest.isAllowDuplicate());
			bulkSmsDTO.setMessageType(bulkRequest.getMessageType());
			bulkSmsDTO.setSmsParts(bulkRequest.getSmsParts());
			bulkSmsDTO.setCharCount(bulkRequest.getCharCount());
			bulkSmsDTO.setCharLimit(bulkRequest.getCharLimit());
			bulkSmsDTO.setExclude(bulkRequest.getExclude());
			bulkSmsDTO.setExpiryHour(bulkRequest.getExpiryHour());
			bulkSmsDTO.setCampaignName(bulkRequest.getCampaignName());
			bulkSmsDTO.setCustomContent(true);

			if (bulkRequest.isSchedule()) {
				bulkSmsDTO.setTimestart(bulkRequest.getTimestart());
				bulkSmsDTO.setRepeat(bulkRequest.getRepeat());
				bulkSmsDTO.setGmt(bulkRequest.getGmt());
			}

			if (bulkRequest.getDestinationNumber() != null)
				bulkSmsDTO.setDestinationNumber(bulkRequest.getDestinationNumber());
			if (bulkRequest.getPeId() != null)
				bulkSmsDTO.setPeId(bulkRequest.getPeId());
			if (bulkRequest.getTelemarketerId() != null)
				bulkSmsDTO.setTelemarketerId(bulkRequest.getTelemarketerId());
			if (bulkRequest.getTemplateId() != null)
				bulkSmsDTO.setTemplateId(bulkRequest.getTemplateId());

			DriverInfo driverInfo = null;
			Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(user.getId());
			if (OptionalDriverInfo.isPresent()) {
				driverInfo = OptionalDriverInfo.get();
			} else
				throw new NotFoundException("drive info  not found with the provided username.");
			bulkSmsDTO.setClientId(user.getSystemId());
			bulkSmsDTO.setSystemId(user.getSystemId());
			bulkSmsDTO.setPassword(new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
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
		if (bulkRequest.getMessageType().equalsIgnoreCase("Unicode")) {
			bulkSmsDTO.setMessage(UTF16(bulkRequest.getMessage()));
			bulkSmsDTO.setOrigMessage(UTF16(bulkRequest.getMessage()));
			bulkSmsDTO.setDistinct("yes");
		} else {
			String sp_msg = bulkRequest.getMessage();
			String hexValue = getHexValue(sp_msg);
			unicodeMsg = SmsConverter.getContent(hexValue.toCharArray());
			bulkSmsDTO.setMessage(unicodeMsg);
			bulkSmsDTO.setMessageType("SpecialChar");
			bulkSmsDTO.setOrigMessage(UTF16(bulkRequest.getMessage()));
		}
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
				writeExcludeNumbers(String.valueOf(user.getId()), String.join("\n", excludeSet));
			} catch (Exception ex) {
				System.out.println(bulkSessionId + " " + ex);
			}
		} else {
			try {
				removeExcludeNumbers(String.valueOf(user.getId()));
			} catch (Exception ex) {
				System.out.println(bulkSessionId + " " + ex);
			}
		}
		logger.info(bulkSessionId + " Exclude Count: " + excludeSet.size());
		if (bulkSmsDTO.getMessageType().equalsIgnoreCase("7bit")) {
			String sp_msg = bulkRequest.getMessage();
			unicodeMsg = SmsConverter.getContent(sp_msg.toCharArray());
			bulkSmsDTO.setMessage(unicodeMsg);
			bulkSmsDTO.setMessageType("SpecialChar");
		}
		String msgType = bulkSmsDTO.getMessageType();
		try {
			Optional<BalanceEntry> masterBalanceOptional = balanceEntryRepository.findBySystemId(user.getMasterId());
			Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findBySystemId(user.getSystemId());
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
			String client_time = null;
			if (bulkSmsDTO.isSchedule()) {
				boolean valid_sch_time = false;
				client_time = bulkSmsDTO.getTimestart();
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

								String hex_param = SevenBitChar.getHexValue(asciilist);
								entry = SmsConverter.getContent(hex_param.toCharArray());
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
						webEntry = webMasterEntryRepository.findByUserId(user.getId());
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
					Map<String, String> campaign_mapping = getCampaignId(String.valueOf(user.getSystemId()),
							bulkSmsDTO.getSenderId(), "GWYM2", web_links_list, String.join(",", entry_map.keySet()),
							campaign_name);
					System.out.println("this is campaign mapping " + campaign_mapping);
					String web_link_hex_param = null;
					if (bulkSmsDTO.getMessageType().equalsIgnoreCase("Unicode")) {
						web_link_hex_param = "005B007700650062005F006C0069006E006B005F0074007200610063006B0069006E0067005F00750072006C005D"
								.toLowerCase();
					} else {
						web_link_hex_param = SevenBitChar.getHexValue(
								"91,119,101,98,95,108,105,110,107,95,116,114,97,99,107,105,110,103,95,117,114,108,93,");
						web_link_hex_param = SmsConverter.getContent(web_link_hex_param.toCharArray());
						System.out.println("web_link_hex_param" + web_link_hex_param);
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
									if (campaign_mapping.containsKey(web_links_list.get(i))) {
										String appending_url = "http://1l.ae/"
												+ campaign_mapping.get(web_links_list.get(i)) + "/r=" + number_serial;
										msg = msg.replaceFirst(web_link_hex_param, UTF16(appending_url).toLowerCase());
									}
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
									if (campaign_mapping.containsKey(web_links_list.get(i))) {
										String appending_url = "http://1l.ae/"
												+ campaign_mapping.get(web_links_list.get(i)) + "/r=" + number_serial;
										msg = msg.replaceFirst(web_link_hex_param, appending_url);
										System.out.println("this is final replace value " + msg);
									}
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

						totalcost = routeService.calculateRoutingCost(user.getId(), msgLengthTable);
						logger.info(bulkSessionId + " Balance:" + wallet + " Calculated Cost: " + totalcost);
						boolean amount = false;
						if (user.isAdminDepend()) {
							Optional<UserEntry> masterOptional = userEntryRepository.findBySystemId(user.getMasterId());
							if (!masterOptional.isPresent()) {
								throw new NotFoundException("User not found with the provided username.");
							}
							adminCost = routeService.calculateRoutingCost(masterOptional.get().getId(), msgLengthTable);
							logger.info(bulkSessionId + " Admin[" + masterOptional.get().getId() + "] Balance:"
									+ adminWallet + " Calculated Cost: " + adminCost);
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
									logger.error(bulkSessionId + " <-- Insufficient Balance --> " + wallet);
									throw new InsufficientBalanceException(
											bulkSessionId + " <-- Insufficient Balance --> " + wallet);
								}
							} else {
								// Insufficient Admin balance
								logger.error(bulkSessionId + " <-- Insufficient Admin("
										+ Integer.parseInt(user.getMasterId()) + ") Balance --> " + adminWallet);
								throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Admin("
										+ Integer.parseInt(user.getMasterId()) + ") Balance --> " + adminWallet);
							}
						} else {
							if (wallet >= totalcost) {
								wallet = wallet - totalcost;
								balanceEntry.setWalletAmount(wallet);
								balanceEntryRepository.save(balanceEntry);
								amount = true;
							} else {
								// Insufficient balance
								logger.error(bulkSessionId + " <-- Insufficient Balance --> " + wallet);
								throw new InsufficientBalanceException(
										bulkSessionId + " <-- Insufficient Balance --> " + wallet);
							}
						}
						if (amount) {
							// String applicationName = request.getContextPath();
							bulkSmsDTO.setMsgCount(totalMsg);
							bulkSmsDTO.setTotalCost(totalcost);
							SendSmsService service = new SendSmsService();
							if (bulkSmsDTO.isSchedule()) {
								bulkSmsDTO.setTotalWalletCost(totalcost);
								String filename = service.createScheduleFile(bulkSmsDTO);
								int generated_id = 0;
								if (filename != null) {
									ScheduleEntry sch = new ScheduleEntry();
									sch.setClientGmt(bulkSmsDTO.getGmt());
									sch.setClientTime(client_time);
									sch.setFileName(filename);
									sch.setRepeated(bulkSmsDTO.getRepeat());
									sch.setScheduleType(bulkSmsDTO.getReqType());
									sch.setServerId(IConstants.SERVER_ID);
									sch.setServerTime(bulkSmsDTO.getTime());
									sch.setStatus("false");
									sch.setUsername(bulkSmsDTO.getSystemId());
									sch.setDate(bulkSmsDTO.getDate());
									sch.setWebId(null);
									generated_id = scheduleEntryRepository.save(sch).getId();
									if (generated_id > 0) {
										String today = Validation.getTodayDateFormat();
										if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
											Set<Integer> set = null;
											if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
												set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
											} else {
												set = new LinkedHashSet<Integer>();
											}
											set.add(generated_id);
											GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
										}
										if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
											GlobalVarsSms.RepeatedSchedules.add(generated_id);
										}
										target = IConstants.SUCCESS_KEY;
										logger.info("message.scheduleSuccess");
									} else {
										// Scheduling Error
										logger.error(
												"Error: Unable to schedule the task. An error occurred during the scheduling process.");
										throw new ScheduledTimeException(
												"Error: Unable to schedule the task. An error occurred during the scheduling process.");

									}
								} else {
									// Already Scheduled
									logger.error(
											"Error: The task is already scheduled. Duplicate schedule request received.");
									throw new ScheduledTimeException(
											"Error: The task is already scheduled. Duplicate schedule request received.");

								}
							} else {
								String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
										user.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									logger.info("Batch processing completed successfully. Message: " + value);
								} else {
									// Submission Error
									logger.error(
											"Error: Unable to process batch submission. An error occurred during the submission process. Details: "
													+ value);
									throw new InternalServerException(
											"Error: Unable to process batch submission. An error occurred during the submission process. Details: "
													+ value);

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
								logger.error(bulkSessionId + "<-- Process Failed --> ");
								throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							// Insufficient Balance
							logger.error(
									"Error: Insufficient balance to perform the operation. Please add funds to your wallet.");
							throw new InsufficientBalanceException(
									"Error: Insufficient balance to perform the operation. Please add funds to your wallet.");

						}
					} // ******************************End Wallet Calculation*********************
						// ******************************Start Credit Calculation*********************
					else if (wallet_flag.equalsIgnoreCase("no")) {
						bulkSmsDTO.setUserMode("credit");
						long credits = balanceEntry.getCredits();
						long adminCredit = masterbalance.getCredits();
						boolean amount = false;
						if (user.isAdminDepend()) {
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
									System.out.println(user.getId() + " <-- Insufficient Credits -->");
									throw new InsufficientBalanceException(
											user.getId() + " <-- Insufficient Credits -->");
								}
							} else {
								System.out.println(user.getId() + " <-- Insufficient Admin(" + user.getMasterId()
										+ ") Credits -->");
								throw new InsufficientBalanceException(user.getId() + " <-- Insufficient Admin("
										+ user.getMasterId() + ") Credits -->");
							}
						} else {
							if (credits >= totalMsg) {
								credits = credits - totalMsg;
								balanceEntry.setCredits(credits);
								balanceEntryRepository.save(balanceEntry);
								amount = true;
							} else {
								System.out.println(user.getId() + " <-- Insufficient Credits -->");
								throw new InsufficientBalanceException(user.getId() + " <-- Insufficient Credits -->");
							}
						}
						if (amount) {
							long deductCredits = totalMsg;
							// String applicationName = request.getContextPath();
							SendSmsService service = new SendSmsService();
							bulkSmsDTO.setMsgCount(deductCredits);
							if (bulkSmsDTO.isSchedule()) {
								String filename = service.createScheduleFile(bulkSmsDTO);
								int generated_id = 0;
								if (filename != null) {
									ScheduleEntry sch = new ScheduleEntry();
									sch.setClientGmt(bulkSmsDTO.getGmt());
									sch.setClientTime(client_time);
									sch.setFileName(filename);
									sch.setRepeated(bulkSmsDTO.getRepeat());
									sch.setScheduleType(bulkSmsDTO.getReqType());
									sch.setServerId(IConstants.SERVER_ID);
									sch.setServerTime(bulkSmsDTO.getTime());
									sch.setStatus("false");
									sch.setUsername(bulkSmsDTO.getSystemId());
									sch.setDate(bulkSmsDTO.getDate());
									sch.setWebId(null);
									generated_id = scheduleEntryRepository.save(sch).getId();
									if (generated_id > 0) {
										String today = Validation.getTodayDateFormat();
										if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
											Set<Integer> set = null;
											if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
												set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
											} else {
												set = new LinkedHashSet<Integer>();
											}
											set.add(generated_id);
											GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
										}
										if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
											GlobalVarsSms.RepeatedSchedules.add(generated_id);
										}
										target = IConstants.SUCCESS_KEY;
										logger.info("Task scheduled successfully. Details: ");

									} else {
										// Scheduling Error
										logger.error(
												"Error: Unable to schedule the task. An error occurred during the scheduling process.");
										throw new ScheduledTimeException(
												"Error: Unable to schedule the task. An error occurred during the scheduling process.");

									}
								} else {
									// Duplicate Schedule Error
									logger.error(
											"Error: The task is already scheduled. Duplicate schedule request received.");
									throw new ScheduledTimeException(
											"Error: The task is already scheduled. Duplicate schedule request received.");

								}
							} else {
								String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
										user.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									logger.info("Batch processing completed successfully. Additional details: ");
								} else {
									// Submission Error
									logger.error(
											"Error: Unable to process batch submission. An error occurred during the submission process.");
									throw new InternalServerException(
											"Error: Unable to process batch submission. An error occurred during the submission process.");

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
								throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							logger.error(
									"Error: Insufficient credits to perform the operation. Please add more credits.");
							throw new InsufficientBalanceException(
									"Error: Insufficient credits to perform the operation. Please add more credits.");

						}
					} else if (wallet_flag.equalsIgnoreCase("MIN")) {
						// Insufficient Balance
						logger.error(
								"Error: Insufficient balance to perform the operation. Please add funds to your wallet.");
						throw new InsufficientBalanceException(
								"Error: Insufficient balance to perform the operation. Please add funds to your wallet.");

					}
				} else {

					logger.error(
							"Error: No valid numbers found in the provided file. Please check the file and try again.");
					throw new InternalServerException(
							"Error: No valid numbers found in the provided file. Please check the file and try again.");

				}
			} else {
				// Invalid File Format
				logger.error("Error: Invalid file format. Please provide a valid file.");
				throw new InvalidPropertyException("Error: Invalid file format. Please provide a valid file.");

			}
		} catch (InvalidPropertyException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InvalidPropertyException(e.getMessage());
		} catch (NotFoundException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (InsufficientBalanceException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InsufficientBalanceException(e.getMessage());
		} catch (InternalServerException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InternalServerException(e.getMessage());
		} catch (ScheduledTimeException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new ScheduledTimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(bulkSessionId, e);
			throw new InternalServerException(e.getMessage());
		}
		bulkResponse.setStatus(target);
		return bulkResponse;
	}

	@Override
	public ResponseEntity<?> sendSmsByContacts(BulkContactRequest bulkContactRequest, String username) {
		BulkResponse bulkResponse = new BulkResponse();
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user.getId());
		DriverInfo driverInfo = null;
		Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(user.getId());
		if (OptionalDriverInfo.isPresent()) {
			driverInfo = OptionalDriverInfo.get();
		} else
			throw new NotFoundException("drive info  not found with the provided username.");

		BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
		bulkSmsDTO.setUploadedNumbers(bulkContactRequest.getUploadedNumbers());
		bulkSmsDTO.setMessage(bulkContactRequest.getMessage());
		bulkSmsDTO.setFrom(bulkContactRequest.getFrom());
		bulkSmsDTO.setSmscount(bulkContactRequest.getSmscount());
		bulkSmsDTO.setDelay(bulkContactRequest.getDelay());
		bulkSmsDTO.setSchedule(bulkContactRequest.isSchedule());
		bulkSmsDTO.setAlert(bulkContactRequest.isAlert());
		bulkSmsDTO.setMessageType(bulkContactRequest.getMessageType());
		bulkSmsDTO.setSmsParts(bulkContactRequest.getSmsParts());
		bulkSmsDTO.setCharCount(bulkContactRequest.getCharCount());
		bulkSmsDTO.setCharLimit(bulkContactRequest.getCharLimit());
		bulkSmsDTO.setExclude(bulkContactRequest.getExclude());
		bulkSmsDTO.setExpiryHour(bulkContactRequest.getExpiryHour());
		bulkSmsDTO.setCampaignName(bulkContactRequest.getCampaignName());
		bulkSmsDTO.setClientId(user.getSystemId());
		bulkSmsDTO.setSystemId(user.getSystemId());
		bulkSmsDTO.setPassword(new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
		bulkSmsDTO.setSenderId(bulkContactRequest.getSenderId());
		bulkSmsDTO.setCustomContent(false);
		bulkSmsDTO.setTotalNumbers(bulkContactRequest.isTotalContact());

		if (bulkContactRequest.getDestinationNumber() != null)
			bulkSmsDTO.setDestinationNumber(bulkContactRequest.getDestinationNumber());
		if (bulkContactRequest.isSchedule()) {
			bulkSmsDTO.setTimestart(bulkContactRequest.getTimestart());
			bulkSmsDTO.setRepeat(bulkContactRequest.getRepeat());
			bulkSmsDTO.setGmt(bulkContactRequest.getGmt());
		}
		if (bulkContactRequest.getPeId() != null)
			bulkSmsDTO.setPeId(bulkContactRequest.getPeId());
		if (bulkContactRequest.getTelemarketerId() != null)
			bulkSmsDTO.setTelemarketerId(bulkContactRequest.getTelemarketerId());
		if (bulkContactRequest.getTemplateId() != null)
			bulkSmsDTO.setTemplateId(bulkContactRequest.getTemplateId());

		if (bulkContactRequest.isTracking()) {
			return tracking(bulkContactRequest, bulkSmsDTO, webEntry, driverInfo, user);
		} else {
			String target = IConstants.FAILURE_KEY;
			String unicodeMsg = "";
			// String msgType = "";

			String systemId = user.getSystemId();
			String bulkSessionId = systemId + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
			if (bulkSmsDTO.isSchedule()) {
				logger.info(bulkSessionId + "<-- Contact Schedule Request --> ");
			} else {
				logger.info(bulkSessionId + "<-- Contact Upload Request -->");
			}
			try {

				BulkListInfo listInfo = new BulkListInfo();
				List<String> destinationList = bulkSmsDTO.getDestinationList2(listInfo);
				logger.info(bulkSessionId + " Total:" + listInfo.getTotal() + " Valid:" + listInfo.getValidCount()
						+ " Invalid:" + listInfo.getInvalidCount() + " Duplicate: " + listInfo.getDuplicate());

				Optional<BalanceEntry> masterBalanceOptional = balanceEntryRepository
						.findBySystemId((user.getMasterId()));
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
				double totalcost = 0;
				int no_of_msg = bulkSmsDTO.getSmsParts();
				if (bulkContactRequest.getMessageType().equalsIgnoreCase("Unicode")) {
					bulkSmsDTO.setMessage(UTF16(bulkContactRequest.getMessage()));
					bulkSmsDTO.setOrigMessage(UTF16(bulkContactRequest.getMessage()));
					bulkSmsDTO.setDistinct("yes");
				} else {
					String sp_msg = bulkContactRequest.getMessage();
					String hexValue = getHexValue(sp_msg);
					unicodeMsg = SmsConverter.getContent(hexValue.toCharArray());
					bulkSmsDTO.setMessage(unicodeMsg);
					bulkSmsDTO.setMessageType("SpecialChar");
					bulkSmsDTO.setOrigMessage(UTF16(bulkContactRequest.getMessage()));
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
						logger.error(bulkSessionId, "An error occurred: " + e.getMessage(), e);
						throw new ScheduledTimeException(
								"Error: Schedule time not valid. An error occurred during processing."
										+ e.getMessage());

					}
					if (!valid_sch_time) {
						throw new ScheduledTimeException("Error: Schedule time not valid.");

					}
				}
				if (wallet_flag.equalsIgnoreCase("yes")) {
					bulkSmsDTO.setUserMode("wallet");
					totalcost = routeService.calculateRoutingCost(user.getId(), destinationList, no_of_msg);
					logger.info(bulkSessionId + " Balance:" + wallet + " Calculated Cost: " + totalcost);
					if (destinationList.size() > 0) {
						boolean amount = false;
						boolean inherit = false;
						if (user.isAdminDepend() && (adminWallet >= totalcost)) {
							amount = true;
							inherit = true;
						} else if (wallet > 0 && wallet >= totalcost) {
							amount = true;
						}
						if (amount) {
							if (inherit) {
								adminWallet = adminWallet - totalcost;
								masterbalance.setWalletAmount(adminWallet);
								balanceEntryRepository.save(masterbalance);
							} else {
								wallet = wallet - totalcost;
								balanceEntry.setWalletAmount(wallet);
								balanceEntryRepository.save(balanceEntry);
							}
							// String applicationName = request.getContextPath();
							bulkSmsDTO.setMsgCount(destinationList.size() * no_of_msg);
							bulkSmsDTO.setTotalCost(totalcost);
							SendSmsService service = new SendSmsService();
							if (bulkSmsDTO.isSchedule()) {
								bulkSmsDTO.setTotalWalletCost(totalcost);
								String filename = service.createScheduleFile(bulkSmsDTO);
								int generated_id = 0;
								if (filename != null) {

									ScheduleEntry sch = new ScheduleEntry();
									sch.setClientGmt(bulkSmsDTO.getGmt());
									sch.setClientTime(bulkSmsDTO.getTimestart());
									sch.setFileName(filename);
									sch.setRepeated(bulkSmsDTO.getRepeat());
									sch.setScheduleType(bulkSmsDTO.getReqType());
									sch.setServerId(IConstants.SERVER_ID);
									sch.setServerTime(bulkSmsDTO.getTime());
									sch.setStatus("false");
									sch.setUsername(bulkSmsDTO.getSystemId());
									sch.setDate(bulkSmsDTO.getDate());
									sch.setWebId(null);
									generated_id = scheduleEntryRepository.save(sch).getId();

									if (generated_id > 0) {
										String today = Validation.getTodayDateFormat();
										if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
											Set<Integer> set = null;
											if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
												set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
											} else {
												set = new LinkedHashSet<Integer>();
											}
											set.add(generated_id);
											GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
										}
										if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
											GlobalVarsSms.RepeatedSchedules.add(generated_id);
										}
										target = IConstants.SUCCESS_KEY;
										logger.info("Task scheduled successfully. Additional details: " + target);

									} else {
										// Scheduling Error
										String message = ("Error: Unable to schedule the task. An error occurred during the scheduling process.");
										logger.error(message);
										throw new ScheduledTimeException(message);
									}
								} else {
									// already Scheduled
									String message = ("Error: The task is already scheduled. Duplicate schedule request received.");
									logger.error(message);
									throw new ScheduledTimeException(message);
								}
							} else {
								String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;

									String message = ("Batch processing completed successfully. Message: " + value);
									logger.info(message);
								} else {
									// Submission Error
									String message = ("Error: Unable to process batch submission. An error occurred during the submission process.");
									logger.error(message);
									throw new InternalServerException(message);
								}
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
								bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost) + "");
								bulkResponse.setBulkSessionId(bulkSessionId);
								logger.info(
										bulkSessionId + " Processed :-> Balance: " + wallet + " Cost: " + totalcost);
							} else {
								logger.info(bulkSessionId + "<-- Process Failed --> ");
								throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							// insufficient balance
							logger.error(bulkSessionId + " <-- Insufficient Balance --> " + wallet);
							throw new InsufficientBalanceException(
									bulkSessionId + " <-- Insufficient Balance --> " + wallet);
						}
					} else {
						// Number File Error
						String message = ("Error: No valid numbers found in the provided data. Please check the data and try again.");
						logger.error(message);
						throw new InternalServerException(message);

					}
				}
				if (wallet_flag.equalsIgnoreCase("no")) {
					bulkSmsDTO.setUserMode("credit");
					if (destinationList.size() > 0) {
						long credits = balanceEntry.getCredits();
						long adminCredit = masterbalance.getCredits();
						boolean amount = false;
						boolean inherit = false;
						// System.out.println("admin pre Credit amount::::"+adminCredit);
						if (user.isAdminDepend() && (adminCredit > (destinationList.size() * no_of_msg))) {
							amount = true;
							inherit = true;
						} else if (credits > (destinationList.size() * no_of_msg)) {
							amount = true;
						}
						if (amount) {
							if (inherit) {
								adminCredit = adminCredit - (destinationList.size() * no_of_msg);
								masterbalance.setCredits(adminCredit);
								balanceEntryRepository.save(masterbalance);
							} else {
								credits = credits - (destinationList.size() * no_of_msg);
								balanceEntry.setCredits(credits);
								balanceEntryRepository.save(balanceEntry);
							}
							long deductCredits = destinationList.size() * no_of_msg;
							// String applicationName = request.getContextPath();
							bulkSmsDTO.setMsgCount(deductCredits);
							SendSmsService service = new SendSmsService();
							if (bulkSmsDTO.isSchedule()) {
								String filename = service.createScheduleFile(bulkSmsDTO);
								int generated_id = 0;
								if (filename != null) {
									ScheduleEntry sch = new ScheduleEntry();
									sch.setClientGmt(bulkSmsDTO.getGmt());
									sch.setClientTime(bulkSmsDTO.getTimestart());
									sch.setFileName(filename);
									sch.setRepeated(bulkSmsDTO.getRepeat());
									sch.setScheduleType(bulkSmsDTO.getReqType());
									sch.setServerId(IConstants.SERVER_ID);
									sch.setServerTime(bulkSmsDTO.getTime());
									sch.setStatus("false");
									sch.setUsername(bulkSmsDTO.getSystemId());
									sch.setDate(bulkSmsDTO.getDate());
									sch.setWebId(null);
									generated_id = scheduleEntryRepository.save(sch).getId();
									if (generated_id > 0) {
										String today = Validation.getTodayDateFormat();
										if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
											Set<Integer> set = null;
											if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
												set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
											} else {
												set = new LinkedHashSet<Integer>();
											}
											set.add(generated_id);
											GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
										}
										if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
											GlobalVarsSms.RepeatedSchedules.add(generated_id);
										}
										target = IConstants.SUCCESS_KEY;
										logger.info("Task scheduled successfully.");
									} else {
										throw new ScheduledTimeException(
												"Error: Unable to schedule the task. An error occurred during the scheduling process.");

									}
								} else {
									logger.error(
											"Error: The task is already scheduled. Duplicate schedule request received.");
									throw new ScheduledTimeException(
											"Error: The task is already scheduled. Duplicate schedule request received.");

								}
							} else {
								String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									// Batch Success
									logger.info(
											"Batch processing completed successfully. Additional details: " + value);

								} else {
									// Submission Error
									logger.error(
											"Error: Unable to process batch submission. An error occurred during the submission process.");

									throw new InternalServerException(
											"Error: Unable to process batch submission. An error occurred during the submission process.");

								}
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(credits + "");
								bulkResponse.setDeductcredits(deductCredits + "");
								bulkResponse.setBulkSessionId(bulkSessionId);

								logger.info(bulkSessionId + " Processed :-> Credits: " + credits + " Deduct: "
										+ deductCredits);
							} else {
								logger.info(bulkSessionId + "<-- Process Failed --> ");
								throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							// insufficient Credits
							logger.error(bulkSessionId + " <-- Insufficient Credits --> " + credits);
							throw new InsufficientBalanceException(
									bulkSessionId + " <-- Insufficient Credits --> " + credits);

						}
					} else {
						// Number File Error
						logger.error(
								"Error: No valid numbers found in the provided data. Please check the data and try again.");
						throw new InternalServerException(
								"Error: No valid numbers found in the provided data. Please check the data and try again.");
					}
				} else if (wallet_flag.equalsIgnoreCase("MIN")) {
					// Insufficient Balance
					logger.error(
							"Error: Insufficient balance to perform the operation. Please add funds to your wallet.");
					throw new InternalServerException(
							"Error: Insufficient balance to perform the operation. Please add funds to your wallet.");
				}
			} catch (InvalidPropertyException e) {
				logger.error(bulkSessionId, e.getMessage());
				throw new InvalidPropertyException(e.getMessage());
			} catch (NotFoundException e) {
				logger.error(bulkSessionId, e.getMessage());
				throw new NotFoundException(e.getMessage());
			} catch (InsufficientBalanceException e) {
				logger.error(bulkSessionId, e.getMessage());
				throw new InsufficientBalanceException(e.getMessage());
			} catch (InternalServerException e) {
				logger.error(bulkSessionId, e.getMessage());
				throw new InternalServerException(e.getMessage());
			} catch (ScheduledTimeException e) {
				logger.error(bulkSessionId, e.getMessage());
				throw new ScheduledTimeException(e.getMessage());
			} catch (Exception e) {
				logger.error(bulkSessionId, e);
				throw new InternalServerException(e.getMessage());
			}
			bulkResponse.setStatus(target);
			return ResponseEntity.ok(bulkResponse);

		}
	}

	private ResponseEntity<?> tracking(BulkContactRequest uploadForm, BulkSmsDTO bulkSmsDTO, WebMasterEntry webEntry,
			DriverInfo driverInfo, UserEntry user) {
		BulkResponse bulkResponse = new BulkResponse();
		String target = IConstants.FAILURE_KEY;
		double totalcost = 0, adminCost = 0;// total_defcost = 0;
		String unicodeMsg = "";
		String systemId = user.getSystemId();
		String bulkSessionId = systemId + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		if (bulkSmsDTO.isSchedule()) {
			logger.info(bulkSessionId + " Contact Tracking Schedule Request");
		} else {
			logger.info(bulkSessionId + " Contact Tracking Upload Request");
		}
		try {
			bulkSmsDTO.setCustomContent(true);
			int no_of_msg = bulkSmsDTO.getSmsParts();
			Optional<BalanceEntry> masterBalanceOptional = balanceEntryRepository.findBySystemId((user.getMasterId()));
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

			BulkListInfo listInfo = new BulkListInfo();
			List<String> destinationList = null;
			if (webEntry.isPrefixApply()) {
				destinationList = new ArrayList<String>();
				List<String> tempList = bulkSmsDTO.getDestinationList2(listInfo);
				for (String number : tempList) {
					if (number.length() < webEntry.getNumberLength()) {
						System.out.println(number + " length is less then " + webEntry.getNumberLength());
						number = webEntry.getPrefixToApply() + number;
					}
					destinationList.add(number);
				}
			} else {
				destinationList = bulkSmsDTO.getDestinationList2(listInfo);
			}
			logger.info(bulkSessionId + " Total:" + listInfo.getTotal() + " Valid:" + listInfo.getValidCount()
					+ " Invalid:" + listInfo.getInvalidCount() + " Duplicate: " + listInfo.getDuplicate());

			if (bulkSmsDTO.getMessageType().equalsIgnoreCase("Unicode")) {
				bulkSmsDTO.setMessage(UTF16(bulkSmsDTO.getMessage()));
				bulkSmsDTO.setOrigMessage(UTF16(bulkSmsDTO.getMessage()));
				bulkSmsDTO.setDistinct("yes");
			} else {
				String sp_msg = bulkSmsDTO.getMessage();
				String hexValue = getHexValue(sp_msg);
				unicodeMsg = SmsConverter.getContent(hexValue.toCharArray());
				bulkSmsDTO.setMessage(unicodeMsg);
				bulkSmsDTO.setMessageType("SpecialChar");
				bulkSmsDTO.setOrigMessage(UTF16(bulkSmsDTO.getMessage()));
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
				if (!valid_sch_time) {
					String errorMessage = "Invalid schedule time provided. Please make sure to provide a valid time.";
					logger.error(errorMessage);
					throw new ScheduledTimeException(errorMessage);

				}
			}
			// ---------------- url appending & message calculation --------------------
			Hashtable<String, List<String>> msgmapping = new Hashtable<String, List<String>>(); // for mapping number
																								// with message content
			Map<String, Integer> msgLengthTable = new HashMap<String, Integer>(); // for balance calculation
			int totalMsg = 0; // FOR CREDIT CALCULATION
			String campaign_name = bulkSmsDTO.getCampaignName();
			System.out.println("Received Web Links: " + uploadForm.getWeblink().length);
			List<String> web_links_list = new ArrayList<String>();
			for (String link : uploadForm.getWeblink()) {
				if (link != null && link.length() > 0) {
					web_links_list.add(link);
				}
			}
			System.out.println("Final Web Links: " + web_links_list);
			Map<String, String> campaign_mapping = getCampaignId(systemId, bulkSmsDTO.getSenderId(),
					IConstants.GATEWAY_NAME, web_links_list, String.join(",", destinationList), campaign_name);
			String web_link_hex_param = null;
			System.out.println("campaign mapping " + campaign_mapping);
			if (bulkSmsDTO.getMessageType().equalsIgnoreCase("Unicode")) {
				web_link_hex_param = "005B007700650062005F006C0069006E006B005F0074007200610063006B0069006E0067005F00750072006C005D"
						.toLowerCase();
			} else {
				web_link_hex_param = SevenBitChar.getHexValue(
						"91,119,101,98,95,108,105,110,107,95,116,114,97,99,107,105,110,103,95,117,114,108,93,");
				web_link_hex_param = SmsConverter.getContent(web_link_hex_param.toCharArray());
			}
			int number_serial = 1;
			for (String destination : destinationList) {
				String msg_content = bulkSmsDTO.getMessage();
				System.out.println("msg_content" + msg_content);
				for (int i = 0; i < web_links_list.size(); i++) {
					if (campaign_mapping.containsKey(web_links_list.get(i))) {
						String appending_url = "http://1l.ae/" + campaign_mapping.get(web_links_list.get(i)) + "/r="
								+ number_serial;
						System.out.println("this appending url" + appending_url);
						if (bulkSmsDTO.getMessageType().equalsIgnoreCase("Unicode")) {
							msg_content = msg_content.replaceFirst(web_link_hex_param,
									UTF16(appending_url).toLowerCase());
						} else {
							msg_content = msg_content.replaceFirst(web_link_hex_param, appending_url);
							System.out.println("msg content " + msg_content);
						}
					}
				}
				int msg_length = msg_content.length();
				if (bulkSmsDTO.getMessageType().equalsIgnoreCase("Unicode")) {
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
				List<String> msgList = new ArrayList<String>();
				msgList.add(msg_content);
				System.out.println("this is message list" + msgList);
				msgmapping.put(destination, msgList);
				msgLengthTable.put(destination, no_of_msg);
				totalMsg = totalMsg + no_of_msg;
				number_serial++;
			}
			// bulkSmsDTO.setDestinationList(new ArrayList<String>(numberslist));
			System.out.println("this is smg mapping " + msgmapping);
			bulkSmsDTO.setMapTable(msgmapping);
			Optional<UserEntry> masterOptional = userEntryRepository.findBySystemId(user.getMasterId());
			// -----------------------------------------------------------------
			if (wallet_flag.equalsIgnoreCase("yes")) {
				bulkSmsDTO.setUserMode("wallet");
				if (!destinationList.isEmpty()) {
					totalcost = routeService.calculateRoutingCost(user.getId(), msgLengthTable);
					logger.info(bulkSessionId + " Balance:" + wallet + " Calculated Cost: " + totalcost);
					boolean amount = false;
					// boolean inherit = false;
					if (user.isAdminDepend()) {

						if (!masterOptional.isPresent()) {
							throw new NotFoundException("User not found with the provided username.");
						}
						adminCost = routeService.calculateRoutingCost(masterOptional.get().getId(), msgLengthTable);
						logger.info(bulkSessionId + " Admin[" + masterOptional.get().getId() + "] Balance:"
								+ adminWallet + " Calculated Cost: " + adminCost);
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
								logger.error(bulkSessionId + " <-- Insufficient Balance -->");
								throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Balance -->");

							}
						} else {
							// Insufficient Admin balance
							logger.error(bulkSessionId + " " + "<-- Insufficient Admin(" + masterOptional.get().getId()
									+ ") Balance -->");
							throw new InsufficientBalanceException(bulkSessionId + " " + "<-- Insufficient Admin("
									+ masterOptional.get().getId() + ") Balance -->");
						}
					} else {
						if (wallet > 0 && wallet >= totalcost) {
							wallet = wallet - totalcost;
							balanceEntry.setWalletAmount(wallet);
							amount = true;
							balanceEntryRepository.save(balanceEntry);
						} else {
							// Insufficient balance
							logger.error(bulkSessionId + " <-- Insufficient Balance -->");
							throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Balance -->");
						}
					}
					if (amount) {
						// String applicationName = request.getContextPath();
						bulkSmsDTO.setMsgCount(totalMsg);
						bulkSmsDTO.setTotalCost(totalcost);
						SendSmsService service = new SendSmsService();
						if (bulkSmsDTO.isSchedule()) {
							bulkSmsDTO.setTotalWalletCost(totalcost);
							String filename = service.createScheduleFile(bulkSmsDTO);
							int generated_id = 0;
							if (filename != null) {
								ScheduleEntry sch = new ScheduleEntry();
								sch.setClientGmt(bulkSmsDTO.getGmt());
								sch.setClientTime(bulkSmsDTO.getTimestart());
								sch.setFileName(filename);
								sch.setRepeated(bulkSmsDTO.getRepeat());
								sch.setScheduleType(bulkSmsDTO.getReqType());
								sch.setServerId(IConstants.SERVER_ID);
								sch.setServerTime(bulkSmsDTO.getTime());
								sch.setStatus("false");
								sch.setUsername(bulkSmsDTO.getSystemId());
								sch.setDate(bulkSmsDTO.getDate());
								sch.setWebId(null);
								generated_id = scheduleEntryRepository.save(sch).getId();
								if (generated_id > 0) {
									String today = Validation.getTodayDateFormat();
									if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
										Set<Integer> set = null;
										if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
											set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
										} else {
											set = new LinkedHashSet<Integer>();
										}
										set.add(generated_id);
										GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
									}
									if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
										GlobalVarsSms.RepeatedSchedules.add(generated_id);
									}
									target = IConstants.SUCCESS_KEY;
									logger.info("Schedule has been successfully updated.");
								} else {
									// Scheduling Error
									String message = "An error occurred while processing the schedule. Please try again later.";
									logger.error(message);
									throw new ScheduledTimeException(message);
								}
							} else {
								// already Scheduled
								String message = "Another schedule with the same information already exists. Please provide unique schedule details.";
								logger.error(message);
								throw new ScheduledTimeException(message);
							}
						} else {
							String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info("Batch processing completed successfully.");
							} else {
								// Submission Error
								String message = "Error occurred during batch processing. Please check your input and try again.";
								logger.error(message);
								throw new InternalServerException(message);
							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setBulkListInfo(listInfo);
							bulkResponse.setBulkSessionId(bulkSessionId);
							bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
							bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
							logger.info(bulkSessionId + " Processed :-> Balance: " + wallet + " Cost: " + totalcost);
						} else {
							logger.info(bulkSessionId + "<-- Process Failed --> ");
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// insufficient balance
						String message = "Insufficient funds in your wallet. Please add funds to proceed.";
						logger.error(message);
						throw new InsufficientBalanceException(message);
					}
				} else {
					// Number File Error
					String message = "Invalid or no valid number file provided. Please check and upload a valid number file.";
					logger.error(message);
					throw new InternalServerException(message);
				}
			} else if (wallet_flag.equalsIgnoreCase("no")) {
				bulkSmsDTO.setUserMode("credit");
				if (!destinationList.isEmpty()) {
					long credits = balanceEntry.getCredits();
					long adminCredit = masterbalance.getCredits();
					boolean amount = false;
					// boolean inherit = false;
					if (user.isAdminDepend()) {
						if (adminCredit >= totalMsg) {
							if (credits >= totalMsg) {
								adminCredit = adminCredit - totalMsg;
								masterbalance.setCredits(adminCredit);
								credits = credits - totalMsg;
								balanceEntry.setCredits(credits);
								amount = true;
								balanceEntryRepository.save(masterbalance);
								balanceEntryRepository.save(balanceEntry);
							} else {
								logger.error(bulkSessionId + " <-- Insufficient Credits -->");
								throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Credits -->");
							}
						} else {
							logger.info(bulkSessionId + " <-- Insufficient Admin(" + masterOptional.get().getId()
									+ ") Credits -->");
							throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Admin("
									+ masterOptional.get().getId() + ") Credits -->");

						}
					} else {
						if (credits >= totalMsg) {
							credits = credits - totalMsg;
							balanceEntry.setCredits(credits);
							amount = true;
							balanceEntryRepository.save(balanceEntry);
						} else {
							logger.error(bulkSessionId + " <-- Insufficient Credits -->");
							throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Credits -->");

						}
					}
					if (amount) {
						// long deductCredits = numberslist.size() * no_of_msg;
						// String applicationName = request.getContextPath();
						bulkSmsDTO.setMsgCount(totalMsg);
						SendSmsService service = new SendSmsService();
						if (bulkSmsDTO.isSchedule()) {
							String filename = service.createScheduleFile(bulkSmsDTO);
							int generated_id = 0;
							if (filename != null) {
								ScheduleEntry sch = new ScheduleEntry();
								sch.setClientGmt(bulkSmsDTO.getGmt());
								sch.setClientTime(bulkSmsDTO.getTimestart());
								sch.setFileName(filename);
								sch.setRepeated(bulkSmsDTO.getRepeat());
								sch.setScheduleType(bulkSmsDTO.getReqType());
								sch.setServerId(IConstants.SERVER_ID);
								sch.setServerTime(bulkSmsDTO.getTime());
								sch.setStatus("false");
								sch.setUsername(bulkSmsDTO.getSystemId());
								sch.setDate(bulkSmsDTO.getDate());
								sch.setWebId(null);
								generated_id = scheduleEntryRepository.save(sch).getId();
								if (generated_id > 0) {
									String today = Validation.getTodayDateFormat();
									if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
										Set<Integer> set = null;
										if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
											set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
										} else {
											set = new LinkedHashSet<Integer>();
										}
										set.add(generated_id);
										GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
									}
									if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
										GlobalVarsSms.RepeatedSchedules.add(generated_id);
									}
									target = IConstants.SUCCESS_KEY;
									String successMessage = "Scheduled successfully. Your task is now in the queue.";
									logger.info(successMessage);
								} else {
									String duplicateScheduleMessage = "Error: Duplicate schedule. A similar task is already scheduled.";
									logger.error(duplicateScheduleMessage);
									throw new ScheduledTimeException(duplicateScheduleMessage);
								}
							} else {
								String scheduleErrorMessage = "Error: Failed to schedule task. Please check your inputs and try again.";
								logger.error(scheduleErrorMessage);
								throw new ScheduledTimeException(scheduleErrorMessage);
							}
						} else {
							String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info("Batch processed successfully. Your changes have been applied.");

							} else {
								// Submission Error
								String message = ("Error processing batch. Please check your input and try again.");
								logger.error(message);
								throw new InternalServerException(message);

							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setBulkListInfo(listInfo);
							bulkResponse.setCredits(credits + "");
							bulkResponse.setDeductcredits(totalMsg + "");
							bulkResponse.setBulkSessionId(bulkSessionId);

							logger.info(bulkSessionId + " Processed :-> Credits: " + credits + " Deduct: " + totalMsg);
						} else {
							logger.error(bulkSessionId + "<-- Process Failed --> ");
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						logger.error("Error: Insufficient credits to perform the operation. Please add more credits.");
						throw new InsufficientBalanceException(
								"Error: Insufficient credits to perform the operation. Please add more credits.");
					}
				} else {

					logger.error(
							"Error: No valid numbers found in the provided file. Please check the file and try again.");
					throw new InternalServerException(
							"Error: No valid numbers found in the provided file. Please check the file and try again.");

				}
			} else if (wallet_flag.equalsIgnoreCase("MIN")) {
				// Insufficient Balance
				logger.error("Error: Insufficient balance to perform the operation. Please add funds to your wallet.");
				throw new InsufficientBalanceException(
						"Error: Insufficient balance to perform the operation. Please add funds to your wallet.");
			}
		} catch (InvalidPropertyException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InvalidPropertyException(e.getMessage());
		} catch (NotFoundException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (InsufficientBalanceException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InsufficientBalanceException(e.getMessage());
		} catch (InternalServerException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InternalServerException(e.getMessage());
		} catch (ScheduledTimeException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new ScheduledTimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(bulkSessionId, e);
			throw new InternalServerException(e.getMessage());
		}
		bulkResponse.setStatus(target);
		return ResponseEntity.ok(bulkResponse);

	}

	@Override
	public ResponseEntity<?> sendSmsGroupData(BulkContactRequest bulkContactRequest, String username) {
		BulkResponse bulkResponse = new BulkResponse();
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user.getId());
		DriverInfo driverInfo = null;
		Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(user.getId());
		if (OptionalDriverInfo.isPresent()) {
			driverInfo = OptionalDriverInfo.get();
		} else
			throw new NotFoundException("drive info  not found with the provided username.");

		BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
		bulkSmsDTO.setUploadedNumbers(bulkContactRequest.getUploadedNumbers());
		bulkSmsDTO.setMessage(bulkContactRequest.getMessage());
		bulkSmsDTO.setFrom(bulkContactRequest.getFrom());
		bulkSmsDTO.setSmscount(bulkContactRequest.getSmscount());
		bulkSmsDTO.setDelay(bulkContactRequest.getDelay());
		bulkSmsDTO.setSchedule(bulkContactRequest.isSchedule());
		bulkSmsDTO.setAlert(bulkContactRequest.isAlert());
		bulkSmsDTO.setMessageType(bulkContactRequest.getMessageType());
		bulkSmsDTO.setSmsParts(bulkContactRequest.getSmsParts());
		bulkSmsDTO.setCharCount(bulkContactRequest.getCharCount());
		bulkSmsDTO.setCharLimit(bulkContactRequest.getCharLimit());
		bulkSmsDTO.setExclude(bulkContactRequest.getExclude());
		bulkSmsDTO.setExpiryHour(bulkContactRequest.getExpiryHour());
		bulkSmsDTO.setCampaignName(bulkContactRequest.getCampaignName());
		bulkSmsDTO.setClientId(user.getSystemId());
		bulkSmsDTO.setSystemId(user.getSystemId());
		bulkSmsDTO.setPassword(new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
		bulkSmsDTO.setSenderId(bulkContactRequest.getSenderId());
		bulkSmsDTO.setCustomContent(false);
		bulkSmsDTO.setTotalNumbers(bulkContactRequest.isTotalContact());

		if (bulkContactRequest.getDestinationNumber() != null)
			bulkSmsDTO.setDestinationNumber(bulkContactRequest.getDestinationNumber());
		if (bulkContactRequest.isSchedule()) {
			bulkSmsDTO.setTimestart(bulkContactRequest.getTimestart());
			bulkSmsDTO.setRepeat(bulkContactRequest.getRepeat());
			bulkSmsDTO.setGmt(bulkContactRequest.getGmt());
		}
		if (bulkContactRequest.getPeId() != null)
			bulkSmsDTO.setPeId(bulkContactRequest.getPeId());
		if (bulkContactRequest.getTelemarketerId() != null)
			bulkSmsDTO.setTelemarketerId(bulkContactRequest.getTelemarketerId());
		if (bulkContactRequest.getTemplateId() != null)
			bulkSmsDTO.setTemplateId(bulkContactRequest.getTemplateId());

		String target = IConstants.FAILURE_KEY;
		double totalcost = 0;
		int no_of_msg = 0;
		String unicodeMsg = "";
		long[] numbers;
		// String number = "";
		SendSmsService service = new SendSmsService();
		int groupId = bulkContactRequest.getGroupId();
		String systemId = bulkSmsDTO.getSystemId();
		bulkSmsDTO.setCustomContent(true);
		bulkSmsDTO.setReqType("GroupData");
		String bulkSessionId = systemId + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		if (bulkSmsDTO.isSchedule()) {
			logger.info(bulkSessionId + " GroupData Schedule Request. Tracking: " + bulkContactRequest.isTracking());
		} else {
			logger.info(bulkSessionId + " GroupData BulkUpload Request. Tracking: " + bulkContactRequest.isTracking());
		}
		try {

			if (bulkContactRequest.getMessageType().equalsIgnoreCase("Unicode")) {
				bulkSmsDTO.setMessage(UTF16(bulkContactRequest.getMessage()));
				bulkSmsDTO.setOrigMessage(UTF16(bulkContactRequest.getMessage()));
				bulkSmsDTO.setDistinct("yes");
			} else {
				String sp_msg = bulkContactRequest.getMessage();
				String hexValue = getHexValue(sp_msg);
				unicodeMsg = SmsConverter.getContent(hexValue.toCharArray());
				bulkSmsDTO.setMessage(unicodeMsg);
				bulkSmsDTO.setMessageType("SpecialChar");
				bulkSmsDTO.setOrigMessage(UTF16(bulkContactRequest.getMessage()));
			}

			BulkListInfo listInfo = new BulkListInfo();
			if (bulkSmsDTO.getMessageType().equalsIgnoreCase("7bit")) {
				String sp_msg = bulkContactRequest.getMessage();
				unicodeMsg = SmsConverter.getContent(sp_msg.toCharArray());
				bulkSmsDTO.setMessage(unicodeMsg);
				bulkSmsDTO.setMessageType("SpecialChar");
			}
			String msgType = bulkSmsDTO.getMessageType();

			Optional<BalanceEntry> masterBalanceOptional = balanceEntryRepository.findBySystemId((user.getMasterId()));
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
			List<String> destinationList = bulkSmsDTO.getDestinationList2(listInfo);
			numbers = new long[destinationList.size()];
			Iterator itr = destinationList.iterator();
			// ***************Adding Numbers*********************
			int number_count = 0;
			while (itr.hasNext()) {
				String number = (String) itr.next();
				try {
					numbers[number_count] = Long.parseLong(number);
					number_count++;
				} catch (Exception ex) {
					logger.error(systemId + " Invalid Number: " + number);
					throw new InternalServerException(systemId + " Invalid Number: " + number + ex.getMessage());
				}
			}
			// ***************Adding Numbers*********************
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
					throw new ScheduledTimeException(
							"Error: getting error in Scheduler bulkSessionId " + bulkSessionId);
				}
			}
			// ********************Start Calculating message Length for Each Number
			// ***************
			// String keyNumber = "", initial, fname, mName, lName;
			Hashtable mapTable = new Hashtable();
			Map<String, Integer> msgLengthTable = new HashMap<String, Integer>();
			int msg_length = 0;
			int totalMsg = 0;
			String msg = "";
			if (bulkSmsDTO.getMessageType().equalsIgnoreCase("SpecialChar")) {
				msg = bulkSmsDTO.getMessage();
			} else {
				msg = SmsConverter.uniHexToCharMsg(bulkSmsDTO.getMessage());
			}
			SearchCriteria criteria = new SearchCriteria();
			criteria.setGroupId(groupId);
			criteria.setNumber(numbers);
			String tempMsg = "";
			// ContactDAService contact_service = new ContactDAServiceImpl();
			List<GroupDataEntry> list = listGroupData(criteria);

			if (list != null && !list.isEmpty()) {
				Map<String, List<GroupDataEntry>> number_wise_entry_map = new LinkedHashMap<String, List<GroupDataEntry>>();
				for (GroupDataEntry entry : list) {
					String number = String.valueOf(entry.getNumber());
					if (webEntry.isPrefixApply()) {
						if (number.length() < webEntry.getNumberLength()) {
							System.out.println(number + " length is less then " + webEntry.getNumberLength());
							number = webEntry.getPrefixToApply() + number;
						}
					}
					List<GroupDataEntry> entry_list = null;
					if (number_wise_entry_map.containsKey(number)) {
						entry_list = number_wise_entry_map.get(number);
					} else {
						entry_list = new ArrayList<GroupDataEntry>();
					}
					entry_list.add(entry);
					number_wise_entry_map.put(number, entry_list);
				}
				if (bulkContactRequest.isTracking()) {
					String campaign_name = bulkContactRequest.getCampaignName();
					System.out.println("Received Web Links: " + bulkContactRequest.getWeblink().length);
					List<String> web_links_list = new ArrayList<String>();
					for (String link : bulkContactRequest.getWeblink()) {
						if (link != null && link.length() > 0) {
							web_links_list.add(link);
						}
					}
					System.out.println("Final Web Links: " + web_links_list);
					Map<String, String> campaign_mapping = getCampaignId(systemId, bulkSmsDTO.getSenderId(),
							IConstants.GATEWAY_NAME, web_links_list, String.join(",", number_wise_entry_map.keySet()),
							campaign_name);
					String web_link_hex_param = null;
					if (bulkSmsDTO.getMessageType().equalsIgnoreCase("Unicode")) {
						web_link_hex_param = "[web_link_tracking_url]";
					} else {
						web_link_hex_param = SevenBitChar.getHexValue(
								"91,119,101,98,95,108,105,110,107,95,116,114,97,99,107,105,110,103,95,117,114,108,93,");
						web_link_hex_param = SmsConverter.getContent(web_link_hex_param.toCharArray());
					}
					int number_serial = 1;
					for (Map.Entry<String, List<GroupDataEntry>> map_entry : number_wise_entry_map.entrySet()) {
						// String appending_url = " http://1l.ae/" + campaignid + "/r=" + number_serial;
						String number = map_entry.getKey();
						int msg_count_per_number = 0;
						List msgList = new ArrayList();
						for (GroupDataEntry entry : map_entry.getValue()) {
							tempMsg = msg;
							// -----------------------------------
							if (tempMsg.contains("initial") && entry.getInitials() != null) {
								tempMsg = !entry.getInitials().isEmpty()
										? tempMsg.replaceAll("initial",
												SmsConverter.hexCodePointsToCharMsg(entry.getInitials()))
										: tempMsg.replaceAll("initial", "");
							}

							if (tempMsg.contains("firstname") && entry.getFirstName() != null) {
								tempMsg = !entry.getFirstName().isEmpty()
										? tempMsg.replaceAll("firstname",
												SmsConverter.hexCodePointsToCharMsg(entry.getFirstName()))
										: tempMsg.replaceAll("firstname", "");
							}
							if (tempMsg.contains("middlename") && entry.getMiddleName() != null) {
								tempMsg = !entry.getMiddleName().isEmpty()
										? tempMsg.replaceAll("middlename",
												SmsConverter.hexCodePointsToCharMsg(entry.getMiddleName()))
										: tempMsg.replaceAll("middlename", "");
							}
							if (tempMsg.contains("lastname") && entry.getLastName() != null) {
								tempMsg = !entry.getLastName().isEmpty()
										? tempMsg.replaceAll("lastname",
												SmsConverter.hexCodePointsToCharMsg(entry.getLastName()))
										: tempMsg.replaceAll("lastname", "");
							}
							for (int i = 0; i < web_links_list.size(); i++) {
								if (campaign_mapping.containsKey(web_links_list.get(i))) {
									String appending_url = "http://1l.ae/" + campaign_mapping.get(web_links_list.get(i))
											+ "/r=" + number_serial;
									tempMsg = tempMsg.replaceFirst(web_link_hex_param, appending_url);
								}
							}
							if (msgType.equalsIgnoreCase("Unicode")) {
								tempMsg = UTF16(tempMsg);
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
								// msg = tmp_msg + appending_url;
								msg_length = tempMsg.length();
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
							msgList.add(tempMsg);
						}
						mapTable.put(number, msgList);
						msgLengthTable.put(number, msg_count_per_number); // FOR WALLET CALCULATION
						number_serial++;
					}
				} else {
					for (Map.Entry<String, List<GroupDataEntry>> map_entry : number_wise_entry_map.entrySet()) {
						String number = map_entry.getKey();
						int msg_count_per_number = 0;
						List msgList = new ArrayList();
						for (GroupDataEntry entry : map_entry.getValue()) {
							tempMsg = msg;
							if (tempMsg.contains("initial") && entry.getInitials() != null) {
								tempMsg = !entry.getInitials().isEmpty()
										? tempMsg.replaceAll("initial",
												SmsConverter.hexCodePointsToCharMsg(entry.getInitials()))
										: tempMsg.replaceAll("initial", "");
							}

							if (tempMsg.contains("firstname") && entry.getFirstName() != null) {
								tempMsg = !entry.getFirstName().isEmpty()
										? tempMsg.replaceAll("firstname",
												SmsConverter.hexCodePointsToCharMsg(entry.getFirstName()))
										: tempMsg.replaceAll("firstname", "");
							}
							if (tempMsg.contains("middlename") && entry.getMiddleName() != null) {
								tempMsg = !entry.getMiddleName().isEmpty()
										? tempMsg.replaceAll("middlename",
												SmsConverter.hexCodePointsToCharMsg(entry.getMiddleName()))
										: tempMsg.replaceAll("middlename", "");
							}
							if (tempMsg.contains("lastname") && entry.getLastName() != null) {
								tempMsg = !entry.getLastName().isEmpty()
										? tempMsg.replaceAll("lastname",
												SmsConverter.hexCodePointsToCharMsg(entry.getLastName()))
										: tempMsg.replaceAll("lastname", "");
							}
							if (msgType.equalsIgnoreCase("Unicode")) {
								tempMsg = UTF16(tempMsg);
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
								// msg = tmp_msg + appending_url;
								msg_length = tempMsg.length();
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
							msgList.add(tempMsg);
						}
						mapTable.put(number, msgList);
						msgLengthTable.put(number, msg_count_per_number); // FOR WALLET CALCULATION
					}
				}
				if (msgType.equalsIgnoreCase("Unicode")) {
					bulkSmsDTO.setDistinct("yes");
				}
				bulkSmsDTO.setDestinationList(destinationList);
				bulkSmsDTO.setMapTable(mapTable);
			}
			// ********************Calculating message Length for Each Number
			// ***************
			// **********************************Start Wallet
			// Calculation*********************
			if (wallet_flag.equalsIgnoreCase("yes")) {
				bulkSmsDTO.setUserMode("wallet");
				totalcost = routeService.calculateRoutingCost(user.getId(), msgLengthTable);
				logger.info(bulkSessionId + " Balance:" + wallet + " Calculated Cost: " + totalcost);
				if (destinationList.size() > 0) {
					boolean amount = false;
					boolean inherit = false;
					if (user.isAdminDepend() && (adminWallet >= totalcost)) {
						amount = true;
						inherit = true;
					} else if (wallet >= totalcost) {
						amount = true;
					}
					if (amount) {
						if (inherit) {
							adminWallet = adminWallet - totalcost;
							masterbalance.setWalletAmount(adminWallet);
							balanceEntryRepository.save(masterbalance);
						} else {
							wallet = wallet - totalcost;
							balanceEntry.setWalletAmount(wallet);
							balanceEntryRepository.save(balanceEntry);
						}
						// String applicationName = request.getContextPath();
						bulkSmsDTO.setMsgCount(totalMsg);
						bulkSmsDTO.setTotalCost(totalcost);
						if (bulkSmsDTO.isSchedule()) {
							bulkSmsDTO.setTotalWalletCost(totalcost);
							String filename = service.createScheduleFile(bulkSmsDTO);
							int generated_id = 0;
							if (filename != null) {
								ScheduleEntry sch = new ScheduleEntry();
								sch.setClientGmt(bulkSmsDTO.getGmt());
								sch.setClientTime(bulkSmsDTO.getTimestart());
								sch.setFileName(filename);
								sch.setRepeated(bulkSmsDTO.getRepeat());
								sch.setScheduleType(bulkSmsDTO.getReqType());
								sch.setServerId(IConstants.SERVER_ID);
								sch.setServerTime(bulkSmsDTO.getTime());
								sch.setStatus("false");
								sch.setUsername(bulkSmsDTO.getSystemId());
								sch.setDate(bulkSmsDTO.getDate());
								sch.setWebId(null);
								generated_id = scheduleEntryRepository.save(sch).getId();
								if (generated_id > 0) {
									String today = Validation.getTodayDateFormat();
									if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
										Set<Integer> set = null;
										if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
											set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
										} else {
											set = new LinkedHashSet<Integer>();
										}
										set.add(generated_id);
										GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
									}
									if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
										GlobalVarsSms.RepeatedSchedules.add(generated_id);
									}
									target = IConstants.SUCCESS_KEY;
									logger.info("Task scheduled successfully.");
								} else {
									throw new ScheduledTimeException(
											"Error: Unable to schedule the task. An error occurred during the scheduling process.");

								}
							} else {
								logger.error(
										"Error: The task is already scheduled. Duplicate schedule request received.");
								throw new ScheduledTimeException(
										"Error: The task is already scheduled. Duplicate schedule request received.");

							}
						} else {
							String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;

								String message = ("Batch processing completed successfully. Message: " + value);
								logger.info(message);
							} else {
								// Submission Error
								String message = ("Error: Unable to process batch submission. An error occurred during the submission process.");
								logger.error(message);
								throw new InternalServerException(message);
							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setBulkListInfo(listInfo);
							bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
							bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost) + "");
							bulkResponse.setBulkSessionId(bulkSessionId);
						}
					} else {
						// insufficient Credits
						logger.error(bulkSessionId + " <-- Insufficient Balance --> " + wallet);
						throw new InsufficientBalanceException(
								bulkSessionId + " <-- Insufficient Balance --> " + wallet);
					}
				} else {
					// Number File Error
					String message = ("Error: No valid numbers found in the provided data. Please check the data and try again.");
					logger.error(message);
					throw new InternalServerException(message);
				}

			} // ******************************End Wallet Calculation*********************
				// ******************************Start Credit Calculation*********************
			else if (wallet_flag.equalsIgnoreCase("no")) {
				bulkSmsDTO.setUserMode("credit");
				long credits = balanceEntry.getCredits();
				long adminCredit = masterbalance.getCredits();
				boolean amount = false;
				boolean inherit = false;
				if (user.isAdminDepend() && (adminCredit > totalMsg)) {
					amount = true;
					inherit = true;
				} else if (credits > totalMsg) {
					amount = true;
				}
				if (amount) {
					if (inherit) {
						adminCredit = adminCredit - totalMsg;
						masterbalance.setCredits(adminCredit);
						balanceEntryRepository.save(masterbalance);
					} else {
						credits = credits - totalMsg;
						balanceEntry.setCredits(credits);
						balanceEntryRepository.save(balanceEntry);
					}
					long deductCredits = totalMsg;
					// String applicationName = request.getContextPath();
					bulkSmsDTO.setMsgCount(deductCredits);
					if (bulkSmsDTO.isSchedule()) {
						String filename = service.createScheduleFile(bulkSmsDTO);
						int generated_id = 0;
						if (filename != null) {
							ScheduleEntry sch = new ScheduleEntry();
							sch.setClientGmt(bulkSmsDTO.getGmt());
							sch.setClientTime(bulkSmsDTO.getTimestart());
							sch.setFileName(filename);
							sch.setRepeated(bulkSmsDTO.getRepeat());
							sch.setScheduleType(bulkSmsDTO.getReqType());
							sch.setServerId(IConstants.SERVER_ID);
							sch.setServerTime(bulkSmsDTO.getTime());
							sch.setStatus("false");
							sch.setUsername(bulkSmsDTO.getSystemId());
							sch.setDate(bulkSmsDTO.getDate());
							sch.setWebId(null);
							generated_id = scheduleEntryRepository.save(sch).getId();
							if (generated_id > 0) {
								String today = Validation.getTodayDateFormat();
								if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
									Set<Integer> set = null;
									if (GlobalVarsSms.ScheduledBatches.containsKey(bulkSmsDTO.getTime())) {
										set = GlobalVarsSms.ScheduledBatches.get(bulkSmsDTO.getTime());
									} else {
										set = new LinkedHashSet<Integer>();
									}
									set.add(generated_id);
									GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(), set);
								}
								if (!bulkSmsDTO.getRepeat().equalsIgnoreCase("no")) {
									GlobalVarsSms.RepeatedSchedules.add(generated_id);
								}
								target = IConstants.SUCCESS_KEY;
								logger.info("Task scheduled successfully.");
							} else {
								throw new ScheduledTimeException(
										"Error: Unable to schedule the task. An error occurred during the scheduling process.");

							}
						} else {
							logger.error("Error: The task is already scheduled. Duplicate schedule request received.");
							throw new ScheduledTimeException(
									"Error: The task is already scheduled. Duplicate schedule request received.");

						}
					} else {
						String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
						if (!value.contains("Error")) {
							target = IConstants.SUCCESS_KEY;

							String message = ("Batch processing completed successfully. Message: " + value);
							logger.info(message);
						} else {
							// Submission Error
							String message = ("Error: Unable to process batch submission. An error occurred during the submission process.");
							logger.error(message);
							throw new InternalServerException(message);
						}
					}
					if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
						bulkResponse.setBulkListInfo(listInfo);
						bulkResponse.setCredits(Long.toString(credits));
						bulkResponse.setDeductcredits(deductCredits + "");
						bulkResponse.setBulkSessionId(bulkSessionId);
					}
				} else {
					logger.error(bulkSessionId + " <-- Insufficient Balance --> " + credits);
					throw new InsufficientBalanceException(bulkSessionId + " <-- Insufficient Balance --> " + credits);
				}
			}
			// *****************************End Credit Calculation***********************
			if (wallet_flag.equalsIgnoreCase("MIN")) {
				// Insufficient Balance
				logger.error("Error: Insufficient balance to perform the operation. Please add funds to your wallet.");
				throw new InsufficientBalanceException(
						"Error: Insufficient balance to perform the operation. Please add funds to your wallet.");
			}
		} catch (InvalidPropertyException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InvalidPropertyException(e.getMessage());
		} catch (NotFoundException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (InsufficientBalanceException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InsufficientBalanceException(e.getMessage());
		} catch (InternalServerException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new InternalServerException(e.getMessage());
		} catch (ScheduledTimeException e) {
			logger.error(bulkSessionId, e.getMessage());
			throw new ScheduledTimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(bulkSessionId, e);
			throw new InternalServerException(e.getMessage());
		}
		bulkResponse.setStatus(target);
		return ResponseEntity.ok(bulkResponse);
	}

	public List<GroupDataEntry> listGroupData(SearchCriteria searchCriteria) {
		logger.info("listing GroupData Using Criteria: " + searchCriteria.getGroupId());
		try {
			// Constructing the base query
			List<GroupDataEntry> groupDataEntries = null;

			if (searchCriteria.getNumber() != null && searchCriteria.getNumber().length > 0) {
				logger.info("Adding Criteria For Numbers: " + searchCriteria.getNumber().length);
				groupDataEntries = groupDataEntryRepository.findByNumberInAndGroupId(
						ArrayUtils.toObject(searchCriteria.getNumber()), searchCriteria.getGroupId());
			}

			return groupDataEntries;
		} catch (Exception e) {
			logger.error("", e);
			return new ArrayList<GroupDataEntry>();
		}
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
				String hexv = (String) GlobalVarsSms.hashTabOne.get(character);
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

	public String sendScheduleSms(String file) {
		String toReturn = "Error In Scheduling";
		try {
			// String appName = "ScheduleAppl";
			// System.out.println("Schedule services...");
			logger.info(" Reading schedule File:-> " + file);
			SendSmsService service = new SendSmsService();
			BulkSmsDTO bulkSmsDTO = service.readScheduleFile(file);
			int user_id = userEntryRepository.getUsers(bulkSmsDTO.getSystemId()).get().getUserId();
			String mode = bulkSmsDTO.getUserMode();
			logger.info(file + " [" + bulkSmsDTO.getSystemId() + ":" + bulkSmsDTO.getPassword() + "] " + mode);
			long credits = 0;
			double walletAmt = 0.0;
			double totalWalletCost = bulkSmsDTO.getTotalWalletCost();
			WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user_id);// GlobalVars.WebmasterEntries.get(user_id);
			BalanceEntry balance = balanceEntryRepository.findByUserId(user_id).get();// GlobalVars.BalanceEntries.get(user_id);
			if (mode.equalsIgnoreCase("credit")) {
				credits = balance.getCredits();
			} else {
				walletAmt = balance.getWalletAmount();
			}
			long list = (long) bulkSmsDTO.getDestinationList().size();
			if (mode.equalsIgnoreCase("credit")) {
				if (list <= credits) {
					logger.info(file + " [" + bulkSmsDTO.getSystemId() + "] Sufficient Credits: " + credits);
					String response = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user_id);
					toReturn = "Scheduled Successfully" + response;
				} else {
					toReturn = "InSufficient Credits";
					logger.error(file + " [" + bulkSmsDTO.getSystemId() + "] Insufficient Credits: " + credits);
				}
			} else if (mode.equalsIgnoreCase("wallet")) {
				if (totalWalletCost <= walletAmt) {
					logger.info(file + " [" + bulkSmsDTO.getSystemId() + "] Sufficient Balance: " + walletAmt
							+ " Required:" + totalWalletCost);
					String response = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user_id);
					toReturn = "Scheduled Successfully" + response;
				} else {
					toReturn = "InSufficient Wallet";
					logger.error(file + " [" + bulkSmsDTO.getSystemId() + "] Insufficient Balance: " + walletAmt
							+ " Required:" + totalWalletCost);
				}
			}
		} catch (Exception e) {
			logger.error(file, e.fillInStackTrace());
		}
		return toReturn;
	}

	@Transactional
	public void addSchedule() {
		String Return = "";
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		month++;
		int daya = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		Return += year + "-";
		if (month < 10) {
			Return += "0";
		}
		Return += month + "-";
		if (daya < 10) {
			Return += "0";
		}
		Return += daya;
		System.out.println(Return);
		List<ScheduleEntry> schList = scheduleEntryRepository.findByDateAndStatusOrderByIdAsc(Return, "false");
		System.out.println(schList);
		try {

			boolean isAfter = true;
			for (ScheduleEntry sch : schList) {
				int id = sch.getId();
				String time = sch.getServerTime();
				String date = sch.getDate();
				String repeated = sch.getRepeated();
				try {
					Date scheduled_time = new SimpleDateFormat("yyyy-MM-dd HHmm").parse(date + " " + time);
					if (scheduled_time.before(new Date())) {
						logger.info("[" + id + "] Schedule Expired: " + scheduled_time);
						isAfter = false;
					} else {
						logger.info("[" + id + "] Schedule Listed: " + scheduled_time);
						isAfter = true;
					}
				} catch (ParseException e) {
					logger.error("[" + id + "]Schedule ParseError: " + date + " " + time);
				}
				if (isAfter) {
					Set<Integer> set = null;
					if (GlobalVarsSms.ScheduledBatches.containsKey(time)) {
						set = GlobalVarsSms.ScheduledBatches.get(time);
					} else {
						set = new LinkedHashSet<Integer>();
					}
					set.add(id);
					GlobalVarsSms.ScheduledBatches.put(time, set);
					if (!repeated.equalsIgnoreCase("no")) {
						GlobalVarsSms.RepeatedSchedules.add(id);
					}
				}
			}
		} catch (Exception sqle) {
			logger.error(" ", sqle.fillInStackTrace());
		}
	}

}
