package com.hti.smpp.common.database;

import java.awt.Color;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.CustomReportDTO;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.service.impl.UserDAServiceImpl;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.logica.smpp.Data;

import jakarta.persistence.EntityManager;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@Service
public class DataBase {

	@Autowired
	private EntityManager entityManager;

	private static final Logger logger = LoggerFactory.getLogger(DataBase.class);

	private boolean summary;
	private String to_gmt;
	private String from_gmt;
	private String template_file = IConstants.FORMAT_DIR + "report//BalanceReport.jrxml";
	private String template_summary_file = IConstants.FORMAT_DIR + "report//BalanceSummaryReport.jrxml";
	private Locale locale = null;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private SalesRepository salesRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;

	public List<BulkEntry> getReportList(CustomReportForm customReportForm, int id) {
		UserDAService userDAService = new UserDAServiceImpl();
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(id);
		if (webMasterEntry == null) {
			throw new NotFoundException("web MasterEntry  not fount username {}" + id);
		}
		String to_gmt = null;
		String from_gmt = null;
		String sql = "select ";
		if (!webMasterEntry.getGmt().equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = webMasterEntry.getGmt().replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
			sql += "CONVERT_TZ(createdOn,'" + from_gmt + "','" + to_gmt + "'),";
		} else {
			sql += "createdOn,";
		}
		sql += "batch_id,system_id,sender_id,totalNum,pending,firstNum,delay,reqType,server_id,ston ,snpi ,alert,alert_number,expiry_hour,content,msg_type,campaign_name from batch_unprocess where";
		if (to_gmt != null) {
			sql += " createdOn between CONVERT_TZ('" + customReportForm.getSday() + " 00:00:00','" + to_gmt + "','"
					+ from_gmt + "') and CONVERT_TZ('" + customReportForm.getEday() + " 23:59:59','" + to_gmt + "','"
					+ from_gmt + "')";
		} else {
			if (customReportForm.getSday().equalsIgnoreCase(customReportForm.getEday())) {
				sql += " DATE(createdOn)= '" + customReportForm.getSday() + "'";
			} else {
				sql += " DATE(createdOn) between '" + customReportForm.getSday() + "' and '"
						+ customReportForm.getEday() + "'";
			}
		}
		if (customReportForm.getClientId() != null && customReportForm.getClientId().length() > 0) {
			if (!customReportForm.getClientId().contains("ALL")) {
				sql += " and system_id = '" + customReportForm.getClientId() + "' ";
			}
		}
		if (customReportForm.getSenderId() != null && customReportForm.getSenderId().length() > 0) {
			String sender = customReportForm.getSenderId();
			if (sender.contains("%")) {
				sql += " and sender_id like '" + sender + "' ";
			} else if (sender.contains(",")) {
				StringTokenizer strTokens = new StringTokenizer(sender, ",");
				String destlist = "";
				while (strTokens.hasMoreTokens()) {
					destlist += "'" + strTokens.nextToken() + "',";
				}
				destlist = destlist.substring(0, destlist.length() - 1);
				sql += " and sender_id in (" + destlist + ") ";
			} else {
				sql += " and sender_id = '" + sender + "' ";
			}
		}
		System.out.println("SQL: " + sql);
		List<BulkEntry> list = entityManager.createNativeQuery(sql, BulkEntry.class).getResultList();
		return list;
	}

