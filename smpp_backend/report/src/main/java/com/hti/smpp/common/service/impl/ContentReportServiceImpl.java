package com.hti.smpp.common.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.TreeMap;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.ContentReportRequest;
import com.hti.smpp.common.request.CustomReportDTO;
import com.hti.smpp.common.request.PaginationRequest;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.response.PaginatedResponse;
import com.hti.smpp.common.service.ContentReportService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;
import com.logica.smpp.Data;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@Service
public class ContentReportServiceImpl implements ContentReportService {

	@Autowired
	private DataBase dataBase;
	@Autowired
	private UserEntryRepository userRepository;
	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	Locale locale = null;
	private static final Logger logger = LoggerFactory.getLogger(ContentReportServiceImpl.class);
	private final String template_file = IConstants.FORMAT_DIR + "report//ContentReport.jrxml";
	@Autowired
	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public ResponseEntity<?> ContentReportView(String username, ContentReportRequest customReportForm) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional.orElseThrow(() -> new NotFoundException(
				messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username })));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION,
					new Object[] { username }));

		}

		try {

			List<DeliveryDTO> reportList = getContentReportList(customReportForm, username);
			System.out.println(reportList);
				if (reportList != null && !reportList.isEmpty()) {
					logger.info(user.getSystemId() + " ReportSize[View]:" + reportList.size());
					
					return ResponseEntity.ok(reportList);
					} else {
						throw new NotFoundException(
								messageResourceBundle.getExMessage(ConstantMessages.DATA_NOT_FOUND_CONTENT));
		
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

//			if (reportList != null && !reportList.isEmpty()) {
//				int totalPages = 0;
//				String tableName = "mis_" + username.toLowerCase();
//				long totalElements = 0l;
//				String countSql = "SELECT (" + "SELECT COUNT(*) FROM " + tableName + ") + ("
//						+ "SELECT COUNT(DISTINCT msg_id) " + "FROM (" + "SELECT msg_id FROM smsc_in WHERE username = ? "
//						+ "UNION " + "SELECT msg_id FROM unprocessed WHERE username = ?"
//						+ ") AS combined_unique_msg_ids" + ") AS total_count";
//				try (Connection connection = getConnection();
//						PreparedStatement pStmt = connection.prepareStatement(countSql)) {
//					pStmt.setString(1, username);
//					pStmt.setString(2, username);
//					try (ResultSet rs = pStmt.executeQuery()) {
//						if (rs.next()) {
//							totalElements = rs.getLong(1);
//							totalPages = (int) Math.ceil((double) totalElements / p.getPageSize());
//						}
//					}
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//				logger.info(messageResourceBundle.getLogMessage("report.size.view.message"), user.getSystemId(),
//						reportList.size());
//				logger.info(messageResourceBundle.getLogMessage("report.size.view.message"), user.getSystemId(),
//						reportList.size());
//				logger.info(messageResourceBundle.getLogMessage("report.view.size.message"), user.getSystemId(),
//						reportList.size());
//
//				// JasperPrint print = dataBase.getJasperPrint(reportList, false, username);
//				logger.info(messageResourceBundle.getLogMessage("report.finished.message"), user.getSystemId());
//				PaginatedResponse<DeliveryDTO> paginatedResponse = new PaginatedResponse<>(reportList,
//						p.getPageNumber(), p.getPageSize(), totalElements, totalPages-1);
//				return ResponseEntity.ok(paginatedResponse);
//			} else {
//				throw new NotFoundException(
//						messageResourceBundle.getExMessage(ConstantMessages.DATA_NOT_FOUND_CONTENT));
//
//			}
//		} catch (NotFoundException e) {
//			// Log NotFoundException
//			throw new NotFoundException(e.getMessage());
//		} catch (IllegalArgumentException e) {
//			logger.error(messageResourceBundle.getLogMessage("invalid.argument"), e.getMessage(), e);
//
//			throw new BadRequestException(messageResourceBundle
//					.getExMessage(ConstantMessages.BAD_REQUEST_EXCEPTION_MESSAGE, new Object[] { e.getMessage() }));
//
//		} catch (Exception e) {
//			// Log other exceptions
//			logger.error(messageResourceBundle.getLogMessage("unexpected.error"), e.getMessage(), e);
//			throw new InternalServerException(e.getMessage());
//		}
//	}





	private <K, V extends Comparable<? super V>> Map<K, V> sortMapByDscValue(Map<K, V> map, int limit) {
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();
		st.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(limit)
				.forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
		return result;
	}

	private List sortListBySender(List list) {
		Comparator<DeliveryDTO> comparator = Comparator.comparing(DeliveryDTO::getDate)
				.thenComparing(DeliveryDTO::getSender).thenComparing(DeliveryDTO::getMsgid);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	



	public List<DeliveryDTO> getContentReportList(ContentReportRequest customReportForm, String username) {
		if (customReportForm.getClientId() == null) {
			return null;
		}
		Optional<UserEntry> usersOptional = userRepository.findBySystemId(username);
		if (!usersOptional.isPresent()) {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

		UserEntry user = usersOptional.get();
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
		if (webMasterEntry == null) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.WEBMASTER_NOT_FOUND,
					new Object[] { user.getId() }));

		}

		logger.info(user.getSystemId() + " Creating Report list");
		CustomReportDTO customReportDTO = new CustomReportDTO();
		BeanUtils.copyProperties(customReportForm, customReportDTO);
		String startDate = customReportForm.getStartDate();
		String endDate = customReportForm.getEndDate();
		// IDatabaseService dbService = HtiSmsDB.getInstance();
		String report_user = customReportDTO.getClientId();
		String destination = customReportDTO.getDestinationNumber();// "9926870493";
																	// //customReportDTO.getDestinationNumber();
		String senderId = customReportDTO.getSenderId();// "%"; //customReportDTO.getSenderId();
		String country = customReportDTO.getCountry();
		String operator = customReportDTO.getOperator();
		String query = null;
		String unproc_query = null;
		String to_gmt = null, from_gmt = null;
		// logger.info(userSessionObject.getSystemId() + " Content Checked ------> " +
		// customReportForm.getCheck_F());
		// logger.info("USER_GMT: " + userSessionObject.getDefaultGmt() + " DEFAULT_GMT:
		// " + IConstants.DEFAULT_GMT);
		if (!webMasterEntry.getGmt().equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = webMasterEntry.getGmt().replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
		}
		logger.info(messageResourceBundle.getLogMessage("time.gmt.message"), to_gmt, from_gmt);

		// logger.info(userSessionObject.getSystemId() + " Content Report Based On
		// Criteria");
		if (to_gmt != null) {
			query = "SELECT CONVERT_TZ(A.submitted_time,'" + from_gmt + "','" + to_gmt + "') as submitted_time,";
			unproc_query = "select CONVERT_TZ(A.time,'" + from_gmt + "','" + to_gmt + "') as time,";
		} else {
			query = "select A.submitted_time as submitted_time,";
			unproc_query = "select A.time as time,";
		}
		if (to_gmt != null) {
			query += "CONVERT_TZ(A.deliver_time,'" + from_gmt + "','" + to_gmt + "') as deliver_time,";
		} else {
			query += "A.deliver_time as deliver_time,";
		}
		query += "A.msg_id,A.source_no,A.dest_no,A.status,A.cost,B.dcs,B.content,B.esm from mis_" + report_user
				+ " A,content_" + report_user + " B where A.msg_id = B.msg_id and ";
		unproc_query += "A.msg_id,A.source_no,A.destination_no,A.s_flag,A.cost,B.dcs,B.content,B.esm FROM table_name A,content_"
				+ report_user + " B WHERE A.msg_id = B.msg_id AND ";
		if (senderId != null && senderId.trim().length() > 0) {
			if (senderId.contains("%")) {
				query += "A.source_no like \"" + senderId + "\" AND ";
				unproc_query += "A.source_no like \"" + senderId + "\" AND ";
			} else {
				query += "A.source_no =\"" + senderId + "\" AND ";
				unproc_query += "A.source_no =\"" + senderId + "\" AND ";
			}
		}
		if (destination != null && destination.trim().length() > 0) {
			if (destination.contains("%")) {
				query += "A.dest_no like '" + destination + "' AND ";
				unproc_query += "A.destination_no like '" + destination + "' AND ";
			} else {
				query += "A.dest_no ='" + destination + "' AND ";
				unproc_query += "A.destination_no ='" + destination + "' AND ";
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
					query += "A.oprCountry in (" + oprCountry + ") AND ";
					unproc_query += "A.oprCountry in (" + oprCountry + ") AND ";
				}
			}
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
				query += "A.msg_id between " + start_msg_id + " and " + end_msg_id;
				unproc_query += "A.msg_id BETWEEN " + start_msg_id + " and " + end_msg_id;
			} catch (Exception e) {
				query += "A.submitted_time BETWEEN CONVERT_TZ('" + startDate + "','" + to_gmt + "','" + from_gmt
						+ "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt + "')";
				unproc_query += "A.time BETWEEN CONVERT_TZ('" + startDate + "','" + to_gmt + "','" + from_gmt
						+ "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt + "')";
			}
		} else {
			if (startDate.equalsIgnoreCase(endDate)) {
				String start_msg_id = startDate.substring(2);
				start_msg_id = start_msg_id.replaceAll("-", "");
				start_msg_id = start_msg_id.replaceAll(":", "");
				start_msg_id = start_msg_id.replaceAll(" ", "");
				query += "A.msg_id like '" + start_msg_id + "%'";
				unproc_query += "A.msg_id like '" + start_msg_id + "%'";
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
				query += "A.msg_id BETWEEN " + start_msg_id + " AND " + end_msg_id
						+ " order by A.msg_id,A.dest_no,A.source_no;";
				unproc_query += "A.msg_id between " + start_msg_id + " AND " + end_msg_id
						+ " order by A.msg_id,A.destination_no,A.source_no;";
			}
		}

