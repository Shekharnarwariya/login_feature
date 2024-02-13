package com.hti.smpp.common.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.database.DBException;
import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NoDataFoundException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.BalanceReportRequest;
import com.hti.smpp.common.request.BlockedReportRequest;
import com.hti.smpp.common.request.CampaignReportRequest;
import com.hti.smpp.common.request.CustomReportDTO;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.service.ReportService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.BalanceEntryRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.logica.smpp.Data;

import jakarta.servlet.http.HttpServletResponse;
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
public class ReportServiceImpl implements ReportService {

	@Autowired
	private DataBase dataBase;

	@Autowired
	private UserEntryRepository userRepository;
	Locale locale = null;
	private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;

	@Autowired
	private DataSource dataSource;

	private final String template_file = IConstants.FORMAT_DIR + "report//BlockedReport.jrxml";

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public ResponseEntity<?> BalanceReportView(String username, BalanceReportRequest customReportForm, String lang) {
		String target = IConstants.FAILURE_KEY;
		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			UserEntry user = userOptional
					.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
			locale = Customlocale.getLocaleByLanguage(lang);
			Map<String, List<DeliveryDTO>> reportList = dataBase.getBalanceReportList(customReportForm, username, lang);
			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				target = IConstants.SUCCESS_KEY;
				return new ResponseEntity<>(reportList, HttpStatus.OK);
			} else {
				throw new NoDataFoundException("No data found for the balance report for user: " + username);
			}
		} catch (NotFoundException e) {
			throw new NoDataFoundException(e.getMessage());
		} catch (UnauthorizedException e) {
			throw new UnauthorizedException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerException("Error occurred while processing the balance report: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> BalanceReportxls(String username, BalanceReportRequest customReportForm,
			HttpServletResponse response, String lang) {
		String target = IConstants.FAILURE_KEY;

		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			Map<String, List<DeliveryDTO>> reportList = dataBase.getBalanceReportList(customReportForm, username, lang);
			if (!reportList.isEmpty()) {
				JasperPrint print = dataBase.getJasperPrint(reportList, false);
				String reportName = "Consumption_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
						+ ".xlsx";
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					JRExporter exporter = new JRXlsxExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
					exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
					exporter.setParameter(JRXlsExporterParameter.MAXIMUM_ROWS_PER_SHEET, 60000);
					exporter.exportReport();
					byte[] content = out.toByteArray();
					if (content.length > 0) {
						return new ResponseEntity<>(content, HttpStatus.OK);
					} else {

						throw new InternalServerException("Error: Empty XLS content");
					}
				} catch (IOException ioe) {
					throw new InternalServerException("Error closing XLS OutputStream" + ioe.getMessage());
				}
			} else {
				throw new InternalServerException("Error: getting error in generating report.");
			}
		} catch (Exception e) {
			throw new InternalServerException("Unexpected error occurred");
		}
	}

	@Override
	public ResponseEntity<?> balanceReportPdf(String username, BalanceReportRequest customReportForm,
			HttpServletResponse response, String lang) {
		String target = IConstants.FAILURE_KEY;
		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			Map<String, List<DeliveryDTO>> reportList = dataBase.getBalanceReportList(customReportForm, username, lang);
			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = dataBase.getJasperPrint(reportList, false);
				System.out.println("<-- Preparing OutputStream --> ");
				String reportName = "Consumption_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
						+ ".pdf";
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				System.out.println("<-- Creating PDF --> ");
				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					JRExporter exporter = new JRPdfExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
					exporter.exportReport();
					byte[] content = out.toByteArray();
					if (content.length > 0) {
						return new ResponseEntity<>(content, HttpStatus.OK);
					} else {
						throw new InternalServerException("Error: Empty PDF content");
					}
				}
			} else {
				throw new InternalServerException("Error generating report PDF file in balance");
			}
		} catch (Exception e) {
			throw new InternalServerException("Error: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> BalanceReportDoc(String username, BalanceReportRequest customReportForm,
			HttpServletResponse response, String lang) {
		String target = IConstants.FAILURE_KEY;

		try {
			locale = Customlocale.getLocaleByLanguage(lang);

			Map<String, List<DeliveryDTO>> reportList = dataBase.getBalanceReportList(customReportForm, username, lang);
			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = dataBase.getJasperPrint(reportList, false);
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "Consumption_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
						+ ".doc";
				response.setContentType("application/msword");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				System.out.println("<-- Creating DOC --> ");

				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					JRExporter exporter = new JRDocxExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
					exporter.exportReport();

					byte[] content = out.toByteArray();

					if (content.length > 0) {
						return new ResponseEntity<>(content, HttpStatus.OK);
					} else {
						throw new InternalServerException("Error: Empty DOC content");
					}
				}
			} else {
				throw new InternalServerException("Error generating report DOC file in balance");
			}
		} catch (Exception e) {
			throw new InternalServerException("Error: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> BlockedReportView(String username, BlockedReportRequest customReportForm, String lang) {
		String target = IConstants.FAILURE_KEY;

		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = getReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info("{} ReportSize[View]: {}", username, reportList.size());
//				JasperPrint print = getJasperPrint(reportList, false, username);
//				byte[] pdfReport = JasperExportManager.exportReportToPdf(print);
//
//				HttpHeaders headers = new HttpHeaders();
//				headers.setContentType(MediaType.APPLICATION_PDF);
//				headers.setContentDispositionFormData("attachment", "blocked.pdf");
//				logger.info("{} <-- Report Finished -->", username);
//				// Return the file in the ResponseEntity
//				return new ResponseEntity<>(pdfReport, headers, HttpStatus.OK);
				target = IConstants.SUCCESS_KEY;
				return new ResponseEntity<>(reportList, HttpStatus.OK);
			} else {
				throw new InternalServerException("No data found for the report");
			}
		} catch (Exception e) {
			logger.error("{} Unexpected error generating report", username, e);
			throw new InternalServerException("Unexpected error generating report" + e);
		}
	}

	@Override
	public ResponseEntity<?> BlockedReportPdf(String username, BlockedReportRequest customReportForm,
			HttpServletResponse response, String lang) {
		String target = IConstants.FAILURE_KEY;

		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = getReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info("{} ReportSize[pdf]: {}", username, reportList.size());
				JasperPrint print = getJasperPrint(reportList, false, username);
				logger.info("{} <-- Preparing OutputStream -->", username);
				String reportName = "blocked_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".pdf";
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info("{} <-- Creating PDF -->", username);
				try (OutputStream out = response.getOutputStream()) {
					JRExporter exporter = new JRPdfExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
					exporter.exportReport();
					logger.info("{} <-- PDF Report Finished -->", username);
					target = IConstants.SUCCESS_KEY;
				} catch (IOException ioe) {
					logger.error("{} PDF OutputStream Closing Error", username, ioe);
					throw new InternalServerException(ioe.getMessage());
				}
			} else {
				throw new InternalServerException("No data found for the report");
			}
		} catch (Exception e) {
			logger.error("{} Unexpected error generating PDF report", username, e);
			throw new InternalServerException("Unexpected error generating PDF report" + e.getMessage());
		}

		return null;
	}

	@Override
	public ResponseEntity<?> BlockedReportxls(String username, BlockedReportRequest customReportForm,
			HttpServletResponse response, String lang) {
		String target = IConstants.FAILURE_KEY;
		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = getReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				int totalRec = reportList.size();
				logger.info("{} ReportSize[xls]: {}", username, totalRec);
				Workbook workbook = dataBase.getWorkBook(reportList, username);
				if (totalRec > 100000) {
					logger.info("{} <-- Creating Zip Folder -->", username);
					response.setContentType("application/zip");
					String zipFileName = "blocked_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
							+ ".zip";
					response.setHeader("Content-Disposition", "attachment; filename=" + zipFileName);
					try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
						String reportName = "blocked.xlsx";
						ZipEntry entry = new ZipEntry(reportName);
						zos.putNextEntry(entry);
						logger.info("{} <-- Starting Zip Download -->", username);
						workbook.write(zos);
					}
				} else {
					logger.info("{} <---- Creating XLS ----->", username);
					String filename = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
							+ ".xlsx";
					response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
					try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
							InputStream is = new ByteArrayInputStream(bos.toByteArray());
							OutputStream out = response.getOutputStream()) {
						logger.info("{} <---- Starting XLS Download ----->", username);
						workbook.write(bos);
						is.transferTo(out);
						out.flush();
					}
				}

				workbook.close();
				reportList.clear();
				logger.info("{} <-- XLS Report Finished -->", username);
				target = IConstants.SUCCESS_KEY;
			} else {
				logger.info("{} <-- No Records Found -->", username);
				throw new InternalServerException("{} <-- No Records Found -->" + username);
			}
		} catch (Exception e) {
			logger.error("{} Unexpected error generating XLS report", username, e);
			throw new InternalServerException("Unexpected error generating XLS report" + e.getMessage());
		}

		return null;
	}

	@Override
	public ResponseEntity<?> BlockedReportDoc(String username, BlockedReportRequest customReportForm,
			HttpServletResponse response, String lang) {
		String target = IConstants.FAILURE_KEY;
		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			;

			List<DeliveryDTO> reportList = getReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(username + " ReportSize[doc]:" + reportList.size());
				JasperPrint print = null;
				print = getJasperPrint(reportList, false, username);
				logger.info(username + " <-- Preparing Outputstream --> ");
				String reportName = "blocked_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".doc";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info(username + " <-- Creating DOC --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRDocxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						logger.info(username + " DOC OutPutSream Closing Error");
					}
				}
				logger.info(username + " <-- doc Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new InternalServerException("data not found username{}" + username);
			}
		} catch (Exception e) {
			logger.error(username, e.fillInStackTrace());
			throw new InternalServerException("Error:getting error in generate doc{}" + e.getMessage());
		}

		return null;
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
		reportList = dataBase.sortListByMessageId(reportList);
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
	public List<DeliveryDTO> getReportList(BlockedReportRequest customReportForm, String username, String lang)
			throws Exception {
		logger.info(username + " Creating Report list");

		CustomReportDTO customReportDTO = new CustomReportDTO();
//		Bean Utils.copyProperties(customReportForm, customReportDTO);

		org.springframework.beans.BeanUtils.copyProperties( customReportForm,customReportDTO);

		String startDate = customReportForm.getStartDate();
		System.out.println(startDate);
		String endDate = customReportForm.getEndDate();

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

	private List<String> distinctSpamUser(String distinct_user_sql) throws DBException {
		System.out.println("distinct_user_sql: " + distinct_user_sql);

		List<String> list = new ArrayList<String>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(distinct_user_sql);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				list.add(rs.getString("username"));
			}
		} catch (Exception sqle) {
			sqle.printStackTrace();
			logger.error("", sqle);
			throw new DBException("distinctSpamUser()", sqle);
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

	public List<DeliveryDTO> blockedReport(String sql) {
		List<DeliveryDTO> list = new ArrayList<DeliveryDTO>();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		Session openSession = null;
		Connection con = null;
		Map<String, Map<Integer, String>> content_map = new HashMap<String, Map<Integer, String>>();
		try {
			con = getConnection();
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
								combined_content += dataBase.hexCodePointsToCharMsg(value[2]);
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
						message = dataBase.hexCodePointsToCharMsg(content);
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

}
