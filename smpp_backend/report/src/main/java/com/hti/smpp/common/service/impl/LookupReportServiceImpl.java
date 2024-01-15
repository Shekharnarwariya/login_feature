package com.hti.smpp.common.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashMap;

import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;

import java.awt.Color;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.LookupServiceInvoker;
import com.hti.smpp.common.rmi.dto.LookupReport;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.LookupReportService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.IConstants;

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
public class LookupReportServiceImpl implements LookupReportService {
	private static final Logger logger = LoggerFactory.getLogger(LookupReportServiceImpl.class);

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

	Locale locale = null;
	private String template_file = IConstants.FORMAT_DIR + "report//lookupReport.jrxml";

	@Override
	public List<LookupReport> LookupReportview(String username, CustomReportForm customReportForm, String lang) {
		String target = IConstants.SUCCESS_KEY;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		try {

			locale = Customlocale.getLocaleByLanguage(lang);

			List<LookupReport> reportList = getLookupReport(customReportForm, user.getRole(), username);
			if (reportList != null && !reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = null;
				print = getJasperPrint(reportList, false);

				target = IConstants.SUCCESS_KEY;
			} else {
				throw new InternalServerException("Error generating report PDF file in LookUpReport");
			}
		} catch (Exception e) {
			throw new InternalServerException("Error: " + e.getMessage());
		}
		return null;

	}

