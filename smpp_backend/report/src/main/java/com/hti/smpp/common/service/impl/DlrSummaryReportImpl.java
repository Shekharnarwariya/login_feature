package com.hti.smpp.common.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.CustomReportDTO;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.DlrSummaryReport;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;

import com.hti.smpp.common.service.DlrSummaryReportService;
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
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@Service
public class DlrSummaryReportImpl implements DlrSummaryReportService {

	private static final Logger logger = LoggerFactory.getLogger(DlrSummaryReportImpl.class);
	private final String template_file = IConstants.FORMAT_DIR + "report//DlrSummaryHourly.jrxml";
	private String to_gmt;
	private String from_gmt;

	@Autowired
	private DataBase dataBase;

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
	private MessageResourceBundle messageResourceBundle;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	Locale locale = null;

	@Override
	public ResponseEntity<?> DlrSummaryReportview(String username, DlrSummaryReport customReportForm, String lang) {
		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			UserEntry user = userOptional
					.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));

			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}

			locale = Customlocale.getLocaleByLanguage(lang);

			List<DeliveryDTO> reportList = getDlrReportList(customReportForm, username, lang);

			if (reportList != null && !reportList.isEmpty()) {
				logger.info(messageResourceBundle.getMessage("report.view.size.message"), user, reportList.size());

				JasperPrint print = getdlrSummaryJasperPrint(reportList, false, username);

				// Convert JasperPrint to byte array (PDF)
				byte[] pdfBytes = JasperExportManager.exportReportToPdf(print);

				// Set response headers
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_PDF);
				headers.setContentDispositionFormData("attachment", "DlrSummaryReport.pdf");

				logger.info(messageResourceBundle.getMessage("report.finished.message"), username);

					
				// Return PDF file in the response body
				return ResponseEntity.ok().headers(headers).contentLength(pdfBytes.length).body(pdfBytes);
			} else {
				throw new Exception(messageResourceBundle.getExMessage(ConstantMessages.NO_DATA_FOUND_DLR_SUMMARY_REPORT));

			}
		} catch (UnauthorizedException e) {
			// Handle unauthorized exception
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_PROCESSING_DLR_SUMMARY_REPORT_MESSAGE));

		} catch (Exception e) {
			// Handle other general exceptions
			logger.error(messageResourceBundle.getMessage("unexpected.error.message"), e.getMessage(), e);

			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.GETTING_ERROR_DLR_SUMMARY_REPORT_MESSAGE , new Object[] {username}));

		}
	}

	@Override
	public ResponseEntity<?> DlrSummaryReportdoc(String username, DlrSummaryReport customReportForm,
			HttpServletResponse response, String lang) {

		String target = IConstants.SUCCESS_KEY;

		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

			UserEntry user = userOptional
					.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));

			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
			locale = Customlocale.getLocaleByLanguage(lang);
			;

			List<DeliveryDTO> reportList = getDlrReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(messageResourceBundle.getMessage("report.size.doc.message"), username, reportList.size());

				JasperPrint print = getdlrSummaryJasperPrint(reportList, false, username);
				logger.info(messageResourceBundle.getMessage("preparing.outputstream.message"), username);


				// Generate a unique filename based on the current timestamp
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".docx";

				// Set response headers
				response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");

				logger.info(messageResourceBundle.getMessage("creating.docx.message"), username);


				// Create the DOCX file and write it to the response output stream
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				JRDocxExporter exporter = new JRDocxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
				exporter.exportReport();

				// Convert ByteArrayOutputStream to byte array
				byte[] docxBytes = byteArrayOutputStream.toByteArray();

				// Return DOCX file in the response body
				return ResponseEntity.ok().contentLength(docxBytes.length).body(docxBytes);
			} else {
				throw new Exception(messageResourceBundle.getExMessage(ConstantMessages.NO_DATA_FOUND_DLR_SUMMARY_REPORT));

			}
		} catch (UnauthorizedException e) {
			// Handle unauthorized exception
			e.printStackTrace();
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_PROCESSING_DLR_SUMMARY_REPORT_MESSAGE));

		} catch (Exception e) {
			// Handle other general exceptions
			e.printStackTrace();
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_PROCESSING_DLR_SUMMARY_REPORT_MESSAGE));
		}
		// return ResponseEntity.ok(target);
	}

	@Override
	public ResponseEntity<?> DlrSummaryReportdpdf(String username, DlrSummaryReport customReportForm,
			HttpServletResponse response, String lang) {
		String target = IConstants.FAILURE_KEY;

		try {
			locale = Customlocale.getLocaleByLanguage(lang);

			List<DeliveryDTO> reportList = getDlrReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(username + " ReportSize[pdf]:" + reportList.size());
				JasperPrint print = getdlrSummaryJasperPrint(reportList, false, username);
				logger.info(messageResourceBundle.getMessage("preparing.outputstream.message"), username);

				// Generate a unique filename based on the current timestamp
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".xls";

				// Set response headers
				response.setContentType("application/vnd.ms-excel");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");

				logger.info(messageResourceBundle.getMessage("creating.xls.message"), username);


				// Create the XLS file and write it to the response output stream
				OutputStream out = response.getOutputStream();
				JRXlsExporter exporter = new JRXlsExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();

				// Close the output stream
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						logger.info(messageResourceBundle.getMessage("xls.outputstream.error.message"), username);

					}
				}

				logger.info(messageResourceBundle.getMessage("xls.report.finished.message"), username);

				return ResponseEntity.ok().build();
			} else {
				throw new Exception(messageResourceBundle.getExMessage(ConstantMessages.NO_DATA_FOUND_DLR_SUMMARY_REPORT));

			}
		} catch (UnauthorizedException e) {
			// Handle unauthorized exception
			e.printStackTrace();
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_PROCESSING_DLR_SUMMARY_REPORT_MESSAGE));
		} catch (Exception e) {
			// Handle other general exceptions
			e.printStackTrace();
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_PROCESSING_DLR_SUMMARY_REPORT_MESSAGE));

		}
		// return ResponseEntity.ok(target);
	}

	@Override
	public ResponseEntity<?> DlrSummaryReportdxls(String username, DlrSummaryReport customReportForm,
			HttpServletResponse response, String lang) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
		}

		String target = IConstants.SUCCESS_KEY;

		try {
			locale = Customlocale.getLocaleByLanguage(lang);

			List<DeliveryDTO> reportList = getDlrReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(messageResourceBundle.getMessage("xls.report.size.message"), username, reportList.size());

				JasperPrint print = getdlrSummaryJasperPrint(reportList, false, username);
				logger.info(messageResourceBundle.getMessage("preparing.outputstream.message"), username);

				// Generate a unique filename based on the current timestamp
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".xls";

				// Set response headers
				response.setContentType("application/vnd.ms-excel");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");

				logger.info(messageResourceBundle.getMessage("creating.xls.message"), username);


				// Create the XLS file and write it to the response output stream
				OutputStream out = response.getOutputStream();
				JRXlsExporter exporter = new JRXlsExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();

				// Close the output stream
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						logger.info(messageResourceBundle.getMessage("xls.outputstream.error.message"), username);

					}
				}

				logger.info(messageResourceBundle.getMessage("xls.report.finished.message"), username);

				return ResponseEntity.ok().build();
			} else {
				throw new Exception(messageResourceBundle.getExMessage(ConstantMessages.NO_DATA_FOUND_DLR_SUMMARY_REPORT));

			}
		} catch (UnauthorizedException e) {
			// Handle unauthorized exception
			e.printStackTrace();
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_PROCESSING_DLR_SUMMARY_REPORT_MESSAGE));

		} catch (Exception e) {
			// Handle other general exceptions
			e.printStackTrace();
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ERROR_PROCESSING_DLR_SUMMARY_REPORT_MESSAGE));

		}
		// return ResponseEntity.ok(target);
	}

	public List<DeliveryDTO> getDlrReportList(DlrSummaryReport customReportForm, String username, String lang)
			throws Exception {

		locale = Customlocale.getLocaleByLanguage(lang);
		UserDAService userDAService = new UserDAServiceImpl();
		Optional<UserEntry> usersOptional = userRepository.findBySystemId(username);
		if (!usersOptional.isPresent()) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

		UserEntry user = usersOptional.get();
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
		if (webMasterEntry == null) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.WEBMASTER_NOT_FOUND,new Object[] {user.getId()}));

		}

		logger.info(messageResourceBundle.getMessage("creating.report.list.message"), username);

		List list = null;
		List final_list = new ArrayList();
		CustomReportDTO customReportDTO = new CustomReportDTO();
		BeanUtils.copyProperties(customReportForm, customReportDTO);
		String startDate = customReportForm.getStartDate();
		String endDate = customReportForm.getEndDate();