//		int remainingSize = pageable.getPageSize();
//		int currentOffset = pageable.getPageNumber() * pageable.getPageSize();
//		List<DeliveryDTO> combinedList = new ArrayList<>();
//		List<DeliveryDTO> list = contentWiseDlrReport(query + " LIMIT " + remainingSize + " OFFSET " + currentOffset,
//				report_user, webMasterEntry.isHideNum());
//		combinedList.addAll(list);
//		remainingSize -= list.size();
//		currentOffset = Math.max(0, currentOffset - list.size());
//		System.out.println("currentOffset" + currentOffset);
//		logger.info(messageResourceBundle.getLogMessage("report.sql.message"), user.getSystemId(), query);
//
//		if (remainingSize > 0) {
//			List<DeliveryDTO> unproc_list_1 = contentWiseUprocessedReport(
//					unproc_query.replaceAll("table_name", "smsc_in") + " LIMIT " + remainingSize + " OFFSET "
//							+ currentOffset,
//					report_user, webMasterEntry.isHideNum());
//			combinedList.addAll(unproc_list_1);
//			remainingSize -= unproc_list_1.size();
//			currentOffset = Math.max(0, currentOffset - unproc_list_1.size());
//		}
//		logger.info(messageResourceBundle.getLogMessage("report.sql.message"), user.getSystemId(), query);
//
//		if (remainingSize > 0) {
//			List<DeliveryDTO> unproc_list_2 = contentWiseUprocessedReport(
//					unproc_query.replaceAll("table_name", "unprocessed") + " LIMIT " + remainingSize + " OFFSET "
//							+ currentOffset,
//					report_user, webMasterEntry.isHideNum());
//			combinedList.addAll(unproc_list_2);
//		}
//		logger.info(messageResourceBundle.getLogMessage("end.criteria.report.message"), user.getSystemId(),
//				combinedList.size());
//		return combinedList;
		//----------------
