package com.hti.smpp.common.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.messages.dto.BulkMapEntry;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.CustomReportDTO;
import com.hti.smpp.common.request.CustomizedReportRequest;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.BulkDAService;
import com.hti.smpp.common.service.CustomizedReportService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Converter;
import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.dto.SevenBitChar;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;

@Service
public class CustomizedReportServicesImpl implements CustomizedReportService {

	@Autowired
	private DataBase dataBase;
	@Autowired
	private UserEntryRepository userRepository;
	@Autowired
	private SalesRepository salesRepository;
	@Autowired
	private UserDAService userService;
	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;

	@Autowired
	private BulkDAService bulkService;

	boolean isSummary;
	private static final Logger logger = LoggerFactory.getLogger(CustomizedReportServicesImpl.class);
	String reportUser = null;
	Locale locale = null;
	String groupby = "country";
	final String template_file = IConstants.FORMAT_DIR + "report//dlrReport.jrxml";
	final String template_sender_file = IConstants.FORMAT_DIR + "report//dlrReportSender.jrxml";
	final String template_content_file = IConstants.FORMAT_DIR + "report//dlrContentReport.jrxml";
	final String template_content_sender_file = IConstants.FORMAT_DIR + "report//dlrContentWithSender.jrxml";
	final String summary_template_file = IConstants.FORMAT_DIR + "report//dlrSummaryReport.jrxml";
	final String summary_sender_file = IConstants.FORMAT_DIR + "report//dlrSummarySender.jrxml";
	boolean isContent;
	String to_gmt;
	String from_gmt;

	@Autowired
	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public ResponseEntity<?> CustomizedReportView(String username, CustomizedReportRequest customReportForm) {
		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			UserEntry user = userOptional
					.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}


