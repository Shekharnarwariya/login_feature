package com.hti.smpp.common.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.service.ContentReportService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.logica.smpp.Data;

import jakarta.persistence.EntityManager;
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
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@Service
public class ContentReportServiceImpl implements ContentReportService {

	@Autowired
	private DataBase dataBase;
	@Autowired
	private EntityManager entityManager;

	@Autowired
	private UserEntryRepository userRepository;
	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;

	Locale locale = null;
	private static final Logger logger = LoggerFactory.getLogger(ContentReportServiceImpl.class);
	private final String template_file = IConstants.FORMAT_DIR + "report//ContentReport.jrxml";
	@Autowired
	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public ResponseEntity<?> ContentReportView(String username, ContentReportRequest customReportForm, String lang) {
		String target = IConstants.FAILURE_KEY;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		try {
			locale = Customlocale.getLocaleByLanguage(lang);

			List<DeliveryDTO> reportList = getContentReportList(customReportForm, username, lang);
			System.out.println(reportList);

			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[View]:" + reportList.size());
				// JasperPrint print = dataBase.getJasperPrint(reportList, false, username);
				logger.info(user.getSystemId() + " <-- Report Finished --> ");
				target = IConstants.SUCCESS_KEY;

				// Return ResponseEntity with the list in the response body
				return new ResponseEntity<>(reportList, HttpStatus.OK);
			} else {
				throw new InternalServerException("No data found for the dlr Content report");
			}
		}
			catch (NotFoundException e) {
		        // Log NotFoundException
		        logger.error("SMS Latency report not found for username: {}", username, e);
		        throw new NotFoundException(e.getMessage());
		        } catch (IllegalArgumentException e) {
		        // Log IllegalArgumentException
		        logger.error("Invalid argument: {}", e.getMessage(), e);
		        throw new BadRequestException("Invalid argument: " + e.getMessage());
		        } catch (Exception e) {
		        // Log other exceptions
		        logger.error("Unexpected error occurred: {}", e.getMessage(), e);
		        throw new InternalServerException("Error: getting error in Latency report with username: " + username);
		        }
		        }

	@Override
	public ResponseEntity<?> ContentReportxls(String username, ContentReportRequest customReportForm,
			HttpServletResponse response, String lang) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		try {
			locale = Customlocale.getLocaleByLanguage(lang);

			List<DeliveryDTO> reportList = getContentReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[doc]:" + reportList.size());

				JasperPrint print = getJasperPrint(reportList, false, username);
				logger.info(user.getSystemId() + " <-- Preparing Outputstream --> ");

				byte[] xlsBytes;
				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					JRExporter exporter = new JRXlsExporter(); // or JRXlsxExporter for Excel 2007 and above
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
					exporter.exportReport();
					xlsBytes = out.toByteArray();
				}

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".xls";
				headers.setContentDispositionFormData(reportName, reportName);

				logger.info(user.getSystemId() + " <-- xls Report Finished --> ");
				target = IConstants.SUCCESS_KEY;

				return new ResponseEntity<>(xlsBytes, headers, HttpStatus.OK);
			} else {
// Handle case where no data is found for the report
				throw new InternalServerException("No data found for the report");
			}
		} catch (Exception e) {
// Log the error
			logger.error(user.getSystemId(), e.fillInStackTrace());
			throw new InternalServerException("Error generating XLS report: " + e.getMessage());
		}

	}

	private JasperPrint getJasperPrint(List<DeliveryDTO> reportList, boolean paging, String username)
			throws JRException {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(userOptional.get().getId());
		JasperPrint print = null;
		JasperReport report = null;
		Map parameters = new HashMap();
		JasperDesign design = JRXmlLoader.load(template_file);
		report = JasperCompileManager.compileReport(design);
		// ---------- Sorting list ----------------------------
		reportList = sortListBySender(reportList);
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
		logger.info(user.getSystemId() + " DisplayCost: " + webMasterEntry.isDisplayCost());
		parameters.put("displayCost", webMasterEntry.isDisplayCost());
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		logger.info(user.getSystemId() + " <-- Filling Report Data --> ");
		print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		logger.info(user.getSystemId() + " <-- Filling Completed --> ");
		return print;
	}

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

	@Override
	public ResponseEntity<?> ContentReportPdf(String username, ContentReportRequest customReportForm,
			HttpServletResponse response, String lang) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		try {
			locale = Customlocale.getLocaleByLanguage(lang);

			List<DeliveryDTO> reportList = getContentReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[pdf]:" + reportList.size());

				JasperPrint print = getJasperPrint(reportList, false, username);
				logger.info(user.getSystemId() + " <-- Preparing Outputstream --> ");

				byte[] pdfBytes;
				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					JRExporter exporter = new JRPdfExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
					exporter.exportReport();
					pdfBytes = out.toByteArray();
				}

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".pdf";
				headers.setContentDispositionFormData(reportName, reportName);

				logger.info(user.getSystemId() + " <-- PDF Report Finished --> ");
				target = IConstants.SUCCESS_KEY;

				return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
			} else {
// Handle case where no data is found for the report
				throw new NotFoundException("No data found for the report");
			}
		} catch (UnauthorizedException | NotFoundException e) {
// Log the specific exception
			logger.error(user.getSystemId(), e.fillInStackTrace());
			throw e; // Re-throw the specific exception
		} catch (Exception e) {
// Log a general exception
			logger.error(user.getSystemId(), e.fillInStackTrace());
			throw new InternalServerException("Error generating PDF report: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> ContentReportDoc(String username, ContentReportRequest customReportForm,
			HttpServletResponse response, String lang) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		try {
			locale = Customlocale.getLocaleByLanguage(lang);

			List<DeliveryDTO> reportList = getContentReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				int total_rec = reportList.size();
				logger.info(user.getSystemId() + " ReportSize[xls]:" + total_rec);

				Workbook workbook = dataBase.getContentWorkBook(reportList, username, lang);
				HttpHeaders headers = new HttpHeaders();

				if (total_rec > 100000) {
					logger.info(user.getSystemId() + "<-- Creating Zip Folder --> ");
					ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
					try (ZipOutputStream zos = new ZipOutputStream(zipBaos)) {
						String reportName = "delivery.xlsx";
						ZipEntry entry = new ZipEntry(reportName);
						zos.putNextEntry(entry);
						logger.info(user.getSystemId() + "<-- Starting Zip Download --> ");
						workbook.write(zos);
						zos.closeEntry();
					}

					headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
					String zipFileName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
							+ ".zip";
					headers.setContentDispositionFormData(zipFileName, zipFileName);

					logger.info(user.getSystemId() + "<-- Zip Report Finished --> ");
					target = IConstants.SUCCESS_KEY;

					return new ResponseEntity<>(zipBaos.toByteArray(), headers, HttpStatus.OK);
				} else {
					logger.info(user.getSystemId() + " <---- Creating XLS -----> ");
					headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
					String filename = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
							+ ".xlsx";
					headers.setContentDispositionFormData(filename, filename);

					ByteArrayOutputStream xlsBaos = new ByteArrayOutputStream();
					workbook.write(xlsBaos);

					logger.info(user.getSystemId() + " <---- Reading XLS -----> ");
					target = IConstants.SUCCESS_KEY;

					return new ResponseEntity<>(xlsBaos.toByteArray(), headers, HttpStatus.OK);
				}
			} else {
// Handle case where no data is found for the report
				throw new NotFoundException("No data found for the report");
			}
		} catch (UnauthorizedException | NotFoundException e) {
// Log the specific exception
			logger.error(user.getSystemId(), e.fillInStackTrace());
			throw e; // Re-throw the specific exception
		} catch (Exception e) {
// Log a general exception
			logger.error(user.getSystemId(), e.fillInStackTrace());
			throw new InternalServerException("Error generating document report: " + e.getMessage());
		}
	}

	public List<DeliveryDTO> getContentReportList(ContentReportRequest customReportForm, String username, String lang) {

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
			to_gmt = webMasterEntry.getGmt().replace("GMT","");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT","");
		}
		logger.info(user.getSystemId() + "To_gmt: " + to_gmt + "From_gmt: " + from_gmt);
		// logger.info(userSessionObject.getSystemId() + " Content Report Based On
		// Criteria");
		if (to_gmt != null) {
			query = "SELECT CONVERT_TZ(A.submitted_time,'"+from_gmt+"','"+to_gmt+"') as submitted_time,";
			unproc_query = "select CONVERT_TZ(A.time,'"+from_gmt+"','"+to_gmt+"') as time,";
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
				query += "A.submitted_time BETWEEN CONVERT_TZ('"+startDate+"','"+to_gmt+"','"+from_gmt
						+"') and CONVERT_TZ('"+endDate+"','"+to_gmt+"','"+from_gmt+"')";
				unproc_query += "A.time BETWEEN CONVERT_TZ('"+startDate+"','"+to_gmt+"','"+from_gmt
						+"') and CONVERT_TZ('"+endDate+"','"+to_gmt+"','"+from_gmt+"')";
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
		logger.info(user.getSystemId() + " ReportSQL:" + query);
		System.out.println("this is line run 455");
		List<DeliveryDTO> list = contentWiseDlrReport(query, report_user, webMasterEntry.isHideNum());
		logger.info(user.getSystemId() + " ReportSQL:" + query);
		List<DeliveryDTO> unproc_list_1 = contentWiseUprocessedReport(unproc_query.replaceAll("table_name", "smsc_in"),
				report_user, webMasterEntry.isHideNum());
		list.addAll(unproc_list_1);
		logger.info(user.getSystemId() + " ReportSQL:" + query);
		List<DeliveryDTO> unproc_list_2 = contentWiseUprocessedReport(
				unproc_query.replaceAll("table_name", "unprocessed"), report_user, webMasterEntry.isHideNum());
		list.addAll(unproc_list_2);
		logger.info(user.getSystemId() + " End Based On Criteria. Final Report Size: " + list.size());
		return list;
	}

	@Transactional
	public List<DeliveryDTO> contentWiseDlrReport(String sql, String report_user, boolean hidenumber) {
		List<DeliveryDTO> list = new ArrayList<DeliveryDTO>();
		Connection con = null;
		// private DBConnection dbCon = null;
		PreparedStatement pStmt = null;
		Connection db_con = null;
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
						logger.error(msg_id + ": " + content, une.fillInStackTrace());
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
				}
			}
		} catch (Exception sqle) {
			logger.error(report_user, sqle);
			throw new InternalServerException("contentWiseUnprocessed" + sqle.getMessage());
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

	public List<DeliveryDTO> contentWiseUprocessedReport(String sql, String report_user, boolean hidenumber) {
		logger.info(report_user + " SQL: " + sql);
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
						logger.error(msg_id + ": " + content, une.fillInStackTrace());
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
			logger.error(report_user, sqle);
			throw new InternalServerException("contentWiseUnprocessed" + sqle.getMessage());
		}

		logger.info(report_user + " report_list: " + list.size());
		return list;
	}
}