/* 
**	Copyright 2004 High Tech InfoSystems. All Rights Reserved.
**	Author		: Satya Prakash [satyaprakash@utils.net]
**	Created on 	: 12/05/2004
**	Modified on	: 12/05/2004
**	Descritpion	: 
*/
package com.hti.smpp.common.httpclient;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebDLRAction extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			String msg_id = request.getParameter("MessageId");
			String Source = request.getParameter("Source");
			String Destination = request.getParameter("Destination");
			String Status = request.getParameter("Status");
			String SubmitDate = request.getParameter("SubmitDate");
			String DoneDate = request.getParameter("DoneDate");
			String mcc = request.getParameter("mcc");
			String mnc = request.getParameter("mnc");
			System.out.println("DLR:-> " + msg_id + ":" + Source + ":" + Destination + ":" + Status + ":" + SubmitDate
					+ ":" + DoneDate + ":" + mcc + ":" + mnc);
			String msg = null;
			if ((msg_id != null) && (msg_id.length() > 0)) {
				msg = "ok";
			} else {
				msg = "NOT OK";
			}
			out.print(msg);
			// System.out.println(msg);
		} catch (Exception e) {
			System.out.println("Exception in WebDLRAction::" + e);
			e.printStackTrace();
		}
	}
}
