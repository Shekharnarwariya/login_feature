package com.hti.smpp.common.httpclient;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlRequestTest extends HttpServlet {
	private static long counter = 0;
	private Logger logger = LoggerFactory.getLogger(UrlRequestTest.class);

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		counter++;
		String decodeUrl = URLDecoder.decode(request.getQueryString(), "UTF-8");
		logger.info("POST[" + counter + "]: " + decodeUrl);
		/*Enumeration enumer = request.getHeaderNames();
		while (enumer.hasMoreElements()) {
			String header = (String) enumer.nextElement();
			logger.info(header + ": " + request.getHeader(header));
		}*/
		response.getWriter().println("Ok");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		counter++;
		String decodeUrl = URLDecoder.decode(request.getQueryString(), "UTF-8");
		logger.info("GET[" + counter + "]: " + decodeUrl);
		/*Enumeration enumer = request.getHeaderNames();
		while (enumer.hasMoreElements()) {
			String header = (String) enumer.nextElement();
			logger.info(header + ": " + request.getHeader(header));
		}*/
		response.getWriter().println("Ok");
	}
}
