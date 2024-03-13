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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.smpp.common.contacts.dto.ContactEntry;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;
import com.hti.smpp.common.contacts.dto.GroupEntryDTO;
import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;
import com.hti.smpp.common.util.WriteLogThread;

public class GroupSmsRequest extends HttpServlet {
	// private static long counter = 0;
	private String batch_id = null;
	private Logger logger = LoggerFactory.getLogger(GroupSmsRequest.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		batch_id = new SimpleDateFormat("ddMMyyHHmmssSSS").format(new Date())
				+ (new Random().nextInt(9999 - 1000) + 1000);
		createURLRequest(request, response);
	}

	private void createURLRequest(HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException, ServletException, IOException {
		String decodeUrl = URLDecoder.decode(request.getQueryString(), "UTF-8");
		request.setAttribute("user", request.getParameter("user"));
		request.setAttribute("pass", request.getParameter("pass"));
		request.setAttribute("sid", request.getParameter("sid"));
		request.setAttribute("group", request.getParameter("group"));
		request.setAttribute("limit", request.getParameter("limit"));
		request.setAttribute("text", request.getParameter("text"));
		request.setAttribute("schtime", request.getParameter("schtime"));
		request.setAttribute("gmt", request.getParameter("gmt"));
		request.setAttribute("type", request.getParameter("type"));
		request.setAttribute("header", request.getParameter("header"));
		request.setAttribute("respformat", request.getParameter("respformat"));
		request.setAttribute("peid", request.getParameter("peid"));
		request.setAttribute("templateid", request.getParameter("templateid"));
		request.setAttribute("tmid", request.getParameter("tmid"));
		request.setAttribute("accesskey", request.getParameter("accesskey"));
		WriteLogThread.logQueue.enqueue(
				"( " + request.getRemoteAddr() + " ) " + request.getMethod() + "[" + batch_id + "]: " + decodeUrl);
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		batch_id = new SimpleDateFormat("ddMMyyHHmmssSSS").format(new Date())
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
						.enqueue("( " + request.getRemoteAddr() + " ) POST[" + batch_id + "]: " + decodeUrl);
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
		String user = null, pass = null, sid = null, group = null, text = null, schtime = null, gmt = null,
				respformat = null;
		int type = 0, limit = 0;// esm = 0, dcs = 0;
		String strType = null, strLimit = null;
		boolean webaccess = true, proceed = true;
		String header = null;
		String msg = null;
		// PrintWriter out = response.getWriter();
		user = (String) request.getAttribute("user");
		pass = (String) request.getAttribute("pass");
		sid = (String) request.getAttribute("sid");
		group = (String) request.getAttribute("group");
		strLimit = (String) request.getAttribute("limit");
		strType = (String) request.getAttribute("type");
		text = (String) request.getAttribute("text");
		schtime = (String) request.getAttribute("schtime");
		gmt = (String) request.getAttribute("gmt");
		header = (String) request.getAttribute("header");
		respformat = (String) request.getAttribute("respformat");
		String peid = (String) request.getAttribute("peid");
		String templateid = (String) request.getAttribute("templateid");
		String tmid = (String) request.getAttribute("tmid");
		String accesscode = (String) request.getAttribute("accesskey");
		System.out.println(request.getMethod() + "<" + request.getRemoteAddr() + "> Http user: " + user + " sid: " + sid
				+ " group: " + group + "Limit:" + limit + " Type:" + strType + " text:" + text + " respformat:"
				+ respformat);
		if (user == null || pass == null) {
			if (accesscode != null && accesscode.length() > 0) {
				com.hazelcast.query.Predicate<Integer, WebMasterEntry> p = new com.hazelcast.query.impl.PredicateBuilderImpl()
						.getEntryObject().get("provCode").equal(accesscode);
				for (WebMasterEntry webEntryItr : GlobalVars.WebmasterEntries.values(p)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(webEntryItr.getUserId());
					user = userEntry.getSystemId();
					pass = userEntry.getPassword();
					break;
				}
			}
		}
		if (user == null || pass == null || sid == null || group == null || text == null || strType == null) {
			msg = IConstants.ERROR_HTTP02;
		} else {
			if (user.length() == 0 || user.length() > 16) {
				msg = IConstants.ERROR_HTTP05;
			} else {
				// System.out.println(request.getMethod() + " Http User : " + user + " From : " + request.getRemoteAddr() + " Sender : " + sid + "
				// Type : " + strType);
				WebMasterEntry webEntry = null;
				try {
					String readFlag = MultiUtility.readFlag(Constants.USER_FLAG_DIR + user + ".txt");
					// System.out.println("Http : " + user + " Flag -> " + readFlag);
					if (readFlag.contains("404")) {
						System.out.println("Http : " + user + " Blocked <404> ");
						webaccess = false;
					} else {
						UserService userService= new UserService();
						UserEntryExt entry = userService.getUserEntryExt(user);
						if (entry != null) {
							if (!entry.getWebMasterEntry().isApiAccess()) {
								webaccess = false;
							} else {
								webEntry = entry.getWebMasterEntry();
								// check ip
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
										logger.error(user + "[" + batch_id + "]" + " Invalid Http Access IPAddress: "
												+ request.getRemoteAddr());
										webaccess = false;
									}
								}
							}
						} else {
							webaccess = false;
							logger.info(user + "[" + batch_id + "]" + " User Record Not Found");
						}
					}
					if (!webaccess) {
						msg = IConstants.ERROR_HTTP15;
						System.out.println(user + "[" + batch_id + "]" + " Access Denied :: " + msg);
					} else {
						if (pass.length() == 0 || pass.length() > 9) {
							proceed = false;
							msg = IConstants.ERROR_HTTP04;
						} else if (sid.length() == 0) {
							proceed = false;
							msg = IConstants.ERROR_HTTP06;
						} else if (group.length() == 0) {
							System.out.println(user + "[" + batch_id + "]" + " No Valid Group :--> " + group);
							proceed = false;
							msg = IConstants.ERROR_HTTP09;
						} else if (text.length() == 0) {
							proceed = false;
							msg = IConstants.ERROR_HTTP17;
						} else {
							// ******** Checking for Message Type **************
							if (strType.length() == 0) {
								proceed = false;
								msg = IConstants.ERROR_HTTP07;
							} else {
								try {
									type = Integer.parseInt(strType);
									if (type < 1 || type > 4) {
										proceed = false;
										msg = IConstants.ERROR_HTTP07;
									}
								} catch (NumberFormatException ne) {
									proceed = false;
									msg = IConstants.ERROR_HTTP07;
								}
							}
						}
						if (proceed) {
							// ********* Creating Number List ************
							Set<String> valid_numbers = new java.util.HashSet<String>();
							if (group.length() > 0) {
								if (strLimit != null) {
									limit = Integer.parseInt(strLimit);
								}
								ContactService contactService=new ContactService();
								Map<String, GroupEntryDTO> group_list = contactService.listGroupNames(user);
								Map<String, GroupEntryDTO> valid_groups = new java.util.HashMap<String, GroupEntryDTO>();
								for (String group_name : group.split(",")) {
									if (group_list.containsKey(group_name.toLowerCase())) {
										valid_groups.put(group_name, group_list.get(group_name.toLowerCase()));
									} else {
										logger.info(user + "[" + batch_id + "]" + " Invalid Group Requested: "
												+ group_name);
									}
								}
								if (valid_groups.isEmpty()) {
									proceed = false;
									msg = IConstants.ERROR_HTTP09;
									logger.info(user + "[" + batch_id + "]" + " <-- No valid Group Found --> ");
								} else {
									for (GroupEntryDTO groupEntry : valid_groups.values()) {
										if (groupEntry.isGroupData()) {
											List<GroupDataEntry> contact_list = null;
											if (limit > 0) {
												contact_list = contactService
														.listGroupData(groupEntry.getId(), 1, limit);
											} else {
												contact_list = contactService
														.listGroupData(groupEntry.getId());
											}
											for (GroupDataEntry groupDataEntry : contact_list) {
												valid_numbers.add(String.valueOf(groupDataEntry.getNumber()));
											}
										} else {
											List<ContactEntry> contact_list = null;
											if (limit > 0) {
												contact_list = contactService.listContact(groupEntry.getId(),
														1, limit);
											} else {
												contact_list = contactService
														.listContact(groupEntry.getId());
											}
											for (ContactEntry contactEntry : contact_list) {
												valid_numbers.add(String.valueOf(contactEntry.getNumber()));
											}
										}
									}
									if (valid_numbers.isEmpty()) {
										proceed = false;
										msg = IConstants.ERROR_HTTP09;
										logger.info(user + "[" + batch_id + "]" + " <-- No valid Number Found --> ");
									} else {
										logger.info(user + "[" + batch_id + "]" + " Total Group Numbers To Proceed: "
												+ valid_numbers.size());
									}
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
								if (schtime == null) {
									BaseApiDTO httpDTO = new BaseApiDTO();
									httpDTO.setUsername(user);
									httpDTO.setPassword(pass);
									httpDTO.setSender(sid);
									httpDTO.setFormat(1);
									httpDTO.setType(type);
									httpDTO.setPeId(peid);
									httpDTO.setTemplateId(templateid);
									httpDTO.setTelemarketerId(tmid);
									httpDTO.setText(text);
									httpDTO.getReceipients().addAll(valid_numbers);
									httpDTO.setRequestFormat("http");
									httpDTO.setWebid(batch_id);
									ApiRequestProcessor.procQueue.enqueue(httpDTO);
									msg = batch_id;
								} else {
									List<String> destinationList = new ArrayList<String>();
									destinationList.addAll(valid_numbers);
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
									// ******* Scheduling **************
									if (schtime.trim().length() == 12) {
										int compare = 0;
										if (gmt == null) {
											try {
												Date givenDate = new SimpleDateFormat("yyyyMMddhhmm").parse(schtime);
												compare = givenDate.compareTo(new Date());
											} catch (ParseException ex) {
												ex.printStackTrace();
											}
										} else {
											compare = 1;
										}
										if (compare > 0) {
											bulksmsDTO.setRepeat("no");
											bulksmsDTO.setReqType("http");
											bulksmsDTO.setDelay(0);
											String year = schtime.substring(0, 4);
											String month = schtime.substring(4, 6);
											String day = schtime.substring(6, 8);
											String hour = schtime.substring(8, 10);
											String minute = schtime.substring(10, 12);
											bulksmsDTO.setTimestart(
													day + "-" + month + "-" + year + " " + hour + ":" + minute + ":00");
											System.out.println("Year: " + year + " Month: " + month + " Day: " + day
													+ " Hour: " + hour + " Minute: " + minute + " GMT: " + gmt);
											if (gmt != null) {
												CurrTimeZone ct = new CurrTimeZone();
												Calendar schGMTtime = ct.CurrentTime(gmt);
												Date schTime = new ScheduleTime().getScheduleTime(schGMTtime, year,
														month, day, hour, minute);
												year = "" + (schTime.getYear() + 1900);
												month = "" + (schTime.getMonth() + 1);
												day = "" + schTime.getDate();
												hour = "" + schTime.getHours();
												minute = "" + schTime.getMinutes();
												if ((schTime.getMonth() + 1) < 10) {
													month = "0" + month;
												}
												if (schTime.getDate() < 10) {
													day = "0" + day;
												}
												if (schTime.getHours() < 10) {
													hour = "0" + hour;
												}
												if (schTime.getMinutes() < 10) {
													minute = "0" + minute;
												}
											} else {
												gmt = IConstants.DEFAULT_GMT;
											}
											bulksmsDTO.setGmt(gmt);
											bulksmsDTO.setDate(year + "-" + month + "-" + day);
											bulksmsDTO.setTime(hour + minute);
											System.out.println(user + "[" + batch_id + "]" + " Received Date -> "
													+ bulksmsDTO.getDate() + " Time -> " + bulksmsDTO.getTime());
											msg = client.scheduleURL(bulksmsDTO, batch_id);
										} else {
											msg = IConstants.ERROR_HTTP22;
											System.out.println(user + "[" + batch_id + "]"
													+ " Invalid Scheduled Time Format :" + schtime);
										}
									} else {
										msg = IConstants.ERROR_HTTP22;
										System.out.println(user + "[" + batch_id + "]"
												+ " Invalid Scheduled Time Format :" + schtime);
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
		WriteLogThread.logQueue.enqueue("Result[" + batch_id + "]: " + msg);
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
