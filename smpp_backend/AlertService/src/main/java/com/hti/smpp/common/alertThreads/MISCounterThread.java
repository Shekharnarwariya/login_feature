package com.hti.smpp.common.alertThreads;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.exception.DataAccessError;
import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.DBMessage;
import com.hti.smpp.common.request.DeliveryDTO;
import com.hti.smpp.common.request.MailUtility;
import com.hti.smpp.common.request.ReportDTO;
import com.hti.smpp.common.response.SalesDAO;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.services.ReportService;
import com.hti.smpp.common.services.UserDAService;
import com.hti.smpp.common.services.Impl.FileContentGenerator;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.RechargeEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.ContextListener;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class MISCounterThread implements Runnable  {

	@Autowired
	private UserDAService userService;
	@Autowired
	private SessionFactory sessionFactory;
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private SalesDAO salesDAO;

	@Autowired
	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		return ((MISCounterThread) dataSource).getConnection();
	}

	// private IDatabaseService dbService = HtiSmsDB.getInstance();
	// private boolean changedate = false;
	private boolean isStop = false;
	private String fileName = IConstants.WEBAPP_DIR + "report//MIS.txt";
	private Logger logger = LoggerFactory.getLogger(MISCounterThread.class);
	private boolean morning_report;
	private UserEntry internalUser;
	private Thread thread = null;
	// ------------private caches ------------------
	private Set<Integer> sentMinBalAlertEmail = new HashSet<Integer>();
	private Set<Integer> sentMinBalAlertSms = new HashSet<Integer>();
	// private Map<String, String> CurrencySymbol = new HashMap<String, String>();
	private Map<String, String> ProfessionRecord = new HashMap<String, String>();

	public MISCounterThread() {
		thread = new Thread("MisCounterThread");
		thread.start();
		logger.info("****************MIS Counter Thread Started************************");
	}

	private Map<String, String> listDomainEmail() {
		Map<String, String> map = new HashMap<String, String>();
		Predicate<Integer, ProfessionEntry> p = new PredicateBuilderImpl().getEntryObject().get("domainEmail")
				.isNotNull();
		for (ProfessionEntry entry : GlobalVars.ProfessionEntries.values(p)) {
			if (entry.getDomainEmail().contains("@") && entry.getDomainEmail().contains(".")) {
				UserEntry userEntry = GlobalVars.UserEntries.get(entry.getUserId());
				map.put(userEntry.getSystemId(), entry.getDomainEmail());
			}
		}
		return map;
	}

	private List<UserEntryExt> listMisUsers() {
		List<UserEntryExt> list = new ArrayList<UserEntryExt>();
		EntryObject e = new PredicateBuilderImpl().getEntryObject();
		Predicate<Integer, WebMasterEntry> p = e.is("misReport").and(e.get("email").isNotNull());
		for (WebMasterEntry entry : GlobalVars.WebmasterEntries.values(p)) {
			if (entry.getEmail().contains("@") && entry.getEmail().contains(".")) {
				UserEntry userEntry = GlobalVars.UserEntries.get(entry.getUserId());
				BalanceEntry balance = GlobalVars.BalanceEntries.get(entry.getUserId());
				if (userEntry != null && balance != null) {
					UserEntryExt ext = new UserEntryExt(userEntry);
					ext.setWebMasterEntry(entry);
					ext.setBalance(balance);
					list.add(ext);
				}
			}
		}
		return list;
	}

	private Map<Integer, Set<UserEntry>> listExpiredAccounts() {
		Map<Integer, Set<UserEntry>> expiredUsers = new HashMap<Integer, Set<UserEntry>>();
		EntryObject e = new PredicateBuilderImpl().getEntryObject();
		Predicate<Integer, UserEntry> p = e.get("expiry")
				.lessThan(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		if (GlobalVars.UserEntries.values(p).isEmpty()) {
			logger.info("*** No Expired UserAccount Found ***");
		} else {
			for (UserEntry entry : GlobalVars.UserEntries.values(p)) {
				WebMasterEntry webEntry = GlobalVars.WebmasterEntries.get(entry.getId());
				if (webEntry.getExecutiveId() > 0) {
					Set<UserEntry> set = null;
					if (expiredUsers.containsKey(webEntry.getExecutiveId())) {
						set = expiredUsers.get(webEntry.getExecutiveId());
					} else {
						set = new HashSet<UserEntry>();
					}
					set.add(entry);
					expiredUsers.put(webEntry.getExecutiveId(), set);
				}
			}
			logger.info("Expired: " + expiredUsers);
		}
		return expiredUsers;
	}

	private class LocalModel {
		private String systemId;
		private String masterId;
		private String expiresOn;
		private String domainEmail;
		private String otpEmail;

		public LocalModel() {
		}

		public LocalModel(String systemId, String masterId, String expiresOn, String domainEmail, String otpEmail) {
		}

		public String getSystemId() {
			return systemId;
		}

		public void setSystemId(String systemId) {
			this.systemId = systemId;
		}

		public String getMasterId() {
			return masterId;
		}

		public void setMasterId(String masterId) {
			this.masterId = masterId;
		}

		public String getDomainEmail() {
			return domainEmail;
		}

		public void setDomainEmail(String domainEmail) {
			this.domainEmail = domainEmail;
		}

		public String getExpiresOn() {
			return expiresOn;
		}

		public void setExpiresOn(String expiresOn) {
			this.expiresOn = expiresOn;
		}

		public String getOtpEmail() {
			return otpEmail;
		}

		public void setOtpEmail(String otpEmail) {
			this.otpEmail = otpEmail;
		}
	}

	private void listExpiringPassword() {
		Set<LocalModel> expiredUsers = new HashSet<LocalModel>();
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.add(java.util.Calendar.DAY_OF_MONTH, +3);
		calendar.add(java.util.Calendar.HOUR_OF_DAY, 23);
		calendar.add(java.util.Calendar.MINUTE, 59);
		calendar.add(java.util.Calendar.SECOND, 59);
		Date enddate = calendar.getTime();
		Date expirydate = null;
		for (UserEntry entry : GlobalVars.UserEntries.values()) {
			try {
				expirydate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(entry.getPasswordExpiresOn() + " 23:59:59");
				if (expirydate.compareTo(new Date()) >= 0 && expirydate.compareTo(enddate) <= 0) {
					logger.info(entry.getSystemId() + " expiring password on " + entry.getPasswordExpiresOn());
					WebMasterEntry webEntry = GlobalVars.WebmasterEntries.get(entry.getId());
					if (webEntry.getOtpEmail() != null) {
						// expiredUsers.add(entry, webEntry.getOtpEmail());
					}
				}
			} catch (Exception e) {
				logger.error(entry.getSystemId(), e);
			}
		}
		logger.info("Expired: " + expiredUsers);
	}

	private List<UserEntryExt> listCoverageReportUsers() {
		List<UserEntryExt> list = new ArrayList<UserEntryExt>();
		EntryObject e = new PredicateBuilderImpl().getEntryObject();
		Predicate<Integer, WebMasterEntry> p = e.get("coverageReport").notEqual("No")
				.and(e.get("coverageEmail").isNotNull());
		for (WebMasterEntry entry : GlobalVars.WebmasterEntries.values(p)) {
			if (entry.getCoverageEmail().contains("@") && entry.getCoverageEmail().contains(".")) {
				UserEntry userEntry = GlobalVars.UserEntries.get(entry.getUserId());
				if (userEntry != null) {
					UserEntryExt ext = new UserEntryExt(userEntry);
					ext.setWebMasterEntry(entry);
					list.add(ext);
				}
			}
		}
		return list;
	}

	private List<UserEntryExt> listDlrReportUsers() {
		List<UserEntryExt> list = new ArrayList<UserEntryExt>();
		EntryObject e = new PredicateBuilderImpl().getEntryObject();
		Predicate<Integer, WebMasterEntry> p = e.is("dlrReport").and(e.get("dlrEmail").isNotNull());
		for (WebMasterEntry entry : GlobalVars.WebmasterEntries.values(p)) {
			if (entry.getDlrEmail().contains("@") && entry.getDlrEmail().contains(".")) {
				UserEntry userEntry = GlobalVars.UserEntries.get(entry.getUserId());
				if (userEntry != null) {
					UserEntryExt ext = new UserEntryExt(userEntry);
					ext.setWebMasterEntry(entry);
					list.add(ext);
				}
			}
		}
		return list;
	}

	private List<UserEntryExt> listUsersUnderSeller(int executiveId) {
		List<UserEntryExt> list = new ArrayList<UserEntryExt>();
		Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject().get("executiveId")
				.equal(executiveId);
		for (WebMasterEntry entry : GlobalVars.WebmasterEntries.values(p)) {
			UserEntry userEntry = GlobalVars.UserEntries.get(entry.getUserId());
			BalanceEntry balance = GlobalVars.BalanceEntries.get(entry.getUserId());
			if (userEntry != null) {
				UserEntryExt ext = new UserEntryExt(userEntry);
				ext.setWebMasterEntry(entry);
				ext.setBalance(balance);
				list.add(ext);
			}
		}
		return list;
	}

	private List<UserEntryExt> listMinBalanceUsers() {
		logger.info("listing min balance Alert Users.");
		List<UserEntryExt> list = new ArrayList<UserEntryExt>();
		Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject().is("minFlag");
		for (WebMasterEntry entry : GlobalVars.WebmasterEntries.values(p)) {
			boolean add = false;
			UserEntry userEntry = GlobalVars.UserEntries.get(entry.getUserId());
			logger.info(userEntry.getSystemId() + ": Minimum balance[" + entry.getMinBalance() + "] Alert Enabled");
			if (userEntry != null) {
				BalanceEntry balance = GlobalVars.BalanceEntries.get(entry.getUserId());
				logger.info(userEntry.getSystemId() + " Wallet: " + balance.getWalletFlag() + " Amount: "
						+ balance.getWalletAmount());
				if (balance.getWalletFlag().equalsIgnoreCase("No")) {
					if (entry.getMinBalance() >= balance.getCredits()) {
						add = true;
					}
				} else {
					if (entry.getMinBalance() >= balance.getWalletAmount()) {
						add = true;
					}
				}
				if (add) {
					UserEntryExt ext = new UserEntryExt(userEntry);
					ext.setWebMasterEntry(entry);
					ext.setBalance(balance);
					list.add(ext);
				} else {
					if (sentMinBalAlertEmail.contains(entry.getUserId())) {
						logger.info(userEntry.getSystemId() + " Removed From Sent AlertEmail Cache");
						sentMinBalAlertEmail.remove(entry.getUserId());
					}
					if (sentMinBalAlertSms.contains(entry.getUserId())) {
						logger.info(userEntry.getSystemId() + " Removed From Sent AlertSms Cache");
						sentMinBalAlertSms.remove(entry.getUserId());
					}
				}
			}
		}
		return list;
	}

	public void run() {
		while (!isStop) {
			try {
				try {
					Thread.sleep(5 * 60 * 1000); // 5 minute
				} catch (InterruptedException e) {
					logger.info("<-- MISCounterThread Interrupted --> ");
					break;
				}
				String date_time = null;
				String date_month = null;
				// String email = null;
				String saved_value = "";
				boolean proceed = true;
				// **************** Reading MIS.txt Content **********************
				BufferedReader in = null;
				File mis_text_file = new File(fileName);
				if (!mis_text_file.exists()) {
					logger.info("Creating File: " + fileName);
					FileOutputStream fileOutputStream = null;
					try {
						fileOutputStream = new FileOutputStream(fileName);
						fileOutputStream.write(new SimpleDateFormat("yyyy-MM-dd").format(new Date()).getBytes());
					} catch (IOException ex) {
						logger.error(fileName, ex);
					} finally {
						if (fileOutputStream != null) {
							try {
								fileOutputStream.close();
							} catch (IOException ioe) {
								fileOutputStream = null;
							}
						}
					}
				}
				try {
					in = new BufferedReader(new FileReader(fileName));
					saved_value = in.readLine();
					saved_value = saved_value.trim();
				} catch (Exception fnfe) {
					proceed = false;
					fnfe.printStackTrace();
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
						}
					}
					in = null;
				}
				// logger.info("Read Date :==> " + saved_value + " Proceed => " + proceed);
				// *************************************************************************
				if (proceed) {
					if (saved_value != null && saved_value.length() > 0 && saved_value.contains("-")) {
						// logger.info("<-- Valid Date Found --> ");
						// ***************** Comparing Read Date With Current Date *************
						try {
							new SimpleDateFormat("yyyy-MM-dd").parse(saved_value.trim());
							date_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
							if (!saved_value.equalsIgnoreCase(date_time)) {
								// *********** Comparing Time ******************************
								logger.info("Configured Mis Time : "
										+ ContextListener.property.getProperty("load.misTime"));
								LocalTime parse_time = LocalTime.parse(
										ContextListener.property.getProperty("load.misTime").trim(),
										DateTimeFormatter.ofPattern("HHmmss"));
								if (LocalTime.now().isAfter(parse_time)) {
									logger.info("<- Current Time is After the configured Time -> ");
									Calendar calendar = Calendar.getInstance();
									if (calendar.get(Calendar.DATE) == 1) {
										calendar.add(Calendar.MONTH, -1);
										date_month = new SimpleDateFormat("yyyy-MM").format(calendar.getTime());
									} else {
										date_month = new SimpleDateFormat("yyyy-MM").format(new Date());
									}
								} else {
									proceed = false;
									logger.info("<- Current Time is before the configured Time -> ");
								}
							} else {
								// logger.info("<- Configured Date is Equal to Current Date -> ");
								proceed = false;
							}
						} catch (ParseException ex) {
							proceed = false;
							logger.info("<--- Invalid Date[" + saved_value + "] Found For Mis Report --> ");
						}
					} else {
						proceed = false;
						logger.info("<--- Invalid Date[" + saved_value + "] Found For Mis Report --> ");
					}
				}
				if (proceed) {
					logger.info("<--- Resetting Date For Mis Flag --->");
					BufferedWriter out = null;
					try {
						out = new BufferedWriter(new FileWriter(fileName));
						out.write(date_time);
						out.close();
					} catch (IOException e) {
					} finally {
						if (out != null) {
							try {
								out.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
							}
							out = null;
						}
					}
				}
				if (GlobalVars.MASTER_CLIENT) {
					try {
						ProfessionRecord.clear();
						ProfessionRecord = listDomainEmail();
						if (proceed) {
							proceed = false;
							morning_report = false;
							List<UserEntryExt> misUsers = listMisUsers();
							logger.info("Start For Mis Report. Users: " + misUsers.size());
							if (!misUsers.isEmpty()) {
								Map Reportmap = null;
								for (UserEntryExt entry : misUsers) {
									UserEntry userEntry = entry.getUserEntry();
									String username = userEntry.getSystemId();
									try {
										String email = entry.getWebMasterEntry().getEmail();
										logger.info("Checking For Mis Report of " + username + " EMail: " + email);
										Reportmap = getMisCount(username, date_month);
										if (!Reportmap.isEmpty()) {
											logger.info(username + " MIS Report Size : " + Reportmap.size());
											if (userEntry != null) {
												String master_id = userEntry.getMasterId();
												String from = IConstants.SUPPORT_EMAIL[0];
												try {
													if (ProfessionRecord.containsKey(username)) {
														from = ProfessionRecord.get(username);
													} else {
														logger.info(username + " Domain-Email Not Found");
														if (ProfessionRecord.containsKey(master_id)) {
															from = ProfessionRecord.get(master_id);
														} else {
															logger.info(master_id + " Domain-Email Not Found");
														}
													}
													String mail_content = createMISCountContent(entry);
													Workbook workbook = getWorkBook(Reportmap, entry);
													Object[] attach = new Object[] { workbook };
													send(email, from, mail_content, "Transaction Details : "
															+ new SimpleDateFormat("dd-MMM-yyyy").format(new Date()),
															attach, false);
													logger.info("MIS Report[" + userEntry.getSystemId() + "] Sent From:"
															+ from + " To:" + email);
												} catch (Exception ex) {
													logger.info(ex + " While Sending MIS Report to "
															+ userEntry.getSystemId() + " @ " + email);
												}
											}
										} else {
											logger.info(username + "< No Mis Records Found >");
										}
									} catch (Exception ex) {
										logger.info(username + " MIS Report Creation ", ex.fillInStackTrace());
									}
								}
							}
							logger.info("End For Mis Report");
							// ****************** Start For Daily Deliver Report **************************
							List<UserEntryExt> dlrReportUsers = listDlrReportUsers();
							logger.info("Start For Daily Delivery Report. Users :" + dlrReportUsers.size());
							List<DBMessage> report = null;
							for (UserEntryExt regUserExt : dlrReportUsers) {
								UserEntry regUser = regUserExt.getUserEntry();
								logger.info("Checking for DLR Report of " + regUser.getSystemId());
								if (regUserExt.getWebMasterEntry().getDlrEmail() != null) {
									Calendar calendar = Calendar.getInstance();
									calendar.add(Calendar.DATE, -1);
									report = getDLRReport(regUserExt,
											new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
									logger.info(
											"User: " + regUser.getSystemId() + " DLR Report Size: " + report.size());
									// logger.info("User: " + regUser.getUserName() + " Report Size: " +
									// report.size());
									if (!report.isEmpty()) {
										String dlrfile = FileContentGenerator.createDlrXLSContent(report,
												regUser.getSystemId());
								
										String from = IConstants.SUPPORT_EMAIL[0];
										if (ProfessionRecord.containsKey(regUser.getSystemId())) {
											from = ProfessionRecord.get(regUser.getSystemId());
										} else {
											logger.info(regUser.getSystemId() + " Domain-Email Not Found");
											if (ProfessionRecord.containsKey(regUser.getMasterId())) {
												from = ProfessionRecord.get(regUser.getMasterId());
											} else {
												logger.info(regUser.getMasterId() + " Domain-Email Not Found");
											}
										}
										try {
											MailUtility.sendDLRReport(regUserExt.getWebMasterEntry().getDlrEmail(),
													from, dlrfile, false, saved_value);
											logger.info(regUser.getSystemId() + " Sent DLR Report :"
													+ regUserExt.getWebMasterEntry().getDlrEmail());
										} catch (Exception ex) {
											logger.info(ex + " While Sending DLR Report Email("
													+ regUserExt.getWebMasterEntry().getDlrEmail() + ") For User: "
													+ regUser.getSystemId());
										}
									}
								} else {
									logger.info(regUser.getSystemId() + " DlrReport Email Not Found");
								}
							}
							logger.info("End For Daily Delivery Report");
							// ******************* End ****************************************************
							// ****** Start Code For Executives Reporting *******
							logger.info("Start For Executives Reporting");
							Collection<SalesEntry> sellerlist =  (Collection<SalesEntry>) list("seller");
							logger.info("Total Executives :--> " + sellerlist.size());
							for (SalesEntry seller : sellerlist) {
								String sellerName = null;
								try {
									sellerName = seller.getUsername();
									int seller_id = seller.getId();
									boolean isReport = false;
									String reporting = seller.getReporting();
									logger.info("Executive : " + sellerName + "[" + seller_id + "] Reporting: "
											+ reporting);
									if (seller.getEmail() != null) {
										if (reporting.equalsIgnoreCase("Monthly")) {
											if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1) {
												isReport = true;
											}
										} else if (reporting.equalsIgnoreCase("Weekly")) {
											if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
												isReport = true;
											}
										} else {
											isReport = true;
										}
										// logger.info("Executive : " + sellerId + " isReport: " + isReport);
										if (isReport) {
											List<UserEntryExt> userlist = listUsersUnderSeller(seller.getId());
											if (!userlist.isEmpty()) {
												Map<String, String> modeMap = new HashMap<String, String>();
												List<UserEntryExt> newUsers = new ArrayList<UserEntryExt>();
												Calendar start = Calendar.getInstance();
												boolean isDaily = false;
												if (reporting.equalsIgnoreCase("Monthly")) {
													start.add(Calendar.MONTH, -1);
												} else if (reporting.equalsIgnoreCase("Weekly")) {
													start.add(Calendar.DATE, -7);
												} else if (reporting.equalsIgnoreCase("Daily")) {
													start.add(Calendar.DATE, -1);
													isDaily = true;
												}
												start.set(Calendar.HOUR, 00);
												start.set(Calendar.MINUTE, 00);
												start.set(Calendar.SECOND, 00);
												String startdate = new SimpleDateFormat("yyyy-MM-dd")
														.format(start.getTime());
												// logger.info("Start Date -> " + startdate);
												Calendar end = Calendar.getInstance();
												end.add(Calendar.DATE, -1);
												String enddate = new SimpleDateFormat("yyyy-MM-dd")
														.format(end.getTime());
												// logger.info("End Date -> " + enddate);
												logger.info("Executive : " + sellerName + " Start Date: " + startdate
														+ " End Date:" + enddate);
												if (userlist != null && !userlist.isEmpty()) {
													// logger.info(sellerName + " Users Found: " + userlist.size());
													Iterator<UserEntryExt> listItr = userlist.iterator();
													String reportusers = "";
													Map<Integer, String> user_id_mapping = new HashMap<Integer, String>();
													while (listItr.hasNext()) {
														boolean isAdd = true;
														UserEntryExt userEntryExt = listItr.next();
														UserEntry userDTO = userEntryExt.getUserEntry();
														logger.info(sellerName + " Reporting User : "
																+ userDTO.getSystemId() + " CreatedOn:"
																+ userDTO.getCreatedOn() + " WalletFlag: "
																+ userEntryExt.getBalance().getWalletFlag());
														String mode = "wallet";
														if (userEntryExt.getBalance().getWalletFlag()
																.equalsIgnoreCase("no")) {
															mode = "credit";
														}
														modeMap.put(userDTO.getSystemId(), mode);
														// ------- Start Checking for New User Creation
														// -----------------
														String createdon = userDTO.getCreatedOn();
														if (createdon != null && createdon.length() > 0) {
															Date creationDate = null;
															try {
																creationDate = new SimpleDateFormat(
																		"yyyy-MM-dd HH:mm:ss").parse(createdon);
																// logger.info("Creation Date -> " + new
																// SimpleDateFormat("yyyy-MM-dd
																// HH:mm:ss").format(creationDate));
															} catch (ParseException ex) {
																ex.printStackTrace();
															}
															if (creationDate != null
																	&& creationDate.compareTo(start.getTime()) >= 0
																	&& creationDate.compareTo(end.getTime()) <= 0) {
																// logger.info("New User ==> " + userDTO.getSystemId());
																newUsers.add(userEntryExt);
																listItr.remove();
															} else if (creationDate != null
																	&& creationDate.compareTo(end.getTime()) > 0) {
																listItr.remove();
																isAdd = false;
															}
														}
														if (isAdd) {
															reportusers += "'" + userDTO.getSystemId() + "',";
															user_id_mapping.put(userDTO.getId(), userDTO.getSystemId());
														}
														// ------ End Checking for New User Creation -----------
													}
													// ---------------------------------------------------------
													logger.info("Executive : " + sellerName + " ReportUsers : "
															+ reportusers);
													if (reportusers.length() > 0) {
														reportusers = reportusers.substring(0,
																reportusers.length() - 1);
														String sql = "select count(msg_id) as msgCount,username from smsc_in_log where username in("
																+ reportusers + ") and ";
														if (isDaily) {
															sql += "DATE(time) = '" + startdate + "'";
														} else {
															sql += "DATE(time) between '" + startdate + "' and '"
																	+ enddate + "'";
														}
														sql += " group by username";
														// logger.info("SQL: " + sql);
														logger.info(sellerName
																+ " <- Checking For Traffic Count Of Users -> ");
														Map<String, Long> smsCount = getSmscInCount(sql);
														Integer[] useridarr = user_id_mapping.keySet()
																.toArray(new Integer[user_id_mapping.size()]);
														Map<Integer, List<RechargeEntry>> recharge_map = userService
																.listTransactions(useridarr, "cr%", startdate, enddate);
														// ---------- Start Creating mail Content ---------------
														logger.info(sellerName + " Start Preparing Email Content");
														String htmlString = "";
														htmlString += "<body bgcolor='#FFEBD9'>"
																+ "<span >Dear Team,</span>" + "<br>" + "<br>"
																+ "<span >This is Reporting of below mentioned Sales Executive :"
																+ "</span><br />" + "<br>" + "<br>"
																+ "<span >Executive[Id]      : <strong >" + sellerName
																+ "[" + seller_id + "]" + "</strong></span><br />"
																+ "<br>" + "<span >Reporting Type : <strong >"
																+ reporting + "</strong></span><br />" + "<br>"
																+ "<span >Reporting Time : From <strong >" + startdate
																+ "</strong> To <strong >" + enddate
																+ "</strong></span>" + "<br>" + "<br>"
																+ "<span >New Users Created : " + newUsers.size()
																+ "</span><br>" + "<br>";
														logger.info(sellerName + " Adding New Users Details: "
																+ newUsers.size());
														if (!newUsers.isEmpty()) {
															htmlString += "<br><span>Newly Created Account Details:</span><br><br>"
																	+ "<table width='758' height='48' cellspacing='1' cellpadding='2' border='1' align='center' summary=''>"
																	+ "<tbody>" + "<tr align='center'>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Username</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>MasterId</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Type</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Balance</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>CreatedOn</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>ExpiresOn</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Remark</strong></font></td>"
																	+ "</tr>";
															for (UserEntryExt newuser : newUsers) {
																htmlString += "<tr align='center'>" + "<td>"
																		+ newuser.getUserEntry().getSystemId() + "</td>"
																		+ "<td>" + newuser.getUserEntry().getMasterId()
																		+ "</td>";
																if (newuser.getBalance().getWalletFlag()
																		.equalsIgnoreCase("no")) {
																	htmlString += "<td> Credit </td>" + "<td>"
																			+ newuser.getBalance().getCredits()
																			+ "</td>";
																} else {
																	htmlString += "<td> Wallet </td>" + "<td>"
																			+ newuser.getBalance().getWalletAmount()
																			+ "</td>";
																}
																htmlString += "<td>"
																		+ newuser.getUserEntry().getCreatedOn()
																		+ "</td>" + "<td>"
																		+ newuser.getUserEntry().getExpiry() + "</td>"
																		+ "</td>" + "<td>"
																		+ newuser.getUserEntry().getRemark() + "</td>"
																		+ "</tr>";
															}
															htmlString += "</tbody>" + "</table>";
														}
														logger.info(sellerName + " Adding Account Details : "
																+ userlist.size());
														if (!userlist.isEmpty()) {
															htmlString += "<br><br><span>Other Account Details</span><br><br>"
																	+ "<table width='700' height='48' cellspacing='1' cellpadding='2' border='1' align='center' summary=''>"
																	+ "<tbody>" + "<tr align='center'>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Username</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>MasterId</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Type</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Balance</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>ExpiresOn</strong></font></td>"
																	+ "</tr>";
															// Iterator iter = userlist.iterator();
															for (UserEntryExt olduser : userlist) {
																htmlString += "<tr align='center'>" + "<td>"
																		+ olduser.getUserEntry().getSystemId() + "</td>"
																		+ "<td>" + olduser.getUserEntry().getMasterId()
																		+ "</td>";
																if (olduser.getBalance().getWalletFlag()
																		.equalsIgnoreCase("no")) {
																	htmlString += "<td> Credit </td>" + "<td>"
																			+ olduser.getBalance().getCredits()
																			+ "</td>";
																} else {
																	htmlString += "<td> Wallet </td>" + "<td>"
																			+ olduser.getBalance().getWalletAmount()
																			+ "</td>";
																}
																htmlString += "<td>"
																		+ olduser.getUserEntry().getExpiry() + "</td>";
																htmlString += "</tr>";
															}
															htmlString += "</tbody>" + "</table>";
														}
														logger.info(sellerName + " Adding Recharge Details : "
																+ recharge_map.size());
														if (!recharge_map.isEmpty()) {
															htmlString += "<br><br><span>Recharge Details</span><br><br>"
																	+ "<table width='758' height='48' cellspacing='1' cellpadding='2' border='1' align='center' summary=''>"
																	+ "<tbody>" + "<tr align='center'>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Username</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Debit A/C</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Usermode</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Previous Balance</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Recharge Amount</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Effective Balance</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Time</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Remarks</strong></font></td>"
																	+ "</tr>";
															for (Map.Entry<Integer, List<RechargeEntry>> entry : recharge_map
																	.entrySet()) {
																int user_id = entry.getKey();
																List<RechargeEntry> rechargeList = entry.getValue();
																String systemId = user_id_mapping.get(user_id);
																for (RechargeEntry recharge : rechargeList) {
																	String preBal = "-", amount = "-", effBal = "-",
																			mode = "-";
																	if (modeMap.containsKey(systemId)) {
																		mode = (String) modeMap.get(systemId);
																	}
																	if (mode.equalsIgnoreCase("credit")) {
																		preBal = recharge.getPreviousCredits() + "";
																		amount = recharge.getToBeAddedCedits() + "";
																		effBal = recharge.getEffectiveCredits() + "";
																	} else {
																		preBal = recharge.getPreviousWallet() + "";
																		amount = recharge.getToBeAddedWallet() + "";
																		effBal = recharge.getEffectiveWallet() + "";
																	}
																	String particular = recharge.getParticular();
																	if (particular != null
																			&& particular.indexOf("_") > -1) {
																		particular = particular.substring(
																				particular.indexOf("_") + 1,
																				particular.length());
																	}
																	htmlString += "<tr align='center'>" + "<td>"
																			+ systemId + "</td>" + "<td>" + particular
																			+ "</td>" + "<td>" + mode + "</td>" + "<td>"
																			+ preBal + "</td>" + "<td>" + amount
																			+ "</td>" + "<td>" + effBal + "</td>"
																			+ "<td>" + recharge.getTime() + "</td>"
																			+ "<td>" + recharge.getRemark() + "</td>"
																			+ "</tr>";
																}
															}
															htmlString += "</tbody>" + "</table>";
														}
														logger.info(sellerName + " Adding Traffic Count Details : "
																+ smsCount.size());
														if (!smsCount.isEmpty()) {
															htmlString += "<br><br><span>Userwise Sms Count</span><br><br>"
																	+ "<table width='350' height='48' cellspacing='1' cellpadding='2' border='1' align='center' summary=''>"
																	+ "<tbody>" + "<tr align='center'>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Username</strong></font></td>"
																	+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Count</strong></font></td>"
																	+ "</tr>";
															for (Map.Entry<String, Long> entry : smsCount.entrySet()) {
																htmlString += "<tr align='center'>" + "<td>"
																		+ entry.getKey() + "</td>" + "<td>"
																		+ entry.getValue() + "</td>" + "</tr>";
															}
															htmlString += "</tbody>" + "</table>";
														}
														htmlString += "<br><br><br><br><br><br>"
																+ "<strong><span >With Kind Regards,</span><br><br>"
																+ "<span >Support Team</span></strong><br>" + "<br>"
																+ "</body>";
														// ---------- End Creating mail Content -----------------
														logger.info("Sending Executive(" + sellerName + ") Report : "
																+ seller.getEmail());
														String reporthtml = IConstants.WEBAPP_DIR + "//mail//"
																+ sellerName + "_report_"
																+ new SimpleDateFormat("yyyy-MM-dd_HHmmss")
																		.format(new Date())
																+ ".html";
														MultiUtility.writeMailContent(reporthtml, htmlString);
														String subject = "Executive Report: " + sellerName + "_"
																+ new SimpleDateFormat("dd-MMM-yyyy_HHmmss")
																		.format(new Date());
														String from = null;
														if (seller.getDomainEmail() != null
																&& !seller.getDomainEmail().isEmpty()) {
															from = seller.getDomainEmail();
														} else {
															from = IConstants.SUPPORT_EMAIL[0];
														}
														try {
															MailUtility.send(seller.getEmail(), reporthtml, subject,
																	from, true);
														} catch (Exception ex) {
															logger.error(" Sending Email For Executive: " + sellerName,
																	ex.fillInStackTrace());
														}
													} else {
														logger.info(sellerName + " < No Reporting Users Found >");
													}
												} else {
													logger.info(sellerName + " < No Users Found >");
												}
											} else {
												logger.info(sellerName + " < No Users Found >");
											}
										}
									} else {
										logger.info(sellerName + " < No Valid Email Found >");
									}
								} catch (Exception e) {
									logger.error(sellerName, e);
								}
							}
							logger.info(" End For Executives Reporting ");
							logger.info(" Start For Excutive alert of Expired Accounts ");
							try {
								Map<Integer, Set<UserEntry>> expiredAccounts = listExpiredAccounts();
								Map<Integer, SalesEntry> sellerMappedManager = listSellerMappedManager();
								Map<Integer, SalesEntry> sellers = list("seller");
								for (Map.Entry<Integer, Set<UserEntry>> expiredEntry : expiredAccounts.entrySet()) {
									if (sellerMappedManager.containsKey(expiredEntry.getKey())) {
										SalesEntry manager = sellerMappedManager.get(expiredEntry.getKey());
										SalesEntry seller = sellers.get(expiredEntry.getKey());
										String manager_name = manager.getUsername();
										logger.info(manager_name + " Under Expired User Accounts: "
												+ expiredEntry.getValue().size());
										String htmlString = "<body bgcolor='#FFEBD9'>" + "<span >Dear Team,</span>"
												+ "<br>" + "<br>" + "<span >Expired Account Details Under Executive["
												+ seller.getUsername() + "] As Below:" + "</span><br />" + "<br>"
												+ "<br>";
										htmlString += "<table width='700' height='48' cellspacing='1' cellpadding='2' border='1' align='center' summary=''>"
												+ "<tbody>" + "<tr align='center'>"
												+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Username</strong></font></td>"
												+ "<td bgcolor='#A8A8A8'><font size='2'><strong>MasterId</strong></font></td>"
												+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Type</strong></font></td>"
												+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Balance</strong></font></td>"
												+ "<td bgcolor='#A8A8A8'><font size='2'><strong>ExpiredOn</strong></font></td>"
												+ "</tr>";
										// Iterator iter = userlist.iterator();
										for (UserEntry userEntry : expiredEntry.getValue()) {
											BalanceEntry balance = GlobalVars.BalanceEntries.get(userEntry.getId());
											htmlString += "<tr align='center'>" + "<td>" + userEntry.getSystemId()
													+ "</td>" + "<td>" + userEntry.getMasterId() + "</td>";
											if (balance.getWalletFlag().equalsIgnoreCase("no")) {
												htmlString += "<td> Credit </td>" + "<td>" + balance.getCredits()
														+ "</td>";
											} else {
												htmlString += "<td> Wallet </td>" + "<td>" + balance.getWalletAmount()
														+ "</td>";
											}
											htmlString += "<td>" + userEntry.getExpiry() + "</td>";
											htmlString += "</tr>";
										}
										htmlString += "</tbody>" + "</table>";
										htmlString += "<br><br><br><br><br><br>"
												+ "<strong><span >With Kind Regards,</span><br><br>"
												+ "<span >Support Team - Broadnet</span></strong><br>" + "<br>"
												+ "</body>";
										// ------ send Email to manager for this seller --------------
										logger.info(manager_name + " Sending Expired Account alert under Executive("
												+ seller.getUsername() + ") : " + manager.getEmail());
										String reporthtml = IConstants.WEBAPP_DIR + "//mail//" + manager_name + "_"
												+ seller.getUsername() + "_expired_"
												+ new SimpleDateFormat("yyMMdd_HHmmss").format(new Date()) + ".html";
										MultiUtility.writeMailContent(reporthtml, htmlString);
										String subject = "Expired Accounts Alert under Executive: "
												+ seller.getUsername() + "_"
												+ new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
										try {
											MailUtility.send(manager.getEmail(), reporthtml, subject,
													IConstants.SUPPORT_EMAIL[0], true);
										} catch (Exception ex) {
											logger.error(manager_name + " Sending Expired Alert Email: "
													+ seller.getUsername(), ex.fillInStackTrace());
										}
									}
								}
							} catch (Exception ex) {
								logger.error("Expired Account Alert", ex.fillInStackTrace());
							}
							logger.info(" End For Excutive alert of Expired Accounts ");
							// ******************* End Executives Reporting ******
							// ****** Start Code For Coverage Reporting *******
							List<UserEntryExt> coverageReportUsers = listCoverageReportUsers();
							logger.info("Start For Coverage Report. Users: " + coverageReportUsers.size());
							ReportService service = new ReportService();
							for (UserEntryExt userEntryExt : coverageReportUsers) {
								String userid = null;
								try {
									boolean isReport = false;
									userid = userEntryExt.getUserEntry().getSystemId();
									String reporting = userEntryExt.getWebMasterEntry().getCoverageReport();
									String coverage_email = userEntryExt.getWebMasterEntry().getCoverageEmail();
									logger.info(userid + " < Checking For Coverage Report[" + reporting + "] > Email: "
											+ coverage_email);
									if (coverage_email != null) {
										if (reporting.equalsIgnoreCase("Monthly")) {
											if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1) {
												isReport = true;
											}
										} else if (reporting.equalsIgnoreCase("Weekly")) {
											if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
												isReport = true;
											}
										} else {
											isReport = true;
										}
										if (isReport) {
											try {
												String filename = null;
												filename = service.getCoverageReportXLS(userid);
												logger.info(filename);
												String from = IConstants.ROUTE_EMAIL[0];
												String master = userEntryExt.getUserEntry().getMasterId();
												if (ProfessionRecord
														.containsKey(userEntryExt.getUserEntry().getSystemId())) {
													from = (String) ProfessionRecord
															.get(userEntryExt.getUserEntry().getSystemId());
												} else {
													if (ProfessionRecord.containsKey(master)) {
														from = (String) ProfessionRecord.get(master);
													}
												}
												MailUtility.sendReport(userid,
														"Coverage Report " + new SimpleDateFormat("dd-MMM-yyyy_HHmmss")
																.format(new Date()),
														from, coverage_email, filename, false);
											} catch (Exception ex) {
												logger.error(userid + " CoverageReport", ex.fillInStackTrace());
											}
										}
									} else {
										logger.info(userid + " Coverage Report Email Not Found ");
									}
								} catch (Exception e) {
									logger.error(userid, e);
								}
							}
							logger.info(" End For Coverage Report ");
							// ****** End Coverage Reporting ******************
							// ********* Start Smsc Performance Report ********
							logger.info(" Start For Smsc Performance Report ");
							try {
								Calendar last = Calendar.getInstance();
								String report_time = "";
								String performance_sql = "select count(msg_id) as msgCount,smsc,oprCountry,status from report_log where ";
								if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1) {
									logger.info("<-- Smsc Performance Report Monthly ---> ");
									last.add(Calendar.MONTH, -1);
									report_time = "Month " + new SimpleDateFormat("MMM-yyyy").format(last.getTime());
									;
									performance_sql += "MONTH(time)='"
											+ new SimpleDateFormat("MM").format(last.getTime()) + "' ";
								} else {
									logger.info("<-- Smsc Performance Report Daily ---> ");
									last.add(Calendar.DATE, -1);
									report_time = new SimpleDateFormat("dd-MMM-yyyy").format(last.getTime());
									performance_sql += "DATE(time)='"
											+ new SimpleDateFormat("yyyy-MM-dd").format(last.getTime()) + "' ";
								}
								performance_sql += "group by smsc,oprCountry,status order by smsc,oprCountry,status";
								logger.info("Smsc Performance SQL: " + performance_sql);
								List final_list = new ArrayList();
								Map map = getSmscPerformanceReport(performance_sql);
								Iterator itr = map.values().iterator(); // operator_maps
								while (itr.hasNext()) {
									final_list.addAll(((Map) itr.next()).values());
								}
								if (!final_list.isEmpty()) {
									try {
										final_list = sortList(final_list);
										Workbook workbook = getSmscWorkBook(final_list);
										String content = createPerformaceContent(report_time);
										Object[] attach = new Object[] { workbook };
										send(IConstants.TO_EMAIl, IConstants.SUPPORT_EMAIL[0], content,
												" Performance Report : "
														+ new SimpleDateFormat("dd-MMM-yyyy").format(new Date()),
												attach, false);
										logger.info(" Smsc Performance Report Sent To: " + IConstants.TO_EMAIl);
									} catch (Exception ex) {
										logger.info(" Smsc Performance Report ", ex.fillInStackTrace());
									}
								} else {
									logger.info(" Smsc Performance Report Empty.");
								}
							} catch (Exception e) {
								logger.error("", e);
							}
							logger.info(" End For Smsc Performance Report ");
							// ********* End Smsc Performance Report **********
						}
					} catch (Exception Ex) {
						logger.info("", Ex.fillInStackTrace());
					}
					logger.info("<-- Checking For Minimum balance Alert -->");
					// **************Start Send Minimum balance Alert sms
					// ***********************************
					List<UserEntryExt> minBalAlertUsers = listMinBalanceUsers();
					internalUser = userService.getInternUserEntry();
					if (!minBalAlertUsers.isEmpty()) {
						logger.info("Users For Minimum balance Alert: " + minBalAlertUsers.size());
						// SendSmsService service = new SendSmsService();
						try {
							String content = null;
							try {
								content = MultiUtility.readContent(IConstants.FORMAT_DIR + "MinAlertSMS.txt");
							} catch (Exception ex) {
								logger.error("Minimum balance Alert", ex.fillInStackTrace());
							}
							if (content == null) {
								content = "Dear #,\n";
								content += "Your A/c Balance is #. Please Recharge Soon.\n";
							}
							for (UserEntryExt user : minBalAlertUsers) {
								String username = user.getUserEntry().getSystemId();
								double balance = 0;
								if (user.getBalance().getWalletFlag().equalsIgnoreCase("no")) {
									balance = user.getBalance().getCredits();
								} else {
									balance = user.getBalance().getWalletAmount();
								}
								if (!sentMinBalAlertEmail.contains(user.getUserEntry().getId())) {
									if (user.getWebMasterEntry().getMinBalEmail() != null
											&& user.getWebMasterEntry().getMinBalEmail().length() > 0
											&& user.getWebMasterEntry().getMinBalEmail().contains("@")) {
										logger.info(" Checking For Minimum balance Alert Email For -->" + username);
										String master_id = user.getUserEntry().getMasterId();
										String from = IConstants.SUPPORT_EMAIL[0];
										if (ProfessionRecord.containsKey(username)) {
											from = (String) ProfessionRecord.get(username);
										} else {
											if (ProfessionRecord.containsKey(master_id)) {
												from = (String) ProfessionRecord.get(master_id);
											}
										}
										String mail_content = createLowBalanceContent(user);
										try {
											send(user.getWebMasterEntry().getMinBalEmail(), from, mail_content,
													"Low Balance Alert : "
															+ new SimpleDateFormat("dd-MMM-yyyy").format(new Date()),
													null, false);
											logger.info("Minimum Balance Alert Sent to "
													+ user.getUserEntry().getSystemId() + " @ "
													+ user.getWebMasterEntry().getMinBalEmail() + " From " + from);
											sentMinBalAlertEmail.add(user.getUserEntry().getId());
										} catch (Exception ex) {
											logger.info(ex + "While Sending Minimum Alert to "
													+ user.getUserEntry().getSystemId() + " @ "
													+ user.getWebMasterEntry().getMinBalEmail());
										}
									} else {
										logger.info(username + " <-- Invalid Minimum balance Email found --> "
												+ user.getWebMasterEntry().getMinBalEmail());
									}
								} else {
									logger.info(username + " Minimum balance Alert Email Already Sent.");
								}
								// String value = (String) map.get(username);
								if (user.getWebMasterEntry().isSmsAlert()) {
									if (internalUser != null) {
										if (!sentMinBalAlertSms.contains(user.getUserEntry().getId())) {
											String mobile = user.getWebMasterEntry().getMinBalMobile();
											if (mobile != null && mobile.length() > 8) {
												String temp_content = new String(content);
												logger.info(
														" Checking For Minimum balance SMS Alert For -->" + username);
												if (temp_content.contains("#")) {
													temp_content = temp_content.replaceFirst("#", username);
													temp_content = temp_content.replaceFirst("#", balance + "");
												}
												ArrayList<String> list = new ArrayList<String>(
														Arrays.asList(mobile.split(",")));
												BulkSmsDTO smsDTO = new BulkSmsDTO();
												smsDTO.setSystemId(internalUser.getSystemId());
												smsDTO.setPassword(internalUser.getPassword());
												// smsDTO.setMessageType("SpecialChar");
												smsDTO.setMessage(temp_content);
												smsDTO.setDestinationList(list);
												// smsDTO.setFrom("Name");
												smsDTO.setSenderId(IConstants.ALERT_SENDER_ID);
												// String Response = service.sendAlert(smsDTO);
//												smsDTO.setSenderId(IConstants.ALERT_SENDER_ID);
//												String Response = service.sendAlert(smsDTO);
//												sentMinBalAlertSms.add(user.getUserEntry().getId());

												final String url = "http://localhost:8083/sms/send/alert";
												HttpHeaders headers = new HttpHeaders();
												headers.set("username", username);
												HttpEntity<BulkSmsDTO> requestEntity = new HttpEntity<>(smsDTO,
														headers);
												ResponseEntity<?> response = restTemplate.postForEntity(url,
														requestEntity, String.class);

												sentMinBalAlertSms.add(user.getUserEntry().getId());
												logger.info("<ALERT SMS: " + response + ">" + username + "<" + mobile
														+ ">");
											} else {
												logger.info(username
														+ " <-- Invalid Mobile Number For Minimum balance Alert --> "
														+ mobile);
											}
										} else {
											logger.info(username + " Minimum balance Alert Sms Already Sent.");
										}
									} else {
										logger.info("<-- No Internal User Found to send Minimum balance Alert --> ");
									}
								}
							}
						} catch (DataAccessError ex) {
							logger.error("Minimum balance Alert", ex.fillInStackTrace());
						} catch (Exception ex) {
							logger.error("Minimum balance Alert", ex.fillInStackTrace());
						}
						minBalAlertUsers.clear();
					} else {
						logger.info("<-- No User Found For Minimum balance Alert --> ");
					}
					// **************End Send Minimum balance Alert sms
					// ***********************************
					// ------------------------ executive Daily descriptive report -------------
					if (!morning_report) {
						LocalTime parse_time = LocalTime.parse("06:00");
						if (LocalTime.now().isAfter(parse_time)) {
							 Map<Integer, SalesEntry> sellerlist = list("seller");
							logger.info(
									"Start For Executive Descriptive Morning Report. Executives: " + sellerlist.size());
							morning_report = true;
							// logger.info("Usermap Size :" + usermap.size());
							for (SalesEntry seller : sellerlist.values()) {
								String sellerName = seller.getUsername();
								logger.info("Checking For Executive: " + sellerName + "[" + seller.getId() + "] Email:"
										+ seller.getEmail());
								if (seller.getEmail() != null) {
									List<UserEntryExt> userlist = listUsersUnderSeller(seller.getId());
									if (!userlist.isEmpty()) {
										// ------------ Daily Descriptive Report ---------------
										String reportUsers = "";
										for (UserEntryExt user : userlist) {
											reportUsers += "'" + user.getUserEntry().getSystemId() + "',";
										}
										if (reportUsers.length() > 0) {
											reportUsers = reportUsers.substring(0, reportUsers.length() - 1);
											String descriptive_sql = "select * from rb.statnew where time like '"
													+ new SimpleDateFormat("yyyy-MM")
															.format(Calendar.getInstance().getTime())
													+ "%' and username in(" + reportUsers + ") order by time,username";
											List<ReportDTO> report_list = getExecutiveDescriptiveReport(
													descriptive_sql);
											logger.info(sellerName + "[" + report_list.size() + "] SQL: "
													+ descriptive_sql);
											if (!report_list.isEmpty()) {
												try {
													Workbook workbook = getExecDescriptiveWorkbook(report_list,
															sellerName);
													String content = createExecDescriptiveContent(sellerName);
													Object[] attach = new Object[] { workbook };
													send(seller.getEmail(), IConstants.SUPPORT_EMAIL[0], content,
															" Descriptive Report : " + new SimpleDateFormat("MMM-yyyy")
																	.format(new Date()),
															attach, false);
													logger.info(sellerName + " Descriptive Report Sent -->"
															+ seller.getEmail());
												} catch (Exception ex) {
													logger.info(sellerName + " Descriptive Report ",
															ex.fillInStackTrace());
												}
											} else {
												logger.info(sellerName + " Descriptive Report Empty");
											}
										}
									}
								} else {
									logger.info(sellerName + " < No Valid Email Found > ");
								}
							}
							logger.info("End For Executive Desriptive Morning Report ");
						}
					}
				} else {
					if (proceed) {
						proceed = false;
					}
				}
				// ------------------------ end --------------------------------
				if (!GlobalVars.ActiveUsers.isEmpty()) {
					logger.info("Active Users: " + GlobalVars.ActiveUsers);
				}
			} catch (Exception e) {
				logger.error("", e.fillInStackTrace());
			}
		} // while closed
		logger.info("<-- MISCounterThread Stopped --> ");
	} // run closed

	public Map<Integer, SalesEntry> listSellerMappedManager() {
		Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
		List<SalesEntry> list = salesDAO.list("manager");
		for (SalesEntry entry : list) {
			Set<Integer> sellers = listNamesUnderManager(entry.getUsername()).keySet();
			for (int seller_id : sellers) {
				map.put(seller_id, entry);
			}
		}
		return map;
	}
		public Map<Integer, SalesEntry> list(String role) {
			Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
			List<SalesEntry> list = list();
			for (SalesEntry entry : list) {
				if (entry.getRole().equalsIgnoreCase(role)) {
					map.put(entry.getId(), entry);
				}
			}
			return map;
		}
	

	public Map<Integer, String> listNamesUnderManager(String mgrId) {
		Map<Integer, String> map = new HashMap<Integer, String>();
		List<SalesEntry> list = listSellersUnderManager(mgrId);
		for (SalesEntry entry : list) {
			map.put(entry.getId(), entry.getUsername());
		}
		return map;
	}

	public List<SalesEntry> listSellersUnderManager(String mgrId) {
		List<SalesEntry> results = new ArrayList<>();
		Session session = null;
		Transaction transaction = null;

		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();

			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<SalesEntry> query = builder.createQuery(SalesEntry.class);
			Root<SalesEntry> root = query.from(SalesEntry.class);

			// Assuming masterId and role are the correct property names in the SalesEntry
			// entity
			jakarta.persistence.criteria.Predicate condition = builder.and(builder.equal(root.get("masterId"), mgrId),
					builder.equal(root.get("role"), "seller"));

			query.where(condition);
			results = session.createQuery(query).getResultList();

			transaction.commit();
			logger.info("Sellers under manager [{}]: {}", mgrId, results.size());
		} catch (RuntimeException e) {
			if (transaction != null) {
				transaction.rollback();
			}
			logger.error("Error retrieving sellers under manager: {}", e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		return results;
	}

	public Map getSmscPerformanceReport(String query) {
		// Map list = new LinkedHashMap();

		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		ReportDTO report = null;
		Map smsc_map = new HashMap();
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				int count = rs.getInt("msgCount");
				String smsc = rs.getString("smsc");
				String opr = rs.getString("oprCountry");
				String status = rs.getString("status");
				Map opr_map = null;
				if (smsc_map.containsKey(smsc)) {
					opr_map = (Map) smsc_map.get(smsc);
				} else {
					opr_map = new HashMap();
				}
				if (opr_map.containsKey(opr)) {
					report = (ReportDTO) opr_map.get(opr);
				} else {
					int network_id = 0;
					try {
						network_id = Integer.parseInt(opr);
					} catch (Exception ex) {
					}
					String country = null, operator = null, mcc = "-", mnc = "-", cc = "-";
					if (GlobalVars.NetworkEntries.containsKey(network_id)) {
						NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
						country = network.getCountry();
						operator = network.getOperator();
						mcc = network.getMcc();
						mnc = network.getMnc();
						cc = String.valueOf(network.getCc());
					} else {
						country = opr;
						operator = opr;
					}
					// ********* set values ************
					report = new ReportDTO();
					report.setSmsc(smsc);
					report.setCountry(country);
					report.setOperator(operator);
					report.setMcc(mcc);
					report.setMnc(mnc);
					report.setCc(cc);
				}
				report.setMsgCount(report.getMsgCount() + count);
				if (status != null && status.startsWith("DELIV")) {
					report.setDelivered(report.getDelivered() + count);
				}
				// logger.info(report.getSmsc()+" ->
				// "+report.getCountry()+"-"+report.getOperator()+" T: "+report.getMsgCount()+"
				// D:
				// "+report.getDelivered());
				opr_map.put(opr, report);
				smsc_map.put(smsc, opr_map);
			}
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (rs != null) {
					rs.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return smsc_map;
	}



	public List<SalesEntry> list() {
		List<SalesEntry> results = new ArrayList<>();
		logger.info("listing SalesEntry Records");
		Session session = null;
		Transaction transaction = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<SalesEntry> query = builder.createQuery(SalesEntry.class);
			Root<SalesEntry> root = query.from(SalesEntry.class);
			results = session.createQuery(query).getResultList();
			transaction.commit();
			logger.info("SalesEntry list: {}", results.size());
		} catch (RuntimeException e) {
			if (transaction != null) {
				transaction.rollback();
			}
			logger.error("Error retrieving SalesEntry entries: {}", e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return results;
	}

	public List<ReportDTO> getExecutiveDescriptiveReport(String sql) {
		ReportDTO reportDTO = null;
		List<ReportDTO> report = new ArrayList<ReportDTO>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				reportDTO = new ReportDTO();
				reportDTO.setClientName(rs.getString("username"));
				reportDTO.setDate(rs.getString("time"));
				reportDTO.setCountry(rs.getString("country"));
				reportDTO.setOperator(rs.getString("operator"));
				reportDTO.setMcc(rs.getString("mcc"));
				reportDTO.setMnc(rs.getString("mnc"));
				reportDTO.setMsgCount(rs.getInt("msgcount"));
				reportDTO.setCost(rs.getString("cost"));
				reportDTO.setTotalcost(rs.getDouble("SUM(cost)"));
				report.add(reportDTO);
			}
		} catch (Exception sqle) {
			logger.error(sql, sqle.fillInStackTrace());
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (rs != null) {
					rs.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return report;
	}

	public Map getSmscInCount(String sql) throws SQLException {
		Map map = new HashMap();
		Connection con = null;
		Statement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.createStatement();
			rs = pStmt.executeQuery(sql);
			while (rs.next()) {
				String username = rs.getString("username");
				long count = rs.getLong("msgCount");
				// logger.info("User: " + username + " Count: " + count);
				if (username != null) {
					map.put(username, count);
				}
			}
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return map;
	}

	public List<DBMessage> getDLRReport(UserEntryExt regUser, String reporttime) {
		DBMessage dbMsg = null;
		List<DBMessage> report = new ArrayList<DBMessage>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String query = null;
		if (regUser.getWebMasterEntry().isHidden()) {
			query = "select SOURCE_NO,CONCAT(left(DEST_NO,length(dest_no)-2),'**') as mobile, SUBMITTED_TIME, deliver_time, STATUS from mis_"
					+ regUser.getUserEntry().getSystemId() + " where DATE(submitted_time) = '" + reporttime
					+ "' order by submitted_time ASC";
		} else {
			query = "select SOURCE_NO,DEST_NO as mobile, SUBMITTED_TIME, deliver_time, STATUS from mis_"
					+ regUser.getUserEntry().getSystemId() + " where DATE(submitted_time) = '" + reporttime
					+ "' order by submitted_time ASC";
		}
		logger.info("SQL: " + query);
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				dbMsg = new DBMessage();
				dbMsg.setSender(rs.getString("source_no"));
				dbMsg.setDestination(rs.getString("mobile"));
				dbMsg.setSub_Time(rs.getString("submitted_time"));
				dbMsg.setDoneTime(rs.getString("deliver_time"));
				dbMsg.setStatus(rs.getString("status"));
				report.add(dbMsg);
			}
		} catch (Exception sqle) {
			if (sqle.getMessage().toLowerCase().contains("doesn't exist")) {
				// Table doesn't exist. Create New
				logger.info("mis_" + regUser.getUserEntry().getSystemId() + " Table Doesn't Exist. Creating New");
				// createMisTable(regUser.getSystemId());
			} else {
				logger.error(regUser.getUserEntry().getSystemId(), sqle.fillInStackTrace());
			}
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (rs != null) {
					rs.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return report;
	}

	public Map getMisCount(String username, String date) {
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		String query = "select date(time) as time,oprCountry ,count(*) as msgcount,cost,sum(cost)as total from smsc_in where time like '"
				+ date + "%' and username='" + username + "' and msg_id not in(select msg_id from mis_" + username
				+ ") group by date(time),oprCountry,cost order by date(time),oprCountry";
		// logger.info("SQL(1) : " + query);
		ReportDTO report = null;
		Map countmap = new TreeMap(); // to automatically sort the keys (date)
		Map oprMap = null;
		try {
			con = getConnection();
			stmt = con.createStatement();
			try {
				rs = stmt.executeQuery(query);
				while (rs.next()) {
					long msg_count = rs.getLong("msgcount");
					String date_time = (rs.getString("time")).trim();
					String oprName = rs.getString("oprCountry");
					String cost = rs.getString("cost");
					double totalcost = rs.getDouble("total");
					if (oprName == null) {
						oprName = "0";
					}
					if (countmap.containsKey(date_time)) {
						oprMap = (Map) countmap.get(date_time);
					} else {
						oprMap = new TreeMap(); // to automatically sort the keys
					}
					// logger.info("User: " + user + " oprMap: " + oprMap);
					if (oprMap.containsKey(oprName)) {
						report = (ReportDTO) oprMap.get(oprName);
						long msgcount = msg_count + report.getMsgCount();
						double newtotalcost = totalcost + report.getTotalcost();
						report.setMsgCount(msgcount);
						report.setTotalcost(newtotalcost);
					} else {
						report = new ReportDTO();
						report.setMsgCount(msg_count);
						report.setCost(cost);
						report.setTotalcost(totalcost);
					}
					// logger.info("User: " + user + " MsgCount: " + report.getMsgCount() + "
					// oprName: " + oprName + " Date: " + date_time);
					oprMap.put(oprName, report);
					countmap.put(date_time, oprMap);
				}
			} finally {
				try {
					if (rs != null) {
						rs.close();
					}
				} catch (SQLException sqle) {
				}
			}
			query = "select date(submitted_time) as time,oprCountry ,count(*) as msgcount,cost,sum(cost)as total from mis_"
					+ username + " where submitted_time like '" + date
					+ "%' group by date(submitted_time),oprCountry,cost order by date(submitted_time),oprCountry";
			// logger.info("SQL(2) : " + query);
			try {
				rs = stmt.executeQuery(query);
				while (rs.next()) {
					long msg_count = rs.getLong("msgcount");
					String date_time = (rs.getString("time")).trim();
					String oprName = rs.getString("oprCountry");
					String cost = rs.getString("cost");
					double totalcost = rs.getDouble("total");
					if (oprName == null) {
						oprName = "0";
					}
					// logger.info("User: " + user + " CountMap: " + countmap);
					if (countmap.containsKey(date_time)) {
						oprMap = (Map) countmap.get(date_time);
					} else {
						oprMap = new HashMap();
					}
					// logger.info("User: " + user + " oprMap: " + oprMap);
					if (oprMap.containsKey(oprName)) {
						report = (ReportDTO) oprMap.get(oprName);
						long msgcount = msg_count + report.getMsgCount();
						double newtotalcost = totalcost + report.getTotalcost();
						report.setMsgCount(msgcount);
						report.setTotalcost(newtotalcost);
					} else {
						report = new ReportDTO();
						report.setMsgCount(msg_count);
						report.setCost(cost);
						report.setTotalcost(totalcost);
					}
					// logger.info("User: " + user + " MsgCount: " + report.getMsgCount() + "
					// oprName: " + oprName + " Date: " + date_time);
					oprMap.put(oprName, report);
					countmap.put(date_time, oprMap);
				}
			} finally {
				try {
					if (rs != null) {
						rs.close();
					}
				} catch (SQLException sqle) {
				}
			}
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException sqle) {
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return countmap;

	}

	private String createMISCountContent(UserEntryExt userEntry) {
		// String header = "";
		// String footer = "";
		boolean walletFlag;
		if (userEntry.getBalance().getWalletFlag() != null
				&& !userEntry.getBalance().getWalletFlag().equalsIgnoreCase("no")) {
			walletFlag = true;
		} else {
			walletFlag = false;
		}
		/*
		 * try { header = MultiUtility.readContent(IConstants.webPath +
		 * "\\format\\email\\header.html"); footer =
		 * MultiUtility.readContent(IConstants.webPath +
		 * "\\format\\email\\footer.html"); } catch (IOException ex) {
		 * ex.printStackTrace(); }
		 */
		String htmlString = "<body>" + "<table >" + "<tr><td>" + "<table width='758'>" + "<tr>" + "<td>"
		// + header
				+ "</td>" + "</tr>" + "</table>" + "</td></tr>" + "<tr align='left'>"
				+ "<td style='font-family: Calibri,sans-serif;font-size: 11pt;'>"
				+ "<span style='color: #1F497D;'>Dear Valued Partner,</span>" + "<br><br>"
				+ "<span style='color: #1F497D;'>Please find Transaction Details as attachment for your account <b>"
				+ userEntry.getUserEntry().getSystemId() + "</b></span>" + "<br><br>";
		if (walletFlag) {
			htmlString += "<span style='color: #1F497D;'>Current Balance:&nbsp;<b>"
					+ userEntry.getUserEntry().getCurrency() + " " + userEntry.getBalance().getWalletAmount()
					+ "</b></span>";
		} else {
			htmlString += "<span style='color: #1F497D;'>Current Credits:&nbsp;<b>"
					+ userEntry.getBalance().getCredits() + "</b></span>";
		}
		htmlString += "</td>" + "</tr>" + "<tr align='left'>" + "<td>&nbsp;</td>" + "</tr>" + "<tr><td>"
				+ "<table width='758' align='left'>" + "<tr>"
				+ "<td style='font-family: Calibri,sans-serif;font-size: 13pt'>" + "Kind Regards<br>Support Team"
				// + footer
				+ "</td>" + "</tr>" + "</table>" + "</td></tr>" + "</table>" + "<br>" + "<br>" + "<br>" + "</body>";
		return htmlString;
	}

	private String createLowBalanceContent(UserEntryExt userDTO) {
		String htmlString = "<body>" + "<table >" + "<tr><td>" + "<table width='758'>" + "<tr>" + "<td>"
		// + header
				+ "</td>" + "</tr>" + "</table>" + "</td></tr>" + "<tr align='left'>"
				+ "<td style='font-family: Calibri,sans-serif;font-size: 11pt;'>"
				+ "<span style='color: #1F497D;'>Dear Valued Partner,</span>" + "<br><br>"
				+ "<span style='color: #1F497D;'>Please note that you have Low Balance in your account. <b>"
				+ userDTO.getUserEntry().getSystemId() + "</b></span>" + "<br><br>";
		if (userDTO.getBalance().getWalletFlag().equalsIgnoreCase("no")) {
			htmlString += "<span style='color: #1F497D;'>Current Credits:&nbsp;<b>"
					+  userDTO.getBalance().getCredits() + "</b></span>";
		} else {
			String currency = "&euro;";
			if (userDTO.getUserEntry().getCurrency() != null
					&& GlobalVars.currencies.containsKey(userDTO.getUserEntry().getCurrency().toUpperCase())) {
				currency = GlobalVars.currencies.get(userDTO.getUserEntry().getCurrency().toUpperCase());
				if (currency == null || currency.length() == 0) {
					currency = "&euro;";
				}
			}
			htmlString += "<span style='color: #1F497D;'>Current Balance:&nbsp;<b>" + currency
					+ userDTO.getBalance().getWalletAmount() + "</b></span>";
		}
		htmlString += "<br><br><span style='color: #1F497D;'>Please Recharge Soon. <b></b></span>";
		htmlString += "<br><br><span style='color: #1F497D;'>Thank You for your business. <b></b></span>" + "<br><br>";
		htmlString += "</td>" + "</tr>" + "<tr align='left'>" + "<td>&nbsp;</td>" + "</tr>" + "<tr><td>"
				+ "<table width='758' align='left'>" + "<tr>"
				+ "<td style='font-family: Calibri,sans-serif;font-size: 13pt'>" + "Kind Regards<br>Support Team"
				// + footer
				+ "</td>" + "</tr>" + "</table>" + "</td></tr>" + "</table>" + "<br>" + "<br>" + "<br>" + "</body>";
		return htmlString;
	}

	private void send(String email, String from, String content, String subject, Object[] attachment, boolean IsCC)
			throws AddressException, MessagingException, Exception {
		String host = IConstants.mailHost;
		String to = email;
		final String pass = IConstants.mailPassword;
		final String mailAuthUser = IConstants.mailId;
		// String messagetext = "";
		// boolean sessionDebug = false;
		Properties props = new Properties();
		// props.put("mail.smtp.user", mailAuthUser);
		props.put("mail.smtp.host", host);
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", IConstants.smtpPort + "");
		// props.put("mail.smtp.auth", "true");
		// props.put("mail.smtp.debug", "true");
		jakarta.mail.Session mailSession = 	jakarta.mail.Session.getDefaultInstance(props);
		/*
		 * Session mailSession = Session.getInstance(props, new Authenticator() {
		 * 
		 * @Override protected PasswordAuthentication getPasswordAuthentication() {
		 * return new PasswordAuthentication(mailAuthUser, pass); } });
		 * 
		 * mailSession.setDebug(sessionDebug);
		 */
		Message message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(from, from));
		InternetAddress[] address;
		if (to.contains(",")) {
			StringTokenizer emails = new StringTokenizer(to, ",");
			address = new InternetAddress[emails.countTokens()];
			int i = 0;
			while (emails.hasMoreTokens()) {
				String emailID = emails.nextToken();
				if (emailID.contains("@")) {
					address[i] = new InternetAddress(emailID);
					i++;
				}
			}
		} else {
			address = new InternetAddress[1];
			address[0] = new InternetAddress(to);
		}
		message.setRecipients(Message.RecipientType.TO, address);
		if (IsCC) {
			InternetAddress[] ccaddress = new InternetAddress[IConstants.CC_EMAIL.length];
			for (String cc : IConstants.CC_EMAIL) {
				ccaddress[0] = new InternetAddress(cc);
			}
			message.setRecipients(Message.RecipientType.CC, ccaddress);
		}
		message.setSubject(subject);
		message.setSentDate(new Date());
		Multipart multipart = new MimeMultipart();
		try {
			// first part (the html) & Attachment
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(content, "text/html");
			multipart.addBodyPart(messageBodyPart);
			// Attachment
			if (attachment != null) {
				for (int i = 0; i < attachment.length; i++) {
					messageBodyPart = new MimeBodyPart();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					Workbook workbook = (Workbook) attachment[i];
					workbook.write(out);
					DataSource source = new ByteArrayDataSource(out.toByteArray(), "application/octet-stream");
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName("report.xlsx");
					multipart.addBodyPart(messageBodyPart);
				}
			}
			// second part (the image)
			File fi = new File(IConstants.FORMAT_DIR + "images//header.jpg");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<headerimg>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				logger.info("Header Image not exists: " + fi.getName());
			}
			// third part (the image)
			fi = new File(IConstants.FORMAT_DIR + "images//footer.jpg");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<footerimg>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				logger.info("Footer Image not exists: " + fi.getName());
			}
			fi = new File(IConstants.FORMAT_DIR + "images//footer_2.png");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<footer2img>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				logger.info("Footer_2 Image not exists: " + fi.getName());
			}
			fi = new File(IConstants.FORMAT_DIR + "images//lebanon.png");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<lebanonimg>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				logger.info("Lebanon Image not exists: " + fi.getName());
			}
			fi = new File(IConstants.FORMAT_DIR + "images//uae.png");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<uaeimg>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				logger.info("UAE Image not exists: " + fi.getName());
			}
			fi = new File(IConstants.FORMAT_DIR + "images//contact.jpg");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<contactimg>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				logger.info("Contact Image not exists: " + fi.getName());
			}
		} catch (Exception ex) {
			logger.info("Error in Adding Multipart Data into Email: " + ex);
		}
		message.setContent(multipart);
		Transport.send(message, mailAuthUser, pass);
	}

	private String createPerformaceContent(String report_time) {
		String htmlString = "<body>" + "<table >" + "<tr><td>" + "<table width='758'>" + "<tr>" + "<td>"
		// + header
				+ "</td>" + "</tr>" + "</table>" + "</td></tr>" + "<tr align='left'>"
				+ "<td style='font-family: Calibri,sans-serif;font-size: 11pt;'>"
				+ "<span style='color: #1F497D;'>Dear Team,</span>" + "<br><br>"
				+ "<span style='color: #1F497D;'>Please find Performance Report as attachment for <b>" + report_time
				+ "</b></span>" + "<br><br>" + "</td>" + "</tr>" + "<tr align='left'>" + "<td>&nbsp;</td>" + "</tr>"
				+ "<tr><td>" + "<table width='758' align='left'>" + "<tr>"
				+ "<td style='font-family: Calibri,sans-serif;font-size: 13pt'>" + "Kind Regards<br>Support Team"
				// + footer
				+ "</td>" + "</tr>" + "</table>" + "</td></tr>" + "</table>" + "<br>" + "<br>" + "<br>" + "</body>";
		return htmlString;
	}

	private String createExecDescriptiveContent(String executive) {
		String htmlString = "<body>" + "<table >" + "<tr><td>" + "<table width='758'>" + "<tr>" + "<td>"
		// + header
				+ "</td>" + "</tr>" + "</table>" + "</td></tr>" + "<tr align='left'>"
				+ "<td style='font-family: Calibri,sans-serif;font-size: 11pt;'>"
				+ "<span style='color: #1F497D;'>Dear " + executive + ",</span>" + "<br><br>"
				+ "<span style='color: #1F497D;'>Please find Userwise Statistics Report as attachment for Current Month <b>"
				+ "</b></span>" + "<br><br>" + "</td>" + "</tr>" + "<tr align='left'>" + "<td>&nbsp;</td>" + "</tr>"
				+ "<tr><td>" + "<table width='758' align='left'>" + "<tr>"
				+ "<td style='font-family: Calibri,sans-serif;font-size: 13pt'>" + "Kind Regards<br>Support Team"
				// + footer
				+ "</td>" + "</tr>" + "</table>" + "</td></tr>" + "</table>" + "<br>" + "<br>" + "<br>" + "</body>";
		return htmlString;
	}

	private Workbook getExecDescriptiveWorkbook(List<ReportDTO> list, String executive) {
		logger.info(executive + " <-- Creating Executive Descriptive WorkBook --> ");
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		int records_per_sheet = 500000;
		int sheet_number = 0;
		Sheet sheet = null;
		Row row = null;
		XSSFFont headerFont = (XSSFFont) workbook.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setColor(new XSSFColor((IndexedColorMap) Color.BLACK));
		XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setFillForegroundColor(new XSSFColor((IndexedColorMap) Color.GRAY));
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); 

		headerStyle.setAlignment(HorizontalAlignment.CENTER); 

		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);

		headerStyle.setBottomBorderColor(new XSSFColor((IndexedColorMap) Color.WHITE));
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		XSSFColor whiteColor = new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null);
		headerStyle.setTopBorderColor(whiteColor);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setLeftBorderColor(whiteColor);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setRightBorderColor(whiteColor);
		XSSFFont rowFont = (XSSFFont) workbook.createFont();
		rowFont.setFontName("Arial");
		rowFont.setFontHeightInPoints((short) 9);
		rowFont.setColor(whiteColor);
		XSSFCellStyle rowStyle = (XSSFCellStyle) workbook.createCellStyle();
		rowStyle.setFont(rowFont);
		byte[] rgbLightGray = {(byte) Color.LIGHT_GRAY.getRed(), (byte) Color.LIGHT_GRAY.getGreen(), (byte) Color.LIGHT_GRAY.getBlue()};
		XSSFColor lightGrayColor = new XSSFColor(rgbLightGray, null); 

		rowStyle.setFillForegroundColor(lightGrayColor.getIndex()); 		rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		rowStyle.setAlignment(HorizontalAlignment.LEFT);
		rowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBottomBorderColor(whiteColor);
		rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setTopBorderColor(whiteColor);
		rowStyle.setBorderLeft(BorderStyle.THIN);
		rowStyle.setBorderLeft(BorderStyle.THIN);
		rowStyle.setLeftBorderColor(whiteColor);
		rowStyle.setBorderRight(BorderStyle.THIN);
		rowStyle.setBorderRight(BorderStyle.THIN);
		rowStyle.setRightBorderColor(whiteColor);
		String[] headers = { "Username", "Time", "Country", "Operator", "MCC", "MNC", "Cost", "Count", "TotalCost" };
		while (!list.isEmpty()) {
			int row_number = 0;
			long total = 0;
			double total_cost = 0;
			sheet = workbook.createSheet("Sheet(" + sheet_number + ")");
			sheet.setDefaultColumnWidth(14);
			logger.info(executive + " Creating Sheet: " + sheet_number);
			while (!list.isEmpty()) {
				row = sheet.createRow(row_number);
				if (row_number == 0) {
					int cell_number = 0;
					for (String header : headers) {
						Cell cell = row.createCell(cell_number);
						cell.setCellValue(header);
						cell.setCellStyle(headerStyle);
						cell_number++;
					}
				} else {
					ReportDTO reportDTO = list.remove(0);
					Cell cell = row.createCell(0);
					cell.setCellValue(reportDTO.getClientName());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(1);
					cell.setCellValue(reportDTO.getDate());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(2);
					cell.setCellValue(reportDTO.getCountry());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(3);
					cell.setCellValue(reportDTO.getOperator());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(4);
					cell.setCellValue(reportDTO.getMcc());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(5);
					cell.setCellValue(reportDTO.getMnc());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(6);
					cell.setCellValue(reportDTO.getCost());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(7);
					cell.setCellValue(reportDTO.getMsgCount());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(8);
					cell.setCellValue(reportDTO.getTotalcost());
					cell.setCellStyle(rowStyle);
					total += reportDTO.getMsgCount();
					total_cost += reportDTO.getTotalcost();
				}
				if (++row_number > records_per_sheet) {
					logger.info(executive + " Sheet Created: " + sheet_number);
					break;
				}
			}
			// ***** Summary For Current Sheet ************
			row = sheet.createRow(row_number);
			Cell cell = row.createCell(6);
			cell.setCellValue("Grand Total");
			cell.setCellStyle(headerStyle);
			cell = row.createCell(7);
			cell.setCellValue(total);
			cell.setCellStyle(headerStyle);
			cell = row.createCell(8);
			cell.setCellValue(total_cost);
			cell.setCellStyle(headerStyle);
			// ***** End Summary For Current Sheet ************
			sheet_number++;
		}
		logger.info(executive + "<--- Workbook Created ----> ");
		return workbook;
	}

	private Workbook getSmscWorkBook(List reportList) {
		logger.info("<-- Creating Smsc Performance WorkBook --> ");
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		int records_per_sheet = 500000;
		int sheet_number = 0;
		Sheet sheet = null;
		Row row = null;
		XSSFFont headerFont = (XSSFFont) workbook.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setColor(new XSSFColor((IndexedColorMap) Color.BLACK));
		XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		 XSSFWorkbook workbook1 = new XSSFWorkbook();
	        XSSFCellStyle headerStyle1 = workbook1.createCellStyle();
	        
	        XSSFColor grayColor = new XSSFColor(Color.GRAY, workbook1.getStylesSource().getIndexedColors()); // Create the gray color
	        
	        headerStyle1.setFillForegroundColor(grayColor); // Set the fill foreground color
	        headerStyle1.setFillPattern(FillPatternType.SOLID_FOREGROUND); // Set the fill pattern
	        headerStyle1.setAlignment(HorizontalAlignment.CENTER); // Set horizontal alignment
	        headerStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle1.setBorderBottom(BorderStyle.THIN);
		headerStyle1.setBorderBottom(BorderStyle.THIN);
		headerStyle1.setBottomBorderColor(IndexedColors.WHITE.getIndex());
		headerStyle1.setBorderTop(BorderStyle.THIN);
		headerStyle1.setBorderTop(BorderStyle.THIN);
		headerStyle1.setTopBorderColor(IndexedColors.WHITE.getIndex());
		headerStyle1.setBorderLeft(BorderStyle.THIN);
		headerStyle1.setBorderLeft(BorderStyle.THIN);
		headerStyle1.setLeftBorderColor(IndexedColors.WHITE.getIndex());
		headerStyle1.setBorderRight(BorderStyle.THIN);
		headerStyle1.setBorderRight(BorderStyle.THIN);
		headerStyle1.setRightBorderColor(IndexedColors.WHITE.getIndex());
		XSSFFont rowFont = (XSSFFont) workbook1.createFont();
		rowFont.setFontName("Arial");
		rowFont.setFontHeightInPoints((short) 9);
		rowFont.setColor(IndexedColors.WHITE.getIndex());
		XSSFCellStyle rowStyle = (XSSFCellStyle) workbook1.createCellStyle();
		rowStyle.setFont(rowFont);
		  Color awtColor = Color.LIGHT_GRAY;
	        XSSFColor xssfColor = new XSSFColor(new byte[]{(byte) awtColor.getRed(), (byte) awtColor.getGreen(), (byte) awtColor.getBlue()}, null); // null for the default IndexedColorMap

		  rowStyle.setFillForegroundColor(xssfColor.getIndex()); // Set foreground color
	        rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); // Set fill pattern
	        rowStyle.setAlignment(HorizontalAlignment.LEFT); // Set horizontal alignment
	        rowStyle.setVerticalAlignment(VerticalAlignment.CENTER); 
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBottomBorderColor(new XSSFColor());
		rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setTopBorderColor(IndexedColors.WHITE.getIndex());
		rowStyle.setBorderLeft(BorderStyle.THIN);
		rowStyle.setBorderLeft(BorderStyle.THIN);
		rowStyle.setLeftBorderColor(IndexedColors.WHITE.getIndex());
		rowStyle.setBorderRight(BorderStyle.THIN);
		rowStyle.setBorderRight(BorderStyle.THIN);
		rowStyle.setRightBorderColor(IndexedColors.WHITE.getIndex());
		String[] headers = { "Route", "Country", "CountryCode", "Operator", "MCC", "MNC", "Total", "Delivered",
				"Delivery(%)" };
		while (!reportList.isEmpty()) {
			int row_number = 0;
			long total = 0, delivered = 0;
			double delivery_percent = 0;
			sheet = workbook1.createSheet("Sheet(" + sheet_number + ")");
			sheet.setDefaultColumnWidth(14);
			logger.info("Creating Sheet: " + sheet_number);
			while (!reportList.isEmpty()) {
				row = sheet.createRow(row_number);
				if (row_number == 0) {
					int cell_number = 0;
					for (String header : headers) {
						Cell cell = row.createCell(cell_number);
						cell.setCellValue(header);
						cell.setCellStyle(headerStyle1);
						cell_number++;
					}
				} else {
					ReportDTO reportDTO = (ReportDTO) reportList.remove(0);
					total += reportDTO.getMsgCount();
					delivered += reportDTO.getDelivered();
					Cell cell = row.createCell(0);
					cell.setCellValue(reportDTO.getSmsc());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(1);
					cell.setCellValue(reportDTO.getCountry());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(2);
					cell.setCellValue(reportDTO.getCc());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(3);
					cell.setCellValue(reportDTO.getOperator());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(4);
					cell.setCellValue(reportDTO.getMcc());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(5);
					cell.setCellValue(reportDTO.getMnc());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(6);
					cell.setCellValue(reportDTO.getMsgCount());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(7);
					cell.setCellValue(reportDTO.getDelivered());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(8);
					cell.setCellValue(reportDTO.getDlrpercent());
					cell.setCellStyle(rowStyle);
				}
				if (++row_number > records_per_sheet) {
					logger.info("Sheet Created: " + sheet_number);
					break;
				}
			}
			// ***** Summary For Current Sheet ************
			delivery_percent = (delivered * 100) / total;
			row = sheet.createRow(row_number);
			Cell cell = row.createCell(5);
			cell.setCellValue("Grand Total");
			cell.setCellStyle(headerStyle1);
			cell = row.createCell(6);
			cell.setCellValue(total);
			cell.setCellStyle(headerStyle1);
			cell = row.createCell(7);
			cell.setCellValue(delivered);
			cell.setCellStyle(headerStyle1);
			cell = row.createCell(8);
			cell.setCellValue(delivery_percent);
			cell.setCellStyle(headerStyle1);
			// ***** End Summary For Current Sheet ************
			sheet_number++;
		}
		logger.info("<--- Workbook Created ----> ");
		return workbook1;
	}

	private Workbook getWorkBook(Map report_map, UserEntryExt userDTO) {
		logger.info("<-- " + userDTO.getUserEntry().getSystemId() + " Creating MIS WorkBook --> ");
		boolean walletFlag;
		if (userDTO.getBalance().getWalletFlag() != null
				&& !userDTO.getBalance().getWalletFlag().equalsIgnoreCase("no")) {
			walletFlag = true;
		} else {
			walletFlag = false;
		}
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		int sheet_number = 0;
		Sheet sheet = null;
		Row row = null;
		XSSFFont headerFont = (XSSFFont) workbook.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		Color awtColor = Color.decode("#191970");
		XSSFWorkbook workbook1 = new XSSFWorkbook();
		XSSFColor xssfColor = new XSSFColor(new java.awt.Color(awtColor.getRGB()), null);
		headerStyle.setFillForegroundColor(xssfColor);
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); 
		headerStyle.setAlignment(HorizontalAlignment.CENTER); 
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER); 
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBottomBorderColor(IndexedColors.WHITE.getIndex());
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setTopBorderColor(IndexedColors.WHITE.getIndex());
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setLeftBorderColor(IndexedColors.WHITE.getIndex());
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		// Directly using the index of the white color
		headerStyle.setRightBorderColor(IndexedColors.WHITE.getIndex());

		// ----------- Data Row Style ------------------------------
		XSSFFont rowFont = (XSSFFont) workbook1.createFont();
		rowFont.setFontName("Arial");
		rowFont.setFontHeightInPoints((short) 9);
		rowFont.setColor(IndexedColors.WHITE.getIndex());

		XSSFCellStyle rowStyle = (XSSFCellStyle) workbook1.createCellStyle();
		rowStyle.setFont(rowFont);
		Color awtColor1 = Color.decode("#6495ED");
		XSSFWorkbook workbook11 = new XSSFWorkbook(); // Example of creating a workbook if you don't already have one
		XSSFColor xssfColor1 = new XSSFColor(awtColor1, workbook11.getStylesSource().getIndexedColors());

		XSSFColor xssfColor11 = new XSSFColor(awtColor1, workbook11.getStylesSource().getIndexedColors());

		// Now, apply the color to the cell style
		rowStyle.setFillForegroundColor(xssfColor11);
		rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); 
		rowStyle.setAlignment(HorizontalAlignment.LEFT); 
		rowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBottomBorderColor(IndexedColors.WHITE.getIndex());
		rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN); // Instead of using (short) 1

		rowStyle.setBorderLeft(BorderStyle.THIN);
		rowStyle.setBorderLeft(BorderStyle.THIN);
		// Directly use IndexedColors for common colors like white
		rowStyle.setLeftBorderColor(IndexedColors.WHITE.getIndex());

		rowStyle.setBorderRight(BorderStyle.THIN);
		rowStyle.setBorderRight(BorderStyle.THIN);
		// Set the right border color using IndexedColors
		rowStyle.setRightBorderColor(IndexedColors.WHITE.getIndex());

		// ----------- Grand Total Row Style -----------------
		XSSFFont summaryRowFont = (XSSFFont) workbook11.createFont();
		summaryRowFont.setFontName("Arial");
		summaryRowFont.setFontHeightInPoints((short) 10);
		// Using IndexedColors to set the font color to white
		summaryRowFont.setColor(IndexedColors.WHITE.getIndex());

		XSSFCellStyle summaryRowStyle = (XSSFCellStyle) workbook11.createCellStyle();
		summaryRowStyle.setFont(summaryRowFont);
		Color midnightBlue = Color.decode("#191970");

		XSSFColor xssfMidnightBlue = new XSSFColor(midnightBlue, null); 
		summaryRowStyle.setFillForegroundColor(xssfMidnightBlue);
		summaryRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		XSSFColor whiteColor = new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null);
		summaryRowStyle.setAlignment(HorizontalAlignment.LEFT);
		summaryRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		summaryRowStyle.setBorderBottom(BorderStyle.THIN);
		summaryRowStyle.setBorderTop(BorderStyle.THIN);
		summaryRowStyle.setBorderLeft(BorderStyle.THIN);
		summaryRowStyle.setBorderRight(BorderStyle.THIN);
		summaryRowStyle.setBottomBorderColor(whiteColor);
		summaryRowStyle.setTopBorderColor(whiteColor);
		summaryRowStyle.setLeftBorderColor(whiteColor);
		summaryRowStyle.setRightBorderColor(whiteColor);
		// ---------------------------------------------------
		String[] headers;
		if (walletFlag) {
			headers = new String[] { "Date", "Country", "Operator", "MCC", "MNC", "Count", "Cost", "Total Cost" };
		} else {
			headers = new String[] { "Date", "Country", "Operator", "MCC", "MNC", "Count" };
		}
		// --------------------
		int row_number = 0;
		sheet = workbook11.createSheet("Sheet(" + sheet_number + ")");
		sheet.setDefaultColumnWidth(14);
		logger.info("Creating Sheet: " + sheet_number);
		if (row_number == 0) {
			row = sheet.createRow(row_number);
			int cell_number = 0;
			for (String header : headers) {
				Cell cell = row.createCell(cell_number);
				cell.setCellValue(header);
				cell.setCellStyle(headerStyle);
				cell_number++;
			}
			row_number++;
		}
		Iterator timeItr = report_map.keySet().iterator();
		// DecimalFormat df = new DecimalFormat("0.00000");
		long total = 0;
		double totalcost = 0;
		while (timeItr.hasNext()) {
			String repTime = (String) timeItr.next();
			// logger.info("Report Time ==> " + repTime);
			Map opr_map = (Map) report_map.get(repTime);
			Iterator temp_itr = opr_map.keySet().iterator();
			List report_list = new ArrayList();
			while (temp_itr.hasNext()) {
				String prefix = (String) temp_itr.next();
				ReportDTO report = (ReportDTO) opr_map.get(prefix);
				int network_id = 0;
				try {
					network_id = Integer.parseInt(prefix);
				} catch (Exception ex) {
				}
				if (GlobalVars.NetworkEntries.containsKey(network_id)) {
					NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
					report.setCountry(network.getCountry());
					report.setOperator(network.getOperator());
					report.setMcc(network.getMcc());
					report.setMnc(network.getMnc());
				} else {
					if (prefix == null || prefix.length() == 0) {
						prefix = "-";
					}
					report.setCountry(prefix);
					report.setOperator(prefix);
					report.setMcc("-");
					report.setMnc("-");
				}
				report_list.add(report);
			}
			report_list.sort(Comparator.comparing(ReportDTO::getCountry, CASE_INSENSITIVE_ORDER)
					.thenComparing(ReportDTO::getOperator, CASE_INSENSITIVE_ORDER));
			int i = 0;
			// logger.info("Report List ==> " + report_list.size());
			while (!report_list.isEmpty()) {
				row = sheet.createRow(row_number);
				ReportDTO report = (ReportDTO) report_list.remove(0);
				total = total + report.getMsgCount();
				totalcost = totalcost + report.getTotalcost();
				if (i > 0) {
					repTime = "-";
				}
				// logger.info("Row Number ===> " + row_number + " " + report.getCountry());
				Cell cell = row.createCell(0);
				cell.setCellValue(repTime);
				cell.setCellStyle(rowStyle);
				cell = row.createCell(1);
				cell.setCellValue(report.getCountry());
				cell.setCellStyle(rowStyle);
				cell = row.createCell(2);
				cell.setCellValue(report.getOperator());
				cell.setCellStyle(rowStyle);
				cell = row.createCell(3);
				cell.setCellValue(report.getMcc());
				cell.setCellStyle(rowStyle);
				cell = row.createCell(4);
				cell.setCellValue(report.getMnc());
				cell.setCellStyle(rowStyle);
				cell = row.createCell(5);
				cell.setCellValue(report.getMsgCount());
				cell.setCellStyle(rowStyle);
				if (walletFlag) {
					cell = row.createCell(6);
					cell.setCellValue(report.getCost());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(7);
					cell.setCellValue(new DecimalFormat("0.00000").format(report.getTotalcost()));
					cell.setCellStyle(rowStyle);
				}
				i++;
				row_number++;
			}
		}
		row = sheet.createRow(row_number);
		Cell cell = row.createCell(4);
		cell.setCellValue("Grand Total");
		cell.setCellStyle(summaryRowStyle);
		cell = row.createCell(5);
		cell.setCellValue(total);
		cell.setCellStyle(summaryRowStyle);
		if (walletFlag) {
			cell = row.createCell(6);
			cell.setCellValue("");
			cell.setCellStyle(summaryRowStyle);
			cell = row.createCell(7);
			cell.setCellValue(totalcost);
			cell.setCellStyle(summaryRowStyle);
		}
		logger.info("<-- " + userDTO.getUserEntry().getSystemId() + " MIS WorkBook Created --> ");
		return workbook11;
	}

	private List sortList(List list) {
		Comparator<ReportDTO> comparator = Comparator.comparing(ReportDTO::getSmsc).thenComparing(ReportDTO::getCountry)
				.thenComparing(ReportDTO::getOperator);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	public void stop() {
		logger.info("<-- MISCounterThread Stopping --> ");
		isStop = true;
		thread.interrupt();
	}

}
