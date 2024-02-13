package com.hti.smpp.common.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.CustomizedReportRequest;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.CustomizedReportService;

import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.IConstants;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
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
	public ResponseEntity<?> CustomizedReportView(String username, CustomizedReportRequest customReportForm,
			String lang) {
		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			UserEntry user = userOptional
					.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = dataBase.getCustomizedReportList(customReportForm, username, lang);
			if (customReportForm.getReportType().equalsIgnoreCase("Summary")) {
				isSummary = true;
			} else {
				isSummary = false;
			}
			System.out.println(isSummary);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[View]:" + reportList.size());
				JasperPrint print = isSummary ? dataBase.getSummaryJasperPrint(reportList, false, username, lang)
						: dataBase.getCustomizedJasperPrint(reportList, false, username, lang);
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

	@Override
	public String CustomizedReportxls(String username, CustomizedReportRequest customReportForm,
			HttpServletResponse response, String lang) {
		String target = IConstants.SUCCESS_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = dataBase.getCustomizedReportList(customReportForm, username, lang);
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
			HttpServletResponse response, String lang) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = dataBase.getCustomizedReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(username + " ReportSize[pdf]:" + reportList.size());
				JasperPrint print = null;
				if (customReportForm.getReportType().equalsIgnoreCase("Summary")) {
					isSummary = true;
				} else {
					isSummary = false;
				}
				if (isSummary) {
					print = dataBase.getSummaryJasperPrint(reportList, false, username, lang);
				} else {
					// Uncomment this line if you have a method for customized JasperPrint
					print = dataBase.getCustomizedJasperPrint(reportList, false, username, lang);
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
			HttpServletResponse response, String lang) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		String target = IConstants.SUCCESS_KEY;
		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = dataBase.getCustomizedReportList(customReportForm, username, lang);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[doc]:" + reportList.size());
				JasperPrint print = null;
				if (customReportForm.getReportType().equalsIgnoreCase("Summary")) {
					isSummary = true;
				} else {
					isSummary = false;
				}
				if (isSummary) {
					print = dataBase.getSummaryJasperPrint(reportList, false, username, lang);
				} else {
					// Uncomment this line if you have a method for customized JasperPrint
					print = dataBase.getCustomizedJasperPrint(reportList, false, username, lang);
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
