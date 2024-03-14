/* 
 **	Copyright 2004 High Tech InfoSystems. All Rights Reserved.
 **	Author		: Satya Prakash [satyaprakash@utils.net]
 **	Created on 	: 12/05/2004
 **	Modified on	: 12/05/2004
 **	Descritpion	: 
 */
package com.hti.smpp.common.httpclient;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.WriteLogThread;

@Service
public class UrlSmsStatusAction extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(UrlSmsStatusAction.class);
	@Autowired
	private IDatabaseService dbService;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// PrintWriter out = response.getWriter();
		String msgid = null;
		String to_return = null;
		try {
			response.setContentType("text/plain");
			// out = response.getWriter();
			msgid = request.getParameter("respid");
			String format = request.getParameter("format");
			String username = request.getParameter("username");
			System.out.println(username + "[" + request.getRemoteAddr() + "] Status Requested For :" + msgid);
			WriteLogThread.logQueue.enqueue(
					"( " + request.getRemoteAddr() + " ) " + request.getMethod() + ":" + request.getQueryString());
			try {
				Long.parseLong(msgid);
			} catch (Exception ex) {
				to_return = IConstants.ERROR_HTTP08;
				System.out.println(username + "[" + request.getRemoteAddr() + "] Invalid MessageId :" + msgid);
			}
			if (to_return == null) {
				String[] result = dbService.getDeliveryStatus(username, msgid);
				if (format != null && format.equalsIgnoreCase("json")) {
					if (result[0] == null) {
						result[0] = IConstants.ERROR_HTTP25;
					}
					JSONObject obj = new JSONObject();
					obj.put("msgid", msgid);
					obj.put("status", result[0]);
					if (result[1] == null) {
						result[1] = " ";
					}
					if (result[2] == null) {
						result[2] = " ";
					}
					obj.put("doneOn", result[1]);
					obj.put("errCode", result[2]);
					to_return = obj.toString();
				} else {
					if (result[0] != null) {
						to_return = "Message ID : " + msgid + " Status :" + result[0];
						if (result[1] != null) {
							to_return += " doneOn :" + result[1];
						}
						if (result[2] != null) {
							to_return += " errCode :" + result[2];
						}
					} else {
						to_return = IConstants.ERROR_HTTP25;
					}
				}
			}
			System.out.println(username + " [" + msgid + "]: " + to_return);
		} catch (Exception e) {
			logger.error(msgid + ": " + e);
			to_return = IConstants.ERROR_HTTP13;
		}
		response.getWriter().println(to_return);
	}
}
