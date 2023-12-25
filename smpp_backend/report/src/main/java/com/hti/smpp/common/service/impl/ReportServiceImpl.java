package com.hti.smpp.common.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Autowired;

import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.service.ReportService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.IConstants;
import com.hti.webems.session.UserSessionObject;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

public class ReportServiceImpl implements ReportService {

	@Autowired
	private DataBase dataBase;

	@Autowired
	private UserEntryRepository userRepository;

	@Override
	public List<BulkEntry> abortBatchReport(String username, CustomReportForm customReportForm) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		String target = IConstants.SUCCESS_KEY;

		try {
			List<BulkEntry> reportList = dataBase.getReportList(customReportForm, user.getId());

			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				return reportList;
			} else {
				target = IConstants.FAILURE_KEY;
				throw new NotFoundException("Abort batch report not found for user: " + username);
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception ex) {
			target = IConstants.FAILURE_KEY;
			throw new InternalServerException("Error getting error in abort batch report: " + ex.getMessage());
		}
	}

	@Override
	public Map<String, List<DeliveryDTO>> BalanceReportView(String username, CustomReportForm customReportForm) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		try {
			Map<String, List<DeliveryDTO>> reportList = dataBase.getBalanceReportList(customReportForm, username);
			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				target = IConstants.SUCCESS_KEY;
				return reportList;
			} else {
				throw new NotFoundException("balance report not fount " + username);
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException("Error:getting error in side the balance report " + e.getMessage());
		}
	}

	@Override
	public String BalanceReportxls(String username, CustomReportForm customReportForm, HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;

		try {
			Map<String, List<DeliveryDTO>> reportList = dataBase.getReportListFile(username, customReportForm);
			if (!reportList.isEmpty()) {
				// log.info("Report Size: {}", reportList.size());
				JasperPrint print = dataBase.getJasperPrint(reportList, false);

				String reportName = "Consumption_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
						+ ".xlsx";
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");

				// log.info("Creating XLS");
				try (OutputStream out = response.getOutputStream()) {
					JRExporter exporter = new JRXlsxExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
					exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
					exporter.setParameter(JRXlsExporterParameter.MAXIMUM_ROWS_PER_SHEET, 60000);
					exporter.exportReport();
				} catch (IOException ioe) {
					// log.error("Error closing XLS OutputStream", ioe);
					throw new InternalServerException("Error closing XLS OutputStream" + ioe.getMessage());
				}

				// log.info("Finished");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new InternalServerException("Error: getting error in generating report.");
			}
		} catch (Exception e) {
			// log.error("Unexpected Exception: {}", e.getMessage(), e);
			throw new InternalServerException("Unexpected error occurred");
		}

		return target;
	}

	@Override
	public String balanceReportPdf(String username, CustomReportForm customReportForm, HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		try {
			Map<String, List<DeliveryDTO>> reportList = dataBase.getReportListFile(username, customReportForm);

			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());

				JasperPrint print = dataBase.getJasperPrint(reportList, false);
				System.out.println("<-- Preparing OutputStream --> ");

				String reportName = "Consumption_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
						+ ".pdf";

				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");

				System.out.println("<-- Creating PDF --> ");

				try (OutputStream out = response.getOutputStream()) {
					JRExporter exporter = new JRPdfExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
					exporter.exportReport();
				}

				System.out.println("<-- Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new InternalServerException("Error generating report PDF file in balance");
			}
		} catch (Exception e) {
			throw new InternalServerException("Error: " + e.getMessage());
		}

		return target;
	}

	@Override
	public String BalanceReportDoc(String username, CustomReportForm customReportForm, HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		try {
			Map<String, List<DeliveryDTO>> reportList = dataBase.getReportListFile(username, customReportForm);
			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = dataBase.getJasperPrint(reportList, false);
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "Consumption_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
						+ ".doc";
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
					} catch (IOException ioe) {
						System.out.println("DOC OutPutSream Closing Error");
					}
				}
				System.out.println("<-- Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new InternalServerException("Error generating report Doc file in balance");
			}
		} catch (Exception e) {
			throw new InternalServerException("Error: " + e.getMessage());
		}
		return target;
	}

	@Override
	public List<DeliveryDTO> BlockedReportView(String username, CustomReportForm customReportForm) {
		String target = IConstants.FAILURE_KEY;
		HttpSession session = request.getSession(false);
		userSessionObject = (UserSessionObject) session.getAttribute(IConstants.USER_SESSION_KEY);
		ActionMessages messages = new ActionMessages();
		ActionMessage message = null;
		CustomReportForm customReportForm = (CustomReportForm) actionForm;
		try {
			locale = (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY);
			List<DeliveryDTO> reportList = getReportList(customReportForm);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(userSessionObject.getSystemId() + " ReportSize[View]:" + reportList.size());
				JasperPrint print = null;
				print = getJasperPrint(reportList, false);
				session.setAttribute("blockedprint", print);
				request.setAttribute("page", "1");
				logger.info(userSessionObject.getSystemId() + " <-- Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				message = new ActionMessage("error.record.unavailable");
			}
		} catch (Exception e) {
			logger.error(userSessionObject.getSystemId(), e.fillInStackTrace());
			message = new ActionMessage("error.processError");
		}
		messages.add(ActionMessages.GLOBAL_MESSAGE, message);
		saveMessages(request, messages);
		return mapping.findForward(target);
	}

	@Override
	public String BlockedReportxls(String username, CustomReportForm customReportForm, HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		HttpSession session = request.getSession(false);
		userSessionObject = (UserSessionObject) session.getAttribute(IConstants.USER_SESSION_KEY);
		ActionMessages messages = new ActionMessages();
		ActionMessage message = null;
		CustomReportForm customReportForm = (CustomReportForm) actionForm;
		try {
			locale = (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY);
			List<DeliveryDTO> reportList = getReportList(customReportForm);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(userSessionObject.getSystemId() + " ReportSize[pdf]:" + reportList.size());
				JasperPrint print = null;
				print = getJasperPrint(reportList, false);
				logger.info(userSessionObject.getSystemId() + " <-- Preparing Outputstream --> ");
				String reportName = "blocked_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".pdf";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info(userSessionObject.getSystemId() + " <-- Creating PDF --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRPdfExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						logger.info(userSessionObject.getSystemId() + " PDF OutPutSream Closing Error");
					}
				}
				logger.info(userSessionObject.getSystemId() + " <-- pdf Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				message = new ActionMessage("error.record.unavailable");
			}
		} catch (Exception e) {
			logger.error(userSessionObject.getSystemId(), e.fillInStackTrace());
			message = new ActionMessage("error.processError");
		}
		messages.add(ActionMessages.GLOBAL_MESSAGE, message);
		saveMessages(request, messages);
		return mapping.findForward(target)
	}

	@Override
	public String BlockedReportPdf(String username, CustomReportForm customReportForm, HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		HttpSession session = request.getSession(false);
		userSessionObject = (UserSessionObject) session.getAttribute(IConstants.USER_SESSION_KEY);
		ActionMessages messages = new ActionMessages();
		ActionMessage message = null;
		CustomReportForm customReportForm = (CustomReportForm) actionForm;
		try {
			locale = (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY);
			List<DeliveryDTO> reportList = getReportList(customReportForm);
			if (reportList != null && !reportList.isEmpty()) {
				int total_rec = reportList.size();
				logger.info(userSessionObject.getSystemId() + " ReportSize[xls]:" + total_rec);
				Workbook workbook = null;
				workbook = getWorkBook(reportList);
				if (total_rec > 100000) {
					logger.info(userSessionObject.getSystemId() + "<-- Creating Zip Folder --> ");
					response.setContentType("application/zip");
					response.setHeader("Content-Disposition", "attachment; filename=" + "blocked_"
							+ new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".zip");
					ZipOutputStream zos = new ZipOutputStream(response.getOutputStream()); // create a ZipOutputStream from servletOutputStream
					String reportName = "blocked.xlsx";
					ZipEntry entry = new ZipEntry(reportName); // create a zip entry and add it to ZipOutputStream
					zos.putNextEntry(entry);
					logger.info(userSessionObject.getSystemId() + "<-- Starting Zip Download --> ");
					workbook.write(zos);
					zos.close();
				} else {
					logger.info(userSessionObject.getSystemId() + " <---- Creating XLS -----> ");
					String filename = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
							+ ".xlsx";
					// response.setContentType("text/html; charset=utf-8");
					response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
					// response.setHeader("Content-Disposition", "attachment; filename=" + filename);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					workbook.write(bos);
					logger.info(userSessionObject.getSystemId() + " <---- Reading XLS -----> ");
					InputStream is = null;
					OutputStream out = null;
					try {
						is = new ByteArrayInputStream(bos.toByteArray());
						// byte[] buffer = new byte[8789];
						int curByte = -1;
						out = response.getOutputStream();
						logger.info(userSessionObject.getSystemId() + " <---- Starting xls Download -----> ");
						while ((curByte = is.read()) != -1) {
							out.write(curByte);
						}
						out.flush();
					} catch (Exception ex) {
						logger.error(userSessionObject.getSystemId() + " DLR XLSReport Error ", ex.fillInStackTrace());
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
				logger.info(userSessionObject.getSystemId() + "<--XLS Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				message = new ActionMessage("error.record.unavailable");
				logger.info(userSessionObject.getSystemId() + "<-- No Records Found --> ");
			}
		} catch (Exception e) {
			logger.error(userSessionObject.getSystemId(), e.fillInStackTrace());
			message = new ActionMessage("error.processError");
		}
		messages.add(ActionMessages.GLOBAL_MESSAGE, message);
		saveMessages(request, messages);
		return mapping.findForward(target);
	}

	@Override
	public String BlockedReportDoc(String username, CustomReportForm customReportForm, HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		HttpSession session = request.getSession(false);
		userSessionObject = (UserSessionObject) session.getAttribute(IConstants.USER_SESSION_KEY);
		ActionMessages messages = new ActionMessages();
		ActionMessage message = null;
		CustomReportForm customReportForm = (CustomReportForm) actionForm;
		try {
			locale = (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY);
			List<DeliveryDTO> reportList = getReportList(customReportForm);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(userSessionObject.getSystemId() + " ReportSize[doc]:" + reportList.size());
				JasperPrint print = null;
				print = getJasperPrint(reportList, false);
				logger.info(userSessionObject.getSystemId() + " <-- Preparing Outputstream --> ");
				String reportName = "blocked_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".doc";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info(userSessionObject.getSystemId() + " <-- Creating DOC --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRDocxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						logger.info(userSessionObject.getSystemId() + " DOC OutPutSream Closing Error");
					}
				}
				logger.info(userSessionObject.getSystemId() + " <-- doc Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				message = new ActionMessage("error.record.unavailable");
			}
		} catch (Exception e) {
			logger.error(userSessionObject.getSystemId(), e.fillInStackTrace());
			message = new ActionMessage("error.processError");
		}
		messages.add(ActionMessages.GLOBAL_MESSAGE, message);
		saveMessages(request, messages);
		return mapping.findForward(target);
	}

}
