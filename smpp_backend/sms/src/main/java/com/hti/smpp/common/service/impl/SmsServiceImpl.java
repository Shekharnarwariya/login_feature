package com.hti.smpp.common.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
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
import java.util.regex.Matcher;

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

import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;
import com.hti.smpp.common.contacts.dto.GroupEntryDTO;
import com.hti.smpp.common.contacts.repository.GroupDataEntryRepository;
import com.hti.smpp.common.contacts.repository.GroupEntryDTORepository;
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
import com.hti.smpp.common.request.BulkAutoScheduleRequest;
import com.hti.smpp.common.request.BulkContactRequest;
import com.hti.smpp.common.request.BulkMmsRequest;
import com.hti.smpp.common.request.BulkRequest;
import com.hti.smpp.common.request.BulkUpdateRequest;
import com.hti.smpp.common.request.SendBulkScheduleRequest;
import com.hti.smpp.common.request.SmsRequest;
import com.hti.smpp.common.response.BulkProccessResponse;
import com.hti.smpp.common.response.BulkResponse;
import com.hti.smpp.common.response.MessageIdentiryResponse;
import com.hti.smpp.common.response.ScheduleEditResponse;
import com.hti.smpp.common.response.SmsResponse;
import com.hti.smpp.common.schedule.dto.ScheduleEntry;
import com.hti.smpp.common.schedule.dto.ScheduleEntryExt;
import com.hti.smpp.common.schedule.repository.ScheduleEntryRepository;
import com.hti.smpp.common.service.RouteDAService;
import com.hti.smpp.common.service.SendSmsService;
import com.hti.smpp.common.service.SmsService;
import com.hti.smpp.common.session.SessionHandler;
import com.hti.smpp.common.session.UserSession;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.DriverInfo;
import com.hti.smpp.common.user.dto.User;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.dto.WebMenuAccessEntry;
import com.hti.smpp.common.user.repository.BalanceEntryRepository;
import com.hti.smpp.common.user.repository.DriverInfoRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.user.repository.WebMenuAccessEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.BatchObject;
import com.hti.smpp.common.util.Body;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.GMTmapping;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.GlobalVarsSms;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;
import com.hti.smpp.common.util.MultiUtility;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private WebMenuAccessEntryRepository webMenuAccessEntryRepository;

	@Autowired
	private GroupEntryDTORepository groupEntryDTORepository;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	public SmsResponse sendSms(SmsRequest smsRequest, String username) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));

			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
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
			logger.info(messageResourceBundle.getLogMessage("single.schedule.request"), bulkSmsDTO.getDestinationNumber());
		} else {
			logger.info(messageResourceBundle.getLogMessage("single.sms.request"), bulkSmsDTO.getDestinationNumber());
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
			logger.info(messageResourceBundle.getLogMessage("bulk.sms.message.info"), bulkSmsDTO.getMessageType(), no_of_msg);
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
						logger.error(messageResourceBundle.getLogMessage("scheduled.time.before.current.error"));
						throw new ScheduledTimeException(bulkSessionId + messageResourceBundle
								.getExMessage(ConstantMessages.SCHEDULED_TIME_BEFORE_CURRENT_TIME_EXCEPTION));
					}
					server_date = schedule_time.split(" ")[0];
					String server_time = schedule_time.split(" ")[1];
					bulkSmsDTO.setDate(server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-"
							+ server_date.split("-")[0]);
					bulkSmsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);
					System.out.println("this is server time " + bulkSmsDTO.getTime());

				} catch (ScheduledTimeException e) {
					logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
					throw new ScheduledTimeException(e.getMessage());
				} catch (Exception e) {
					logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
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
						logger.error(messageResourceBundle.getLogMessage("error.invalid.destination"), bulkSessionId, numToken);
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
							throw new NotFoundException(
									messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
						}
						adminCost = routeService.calculateRoutingCost(masterOptional.get().getId(), destinationList,
								no_of_msg);

						if ((adminWallet >= adminCost)) {
							if (wallet >= totalcost) {
								adminWallet = adminWallet - adminCost;
								wallet = wallet - totalcost;
								amount = true;

							} else {
								logger.error(messageResourceBundle.getLogMessage("error.insufficient.balance"), bulkSessionId);
								throw new InsufficientBalanceException(messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION) + bulkSessionId);
							}
						} else {
							// Insufficient Admin balance
							logger.error(messageResourceBundle.getLogMessage("error.insufficient.admin.balance"), userEntry.getMasterId(), bulkSessionId);

							throw new InsufficientBalanceException(messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_ADMIN_BALANCE_EXCEPTION) + bulkSessionId
									+ userEntry.getMasterId());
						}
					} else {
						if (wallet >= totalcost) {
							wallet = wallet - totalcost;
							amount = true;
						} else {
							// Insufficient balance
							logger.error(messageResourceBundle.getLogMessage("error.insufficient.balance"), bulkSessionId);
							throw new InsufficientBalanceException(
									messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION)
											+ bulkSessionId);
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
									logger.info(messageResourceBundle.getLogMessage("message.schedule.success"));

								} else {
									// Scheduling Error
									logger.error(messageResourceBundle.getLogMessage("error.singleschedule.error"));
									throw new ScheduledTimeException(messageResourceBundle.getExMessage(
											ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION) + bulkSessionId);
								}
							} else {
								// already Scheduled
								logger.error(messageResourceBundle.getLogMessage("error.singleschedule.error"));
								throw new ScheduledTimeException(messageResourceBundle.getExMessage(
										ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION) + bulkSessionId);
							}
						} else {
							respMsgId = sendSingleMsg(bulkSmsDTO);

							if (respMsgId.contains("Error") || respMsgId.contains("SERVER NOT RESPONDING")) {
								// Submission Error
								if (respMsgId.contains("SERVER NOT RESPONDING")) {
									logger.error(messageResourceBundle.getLogMessage("error.hostconnection.error"));
									throw new InternalServerException(messageResourceBundle.getExMessage(
											ConstantMessages.HOST_CONNECTION_ERROR_EXCEPTION) + bulkSessionId);

								} else {
									logger.error(messageResourceBundle.getLogMessage("error.smsError.error"));
									throw new InternalServerException(
											messageResourceBundle.getExMessage(ConstantMessages.SMS_ERROR_EXCEPTION)
													+ bulkSessionId);

								}
							} else {
								target = IConstants.SUCCESS_KEY;
								logger.info(messageResourceBundle.getLogMessage("message.smsSuccess.info"));
							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {

							smsResponse.setRespMsgId(respMsgId);
							smsResponse.setMsgCount(total_msg + "");
							smsResponse.setBulkListInfo(listInfo);
							smsResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
							smsResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
							logger.info(messageResourceBundle.getLogMessage("sms.processed"), bulkSessionId, wallet, totalcost);
						} else {
							logger.info(messageResourceBundle.getLogMessage("process.failed"), bulkSessionId);
						}
					} else {
						// insufficient balance
						logger.error(messageResourceBundle.getLogMessage("error.insufficientWallet"));
						throw new InsufficientBalanceException(messageResourceBundle
								.getExMessage(ConstantMessages.INSUFFICIENT_WALLET_BALANCE_EXCEPTION) + bulkSessionId);
					}
				} else {
					// Number File Error
					logger.error(messageResourceBundle.getLogMessage("error.novalidNumber"));
					throw new InternalServerException(
							messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_EXCEPTION)
									+ bulkSessionId);

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
								credits = credits - total_msg;
								amount = true;

							} else {
								logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"));
								throw new InsufficientBalanceException(messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION) + bulkSessionId);
							}
						} else {
							logger.error(messageResourceBundle.getLogMessage("error.insufficientAdminCredits"), userEntry.getMasterId());

							throw new InsufficientBalanceException(messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_ADMIN_CREDITS_EXCEPTION) + bulkSessionId
									+ userEntry.getMasterId());
						}
					} else {
						if (credits >= total_msg) {
							credits = credits - total_msg;

							amount = true;

						} else {
							logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"));
							throw new InsufficientBalanceException(
									messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION)
											+ bulkSessionId);
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
									logger.info(messageResourceBundle.getLogMessage("message.scheduleSuccess"));
								} else {
									logger.error(messageResourceBundle.getLogMessage("error.singleschedule.error"));
									throw new ScheduledTimeException(messageResourceBundle.getExMessage(
											ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION) + bulkSessionId);
								}
							} else {
								logger.error(messageResourceBundle.getLogMessage("error.singleschedule.error"));
								throw new ScheduledTimeException(messageResourceBundle.getExMessage(
										ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION) + bulkSessionId);

							}
						} else {
							respMsgId = sendSingleMsg(bulkSmsDTO);
							if (respMsgId.contains("Error") || respMsgId.contains("SERVER NOT RESPONDING")) {
								// Submission Error
								if (respMsgId.contains("SERVER NOT RESPONDING")) {
									logger.error(messageResourceBundle.getLogMessage("error.hostconnection.error"));
									throw new InternalServerException(messageResourceBundle.getExMessage(
											ConstantMessages.HOST_CONNECTION_ERROR_EXCEPTION) + bulkSessionId);

								} else {
									logger.error(messageResourceBundle.getLogMessage("error.smsError.error"));
									throw new InternalServerException(
											messageResourceBundle.getExMessage(ConstantMessages.SMS_ERROR_EXCEPTION)
													+ bulkSessionId);

								}
							} else {
								target = IConstants.SUCCESS_KEY;
								logger.info(messageResourceBundle.getLogMessage("message.smsSuccess.info"));
							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							smsResponse.setRespMsgId(respMsgId);
							smsResponse.setMsgCount(total_msg + "");
							smsResponse.setBulkListInfo(listInfo);
							smsResponse.setCredits(Long.toString(credits));
							smsResponse.setDeductcredits(deductCredits + "");

							logger.info(messageResourceBundle.getLogMessage("message.processed.credits"), credits, deductCredits);
						} else {
							logger.error(messageResourceBundle.getLogMessage("error.process.failed"), bulkSessionId);
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// Insufficient Credits
						logger.error(messageResourceBundle.getLogMessage("error.insufficientCredit"), bulkSessionId);
						throw new InsufficientBalanceException(messageResourceBundle.getExMessage(
								ConstantMessages.INSUFFICIENT_CREDITS_OPERATION_EXCEPTION) + bulkSessionId);

					}
				} else {
					// Number File Error
					logger.error(messageResourceBundle.getLogMessage("error.novalidNumber"));
					throw new InternalServerException(
							messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_EXCEPTION)
									+ bulkSessionId);
				}
			} else if (wallet_flag.equalsIgnoreCase("MIN")) {
				// insufficient balance
				logger.error(messageResourceBundle.getLogMessage("error.insufficientWallet"));
				throw new InsufficientBalanceException(messageResourceBundle.getExMessage(
						ConstantMessages.INSUFFICIENT_WALLET_BALANCE_TRANSACTION_EXCEPTION) + bulkSessionId);

			}
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (InsufficientBalanceException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InsufficientBalanceException(e.getMessage());
		} catch (InternalServerException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InternalServerException(e.getMessage());
		} catch (ScheduledTimeException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new ScheduledTimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
			throw new InternalServerException(e.getMessage());
		}

		smsResponse.setStatus(target);
		return smsResponse;

	}

	public String sendSingleMsg(BulkSmsDTO bulkSmsDTO) {
		logger.info(messageResourceBundle.getLogMessage("send.single.message"), bulkSmsDTO.getSystemId(), bulkSmsDTO.getSenderId());
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
			logger.error(messageResourceBundle.getLogMessage("summary.report.error"), ex.getMessage());
			throw new InternalServerException(
					user + messageResourceBundle.getExMessage(ConstantMessages.ERROR_ADDING_TO_SUMMARY_REPORT_EXCEPTION)
							+ ex.getMessage());
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
								logger.error(messageResourceBundle.getLogMessage("submit.exception.error"), destination_no, e.getMessage());
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
										logger.error(messageResourceBundle.getLogMessage("submit.failed.insufficient.balance"), destination_no);
										ret += "SubmitError: Insufficient balance\n";
										userSession.setCommandStatus(submitResponse.getCommandStatus());
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
										logger.error(messageResourceBundle.getLogMessage("submit.failed.invalid.message.length"), destination_no);
										ret += "SubmitError: Invalid Message Length\n";
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
										logger.error(messageResourceBundle.getLogMessage("submit.failed.invalid.destination"), destination_no);
										ret += "SubmitError: Invalid Destination[" + destination_no + "]\n";
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
										logger.error(messageResourceBundle.getLogMessage("submit.failed.invalid.source.address"), sender);
										ret += "SubmitError: Invalid SourceAddress\n";
									} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
										logger.error(messageResourceBundle.getLogMessage("submit.failed.account.expired"), destination_no);
										ret += "SubmitError: Account Expired\n";
										userSession.setCommandStatus(submitResponse.getCommandStatus());
									} else {
										ret += "SubmitError: " + submitResponse.getCommandStatus() + "\n";
										logger.error(messageResourceBundle.getLogMessage("submit.failed.command.status"), submitResponse.getCommandStatus(), destination_no);
									}
								}
							} else {
								ret += "Submit Failed\n";
								logger.error(messageResourceBundle.getLogMessage("submit.failed.no.response"), destination_no);
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
						logger.error(messageResourceBundle.getLogMessage("error.message"), user, e.getMessage());
						throw new InternalServerException("username :{}" + user + e.getMessage());
					}
				}
				totalCounter++;
				if (++loopCounter > 100) {
					logger.info(messageResourceBundle.getLogMessage("total.submitted"), user, totalCounter);

					loopCounter = 0;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
					}
				}
			} // for loop end here
		} catch (Exception e) {
			ret += "Processing Error\n";
			logger.error(messageResourceBundle.getLogMessage("error.message"), user, e.getMessage());
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
			logger.error(messageResourceBundle.getLogMessage("error.fillInStackTrace"), userSession.getUsername(), e.fillInStackTrace());
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
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		DriverInfo driverInfo = null;
		Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(userEntry.getId());
		if (OptionalDriverInfo.isPresent()) {
			driverInfo = OptionalDriverInfo.get();
		} else
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.DRIVE_INFO_NOT_FOUND_EXCEPTION));

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

		bulkSmsDTO.setReqType("Bulk");
		if (bulkRequest.isTracking()) {
			return BulkTracking(bulkRequest, bulkSmsDTO, webEntry, driverInfo, userEntry, progressEvent,
					destinationNumberFile);
		}

		else {
			try {

				if (bulkSmsDTO.isSchedule()) {
					logger.info(messageResourceBundle.getLogMessage("bulk.schedule.request"), bulkSessionId, destinationNumberFile.size());
				} else {
					logger.info(messageResourceBundle.getLogMessage("bulk.upload.request"), bulkSessionId, destinationNumberFile.size());
				}
				// ------ merge uploaded files into a list ---------------
				Map<String, Integer> errors = new HashMap<String, Integer>();
				int invalidCount = 0;
				int total = 0;
				logger.info(messageResourceBundle.getLogMessage("start.processing.uploaded.files"), bulkSessionId);
				for (MultipartFile uploaded_file : destinationNumberFile) {
					String fileName = uploaded_file.getOriginalFilename();
					logger.info(messageResourceBundle.getLogMessage("processing.file"), bulkSessionId, fileName);
					String fileMode = null;
					if (fileName.endsWith(".txt")) {
						fileMode = "txt";
					} else if (fileName.endsWith(".csv")) {
						fileMode = "csv";
					} else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
						fileMode = "xls";
					} else {
						logger.warn(messageResourceBundle.getLogMessage("invalid.file.uploaded"), bulkSessionId, fileName);
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
									logger.info(messageResourceBundle.getLogMessage("invalid.exclude.number.found"), next);
								}
							}
						}
					}

					if (!excludeSet.isEmpty()) {
						try {
							MultiUtility.writeExcludeNumbers(String.valueOf(userEntry.getId()),
									String.join("\n", excludeSet));
						} catch (Exception ex) {
							System.out.println(bulkSessionId + " " + ex);
						}
					} else {
						try {
							MultiUtility.removeExcludeNumbers(String.valueOf(userEntry.getId()));
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
											logger.info(messageResourceBundle.getLogMessage("excluded.numbers"), num);
										}
									} catch (NumberFormatException nfe) {
										int counter = 0;
										if (errors.containsKey("Invalid Destination")) {
											counter = errors.get("Invalid Destination");
										}
										errors.put("Invalid Destination", ++counter);
										logger.info(messageResourceBundle.getLogMessage("invalid.destination.number"), destinationNumber);
										invalidCount++;
									}
								} else {
									int counter = 0;
									if (errors.containsKey("Empty Row")) {
										counter = errors.get("Empty Row");
									}
									errors.put("Empty Row", ++counter);
									logger.info(messageResourceBundle.getLogMessage("empty.row.found"), row);
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
									logger.info(messageResourceBundle.getLogMessage("total.rows"), i, uploaded_file.getName());
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
												logger.info(messageResourceBundle.getLogMessage("info.invalidColumn"), nextRow.getRowNum() + 1);
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
														logger.info(messageResourceBundle.getLogMessage("excluded.numbers"), num);
													}
												} catch (NumberFormatException nfe) {
													int counter = 0;
													if (errors.containsKey("Invalid Destination")) {
														counter = (Integer) errors.get("Invalid Destination");
													}
													errors.put("Invalid Destination", ++counter);
													logger.info(messageResourceBundle.getLogMessage("invalid.destination.number"), destination);
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
									logger.info(messageResourceBundle.getLogMessage("sheet.processed"), uploaded_file.getName(), i);
								}
								// *********************************************************
							} catch (Exception ex) {
								logger.error(messageResourceBundle.getLogMessage("file.parsing.error"), uploaded_file.getName(), ex.getLocalizedMessage());
								throw new InternalServerException(
										"Parsing File: " + uploaded_file.getName() + ex.getMessage());
							}
						}
						logger.info(messageResourceBundle.getLogMessage("file.number.counter"), uploaded_file.getName(), file_total_counter);
					}
				}
				logger.info(messageResourceBundle.getLogMessage("uploaded.files.processed"), bulkSessionId, temp_number_list.size());
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
				logger.info(messageResourceBundle.getLogMessage("list.info.summary"), bulkSessionId, listInfo.getTotal(), listInfo.getValidCount(), listInfo.getInvalidCount(), listInfo.getDuplicate(), bulkSmsDTO.isAllowDuplicate());
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
				logger.info(messageResourceBundle.getLogMessage("message.type.parts.info"), bulkSessionId, bulkSmsDTO.getMessageType(), no_of_msg);
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
						logger.info(messageResourceBundle.getLogMessage("client.server.time.info"), client_gmt, client_time, schedule_time);

						if (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(schedule_time).after(new Date())) {
							valid_sch_time = true;
						} else {
							logger.error(messageResourceBundle.getLogMessage("scheduled.time.before.current.error"), bulkSessionId);
						}
						String server_date = schedule_time.split(" ")[0];
						String server_time = schedule_time.split(" ")[1];
						bulkSmsDTO.setDate(server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-"
								+ server_date.split("-")[0]);
						bulkSmsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);
					} catch (Exception e) {
						logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
						throw new InternalServerException("Error: getting error in parssing time with bulkSesssionId"
								+ bulkSessionId + e.getMessage());
					}
					if (!valid_sch_time) {

						logger.error(messageResourceBundle.getLogMessage("error.schedule.time"));
						bulkResponse.setStatus(target);
						throw new ScheduledTimeException(
								messageResourceBundle.getExMessage(ConstantMessages.SCHEDULER_ERROR_EXCEPTION)
										+ bulkSessionId);

						// return bulkResponse;
					}
				}
				if (wallet_flag.equalsIgnoreCase("yes")) {
					bulkSmsDTO.setUserMode("wallet");
					if (destinationList.size() > 0) {
						totalcost = routeService.calculateRoutingCost(userEntry.getId(), destinationList, no_of_msg);
						logger.info(messageResourceBundle.getLogMessage("balance.calculated.cost"), wallet, totalcost);
						boolean amount = false;
						// boolean inherit = false;
						if (userEntry.isAdminDepend()) {
							Optional<UserEntry> masterOptional = userEntryRepository
									.findBySystemId(userEntry.getMasterId());
							if (!masterOptional.isPresent()) {
								throw new NotFoundException(
										messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
							}
							adminCost = routeService.calculateRoutingCost(masterOptional.get().getId(), destinationList,
									no_of_msg);
							logger.info(messageResourceBundle.getLogMessage("admin.balance.calculated.cost"), userEntry.getMasterId(), adminWallet, adminCost);

							if ((adminWallet >= adminCost)) {
								if (wallet >= totalcost) {
									adminWallet = adminWallet - adminCost;
									wallet = wallet - totalcost;
									amount = true;
								} else {
									logger.error(messageResourceBundle.getLogMessage("error.insufficient.balance"), bulkSessionId);
									throw new InsufficientBalanceException(messageResourceBundle.getExMessage(
											ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION) + bulkSessionId);
								}
							} else {
								// Insufficient Admin balance
								logger.error(messageResourceBundle.getLogMessage("insufficient.admin.balance"), userEntry.getMasterId());
								throw new InsufficientBalanceException(messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_ADMIN_BALANCE_EXCEPTION)
										+ bulkSessionId + userEntry.getMasterId());
							}
						} else {
							if (wallet > 0 && wallet >= totalcost) {
								wallet = wallet - totalcost;
								amount = true;
							} else {
								// Insufficient balance
								logger.error(messageResourceBundle.getLogMessage("error.insufficient.balance"), bulkSessionId);
								throw new InsufficientBalanceException(messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION) + bulkSessionId);

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
										logger.info(messageResourceBundle.getLogMessage("message.scheduleSuccess"));
									} else {
										logger.error(messageResourceBundle.getMessage("schedule.task.error"));
										throw new ScheduledTimeException(messageResourceBundle
												.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION)
												+ "for username " + username);

									}
								} else {
									// Already Scheduled
									logger.error(messageResourceBundle.getMessage("duplicate.schedule.error"));
									throw new ScheduledTimeException(messageResourceBundle
											.getExMessage(ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION)
											+ "for username " + username);

								}
							} else {

								String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
										userEntry.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									logger.info(messageResourceBundle.getLogMessage("bulk.sms.sent.successfully"), value);
								} else {
									// Submission Error
									logger.error(messageResourceBundle.getLogMessage("error.sending.bulk.sms"), value);
									throw new InternalServerException(messageResourceBundle
											.getExMessage(ConstantMessages.BULK_SMS_ERROR_EXCEPTION));

								}
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
								bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
								bulkResponse.setBulkSessionId(bulkSessionId);
								logger.info(messageResourceBundle.getLogMessage("processed.balance.cost"), wallet, totalcost);
							} else {
								logger.info(messageResourceBundle.getLogMessage("process.failed"), bulkSessionId);
								throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							logger.error(messageResourceBundle.getMessage("error.insufficient.balance"));
							throw new InsufficientBalanceException(messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));

						}
					} else {
						String errorMessage = "Error: No valid numbers found in the provided file. Please check the file and try again.";
						logger.error(messageResourceBundle.getMessage("error.general"), errorMessage);
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

									credits = credits - (destinationList.size() * no_of_msg);

									amount = true;
								} else {
									String errorMessage = bulkSessionId + " Error: Insufficient Credits.";
									logger.error(messageResourceBundle.getMessage("error.general"), errorMessage);
									throw new InsufficientBalanceException(errorMessage);
								}
							} else {
								String errorMessage = bulkSessionId + " Error: Insufficient Admin("
										+ userEntry.getMasterId() + ") Credits.";
								logger.error(messageResourceBundle.getMessage("error.general"), errorMessage);
								throw new InsufficientBalanceException(errorMessage);
							}
						} else {
							if (credits >= (destinationList.size() * no_of_msg)) {
								credits = credits - (destinationList.size() * no_of_msg);

								amount = true;
							} else {
								String errorMessage = bulkSessionId + " Error: Insufficient Credits.";
								logger.info(messageResourceBundle.getMessage("info.general"), errorMessage);
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
										logger.info(messageResourceBundle.getLogMessage("message.scheduleSuccess"));
									} else {
										// Schedule Error
										String errorMessage = "Error: Unable to schedule the task. An error occurred while processing the schedule request.";
										logger.error(messageResourceBundle.getMessage("error.general"), errorMessage);
										throw new ScheduledTimeException(errorMessage);
									}
								} else {
									// Duplicate Schedule Error
									String errorMessage = "Error: The task is already scheduled. Duplicate schedule request received.";
									logger.error(messageResourceBundle.getMessage("error.general"), errorMessage);
									throw new ScheduledTimeException(errorMessage);
								}
							} else {

								String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
										userEntry.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									logger.info(messageResourceBundle.getLogMessage("info.batch.success"));
								} else {
									// Submission Error
									String errorMessage = "Error: Unable to process batch submission. An error occurred during the submission process.";
									logger.error(messageResourceBundle.getMessage("error.general"), errorMessage);
									throw new InternalServerException(errorMessage);
								}
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(Long.toString(credits));
								bulkResponse.setDeductcredits(deductCredits + "");
								bulkResponse.setBulkSessionId(bulkSessionId);
								logger.info(messageResourceBundle.getLogMessage("info.processed.credits"), bulkSessionId, credits, deductCredits);
							} else {
								// Process Failed
								String errorMessage = bulkSessionId + " Error: The process failed.";
								logger.error(messageResourceBundle.getMessage("error.general"), errorMessage);
								throw new InternalServerException(errorMessage);
							}
						} else {
							logger.error(messageResourceBundle.getLogMessage("error.insufficient.credits"));
							throw new InsufficientBalanceException(messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION));

						}
					} else {
						// Number File Error
						String errorMessage = "Error: No valid numbers found in the provided file. Please check the file and try again.";
						logger.error(messageResourceBundle.getMessage("error.general"), errorMessage);
						throw new InternalServerException(errorMessage);
					}
				} else if (wallet_flag.equalsIgnoreCase("MIN")) {
					logger.error(messageResourceBundle.getLogMessage("error.insufficient.wallet.balance"));
					throw new InsufficientBalanceException(
							messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_WALLET_BALANCE_EXCEPTION));

				}
			} catch (InvalidPropertyException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new InvalidPropertyException(e.getMessage());
			} catch (NotFoundException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new NotFoundException(e.getMessage());
			} catch (InsufficientBalanceException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new InsufficientBalanceException(e.getMessage());
			} catch (InternalServerException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new InternalServerException(e.getMessage());
			} catch (ScheduledTimeException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new ScheduledTimeException(e.getMessage());
			} catch (Exception e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
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
			logger.info(messageResourceBundle.getLogMessage("tracking.schedule.request"), destinationNumberFile.size());
		} else {
			logger.info(messageResourceBundle.getLogMessage("tracking.upload.request"), destinationNumberFile.size());
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
				logger.info(messageResourceBundle.getLogMessage("processing.file"), bulkSessionId, fileName);
				String fileMode = null;
				if (fileName.endsWith(".txt")) {
					fileMode = "txt";
				} else if (fileName.endsWith(".csv")) {
					fileMode = "csv";
				} else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
					fileMode = "xls";
				} else {
					logger.warn(messageResourceBundle.getLogMessage("invalid.file.uploaded"), bulkSessionId, fileName);
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
								logger.info(messageResourceBundle.getLogMessage("invalid.exclude.number.found"), next);
							}
						}
					}
				}

				if (!excludeSet.isEmpty()) {
					try {
						MultiUtility.writeExcludeNumbers(String.valueOf(userEntry.getId()),
								String.join("\n", excludeSet));
					} catch (Exception ex) {
						System.out.println(bulkSessionId + " " + ex);
					}
				} else {
					try {
						MultiUtility.removeExcludeNumbers(String.valueOf(userEntry.getId()));
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
										logger.info(messageResourceBundle.getLogMessage("excluded.numbers"), num);
									}
								} catch (NumberFormatException nfe) {
									int counter = 0;
									if (errors.containsKey("Invalid Destination")) {
										counter = errors.get("Invalid Destination");
									}
									errors.put("Invalid Destination", ++counter);
									logger.info(messageResourceBundle.getLogMessage("invalid.destination.number"), destinationNumber);
									invalidCount++;
								}
							} else {
								int counter = 0;
								if (errors.containsKey("Empty Row")) {
									counter = errors.get("Empty Row");
								}
								errors.put("Empty Row", ++counter);
								logger.info(messageResourceBundle.getLogMessage("empty.row.found"), row);
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
								logger.info(messageResourceBundle.getLogMessage("total.rows"), i, uploaded_file.getName());
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
											logger.info(messageResourceBundle.getLogMessage("invalid.column.found"), nextRow.getRowNum() + 1);
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
													logger.info(messageResourceBundle.getLogMessage("excluded.numbers"), num);
												}
											} catch (NumberFormatException nfe) {
												int counter = 0;
												if (errors.containsKey("Invalid Destination")) {
													counter = (Integer) errors.get("Invalid Destination");
												}
												errors.put("Invalid Destination", ++counter);
												logger.info(messageResourceBundle.getLogMessage("invalid.destination.number"), destination);
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
								logger.info(messageResourceBundle.getLogMessage("sheet.processed"), uploaded_file.getName(), i);
							}
							// *********************************************************
						} catch (Exception ex) {
							logger.error(messageResourceBundle.getLogMessage("file.parsing.error"), uploaded_file.getName(), ex.getLocalizedMessage());
							throw new InternalServerException(
									"Parsing File: " + uploaded_file.getName() + ex.getMessage());
						}
					}
					logger.info(messageResourceBundle.getLogMessage("file.number.counter"), uploaded_file.getName(), file_total_counter);
				}
			}
			Set<String> hashSet = new HashSet<String>(temp_number_list);
			if (webEntry.isPrefixApply()) {
				destinationList = new ArrayList<String>();
				for (String number : hashSet) {
					if (number.length() < webEntry.getNumberLength()) {
						logger.info(messageResourceBundle.getLogMessage("number.length.less"), number, webEntry.getNumberLength());
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
			logger.info(messageResourceBundle.getLogMessage("list.info.summary"), bulkSessionId, listInfo.getTotal(), listInfo.getValidCount(), listInfo.getInvalidCount(), listInfo.getDuplicate(), bulkSmsDTO.isAllowDuplicate());
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
			logger.info(messageResourceBundle.getLogMessage("message.type.parts.info"), bulkSessionId, bulkSmsDTO.getMessageType(), no_of_msg);
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
					logger.info(messageResourceBundle.getLogMessage("client.time.gmt.info"), client_gmt, client_time, schedule_time);
					if (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(schedule_time).after(new Date())) {
						valid_sch_time = true;
					} else {
						logger.error(messageResourceBundle.getLogMessage("scheduled.time.before.current.error"), bulkSessionId);
					}
					String server_date = schedule_time.split(" ")[0];
					String server_time = schedule_time.split(" ")[1];
					bulkSmsDTO.setDate(server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-"
							+ server_date.split("-")[0]);
					bulkSmsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);
				} catch (Exception e) {
					logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
				}
				if (!valid_sch_time) {
					throw new ScheduledTimeException(
							messageResourceBundle.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION)
									+ "for bulkSession :" + bulkSessionId);
				}
			}
			// ---------------- url appending & message calculation --------------------
			Hashtable<String, List<String>> msgmapping = new Hashtable<String, List<String>>(); // for mapping number
																								// with message content
			Map<String, Integer> msgLengthTable = new HashMap<String, Integer>(); // for balance calculation
			int totalMsg = 0; // FOR CREDIT CALCULATION
			String campaign_name = bulkRequest.getCampaignName();
			logger.info(messageResourceBundle.getLogMessage("web.links.received"), bulkRequest.getWeblink().length);
			List<String> web_links_list = new ArrayList<String>();
			for (String link : bulkRequest.getWeblink()) {
				if (link != null && link.length() > 0) {
					web_links_list.add(link);
				}
			}
			logger.info(messageResourceBundle.getLogMessage("final.web.links"), web_links_list);
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
				logger.info(messageResourceBundle.getLogMessage("content.one"), msg_content);
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
				logger.info(messageResourceBundle.getLogMessage("content.two"), msg_content);
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
					logger.info(messageResourceBundle.getLogMessage("balance.calculated.cost"), wallet, totalcost);
					boolean amount = false;
					// boolean inherit = false;
					if (userEntry.isAdminDepend()) {
						adminCost = routeService.calculateRoutingCost(masterOptional.get().getId(), msgLengthTable);
						logger.info(messageResourceBundle.getLogMessage("admin.balance.calculatedcost"), masterOptional.get().getId(), adminWallet, adminCost);
						if ((adminWallet >= adminCost)) {
							if (wallet >= totalcost) {
								adminWallet = adminWallet - adminCost;
								wallet = wallet - totalcost;
								amount = true;

							} else {
								logger.error(messageResourceBundle.getLogMessage("error.insufficient.balance"), bulkSessionId);
								throw new InsufficientBalanceException(messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION) + bulkSessionId);
							}
						} else {
							// Insufficient Admin balance
							logger.error(messageResourceBundle.getLogMessage("insufficient.admin.balance"), masterOptional.get().getId());
							throw new InsufficientBalanceException(messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_ADMIN_BALANCE_EXCEPTION) + bulkSessionId
									+ masterOptional.get().getId());
						}
					} else {
						if (wallet > 0 && wallet >= totalcost) {
							wallet = wallet - totalcost;
							amount = true;

						} else {
							// Insufficient balance
							logger.info(messageResourceBundle.getLogMessage("insufficient.balance"), bulkSessionId);
							throw new InsufficientBalanceException(
									messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION)
											+ bulkSessionId);
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
									logger.info(messageResourceBundle.getLogMessage("message.scheduleSuccess"));
								} else {
									logger.error(messageResourceBundle.getLogMessage("unable.schedule.task.error"));
									throw new ScheduledTimeException(messageResourceBundle
											.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION)
											+ "for username " + userEntry.getSystemId());

								}
							} else {
								// Already Scheduled
								logger.error(messageResourceBundle.getLogMessage("duplicate.schedule.error"));
								throw new ScheduledTimeException(messageResourceBundle
										.getExMessage(ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION)
										+ "for username " + userEntry.getSystemId());
							}
						} else {
							String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
									userEntry.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info(messageResourceBundle.getLogMessage("bulk.sms.success"), value);
							} else {
								// Submission Error
								logger.error(messageResourceBundle.getLogMessage("error.sending.bulk.sms"), value);
								throw new InternalServerException(
										messageResourceBundle.getExMessage(ConstantMessages.BULK_SMS_ERROR_EXCEPTION));

							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setBulkListInfo(listInfo);
							bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
							bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
							bulkResponse.setBulkSessionId(bulkSessionId);
							logger.info(messageResourceBundle.getLogMessage("sms.processed"), bulkSessionId, wallet, totalcost);
						} else {
							logger.info(messageResourceBundle.getLogMessage("process.failed"), bulkSessionId);
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// insufficient balance
						String insufficientBalanceMessage = "Error: Insufficient funds in the wallet.";
						logger.error(messageResourceBundle.getLogMessage("error.insufficient.balance"), insufficientBalanceMessage);
						throw new InsufficientBalanceException(insufficientBalanceMessage);
					}
				} else {
					// Number File Error
					String invalidNumberMessage = "Error: The provided number is not valid.";
					logger.error(messageResourceBundle.getLogMessage("error.invalid.number"), invalidNumberMessage);
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
								credits = credits - totalMsg;
								amount = true;

							} else {
								logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"));
								throw new InsufficientBalanceException(messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION) + bulkSessionId);
							}
						} else {
							logger.error(messageResourceBundle.getLogMessage("error.insufficient.admin.credits"), masterOptional.get().getSystemId());
							throw new InsufficientBalanceException(
									messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION)
											+ bulkSessionId + masterOptional.get().getSystemId());
						}
					} else {
						if (credits >= totalMsg) {
							credits = credits - totalMsg;

							amount = true;

						} else {
							logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"));
							throw new InsufficientBalanceException(
									messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION)
											+ bulkSessionId);

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
									logger.info(messageResourceBundle.getLogMessage("message.scheduleSuccess"));
								} else {
									// Schedule error
									String scheduleErrorMessage = "Error: There was an issue scheduling the task.";
									logger.error(messageResourceBundle.getLogMessage("error.scheduleError"));
									throw new ScheduledTimeException(scheduleErrorMessage);
								}
							} else {
								// Duplicate schedule error
								String duplicateScheduleMessage = "Error: The task has already been scheduled at this time.";
								logger.error(messageResourceBundle.getLogMessage("error.duplicateSchedule"));
								throw new ScheduledTimeException(duplicateScheduleMessage);
							}
						} else {
							String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
									userEntry.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info(messageResourceBundle.getLogMessage("message.bulkSMSSuccess"), value);
							} else {
								// Submission Error
								logger.error(messageResourceBundle.getLogMessage("error.sending.bulk.sms"), value);
								throw new InternalServerException(
										messageResourceBundle.getExMessage(ConstantMessages.BULK_SMS_ERROR_EXCEPTION));

							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setBulkListInfo(listInfo);
							bulkResponse.setCredits(Long.toString(credits));
							bulkResponse.setDeductcredits(totalMsg + "");
							bulkResponse.setBulkSessionId(bulkSessionId);
							logger.info(messageResourceBundle.getLogMessage("message.bulkSMSProcessed"), credits, totalMsg);
						} else {
							logger.error(messageResourceBundle.getLogMessage("error.process.failed"), bulkSessionId);
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// Insufficient Credits
						String insufficientCreditMessage = "Error: Insufficient credits for the operation.";
						logger.error(messageResourceBundle.getMessage("error.insufficientCredit"));
						throw new InsufficientBalanceException(insufficientCreditMessage);
					}
				} else {
					// Number File Error
					String noValidNumberMessage = "Error: The provided number is not valid.";
					logger.error(messageResourceBundle.getMessage("error.novalidNumber"));
					throw new InternalServerException(noValidNumberMessage);
				}
			} else if (wallet_flag.equalsIgnoreCase("MIN")) {
				String insufficientWalletMessage = "Error: Insufficient wallet balance for the operation.";
				logger.error(messageResourceBundle.getMessage("error.insufficientWallet"));
				throw new InsufficientBalanceException(insufficientWalletMessage);
			}
		} catch (InvalidPropertyException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InvalidPropertyException(e.getMessage());
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (InsufficientBalanceException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InsufficientBalanceException(e.getMessage());
		} catch (InternalServerException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InternalServerException(e.getMessage());
		} catch (ScheduledTimeException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new ScheduledTimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
			throw new InternalServerException(e.getMessage());
		}
		bulkResponse.setStatus(target);
		return bulkResponse;

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
			logger.error(messageResourceBundle.getLogMessage("error.fillInStackTrace"), bulkSmsDTO.getSystemId(), e.fillInStackTrace());
		}
		return response;
	}

	public String sendBulkMsg(BulkSmsDTO bulkSmsDTO, boolean waitForApprove, int System_Id) {

		logger.info(messageResourceBundle.getLogMessage("info.reqTypeAlert"), bulkSmsDTO.getSystemId(), bulkSmsDTO.getReqType(), bulkSmsDTO.isAlert(), bulkSmsDTO.getDestinationNumber());
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
						logger.info(messageResourceBundle.getLogMessage("info.invalidAlertNumber"), alertNumber);
					}
				}
				if (!alertNumbers.isEmpty()) {
					entry.setAlertNumbers(String.join(",", alertNumbers));
					entry.setAlert(true);
				} else {
					logger.info(messageResourceBundle.getLogMessage("info.invalidAlertNumbers"), bulkSmsDTO.getDestinationNumber());
					entry.setAlert(false);
				}
			}
			entry.setExpiryHour(bulkSmsDTO.getExpiryHour());
			// ---------------- For Batch Content --------------------
			List<BulkContentEntry> bulk_list = new ArrayList<BulkContentEntry>();
			logger.info(messageResourceBundle.getLogMessage("info.preparingBatchContentList"));
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
						logger.info(messageResourceBundle.getLogMessage("info.invalidNumber"), map_entry.getKey());
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
						logger.info(messageResourceBundle.getLogMessage("info.invalidNumber"), destination_loop);
					}
				}
			}
			logger.info(messageResourceBundle.getLogMessage("info.endPreparingBatchContentList"));
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
					logger.error(messageResourceBundle.getLogMessage("error.addingBulkEntry"), user, ex);
				}
				logger.info(messageResourceBundle.getLogMessage("info.batchAddedToWaitingForApprove"), user);
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
					logger.info(messageResourceBundle.getLogMessage("info.entryAdded"), entry.toString());
					if (event != null) {
						saveBulkMgmtContent(batch_id, bulk_list, event);
					} else {
						saveBulkMgmtContent(batch_id, bulk_list);
					}
					try {
						addSummaryReport(backupExt);
					} catch (Exception ex) {
						logger.error(messageResourceBundle.getLogMessage("error.summaryReport"), user, ex);
					}
					GlobalVars.BatchQueue.put(batch_id, new BatchObject(batch_id, user, IConstants.SERVER_ID, true));
					logger.info(messageResourceBundle.getLogMessage("info.batchAddedToProcessing"), user, entry.getId());
				} else {
					logger.info(messageResourceBundle.getLogMessage("info.entryNotAdded"), user, entry.toString());
				}
			}
		} catch (Exception ex) {
			ret = "Error: " + ex.getMessage();
			logger.error(messageResourceBundle.getLogMessage("error.startError"), ex.toString(), user, batch_id);
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

			List<BulkContentEntry> bulkMgmtContentList = new ArrayList<>();
			for (BulkContentEntry entry : list) {
				BulkContentEntry content = new BulkContentEntry();
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
			List<BulkContentEntry> bulkMgmtContentList = new ArrayList<>();
			for (BulkContentEntry entry : list) {
				BulkContentEntry content = new BulkContentEntry();
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
	public boolean createBulkMgmtContentTable(int batchId, List<BulkContentEntry> bulkMgmtContentList) {
		try {
			String tableName = "batch_content_" + batchId;
			{
				createTable(tableName);
				persistEntities(bulkMgmtContentList, tableName);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(messageResourceBundle.getLogMessage("error.createTableOrPersistEntities"), e.getMessage());
			return false;
		}
	}

	private void createTable(String tableName) {
		jdbcTemplate.update("CALL CreateTableIfNotExists(?)", new Object[] { tableName });

	}

	private void persistEntities(List<BulkContentEntry> bulkMgmtContentList, String tableName) {
		for (BulkContentEntry content : bulkMgmtContentList) {
			jdbcTemplate.update("CALL InsertDataIntoTable(?, ?, ?, ?)", content.getDestination(), content.getContent(),
					content.getFlag(), tableName);
		}
	}

	@Override
	public BulkResponse sendBulkCustom(BulkRequest bulkRequest, String username, MultipartFile destinationNumberFile,
			HttpSession session) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
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
				throw new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.DRIVE_INFO_NOT_FOUND_EXCEPTION));
			bulkSmsDTO.setClientId(user.getSystemId());
			bulkSmsDTO.setSystemId(user.getSystemId());
			bulkSmsDTO.setPassword(new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
		} catch (Exception ex) {
			System.out.println(bulkSessionId + " " + ex);
		}
		if (bulkSmsDTO.isSchedule()) {
			logger.info(messageResourceBundle.getLogMessage("info.customScheduleRequest"), bulkRequest.isTracking(), destinationNumberFile.getName());
		} else {
			logger.info(messageResourceBundle.getLogMessage("info.customUploadRequest"), bulkRequest.isTracking(), destinationNumberFile.getName());
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
						logger.info(messageResourceBundle.getLogMessage("info.invalidExcludeNumber"), next);
					}
				}
			}
		}
		if (!excludeSet.isEmpty()) {
			try {
				MultiUtility.writeExcludeNumbers(String.valueOf(user.getId()), String.join("\n", excludeSet));
			} catch (Exception ex) {
				System.out.println(bulkSessionId + " " + ex);
			}
		} else {
			try {
				MultiUtility.removeExcludeNumbers(String.valueOf(user.getId()));
			} catch (Exception ex) {
				System.out.println(bulkSessionId + " " + ex);
			}
		}
		logger.info(messageResourceBundle.getLogMessage("info.excludeCount"), excludeSet.size());
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
						logger.error(messageResourceBundle.getLogMessage("scheduled.time.before.current.error"), bulkSessionId);
						throw new ScheduledTimeException(messageResourceBundle.getExMessage(
								ConstantMessages.SCHEDULED_TIME_BEFORE_CURRENT_TIME_EXCEPTION) + bulkSessionId);
					}
					String server_date = schedule_time.split(" ")[0];
					String server_time = schedule_time.split(" ")[1];
					bulkSmsDTO.setDate(server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-"
							+ server_date.split("-")[0]);
					bulkSmsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);
				} catch (Exception e) {
					logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
				}
				if (!valid_sch_time) {
					logger.error(messageResourceBundle.getLogMessage("error.schedule.time"));

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
										logger.info(messageResourceBundle.getLogMessage("info.invalidSeparator"), row, content);
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
							logger.info(messageResourceBundle.getLogMessage("info.sheetTotalRows"), i, firstSheet.getPhysicalNumberOfRows());
							for (Row nextRow : firstSheet) {
								if (nextRow.getRowNum() == 0) {
									column_count = nextRow.getPhysicalNumberOfCells();
									logger.info(messageResourceBundle.getLogMessage("info.totalColumns"), column_count);
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
											logger.info(messageResourceBundle.getLogMessage("info.invalidColumn"), cell.getColumnIndex(), cell_value);
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
				logger.info(messageResourceBundle.getLogMessage("info.unsupportedFileFormat"));
			}
			System.out.println("file is valid or not....... " + isValidFile);
			if (isValidFile) {
				// --------------------------Checking Numbers & Creating Message Content
				// ------------------
				logger.info(messageResourceBundle.getLogMessage("info.columnsCount"), bulkSessionId, column_count);
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
							logger.info(messageResourceBundle.getLogMessage("info.conversion"), param, p);
							param_map.put(p, UTF16(param + p).toLowerCase());
						}
					}
				}
				logger.info(messageResourceBundle.getLogMessage("info.params"), param_map);
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
						logger.error(messageResourceBundle.getLogMessage("error.invalidDestination"), row_number, destNumber);
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
				logger.info(messageResourceBundle.getLogMessage("info.numbersCount"), bulkSessionId, msgLengthTable.keySet().size());
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
						logger.info(messageResourceBundle.getLogMessage("balance.calculated.cost"), wallet, totalcost);
						boolean amount = false;
						if (user.isAdminDepend()) {
							Optional<UserEntry> masterOptional = userEntryRepository.findBySystemId(user.getMasterId());
							if (!masterOptional.isPresent()) {
								throw new NotFoundException(
										messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
							}
							adminCost = routeService.calculateRoutingCost(masterOptional.get().getId(), msgLengthTable);
							logger.info(messageResourceBundle.getLogMessage("info.adminBalance"), masterOptional.get().getId(), adminWallet, adminCost);
							if ((adminWallet >= adminCost)) {
								if (wallet >= totalcost) {
									adminWallet = adminWallet - adminCost;
									wallet = wallet - totalcost;
									amount = true;

								} else {
									logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"), bulkSessionId, wallet);
									throw new InsufficientBalanceException(bulkSessionId + messageResourceBundle
											.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION) + wallet);
								}
							} else {
								// Insufficient Admin balance
								logger.error(messageResourceBundle.getLogMessage("error.insufficientAdminBalance"), Integer.parseInt(user.getMasterId()), bulkSessionId, adminWallet);
								throw new InsufficientBalanceException(bulkSessionId
										+ messageResourceBundle
												.getExMessage(ConstantMessages.INSUFFICIENT_ADMIN_BALANCE_EXCEPTION)
										+ Integer.parseInt(user.getMasterId()) + adminWallet);
							}
						} else {
							if (wallet >= totalcost) {
								wallet = wallet - totalcost;
								amount = true;
							} else {
								// Insufficient balance
								logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"), bulkSessionId, wallet);
								throw new InsufficientBalanceException(bulkSessionId + messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION) + wallet);
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
										logger.info(messageResourceBundle.getLogMessage("message.scheduleSuccess"));
									} else {
										// Scheduling Error
										logger.error(messageResourceBundle.getLogMessage("error.scheduleTask"));
										throw new ScheduledTimeException(messageResourceBundle
												.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION));

									}
								} else {
									// Already Scheduled
									logger.error(messageResourceBundle.getLogMessage("error.duplicateSchedule"));
									throw new ScheduledTimeException(messageResourceBundle
											.getExMessage(ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION));

								}
							} else {
								String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
										user.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									logger.info(messageResourceBundle.getLogMessage("info.batchProcessingCompleted"), value);
								} else {
									// Submission Error
									logger.error(messageResourceBundle.getLogMessage("error.batchSubmissionProcessing"), value);
									throw new InternalServerException(messageResourceBundle
											.getExMessage(ConstantMessages.BATCH_SUBMISSION_ERROR_EXCEPTION) + value);

								}
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
								bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
								bulkResponse.setBulkSessionId(bulkSessionId);

								logger.info(messageResourceBundle.getLogMessage("info.processedBalanceCost"), bulkSessionId, wallet, totalcost);
							} else {
								logger.error(messageResourceBundle.getLogMessage("error.process.failed"), bulkSessionId);
								throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							// Insufficient Balance
							logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"));
							throw new InsufficientBalanceException(messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));

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

									credits = credits - totalMsg;

									amount = true;

								} else {
									System.out.println(user.getId() + " <-- Insufficient Credits -->");
									throw new InsufficientBalanceException(user.getId() + messageResourceBundle
											.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION));
								}
							} else {
								System.out.println(user.getId() + " <-- Insufficient Admin(" + user.getMasterId()
										+ ") Credits -->");
								throw new InsufficientBalanceException(user.getId()
										+ messageResourceBundle
												.getExMessage(ConstantMessages.INSUFFICIENT_ADMIN_CREDITS_EXCEPTION)
										+ user.getMasterId());
							}
						} else {
							if (credits >= totalMsg) {
								credits = credits - totalMsg;

								amount = true;
							} else {
								System.out.println(user.getId() + " <-- Insufficient Credits -->");
								throw new InsufficientBalanceException(user.getId() + messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION));
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
										logger.info(messageResourceBundle.getLogMessage("info.taskScheduled"));

									} else {
										// Scheduling Error
										logger.error(messageResourceBundle.getLogMessage("error.scheduleTask"));
										throw new ScheduledTimeException(messageResourceBundle
												.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION));

									}
								} else {
									// Duplicate Schedule Error
									logger.error(messageResourceBundle.getLogMessage("error.duplicateSchedule"));
									throw new ScheduledTimeException(messageResourceBundle
											.getExMessage(ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION));

								}
							} else {
								String value = sendBulkSms(bulkSmsDTO, progressEvent, webEntry.isBulkOnApprove(),
										user.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									logger.info(messageResourceBundle.getLogMessage("info.batchProcessingCompleted"));
								} else {
									// Submission Error
									logger.error(messageResourceBundle.getLogMessage("error.batchSubmissionProcessing"));
									throw new InternalServerException(messageResourceBundle
											.getExMessage(ConstantMessages.BATCH_SUBMISSION_ERROR_EXCEPTION));

								}
							}

							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {

								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(Long.toString(credits));
								bulkResponse.setDeductcredits(deductCredits + "");
								bulkResponse.setBulkSessionId(bulkSessionId);
								logger.info(messageResourceBundle.getLogMessage("info.processedCreditsDeducted"), bulkSessionId, credits, deductCredits);
							} else {
								logger.info(messageResourceBundle.getLogMessage("process.failed"), bulkSessionId);
								throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"));
							throw new InsufficientBalanceException(messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));

						}
					} else if (wallet_flag.equalsIgnoreCase("MIN")) {
						// Insufficient Balance
						logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"));
						throw new InsufficientBalanceException(
								messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));

					}
				} else {

					logger.error(messageResourceBundle.getLogMessage("error.noValidNumbers"));
					throw new InternalServerException(
							messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_FILE_EXCEPTION));

				}
			} else {
				// Invalid File Format
				logger.error(messageResourceBundle.getLogMessage("error.invalidFileFormat"));
				throw new InvalidPropertyException(
						messageResourceBundle.getExMessage(ConstantMessages.INVALID_FILE_FORMAT_EXCEPTION));

			}
		} catch (InvalidPropertyException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InvalidPropertyException(e.getMessage());
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (InsufficientBalanceException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InsufficientBalanceException(e.getMessage());
		} catch (InternalServerException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InternalServerException(e.getMessage());
		} catch (ScheduledTimeException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new ScheduledTimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
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
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user.getId());
		DriverInfo driverInfo = null;
		Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(user.getId());
		if (OptionalDriverInfo.isPresent()) {
			driverInfo = OptionalDriverInfo.get();
		} else
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.DRIVE_INFO_NOT_FOUND_EXCEPTION));

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
				logger.info(messageResourceBundle.getLogMessage("info.contactScheduleRequest"), bulkSessionId);
			} else {
				logger.info(messageResourceBundle.getLogMessage("info.contactUploadRequest"), bulkSessionId);
			}
			try {

				BulkListInfo listInfo = new BulkListInfo();
				List<String> destinationList = bulkSmsDTO.getDestinationList2(listInfo);
				logger.info(messageResourceBundle.getLogMessage("info.listSummary"), bulkSessionId, listInfo.getTotal(), listInfo.getValidCount(), listInfo.getInvalidCount(), listInfo.getDuplicate());

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
				logger.info(messageResourceBundle.getLogMessage("message.type.parts.info"), bulkSessionId, bulkSmsDTO.getMessageType(), no_of_msg);
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
							logger.error(messageResourceBundle.getLogMessage("scheduled.time.before.current.error"), bulkSessionId);
						}
						String server_date = schedule_time.split(" ")[0];
						String server_time = schedule_time.split(" ")[1];
						bulkSmsDTO.setDate(server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-"
								+ server_date.split("-")[0]);
						bulkSmsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);
					} catch (Exception e) {
						logger.error(messageResourceBundle.getLogMessage("error.sessionIdWithErrorMessage"), bulkSessionId, e.getMessage(), e);
						throw new ScheduledTimeException(
								messageResourceBundle.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION)
										+ e.getMessage());
					}
					if (!valid_sch_time) {
						throw new ScheduledTimeException(messageResourceBundle
								.getExMessage(ConstantMessages.SCHEDULED_TIME_BEFORE_CURRENT_TIME_EXCEPTION));

					}
				}
				if (wallet_flag.equalsIgnoreCase("yes")) {
					bulkSmsDTO.setUserMode("wallet");
					totalcost = routeService.calculateRoutingCost(user.getId(), destinationList, no_of_msg);
					logger.info(messageResourceBundle.getLogMessage("balance.calculated.cost"), wallet, totalcost);
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

							} else {
								wallet = wallet - totalcost;
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
										logger.info(messageResourceBundle.getLogMessage("info.taskScheduledSuccessfully"), target);

									} else {
										// Scheduling Error
										String message = ("Error: Unable to schedule the task. An error occurred during the scheduling process.");
										logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
										throw new ScheduledTimeException(message);
									}
								} else {
									// already Scheduled
									String message = ("Error: The task is already scheduled. Duplicate schedule request received.");
									logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
									throw new ScheduledTimeException(message);
								}
							} else {
								String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;

									String message = ("Batch processing completed successfully. Message: " + value);
									logger.info(messageResourceBundle.getLogMessage("info.genericMessage"), message);
								} else {
									// Submission Error
									String message = ("Error: Unable to process batch submission. An error occurred during the submission process.");
									logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
									throw new InternalServerException(message);
								}
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
								bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost) + "");
								bulkResponse.setBulkSessionId(bulkSessionId);
								logger.info(messageResourceBundle.getLogMessage("info.processedBalanceCost"), bulkSessionId, wallet, totalcost);
							} else {
								logger.info(messageResourceBundle.getLogMessage("process.failed"), bulkSessionId);
								throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							// insufficient balance
							logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"), bulkSessionId, wallet);
							throw new InsufficientBalanceException(bulkSessionId + messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION) + wallet);
						}
					} else {
						// Number File Error
						String message = ("Error: No valid numbers found in the provided data. Please check the data and try again.");
						logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
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

							} else {
								credits = credits - (destinationList.size() * no_of_msg);

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
										logger.info(messageResourceBundle.getLogMessage("info.taskScheduledSuccessfully"));
									} else {
										throw new ScheduledTimeException(messageResourceBundle
												.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION));

									}
								} else {
									logger.error(messageResourceBundle.getLogMessage("error.duplicateScheduleRequest"));
									throw new ScheduledTimeException(messageResourceBundle
											.getExMessage(ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION));

								}
							} else {
								String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
								if (!value.contains("Error")) {
									target = IConstants.SUCCESS_KEY;
									// Batch Success
									logger.info(messageResourceBundle.getLogMessage("info.batchProcessingCompletedSuccessfully"), value);

								} else {
									// Submission Error
									logger.error(messageResourceBundle.getLogMessage("error.batchSubmissionProcessing"));

									throw new InternalServerException(messageResourceBundle
											.getExMessage(ConstantMessages.BATCH_SUBMISSION_ERROR_EXCEPTION));

								}
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(credits + "");
								bulkResponse.setDeductcredits(deductCredits + "");
								bulkResponse.setBulkSessionId(bulkSessionId);

								logger.info(messageResourceBundle.getLogMessage("info.processedCreditsDeducted"), bulkSessionId, credits, deductCredits);
							} else {
								logger.info(messageResourceBundle.getLogMessage("process.failed"), bulkSessionId);
								throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
							}
						} else {
							// insufficient Credits
							logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"), bulkSessionId, credits);
							throw new InsufficientBalanceException(bulkSessionId + messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION) + credits);

						}
					} else {
						// Number File Error
						logger.error(messageResourceBundle.getLogMessage("error.noValidNumbers"));
						throw new InternalServerException(
								messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_EXCEPTION));
					}
				} else if (wallet_flag.equalsIgnoreCase("MIN")) {
					// Insufficient Balance
					logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"));
					throw new InternalServerException(
							messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));
				}
			} catch (InvalidPropertyException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new InvalidPropertyException(e.getMessage());
			} catch (NotFoundException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new NotFoundException(e.getMessage());
			} catch (InsufficientBalanceException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new InsufficientBalanceException(e.getMessage());
			} catch (InternalServerException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new InternalServerException(e.getMessage());
			} catch (ScheduledTimeException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new ScheduledTimeException(e.getMessage());
			} catch (Exception e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
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
			logger.info(messageResourceBundle.getLogMessage("info.contactTrackingScheduleRequest"), bulkSessionId);
		} else {
			logger.info(messageResourceBundle.getLogMessage("info.contactTrackingUploadRequest"), bulkSessionId);
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
			logger.info(messageResourceBundle.getLogMessage("info.listInfoSummary"), listInfo.getTotal(), listInfo.getValidCount(), listInfo.getInvalidCount(), listInfo.getDuplicate());

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

			logger.info(messageResourceBundle.getLogMessage("message.type.parts.info"), bulkSessionId, bulkSmsDTO.getMessageType(), no_of_msg);
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
						logger.error(messageResourceBundle.getLogMessage("scheduled.time.before.current.error"), bulkSessionId);
					}
					String server_date = schedule_time.split(" ")[0];
					String server_time = schedule_time.split(" ")[1];
					bulkSmsDTO.setDate(server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-"
							+ server_date.split("-")[0]);
					bulkSmsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);
				} catch (Exception e) {
					logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
				}
				if (!valid_sch_time) {
					String errorMessage = "Invalid schedule time provided. Please make sure to provide a valid time.";
					logger.error(messageResourceBundle.getMessage("error.general"), errorMessage);
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
					logger.info(messageResourceBundle.getLogMessage("balance.calculated.cost"), wallet, totalcost);
					boolean amount = false;
					// boolean inherit = false;
					if (user.isAdminDepend()) {

						if (!masterOptional.isPresent()) {
							throw new NotFoundException(
									messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
						}
						adminCost = routeService.calculateRoutingCost(masterOptional.get().getId(), msgLengthTable);
						logger.info(messageResourceBundle.getLogMessage("info.adminBalanceAndCost"), masterOptional.get().getId(), adminWallet, adminCost);
						if ((adminWallet >= adminCost)) {
							if (wallet >= totalcost) {
								adminWallet = adminWallet - adminCost;
								wallet = wallet - totalcost;
								amount = true;

							} else {
								logger.error(messageResourceBundle.getLogMessage("error.insufficient.balance"), bulkSessionId);
								throw new InsufficientBalanceException(messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION) + bulkSessionId);

							}
						} else {
							// Insufficient Admin balance
							logger.error(messageResourceBundle.getLogMessage("error.insufficientAdminBalance"), masterOptional.get().getId());
							throw new InsufficientBalanceException(bulkSessionId + " "
									+ messageResourceBundle
											.getExMessage(ConstantMessages.INSUFFICIENT_ADMIN_BALANCE_EXCEPTION)
									+ masterOptional.get().getId());
						}
					} else {
						if (wallet > 0 && wallet >= totalcost) {
							wallet = wallet - totalcost;
							amount = true;

						} else {
							// Insufficient balance
							logger.error(messageResourceBundle.getLogMessage("error.insufficient.balance"), bulkSessionId);
							throw new InsufficientBalanceException(
									messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION)
											+ bulkSessionId);
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
									logger.info(messageResourceBundle.getLogMessage("info.scheduleUpdated"));
								} else {
									// Scheduling Error
									String message = "An error occurred while processing the schedule. Please try again later.";
									logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
									throw new ScheduledTimeException(message);
								}
							} else {
								// already Scheduled
								String message = "Another schedule with the same information already exists. Please provide unique schedule details.";
								logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
								throw new ScheduledTimeException(message);
							}
						} else {
							String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info(messageResourceBundle.getLogMessage("info.batchProcessingCompleted"));
							} else {
								// Submission Error
								String message = "Error occurred during batch processing. Please check your input and try again.";
								logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
								throw new InternalServerException(message);
							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setBulkListInfo(listInfo);
							bulkResponse.setBulkSessionId(bulkSessionId);
							bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
							bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
							logger.info(messageResourceBundle.getLogMessage("sms.processed"), bulkSessionId, wallet, totalcost);
						} else {
							logger.info(messageResourceBundle.getLogMessage("process.failed"), bulkSessionId);
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// insufficient balance
						String message = "Insufficient funds in your wallet. Please add funds to proceed.";
						logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
						throw new InsufficientBalanceException(message);
					}
				} else {
					// Number File Error
					String message = "Invalid or no valid number file provided. Please check and upload a valid number file.";
					logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
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

								credits = credits - totalMsg;

								amount = true;

							} else {
								logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"));
								throw new InsufficientBalanceException(messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION) + bulkSessionId);
							}
						} else {
							logger.error(messageResourceBundle.getLogMessage("error.insufficientAdminCredits"), masterOptional.get().getId());
							throw new InsufficientBalanceException(bulkSessionId
									+ messageResourceBundle
											.getExMessage(ConstantMessages.INSUFFICIENT_ADMIN_CREDITS_EXCEPTION)
									+ masterOptional.get().getId());

						}
					} else {
						if (credits >= totalMsg) {
							credits = credits - totalMsg;

							amount = true;

						} else {
							logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"));
							throw new InsufficientBalanceException(
									messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION)
											+ bulkSessionId);

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
									logger.info(messageResourceBundle.getLogMessage("info.successMessage"), successMessage);
								} else {
									String duplicateScheduleMessage = "Error: Duplicate schedule. A similar task is already scheduled.";
									logger.error(messageResourceBundle.getLogMessage("error.duplicateSchedule"));
									throw new ScheduledTimeException(duplicateScheduleMessage);
								}
							} else {
								String scheduleErrorMessage = "Error: Failed to schedule task. Please check your inputs and try again.";
								logger.error(messageResourceBundle.getLogMessage("error.scheduleError"));
								throw new ScheduledTimeException(scheduleErrorMessage);
							}
						} else {
							String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info(messageResourceBundle.getLogMessage("info.batchProcessedSuccessfully"));

							} else {
								// Submission Error
								String message = ("Error processing batch. Please check your input and try again.");
								logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
								throw new InternalServerException(message);

							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setBulkListInfo(listInfo);
							bulkResponse.setCredits(credits + "");
							bulkResponse.setDeductcredits(totalMsg + "");
							bulkResponse.setBulkSessionId(bulkSessionId);

							logger.info(messageResourceBundle.getLogMessage("message.bulkSMSProcessed"), credits, totalMsg);
						} else {
							logger.error(messageResourceBundle.getLogMessage("error.process.failed"), bulkSessionId);
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"));
						throw new InsufficientBalanceException(
								messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION));
					}
				} else {

					logger.error(messageResourceBundle.getLogMessage("error.noValidNumbersFoundInFile"));
					throw new InternalServerException(
							messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_FILE_EXCEPTION));

				}
			} else if (wallet_flag.equalsIgnoreCase("MIN")) {
				// Insufficient Balance
				logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"));
				throw new InsufficientBalanceException(
						messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));
			}
		} catch (InvalidPropertyException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InvalidPropertyException(e.getMessage());
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (InsufficientBalanceException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InsufficientBalanceException(e.getMessage());
		} catch (InternalServerException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InternalServerException(e.getMessage());
		} catch (ScheduledTimeException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new ScheduledTimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
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
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user.getId());
		DriverInfo driverInfo = null;
		Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(user.getId());
		if (OptionalDriverInfo.isPresent()) {
			driverInfo = OptionalDriverInfo.get();
		} else
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.DRIVE_INFO_NOT_FOUND_EXCEPTION));

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
			logger.info(messageResourceBundle.getLogMessage("info.groupDataScheduleRequest"), bulkContactRequest.isTracking());
		} else {
			logger.info(messageResourceBundle.getLogMessage("info.groupDataBulkUploadRequest"), bulkContactRequest.isTracking());
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
					logger.error(messageResourceBundle.getLogMessage("error.invalidNumber"), systemId, number);
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
						logger.error(messageResourceBundle.getLogMessage("scheduled.time.before.current.error"), bulkSessionId);
					}
					String server_date = schedule_time.split(" ")[0];
					String server_time = schedule_time.split(" ")[1];
					bulkSmsDTO.setDate(server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-"
							+ server_date.split("-")[0]);
					bulkSmsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);
				} catch (Exception e) {
					logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
				}
				if (!valid_sch_time) {
					throw new ScheduledTimeException(
							messageResourceBundle.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION)
									+ bulkSessionId);
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
				logger.info(messageResourceBundle.getLogMessage("balance.calculated.cost"), wallet, totalcost);
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

						} else {
							wallet = wallet - totalcost;
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
									logger.info(messageResourceBundle.getLogMessage("info.taskScheduledSuccessfully"));
								} else {
									throw new ScheduledTimeException(messageResourceBundle
											.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION));

								}
							} else {
								logger.error(messageResourceBundle.getLogMessage("error.duplicateScheduleRequest"));
								throw new ScheduledTimeException(messageResourceBundle
										.getExMessage(ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION));

							}
						} else {
							String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;

								String message = ("Batch processing completed successfully. Message: " + value);
								logger.info(messageResourceBundle.getLogMessage("info.genericMessage"), message);
							} else {
								// Submission Error
								String message = ("Error: Unable to process batch submission. An error occurred during the submission process.");
								logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
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
						logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"), bulkSessionId, wallet);
						throw new InsufficientBalanceException(bulkSessionId
								+ messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION)
								+ wallet);
					}
				} else {
					// Number File Error
					String message = ("Error: No valid numbers found in the provided data. Please check the data and try again.");
					logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
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

					} else {
						credits = credits - totalMsg;

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
								logger.info(messageResourceBundle.getLogMessage("info.taskScheduledSuccessfully"));
							} else {
								throw new ScheduledTimeException(messageResourceBundle
										.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION));

							}
						} else {
							logger.error(messageResourceBundle.getMessage("duplicate.schedule.error"));
							throw new ScheduledTimeException(messageResourceBundle
									.getExMessage(ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION));

						}
					} else {
						String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
						if (!value.contains("Error")) {
							target = IConstants.SUCCESS_KEY;

							String message = ("Batch processing completed successfully. Message: " + value);
							logger.info(messageResourceBundle.getLogMessage("info.genericMessage"), message);
						} else {
							// Submission Error
							String message = ("Error: Unable to process batch submission. An error occurred during the submission process.");
							logger.error(messageResourceBundle.getLogMessage("error.genericMessage"), message);
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
					logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"));
					throw new InsufficientBalanceException(bulkSessionId
							+ messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION)
							+ credits);
				}
			}
			// *****************************End Credit Calculation***********************
			if (wallet_flag.equalsIgnoreCase("MIN")) {
				// Insufficient Balance
				logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"));
				throw new InsufficientBalanceException(
						messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));
			}
		} catch (InvalidPropertyException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InvalidPropertyException(e.getMessage());
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (InsufficientBalanceException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InsufficientBalanceException(e.getMessage());
		} catch (InternalServerException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InternalServerException(e.getMessage());
		} catch (ScheduledTimeException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new ScheduledTimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
			throw new InternalServerException(e.getMessage());
		}
		bulkResponse.setStatus(target);
		return ResponseEntity.ok(bulkResponse);
	}

	@Override
	public ResponseEntity<?> sendSmsMms(BulkMmsRequest uploadForm, String username, HttpSession session,
			List<MultipartFile> destinationNumberFile) {
		BulkResponse bulkResponse = new BulkResponse();
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user.getId());
		DriverInfo driverInfo = null;
		Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(user.getId());
		if (OptionalDriverInfo.isPresent()) {
			driverInfo = OptionalDriverInfo.get();
		} else
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.DRIVE_INFO_NOT_FOUND_EXCEPTION));
		Optional<UserEntry> masterOptional = userEntryRepository.findBySystemId(user.getMasterId());
		if (!masterOptional.isPresent()) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		UserEntry master = masterOptional.get();
		ProgressEvent progressEvent = new ProgressEvent(session);
		String target = IConstants.FAILURE_KEY;
		double totalcost = 0, adminCost = 0;// total_defcost = 0;
		// String unicodeMsg = "";
		logger.info(messageResourceBundle.getLogMessage("info.mmsUploadAction"), user.getSystemId());
		String systemId = user.getSystemId();
		String bulkSessionId = systemId + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		try {
			BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
			bulkSmsDTO.setMessage(uploadForm.getMessage());
			bulkSmsDTO.setFrom(uploadForm.getFrom());
			bulkSmsDTO.setSmscount(uploadForm.getSmscount());
			bulkSmsDTO.setSchedule(uploadForm.isSchedule());
			bulkSmsDTO.setAlert(uploadForm.isAlert());
			bulkSmsDTO.setMessageType(uploadForm.getMessageType());
			bulkSmsDTO.setSmsParts(uploadForm.getSmsParts());
			bulkSmsDTO.setCharCount(uploadForm.getCharCount());
			bulkSmsDTO.setCharLimit(uploadForm.getCharLimit());
			bulkSmsDTO.setExclude(uploadForm.getExclude());
			bulkSmsDTO.setExpiryHour(uploadForm.getExpiryHour());
			bulkSmsDTO.setCampaignName(uploadForm.getCampaignName());
			bulkSmsDTO.setClientId(user.getSystemId());
			bulkSmsDTO.setSystemId(user.getSystemId());
			bulkSmsDTO.setPassword(new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
			bulkSmsDTO.setSenderId(uploadForm.getSenderId());
			bulkSmsDTO.setCustomContent(false);

			if (uploadForm.getDestinationNumber() != null)
				bulkSmsDTO.setDestinationNumber(uploadForm.getDestinationNumber());
			if (uploadForm.isSchedule()) {
				bulkSmsDTO.setTimestart(uploadForm.getTimestart());
				bulkSmsDTO.setRepeat(uploadForm.getRepeat());
				bulkSmsDTO.setGmt(uploadForm.getGmt());
			}
			if (uploadForm.getPeId() != null)
				bulkSmsDTO.setPeId(uploadForm.getPeId());
			if (uploadForm.getTelemarketerId() != null)
				bulkSmsDTO.setTelemarketerId(uploadForm.getTelemarketerId());
			if (uploadForm.getTemplateId() != null)
				bulkSmsDTO.setTemplateId(uploadForm.getTemplateId());
			String caption = uploadForm.getCaption();
			String mmsType = uploadForm.getMmsType();
			if (webEntry.getBatchAlertNumber() != null) {
				Set<String> alertNumbers = new HashSet<String>();
				for (String alertNumber : webEntry.getBatchAlertNumber().split(",")) {
					try {
						Long.parseLong(alertNumber);
						alertNumbers.add(alertNumber);
					} catch (Exception ex) {
						logger.error(messageResourceBundle.getLogMessage("error.invalidBatchFinishAlertNumber"), systemId, alertNumber);
					}
				}
				if (bulkSmsDTO.isAlert()) {
					for (String alertNumber : bulkSmsDTO.getDestinationNumber().split(",")) {
						try {
							Long.parseLong(alertNumber);
							alertNumbers.add(alertNumber);
						} catch (Exception ex) {
							logger.error(messageResourceBundle.getLogMessage("error.invalidAlertNumber"), systemId, alertNumber);
							throw new InternalServerException(systemId
									+ messageResourceBundle
											.getExMessage(ConstantMessages.INVALID_BATCH_FINISH_ALERT_NUMBER_MESSAGE)
									+ alertNumber + " " + ex.getMessage());
						}
					}
				}
				if (!alertNumbers.isEmpty()) {
					bulkSmsDTO.setAlert(true);
					bulkSmsDTO.setDestinationNumber(String.join(",", alertNumbers));
				}
			}
			// String fileName = IConstants.WEBAPP_DIR + "upload" + "//" + bulkSessionId;
			// bulkSmsDTO.writeToFile(fileName);
			if (bulkSmsDTO.isSchedule()) {
				logger.info(messageResourceBundle.getLogMessage("info.mmsScheduleRequest"), bulkSessionId, destinationNumberFile.size());
			} else {
				logger.info(messageResourceBundle.getLogMessage("info.mmsUploadRequest"), bulkSessionId, destinationNumberFile.size());
			}
			// ------ merge uploaded files into a list ---------------
			List<String> destinationList = null;
			List<String> temp_number_list = new ArrayList<String>();
			Map<String, Integer> errors = new HashMap<String, Integer>();
			int invalidCount = 0;
			int total = 0;
			logger.info(messageResourceBundle.getLogMessage("start.processing.uploaded.files"), bulkSessionId);
			for (MultipartFile uploaded_file : destinationNumberFile) {
				if (uploaded_file != null) {
					String file_mode = null;
					logger.info(messageResourceBundle.getLogMessage("info.processingFile"), bulkSessionId, uploaded_file.getOriginalFilename());
					if (uploaded_file.getOriginalFilename().lastIndexOf(".txt") > -1) {
						file_mode = "txt";
					} else if (uploaded_file.getOriginalFilename().lastIndexOf(".csv") > -1) {
						file_mode = "csv";
					} else if ((uploaded_file.getOriginalFilename().lastIndexOf(".xls") > -1)
							|| (uploaded_file.getOriginalFilename().lastIndexOf(".xlsx") > -1)) {
						file_mode = "xls";
					} else {
						logger.warn(messageResourceBundle.getLogMessage("warn.invalidFileUploaded"), bulkSessionId);
						continue;
					}
					Set<String> excludeSet = new HashSet<String>();
					if (uploadForm.getExclude() != null && uploadForm.getExclude().length() > 0) {
						String seperator = ",";
						if (uploadForm.getExclude().contains(",")) {
							seperator = ",";
						} else {
							seperator = "\n";
						}
						StringTokenizer tokens = new StringTokenizer(uploadForm.getExclude(), seperator);
						while (tokens.hasMoreTokens()) {
							String next = tokens.nextToken();
							if (next != null && next.length() > 0) {
								next = next.replaceAll("\\s+", ""); // Replace all the spaces in the String with empty
																	// character.
								try {
									long num = Long.parseLong(next);
									excludeSet.add(String.valueOf(num));
								} catch (NumberFormatException ne) {
									logger.error(messageResourceBundle.getLogMessage("error.invalidExcludeNumber"), next);
								}
							}
						}
					}

					if (!excludeSet.isEmpty()) {
						try {
							MultiUtility.writeExcludeNumbers(systemId, String.join("\n", excludeSet));
						} catch (Exception ex) {
							System.out.println(bulkSessionId + " " + ex);
							throw new InternalServerException(bulkSessionId + " " + ex.getMessage());
						}
					} else {
						try {
							MultiUtility.removeExcludeNumbers(systemId);
						} catch (Exception ex) {
							System.out.println(bulkSessionId + " " + ex);
							throw new InternalServerException(bulkSessionId + " " + ex.getMessage());
						}
					}
					if (file_mode != null) {
						InputStream stream = uploaded_file.getInputStream();
						int file_total_counter = 0;
						if (file_mode.equalsIgnoreCase("txt")) {
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
																									// spaces in the
																									// String with empty
																									// character.
									try {
										long num = Long.parseLong(destinationNumber);
										if (!excludeSet.contains(String.valueOf(num))) {
											temp_number_list.add(String.valueOf(num));
										} else {
											logger.info(messageResourceBundle.getLogMessage("excluded.numbers"), num);
										}
									} catch (NumberFormatException nfe) {
										int counter = 0;
										if (errors.containsKey("Invalid Destination")) {
											counter = errors.get("Invalid Destination");
										}
										errors.put("Invalid Destination", ++counter);
										logger.info(messageResourceBundle.getLogMessage("invalid.destination.number"), destinationNumber);
										invalidCount++;
									}
								} else {
									int counter = 0;
									if (errors.containsKey("Empty Row")) {
										counter = errors.get("Empty Row");
									}
									errors.put("Empty Row", ++counter);
									logger.info(messageResourceBundle.getLogMessage("empty.row.found"), row);
									invalidCount++;
								}
							}
						} else {
							try {
								Workbook workbook = null;
								if (uploaded_file.getOriginalFilename().endsWith(".xlsx")) {
									workbook = new XSSFWorkbook(stream);
								} else {
									workbook = new HSSFWorkbook(stream);
								}
								int numberOfSheets = workbook.getNumberOfSheets();
								for (int i = 0; i < numberOfSheets; i++) {
									Sheet firstSheet = workbook.getSheetAt(i);
									int total_rows = firstSheet.getPhysicalNumberOfRows();
									logger.info(messageResourceBundle.getLogMessage("info.totalRows"), i, total_rows);
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
												logger.info(messageResourceBundle.getLogMessage("info.invalidColumn"), nextRow.getRowNum() + 1);
												break;
											}
											total++;
											file_total_counter++;
											destination = new DataFormatter().formatCellValue(cell);
											// logger.info((nextRow.getRowNum() + 1) + " -> " + destination);
											if (destination != null && destination.length() > 0) {
												destination = destination.replaceAll("\\s+", ""); // Replace all the
																									// spaces in the
																									// String with empty
																									// character.
												try {
													long num = Long.parseLong(destination);
													if (!excludeSet.contains(String.valueOf(num))) {
														temp_number_list.add(String.valueOf(num));
													} else {
														logger.info(messageResourceBundle.getLogMessage("excluded.numbers"), num);
													}
												} catch (NumberFormatException nfe) {
													int counter = 0;
													if (errors.containsKey("Invalid Destination")) {
														counter = (Integer) errors.get("Invalid Destination");
													}
													errors.put("Invalid Destination", ++counter);
													logger.info(messageResourceBundle.getLogMessage("invalid.destination.number"), destination);
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
									logger.info(messageResourceBundle.getLogMessage("info.sheetProcessed"), uploaded_file.getOriginalFilename(), i);
								}
								// *********************************************************
							} catch (Exception ex) {
								logger.error(messageResourceBundle.getLogMessage("error.fileParsing"), uploaded_file, ex);
							}
						}
						logger.info(messageResourceBundle.getLogMessage("info.numberCounter"), file_total_counter);
					}
				}
			}
			logger.info(messageResourceBundle.getLogMessage("uploaded.files.processed"), bulkSessionId, temp_number_list.size());
			Set<String> hashSet = new HashSet<String>(temp_number_list);
			if (uploadForm.isAllowDuplicate()) {
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
			logger.info(messageResourceBundle.getLogMessage("info.listInfoSummary"), listInfo.getTotal(), listInfo.getValidCount(), listInfo.getInvalidCount(), listInfo.getDuplicate(), bulkSmsDTO.isAllowDuplicate());
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
			int no_of_msg = bulkSmsDTO.getSmsParts();
			if (uploadForm.getMessageType().equalsIgnoreCase("Unicode")) {
				bulkSmsDTO.setDistinct("yes");
			} else {
				bulkSmsDTO.setMessageType("SpecialChar");
			}
			logger.info(messageResourceBundle.getLogMessage("message.type.parts.info"), bulkSessionId, bulkSmsDTO.getMessageType(), no_of_msg);
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
					logger.info(messageResourceBundle.getLogMessage("info.clientTime"), client_gmt, client_time, schedule_time);
					if (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(schedule_time).after(new Date())) {
						valid_sch_time = true;
					} else {
						logger.error(messageResourceBundle.getLogMessage("scheduled.time.before.current.error"), bulkSessionId);
						throw new ScheduledTimeException(bulkSessionId + messageResourceBundle
								.getExMessage(ConstantMessages.SCHEDULED_TIME_BEFORE_CURRENT_TIME_EXCEPTION));
					}
					String server_date = schedule_time.split(" ")[0];
					String server_time = schedule_time.split(" ")[1];
					bulkSmsDTO.setDate(server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-"
							+ server_date.split("-")[0]);
					bulkSmsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);
				} catch (Exception e) {
					logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
					throw new ScheduledTimeException(bulkSessionId + e.getMessage());
				}
				if (!valid_sch_time) {
					logger.error(messageResourceBundle.getLogMessage("error.schedule.time"));
					throw new ScheduledTimeException(
							messageResourceBundle.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION));
				}
			}
			if (wallet_flag.equalsIgnoreCase("yes")) {
				bulkSmsDTO.setUserMode("wallet");
				if (destinationList.size() > 0) {
					totalcost = routeService.calculateMmsRoutingCost(user.getId(), destinationList, no_of_msg);
					logger.info(messageResourceBundle.getLogMessage("balance.calculated.cost"), wallet, totalcost);
					boolean amount = false;
					// boolean inherit = false;
					if (user.isAdminDepend()) {
						adminCost = routeService.calculateMmsRoutingCost(master.getId(), destinationList, no_of_msg);
						logger.info(messageResourceBundle.getLogMessage("admin.balance.calculated"), master.getId(), adminWallet, adminCost);
						if ((adminWallet >= adminCost)) {
							if (wallet >= totalcost) {
								adminWallet = adminWallet - adminCost;
								wallet = wallet - totalcost;
								amount = true;

							} else {
								logger.error(messageResourceBundle.getLogMessage("error.insufficient.balance"), bulkSessionId);
								throw new InsufficientBalanceException(messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION) + bulkSessionId);
							}
						} else {
							// Insufficient Admin balance
							logger.error(messageResourceBundle.getLogMessage("insufficient.admin.balance"), master.getId());
							throw new InsufficientBalanceException(
									bulkSessionId
											+ messageResourceBundle
													.getExMessage(ConstantMessages.INSUFFICIENT_ADMIN_BALANCE_EXCEPTION)
											+ master.getId());
						}
					} else {
						if (wallet > 0 && wallet >= totalcost) {
							wallet = wallet - totalcost;

							amount = true;

						} else {
							// Insufficient balance
							logger.error(messageResourceBundle.getLogMessage("error.insufficient.balance"), bulkSessionId);
							throw new InsufficientBalanceException(
									messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION)
											+ bulkSessionId);
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
									logger.info(messageResourceBundle.getLogMessage("message.scheduleSuccess"));

								} else {
									// Scheduling Error
									logger.error(messageResourceBundle.getLogMessage("error.scheduleError"));
									throw new ScheduledTimeException(messageResourceBundle
											.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION));
								}
							} else {
								// already Scheduled
								logger.error(messageResourceBundle.getLogMessage("error.duplicateSchedule"));
								throw new ScheduledTimeException(messageResourceBundle
										.getExMessage(ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION));
							}
						} else {
							String value = sendBulkMms(bulkSmsDTO, progressEvent, mmsType, caption, webEntry);
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								// Assuming the batch operation was successful
								logger.info(messageResourceBundle.getLogMessage("message.batchSuccess"));
							} else {
								// Submission Error
								logger.error(messageResourceBundle.getLogMessage("error.batchError"));
								throw new InternalServerException(messageResourceBundle
										.getExMessage(ConstantMessages.BATCH_SUBMISSION_ERROR_EXCEPTION));

							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setBulkListInfo(listInfo);
							bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
							bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
							bulkResponse.setBulkSessionId(bulkSessionId);
							logger.info(messageResourceBundle.getLogMessage("sms.processed"), bulkSessionId, wallet, totalcost);
						} else {
							logger.info(messageResourceBundle.getLogMessage("process.failed"), bulkSessionId);
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// insufficient balance
						logger.error(messageResourceBundle.getLogMessage("error.insufficientWallet"));
						throw new InsufficientBalanceException(
								messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));
					}
				} else {
					// Number File Error
					logger.error(messageResourceBundle.getLogMessage("error.noValidNumber"));
					throw new InsufficientBalanceException(
							messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_FILE_EXCEPTION));
				}
			} else if (wallet_flag.equalsIgnoreCase("no")) {
				bulkSmsDTO.setUserMode("credit");
				if (destinationList.size() > 0) {
					long credits = balanceEntry.getCredits();
					long adminCredit = masterbalance.getCredits();
					boolean amount = false;
					// boolean inherit = false;
					if (user.isAdminDepend()) {
						if (adminCredit >= (destinationList.size() * no_of_msg)) {
							if (credits >= (destinationList.size() * no_of_msg)) {
								adminCredit = adminCredit - (destinationList.size() * no_of_msg);

								credits = credits - (destinationList.size() * no_of_msg);

								amount = true;

							} else {
								logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"));
								throw new InsufficientBalanceException(messageResourceBundle
										.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION) + bulkSessionId);
							}
						} else {
							logger.error(messageResourceBundle.getLogMessage("error.insufficientAdminCredits"), master.getId());
							throw new InsufficientBalanceException(
									bulkSessionId
											+ messageResourceBundle
													.getExMessage(ConstantMessages.INSUFFICIENT_ADMIN_CREDITS_EXCEPTION)
											+ master.getId());
						}
					} else {
						if (credits >= (destinationList.size() * no_of_msg)) {
							credits = credits - (destinationList.size() * no_of_msg);

							amount = true;

						} else {
							logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"));
							throw new InsufficientBalanceException(
									messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION)
											+ bulkSessionId);
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
									logger.info(messageResourceBundle.getLogMessage("message.scheduleSuccess"));
								} else {
									logger.error(messageResourceBundle.getLogMessage("error.scheduleError"));
									throw new ScheduledTimeException(messageResourceBundle
											.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION));
								}
							} else {
								logger.error(messageResourceBundle.getLogMessage("error.duplicateSchedule"));
								throw new ScheduledTimeException(messageResourceBundle
										.getExMessage(ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION));
							}
						} else {
							String value = sendBulkMms(bulkSmsDTO, progressEvent, mmsType, caption, webEntry);
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info(messageResourceBundle.getLogMessage("message.batchSuccess"));
							} else {
								// Submission Error
								logger.error(messageResourceBundle.getLogMessage("error.batchError"));
								throw new InternalServerException(messageResourceBundle
										.getExMessage(ConstantMessages.BATCH_SUBMISSION_ERROR_EXCEPTION));
							}
						}
						if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
							bulkResponse.setCredits(Long.toString(credits));
							bulkResponse.setDeductcredits(deductCredits + "");
							bulkResponse.setBulkSessionId(bulkSessionId);
							bulkResponse.setBulkListInfo(listInfo);
							logger.info(messageResourceBundle.getLogMessage("info.processedCreditsDeducted"), credits, deductCredits);
						} else {
							logger.error(messageResourceBundle.getLogMessage("error.process.failed"), bulkSessionId);
							throw new InternalServerException(bulkSessionId + "<-- Process Failed --> ");
						}
					} else {
						// insufficient Credits
						logger.error(messageResourceBundle.getLogMessage("error.insufficientCredit"));
						throw new InsufficientBalanceException(
								messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION));
					}
				} else {
					// Number File Error
					logger.error(messageResourceBundle.getLogMessage("error.noValidNumber"));
					throw new InternalServerException(
							messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_FILE_EXCEPTION));
				}
			} else if (wallet_flag.equalsIgnoreCase("MIN")) {
				// insufficient balance
				logger.error(messageResourceBundle.getLogMessage("error.insufficientWallet"));
				throw new InsufficientBalanceException(
						messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));
			}
		} catch (InvalidPropertyException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InvalidPropertyException(e.getMessage());
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (InsufficientBalanceException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InsufficientBalanceException(e.getMessage());
		} catch (InternalServerException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new InternalServerException(e.getMessage());
		} catch (ScheduledTimeException e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
			throw new ScheduledTimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
			throw new InternalServerException(e.getMessage());
		}

		bulkResponse.setStatus(target);
		return ResponseEntity.ok(bulkResponse);
	}

	@Override
	public ResponseEntity<?> autoSchedule(MultipartFile destinationNumberFile, String username,
			BulkAutoScheduleRequest bulkAutoScheduleRequest) {
		BulkResponse bulkResponse = new BulkResponse();
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		DriverInfo driverInfo = null;
		Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(user.getId());
		if (OptionalDriverInfo.isPresent()) {
			driverInfo = OptionalDriverInfo.get();
		} else
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.DRIVE_INFO_NOT_FOUND_EXCEPTION));

		Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findBySystemId(user.getSystemId());

		BalanceEntry balanceEntry = null;

		if (balanceOptional.isPresent()) {

			balanceEntry = balanceOptional.get();
		}
		List<ScheduleEntryExt> scheduleList = new ArrayList<ScheduleEntryExt>();
		String target = IConstants.FAILURE_KEY;
		String systemId = user.getSystemId();
		logger.info(messageResourceBundle.getLogMessage("info.autoScheduleRequest"), systemId, destinationNumberFile);
		String bulkSessionId = systemId + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		String uploaded_file = destinationNumberFile.getOriginalFilename();
		if ((uploaded_file.lastIndexOf(".xls") > -1) || (uploaded_file.lastIndexOf(".xlsx") > -1)) {
			List<String[]> param_list = new ArrayList<String[]>();
			Workbook workbook = null;
			try {
				InputStream inputStream = destinationNumberFile.getInputStream();
				if (uploaded_file.endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(inputStream);
				} else {
					workbook = new HSSFWorkbook(inputStream);
				}
				Sheet firstSheet = workbook.getSheetAt(0);
				logger.info(messageResourceBundle.getLogMessage("info.totalRows"), bulkSessionId, firstSheet.getPhysicalNumberOfRows());
				Iterator<org.apache.poi.ss.usermodel.Row> iterator = firstSheet.iterator();
				int column_count = 0;
				while (iterator.hasNext()) {
					org.apache.poi.ss.usermodel.Row nextRow = iterator.next();
					if (nextRow.getRowNum() == 0) {
						column_count = nextRow.getPhysicalNumberOfCells();
						logger.info(messageResourceBundle.getLogMessage("info.totalColumns"), column_count);
						if (column_count < 5) {
							logger.error(messageResourceBundle.getLogMessage("error.invalidFormat"), bulkSessionId);
							break;
						}
					} else {
						Iterator<Cell> cellIterator = nextRow.cellIterator();
						String cell_value = null;
						String params[] = new String[column_count];
						while (cellIterator.hasNext()) {
							Cell cell = cellIterator.next();
							cell_value = new DataFormatter().formatCellValue(cell);
							if (cell.getColumnIndex() < column_count) {
								params[cell.getColumnIndex()] = cell_value;
							} else {
								logger.info(messageResourceBundle.getLogMessage("info.invalidColumn"), cell.getColumnIndex(), cell_value);
							}
						}
						param_list.add(params);
					}
				}
			} catch (Exception ex) {
				logger.error(bulkSessionId, ex);
			} finally {
				try {
					workbook.close();
				} catch (Exception e) {
				}
			}
			if (!param_list.isEmpty()) {
				try {
					// BulkSmsDTO bulkSmsDTO = null;
					String msgType = bulkAutoScheduleRequest.getMessageType();
					String msg_content = null;
					if (msgType.equalsIgnoreCase("7bit")) {
						String sp_msg = bulkAutoScheduleRequest.getMessage();
						msg_content = SmsConverter.getContent(sp_msg.toCharArray());
						msgType = "SpecialChar";
					} else {
						msg_content = bulkAutoScheduleRequest.getMessage();
					}
					int entry_number = 1;
					for (String[] entry : param_list) {
						System.out.println(bulkSessionId + " Starting Process For Entry: " + entry_number);
						if (entry[0] != null && entry[0].length() == 6) {
							if (entry[1] != null && entry[1].length() == 4
									&& Integer.parseInt(entry[1]) >= Calendar.getInstance().get(Calendar.YEAR)) {
								if (entry[2] != null && Integer.parseInt(entry[2]) > 0
										&& Integer.parseInt(entry[2]) <= 12) {
									if (entry[3] != null && Integer.parseInt(entry[3]) > 0
											&& Integer.parseInt(entry[3]) <= 31) {
										if (entry[4] != null && entry[4].length() == 5) {
											if (entry[5] != null && entry[5].length() > 0) {
												String number = entry[5];
												if (number.startsWith("+")) {
													number = number.substring(1, number.length());
												}
												number = number.replaceAll("\\s+", ""); // Replace all the spaces in the
																						// String with empty character.
												try {
													Long.parseLong(number);
												} catch (NumberFormatException nfe) {
													logger.info(messageResourceBundle.getLogMessage("info.invalidMobileEntry"), number, entry_number);
													continue;
												}
												String YYYY = entry[1];
												String MM = entry[2];
												String dd = entry[3];
												if (MM.length() == 1) {
													MM = "0" + MM;
												}
												if (dd.length() == 1) {
													dd = "0" + dd;
												}
												BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
												// -------- proceed for time calculation -----------
												// Calendar calendar = Calendar.getInstance();
												String client_time = dd + "-" + MM + "-" + YYYY + " " + entry[4]
														+ ":00";
												String client_gmt = "GMT" + entry[0];
												System.out.println(
														"client_time: " + client_time + " client_gmt: " + client_gmt);
												SimpleDateFormat client_formatter = new SimpleDateFormat(
														"dd-MM-yyyy HH:mm:ss");
												client_formatter.setTimeZone(TimeZone.getTimeZone(client_gmt));
												SimpleDateFormat local_formatter = new SimpleDateFormat(
														"dd-MM-yyyy HH:mm:ss");
												String schedule_time = null;
												try {
													schedule_time = local_formatter
															.format(client_formatter.parse(client_time));
													System.out
															.println(bulkSessionId + " server_time: " + schedule_time);
													if (local_formatter.parse(schedule_time).before(new Date())) {
														logger.info(messageResourceBundle.getLogMessage("info.scheduledTimeBeforeCurrentTime"), entry_number, schedule_time);
														continue;

													}
													String server_date = schedule_time.split(" ")[0];
													String server_time = schedule_time.split(" ")[1];
													bulkSmsDTO.setDate(
															server_date.split("-")[2] + "-" + server_date.split("-")[1]
																	+ "-" + server_date.split("-")[0]);
													bulkSmsDTO.setTime(
															server_time.split(":")[0] + "" + server_time.split(":")[1]);
												} catch (Exception e) {
													logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
													continue;
												}
												bulkSmsDTO.setTimestart(client_time);
												bulkSmsDTO.setGmt(client_gmt);
												// ----------------Proceed for other values ---------------------
												// System.out.println("Message: " + msg_content);
												String msg = new String(msg_content);
												int param_number = 1;
												for (int i = 6; i < entry.length; i++) {
													String param_value = entry[i];
													// System.out.println("entry[" + i + "]: " + param_value);
													if (msgType.equalsIgnoreCase("Unicode")) {
														String encoded_value = UTF16(param_value).toLowerCase();
														String encoded_param = UTF16("param" + param_number)
																.toLowerCase();
														if (encoded_param != null) {
															try {
																msg = msg.replaceAll(encoded_param,
																		Matcher.quoteReplacement(encoded_value));
															} catch (Exception ex) {
																// System.out.println(ex + " ==> " + destNumber + ": " +
																// entry);
																System.out.println(ex + ": " + encoded_param + " ==> "
																		+ encoded_value);
															}
														}
													} else {
														String asciilist = "";
														for (int h = 0; h < param_value.length(); h++) {
															int asciiNum = param_value.charAt(h);
															asciilist += asciiNum + ",";
														}
														String hex_param = SevenBitChar.getHexValue(asciilist);
														param_value = SmsConverter.getContent(hex_param.toCharArray());
														msg = msg.replaceAll("param" + param_number,
																Matcher.quoteReplacement(param_value));
													}
													// System.out.println("Converted: " + msg);
													param_number++;
												}
												List<String> destination_list = new ArrayList<String>();
												destination_list.add(number);
												if (bulkAutoScheduleRequest.isTracking()) {
													// --------- for tracking -------------------
													String campaign_name = bulkAutoScheduleRequest.getCampaignName();
													System.out.println("Received Web Links: "
															+ bulkAutoScheduleRequest.getWeblink().length);
													List<String> web_links_list = new ArrayList<String>();
													for (String link : bulkAutoScheduleRequest.getWeblink()) {
														if (link != null && link.length() > 0) {
															web_links_list.add(link);
														}
													}
													System.out.println("Final Web Links: " + web_links_list);
													Map<String, String> campaign_mapping = getCampaignId(systemId,
															bulkAutoScheduleRequest.getSenderId(),
															IConstants.GATEWAY_NAME, web_links_list,
															String.join(",", destination_list), campaign_name);
													for (int i = 0; i < web_links_list.size(); i++) {
														if (campaign_mapping.containsKey(web_links_list.get(i))) {
															String appending_url = "http://1l.ae/"
																	+ campaign_mapping.get(web_links_list.get(i))
																	+ "/r=1";
															String web_link_hex_param = null;
															if (msgType.equalsIgnoreCase("Unicode")) {
																web_link_hex_param = "005B007700650062005F006C0069006E006B005F0074007200610063006B0069006E0067005F00750072006C005D"
																		.toLowerCase();
																msg = msg.replaceFirst(web_link_hex_param,
																		UTF16(appending_url).toLowerCase());
															} else {
																web_link_hex_param = SevenBitChar.getHexValue(
																		"91,119,101,98,95,108,105,110,107,95,116,114,97,99,107,105,110,103,95,117,114,108,93,");
																web_link_hex_param = SmsConverter
																		.getContent(web_link_hex_param.toCharArray());
																msg = msg.replaceFirst(web_link_hex_param,
																		appending_url);
															}
														}
													}
												}
												bulkSmsDTO.setFrom(bulkAutoScheduleRequest.getFrom());
												bulkSmsDTO.setSenderId(bulkAutoScheduleRequest.getSenderId());
												bulkSmsDTO.setMessageType(msgType);
												bulkSmsDTO.setDestinationList(destination_list);
												bulkSmsDTO.setMessage(msg);
												bulkSmsDTO.setOrigMessage(UTF16(bulkAutoScheduleRequest.getMessage()));
												bulkSmsDTO.setRepeat("No");
												bulkSmsDTO.setReqType("bulk");
												bulkSmsDTO.setClientId(systemId);
												bulkSmsDTO.setSystemId(systemId);
												bulkSmsDTO.setPassword(new PasswordConverter()
														.convertToEntityAttribute(driverInfo.getDriver()));
												if (balanceEntry.getWalletFlag().equalsIgnoreCase("No")) {
													bulkSmsDTO.setUserMode("credit");
												} else {
													bulkSmsDTO.setUserMode("wallet");
												}
												// ------- create schedule for this entry ------------------
												System.out
														.println(bulkSessionId + " Creating Schedule: " + entry_number);
												String filename = new SendSmsService().createScheduleFile(bulkSmsDTO);
												int generated_id = 0;
												ScheduleEntry scheduleEntry = null;
												if (filename != null) {
													scheduleEntry = new ScheduleEntry();
													scheduleEntry.setClientGmt(bulkSmsDTO.getGmt());
													scheduleEntry.setClientTime(bulkSmsDTO.getTimestart());
													scheduleEntry.setServerId(IConstants.SERVER_ID);
													scheduleEntry.setDate(bulkSmsDTO.getDate());
													scheduleEntry.setServerTime(bulkSmsDTO.getTime());
													scheduleEntry.setUsername(systemId);
													scheduleEntry.setFileName(filename);
													scheduleEntry.setStatus("false");
													scheduleEntry.setRepeated(bulkSmsDTO.getRepeat());
													scheduleEntry.setScheduleType(bulkSmsDTO.getReqType());
													scheduleEntry.setWebId(null);
													generated_id = scheduleEntryRepository.save(scheduleEntry).getId();
													if (generated_id > 0) {
														scheduleEntry.setId(generated_id);
														ScheduleEntryExt ext = new ScheduleEntryExt(scheduleEntry);
														ext.setMessageType(bulkSmsDTO.getMessageType());
														ext.setTotalNumbers(bulkSmsDTO.getDestinationList().size());
														ext.setSenderId(bulkSmsDTO.getSenderId());
														ext.setCustomContent(bulkSmsDTO.isCustomContent());
														ext.setCampaign(bulkSmsDTO.getCampaignName());
														scheduleList.add(ext);
														String today = Validation.getTodayDateFormat();
														if (today.equalsIgnoreCase(bulkSmsDTO.getDate().trim())) {
															Set<Integer> set = null;
															if (GlobalVarsSms.ScheduledBatches
																	.containsKey(bulkSmsDTO.getTime())) {
																set = GlobalVarsSms.ScheduledBatches
																		.get(bulkSmsDTO.getTime());
															} else {
																set = new LinkedHashSet<Integer>();
															}
															set.add(generated_id);
															GlobalVarsSms.ScheduledBatches.put(bulkSmsDTO.getTime(),
																	set);
														}
													} else {
														// Scheduling Error
														logger.error(messageResourceBundle.getLogMessage("error.schedulingError"), entry_number);
														throw new ScheduledTimeException(
																entry_number + "<--  Scheduling Error --> ");
													}
												} else {
													logger.error(messageResourceBundle.getLogMessage("error.schedulingFileCreationError"), entry_number);
													throw new ScheduledTimeException(
															entry_number + "<--  Scheduling File Creation Error --> ");
												}
												// -------- end create schedule ----------------------------
											} else {
												logger.error(messageResourceBundle.getLogMessage("error.invalidMobileEntry"), bulkSessionId, entry[5], entry_number);
												throw new InternalServerException(bulkSessionId + " Invalid Mobile["
														+ entry[5] + "] Entry: " + entry_number);
											}
										} else {
											logger.error(messageResourceBundle.getLogMessage("error.invalidTimeEntry"), bulkSessionId, entry[4], entry_number);
											throw new InternalServerException(bulkSessionId + " Invalid Time["
													+ entry[4] + "] Entry: " + entry_number);
										}
									} else {
										logger.error(messageResourceBundle.getLogMessage("error.invalidDayEntry"), bulkSessionId, entry[3], entry_number);
										throw new InternalServerException(bulkSessionId + " Invalid Day[" + entry[3]
												+ "] Entry: " + entry_number);
									}
								} else {
									logger.error(messageResourceBundle.getLogMessage("error.invalidMonthEntry"), bulkSessionId, entry[2], entry_number);
									throw new InternalServerException(
											bulkSessionId + " Invalid Month[" + entry[2] + "] Entry: " + entry_number);
								}
							} else {
								logger.error(messageResourceBundle.getLogMessage("error.invalidYearEntry"), bulkSessionId, entry[2], entry_number);
								throw new InternalServerException(
										bulkSessionId + " Invalid Year[" + entry[1] + "] Entry: " + entry_number);
							}
						} else {
							logger.error(messageResourceBundle.getLogMessage("error.invalidGmtEntry"), bulkSessionId, entry[2], entry_number);
							throw new InternalServerException(
									bulkSessionId + " Invalid Gmt[" + entry[0] + "] Entry: " + entry_number);
						}
						entry_number++;
					}
					if (!scheduleList.isEmpty()) {
						target = IConstants.SUCCESS_KEY;
						logger.info(messageResourceBundle.getLogMessage("info.totalScheduleCreated"), bulkSessionId, scheduleList.size());

					} else {
						logger.info(messageResourceBundle.getLogMessage("info.noValidEntryFound"), bulkSessionId);
						throw new InternalServerException(bulkSessionId + "<-- No Valid Entry Found --> ");
					}
				} catch (ScheduledTimeException e) {
					logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
					throw new ScheduledTimeException(e.getMessage());
				} catch (Exception e) {
					logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
					throw new InternalServerException(e.getMessage());
				}
			} else {
				logger.error(messageResourceBundle.getLogMessage("error.noValidEntryFound"), bulkSessionId);
			}
		} else {
			throw new InternalServerException("please put valid file format...");
		}
		return ResponseEntity.ok(scheduleList);
	}

	public String sendBulkMms(BulkSmsDTO bulkSmsDTO, ProgressEvent progressEvent, String mmsType, String caption,
			WebMasterEntry webEntry) {
		String response = "";
		try {
			setProgressEvent(progressEvent);
			response = sendBulkMms(bulkSmsDTO, mmsType, caption, webEntry);
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("error.fillInStackTrace"), bulkSmsDTO.getSystemId(), e.fillInStackTrace());
		}

		return response;
	}

	public String sendBulkMms(BulkSmsDTO bulkSmsDTO, String mmsType, String caption, WebMasterEntry webEntry) {
		logger.info(messageResourceBundle.getLogMessage("info.bulkSmsRequest"), bulkSmsDTO.getSystemId(), bulkSmsDTO.getReqType(), bulkSmsDTO.isAlert(), bulkSmsDTO.getDestinationNumber());
		String user = bulkSmsDTO.getSystemId();
		// QueueBackup backupObject = null;
		BulkEntry entry = null;
		int ston = 5;
		int snpi = 0;
		String ret = "";
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
			// ------ mms params ------
			entry.setMmsType(mmsType);
			entry.setCaption(UTF16(caption));
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
						logger.info(messageResourceBundle.getLogMessage("info.invalidAlertNumber"), alertNumber);
					}
				}
				if (!alertNumbers.isEmpty()) {
					entry.setAlertNumbers(String.join(",", alertNumbers));
					entry.setAlert(true);
				} else {
					logger.info(messageResourceBundle.getLogMessage("info.invalidAlertNumbers"), bulkSmsDTO.getDestinationNumber());
					entry.setAlert(false);
				}
			}
			entry.setExpiryHour(bulkSmsDTO.getExpiryHour());
			// ---------------- For Batch Content --------------------
			List<BulkContentEntry> bulk_list = new ArrayList<BulkContentEntry>();
			logger.info(messageResourceBundle.getLogMessage("info.preparingBatchContentList"));
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
						logger.info(messageResourceBundle.getLogMessage("info.invalidNumber"), map_entry.getKey());
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
						logger.info(messageResourceBundle.getLogMessage("info.invalidNumber"), destination_loop);
					}
				}
			}
			logger.info(messageResourceBundle.getLogMessage("info.endPreparingBatchContentList"));

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
				logger.info(messageResourceBundle.getLogMessage("info.entryAdded"), entry.toString());
				if (event != null) {
					saveBulkMgmtContent(batch_id, bulk_list, event);
				} else {
					saveBulkMgmtContent(batch_id, bulk_list);
				}
				try {
					addSummaryReport(backupExt);
				} catch (Exception ex) {
					logger.error(messageResourceBundle.getLogMessage("error.summaryReport"), user, ex);
				}
				GlobalVars.BatchQueue.put(batch_id, new BatchObject(batch_id, user, IConstants.SERVER_ID, true));
				logger.info(messageResourceBundle.getLogMessage("info.batchAddedToProcessing"), user, entry.getId());
			} else {
				logger.info(messageResourceBundle.getLogMessage("info.entryNotAdded"), user, entry.toString());
			}
			// }
		} catch (Exception ex) {
			ret = "Error: " + ex.getMessage();
			logger.error(messageResourceBundle.getLogMessage("error.startError"), ex.toString(), user, batch_id);
		}
		return ret;
	}

	public List<GroupDataEntry> listGroupData(SearchCriteria searchCriteria) {
		logger.info(messageResourceBundle.getLogMessage("info.groupDataCriteria"), searchCriteria.getGroupId());
		try {
			// Constructing the base query
			List<GroupDataEntry> groupDataEntries = null;

			if (searchCriteria.getNumber() != null && searchCriteria.getNumber().length > 0) {
				logger.info(messageResourceBundle.getLogMessage("info.criteriaForNumbers"), searchCriteria.getNumber().length);
				groupDataEntries = groupDataEntryRepository.findByNumberInAndGroupId(
						ArrayUtils.toObject(searchCriteria.getNumber()), searchCriteria.getGroupId());
			}

			return groupDataEntries;
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("error.exceptionOccurred"), e);
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
		logger.info(messageResourceBundle.getLogMessage("info.trackingDetails"), jsonobj.toString());
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
			logger.info(messageResourceBundle.getLogMessage("info.campaignResponse"), buf.toString());

			result_map.put(links.get(0), buf.toString());
		} else {
			JSONObject resultJson = new JSONObject(buf.toString());
			logger.info(messageResourceBundle.getLogMessage("info.campaignResponse"), resultJson.toString());
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
			logger.info(messageResourceBundle.getLogMessage("info.readingScheduleFile"), file);
			SendSmsService service = new SendSmsService();
			BulkSmsDTO bulkSmsDTO = service.readScheduleFile(file);
			int user_id = userEntryRepository.getUsers(bulkSmsDTO.getSystemId()).get().getUserId();
			String mode = bulkSmsDTO.getUserMode();
			logger.info(messageResourceBundle.getLogMessage("info.fileMode"), bulkSmsDTO.getSystemId(), bulkSmsDTO.getPassword(), mode);
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
					logger.info(messageResourceBundle.getLogMessage("info.sufficientCredits"), bulkSmsDTO.getSystemId(), credits);
					String response = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user_id);
					toReturn = "Scheduled Successfully" + response;
				} else {
					toReturn = "InSufficient Credits";
					logger.error(messageResourceBundle.getLogMessage("error.insufficientCredits"), bulkSmsDTO.getSystemId(), credits);
				}
			} else if (mode.equalsIgnoreCase("wallet")) {
				if (totalWalletCost <= walletAmt) {
					logger.info(messageResourceBundle.getLogMessage("info.sufficientBalance"), bulkSmsDTO.getSystemId(), walletAmt, totalWalletCost);
					String response = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user_id);
					toReturn = "Scheduled Successfully" + response;
				} else {
					toReturn = "InSufficient Wallet";
					logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"), bulkSmsDTO.getSystemId(), walletAmt, totalWalletCost);
				}
			}
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("error.fileProcessingError"), e);
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
						logger.info(messageResourceBundle.getLogMessage("info.scheduleExpired"), id, scheduled_time);
						isAfter = false;
					} else {
						logger.info(messageResourceBundle.getLogMessage("info.scheduleListed"), id, scheduled_time);
						isAfter = true;
					}
				} catch (ParseException e) {
					logger.error(messageResourceBundle.getLogMessage("error.scheduleParseError"), id, date, time);
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
			logger.error(messageResourceBundle.getLogMessage("error.emptyMessage"), sqle.getMessage(), sqle);
		}
	}

