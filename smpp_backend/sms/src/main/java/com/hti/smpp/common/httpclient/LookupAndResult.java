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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.rmi.LookupReport;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

@Service
public class LookupAndResult extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger("hlrLogger");
	@Autowired
	private IDatabaseService dbService;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		MultiUtility.writeContent(IConstants.WEBSMPP_EXT_DIR + "log//http_hlr_log.txt",
				"( " + request.getRemoteAddr() + " ) GET: " + request.getRequestURL(), true);
		processRequest(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
			ioe.printStackTrace();
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
			String decodeUrl = URLDecoder.decode(sbf.toString());
			MultiUtility.writeContent(IConstants.WEBSMPP_EXT_DIR + "log//http_hlr_log.txt",
					"( " + request.getRemoteAddr() + " ) POST: " + decodeUrl, true);
			StringTokenizer tokens = new StringTokenizer(decodeUrl, "&");
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				if (token.indexOf("=") > -1) {
					String param = token.substring(0, token.indexOf("="));
					String value = token.substring(token.indexOf("=") + 1);
					// logger.info(param + " : " + value);
					request.setAttribute(param, value);
				}
			}
			processRequest(request, response);
		}
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String msg = null;
		boolean proceed = false;
		List<String> list = null;
		String username = null, password = null, destination = null;
		try {
			if (request.getMethod().equalsIgnoreCase("POST")) {
				username = (String) request.getAttribute("username");
				password = (String) request.getAttribute("password");
				destination = (String) request.getAttribute("destination");
			} else {
				username = request.getParameter("username");
				password = request.getParameter("password");
				destination = request.getParameter("destination");
			}
			logger.info(request.getMethod() + " Http Lookup Request By User : " + username + " From : "
					+ request.getRemoteAddr() + " dest: " + destination);
			if ((username != null && username.length() > 0) && (password != null && password.length() > 0)
					&& (destination != null && destination.length() > 0)) {
				proceed = true;
			} else {
				logger.info("<- Invalid HTTP Lookup URL Format -> ");
				msg = IConstants.ERROR_HTTP01;
			}
			if (proceed) {
				// proceed = false;
				UserService userService = new UserService();
				UserEntry user = userService.getUserEntry(username);
				String readFlag = MultiUtility.readFlag(Constants.USER_FLAG_DIR + username + ".txt");
				// System.out.println("Http : " + username + " Flag -> " + readFlag);
				if (readFlag.contains("404")) {
					logger.info("Not Authorized For HLR Request -> " + username);
					msg = IConstants.ERROR_HTTP15;
				} else {
					if (user == null) {
						logger.info("<- Invalid Username -> ");
						msg = IConstants.ERROR_HTTP05;
					} else {
						// boolean webaccess = true;
						if (user.getAccessIp() != null && user.getAccessIp().length() > 0) {
							boolean matched = false;
							if (request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1")
									|| request.getRemoteAddr().equalsIgnoreCase("127.0.0.1")) {
								matched = true;
							} else {
								String allowed_list = user.getAccessIp();
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
								logger.error(user + " Invalid Http lookup IPAddress: " + request.getRemoteAddr());
								proceed = false;
								msg = IConstants.ERROR_HTTP15;
							}
						}
						if (proceed) {
							if (user.getPassword().equalsIgnoreCase(password)) {
								if (!user.isHlr()) {
									logger.info("Not Authorized For HLR Request -> " + username);
									msg = IConstants.ERROR_HTTP15;
									proceed = false;
								} else {
									StringTokenizer tokens = new StringTokenizer(destination, ",");
									if (tokens.countTokens() > 100) {
										logger.info(
												username + " Invalid number of Destination -> " + tokens.countTokens());
										msg = IConstants.ERROR_HTTP24;
										proceed = false;
									} else {
										list = new ArrayList<String>();
										while (tokens.hasMoreTokens()) {
											list.add(tokens.nextToken());
										}
									}
								}
							} else {
								proceed = false;
								logger.info("<- Invalid Password -> ");
								msg = IConstants.ERROR_HTTP04;
							}
						}
					}
				}
			}
			if (proceed) {
				String batch_id = new SimpleDateFormat("ddMMyyHHmmssSSS").format(new Date())
						+ (new Random().nextInt(9999 - 1000) + 1000);
				LookupObject lookupObject = new LookupObject();
				lookupObject.setBatchid(batch_id);
				lookupObject.setList(list);
				lookupObject.setSystemid(username);
				lookupObject.setPassword(password);
				String ret = new HlrClient().processHlr(lookupObject);
				if (ret != null) {
					msg = ret;
				} else {
					// msg = "BatchId: " + batch_id;
					Map<String, String> params = new HashMap<String, String>();
					params.put("batch_id", batch_id);
					List<LookupReport> rslist = null;
					// String sql = "select * from lookup_result where batch_id='" + batch + "'";
					try {
						for (int i = 0; i < 3; i++) {
							rslist = new LookupServiceInvoker().getLookupReport(params);
							if (rslist.isEmpty()) {
								Thread.sleep(1000);
							} else {
								break;
							}
						}
						if (rslist.isEmpty()) {
							msg = "BatchId: " + batch_id;
						} else {
							LookupReport lookup = null;
							Map<String, String> map = null;
							while (!rslist.isEmpty()) {
								lookup = rslist.remove(0);
								map = new LinkedHashMap<String, String>();
								map.put("id", lookup.getHlrid() + "");
								map.put("batchid", lookup.getBatchId());
								map.put("destination", lookup.getNumber() + "");
								map.put("time", lookup.getSubmitTime());
								map.put("status", lookup.getStatus());
								map.put("error_code", lookup.getErrorCode());
								map.put("error", lookup.getError());
								map.put("imsi", lookup.getImsi());
								map.put("msc", lookup.getMsc());
								map.put("cc", lookup.getCc() + "");
								map.put("nnc", lookup.getNetworkCode());
								map.put("isPorted", lookup.getIsPorted());
								map.put("p_nnc", lookup.getPortedNNC());
								map.put("isRoaming", lookup.getIsRoaming());
								map.put("r_cc", lookup.getRoamingCC());
								map.put("r_nnc", lookup.getRoamingNNC());
								msg += new JSONObject(map).toString();
								msg += "\n";
							}
						}
					} catch (Exception ex) {
						System.out.println("Query request failed : " + ex);
						msg = IConstants.ERROR_HTTP13;
					}
				}
				// ------- Summary Report -----------------
				LookupSummaryObj summary = new LookupSummaryObj();
				summary.setSystemId(username);
				summary.setBatchid(batch_id);
				summary.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				// if (isCredit) {
				summary.setCost("0");
				summary.setUserMode("HTTP");
				/*
				 * } else { summary.setCost(new DecimalFormat("0.00000").format(cost));
				 * summary.setUserMode("Wallet"); }
				 */
				if (list != null) {
					summary.setNumberCount(list.size());
				} else {
					summary.setNumberCount(0);
				}
				try {
					dbService.addLookupSummaryReport(summary);
				} catch (Exception ex) {
					logger.info(ex + " While Adding Lookup Summary Report For : " + username);
				}
			} else {
				logger.error("Unable To process Request");
			}
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
			msg = IConstants.ERROR_HTTP03;
		}
		out.println(msg);
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
