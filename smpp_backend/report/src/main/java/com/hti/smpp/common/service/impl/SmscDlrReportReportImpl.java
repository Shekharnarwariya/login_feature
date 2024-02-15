package com.hti.smpp.common.service.impl;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import com.hazelcast.internal.util.collection.ArrayUtils;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.SmscDlrReportRequest;
import com.hti.smpp.common.response.DeliveryDTO;

import com.hti.smpp.common.service.SmscDlrReportReportService;
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
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@Service
public class SmscDlrReportReportImpl implements SmscDlrReportReportService {

	@Autowired
	private DataBase dataBase;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Autowired
	private UserDAService userService;
	private Logger logger = LoggerFactory.getLogger(UserDeliveryReportServiceImpl.class);
	boolean isSummary;
	@Autowired
	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	Locale locale = null;
	private String template_file = IConstants.FORMAT_DIR + "report//SmscDlrReport.jrxml";
	private final String summary_template_file = IConstants.FORMAT_DIR + "report//SmscDlrSummaryReport.jrxml";

	@Override
	public ResponseEntity<?> SmscDlrReportview(String username, SmscDlrReportRequest customReportForm, String lang) {
		List<DeliveryDTO> target = new ArrayList<>();
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
		}
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
		if (webMasterEntry == null) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.WEBMASTER_ENTRY_NOT_FOUND_MESSAGE,new Object[] {user.getId()}));

		}
		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = getReportList(customReportForm, username, webMasterEntry, lang);
			if (!reportList.isEmpty()) {
				System.out.println(user.getSystemId() + " Report Size: " + reportList.size());
				JasperPrint print = null;

				if (isSummary) {
					print = getSummaryJasperPrint(reportList, false, username);
				} else {
					print = getJasperPrint(reportList, false, username, webMasterEntry);
				}
				return new ResponseEntity<>(reportList, HttpStatus.OK);
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SMS_DLR_REPORT_NOT_FOUND_MESSAGE, new Object[] {username}));

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerException("Error: " + e.getMessage());
		}

	}

	@Override
	public ResponseEntity<?> SmscDlrReportxls(String username, SmscDlrReportRequest customReportForm, String lang,
			HttpServletResponse response) {

		List<DeliveryDTO> target = null;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
		}
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
		if (webMasterEntry == null) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.WEBMASTER_ENTRY_NOT_FOUND_MESSAGE,new Object[] {user.getId()}));
		}
		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = getReportList(customReportForm, username, webMasterEntry, lang);
			Map<String, DeliveryDTO> map = new LinkedHashMap<String, DeliveryDTO>();
			if (isSummary) {
				Iterator itr = reportList.iterator();
				DeliveryDTO reportDTO = null;
				DeliveryDTO tempDTO = null;
				while (itr.hasNext()) {

					reportDTO = (DeliveryDTO) itr.next();
					String key = reportDTO.getDate() + "#" + reportDTO.getRoute() + "#" + reportDTO.getCountry() + "#"
							+ reportDTO.getOperator() + "#" + reportDTO.getStatus();
					if (map.containsKey(key)) {
						tempDTO = map.get(key);
					} else {
						tempDTO = new DeliveryDTO();
						tempDTO.setDate(reportDTO.getDate());
						tempDTO.setCountry(reportDTO.getCountry());
						tempDTO.setOperator(reportDTO.getOperator());
						tempDTO.setRoute(reportDTO.getRoute());
						tempDTO.setStatus(reportDTO.getStatus());
					}
					tempDTO.setStatusCount(tempDTO.getStatusCount() + reportDTO.getStatusCount());
					System.out.println(key + " :-> " + " Count: " + tempDTO.getStatusCount());
					map.put(key, tempDTO);
				}
				reportList.clear();
				reportList.addAll(map.values());
			}
			if (reportList != null && !reportList.isEmpty()) {
				int total_rec = reportList.size();
				logger.info(messageResourceBundle.getMessage("xls.report.size.message"), username, total_rec);

				// ---------- Sorting list ----------------------------
				reportList = sortListByCountry(reportList);
				Workbook workbook = null;
				if (isSummary) {
					workbook = getSummaryWorkBook(reportList, username);
				} else {
					workbook = getWorkBook(reportList, username, webMasterEntry);
				}
				if (total_rec > 100000) {
					logger.info(messageResourceBundle.getMessage("creating.zip.folder.message"), username);

					response.setContentType("application/zip");
					response.setHeader("Content-Disposition", "attachment; filename=" + "delivery_"
							+ new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".zip");
					ZipOutputStream zos = new ZipOutputStream(response.getOutputStream()); // create a ZipOutputStream
																							// from servletOutputStream
					String reportName = "delivery.xlsx";
					ZipEntry entry = new ZipEntry(reportName); // create a zip entry and add it to ZipOutputStream
					zos.putNextEntry(entry);
					logger.info(messageResourceBundle.getMessage("starting.zip.download.message"), username);

					workbook.write(zos);
					zos.close();
				} else {
					logger.info(messageResourceBundle.getMessage("creating.xls.message"), username);

					String filename = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0))
							+ ".xlsx";
					// response.setContentType("text/html; charset=utf-8");
					response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
					// response.setHeader("Content-Disposition", "attachment; filename=" +
					// filename);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					workbook.write(bos);
					logger.info(messageResourceBundle.getMessage("reading.xls.message"), username);

					InputStream is = null;
					OutputStream out = null;
					try {
						is = new ByteArrayInputStream(bos.toByteArray());
						// byte[] buffer = new byte[8789];
						int curByte = -1;
						out = response.getOutputStream();
						logger.info(messageResourceBundle.getMessage("starting.xls.download.message"), username);

						while ((curByte = is.read()) != -1) {
							out.write(curByte);
						}
						out.flush();
					} catch (Exception ex) {
						logger.error(messageResourceBundle.getMessage("dlr.xlsreport.error.message"), username);

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
				logger.info(messageResourceBundle.getMessage("xls.report.finished.message"), username);

				target = reportList;
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SMS_DLR_REPORT_NOT_FOUND_MESSAGE, new Object[] {username}));
			}
		} catch (Exception e) {
			logger.error(messageResourceBundle.getMessage("error.message"), username);

			throw new InternalServerException("Error: " + e.getMessage());

		}
		return (ResponseEntity<?>) target;
	}

	@Override
	public ResponseEntity<?> SmscDlrReportpdf(String username, SmscDlrReportRequest customReportForm, String lang,
			HttpServletResponse response) {
		List<DeliveryDTO> target = null;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
		}

		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());

		if (webMasterEntry == null) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.WEBMASTER_ENTRY_NOT_FOUND_MESSAGE,new Object[] {user.getId()}));
		}

		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = getReportList(customReportForm, username, webMasterEntry, lang);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[pdf]:" + reportList.size());
				JasperPrint print = null;
				print = getSummaryJasperPrint(reportList, false, username);
				logger.info(user.getSystemId() + " <-- Preparing Outputstream --> ");
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".pdf";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info(user.getSystemId() + " <-- Creating PDF --> ");
				OutputStream out = response.getOutputStream();
				
				 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			        JRExporter exporter = new JRPdfExporter();
			        exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
			        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
			        exporter.exportReport();

			        // Set up the response headers
			        HttpHeaders headers = new HttpHeaders();
			        headers.setContentType(MediaType.APPLICATION_PDF);
			        headers.setContentDispositionFormData("attachment", "delivery_report.pdf");

			        // Return the byte array as ResponseEntity
			        return ResponseEntity.ok()
			                .headers(headers)
			                .contentLength(byteArrayOutputStream.size())
			                .body(byteArrayOutputStream.toByteArray());
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SMS_DLR_REPORT_NOT_FOUND_MESSAGE, new Object[] {username}));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}

	@Override
	public ResponseEntity<?> SmscDlrReportdoc(String username, SmscDlrReportRequest customReportForm, String lang,
			HttpServletResponse response) {
		List<DeliveryDTO> target = null;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
		if (webMasterEntry == null) {
			throw new NotFoundException("web MasterEntry not found for username: " + user.getId());
		}
		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = getReportList(customReportForm, username, webMasterEntry, lang);
			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = null;
				if (isSummary) {
					print = getSummaryJasperPrint(reportList, false, username);
				} else {
					print = getJasperPrint(reportList, false, username, webMasterEntry);
				}
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "SmscDlr_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".doc";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				System.out.println("<-- Creating DOC --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRDocxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (Exception e) {
						System.out.println("DOC OutPutSream Closing Error");
					}
				}
				System.out.println("<-- Finished --> ");
				return new ResponseEntity<>(reportList, HttpStatus.OK);
			} else {
				throw new NotFoundException("SmscDlr  report not found with username: " + username);
			}
		} catch (Exception e) {
			logger.error(username, e.fillInStackTrace());
			throw new InternalServerException("Error: " + e.getMessage());
		}
	}

	private Workbook getWorkBook(List reportList, String username, WebMasterEntry webMasterEntry) {
		logger.info(username + " <-- Creating WorkBook --> ");
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
		String[] headers = { "Smsc", "MessageId", "SubmitOn", "Destination", "SenderId", "Country", "Operator",
				"DeliverOn", "Cost", "Status" };
		if (!webMasterEntry.isDisplayCost()) {
			headers = (String[]) ArrayUtils.remove(headers, 8);
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
					cell.setCellValue(reportDTO.getRoute());
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
					cell.setCellValue(reportDTO.getCountry());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(6);
					cell.setCellValue(reportDTO.getOperator());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(7);
					cell.setCellValue(reportDTO.getDeliverOn());
					cell.setCellStyle(rowStyle);
					if (webMasterEntry.isDisplayCost()) {
						cell = row.createCell(8);
						cell.setCellValue(reportDTO.getCost());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(9);
						cell.setCellValue(reportDTO.getStatus());
						cell.setCellStyle(rowStyle);
					} else {
						cell = row.createCell(8);
						cell.setCellValue(reportDTO.getStatus());
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

	private Workbook getSummaryWorkBook(List reportList, String username) {
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
		headers = new String[] { "Smsc", "Date", "Country", "Operator", "Status", "Count" };
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
					cell.setCellValue(reportDTO.getRoute());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(1);
					cell.setCellValue(reportDTO.getDate());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(2);
					cell.setCellValue(reportDTO.getCountry());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(3);
					cell.setCellValue(reportDTO.getOperator());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(4);
					cell.setCellValue(reportDTO.getStatus());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(5);
					cell.setCellValue(reportDTO.getStatusCount());
					cell.setCellStyle(rowStyle);
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

	private JasperPrint getJasperPrint(List<DeliveryDTO> reportList, boolean paging, String username,
			WebMasterEntry webMasterEntry) throws Exception {
		JasperPrint print = null;
		JasperReport report = null;
		JasperDesign design = null;
		Map parameters = new HashMap();
		design = JRXmlLoader.load(template_file);
		report = JasperCompileManager.compileReport(design);
		// ---------- Sorting list ----------------------------
		reportList = sortListByCountry(reportList);
		// ------------- Preparing databeancollection for chart ------------------
		logger.info(username + " <-- Preparing Charts --> ");
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
			chartDTO.setCountry(entry.getKey());
			bar_chart_list.add(chartDTO);
			int delivered = 0;
			if (deliv_map.containsKey(entry.getKey())) {
				delivered = (Integer) deliv_map.get(entry.getKey());
			}
			chartDTO = new DeliveryDTO("DELIVRD", delivered);
			chartDTO.setCountry(entry.getKey());
			bar_chart_list.add(chartDTO);
		}
		JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(chart_list);
		JRBeanCollectionDataSource barchart1DataSource = new JRBeanCollectionDataSource(bar_chart_list);
		parameters.put("piechartDataSource", piechartDataSource);
		parameters.put("barchart1DataSource", barchart1DataSource);
		logger.info(username + " <-- Finished Charts --> ");
		// -----------------------------------------------------------------------
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		if (reportList.size() > 20000) {
			logger.info(username + " <-- Creating Virtualizer --> ");
			JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(100,
					new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 1024, 512));
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		}
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		logger.info(username + " DisplayCost: " + webMasterEntry.isDisplayCost());
		parameters.put("displayCost", webMasterEntry.isDisplayCost());
		logger.info(username + " <-- Filling Report Data --> ");
		print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		logger.info(username + " <-- Filling Completed --> ");
		return print;
	}

	private static List<DeliveryDTO> sortListByCountry(List<DeliveryDTO> list) {
		Comparator<DeliveryDTO> comparator = null;
		comparator = Comparator.comparing(DeliveryDTO::getRoute).thenComparing(DeliveryDTO::getCountry)
				.thenComparing(DeliveryDTO::getOperator);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortMapByDscValue(Map<K, V> map, int limit) {
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();
		st.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(limit)
				.forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
		return result;
	}

	private JasperPrint getSummaryJasperPrint(List reportList, boolean paging, String username) throws Exception {
		JasperPrint print = null;
		JasperReport report = null;
		JasperDesign design = null;
		Map parameters = new HashMap();
		design = JRXmlLoader.load(summary_template_file);
		report = JasperCompileManager.compileReport(design);
		reportList = sortListByCountry(reportList);
		logger.info(username + " <-- Preparing Charts --> ");
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
		Map<String, DeliveryDTO> map = new LinkedHashMap<String, DeliveryDTO>();
		itr = reportList.iterator();
		DeliveryDTO reportDTO = null;
		DeliveryDTO tempDTO = null;
		while (itr.hasNext()) {
			reportDTO = (DeliveryDTO) itr.next();
			String key = reportDTO.getDate() + "#" + reportDTO.getRoute() + "#" + reportDTO.getCountry() + "#"
					+ reportDTO.getOperator() + "#" + reportDTO.getStatus();
			if (map.containsKey(key)) {
				tempDTO = map.get(key);
			} else {
				tempDTO = new DeliveryDTO();
				tempDTO.setDate(reportDTO.getDate());
				tempDTO.setCountry(reportDTO.getCountry());
				tempDTO.setOperator(reportDTO.getOperator());
				tempDTO.setRoute(reportDTO.getRoute());
				tempDTO.setStatus(reportDTO.getStatus());
			}
			tempDTO.setStatusCount(tempDTO.getStatusCount() + reportDTO.getStatusCount());
			System.out.println(key + " :-> " + " Count: " + tempDTO.getStatusCount());
			map.put(key, tempDTO);
		}
		reportList.clear();
		reportList.addAll(map.values());
		JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(chart_list);
		parameters.put("piechartDataSource", piechartDataSource);
		JRBeanCollectionDataSource barchart1DataSource = new JRBeanCollectionDataSource(bar_chart_list);
		parameters.put("barchart1DataSource", barchart1DataSource);
		logger.info(username + " <-- Preparing report --> ");
		String time_interval = "";
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
			logger.info(username + " <-- Creating Virtualizer --> ");
			JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(1000,
					new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 2048, 1024));
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		}
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		parameters.put("time_interval", time_interval);
		logger.info(username + " <-- Filling Report Data --> ");
		print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		logger.info(username + " <-- Filling Finished --> ");
		return print;
	}

	private List<DeliveryDTO> getReportList(SmscDlrReportRequest customReportForm, String username,
			WebMasterEntry webMasterEntry, String lang) throws Exception {
		List<DeliveryDTO> list = null;

		// int back_day = 1;
		List final_list = new ArrayList();
		// IDatabaseService dbService = HtiSmsDB.getInstance();
		String start_date = null;
		String last_date = null;
		String country = customReportForm.getCountry();
		String operator = customReportForm.getOperator();
		String senderId = customReportForm.getSenderId();
		String smscnames = String.join("','", customReportForm.getSmscnames());
		String startDate = customReportForm.getStartDate();
		String endDate = customReportForm.getEndDate();
		if (customReportForm.getStartDate() == null || customReportForm.getStartDate().length() == 0) {
			start_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(0));
		} else {
			start_date = customReportForm.getStartDate().split(" ")[0];
		}
		if (customReportForm.getEndDate() == null || customReportForm.getEndDate().length() == 0) {
			last_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(0));
		} else {
			last_date = customReportForm.getEndDate().split(" ")[0];
		}
		String report_sql = "select distinct(username) from report where smsc in('" + smscnames + "')";
		String summary_sql = "select distinct(username) from report_summary where smsc in('" + smscnames + "') and ";
		if (senderId != null && senderId.trim().length() > 0) {
			if (senderId.contains("%")) {
				report_sql += " and sender like \"" + senderId + "\"";
				summary_sql += "sender like \"" + senderId + "\" and ";
			} else {
				report_sql += " and sender =\"" + senderId + "\"";
				summary_sql += "sender =\"" + senderId + "\" and ";
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
				summary_sql += "oprCountry in (" + oprCountry + ") and ";
				report_sql += " and oprCountry in (" + oprCountry + ")";
			}
		}
		if (start_date.equalsIgnoreCase(last_date)) {
			summary_sql += "time ='" + start_date + "' ";
		} else {
			summary_sql += "time between '" + start_date + "' and '" + last_date + "' ";
		}
		logger.info(username + " SQL: " + summary_sql);
		Set<String> set1 = getDistinctReportUsers(summary_sql);
		logger.info(username + " SQL: " + report_sql);
		Set<String> set2 = getDistinctReportUsers(report_sql);
		set1.addAll(set2);
		logger.info(username + " Total Report Users: " + set1);
		if (customReportForm.getReportType().equalsIgnoreCase("Summary")) {
			isSummary = true;
		} else {
			isSummary = false;
		}
		if (!set1.isEmpty()) {
			for (String report_user : set1) {
				logger.info(username + " Checking Report For " + report_user);
				String query = "";
				if (isSummary) {
					query = "select count(msg_id) as count,date(submitted_time) as date,oprCountry,status,cost,route_to_smsc from mis_"
							+ report_user + " where route_to_smsc in('" + smscnames + "') and ";
				} else {
					query = "select submitted_time,msg_id,oprCountry,source_no,dest_no,cost,status,deliver_time,route_to_smsc,err_code from mis_"
							+ report_user + " where route_to_smsc in('" + smscnames + "') and ";
				}
				if (senderId != null && senderId.trim().length() > 0) {
					if (senderId.contains("%")) {
						query += "source_no like \"" + senderId + "\" and ";
					} else {
						query += "source_no =\"" + senderId + "\" and ";
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
				// query += " order by submitted_time DESC,oprCountry ASC,msg_id DESC";
				if (isSummary) {
					query += " group by route_to_smsc,date(submitted_time),oprCountry,status,cost";
					logger.info(username + " ReportSQL:" + query);
					list = getSmscDlrSummaryReport(report_user, query);
				} else {
					logger.info(username + " ReportSQL:" + query);
					list = getCustomizedReport(report_user, query, webMasterEntry.isHideNum());
				}
				logger.info(username + " list:" + list.size());
				if (list != null && !list.isEmpty()) {
					// System.out.println(report_user + " Report List Size --> " + list.size());
					final_list.addAll(list);
					list.clear();
				}
			}
			// ---------- check for unprocessed/Blocked/M/F entries -----------------
			String cross_unprocessed_query = "";
			if (isSummary) {
				cross_unprocessed_query = "select count(msg_id) as count,date(time) as date,oprCountry,s_flag,cost,smsc from table_name where smsc in('"
						+ smscnames + "') and ";
			} else {
				cross_unprocessed_query = "select time,msg_id,username,oprCountry,source_no,destination_no,cost,s_flag,smsc from table_name where smsc in('"
						+ smscnames + "') and ";
			}
			if (senderId != null && senderId.trim().length() > 0) {
				if (senderId.contains("%")) {
					cross_unprocessed_query += "source_no like \"" + senderId + "\" and ";
				} else {
					cross_unprocessed_query += "source_no =\"" + senderId + "\" and ";
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
			if (isSummary) {
				cross_unprocessed_query += " group by smsc,date(time),oprCountry,s_flag,cost";
				List unproc_list = (List) getSmscUnprocessedSummary(
						cross_unprocessed_query.replaceAll("table_name", "unprocessed"));
				if (unproc_list != null && !unproc_list.isEmpty()) {
					final_list.addAll(unproc_list);
				}
				unproc_list = (List) getSmscUnprocessedSummary(
						cross_unprocessed_query.replaceAll("table_name", "smsc_in"));
				if (unproc_list != null && !unproc_list.isEmpty()) {
					final_list.addAll(unproc_list);
				}
			} else {
				List unproc_list = getUnprocessedReport(cross_unprocessed_query.replaceAll("table_name", "unprocessed"),
						webMasterEntry.isHideNum(), false);
				if (unproc_list != null && !unproc_list.isEmpty()) {
					final_list.addAll(unproc_list);
				}
				unproc_list = getUnprocessedReport(cross_unprocessed_query.replaceAll("table_name", "smsc_in"),
						webMasterEntry.isHideNum(), false);
				if (unproc_list != null && !unproc_list.isEmpty()) {
					final_list.addAll(unproc_list);
				}
			}
			// --------- end unprocessed --------------
		}
		logger.info(username + " Final Report Size: " + final_list.size());
		return final_list;

	}

	public List getUnprocessedReport(String query, boolean hide_number, boolean isContent) throws Exception {
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
					// dbCon.releaseConnection(con);
				}
			} catch (SQLException sqle) {
			}
		}
		logger.info(" Report List Count:--> " + customReport.size());
		return customReport;
	}

	private Set<String> getSmscErrorFlagSymbol() {
		Set<String> map = new HashSet<String>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "select distinct(Flag_symbol) from smsc_error_code";
		try {
			con = getConnection();
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
					// dbCon.releaseConnection(con);
				}
			} catch (SQLException sqle) {
			}
		}
		return map;
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

	public List getSmscUnprocessedSummary(String query) throws Exception {
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
						String smsc = rs.getString("smsc");
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
						report = new DeliveryDTO(country, operator, rs.getDouble("cost"), date, status,
								rs.getInt("count"));
						report.setRoute(smsc);
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
					// dbCon.releaseConnection(con);
				}
			} catch (SQLException sqle) {
			}
		}
		logger.info(" Report List Count:--> " + customReport.size());
		return customReport;
	}

	public List getSmscDlrSummaryReport(String username, String query) throws Exception {
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
						String date = rs.getString("date");
						String smsc = rs.getString("route_to_smsc");
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
						report = new DeliveryDTO(country, operator, rs.getDouble("cost"), date, rs.getString("status"),
								rs.getInt("count"));
						report.setRoute(smsc);
						customReport.add(report);
					} catch (Exception sqle) {
						logger.error(" ", sqle.fillInStackTrace());
					}
				}
			}
		} catch (SQLException ex) {
			if (ex.getMessage().contains("Table") && ex.getMessage().contains("doesn't exist")) {
				logger.error("<-- " + username + " Mis & Content Table Doesn't Exist -->");
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
					// dbCon.releaseConnection(con);
				}
			} catch (SQLException sqle) {
			}
		}
		return customReport;
	}

	public Set<String> getDistinctReportUsers(String query) {
		Set<String> username = new HashSet<String>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				username.add(rs.getString("username"));
			}
		} catch (SQLException sqle) {
			logger.error(query + "", sqle);
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
					// dbCon.releaseConnection(con);
				}
			} catch (SQLException sqle) {
			}
		}
		return username;
	}

	public List getCustomizedReport(String username, String query, boolean hideNum) throws Exception {
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
						report.setRoute(rs.getString("route_to_smsc"));
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
					// dbCon.releaseConnection(con);
				}
			} catch (SQLException sqle) {
			}
		}
		return customReport;
	}

}
