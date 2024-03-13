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
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.WriteLogThread;

public class JsonScheduleList extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(JsonScheduleList.class);
	private IDatabaseService dbService =new  IDatabaseService();

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
		StringBuilder sbf = new StringBuilder();
		InputStream is = null;
		BufferedReader br = null;
		String username = null;
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
		String decodeUrl = URLDecoder.decode(sbf.toString(), "UTF-8");
		WriteLogThread.logQueue.enqueue(decodeUrl);
		BaseApiDTO jsonDTO = null;
		try {
			jsonDTO = parseRequest(decodeUrl);
			logger.debug("<--- Request Format is Correct -->");
		} catch (JSONException e) {
			status = ResponseCode.INVALID_REQUEST;
			logger.error(username, e.fillInStackTrace());
		} catch (InvalidFormatException e) {
			status = e.getMessage();
			logger.error(username, e.getMessage());
		} catch (Exception e) {
			logger.error(username, e.fillInStackTrace());
			status = ResponseCode.SYSTEM_ERROR;
		}
		List<ApiResultDTO> result = null;
		if (jsonDTO != null) {
			logger.info(jsonDTO.getUsername() + ": " + jsonDTO.toString());
			username = jsonDTO.getUsername();
			UserService userService=new UserService();
			UserEntry userEntry = userService.getUserEntry(jsonDTO.getUsername());
			if (userEntry != null) {
				if (isValidAccessIP(userEntry, request.getRemoteAddr())) {
					result = dbService.listApiSchedule(jsonDTO.getUsername());
					logger.info(username + " Total API Records : " + result.size());
					if (result.isEmpty()) {
						status = ResponseCode.NO_RECORDS;
					} else {
						JSONArray reports = new JSONArray();
						JSONObject report = null;
						for (ApiResultDTO resultEntry : result) {
							report = new JSONObject();
							report.put("batch_id", resultEntry.getBatchId());
							report.put("scheduled_on", resultEntry.getScheduleOn());
							report.put("created_on", resultEntry.getCreatedOn());
							report.put("sender", resultEntry.getSender());
							reports.put(report);
						}
						JSONObject resp_object = new JSONObject();
						resp_object.put("result", reports);
						out.print(resp_object.toString());
					}
				} else {
					status = ResponseCode.ACCESS_DENIED;
					logger.error(username, "Invalid Access Remote Address");
				}
			} else {
				status = ResponseCode.INVALID_LOGIN;
				logger.error(username, "Invalid Account");
			}
		}
		if (!status.equalsIgnoreCase(ResponseCode.NO_ERROR)) {
			JSONObject resp_object = new JSONObject();
			JSONObject report = new JSONObject();
			report.put("error", status);
			resp_object.put("result", report);
			out.print(resp_object);
		}
	}

	private BaseApiDTO parseRequest(String uri) throws JSONException, InvalidFormatException {
		logger.info("parsing Request: " + uri);
		BaseApiDTO json = new BaseApiDTO();
		JSONObject parse = new JSONObject(uri);
		JSONObject enquiry = parse.getJSONObject("schedules");
		if (((enquiry.has("username") && enquiry.has("password")) || enquiry.has("accesskey"))) {
			if (enquiry.has("username") && enquiry.has("password")) {
				String username = enquiry.getString("username");
				String password = enquiry.getString("password");
				if (username.length() == 0 || username.length() > 16) {
					logger.error(" Invalid Username: " + username);
					throw new InvalidFormatException(ResponseCode.INVALID_LOGIN);
				} else if (password.length() == 0 || password.length() > 9) {
					logger.error(" Invalid Password: " + password);
					throw new InvalidFormatException(ResponseCode.INVALID_LOGIN);
				}
				json.setUsername(username);
				json.setPassword(password);
			} else {
				String accessKey = enquiry.getString("accesskey");
				if (accessKey.length() != 15) {
					logger.error(" Invalid AccessKey: " + accessKey);
					throw new InvalidFormatException(ResponseCode.INVALID_LOGIN);
				} else {
					boolean found = false;
					com.hazelcast.query.Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl()
							.getEntryObject().get("provCode").equal(accessKey);
					UserEntry userEntry = null;
					for (WebMasterEntry webEntryItr : GlobalVars.WebmasterEntries.values(p)) {
						userEntry = GlobalVars.UserEntries.get(webEntryItr.getUserId());
						json.setUsername(userEntry.getSystemId());
						json.setPassword(userEntry.getPassword());
						found = true;
						break;
					}
					if (!found) {
						logger.info(" Invalid AccessKey: " + accessKey);
						throw new InvalidFormatException(ResponseCode.INVALID_LOGIN);
					}
				}
			}
		} else {
			throw new InvalidFormatException(ResponseCode.INVALID_REQUEST);
		}
		return json;
	}

	private boolean isValidAccessIP(UserEntry userEntry, String ip) {
		if (userEntry.getAccessIp() != null && userEntry.getAccessIp().length() > 0) {
			boolean matched = false;
			if (ip.equalsIgnoreCase("0:0:0:0:0:0:0:1") || ip.equalsIgnoreCase("127.0.0.1")) {
				matched = true;
			} else {
				String allowed_list = userEntry.getAccessIp();
				StringTokenizer st = new StringTokenizer(allowed_list, ",");
				while (st.hasMoreTokens()) {
					String allowedip = st.nextToken();
					logger.debug(userEntry.getSystemId() + " matching: " + allowedip);
					if (allowedip.indexOf("/") > 0) {
						if (isInRange(allowedip, ip)) {
							matched = true;
							break;
						}
					} else {
						if (ip.equalsIgnoreCase(allowedip)) {
							matched = true;
							break;
						}
					}
				}
			}
			if (!matched) {
				logger.error(userEntry.getSystemId() + " Invalid JSON Access IPAddress: " + ip);
				return false;
			}
		} else {
			if (userEntry.getAccessCountry() != null && userEntry.getAccessCountry().length() > 0) {
				boolean matched = false;
				if (ip.equalsIgnoreCase("0:0:0:0:0:0:0:1") || ip.equalsIgnoreCase("127.0.0.1")) {
					matched = true;
				} else {
					String country = new LoginService().getCountryname(ip);
					if (country != null) {
						for (String allowedCountry : userEntry.getAccessCountry().split(",")) {
							if (allowedCountry.equalsIgnoreCase(country)) {
								matched = true;
								break;
							}
						}
					} else {
						logger.info(userEntry.getSystemId() + " Country [" + ip + "] not found in database.");
					}
				}
				if (!matched) {
					logger.error(userEntry.getSystemId() + " Invalid JSON Access IPAddress: " + ip);
					return false;
				}
			}
		}
		return true;
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
