package com.hti.smpp.common.database;

import java.awt.Color;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import javax.sql.DataSource;

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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.hazelcast.internal.util.collection.ArrayUtils;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.messages.dto.BulkMapEntry;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.BalanceReportRequest;
import com.hti.smpp.common.request.CampaignReportRequest;
import com.hti.smpp.common.request.ContentReportRequest;
import com.hti.smpp.common.request.CustomReportDTO;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.CustomizedReportRequest;
import com.hti.smpp.common.request.DlrSummaryReport;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.BulkDAService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.service.impl.UserDAServiceImpl;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.BalanceEntryRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Converter;
import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.dto.SevenBitChar;
import com.logica.smpp.Data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
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
	private UserDAService userService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private BalanceEntryRepository balanceEntryRepository;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public List<DeliveryDTO> getReportList(ContentReportRequest customReportForm, String username, String lang) {
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
		List<DeliveryDTO> list = entityManager.createNativeQuery(sql, BulkEntry.class).getResultList();
		return list;
	}

//////////////////////////////
	public Map<String, List<DeliveryDTO>> getBalanceReportList(BalanceReportRequest customReportForm, String username) {
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
		String startDate = customReportForm.getStartDate();
		String endDate = customReportForm.getEndDate();
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
//					query ="SELECT COUNT(msg_id) AS count, submitted_time AS time, oprcountry, SUM(cost) AS cost_sum FROM mis_testUser1 WHERE msg_id NOT IN (SELECT msg_id FROM smsc_in) AND msg_id NOT IN (SELECT msg_id FROM unprocessed) AND msg_id BETWEEN 2001060000000000000 AND 2402080000000000000 GROUP BY time, oprcountry;\r\n"
//							+ report_user;
					query = "select count(msg_id) as count,TIME(submitted_time) AS time,DATE(submitted_time) AS date,oprcountry,SUM(cost) as cost_sum from mis_"
							+ report_user;

					query += " where msg_id not in(select msg_id from smsc_in) and msg_id not in(select msg_id from unprocessed) and ";
				}
				if (to_gmt != null) {
					SimpleDateFormat client_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					client_formatter.setTimeZone(TimeZone.getTimeZone(webMasterEntry.getGmt()));
					SimpleDateFormat local_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						System.out.println("praper");
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

						// query += "time between CONVERT_TZ('" + startDate + "','" + to_gmt + "','" +
						// from_gmt + "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" +
						// from_gmt + "')";
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
					System.out.println("343");
				} else {
					query += " group by time,date,oprcountry";
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

			///////////////////
//			int limit = pageable.getPageSize();
//			int offset = pageable.getPageNumber() * pageable.getPageSize();
//			unproc_query += "LIMIT " + limit + " OFFSET " + offset;
			logger.info("SQL: " + unproc_query);
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
			System.out.println("this is final query " + query);
			list = jdbcTemplate.query(query, (rs, rowNum) -> {
				DeliveryDTO entry = new DeliveryDTO();
				entry.setUsername(reportUser);
				entry.setSubmitted(rs.getInt("count"));
				entry.setCost(rs.getDouble("cost_sum"));
				entry.setTime(rs.getString("time"));
				entry.setDate(rs.getString("date"));

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
		try (Connection con = getConnection();
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
			logger.error("", sqle);
		}
		// No need for a finally block to close resources when using try-with-resources
		return map;
	}

	public Map<String, List<DeliveryDTO>> getConsumptionReport(String query) {
		Map<String, List<DeliveryDTO>> map = new HashMap<>();
		try (Connection con = getConnection();
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

	public Map<String, List<DeliveryDTO>> getReportListFile(String username, BalanceReportRequest customReportForm) {
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
		String startDate = customReportForm.getStartDate();
		String endDate = customReportForm.getEndDate();
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
					BalanceEntry balanceEntry = balanceEntryRepository.findByUserId(userEntry.getId()).get();
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
					BalanceEntry balanceEntry = balanceEntryRepository.findByUserId(userEntry.getId()).get();
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
		comparator = Comparator.comparing(DeliveryDTO::getDate).thenComparing(DeliveryDTO::getCampaign);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
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

	private <K, V extends Comparable<? super V>> Map<K, V> sortMapByDscValue(Map<K, V> map, int limit) {
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();
		st.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(limit)
				.forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
		return result;
	}

	public List sortListByMessageId(List list) {
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

	public JasperPrint getCampaignReportList(CampaignReportRequest customReportForm, String username, boolean paging,
			String lang) throws JRException {
		locale = Customlocale.getLocaleByLanguage(lang);
		if (customReportForm.getClientId() == null) {
			return null;
		}
		Optional<UserEntry> usersOptional = userRepository.findBySystemId(username);
		if (!usersOptional.isPresent()) {
			throw new NotFoundException("user not fout with system id" + username);
		}
		UserEntry userEntry2 = usersOptional.get();

		List<DeliveryDTO> final_list = new ArrayList<DeliveryDTO>();
		List<DeliveryDTO> chart_list = new ArrayList<DeliveryDTO>();
		int final_pending = 0, final_deliv = 0, final_undeliv = 0, final_expired = 0, final_others = 0;
		String[] start_date = customReportForm.getStartDate().split("-");
		String[] end_date = customReportForm.getEndDate().split("-");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DATE, Integer.parseInt(end_date[2]));
		calendar.set(Calendar.MONTH, (Integer.parseInt(end_date[1])) - 1);
		calendar.set(Calendar.YEAR, Integer.parseInt(end_date[0]));
		calendar.add(Calendar.DATE, 1);
		String end_date_str = new SimpleDateFormat("yyMMdd").format(calendar.getTime()) + "0000000000000";
		String start_date_str = (start_date[0].substring(2)) + "" + start_date[1] + "" + start_date[2]
				+ "0000000000000";

		List<String> users = null;
		if (customReportForm.getClientId().equalsIgnoreCase("All")) {
			String role = userEntry2.getRole();
			if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
				users = new ArrayList<String>(userService.listUsers().values());
			} else if (role.equalsIgnoreCase("admin")) {
				users = new ArrayList<String>(userService.listUsersUnderMaster(userEntry2.getSystemId()).values());
				users.add(userEntry2.getSystemId());
				Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
						.get("secondaryMaster").equal(userEntry2.getSystemId());
				for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
					users.add(userEntry.getSystemId());
				}
			} else if (role.equalsIgnoreCase("seller")) {
				users = new ArrayList<String>(userService.listUsersUnderSeller(userEntry2.getId()).values());
			} else if (role.equalsIgnoreCase("manager")) {
				// SalesDAService salesService = new SalesDAServiceImpl();
				users = new ArrayList<String>(listUsernamesUnderManager(userEntry2.getSystemId()).values());
			}
			logger.info(userEntry2 + " Under Users: " + users.size());
		} else {
			users = new ArrayList<String>();
			users.add(customReportForm.getClientId());
		}
		logger.info(userEntry2.getSystemId() + " <-- preparing Report Data --> ");
		List<BulkMapEntry> list = list(users.toArray(new String[users.size()]), Long.parseLong(start_date_str),
				Long.parseLong(end_date_str));
		System.out.println("this is bulk map entry " + list);
		if (customReportForm.getGroupBy().equalsIgnoreCase("name")) {
			Map<String, Map<String, List<String>>> filtered_map = new HashMap<String, Map<String, List<String>>>();
			for (BulkMapEntry entry : list) {
				Map<String, List<String>> campaign_mapping = null;
				if (filtered_map.containsKey(entry.getSystemId())) {
					campaign_mapping = filtered_map.get(entry.getSystemId());
				} else {
					campaign_mapping = new HashMap<String, List<String>>();
				}
				List<String> msg_id_list = null;
				if (campaign_mapping.containsKey(entry.getName())) {
					msg_id_list = campaign_mapping.get(entry.getName());
				} else {
					msg_id_list = new ArrayList<String>();
				}
				msg_id_list.add(String.valueOf(entry.getMsgid()));
				campaign_mapping.put(entry.getName(), msg_id_list);
				filtered_map.put(entry.getSystemId(), campaign_mapping);
			}
			DeliveryDTO report = null;
			for (Map.Entry<String, Map<String, List<String>>> entry : filtered_map.entrySet()) {
				for (Map.Entry<String, List<String>> campaign_entry : entry.getValue().entrySet()) {
					System.out.println("Checking Report For " + entry.getKey() + " Campaign: " + campaign_entry.getKey()
							+ " Listed: " + campaign_entry.getValue().size());
					Map<String, Map<String, Map<String, Integer>>> report_map = getCampaignReport(entry.getKey(),
							campaign_entry.getValue());
					for (Map.Entry<String, Map<String, Map<String, Integer>>> time_entry : report_map.entrySet()) {
						for (Map.Entry<String, Map<String, Integer>> source_entry : time_entry.getValue().entrySet()) {
							report = new DeliveryDTO();
							report.setUsername(entry.getKey());
							report.setDate(time_entry.getKey());
							report.setCampaign(campaign_entry.getKey());
							report.setSender(source_entry.getKey());
							int total_submitted = 0;
							int others = 0;
							for (Map.Entry<String, Integer> status_entry : source_entry.getValue().entrySet()) {
								if (status_entry.getKey() != null && status_entry.getKey().length() > 0) {
									if (status_entry.getKey().toLowerCase().startsWith("del")) {
										report.setDelivered(status_entry.getValue());
										final_deliv += status_entry.getValue();
									} else if (status_entry.getKey().toLowerCase().startsWith("und")) {
										report.setUndelivered(status_entry.getValue());
										final_undeliv += status_entry.getValue();
									} else if (status_entry.getKey().toLowerCase().startsWith("exp")) {
										report.setExpired(status_entry.getValue());
										final_expired += status_entry.getValue();
									} else if (status_entry.getKey().toLowerCase().startsWith("ates")) {
										report.setPending(status_entry.getValue());
										final_pending += status_entry.getValue();
									} else {
										others += status_entry.getValue();
									}
								} else {
									others += status_entry.getValue();
								}
								total_submitted += status_entry.getValue();
							}
							report.setSubmitted(total_submitted);
							report.setOthers(others);
							final_others += others;
							final_list.add(report);
						}
					}
				}
			}
		} else {
			Map<String, String> campaign_map = new HashMap<String, String>();
			for (BulkMapEntry entry : list) {
				campaign_map.put(String.valueOf(entry.getMsgid()), entry.getName());
			}
			if (!users.isEmpty()) {
				while (!users.isEmpty()) {
					String report_user = (String) users.remove(0);
					try {
						logger.info(userEntry2.getSystemId() + " Checking Report For " + report_user);
						String sql = "select msg_id,DATE(submitted_time) as date,source_no,status from mis_"
								+ report_user + " where msg_id between " + start_date_str + " and " + end_date_str;
						System.out.println(sql);
						List<DeliveryDTO> part_list = getCampaignReport(sql);
						System.out.println(part_list);
						logger.info(report_user + " Start Processing Entries: " + part_list.size());
						Map<String, Map<String, Map<String, DeliveryDTO>>> date_wise_map = new HashMap<String, Map<String, Map<String, DeliveryDTO>>>();
						// int total_submitted = 0;
						for (DeliveryDTO dlrEntry : part_list) {
							System.out.println("1703");
							if (campaign_map.containsKey(dlrEntry.getMsgid())) {
								System.out.println("1705");
								dlrEntry.setCampaign(campaign_map.get(dlrEntry.getMsgid()));
							} else {
								System.out.println("1708");
								dlrEntry.setCampaign("-");
							}
							Map<String, Map<String, DeliveryDTO>> campaign_wise_map = null;
							if (date_wise_map.containsKey(dlrEntry.getDate())) {
								System.out.println("1713");
								campaign_wise_map = date_wise_map.get(dlrEntry.getDate());
							} else {
								System.out.println("1716");
								campaign_wise_map = new HashMap<String, Map<String, DeliveryDTO>>();
							}
							Map<String, DeliveryDTO> source_wise_map = null;
							if (campaign_wise_map.containsKey(dlrEntry.getCampaign())) {
								System.out.println("1721");
								source_wise_map = campaign_wise_map.get(dlrEntry.getCampaign());
							} else {
								System.out.println("1724");
								source_wise_map = new HashMap<String, DeliveryDTO>();
							}
							DeliveryDTO dlrDTO = null;
							if (source_wise_map.containsKey(dlrEntry.getSender())) {
								System.out.println("1729");
								dlrDTO = source_wise_map.get(dlrEntry.getSender());
							} else {
								System.out.println("1732");
								dlrDTO = new DeliveryDTO();
								dlrDTO.setCampaign(dlrEntry.getCampaign());
								dlrDTO.setSender(dlrEntry.getSender());
								dlrDTO.setDate(dlrEntry.getDate());
								dlrDTO.setUsername(report_user);
							}
							if (dlrEntry.getStatus() != null) {
								System.out.println("1740");
								if (dlrEntry.getStatus().toLowerCase().startsWith("deliv")) {
									System.out.println("1742");
									dlrDTO.setDelivered(dlrDTO.getDelivered() + 1);
									final_deliv++;
								} else if (dlrEntry.getStatus().toLowerCase().startsWith("undel")) {
									System.out.println("1746");
									dlrDTO.setUndelivered(dlrDTO.getUndelivered() + 1);
									final_undeliv++;
								} else if (dlrEntry.getStatus().toLowerCase().startsWith("expir")) {
									System.out.println("1750");
									dlrDTO.setExpired(dlrDTO.getExpired() + 1);
									final_expired++;
								} else if (dlrEntry.getStatus().toLowerCase().startsWith("ates")) {
									System.out.println("1754");
									dlrDTO.setPending(dlrDTO.getPending() + 1);
									final_pending++;
								} else {
									System.out.println("1758");
									dlrDTO.setOthers(dlrDTO.getOthers() + 1);
									final_others++;
								}
							} else {
								System.out.println("1763");
								dlrDTO.setOthers(dlrDTO.getOthers() + 1);
								final_others++;
							}
							System.out.println("1767");
							dlrDTO.setSubmitted(dlrDTO.getSubmitted() + 1);
							// total_submitted++;
							source_wise_map.put(dlrDTO.getSender(), dlrDTO);
							campaign_wise_map.put(dlrDTO.getCampaign(), source_wise_map);
							date_wise_map.put(dlrDTO.getDate(), campaign_wise_map);
							System.out.println("this is data wise map" + date_wise_map);
						}
						logger.info(report_user + " <- End Processing Entries -> ");
						if (!date_wise_map.isEmpty()) {
							for (Map.Entry<String, Map<String, Map<String, DeliveryDTO>>> date_wise_entry : date_wise_map
									.entrySet()) {
								for (Map.Entry<String, Map<String, DeliveryDTO>> campaign_wise_entry : date_wise_entry
										.getValue().entrySet()) {
									for (Map.Entry<String, DeliveryDTO> source_wise_entry : campaign_wise_entry
											.getValue().entrySet()) {
										System.out.println("this is source wise entry " + source_wise_entry.getValue());
										final_list.add(source_wise_entry.getValue());
									}
								}
							}
						}
					} catch (Exception ex) {
						logger.error(report_user + ":" + ex);
					}
				}
			}
		}
		chart_list.add(new DeliveryDTO("DELIVRD", final_deliv));
		chart_list.add(new DeliveryDTO("UNDELIV", final_undeliv));
		chart_list.add(new DeliveryDTO("EXPIRED", final_expired));
		chart_list.add(new DeliveryDTO("PENDING", final_pending));
		chart_list.add(new DeliveryDTO("OTHERS", final_others));
		System.out.println("this is chart" + chart_list);
		System.out.println("this is final list" + final_list);
		JasperPrint print = null;
		if (!final_list.isEmpty()) {
			logger.info(userEntry2.getSystemId() + " Prepared List: " + final_list.size());
			final_list = sortList(final_list);
			JasperDesign design = JRXmlLoader.load(template_file);
			// List<DeliveryDTO> chart_list = new ArrayList<DeliveryDTO>();
			JasperReport jasperreport = JasperCompileManager.compileReport(design);
			JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(chart_list);
			Map parameters = new HashMap();
			parameters.put("piechartDataSource", piechartDataSource);
			JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(final_list);
			if (final_list.size() > 20000) {
				logger.info(userEntry2.getSystemId() + " <-- Creating Virtualizer --> ");
				JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(1000,
						new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 2048, 1024));
				parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			}
			parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
			ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
			parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
			logger.info(userEntry2.getSystemId() + " <-- Filling Report Data --> ");
			print = JasperFillManager.fillReport(jasperreport, parameters, beanColDataSource);
			logger.info(userEntry2.getSystemId() + " <-- Filling Finished --> ");
		} else {
			logger.info(userEntry2.getSystemId() + " <-- No Report Data Found --> ");
		}
		return print;
	}

	public List<DeliveryDTO> getCampaignReport(String sql) throws SQLException {
		List<DeliveryDTO> list = new ArrayList<DeliveryDTO>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			// pStmt.setString(1, String.join(",", msg_id_list));
			rs = pStmt.executeQuery();
			DeliveryDTO deliver = null;
			while (rs.next()) {
				deliver = new DeliveryDTO();
				deliver.setMsgid(rs.getString("msg_id"));
				deliver.setSender(rs.getString("source_no"));
				deliver.setDate(rs.getString("date"));
				deliver.setStatus(rs.getString("status"));
				list.add(deliver);
			}
		} catch (SQLException sqle) {
			throw new SQLException(sqle);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
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

	public Map<String, Map<String, Map<String, Integer>>> getCampaignReport(String username, List<String> msg_id_list) {
		Map<String, Map<String, Map<String, Integer>>> report_list = new HashMap<>();

		String jpql = "SELECT COUNT(b.msgId) AS count, DATE(b.submittedTime) AS time, b.sourceNo, b.status "
				+ "FROM BulkMapEntry b " + "WHERE b.msgId IN :msgIdList AND b.username = :username "
				+ "GROUP BY time, b.sourceNo, b.status";

		try {
			Query query = entityManager.createQuery(jpql);
			query.setParameter("msgIdList", msg_id_list);
			query.setParameter("username", username);

			List<Object[]> resultList = query.getResultList();

			for (Object[] result : resultList) {
				String time = (String) result[1];
				String sourceNo = (String) result[2];
				String status = (String) result[3];
				Integer count = ((Number) result[0]).intValue();

				report_list.computeIfAbsent(time, k -> new HashMap<>()).computeIfAbsent(sourceNo, k -> new HashMap<>())
						.merge(status == null ? "" : status, count, Integer::sum);
			}

			logger.info("Campaign[" + username + "] Report: " + report_list.size());
		} catch (Exception e) {
			logger.error("Error fetching campaign report for user " + username, e);
		}

		return report_list;
	}

	public List<BulkMapEntry> list(String[] systemId, long from, long to) {
		logger.info("Checking for systemId: " + systemId + " From: " + from + " To: " + to);

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<BulkMapEntry> criteriaQuery = criteriaBuilder.createQuery(BulkMapEntry.class);
		Root<BulkMapEntry> root = criteriaQuery.from(BulkMapEntry.class);

		jakarta.persistence.criteria.Predicate predicate = criteriaBuilder.conjunction(); // Initialize with conjunction
																							// (AND)

		if (systemId != null && systemId.length > 0) {
			predicate = criteriaBuilder.and(predicate, root.get("systemId").in((Object[]) systemId));
		}

		if (from > 0 && to > 0) {
			predicate = criteriaBuilder.and(predicate, criteriaBuilder.between(root.get("msgid"), from, to));
		}

		criteriaQuery.select(root).where(predicate);

		List<BulkMapEntry> list = entityManager.createQuery(criteriaQuery).getResultList();

		logger.info("Campaign entries: " + list.size());
		return list;
	}

	public Workbook getContentWorkBook(List<DeliveryDTO> reportList, String username) {
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
		// rowStyle.setBorderBottom((short) 1);
		rowStyle.setBottomBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderTop(BorderStyle.THIN);
		// rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setTopBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderLeft(BorderStyle.THIN);
		// rowStyle.setBorderLeft((short) 1);
		rowStyle.setLeftBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderRight(BorderStyle.THIN);
		// rowStyle.setBorderRight((short) 1);
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

	public List<DeliveryDTO> getCustomizedReportList(CustomizedReportRequest customReportForm, String username)
			throws Exception {
		String target = IConstants.FAILURE_KEY;
		String groupby = "country";
		String reportUser = null;
		boolean isContent;
		boolean isSummary;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
		if (webMasterEntry == null) {
			throw new NotFoundException("web MasterEntry  not fount username {}" + username);
		}

		logger.info(user.getSystemId() + " Creating Report list");
		List list = null;
		List final_list = new ArrayList();
		CustomReportDTO customReportDTO = new CustomReportDTO();
		groupby = customReportForm.getGroupBy();
		BeanUtils.copyProperties(customReportForm, customReportDTO);
		System.out.println(customReportDTO.getCampaign());
		String startDate = customReportForm.getStartDate();
		String endDate = customReportForm.getEndDate();
		BulkDAService bulkService = null;
		// IDatabaseService dbService = HtiSmsDB.getInstance();
		reportUser = customReportDTO.getClientId();
		String campaign = customReportForm.getCampaign();
		String messageId = customReportDTO.getMessageId(); // null; //customReportDTO.getMessageId();
		// String messageStatus = customReportDTO.getMessageStatus();// "PENDING";
		// //customReportDTO.getMessageStatus();
		String destination = customReportDTO.getDestinationNumber();// "9926870493";
																	// //customReportDTO.getDestinationNumber();
		String senderId = customReportDTO.getSenderId();// "%"; //customReportDTO.getSenderId();
		String status = customReportDTO.getStatus();
		String country = customReportDTO.getCountry();
		String operator = customReportDTO.getOperator();
		String criteria_type = customReportForm.getCheck_A();
		String query = null;
		// logger.info(userSessionObject.getSystemId() + " Criteria Type --> " +
		// criteria_type);
		if (customReportForm.getCheck_F() != null) {
			isContent = true;
		} else {
			isContent = false;
		}
		to_gmt = null;
		from_gmt = null;
		logger.info(user.getSystemId() + ": " + webMasterEntry.getGmt() + " Default: " + IConstants.DEFAULT_GMT);
		if (!webMasterEntry.getGmt().equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = webMasterEntry.getGmt().replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
			logger.info(user.getSystemId() + " " + customReportDTO + ",isContent=" + isContent + ",GroupBy=" + groupby
					+ ",Status=" + status);
			if (criteria_type.equalsIgnoreCase("messageid")) {
				logger.info(user.getSystemId() + " Report Based On MessageId: " + messageId);
				if (messageId != null && messageId.trim().length() > 0) {
				}
				System.out.println("run 2618");
				isSummary = false;
				String userQuery = "select username from mis_table where msg_id ='" + messageId + "'";
				List userSet = getDistinctMisUser(userQuery);
				System.out.println("run 2622" + userSet);
				if (!userSet.isEmpty()) {
					reportUser = (String) userSet.remove(0);
					if (to_gmt != null) {
						query = "select CONVERT_TZ(submitted_time,'" + from_gmt + "','" + to_gmt
								+ "') as submitted_time,";
					} else {
						query = "select submitted_time,";
					}
					query += "msg_id,oprCountry,source_no,dest_no,cost,status,deliver_time,err_code,Route_to_SMSC from mis_"
							+ reportUser + " where msg_id ='" + messageId + "'";
					logger.info(user.getSystemId() + " ReportSQL:" + query);
					if (isContent) {
						System.out.println("run 2633");
						Map map = getDlrReport(reportUser, query, webMasterEntry.isHideNum());
						if (!map.isEmpty()) {
							try {
								list = getMessageContent(map, reportUser);
							} catch (Exception e) {
								list = new ArrayList(map.values());
							}
						}
					} else {
						System.out.println("run 2644");
						list = (List) getCustomizedReport(reportUser, query, webMasterEntry.isHideNum());
					}
				}

				final_list.addAll(list);
				logger.info(user.getSystemId() + " End Based On MessageId. Final Report Size: " + final_list.size());
			} else {
				logger.info(user.getSystemId() + " Invalid MessageId");
			}
		} else if (criteria_type.equalsIgnoreCase("campaign")) {
			logger.info(user.getSystemId() + " Report Based On Campaign: " + campaign);
			if (campaign != null && campaign.length() > 1) {
				List<BulkMapEntry> bulk_list = null;
				if (campaign.equalsIgnoreCase("all")) {
					if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")) {
						bulk_list = bulkService.list(null, null);
					} else {
						String[] user_array;
						if (user.getRole().equalsIgnoreCase("admin") || user.getRole().equalsIgnoreCase("seller")
								|| user.getRole().equalsIgnoreCase("manager")) {
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
							user_array = users.toArray(new String[users.size()]);
						} else { // user
							user_array = new String[] { user.getSystemId() };
						}
						bulk_list = bulkService.list(null, user_array);
					}
				} else {
					bulk_list = bulkService.list(campaign, null);
				}
				Map<String, List<String>> user_bulk_mapping = new HashMap<String, List<String>>();
				List<String> msg_id_list = null;
				for (BulkMapEntry mapEntry : bulk_list) {
					if (user_bulk_mapping.containsKey(mapEntry.getSystemId())) {
						msg_id_list = user_bulk_mapping.get(mapEntry.getSystemId());
					} else {
						msg_id_list = new ArrayList<String>();
					}
					msg_id_list.add(String.valueOf(mapEntry.getMsgid()));
					user_bulk_mapping.put(mapEntry.getSystemId(), msg_id_list);
				}
				if (user_bulk_mapping.isEmpty()) {
					logger.error(user.getSystemId() + " No Records Found For campaign: " + campaign);
				} else {
					if (user_bulk_mapping.size() > 1) {
						logger.debug("Multiple users Has Same Campaign name.");
						if (user.getRole().equalsIgnoreCase("superadmin")
								|| user.getRole().equalsIgnoreCase("system")) {
							// superadmin or system user can check for any user's campaign
						} else {
							if (user.getRole().equalsIgnoreCase("admin") || user.getRole().equalsIgnoreCase("seller")
									|| user.getRole().equalsIgnoreCase("manager")) {
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
									users = new ArrayList<String>(
											userService.listUsersUnderSeller(user.getId()).values());
								} else if (user.getRole().equalsIgnoreCase("manager")) {
									// SalesDAService salesService = new SalesDAServiceImpl();
									users = new ArrayList<String>(
											listUsernamesUnderManager(user.getSystemId()).values());
								}
								if (users != null && !users.isEmpty()) {
									Iterator<String> itr = user_bulk_mapping.keySet().iterator();
									while (itr.hasNext()) {
										String systemId = itr.next();
										if (!users.contains(systemId)) {
											logger.info(systemId + " is not under " + user.getSystemId());
											itr.remove();
										}
									}
								}
							} else {
								Iterator<String> itr = user_bulk_mapping.keySet().iterator();
								while (itr.hasNext()) {
									String systemId = itr.next();
									if (!user.getSystemId().equalsIgnoreCase(systemId)) {
										logger.info(systemId + " is not account of " + user.getSystemId());
										itr.remove();
									}
								}
							}
						}
					}
					// -------------- report fetching ---------------------------------------

					isSummary = false;
					Set<String> unprocessed_set = new java.util.HashSet<String>();
					for (Map.Entry<String, List<String>> entry : user_bulk_mapping.entrySet()) {
						unprocessed_set.addAll(entry.getValue());
						reportUser = entry.getKey();
						if (to_gmt != null) {
							query = "select CONVERT_TZ(submitted_time,'" + from_gmt + "','" + to_gmt
									+ "') as submitted_time,";
						} else {
							query = "select submitted_time,";
						}
						query += "msg_id,oprCountry,source_no,dest_no,cost,status,deliver_time,err_code from mis_"
								+ reportUser + " where msg_id in(" + String.join(",", entry.getValue()) + ")";
						logger.info(user.getSystemId() + " ReportSQL:" + query);
						if (isContent) {
							Map map = getDlrReport(reportUser, query, webMasterEntry.isHideNum());
							if (!map.isEmpty()) {
								try {
									list = getMessageContent(map, reportUser);
								} catch (Exception e) {
									list = new ArrayList(map.values());
								}
							}
						} else {
							list = getCustomizedReport(reportUser, query, webMasterEntry.isHideNum());
						}
						final_list.addAll(list);
					}
					String cross_unprocessed_query = "";
					if (to_gmt != null) {
						cross_unprocessed_query = "select CONVERT_TZ(time,'" + from_gmt + "','" + to_gmt
								+ "') as time,";
					} else {
						cross_unprocessed_query = "select time,";
					}
					cross_unprocessed_query += "msg_id,username,oprCountry,source_no,destination_no,cost,s_flag";
					if (isContent) {
						cross_unprocessed_query += ",content,dcs";
					}
					cross_unprocessed_query += " from table_name where msg_id in(" + String.join(",", unprocessed_set)
							+ ")";
					List unproc_list = getUnprocessedReport(
							cross_unprocessed_query.replaceAll("table_name", "unprocessed"), webMasterEntry.isHideNum(),
							isContent);
					if (unproc_list != null && !unproc_list.isEmpty()) {
						final_list.addAll(unproc_list);
					}
					unproc_list = getUnprocessedReport(cross_unprocessed_query.replaceAll("table_name", "smsc_in"),
							webMasterEntry.isHideNum(), isContent);
					if (unproc_list != null && !unproc_list.isEmpty()) {
						final_list.addAll(unproc_list);
					}
					logger.info(user.getSystemId() + " End Based On Campaign. Final Report Size: " + final_list.size());
					// ------------- end report fetching ------------------------------------
				}
				logger.info(user.getSystemId() + " End Report Based On Campaign: " + campaign);
			} else {
				logger.info(user.getSystemId() + " Invalid Campaign: " + campaign);
				return null;
			}
		} else {
			logger.info(user.getSystemId() + " Report Based On Criteria");
			List<String> users = null;
			if (customReportDTO.getClientId().equalsIgnoreCase("All")) {
				String role = user.getRole();
				if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
					users = new ArrayList<String>(userService.listUsers().values());
				} else if (role.equalsIgnoreCase("admin")) {
					users = new ArrayList<String>(userService.listUsersUnderMaster(user.getSystemId()).values());
					users.add(user.getSystemId());
					Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
							.get("secondaryMaster").equal(user.getSystemId());
					for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
						UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
						users.add(userEntry.getSystemId());
					}
				} else if (role.equalsIgnoreCase("seller")) {
					users = new ArrayList<String>(userService.listUsersUnderSeller(user.getId()).values());
				} else if (role.equalsIgnoreCase("manager")) {
					// SalesDAService salesService = new SalesDAServiceImpl();
					users = new ArrayList<String>(listUsernamesUnderManager(user.getSystemId()).values());
				}
				logger.info(user.getSystemId() + ": Under Users: " + users.size());
			} else {
				users = new ArrayList<String>();
				users.add(customReportDTO.getClientId());
			}
			if (customReportDTO.getReportType().equalsIgnoreCase("Summary")) {
				isSummary = true;
			} else {
				isSummary = false;
			}
			logger.info(user.getSystemId() + " isSummary: " + isSummary);
			if (users != null && !users.isEmpty()) {
				for (String report_user : users) {
					logger.info(user.getSystemId() + " Checking Report For " + report_user);
					if (isSummary) {
						if (groupby.equalsIgnoreCase("country")) {
							query = "select count(msg_id) as count,date(submitted_time) as date,oprCountry,status,cost from mis_"
									+ report_user + " where ";
						} else {
							query = "select count(msg_id) as count,date(submitted_time) as date,source_no,oprCountry,status from mis_"
									+ report_user + " where ";
						}
					} else {
						if (to_gmt != null) {
							query = "select CONVERT_TZ(submitted_time,'" + from_gmt + "','" + to_gmt
									+ "') as submitted_time,";
						} else {
							query = "select submitted_time,";
						}
						query += "msg_id,oprCountry,source_no,dest_no,cost,status,route_to_smsc,err_code,";
						if (to_gmt != null) {
							query += "CONVERT_TZ(deliver_time,'" + from_gmt + "','" + to_gmt + "') as deliver_time";
						} else {
							query += "deliver_time";
						}
						query += " from mis_" + report_user + " where ";
					}
					if (status != null && status.trim().length() > 0) {
						if (!status.equals("%")) {
							query += "status = '" + status + "' and ";
						}
					}
					if (senderId != null && senderId.trim().length() > 0) {
						if (senderId.contains("%")) {
							query += "source_no like \"" + senderId + "\" and ";
						} else {
							query += "source_no =\"" + senderId + "\" and ";
						}
					}
					if (destination != null && destination.trim().length() > 0) {
						if (destination.contains("%")) {
							query += "dest_no like '" + destination + "' and ";
						} else {
							query += "dest_no ='" + destination + "' and ";
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
								query += "oprCountry in (" + oprCountry + ") and ";
							}
						}
					}
					if (customReportForm.getContent() != null) {
						String hexmsg = "";
						if (customReportForm.getContentType().equalsIgnoreCase("7bit")) {
							hexmsg = SevenBitChar.getHexValue(customReportForm.getContent());
							// System.out.println("Hex: " + hexmsg);
							hexmsg = Converter.getContent(hexmsg.toCharArray());
							// System.out.println("content: " + hexmsg);
							hexmsg = new Converters().UTF16(hexmsg);
						} else {
							hexmsg = customReportForm.getContent();
						}
						System.out.println(customReportForm.getContentType() + " Content: " + hexmsg);
						query += "msg_id in (select msg_id from content_" + report_user + " where content like '%"
								+ hexmsg + "%') and ";
					}
					if (to_gmt != null) {
						SimpleDateFormat client_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						client_formatter.setTimeZone(TimeZone.getTimeZone(webMasterEntry.getGmt()));
						SimpleDateFormat local_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						try {
							String start_msg_id = local_formatter.format(client_formatter.parse(startDate));
							String end_msg_id = local_formatter.format(client_formatter.parse(endDate));
							start_msg_id = start_msg_id.replaceAll("-", "");
							start_msg_id = start_msg_id.replaceAll(":", "");
							start_msg_id = start_msg_id.replaceAll(" ", "");
							start_msg_id = start_msg_id.substring(2);
							start_msg_id += "0000000";
							end_msg_id = end_msg_id.replaceAll("-", "");
							end_msg_id = end_msg_id.replaceAll(":", "");
							end_msg_id = end_msg_id.replaceAll(" ", "");
							end_msg_id = end_msg_id.substring(2);
							end_msg_id += "0000000";
							query += "msg_id between " + start_msg_id + " and " + end_msg_id;
						} catch (Exception e) {
							query += "submitted_time between CONVERT_TZ('" + startDate + "','" + to_gmt + "','"
									+ from_gmt + "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt
									+ "')";
						}
					} else {
						if (startDate.equalsIgnoreCase(endDate)) {
							String start_msg_id = startDate.substring(2);
							start_msg_id = start_msg_id.replaceAll("-", "");
							start_msg_id = start_msg_id.replaceAll(":", "");
							start_msg_id = start_msg_id.replaceAll(" ", "");
							query += "msg_id like '" + start_msg_id + "%'";
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
							query += "msg_id between " + start_msg_id + " and " + end_msg_id + "";
						}
					}
					if (isSummary) {
						if (groupby.equalsIgnoreCase("country")) {
							query += " group by date(submitted_time),oprCountry,status,cost";
						} else {
							query += " group by date(submitted_time),source_no,oprCountry,status";
						}
						logger.info(user.getSystemId() + " ReportSQL:" + query);
						list = getDlrSummaryReport(report_user, query, groupby);
						logger.info(user.getSystemId() + " list:" + list.size());
					} else {
						// query += " order by submitted_time DESC,oprCountry ASC,msg_id DESC";
						logger.debug(user.getSystemId() + " ReportSQL:" + query);
						if (isContent) {
							Map map = getDlrReport(report_user, query, webMasterEntry.isHideNum());
							if (!map.isEmpty()) {
								try {
									list = getMessageContent(map, report_user);
								} catch (Exception e) {
									list = new ArrayList(map.values());
								}
							}
						} else {
							list = getCustomizedReport(report_user, query, webMasterEntry.isHideNum());
						}
					}
					if (list != null && !list.isEmpty()) {
						// System.out.println(report_user + " Report List Size --> " + list.size());
						final_list.addAll(list);
						list.clear();
					}
				}
				boolean unproc = true;
				if (status != null && status.trim().length() > 0) {
					if (!status.equals("%")) {
						unproc = false;
					}
				}
				if (unproc) {
					// check for unprocessed/Blocked/M/F entries
					String cross_unprocessed_query = "";
					if (isSummary) {
						if (groupby.equalsIgnoreCase("country")) {
							cross_unprocessed_query = "select count(msg_id) as count,date(time) as date,oprCountry,s_flag,cost,username from table_name where s_flag not like 'E' and ";
						} else {
							cross_unprocessed_query = "select count(msg_id) as count,date(time) as date,source_no,oprCountry,s_flag from table_name where s_flag not like 'E' and ";
						}
					} else {
						if (to_gmt != null) {
							cross_unprocessed_query = "select CONVERT_TZ(time,'" + from_gmt + "','" + to_gmt
									+ "') as time,";
						} else {
							cross_unprocessed_query = "select time,";
						}
						cross_unprocessed_query += "msg_id,username,oprCountry,source_no,destination_no,cost,s_flag,smsc";
						if (isContent) {
							cross_unprocessed_query += ",content,dcs";
						}
						cross_unprocessed_query += " from table_name where ";
					}
					cross_unprocessed_query += "username in('" + String.join("','", users) + "') and ";
					if (senderId != null && senderId.trim().length() > 0) {
						if (senderId.contains("%")) {
							cross_unprocessed_query += "source_no like \"" + senderId + "\" and ";
						} else {
							cross_unprocessed_query += "source_no =\"" + senderId + "\" and ";
						}
					}
					if (customReportForm.getSmscName() != null && customReportForm.getSmscName().length() > 1) {
						cross_unprocessed_query += "smsc ='" + customReportForm.getSmscName() + "' and ";
					}
					if (destination != null && destination.trim().length() > 0) {
						if (destination.contains("%")) {
							cross_unprocessed_query += "destination_no like '" + destination + "' and ";
						} else {
							cross_unprocessed_query += "destination_no ='" + destination + "' and ";
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
								cross_unprocessed_query += "oprCountry in (" + oprCountry + ") and ";
							}
						}
					}
					if (customReportForm.getContent() != null) {
						String hexmsg = "";
						if (customReportForm.getContentType().equalsIgnoreCase("7bit")) {
							hexmsg = SevenBitChar.getHexValue(customReportForm.getContent());
							// System.out.println("Hex: " + hexmsg);
							hexmsg = Converter.getContent(hexmsg.toCharArray());
						} else {
							hexmsg = customReportForm.getContent();
						}
						System.out.println(customReportForm.getContentType() + " Content: " + hexmsg);
						cross_unprocessed_query += " content like '%" + hexmsg + "%' and ";
					}
					if (to_gmt != null) {
						SimpleDateFormat client_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						client_formatter.setTimeZone(TimeZone.getTimeZone(webMasterEntry.getGmt()));
						SimpleDateFormat local_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						try {
							String start_msg_id = local_formatter.format(client_formatter.parse(startDate));
							String end_msg_id = local_formatter.format(client_formatter.parse(endDate));
							start_msg_id = start_msg_id.replaceAll("-", "");
							start_msg_id = start_msg_id.replaceAll(":", "");
							start_msg_id = start_msg_id.replaceAll(" ", "");
							start_msg_id = start_msg_id.substring(2);
							start_msg_id += "0000000";
							end_msg_id = end_msg_id.replaceAll("-", "");
							end_msg_id = end_msg_id.replaceAll(":", "");
							end_msg_id = end_msg_id.replaceAll(" ", "");
							end_msg_id = end_msg_id.substring(2);
							end_msg_id += "0000000";
							cross_unprocessed_query += "msg_id between " + start_msg_id + " and " + end_msg_id;
						} catch (Exception e) {
							cross_unprocessed_query += "time between CONVERT_TZ('" + startDate + "','" + to_gmt + "','"
									+ from_gmt + "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt
									+ "')";
						}
					} else {
						if (startDate.equalsIgnoreCase(endDate)) {
							String start_msg_id = startDate.substring(2);
							start_msg_id = start_msg_id.replaceAll("-", "");
							start_msg_id = start_msg_id.replaceAll(":", "");
							start_msg_id = start_msg_id.replaceAll(" ", "");
							cross_unprocessed_query += "msg_id like '" + start_msg_id + "%'";
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
							cross_unprocessed_query += "msg_id between " + start_msg_id + " and " + end_msg_id + "";
						}
					}
					if (isSummary) {
						if (groupby.equalsIgnoreCase("country")) {
							cross_unprocessed_query += " group by username,date(time),oprCountry,s_flag,cost";
						} else {
							cross_unprocessed_query += " group by date(time),source_no,oprCountry,s_flag";
						}
						List unproc_list = getUnprocessedSummary(
								cross_unprocessed_query.replaceAll("table_name", "unprocessed"), groupby);
						if (unproc_list != null && !unproc_list.isEmpty()) {
							final_list.addAll(unproc_list);
						}
						unproc_list = getUnprocessedSummary(cross_unprocessed_query.replaceAll("table_name", "smsc_in"),
								groupby);
						if (unproc_list != null && !unproc_list.isEmpty()) {
							final_list.addAll(unproc_list);
						}
					} else {
						List unproc_list = getUnprocessedReport(
								cross_unprocessed_query.replaceAll("table_name", "unprocessed"),
								webMasterEntry.isHideNum(), isContent);
						if (unproc_list != null && !unproc_list.isEmpty()) {
							final_list.addAll(unproc_list);
						}
						unproc_list = getUnprocessedReport(cross_unprocessed_query.replaceAll("table_name", "smsc_in"),
								webMasterEntry.isHideNum(), isContent);
						if (unproc_list != null && !unproc_list.isEmpty()) {
							final_list.addAll(unproc_list);
						}
					}
				}
				// logger.info(userSessionObject.getSystemId() + " ReportSQL: " +
				// cross_unprocessed_query);
				// end check for unprocessed/Blocked/M/F entries
			}
			logger.info(user.getSystemId() + " End Based On Criteria. Final Report Size: " + final_list.size());
		}
		return final_list;

	}

	private List getDlrSummaryReport(String report_user, String query, String groupby) {
		List customReport = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		DeliveryDTO report = null;
		try {
			Connection connection = getConnection();

			// con = dbCon.getConnection();
			pStmt = con.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					try {
						String date = rs.getString("date");
						/*
						 * try { // -------------- Converting date format ------------ Date dateobj =
						 * new SimpleDateFormat("yyyy-MM-dd").parse(date); date = new
						 * SimpleDateFormat("dd-MMM-yyyy").format(dateobj); } catch (ParseException ex)
						 * { }
						 */
						String oprCountry = rs.getString("oprCountry");
						int network_id = 0;
						try {
							network_id = Integer.parseInt(oprCountry);
						} catch (Exception ex) {
						}
						String country = null, operator = null;
						if (GlobalVars.NetworkEntries.containsKey(network_id)) {
							NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
							country = network.getCountry();
							operator = network.getOperator();
						} else {
							country = oprCountry;
							operator = oprCountry;
						}
						if (groupby.equalsIgnoreCase("country")) {
							report = new DeliveryDTO(country, operator, rs.getDouble("cost"), date,
									rs.getString("status"), rs.getInt("count"));
							report.setUsername(report_user);
						} else {
							report = new DeliveryDTO(country, operator, 0, date, rs.getString("status"),
									rs.getInt("count"));
							report.setUsername(report_user);
							report.setSender(rs.getString("source_no"));
						}
						customReport.add(report);
					} catch (Exception sqle) {
						logger.error(" ", sqle.fillInStackTrace());
					}
				}
			}
		} catch (SQLException ex) {
			if (ex.getMessage().contains("Table") && ex.getMessage().contains("doesn't exist")) {
				logger.error("<-- " + report_user + " Mis & Content Table Doesn't Exist -->");
				// createMisTable(username);
				// createContentTable(username);
			} else {
				logger.error(" ", ex.fillInStackTrace());
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
		return customReport;

	}

	public List getDistinctMisUser(String userQuery) throws SQLException {
		List userSet = new ArrayList();
		Connection con = null;
		Statement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.createStatement();
			rs = pStmt.executeQuery(userQuery);
			while (rs.next()) {
				userSet.add(rs.getString("username"));
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqle) {
				}
			}
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException sqle) {
				}
			}
			if (con != null) {
				con.close();
			}
		}
		return userSet;
	}

	public List getUnprocessedReport(String query, boolean hide_number, boolean isContent) {
		List customReport = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		// DBMessage dbMsg = null;
		// boolean replace = false;
		String dest = "";
		DeliveryDTO report = null;
		// int counter = 0;
		System.out.println("UnprocessedReport:" + query);
		String msg_id = null;
		try {
			Set<String> flags = getSmscErrorFlagSymbol();
			Connection connection = getConnection();

			// con = dbCon.getConnection();
			pStmt = con.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					msg_id = rs.getString("msg_id");
					String status = rs.getString("s_flag");
					if (status != null) {
						if (status.equalsIgnoreCase("B")) {
							status = "BLOCKED";
						} else if (status.equalsIgnoreCase("M")) {
							status = "MINCOST";
						} else if (status.equalsIgnoreCase("F")) {
							status = "NONRESP";
						} else if (status.equalsIgnoreCase("Q")) {
							status = "QUEUED";
						} else {
							if (status.equalsIgnoreCase("E")) {
								continue; // already in mis_tables
							} else if (flags.contains(status.toUpperCase())) {
								continue; // already in mis_tables
							} else {
								status = "UNPROCD";
							}
						}
					}
					try {
						dest = rs.getString("destination_no");
						if (hide_number) {
							String newDest = dest.substring(0, dest.length() - 2);
							dest = newDest + "**";
						}
						String[] time = rs.getString("time").split(" ");
						String oprCountry = rs.getString("oprCountry");
						int network_id = 0;
						try {
							network_id = Integer.parseInt(oprCountry);
						} catch (Exception ex) {
						}
						String country = null, operator = null;
						if (GlobalVars.NetworkEntries.containsKey(network_id)) {
							NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
							country = network.getCountry();
							operator = network.getOperator();
						} else {
							country = oprCountry;
							operator = oprCountry;
						}
						report = new DeliveryDTO(msg_id, country, operator, dest, rs.getString("source_no"),
								rs.getDouble("cost"), time[0], time[1], status);
						report.setUsername(rs.getString("username"));
						report.setRoute(rs.getString("smsc"));
						if (isContent) {
							String message = null;
							if (rs.getInt("dcs") == 0) {
								message = rs.getString("content").trim();
							} else {
								try {
									String content = rs.getString("content").trim();
									// System.out.println("content: " + content);
									if (content.contains("0000")) {
										// logger.info(msg_id + " Content: " + content);
										content = content.replaceAll("0000", "0040");
										// logger.info(msg_id + " Replaced: " + content);
									}
									message = hexCodePointsToCharMsg(content);
								} catch (Exception ex) {
									message = "Conversion Failed";
								}
							}
							report.setContent(message);
						}
						customReport.add(report);
						/*
						 * if (++counter > 1000) { counter = 0; logger.info(username +
						 * " Report List Counter:--> " + customReport.size()); }
						 */
					} catch (Exception sqle) {
						logger.error(msg_id, sqle.fillInStackTrace());
					}
				}
				// logger.info(" Report List Count:--> " + customReport.size());
			}
		} catch (SQLException ex) {
			logger.error(" ", ex.fillInStackTrace());
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
		logger.info(" Report List Count:--> " + customReport.size());
		return customReport;
	}

	private Set<String> getSmscErrorFlagSymbol() {
		Set<String> map = new HashSet<>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "select distinct(Flag_symbol) from smsc_error_code";
		try {
			Connection connection = getConnection();

			// con = dbCon.getConnection();
			pStmt = con.prepareStatement(sql);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				String flag_symbol = rs.getString("Flag_symbol");
				if (flag_symbol != null && flag_symbol.length() > 0) {
					map.add(flag_symbol.toUpperCase());
				}
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
		return map;
	}

	public List getUnprocessedSummary(String query, String groupby) {
		List customReport = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		DeliveryDTO report = null;
		System.out.println("UnprocessedSummary:" + query);
		try {
			Connection connection = getConnection();

			// con = dbCon.getConnection();
			pStmt = con.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					try {
						String date = rs.getString("date");
						/*
						 * try { // -------------- Converting date format ------------ Date dateobj =
						 * new SimpleDateFormat("yyyy-MM-dd").parse(date); date = new
						 * SimpleDateFormat("dd-MMM-yyyy").format(dateobj); } catch (ParseException ex)
						 * { }
						 */
						String status = rs.getString("s_flag");
						if (status != null) {
							if (status.equalsIgnoreCase("B")) {
								status = "BLOCKED";
							} else if (status.equalsIgnoreCase("M")) {
								status = "MINCOST";
							} else if (status.equalsIgnoreCase("F")) {
								status = "NONRESP";
							} else if (status.equalsIgnoreCase("Q")) {
								status = "QUEUED";
							} else {
								status = "UNPROCD";
							}
						}
						String oprCountry = rs.getString("oprCountry");
						int network_id = 0;
						try {
							network_id = Integer.parseInt(oprCountry);
						} catch (Exception ex) {
						}
						String country = null, operator = null;
						if (GlobalVars.NetworkEntries.containsKey(network_id)) {
							NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
							country = network.getCountry();
							operator = network.getOperator();
						} else {
							country = oprCountry;
							operator = oprCountry;
						}
						if (groupby.equalsIgnoreCase("country")) {
							String username = rs.getString("username");
							report = new DeliveryDTO(country, operator, rs.getDouble("cost"), date, status,
									rs.getInt("count"));
							report.setUsername(username);
						} else {
							report = new DeliveryDTO(country, operator, 0, date, status, rs.getInt("count"));
							report.setSender(rs.getString("source_no"));
						}
						customReport.add(report);
					} catch (Exception sqle) {
						logger.error(" ", sqle.fillInStackTrace());
					}
				}
			}
		} catch (SQLException ex) {
			logger.error(" ", ex.fillInStackTrace());
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
		logger.info(" Report List Count:--> " + customReport.size());
		return customReport;
	}

	public List getCustomizedReport(String username, String query, boolean hideNum) {
		List customReport = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		// DBMessage dbMsg = null;
		// boolean replace = false;
		String dest = "";
		DeliveryDTO report = null;
		// int counter = 0;
		System.out.println("SQL: " + query);
		String msg_id = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					msg_id = rs.getString("msg_id");
					try {
						dest = rs.getString("dest_no");
						if (hideNum) {
							String newDest = dest.substring(0, dest.length() - 2);
							dest = newDest + "**";
						}
						String[] time = rs.getString("submitted_time").split(" ");
						String oprCountry = rs.getString("oprCountry");
						int network_id = 0;
						try {
							network_id = Integer.parseInt(oprCountry);
						} catch (Exception ex) {
						}
						String country = null, operator = null;
						if (GlobalVars.NetworkEntries.containsKey(network_id)) {
							NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
							country = network.getCountry();
							operator = network.getOperator();
						} else {
							country = oprCountry;
							operator = oprCountry;
						}
						report = new DeliveryDTO(msg_id, country, operator, dest, rs.getString("source_no"),
								rs.getDouble("cost"), time[0], time[1], rs.getString("status"));
						report.setDeliverOn(rs.getString("deliver_time"));
						report.setUsername(username);
						report.setRoute(rs.getString("Route_to_SMSC"));
						report.setErrCode(rs.getString("err_code"));
						customReport.add(report);
						/*
						 * if (++counter > 1000) { counter = 0; logger.info(username +
						 * " Report List Counter:--> " + customReport.size()); }
						 */
					} catch (Exception sqle) {
						logger.error(msg_id, sqle.fillInStackTrace());
					}
				}
				// logger.info(username + " Report List Final Count:--> " +
				// customReport.size());
			}
		} catch (SQLException ex) {
			if (ex.getMessage().contains("Table") && ex.getMessage().contains("doesn't exist")) {
				logger.info("<-- " + username + " Mis & Content Table Doesn't Exist -->");
				// createMisTable(username);
				// createContentTable(username);
			} else {
				logger.error(" ", ex.fillInStackTrace());
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
		return customReport;
	}

	public List getMessageContent(Map map, String username) {
		List list = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = null;
		try {
			String keys = map.keySet().toString();
			keys = keys.substring(1, keys.length() - 1);
			sql = "select msg_id,dcs,content from content_" + username + " where msg_id in(" + keys + ")";

			Connection connection = getConnection();
			pStmt = con.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				int dcs = rs.getInt("dcs");
				String msg_id = rs.getString("msg_id");
				String message = null;
				if (dcs == 0 || dcs == 240) {
					message = rs.getString("content");
				} else {
					try {
						String content = rs.getString("content").trim();
						// System.out.println("content: " + content);
						if (content.contains("0000")) {
							// logger.info(msg_id + " Content: " + content);
							content = content.replaceAll("0000", "0040");
							// logger.info(msg_id + " Replaced: " + content);
						}
						message = hexCodePointsToCharMsg(content);
					} catch (Exception ex) {
						message = "Conversion Failed";
					}
				}
				if (map.containsKey(msg_id)) {
					DeliveryDTO report = (DeliveryDTO) map.remove(msg_id);
					report.setContent(message);
					list.add(report);
				}
			}
			logger.info("Objects without Content: " + map.size());
			list.addAll(map.values());
		} catch (Exception sqle) {
			if (sqle.getMessage().contains("doesn't exist")) {
				// Table doesn't exist. Create New
				logger.info("content_" + username + " Table Doesn't Exist. Creating New");
				// createContentTable(username);
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
		return list;
	}

	private Map getDlrReport(String reportUser, String query, boolean hideNum) {
		// public Map getDlrReport(String reportUser, String query, boolean hideNum)
		// throws DBException {
		Map customReport = new HashMap();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		// DBMessage dbMsg = null;
		// boolean replace = false;
		String dest = "";
		// if (hideNum.equalsIgnoreCase("yes")) {
		// replace = true;
		// }
		System.out.println("SQL: " + query);
		DeliveryDTO report = null;
		String msg_id = null;
		try {
			Connection connection = getConnection();

			// con = dbCon.getConnection();
			pStmt = con.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					msg_id = rs.getString("msg_id");
					try {
						dest = rs.getString("dest_no");
						if (hideNum) {
							String newDest = dest.substring(0, dest.length() - 2);
							dest = newDest + "**";
						}
						String oprCountry = rs.getString("oprCountry");
						int network_id = 0;
						try {
							network_id = Integer.parseInt(oprCountry);
						} catch (Exception ex) {
						}
						String[] time = rs.getString("submitted_time").split(" ");
						String country = null, operator = null;
						if (GlobalVars.NetworkEntries.containsKey(network_id)) {
							NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
							country = network.getCountry();
							operator = network.getOperator();
						} else {
							country = oprCountry;
							operator = oprCountry;
						}
						report = new DeliveryDTO(msg_id, country, operator, dest, rs.getString("source_no"),
								rs.getDouble("cost"), time[0], time[1], rs.getString("status"));
						report.setDeliverOn(rs.getString("deliver_time"));
						report.setUsername(reportUser);
						if (rs.getString("err_code") == null) {
							report.setErrCode("000");
						} else {
							report.setErrCode(rs.getString("err_code"));
						}
						customReport.put(rs.getString("msg_id"), report);
					} catch (Exception sqle) {
						logger.error(msg_id, sqle.fillInStackTrace());
					}
				}
			}
		} catch (SQLException ex) {
			if (ex.getMessage().contains("Table") && ex.getMessage().contains("doesn't exist")) {
				logger.info("<-- " + reportUser + " Mis & Content Table Doesn't Exist -->");
				// createMisTable(username);
				// createContentTable(username);
			} else {
				logger.error(" ", ex.fillInStackTrace());
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
		return customReport;

	}

	public List getDLRReport(UserEntryExt reportUser, String reporttime) {
		DBMessage dbMsg = null;
		List<DBMessage> report = new ArrayList<DBMessage>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String query = null;
		if (reportUser.getWebMasterEntry().isHidden()) {
			query = "select SOURCE_NO,CONCAT(left(DEST_NO,length(dest_no)-2),'**') as mobile, SUBMITTED_TIME, deliver_time, STATUS from mis_"
					+ reportUser.getUserEntry().getSystemId() + " where DATE(submitted_time) = '" + reporttime
					+ "' order by submitted_time ASC";
		} else {
			query = "select SOURCE_NO,DEST_NO as mobile, SUBMITTED_TIME, deliver_time, STATUS from mis_"
					+ reportUser.getUserEntry().getSystemId() + " where DATE(submitted_time) = '" + reporttime
					+ "' order by submitted_time ASC";
		}
		logger.info("SQL: " + query);
		try {
			Connection connection = getConnection();

			// con = Connection.getConnection();
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
				logger.info("mis_" + reportUser.getUserEntry().getSystemId() + " Table Doesn't Exist. Creating New");
				// createMisTable(regUser.getSystemId());
			} else {
				logger.error(reportUser.getUserEntry().getSystemId(), sqle.fillInStackTrace());
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

	public JasperPrint getSummaryJasperPrint(List reportList, boolean paging, String username) throws JRException {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		JasperPrint print = null;
		JasperReport report = null;
		JasperDesign design = null;
		String groupby = "country";
		String reportUser = null;
		final String template_file = IConstants.FORMAT_DIR + "report//dlrReport.jrxml";
		final String template_sender_file = IConstants.FORMAT_DIR + "report//dlrReportSender.jrxml";
		final String template_content_file = IConstants.FORMAT_DIR + "report//dlrContentReport.jrxml";
		final String template_content_sender_file = IConstants.FORMAT_DIR + "report//dlrContentWithSender.jrxml";
		final String summary_template_file = IConstants.FORMAT_DIR + "report//dlrSummaryReport.jrxml";
		final String summary_sender_file = IConstants.FORMAT_DIR + "report//dlrSummarySender.jrxml";

		Map parameters = new HashMap();
		if (groupby.equalsIgnoreCase("country")) {
			design = JRXmlLoader.load(summary_template_file);
		} else {
			design = JRXmlLoader.load(summary_sender_file);
		}

		report = JasperCompileManager.compileReport(design);
		if (groupby.equalsIgnoreCase("country")) {
			reportList = sortListByCountry(reportList);
			logger.info(user.getSystemId() + " <-- Preparing Charts --> ");
			// ------------- Preparing databeancollection for chart ------------------
			Iterator itr = reportList.iterator();
			Map temp_chart = new HashMap();
			Map deliv_map = new LinkedHashMap();
			Map total_map = new HashMap();
			while (itr.hasNext()) {
				DeliveryDTO chartDTO = (DeliveryDTO) itr.next();
				String status = chartDTO.getStatus();
				int counter = 0;
				if (temp_chart.containsKey(status)) {
					counter = (Integer) temp_chart.get(status);
				}
				counter = counter + chartDTO.getStatusCount();
				temp_chart.put(status, counter);
				// --------------- Bar Chart -------------------
				if (status.startsWith("DELIV")) {
					int delivCounter = 0;
					if (deliv_map.containsKey(chartDTO.getCountry())) {
						delivCounter = (Integer) deliv_map.get(chartDTO.getCountry());
					}
					delivCounter = delivCounter + chartDTO.getStatusCount();
					deliv_map.put(chartDTO.getCountry(), delivCounter);
				}
				int total_counter = 0;
				if (total_map.containsKey(chartDTO.getCountry())) {
					total_counter = (Integer) total_map.get(chartDTO.getCountry());
				}
				total_counter = total_counter + chartDTO.getStatusCount();
				total_map.put(chartDTO.getCountry(), total_counter);
			}
			List<DeliveryDTO> chart_list = new ArrayList();
			if (!temp_chart.isEmpty()) {
				itr = temp_chart.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry entry = (Map.Entry) itr.next();
					DeliveryDTO chartDTO = new DeliveryDTO((String) entry.getKey(), (Integer) entry.getValue());
					chart_list.add(chartDTO);
				}
			}
			List<DeliveryDTO> bar_chart_list = new ArrayList();
			total_map = sortMapByDscValue(total_map, 10);
			DeliveryDTO chartDTO = null;
			itr = total_map.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry entry = (Map.Entry) itr.next();
				String country = (String) entry.getKey();
				int total = (Integer) entry.getValue();
				chartDTO = new DeliveryDTO("SUBMITTED", total);
				chartDTO.setCountry(country);
				bar_chart_list.add(chartDTO);
				int delivered = 0;
				if (deliv_map.containsKey(country)) {
					delivered = (Integer) deliv_map.get(country);
				}
				chartDTO = new DeliveryDTO("DELIVRD", delivered);
				chartDTO.setCountry(country);
				bar_chart_list.add(chartDTO);
				// System.out.println(country + " -> " + total + " : " + delivered);
			}
			JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(chart_list);
			parameters.put("piechartDataSource", piechartDataSource);
			JRBeanCollectionDataSource barchart1DataSource = new JRBeanCollectionDataSource(bar_chart_list);
			parameters.put("barchart1DataSource", barchart1DataSource);
		} else {
			Map<String, DeliveryDTO> map = new LinkedHashMap<String, DeliveryDTO>();
			reportList = sortListBySender(reportList);
			Iterator itr = reportList.iterator();
			DeliveryDTO reportDTO = null;
			DeliveryDTO tempDTO = null;
			while (itr.hasNext()) {
				reportDTO = (DeliveryDTO) itr.next();
				String key = reportDTO.getDate() + "#" + reportDTO.getSender().toLowerCase() + "#"
						+ reportDTO.getCountry() + "#" + reportDTO.getOperator();
				if (map.containsKey(key)) {
					tempDTO = map.get(key);
				} else {
					tempDTO = new DeliveryDTO();
					tempDTO.setDate(reportDTO.getDate());
					tempDTO.setCountry(reportDTO.getCountry());
					tempDTO.setOperator(reportDTO.getOperator());
					tempDTO.setSender(reportDTO.getSender());
				}
				// System.out.println(key + " :-> " + reportDTO.getStatus() + " " +
				// reportDTO.getStatusCount());
				if (reportDTO.getStatus().startsWith("DELIV")) {
					tempDTO.setDelivered(tempDTO.getDelivered() + reportDTO.getStatusCount());
				}
				tempDTO.setSubmitted(tempDTO.getSubmitted() + reportDTO.getStatusCount());
				System.out.println(
						key + " :-> " + " Submit: " + tempDTO.getSubmitted() + " Delivered: " + tempDTO.getDelivered());
				map.put(key, tempDTO);
			}
			reportList.clear();
			reportList.addAll(map.values());
		}
		logger.info(user.getSystemId() + " <-- Preparing report --> ");
		String time_interval = reportUser;
		if (!reportList.isEmpty()) {
			String first_date = ((DeliveryDTO) reportList.get(0)).getDate();
			String last_date = ((DeliveryDTO) reportList.get((reportList.size() - 1))).getDate();
			if (first_date.equalsIgnoreCase(last_date)) {
				time_interval += " [ For " + first_date + " ]";
			} else {
				time_interval += " [ From " + first_date + " To " + last_date + " ]";
			}
		}
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		if (reportList.size() > 20000) {
			logger.info(user.getSystemId() + " <-- Creating Virtualizer --> ");
			JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(1000,
					new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 2048, 1024));
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		}
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		parameters.put("time_interval", time_interval);
		logger.info(user.getSystemId() + " <-- Filling Report Data --> ");
		print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		logger.info(user.getSystemId() + " <-- Filling Finished --> ");
		return print;
	}

	public List sortListBySender(List list) {
		// logger.info(userSessionObject.getSystemId() + " sortListBySender ");
		boolean isSummary = false;
		Comparator<DeliveryDTO> comparator = null;
		if (isSummary) {
			comparator = Comparator.comparing(DeliveryDTO::getDate).thenComparing(DeliveryDTO::getSender);
		} else {
			comparator = Comparator.comparing(DeliveryDTO::getDate).thenComparing(DeliveryDTO::getSender)
					.thenComparing(DeliveryDTO::getMsgid);
		}
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	public List sortListByCountry(List reportList) {
		boolean isSummary = false;

		// logger.info(user.getSystemId() + " sortListByCountry ");
		Comparator<DeliveryDTO> comparator = null;
		if (isSummary) {
			comparator = Comparator.comparing(DeliveryDTO::getDate).thenComparing(DeliveryDTO::getCountry);
		} else {
			comparator = Comparator.comparing(DeliveryDTO::getDate).thenComparing(DeliveryDTO::getCountry)
					.thenComparing(DeliveryDTO::getMsgid);
		}
		Stream<DeliveryDTO> personStream = reportList.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	public JasperPrint getCustomizedJasperPrint(List<DeliveryDTO> reportList, boolean paging, String username)
			throws Exception {
		final String template_file = IConstants.FORMAT_DIR + "report//dlrReport.jrxml";
		String template_sender_file = IConstants.FORMAT_DIR + "report//dlrReportSender.jrxml";
		String template_content_file = IConstants.FORMAT_DIR + "report//dlrContentReport.jrxml";
		String template_content_sender_file = IConstants.FORMAT_DIR + "report//dlrContentWithSender.jrxml";
		String summary_template_file = IConstants.FORMAT_DIR + "report//dlrSummaryReport.jrxml";
		String summary_sender_file = IConstants.FORMAT_DIR + "report//dlrSummarySender.jrxml";

		JasperPrint print = null;
		JasperReport report = null;
		JasperDesign design = null;
		String groupby = "country";
		boolean isContent = false;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		// boolean isContent;
		Map parameters = new HashMap();
		if (groupby.equalsIgnoreCase("country")) {

			if (isContent) {
				design = JRXmlLoader.load(template_content_file);
			} else {
				design = JRXmlLoader.load(template_file);
			}
		} else {
			if (isContent) {
				design = JRXmlLoader.load(template_content_sender_file);
			} else {
				design = JRXmlLoader.load(template_sender_file);
			}
		}
		report = JasperCompileManager.compileReport(design);
		// ---------- Sorting list ----------------------------
		if (groupby.equalsIgnoreCase("country")) {
			reportList = sortListByCountry(reportList);
		} else {
			reportList = sortListBySender(reportList);
		}
		// ------------- Preparing databeancollection for chart ------------------
		logger.info(user.getSystemId() + " <-- Preparing Charts --> ");
		// Iterator itr = reportList.iterator();
		Map<String, Integer> temp_chart = new HashMap<String, Integer>();
		Map<String, Integer> deliv_map = new LinkedHashMap<String, Integer>();
		Map<String, Integer> total_map = new HashMap<String, Integer>();
		for (DeliveryDTO chartDTO : reportList) {
			// System.out.println("Cost: " + chartDTO.getCost());
			String status = chartDTO.getStatus();
			int counter = 0;
			if (temp_chart.containsKey(status)) {
				counter = temp_chart.get(status);
			}
			temp_chart.put(status, ++counter);
			// --------- bar Chart ------------
			if (groupby.equalsIgnoreCase("country")) {
				if (status.startsWith("DELIV")) {
					int delivCounter = 0;
					if (deliv_map.containsKey(chartDTO.getCountry())) {
						delivCounter = deliv_map.get(chartDTO.getCountry());
					}
					deliv_map.put(chartDTO.getCountry(), ++delivCounter);
				}
				int total_counter = 0;
				if (total_map.containsKey(chartDTO.getCountry())) {
					total_counter = total_map.get(chartDTO.getCountry());
				}
				total_map.put(chartDTO.getCountry(), ++total_counter);
			} else {
				if (status.startsWith("DELIV")) {
					int delivCounter = 0;
					if (deliv_map.containsKey(chartDTO.getSender())) {
						delivCounter = deliv_map.get(chartDTO.getSender());
					}
					deliv_map.put(chartDTO.getSender(), ++delivCounter);
				}
				int total_counter = 0;
				if (total_map.containsKey(chartDTO.getSender())) {
					total_counter = total_map.get(chartDTO.getSender());
				}
				total_map.put(chartDTO.getSender(), ++total_counter);
			}
			// --------------------------------
		}
		List<DeliveryDTO> chart_list = new ArrayList<DeliveryDTO>();
		if (!temp_chart.isEmpty()) {
			for (Map.Entry<String, Integer> entry : temp_chart.entrySet()) {
				chart_list.add(new DeliveryDTO((String) entry.getKey(), (Integer) entry.getValue()));
			}
		}
		List<DeliveryDTO> bar_chart_list = new ArrayList<DeliveryDTO>();
		total_map = sortMapByDscValue(total_map, 5);
		DeliveryDTO chartDTO = null;
		for (Map.Entry<String, Integer> entry : total_map.entrySet()) {
			if (groupby.equalsIgnoreCase("country")) {
				chartDTO = new DeliveryDTO("SUBMITTED", entry.getValue());
				chartDTO.setCountry(entry.getKey());
				bar_chart_list.add(chartDTO);
				int delivered = 0;
				if (deliv_map.containsKey(entry.getKey())) {
					delivered = (Integer) deliv_map.get(entry.getKey());
				}
				chartDTO = new DeliveryDTO("DELIVRD", delivered);
				chartDTO.setCountry(entry.getKey());
				bar_chart_list.add(chartDTO);
			} else {
				chartDTO = new DeliveryDTO("SUBMITTED", entry.getValue());
				chartDTO.setSender(entry.getKey());
				bar_chart_list.add(chartDTO);
				int delivered = 0;
				if (deliv_map.containsKey(entry.getKey())) {
					delivered = (Integer) deliv_map.get(entry.getKey());
				}
				chartDTO = new DeliveryDTO("DELIVRD", delivered);
				chartDTO.setSender(entry.getKey());
				bar_chart_list.add(chartDTO);
			}
		}
		JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(chart_list);
		JRBeanCollectionDataSource barchart1DataSource = new JRBeanCollectionDataSource(bar_chart_list);
		parameters.put("piechartDataSource", piechartDataSource);
		parameters.put("barchart1DataSource", barchart1DataSource);
		logger.info(user.getSystemId() + " <-- Finished Charts --> ");
		// -----------------------------------------------------------------------
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		if (reportList.size() > 20000) {
			logger.info(user.getSystemId() + " <-- Creating Virtualizer --> ");
			JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(100,
					new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 1024, 512));
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		}
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(userOptional.get().getId());

		logger.info(user.getSystemId() + " DisplayCost: " + webMasterEntry.isDisplayCost());
		parameters.put("displayCost", webMasterEntry.isDisplayCost());
		logger.info(user.getSystemId() + " <-- Filling Report Data --> ");
		print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		logger.info(user.getSystemId() + " <-- Filling Completed --> ");
		return print;

	}

	public Workbook getCustomizedWorkBook(List<DeliveryDTO> reportList, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isEmpty()) {
			throw new NotFoundException("username not found username{}" + username);
		}
		String groupby = "country";

		logger.info(username + " <-- Creating Summary WorkBook --> ");
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		int records_per_sheet = 500000;
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
		String[] headers;
		if (groupby.equalsIgnoreCase("country")) {
			headers = new String[] { "Date", "Country", "Operator", "Rate", "Status", "Count" };
		} else {
			headers = new String[] { "Date", "SenderId", "Country", "Operator", "Submitted", "Delivered", "(%)" };
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
					cell.setCellValue(reportDTO.getDate());
					cell.setCellStyle(rowStyle);
					if (groupby.equalsIgnoreCase("country")) {
						cell = row.createCell(1);
						cell.setCellValue(reportDTO.getCountry());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(2);
						cell.setCellValue(reportDTO.getOperator());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(3);
						cell.setCellValue(reportDTO.getCost());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(4);
						cell.setCellValue(reportDTO.getStatus());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(5);
						cell.setCellValue(reportDTO.getStatusCount());
						cell.setCellStyle(rowStyle);
					} else {
						cell = row.createCell(1);
						cell.setCellValue(reportDTO.getSender());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(2);
						cell.setCellValue(reportDTO.getCountry());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(3);
						cell.setCellValue(reportDTO.getOperator());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(4);
						cell.setCellValue(reportDTO.getSubmitted());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(5);
						cell.setCellValue(reportDTO.getDelivered());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(6);
						cell.setCellValue((reportDTO.getDelivered() * 100) / reportDTO.getSubmitted());
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
		logger.info(username + " <--- Summary Workbook Created ----> ");
		return workbook;
	}

	public Workbook getCustomizedSummaryWorkBook(List<DeliveryDTO> reportList, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isEmpty()) {
			throw new NotFoundException("username not found username{}" + username);
		}
		String groupby = "country";

		logger.info(username + " <-- Creating Summary WorkBook --> ");
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		int records_per_sheet = 500000;
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
		String[] headers;
		if (groupby.equalsIgnoreCase("country")) {
			headers = new String[] { "Date", "Country", "Operator", "Rate", "Status", "Count" };
		} else {
			headers = new String[] { "Date", "SenderId", "Country", "Operator", "Submitted", "Delivered", "(%)" };
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
					cell.setCellValue(reportDTO.getDate());
					cell.setCellStyle(rowStyle);
					if (groupby.equalsIgnoreCase("country")) {
						cell = row.createCell(1);
						cell.setCellValue(reportDTO.getCountry());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(2);
						cell.setCellValue(reportDTO.getOperator());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(3);
						cell.setCellValue(reportDTO.getCost());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(4);
						cell.setCellValue(reportDTO.getStatus());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(5);
						cell.setCellValue(reportDTO.getStatusCount());
						cell.setCellStyle(rowStyle);
					} else {
						cell = row.createCell(1);
						cell.setCellValue(reportDTO.getSender());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(2);
						cell.setCellValue(reportDTO.getCountry());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(3);
						cell.setCellValue(reportDTO.getOperator());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(4);
						cell.setCellValue(reportDTO.getSubmitted());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(5);
						cell.setCellValue(reportDTO.getDelivered());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(6);
						cell.setCellValue((reportDTO.getDelivered() * 100) / reportDTO.getSubmitted());
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
		logger.info(username + " <--- Summary Workbook Created ----> ");
		return workbook;
	}

	public List getdlrUnprocessedSummary(String query) throws Exception {
		List customReport = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		DeliveryDTO report = null;
		System.out.println("UnprocessedSummary:" + query);
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					try {
						String date = rs.getString("datet");
						String hours = rs.getString("hours");
						// String username = rs.getString("username");
						/*
						 * try { // -------------- Converting date format ------------ Date dateobj =
						 * new SimpleDateFormat("yyyy-MM-dd").parse(date); date = new
						 * SimpleDateFormat("dd-MMM-yyyy").format(dateobj); } catch (ParseException ex)
						 * { }
						 */
						String status = rs.getString("s_flag");
						if (status != null) {
							if (status.equalsIgnoreCase("B")) {
								status = "BLOCKED";
							} else if (status.equalsIgnoreCase("M")) {
								status = "MINCOST";
							} else if (status.equalsIgnoreCase("F")) {
								status = "NONRESP";
							} else if (status.equalsIgnoreCase("Q")) {
								status = "QUEUED";
							} else {
								status = "UNPROCD";
							}
						}
						report = new DeliveryDTO(null, null, 0, date, status, rs.getInt("count"));
						report.setTime(hours);
						customReport.add(report);
					} catch (Exception sqle) {
						logger.error(" ", sqle.fillInStackTrace());
					}
				}
			}
		} catch (SQLException ex) {
			logger.error(" ", ex.fillInStackTrace());
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
		logger.info(" Report List Count:--> " + customReport.size());
		return customReport;
	}

}