//		logger.info(user.getSystemId() + " ReportSQL:" + query);
//		System.out.println("this is line run 455");
//		List<DeliveryDTO> list = contentWiseDlrReport(query, report_user, webMasterEntry.isHideNum());
//		logger.info(messageResourceBundle.getLogMessage("report.sql.message"), user.getSystemId(), query);
//
//		List<DeliveryDTO> unproc_list_1 = contentWiseUprocessedReport(unproc_query.replaceAll("table_name", "smsc_in"),
//				report_user, webMasterEntry.isHideNum());
//		list.addAll(unproc_list_1);
//		logger.info(messageResourceBundle.getLogMessage("report.sql.message"), user.getSystemId(), query);
//
//		List<DeliveryDTO> unproc_list_2 = contentWiseUprocessedReport(
//				unproc_query.replaceAll("table_name", "unprocessed"), report_user, webMasterEntry.isHideNum());
//		list.addAll(unproc_list_2);
//		logger.info(messageResourceBundle.getLogMessage("end.criteria.report.message"), user.getSystemId(),
//				list.size());
//
//		return list;
		logger.info(user.getSystemId() + " ReportSQL:" + query);
		List<DeliveryDTO> list = contentWiseDlrReport(query, report_user,
				webMasterEntry.isHideNum());
		logger.info(messageResourceBundle.getLogMessage("report.sql.message"), user.getSystemId(), query);
		List<DeliveryDTO> unproc_list_1 = contentWiseUprocessedReport(
				unproc_query.replaceAll("table_name", "smsc_in"), report_user,
				 webMasterEntry.isHideNum());
		list.addAll(unproc_list_1);
		logger.info(messageResourceBundle.getLogMessage("report.sql.message"), user.getSystemId(), query);
		List<DeliveryDTO> unproc_list_2 = contentWiseUprocessedReport(
				unproc_query.replaceAll("table_name", "unprocessed"), report_user,
				 webMasterEntry.isHideNum());
		list.addAll(unproc_list_2);
		logger.info(messageResourceBundle.getLogMessage("end.criteria.report.message"), user.getSystemId(),
			list.size());
		return list;
	}

	@Transactional
	public List<DeliveryDTO> contentWiseDlrReport(String sql, String report_user, boolean hidenumber) {
		System.out.println(sql);
		List<DeliveryDTO> list = new ArrayList<DeliveryDTO>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		Map<String, Map<Integer, String>> content_map = new HashMap<String, Map<Integer, String>>();
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				String msg_id = rs.getString("A.msg_id");
				int esm = rs.getInt("B.esm");
				int dcs = rs.getInt("B.dcs");
				String content = rs.getString("B.content").trim();
				String destination = rs.getString("A.dest_no");
				String submit_time = rs.getString("submitted_time");
				String deliver_time = rs.getString("deliver_time");
				String date = submit_time.substring(0, 10);
				String time = submit_time.substring(10, submit_time.length());
				String status = rs.getString("A.status");
				String sender = rs.getString("A.source_no");
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
								try {
									total_parts = Integer.parseInt(content.substring(8, 10));
								} catch (Exception ex) {
									total_parts = Integer.parseInt(content.substring(8, 10), 16);
								}
								try {
									part_number = Integer.parseInt(content.substring(10, 12));
								} catch (Exception ex) {
									part_number = Integer.parseInt(content.substring(10, 12), 16);
								}
								content = content.substring(12, content.length());
							} else if (header_length == 6) {
								reference_number = content.substring(8, 10);
								try {
									total_parts = Integer.parseInt(content.substring(10, 12));
								} catch (Exception e) {
									total_parts = Integer.parseInt(content.substring(10, 12), 16);
								}
								try {
									part_number = Integer.parseInt(content.substring(12, 14));
								} catch (Exception e) {
									part_number = Integer.parseInt(content.substring(12, 14), 16);
								}
								content = content.substring(14, content.length());
							}
						} else {
							if (header_length == 5) {
								reference_number = content.substring(12, 16);
								try {
									total_parts = Integer.parseInt(content.substring(18, 20));
								} catch (Exception e) {
									total_parts = Integer.parseInt(content.substring(18, 20), 16);
								}
								try {
									part_number = Integer.parseInt(content.substring(22, 24));
								} catch (Exception e) {
									part_number = Integer.parseInt(content.substring(22, 24), 16);
								}
								content = content.substring(24, content.length());
							} else if (header_length == 6) {
								reference_number = content.substring(16, 20);
								try {
									total_parts = Integer.parseInt(content.substring(22, 24));
								} catch (Exception e) {
									total_parts = Integer.parseInt(content.substring(22, 24), 16);
								}
								try {
									part_number = Integer.parseInt(content.substring(26, 28));
								} catch (Exception e) {
									part_number = Integer.parseInt(content.substring(26, 28), 16);
								}
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
							reportDTO.setStatus(status);
							reportDTO.setSender(sender);
							if (hidenumber) {
								String dest_sub = destination.substring(0, destination.length() - 2);
								dest_sub += "**";
								reportDTO.setDestination(dest_sub);
							} else {
								reportDTO.setDestination(destination);
							}
							reportDTO.setDate(date);
							reportDTO.setTime(time);
							reportDTO.setMsgType(msg_type);
							reportDTO.setUsername(report_user);
							reportDTO.setDeliverOn(deliver_time);
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
						logger.error(messageResourceBundle.getLogMessage("error.message"), msg_id, content, une);

					}
				} else {
					DeliveryDTO reportDTO = new DeliveryDTO();
					reportDTO.setStatus(status);
					reportDTO.setSender(sender);
					if (hidenumber) {
						String dest_sub = destination.substring(0, destination.length() - 2);
						dest_sub += "**";
						reportDTO.setDestination(dest_sub);
					} else {
						reportDTO.setDestination(destination);
					}
					reportDTO.setDate(date);
					reportDTO.setTime(time);
					reportDTO.setMsgType(msg_type);
					reportDTO.setUsername(report_user);
					reportDTO.setDeliverOn(deliver_time);
					reportDTO.setMsgParts(1);
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

					logger.info("SQL: " + sql);
				}
			}
		} catch (Exception sqle) {
			logger.error(report_user, sqle);
			throw new InternalServerException(messageResourceBundle.getExMessage(
					ConstantMessages.CONTENT_WISE_PROCESS_DATA_ERROR, new Object[] { sqle.getMessage() }));

		}
		finally {
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

	public List<DeliveryDTO> contentWiseUprocessedReport(String sql, String report_user, boolean hidenumber) {
		logger.info(messageResourceBundle.getLogMessage("sql.message"), report_user, sql);

		List<DeliveryDTO> list = new ArrayList<DeliveryDTO>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		Map<String, Map<Integer, String>> content_map = new HashMap<String, Map<Integer, String>>();
		try {
			// Connection connection = entityManager.unwrap(Connection.class);

			con = getConnection();
			pStmt = con.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				String msg_id = rs.getString("A.msg_id");
				int esm = rs.getInt("B.esm");
				int dcs = rs.getInt("B.dcs");
				String content = rs.getString("B.content").trim();
				String destination = rs.getString("A.destination_no");

				String submit_time = rs.getString("time");
				System.out.println("--------------" + submit_time);
				String date = submit_time.substring(0, 10);

				String time = submit_time.substring(10, submit_time.length());
				String status = rs.getString("A.s_flag");
				String sender = rs.getString("A.source_no");
				double cost = rs.getDouble("A.cost");
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
								try {
									total_parts = Integer.parseInt(content.substring(8, 10));
								} catch (Exception e) {
									total_parts = Integer.parseInt(content.substring(8, 10), 16);
								}
								try {
									part_number = Integer.parseInt(content.substring(10, 12));
								} catch (Exception e) {
									part_number = Integer.parseInt(content.substring(10, 12), 16);
								}
								content = content.substring(12, content.length());
							} else if (header_length == 6) {
								reference_number = content.substring(8, 10);
								try {
									total_parts = Integer.parseInt(content.substring(10, 12));
								} catch (Exception e) {
									total_parts = Integer.parseInt(content.substring(10, 12), 16);
								}
								try {
									part_number = Integer.parseInt(content.substring(12, 14));
								} catch (Exception e) {
									part_number = Integer.parseInt(content.substring(12, 14), 16);
								}
								content = content.substring(14, content.length());
							}
						} else {
							if (header_length == 5) {
								reference_number = content.substring(12, 16);
								try {
									total_parts = Integer.parseInt(content.substring(16, 20));
								} catch (Exception e) {
									total_parts = Integer.parseInt(content.substring(18, 20), 16);
								}
								try {
									part_number = Integer.parseInt(content.substring(20, 24));
								} catch (Exception e) {
									part_number = Integer.parseInt(content.substring(22, 24), 16);
								}
								content = content.substring(24, content.length());
							} else if (header_length == 6) {
								reference_number = content.substring(16, 20);
								try {
									total_parts = Integer.parseInt(content.substring(20, 24));
								} catch (Exception e) {
									total_parts = Integer.parseInt(content.substring(22, 24), 16);
								}
								try {
									part_number = Integer.parseInt(content.substring(24, 28));
								} catch (Exception e) {
									part_number = Integer.parseInt(content.substring(26, 28), 16);
								}
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
							reportDTO.setStatus(status);
							reportDTO.setSender(sender);
							if (hidenumber) {
								String dest_sub = destination.substring(0, destination.length() - 2);
								dest_sub += "**";
								reportDTO.setDestination(dest_sub);
							} else {
								reportDTO.setDestination(destination);
							}
							reportDTO.setDate(date);
							reportDTO.setTime(time);
							reportDTO.setDeliverOn("-");
							reportDTO.setMsgType(msg_type);
							reportDTO.setUsername(report_user);
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
						logger.error(messageResourceBundle.getLogMessage("error.message"), msg_id, content, une);

					}
				} else {
					DeliveryDTO reportDTO = new DeliveryDTO();
					reportDTO.setStatus(status);
					reportDTO.setSender(sender);
					if (hidenumber) {
						String dest_sub = destination.substring(0, destination.length() - 2);
						dest_sub += "**";
						reportDTO.setDestination(dest_sub);
					} else {
						reportDTO.setDestination(destination);
					}
					reportDTO.setDate(date);
					reportDTO.setTime(time);
					reportDTO.setDeliverOn("-");
					reportDTO.setMsgType(msg_type);
					reportDTO.setUsername(report_user);
					reportDTO.setMsgParts(1);
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
			logger.error(messageResourceBundle.getLogMessage("sql.error.message"), report_user, sqle);

			throw new InternalServerException(messageResourceBundle.getExMessage(
					ConstantMessages.CONTENT_WISE_PROCESS_DATA_ERROR, new Object[] { sqle.getMessage() }));
		}
	//	logger.info(messageResourceBundle.getLogMessage("report.list.size.message"), report_user, list.size());
		finally {
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
}