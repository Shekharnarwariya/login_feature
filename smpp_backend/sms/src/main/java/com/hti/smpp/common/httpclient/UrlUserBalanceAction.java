/* 
 **	Copyright 2004 High Tech InfoSystems. All Rights Reserved.
 **	Author		: Satya Prakash [satyaprakash@utils.net]
 **	Created on 	: 12/05/2004
 **	Modified on	: 12/05/2004
 **	Descritpion	:
 */
package com.hti.smpp.common.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.smpp.common.util.WriteLogThread;

public class UrlUserBalanceAction extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(UrlUserBalanceAction.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String decodeUrl = URLDecoder.decode(request.getQueryString(), "UTF-8");
		WriteLogThread.logQueue.enqueue("GET: " + request.getRemoteAddr() + ": " + decodeUrl);
		request.setAttribute("userid", request.getParameter("userid"));
		request.setAttribute("password", request.getParameter("password"));
		request.setAttribute("accesskey", request.getParameter("accesskey"));
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getQueryString() != null) {
			String decodeUrl = URLDecoder.decode(request.getQueryString(), "UTF-8");
			WriteLogThread.logQueue.enqueue("POST: " + request.getRemoteAddr() + ": " + decodeUrl);
			request.setAttribute("userid", request.getParameter("userid"));
			request.setAttribute("password", request.getParameter("password"));
			request.setAttribute("accesskey", request.getParameter("accesskey"));
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
		try {
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			String userid = (String) request.getAttribute("userid");
			String password = (String) request.getAttribute("password");
			String accesskey = (String) request.getAttribute("accesskey");
			System.out.println("[" + request.getRemoteAddr() + "] " + userid + "  balance Request");
			String msg = null;
			if (accesskey != null && accesskey.length() == 15) {
				msg = new WebApiService().getUrlUserBalance(accesskey);
			} else {
				msg = new WebApiService().getUrlUserBalance(userid, password);
			}
			if (msg == null || msg.equalsIgnoreCase("INVALID USER")) {
				msg = "INVALID USER/PASSWORD";
			}
			out.println(msg);
		} catch (Exception e) {
			logger.error(request.getRequestURL().toString(), e);
		}
	}
}