	@Override
	public String LookupReportxls(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response) {

		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<LookupReport> reportList = getLookupReport(customReportForm, user.getRole(), username);
			if (reportList != null && !reportList.isEmpty()) {
				int total_rec = reportList.size();
				System.out.println("Report Size: " + total_rec);
				reportList = sortList(reportList);
				org.apache.poi.ss.usermodel.Workbook workbook = getWorkBook(reportList);
				if (total_rec > 100000) {
					response.setContentType("application/zip");
					response.setHeader("Content-Disposition", "attachment; filename=" + "lookup_"
							+ new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".zip");
					ZipOutputStream zos = new ZipOutputStream(response.getOutputStream()); // create a ZipOutputStream
																							// from servletOutputStream
					String reportName = "lookup.xlsx";
					ZipEntry entry = new ZipEntry(reportName); // create a zip entry and add it to ZipOutputStream
					zos.putNextEntry(entry);
					System.out.println("<-- Starting Zip Download --> ");
					workbook.write(zos);
					zos.close();
				} else {
					// response.setContentType("text/html; charset=utf-8");
					response.setHeader("Content-Disposition", "attachment; filename=" + "lookup_"
							+ new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".xlsx");
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					workbook.write(bos);
					InputStream is = null;
					OutputStream out = null;
					try {
						is = new ByteArrayInputStream(bos.toByteArray());
						int curByte = -1;
						out = response.getOutputStream();
						System.out.println("<---- Starting Download -----> ");
						while ((curByte = is.read()) != -1) {
							out.write(curByte);
						}
						out.flush();
					} catch (Exception ex) {
						System.out.println("lookup XlsReport Download Stream Error : " + ex.toString());
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
							System.out.println("Major Error Releasing Streams: " + ex.toString());
						}
					}
				}
				System.out.println("<-- Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new InternalServerException("Error generating report PDF file in LookUpReport");
			}
		} catch (Exception e) {
			throw new InternalServerException("Error: " + e.getMessage());
		}
		return target;
	}

	@Override
	public String LookupReportPdf(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		try {
			locale = Customlocale.getLocaleByLanguage(lang);

			List<LookupReport> reportList = getLookupReport(customReportForm, user.getRole(), username);
			if (reportList != null && !reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = getJasperPrint(reportList, false);
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "lookup_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".pdf";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				System.out.println("<-- Creating PDF --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRPdfExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (Exception e) {
						System.out.println("PDF OutPutSream Closing Error");
					}
				}
				System.out.println("<-- Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new InternalServerException("Error generating report PDF file in LookUpReport");
			}
		} catch (Exception e) {
			throw new InternalServerException("Error: " + e.getMessage());
		}
		return target;
	}

	@Override
	public String LookupReportDoc(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		try {

			locale = Customlocale.getLocaleByLanguage(lang);
			List<LookupReport> reportList = getLookupReport(customReportForm, user.getRole(), username);
			if (reportList != null && !reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = getJasperPrint(reportList, false);
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "lookup_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".doc";
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
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new InternalServerException("Error generating report PDF file in LookUpReport");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return target;
	}

	@Override
	public String LookupReportRecheck(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		String sql = "select * from lookup_result where status='ACCEPTD' ";

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		try {

			String batchid = null;
			if (customReportForm.getCheck_A() != null) // batch_id specified
			{
				if (!customReportForm.getMessageId().equals("%")) {
					batchid = customReportForm.getMessageId();
				}
			}
			if (batchid != null) {
				sql += "and batch_id = '" + batchid + "' ";
			} else {

				// String role = SessionHelper.getClientRole(request);
				String role = user.getRole();
				if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
					if (customReportForm.getCheck_B() != null) // username specified
					{
						if (!customReportForm.getClientId().equalsIgnoreCase("%")) {
							sql += "and username='" + customReportForm.getClientId() + "' ";
						}
					}
				} else {
					sql += "and username='" + customReportForm.getClientId() + "' ";
				}
				if (customReportForm.getSday() != null && customReportForm.getEday() != null) {
					if (customReportForm.getSday().equalsIgnoreCase(customReportForm.getEday())) {
						sql += "and DATE(createtime) = '" + customReportForm.getSday() + "'";
					} else {
						sql += "and DATE(createtime) between '" + customReportForm.getSday() + "' and '"
								+ customReportForm.getEday() + "'";
					}
				} else {
					if (batchid == null) {
						sql += "and DATE(createtime) ='" + new SimpleDateFormat("yyyy-MM-dd").format(new Date(0)) + "'";
					}
				}
			}
			System.out.println("SQL: " + sql);
			int count = new LookupServiceInvoker().reCheckStatus(sql);
			System.out.println("Recheck Lookup Size: " + count);
			if (count > 0) {
				System.out.println("message.recheckSuccess");
				target = "recheck";
			} else {
				System.out.println("error.record.unavailable");
			}
		} catch (Exception ex) {
			throw new InternalServerException("error.processError");

		}

		return target;
	}

	private org.apache.poi.ss.usermodel.Workbook getWorkBook(List reportList) {
		System.out.println("<-- Creating WorkBook --> ");
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		int records_per_sheet = 100000;
		int sheet_number = 0;
		Sheet sheet = null;
		Row row = null;
		XSSFFont headerFont = (XSSFFont) workbook.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setColor(new XSSFColor(Color.white));
		XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
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
		rowFont.setColor(new XSSFColor(Color.WHITE));
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
		String[] headers = { "BatchId", "Username", "LookupId", "SubmitOn", "Destination", "Status", "ErrorCode",
				"Error", "Country", "Operator", "NNC", "DeliverOn", "IMSI", "MSC", "isPorted", "Operator", "NNC",
				"isRoaming", "Country", "Operator", "NNC" };
		while (!reportList.isEmpty()) {
			int row_number = 0;
			sheet = workbook.createSheet("Sheet(" + sheet_number + ")");
			sheet.setDefaultColumnWidth(14);
			System.out.println("Creating Sheet: " + sheet_number);
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
					LookupReport reportDTO = (LookupReport) reportList.remove(0);
					Cell cell = row.createCell(0);
					cell.setCellValue(reportDTO.getBatchId());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(1);
					cell.setCellValue(reportDTO.getUsername());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(2);
					cell.setCellValue(reportDTO.getHlrid());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(3);
					cell.setCellValue(reportDTO.getSubmitTime());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(4);
					cell.setCellValue(reportDTO.getNumber());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(5);
					cell.setCellValue(reportDTO.getStatus());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(6);
					cell.setCellValue(reportDTO.getErrorCode());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(7);
					cell.setCellValue(reportDTO.getError());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(8);
					cell.setCellValue(reportDTO.getCountry());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(9);
					cell.setCellValue(reportDTO.getOperator());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(10);
					cell.setCellValue(reportDTO.getNetworkCode());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(11);
					cell.setCellValue(reportDTO.getDoneTime());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(12);
					cell.setCellValue(reportDTO.getImsi());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(13);
					cell.setCellValue(reportDTO.getMsc());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(14);
					cell.setCellValue(reportDTO.getIsPorted());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(15);
					cell.setCellValue(reportDTO.getPortedOperater());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(16);
					cell.setCellValue(reportDTO.getPortedNNC());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(17);
					cell.setCellValue(reportDTO.getIsRoaming());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(18);
					cell.setCellValue(reportDTO.getRoamingCountry());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(19);
					cell.setCellValue(reportDTO.getRoamingOperator());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(20);
					cell.setCellValue(reportDTO.getRoamingNNC());
					cell.setCellStyle(rowStyle);
				}
				if (++row_number > records_per_sheet) {
					System.out.println("Sheet Created: " + sheet_number);
					break;
				}
			}
			sheet_number++;
		}
		System.out.println("<--- Workbook Created ----> ");
		return workbook;
	}

	private JasperPrint getJasperPrint(List reportList, boolean paging) throws Exception {
		JasperPrint print = null;
		JasperReport report = null;
		JasperDesign design = JRXmlLoader.load(template_file);
		report = JasperCompileManager.compileReport(design);
		reportList = sortList(reportList);
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		Map parameters = new HashMap();
		if (reportList.size() > 20000) {
			JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(1000,
					new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 2048, 1024));
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		}
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		return print;
	}

	private static List sortList(List list) {
		Comparator<LookupReport> comparator = null;
		comparator = Comparator.comparing(LookupReport::getUsername).thenComparing(LookupReport::getBatchId)
				.thenComparing(LookupReport::getHlrid);
		Stream<LookupReport> personStream = list.stream().sorted(comparator);
		List<LookupReport> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	private List<LookupReport> getLookupReport(CustomReportForm customReportForm, String role, String username)
			throws Exception {

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

		List<LookupReport> list = null;
		Map<String, String> params = new HashMap<String, String>();
		String batchid = null;
		if (customReportForm.getCheck_A() != null) // batch_id specified
		{
			if (!customReportForm.getMessageId().equals("%")) {
				batchid = customReportForm.getMessageId();
				params.put("batch_id", batchid);
			} else {
				if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
					if (customReportForm.getCheck_B() != null) // username specified
					{
						if (!customReportForm.getClientId().equalsIgnoreCase("%")) {
							params.put("username", customReportForm.getClientId());
						}
					}
				} else {
					params.put("username", customReportForm.getClientId());
				}
				params.put("batch_id", "ALL");
			}
		}
		if (batchid != null) {
		} else {
			if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
				if (customReportForm.getCheck_B() != null) // username specified
				{
					if (!customReportForm.getClientId().equalsIgnoreCase("%")) {
						params.put("username", customReportForm.getClientId());
					}
				}
			} else {
				params.put("username", customReportForm.getClientId());
			}
			if (customReportForm.getSday() != null && customReportForm.getEday() != null) {
				params.put("time", customReportForm.getSday());
				params.put("end", customReportForm.getEday());
			} else {
				params.put("time", new SimpleDateFormat("yyyy-MM-dd").format(new Date(0)));
				params.put("end", new SimpleDateFormat("yyyy-MM-dd").format(new Date(0)));
			}
		}
		String to_gmt = null;
		String from_gmt = null;
		if (!webMasterEntry.getGmt().equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = webMasterEntry.getGmt().replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
			params.put("to_gmt", to_gmt);
			params.put("from_gmt", from_gmt);
		}
		list = new LookupServiceInvoker().getLookupReport(params);
		return list;
	}

}
