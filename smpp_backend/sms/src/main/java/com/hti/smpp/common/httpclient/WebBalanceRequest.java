package com.hti.smpp.common.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.smpp.common.user.dto.OTPEntry;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

public class WebBalanceRequest extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(WebBalanceRequest.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String status = ResponseCode.NO_ERROR;
		PrintWriter out = response.getWriter();
		String username = null, password = null, targetUser = null, balance_mode = " ", amount = " ";
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
				String decodeUrl = URLDecoder.decode(sbf.toString(), "UTF-8");
				logger.info("Api Balance Request: " + request.getRemoteAddr() + " [ " + request.getMethod() + " ] "
						+ decodeUrl);
				JSONObject json = new JSONObject(decodeUrl);
				if (json.has("username")) {
					username = json.getString("username");
				}
				if (json.has("password")) {
					password = json.getString("password");
				}
				if (json.has("target_user")) {
					targetUser = json.getString("target_user");
				}
				status = validate(username, password, targetUser, request.getRemoteAddr());
				if (status.equalsIgnoreCase(ResponseCode.NO_ERROR)) {
					JSONObject balance_resp = new WebApiService().checkBalance(targetUser);
					if (balance_resp != null) {
						if (balance_resp.has("balance_mode")) {
							balance_mode = balance_resp.getString("balance_mode");
						}
						if (balance_resp.has("amount")) {
							amount = balance_resp.getString("amount");
						}
					}
				} else {
					logger.info(" Validation Failed: " + username);
				}
			}
		} catch (Exception e) {
			logger.error("", e.fillInStackTrace());
			status = ResponseCode.SYSTEM_ERROR;
		}
		JSONObject resp = new JSONObject();
		resp.put("status", status);
		resp.put("target_user", targetUser);
		resp.put("balance_mode", balance_mode);
		resp.put("amount", amount);
		logger.info(resp.toString());
		out.print(resp.toString());
	}

	private String validate(String username, String password, String targetUser, String remoteAddr) {
		logger.info("Checking For Validations: " + username);
		if (IConstants.CRM_ACCESS_IP != null) {
			if (IConstants.CRM_ACCESS_IP.contains(remoteAddr)) {
				logger.info(username + " Valid Api Access Ip: " + remoteAddr);
			} else {
				logger.error(username + " Invalid Api Access Ip: " + remoteAddr);
				return ResponseCode.INVALID_API_ACCESS_IP;
			}
		} else {
			logger.error(username + " Api Access Ip Not Configured");
			return ResponseCode.ACCESS_DENIED;
		}
		if (username == null || password == null || targetUser == null) {
			logger.info("Invalid Request Parameters.");
			return ResponseCode.INVALID_REQUEST;
		}
		if (!GlobalVars.UserMapping.containsKey(targetUser)) {
			logger.error(username + " Invalid Target User: " + targetUser);
			return ResponseCode.INVALID_TARGET_USER;
		}
		try {
			// --------- Checking if Account blocked --------
			String readFlag = MultiUtility.readFlag(Constants.USER_FLAG_DIR + username + ".txt");
			// System.out.println("Http : " + user + " Flag -> " + readFlag);
			if (readFlag.contains("404")) {
				logger.info(username + " Blocked <404> ");
				return ResponseCode.ACCESS_DENIED;
			} else {
				UserService userService = new UserService();
				UserEntryExt userEntry = userService.getUserEntryExt(username);
				if (userEntry != null) {
					if (!userEntry.getWebMasterEntry().isApiAccess()) {
						logger.info(username + " ApiAccess Denied");
						return ResponseCode.ACCESS_DENIED;
					} else {
						OTPEntry otpEntry = userService.getOTPEntry(username);
						int otp_password = 0;
						try {
							otp_password = Integer.parseInt(password);
						} catch (Exception ex) {
							logger.error(username, ex);
							return ResponseCode.INVALID_OTP;
						}
						if (otpEntry != null) {
							if (otpEntry.getOneTimePass() == otp_password) {
								String expiresOn = otpEntry.getExpiresOn();
								if (expiresOn != null) {
									Date expiredDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(expiresOn);
									if (new Date().after(expiredDate)) {
										logger.error(username + " OTP Expired: " + expiresOn);
										return ResponseCode.INVALID_OTP;
									}
								} else {
									logger.error(username + " Invalid OTP Expiry: " + expiresOn);
									return ResponseCode.INVALID_OTP;
								}
							} else {
								logger.error(username + " Invalid OTPPassword: " + password);
								return ResponseCode.INVALID_OTP;
							}
						} else {
							logger.error(username + " Invalid OTPPassword: " + password);
							return ResponseCode.INVALID_OTP;
						}
					}
				} else {
					logger.error(username + " User Record Not Found");
					return ResponseCode.INVALID_LOGIN;
				}
			}
		} catch (Exception ex) {
			logger.error(username, ex.fillInStackTrace());
			return ResponseCode.SYSTEM_ERROR;
		}
		return ResponseCode.NO_ERROR;
	}
}
