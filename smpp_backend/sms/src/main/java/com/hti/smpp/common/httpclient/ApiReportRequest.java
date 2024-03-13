package com.hti.smpp.common.httpclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
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

import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

public class ApiReportRequest extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(ApiReportRequest.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("DlrReport Request: " + request.getRemoteAddr() + " [ " + request.getMethod() + " ] "
				+ request.getQueryString());
		String username = request.getParameter("username");
		String pass = request.getParameter("pass");
		String accesscode = request.getParameter("accesskey");
		String from = request.getParameter("from_date");
		String to = request.getParameter("to_date");
		String sender = request.getParameter("sender");
		String msg = null;
		if (username == null || pass == null) {
			com.hazelcast.query.Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
					.get("provCode").equal(accesscode);
			if (accesscode != null && accesscode.length() > 0) {
				for (WebMasterEntry webEntryItr : GlobalVars.WebmasterEntries.values(p)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(webEntryItr.getUserId());
					username = userEntry.getSystemId();
					pass = userEntry.getPassword();
					break;
				}
			}
		}
		boolean isContent = false;
		if (request.getParameter("is_content") != null) {
			if (request.getParameter("is_content").equals("1")) {
				isContent = true;
			}
		}
		if (username == null || pass == null || from == null || to == null) {
			msg = IConstants.ERROR_HTTP02;
		} else {
			if (from.length() == 14 && to.length() == 14) {
				// WebMasterEntry webEntry = null;
				boolean webaccess = true;
				String gmt = null;
				try {
					String readFlag = MultiUtility.readFlag(Constants.USER_FLAG_DIR + username + ".txt");
					if (readFlag.contains("404")) {
						System.out.println("Http : " + username + " Blocked <404> ");
						webaccess = false;
					} else {
						UserService userService = new UserService();
						UserEntryExt entry = userService.getUserEntryExt(username);
						if (entry != null) {
							gmt = entry.getWebMasterEntry().getGmt();
							if (!entry.getWebMasterEntry().isWebAccess()) {
								webaccess = false;
								msg = IConstants.ERROR_HTTP15;
								System.out.println(username + " Access Denied :: " + msg);
							} else {
								if (!entry.getUserEntry().getPassword().equals(pass)) {
									System.out.println(username + " Invalid Password :: " + pass);
									msg = IConstants.ERROR_HTTP04;
									webaccess = false;
								} else {
									// webEntry = entry.getWebMasterEntry();
									// check ip
									if (entry.getUserEntry().getAccessIp() != null
											&& entry.getUserEntry().getAccessIp().length() > 0) {
										boolean matched = false;
										if (request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1")
												|| request.getRemoteAddr().equalsIgnoreCase("127.0.0.1")) {
											matched = true;
										} else {
											String allowed_list = entry.getUserEntry().getAccessIp();
											for (String allowedip : allowed_list.split(",")) {
												if (allowedip.indexOf("/") > 0) {
													if (isInRange(allowedip, request.getRemoteAddr())) {
														matched = true;
														break;
													}
												} else {
													if (request.getRemoteAddr().equalsIgnoreCase(allowedip)) {
														matched = true;
														break;
													}
												}
											}
										}
										if (!matched) {
											logger.error(username + " Invalid Http Access IPAddress: "
													+ request.getRemoteAddr());
											webaccess = false;
											msg = IConstants.ERROR_HTTP15;
											System.out.println(username + " Access Denied :: " + msg);
										}
									}
								}
							}
						} else {
							webaccess = false;
							msg = IConstants.ERROR_HTTP05;
							logger.info(username + " User Record Not Found");
						}
					}
					if (webaccess) {
						List<DeliveryDTO> list = getReportList(username, sender, from, to, isContent, gmt);
						if (!list.isEmpty()) {
							Workbook workbook = getWorkBook(list, username, true, isContent);
							if (list.size() > 100000) {
								logger.info(username + "<-- Creating Zip Folder --> ");
								response.setContentType("application/zip");
								response.setHeader("Content-Disposition", "attachment; filename=" + "delivery_"
										+ new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".zip");
								ZipOutputStream zos = new ZipOutputStream(response.getOutputStream()); // create a
																										// ZipOutputStream
																										// from
																										// servletOutputStream
								String reportName = "delivery.xlsx";
								ZipEntry entry = new ZipEntry(reportName); // create a zip entry and add it to
																			// ZipOutputStream
								zos.putNextEntry(entry);
								logger.info(username + "<-- Starting Zip Download --> ");
								workbook.write(zos);
								zos.close();
							} else {
								logger.info(username + " <---- Creating XLS -----> ");
								String filename = "delivery_"
										+ new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".xlsx";
								// response.setContentType("text/html; charset=utf-8");
								response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
								// response.setHeader("Content-Disposition", "attachment; filename=" +
								// filename);
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								workbook.write(bos);
								logger.info(username + " <---- Reading XLS -----> ");
								InputStream is = null;
								OutputStream out = null;
								try {
									is = new ByteArrayInputStream(bos.toByteArray());
									// byte[] buffer = new byte[8789];
									int curByte = -1;
									out = response.getOutputStream();
									logger.info(username + " <---- Starting xls Download -----> ");
									while ((curByte = is.read()) != -1) {
										out.write(curByte);
									}
									out.flush();
								} catch (Exception ex) {
									logger.error(username + " DLR XLSReport Error ", ex.fillInStackTrace());
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
						} else {
							msg = IConstants.ERROR_HTTP25;
							logger.info(username + " No Record Found");
						}
					}
				} catch (Exception ex) {
					logger.info(username, ex);
					msg = IConstants.ERROR_HTTP03;
				}
			} else {
				logger.info(username + " Invalid Date From:" + from + " To: " + to);
				msg = IConstants.ERROR_HTTP02;
			}
		}
		if (msg != null) {
			response.setContentType("text/plain");
			response.getWriter().print(msg);
			response.getWriter().flush();
			response.getWriter().close();
		}
	}

	private List<DeliveryDTO> getReportList(String username, String senderId, String startDate, String endDate,
			boolean isContent, String gmt) throws Exception {
		logger.info(username + " Creating Report list");
		List list = null;
		List final_list = new ArrayList();
		IDatabaseService dbService = new IDatabaseService();
		String query = null;
		String to_gmt = null, from_gmt = null;
		if (gmt != null && !gmt.equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = gmt.replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
		}
		if (to_gmt != null) {
			query = "select CONVERT_TZ(submitted_time,'" + from_gmt + "','" + to_gmt + "') as submitted_time,";
		} else {
			query = "select submitted_time,";
		}
		query += "msg_id,oprCountry,source_no,dest_no,cost,status,err_code,route_to_smsc,response_id,";
		if (to_gmt != null) {
			query += "CONVERT_TZ(deliver_time,'" + from_gmt + "','" + to_gmt + "') as deliver_time";
		} else {
			query += "deliver_time";
		}
		query += " from mis_" + username + " where ";
		if (senderId != null && senderId.trim().length() > 0) {
			if (senderId.contains("%")) {
				query += "source_no like \"" + senderId + "\" and ";
			} else {
				query += "source_no =\"" + senderId + "\" and ";
			}
		}
		if (to_gmt != null) {
			SimpleDateFormat client_formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			client_formatter.setTimeZone(TimeZone.getTimeZone(gmt));
			SimpleDateFormat local_formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			try {
				String start_msg_id = local_formatter.format(client_formatter.parse(startDate));
				String end_msg_id = local_formatter.format(client_formatter.parse(endDate));
				start_msg_id = start_msg_id.substring(2);
				start_msg_id += "0000000";
				end_msg_id = end_msg_id.substring(2);
				end_msg_id += "0000000";
				query += "msg_id between " + start_msg_id + " and " + end_msg_id;
			} catch (Exception e) {
				query += "submitted_time between CONVERT_TZ('" + startDate.substring(0, 4) + "-"
						+ startDate.substring(4, 6) + "-" + startDate.substring(6, 8) + " " + startDate.substring(8, 10)
						+ ":" + startDate.substring(10, 12) + ":" + startDate.substring(12, 14) + "','" + to_gmt + "','"
						+ from_gmt + "') and CONVERT_TZ('" + endDate.substring(0, 4) + "-" + endDate.substring(4, 6)
						+ "-" + endDate.substring(6, 8) + " " + endDate.substring(8, 10) + ":"
						+ endDate.substring(10, 12) + ":" + endDate.substring(12, 14) + "','" + to_gmt + "','"
						+ from_gmt + "')";
			}
		} else {
			if (startDate.equalsIgnoreCase(endDate)) {
				String start_msg_id = startDate.substring(2);
				query += "msg_id like '" + start_msg_id + "%'";
			} else {
				String start_msg_id = startDate.substring(2);
				start_msg_id += "0000000";
				String end_msg_id = endDate.substring(2);
				end_msg_id += "0000000";
				query += "msg_id between " + start_msg_id + " and " + end_msg_id + "";
			}
		}
		// query += " order by submitted_time DESC,oprCountry ASC,msg_id DESC";
		logger.debug(username + " ReportSQL:" + query);
		if (isContent) {
			Map map = dbService.getDlrReport(username, query, false);
			if (!map.isEmpty()) {
				try {
					list = dbService.getMessageContent(map, username);
				} catch (Exception ex) {
					list = new ArrayList(map.values());
				}
			}
		} else {
			list = (List) dbService.getCustomizedReport(username, query, false);
		}
		if (list != null && !list.isEmpty()) {
			// System.out.println(report_user + " Report List Size --> " + list.size());
			final_list.addAll(list);
			list.clear();
		}
		// check for unprocessed/Blocked/M/F entries
		String cross_unprocessed_query = "";
		if (to_gmt != null) {
			cross_unprocessed_query = "select CONVERT_TZ(time,'" + from_gmt + "','" + to_gmt + "') as time,";
		} else {
			cross_unprocessed_query = "select time,";
		}
		cross_unprocessed_query += "msg_id,username,oprCountry,source_no,destination_no,cost,s_flag,smsc";
		if (isContent) {
			cross_unprocessed_query += ",content,dcs";
		}
		cross_unprocessed_query += " from table_name where ";
		cross_unprocessed_query += "username ='" + username + "' and ";
		if (senderId != null && senderId.trim().length() > 0) {
			if (senderId.contains("%")) {
				cross_unprocessed_query += "source_no like \"" + senderId + "\" and ";
			} else {
				cross_unprocessed_query += "source_no =\"" + senderId + "\" and ";
			}
		}
		if (to_gmt != null) {
			SimpleDateFormat client_formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			client_formatter.setTimeZone(TimeZone.getTimeZone(gmt));
			SimpleDateFormat local_formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			try {
				String start_msg_id = local_formatter.format(client_formatter.parse(startDate));
				String end_msg_id = local_formatter.format(client_formatter.parse(endDate));
				start_msg_id = start_msg_id.substring(2);
				start_msg_id += "0000000";
				end_msg_id = end_msg_id.substring(2);
				end_msg_id += "0000000";
				cross_unprocessed_query += "msg_id between " + start_msg_id + " and " + end_msg_id;
			} catch (Exception e) {
				cross_unprocessed_query += "time between CONVERT_TZ('" + startDate.substring(0, 4) + "-"
						+ startDate.substring(4, 6) + "-" + startDate.substring(6, 8) + " " + startDate.substring(8, 10)
						+ ":" + startDate.substring(10, 12) + ":" + startDate.substring(12, 14) + "','" + to_gmt + "','"
						+ from_gmt + "') and CONVERT_TZ('" + endDate.substring(0, 4) + "-" + endDate.substring(4, 6)
						+ "-" + endDate.substring(6, 8) + " " + endDate.substring(8, 10) + ":"
						+ endDate.substring(10, 12) + ":" + endDate.substring(12, 14) + "','" + to_gmt + "','"
						+ from_gmt + "')";
			}
		} else {
			if (startDate.equalsIgnoreCase(endDate)) {
				String start_msg_id = startDate.substring(2);
				cross_unprocessed_query += "msg_id like '" + start_msg_id + "%'";
			} else {
				String start_msg_id = startDate.substring(2);
				start_msg_id += "0000000";
				String end_msg_id = endDate.substring(2);
				end_msg_id += "0000000";
				cross_unprocessed_query += "msg_id between " + start_msg_id + " and " + end_msg_id + "";
			}
		}
		List unproc_list = (List) dbService.getUnprocessedReport(
				cross_unprocessed_query.replaceAll("table_name", "unprocessed"), false, isContent);
		if (unproc_list != null && !unproc_list.isEmpty()) {
			final_list.addAll(unproc_list);
		}
		unproc_list = (List) dbService.getUnprocessedReport(cross_unprocessed_query.replaceAll("table_name", "smsc_in"),
				false, isContent);
		if (unproc_list != null && !unproc_list.isEmpty()) {
			final_list.addAll(unproc_list);
		}
		// logger.info(username + " ReportSQL: " + cross_unprocessed_query);
		// end check for unprocessed/Blocked/M/F entries
		logger.info(username + " End Based On Criteria. Final Report Size: " + final_list.size());
		return final_list;
	}

	private Workbook getWorkBook(List<DeliveryDTO> reportList, String username, boolean displayCost,
			boolean isContent) {
		logger.info(username + " <-- Creating WorkBook --> ");
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		int records_per_sheet = 500000;
		if (isContent) {
			records_per_sheet = 400000;
		}
		int sheet_number = 0;
		Sheet sheet = null;
		Row row = null;
		XSSFFont headerFont = (XSSFFont) workbook.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 128, (byte) 128, (byte) 128 }, null)); // Example
																													// for
																													// custom
																													// gray
																													// color
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBottomBorderColor(IndexedColors.WHITE.getIndex());
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setTopBorderColor(IndexedColors.WHITE.getIndex());
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setLeftBorderColor(IndexedColors.WHITE.getIndex());
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setRightBorderColor(IndexedColors.WHITE.getIndex());
		XSSFFont rowFont = (XSSFFont) workbook.createFont();
		rowFont.setFontName("Arial");
		rowFont.setFontHeightInPoints((short) 9);
		rowFont.setColor(IndexedColors.BLACK.getIndex());
		XSSFCellStyle rowStyle = (XSSFCellStyle) workbook.createCellStyle();
		rowStyle.setFont(rowFont);
		rowStyle.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 211, (byte) 211, (byte) 211 }, null));
		rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		rowStyle.setAlignment(HorizontalAlignment.LEFT);
		rowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBottomBorderColor(IndexedColors.WHITE.getIndex());
		rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setTopBorderColor(IndexedColors.WHITE.getIndex());
		rowStyle.setBorderLeft(BorderStyle.THIN);
		rowStyle.setLeftBorderColor(IndexedColors.WHITE.getIndex());
		rowStyle.setBorderRight(BorderStyle.THIN);
		rowStyle.setRightBorderColor(IndexedColors.WHITE.getIndex());
		String[] headers = { "Username", "MessageId", "SubmitOn", "Destination", "SenderId", "Country", "Operator",
				"DeliverOn", "Cost", "Status", "Content" };
		if (!displayCost) {
			headers = (String[]) ArrayUtils.remove(headers, 8);
		}
		if (!isContent) {
			if (headers.length == 11) {
				headers = (String[]) ArrayUtils.remove(headers, 10);
			} else {
				headers = (String[]) ArrayUtils.remove(headers, 9);
			}
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
					DeliveryDTO reportDTO = reportList.remove(0);
					Cell cell = row.createCell(0);
					cell.setCellValue(reportDTO.getUsername());
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
					if (displayCost) {
						cell = row.createCell(8);
						cell.setCellValue(reportDTO.getCost());
						cell.setCellStyle(rowStyle);
						cell = row.createCell(9);
						cell.setCellValue(reportDTO.getStatus());
						cell.setCellStyle(rowStyle);
						if (isContent) {
							cell = row.createCell(10);
							cell.setCellValue(reportDTO.getContent());
							cell.setCellStyle(rowStyle);
						}
					} else {
						cell = row.createCell(8);
						cell.setCellValue(reportDTO.getStatus());
						cell.setCellStyle(rowStyle);
						if (isContent) {
							cell = row.createCell(9);
							cell.setCellValue(reportDTO.getContent());
							cell.setCellStyle(rowStyle);
						}
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

	private boolean isInRange(String range, String requestip) {
		boolean inRange = false;
		String[] parts = range.split("/");
		String ip = parts[0];
		int prefix;
		if (parts.length < 2) {
			prefix = 0;
		} else {
			prefix = Integer.parseInt(parts[1]);
		}
		Inet4Address a = null;
		Inet4Address a1 = null;
		try {
			a = (Inet4Address) InetAddress.getByName(ip);
			a1 = (Inet4Address) InetAddress.getByName(requestip);
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException : " + e);
		}
		byte[] b = a.getAddress();
		int ipInt = ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | ((b[3] & 0xFF) << 0);
		byte[] b1 = a1.getAddress();
		int ipInt1 = ((b1[0] & 0xFF) << 24) | ((b1[1] & 0xFF) << 16) | ((b1[2] & 0xFF) << 8) | ((b1[3] & 0xFF) << 0);
		int mask = ~((1 << (32 - prefix)) - 1);
		if ((ipInt & mask) == (ipInt1 & mask)) {
			inRange = true;
		}
		return inRange;
	}
}
