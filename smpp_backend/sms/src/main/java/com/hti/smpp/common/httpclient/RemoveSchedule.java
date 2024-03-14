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
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;
import com.hti.smpp.common.util.WriteLogThread;

@Service
public class RemoveSchedule extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(UrlUserBalanceAction.class);
	@Autowired
	private IDatabaseService dbService;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String decodeUrl = URLDecoder.decode(request.getQueryString(), "UTF-8");
		WriteLogThread.logQueue.enqueue("GET: " + request.getRemoteAddr() + ": " + decodeUrl);
		request.setAttribute("userid", request.getParameter("userid"));
		request.setAttribute("password", request.getParameter("password"));
		request.setAttribute("accesskey", request.getParameter("accesskey"));
		request.setAttribute("scheduleid", request.getParameter("scheduleid"));
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getQueryString() != null) {
			String decodeUrl = URLDecoder.decode(request.getQueryString(), "UTF-8");
			WriteLogThread.logQueue.enqueue("POST: " + request.getRemoteAddr() + ": " + decodeUrl);
			request.setAttribute("userid", request.getParameter("userid"));
			request.setAttribute("password", request.getParameter("password"));
			request.setAttribute("accesskey", request.getParameter("accesskey"));
			request.setAttribute("scheduleid", request.getParameter("scheduleid"));
			processRequest(request, response);
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
				WriteLogThread.logQueue.enqueue("POST: " + request.getRemoteAddr() + ": " + decodeUrl);
				// StringTokenizer tokens = new StringTokenizer(decodeUrl, "&");
				for (String token : decodeUrl.split("&")) {
					// String token = tokens.nextToken();
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

	public void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = null;
		String msg = null;
		try {
			response.setContentType("text/plain");
			out = response.getWriter();
			String user = (String) request.getAttribute("userid");
			String pass = (String) request.getAttribute("password");
			String accesscode = (String) request.getAttribute("accesskey");
			String scheduleid = (String) request.getAttribute("scheduleid");
			System.out.println(
					"[" + request.getRemoteAddr() + "] " + user + "-" + scheduleid + " Schedule Remove Request");
			if (user == null || pass == null) {
				if (accesscode != null && accesscode.length() > 0) {
					com.hazelcast.query.Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl()
							.getEntryObject().get("provCode").equal(accesscode);
					for (WebMasterEntry webEntryItr : GlobalVars.WebmasterEntries.values(p)) {
						UserEntry userEntry = GlobalVars.UserEntries.get(webEntryItr.getUserId());
						user = userEntry.getSystemId();
						pass = userEntry.getPassword();
						break;
					}
				}
			}
			if (user == null || pass == null || scheduleid == null) {
				msg = IConstants.ERROR_HTTP02;
			} else {
				boolean webaccess = true;
				UserService userService = new UserService();
				UserEntryExt entry = userService.getUserEntryExt(user);
				try {
					String readFlag = MultiUtility.readFlag(Constants.USER_FLAG_DIR + user + ".txt");
					// System.out.println("Http : " + user + " Flag -> " + readFlag);
					if (readFlag.contains("404")) {
						System.out.println("Http : " + user + " Blocked <404> ");
						webaccess = false;
					} else {
						if (entry != null) {
							if (!entry.getWebMasterEntry().isApiAccess()) {
								webaccess = false;
							} else {
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
										logger.error(
												user + " Invalid Http Access IPAddress: " + request.getRemoteAddr());
										webaccess = false;
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
						if (pass.equals(entry.getUserEntry().getPassword())) {
							if (dbService.deleteschedule(scheduleid, user)) {
								msg = "Schedule Removed: " + scheduleid;
								System.out.println(user + " Schedule Removed: " + scheduleid);
							} else {
								msg = IConstants.ERROR_HTTP25;
								System.out.println(user + " Schedule Not Found: " + scheduleid);
							}
						} else {
							msg = IConstants.ERROR_HTTP04;
							System.out.println(user + " Invalid Password: " + pass);
						}
					}
				} catch (Exception ex) {
					logger.error(user, ex);
					msg = IConstants.ERROR_HTTP27;
				}
			}
		} catch (Exception e) {
			logger.error(request.getRequestURL().toString(), e);
			msg = IConstants.ERROR_HTTP27;
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
