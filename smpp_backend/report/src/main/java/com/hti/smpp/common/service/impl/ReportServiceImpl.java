package com.hti.smpp.common.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

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

		try {
			List<DeliveryDTO> reportList = dataBase.getReportList(customReportForm, username);

			if (reportList != null && !reportList.isEmpty()) {
				logger.info("{} ReportSize[View]: {}", username, reportList.size());
				// Uncomment the following lines if needed
				// JasperPrint print = dataBase.getJasperPrint(reportList, false, username);
				// session.setAttribute("blockedprint", print);
				// request.setAttribute("page", "1");

				logger.info("{} <-- Report Finished -->", username);
				target = IConstants.SUCCESS_KEY;
				return reportList;
			} else {
				throw new InternalServerException("No data found for the report");
			}
		} catch (Exception e) {
			logger.error("{} Unexpected error generating report", username, e);
			throw new InternalServerException("Unexpected error generating report" + e);
		}
	}

	@Override
	public String BlockedReportPdf(String username, CustomReportForm customReportForm, HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;

		try {
			List<DeliveryDTO> reportList = dataBase.getReportList(customReportForm, username);

			if (reportList != null && !reportList.isEmpty()) {
				logger.info("{} ReportSize[pdf]: {}", username, reportList.size());
				JasperPrint print = dataBase.getJasperPrint(reportList, false, username);

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

		return target;
	}

	@Override
	public String BlockedReportxls(String username, CustomReportForm customReportForm, HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;

		try {
			List<DeliveryDTO> reportList = dataBase.getReportList(customReportForm, username);

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

		return target;
	}

	@Override
	public String BlockedReportDoc(String username, CustomReportForm customReportForm, HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		try {
			List<DeliveryDTO> reportList = dataBase.getReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(username + " ReportSize[doc]:" + reportList.size());
				JasperPrint print = null;
				print = dataBase.getJasperPrint(reportList, false, username);
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

		return target;
	}

	@Override
	public JasperPrint CampaignReportview(String username, CustomReportForm customReportForm) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		try {
			JasperPrint reportList = dataBase.getCampaignReportList(customReportForm, username, false);
			if (reportList != null) {
				target = IConstants.SUCCESS_KEY;
				return reportList;

			} else {
				target = IConstants.FAILURE_KEY;
				throw new NotFoundException("");

			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException("Error:getting error in side the Campaign report " + e.getMessage());
		}
	}

	@Override
	public JasperPrint CampaignReportxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		JasperPrint reportList = null;
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		try {
			reportList = dataBase.getCampaignReportList(customReportForm, username, false);
			if (reportList != null) {
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "Campaign_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".xlsx";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				System.out.println("<-- Creating XLS --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRXlsxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, reportList);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
				exporter.setParameter(JRXlsExporterParameter.MAXIMUM_ROWS_PER_SHEET, 60000);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						System.out.println("XLS OutPutSream Closing Error");
					}
				}
				System.out.println("<-- Finished --> ");
				target = IConstants.SUCCESS_KEY;

			} else {
				target = IConstants.FAILURE_KEY;
				throw new NotFoundException("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reportList;

	}

	@Override
	public JasperPrint CampaignReportPdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		JasperPrint reportList = null;

		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		try {
			JasperPrint print = dataBase.getCampaignReportList(customReportForm, username, false);

			if (print != null) {
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "campaign_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".pdf";
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
					} catch (IOException ioe) {
						System.out.println("PDF OutPutSream Closing Error");
					}
				}
				System.out.println("<-- Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new InternalServerException("No data found for the report");
			}
		} catch (Exception e) {
			logger.error("{} Unexpected error generating PDF report", username, e);
			throw new InternalServerException("Unexpected error generating PDF report" + e.getMessage());
		}
		return reportList;

	}

	@Override
	public JasperPrint CampaignReportDoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		JasperPrint reportList = null;
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		try {
			JasperPrint print = dataBase.getCampaignReportList(customReportForm, username, false);

			if (print != null) {
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "Operator_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".doc";
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
				throw new InternalServerException("No data found for the report");

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reportList;
	}

	@Override
	public List<DeliveryDTO> ContentReportView(String username, CustomReportForm customReportForm) {
		String target = IConstants.FAILURE_KEY;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		try {
			List<DeliveryDTO> reportList = dataBase.getContentReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[View]:" + reportList.size());
				// JasperPrint print = null;
				JasperPrint print = dataBase.getJasperPrint(reportList, false, username);
				logger.info(user.getSystemId() + " <-- Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
				return reportList;

			} else {
				throw new InternalServerException("No data found for the report");

			}
		} catch (Exception e) {
			logger.error(user.getSystemId(), e.fillInStackTrace());
			return null;
		}

	}

	@Override
	public List<DeliveryDTO> ContentReportxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		try {
			List<DeliveryDTO> reportList = dataBase.getReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[doc]:" + reportList.size());
				// JasperPrint print = null;
				JasperPrint print = dataBase.getJasperPrint(reportList, false, username);
				logger.info(user.getSystemId() + " <-- Preparing Outputstream --> ");
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".doc";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info(user.getSystemId() + " <-- Creating DOC --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRDocxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						logger.info(user.getSystemId() + " DOC OutPutSream Closing Error");
					}
				}
				logger.info(user.getSystemId() + " <-- doc Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
//				message = new ActionMessage("error.record.unavailable");
			}
		} catch (Exception e) {
			logger.error(user.getSystemId(), e.fillInStackTrace());
		}
		return null;
	}

	@Override
	public List<DeliveryDTO> ContentReportPdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		try {
			List<DeliveryDTO> reportList = dataBase.getReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[pdf]:" + reportList.size());
				// JasperPrint print = null;
				JasperPrint print = dataBase.getJasperPrint(reportList, false, username);
				logger.info(user.getSystemId() + " <-- Preparing Outputstream --> ");
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".pdf";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info(user.getSystemId() + " <-- Creating PDF --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRPdfExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						logger.info(user.getSystemId() + " PDF OutPutSream Closing Error");
					}
				}
				logger.info(user.getSystemId() + " <-- pdf Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
//				message = new ActionMessage("error.record.unavailable");
			}
		} catch (Exception e) {
			logger.error(user.getSystemId(), e.fillInStackTrace());
		}
		return null;
	}

	@Override
	public List<DeliveryDTO> ContentReportDoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		try {
			List<DeliveryDTO> reportList = dataBase.getContentReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				int total_rec = reportList.size();
				logger.info(user.getSystemId() + " ReportSize[xls]:" + total_rec);
				// Workbook workbook = null;
				Workbook workbook = dataBase.getContentWorkBook(reportList, username);
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
					InputStream is = null;
					OutputStream out = null;
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
				target = IConstants.SUCCESS_KEY;
			} else {
				// message = new ActionMessage("error.record.unavailable");
				logger.info(user.getSystemId() + "<-- No Records Found --> ");
			}
		} catch (Exception e) {
			logger.error(user.getSystemId(), e.fillInStackTrace());
			// message = new ActionMessage("error.processError");
		}

		return null;
	}

}
