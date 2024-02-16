package com.hti.smpp.common.service.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;

import com.hti.smpp.common.database.EntryNotFoundException;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.CustomReportDTO;
import com.hti.smpp.common.request.SubmissionReportRequest;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.SubmissionReportService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class SubmissionReportServiceImpl implements SubmissionReportService {

	Logger logger = LoggerFactory.getLogger(SubmissionReportServiceImpl.class);

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private UserDAService userService;
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;

	Locale locale = null;
	@Autowired
	private DataSource dataSource;

	@Autowired
	private SalesRepository salesRepository;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	Set<String> paramSet = new HashSet<String>();
	boolean noCriteria = true;
	List<String> sql_list = new ArrayList<String>();
	Map<String, String> mis_sql_list = new HashMap<String, String>();

	@Override
	public ResponseEntity<?> execute(String username, SubmissionReportRequest customReportForm,
			HttpServletResponse response, String lang) {

		String target = IConstants.SUCCESS_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
		}

		boolean mis = true;
		boolean mis_log = false;
		String mis_sql = "";

		logger.info(messageResourceBundle.getLogMessage("submission.report.requested.message"), username);

		try {
			locale = Customlocale.getLocaleByLanguage(lang);

			// CustomReportForm customReportForm = (CustomReportForm) actionForm;
			CustomReportDTO customReportDTO = new CustomReportDTO();
			// BeanUtils.copyProperties(customReportDTO, customReportForm);
			org.springframework.beans.BeanUtils.copyProperties(customReportForm, customReportDTO);
			// String username = customReportDTO.getClientId();
			String smsc = customReportDTO.getSmscName();
			String country = customReportDTO.getCountry();
			String sender = customReportDTO.getSenderId();
			String status = customReportDTO.getMessageStatus();
			String reportType = customReportDTO.getReportType();
			logger.info(messageResourceBundle.getLogMessage("report.info.message"), username, reportType, username, smsc, country, sender, status);

			if (status != null && !status.equals("%") && !status.equalsIgnoreCase("T")) {
				mis = false;
			}
			if (mis) {
				logger.info(messageResourceBundle.getLogMessage("checking.mis.records.message"), username);

				mis_sql = "";
				String mis_select = "select count(msg_id) as count,date(submitted_time) as timing";
				String mis_conditionStr = " where ";
				String mis_condition_opt = "";
				String mis_groupbyStr = " group by date(submitted_time)";
				String mis_orderbyStr = " order by timing";
				if (reportType.equalsIgnoreCase("Daily")) {
					mis_conditionStr += "date(submitted_time)='" + customReportDTO.getSyear() + "-"
							+ customReportDTO.getSmonth() + "-" + customReportDTO.getSday() + "'";
					try {
						if (daysDifference(customReportDTO.getSyear() + "-" + customReportDTO.getSmonth() + "-"
								+ customReportDTO.getSday()) > 2) {
							mis_log = true;
							logger.info(messageResourceBundle.getLogMessage("mis.log.criteria.message"));

						} else {
							logger.info(messageResourceBundle.getLogMessage("not.mis.log.criteria.message"));

						}
					} catch (Exception pex) {
						logger.info(messageResourceBundle.getLogMessage("days.difference.check.message"), pex);

						mis_log = true;
					}
				} else if (reportType.equalsIgnoreCase("Monthly")) {
					mis_conditionStr += "submitted_time like '" + customReportDTO.getSyear() + "-"
							+ customReportDTO.getSmonth() + "%'";
					mis_log = true;
				} else {
					mis_conditionStr += "submitted_time between '" + customReportDTO.getSyear() + "-"
							+ customReportDTO.getSmonth() + "-" + customReportDTO.getSday() + " "
							+ customReportDTO.getShour() + ":" + customReportDTO.getSmin() + ":01'";
					mis_conditionStr += " and '" + customReportDTO.getEyear() + "-" + customReportDTO.getEmonth() + "-"
							+ customReportDTO.getEday() + " " + customReportDTO.getEhour() + ":"
							+ customReportDTO.getEmin() + ":59'";
					try {
						if (daysDifference(customReportDTO.getSyear() + "-" + customReportDTO.getSmonth() + "-"
								+ customReportDTO.getSday()) > 2) {
							mis_log = true;
							logger.info(messageResourceBundle.getLogMessage("mis.log.criteria.message"));
						} else {
							logger.info(messageResourceBundle.getLogMessage("not.mis.log.criteria.message"));
						}
					} catch (Exception pex) {
						logger.info(messageResourceBundle.getLogMessage("days.difference.check.message"), pex);
						mis_log = true;
					}
				}
				// Query for Processed
				if (smsc != null) {
					mis_select += ",Route_to_smsc as smsc";
					if (!smsc.equals("%")) {
						mis_conditionStr += " and Route_to_smsc='" + smsc + "'";
					}
					mis_groupbyStr += ",Route_to_smsc";
					mis_orderbyStr += ",Route_to_smsc";
				}
				if (status != null) {
					mis_select += ",status as s_flag";
					mis_groupbyStr += ",s_flag";
				}
				if (country != null) {
					mis_select += ",oprCountry";
					if (!country.equals("%")) {
						mis_condition_opt += " and dest_no like '" + country + "%' ";
					}
					mis_groupbyStr += ",oprCountry";
				}
				if (sender != null) {
					mis_select += ",source_no";
					if (sender.length() > 0) {
						mis_condition_opt += " and source_no like '" + sender + "%'";
					}
					mis_groupbyStr += ",source_no";
				}
				if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")) {
					if (username.equals("%")) {
						logger.info(messageResourceBundle.getLogMessage("checking.mis.records.all.users.message"), username);

						// Check Users From mis_table
						String check_user_sql = "select distinct(username) from mis_table " + mis_conditionStr;
						List<String> recent_users = getSubmissionUsers(check_user_sql);
						Set<String> users = new HashSet<String>(recent_users);
						if (mis_log) {
							check_user_sql = "select distinct(username) from mis_table_log " + mis_conditionStr;
							List<String> log_users = getSubmissionUsers(check_user_sql);
							users.addAll(log_users);
						}
						if (!users.isEmpty()) {
							for (String mis_user : users) {
								// Query from User's relative mis
								mis_sql = mis_select + " from mis_" + mis_user + mis_conditionStr + mis_condition_opt
										+ mis_groupbyStr + mis_orderbyStr;
								// logger.info(system_id+" mis_sql(2) ===> " + mis_sql);
								mis_sql_list.put(mis_user, mis_sql);
							}
						}
					} else {
						logger.info(messageResourceBundle.getLogMessage("checking.mis.records.for.user.message"),username ,username);

						// Query from User's relative mis
						mis_sql = mis_select + " from mis_" + username + mis_conditionStr + mis_condition_opt
								+ mis_groupbyStr + mis_orderbyStr;
						mis_sql_list.put(username, mis_sql);
					}
				} else if (user.getRole().equalsIgnoreCase("admin") || user.getRole().equalsIgnoreCase("manager")
						|| user.getRole().equalsIgnoreCase("seller")) {
					if (username.equals("%")) {
						logger.info(messageResourceBundle.getLogMessage("checking.mis.records.all.users.message"), username);
						// Get Users List From Usermaster Under this admin
						List<String> users = null;
						if (user.getRole().equalsIgnoreCase("admin")) {
							users = new ArrayList<String>(
									userService.listUsersUnderMaster(user.getSystemId()).values());
							users.add(user.getSystemId());
							Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
									.get("secondaryMaster").equal(user.getSystemId());
							for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
								UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
								users.add(userEntry.getSystemId());
							}
						} else if (user.getRole().equalsIgnoreCase("seller")) {
							users = new ArrayList<String>(userService.listUsersUnderSeller(user.getId()).values());
						} else if (user.getRole().equalsIgnoreCase("manager")) {
							// SalesDAService salesService = new SalesDAServiceImpl();
							users = new ArrayList<String>(listUsernamesUnderManager(user.getSystemId()).values());
						}
						if (users != null) {
							while (!users.isEmpty()) {
								String underuserinfo = (String) users.remove(0);
								// Query from User's relative mis
								mis_sql = mis_select + " from mis_" + underuserinfo + mis_conditionStr
										+ mis_condition_opt + mis_groupbyStr + mis_orderbyStr;
								mis_sql_list.put(underuserinfo, mis_sql);
							}
						} else {
							logger.error(messageResourceBundle.getLogMessage("no.users.found.error"), user.getSystemId(), user.getRole());

							throw new EntryNotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NO_USERS_FOUND_UNDER_MESSAGE, new Object[] {user.getSystemId(),user.getRole()})) ;

						}
					} else {
						logger.info(messageResourceBundle.getLogMessage("checking.mis.records.for.user.message"),user.getSystemId() ,username);
						// Query from User's relative mis
						mis_sql = mis_select + " from mis_" + username + mis_conditionStr + mis_condition_opt
								+ mis_groupbyStr + mis_orderbyStr;
						mis_sql_list.put(username, mis_sql);
					}
				} else {
					logger.info(messageResourceBundle.getLogMessage("checking.mis.records.for.user.message"),user.getSystemId() ,username);
					// Query from User's relative mis
					mis_sql = mis_select + " from mis_" + user.getSystemId() + mis_conditionStr + mis_condition_opt
							+ mis_groupbyStr + mis_orderbyStr;
					mis_sql_list.put(user.getSystemId(), mis_sql);
					logger.info(user.getSystemId() + ": " + mis_sql);
				}
			} else {
				logger.info(messageResourceBundle.getLogMessage("no.checking.from.mis.table.message"));

			}
			// ************** Preparing SQL For Unprocessed *******************************
			String selectStr = "select count(msg_id) as count,date(time) as timing";
			String conditionStr = " where ";
			String groupbyStr = " group by date(time)";
			String orderbyStr = " order by timing";
			if (status != null) {
				selectStr += ",s_flag";
				if (!status.equals("%")) {
					conditionStr += "s_flag = '" + status + "' and ";
				}
				groupbyStr += ",s_flag";
				paramSet.add("sflag");
				// request.setAttribute("sflag", "yes");
				noCriteria = false;
			}
			if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")) {
				if (username != null) {
					selectStr += ",username";
					if (!username.equals("%")) {
						conditionStr += "username='" + username + "' and ";
					}
					groupbyStr += ",username";
					orderbyStr += ",username";
					paramSet.add("username");
					// request.setAttribute("user", "yes");
					noCriteria = false;
				}
			} else if (user.getRole().equalsIgnoreCase("admin") || user.getRole().equalsIgnoreCase("manager")
					|| user.getRole().equalsIgnoreCase("seller")) {
				selectStr += ",username";
				if (username.equals("%")) {
					List<String> users = null;
					if (user.getRole().equalsIgnoreCase("admin")) {
						users = new ArrayList<String>(userService.listUsersUnderMaster(user.getSystemId()).values());
						users.add(user.getSystemId());
					} else if (user.getRole().equalsIgnoreCase("seller")) {
						users = new ArrayList<String>(userService.listUsersUnderSeller(user.getId()).values());
					} else if (user.getRole().equalsIgnoreCase("manager")) {
						// SalesDAService salesService = new SalesDAServiceImpl();
						users = new ArrayList<String>(listUsernamesUnderManager(user.getSystemId()).values());
					}
					if (users != null) {
						conditionStr += "username in('" + String.join("','", users) + "') and ";
					} else {
						logger.error(messageResourceBundle.getLogMessage("no.users.found.message"), user.getSystemId(), user.getRole());

						throw new EntryNotFoundException(
								"No Users Found Under " + user.getSystemId() + " [" + user.getRole() + "]");
					}
				} else {
					conditionStr += "username='" + username + "' and ";
				}
				groupbyStr += ",username";
				orderbyStr += ",username";
				paramSet.add("username");
				// request.setAttribute("user", "yes");
				noCriteria = false;
			} else {
				conditionStr += "username='" + user.getSystemId() + "' and ";
			}
			if (smsc != null) {
				selectStr += ",smsc";
				if (!smsc.equals("%")) {
					conditionStr += "smsc='" + smsc + "' and ";
				}
				groupbyStr += ",smsc";
				orderbyStr += ",smsc";
				paramSet.add("smsc");
				// request.setAttribute("smsc", "yes");
				noCriteria = false;
			}
			if (country != null) {
				selectStr += ",oprCountry";
				if (!country.equals("%")) {
					conditionStr += "destination_no like '" + country + "%' and ";
				}
				groupbyStr += ",oprCountry";
				paramSet.add("oprCountry");
				// request.setAttribute("oprCountry", "yes");
				noCriteria = false;
			}
			if (sender != null) {
				selectStr += ",source_no";
				if (sender.length() > 0) {
					conditionStr += "source_no like '" + sender + "%' and ";
				}
				groupbyStr += ",source_no";
				paramSet.add("source_no");
				// request.setAttribute("source", "yes");
				noCriteria = false;
			}
			if (reportType.equalsIgnoreCase("Daily")) {
				conditionStr += "date(time)='" + customReportDTO.getSyear() + "-" + customReportDTO.getSmonth() + "-"
						+ customReportDTO.getSday() + "'";
			} else if (reportType.equalsIgnoreCase("Monthly")) {
				conditionStr += "time like '" + customReportDTO.getSyear() + "-" + customReportDTO.getSmonth() + "%'";
			} else {
				conditionStr += "time between '" + customReportDTO.getSyear() + "-" + customReportDTO.getSmonth() + "-"
						+ customReportDTO.getSday() + " " + customReportDTO.getShour() + ":" + customReportDTO.getSmin()
						+ ":01'";
				conditionStr += " and '" + customReportDTO.getEyear() + "-" + customReportDTO.getEmonth() + "-"
						+ customReportDTO.getEday() + " " + customReportDTO.getEhour() + ":" + customReportDTO.getEmin()
						+ ":59'";
			}
			String sql = selectStr + " from smsc_in " + conditionStr + " " + groupbyStr + orderbyStr;
			logger.info(user.getSystemId() + ": " + sql);
			sql_list.add(sql);

			sql = selectStr + " from unprocessed " + conditionStr + " " + groupbyStr + orderbyStr;
			logger.info(user.getSystemId() + ": " + sql);
			sql_list.add(sql);
			// logger.info(system_id+" logSQL: " + logsql);
			// ************* Getting Record From Database *****************
			Map<String, List<CustomReportDTO>> misreportmap = null;
			if (!mis_sql_list.isEmpty()) {
				misreportmap = getSubmissionReport(mis_sql_list, paramSet);
			}
			Map<String, List<CustomReportDTO>> reportmap = getSubmissionReport(sql_list, paramSet);
			if (misreportmap != null) {
				for (String date_key : misreportmap.keySet()) {
					if (reportmap.containsKey(date_key)) {
						reportmap.get(date_key).addAll(misreportmap.get(date_key));
					} else {
						reportmap.put(date_key, misreportmap.get(date_key));
					}
				}
			}
			// ************* Preparing Report Content **********************
			List reportList = new ArrayList();
			Iterator<String> itr = reportmap.keySet().iterator();
			long grandTotal = 0;
			while (itr.hasNext()) {
				String date = itr.next();
				logger.info(messageResourceBundle.getLogMessage("submit.report.processing.message"), date);

				List<CustomReportDTO> list = reportmap.get(date);
				Iterator<CustomReportDTO> listItr = list.iterator();
				long count = 0;
				if (noCriteria) {
					CustomReportDTO reportDTO = null;
					while (listItr.hasNext()) {
						reportDTO = listItr.next();
						count += Long.parseLong(reportDTO.getTotalSmsSend());
						grandTotal += Long.parseLong(reportDTO.getTotalSmsSend());
					}
					CustomReportDTO copyDTO = new CustomReportDTO();
					BeanUtils.copyProperties(copyDTO, reportDTO);
					copyDTO.setTotalSmsSend(count + "");
					reportList.add(copyDTO);
				} else if (username != null && status == null && smsc == null && country == null && sender == null) {
					CustomReportDTO reportDTO = null;
					Map usermap = new HashMap();
					while (listItr.hasNext()) {
						reportDTO = listItr.next();
						if (usermap.containsKey(reportDTO.getClientId())) {
							count = (Long) usermap.get(reportDTO.getClientId());
						} else {
							count = 0;
						}
						count += Long.parseLong(reportDTO.getTotalSmsSend());
						usermap.put(reportDTO.getClientId(), count);
					}
					Iterator userItr = usermap.keySet().iterator();
					CustomReportDTO copyDTO = null;
					while (userItr.hasNext()) {
						copyDTO = new CustomReportDTO();
						String user1 = (String) userItr.next();
						long totalSend = (Long) usermap.get(user1);
						grandTotal += totalSend;
						copyDTO.setClientId(user1);
						copyDTO.setTotalSmsSend(totalSend + "");
						copyDTO.setStartdate(date);
						reportList.add(copyDTO);
					}
				} else if (smsc != null && username == null && status == null && country == null && sender == null) {
					CustomReportDTO reportDTO = null;
					Map smscmap = new HashMap();
					while (listItr.hasNext()) {
						reportDTO = (CustomReportDTO) listItr.next();
						if (smscmap.containsKey(reportDTO.getSmscName())) {
							count = (Long) smscmap.get(reportDTO.getSmscName());
						} else {
							count = 0;
						}
						count += Long.parseLong(reportDTO.getTotalSmsSend());
						smscmap.put(reportDTO.getSmscName(), count);
					}
					Iterator smscItr = smscmap.keySet().iterator();
					CustomReportDTO copyDTO = null;
					while (smscItr.hasNext()) {
						copyDTO = new CustomReportDTO();
						String smscname = (String) smscItr.next();
						long totalSend = (Long) smscmap.get(smscname);
						grandTotal += totalSend;
						copyDTO.setSmscName(smscname);
						copyDTO.setTotalSmsSend(totalSend + "");
						copyDTO.setStartdate(date);
						reportList.add(copyDTO);
					}
				} else if (country != null && smsc == null && username == null && status == null && sender == null) {
					CustomReportDTO reportDTO = null;
					Map countrymap = new HashMap();
					while (listItr.hasNext()) {
						reportDTO = (CustomReportDTO) listItr.next();
						if (countrymap.containsKey(reportDTO.getCountry())) {
							count = (Long) countrymap.get(reportDTO.getCountry());
						} else {
							count = 0;
						}
						count += Long.parseLong(reportDTO.getTotalSmsSend());
						countrymap.put(reportDTO.getCountry(), count);
					}
					Iterator countryItr = countrymap.keySet().iterator();
					CustomReportDTO copyDTO = null;
					while (countryItr.hasNext()) {
						copyDTO = new CustomReportDTO();
						String countryname = (String) countryItr.next();
						long totalSend = (Long) countrymap.get(countryname);
						grandTotal += totalSend;
						copyDTO.setCountry(countryname);
						copyDTO.setTotalSmsSend(totalSend + "");
						copyDTO.setStartdate(date);
						reportList.add(copyDTO);
					}
				} else if (sender != null && country == null && smsc == null && username == null && status == null) {
					CustomReportDTO reportDTO = null;
					Map sendermap = new HashMap();
					while (listItr.hasNext()) {
						reportDTO = (CustomReportDTO) listItr.next();
						if (sendermap.containsKey(reportDTO.getSenderId())) {
							count = (Long) sendermap.get(reportDTO.getSenderId());
						} else {
							count = 0;
						}
						count += Long.parseLong(reportDTO.getTotalSmsSend());
						sendermap.put(reportDTO.getSenderId(), count);
					}
					Iterator senderItr = sendermap.keySet().iterator();
					CustomReportDTO copyDTO = null;
					while (senderItr.hasNext()) {
						copyDTO = new CustomReportDTO();
						String senderid = (String) senderItr.next();
						long totalSend = (Long) sendermap.get(senderid);
						grandTotal += totalSend;
						copyDTO.setSenderId(senderid);
						copyDTO.setTotalSmsSend(totalSend + "");
						copyDTO.setStartdate(date);
						reportList.add(copyDTO);
					}
				} else if (smsc != null && username != null && status == null && country == null && sender == null) {
					CustomReportDTO reportDTO = null;
					Map usermap = new HashMap();
					Map smscmap = null;
					while (listItr.hasNext()) {
						reportDTO = (CustomReportDTO) listItr.next();
						if (usermap.containsKey(reportDTO.getClientId())) {
							smscmap = (Map) usermap.get(reportDTO.getClientId());
						} else {
							smscmap = new HashMap();
						}
						if (smscmap.containsKey(reportDTO.getSmscName())) {
							count = (Long) smscmap.get(reportDTO.getSmscName());
						} else {
							count = 0;
						}
						count += Long.parseLong(reportDTO.getTotalSmsSend());
						smscmap.put(reportDTO.getSmscName(), count);
						usermap.put(reportDTO.getClientId(), smscmap);
					}
					// logger.info(system_id+" Usermap: "+usermap);
					Iterator userItr = usermap.keySet().iterator();
					CustomReportDTO copyDTO = null;
					while (userItr.hasNext()) {
						String user1 = (String) userItr.next();
						smscmap = (Map) usermap.get(user1);
						// logger.info(system_id+" User: "+user+" Smscmap:"+smscmap);
						Iterator smscItr = smscmap.keySet().iterator();
						while (smscItr.hasNext()) {
							String smscname = (String) smscItr.next();
							copyDTO = new CustomReportDTO();
							long totalSend = (Long) smscmap.get(smscname);
							grandTotal += totalSend;
							copyDTO.setClientId(user1);
							copyDTO.setSmscName(smscname);
							copyDTO.setTotalSmsSend(totalSend + "");
							copyDTO.setStartdate(date);
							reportList.add(copyDTO);
						}
					}
				} else if (country != null && username != null && smsc == null && status == null && sender == null) {
					CustomReportDTO reportDTO = null;
					Map usermap = new HashMap();
					Map countrymap = null;
					while (listItr.hasNext()) {
						reportDTO = (CustomReportDTO) listItr.next();
						if (usermap.containsKey(reportDTO.getClientId())) {
							countrymap = (Map) usermap.get(reportDTO.getClientId());
						} else {
							countrymap = new HashMap();
						}
						if (countrymap.containsKey(reportDTO.getCountry())) {
							count = (Long) countrymap.get(reportDTO.getCountry());
						} else {
							count = 0;
						}
						count += Long.parseLong(reportDTO.getTotalSmsSend());
						countrymap.put(reportDTO.getCountry(), count);
						usermap.put(reportDTO.getClientId(), countrymap);
					}
					// logger.info(system_id+" Usermap: "+usermap);
					Iterator userItr = usermap.keySet().iterator();
					CustomReportDTO copyDTO = null;
					while (userItr.hasNext()) {
						String user1 = (String) userItr.next();
						countrymap = (Map) usermap.get(user1);
						// logger.info(system_id+" User: "+user+" Smscmap:"+smscmap);
						Iterator countryItr = countrymap.keySet().iterator();
						while (countryItr.hasNext()) {
							String countryname = (String) countryItr.next();
							copyDTO = new CustomReportDTO();
							long totalSend = (Long) countrymap.get(countryname);
							grandTotal += totalSend;
							copyDTO.setClientId(user1);
							copyDTO.setCountry(countryname);
							copyDTO.setTotalSmsSend(totalSend + "");
							copyDTO.setStartdate(date);
							reportList.add(copyDTO);
						}
					}
				} else if (sender != null && username != null && smsc == null && status == null && country == null) {
					CustomReportDTO reportDTO = null;
					Map usermap = new HashMap();
					Map sendermap = null;
					while (listItr.hasNext()) {
						reportDTO = (CustomReportDTO) listItr.next();
						if (usermap.containsKey(reportDTO.getClientId())) {
							sendermap = (Map) usermap.get(reportDTO.getClientId());
						} else {
							sendermap = new HashMap();
						}
						if (sendermap.containsKey(reportDTO.getSenderId())) {
							count = (Long) sendermap.get(reportDTO.getSenderId());
						} else {
							count = 0;
						}
						count += Long.parseLong(reportDTO.getTotalSmsSend());
						sendermap.put(reportDTO.getSenderId(), count);
						usermap.put(reportDTO.getClientId(), sendermap);
					}
					// logger.info(system_id+" Usermap: "+usermap);
					Iterator userItr = usermap.keySet().iterator();
					CustomReportDTO copyDTO = null;
					while (userItr.hasNext()) {
						String user1 = (String) userItr.next();
						sendermap = (Map) usermap.get(user1);
						// logger.info(system_id+" User: "+user+" Smscmap:"+smscmap);
						Iterator senderItr = sendermap.keySet().iterator();
						while (senderItr.hasNext()) {
							String senderid = (String) senderItr.next();
							copyDTO = new CustomReportDTO();
							long totalSend = (Long) sendermap.get(senderid);
							grandTotal += totalSend;
							copyDTO.setClientId(user1);
							copyDTO.setSenderId(senderid);
							copyDTO.setTotalSmsSend(totalSend + "");
							copyDTO.setStartdate(date);
							reportList.add(copyDTO);
						}
					}
				} else {
					CustomReportDTO reportDTO = null;
					while (listItr.hasNext()) {
						reportDTO = (CustomReportDTO) listItr.next();
						grandTotal += Long.parseLong(reportDTO.getTotalSmsSend());
						reportList.add(reportDTO);
					}
				}
			}
			if (grandTotal > 0 && reportList.size() > 1) {
				CustomReportDTO totalDTO = new CustomReportDTO();
				totalDTO.setStartdate("Grand Total");
				totalDTO.setClientId("-");
				totalDTO.setSenderId("-");
				totalDTO.setCountry("-");
				totalDTO.setSmscName("-");
				totalDTO.setMessageStatus("-");
				totalDTO.setTotalSmsSend(grandTotal + "");
				reportList.add(totalDTO);
			}
			logger.info(username + " reportList: " + reportList.size());
			if (reportList.isEmpty()) {
				target = IConstants.FAILURE_KEY;
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_SUBMISSION_REPORT_NOT_FOUND_MESSAGE, new Object[] {username}));

				// message = new ActionMessage("error.record.unavailable");
			} else {
				System.out.println("Report Size: " + reportList.size());
				target = IConstants.SUCCESS_KEY;
				return new ResponseEntity<>(reportList, HttpStatus.OK);
				// request.setAttribute("reportList", reportList);
			}
		} catch (Exception ex) {
			// logger.error(user.getSystemId(), ex.fillInStackTrace());
			ex.printStackTrace();
			target = IConstants.FAILURE_KEY;
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_GETTING_SUBMISSION_REPORT_MESSAGE, new Object[] {username}));

		}

	}

	public Map<String, List<CustomReportDTO>> getSubmissionReport(List sql_list, Set paramSet) throws Exception {
		List list = null;
		Map<String, List<CustomReportDTO>> reportmap = new TreeMap<String, List<CustomReportDTO>>();
		// Map countrymap = null;
		Connection con = null;
		Statement pStmt = null;
		ResultSet rs = null;
		CustomReportDTO report = null;
		try {
			con = getConnection();
			pStmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			while (!sql_list.isEmpty()) {
				String sql = (String) sql_list.remove(0);
				logger.info(messageResourceBundle.getLogMessage("sql.get.submission.report.message"), sql);


				rs = pStmt.executeQuery(sql);
				while (rs.next()) {
					String timing = rs.getString("timing");
					report = new CustomReportDTO();
					report.setTotalSmsSend(rs.getString("count"));
					report.setStartdate(timing);
					if (paramSet.contains("sflag")) {
						String sflag = rs.getString("s_flag");
						String status = "Error";
						if (sflag.equalsIgnoreCase("Q")) {
							status = "Queued";
						} else if (sflag.equalsIgnoreCase("C")) {
							status = "Processing";
						} else if (sflag.equalsIgnoreCase("F")) {
							status = "NonRespond";
						} else if (sflag.equalsIgnoreCase("NR")) {
							status = "NegativeRespond";
						} else if (sflag.equalsIgnoreCase("B")) {
							status = "Blocked";
						} else if (sflag.equalsIgnoreCase("M")) {
							status = "MinCost";
						}
						report.setMessageStatus(status);
					}
					if (paramSet.contains("username")) {
						report.setClientId(rs.getString("username"));
					}
					if (paramSet.contains("smsc")) {
						report.setSmscName(rs.getString("smsc"));
					}
					if (paramSet.contains("oprCountry")) {
						String country = rs.getString("oprCountry");
						int network_id = 0;
						try {
							network_id = Integer.parseInt(country);
						} catch (Exception ex) {
						}
						if (GlobalVars.NetworkEntries.containsKey(network_id)) {
							country = GlobalVars.NetworkEntries.get(network_id).getCountry();
						}
						if (country == null) {
							country = "-";
						}
						report.setCountry(country);
					}
					/*
					 * if (paramSet.contains("oprCountry")) {
					 * report.setOperator(rs.getString("oprCountry")); }
					 */
					if (paramSet.contains("source_no")) {
						report.setSenderId(rs.getString("source_no"));
					}
					if (reportmap.containsKey(timing)) {
						list = (List) reportmap.get(timing);
					} else {
						list = new ArrayList();
					}
					list.add(report);
					reportmap.put(timing, list);
				}
			}
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
			throw new SQLException("getSubmissionReport()", sqle);
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
				sqle.printStackTrace();
			}
		}
		return reportmap;
	}
	////////////////
	private Map<String, List<CustomReportDTO>> getSubmissionReport(Map<String, String> sql_list, Set<String> paramSet)
			throws SQLException {
		List list = null;
		Map<String, List<CustomReportDTO>> reportmap = new TreeMap<String, List<CustomReportDTO>>();
		// Map countrymap = null;
		Connection con = null;
		Statement pStmt = null;
		ResultSet rs = null;
		CustomReportDTO report = null;
		try {
			con = getConnection();
			pStmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			for (String mis_user : sql_list.keySet()) {
				String sql = sql_list.get(mis_user);
				logger.info(mis_user + " SubmissionReport: " + sql);
				try {
					rs = pStmt.executeQuery(sql);
					while (rs.next()) {
						String timing = rs.getString("timing");
						report = new CustomReportDTO();
						report.setTotalSmsSend(rs.getString("count"));
						report.setStartdate(timing);
						
						if (paramSet.contains("sflag")) {
							String sflag = rs.getString("s_flag");
							String status = "Processed";
							if (sflag.startsWith("ER")) {
								status = "Error";
							}
							report.setMessageStatus(status);
						}
						if (paramSet.contains("username")) {
							report.setClientId(mis_user);
						}
						if (paramSet.contains("smsc")) {
							report.setSmscName(rs.getString("smsc"));
						}
						if (paramSet.contains("oprCountry")) {
							String country = rs.getString("oprCountry");
							int network_id = 0;
							try {
								network_id = Integer.parseInt(country);
							} catch (Exception ex) {
							}
							if (GlobalVars.NetworkEntries.containsKey(network_id)) {
								country = GlobalVars.NetworkEntries.get(network_id).getCountry();
							}
							if (country == null) {
								country = "-";
							}
							report.setCountry(country);
						}
						/*
						 * if (paramSet.contains("oprCountry")) {
						 * report.setOperator(rs.getString("oprCountry")); }
						 */
						if (paramSet.contains("source_no")) {
							report.setSenderId(rs.getString("source_no"));
						}
						if (reportmap.containsKey(timing)) {
							list = (List) reportmap.get(timing);
						} else {
							list = new ArrayList();
						}
						list.add(report);
						reportmap.put(timing, list);
					}
				} catch (SQLException ex) {
					logger.error(mis_user, ex.fillInStackTrace());
				}
			}
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
			throw new SQLException("getSubmissionReport()", sqle);
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
		return reportmap;
	}
	public Map<Integer, String> listUsernamesUnderManager(String mgrId) {
		Map<Integer, String> map = listNamesUnderManager(mgrId);
		Map<Integer, String> users = new HashMap<Integer, String>();
		for (Integer seller_id : map.keySet()) {
			users.putAll(userService.listUsersUnderSeller(seller_id));
		}
		return users;
	}

	public Map<Integer, String> listNamesUnderManager(String mgrId) {
		Map<Integer, String> map = new HashMap<Integer, String>();
		List<SalesEntry> list = (List) listSellersUnderManager(mgrId);
		for (SalesEntry entry : list) {
			map.put(entry.getId(), entry.getUsername());
		}
		return map;
	}

	public List<SalesEntry> listSellersUnderManager(String mgrId) {
		return salesRepository.findByMasterIdAndRole(mgrId, "seller");
	}

//methode getSubmissionUsers//
	public List getSubmissionUsers(String sql) {
		List list = new ArrayList();
		Connection con = null;
		Statement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.createStatement();
			rs = pStmt.executeQuery(sql);
			String username = null;
			while (rs.next()) {
				username = rs.getString("username");
				list.add(username);
			}
		} catch (Exception ex) {
			logger.error(" ", ex.fillInStackTrace());
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
		return list;
	}

	private long daysDifference(String inputtxt) throws IOException, ParseException {
		Date inputDate = new SimpleDateFormat("yyyy-MM-dd").parse(inputtxt);
		long days = ChronoUnit.DAYS.between(inputDate.toInstant(), new Date().toInstant());
		logger.info(messageResourceBundle.getLogMessage("difference.from.today.message"), inputtxt, days);

		return days;
	}
}