	public Map<String, List<DeliveryDTO>> getBalanceReportList(CustomReportForm customReportForm, String username) {
		UserDAService userDAService = new UserDAServiceImpl();
		if (customReportForm.getClientId() == null) {
			return null;
		}
		Optional<UserEntry> usersOptional = userRepository.findBySystemId(username);
		if (!usersOptional.isPresent()) {
			throw new NotFoundException("user not fout with system id" + username);
		}

		UserEntry user = usersOptional.get();
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
		if (webMasterEntry == null) {
			throw new NotFoundException("web MasterEntry  not fount username {}" + user.getId());
		}
		Map<String, List<DeliveryDTO>> final_map = new HashMap<String, List<DeliveryDTO>>();
		logger.info(username + " Report Based On Criteria");
		List<String> users = null;
		String query = null;
		String country = customReportForm.getCountry();
		String operator = customReportForm.getOperator();
		String startDate = customReportForm.getSday();
		String endDate = customReportForm.getEday();
		if (customReportForm.getReportType().equalsIgnoreCase("Summary")) {
			summary = true;
		}
		List<DeliveryDTO> list = null;
		List<DeliveryDTO> final_list = new ArrayList<DeliveryDTO>();
		if (!webMasterEntry.getGmt().equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = webMasterEntry.getGmt().replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
		}
		if (customReportForm.getClientId().equalsIgnoreCase("All")) {
			String role = user.getRole();
			if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
				users = new ArrayList<String>(userDAService.listUsers().values());
			} else if (role.equalsIgnoreCase("admin")) {
				users = new ArrayList<String>(userDAService.listUsersUnderMaster(user.getSystemId()).values());
				users.add(user.getSystemId());
				Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
						.get("secondaryMaster").equal(user.getSystemId());
				for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
					users.add(userEntry.getSystemId());
				}
			} else if (role.equalsIgnoreCase("seller")) {
				users = new ArrayList<String>(userDAService.listUsersUnderSeller(user.getId()).values());
			} else if (role.equalsIgnoreCase("manager")) {
				// SalesDAService salesService = new SalesDAServiceImpl();
				users = new ArrayList<String>(listUsernamesUnderManager(user.getSystemId()).values());
			}
			logger.info(user.getSystemId() + " Under Users: " + users.size());
		} else {
			users = new ArrayList<String>();
			users.add(customReportForm.getClientId());
		}
		if (users != null && !users.isEmpty()) {
			Map<String, List<DeliveryDTO>> map = new HashMap<String, List<DeliveryDTO>>();
			for (String report_user : users) {
				logger.info(user.getSystemId() + " Checking Report For " + report_user);
				if (summary) {
					query = "select count(msg_id) as count,SUM(cost) as cost_sum from mis_" + report_user;
				} else {
					query = "select count(msg_id) as count,DATE(submitted_time) as time,oprcountry,SUM(cost) as cost_sum from mis_"
							+ report_user;
				}
				query += " where msg_id not in(select msg_id from smsc_in) and msg_id not in(select msg_id from unprocessed) and ";
				if (to_gmt != null) {
					SimpleDateFormat client_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					client_formatter.setTimeZone(TimeZone.getTimeZone(webMasterEntry.getGmt()));
					SimpleDateFormat local_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						String start_msg_id = local_formatter.format(client_formatter.parse(startDate));
						String end_msg_id = local_formatter.format(client_formatter.parse(endDate));
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						start_msg_id = start_msg_id.substring(2);
						start_msg_id += "0000000";
						end_msg_id = end_msg_id.replaceAll("-", "");
						end_msg_id = end_msg_id.replaceAll(" ", "");
						end_msg_id = end_msg_id.replaceAll(":", "");
						end_msg_id = end_msg_id.substring(2);
						end_msg_id += "0000000";
						query += "msg_id between " + start_msg_id + " and " + end_msg_id;
					} catch (Exception e) {
						query += "submitted_time between CONVERT_TZ('" + startDate + "','" + to_gmt + "','" + from_gmt
								+ "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt + "')";
					}
				} else {
					if (startDate.equalsIgnoreCase(endDate)) {
						String start_msg_id = startDate.substring(2);
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						query += "msg_id like '" + start_msg_id + "%'";
					} else {
						String start_msg_id = startDate.substring(2);
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						start_msg_id += "0000000";
						String end_msg_id = endDate.substring(2);
						end_msg_id = end_msg_id.replaceAll("-", "");
						end_msg_id = end_msg_id.replaceAll(" ", "");
						end_msg_id = end_msg_id.replaceAll(":", "");
						end_msg_id += "0000000";
						/*
						 * String[] end_date = endDate.split("-"); Calendar calendar =
						 * Calendar.getInstance(); calendar.set(Calendar.DATE,
						 * Integer.parseInt(end_date[2])); calendar.set(Calendar.MONTH,
						 * (Integer.parseInt(end_date[1])) - 1); calendar.set(Calendar.YEAR,
						 * Integer.parseInt(end_date[0])); calendar.add(Calendar.DATE, 1); String
						 * end_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime()) +
						 * "0000000000000";
						 */
						query += "msg_id between " + start_msg_id + " and " + end_msg_id + "";
					}
				}
				if (country != null && country.length() > 0) {
					Predicate<Integer, NetworkEntry> p = null;
					String oprCountry = "";
					if (operator.equalsIgnoreCase("All")) {
						p = new PredicateBuilderImpl().getEntryObject().get("mcc").equal(country);
					} else {
						EntryObject e = new PredicateBuilderImpl().getEntryObject();
						p = e.get("mcc").equal(country).and(e.get("mnc").equal(operator));
					}
					for (int cc : GlobalVars.NetworkEntries.keySet(p)) {
						oprCountry += "'" + cc + "',";
					}
					if (oprCountry.length() > 0) {
						oprCountry = oprCountry.substring(0, oprCountry.length() - 1);
						query += " and oprCountry in (" + oprCountry + ")";
					}
				}
				if (summary) {
					logger.info(user.getSystemId() + " ReportSQL:" + query);
					list = getConsumptionSummaryReport(query, report_user);
				} else {
					query += " group by time,oprcountry";
					logger.info(user.getSystemId() + " ReportSQL:" + query);
					list = getConsumptionReport(query, report_user);
				}
				logger.info(user.getSystemId() + " list:" + list.size());
				if (list != null && !list.isEmpty()) {
					System.out.println(report_user + " Report List Size --> " + list.size());
					map.put(report_user, list);
				}
			}
			logger.info(user.getSystemId() + " <- Checking For Unprocessed ->");
			String unproc_query = null;
			if (summary) {
				unproc_query = "select count(msg_id) as count,username,SUM(cost) as cost_sum from table_name where ";
			} else {
				unproc_query = "select count(msg_id) as count,username,DATE(time) as time,oprcountry,SUM(cost) as cost_sum from table_name where ";
			}
			unproc_query += "username in('" + String.join("','", users) + "') and ";
			if (to_gmt != null) {
				SimpleDateFormat client_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				client_formatter.setTimeZone(TimeZone.getTimeZone(webMasterEntry.getGmt()));
				SimpleDateFormat local_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					String start_msg_id = local_formatter.format(client_formatter.parse(startDate));
					String end_msg_id = local_formatter.format(client_formatter.parse(endDate));
					start_msg_id = start_msg_id.replaceAll("-", "");
					start_msg_id = start_msg_id.replaceAll(" ", "");
					start_msg_id = start_msg_id.replaceAll(":", "");
					start_msg_id = start_msg_id.substring(2);
					start_msg_id += "0000000";
					end_msg_id = end_msg_id.replaceAll("-", "");
					end_msg_id = end_msg_id.replaceAll(" ", "");
					end_msg_id = end_msg_id.replaceAll(":", "");
					end_msg_id = end_msg_id.substring(2);
					end_msg_id += "0000000";
					unproc_query += "msg_id between " + start_msg_id + " and " + end_msg_id;
				} catch (Exception e) {
					unproc_query += "time between CONVERT_TZ('" + startDate + "','" + to_gmt + "','" + from_gmt
							+ "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt + "')";
				}
			} else {
				if (startDate.equalsIgnoreCase(endDate)) {
					String start_msg_id = startDate.substring(2);
					start_msg_id = start_msg_id.replaceAll("-", "");
					start_msg_id = start_msg_id.replaceAll(" ", "");
					start_msg_id = start_msg_id.replaceAll(":", "");
					unproc_query += "msg_id like '" + start_msg_id + "%'";
				} else {
					String start_msg_id = startDate.substring(2);
					start_msg_id = start_msg_id.replaceAll("-", "");
					start_msg_id = start_msg_id.replaceAll(" ", "");
					start_msg_id = start_msg_id.replaceAll(":", "");
					start_msg_id += "0000000";
					String end_msg_id = endDate.substring(2);
					end_msg_id = end_msg_id.replaceAll("-", "");
					end_msg_id = end_msg_id.replaceAll(" ", "");
					end_msg_id = end_msg_id.replaceAll(":", "");
					end_msg_id += "0000000";
					/*
					 * String[] end_date = endDate.split("-"); Calendar calendar =
					 * Calendar.getInstance(); calendar.set(Calendar.DATE,
					 * Integer.parseInt(end_date[2])); calendar.set(Calendar.MONTH,
					 * (Integer.parseInt(end_date[1])) - 1); calendar.set(Calendar.YEAR,
					 * Integer.parseInt(end_date[0])); calendar.add(Calendar.DATE, 1); String
					 * end_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime()) +
					 * "0000000000000";
					 */
					unproc_query += "msg_id between " + start_msg_id + " and " + end_msg_id + "";
				}
			}
			if (country != null && country.length() > 0) {
				Predicate<Integer, NetworkEntry> p = null;
				String oprCountry = "";
				if (operator.equalsIgnoreCase("All")) {
					p = new PredicateBuilderImpl().getEntryObject().get("mcc").equal(country);
				} else {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					p = e.get("mcc").equal(country).and(e.get("mnc").equal(operator));
				}
				for (int cc : GlobalVars.NetworkEntries.keySet(p)) {
					oprCountry += "'" + cc + "',";
				}
				if (oprCountry.length() > 0) {
					oprCountry = oprCountry.substring(0, oprCountry.length() - 1);
					unproc_query += " and oprCountry in (" + oprCountry + ")";
				}
			}
			if (summary) {
				unproc_query += " group by username";
			} else {
				unproc_query += " group by username,time,oprcountry";
			}
			Map<String, List<DeliveryDTO>> first = null;
			Map<String, List<DeliveryDTO>> second = null;
			if (summary) {
				first = getConsumptionSummaryReport(unproc_query.replaceFirst("table_name", "smsc_in"));
				second = getConsumptionSummaryReport(unproc_query.replaceFirst("table_name", "unprocessed"));
			} else {
				first = getConsumptionReport(unproc_query.replaceFirst("table_name", "smsc_in"));
				second = getConsumptionReport(unproc_query.replaceFirst("table_name", "unprocessed"));
			}
			/*
			 * Predicate<Integer, UserEntry> p = new
			 * PredicateBuilderImpl().getEntryObject().get("systemId") .in(users.toArray(new
			 * String[0]));
			 */
			for (String username1 : users) {
				List<DeliveryDTO> inner = null;
				if (map.containsKey(username1)) {
					inner = map.get(username1);
				}
				if (first.containsKey(username1)) {
					if (inner == null) {
						inner = first.get(username1);
					} else {
						inner.addAll(first.get(username1));
					}
				}
				if (second.containsKey(username1)) {
					if (inner == null) {
						inner = second.get(username1);
					} else {
						inner.addAll(second.get(username1));
					}
				}
				if (inner != null) {
					final_map.put(username1, inner);
				}
			}
		}
		logger.info(user.getSystemId() + " End Based On Criteria. Final Report Size: " + final_list.size());
		return final_map;
	}

	public Map<Integer, SalesEntry> listSellerMappedManager() {

		Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
		List<SalesEntry> list = salesRepository.findByRole("manager");
		for (SalesEntry entry : list) {
			Set<Integer> sellers = listNamesUnderManager(entry.getUsername()).keySet();
			for (int seller_id : sellers) {
				map.put(seller_id, entry);
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
		return salesRepository.findByMasterIdAndRole(mgrId, "seller");
	}

	public Map<Integer, String> listUsernamesUnderManager(String mgrId) {
		UserDAService userDAService = new UserDAServiceImpl();
		Map<Integer, String> map = listNamesUnderManager(mgrId);
		Map<Integer, String> users = new HashMap<Integer, String>();
		for (Integer seller_id : map.keySet()) {
			users.putAll(userDAService.listUsersUnderSeller(seller_id));
		}
		return users;
	}

	public List<DeliveryDTO> getConsumptionSummaryReport(String query, String reportUser) {
		List<DeliveryDTO> list = new ArrayList<>();
		try {
			list = jdbcTemplate.query(query, (rs, rowNum) -> {
				DeliveryDTO entry = new DeliveryDTO();
				entry.setUsername(reportUser);
				entry.setSubmitted(rs.getInt("count"));
				entry.setCost(rs.getDouble("cost_sum"));
				return entry;
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<DeliveryDTO> getConsumptionReport(String query, String reportUser) {
		List<DeliveryDTO> list = new ArrayList<>();
		try {
			list = jdbcTemplate.query(query, (rs, rowNum) -> {
				DeliveryDTO entry = new DeliveryDTO();
				entry.setUsername(reportUser);
				entry.setSubmitted(rs.getInt("count"));
				entry.setCost(rs.getDouble("cost_sum"));
				entry.setTime(rs.getString("time"));

				String oprCountry = rs.getString("oprCountry");
				int network_id = 0;
				try {
					network_id = Integer.parseInt(oprCountry);
				} catch (Exception ex) {
				}

				String country, operator;
				if (GlobalVars.NetworkEntries.containsKey(network_id)) {
					NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
					country = network.getCountry();
					operator = network.getOperator();
				} else {
					country = oprCountry;
					operator = oprCountry;
				}
				entry.setCountry(country);
				entry.setOperator(operator);

				return entry;
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public Map<String, List<DeliveryDTO>> getConsumptionSummaryReport(String query) {
		Map<String, List<DeliveryDTO>> map = new HashMap<>();
		try (Connection con = jdbcTemplate.getDataSource().getConnection();
				PreparedStatement pStmt = con.prepareStatement(query);
				ResultSet rs = pStmt.executeQuery()) {

			while (rs.next()) {
				DeliveryDTO entry = new DeliveryDTO();
				String username = rs.getString("username");
				List<DeliveryDTO> list = map.computeIfAbsent(username, k -> new ArrayList<>());

				entry.setSubmitted(rs.getInt("count"));
				entry.setUsername(username);
				entry.setCost(rs.getDouble("cost_sum"));

				list.add(entry);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return map;
	}

	public Map<String, List<DeliveryDTO>> getConsumptionReport(String query) {
		Map<String, List<DeliveryDTO>> map = new HashMap<>();
		try (Connection con = jdbcTemplate.getDataSource().getConnection();
				PreparedStatement pStmt = con.prepareStatement(query);
				ResultSet rs = pStmt.executeQuery()) {

			while (rs.next()) {
				DeliveryDTO entry = new DeliveryDTO();
				String username = rs.getString("username");
				List<DeliveryDTO> list = map.computeIfAbsent(username, k -> new ArrayList<>());

				entry.setSubmitted(rs.getInt("count"));
				entry.setUsername(username);
				entry.setCost(rs.getDouble("cost_sum"));
				entry.setTime(rs.getString("time"));

				String oprCountry = rs.getString("oprCountry");
				int network_id = 0;
				try {
					network_id = Integer.parseInt(oprCountry);
				} catch (Exception ex) {
				}
				String country, operator;
				if (GlobalVars.NetworkEntries.containsKey(network_id)) {
					NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
					country = network.getCountry();
					operator = network.getOperator();
				} else {
					country = oprCountry;
					operator = oprCountry;
				}
				entry.setCountry(country);
				entry.setOperator(operator);

				list.add(entry);
				map.put(username, list);
			}
		} catch (SQLException sqle) {
			// Handle or log the exception
			sqle.printStackTrace();
		}

		return map;
	}

	public Map<String, List<DeliveryDTO>> getReportListFile(String username, CustomReportForm customReportForm) {
		if (customReportForm.getClientId() == null) {
			return null;
		}
		Optional<UserEntry> usersOptional = userRepository.findBySystemId(username);
		if (!usersOptional.isPresent()) {
			throw new NotFoundException("user not fout with system id" + username);
		}
		UserDAService userDAService = new UserDAServiceImpl();
		UserEntry user = usersOptional.get();
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
		if (webMasterEntry == null) {
			throw new NotFoundException("web MasterEntry  not fount username {}" + user.getId());
		}
		Map<String, List<DeliveryDTO>> final_map = new HashMap<String, List<DeliveryDTO>>();
		logger.info(user.getSystemId() + " Report Based On Criteria");
		List<String> users = null;
		String query = null;
		String country = customReportForm.getCountry();
		String operator = customReportForm.getOperator();
		String startDate = customReportForm.getSday();
		String endDate = customReportForm.getEday();
		if (customReportForm.getReportType().equalsIgnoreCase("Summary")) {
			summary = true;
		}
		List<DeliveryDTO> list = null;
		List<DeliveryDTO> final_list = new ArrayList<DeliveryDTO>();
		if (!webMasterEntry.getGmt().equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = webMasterEntry.getGmt().replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
		}
		if (customReportForm.getClientId().equalsIgnoreCase("All")) {
			String role = user.getRole();
			if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
				users = new ArrayList<String>(userDAService.listUsers().values());
			} else if (role.equalsIgnoreCase("admin")) {
				users = new ArrayList<String>(userDAService.listUsersUnderMaster(user.getSystemId()).values());
				users.add(user.getSystemId());
				Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
						.get("secondaryMaster").equal(user.getSystemId());
				for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
					users.add(userEntry.getSystemId());
				}
			} else if (role.equalsIgnoreCase("seller")) {
				users = new ArrayList<String>(userDAService.listUsersUnderSeller(user.getId()).values());
			} else if (role.equalsIgnoreCase("manager")) {
				// SalesDAService salesService = new SalesDAServiceImpl();
				users = new ArrayList<String>(listUsernamesUnderManager(user.getSystemId()).values());
			}
			logger.info(user.getSystemId() + " Under Users: " + users.size());
		} else {
			users = new ArrayList<String>();
			users.add(customReportForm.getClientId());
		}
		if (users != null && !users.isEmpty()) {
			Map<String, List<DeliveryDTO>> map = new HashMap<String, List<DeliveryDTO>>();
			for (String report_user : users) {
				logger.info(user.getSystemId() + " Checking Report For " + report_user);
				if (summary) {
					query = "select count(msg_id) as count,SUM(cost) as cost_sum from mis_" + report_user;
				} else {
					query = "select count(msg_id) as count,DATE(submitted_time) as time,oprcountry,SUM(cost) as cost_sum from mis_"
							+ report_user;
				}
				query += " where msg_id not in(select msg_id from smsc_in) and msg_id not in(select msg_id from unprocessed) and ";
				if (to_gmt != null) {
					SimpleDateFormat client_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					client_formatter.setTimeZone(TimeZone.getTimeZone(webMasterEntry.getGmt()));
					SimpleDateFormat local_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						String start_msg_id = local_formatter.format(client_formatter.parse(startDate));
						String end_msg_id = local_formatter.format(client_formatter.parse(endDate));
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						start_msg_id = start_msg_id.substring(2);
						start_msg_id += "0000000";
						end_msg_id = end_msg_id.replaceAll("-", "");
						end_msg_id = end_msg_id.replaceAll(" ", "");
						end_msg_id = end_msg_id.replaceAll(":", "");
						end_msg_id = end_msg_id.substring(2);
						end_msg_id += "0000000";
						query += "msg_id between " + start_msg_id + " and " + end_msg_id;
					} catch (Exception e) {
						query += "submitted_time between CONVERT_TZ('" + startDate + "','" + to_gmt + "','" + from_gmt
								+ "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt + "')";
					}
				} else {
					if (startDate.equalsIgnoreCase(endDate)) {
						String start_msg_id = startDate.substring(2);
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						query += "msg_id like '" + start_msg_id + "%'";
					} else {
						String start_msg_id = startDate.substring(2);
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						start_msg_id += "0000000";
						String end_msg_id = endDate.substring(2);
						end_msg_id = end_msg_id.replaceAll("-", "");
						end_msg_id = end_msg_id.replaceAll(" ", "");
						end_msg_id = end_msg_id.replaceAll(":", "");
						end_msg_id += "0000000";
						/*
						 * String[] end_date = endDate.split("-"); Calendar calendar =
						 * Calendar.getInstance(); calendar.set(Calendar.DATE,
						 * Integer.parseInt(end_date[2])); calendar.set(Calendar.MONTH,
						 * (Integer.parseInt(end_date[1])) - 1); calendar.set(Calendar.YEAR,
						 * Integer.parseInt(end_date[0])); calendar.add(Calendar.DATE, 1); String
						 * end_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime()) +
						 * "0000000000000";
						 */
						query += "msg_id between " + start_msg_id + " and " + end_msg_id + "";
					}
				}
				if (country != null && country.length() > 0) {
					Predicate<Integer, NetworkEntry> p = null;
					String oprCountry = "";
					if (operator.equalsIgnoreCase("All")) {
						p = new PredicateBuilderImpl().getEntryObject().get("mcc").equal(country);
					} else {
						EntryObject e = new PredicateBuilderImpl().getEntryObject();
						p = e.get("mcc").equal(country).and(e.get("mnc").equal(operator));
					}
					for (int cc : GlobalVars.NetworkEntries.keySet(p)) {
						oprCountry += "'" + cc + "',";
					}
					if (oprCountry.length() > 0) {
						oprCountry = oprCountry.substring(0, oprCountry.length() - 1);
						query += " and oprCountry in (" + oprCountry + ")";
					}
				}
				if (summary) {
					logger.info(user.getSystemId() + " ReportSQL:" + query);
					list = getConsumptionSummaryReport(query, report_user);
				} else {
					query += " group by time,oprcountry";
					logger.info(user.getSystemId() + " ReportSQL:" + query);
					list = getConsumptionReport(query, report_user);
				}
				logger.info(user.getSystemId() + " list:" + list.size());
				if (list != null && !list.isEmpty()) {
					System.out.println(report_user + " Report List Size --> " + list.size());
					map.put(report_user, list);
				}
			}
			logger.info(user.getSystemId() + " <- Checking For Unprocessed ->");
			String unproc_query = null;
			if (summary) {
				unproc_query = "select count(msg_id) as count,username,SUM(cost) as cost_sum from table_name where ";
			} else {
				unproc_query = "select count(msg_id) as count,username,DATE(time) as time,oprcountry,SUM(cost) as cost_sum from table_name where ";
			}
			unproc_query += "username in('" + String.join("','", users) + "') and ";
			if (to_gmt != null) {
				SimpleDateFormat client_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				client_formatter.setTimeZone(TimeZone.getTimeZone(webMasterEntry.getGmt()));
				SimpleDateFormat local_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					String start_msg_id = local_formatter.format(client_formatter.parse(startDate));
					String end_msg_id = local_formatter.format(client_formatter.parse(endDate));
					start_msg_id = start_msg_id.replaceAll("-", "");
					start_msg_id = start_msg_id.replaceAll(" ", "");
					start_msg_id = start_msg_id.replaceAll(":", "");
					start_msg_id = start_msg_id.substring(2);
					start_msg_id += "0000000";
					end_msg_id = end_msg_id.replaceAll("-", "");
					end_msg_id = end_msg_id.replaceAll(" ", "");
					end_msg_id = end_msg_id.replaceAll(":", "");
					end_msg_id = end_msg_id.substring(2);
					end_msg_id += "0000000";
					unproc_query += "msg_id between " + start_msg_id + " and " + end_msg_id;
				} catch (Exception e) {
					unproc_query += "time between CONVERT_TZ('" + startDate + "','" + to_gmt + "','" + from_gmt
							+ "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt + "')";
				}
			} else {
				if (startDate.equalsIgnoreCase(endDate)) {
					String start_msg_id = startDate.substring(2);
					start_msg_id = start_msg_id.replaceAll("-", "");
					start_msg_id = start_msg_id.replaceAll(" ", "");
					start_msg_id = start_msg_id.replaceAll(":", "");
					unproc_query += "msg_id like '" + start_msg_id + "%'";
				} else {
					String start_msg_id = startDate.substring(2);
					start_msg_id = start_msg_id.replaceAll("-", "");
					start_msg_id = start_msg_id.replaceAll(" ", "");
					start_msg_id = start_msg_id.replaceAll(":", "");
					start_msg_id += "0000000";
					String end_msg_id = endDate.substring(2);
					end_msg_id = end_msg_id.replaceAll("-", "");
					end_msg_id = end_msg_id.replaceAll(" ", "");
					end_msg_id = end_msg_id.replaceAll(":", "");
					end_msg_id += "0000000";
					/*
					 * String[] end_date = endDate.split("-"); Calendar calendar =
					 * Calendar.getInstance(); calendar.set(Calendar.DATE,
					 * Integer.parseInt(end_date[2])); calendar.set(Calendar.MONTH,
					 * (Integer.parseInt(end_date[1])) - 1); calendar.set(Calendar.YEAR,
					 * Integer.parseInt(end_date[0])); calendar.add(Calendar.DATE, 1); String
					 * end_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime()) +
					 * "0000000000000";
					 */
					unproc_query += "msg_id between " + start_msg_id + " and " + end_msg_id + "";
				}
			}
			if (country != null && country.length() > 0) {
				Predicate<Integer, NetworkEntry> p = null;
				String oprCountry = "";
				if (operator.equalsIgnoreCase("All")) {
					p = new PredicateBuilderImpl().getEntryObject().get("mcc").equal(country);
				} else {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					p = e.get("mcc").equal(country).and(e.get("mnc").equal(operator));
				}
				for (int cc : GlobalVars.NetworkEntries.keySet(p)) {
					oprCountry += "'" + cc + "',";
				}
				if (oprCountry.length() > 0) {
					oprCountry = oprCountry.substring(0, oprCountry.length() - 1);
					unproc_query += " and oprCountry in (" + oprCountry + ")";
				}
			}
			if (summary) {
				unproc_query += " group by username";
			} else {
				unproc_query += " group by username,time,oprcountry";
			}
			Map<String, List<DeliveryDTO>> first = null;
			Map<String, List<DeliveryDTO>> second = null;
			if (summary) {
				first = getConsumptionSummaryReport(unproc_query.replaceFirst("table_name", "smsc_in"));
				second = getConsumptionSummaryReport(unproc_query.replaceFirst("table_name", "unprocessed"));
			} else {
				first = getConsumptionReport(unproc_query.replaceFirst("table_name", "smsc_in"));
				second = getConsumptionReport(unproc_query.replaceFirst("table_name", "unprocessed"));
			}
			/*
			 * Predicate<Integer, UserEntry> p = new
			 * PredicateBuilderImpl().getEntryObject().get("systemId") .in(users.toArray(new
			 * String[0]));
			 */
			for (String username1 : users) {
				List<DeliveryDTO> inner = null;
				if (map.containsKey(username1)) {
					inner = map.get(username1);
				}
				if (first.containsKey(username1)) {
					if (inner == null) {
						inner = first.get(username1);
					} else {
						inner.addAll(first.get(username1));
					}
				}
				if (second.containsKey(username1)) {
					if (inner == null) {
						inner = second.get(username1);
					} else {
						inner.addAll(second.get(username1));
					}
				}
				if (inner != null) {
					final_map.put(username1, inner);
				}
			}
		}
		logger.info(user.getSystemId() + " End Based On Criteria. Final Report Size: " + final_list.size());
		return final_map;
	}

	public JasperPrint getJasperPrint(Map<String, List<DeliveryDTO>> reportList, boolean paging) throws JRException {
		System.out.println("Creating Design");
		JasperDesign design = null;
		if (summary) {
			design = JRXmlLoader.load(template_summary_file);
		} else {
			design = JRXmlLoader.load(template_file);
		}
		System.out.println("<--- Compiling -->");
		JasperReport report = JasperCompileManager.compileReport(design);
		List<DeliveryDTO> final_list = new ArrayList<DeliveryDTO>();
		if (summary) {
			for (String system_id : reportList.keySet()) {
				UserEntry userEntry = userRepository.findBySystemId(system_id).get();
				if (userEntry != null) {
					BalanceEntry balanceEntry = GlobalVars.BalanceEntries.get(userEntry.getId());
					DeliveryDTO value = new DeliveryDTO();
					value.setUsername(system_id);
					value.setCurrency(userEntry.getCurrency());
					for (DeliveryDTO inner : reportList.get(system_id)) {
						if (balanceEntry.getWalletFlag().equalsIgnoreCase("no")) {
							value.setConsumption(value.getConsumption() + inner.getSubmitted());
						} else {
							value.setConsumption(value.getConsumption() + inner.getCost());
						}
					}
					final_list.add(value);
				}
			}
		} else {
			Map<String, DeliveryDTO> keyValue = null;
			for (String system_id : reportList.keySet()) {
				UserEntry userEntry = userRepository.findBySystemId(system_id).get();
				if (userEntry != null) {
					BalanceEntry balanceEntry = GlobalVars.BalanceEntries.get(userEntry.getId());
					keyValue = new HashMap<String, DeliveryDTO>();
					for (DeliveryDTO inner : reportList.get(system_id)) {
						DeliveryDTO value = null;
						if (keyValue
								.containsKey(inner.getTime() + "#" + inner.getCountry() + "#" + inner.getOperator())) {
							value = keyValue
									.get(inner.getTime() + "#" + inner.getCountry() + "#" + inner.getOperator());
						} else {
							value = new DeliveryDTO();
							value.setCurrency(userEntry.getCurrency());
							value.setCountry(inner.getCountry());
							value.setOperator(inner.getOperator());
							value.setTime(inner.getTime());
							value.setUsername(system_id);
						}
						if (balanceEntry.getWalletFlag().equalsIgnoreCase("no")) {
							value.setConsumption(value.getConsumption() + inner.getSubmitted());
						} else {
							value.setConsumption(value.getConsumption() + inner.getCost());
						}
						keyValue.put(inner.getTime() + "#" + inner.getCountry() + "#" + inner.getOperator(), value);
					}
					final_list.addAll(keyValue.values());
				}
			}
			final_list = sortList(final_list);
		}
		// System.out.println("<-- Preparing Charts --> ");
		// -------------------------------------------------------------
		System.out.println("<-- Preparing report --> ");
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(final_list);
		Map parameters = new HashMap();
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		JasperPrint print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		return print;
	}

	private List<DeliveryDTO> sortList(List<DeliveryDTO> list) {
		Comparator<DeliveryDTO> comparator = null;
		comparator = Comparator.comparing(DeliveryDTO::getUsername).thenComparing(DeliveryDTO::getTime)
				.thenComparing(DeliveryDTO::getCountry).thenComparing(DeliveryDTO::getOperator);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	public JasperPrint getJasperPrint(List<DeliveryDTO> reportList, boolean paging, String username)
			throws JRException {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isEmpty()) {
			throw new NotFoundException("user not found username{}" + username);
		}
		JasperPrint print = null;
		JasperReport report = null;
		Map parameters = new HashMap();
		JasperDesign design = JRXmlLoader.load(template_file);
		report = JasperCompileManager.compileReport(design);
		// ---------- Sorting list ----------------------------
		reportList = sortListByMessageId(reportList);
		// ------------- Preparing databeancollection for chart ------------------
		logger.info(username + " <-- Preparing Charts --> ");
		// Iterator itr = reportList.iterator();
		Map<String, Integer> temp_chart = new HashMap<String, Integer>();
		for (DeliveryDTO chartDTO : reportList) {
			// System.out.println("Cost: " + chartDTO.getCost());
			String bsfmRule = chartDTO.getBsfmRule();
			int counter = 0;
			if (temp_chart.containsKey(bsfmRule)) {
				counter = temp_chart.get(bsfmRule);
			}
			temp_chart.put(bsfmRule, ++counter);
		}
		List<DeliveryDTO> chart_list = new ArrayList<DeliveryDTO>();
		if (!temp_chart.isEmpty()) {
			for (Map.Entry<String, Integer> entry : temp_chart.entrySet()) {
				DeliveryDTO chartDTO = new DeliveryDTO();
				chartDTO.setBsfmRule(entry.getKey());
				chartDTO.setRuleCount(entry.getValue());
				chart_list.add(chartDTO);
			}
		}
		JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(chart_list);
		parameters.put("piechartDataSource", piechartDataSource);
		logger.info(username + " <-- Finished Charts --> ");
		// -----------------------------------------------------------------------
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		if (reportList.size() > 20000) {
			logger.info(username + " <-- Creating Virtualizer --> ");
			JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(100,
					new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 1024, 512));
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		}
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(userOptional.get().getId());
		logger.info(username + " DisplayCost: " + webMasterEntry.isDisplayCost());
		parameters.put("displayCost", webMasterEntry.isDisplayCost());
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		logger.info(username + " <-- Filling Report Data --> ");
		print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		logger.info(username + " <-- Filling Completed --> ");
		return print;
	}

	// block report
	public List<DeliveryDTO> getReportList(CustomReportForm customReportForm, String username) throws Exception {
		logger.info(username + " Creating Report list");
		CustomReportDTO customReportDTO = new CustomReportDTO();
//		BeanUtils.copyProperties(customReportForm, customReportDTO);
		org.springframework.beans.BeanUtils.copyProperties(customReportForm, customReportDTO);
		String startDate = customReportDTO.getSday();
		String endDate = customReportDTO.getEday();
		String report_user = customReportDTO.getClientId();
		String destination = customReportDTO.getDestinationNumber();// "9926870493";
																	// //customReportDTO.getDestinationNumber();
		String senderId = customReportDTO.getSenderId();// "%"; //customReportDTO.getSenderId();
		String country = customReportDTO.getCountry();
		String operator = customReportDTO.getOperator();
		// String query = null;
		String block_query = null;
		List<String> report_user_list = null;
		List<DeliveryDTO> finallist = new ArrayList<DeliveryDTO>();
		if (report_user != null) {
			report_user_list = new ArrayList<String>();
			report_user_list.add(report_user);
		} else {
			String distinct_user_sql = "select distinct(username) from report_spam A where ";
			if (senderId != null && senderId.trim().length() > 0) {
				if (senderId.contains("%")) {
					distinct_user_sql += "A.sender like \"" + senderId + "\" and ";
				} else {
					distinct_user_sql += "A.sender =\"" + senderId + "\" and ";
				}
			}
			if (destination != null && destination.trim().length() > 0) {
				if (destination.contains("%")) {
					distinct_user_sql += "A.destination like '" + destination + "' and ";
				} else {
					distinct_user_sql += "A.destination ='" + destination + "' and ";
				}
			} else {
				if (country != null && country.length() > 0) {
					Predicate<Integer, NetworkEntry> p = null;
					String oprCountry = "";
					if (operator.equalsIgnoreCase("All")) {
						p = new PredicateBuilderImpl().getEntryObject().get("mcc").equal(country);
					} else {
						EntryObject e = new PredicateBuilderImpl().getEntryObject();
						p = e.get("mcc").equal(country).and(e.get("mnc").equal(operator));
					}
					// Coll<Integer, Network> networkmap = dbService.getNetworkRecord(country,
					// operator);
					for (int cc : GlobalVars.NetworkEntries.keySet(p)) {
						oprCountry += "'" + cc + "',";
					}
					if (oprCountry.length() > 0) {
						oprCountry = oprCountry.substring(0, oprCountry.length() - 1);
						distinct_user_sql += "A.oprCountry in (" + oprCountry + ") and ";
					}
				}
			}
			if (customReportForm.getBsfmRule() > 0) {
				distinct_user_sql += "A.profile_id = " + customReportForm.getBsfmRule() + " and ";
			}
			if (startDate.equalsIgnoreCase(endDate)) {
				String start_msg_id = startDate.substring(2);
				start_msg_id = start_msg_id.replaceAll("-", "");
				start_msg_id = start_msg_id.replaceAll(":", "");
				start_msg_id = start_msg_id.replaceAll(" ", "");
				distinct_user_sql += "A.msg_id like '" + start_msg_id + "%'";
			} else {
				String start_msg_id = startDate.substring(2);
				start_msg_id = start_msg_id.replaceAll("-", "");
				start_msg_id = start_msg_id.replaceAll(":", "");
				start_msg_id = start_msg_id.replaceAll(" ", "");
				start_msg_id += "0000000";
				String end_msg_id = endDate.substring(2);
				end_msg_id = end_msg_id.replaceAll("-", "");
				end_msg_id = end_msg_id.replaceAll(":", "");
				end_msg_id = end_msg_id.replaceAll(" ", "");
				end_msg_id += "0000000";
				distinct_user_sql += "A.msg_id between " + start_msg_id + " and " + end_msg_id + ";";
			}
			logger.info("Distinct User Sql: " + distinct_user_sql);
			report_user_list = distinctSpamUser(distinct_user_sql);
		}
		for (String spamUser : report_user_list) {
			try {
				block_query = "select A.msg_id,A.username,A.smsc,A.oprCountry,A.cost,A.time,A.sender,A.destination,A.remarks,B.profilename,B.reverse,C.country,C.operator,"
						+ "D.dcs,D.content,D.esm from report_spam A,bsfmaster B,network C,content_" + spamUser
						+ " D where ";
				if (senderId != null && senderId.trim().length() > 0) {
					if (senderId.contains("%")) {
						block_query += "A.sender like \"" + senderId + "\" and ";
					} else {
						block_query += "A.sender =\"" + senderId + "\" and ";
					}
				}
				if (destination != null && destination.trim().length() > 0) {
					if (destination.contains("%")) {
						block_query += "A.destination like '" + destination + "' and ";
					} else {
						block_query += "A.destination ='" + destination + "' and ";
					}
				} else {
					if (country != null && country.length() > 0) {
						Predicate<Integer, NetworkEntry> p = null;
						String oprCountry = "";
						if (operator.equalsIgnoreCase("All")) {
							p = new PredicateBuilderImpl().getEntryObject().get("mcc").equal(country);
						} else {
							EntryObject e = new PredicateBuilderImpl().getEntryObject();
							p = e.get("mcc").equal(country).and(e.get("mnc").equal(operator));
						}
						// Coll<Integer, Network> networkmap = dbService.getNetworkRecord(country,
						// operator);
						for (int cc : GlobalVars.NetworkEntries.keySet(p)) {
							oprCountry += "'" + cc + "',";
						}
						if (oprCountry.length() > 0) {
							oprCountry = oprCountry.substring(0, oprCountry.length() - 1);
							block_query += "A.oprCountry in (" + oprCountry + ") and ";
						}
					}
				}
				if (customReportForm.getBsfmRule() > 0) {
					block_query += "A.profile_id = " + customReportForm.getBsfmRule() + " and ";
				}
				if (startDate.equalsIgnoreCase(endDate)) {
					String start_msg_id = startDate.substring(2);
					start_msg_id = start_msg_id.replaceAll("-", "");
					start_msg_id = start_msg_id.replaceAll(":", "");
					start_msg_id = start_msg_id.replaceAll(" ", "");
					block_query += "A.msg_id like '" + start_msg_id + "%'";
				} else {
					String start_msg_id = startDate.substring(2);
					start_msg_id = start_msg_id.replaceAll("-", "");
					start_msg_id = start_msg_id.replaceAll(":", "");
					start_msg_id = start_msg_id.replaceAll(" ", "");
					start_msg_id += "0000000";
					String end_msg_id = endDate.substring(2);
					end_msg_id = end_msg_id.replaceAll("-", "");
					end_msg_id = end_msg_id.replaceAll(":", "");
					end_msg_id = end_msg_id.replaceAll(" ", "");
					end_msg_id += "0000000";
					block_query += "A.msg_id between " + start_msg_id + " and " + end_msg_id + " and ";
				}
				block_query += "A.msg_id=D.msg_id and A.profile_id=B.id and A.oprCountry=C.id order by A.msg_id,A.destination";
				logger.info("SQL: " + block_query);
				List<DeliveryDTO> list = blockedReport(block_query);
				finallist.addAll(list);
			} catch (Exception ex) {
				logger.error(spamUser, ex.fillInStackTrace());
			}
		}
		return finallist;
	}

	public List<DeliveryDTO> blockedReport(String sql) {
		List<DeliveryDTO> list = new ArrayList<DeliveryDTO>();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		Session openSession = null;
		Connection con = null;
		Map<String, Map<Integer, String>> content_map = new HashMap<String, Map<Integer, String>>();
		try {
			con = entityManager.unwrap(Connection.class);
			pStmt = con.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				String msg_id = rs.getString("A.msg_id");
				int esm = rs.getInt("D.esm");
				int dcs = rs.getInt("D.dcs");
				String content = rs.getString("D.content").trim();
				String destination = rs.getString("A.destination");
				String submit_time = rs.getString("time");
				String date = submit_time.substring(0, 10);
				String time = submit_time.substring(10, submit_time.length());
				String sender = rs.getString("A.sender");
				String report_user = rs.getString("A.username");
				String smsc = rs.getString("A.smsc");
				String remarks = rs.getString("A.remarks");
				String rulename = rs.getString("B.profilename");
				String country = rs.getString("C.country");
				String operator = rs.getString("C.operator");
				double cost = rs.getDouble("A.cost");
				String msg_type = "English";
				if (dcs == 8) {
					msg_type = "Unicode";
				}
				// logger.info(msg_id + " " + esm + " " + dcs + " " + destination + " " +
				// msg_type);
				if (esm == Data.SM_UDH_GSM || esm == 0x43) { // multipart
					String reference_number = "0";
					int part_number = 0;
					int total_parts = 0;
					try {
						int header_length = 0;
						if (dcs == 8) {
							header_length = Integer.parseInt(content.substring(0, 2));
						} else {
							header_length = Integer.parseInt(content.substring(0, 4));
						}
						if (dcs == 8) {
							if (header_length == 5) {
								reference_number = content.substring(6, 8);
								total_parts = Integer.parseInt(content.substring(8, 10));
								part_number = Integer.parseInt(content.substring(10, 12));
								content = content.substring(12, content.length());
							} else if (header_length == 6) {
								reference_number = content.substring(8, 10);
								total_parts = Integer.parseInt(content.substring(10, 12));
								part_number = Integer.parseInt(content.substring(12, 14));
								content = content.substring(14, content.length());
							}
						} else {
							if (header_length == 5) {
								reference_number = content.substring(12, 16);
								total_parts = Integer.parseInt(content.substring(16, 20));
								part_number = Integer.parseInt(content.substring(20, 24));
								content = content.substring(24, content.length());
							} else if (header_length == 6) {
								reference_number = content.substring(16, 20);
								total_parts = Integer.parseInt(content.substring(20, 24));
								part_number = Integer.parseInt(content.substring(24, 28));
								content = content.substring(28, content.length());
							}
						}
						Map<Integer, String> part_content = null;
						if (content_map.containsKey(destination + "#" + reference_number + "#" + dcs)) {
							part_content = content_map.get(destination + "#" + reference_number + "#" + dcs);
						} else {
							part_content = new TreeMap<Integer, String>();
						}
						part_content.put(part_number, msg_id + "#" + cost + "#" + content);
						if (part_content.size() == total_parts) { // all parts found
							DeliveryDTO reportDTO = new DeliveryDTO();
							reportDTO.setSender(sender);
							reportDTO.setDestination(destination);
							reportDTO.setDate(date);
							reportDTO.setTime(time);
							reportDTO.setMsgType(msg_type);
							reportDTO.setUsername(report_user);
							reportDTO.setCountry(country);
							reportDTO.setOperator(operator);
							reportDTO.setRoute(smsc);
							reportDTO.setBsfmRule(rulename);
							reportDTO.setRemarks(remarks);
							String combined_msg_id = "";
							String combined_content = "";
							double combined_cost = 0;
							int msgParts = 0;
							for (String message : part_content.values()) {
								String[] value = message.split("#");
								combined_msg_id += value[0] + " \n";
								combined_cost += Double.valueOf(value[1]);
								combined_content += hexCodePointsToCharMsg(value[2]);
								msgParts++;
							}
							reportDTO.setCost(combined_cost);
							reportDTO.setMsgParts(msgParts);
							reportDTO.setMsgid(combined_msg_id);
							reportDTO.setContent(combined_content);
							list.add(reportDTO);
						} else {
							content_map.put(destination + "#" + reference_number + "#" + dcs, part_content);
						}
					} catch (Exception une) {
						logger.error(msg_id + ": " + content, une.fillInStackTrace());
					}
				} else {
					DeliveryDTO reportDTO = new DeliveryDTO();
					reportDTO.setSender(sender);
					reportDTO.setDestination(destination);
					reportDTO.setDate(date);
					reportDTO.setTime(time);
					reportDTO.setMsgType(msg_type);
					reportDTO.setUsername(report_user);
					reportDTO.setMsgParts(1);
					reportDTO.setCountry(country);
					reportDTO.setOperator(operator);
					reportDTO.setRoute(smsc);
					reportDTO.setBsfmRule(rulename);
					reportDTO.setRemarks(remarks);
					String message = null;
					try {
						/*
						 * if (content.contains("0000")) { content = content.replaceAll("0000", "0040");
						 * }
						 */
						message = hexCodePointsToCharMsg(content);
					} catch (Exception ex) {
						message = "Conversion Failed";
					}
					reportDTO.setCost(cost);
					reportDTO.setContent(message);
					reportDTO.setMsgid(msg_id);
					list.add(reportDTO);
				}
			}
		} catch (Exception sqle) {
			if (sqle.getMessage().contains("doesn't exist")) {
			}
			throw new InternalServerException("contentWiseDlrReport()" + sqle.getMessage());
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
		return list;
	}

	public String hexCodePointsToCharMsg(String msg) {
		// logger.info("got request ");
		// this mthd made decreasing codes, only.
		//// This mthd will take msg who contain hex values of unicode, then it will
		// convert this msg to Unicode from hex.
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		if (msg.substring(0, 2).compareTo("00") == 0) // if true means first byte is null, then null is required in
														// first byte, after header.
		{
			reqNULL = true;
		}
		charsByt = new BigInteger(msg, 16).toByteArray(); // this won't give null value in first byte if occured, so i
															// have to append it .
		if (charsByt[0] == '\0') // cut this null.
		{
			var = new byte[charsByt.length - 1];
			for (int q = 1; q < charsByt.length; q++) {
				var[q - 1] = charsByt[q];
			}
			charsByt = var;
		}
		if (reqNULL) {
			var = new byte[charsByt.length + 1];
			x = 0;
			var[0] = '\0';
			reqNULL = false;
		} else {
			var = new byte[charsByt.length];
			x = -1;
		}
		for (int l = 0; l < charsByt.length; l++) {
			var[++x] = charsByt[l];
		}
		try {
			msg = new String(var, "UTF-16"); // charsTA msg Setted.
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msg;
	}

	public List<String> distinctSpamUser(String sql) {
		List<String> list = new ArrayList<>();
		try (Connection con = jdbcTemplate.getDataSource().getConnection();
				PreparedStatement pStmt = con.prepareStatement(sql);
				ResultSet rs = pStmt.executeQuery()) {

			while (rs.next()) {
				list.add(rs.getString("username"));
			}
		} catch (SQLException sqle) {
			// Handle or log the exception
			throw new InternalServerException("distinctSpamUser()" + sqle.getMessage());
		}

		return list;
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortMapByDscValue(Map<K, V> map, int limit) {
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();
		st.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(limit)
				.forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
		return result;
	}

	private List sortListByMessageId(List list) {
		// logger.info(userSessionObject.getSystemId() + " sortListBySender ");
		Comparator<DeliveryDTO> comparator = Comparator.comparing(DeliveryDTO::getMsgid);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	public Workbook getWorkBook(List reportList, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isEmpty()) {
			throw new NotFoundException("username not found username{}" + username);
		}
		logger.info(username + " <-- Creating WorkBook --> ");
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		int records_per_sheet = 400000;
		int sheet_number = 0;
		Sheet sheet = null;
		Row row = null;
		XSSFFont headerFont = (XSSFFont) workbook.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setColor(new XSSFColor(Color.WHITE));
		XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setFillForegroundColor(new XSSFColor(Color.GRAY));
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBottomBorderColor(new XSSFColor(Color.WHITE));
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setTopBorderColor(new XSSFColor(Color.WHITE));
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setLeftBorderColor(new XSSFColor(Color.WHITE));
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setRightBorderColor(new XSSFColor(Color.WHITE));
		XSSFFont rowFont = (XSSFFont) workbook.createFont();
		rowFont.setFontName("Arial");
		rowFont.setFontHeightInPoints((short) 9);
		rowFont.setColor(new XSSFColor(Color.BLACK));
		XSSFCellStyle rowStyle = (XSSFCellStyle) workbook.createCellStyle();
		rowStyle.setFont(rowFont);
		rowStyle.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
		rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		rowStyle.setAlignment(HorizontalAlignment.LEFT);
		rowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBottomBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setTopBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderLeft(BorderStyle.THIN);
		rowStyle.setBorderLeft(BorderStyle.THIN);
		rowStyle.setLeftBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderRight(BorderStyle.THIN);
		rowStyle.setBorderRight(BorderStyle.THIN);
		rowStyle.setRightBorderColor(new XSSFColor(Color.WHITE));
		String[] headers = { "Username", "MessageId", "SubmitOn", "Destination", "SenderId", "Status", "Content",
				"Cost" };
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(userOptional.get().getId());
		if (!webMasterEntry.isDisplayCost()) {
			headers = (String[]) ArrayUtils.remove(headers, 7);
		}
		while (!reportList.isEmpty()) {
			int row_number = 0;
			sheet = workbook.createSheet("Sheet(" + sheet_number + ")");
			sheet.setDefaultColumnWidth(14);
			logger.info(username + " Creating Sheet: " + sheet_number);
			while (!reportList.isEmpty()) {
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
					DeliveryDTO reportDTO = (DeliveryDTO) reportList.remove(0);
					Cell cell = row.createCell(0);
					cell.setCellValue(reportDTO.getUsername());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(1);
					cell.setCellValue(reportDTO.getMsgid());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(2);
					cell.setCellValue(reportDTO.getDate() + " " + reportDTO.getTime());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(3);
					cell.setCellValue(reportDTO.getDestination());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(4);
					cell.setCellValue(reportDTO.getSender());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(5);
					cell.setCellValue(reportDTO.getStatus());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(6);
					cell.setCellValue(reportDTO.getContent());
					cell.setCellStyle(rowStyle);
					if (webMasterEntry.isDisplayCost()) {
						cell = row.createCell(7);
						cell.setCellValue(reportDTO.getCost());
						cell.setCellStyle(rowStyle);
					}
				}
				if (++row_number > records_per_sheet) {
					logger.info(username + " Sheet Created: " + sheet_number);
					break;
				}
			}
			sheet_number++;
		}
		logger.info(username + " <--- Workbook Created ----> ");
		return workbook;
	}

}
