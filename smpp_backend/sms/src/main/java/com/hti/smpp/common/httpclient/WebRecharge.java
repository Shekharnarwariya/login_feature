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
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

public class WebRecharge extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(WebRecharge.class);

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
				logger.info("Api Recharge Request: " + request.getRemoteAddr() + " [ " + request.getMethod() + " ] "
						+ decodeUrl);
				JSONObject json = new JSONObject(decodeUrl);
				String username = null, balanceMode = null, password = null, targetUser = null, amount = null,
						operation = null;
				if (json.has("username")) {
					username = json.getString("username");
				}
				if (json.has("password")) {
					password = json.getString("password");
				}
				if (json.has("target_user")) {
					targetUser = json.getString("target_user");
				}
				if (json.has("amount")) {
					amount = json.getString("amount");
				}
				if (json.has("operation")) {
					operation = json.getString("operation");
				}
				if (json.has("balance_mode")) {
					balanceMode = json.getString("balance_mode");
				}
				WebRechargeEntry entry = new WebRechargeEntry(username, password, targetUser, operation, amount,
						request.getRemoteAddr(), balanceMode);
				status = checkValidations(entry);
				if (status.equalsIgnoreCase(ResponseCode.NO_ERROR)) {
					status = new WebApiService().recharge(entry);
				} else {
					logger.info(" Validation Failed: " + entry);
				}
			}
		} catch (Exception e) {
			logger.error("", e.fillInStackTrace());
			status = ResponseCode.SYSTEM_ERROR;
		}
		JSONObject resp = new JSONObject();
		resp.put("status", status);
		out.print(resp.toString());
	}

	private String checkValidations(WebRechargeEntry entry) {
		logger.info("Checking For Validations: " + entry);
		String username = entry.getMaster();
		int otp_password = 0;
		try {
			otp_password = Integer.parseInt(entry.getPassword());
		} catch (Exception ex) {
			logger.error(username, ex);
			return ResponseCode.INVALID_OTP;
		}
		if (IConstants.CRM_ACCESS_IP != null) {
			if (IConstants.CRM_ACCESS_IP.contains(entry.getRemoteAddr())) {
				logger.info(username + " Valid Api Access Ip: " + entry.getRemoteAddr());
			} else {
				logger.error(username + " Invalid Api Access Ip: " + entry.getRemoteAddr());
				return ResponseCode.INVALID_API_ACCESS_IP;
			}
		} else {
			logger.error(username + " Api Access Ip Not Configured");
			return ResponseCode.ACCESS_DENIED;
		}
		if (username == null || entry.getPassword() == null || entry.getTargetUser() == null
				|| entry.getAmount() == null || entry.getOperation() == null) {
			logger.info("Invalid Request Parameters.");
			return ResponseCode.INVALID_REQUEST;
		} else {
			try {
				double amount = Double.parseDouble(entry.getAmount());
				if (amount <= 0) {
					logger.error(username + " Invalid Amount: " + entry.getAmount());
					return ResponseCode.INVALID_AMOUNT;
				}
			} catch (Exception e) {
				logger.error(username + " Invalid Amount: " + entry.getAmount());
				return ResponseCode.INVALID_AMOUNT;
			}
			if (entry.getOperation().equalsIgnoreCase("plus") || entry.getOperation().equalsIgnoreCase("minus")) {
				logger.info(username + " Valid Operation: " + entry.getOperation());
			} else {
				logger.error(username + " Invalid Operation: " + entry.getOperation());
				return ResponseCode.INVALID_OPERATION;
			}
			if (username.length() == 0 || username.length() > 16) {
				logger.error("Invalid Username: " + username);
				return ResponseCode.INVALID_LOGIN;
			}
			if (!GlobalVars.UserMapping.containsKey(entry.getTargetUser())) {
				logger.error(username + " Invalid Target User: " + entry.getTargetUser());
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
							if (userEntry.getUserEntry().getRole().equalsIgnoreCase("superadmin")
									|| userEntry.getUserEntry().getRole().equalsIgnoreCase("system")) {
								if (entry.getBalanceMode() != null && (entry.getBalanceMode().equalsIgnoreCase("credit")
										|| entry.getBalanceMode().equalsIgnoreCase("wallet"))) {
									logger.info(username + "[" + userEntry.getUserEntry().getRole()
											+ "] Valid Balance mode: " + entry.getBalanceMode());
									entry.setRole(userEntry.getUserEntry().getRole());
								} else {
									logger.error(username + "[" + userEntry.getUserEntry().getRole()
											+ "] InValid Balance mode: " + entry.getBalanceMode());
									return ResponseCode.INVALID_BALANCE_MODE;
								}
								if (userEntry.getUserEntry().getRole().equalsIgnoreCase("system")) {
									UserEntry targetUserEntry = userService.getUserEntry(entry.getTargetUser());
									if (targetUserEntry.getRole().equalsIgnoreCase("superadmin")
											|| targetUserEntry.getRole().equalsIgnoreCase("system")) {
										logger.error(username + " System User Can't Recharge: " + entry.getTargetUser()
												+ " [" + targetUserEntry.getRole() + "]");
										return ResponseCode.INVALID_TARGET_USER;
									}
								}
							} else {
								if (userEntry.getUserEntry().getRole().equalsIgnoreCase("admin")) {
									UserEntry targetUserEntry = userService.getUserEntry(entry.getTargetUser());
									if (targetUserEntry.getRole().equalsIgnoreCase("superadmin")
											|| targetUserEntry.getRole().equalsIgnoreCase("system")) {
										logger.error(username + " Reseller Can't Recharge: " + entry.getTargetUser()
												+ " [" + targetUserEntry.getRole() + "]");
										return ResponseCode.INVALID_TARGET_USER;
									}
									if (!targetUserEntry.getMasterId().equalsIgnoreCase(username)) {
										logger.error(username + "[" + userEntry.getUserEntry().getRole()
												+ "] Invalid Child User: " + entry.getTargetUser());
										return ResponseCode.ACCESS_DENIED;
									} else {
										entry.setRole(userEntry.getUserEntry().getRole());
									}
								} else {
									logger.error(username + "[" + userEntry.getUserEntry().getRole()
											+ "] Unauthorized User");
									return ResponseCode.ACCESS_DENIED;
								}
							}
							OTPEntry otpEntry = userService.getOTPEntry(username);
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
									logger.error(username + " Invalid OTPPassword: " + entry.getPassword());
									return ResponseCode.INVALID_OTP;
								}
							} else {
								logger.error(username + " Invalid OTPPassword: " + entry.getPassword());
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
		}
		logger.info(username + " Validation Finished");
		return ResponseCode.NO_ERROR;
	}
}
