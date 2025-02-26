package com.hti.smpp.common.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.SummaryReportForm;
import com.hti.smpp.common.response.BatchDTO;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.SummaryReportService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BadRequestException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@Service
public class SummaryReportServiceImpl implements SummaryReportService {

	@Autowired
	private DataBase dataBase;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private UserDAService userService;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SalesRepository salesRepository;

	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;
	@Autowired
	private MessageResourceBundle messageResourceBundle;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	private String template_file = IConstants.FORMAT_DIR + "report//BatchReport.jrxml";
	Logger logger = LoggerFactory.getLogger(SummaryReportServiceImpl.class);
	Locale locale = null;
	String target = IConstants.FAILURE_KEY;

	@Override
	public ResponseEntity<?> SummaryReportview(String username, SummaryReportForm customReportForm) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
		}
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
		if (webMasterEntry == null) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.WEBMASTER_ENTRY_NOT_FOUND_MESSAGE, new Object[] {user.getId()}));

		}
		try {
			logger.info(messageResourceBundle.getLogMessage("run.summary.report.view.message"));

			List<BatchDTO> reportList = getSummaryReportList(customReportForm, username, webMasterEntry);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(messageResourceBundle.getLogMessage("report.size.view.message"), user.getSystemId(), reportList.size());
				return new ResponseEntity<>(reportList, HttpStatus.OK);

			} else {
				throw new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.SUMMARY_REPORT_NOT_FOUND_MESSAGE));
			}
		} catch (NotFoundException e) {
			// Log NotFoundException
			throw new NotFoundException(e.getMessage());
		} catch (IllegalArgumentException e) {

			logger.error(messageResourceBundle.getLogMessage("invalid.argument"), e.getMessage(), e);
			throw new BadRequestException(messageResourceBundle
					.getExMessage(ConstantMessages.BAD_REQUEST_EXCEPTION_MESSAGE, new Object[] { e.getMessage() }));

		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(e.getMessage());
		}
	}

	

	private byte[] generatePDFReport(JasperPrint print) throws JRException {
		JRExporter exporter = new JRPdfExporter();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
		exporter.exportReport();
		return byteArrayOutputStream.toByteArray();
	}

	private byte[] generateDocReport(JasperPrint print) throws JRException {
		JRDocxExporter exporter = new JRDocxExporter();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
		exporter.exportReport();
		return byteArrayOutputStream.toByteArray();
	}

	private JasperPrint getJasperPrint(List reportList, String username, boolean paging) throws Exception {
		logger.info(messageResourceBundle.getLogMessage("creating.design.message"));

		JasperDesign design = JRXmlLoader.load(template_file);
		logger.info(messageResourceBundle.getLogMessage("compiling.source.format.message"));

		JasperReport report = JasperCompileManager.compileReport(design);
		logger.info(messageResourceBundle.getLogMessage("preparing.chart.data.message"));

		// ------------- Preparing databeancollection for chart ------------------
		Iterator itr = reportList.iterator();
		Map temp_chart = new HashMap();
		Map sender_chart = new HashMap();
		while (itr.hasNext()) {
			BatchDTO chartDTO = (BatchDTO) itr.next();
			// --------- pie Chart 1st ---------
			String type = chartDTO.getReqType();
			int counter = 0;
			if (temp_chart.containsKey(type)) {
				counter = (Integer) temp_chart.get(type);
			}
			temp_chart.put(type, ++counter);
			// --------- pie Chart 2nd ---------
			int sender_count = 0;
			if (sender_chart.containsKey(chartDTO.getSender())) {
				sender_count = (Integer) sender_chart.get(chartDTO.getSender());
			}
			sender_chart.put(chartDTO.getSender(), ++sender_count);
		}
		List<BatchDTO> chart_list = new ArrayList();
		if (!temp_chart.isEmpty()) {
			itr = temp_chart.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry entry = (Map.Entry) itr.next();
				BatchDTO chartDTO = new BatchDTO((String) entry.getKey(), (Integer) entry.getValue());
				chart_list.add(chartDTO);
			}
		}
		sender_chart = sortMapByDscValue(sender_chart, 5);
		List<BatchDTO> sender_list = new ArrayList();
		if (!sender_chart.isEmpty()) {
			itr = sender_chart.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry entry = (Map.Entry) itr.next();
				BatchDTO chartDTO = new BatchDTO();
				chartDTO.setSender((String) entry.getKey());
				chartDTO.setNumberCount((Integer) entry.getValue());
				sender_list.add(chartDTO);
			}
		}
		JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(chart_list);
		JRBeanCollectionDataSource piechart2DataSource = new JRBeanCollectionDataSource(sender_list);
		// -----------------------------------------------------------------------
		reportList = sortList(reportList);
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		Map parameters = new HashMap();
		if (reportList.size() > 20000) {
			JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(1000,
					new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 2048, 1024));
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		}
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		parameters.put("piechartDataSource", piechartDataSource);
		parameters.put("piechart2DataSource", piechart2DataSource);
		// -------- end -------------------------------------------
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		JasperPrint print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		return print;
	}

	private List sortList(List list) {
		Comparator<BatchDTO> comparator = null;
		comparator = Comparator.comparing(BatchDTO::getUsername).thenComparing(BatchDTO::getDate)
				.thenComparing(BatchDTO::getReqType);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		System.out.println("shortedlist"+sortedlist);
		return sortedlist;
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortMapByDscValue(Map<K, V> map, int limit) {
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();
		st.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(limit)
				.forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
		return result;
	}

	private List<BatchDTO> getSummaryReportList(SummaryReportForm customReportForm, String username,
			WebMasterEntry webMasterEntry) throws SQLException {
		System.out.println("call get summary report list 390 ");
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() ->new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
		}
		String to_gmt = null;
		String from_gmt = null;
		String sql = "select ";
		if (!webMasterEntry.getGmt().equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = webMasterEntry.getGmt().replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
			sql += "CONVERT_TZ(date,'" + from_gmt + "','" + to_gmt + "') as time,";
		} else {
			sql += "date as time,";
		}
		sql += "username,sender,msgcount,cost,content,numbercount,reqtype from summary_report";
		if (customReportForm.getCampaign() != null && customReportForm.getCampaign().length() > 1) {
			logger.info(user.getSystemId() + " Batch Report Requested Based On Campaign: "
					+ customReportForm.getCampaign());
			sql += " where campaign_name='" + customReportForm.getCampaign() + "'";
			if (user.getRole().equalsIgnoreCase("superadmin") || user.getRole().equalsIgnoreCase("system")) {
			} else {
				if (user.getRole().equalsIgnoreCase("admin") || user.getRole().equalsIgnoreCase("seller")
						|| user.getRole().equalsIgnoreCase("manager")) {
					List<String> users = null;
					if (user.getRole().equalsIgnoreCase("admin")) {
						users = new ArrayList<String>(userService.listUsersUnderMaster(user.getSystemId()).values());
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
					if (users != null && !users.isEmpty()) {
						sql += " and username in('" + String.join("','", users) + "')";
					} else {
						logger.info(messageResourceBundle.getLogMessage("no.user.found.message"), user.getSystemId(), user.getRole());

						return null;
					}
				} else {
					sql += " and username='" + user.getSystemId() + "'";
				}
			}
		} else {
			logger.info(messageResourceBundle.getLogMessage("batch.report.requested.message"), user.getSystemId());

			// boolean and = false;
			sql += " where campaign_type like '" + customReportForm.getCampaignType() + "'";
			if (user.getRole().equalsIgnoreCase("user")) {
				sql += " and username = '" + user.getSystemId() + "' ";
			} else {
				if (!customReportForm.getClientId().contains("ALL")) {
					sql += " and username = '" + customReportForm.getClientId() + "' ";
				} else {
					if (user.getRole().equalsIgnoreCase("admin") || user.getRole().equalsIgnoreCase("seller")
							|| user.getRole().equalsIgnoreCase("manager")) {
						List<String> users = null;
						if (user.getRole().equalsIgnoreCase("admin")) {
							users = new ArrayList<String>(
									userService.listUsersUnderMaster(user.getSystemId()).values());
							users.add(user.getSystemId());
						} else if (user.getRole().equalsIgnoreCase("seller")) {
							users = new ArrayList<String>(userService.listUsersUnderSeller(user.getId()).values());
						} else if (user.getRole().equalsIgnoreCase("manager")) {
							// SalesDAService salesService = new SalesDAServiceImpl();
							users = new ArrayList<String>(listUsernamesUnderManager(user.getSystemId()).values());

						}
						if (users != null && !users.isEmpty()) {
							sql += " and username in('" + String.join("','", users) + "')";
						} else {
							logger.info(messageResourceBundle.getLogMessage("no.user.found.message"), user.getSystemId(), user.getRole());
							return null;
						}
					}
				}
			}
			if (customReportForm.getSenderId() != null && customReportForm.getSenderId().length() > 0) {
				String sender = customReportForm.getSenderId();
				if (sender.contains("%")) {
					sql += " and sender like '" + sender + "' ";
				} else if (sender.contains(",")) {
					StringTokenizer strTokens = new StringTokenizer(sender, ",");
					String destlist = "";
					while (strTokens.hasMoreTokens()) {
						destlist += "'" + strTokens.nextToken() + "',";
					}
					destlist = destlist.substring(0, destlist.length() - 1);
					sql += " and sender in (" + destlist + ") ";
				} else {
					sql += " and sender = '" + sender + "' ";
				}
			}
			if (to_gmt != null) {
				sql += " and date between CONVERT_TZ('" + customReportForm.getStartDate() + " 00:00:00','" + to_gmt + "','"
						+ from_gmt + "') and CONVERT_TZ('" + customReportForm.getEndDate() + " 23:59:59','" + to_gmt
						+ "','" + from_gmt + "')";
			} else {
				if (customReportForm.getStartDate().equalsIgnoreCase(customReportForm.getEndDate())) {
					sql += " and DATE(date)= '" + customReportForm.getStartDate() + "'";
				} else {
					sql += " and DATE(date) between '" + customReportForm.getStartDate() + "' and '"
							+ customReportForm.getEndDate() + "'";
				}
			}
			System.out.println(sql);
		}
		System.out.println("SQL: " + sql);
		List<BatchDTO> reportList = getSummaryReport(sql);
		return reportList;
	}

	public List getSummaryReport(String sql) throws SQLException {
		List reportList = new ArrayList();
		Connection con = null;
		Statement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery(sql);
			BatchDTO report = null;
			String[] time;
			while (rs.next()) {
				time = rs.getString("time").split(" ");
				String content = uniHexToCharMsg(rs.getString("content"));
				double cost = 0;
				try {
					cost = Double.parseDouble(rs.getString("cost"));
				} catch (Exception ex) {
				}
				report = new BatchDTO(rs.getString("username"), rs.getString("sender"), time[0], time[1], cost, content,
						rs.getInt("msgcount"), rs.getInt("numbercount"), rs.getString("reqtype"));
				reportList.add(report);
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
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return reportList;
	}

	public String uniHexToCharMsg(String msg) {
		if (msg == null || msg.length() == 0) {
			msg = "0020";
		}
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		try {
			if (msg.substring(0, 2).compareTo("00") == 0) {
				reqNULL = true;
			}
			charsByt = new BigInteger(msg, 16).toByteArray();
			if (charsByt[0] == '\0') {
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
			msg = new String(var, "UTF-16");
		} catch (Exception ex) {
			logger.error(msg, ex);
		}
		return msg;
	}

	private Map<Integer, String> listUsernamesUnderManager(String systemId) {
		Map<Integer, String> map = listNamesUnderManager(systemId);
		Map<Integer, String> users = new HashMap<Integer, String>();
		for (Integer seller_id : map.keySet()) {
			users.putAll(userService.listUsersUnderSeller(seller_id));
		}
		return users;
	}

	public Map<Integer, String> listNamesUnderManager(String systemId) {
		Map<Integer, String> map = new HashMap<Integer, String>();
		List<SalesEntry> list = listSellersUnderManager(systemId);
		for (SalesEntry entry : list) {
			map.put(entry.getId(), entry.getUsername());
		}
		return map;
	}

	public List<SalesEntry> listSellersUnderManager(String systemId) {
		try {
			List<SalesEntry> salesList = salesRepository.findByMasterIdAndRole(systemId, "seller");

			return salesList;
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_GETTING_DELIVERY_REPORT_MESSAGE, new Object[] {systemId}));

		}
	}

	
}
