package com.hti.smpp.common.httpclient;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsgbrdHlrResult extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(MsgbrdHlrResult.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info(request.getQueryString());
		String msg_id = request.getParameter("id");
		String reference = request.getParameter("reference");
		String Destination = request.getParameter("msisdn");
		String Status = request.getParameter("status");
		String SubmitDate = request.getParameter("createdDatetime");
		String DoneDate = request.getParameter("statusDatetime");
		System.out.println("Result:-> " + msg_id + ":" + reference + ":" + Destination + ":" + Status + ":" + SubmitDate
				+ ":" + DoneDate);
		response.getWriter().print("Ok");
	}
}
