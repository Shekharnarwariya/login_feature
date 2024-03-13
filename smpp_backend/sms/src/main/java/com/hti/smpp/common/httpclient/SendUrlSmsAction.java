/* 
 **	Copyright 2004 High Tech InfoSystems. All Rights Reserved.
 **	Author		: Satya Prakash [satyaprakash@utils.net]
 **	Created on 	: 12/05/2004
 **	Modified on	: 04/07/2008 by sanjeev
 **	Descritpion	:
 */
package com.hti.smpp.common.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.service.impl.SmsServiceImpl;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.Converter;
import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;
import com.hti.smpp.common.util.WriteLogThread;

public class SendUrlSmsAction extends HttpServlet {
	// private static long counter = 0;
	private String webresp = null;
	private Logger logger = LoggerFactory.getLogger(SendUrlSmsAction.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		webresp = new SimpleDateFormat("ddMMyyHHmmssSSS").format(new Date())
				+ (new Random().nextInt(9999 - 1000) + 1000);
		createURLRequest(request, response);
	}

	private void createURLRequest(HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException, ServletException, IOException {
		String decodeUrl = URLDecoder.decode(request.getQueryString(), "UTF-8");
		request.setAttribute("user", request.getParameter("user"));
		request.setAttribute("pass", request.getParameter("pass"));
		request.setAttribute("sid", request.getParameter("sid"));
		request.setAttribute("mno", request.getParameter("mno"));
		request.setAttribute("text", request.getParameter("text"));
		request.setAttribute("schtime", request.getParameter("schtime"));
		request.setAttribute("gmt", request.getParameter("gmt"));
		request.setAttribute("type", request.getParameter("type"));
		request.setAttribute("routes", request.getParameter("routes"));
		request.setAttribute("header", request.getParameter("header"));
		request.setAttribute("respformat", request.getParameter("respformat"));
		request.setAttribute("peid", request.getParameter("peid"));
		request.setAttribute("templateid", request.getParameter("templateid"));
		request.setAttribute("tmid", request.getParameter("tmid"));
		request.setAttribute("accesskey", request.getParameter("accesskey"));
		request.setAttribute("resp", request.getParameter("resp"));
		request.setAttribute("stat", request.getParameter("stat"));

		WriteLogThread.logQueue.enqueue(
				"( " + request.getRemoteAddr() + " ) " + request.getMethod() + "[" + webresp + "]: " + decodeUrl);
		HttpRequestLog.logQueue.enqueue(new HttpRequestEntry(request.getRemoteAddr(),
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), decodeUrl, request.getMethod()));
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		webresp = new SimpleDateFormat("ddMMyyHHmmssSSS").format(new Date())
				+ (new Random().nextInt(9999 - 1000) + 1000);
		if (request.getQueryString() != null) {
			createURLRequest(request, response);
		} else {
			boolean proceed = true;
			StringBuilder sbf = new StringBuilder();
			InputStream is = null;
			BufferedReader br = null;
			try {
				is = request.getInputStream();
				if (is != null) {
					br = new BufferedReader(new InputStreamReader(is));
					String strLine = null;
					while ((strLine = br.readLine()) != null) {
						sbf.append(strLine);
					}
				}
			} catch (IOException ioe) {
				System.out.println("HTTP POST Error[" + sbf.toString() + "]: " + ioe);
				response.getWriter().println("Error: " + ioe);
				proceed = false;
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException ioe) {
						br = null;
					}
				}
			}
			if (proceed) {
				String decodeUrl = URLDecoder.decode(sbf.toString(), "UTF-8");
				WriteLogThread.logQueue
						.enqueue("( " + request.getRemoteAddr() + " ) POST[" + webresp + "]: " + decodeUrl);
				HttpRequestLog.logQueue.enqueue(new HttpRequestEntry(request.getRemoteAddr(),
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), decodeUrl,
						request.getMethod()));
				StringTokenizer tokens = new StringTokenizer(decodeUrl, "&");
				while (tokens.hasMoreTokens()) {
					String token = tokens.nextToken();
					if (token.contains("=")) {
						String param = token.substring(0, token.indexOf("="));
						String value = token.substring(token.indexOf("=") + 1);
						request.setAttribute(param, value);
					}
				}
				processRequest(request, response);
			}
		}
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String user = null, pass = null, sid = null, mno = null, text = null, schtime = null, gmt = null, routes = null,
				respformat = null;
		int type = 0;// esm = 0, dcs = 0;
		String strType = null;
		boolean webaccess = true, proceed = true, resp = false;
		String header = null;
		String msg = null;
		boolean invalidAccessAlert = false;
		WebMasterEntry webEntry = null;
		// PrintWriter out = response.getWriter();
		user = (String) request.getAttribute("user");
		pass = (String) request.getAttribute("pass");
		sid = (String) request.getAttribute("sid");
		mno = (String) request.getAttribute("mno");
		strType = (String) request.getAttribute("type");
		text = (String) request.getAttribute("text");
		schtime = (String) request.getAttribute("schtime");
		gmt = (String) request.getAttribute("gmt");
		routes = (String) request.getAttribute("routes");
		header = (String) request.getAttribute("header");
		respformat = (String) request.getAttribute("respformat");
		String peid = (String) request.getAttribute("peid");
		String templateid = (String) request.getAttribute("templateid");
		String tmid = (String) request.getAttribute("tmid");
		String accesscode = (String) request.getAttribute("accesskey");
		String isResp = (String) request.getAttribute("resp");
		String stat = (String) request.getAttribute("stat");
		boolean isStatus = false;
		if (stat != null && stat.equals("1")) {
			isStatus = true;
		}
		if (isResp != null && isResp.equalsIgnoreCase("true")) {
			resp = true;
		}
		System.out.println(request.getMethod() + "<" + request.getRemoteAddr() + "> Http user: " + user + " sid: " + sid
				+ " mno: " + mno + " Type:" + strType + " text:" + text + " respformat:" + respformat);
		if (routes != null) {
			logger.info("Coverage Test Routes: " + routes);
		}
		if (user == null || pass == null) {
			if (accesscode != null && accesscode.length() > 0) {
				com.hazelcast.query.Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
						.get("provCode").equal(accesscode);
				for (WebMasterEntry webEntryItr : GlobalVars.WebmasterEntries.values(p)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(webEntryItr.getUserId());
					user = userEntry.getSystemId();
					pass = userEntry.getPassword();
					break;
				}
			}
		}
		if (user == null || pass == null || sid == null || mno == null || text == null) {
			msg = IConstants.ERROR_HTTP02;
		} else {
			if (user.length() == 0 || user.length() > 16) {
				msg = IConstants.ERROR_HTTP05;
			} else {
				// System.out.println(request.getMethod() + " Http User : " + user + " From : "
				// + request.getRemoteAddr() + " Sender : " + sid + "
				// Type : " + strType);
				UserEntryExt entry = null;
				try {
					String readFlag = MultiUtility.readFlag(Constants.USER_FLAG_DIR + user + ".txt");
					// System.out.println("Http : " + user + " Flag -> " + readFlag);
					if (readFlag.contains("404")) {
						System.out.println("Http : " + user + " Blocked <404> ");
						webaccess = false;
					} else {
						UserService userService = new UserService();
						entry = userService.getUserEntryExt(user);
						if (entry != null) {
							if (!entry.getWebMasterEntry().isApiAccess()) {
								webaccess = false;
							} else {
								webEntry = entry.getWebMasterEntry();
								// check ip
								if (webEntry.isApiKeyOnly()) {
									if (accesscode != null && accesscode.length() == 15) {
										if (!accesscode.equalsIgnoreCase(webEntry.getProvCode())) {
											logger.error(user + "[" + request.getRemoteAddr() + "] Invalid AccessKey: "
													+ accesscode);
											webaccess = false;
										}
									} else {
										logger.error(user + "[" + request.getRemoteAddr() + "] AccessKey Required.");
										webaccess = false;
									}
								}
								if (webaccess) {
									if (entry.getUserEntry().getAccessIp() != null
											&& entry.getUserEntry().getAccessIp().length() > 0) {
										boolean matched = false;
										if (request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1")
												|| request.getRemoteAddr().equalsIgnoreCase("127.0.0.1")) {
											matched = true;
										} else {
											String allowed_list = entry.getUserEntry().getAccessIp();
											StringTokenizer st = new StringTokenizer(allowed_list, ",");
											while (st.hasMoreTokens()) {
												String allowedip = st.nextToken();
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
											logger.error(user + " Invalid Http Access IPAddress: "
													+ request.getRemoteAddr());
											webaccess = false;
										}
									} else {
										if (entry.getUserEntry().getAccessCountry() != null
												&& entry.getUserEntry().getAccessCountry().length() > 0) {
											boolean matched = false;
											if (request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1")
													|| request.getRemoteAddr().equalsIgnoreCase("127.0.0.1")) {
												matched = true;
											} else {
												String country = new LoginService()
														.getCountryname(request.getRemoteAddr());
												if (country != null) {
													for (String allowedCountry : entry.getUserEntry().getAccessCountry()
															.split(",")) {
														if (allowedCountry.equalsIgnoreCase(country)) {
															matched = true;
															break;
														}
													}
												} else {
													logger.info(user + " Country [" + request.getRemoteAddr()
															+ "] not found in database.");
												}
											}
											if (!matched) {
												logger.error(user + " Invalid Http Access IPAddress: "
														+ request.getRemoteAddr());
												webaccess = false;
											}
										}
									}
									if (!webaccess) {
										if (webEntry.isEmailOnLogin()) {
											invalidAccessAlert = true;
										}
									}
								}
							}
						} else {
							webaccess = false;
							logger.info(user + " User Record Not Found");
						}
					}
					if (!webaccess) {
						msg = IConstants.ERROR_HTTP15;
						System.out.println(user + " Access Denied :: " + msg);
					} else {
						if (pass.length() == 0) {
							proceed = false;
							msg = IConstants.ERROR_HTTP04;
						} else {
							boolean isPasswordExpired = false;
							if (entry.getUserEntry().isForcePasswordChange()) {
								try {
									isPasswordExpired = new SimpleDateFormat("yyyy-MM-dd")
											.parse(entry.getUserEntry().getPasswordExpiresOn())
											.before(new java.util.Date());
								} catch (ParseException ex) {
									logger.error(user, ex.fillInStackTrace());
								}
							}
							if (isPasswordExpired) {
								logger.info(user + " Password Expired");
								proceed = false;
								msg = IConstants.ERROR_HTTP28;
							}
						}
						if (proceed) {
							if (sid.length() == 0) {
								proceed = false;
								msg = IConstants.ERROR_HTTP06;
							} else if (mno.length() == 0) {
								System.out.println(user + " Invalid Destination Length :--> " + mno.length());
								proceed = false;
								msg = IConstants.ERROR_HTTP09;
							} else if (text.length() == 0) {
								proceed = false;
								msg = IConstants.ERROR_HTTP17;
							} else {
								// ******** Checking for Message Type **************
								if (strType == null || strType.length() == 0) {
									if (!Pattern.compile(
											"^[A-Za-z0-9 \\r\\n@�$�����������\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u00A4\u03A3\u0398\u039E����!\"#$%&'()*+,\\-./:;<=>?����ܧ������^{}\\\\\\[~\\]|\u20AC]*$")
											.matcher(text).find()) {
										type = 4;
										System.out.println(user + " auto detect type: " + type);
									} else {
										int msg_length = text.length();
										if (msg_length % 4 == 0) {
											try {
												String original = new Converters()
														.UTF16(Converter.getUnicode(text.toCharArray()));
												if (text.equalsIgnoreCase(original)) {
													type = 2;
													System.out.println(user + " auto detect type: " + type);
												} else {
													type = 1;
													System.out.println(user + " auto detect type: " + type);
												}
											} catch (Exception e) {
												type = 1;
												System.out
														.println(user + " auto detect type due to exception: " + type);
											}
										} else {
											type = 1;
											System.out.println(user + " auto detect type: " + type);
										}
									}
								} else {
									try {
										type = Integer.parseInt(strType);
										if (type < 1 || type > 4) {
											proceed = false;
											msg = IConstants.ERROR_HTTP07;
										} else {
											if (type == 1 || type == 3) {
												if (!Pattern.compile(
														"^[A-Za-z0-9 \\r\\n@�$�����������\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u00A4\u03A3\u0398\u039E����!\"#$%&'()*+,\\-./:;<=>?����ܧ������^{}\\\\\\[~\\]|\u20AC]*$")
														.matcher(text).find()) {
													proceed = false;
													msg = IConstants.ERROR_HTTP07;
													System.out.println(user + " message & type mismatched");
												}
											}
										}
									} catch (NumberFormatException ne) {
										proceed = false;
										msg = IConstants.ERROR_HTTP07;
									}
								}
							}
						}
						if (proceed) {
							if ((webEntry != null && webEntry.getSenderId() != null
									&& webEntry.getSenderId().length() > 0)
									&& (webEntry.getSenderRestrictTo().equalsIgnoreCase("ALL")
											|| webEntry.getSenderRestrictTo().equalsIgnoreCase("API"))) {
								if (webEntry.getSenderId().toLowerCase().contains(sid.toLowerCase())) {
									System.out.println(user + " Allowed SenderId: " + sid);
								} else {
									System.out.println(user + " SenderId Not Allowed: " + sid);
									proceed = false;
									msg = IConstants.ERROR_HTTP06;
								}
							}
							if (proceed) {
								// ********* Creating Number List ************
								ArrayList destinationList = null;
								if (mno.length() > 0) {
									destinationList = new ArrayList();
									try {
										StringTokenizer stoken = new StringTokenizer(mno, ",");
										String number = "";
										while (stoken.hasMoreTokens()) {
											number = stoken.nextToken();
											number = number.replaceAll("\\s+", ""); // Replace all the spaces in the
																					// String with empty character.
											number = number.substring(number.lastIndexOf("+") + 1); // Remove +
											try {
												long long_num = Long.parseLong(number.trim());
												number = String.valueOf(long_num);
												if (webEntry != null && webEntry.isPrefixApply()) {
													if (number.length() < webEntry.getNumberLength()) {
														System.out.println(number + " length is less then "
																+ webEntry.getNumberLength());
														number = webEntry.getPrefixToApply() + number;
													}
												}
												// System.out.println("final number: " + number);
												destinationList.add(number);
											} catch (NumberFormatException NFE) {
												System.out.println("Invalid Destination: " + number);
											}
										}
										if (destinationList.isEmpty()) {
											proceed = false;
											msg = IConstants.ERROR_HTTP09;
											System.out.println("No valid Destination Found :--> " + mno);
										} else if (destinationList.size() > 10000) {
											proceed = false;
											msg = IConstants.ERROR_HTTP24;
										}
									} catch (Exception ex) {
										proceed = false;
										msg = IConstants.ERROR_HTTP09;
										System.out.println(ex + " while validating Destination :--> " + mno);
										ex.printStackTrace();
									}
								}
								if (proceed) {
									boolean isNumeric = true;
									// ************* Checking For Sender Id ******************
									if (sid.startsWith("+")) {
										sid = sid.substring(1, sid.length());
									}
									try {
										Long.parseLong(sid.trim());
									} catch (NumberFormatException ne) {
										isNumeric = false;
									}
									// ************* Message Type **********
									String messageType = null;
									if (type == 1) {
										messageType = "SpecialChar";
									} else if (type == 2) {
										messageType = "Unicode";
									} else if (type == 3) {
										messageType = "SpecialChar";
									} else if (type == 4) {
										messageType = "Arabic";
									}
									// ********* Creating DTO **************
									BulkSmsDTO bulksmsDTO = new BulkSmsDTO();
									if (isNumeric) {
										bulksmsDTO.setFrom("Mobile");
									} else {
										bulksmsDTO.setFrom("Name");
									}
									bulksmsDTO.setSystemId(user);
									bulksmsDTO.setPassword(pass);
									bulksmsDTO.setSenderId(sid);
									bulksmsDTO.setDestinationList(destinationList);
									bulksmsDTO.setMessageType(messageType);
									bulksmsDTO.setMessage(text);
									bulksmsDTO.setOrigMessage(text);
									bulksmsDTO.setPeId(peid);
									bulksmsDTO.setTemplateId(templateid);
									bulksmsDTO.setTelemarketerId(tmid);
									// bulksmsDTO.setCount((int) counter);
									// bulksmsDTO.setDcsValue(strDcs.trim());
									// bulksmsDTO.setEsmClass(strEsm.trim());
									Client client = new Client();
									if (schtime == null) {
										if (routes != null) {
											String[] route_arr = routes.split(",");
											bulksmsDTO.setSmscList(route_arr);
											msg = client.sendCoverageURL(bulksmsDTO);
										} else {
											String dlr_param = null;
											if (entry.getDlrSettingEntry().getWebDlrParam() != null
													&& entry.getDlrSettingEntry().getWebDlrParam().length() > 0) {
												String dlr_param_value = (String) request
														.getAttribute(entry.getDlrSettingEntry().getWebDlrParam());
												if (dlr_param_value == null) {
													dlr_param_value = (String) request
															.getParameter(entry.getDlrSettingEntry().getWebDlrParam());
												}
												if (dlr_param_value != null) {
													dlr_param = entry.getDlrSettingEntry().getWebDlrParam() + "="
															+ dlr_param_value;
												}
											}
											msg = client.sendURL(bulksmsDTO, dlr_param);
											if (isStatus) {
												List<String> status_list = new ArrayList<String>();
												for (String resp_part : msg.split("\n")) {
													if (!resp_part.toLowerCase().contains("error")) {
														status_list.add(resp_part + ";ACCEPTD");
													} else {
														status_list.add(resp_part);
													}
												}
												if (!status_list.isEmpty()) {
													msg = String.join("\n", status_list);
												}
											}
										}
									} else {
										// ******* Scheduling **************
										if (schtime.trim().length() == 12) {
											int compare = 0;
											if (gmt == null) {
												try {
													Date givenDate = new SimpleDateFormat("yyyyMMddhhmm")
															.parse(schtime);
													compare = givenDate.compareTo(new Date());
												} catch (ParseException ex) {
													ex.printStackTrace();
												}
											} else {
												compare = 1;
											}
											if (compare > 0) {
												String webId = new SimpleDateFormat("ddMMyyHHmmssSSS").format(
														new Date()) + (new Random().nextInt(9999 - 1000) + 1000);
												// -------------------------------------
												bulksmsDTO.setRepeat("no");
												bulksmsDTO.setReqType("http");
												bulksmsDTO.setDelay(0);
												String year = schtime.substring(0, 4);
												String month = schtime.substring(4, 6);
												String day = schtime.substring(6, 8);
												String hour = schtime.substring(8, 10);
												String minute = schtime.substring(10, 12);
												if (gmt != null) {
													if (gmt.startsWith("-")) {
														gmt = "GMT" + gmt;
													} else {
														gmt = "GMT+" + gmt.trim();
													}
												} else {
													gmt = IConstants.DEFAULT_GMT;
												}
												bulksmsDTO.setGmt(gmt);
												bulksmsDTO.setTimestart(day + "-" + month + "-" + year + " " + hour
														+ ":" + minute + ":00");
												String client_time = bulksmsDTO.getTimestart();
												String client_gmt = bulksmsDTO.getGmt();
												SimpleDateFormat client_formatter = new SimpleDateFormat(
														"dd-MM-yyyy HH:mm:ss");
												client_formatter.setTimeZone(TimeZone.getTimeZone(client_gmt));
												SimpleDateFormat local_formatter = new SimpleDateFormat(
														"dd-MM-yyyy HH:mm:ss");
												String schedule_time = null;
												boolean valid_sch_time = false;
												try {
													schedule_time = local_formatter
															.format(client_formatter.parse(client_time));
													System.out.println(
															webId + " client_gmt: " + client_gmt + " client_time: "
																	+ client_time + " server_time: " + schedule_time);
													if (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(schedule_time)
															.after(new Date())) {
														valid_sch_time = true;
													} else {
														logger.error(webId + " Scheduled Time is before Current Time");
													}
													String server_date = schedule_time.split(" ")[0];
													String server_time = schedule_time.split(" ")[1];
													bulksmsDTO.setDate(
															server_date.split("-")[2] + "-" + server_date.split("-")[1]
																	+ "-" + server_date.split("-")[0]);
													bulksmsDTO.setTime(
															server_time.split(":")[0] + "" + server_time.split(":")[1]);
												} catch (Exception e) {
													logger.error(webId, e);
												}
												// -------------------------------------
												if (valid_sch_time) {
													msg = client.scheduleURL(bulksmsDTO, webId);
													if (resp) {
														if (msg.startsWith("Total Message Scheduled")) {
															msg = "ScheduleId: " + webId;
														}
													}
												} else {
													msg = IConstants.ERROR_HTTP22;
													System.out.println("Invalid Scheduled Time Format :" + schtime);
												}
											} else {
												msg = IConstants.ERROR_HTTP22;
												System.out.println("Invalid Scheduled Time Format :" + schtime);
											}
										} else {
											msg = IConstants.ERROR_HTTP22;
											System.out.println("Invalid Scheduled Time Format :" + schtime);
										}
									}
								}
							}
						}
					}
				} catch (Exception ex) {
					logger.info(user, ex);
					msg = IConstants.ERROR_HTTP03;
				}
			}
		}
		if (header != null && header.equals("1")) {
			// System.err.println(msg);
			String temp_msg = msg.replaceAll("\n", "<br>");
			// System.err.println(temp_msg);
			msg = "<html><head><meta charset='UTF-8'/><meta http-equiv='Cache-Control' content='max-age=86400'/></head><body><b>Response: </b>"
					+ temp_msg + "</body></html>";
		} else {
			if (respformat != null && respformat.equalsIgnoreCase("json")) {
				Map<String, List<String>> map = new HashMap<String, List<String>>();
				map.put("Response", Arrays.asList(msg.split("\n")));
				msg = new JSONObject(map).toString();
			} else {
				msg = "Response: " + msg;
			}
		}
		response.getWriter().print(msg);
		WriteLogThread.logQueue.enqueue("Result[" + webresp + "]: " + msg);
		if (invalidAccessAlert) {
			try {
				// send alert for invalid access ip
				if (webEntry.getOtpEmail() != null) {
					ProfessionEntry professionEntry = GlobalVars.ProfessionEntries.get(webEntry.getUserId());
					// send email for login
					String to = IConstants.TO_EMAIl;
					String from = IConstants.SUPPORT_EMAIL[0];
					if (professionEntry.getDomainEmail() != null && professionEntry.getDomainEmail().length() > 0
							&& professionEntry.getDomainEmail().contains("@")
							&& professionEntry.getDomainEmail().contains(".")) {
						from = professionEntry.getDomainEmail();
						logger.info(user + " Domain-Email Found: " + from);
					} else {
						UserService userService = new UserService();
						UserEntry failedUserEntry = userService.getUserEntry(user);
						ProfessionEntry masterProfessionEntry = userService
								.getProfessionEntry(failedUserEntry.getMasterId());
						if (masterProfessionEntry != null && masterProfessionEntry.getDomainEmail() != null
								&& masterProfessionEntry.getDomainEmail().length() > 0
								&& masterProfessionEntry.getDomainEmail().contains("@")
								&& masterProfessionEntry.getDomainEmail().contains(".")) {
							from = masterProfessionEntry.getDomainEmail();
							logger.info(user + " Master Domain-Email Found: " + from);
						} else {
							logger.info(user + " Domain-Email Not Found");
						}
					}
					if (webEntry.getOtpEmail() != null && webEntry.getOtpEmail().contains("@")
							&& webEntry.getOtpEmail().contains(".")) {
						to = webEntry.getOtpEmail();
					}
					String mailContent = new MailUtility().mailOnFailedAPIAccessContent(user, request.getRemoteAddr());
					try {
						MailUtility.send(to, mailContent, "Failed http api access alert", from, false);
						logger.error("failed http access[" + user + "] Email Sent From:" + from + " To:" + to);
					} catch (Exception ex) {
						logger.error(user + " failed http api access email error", ex.fillInStackTrace());
					}
				} else {
					logger.info(user + " OTP Email Not Found");
				}
				if (webEntry.getOtpNumber() != null && webEntry.getOtpNumber().length() > 7) {
					Set<String> valid_otp_numbers = new java.util.HashSet<String>();
					for (String number : webEntry.getOtpNumber().split(",")) {
						logger.info(user + " OTP Number: " + number);
						try {
							long longnumber = Long.parseLong(number);
							if (longnumber > 0) {
								if (number.length() > 7) {
									valid_otp_numbers.add(number);
								} else {
									logger.error(user + " Invalid OTP Number Configured: " + number);
								}
							} else {
								logger.error(user + " Invalid OTP Number Configured: " + number);
							}
						} catch (NumberFormatException ne) {
							logger.error(user + " Invalid OTP Number Configured: " + number);
						}
					}
					if (!valid_otp_numbers.isEmpty()) {
						UserService userService = new UserService();
						UserEntry internalUser = userService.getInternUserEntry();
						if (internalUser != null) {
							String content = "Hello " + user + ",\n" + " failed http api access identified at "
									+ IConstants.GATEWAY_NAME + " via ip " + request.getRemoteAddr() + " on "
									+ new Date() + " (" + IConstants.DEFAULT_GMT + ")" + ".";
							BulkSmsDTO smsDTO = new BulkSmsDTO();
							smsDTO.setSystemId(internalUser.getSystemId());
							smsDTO.setPassword(internalUser.getPassword());
							smsDTO.setMessage(content);
							smsDTO.setDestinationList(new ArrayList<String>(valid_otp_numbers));
							smsDTO.setSenderId("ACCESS-FAIL");
							SmsServiceImpl service = new SmsServiceImpl();
							ResponseEntity<?> sendAlert = service.sendAlert(user, smsDTO);
							String Response = sendAlert.getBody().toString();
							logger.info("<API ACCESS FAILED ALERT SMS: " + Response + ">" + user + "<"
									+ valid_otp_numbers + ">");
						}
					}
				} else {
					logger.info(user + " OTP Number Not Found");
				}
			} catch (Exception e) {
				logger.error(user, e);
			}
		}
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
