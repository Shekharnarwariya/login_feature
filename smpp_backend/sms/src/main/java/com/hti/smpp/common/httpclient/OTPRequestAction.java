package com.hti.smpp.common.httpclient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.smpp.common.user.dto.OTPEntry;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;

public class OTPRequestAction extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(OTPRequestAction.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("OTP Request: " + request.getRemoteAddr() + " [ " + request.getMethod() + " ] "
				+ request.getQueryString());
		String username = request.getParameter("username");
		String status = ResponseCode.NO_ERROR;
		int otp = 0;
		String validity = "";
		if (IConstants.CRM_ACCESS_IP != null) {
			if (IConstants.CRM_ACCESS_IP.contains(request.getRemoteAddr())) {
				logger.info(username + " Valid Api Access Ip: " + request.getRemoteAddr());
			} else {
				logger.error(username + " Invalid Api Access Ip: " + request.getRemoteAddr());
				status = ResponseCode.INVALID_API_ACCESS_IP;
			}
		} else {
			logger.error(username + " Api Access Ip Not Configured");
			status = ResponseCode.ACCESS_DENIED;
		}
		if (username != null) {
			if (!GlobalVars.UserMapping.containsKey(username)) {
				logger.error(username + " Invalid Request User: " + username);
				status = ResponseCode.INVALID_LOGIN;
			}
		} else {
			status = ResponseCode.INVALID_LOGIN;
		}
		if (status.equalsIgnoreCase(ResponseCode.NO_ERROR)) {
			UserService userService=new UserService();
			OTPEntry otpEntry = userService.getOTPEntry(username);
			boolean generate_otp = true;
			if (otpEntry != null) {
				if (otpEntry.getExpiresOn() != null) {
					try {
						if (new Date()
								.after(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(otpEntry.getExpiresOn()))) {
							logger.info(username + " OTP ExpiredOn: " + otpEntry.getExpiresOn());
						} else {
							generate_otp = false;
							otp = otpEntry.getOneTimePass();
							validity = otpEntry.getExpiresOn();
						}
					} catch (Exception ex) {
						logger.error(username + " " + otpEntry, ex);
					}
				}
			}
			if (generate_otp) {
				otp = new Random().nextInt(999999 - 100000) + 100000;
				java.util.Calendar calendar = java.util.Calendar.getInstance();
				if (IConstants.LOGIN_OTP_VALIDITY > 0) {
					calendar.add(Calendar.MINUTE, IConstants.LOGIN_OTP_VALIDITY);
				} else {
					calendar.add(Calendar.MINUTE, 5);
				}
				validity = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
				if (otpEntry != null) {
					otpEntry.setExpiresOn(validity);
					otpEntry.setOneTimePass(otp);
					userService.updateOTPEntry(otpEntry);
				} else {
					userService.saveOTPEntry(new OTPEntry(username, otp, validity));
				}
			}
		}
		JSONObject resp = new JSONObject();
		resp.put("status", status);
		resp.put("otp", String.valueOf(otp));
		resp.put("expiresOn", validity);
		logger.info(resp.toString());
		response.getWriter().print(resp.toString());
	}
}
