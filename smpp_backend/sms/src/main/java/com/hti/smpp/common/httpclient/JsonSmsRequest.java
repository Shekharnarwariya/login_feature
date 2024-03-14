package com.hti.smpp.common.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.contacts.dto.ContactEntry;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;
import com.hti.smpp.common.contacts.dto.GroupEntryDTO;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.Converter;
import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;
import com.hti.smpp.common.util.WriteLogThread;

@Service
public class JsonSmsRequest extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(JsonSmsRequest.class);
	private String batch_id = null;
	@Autowired
	private IDatabaseService dbService;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private synchronized void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String status = ResponseCode.NO_ERROR;
		PrintWriter out = response.getWriter();
		try {
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
				logger.error(sbf.toString(), ioe.fillInStackTrace());
				status = ResponseCode.IOSTREAM_ERROR;
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException ioe) {
						br = null;
					}
				}
			}
			if (status.equalsIgnoreCase(ResponseCode.NO_ERROR)) {
				batch_id = new SimpleDateFormat("ddMMyyHHmmssSSS").format(new Date())
						+ (new Random().nextInt(9999 - 1000) + 1000);
				String decodeUrl = URLDecoder.decode(sbf.toString(), "UTF-8");
				WriteLogThread.logQueue.enqueue(request.getRemoteAddr() + " [" + batch_id + "] " + decodeUrl);
				BaseApiDTO jsonDTO = null;
				try {
					jsonDTO = parseRequest(decodeUrl);
					logger.debug("<--- Request Format is Correct -->");
				} catch (JSONException e) {
					status = e.getMessage();
					logger.error(batch_id, e.fillInStackTrace());
				} catch (InvalidFormatException e) {
					status = e.getMessage();
					logger.error(batch_id, e.getMessage());
				} catch (Exception e) {
					logger.error(batch_id, e.fillInStackTrace());
					status = ResponseCode.SYSTEM_ERROR;
				}
				if (jsonDTO != null) {
					jsonDTO.setRequestFormat("JSON");
					jsonDTO.setIpAddr(request.getRemoteAddr());
					int format = jsonDTO.getFormat();
					status = checkValidations(jsonDTO);
					logger.info("RequestBulk[" + format + "] --> " + jsonDTO);
					if (status.equalsIgnoreCase(ResponseCode.NO_ERROR)) {
						// -------- checking if invalid destination in list ------------
						int validReceipients = 0;
						if (format == 1) {
							validReceipients = jsonDTO.getReceipients().size();
						} else {
							validReceipients = jsonDTO.getCustomReceipients().size();
						}
						if (validReceipients == 0) {
							status = ResponseCode.INVALID_DEST_ADDR;
							logger.info(batch_id + " No Valid Destination Found");
						} else {
							// -------- checking if its Schedule ------------
							if (jsonDTO.getScheduleTime() != null && jsonDTO.getScheduleTime().trim().length() == 12) {
								String gmt = jsonDTO.getGmt();
								String schtime = jsonDTO.getScheduleTime();
								logger.info(batch_id + " Schedule JSON request: " + schtime);
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
									if (gmt != null) {
										if (gmt.startsWith("-") || gmt.startsWith("+")) {
											gmt = "GMT" + gmt;
										} else {
											gmt = "GMT+" + gmt.trim();
										}
									} else {
										gmt = IConstants.DEFAULT_GMT;
									}
									String client_time = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
											.format(new SimpleDateFormat("yyyyMMddhhmm").parse(schtime));
									jsonDTO.setScheduleTime(client_time);
									SimpleDateFormat client_formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
									client_formatter.setTimeZone(TimeZone.getTimeZone(gmt));
									String schedule_time = null;
									boolean valid_sch_time = false;
									try {
										schedule_time = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
												.format(client_formatter.parse(client_time));
										System.out.println(batch_id + " client_gmt: " + gmt + " client_time: "
												+ client_time + " server_time: " + schedule_time);
										if (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(schedule_time)
												.after(new Date())) {
											valid_sch_time = true;
										} else {
											logger.error(batch_id + " Scheduled Time is before Current Time");
										}
									} catch (Exception e) {
										logger.error(batch_id, e);
									}
									if (valid_sch_time) {
										jsonDTO.setWebid(batch_id);
										jsonDTO.setServerScheduleTime(schedule_time);
										jsonDTO.setGmt(gmt);
										String scheduleFile = jsonDTO.getUsername() + "_" + batch_id + ".ser";
										jsonDTO.setScheduleFile(scheduleFile);
										int scheduleId = dbService.createApiSchedule(jsonDTO);
										jsonDTO.setScheduleId(scheduleId);
										MultiUtility.writeObject(
												IConstants.WEBSMPP_EXT_DIR + "schedule//" + scheduleFile, jsonDTO);
										ApiRequestProcessor.scheduleTask(jsonDTO);
									} else {
										status = ResponseCode.INVALID_REQUEST;
									}
								} else {
									logger.error(batch_id + " Invalid Scheduled Time:" + schtime);
									status = ResponseCode.INVALID_REQUEST;
								}
							} else {
								jsonDTO.setWebid(batch_id);
								ApiRequestProcessor.procQueue.enqueue(jsonDTO);
							}
						}
					}
				}
			}
			JSONObject resp_object = new JSONObject();
			JSONObject status_object = new JSONObject();
			status_object.put("status", status);
			if (status.equalsIgnoreCase(ResponseCode.NO_ERROR)) {
				status_object.put("batch_id", batch_id);
			} else {
				status_object.put("batch_id", " ");
			}
			resp_object.put("response", status_object);
			out.print(resp_object.toString());
			WriteLogThread.logQueue.enqueue(batch_id + " Response: " + resp_object.toString());
		} catch (Exception e) {
			logger.error("", e.fillInStackTrace());
		}
	}

	private String checkValidations(BaseApiDTO jsonDTO) {
		logger.debug(batch_id + " Checking For Validations");
		String username = null;
		if (jsonDTO.getAccessKey() != null) {
			boolean found = false;
			com.hazelcast.query.Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
					.get("provCode").equal(jsonDTO.getAccessKey());
			for (WebMasterEntry webEntryItr : GlobalVars.WebmasterEntries.values(p)) {
				UserEntry userEntry = GlobalVars.UserEntries.get(webEntryItr.getUserId());
				jsonDTO.setUsername(userEntry.getSystemId());
				jsonDTO.setPassword(userEntry.getPassword());
				found = true;
				break;
			}
			if (!found) {
				logger.info(batch_id + " Invalid AccessKey: " + jsonDTO.getAccessKey());
				return ResponseCode.INVALID_LOGIN;
			}
		}
		username = jsonDTO.getUsername();
		try {
			logger.debug(batch_id + username + " Checking For FLag");
			// --------- Checking if Account blocked --------
			String readFlag = MultiUtility.readFlag(Constants.USER_FLAG_DIR + username + ".txt");
			// System.out.println("Http : " + user + " Flag -> " + readFlag);
			if (readFlag.contains("404")) {
				logger.info(batch_id + " " + username + " Blocked <404> ");
				return ResponseCode.ACCESS_DENIED;
			}
			logger.debug(batch_id + username + " Checking For UserEntry");
			UserService userService = new UserService();
			UserEntryExt userEntry = userService.getUserEntryExt(username);
			if (userEntry == null) {
				logger.info(batch_id + " " + username + " User Record Not Found");
				return ResponseCode.INVALID_LOGIN;
			}
			logger.debug(batch_id + username + " Checking For ApiKeyAccess");
			if (userEntry.getWebMasterEntry().isApiKeyOnly()) {
				if (jsonDTO.getAccessKey() != null && jsonDTO.getAccessKey().length() == 15) {
					if (!userEntry.getWebMasterEntry().getProvCode().equalsIgnoreCase(jsonDTO.getAccessKey())) {
						logger.info(batch_id + " " + username + " Mismatched AccessKey: " + jsonDTO.getAccessKey());
						return ResponseCode.INVALID_LOGIN;
					}
				} else {
					logger.info(batch_id + " " + username + " Invalid AccessKey: " + jsonDTO.getAccessKey());
					return ResponseCode.INVALID_LOGIN;
				}
			}
			logger.debug(batch_id + username + " Checking For Password");
			if (!userEntry.getUserEntry().getPassword().equalsIgnoreCase(jsonDTO.getPassword())) {
				logger.info(batch_id + " " + username + " Invalid Password: " + jsonDTO.getPassword());
				return ResponseCode.INVALID_LOGIN;
			}
			logger.debug(batch_id + username + " Checking For APIAccess");
			if (!userEntry.getWebMasterEntry().isApiAccess()) {
				logger.info(batch_id + " " + username + " WebAccess Not Allowed");
				return ResponseCode.ACCESS_DENIED;
			}
			logger.debug(batch_id + username + " Checking For ForcePasswordChange");
			if (userEntry.getUserEntry().isForcePasswordChange()) {
				boolean isPasswordExpired = false;
				try {
					isPasswordExpired = new SimpleDateFormat("yyyy-MM-dd")
							.parse(userEntry.getUserEntry().getPasswordExpiresOn()).before(new java.util.Date());
				} catch (ParseException ex) {
					logger.error(batch_id + " " + username, ex.fillInStackTrace());
				}
				if (isPasswordExpired) {
					logger.info(batch_id + " " + username + " Password Expired");
					return ResponseCode.PASSWORD_EXPIRED;
				}
			}
			if (jsonDTO.getSender() != null && jsonDTO.getSender().length() > 0) {
				if ((userEntry.getWebMasterEntry().getSenderId() != null
						&& userEntry.getWebMasterEntry().getSenderId().length() > 0)
						&& (userEntry.getWebMasterEntry().getSenderRestrictTo().equalsIgnoreCase("ALL")
								|| userEntry.getWebMasterEntry().getSenderRestrictTo().equalsIgnoreCase("API"))) {
					if (!userEntry.getWebMasterEntry().getSenderId().toLowerCase()
							.contains(jsonDTO.getSender().toLowerCase())) {
						logger.error(batch_id + " SenderId Not Allowed: " + jsonDTO.getSender());
						return ResponseCode.INVALID_SENDER;
					}
				}
			} else {
				logger.error(batch_id + " Invalid SenderId: " + jsonDTO.getSender());
				return ResponseCode.INVALID_SENDER;
			}
			logger.debug(batch_id + username + " Checking For AllowedIP");
			// check ip
			if (userEntry.getUserEntry().getAccessIp() != null && userEntry.getUserEntry().getAccessIp().length() > 0) {
				boolean matched = false;
				if (jsonDTO.getIpAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1")
						|| jsonDTO.getIpAddr().equalsIgnoreCase("127.0.0.1")) {
					matched = true;
				} else {
					String allowed_list = userEntry.getUserEntry().getAccessIp();
					StringTokenizer st = new StringTokenizer(allowed_list, ",");
					while (st.hasMoreTokens()) {
						String allowedip = st.nextToken();
						logger.debug(batch_id + username + " matching: " + allowedip);
						if (allowedip.indexOf("/") > 0) {
							if (isInRange(allowedip, jsonDTO.getIpAddr())) {
								matched = true;
								break;
							}
						} else {
							if (jsonDTO.getIpAddr().equalsIgnoreCase(allowedip)) {
								matched = true;
								break;
							}
						}
					}
				}
				if (!matched) {
					logger.error(batch_id + " " + username + " Invalid JSON Access IPAddress: " + jsonDTO.getIpAddr());
					return ResponseCode.ACCESS_DENIED;
				}
			} else {
				if (userEntry.getUserEntry().getAccessCountry() != null
						&& userEntry.getUserEntry().getAccessCountry().length() > 0) {
					boolean matched = false;
					if (jsonDTO.getIpAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1")
							|| jsonDTO.getIpAddr().equalsIgnoreCase("127.0.0.1")) {
						matched = true;
					} else {
						String country = new LoginService().getCountryname(jsonDTO.getIpAddr());
						if (country != null) {
							for (String allowedCountry : userEntry.getUserEntry().getAccessCountry().split(",")) {
								if (allowedCountry.equalsIgnoreCase(country)) {
									matched = true;
									break;
								}
							}
						} else {
							logger.info(username + " Country [" + jsonDTO.getIpAddr() + "] not found in database.");
						}
					}
					if (!matched) {
						logger.error(
								batch_id + " " + username + " Invalid JSON Access IPAddress: " + jsonDTO.getIpAddr());
						return ResponseCode.ACCESS_DENIED;
					}
				}
			}
			logger.debug(batch_id + username + " Checking For Account Expiry");
			try {
				if (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(userEntry.getUserEntry().getExpiry() + " 23:59:59").before(new Date())) {
					logger.info(
							batch_id + " " + username + " Account Expired: " + userEntry.getUserEntry().getExpiry());
					return ResponseCode.ACCOUNT_EXPIRED;
				}
			} catch (ParseException e) {
				logger.error(username, "Expiry Parse Error: " + userEntry.getUserEntry().getExpiry());
			}
			logger.debug(batch_id + username + " Checking For Balance");
			// -- check balance -----------
			if (GlobalVars.BalanceEntries.containsKey(userEntry.getUserEntry().getId())) {
				BalanceEntry balance = GlobalVars.BalanceEntries.get(userEntry.getUserEntry().getId());
				if (balance.getWalletFlag().equalsIgnoreCase("No")) {
					if (balance.getCredits() <= 1) {
						logger.info(batch_id + " " + username + " Insufficient Credits: " + balance.getCredits());
						return ResponseCode.INSUF_BALANCE;
					}
				} else {
					if (balance.getWalletAmount() <= 1) {
						logger.info(batch_id + " " + username + " Insufficient Balance: " + balance.getWalletAmount());
						return ResponseCode.INSUF_BALANCE;
					}
				}
			}
		} catch (Exception ex) {
			logger.error(batch_id + " " + username, ex.fillInStackTrace());
		}
		return ResponseCode.NO_ERROR;
	}

	private BaseApiDTO parseRequest(String uri) throws JSONException, InvalidFormatException {
		logger.info("parsing Request: " + uri);
		BaseApiDTO json = null;
		JSONObject parse = new JSONObject(uri);
		if (parse.has("campaign")) {
			JSONObject campaign = (JSONObject) parse.get("campaign");
			if (campaign.has("format")
					&& ((campaign.has("username") && campaign.has("password")) || campaign.has("accesskey"))
					&& campaign.has("sender")) {
				int format = Integer.parseInt(campaign.getString("format"));
				if (format == 1 || format == 2) {
					json = new BaseApiDTO();
					int type = 0;
					if (campaign.has("type")) {
						type = Integer.parseInt(campaign.getString("type"));
					}
					String sender = campaign.getString("sender");
					if (campaign.has("username") && campaign.has("password")) {
						String username = campaign.getString("username");
						String password = campaign.getString("password");
						if (username.length() == 0 || username.length() > 9) {
							logger.error(batch_id + " Invalid Username: " + username);
							throw new InvalidFormatException(ResponseCode.INVALID_LOGIN);
						}
						if (password.length() == 0 || password.length() > 9) {
							logger.error(batch_id + " Invalid Password: " + password);
							throw new InvalidFormatException(ResponseCode.INVALID_LOGIN);
						}
						json.setUsername(username);
						json.setPassword(password);
					} else {
						String accessKey = campaign.getString("accesskey");
						if (accessKey.length() != 15) {
							logger.error(batch_id + " Invalid AccessKey: " + accessKey);
							throw new InvalidFormatException(ResponseCode.INVALID_LOGIN);
						} else {
							json.setAccessKey(accessKey);
						}
					}
					if (sender.length() == 0 || sender.length() > 16) {
						logger.error(batch_id + " Invalid Sender: " + sender);
						throw new InvalidFormatException(ResponseCode.INVALID_SENDER);
					}
					if (type > 4) {
						logger.error(batch_id + " Invalid Type: " + type);
						throw new InvalidFormatException(ResponseCode.INVALID_TYPE);
					}
					json.setSender(sender);
					json.setFormat(format);
					if (campaign.has("peid")) {
						json.setPeId(campaign.getString("peid"));
					}
					if (campaign.has("templateid")) {
						json.setTemplateId(campaign.getString("templateid"));
					}
					if (campaign.has("tmid")) {
						json.setTelemarketerId(campaign.getString("tmid"));
					}
					if (campaign.has("schtime")) {
						json.setScheduleTime(campaign.getString("schtime"));
						if (campaign.has("gmt")) {
							json.setGmt(campaign.getString("gmt"));
						}
					}
					if (format == 1) {
						String text = null;
						if (campaign.has("gsm")) {
							JSONArray receipients = campaign.getJSONArray("gsm");
							if (receipients.length() == 0) {
								logger.error(batch_id + " No Valid Destination");
								throw new InvalidFormatException(ResponseCode.INVALID_DEST_ADDR);
							} else {
								Iterator<Object> itr = receipients.iterator();
								while (itr.hasNext()) {
									String number = (String) itr.next();
									number = number.replaceAll("\\s+", ""); // Replace all the spaces in the String with
																			// empty character.
									number = number.substring(number.lastIndexOf("+") + 1); // Remove +
									try {
										Long.parseLong(number.trim());
										json.getReceipients().add(number);
									} catch (NumberFormatException NFE) {
										logger.info(batch_id + " Invalid Destination: " + number);
									}
								}
							}
						} else if (campaign.has("group")) {
							JSONArray group = campaign.getJSONArray("group");
							if (group.length() == 0) {
								logger.error(batch_id + " No Valid Group");
								throw new InvalidFormatException(ResponseCode.INVALID_DEST_ADDR);
							} else {
								int limit = 0;
								if (campaign.has("limit")) {
									limit = Integer.parseInt(campaign.getString("limit"));
								}
								ContactService contactService = new ContactService();
								Map<String, GroupEntryDTO> group_list = contactService
										.listGroupNames(json.getUsername());
								Map<String, GroupEntryDTO> valid_groups = new java.util.HashMap<String, GroupEntryDTO>();
								Iterator<Object> itr = group.iterator();
								while (itr.hasNext()) {
									String group_name = (String) itr.next();
									if (group_list.containsKey(group_name.toLowerCase())) {
										valid_groups.put(group_name, group_list.get(group_name.toLowerCase()));
									} else {
										logger.info(batch_id + " Invalid Group Requested: " + group_name);
									}
								}
								if (valid_groups.isEmpty()) {
									logger.info(batch_id + " <-- No valid Group Found --> ");
									throw new InvalidFormatException(ResponseCode.INVALID_DEST_ADDR);
								} else {
									Set<String> valid_numbers = new java.util.HashSet<String>();
									for (GroupEntryDTO groupEntry : valid_groups.values()) {
										if (groupEntry.isGroupData()) {
											List<GroupDataEntry> contact_list = null;
											if (limit > 0) {
												contact_list = contactService.listGroupData(groupEntry.getId(), 1,
														limit);
											} else {
												contact_list = contactService.listGroupData(groupEntry.getId());
											}
											for (GroupDataEntry groupDataEntry : contact_list) {
												valid_numbers.add(String.valueOf(groupDataEntry.getNumber()));
											}
										} else {
											List<ContactEntry> contact_list = null;
											if (limit > 0) {
												contact_list = contactService.listContact(groupEntry.getId(), 1, limit);
											} else {
												contact_list = contactService.listContact(groupEntry.getId());
											}
											for (ContactEntry contactEntry : contact_list) {
												valid_numbers.add(String.valueOf(contactEntry.getNumber()));
											}
										}
									}
									if (valid_numbers.isEmpty()) {
										logger.error(batch_id + " No Valid Destination");
										throw new InvalidFormatException(ResponseCode.INVALID_DEST_ADDR);
									} else {
										logger.info(
												batch_id + " Total Group Numbers To Proceed: " + valid_numbers.size());
										json.getReceipients().addAll(valid_numbers);
									}
								}
							}
						} else {
							logger.error(batch_id + " Parameter missing: gsm");
							throw new InvalidFormatException(ResponseCode.INVALID_DEST_ADDR);
						}
						if (campaign.has("text")) {
							text = campaign.getString("text");
							if (text.length() == 0) {
								logger.error(batch_id + " Invalid Text: " + text);
								throw new InvalidFormatException(ResponseCode.INVALID_TEXT);
							} else {
								if (type == 0) {
									if (!Pattern.compile(
											"^[A-Za-z0-9 \\r\\n@�$�����������\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u00A4\u03A3\u0398\u039E����!\"#$%&'()*+,\\-./:;<=>?����ܧ������^{}\\\\\\[~\\]|\u20AC]*$")
											.matcher(text).find()) {
										type = 4;
										System.out.println(batch_id + " auto detect type: " + type);
									} else {
										int msg_length = text.length();
										if (msg_length % 4 == 0) {
											try {
												String original = new Converters()
														.UTF16(Converter.getUnicode(text.toCharArray()));
												if (text.equalsIgnoreCase(original)) {
													type = 2;
													System.out.println(batch_id + " auto detect type: " + type);
												} else {
													type = 1;
													System.out.println(batch_id + " auto detect type: " + type);
												}
											} catch (Exception e) {
												type = 1;
												System.out.println(
														batch_id + " auto detect type due to exception: " + type);
											}
										} else {
											type = 1;
											System.out.println(batch_id + " auto detect type: " + type);
										}
									}
								} else {
									if (type == 1 || type == 3) {
										if (!Pattern.compile(
												"^[A-Za-z0-9 \\r\\n@�$�����������\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u00A4\u03A3\u0398\u039E����!\"#$%&'()*+,\\-./:;<=>?����ܧ������^{}\\\\\\[~\\]|\u20AC]*$")
												.matcher(text).find()) {
											logger.error(batch_id + " message content & type mismatched");
											throw new InvalidFormatException(ResponseCode.INVALID_TYPE);
										}
									} else if (type == 2) {
										if (text.length() % 4 != 0) {
											logger.error(
													batch_id + " Invalid Unicode Message Length: " + text.length());
											throw new InvalidFormatException(ResponseCode.INVALID_TEXT);
										}
									}
								}
							}
							json.setText(text);
							json.setType(type);
						} else {
							logger.error(batch_id + " Parameter missing: text");
							throw new InvalidFormatException(ResponseCode.INVALID_TEXT);
						}
					} else { // format 2
						if (campaign.has("custom")) {
							JSONArray receipients = campaign.getJSONArray("custom");
							if (receipients.length() == 0) {
								logger.error(batch_id + " No Valid Destination");
								throw new InvalidFormatException(ResponseCode.INVALID_DEST_ADDR);
							} else {
								if (type == 0) {
									logger.error(batch_id + " Invalid Type: " + type);
									throw new InvalidFormatException(ResponseCode.INVALID_TYPE);
								} else {
									json.setType(type);
								}
								Iterator<Object> itr = receipients.iterator();
								while (itr.hasNext()) {
									JSONObject custom = (JSONObject) itr.next();
									if (custom.has("gsm") && custom.has("text")) {
										String number = custom.getString("gsm");
										number = number.replaceAll("\\s+", ""); // Replace all the spaces in the String
																				// with empty character.
										number = number.substring(number.lastIndexOf("+") + 1); // Remove +
										try {
											Long.parseLong(number.trim());
											json.getReceipients().add(number);
										} catch (NumberFormatException NFE) {
											logger.info(batch_id + " Invalid Destination: " + number);
											continue;
										}
										String text = custom.getString("text");
										if (text.length() == 0) {
											logger.error(batch_id + " Invalid Text: " + text);
											continue;
										} else {
											if (type == 1 || type == 3) {
												if (!Pattern.compile(
														"^[A-Za-z0-9 \\r\\n@�$�����������\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u00A4\u03A3\u0398\u039E����!\"#$%&'()*+,\\-./:;<=>?����ܧ������^{}\\\\\\[~\\]|\u20AC]*$")
														.matcher(text).find()) {
													logger.error(batch_id + " message content & type mismatched");
													continue;
												}
											} else if (type == 2) {
												if (text.length() % 4 != 0) {
													logger.error(batch_id + " Invalid Unicode Message Length: "
															+ text.length());
													continue;
												}
											}
										}
										json.getCustomReceipients().add(new String[] { number, text });
									} else {
										logger.info(batch_id + " Invalid Custom: " + custom.toString());
									}
								}
							}
						} else {
							logger.error(batch_id + " Parameter missing: custom");
							throw new InvalidFormatException(ResponseCode.INVALID_DEST_ADDR);
						}
					}
				} else {
					logger.error(batch_id + " Invalid Parameter: Format=" + format);
					throw new InvalidFormatException(ResponseCode.INVALID_REQUEST);
				}
			} else {
				throw new InvalidFormatException(ResponseCode.INVALID_REQUEST);
			}
		} else {
			logger.error(batch_id + " Parameter missing: campaign");
			throw new InvalidFormatException(ResponseCode.INVALID_REQUEST);
		}
		return json;
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
