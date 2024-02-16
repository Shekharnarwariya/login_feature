package com.hti.smpp.common.service.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hazelcast.org.json.JSONArray;
import com.hazelcast.org.json.JSONObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.database.DBException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.DashboardRequest;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.DashboardService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.repository.SmscEntryRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.MessageResourceBundle;

@Service
public class Dashboard implements DashboardService {

	@Autowired
	private SalesRepository salesRepository;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private UserDAService userService;

	@Autowired
	private SmscEntryRepository smscEntryRepository;

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	private Logger logger = LoggerFactory.getLogger(Dashboard.class);

	@Override
	public ResponseEntity<?> processRequest(DashboardRequest request, String username) {
		System.out.println(username);
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
		}
		String resp = "";
		if (request.getMethod() != null) {
			if (request.getMethod().equalsIgnoreCase("dashboard")) {
				if (request.getDays() != null) {
					int days = Integer.parseInt(request.getDays());
					String systemid = request.getUsername();
					logger.info(messageResourceBundle.getLogMessage("user.days.message"), user.getSystemId(), days);

					try {
						if (systemid != null) {
							System.out.println("1st called: " + systemid + " " + days);
							return dashboard(systemid, days, request, username);
						} else {
							if (days == 0) {
								System.out.println("2nd called: " + systemid + " " + days);
								return dashboard(request, username);
							} else {
								System.out.println("3rd called: " + systemid + " " + days);
								return dashboard(days, request, username);
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} else {
				if (request.getMethod().equalsIgnoreCase("userlist")) {

					String seller = request.getSeller();
					int seller_id = 0;
					
					try {
						if (seller != null && Integer.parseInt(seller) > 0) {
							seller_id = Integer.parseInt(seller);
						}
					} catch (Exception ex) {
						logger.error(messageResourceBundle.getLogMessage("invalid.seller.id.message"), user.getSystemId(), seller);

					}
					Map<Integer, String> map = null;
					if (seller_id > 0) {
						map = userService.listUsersUnderSeller(seller_id);
						logger.info(messageResourceBundle.getLogMessage("user.found.under.seller.message"), user.getSystemId(), seller_id, map.values());

					} else {
						map = listUsernamesUnderManager(user.getSystemId());
					}
					resp = String.join(",", map.values());

				}
			}
		}
		return ResponseEntity.ok(resp);
	}

	private ResponseEntity<?> dashboard(int days, DashboardRequest request, String username) throws Exception {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
		}
		// IDatabaseService dbService = HtiSmsDB.getInstance();
		logger.info(messageResourceBundle.getLogMessage("dashboard.report.last.days.message"), days, user.getSystemId());

		boolean proceed = true;
		Calendar calender = Calendar.getInstance();
		String sql = "";
		if (days > 0) {
			if (days <= 2) {
				sql = "select count(msg_id) as count,username,oprCountry,sender,status,smsc from report_log where ";
			} else {
				sql = "select total as count,username,oprCountry,sender,status,smsc from report_summary where ";
			}
			calender.add(Calendar.DATE, -days);
			if (days == 1) {
				sql += "time ='" + new SimpleDateFormat("yyyy-MM-dd").format(calender.getTime()) + "'";
			} else {
				Calendar end_calender = Calendar.getInstance();
				end_calender.add(Calendar.DATE, -1);
				sql += "time between '" + new SimpleDateFormat("yyyy-MM-dd").format(calender.getTime()) + "' and '"
						+ new SimpleDateFormat("yyyy-MM-dd").format(end_calender.getTime()) + "'";
			}
			if (user.getRole().equalsIgnoreCase("user")) {
				sql += " and username = '" + user.getSystemId() + "'";
			} else {
				if (user.getRole().equalsIgnoreCase("seller") || user.getRole().equalsIgnoreCase("admin")
						|| user.getRole().equalsIgnoreCase("manager")) {
					String users = "";
					Map<Integer, String> map = null;
					if (user.getRole().equalsIgnoreCase("manager")) {
						// SalesDAService salesService = new SalesDAServiceImpl();
						String seller = request.getSeller();
						int seller_id = 0;
						try {
							if (seller != null && Integer.parseInt(seller) > 0) {
								seller_id = Integer.parseInt(seller);
							}
						} catch (Exception ex) {
							logger.error(messageResourceBundle.getLogMessage("invalid.seller.id.message"), user.getSystemId(), seller);

						}
						if (seller_id > 0) {
							map = userService.listUsersUnderSeller(seller_id);
							
							logger.info(messageResourceBundle.getLogMessage("user.found.under.seller.message"), user.getSystemId(), seller_id, map.values());
							
							
						} else {
							map = listUsernamesUnderManager(user.getSystemId());
						}
					} else if (user.getRole().equalsIgnoreCase("seller")) {
						map = userService.listUsersUnderSeller(user.getId());
					} else {
						map = userService.listUsersUnderMaster(user.getSystemId());
						Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
								.get("secondaryMaster").equal(user.getSystemId());
						for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
							UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
							map.put(userEntry.getId(), userEntry.getSystemId());
						}
						map.put(user.getId(), user.getSystemId());
					}
					List<String> list = new ArrayList<String>(map.values());
					if (user.getRole().equalsIgnoreCase("admin")) {
						list.add(user.getSystemId());
						if (smscEntryRepository.findByMasterId(username) != null) {
							sql += " and smsc in ('" + String.join("','", listNames(user.getSystemId()).values())
									+ "')";
						}
					}
					for (String user1 : list) {
						users += "'" + user1 + "',";
					}
					if (users.length() > 0) {
						users = users.substring(0, users.length() - 1);
						sql += " and username in (" + users + ")";
					} else {
						proceed = false;
					}
				}
			}
			if (days <= 2) {
				sql += " group by username,oprCountry,sender,status,smsc";
			}
		} else {
			sql = "select count(msg_id) as count,username,oprCountry,sender,status,smsc from report";
			if (!(user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system"))) {
				if (user.getRole().equalsIgnoreCase("seller") || user.getRole().equalsIgnoreCase("manager")
						|| user.getRole().equalsIgnoreCase("admin")) {
					String users = "";
					Map<Integer, String> map = null;
					if (user.getRole().equalsIgnoreCase("seller")) {
						map = userService.listUsersUnderSeller(user.getId());
					} else if (user.getRole().equalsIgnoreCase("manager")) {
						// SalesDAService salesService = new SalesDAServiceImpl();
						String seller = request.getSeller();
						int seller_id = 0;
						try {
							if (seller != null && Integer.parseInt(seller) > 0) {
								seller_id = Integer.parseInt(seller);
							}
						} catch (Exception ex) {
							logger.error(messageResourceBundle.getLogMessage("invalid.seller.id.message"), user.getSystemId(), seller);

						}
						if (seller_id > 0) {
							map = userService.listUsersUnderSeller(seller_id);
							logger.info(messageResourceBundle.getLogMessage("user.found.under.seller.message"), user.getSystemId(), seller_id, map.values());

						} else {
							map = listUsernamesUnderManager(user.getSystemId());
						}
					} else {
						map = userService.listUsersUnderMaster(user.getSystemId());
						Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
								.get("secondaryMaster").equal(user.getSystemId());
						for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
							UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
							map.put(userEntry.getId(), userEntry.getSystemId());
						}
						map.put(user.getId(), user.getSystemId());
					}
					List<String> list = new ArrayList<String>(map.values());
					for (String user1 : list) {
						users += "'" + user1 + "',";
					}
					if (users.length() > 0) {
						users = users.substring(0, users.length() - 1);
						sql += " where username in (" + users + ")";
						if (user.getRole().equalsIgnoreCase("admin")) {
							if (smscEntryRepository.findByMasterId(username) != null) {
								sql += " and smsc in ('" + String.join("','", listNames(user.getSystemId()).values())
										+ "')";
							}
						}
					} else {
						proceed = false;
					}
				} else {
					sql += " where username = '" + user.getSystemId() + "'";
				}
			}
			sql += " group by username,oprCountry,sender,status,smsc";
		}
		// ************* Creating Response Object ******************************
		logger.info(user.getSystemId() + " " + "Dashboard SQL: " + sql);
		List<DeliveryDTO> list = null;
		if (proceed) {
			list = getDashboardReport(sql);
		} else {
			list = new ArrayList<DeliveryDTO>();
		}
		logger.info(user.getSystemId() + " " + "Dashboard Report List Size:--> " + list.size());
		// Iterator itr = list.iterator();
		int received = 0, processed = 0, delivered = 0;
		Map<String, Integer> country_map = new HashMap<String, Integer>();
		Map<String, Integer> sender_map = new HashMap<String, Integer>();
		Map<String, Integer> user_received_map = new HashMap<String, Integer>();
		Map<String, Integer> user_processed_map = new HashMap<String, Integer>();
		Map<String, Integer> user_delivered_map = new HashMap<String, Integer>();
		Map<String, Integer> smsc_count = new HashMap<String, Integer>();
		Map<String, Integer> smsc_deliver = new HashMap<String, Integer>();
		Map<String, Integer> smsc_spam = new HashMap<String, Integer>();
		Map<String, Integer> user_spam = new HashMap<String, Integer>();
		// Map user_count_map = new HashMap();
		Map<String, Set<String>> sender_user_map = new HashMap<String, Set<String>>();
		DeliveryDTO reportDTO = null;
		while (!list.isEmpty()) {
			// ----------- status Chart ----------------------
			reportDTO = (DeliveryDTO) list.remove(0);
			int submitted = reportDTO.getSubmitted();
			received += submitted;
			if (reportDTO.getStatus() != null) {
				processed += submitted;
				if (reportDTO.getStatus().startsWith("DELIV")) {
					delivered += submitted;
				}
			}
			// ----------- CountryCounter Chart ----------------------
			int c_counter = 0;
			if (country_map.containsKey(reportDTO.getCountry())) {
				c_counter = country_map.get(reportDTO.getCountry());
			}
			c_counter += submitted;
			country_map.put(reportDTO.getCountry(), c_counter);
			// ----------- SenderCounter Chart ----------------------
			int s_counter = 0;
			if (sender_map.containsKey(reportDTO.getSender())) {
				s_counter = sender_map.get(reportDTO.getSender());
			}
			s_counter += submitted;
			sender_map.put(reportDTO.getSender(), s_counter);
			if (!user.getRole().equalsIgnoreCase("user")) {
				Set<String> users_set = null;
				if (sender_user_map.containsKey(reportDTO.getSender())) {
					users_set = sender_user_map.get(reportDTO.getSender());
				} else {
					users_set = new HashSet<String>();
				}
				users_set.add(reportDTO.getUsername());
				sender_user_map.put(reportDTO.getSender(), users_set);
			}
			if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
					|| (user.getRole().equalsIgnoreCase("admin")
							&& smscEntryRepository.findByMasterId(username) != null)) {
				// ------------ Smsc Submit Counter --------------------------
				int ss_counter = 0;
				if (smsc_count.containsKey(reportDTO.getRoute())) {
					ss_counter = smsc_count.get(reportDTO.getRoute());
				}
				ss_counter += submitted;
				smsc_count.put(reportDTO.getRoute(), ss_counter);
				// ------------ Smsc Deliver Counter --------------------------
				if (reportDTO.getStatus() != null && reportDTO.getStatus().startsWith("DEL")) {
					int sd_counter = 0;
					if (smsc_deliver.containsKey(reportDTO.getRoute())) {
						sd_counter = smsc_deliver.get(reportDTO.getRoute());
					}
					sd_counter += submitted;
					smsc_deliver.put(reportDTO.getRoute(), sd_counter);
				}
			}
			if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")) {
				if (reportDTO.getStatus() != null && reportDTO.getStatus().equalsIgnoreCase("B")) {
					// ------------ Smsc Spam Counter --------------------------
					int sb_counter = 0;
					if (smsc_spam.containsKey(reportDTO.getRoute())) {
						sb_counter = smsc_spam.get(reportDTO.getRoute());
					}
					sb_counter += submitted;
					smsc_spam.put(reportDTO.getRoute(), sb_counter);
					// ------------ User Spam Counter --------------------------
					int ub_counter = 0;
					if (user_spam.containsKey(reportDTO.getUsername())) {
						ub_counter = user_spam.get(reportDTO.getUsername());
					}
					ub_counter += submitted;
					user_spam.put(reportDTO.getUsername(), ub_counter);
				}
			}
			if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
					|| user.getRole().equalsIgnoreCase("seller") || user.getRole().equalsIgnoreCase("manager")
					|| user.getRole().equalsIgnoreCase("admin")) {
				// ----------- UserCounter Chart ----------------------
				int user_received = 0, user_processed = 0, user_delivered = 0;
				if (user_received_map.containsKey(reportDTO.getUsername())) {
					user_received = (Integer) user_received_map.get(reportDTO.getUsername());
				}
				user_received += submitted;
				user_received_map.put(reportDTO.getUsername(), user_received);
				if (reportDTO.getStatus() != null) {
					if (user_processed_map.containsKey(reportDTO.getUsername())) {
						user_processed = (Integer) user_processed_map.get(reportDTO.getUsername());
					}
					user_processed += submitted;
					user_processed_map.put(reportDTO.getUsername(), user_processed);
					if (reportDTO.getStatus().startsWith("DELIV")) {
						if (user_delivered_map.containsKey(reportDTO.getUsername())) {
							user_delivered = (Integer) user_delivered_map.get(reportDTO.getUsername());
						}
						user_delivered += submitted;
						user_delivered_map.put(reportDTO.getUsername(), user_delivered);
					}
				}
			}
			reportDTO = null;
		}
		list.clear();
		// --------- first Chart -------------------
		JSONArray responseObj = new JSONArray();
		JSONObject jo = new JSONObject();
		jo.put("counter", (Integer) received);
		jo.put("processed", (Integer) processed);
		jo.put("deliverd", (Integer) delivered);
		responseObj.put(jo);
		// ----------- 2nd Chart -----------------------
		JSONObject json = new JSONObject();
		JSONArray colsArr = new JSONArray();
		JSONObject cols = new JSONObject();
		cols.put("id", "");
		cols.put("label", "oprCountry");
		cols.put("type", "string");
		colsArr.put(cols);
		cols = new JSONObject();
		cols.put("id", "");
		cols.put("label", "count");
		cols.put("type", "number");
		colsArr.put(cols);
		JSONArray rowsArr = new JSONArray();
		Map temp_map = new TreeMap();
		Iterator itr = country_map.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			int count = (Integer) country_map.get(key);
			int network_id = 0;
			try {
				network_id = Integer.parseInt(key);
			} catch (Exception ex) {
			}
			if (GlobalVars.NetworkEntries.containsKey(network_id)) {
				key = GlobalVars.NetworkEntries.get(network_id).getCountry();
			}
			if (temp_map.containsKey(key)) {
				int counter = (Integer) temp_map.get(key);
				count = count + counter;
			}
			temp_map.put(key, count);
		}
		// logger.info(userSessionObject.getSystemId()+" "+"temp_map: " +
		// temp_map.size());
		temp_map = sortByDscValue(temp_map, 10);
		itr = temp_map.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			int count = (Integer) temp_map.get(key);
			// logger.info(userSessionObject.getSystemId()+" "+"Key: "+key+" Count:
			// "+count);
			JSONObject rows = new JSONObject();
			JSONArray rowValArr = new JSONArray();
			JSONObject rowVal = new JSONObject();
			rowVal.put("v", key);
			rowValArr.put(rowVal);
			rowVal = new JSONObject();
			rowVal.put("v", count);
			rowValArr.put(rowVal);
			rows.put("c", rowValArr);
			rowsArr.put(rows);
		}
		// logger.info(userSessionObject.getSystemId()+" "+" <-- Added First ---> ");
		json.put("cols", colsArr);
		json.put("rows", rowsArr);
		responseObj.put(json);
		logger.info(messageResourceBundle.getLogMessage("country.counter.message"), user.getSystemId(), json.toString());

		// ----------- 3rd Chart -----------------------
		sender_map = sortByDscValue(sender_map, 10);
		json = new JSONObject();
		colsArr = new JSONArray();
		cols = new JSONObject();
		// rowsArr = new JSONArray();
		cols.put("id", "");
		cols.put("label", "SenderId");
		cols.put("type", "string");
		colsArr.put(cols);
		cols = new JSONObject();
		cols.put("id", "");
		cols.put("label", "count");
		cols.put("type", "number");
		colsArr.put(cols);
		rowsArr = new JSONArray();
		itr = sender_map.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			int count = (Integer) sender_map.get(key);
			// String costStr = new DecimalFormat("0.00000").format(cost);
			JSONObject rows = new JSONObject();
			JSONArray rowValArr = new JSONArray();
			JSONObject rowVal = new JSONObject();
			if (!user.getRole().equalsIgnoreCase("user")) {
				if (sender_user_map.containsKey(key)) {
					rowVal.put("v", key + "" + sender_user_map.get(key));
				} else {
					rowVal.put("v", key);
				}
			} else {
				rowVal.put("v", key);
			}
			rowValArr.put(rowVal);
			rowVal = new JSONObject();
			rowVal.put("v", count);
			rowValArr.put(rowVal);
			rows.put("c", rowValArr);
			rowsArr.put(rows);
		}
		json.put("cols", colsArr);
		json.put("rows", rowsArr);
		responseObj.put(json);
		logger.info(messageResourceBundle.getLogMessage("sender.count.message"), user.getSystemId(), json.toString());

		// ------------------ 4th chart --------------
		JSONArray arr = new JSONArray();
		JSONObject smscCount = new JSONObject();
		JSONObject smscDeliver = new JSONObject();
		JSONObject smscSpam = new JSONObject();
		JSONObject userSpam = new JSONObject();
		if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
				|| user.getRole().equalsIgnoreCase("seller") || user.getRole().equalsIgnoreCase("manager")
				|| user.getRole().equalsIgnoreCase("admin")) {
			user_received_map = sortByDscValue(user_received_map, 30);
			itr = user_received_map.keySet().iterator();
			while (itr.hasNext()) {
				int user_processed = 0, user_delivered = 0;
				String username1 = (String) itr.next();
				int user_received = (Integer) user_received_map.get(username1);
				if (user_processed_map.containsKey(username1)) {
					user_processed = (Integer) user_processed_map.get(username1);
				}
				if (user_delivered_map.containsKey(username1)) {
					user_delivered = (Integer) user_delivered_map.get(username1);
				}
				logger.info(messageResourceBundle.getLogMessage("user.processing.info.message"), user.getSystemId(), username1, user_received, user_processed, user_delivered);

				json = new JSONObject();
				json.put("user", username1);
				json.put("counter", user_received);
				json.put("processed", (Integer) user_processed);
				json.put("deliverd", (Integer) user_delivered);
				arr.put(json);
			}
			logger.info(messageResourceBundle.getLogMessage("userwise.counter.message"), user.getSystemId(), arr.toString());

		}
		if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
				|| (user.getRole().equalsIgnoreCase("admin") && smscEntryRepository.findByMasterId(username) != null)) {
			// ----------- 5th Chart -----------------------
			smsc_count = sortByDscValue(smsc_count, 15);
			logger.info(messageResourceBundle.getLogMessage("smsc.counter.message"), user.getSystemId(), smsc_count);

			// Set dlr_set = smsc_count.keySet();
			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "Route");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "count");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = smsc_count.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int count = (Integer) smsc_count.get(key);
				// String costStr = new DecimalFormat("0.00000").format(cost);
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", count);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			smscCount.put("cols", colsArr);
			smscCount.put("rows", rowsArr);
			
			logger.info(messageResourceBundle.getLogMessage("smsc.counter.message"), user.getSystemId(), smscCount.toString());

			// ----------- 6th Chart -----------------------
			// smsc_deliver = sortByDscValue(smsc_deliver, 10);
			logger.info(messageResourceBundle.getLogMessage("smsc.deliver.message"), user.getSystemId(), smsc_deliver);
			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "Route");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "Percent");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = smsc_count.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int percent = 0;
				if (smsc_deliver.containsKey(key)) {
					int deliv = (Integer) smsc_deliver.get(key);
					int submit = (Integer) smsc_count.get(key);
					percent = (int) (((double) deliv / (double) submit) * 100);
				} else {
					logger.info(messageResourceBundle.getLogMessage("no.delivery.found.message"), user.getSystemId(), key);

				}
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", percent);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			smscDeliver.put("cols", colsArr);
			smscDeliver.put("rows", rowsArr);
			logger.info(user.getSystemId() + " " + "Smsc Deliver: " + smscDeliver.toString());
			logger.info(messageResourceBundle.getLogMessage("smsc.deliver.message"), user.getSystemId(), smscDeliver.toString());

		}
		if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")) {
			// ----------- 7th Chart -----------------------
			smsc_spam = sortByDscValue(smsc_spam, 10);
			logger.info(messageResourceBundle.getLogMessage("smsc.spam.message"), user.getSystemId(), smsc_spam);

			// Set dlr_set = smsc_count.keySet();
			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "Route");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "count");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = smsc_spam.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int count = (Integer) smsc_spam.get(key);
				// String costStr = new DecimalFormat("0.00000").format(cost);
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", count);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			smscSpam.put("cols", colsArr);
			smscSpam.put("rows", rowsArr);
			logger.info(messageResourceBundle.getLogMessage("smsc.spam.count.message"), user.getSystemId(), smscSpam.toString());

			// ----------- 8th Chart -----------------------
			user_spam = sortByDscValue(user_spam, 10);
			logger.info(messageResourceBundle.getLogMessage("user.spam.message"), user.getSystemId(), user_spam);

			// Set dlr_set = smsc_count.keySet();
			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "User");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "count");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = user_spam.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int count = (Integer) user_spam.get(key);
				// String costStr = new DecimalFormat("0.00000").format(cost);
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", count);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			userSpam.put("cols", colsArr);
			userSpam.put("rows", rowsArr);
			logger.info(messageResourceBundle.getLogMessage("user.spam.count.message"), user.getSystemId(), userSpam.toString());

		}
		responseObj.put(arr);
		responseObj.put(smscCount);
		responseObj.put(smscDeliver);
		responseObj.put(smscSpam);
		responseObj.put(userSpam);
		logger.info(messageResourceBundle.getLogMessage("finished.dashboard.message"), user.getSystemId());

		return ResponseEntity.ok(responseObj.toString());
	}
	

	private ResponseEntity<?> dashboard(DashboardRequest request, String username) throws IOException, DBException {
		System.out.println("line 718 for request , username methode ");
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
		}

		logger.info(messageResourceBundle.getLogMessage("getting.recent.dashboard.report.message"), user.getSystemId(), user.getRole());

		boolean proceed = true;
		String sql = "";
		sql = "select count(msg_id) as count,username,oprCountry,sender,status,smsc from report";
		if (!(user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system"))) {
			if (user.getRole().equalsIgnoreCase("seller") || user.getRole().equalsIgnoreCase("manager")
					|| user.getRole().equalsIgnoreCase("admin")) {
				String users = "";
				Map<Integer, String> map = null;
				if (user.getRole().equalsIgnoreCase("seller")) {
					map = userService.listUsersUnderSeller(user.getId());
				} else if (user.getRole().equalsIgnoreCase("manager")) {
					// SalesDAService salesService = new SalesDAServiceImpl();
					String seller = request.getSeller();
					int seller_id = 0;
					try {
						if (seller != null && Integer.parseInt(seller) > 0) {
							seller_id = Integer.parseInt(seller);
						}
					} catch (Exception ex) {
						logger.error(user.getSystemId() + " Invalid seller Id: " + seller);
					}
					if (seller_id > 0) {
						map = userService.listUsersUnderSeller(seller_id);
						logger.info(
								user.getSystemId() + " User Found Under Seller[" + seller_id + "]: " + map.values());
					} else {
						map = listUsernamesUnderManager(user.getSystemId());
					}
				} else {
					map = userService.listUsersUnderMaster(user.getSystemId());
					Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
							.get("secondaryMaster").equal(user.getSystemId());
					for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
						UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
						map.put(userEntry.getId(), userEntry.getSystemId());
					}
					map.put(user.getId(), user.getSystemId());
				}
				List<String> list = new ArrayList<String>(map.values());
				for (String user1 : list) {
					users += "'" + user1 + "',";
				}
				if (users.length() > 0) {
					users = users.substring(0, users.length() - 1);
					sql += " where username in (" + users + ")";
					if (user.getRole().equalsIgnoreCase("admin")) {
						if (smscEntryRepository.findByMasterId(username) != null) {
							sql += " and smsc in ('" + String.join("','", listNames(user.getSystemId()).values())
									+ "')";
						}
					}
				} else {
					proceed = false;
				}
			} else {
				sql += " where username = '" + user.getSystemId() + "'";
			}
		}
		sql += " group by username,oprCountry,sender,status,smsc";
		// ************* Creating Response Object ******************************
		logger.info(messageResourceBundle.getLogMessage("dashboard.sql.message"), user.getSystemId(), sql);

		List<DeliveryDTO> list = null;
		if (proceed) {
			list = getDashboardReport(sql);
		} else {
			list = new ArrayList<DeliveryDTO>();
		}
		logger.info(messageResourceBundle.getLogMessage("dashboard.report.list.size.message"), user.getSystemId(), list.size());

		// Iterator itr = list.iterator();
		int received = 0, processed = 0, delivered = 0;
		Map<String, Integer> country_map = new HashMap<String, Integer>();
		Map<String, Integer> sender_map = new HashMap<String, Integer>();
		Map<String, Integer> user_received_map = new HashMap<String, Integer>();
		Map<String, Integer> user_processed_map = new HashMap<String, Integer>();
		Map<String, Integer> user_delivered_map = new HashMap<String, Integer>();
		Map<String, Integer> smsc_count = new HashMap<String, Integer>();
		Map<String, Integer> smsc_deliver = new HashMap<String, Integer>();
		Map<String, Integer> smsc_spam = new HashMap<String, Integer>();
		Map<String, Integer> user_spam = new HashMap<String, Integer>();
		// Map user_count_map = new HashMap();
		DeliveryDTO reportDTO = null;
		Map<String, Set<String>> sender_user_map = new HashMap<String, Set<String>>();
		while (!list.isEmpty()) {
			// ----------- status Chart ----------------------
			reportDTO = (DeliveryDTO) list.remove(0);
			int submitted = reportDTO.getSubmitted();
			received += submitted;
			if (reportDTO.getStatus() != null) {
				processed += submitted;
				if (reportDTO.getStatus().startsWith("DELIV")) {
					delivered += submitted;
				}
			}
			// ----------- CountryCounter Chart ----------------------
			int c_counter = 0;
			if (country_map.containsKey(reportDTO.getCountry())) {
				c_counter = country_map.get(reportDTO.getCountry());
			}
			c_counter += submitted;
			country_map.put(reportDTO.getCountry(), c_counter);
			// ----------- SenderCounter Chart ----------------------
			int s_counter = 0;
			if (sender_map.containsKey(reportDTO.getSender())) {
				s_counter = sender_map.get(reportDTO.getSender());
			}
			s_counter += submitted;
			sender_map.put(reportDTO.getSender(), s_counter);
			if (!user.getRole().equalsIgnoreCase("user")) {
				Set<String> users_set = null;
				if (sender_user_map.containsKey(reportDTO.getSender())) {
					users_set = sender_user_map.get(reportDTO.getSender());
				} else {
					users_set = new HashSet<String>();
				}
				users_set.add(reportDTO.getUsername());
				sender_user_map.put(reportDTO.getSender(), users_set);
			}
			if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
					|| (user.getRole().equalsIgnoreCase("admin")
							&& smscEntryRepository.findByMasterId(username) != null)) {
				int ss_counter = 0;
				if (smsc_count.containsKey(reportDTO.getRoute())) {
					ss_counter = smsc_count.get(reportDTO.getRoute());
				}
				ss_counter += submitted;
				smsc_count.put(reportDTO.getRoute(), ss_counter);
				// ------------ Smsc Deliver Counter --------------------------
				if (reportDTO.getStatus() != null && reportDTO.getStatus().startsWith("DEL")) {
					int sd_counter = 0;
					if (smsc_deliver.containsKey(reportDTO.getRoute())) {
						sd_counter = smsc_deliver.get(reportDTO.getRoute());
					}
					sd_counter += submitted;
					smsc_deliver.put(reportDTO.getRoute(), sd_counter);
				}
			}
			if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")) {
				if (reportDTO.getStatus() != null && reportDTO.getStatus().equalsIgnoreCase("B")) {
					// ------------ Smsc Spam Counter --------------------------
					int sb_counter = 0;
					if (smsc_spam.containsKey(reportDTO.getRoute())) {
						sb_counter = smsc_spam.get(reportDTO.getRoute());
					}
					sb_counter += submitted;
					smsc_spam.put(reportDTO.getRoute(), sb_counter);
					// ------------ User Spam Counter --------------------------
					int ub_counter = 0;
					if (user_spam.containsKey(reportDTO.getUsername())) {
						ub_counter = user_spam.get(reportDTO.getUsername());
					}
					ub_counter += submitted;
					user_spam.put(reportDTO.getUsername(), ub_counter);
				}
			}
			if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
					|| user.getRole().equalsIgnoreCase("seller") || user.getRole().equalsIgnoreCase("manager")
					|| user.getRole().equalsIgnoreCase("admin")) {
				// ----------- UserCounter Chart ----------------------
				int user_received = 0, user_processed = 0, user_delivered = 0;
				if (user_received_map.containsKey(reportDTO.getUsername())) {
					user_received = (Integer) user_received_map.get(reportDTO.getUsername());
				}
				user_received += submitted;
				user_received_map.put(reportDTO.getUsername(), user_received);
				if (reportDTO.getStatus() != null) {
					if (user_processed_map.containsKey(reportDTO.getUsername())) {
						user_processed = (Integer) user_processed_map.get(reportDTO.getUsername());
					}
					user_processed += submitted;
					user_processed_map.put(reportDTO.getUsername(), user_processed);
					if (reportDTO.getStatus().startsWith("DELIV")) {
						if (user_delivered_map.containsKey(reportDTO.getUsername())) {
							user_delivered = (Integer) user_delivered_map.get(reportDTO.getUsername());
						}
						user_delivered += submitted;
						user_delivered_map.put(reportDTO.getUsername(), user_delivered);
					}
				}
			}
			reportDTO = null;
		}
		list.clear();
		// --------- first Chart -------------------
		JSONArray responseObj = new JSONArray();
		JSONObject jo = new JSONObject();
		jo.put("counter", (Integer) received);
		jo.put("processed", (Integer) processed);
		jo.put("deliverd", (Integer) delivered);
		responseObj.put(jo);
		// ----------- 2nd Chart -----------------------
		JSONObject json = new JSONObject();
		JSONArray colsArr = new JSONArray();
		JSONObject cols = new JSONObject();
		cols.put("id", "");
		cols.put("label", "oprCountry");
		cols.put("type", "string");
		colsArr.put(cols);
		cols = new JSONObject();
		cols.put("id", "");
		cols.put("label", "count");
		cols.put("type", "number");
		colsArr.put(cols);
		JSONArray rowsArr = new JSONArray();
		Map temp_map = new TreeMap();
		Iterator itr = country_map.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			int count = (Integer) country_map.get(key);
			int network_id = 0;
			try {
				network_id = Integer.parseInt(key);
			} catch (Exception ex) {
			}
			if (GlobalVars.NetworkEntries.containsKey(network_id)) {
				key = GlobalVars.NetworkEntries.get(network_id).getCountry();
			}
			if (temp_map.containsKey(key)) {
				int counter = (Integer) temp_map.get(key);
				count = count + counter;
			}
			temp_map.put(key, count);
		}
		// logger.info(userSessionObject.getSystemId()+" "+"temp_map: " +
		// temp_map.size());
		temp_map = sortByDscValue(temp_map, 10);
		itr = temp_map.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			int count = (Integer) temp_map.get(key);
			// logger.info(userSessionObject.getSystemId()+" "+"Key: "+key+" Count:
			// "+count);
			JSONObject rows = new JSONObject();
			JSONArray rowValArr = new JSONArray();
			JSONObject rowVal = new JSONObject();
			rowVal.put("v", key);
			rowValArr.put(rowVal);
			rowVal = new JSONObject();
			rowVal.put("v", count);
			rowValArr.put(rowVal);
			rows.put("c", rowValArr);
			rowsArr.put(rows);
		}
		// logger.info(userSessionObject.getSystemId()+" "+" <-- Added First ---> ");
		json.put("cols", colsArr);
		json.put("rows", rowsArr);
		responseObj.put(json);
		logger.info(messageResourceBundle.getLogMessage("country.counter.message"), user.getSystemId(), json.toString());

		// ----------- 3rd Chart -----------------------
		sender_map = sortByDscValue(sender_map, 10);
		json = new JSONObject();
		colsArr = new JSONArray();
		cols = new JSONObject();
		// rowsArr = new JSONArray();
		cols.put("id", "");
		cols.put("label", "SenderId");
		cols.put("type", "string");
		colsArr.put(cols);
		cols = new JSONObject();
		cols.put("id", "");
		cols.put("label", "count");
		cols.put("type", "number");
		colsArr.put(cols);
		rowsArr = new JSONArray();
		itr = sender_map.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			int count = (Integer) sender_map.get(key);
			// String costStr = new DecimalFormat("0.00000").format(cost);
			JSONObject rows = new JSONObject();
			JSONArray rowValArr = new JSONArray();
			JSONObject rowVal = new JSONObject();
			if (!user.getRole().equalsIgnoreCase("user")) {
				if (sender_user_map.containsKey(key)) {
					rowVal.put("v", key + "" + sender_user_map.get(key));
				} else {
					rowVal.put("v", key);
				}
			} else {
				rowVal.put("v", key);
			}
			rowValArr.put(rowVal);
			rowVal = new JSONObject();
			rowVal.put("v", count);
			rowValArr.put(rowVal);
			rows.put("c", rowValArr);
			rowsArr.put(rows);
		}
		json.put("cols", colsArr);
		json.put("rows", rowsArr);
		responseObj.put(json);
		logger.info(messageResourceBundle.getLogMessage("sender.count.message"), user.getSystemId(), json.toString());

		// ------------------ 4th chart --------------
		JSONArray arr = new JSONArray();
		JSONObject smscCount = new JSONObject();
		JSONObject smscDeliver = new JSONObject();
		JSONObject smscSpam = new JSONObject();
		JSONObject userSpam = new JSONObject();
		if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
				|| user.getRole().equalsIgnoreCase("seller") || user.getRole().equalsIgnoreCase("manager")
				|| user.getRole().equalsIgnoreCase("admin")) {
			user_received_map = sortByDscValue(user_received_map, 30);
			itr = user_received_map.keySet().iterator();
			while (itr.hasNext()) {
				int user_processed = 0, user_delivered = 0;
				String username1 = (String) itr.next();
				int user_received = (Integer) user_received_map.get(username1);
				if (user_processed_map.containsKey(username1)) {
					user_processed = (Integer) user_processed_map.get(username1);
				}
				if (user_delivered_map.containsKey(username1)) {
					user_delivered = (Integer) user_delivered_map.get(username1);
				}
				logger.info(messageResourceBundle.getLogMessage("user.processing.info.message"), user.getSystemId(), username1, user_received, user_processed, user_delivered);

				json = new JSONObject();
				json.put("user", username1);
				json.put("counter", user_received);
				json.put("processed", (Integer) user_processed);
				json.put("deliverd", (Integer) user_delivered);
				arr.put(json);
			}
			logger.info(messageResourceBundle.getLogMessage("userwise.counter.message"), user.getSystemId(), arr.toString());

		}
		if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
				|| (user.getRole().equalsIgnoreCase("admin") && smscEntryRepository.findByMasterId(username) != null)) {

			// ----------- 5th Chart -----------------------
			smsc_count = sortByDscValue(smsc_count, 15);
			logger.info(messageResourceBundle.getLogMessage("smsc.counter.message"), user.getSystemId(), smsc_count);

			// Set dlr_set = smsc_count.keySet();
			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "Route");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "count");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = smsc_count.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int count = (Integer) smsc_count.get(key);
				// String costStr = new DecimalFormat("0.00000").format(cost);
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", count);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			smscCount.put("cols", colsArr);
			smscCount.put("rows", rowsArr);
			
			logger.info(messageResourceBundle.getLogMessage("smsc.counter.message"), user.getSystemId(), smscCount.toString());
			// ----------- 6th Chart -----------------------
			// smsc_deliver = sortByDscValue(smsc_deliver, 10);
			logger.info(messageResourceBundle.getLogMessage("smsc.deliver.message"), user.getSystemId(), smsc_deliver);

			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "Route");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "Percent");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = smsc_count.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int percent = 0;
				if (smsc_deliver.containsKey(key)) {
					int deliv = (Integer) smsc_deliver.get(key);
					int submit = (Integer) smsc_count.get(key);
					percent = (int) (((double) deliv / (double) submit) * 100);
				} else {
					logger.info(user.getSystemId() + " " + "No Delivery Found: " + key);
				}
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", percent);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			smscDeliver.put("cols", colsArr);
			smscDeliver.put("rows", rowsArr);
			logger.info(messageResourceBundle.getLogMessage("smsc.deliver.message"), user.getSystemId(), smscDeliver.toString());

		}
		if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")) {
			// ----------- 7th Chart -----------------------
			smsc_spam = sortByDscValue(smsc_spam, 10);
			logger.info(messageResourceBundle.getLogMessage("smsc.spam.message"), user.getSystemId(), smsc_spam);

			// Set dlr_set = smsc_count.keySet();
			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "Route");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "count");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = smsc_spam.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int count = (Integer) smsc_spam.get(key);
				// String costStr = new DecimalFormat("0.00000").format(cost);
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", count);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			smscSpam.put("cols", colsArr);
			smscSpam.put("rows", rowsArr);
			logger.info(messageResourceBundle.getLogMessage("smsc.spam.count.message"), user.getSystemId(), smscSpam.toString());

			// ----------- 8th Chart -----------------------
			user_spam = sortByDscValue(user_spam, 10);
			logger.info(messageResourceBundle.getLogMessage("user.spam.message"), user.getSystemId(), user_spam);

			// Set dlr_set = smsc_count.keySet();
			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "User");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "count");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = user_spam.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int count = (Integer) user_spam.get(key);
				// String costStr = new DecimalFormat("0.00000").format(cost);
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", count);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			userSpam.put("cols", colsArr);
			userSpam.put("rows", rowsArr);
			logger.info(messageResourceBundle.getLogMessage("user.spam.count.message"), user.getSystemId(), userSpam.toString());

		}
		responseObj.put(arr);
		responseObj.put(smscCount);
		responseObj.put(smscDeliver);
		responseObj.put(smscSpam);
		responseObj.put(userSpam);
		logger.info(messageResourceBundle.getLogMessage("finished.dashboard.message"), user.getSystemId());

		return ResponseEntity.ok(responseObj.toString());

	}

	private ResponseEntity<?> dashboard(String systemid, int days, DashboardRequest request, String username)
			throws DBException, IOException {
		
		System.out.println(" Dashboard methode -- line 1216");
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
		}
		logger.info(messageResourceBundle.getLogMessage("dashboard.report.message"), user.getSystemId(), days);

		boolean proceed = true;
		Calendar calender = Calendar.getInstance();
		String sql = "";
		if (days > 0) {
			if (days <= 2) {
				sql = "select count(msg_id) as count,username,oprCountry,sender,status,smsc from report_log where ";
			} else {
				sql = "select total as count,username,oprCountry,sender,status,smsc from report_summary where ";
			}
			calender.add(Calendar.DATE, -days);
			if (days == 1) {
				sql += "time ='" + new SimpleDateFormat("yyyy-MM-dd").format(calender.getTime()) + "'";
			} else {
				Calendar end_calender = Calendar.getInstance();
				end_calender.add(Calendar.DATE, -1);
				sql += "time between '" + new SimpleDateFormat("yyyy-MM-dd").format(calender.getTime()) + "' and '"
						+ new SimpleDateFormat("yyyy-MM-dd").format(end_calender.getTime()) + "'";
			}
			if (user.getRole().equalsIgnoreCase("user")) {
				sql += " and username = '" + user.getSystemId() + "'";
			} else {
				if (user.getRole().equalsIgnoreCase("seller") || user.getRole().equalsIgnoreCase("admin")
						|| user.getRole().equalsIgnoreCase("manager")) {
					String users = "";
					Map<Integer, String> map = null;
					if (user.getRole().equalsIgnoreCase("manager")) {
						// SalesDAService salesService = new SalesDAServiceImpl();
						String seller = request.getSeller();
						int seller_id = 0;
						try {
							if (seller != null && Integer.parseInt(seller) > 0) {
								seller_id = Integer.parseInt(seller);
							}
						} catch (Exception ex) {
							logger.error(messageResourceBundle.getLogMessage("invalid.seller.id.message"), user.getSystemId(), seller);

						}
						if (seller_id > 0) {
							map = userService.listUsersUnderSeller(seller_id);
							logger.info(messageResourceBundle.getLogMessage("user.found.under.seller.message"), seller_id, map.values());

						} else {
							map = listUsernamesUnderManager(user.getSystemId());
						}
					} else if (user.getRole().equalsIgnoreCase("seller")) {
						map = userService.listUsersUnderSeller(user.getId());
					} else {
						map = userService.listUsersUnderMaster(user.getSystemId());
						Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
								.get("secondaryMaster").equal(user.getSystemId());
						for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
							UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
							map.put(userEntry.getId(), userEntry.getSystemId());
						}
						map.put(user.getId(), user.getSystemId());
					}
					List<String> list = new ArrayList<String>(map.values());
					if (user.getRole().equalsIgnoreCase("admin")) {
						list.add(user.getSystemId());
						if (smscEntryRepository.findByMasterId(systemid) != null) {
							sql += " and smsc in ('" + String.join("','", listNames(user.getSystemId()).values())
									+ "')";
						}
					}
					for (String user1 : list) {
						users += "'" + user1 + "',";
					}
					if (users.length() > 0) {
						users = users.substring(0, users.length() - 1);
						sql += " and username in (" + users + ")";
					} else {
						proceed = false;
					}
				}
			}
			if (days <= 2) {
				sql += " group by username,oprCountry,sender,status,smsc";
			}
		} else {
			sql = "select count(msg_id) as count,username,oprCountry,sender,status,smsc from report";
			if (!(user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system"))) {
				if (user.getRole().equalsIgnoreCase("seller") || user.getRole().equalsIgnoreCase("manager")
						|| user.getRole().equalsIgnoreCase("admin")) {
					String users = "";
					Map<Integer, String> map = null;
					if (user.getRole().equalsIgnoreCase("seller")) {
						map = userService.listUsersUnderSeller(user.getId());
					} else if (user.getRole().equalsIgnoreCase("manager")) {
						// SalesDAService salesService = new SalesDAServiceImpl();
						String seller = request.getSeller();
						int seller_id = 0;
						try {
							if (seller != null && Integer.parseInt(seller) > 0) {
								seller_id = Integer.parseInt(seller);
							}
						} catch (Exception ex) {
							logger.error(messageResourceBundle.getLogMessage("invalid.seller.id.message"), user.getSystemId(), seller);

						}
						if (seller_id > 0) {
							map = userService.listUsersUnderSeller(seller_id);
							logger.info(messageResourceBundle.getLogMessage("user.found.under.seller.message"), seller_id, map.values());

						} else {
							map = listUsernamesUnderManager(user.getSystemId());
						}
					} else {
						map = userService.listUsersUnderMaster(user.getSystemId());
						Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
								.get("secondaryMaster").equal(user.getSystemId());
						for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
							UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
							map.put(userEntry.getId(), userEntry.getSystemId());
						}
						map.put(user.getId(), user.getSystemId());
					}
					List<String> list = new ArrayList<String>(map.values());
					for (String user1 : list) {
						users += "'" + user1 + "',";
					}
					if (users.length() > 0) {
						users = users.substring(0, users.length() - 1);
						sql += " where username in (" + users + ")";
						if (user.getRole().equalsIgnoreCase("admin")) {
							if (smscEntryRepository.findByMasterId(systemid) != null) {
								sql += " and smsc in ('" + String.join("','", listNames(user.getSystemId()).values())
										+ "')";
							}
						}
					} else {
						proceed = false;
					}
				} else {
					sql += " where username = '" + user.getSystemId() + "'";
				}
			}
			sql += " group by username,oprCountry,sender,status,smsc";
		}
		// ************* Creating Response Object ******************************
		logger.info(messageResourceBundle.getLogMessage("dashboard.sql.message"), user.getSystemId(), sql);

		List<DeliveryDTO> list = null;
		if (proceed) {
			list = getDashboardReport(sql);
		} else {
			list = new ArrayList<DeliveryDTO>();
		}
		logger.info(messageResourceBundle.getLogMessage("dashboard.report.list.size.message"), user.getSystemId(), list.size());

		// Iterator itr = list.iterator();
		int received = 0, processed = 0, delivered = 0;
		Map<String, Integer> country_map = new HashMap<String, Integer>();
		Map<String, Integer> sender_map = new HashMap<String, Integer>();
		Map<String, Integer> user_received_map = new HashMap<String, Integer>();
		Map<String, Integer> user_processed_map = new HashMap<String, Integer>();
		Map<String, Integer> user_delivered_map = new HashMap<String, Integer>();
		Map<String, Integer> smsc_count = new HashMap<String, Integer>();
		Map<String, Integer> smsc_deliver = new HashMap<String, Integer>();
		Map<String, Integer> smsc_spam = new HashMap<String, Integer>();
		Map<String, Integer> user_spam = new HashMap<String, Integer>();
		// Map user_count_map = new HashMap();
		Map<String, Set<String>> sender_user_map = new HashMap<String, Set<String>>();
		DeliveryDTO reportDTO = null;
		while (!list.isEmpty()) {
			// ----------- status Chart ----------------------
			reportDTO = (DeliveryDTO) list.remove(0);
			int submitted = reportDTO.getSubmitted();
			received += submitted;
			if (reportDTO.getStatus() != null) {
				processed += submitted;
				if (reportDTO.getStatus().startsWith("DELIV")) {
					delivered += submitted;
				}
			}
			// ----------- CountryCounter Chart ----------------------
			int c_counter = 0;
			if (country_map.containsKey(reportDTO.getCountry())) {
				c_counter = country_map.get(reportDTO.getCountry());
			}
			c_counter += submitted;
			country_map.put(reportDTO.getCountry(), c_counter);
			// ----------- SenderCounter Chart ----------------------
			int s_counter = 0;
			if (sender_map.containsKey(reportDTO.getSender())) {
				s_counter = sender_map.get(reportDTO.getSender());
			}
			s_counter += submitted;
			sender_map.put(reportDTO.getSender(), s_counter);
			if (!user.getRole().equalsIgnoreCase("user")) {
				Set<String> users_set = null;
				if (sender_user_map.containsKey(reportDTO.getSender())) {
					users_set = sender_user_map.get(reportDTO.getSender());
				} else {
					users_set = new HashSet<String>();
				}
				users_set.add(reportDTO.getUsername());
				sender_user_map.put(reportDTO.getSender(), users_set);
			}
			if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
					|| (user.getRole().equalsIgnoreCase("admin")
							&& (smscEntryRepository.findByMasterId(systemid) != null))) {
				// ------------ Smsc Submit Counter --------------------------
				int ss_counter = 0;
				if (smsc_count.containsKey(reportDTO.getRoute())) {
					ss_counter = smsc_count.get(reportDTO.getRoute());
				}
				ss_counter += submitted;
				smsc_count.put(reportDTO.getRoute(), ss_counter);
				// ------------ Smsc Deliver Counter --------------------------
				if (reportDTO.getStatus() != null && reportDTO.getStatus().startsWith("DEL")) {
					int sd_counter = 0;
					if (smsc_deliver.containsKey(reportDTO.getRoute())) {
						sd_counter = smsc_deliver.get(reportDTO.getRoute());
					}
					sd_counter += submitted;
					smsc_deliver.put(reportDTO.getRoute(), sd_counter);
				}
			}
			if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")) {
				if (reportDTO.getStatus() != null && reportDTO.getStatus().equalsIgnoreCase("B")) {
					// ------------ Smsc Spam Counter --------------------------
					int sb_counter = 0;
					if (smsc_spam.containsKey(reportDTO.getRoute())) {
						sb_counter = smsc_spam.get(reportDTO.getRoute());
					}
					sb_counter += submitted;
					smsc_spam.put(reportDTO.getRoute(), sb_counter);
					// ------------ User Spam Counter --------------------------
					int ub_counter = 0;
					if (user_spam.containsKey(reportDTO.getUsername())) {
						ub_counter = user_spam.get(reportDTO.getUsername());
					}
					ub_counter += submitted;
					user_spam.put(reportDTO.getUsername(), ub_counter);
				}
			}
			if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
					|| user.getRole().equalsIgnoreCase("seller") || user.getRole().equalsIgnoreCase("manager")
					|| user.getRole().equalsIgnoreCase("admin")) {
				// ----------- UserCounter Chart ----------------------
				int user_received = 0, user_processed = 0, user_delivered = 0;
				if (user_received_map.containsKey(reportDTO.getUsername())) {
					user_received = (Integer) user_received_map.get(reportDTO.getUsername());
				}
				user_received += submitted;
				user_received_map.put(reportDTO.getUsername(), user_received);
				if (reportDTO.getStatus() != null) {
					if (user_processed_map.containsKey(reportDTO.getUsername())) {
						user_processed = (Integer) user_processed_map.get(reportDTO.getUsername());
					}
					user_processed += submitted;
					user_processed_map.put(reportDTO.getUsername(), user_processed);
					if (reportDTO.getStatus().startsWith("DELIV")) {
						if (user_delivered_map.containsKey(reportDTO.getUsername())) {
							user_delivered = (Integer) user_delivered_map.get(reportDTO.getUsername());
						}
						user_delivered += submitted;
						user_delivered_map.put(reportDTO.getUsername(), user_delivered);
					}
				}
			}
			reportDTO = null;
		}
		list.clear();
		// --------- first Chart -------------------
		JSONArray responseObj = new JSONArray();
		JSONObject jo = new JSONObject();
		jo.put("counter", (Integer) received);
		jo.put("processed", (Integer) processed);
		jo.put("deliverd", (Integer) delivered);
		responseObj.put(jo);
		// ----------- 2nd Chart -----------------------
		JSONObject json = new JSONObject();
		JSONArray colsArr = new JSONArray();
		JSONObject cols = new JSONObject();
		cols.put("id", "");
		cols.put("label", "oprCountry");
		cols.put("type", "string");
		colsArr.put(cols);
		cols = new JSONObject();
		cols.put("id", "");
		cols.put("label", "count");
		cols.put("type", "number");
		colsArr.put(cols);
		JSONArray rowsArr = new JSONArray();
		Map temp_map = new TreeMap();
		Iterator itr = country_map.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			int count = (Integer) country_map.get(key);
			int network_id = 0;
			try {
				network_id = Integer.parseInt(key);
			} catch (Exception ex) {
			}
			if (GlobalVars.NetworkEntries.containsKey(network_id)) {
				key = GlobalVars.NetworkEntries.get(network_id).getCountry();
			}
			if (temp_map.containsKey(key)) {
				int counter = (Integer) temp_map.get(key);
				count = count + counter;
			}
			temp_map.put(key, count);
		}
		// logger.info(userSessionObject.getSystemId()+" "+"temp_map: " +
		// temp_map.size());
		temp_map = sortByDscValue(temp_map, 10);
		itr = temp_map.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			int count = (Integer) temp_map.get(key);
			// logger.info(userSessionObject.getSystemId()+" "+"Key: "+key+" Count:
			// "+count);
			JSONObject rows = new JSONObject();
			JSONArray rowValArr = new JSONArray();
			JSONObject rowVal = new JSONObject();
			rowVal.put("v", key);
			rowValArr.put(rowVal);
			rowVal = new JSONObject();
			rowVal.put("v", count);
			rowValArr.put(rowVal);
			rows.put("c", rowValArr);
			rowsArr.put(rows);
		}
		// logger.info(userSessionObject.getSystemId()+" "+" <-- Added First ---> ");
		json.put("cols", colsArr);
		json.put("rows", rowsArr);
		responseObj.put(json);
		logger.info(messageResourceBundle.getLogMessage("country.counter.message"), user.getSystemId(), json.toString());

		// ----------- 3rd Chart -----------------------
		sender_map = sortByDscValue(sender_map, 10);
		json = new JSONObject();
		colsArr = new JSONArray();
		cols = new JSONObject();
		// rowsArr = new JSONArray();
		cols.put("id", "");
		cols.put("label", "SenderId");
		cols.put("type", "string");
		colsArr.put(cols);
		cols = new JSONObject();
		cols.put("id", "");
		cols.put("label", "count");
		cols.put("type", "number");
		colsArr.put(cols);
		rowsArr = new JSONArray();
		itr = sender_map.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			int count = (Integer) sender_map.get(key);
			// String costStr = new DecimalFormat("0.00000").format(cost);
			JSONObject rows = new JSONObject();
			JSONArray rowValArr = new JSONArray();
			JSONObject rowVal = new JSONObject();
			if (!user.getRole().equalsIgnoreCase("user")) {
				if (sender_user_map.containsKey(key)) {
					rowVal.put("v", key + "" + sender_user_map.get(key));
				} else {
					rowVal.put("v", key);
				}
			} else {
				rowVal.put("v", key);
			}
			rowValArr.put(rowVal);
			rowVal = new JSONObject();
			rowVal.put("v", count);
			rowValArr.put(rowVal);
			rows.put("c", rowValArr);
			rowsArr.put(rows);
		}
		json.put("cols", colsArr);
		json.put("rows", rowsArr);
		responseObj.put(json);
		logger.info(messageResourceBundle.getLogMessage("sender.count.message"), user.getSystemId(), json.toString());

		// ------------------ 4th chart --------------
		JSONArray arr = new JSONArray();
		JSONObject smscCount = new JSONObject();
		JSONObject smscDeliver = new JSONObject();
		JSONObject smscSpam = new JSONObject();
		JSONObject userSpam = new JSONObject();
		if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
				|| user.getRole().equalsIgnoreCase("seller") || user.getRole().equalsIgnoreCase("manager")
				|| user.getRole().equalsIgnoreCase("admin")) {
			user_received_map = sortByDscValue(user_received_map, 30);
			itr = user_received_map.keySet().iterator();
			while (itr.hasNext()) {
				int user_processed = 0, user_delivered = 0;
				String username1 = (String) itr.next();
				int user_received = (Integer) user_received_map.get(username1);
				if (user_processed_map.containsKey(username1)) {
					user_processed = (Integer) user_processed_map.get(username1);
				}
				if (user_delivered_map.containsKey(username1)) {
					user_delivered = (Integer) user_delivered_map.get(username1);
				}
				logger.info(messageResourceBundle.getLogMessage("user.delivery.status.message"), user.getSystemId(), username1, user_received, user_processed, user_delivered);

				json = new JSONObject();
				json.put("user", username1);
				json.put("counter", user_received);
				json.put("processed", (Integer) user_processed);
				json.put("deliverd", (Integer) user_delivered);
				arr.put(json);
			}
			logger.info(messageResourceBundle.getLogMessage("userwise.counter.message"), user.getSystemId(), arr.toString());

		}
		if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")
				|| (user.getRole().equalsIgnoreCase("admin")
						&& (smscEntryRepository.findByMasterId(systemid) != null))) {
			// ----------- 5th Chart -----------------------
			smsc_count = sortByDscValue(smsc_count, 15);
			logger.info(messageResourceBundle.getLogMessage("smsc.counter.message"), user.getSystemId(), smsc_count);

			// Set dlr_set = smsc_count.keySet();
			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "Route");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "count");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = smsc_count.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int count = (Integer) smsc_count.get(key);
				// String costStr = new DecimalFormat("0.00000").format(cost);
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", count);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			smscCount.put("cols", colsArr);
			smscCount.put("rows", rowsArr);
			logger.info(messageResourceBundle.getLogMessage("smsc.counter.message"), user.getSystemId(), smscCount.toString());

			// ----------- 6th Chart -----------------------
			// smsc_deliver = sortByDscValue(smsc_deliver, 10);
			logger.info(messageResourceBundle.getLogMessage("smsc.deliver.message"), user.getSystemId(), smsc_deliver);

			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "Route");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "Percent");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = smsc_count.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int percent = 0;
				if (smsc_deliver.containsKey(key)) {
					int deliv = (Integer) smsc_deliver.get(key);
					int submit = (Integer) smsc_count.get(key);
					percent = (int) (((double) deliv / (double) submit) * 100);
				} else {
					logger.info(messageResourceBundle.getLogMessage("no.delivery.found.message"), user.getSystemId(), key);

				}
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", percent);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			smscDeliver.put("cols", colsArr);
			smscDeliver.put("rows", rowsArr);
			logger.info(messageResourceBundle.getLogMessage("smsc.deliver.message"), user.getSystemId(), smscDeliver.toString());

		}
		if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")) {
			// ----------- 7th Chart -----------------------
			smsc_spam = sortByDscValue(smsc_spam, 10);
			logger.info(messageResourceBundle.getLogMessage("smsc.spam.message"), user.getSystemId(), smsc_spam);

			// Set dlr_set = smsc_count.keySet();
			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "Route");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "count");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = smsc_spam.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int count = (Integer) smsc_spam.get(key);
				// String costStr = new DecimalFormat("0.00000").format(cost);
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", count);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			smscSpam.put("cols", colsArr);
			smscSpam.put("rows", rowsArr);
			logger.info(messageResourceBundle.getLogMessage("smsc.spam.count.message"), user.getSystemId(), smscSpam.toString());

			// ----------- 8th Chart -----------------------
			user_spam = sortByDscValue(user_spam, 10);
			logger.info(messageResourceBundle.getLogMessage("user.spam.message"), user.getSystemId(), user_spam);

			// Set dlr_set = smsc_count.keySet();
			colsArr = new JSONArray();
			cols = new JSONObject();
			// rowsArr = new JSONArray();
			cols.put("id", "");
			cols.put("label", "User");
			cols.put("type", "string");
			colsArr.put(cols);
			cols = new JSONObject();
			cols.put("id", "");
			cols.put("label", "count");
			cols.put("type", "number");
			colsArr.put(cols);
			rowsArr = new JSONArray();
			itr = user_spam.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				int count = (Integer) user_spam.get(key);
				// String costStr = new DecimalFormat("0.00000").format(cost);
				JSONObject rows = new JSONObject();
				JSONArray rowValArr = new JSONArray();
				JSONObject rowVal = new JSONObject();
				rowVal.put("v", key);
				rowValArr.put(rowVal);
				rowVal = new JSONObject();
				rowVal.put("v", count);
				rowValArr.put(rowVal);
				rows.put("c", rowValArr);
				rowsArr.put(rows);
			}
			userSpam.put("cols", colsArr);
			userSpam.put("rows", rowsArr);
			logger.info(messageResourceBundle.getLogMessage("user.spam.count.message"), user.getSystemId(), userSpam.toString());

		}
		responseObj.put(arr);
		responseObj.put(smscCount);
		responseObj.put(smscDeliver);
		responseObj.put(smscSpam);
		responseObj.put(userSpam);
		logger.info(messageResourceBundle.getLogMessage("finished.dashboard.message"), user.getSystemId());

		return ResponseEntity.ok(responseObj.toString());

	}

	public Map<Integer, String> listNames(String masterId) {
		Map<Integer, String> names = new HashMap<Integer, String>();
		Predicate<Integer, SmscEntry> p = new PredicateBuilderImpl().getEntryObject().get("masterId").equal(masterId);
		for (SmscEntry entry : GlobalVars.SmscEntries.values(p)) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}

	public List getDashboardReport(String query) throws DBException {
		List report = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		DeliveryDTO reportDTO = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				try {
					reportDTO = new DeliveryDTO();
					reportDTO.setSubmitted(rs.getInt("count"));
					reportDTO.setUsername(rs.getString("username"));
					reportDTO.setSender(rs.getString("sender"));
					reportDTO.setCountry(rs.getString("oprCountry"));
					reportDTO.setStatus(rs.getString("status"));
					reportDTO.setRoute(rs.getString("smsc"));
					report.add(reportDTO);
				} catch (Exception sqle) {
					logger.error(messageResourceBundle.getLogMessage("database.error.message"), sqle.getMessage(), sqle);

				}
			}
		} catch (SQLException sqle) {
			logger.error(messageResourceBundle.getLogMessage("unexpected.error "), sqle.getMessage(), sqle);

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

	private <K, V extends Comparable<? super V>> Map<K, V> sortByDscValue(Map<K, V> map, int limit) {
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();
		st.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(limit)
				.forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
		return result;
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
		List<SalesEntry> list = listSellersUnderManager(mgrId);
		for (SalesEntry entry : list) {
			map.put(entry.getId(), entry.getUsername());
		}
		return map;
	}

	public List<SalesEntry> listSellersUnderManager(String mgrId) {
		return salesRepository.findByMasterIdAndRole(mgrId, "seller");
	}

}