			List<DeliveryDTO> reportList = getCustomizedReportList(customReportForm, username);
			if (customReportForm.getReportType().equalsIgnoreCase("Summary")) {
				isSummary = true;
			} else {
				isSummary = false;
			}
			System.out.println(isSummary);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[View]:" + reportList.size());
				List<DeliveryDTO> print = isSummary ? getSummaryJasperPrint(reportList, username)
						: getCustomizedJasperPrint(reportList, false, username);
				logger.info(user.getSystemId() + " <-- Report Finished --> ");
				return new ResponseEntity<>(reportList, HttpStatus.OK);

			} else {
				throw new Exception("No data found for the CustomizedReport report");
			}
		} catch (UnauthorizedException e) {
			// Handle unauthorized exception
			e.printStackTrace();
			throw new InternalServerException("Error processing in the CustomizedReport report");
		} catch (Exception e) {
			// Handle other general exceptions
			e.printStackTrace();
			throw new InternalServerException("Error processing in the CustomizedReport report");
		}
	}

	public List<DeliveryDTO> getSummaryJasperPrint(List reportList, String username){

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		String groupby = "country";
		String reportUser = null;
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
			}
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
		return reportList;
	}

	private List<DeliveryDTO> getCustomizedJasperPrint(List<DeliveryDTO> reportList, boolean b, String username) {

		final String template_file = IConstants.FORMAT_DIR + "report//dlrReport.jrxml";
		String template_sender_file = IConstants.FORMAT_DIR + "report//dlrReportSender.jrxml";
		String template_content_file = IConstants.FORMAT_DIR + "report//dlrContentReport.jrxml";
		String template_content_sender_file = IConstants.FORMAT_DIR + "report//dlrContentWithSender.jrxml";
		String summary_template_file = IConstants.FORMAT_DIR + "report//dlrSummaryReport.jrxml";
		String summary_sender_file = IConstants.FORMAT_DIR + "report//dlrSummarySender.jrxml";
		
		List<DeliveryDTO> print = null;
		List<DeliveryDTO> report = null;
		List<DeliveryDTO> design = null;
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
//		if (groupby.equalsIgnoreCase("country")) {
//
//			if (isContent) {
//				design = JRXmlLoader.load(template_content_file);
//			} else {
//				design = JRXmlLoader.load(template_file);
//			}
//		} else {
//			if (isContent) {
//				design = JRXmlLoader.load(template_content_sender_file);
//			} else {
//				design = JRXmlLoader.load(template_sender_file);
//			}
//		}
//		report = JasperCompileManager.compileReport(design);
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
		
//		JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(chart_list);
//		JRBeanCollectionDataSource barchart1DataSource = new JRBeanCollectionDataSource(bar_chart_list);
//		parameters.put("piechartDataSource", piechartDataSource);
//		parameters.put("barchart1DataSource", barchart1DataSource);
//		logger.info(user.getSystemId() + " <-- Finished Charts --> ");
//		// -----------------------------------------------------------------------
//		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
//		if (reportList.size() > 20000) {
//			logger.info(user.getSystemId() + " <-- Creating Virtualizer --> ");
//			JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(100,
//					new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 1024, 512));
//			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);FuserEnrty
		
		
//		}
//		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
//		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
//		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
//		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(userOptional.get().getId());
//
//		logger.info(user.getSystemId() + " DisplayCost: " + webMasterEntry.isDisplayCost());
//		parameters.put("displayCost", webMasterEntry.isDisplayCost());
//		logger.info(user.getSystemId() + " <-- Filling Report Data --> ");
//		print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
//		logger.info(user.getSystemId() + " <-- Filling Completed --> ");
		return report;

	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortMapByDscValue(Map<K, V> map, int limit) {
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();
		st.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(limit)
				.forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
		return result;
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

	private List<DeliveryDTO> getCustomizedReportList(CustomizedReportRequest customReportForm, String username) {
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
		String startDate = customReportForm.getStartDate();
		String endDate = customReportForm.getEndDate();
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
		}
		logger.info(user.getSystemId() + " " + customReportDTO + ",isContent=" + isContent + ",GroupBy=" + groupby
				+ ",Status=" + status);
		if (criteria_type.equalsIgnoreCase("messageid")) {
			logger.info(user.getSystemId() + " Report Based On MessageId: " + messageId);
			if (messageId != null && messageId.trim().length() > 0) {
				isSummary = false;
				String userQuery = "select username from mis_table where msg_id ='" + messageId + "'";
				List userSet = getDistinctMisUser(userQuery);
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
						Map map = getDlrReport(reportUser, query, webMasterEntry.isHideNum());
						if (!map.isEmpty()) {
							list = getMessageContent(map, reportUser);
						}
					} else {
						list = (List) getCustomizedReport(reportUser, query, webMasterEntry.isHideNum());
					}
				}
				final_list.addAll(list);
				logger.info(user.getSystemId() + " End Based On MessageId. Final Report Size: " + final_list.size());
			} else {
				logger.info(user.getSystemId() + " Invalid MessageId");
				return null;
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
								list = getMessageContent(map, reportUser);
							}
						} else {
							list = (List) getCustomizedReport(reportUser, query, webMasterEntry.isHideNum());
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
					cross_unprocessed_query += ",smsc";
					cross_unprocessed_query += " from table_name where msg_id in(" + String.join(",", unprocessed_set)
							+ ")";
					System.out.println("unprcced" + cross_unprocessed_query);
					List unproc_list = getUnprocessedReport(
							cross_unprocessed_query.replaceAll("table_name", "unprocessed"), webMasterEntry.isHideNum(),
							isContent);
					if (unproc_list != null && !unproc_list.isEmpty()) {
						final_list.addAll(unproc_list);
					}
					System.out.println("smsc_in" + unproc_list);
					unproc_list = (List) getUnprocessedReport(
							cross_unprocessed_query.replaceAll("table_name", "smsc_in"), webMasterEntry.isHideNum(),
							isContent);
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
						list = (List) getDlrSummaryReport(report_user, query, groupby);
						logger.info(user.getSystemId() + " list:" + list.size());
					} else {
						// query += " order by submitted_time DESC,oprCountry ASC,msg_id DESC";
						logger.debug(user.getSystemId() + " ReportSQL:" + query);
						if (isContent) {
							Map map = getDlrReport(report_user, query, webMasterEntry.isHideNum());
							if (!map.isEmpty()) {
								list = getMessageContent(map, report_user);
							}
						} else {
							list = (List) getCustomizedReport(report_user, query, webMasterEntry.isHideNum());
						}
					}
					if (list != null && !list.isEmpty()) {
						System.out.println(report_user + " Report List Size --> " + list.size());
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
					System.out.println("cross_unprocessed_query " + cross_unprocessed_query);
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
						List unproc_list = (List) getUnprocessedSummary(
								cross_unprocessed_query.replaceAll("table_name", "unprocessed"), groupby);
						System.out.println("unprocessed" + unproc_list);
						if (unproc_list != null && !unproc_list.isEmpty()) {
							final_list.addAll(unproc_list);
						}
						unproc_list = (List) getUnprocessedSummary(
								cross_unprocessed_query.replaceAll("table_name", "smsc_in"), groupby);
						if (unproc_list != null && !unproc_list.isEmpty()) {
							final_list.addAll(unproc_list);
						}
					} else {
						List unproc_list = (List) getUnprocessedReport(
								cross_unprocessed_query.replaceAll("table_name", "unprocessed"),
								webMasterEntry.isHideNum(), isContent);
						if (unproc_list != null && !unproc_list.isEmpty()) {
							final_list.addAll(unproc_list);
						}
						unproc_list = (List) getUnprocessedReport(
								cross_unprocessed_query.replaceAll("table_name", "smsc_in"), webMasterEntry.isHideNum(),
								isContent);
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

	public List getUnprocessedSummary(String query, String groupby) {
		System.out.println("query4"+query);
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

	List getUnprocessedReport(String query, boolean hide_number, boolean isContent) {
		System.out.println("query3"+query);
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
			con = getConnection();
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
			con = getConnection();

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

	public List getCustomizedReport(String username, String query, boolean hideNum) {
		System.out.println("query1"+query);
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

	private List getDlrSummaryReport(String report_user, String query, String groupby) {
		List customReport = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		DeliveryDTO report = null;
		try {
			con = getConnection();

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

			con = getConnection();
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
					// dbCon.releaseConnection(con);
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

	private Map getDlrReport(String reportUser, String query, boolean hideNum) {
		System.out.println("query2"+query);
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
					;
				}
			} catch (SQLException sqle) {
			}
		}
		return customReport;

	}

	public List<String> getDistinctMisUser(String userQuery) {
		List<String> userSet = new ArrayList<>();
		Connection con = null;
		Statement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection(); // Ensure this method exists and returns a valid connection
			pStmt = con.createStatement();
			rs = pStmt.executeQuery(userQuery);
			while (rs.next()) {
				userSet.add(rs.getString("username"));
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace(); // Log or handle the SQLException
		} catch (Exception e) {
			e.printStackTrace(); // Log or handle other exceptions
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace(); // Log or handle the closing exception
				}
			}
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace(); // Log or handle the closing exception
				}
			}
			if (con != null) {
				try {
					// Assuming con.close connection is your method to close/release the connection
					con.close();
				} catch (Exception e) {
					e.printStackTrace(); // Log or handle the closing/release exception
				}
			}
		}
		return userSet;
	}

	@Override
	public String CustomizedReportxls(String username, CustomizedReportRequest customReportForm,
			HttpServletResponse response) {
		String target = IConstants.SUCCESS_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		try {
			List<DeliveryDTO> reportList = dataBase.getCustomizedReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				int total_rec = reportList.size();
				logger.info(user.getSystemId() + " ReportSize[xls]:" + total_rec);
				// ---------- Sorting list ----------------------------
				if (groupby.equalsIgnoreCase("country")) {
					reportList = dataBase.sortListByCountry(reportList);
				} else {
					reportList = dataBase.sortListBySender(reportList);
				}
				Workbook workbook = null;
				if (isSummary) {
					if (groupby.equalsIgnoreCase("country")) {
						workbook = dataBase.getCustomizedSummaryWorkBook(reportList, username);
					} else {
						Map<String, DeliveryDTO> map = new LinkedHashMap<String, DeliveryDTO>();
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
							if (reportDTO.getStatus().startsWith("DELIV")) {
								tempDTO.setDelivered(tempDTO.getDelivered() + reportDTO.getStatusCount());
							}
							tempDTO.setSubmitted(tempDTO.getSubmitted() + reportDTO.getStatusCount());
							System.out.println(key + " :-> " + " Submit: " + tempDTO.getSubmitted() + " Delivered: "
									+ tempDTO.getDelivered());
							map.put(key, tempDTO);
						}
						reportList.clear();
						reportList.addAll(map.values());
						workbook = dataBase.getCustomizedSummaryWorkBook(reportList, username);
					}
				} else {
					workbook = dataBase.getCustomizedWorkBook(reportList, username);
				}
				if (total_rec > 100000) {
					logger.info(user.getSystemId() + "<-- Creating Zip Folder --> ");
					response.setContentType("application/zip");
					response.setHeader("Content-Disposition", "attachment; filename=" + "delivery_"
							+ new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".zip");
					ZipOutputStream zos = new ZipOutputStream(response.getOutputStream()); // create a ZipOutputStream
																							// from servletOutputStream
					String reportName = "delivery.xlsx";
					ZipEntry entry = new ZipEntry(reportName); // create a zip entry and add it to ZipOutputStream
					zos.putNextEntry(entry);
					logger.info(user.getSystemId() + "<-- Starting Zip Download --> ");
					workbook.write(zos);
					zos.close();
				} else {
					logger.info(user.getSystemId() + " <---- Creating XLS -----> ");
					String filename = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
							+ ".xlsx";
					// response.setContentType("text/html; charset=utf-8");
					response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
					// response.setHeader("Content-Disposition", "attachment; filename=" +
					// filename);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					workbook.write(bos);
					logger.info(user.getSystemId() + " <---- Reading XLS -----> ");
					ByteArrayInputStream is = null;
					ServletOutputStream out = null;
					try {
						is = new ByteArrayInputStream(bos.toByteArray());
						// byte[] buffer = new byte[8789];
						int curByte = -1;
						out = response.getOutputStream();
						logger.info(user.getSystemId() + " <---- Starting xls Download -----> ");
						while ((curByte = is.read()) != -1) {
							out.write(curByte);
						}
						out.flush();
					} catch (Exception ex) {
						logger.error(user.getSystemId() + " DLR XLSReport Error ", ex.fillInStackTrace());
						// ex.printStackTrace();
					} finally {
						try {
							if (is != null) {
								is.close();
							}
							if (out != null) {
								out.close();
							}
						} catch (Exception ex) {
						}
					}
				}
				workbook.close();
				reportList.clear();
				logger.info(user.getSystemId() + "<--XLS Report Finished --> ");
			} else {
				logger.info(user.getSystemId() + "<-- No Records Found --> ");
			}
		} catch (Exception e) {
			logger.error(user.getSystemId(), e.fillInStackTrace());
		}
		return target;
	}

	@Override
	public ResponseEntity<?> CustomizedReportpdf(String username, CustomizedReportRequest customReportForm,
			HttpServletResponse response) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		try {
		
			List<DeliveryDTO> reportList = dataBase.getCustomizedReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(username + " ReportSize[pdf]:" + reportList.size());
				JasperPrint print = null;
				if (customReportForm.getReportType().equalsIgnoreCase("Summary")) {
					isSummary = true;
				} else {
					isSummary = false;
				}
				if (isSummary) {
					print = dataBase.getSummaryJasperPrint(reportList, false, username);
				} else {
					// Uncomment this line if you have a method for customized JasperPrint
					print = dataBase.getCustomizedJasperPrint(reportList, false, username);
				}

				if (print != null) {

					logger.info(username + " <-- Preparing Outputstream --> ");
					String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
							+ ".pdf";

					byte[] pdfReport = JasperExportManager.exportReportToPdf(print);

					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_PDF);
					headers.setContentDispositionFormData("attachment", reportName);

					// Return the file in the ResponseEntity
					return new ResponseEntity<>(pdfReport, headers, HttpStatus.OK);
				} else {
					throw new InternalServerException("Failed to generate JasperPrint");
				}
			} else {
				throw new InternalServerException("No data found for the report");
			}
		} catch (Exception e) {
			logger.error(username, e.fillInStackTrace());
			// Handle exceptions and return an appropriate response
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating the PDF report");
		}
	}

	@Override
	public ResponseEntity<?> CustomizedReportdoc(String username, CustomizedReportRequest customReportForm,
			HttpServletResponse response) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		String target = IConstants.SUCCESS_KEY;
		try {
			
			List<DeliveryDTO> reportList = dataBase.getCustomizedReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[doc]:" + reportList.size());
				JasperPrint print = null;
				if (customReportForm.getReportType().equalsIgnoreCase("Summary")) {
					isSummary = true;
				} else {
					isSummary = false;
				}
				if (isSummary) {
					print = dataBase.getSummaryJasperPrint(reportList, false, username);
				} else {
					// Uncomment this line if you have a method for customized JasperPrint
					print = dataBase.getCustomizedJasperPrint(reportList, false, username);
				}

				logger.info(user.getSystemId() + " <-- Preparing Outputstream --> ");
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".doc";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info(user.getSystemId() + " <-- Creating DOC --> ");

				// String reportName = "delivery_" + new
				// SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".docx";
				response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");

				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				JRExporterParameter exporterParameter = JRExporterParameter.JASPER_PRINT;
				JRDocxExporter exporter = new JRDocxExporter();
				exporter.setParameter(exporterParameter, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
				exporter.exportReport();

				return ResponseEntity.ok()
						.header("Content-Type",
								"application/vnd.openxmlformats-officedocument.wordprocessingml.document")
						.body(byteArrayOutputStream.toByteArray());
			} else {
				throw new InternalServerException("Failed to generate JasperPrint");
			}

		} catch (Exception e) {
			logger.error(username, e.fillInStackTrace());
			// Handle exceptions and return an appropriate response
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating the doc report");
		}

	}
}