//			IDatabaseService dbService = HtiSmsDB.getInstance();
//			reportUser = customReportDTO.getClientId();
		String campaign = customReportForm.getCampaign();
		// String messageStatus = customReportDTO.getMessageStatus();// "PENDING";
		// //customReportDTO.getMessageStatus();
		String destination = customReportDTO.getDestinationNumber();// "9926870493";
																	// //customReportDTO.getDestinationNumber();
		String senderId = customReportDTO.getSenderId();// "%"; //customReportDTO.getSenderId();
		String country = customReportDTO.getCountry();
		String operator = customReportDTO.getOperator();
		String query = null;
		if (webMasterEntry.getGmt().equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = webMasterEntry.getGmt().replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
		}
		logger.info(messageResourceBundle.getMessage("custom.report.dto.message"), username, customReportDTO);

		logger.info(messageResourceBundle.getMessage("report.criteria.message"), username);

		List<String> users = null;
		if (customReportDTO.getClientId().equalsIgnoreCase("All")) {
			String role = user.getRole();
			if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
				users = new ArrayList<String>(userService.listUsers().values());
			} else if (role.equalsIgnoreCase("admin")) {
				users = new ArrayList<String>(userService.listUsersUnderMaster(username).values());
				users.add(username);
				Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
						.get("secondaryMaster").equal(username);
				for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
					users.add(userEntry.getSystemId());
				}
			} else if (role.equalsIgnoreCase("seller")) {
				users = new ArrayList<String>(userService.listUsersUnderSeller(user.getId()).values());
			} else if (role.equalsIgnoreCase("manager")) {
				// SalesDAService salesService = new SalesDAServiceImpl();
				users = new ArrayList<String>(listUsernamesUnderManager(username).values());
			}
			logger.info(messageResourceBundle.getMessage("under.users.message"), username, users.size());

		} else {
			users = new ArrayList<String>();
			users.add(customReportDTO.getClientId());
		}
		if (users != null && !users.isEmpty()) {
			for (String report_user : users) {
				logger.info(messageResourceBundle.getMessage("checking.report.message"), username, report_user);
				query = "select count(msg_id) as count,date(submitted_time) as datet,HOUR(submitted_time) as hours,status from mis_"
						+ report_user + " where ";
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
						query += "submitted_time between CONVERT_TZ('" + startDate + "','" + to_gmt + "','" + from_gmt
								+ "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt + "')";
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
				query += " group by datet,hours,status order by datet,hours,status";
				
				logger.info(messageResourceBundle.getMessage("report.sql.message"), username, query);

				list = (List) getDlrSummaryReport(report_user, query);
				logger.info(username + " list:" + list.size());
				if (list != null && !list.isEmpty()) {
					// System.out.println(report_user + " Report List Size --> " + list.size());
					final_list.addAll(list);
					list.clear();
				}
			}
			// check for unprocessed/Blocked/M/F entries
			String cross_unprocessed_query = "";
			cross_unprocessed_query = "select count(msg_id) as count,date(time) as datet,HOUR(time) as hours,s_flag from table_name where ";
			cross_unprocessed_query += "username in('" + String.join("','", users) + "') and ";
			if (senderId != null && senderId.trim().length() > 0) {
				if (senderId.contains("%")) {
					cross_unprocessed_query += "source_no like \"" + senderId + "\" and ";
				} else {
					cross_unprocessed_query += "source_no =\"" + senderId + "\" and ";
				}
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
							+ from_gmt + "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt + "')";
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
			cross_unprocessed_query += " group by datet,hours,s_flag order by datet,hours,s_flag";
			List unproc_list = (List) getdlrUnprocessedSummary(
					cross_unprocessed_query.replaceAll("table_name", "unprocessed"));
			if (unproc_list != null && !unproc_list.isEmpty()) {
				final_list.addAll(unproc_list);
			}
			unproc_list = (List) getdlrUnprocessedSummary(cross_unprocessed_query.replaceAll("table_name", "smsc_in"));
			if (unproc_list != null && !unproc_list.isEmpty()) {
				final_list.addAll(unproc_list);
			}
			// logger.info(userSessionObject.getSystemId() + " ReportSQL: " +
			// cross_unprocessed_query);
			// end check for unprocessed/Blocked/M/F entries
		}
		logger.info(messageResourceBundle.getMessage("end.criteria.report.size.message"), username, final_list.size());

		return final_list;
	}

	public List getdlrUnprocessedSummary(String query) throws Exception {
		List customReport = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		DeliveryDTO report = null;
		System.out.println("UnprocessedSummary:" + query);
		try {
			Connection connection = jdbcTemplate.getDataSource().getConnection();

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
						logger.error(messageResourceBundle.getMessage("empty.error.message"), sqle.fillInStackTrace());

					}
				}
			}
		} catch (SQLException ex) {
			logger.error(messageResourceBundle.getMessage("empty.error.message"), ex.fillInStackTrace());
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
		logger.info(messageResourceBundle.getMessage("report.list.count.message"), customReport.size());

		return customReport;
	}

	public List getDlrSummaryReport(String username, String query) throws Exception {
		List customReport = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		DeliveryDTO report = null;
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
						report = new DeliveryDTO(null, null, 0, date, rs.getString("status"), rs.getInt("count"));
						report.setUsername(username);
						report.setTime(hours);
						customReport.add(report);
					} catch (Exception sqle) {
						logger.error(messageResourceBundle.getMessage("empty.error.message"), sqle.fillInStackTrace());
					}
				}
			}
		} catch (SQLException ex) {
			if (ex.getMessage().contains("Table") && ex.getMessage().contains("doesn't exist")) {
				logger.error(messageResourceBundle.getMessage("missing.tables.message"), username);

				// createMisTable(username);
				// createContentTable(username);
			} else {
				logger.error(messageResourceBundle.getMessage("empty.error.message"), ex.fillInStackTrace());
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
				}
			} catch (SQLException sqle) {
			}
		}
		return customReport;
	}

	public JasperPrint getdlrSummaryJasperPrint(List reportList, boolean paging, String username) throws Exception {
		JasperPrint print = null;
		JasperReport report = null;
		JasperDesign design = null;
		Map parameters = new HashMap();
		design = JRXmlLoader.load(template_file);
		report = JasperCompileManager.compileReport(design);
		reportList = sortListByTime(reportList);
		logger.info(messageResourceBundle.getMessage("preparing.charts.message"), username);

		// ------------- Preparing databeancollection for chart ------------------
		Iterator itr = reportList.iterator();
		Map temp_chart = new HashMap();
		Map<String, DeliveryDTO> prepared_list = new HashMap<String, DeliveryDTO>();
		while (itr.hasNext()) {
			DeliveryDTO chartDTO = (DeliveryDTO) itr.next();
			String status = chartDTO.getStatus();
			int counter = 0;
			if (temp_chart.containsKey(status)) {
				counter = (Integer) temp_chart.get(status);
			}
			counter = counter + chartDTO.getStatusCount();
			temp_chart.put(status, counter);
			// --------------- prepared list -------------------
			DeliveryDTO prepared_dto = null;
			if (prepared_list.containsKey(chartDTO.getDate() + " " + chartDTO.getTime())) {
				prepared_dto = prepared_list.get(chartDTO.getDate() + " " + chartDTO.getTime());
			} else {
				prepared_dto = new DeliveryDTO();
				prepared_dto.setDate(chartDTO.getDate());
				prepared_dto.setTime(chartDTO.getTime());
			}
			if (status.startsWith("DELIV")) {
				prepared_dto.setDelivered(prepared_dto.getDelivered() + chartDTO.getStatusCount());
			} else if (status.startsWith("UNDEL")) {
				prepared_dto.setUndelivered(prepared_dto.getUndelivered() + chartDTO.getStatusCount());
			} else if (status.startsWith("EXPIR")) {
				prepared_dto.setExpired(prepared_dto.getExpired() + chartDTO.getStatusCount());
			} else if (status.startsWith("ATES")) {
				prepared_dto.setPending(prepared_dto.getPending() + chartDTO.getStatusCount());
			} else {
				prepared_dto.setOthers(prepared_dto.getOthers() + chartDTO.getStatusCount());
			}
			prepared_dto.setSubmitted(prepared_dto.getSubmitted() + chartDTO.getStatusCount());
			prepared_list.put(chartDTO.getDate() + " " + chartDTO.getTime(), prepared_dto);
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
		JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(chart_list);
		parameters.put("piechartDataSource", piechartDataSource);
		logger.info(messageResourceBundle.getMessage("preparing.report.message"), username);

		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(
				sortListByTime(new ArrayList<DeliveryDTO>(prepared_list.values())));
		if (reportList.size() > 20000) {
			logger.info(messageResourceBundle.getMessage("creating.virtualizer.message"), username);

			JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(1000,
					new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 2048, 1024));
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		}
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		logger.info(messageResourceBundle.getMessage("filling.report.data.message"), username);

		print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		logger.info(messageResourceBundle.getMessage("filling.completed.message"), username);

		return print;
	}

	private List sortListByTime(List list) {

		// logger.info(username + " sortListByTime ");
		Comparator<DeliveryDTO> comparator = null;
		comparator = Comparator.comparing(DeliveryDTO::getDate).thenComparing(DeliveryDTO::getTime)
				.thenComparing(DeliveryDTO::getStatus);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
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