//================================edit=================================
	@Override
	public ResponseEntity<?> editBulk(String username, int batchId) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		BulkProccessResponse bulkProccessResponse = new BulkProccessResponse();
		String target = IConstants.FAILURE_KEY;
		logger.info(messageResourceBundle.getLogMessage("info.editRequest"), batchId);
		try {
			BatchObject batch = GlobalVars.BatchQueue.get(batchId);
			if (batch != null && batch.isActive()) {
				logger.info(messageResourceBundle.getLogMessage("info.deactivateBatch"), batchId);
				batch.setActive(false);
				GlobalVars.BatchQueue.replace(batchId, batch);
			}

			if (batch != null) {
				Optional<BulkEntry> bulkOptional = bulkEntryRepository.findById(batchId);
				if (!bulkOptional.isPresent()) {
					throw new NotFoundException("bulk entity not foud with id " + batchId);
				}
				BulkEntry entry = bulkOptional.get();

				if (entry != null) {
					String message = SmsConverter.uniHexToCharMsg(entry.getContent());
					String from = "Name";

					if (entry.getSton() == 1 && entry.getSnpi() == 1) {
						from = "Mobile";
					}

					String sender = entry.getSenderId();

					bulkProccessResponse.setBatchId(entry.getId() + "");
					bulkProccessResponse.setDelay(entry.getDelay() + "");
					bulkProccessResponse.setExpiry(entry.getExpiryHour() + "");
					bulkProccessResponse.setFrom(from);
					bulkProccessResponse.setSender(sender);
					bulkProccessResponse.setMessage(message);
					bulkProccessResponse.setReqType(entry.getReqType());
					target = IConstants.SUCCESS_KEY;

					logger.info(messageResourceBundle.getLogMessage("info.batchEdited"), batchId);
				} else {
					logger.error(messageResourceBundle.getLogMessage("error.batchNotFound"), batchId);
					throw new NotFoundException("BatchId " + batchId + " not found in bulkEntryRepository.");
				}
			} else {
				logger.error(messageResourceBundle.getLogMessage("error.batchNotFound"), batchId);
				throw new NotFoundException("BatchId " + batchId + " not found in BatchQueue.");
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception ex) {
			logger.error(messageResourceBundle.getLogMessage("error.editBatch"), batchId, ex);
			throw new InternalServerException("Error editing BatchId: " + batchId);
		}

		return ResponseEntity.ok(bulkProccessResponse);
	}

	@Override
	public ResponseEntity<?> pauseBulk(String username, int batchId) {
		String target = IConstants.FAILURE_KEY;

		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		try {
			logger.info(messageResourceBundle.getLogMessage("info.pauseBatch"), batchId);
			BatchObject batch = GlobalVars.BatchQueue.get(batchId);

			if (batch != null) {
				batch.setActive(false);
				GlobalVars.BatchQueue.replace(batchId, batch);
				logger.info(messageResourceBundle.getLogMessage("info.batchPaused"), batchId);
				logger.info(messageResourceBundle.getLogMessage("info.batchPausedSuccessfully"));
				target = "Paused";
			} else {
				logger.warn(messageResourceBundle.getLogMessage("warn.batchNotFound"), batchId);
				throw new NotFoundException("Batch Not Found: " + batchId);
			}
		} catch (NotFoundException ex) {
			logger.error(messageResourceBundle.getLogMessage("error.pauseBatchNotFound"), ex);
			throw ex;
		} catch (Exception ex) {
			logger.error(messageResourceBundle.getLogMessage("error.pauseBatchError"), batchId, ex);
			throw new InternalServerException("Error pausing batch: " + batchId);
		}

		return ResponseEntity.ok(target);
	}

	@Override
	public ResponseEntity<?> abortBulk(String username, int batchId) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		String target = IConstants.FAILURE_KEY;

		try {
			logger.info("Abort Request For BatchId: " + batchId);
			BatchObject batch = GlobalVars.BatchQueue.remove(batchId);

			if (batch != null) {
				logger.info("Batch Removed: " + batchId);
				logger.info("Batch aborted successfully.");
				target = "abort";
			} else {
				logger.warn(messageResourceBundle.getLogMessage("warn.batchNotFound"), batchId);
				throw new NotFoundException("Batch Not Found: " + batchId);
			}

			logger.info("Abort Request target: " + target);
		} catch (NotFoundException ex) {
			logger.error("Error aborting batch: Batch Not Found", ex);
			throw ex;
		} catch (Exception ex) {
			logger.error("Error aborting batch: " + batchId, ex);
			throw new InternalServerException("Error aborting batch: " + batchId);
		}

		return ResponseEntity.ok(target);
	}

	@Override
	public ResponseEntity<?> resumeBulk(String username, int batchId) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		String target = IConstants.FAILURE_KEY;

		try {
			logger.info(messageResourceBundle.getLogMessage("info.resumeBatch"), batchId);
			BatchObject batch = GlobalVars.BatchQueue.get(batchId);

			if (batch != null) {
				batch.setActive(true);
				GlobalVars.BatchQueue.replace(batchId, batch);
				logger.info(messageResourceBundle.getLogMessage("info.batchResumed"), batchId);
				logger.info(messageResourceBundle.getLogMessage("info.batchResumedSuccess"));
				target = "resume";
			} else {
				logger.warn(messageResourceBundle.getLogMessage("warn.batchNotFound"), batchId);
				throw new NotFoundException("Batch Not Found: " + batchId);
			}

			logger.info(messageResourceBundle.getLogMessage("info.resumeRequest"), target);

		} catch (NotFoundException ex) {
			logger.error(messageResourceBundle.getLogMessage("error.resumeBatchNotFound"), ex);
			throw ex;
		} catch (Exception ex) {
			logger.error(messageResourceBundle.getLogMessage("error.resumeBatch"), batchId, ex);

			throw new InternalServerException("Error resuming batch: " + batchId);
		}

		return ResponseEntity.ok(target);
	}

	@Override
	public ResponseEntity<?> sendModifiedBulk(String username, BulkUpdateRequest bulkUpdateRequest) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		String target = IConstants.SUCCESS_KEY;

		int id = bulkUpdateRequest.getId();
		try {
			BulkEntry queueBackup = bulkEntryRepository.findById(id).get();
			logger.info(messageResourceBundle.getLogMessage("info.fileIdModified"), id, bulkUpdateRequest.getExpiryHour(), bulkUpdateRequest.getDelay());
			queueBackup.setDelay(bulkUpdateRequest.getDelay());
			int ston = 5;
			int snpi = 0;
			if (bulkUpdateRequest.getFrom().compareToIgnoreCase("Mobile") == 0) {
				ston = 1;
				snpi = 1;
			}
			queueBackup.setSton(ston);
			queueBackup.setSnpi(snpi);
			queueBackup.setSenderId(bulkUpdateRequest.getSenderId());
			queueBackup.setReqType(bulkUpdateRequest.getReqType());
			queueBackup.setExpiryHour(bulkUpdateRequest.getExpiryHour());
			queueBackup.setContent(bulkUpdateRequest.getMessage());
			String unicodeMsg = null;
			// int no_of_msg = smsForm.getSmsParts();
			if (bulkUpdateRequest.getMessageType().equalsIgnoreCase("Unicode")) {
				queueBackup.setMessageType("Unicode");
				unicodeMsg = bulkUpdateRequest.getMessage();
			} else {
				String sp_msg = bulkUpdateRequest.getMessage();
				unicodeMsg = SmsConverter.getContent(sp_msg.toCharArray());
				unicodeMsg = UTF16(unicodeMsg);
				queueBackup.setMessageType("SpecialChar");
			}
			bulkEntryRepository.save(queueBackup);
			List<BulkContentEntry> list = listContent(id);
			for (BulkContentEntry entry : list) {
				entry.setContent(unicodeMsg);
				entry.setFlag("F");
			}
			UpdateContent(list, "batch_content_" + id);
			BatchObject batch = GlobalVars.BatchQueue.get(id);
			if (!batch.isActive()) {
				batch.setActive(true);
			}
			GlobalVars.BatchQueue.replace(id, batch);
		} catch (Exception ex) {
			target = IConstants.FAILURE_KEY;
			logger.error(messageResourceBundle.getLogMessage("error.fileId"), id, ex.fillInStackTrace());
		}
		if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
			logger.info(messageResourceBundle.getLogMessage("info.batch.success"));
		} else {
			throw new InternalServerException(
					messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR_INSIDE_BATCH_PROCESS));

		}

		return ResponseEntity.ok(target);
	}

	private List<BulkContentEntry> listContent(int id) {

		String queryString = "SELECT id, destination, content, flag FROM batch_content_" + id;
		Query nativeQuery = entityManager.createNativeQuery(queryString);

		List<Object[]> resultList = nativeQuery.getResultList();

		List<BulkContentEntry> bulkContentEntries = new ArrayList<>();

		for (Object[] result : resultList) {
			BulkContentEntry bulkContentEntry = new BulkContentEntry();
			bulkContentEntry.setId((int) result[0]);
			bulkContentEntry.setDestination((long) result[1]);
			bulkContentEntry.setContent((String) result[2]);
			bulkContentEntry.setFlag((String) String.valueOf(result[3]));

			bulkContentEntries.add(bulkContentEntry);
		}
		return bulkContentEntries;

	}

	private void UpdateContent(List<BulkContentEntry> bulkMgmtContentList, String tableName) {
		for (BulkContentEntry content : bulkMgmtContentList) {
			jdbcTemplate.update("CALL UpdateDataIntoTable(?, ?,?,?, ?)", content.getDestination(), content.getContent(),
					content.getFlag(), content.getId(), tableName);
		}
	}

	@Override
	public ResponseEntity<?> listBulk(String username) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		Optional<WebMenuAccessEntry> webOptional = webMenuAccessEntryRepository.findById(user.getId());
		if (!webOptional.isPresent()) {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.WEB_MENU_ACCESS_NOT_FOUND) + username);
		}
		List<BulkEntry> reportList = null;
		String target = null;
		if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem") || webOptional.get().isUtility()) {
			Collection<BatchObject> list = null;
			if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				list = GlobalVars.BatchQueue.values();
			} else {
				Predicate<Integer, BatchObject> predicate = Predicates.equal("systemId", user.getSystemId());
				list = GlobalVars.BatchQueue.values(predicate);
			}
			// Collection statusList = dbService.getUploadedStatusbulk(clientId, role, opt);
			if (list.isEmpty()) {
				target = IConstants.FAILURE_KEY;
				logger.error(messageResourceBundle.getLogMessage("error.failedToRetrieveBatchList"), username, user.getRole());
				throw new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.NO_BATCHES_AVAILABLE_FOR_PROCESSING));

			} else {
				System.out.println(list);
				reportList = new ArrayList<BulkEntry>();
				BulkEntry entry = null;
				long count = 0;
				for (BatchObject batch : list) {
					entry = bulkEntryRepository.findById(batch.getId()).get();
					count = rowCount(batch.getId());
					long processed = entry.getTotal() - count;
					entry.setProcessed(processed);
					entry.setActive(batch.isActive());
					reportList.add(entry);
				}
				target = IConstants.SUCCESS_KEY;

			}
		} else {
			logger.error(messageResourceBundle.getLogMessage("error.invalidRequest"), username, user.getRole());
			target = "invalidRequest";
			throw new InternalServerException(username + "[" + user.getRole() + "]" + " <- Invalid Request ->");
		}
		return ResponseEntity.ok(reportList);
	}

	private long rowCount(int id) {
		String queryString = "SELECT COUNT(*) FROM batch_content_" + id;
		Query nativeQuery = entityManager.createNativeQuery(queryString);

		// Execute the query and retrieve the result
		Object result = nativeQuery.getSingleResult();

		long rowCount;

		if (result instanceof BigInteger) {
			rowCount = ((BigInteger) result).longValue();
		} else if (result instanceof Long) {
			rowCount = (Long) result;
		} else {
			// Handle other cases or throw an exception if needed
			throw new InternalServerException("Unexpected result type: " + result.getClass().getName());
		}

		return rowCount;
	}

	@Override
	public ResponseEntity<?> listSchedule(String username) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		Optional<WebMenuAccessEntry> webOptional = webMenuAccessEntryRepository.findById(user.getId());
		if (!webOptional.isPresent()) {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.WEB_MENU_ACCESS_NOT_FOUND) + username);
		}
		List<ScheduleEntryExt> scheduleList = null;
		String target = IConstants.FAILURE_KEY;
		logger.info(messageResourceBundle.getLogMessage("info.scheduleListRequest"), username, user.getRole());
		if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem") || webOptional.get().isUtility()) {
			scheduleList = new ArrayList<ScheduleEntryExt>();
			try {
				ScheduleEntry entry = null;
				BulkSmsDTO bulkSmsDTO = null;
				String filename = "";
				List<ScheduleEntry> tempList = getScheduleList(user.getSystemId(), user.getRole());
				Iterator<ScheduleEntry> iterator = tempList.iterator();
				while (iterator.hasNext()) {
					entry = iterator.next();
					filename = IConstants.WEBSMPP_EXT_DIR + "schedule//" + entry.getFileName().trim();
					// tempPath = filePath + filename;
					System.out.println(entry.getUsername() + "[" + entry.getId() + "] Scheduled File: " + filename);
					ObjectInputStream objectInputStream = null;
					try {
						objectInputStream = new ObjectInputStream(new FileInputStream(filename));
						bulkSmsDTO = (BulkSmsDTO) objectInputStream.readObject();
						ScheduleEntryExt ext = new ScheduleEntryExt(entry);
						ext.setMessageType(bulkSmsDTO.getMessageType());
						ext.setTotalNumbers(bulkSmsDTO.getDestinationList().size());
						ext.setSenderId(bulkSmsDTO.getSenderId());
						ext.setCustomContent(bulkSmsDTO.isCustomContent());
						ext.setCampaign(bulkSmsDTO.getCampaignName());
						ext.setFirstNum(bulkSmsDTO.getDestinationList().get(0));
						ext.setDate(entry.getDate());
						scheduleList.add(ext);
					} catch (FileNotFoundException fnfe) {
						logger.error(messageResourceBundle.getLogMessage("error.fileAccess"), filename, fnfe);
					} finally {
						if (objectInputStream != null) {
							try {
								objectInputStream.close();
							} catch (IOException ioe) {
							}
						}
					}
				}
				if (scheduleList.size() > 0) {
					target = IConstants.SUCCESS_KEY;

				} else {
					throw new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_FILE_EXCEPTION)
									+ filename);
				}
			} catch (NotFoundException ex) {
				logger.error(messageResourceBundle.getLogMessage("error.failedToRetrieveBatchList"), username, user.getRole(), ex.getCause());
				throw new NotFoundException(username + "[" + user.getRole() + "] " + ex.getLocalizedMessage());
			} catch (Exception ex) {
				logger.error(messageResourceBundle.getLogMessage("error.failedToRetrieveBatchList"), username, user.getRole(), ex.getCause());
				throw new InternalServerException(username + "[" + user.getRole() + "] " + ex.getLocalizedMessage());
			}
			logger.info(messageResourceBundle.getLogMessage("info.scheduleList"), scheduleList);
		} else {
			logger.error(messageResourceBundle.getLogMessage("error.invalidRequest"), user.getSystemId() + "[" + user.getRole() + "]");
			target = "invalidRequest";
			throw new InternalServerException(
					user.getSystemId() + "[" + user.getRole() + "]" + " <- Invalid Request ->");
		}

		return ResponseEntity.ok(scheduleList);
	}

	private List<ScheduleEntry> getScheduleList(String systemId, String role) {
		if (Access.isAuthorized(role, "isAuthorizedSuperAdminAndSystem")) {
			return scheduleEntryRepository.findAll();
		} else {
			return scheduleEntryRepository.findByUsername(systemId);
		}
	}

	@Override
	public ResponseEntity<?> abortSchedule(String username, int schedule_Id) {

		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		String master = user.getSystemId();
		String role = user.getRole();
		logger.info(messageResourceBundle.getLogMessage("info.scheduleAbortRequest"), schedule_Id);
		String target = IConstants.FAILURE_KEY;
		boolean deleted = false;
		File file = null;
		boolean proceed = true;

		if (!scheduleEntryRepository.existsById(schedule_Id)) {
			throw new ScheduledTimeException(
					messageResourceBundle.getExMessage(ConstantMessages.SCHEDULE_NOT_FOUND) + schedule_Id);
		}
		try {
			ScheduleEntry schedule = scheduleEntryRepository.findById(schedule_Id).get();
			if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
			} else {
				if (!schedule.getUsername().equalsIgnoreCase(master)) {
					target = "invalidRequest";
					proceed = false;
					logger.error(messageResourceBundle.getLogMessage("error.unauthorizedAbortSchedule"), master, role, schedule.getUsername());
				}
			}
			if (proceed) {
				String filename = schedule.getFileName();
				file = new File(IConstants.WEBSMPP_EXT_DIR + "schedule//" + filename);
				if (file.exists()) {
					System.out.println(" Deleting Scheduled File -> " + file.getName());
					if (file.delete()) {
						System.out.println(" Scheduled File Deleted -> " + file.getName());
						deleted = true;
					}
				} else {
					throw new ScheduledTimeException(
							messageResourceBundle.getExMessage(ConstantMessages.SCHEDULE_REMOVAL_FAILURE));

				}
				scheduleEntryRepository.deleteById(schedule_Id);
				String schedule_time = schedule.getServerTime();
				System.out.println("Schedule[" + schedule_Id + "] Time: " + schedule_time);
				if (GlobalVarsSms.ScheduledBatches.containsKey(schedule_time)) {
					Set<Integer> set = GlobalVarsSms.ScheduledBatches.get(schedule_time);
					System.out.println("Time: " + schedule_time + " Schedules: " + set);
					if (set.contains(schedule_Id)) {
						set.remove(schedule_Id);
					}
				}
				System.out.println("Today's Schedules:" + GlobalVarsSms.ScheduledBatches);
			}
		} catch (ScheduledTimeException ioex) {
			throw new ScheduledTimeException(ioex.getMessage());
		} catch (Exception ioex) {
			throw new InternalServerException(ioex.getMessage());
		}
		target = IConstants.SUCCESS_KEY;
		logger.info(messageResourceBundle.getLogMessage("info.scheduleAbort"), master, role, schedule_Id, target);
		return ResponseEntity.ok(target);
	}

	@Override
	public ResponseEntity<?> editSchedule(String username, int schedule_Id) {

		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		if (!scheduleEntryRepository.existsById(schedule_Id)) {
			throw new ScheduledTimeException(
					messageResourceBundle.getExMessage(ConstantMessages.SCHEDULE_NOT_FOUND) + schedule_Id);
		}
		String master = user.getSystemId();
		String role = user.getRole();
		logger.info(messageResourceBundle.getLogMessage("info.scheduleEditRequest"), master, role, schedule_Id);
		String target = IConstants.FAILURE_KEY;
		String destination_no = "";
		String gmtValue = "";
		String clientId = null;
		String filename = null;
		ScheduleEditResponse scheduleEditResponse = new ScheduleEditResponse();
		try {
			ScheduleEntry schedule = scheduleEntryRepository.findById(schedule_Id).get();
			if (schedule != null) {
				clientId = schedule.getUsername();
				filename = schedule.getFileName();
				if (clientId.equalsIgnoreCase(master)) {
					BulkSmsDTO upload = null;
					boolean proceed = false;
					File sch_file = new File(IConstants.WEBSMPP_EXT_DIR + "schedule//" + filename);
					// System.out.println("Edit File Exist: " + sch_file.exists());
					if (sch_file.exists()) {
						logger.info(messageResourceBundle.getLogMessage("info.scheduleFileFound"), clientId, schedule_Id, filename);
						ObjectInputStream ois = null;
						try {
							ois = new ObjectInputStream(new FileInputStream(sch_file));
							upload = (BulkSmsDTO) ois.readObject();
							proceed = true;
						} catch (Exception ex) {
							logger.error(messageResourceBundle.getLogMessage("error.fileAccessError"), filename, ex);
						} finally {
							if (ois != null) {
								try {
									ois.close();
								} catch (IOException ioe) {
									System.out.println("Error: Unable To Close File: " + filename);
								}
							}
						}
					} else {
						logger.error(messageResourceBundle.getLogMessage("error.scheduleFileNotFound"), filename);
					}
					if (proceed) {
						// BulkListInfo listInfo = new BulkListInfo();
						List<String> list = upload.getDestinationList();
						long listSize = (long) list.size();
						String listSizeStr = Long.toString(listSize);
						List<String> templist = new ArrayList<String>();
						for (int i = 0; i < list.size(); i++) {
							destination_no = (String) list.get(i);
							templist.add(destination_no);
						}
						System.out.println("Org: " + upload.getOrigMessage());
						clientId = upload.getUsername();
						String msg = null;
						if (upload.getMessageType().equalsIgnoreCase("SpecialChar")) {
							msg = SmsConverter.hexCodePointsToCharMsg(upload.getOrigMessage());
						} else {
							String unicodemsg = SmsConverter.getUTF8toHex(upload.getMessage());
							msg = SmsConverter.hexCodePointsToCharMsg(unicodemsg);
						}
						if (upload.getReqType().equalsIgnoreCase("contact")) {
							List<GroupEntryDTO> groupList = groupEntryDTORepository.findByMasterId(clientId);
							scheduleEditResponse.setGroupList(groupList);
						}
						String gmt = upload.getGmt();
						gmtValue = GMTmapping.getGMT(gmt);
						scheduleEditResponse.setListSizeStr(listSizeStr);
						scheduleEditResponse.setTempList(templist);
						scheduleEditResponse.setGmt(gmt);
						scheduleEditResponse.setGmtValue(gmtValue);
						scheduleEditResponse.setSchaduleTime(schedule.getClientTime());
						scheduleEditResponse.setMsg(msg);
						scheduleEditResponse.setSenderId(upload.getSenderId());
						scheduleEditResponse.setReqType(upload.getReqType());
						scheduleEditResponse.setFilename(filename);
						scheduleEditResponse.setDelay(upload.getDelay() + "");
						scheduleEditResponse.setRepeat(upload.getRepeat());
						scheduleEditResponse.setUsername(upload.getSystemId());
						scheduleEditResponse.setExpiry(upload.getExpiryHour() + "");

						target = IConstants.SUCCESS_KEY;
					} else {
						logger.error(messageResourceBundle.getLogMessage("error.fileNotFound"), filename = "your_file_name.txt");
						throw new NotFoundException("File not found: {filename}" + filename + "=.txt");
					}
				} else {
					target = "invalidRequest";
					logger.info(messageResourceBundle.getLogMessage("info.mismatchedSchedule"), schedule_Id, clientId);
					throw new InternalServerException(master + " mismatched Schedule[" + schedule_Id + "]" + clientId);

				}
			} else {
				logger.error(messageResourceBundle.getLogMessage("error.fileNotFound"), filename = "your_file_name.txt");
				throw new NotFoundException("File not found: {filename}" + filename + "=.txt");
			}
		} catch (NotFoundException ex) {
			logger.error(messageResourceBundle.getLogMessage("error.fileOperation"), clientId + "[" + filename + "]");
			throw new NotFoundException(ex.getMessage());
		} catch (Exception ex) {
			logger.error(messageResourceBundle.getLogMessage("error.fileOperation"), clientId + "[" + filename + "]");
			throw new InternalServerException(ex.getMessage());
		}
		return ResponseEntity.ok(scheduleEditResponse);
	}

	@Override
	public ResponseEntity<?> sendNowSchedule(String username, SendBulkScheduleRequest sendBulkScheduleRequest,
			MultipartFile destinationNumberFile) {
		BulkResponse bulkResponse = new BulkResponse();
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user.getId());
		SendSmsService service = new SendSmsService();
		String uploadedNumbers = "";
		long count = 0;
		double totalcost = 0;
		String contactList = "";
		boolean contactListExist = true;
		boolean numListExist = true;
		String target = IConstants.FAILURE_KEY;
		BulkListInfo listInfo = new BulkListInfo();
		double wallet = 0;
		BulkSmsDTO bulkSmsDTO = new BulkSmsDTO();
		bulkSmsDTO.setReqType(sendBulkScheduleRequest.getReqType());
		bulkSmsDTO.setReCheck(sendBulkScheduleRequest.getReCheck());
		bulkSmsDTO.setUploadedNumbers(sendBulkScheduleRequest.getUploadedNumbers());
		bulkSmsDTO.setTotalNumbers(sendBulkScheduleRequest.getTotalNumbers());
		bulkSmsDTO.setFileName(sendBulkScheduleRequest.getFileName());
		bulkSmsDTO.setAlert(sendBulkScheduleRequest.isAlert());
		bulkSmsDTO.setCharCount(sendBulkScheduleRequest.getCharCount());
		bulkSmsDTO.setSmscount(sendBulkScheduleRequest.getSmscount());
		bulkSmsDTO.setUsername(username);
		bulkSmsDTO.setUploadedNumbers(sendBulkScheduleRequest.getUploadedNumbers());
		bulkSmsDTO.setSmsParts(sendBulkScheduleRequest.getSmsParts());
		bulkSmsDTO.setSenderId(sendBulkScheduleRequest.getSenderId());
		bulkSmsDTO.setCharLimit(sendBulkScheduleRequest.getCharLimit());
		bulkSmsDTO.setExpiryHour(sendBulkScheduleRequest.getExpiryHour());
		bulkSmsDTO.setDelay(sendBulkScheduleRequest.getDelay());
		bulkSmsDTO.setFrom(sendBulkScheduleRequest.getFrom());
		if (sendBulkScheduleRequest.getDestinationNumber() != null)
			bulkSmsDTO.setDestinationNumber(sendBulkScheduleRequest.getDestinationNumber());

		if (sendBulkScheduleRequest.getPeId() != null)
			bulkSmsDTO.setPeId(sendBulkScheduleRequest.getPeId());
		if (sendBulkScheduleRequest.getTelemarketerId() != null)
			bulkSmsDTO.setTelemarketerId(sendBulkScheduleRequest.getTelemarketerId());
		if (sendBulkScheduleRequest.getTemplateId() != null)
			bulkSmsDTO.setTemplateId(sendBulkScheduleRequest.getTemplateId());
		String reCheck = bulkSmsDTO.getReCheck();
		boolean formFileExist = false;
		if ((bulkSmsDTO.getReqType().equalsIgnoreCase("bulk"))) {
			if (reCheck.equalsIgnoreCase("yes")) {
				try {
					if ((destinationNumberFile) != null) {
						formFileExist = true;
					}
				} catch (Exception ex) {
					formFileExist = false;
					System.out.println("Exception in FormFile ::" + ex);
				}
			}
		}
		if ((bulkSmsDTO.getReqType().equalsIgnoreCase("mobiledb"))) {
			formFileExist = false;
//			if (reCheck.equalsIgnoreCase("yes")) {
//				String session_Query = (String) session.getAttribute("session_query");
//				session.removeAttribute("session_query");
//				if (session_Query == null) {
//					numListExist = false;
//				} else {
//					boolean b_count = false;
//					ArrayList newList = new MobileDBServices().getMobileRecords(session_Query, b_count);
//					if (newList.size() > 0) {
//						Iterator iterator = newList.iterator();
//						while (iterator.hasNext()) {
//							uploadedNumbers += (String) iterator.next() + "\n";
//							count++;
//						}
//						bulkSmsDTO.setUploadedNumbers(uploadedNumbers);
//						bulkSmsDTO.setTotalNumbers(count + "");
//					} else {
//						numListExist = false;
//					}
//				}
//			}
		}
		if (bulkSmsDTO.getReqType().equalsIgnoreCase("contact")) {
			formFileExist = false;
			contactList = bulkSmsDTO.getUploadedNumbers();
			if (contactList == null || contactList.equalsIgnoreCase("")) {
				contactListExist = false;
			}
		}
		if (numListExist && contactListExist) {
			DriverInfo driverInfo = null;
			Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(user.getId());
			if (OptionalDriverInfo.isPresent()) {
				driverInfo = OptionalDriverInfo.get();
			} else
				throw new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.DRIVE_INFO_NOT_FOUND_EXCEPTION));
			String systemId = user.getSystemId();
			bulkSmsDTO.setClientId(systemId);
			bulkSmsDTO.setSystemId(systemId);
			bulkSmsDTO.setPassword(new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
			String bulkSessionId = systemId + Long.toString(System.currentTimeMillis());
			// String appPath = request.getPathTranslated();
			boolean update = false;
			String reqFileName = bulkSmsDTO.getFileName();
			if (reqFileName != null && reqFileName.length() > 0) {
				update = true;
				reqFileName = reqFileName.trim();
			}
			try {
				Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findBySystemId(user.getSystemId());
				Optional<BalanceEntry> masterbalanceOptional = balanceEntryRepository
						.findBySystemId(user.getMasterId());
				String wallet_flag = null;
				BalanceEntry balanceEntry = null;
				BalanceEntry masterbalanceEntry = null;
				double adminWallet = 0;
				if (balanceOptional.isPresent()) {
					balanceEntry = balanceOptional.get();
					wallet_flag = balanceEntry.getWalletFlag();
					wallet = balanceEntry.getWalletAmount();
				}
				if (masterbalanceOptional.isPresent()) {
					masterbalanceEntry = masterbalanceOptional.get();
					adminWallet = masterbalanceEntry.getWalletAmount();
				}
				int no_of_msg = bulkSmsDTO.getSmsParts();

				if (sendBulkScheduleRequest.getMessageType().equalsIgnoreCase("Unicode")) {
					bulkSmsDTO.setMessage(UTF16(sendBulkScheduleRequest.getMessage()));
					bulkSmsDTO.setOrigMessage(UTF16(sendBulkScheduleRequest.getMessage()));
					bulkSmsDTO.setDistinct("yes");
				} else {
					String sp_msg = sendBulkScheduleRequest.getMessage();
					String hexValue = getHexValue(sp_msg);
					String unicodeMsg = SmsConverter.getContent(hexValue.toCharArray());
					bulkSmsDTO.setMessage(unicodeMsg);
					bulkSmsDTO.setMessageType("SpecialChar");
					bulkSmsDTO.setOrigMessage(UTF16(sendBulkScheduleRequest.getMessage()));
				}

				System.out.println("message " + bulkSmsDTO.getMessage());
				System.out.println(" No of Parts ::::::::" + no_of_msg);
				List<String> destinationList = null;
				if (formFileExist) {
					destinationList = getDestinationList(bulkSmsDTO, listInfo, destinationNumberFile);
				} else {
					destinationList = bulkSmsDTO.getDestinationList2(listInfo);
				}
				if (wallet_flag.equalsIgnoreCase("yes")) {
					bulkSmsDTO.setUserMode("wallet");
					totalcost = routeService.calculateRoutingCost(user.getId(), destinationList, no_of_msg);
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
							// ****************** Delete Schedule File ***********
							ScheduleEntry entry = scheduleEntryRepository.findByFileName(reqFileName);// dbService.getScheduleInfo(reqFileName);
							if (update) {
								scheduleEntryRepository.deleteById(entry.getId());
								if (!scheduleEntryRepository.existsById(entry.getId())) {
									System.out.println("Deleted File :" + reqFileName + " From DB");
								}
								String schedPath = IConstants.WEBSMPP_EXT_DIR + "schedule//";
								String path = schedPath + reqFileName;
								File delFile = new File(path);
								try {
									if (delFile.exists()) {
										boolean removed = delFile.delete();
										if (removed) {
											System.out.println("Deleted File :" + reqFileName + " From DIRECTORY");
										}
									}
								} catch (Exception ex) {
									logger.error(messageResourceBundle.getLogMessage("error.deleteScheduleFileException"), path);
									throw new InternalServerException(messageResourceBundle
											.getExMessage(ConstantMessages.DELETE_SCHEDULE_FILE_EXCEPTION) + path);
								}
								String schedule_time = entry.getServerTime();
								System.out.println("Schedule[" + entry.getId() + "] Time: " + schedule_time);
								if (GlobalVarsSms.ScheduledBatches.containsKey(schedule_time)) {
									Set<Integer> set = GlobalVarsSms.ScheduledBatches.get(schedule_time);
									System.out.println("Time: " + schedule_time + " Schedules: " + set);
									if (set.contains(entry.getId())) {
										set.remove(entry.getId());
									}
								}
							}
							// ********************* Delete Schedule File ***********
							if (inherit) {
								adminWallet = adminWallet - totalcost;

							} else {
								wallet = wallet - totalcost;

							}
							// String applicationName = request.getContextPath();
							bulkSmsDTO.setMsgCount(destinationList.size() * no_of_msg);
							bulkSmsDTO.setTotalCost(totalcost);
							String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								logger.info(messageResourceBundle.getLogMessage("info.batchSubmissionSuccess"));
							} else {
								// Batch Submission Error
								logger.error(messageResourceBundle.getLogMessage("error.batchSubmissionError"));
								throw new InternalServerException(messageResourceBundle
										.getExMessage(ConstantMessages.BATCH_SUBMISSION_ERROR_EXCEPTION));
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setBulkSessionId(bulkSessionId);
								bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
								bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
							}
						} else {
							// Insufficient Balance Error
							logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"));
							throw new InsufficientBalanceException(messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));
						}
					} else {
						// Invalid Number File Error
						logger.error(messageResourceBundle.getLogMessage("error.invalidNumberFile"));
						throw new InternalServerException(
								messageResourceBundle.getExMessage(ConstantMessages.INVALID_FILE_FORMAT_EXCEPTION));
					}
				} else if (wallet_flag.equalsIgnoreCase("no")) {
					bulkSmsDTO.setUserMode("credit");
					if (destinationList.size() > 0) {
						long credits = balanceEntry.getCredits();
						long adminCredit = masterbalanceEntry.getCredits();
						boolean amount = false;
						boolean inherit = false;
						if (user.isAdminDepend() && (adminCredit > (destinationList.size() * no_of_msg))) {
							amount = true;
							inherit = true;
						} else if (credits > (destinationList.size() * no_of_msg)) {
							amount = true;
						}
						if (amount) {
							// ****************** Delete Schedule File ***********

							ScheduleEntry entry = scheduleEntryRepository.findByFileName(reqFileName);
							if (update) {
								scheduleEntryRepository.deleteById(entry.getId());// (entry.getId());
								if (!scheduleEntryRepository.existsById(entry.getId())) {
									System.out.println("Deleted File :" + reqFileName + " From DB");
								}
								String schedPath = IConstants.WEBSMPP_EXT_DIR + "schedule//";
								String path = schedPath + reqFileName;
								File delFile = new File(path);
								try {
									if (delFile.exists()) {
										boolean removed = delFile.delete();
										if (removed) {
											System.out.println("Deleted File :" + reqFileName + " From DIRECTORY");
										}
									}
								} catch (Exception ex) {
									logger.error(messageResourceBundle.getLogMessage("error.deleteScheduleFileException"), path);
									throw new InternalServerException(messageResourceBundle
											.getExMessage(ConstantMessages.DELETE_SCHEDULE_FILE_EXCEPTION) + path);
								}
								String schedule_time = entry.getServerTime().split(" ")[1];
								System.out.println(
										"Schedule[" + sendBulkScheduleRequest.getId() + "] Time: " + schedule_time);
								if (GlobalVarsSms.ScheduledBatches.containsKey(schedule_time)) {
									Set<Integer> set = GlobalVarsSms.ScheduledBatches.get(schedule_time);
									System.out.println("Time: " + schedule_time + " Schedules: " + set);
									if (set.contains(sendBulkScheduleRequest.getId())) {
										set.remove(sendBulkScheduleRequest.getId());
									}
								}
							}
							// *********************End Delete Schedule File ***********
							if (inherit) {
								adminCredit = adminCredit - (destinationList.size() * no_of_msg);
							} else {
								credits = credits - (destinationList.size() * no_of_msg);

							}
							long deductCredits = destinationList.size() * no_of_msg;
							bulkSmsDTO.setMsgCount(deductCredits);
							String value = sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove(), user.getId());
							if (!value.contains("Error")) {
								target = IConstants.SUCCESS_KEY;
								// Batch Submission Success
								logger.info(messageResourceBundle.getLogMessage("info.batchSubmissionSuccess"));
							} else {
								// Batch Submission Error
								logger.error(messageResourceBundle.getLogMessage("error.batchSubmissionError"));
								throw new InternalServerException(messageResourceBundle
										.getExMessage(ConstantMessages.BATCH_SUBMISSION_ERROR_EXCEPTION));
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setBulkSessionId(bulkSessionId);
								bulkResponse.setCredits(Long.toString(credits));
								bulkResponse.setDeductcredits(deductCredits + "");
							}
						} else {
							// Insufficient Credits Error
							logger.error(
									"Error: Insufficient credits to process the transaction. Error code: error.insufficientCredit");
							throw new InsufficientBalanceException(messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION));
						}
					} else {
						// Invalid Number File Error
						logger.error(messageResourceBundle.getLogMessage("error.invalidNumberFile"));
						throw new InternalServerException(
								messageResourceBundle.getExMessage(ConstantMessages.INVALID_FILE_FORMAT_EXCEPTION));
					}
				} else if (wallet_flag.equalsIgnoreCase("MIN")) {
					// Insufficient Wallet Balance Error
					logger.error(messageResourceBundle.getLogMessage("error.insufficientWallet"));
					throw new InsufficientBalanceException(
							messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));
				}
			} catch (NotFoundException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new NotFoundException(e.getMessage());
			} catch (InsufficientBalanceException e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id"), bulkSessionId, e.getMessage());
				throw new InsufficientBalanceException(e.getMessage());
			} catch (Exception e) {
				logger.error(messageResourceBundle.getLogMessage("error.with.session.id.and.exception"), bulkSessionId, e);
				throw new InternalServerException(e.getMessage());
			}
		} else {
			target = IConstants.FAILURE_KEY;
			// Invalid Number File Error
			logger.error(messageResourceBundle.getLogMessage("error.novalidNumber"));
			throw new InternalServerException(
					messageResourceBundle.getExMessage(ConstantMessages.INVALID_FILE_FORMAT_EXCEPTION));
		}
		bulkResponse.setStatus(target);
		return ResponseEntity.ok(bulkResponse);
	}

	public List<String> getDestinationList(BulkSmsDTO bulkSmsDTO, BulkListInfo listInfo, MultipartFile file)
			throws FileNotFoundException, IOException {
		List<String> destinationList = bulkSmsDTO.getDestinationList();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream stream = file.getInputStream();
		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
			baos.write(buffer, 0, bytesRead);
		}
		String data = new String(baos.toByteArray());
		List<String> noList = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(data, "\n");
		int tot = st.countTokens();
		listInfo.setTotal(st.countTokens());
		int invalidCount = 0;
		while (st.hasMoreTokens()) {
			String destinationNumber = (String) st.nextToken();
			destinationNumber = destinationNumber.trim();
			if ((destinationNumber != null) && (destinationNumber.contains(";"))) {
				if (bulkSmsDTO.getReqType().equalsIgnoreCase("groupDataBulk")) {
					String number = destinationNumber.substring(0, destinationNumber.indexOf(";"));
					if (number != null && number.startsWith("+")) {
						number = number.substring(1, number.length());
					}
					try {
						long num = Long.parseLong(number);
						number = String.valueOf(num);
						noList.add(number);
					} catch (NumberFormatException ne) {
						System.out.println("INVALID DESTINATION NUMBER(1): " + number);
						invalidCount++;
					}
				} else {
					String name = destinationNumber.substring(0, destinationNumber.indexOf(";"));
					String number = destinationNumber.substring(destinationNumber.indexOf(";") + 1,
							destinationNumber.length());
					if (number != null && number.startsWith("+")) {
						number = number.substring(1, number.length());
					}
					try {
						long num = Long.parseLong(number);
						destinationNumber = name + ";" + String.valueOf(num);
						noList.add(destinationNumber);
					} catch (NumberFormatException ne) {
						System.out.println("INVALID DESTINATION NUMBER(2): " + number);
						invalidCount++;
					}
				}
			} else if (destinationNumber == null) {
				invalidCount++;
			} else if (destinationNumber.equalsIgnoreCase("")) {
				invalidCount++;
			} else if (destinationNumber.length() == 0) {
				invalidCount++;
			} else {
				try {
					long num = Long.parseLong(destinationNumber);
					noList.add(String.valueOf(num));
				} catch (NumberFormatException nfe) {
					System.out.println("INVALID DESTINATION NUMBER(3): " + destinationNumber);
					invalidCount++;
				}
			} // else closed
		}
		Set<String> hashSet = new HashSet<String>(noList);
		destinationList = new ArrayList<String>(hashSet);
		Collections.sort(destinationList);
		// --------------------------------------------------------
		listInfo.setValidCount(destinationList.size());
		listInfo.setInvalidCount(invalidCount);
		int dup = tot - destinationList.size() - invalidCount;
		// System.out.println("duplicate===="+dup);
		listInfo.setDuplicate(dup);
		bulkSmsDTO.setDestinationList(destinationList);
		return destinationList;
	}

	@Override
	public ResponseEntity<?> modifiedSchedule(String username, SendBulkScheduleRequest sendBulkScheduleRequest,
			MultipartFile destinationNumberFile) {
		BulkResponse bulkResponse = new BulkResponse();
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findBySystemId(user.getSystemId());
		Optional<BalanceEntry> masterbalanceOptional = balanceEntryRepository.findBySystemId(user.getMasterId());
		if (balanceOptional.isEmpty()) {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.BALANCE_INFO_NOT_FOUND_EXCEPTION)
							+ user.getSystemId());
		}
		if (masterbalanceOptional.isEmpty()) {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.BALANCE_INFO_NOT_FOUND_EXCEPTION)
							+ user.getMasterId());
		}
		BalanceEntry balanceEntry = balanceOptional.get();
		BalanceEntry masterbalanceEntry = masterbalanceOptional.get();
		DriverInfo driverInfo = null;
		Optional<DriverInfo> OptionalDriverInfo = driverInfoRepository.findById(user.getId());
		if (OptionalDriverInfo.isPresent()) {
			driverInfo = OptionalDriverInfo.get();
		} else
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.DRIVE_INFO_NOT_FOUND_EXCEPTION));
		String target = IConstants.FAILURE_KEY;
		SendSmsService service = new SendSmsService();
		BulkListInfo listInfo = new BulkListInfo();
		String uploadedNumbers = "";
		long count = 0;
		double totalcost = 0;
		String contactList = "";
		boolean contactListExist = true;
		boolean numListExist = true;
		BulkSmsDTO smsDTO = new BulkSmsDTO();
		smsDTO.setReqType(sendBulkScheduleRequest.getReqType());
		smsDTO.setReCheck(sendBulkScheduleRequest.getReCheck());
		smsDTO.setUploadedNumbers(sendBulkScheduleRequest.getUploadedNumbers());
		smsDTO.setTotalNumbers(sendBulkScheduleRequest.getTotalNumbers());
		smsDTO.setFileName(sendBulkScheduleRequest.getFileName());
		smsDTO.setAlert(sendBulkScheduleRequest.isAlert());
		smsDTO.setCharCount(sendBulkScheduleRequest.getCharCount());
		smsDTO.setSmscount(sendBulkScheduleRequest.getSmscount());
		smsDTO.setUsername(username);
		smsDTO.setUploadedNumbers(sendBulkScheduleRequest.getUploadedNumbers());
		smsDTO.setSmsParts(sendBulkScheduleRequest.getSmsParts());
		smsDTO.setSenderId(sendBulkScheduleRequest.getSenderId());
		smsDTO.setCharLimit(sendBulkScheduleRequest.getCharLimit());
		smsDTO.setExpiryHour(sendBulkScheduleRequest.getExpiryHour());
		smsDTO.setGmt(sendBulkScheduleRequest.getGmt());
		smsDTO.setDelay(sendBulkScheduleRequest.getDelay());
		smsDTO.setMessageType(sendBulkScheduleRequest.getMessageType());
		smsDTO.setFrom(sendBulkScheduleRequest.getFrom());
		smsDTO.setTimestart(sendBulkScheduleRequest.getSchedTime());
		smsDTO.setReCheck(sendBulkScheduleRequest.getReCheck());
		smsDTO.setRepeat(sendBulkScheduleRequest.getRepeat());
		if (sendBulkScheduleRequest.getDestinationNumber() != null)
			smsDTO.setDestinationNumber(sendBulkScheduleRequest.getDestinationNumber());

		if (sendBulkScheduleRequest.getPeId() != null)
			smsDTO.setPeId(sendBulkScheduleRequest.getPeId());
		if (sendBulkScheduleRequest.getTelemarketerId() != null)
			smsDTO.setTelemarketerId(sendBulkScheduleRequest.getTelemarketerId());
		if (sendBulkScheduleRequest.getTemplateId() != null)
			smsDTO.setTemplateId(sendBulkScheduleRequest.getTemplateId());
		// String sheduleType = smsDTO.getSchType();
		String reCheck = smsDTO.getReCheck();
		boolean formFileExist = false;
		if ((smsDTO.getReqType().equalsIgnoreCase("bulk"))) {
			if (reCheck.equalsIgnoreCase("yes")) {
				try {
					if ((destinationNumberFile) != null) {
						formFileExist = true;
					}
				} catch (Exception ex) {
					formFileExist = false;
					System.out.println("Exception in FormFile ::" + ex);
				}
			}
		}
		if ((smsDTO.getReqType().equalsIgnoreCase("mobiledb"))) {
//			if (reCheck.equalsIgnoreCase("yes")) {
//				String session_Query = (String) session.getAttribute("session_query");
//				session.removeAttribute("session_query");
//				if (session_Query == null) {
//					numListExist = false;
//				} else {
//					ArrayList newList = new MobileDBServices().getMobileRecords(session_Query, false);
//					if (newList.size() > 0) {
//						Iterator iterator = newList.iterator();
//						while (iterator.hasNext()) {
//							uploadedNumbers += (String) iterator.next() + "\n";
//							count++;
//						}
//						smsDTO.setUploadedNumbers(uploadedNumbers);
//						smsDTO.setTotalNumbers(count + "");
//					} else {
//						numListExist = false;
//					}
//				}
//			}
		}
		if (smsDTO.getReqType().equalsIgnoreCase("contact")) {
			formFileExist = false;
			contactList = smsDTO.getUploadedNumbers();
			if (contactList == null || contactList.equalsIgnoreCase("")) {
				contactListExist = false;
			}
		}
		if (numListExist && contactListExist) {
			smsDTO.setSystemId(username);
			smsDTO.setPassword(new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
			String reqFileName = smsDTO.getFileName();
			boolean update = false;
			if (reqFileName != null && reqFileName.length() > 0) {
				update = true;
				reqFileName = reqFileName.trim();
			}
			String client_time = smsDTO.getTimestart();
			String client_gmt = smsDTO.getGmt();
			SimpleDateFormat client_formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			client_formatter.setTimeZone(TimeZone.getTimeZone(client_gmt));
			SimpleDateFormat local_formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			String schedule_time = null;
			try {
				schedule_time = local_formatter.format(client_formatter.parse(client_time));
				System.out.println(user.getSystemId() + " client_gmt: " + client_gmt + " client_time: " + client_time
						+ " server_time: " + schedule_time);
				String server_date = schedule_time.split(" ")[0];
				String server_time = schedule_time.split(" ")[1];
				smsDTO.setDate(
						server_date.split("-")[2] + "-" + server_date.split("-")[1] + "-" + server_date.split("-")[0]);
				smsDTO.setTime(server_time.split(":")[0] + "" + server_time.split(":")[1]);
			} catch (Exception e) {
				logger.error(messageResourceBundle.getLogMessage("error.exceptionOccurred"), e);

			}
			try {
				String userExparyDate = (user.getExpiry()).toString();
				String adminId = user.getMasterId();
				String wallet_flag = balanceEntry.getWalletFlag();
				smsDTO.setUserMode(wallet_flag);
				double wallet = balanceEntry.getWalletAmount();
				double adminWallet = masterbalanceEntry.getWalletAmount();
				int no_of_msg = smsDTO.getSmsParts();
				String unicodeMsg = "";
				if (sendBulkScheduleRequest.getMessageType().equalsIgnoreCase("Unicode")) {
					smsDTO.setMessage(UTF16(sendBulkScheduleRequest.getMessage()));
					smsDTO.setOrigMessage(UTF16(sendBulkScheduleRequest.getMessage()));
					smsDTO.setDistinct("yes");
				} else {
					String sp_msg = sendBulkScheduleRequest.getMessage();
					String hexValue = getHexValue(sp_msg);
					unicodeMsg = SmsConverter.getContent(hexValue.toCharArray());
					smsDTO.setMessage(unicodeMsg);
					smsDTO.setMessageType("SpecialChar");
					smsDTO.setOrigMessage(UTF16(sendBulkScheduleRequest.getMessage()));
				}
				System.out.println(" No of Parts ::::::::" + no_of_msg);
				List<String> destinationList = null;
				if (formFileExist) {
					destinationList = getDestinationList(smsDTO, listInfo, destinationNumberFile);
				} else {
					destinationList = smsDTO.getDestinationList2(listInfo);
				}
				if (wallet_flag.equalsIgnoreCase("yes")) {
					smsDTO.setUserMode("wallet");
					totalcost = routeService.calculateRoutingCost(user.getId(), destinationList, no_of_msg);
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
							// ****************** Delete Schedule File ***********
							ScheduleEntry entry = scheduleEntryRepository.findByFileName(reqFileName);
							if (update) {
								scheduleEntryRepository.deleteById(entry.getId());
								if (!scheduleEntryRepository.existsById(entry.getId())) {
									System.out.println("Deleted File :" + reqFileName + " From DB");
								}
								String schedPath = IConstants.WEBSMPP_EXT_DIR + "schedule//";
								String path = schedPath + reqFileName;
								File delFile = new File(path);
								System.out.println("File Exist => " + delFile.exists());
								try {
									System.out.println("File Deleted => " + Files.deleteIfExists(delFile.toPath()));
								} catch (Exception ex) {
									System.out.println("Unable To Delete File => " + path);
								}
								System.out.println("Before: " + GlobalVarsSms.ScheduledBatches);
								String pre_schedule_time = entry.getServerTime();
								System.out.println("Schedule[" + entry.getId() + "] Time: " + pre_schedule_time);
								if (GlobalVarsSms.ScheduledBatches.containsKey(pre_schedule_time)) {
									Set<Integer> set = GlobalVarsSms.ScheduledBatches.get(pre_schedule_time);
									System.out.println("Time: " + pre_schedule_time + " Schedules: " + set);
									if (set.contains(entry.getId())) {
										set.remove(entry.getId());
									}
								}
								System.out.println("After: " + GlobalVarsSms.ScheduledBatches);
							}
							// ********************* Delete Schedule File ***********
							if (inherit) {
								adminWallet = adminWallet - totalcost;

							} else {
								wallet = wallet - totalcost;
							}
							smsDTO.setMsgCount(destinationList.size() * no_of_msg);
							smsDTO.setTotalCost(totalcost);
							smsDTO.setTotalWalletCost(totalcost);
							String filename = service.createScheduleFile(smsDTO);
							int generated_id = 0;
							if (filename != null) {
								ScheduleEntry sch = new ScheduleEntry();
								sch.setClientGmt(smsDTO.getGmt());
								sch.setClientTime(smsDTO.getTimestart());
								sch.setFileName(filename);
								sch.setRepeated(smsDTO.getRepeat());
								sch.setScheduleType(smsDTO.getReqType());
								sch.setServerId(IConstants.SERVER_ID);
								sch.setServerTime(smsDTO.getTime());
								sch.setStatus("false");
								sch.setUsername(smsDTO.getSystemId());
								sch.setDate(smsDTO.getDate());
								sch.setWebId(entry.getWebId());
								generated_id = scheduleEntryRepository.save(sch).getId();
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
									target = IConstants.SUCCESS_KEY;
									logger.info(messageResourceBundle.getLogMessage("message.scheduleSuccess"));
								} else {
									// Scheduling Error
									logger.error(messageResourceBundle.getLogMessage("error.scheduleError"));
									throw new ScheduledTimeException(messageResourceBundle
											.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION));
								}
							} else {
								// already Scheduled
								logger.error(messageResourceBundle.getLogMessage("error.duplicateSchedule"));
								throw new ScheduledTimeException(messageResourceBundle
										.getExMessage(ConstantMessages.DUPLICATE_SCHEDULE_ERROR_EXCEPTION));
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(new DecimalFormat("0.00000").format(wallet));
								bulkResponse.setDeductcredits(new DecimalFormat("0.00000").format(totalcost));
							}
						} else {
							// insufficient balance
							logger.error("Error: " + messageResourceBundle.getLogMessage("error.insufficientWallet"));
							throw new InsufficientBalanceException(messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));

						}
					} else {
						// Number File Error
						logger.error("Error: " + messageResourceBundle.getLogMessage("error.invalidNumber"));
						throw new InternalServerException(
								messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_EXCEPTION));
					}
				} else if (wallet_flag.equalsIgnoreCase("no")) {
					smsDTO.setUserMode("credit");
					if (destinationList.size() > 0) {
						long credits = balanceEntry.getCredits();
						long adminCredit = masterbalanceEntry.getCredits();
						boolean amount = false;
						boolean inherit = false;
						if (user.isAdminDepend() && (adminCredit > (destinationList.size() * no_of_msg))) {
							amount = true;
							inherit = true;
						} else if (credits > (destinationList.size() * no_of_msg)) {
							amount = true;
						}
						if (amount) {

							ScheduleEntry entry = scheduleEntryRepository.findByFileName(reqFileName);
							if (update) {
								scheduleEntryRepository.deleteById(entry.getId());
								if (!scheduleEntryRepository.existsById(entry.getId())) {
									System.out.println("Deleted File :" + reqFileName + " From DB");
								}
								String schedPath = IConstants.WEBSMPP_EXT_DIR + "schedule//";
								String path = schedPath + reqFileName;
								File delFile = new File(path);
								try {
									if (delFile.exists()) {
										boolean removed = delFile.delete();
										if (removed) {
											System.out.println("Deleted File :" + reqFileName + " From DIRECTORY");
										}
									}
								} catch (Exception ex) {
									System.out.println("Exception in Delete Schedule File :" + path);
									ex.printStackTrace();
								}
								System.out.println("Before: " + GlobalVarsSms.ScheduledBatches);
								String pre_schedule_time = entry.getServerTime().split(" ")[1];
								System.out.println("Schedule[" + entry.getId() + "] Time: " + pre_schedule_time);
								if (GlobalVarsSms.ScheduledBatches.containsKey(pre_schedule_time)) {
									Set<Integer> set = GlobalVarsSms.ScheduledBatches.get(pre_schedule_time);
									System.out.println("Time: " + pre_schedule_time + " Schedules: " + set);
									if (set.contains(entry.getId())) {
										set.remove(entry.getId());
									}
								}
								System.out.println("After: " + GlobalVarsSms.ScheduledBatches);
							}
							// *********************End Delete Schedule File ***********
							if (inherit) {
								adminCredit = adminCredit - (destinationList.size() * no_of_msg);
							} else {
								credits = credits - (destinationList.size() * no_of_msg);
							}
							long deductCredits = destinationList.size() * no_of_msg;
							smsDTO.setMsgCount(deductCredits);
							String filename = service.createScheduleFile(smsDTO);
							int generated_id = 0;
							if (filename != null) {
								ScheduleEntry sch = new ScheduleEntry();
								sch.setClientGmt(smsDTO.getGmt());
								sch.setClientTime(smsDTO.getTimestart());
								sch.setFileName(filename);
								sch.setRepeated(smsDTO.getRepeat());
								sch.setScheduleType(smsDTO.getReqType());
								sch.setServerId(IConstants.SERVER_ID);
								sch.setServerTime(smsDTO.getTime());
								sch.setStatus("false");
								sch.setUsername(smsDTO.getSystemId());
								sch.setDate(smsDTO.getDate());
								sch.setWebId(entry.getWebId());
								generated_id = scheduleEntryRepository.save(sch).getId();
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
									target = IConstants.SUCCESS_KEY;
									logger.info(messageResourceBundle.getLogMessage("info.scheduleSuccess"));
								} else {
									logger.error(messageResourceBundle.getLogMessage("error.scheduleError"));
									throw new ScheduledTimeException(messageResourceBundle
											.getExMessage(ConstantMessages.SINGLE_SCHEDULE_ERROR_EXCEPTION));
								}
							} else {
								logger.error(messageResourceBundle.getLogMessage("error.duplicateSchedule"));
							}
							if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
								bulkResponse.setBulkListInfo(listInfo);
								bulkResponse.setCredits(Long.toString(credits));
								bulkResponse.setDeductcredits(deductCredits + "");
							}
						} else {
							// insufficient Credits
							logger.error(messageResourceBundle.getLogMessage("error.insufficientCredit"));
							throw new InsufficientBalanceException(messageResourceBundle
									.getExMessage(ConstantMessages.INSUFFICIENT_CREDITS_EXCEPTION));
						}
					} else {
						// Number File Error
						logger.error(messageResourceBundle.getLogMessage("error.invalidNumber"));
						throw new InternalServerException(
								messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_EXCEPTION));
					}
				} else if (wallet_flag.equalsIgnoreCase("MIN")) {
					// insufficient balance
					logger.error(messageResourceBundle.getLogMessage("error.insufficientWallet"));
					throw new InsufficientBalanceException(
							messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_BALANCE_EXCEPTION));
				}
			} catch (InvalidPropertyException e) {
				logger.error(messageResourceBundle.getLogMessage("error.userError"), username, e);
				throw new InvalidPropertyException(e.getMessage());
			} catch (NotFoundException e) {
				logger.error(messageResourceBundle.getLogMessage("error.userError"), username, e);
				throw new NotFoundException(e.getMessage());
			} catch (InsufficientBalanceException e) {
				logger.error(messageResourceBundle.getLogMessage("error.userError"), username, e);
				throw new InsufficientBalanceException(e.getMessage());
			} catch (InternalServerException e) {
				logger.error(messageResourceBundle.getLogMessage("error.userError"), username, e);
				throw new InternalServerException(e.getMessage());
			} catch (ScheduledTimeException e) {
				logger.error(messageResourceBundle.getLogMessage("error.userError"), username, e);
				throw new ScheduledTimeException(e.getMessage());
			} catch (Exception e) {
				logger.error(messageResourceBundle.getLogMessage("error.userError"), username, e);
				throw new InternalServerException(e.getMessage());
			}
		} else {
			// Destination Number Error
			logger.error(messageResourceBundle.getLogMessage("error.invalidNumber"));
			throw new InternalServerException(
					messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_EXCEPTION));
		}
		bulkResponse.setStatus(target);
		return ResponseEntity.ok(bulkResponse);
	}

	@Override
	public ResponseEntity<?> identifyMessage(String username, String msg) {
		MessageIdentiryResponse messageIdentiryResponse = new MessageIdentiryResponse();
		Optional<User> userOptional = userEntryRepository.getUsers(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		String messageType = null;
		int charCount = 0;
		int charLimit = 0;
		int smsParts = 0;
		String regexpPattern = "^[A-Za-z0-9 \\r\\n@£$¥èéùìòÇØøÅå\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u00A4\u03A3\u0398\u039EÆæßÉ!\"#$%&'()*+,\\-./:;<=>?¡ÄÖÑÜ§¿äöñüà^{}\\\\\\[~\\]|\u20AC]*$";
		java.util.regex.Pattern regexp = java.util.regex.Pattern.compile(regexpPattern);

		if (regexp.matcher(msg).matches()) {
			messageType = "SpecialChar";
		} else {
			messageType = "Unicode";
		}

		int count = 0;
		if (msg != null && !msg.isEmpty()) {
			for (int i = 0; i < msg.length(); i++) {
				int asciiNum = (int) msg.charAt(i);
				if (asciiNum == 12 || asciiNum == 94 || asciiNum == 123 || asciiNum == 125 || asciiNum == 91
						|| asciiNum == 92 || asciiNum == 126 || asciiNum == 93 || asciiNum == 124 || asciiNum == 8364) {
					count++;
				}
			}
		}
		charCount = msg.length() + count;

		if ("SpecialChar".equals(messageType)) {
			if (charCount <= 160) {
				smsParts = 1;
				charLimit = 160;
			} else {
				double x = (double) charCount / 153;
				double remainder = charCount % 153;
				if (x > 0) {
					if (remainder > 0) {
						x = x + 1;
					}
				}
				smsParts = (int) x;
				charLimit = 153;
			}
		} else {
			if (charCount <= 70) {
				smsParts = 1;
				charLimit = 70;
			} else {
				double x = (double) charCount / 67;
				double remainder = charCount % 67;
				if (x > 0) {
					if (remainder > 0) {
						x = x + 1;
					}
				}
				smsParts = (int) x;
				charLimit = 67;
			}
		}
		messageIdentiryResponse.setMessage(msg);
		messageIdentiryResponse.setCharCount(charCount);
		messageIdentiryResponse.setCharLimit(charLimit);
		messageIdentiryResponse.setMessageType(messageType);
		messageIdentiryResponse.setSmscount(smsParts);
		return ResponseEntity.ok(messageIdentiryResponse);
	}

	@Override
	public ResponseEntity<?> getExcludeNumbers(String username) {
		Optional<User> userOptional = userEntryRepository.getUsers(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		String readExcludeNumbers = null;
		try {
			readExcludeNumbers = MultiUtility.readExcludeNumbers(String.valueOf(user.getUserId()));
		} catch (Exception e) {
			throw new InternalServerException(
					messageResourceBundle.getExMessage(ConstantMessages.ERROR_GETTING_EXCLUDE_NUMBER));
		}

		return ResponseEntity.ok(readExcludeNumbers);
	}

	@Override
	public ResponseEntity<?> getSenderId(String username) {
		Optional<User> userOptional = userEntryRepository.getUsers(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(
						messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_EXCEPTION));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND));
		}
		Set<String> senders = null;
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getUserId());
		if (webMasterEntry != null) {
			if (webMasterEntry.getSenderId() != null && webMasterEntry.getSenderId().length() > 1) {
				if (webMasterEntry.getSenderRestrictTo().equalsIgnoreCase("ALL")
						|| webMasterEntry.getSenderRestrictTo().equalsIgnoreCase("WEB")) {
					senders = new HashSet<String>(Arrays.asList(webMasterEntry.getSenderId().split(",")));
					logger.info(username + " " + messageResourceBundle.getLogMessage("info.configuredSenders"), senders);
				} else {
					logger.info(username + " " + messageResourceBundle.getLogMessage("info.configuredSendersRestrictedTo"), webMasterEntry.getSenderRestrictTo());
				}
			} else {
				logger.info(messageResourceBundle.getLogMessage("info.noSendersConfigured"), username);
			}
		} else {
			logger.error(messageResourceBundle.getLogMessage("error.webmasterEntryNotFound"), username);
			throw new NotFoundException(username + "webmaster entry not found");
		}
		return ResponseEntity.ok(senders);
	}

	@Override
	public ResponseEntity<?> sendAlert(String username, BulkSmsDTO bulkSmsDTO) {
		logger.info(messageResourceBundle.getLogMessage("info.sendAlert"), bulkSmsDTO.getSystemId(), bulkSmsDTO.getSenderId(), bulkSmsDTO.getDestinationList());
		String ret = "";
		String user = bulkSmsDTO.getSystemId();
		String pwd = bulkSmsDTO.getPassword();
		String message;
		String sender;
		String destination_no;
		int ston = 5;
		int snpi = 0;
		List destination_list = bulkSmsDTO.getDestinationList();
		UserSession userSession = getUserSession(user, pwd);
		while (!destination_list.isEmpty()) {
			destination_no = (String) destination_list.remove(0);
			SubmitSM msg = new SubmitSM();
			sender = bulkSmsDTO.getSenderId();
			message = bulkSmsDTO.getMessage();
			try {
				commandid = userSession.getCommandStatus();
				if (commandid == Data.ESME_ROK) {
					session = userSession.getSession();
					msg.setShortMessage(message, "ISO8859_1");
					msg.setRegisteredDelivery((byte) 1);
					msg.setDataCoding((byte) 0);
					msg.setEsmClass((byte) 0);
					msg.setSourceAddr((byte) ston, (byte) snpi, sender);
					msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
					SubmitSMResp submitResponse = null;
					try {
						submitResponse = session.submit(msg);
					} catch (Exception e) {
						logger.error(messageResourceBundle.getLogMessage("error.submitException"), user, destination_no, e);
					}
					if (submitResponse != null) {
						// ret += submitResponse.getMessageId() + "\n";
						if (submitResponse.getCommandStatus() == Data.ESME_ROK) {
							ret += submitResponse.getMessageId() + "\n";
							System.out.println(user + " Message submitted. Status=" + submitResponse.getCommandStatus()
									+ " < " + destination_no);
						} else {
							if (submitResponse.getCommandStatus() == 1035) {
								logger.error(messageResourceBundle.getLogMessage("error.insufficientBalance"), user, destination_no);
								ret += "SubmitError: Insufficient balance\n";
								userSession.setCommandStatus(submitResponse.getCommandStatus());
							} else if (submitResponse.getCommandStatus() == Data.ESME_RINVMSGLEN) {
								logger.error(messageResourceBundle.getLogMessage("error.invalidMessageLength"), user, destination_no);
								ret += "SubmitError: Invalid Message Length\n";
							} else if (submitResponse.getCommandStatus() == Data.ESME_RINVDSTADR) {
								logger.error(messageResourceBundle.getLogMessage("error.invalidDestination"), user, destination_no);
								ret += "SubmitError: Invalid Destination[" + destination_no + "]\n";
							} else if (submitResponse.getCommandStatus() == Data.ESME_RINVSRCADR) {
								logger.error(messageResourceBundle.getLogMessage("error.invalidSourceAddress"), user, sender);
								ret += "SubmitError: Invalid SourceAddress\n";
							} else if (submitResponse.getCommandStatus() == Data.ESME_RINVEXPIRY) {
								logger.error(messageResourceBundle.getLogMessage("error.accountExpired"), user, destination_no);
								ret += "SubmitError: Account Expired\n";
								userSession.setCommandStatus(submitResponse.getCommandStatus());
							} else {
								ret += "SubmitError: " + submitResponse.getCommandStatus() + "\n";
								logger.error(messageResourceBundle.getLogMessage("error.submitFailed"), submitResponse.getCommandStatus(), destination_no);
							}
						}
					} else {
						ret += "Submit Failed\n";
						logger.error(messageResourceBundle.getLogMessage("error.submitFailedNoResponse"), destination_no);
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
				logger.error(messageResourceBundle.getLogMessage("error.unknownError"), e.fillInStackTrace());
			}
		} // for loop end here
		putUserSession(userSession);
		return ResponseEntity.ok(ret);
	}

}