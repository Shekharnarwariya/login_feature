package com.hti.smpp.common.service.impl;




import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.messages.dto.BulkMapEntry;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.CustomizedReportServices;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.UserSessionObject;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;

@Service
public class CustomizedReportServicesImpl implements CustomizedReportServices {

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
	Locale locale =null;
	
	
	String groupby = "country";
	@Override
	public List<DeliveryDTO> CustomizedReportView(String username, CustomReportForm customReportForm,String lang) {
		final String template_file = IConstants.FORMAT_DIR + "report//dlrReport.jrxml";
		final String template_sender_file = IConstants.FORMAT_DIR + "report//dlrReportSender.jrxml";
		final String template_content_file = IConstants.FORMAT_DIR + "report//dlrContentReport.jrxml";
		final String template_content_sender_file = IConstants.FORMAT_DIR + "report//dlrContentWithSender.jrxml";
		final String summary_template_file = IConstants.FORMAT_DIR + "report//dlrSummaryReport.jrxml";
		final String summary_sender_file = IConstants.FORMAT_DIR + "report//dlrSummarySender.jrxml";
		boolean isSummary = false;
		String reportUser = null;
		
		
		String groupby = "country";
		boolean isContent;
		// UserSessionObject userSessionObject = null;
		Logger logger = LoggerFactory.getLogger(CustomizedReportServicesImpl.class);
		String to_gmt;
		String from_gmt;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		
		
		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}

		String target = IConstants.SUCCESS_KEY;
		try {
			locale = Customlocale.getLocaleByLanguage(lang); ;
				
			List<DeliveryDTO> reportList = dataBase.getCustomizedReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(user.getSystemId() + " ReportSize[View]:" + reportList.size());
				JasperPrint print = null;
				if (isSummary) {
					print = dataBase.getSummaryJasperPrint(reportList, false, username);
				} else {
					print = dataBase.getCustomizedJasperPrint(reportList, false, username);
				}

				logger.info(user.getSystemId() + " <-- Report Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new InternalServerException("No data found for the report");

				// message = new ActionMessage("error.record.unavailable");
			}
		} catch (Exception e) {

			logger.error(user.getSystemId(), e.fillInStackTrace());
			// message = new ActionMessage("error.processError");
		}
		return null;
		
	}

	@Override
	public String CustomizedReportdoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang) {
	
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
				

		String target = IConstants.SUCCESS_KEY;
		try {
			locale = Customlocale.getLocaleByLanguage(lang); ;
			
			 
			List<DeliveryDTO> reportList = dataBase.getCustomizedReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				//String target = IConstants.SUCCESS_KEY;
				logger.info(user.getSystemId() + " ReportSize[doc]:" + reportList.size());
				JasperPrint print = null;
				if (isSummary) {
					print = dataBase.getSummaryJasperPrint(reportList, false, username);
				} else {
					print = dataBase.getCustomizedJasperPrint(reportList, false, username);
				}
				logger.info(user.getSystemId() + " <-- Preparing Outputstream --> ");
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".doc";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info(user.getSystemId() + " <-- Creating DOC --> ");
				ServletOutputStream out = response.getOutputStream();
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
				throw new InternalServerException("No data found for the report");
			}
		} catch (Exception e) {
			logger.error(user.getSystemId(), e.fillInStackTrace());
		}
		return target ;
	}

	@Override
	public String  CustomizedReportxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang) {
		

		String target = IConstants.SUCCESS_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		
		
		try {
			locale = Customlocale.getLocaleByLanguage(lang); ;
			
			
			List<DeliveryDTO> reportList =  dataBase.getCustomizedReportList(customReportForm,username);
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
						workbook = dataBase.getCustomizedSummaryWorkBook(reportList,username);
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
						workbook = dataBase.getCustomizedSummaryWorkBook(reportList,username);
					}
				} else {
					workbook = dataBase.getCustomizedWorkBook(reportList,username);
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
	public String CustomizedReportpdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang) {

		String target = IConstants.SUCCESS_KEY;
		
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

		UserEntry user = userOptional
				.orElseThrow(() -> new NotFoundException("User not found with the provided username."));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException("User does not have the required roles for this operation.");
		}
		
		
		
		try {
			locale = Customlocale.getLocaleByLanguage(lang); ;
			
			List<DeliveryDTO> reportList = dataBase.getCustomizedReportList(customReportForm,username);
			if (reportList != null && !reportList.isEmpty()) {
				logger.info(username + " ReportSize[pdf]:" + reportList.size());
				JasperPrint print = null;
				if (isSummary) {
					print = dataBase.getSummaryJasperPrint(reportList, false,username);
				} else {
					print = dataBase.getCustomizedJasperPrint(reportList, false,username);
				}
				logger.info(username + " <-- Preparing Outputstream --> ");
				String reportName = "delivery_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".pdf";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				logger.info(username + " <-- Creating PDF --> ");
				ServletOutputStream out = response.getOutputStream();
				JRPdfExporter exporter = new JRPdfExporter();
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
			} else {
				throw new InternalServerException("No data found for the report");
			}
		} catch (Exception e) {
			logger.error(username, e.fillInStackTrace());
			
		
		}
		
		return target;
	}




}
