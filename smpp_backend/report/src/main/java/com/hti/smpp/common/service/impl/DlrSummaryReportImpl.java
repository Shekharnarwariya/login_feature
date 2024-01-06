package com.hti.smpp.common.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.DlrSummaryReport;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.IConstants;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

@Service
public class DlrSummaryReportImpl implements DlrSummaryReport {

	private static final Logger logger = LoggerFactory.getLogger(DlrSummaryReportImpl.class);

	@Autowired
	private DataBase dataBase;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private SalesRepository salesRepository;

	@Autowired
	private UserDAService userService;

	@Override
	public List<DeliveryDTO> DlrSummaryReportview(String username, CustomReportForm customReportForm) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		String target = IConstants.SUCCESS_KEY;

		try {
			List<DeliveryDTO> reportList = dataBase.getDlrReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user + " ReportSize[View]:" + reportList.size());
				JasperPrint print = null;
				print = dataBase.getdlrSummaryJasperPrint(reportList, false, username);
				logger.info(username + " <-- Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new InternalServerException("No data found for the report");
			}
		} catch (Exception e) {
			logger.error(username, e.fillInStackTrace());
			// message = new ActionMessage("error.processError");

		}
		return null;

	}

	@Override
	public List<DeliveryDTO> DlrSummaryReportdoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		String target = IConstants.SUCCESS_KEY;

		try {
			List<DeliveryDTO> reportList = dataBase.getDlrReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(username + " ReportSize[doc]:" + reportList.size());
				JasperPrint print = null;
				print = dataBase.getdlrSummaryJasperPrint(reportList, false, username);
				logger.info(username + " <-- Preparing Outputstream --> ");
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".doc";
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

				throw new Exception("No data found for the report");
			}
		} catch (Exception e) {
			logger.error(username, e.fillInStackTrace());
		}
		return null;
	}

	@Override
	public List<DeliveryDTO> DlrSummaryReportdpdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;

		try {
			List<DeliveryDTO> reportList = dataBase.getDlrReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(username + " ReportSize[pdf]:" + reportList.size());
				JasperPrint print = null;
				print = dataBase.getdlrSummaryJasperPrint(reportList, false, username);
				logger.info(username + " <-- Preparing Outputstream --> ");
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".pdf";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info(username + " <-- Creating PDF --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRPdfExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						logger.info(username + " PDF OutPutSream Closing Error");
					}
				}
				logger.info(username + " <-- pdf Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {

				throw new Exception("error.record.unavailable");
			}
		} catch (Exception e) {
			logger.error(username, e.fillInStackTrace());
		}
		return null;
	}

	@Override
	public List<DeliveryDTO> DlrSummaryReportdxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		String target = IConstants.SUCCESS_KEY;

		try {
			List<DeliveryDTO> reportList = dataBase.getDlrReportList(customReportForm,username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(username + " ReportSize[xls]:" + reportList.size());
				JasperPrint print = null;
				print = dataBase.getdlrSummaryJasperPrint(reportList, false,username);
				logger.info(username + " <-- Preparing Outputstream --> ");
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".xlsx";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info(username + " <-- Creating XLSX --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRXlsxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
				exporter.setParameter(JRXlsExporterParameter.MAXIMUM_ROWS_PER_SHEET, 60000);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						logger.info(username + " XLSX OutPutSream Closing Error");
					}
				}
				logger.info(username + " <-- XLSX Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
//			message = new ActionMessage("error.record.unavailable");
			}
		} catch (Exception e) {
			logger.error(username, e.fillInStackTrace());
//		message = new ActionMessage("error.processError");
		}
		return null;
	}

}
