/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smpp.common.httpclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.hti.rmi.LookupReport;
import com.hti.smpp.common.util.IConstants;

/**
 *
 * @author Administrator
 */
public class LookupResult extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String msg = "";
		List<LookupReport> list = null;
		String batch = request.getParameter("batchid");
		System.out.println(request.getMethod() + " Http Lookup Result Request For Batch : " + batch + " From : "
				+ request.getRemoteAddr());
		if (batch != null && batch.length() > 0) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("batch_id", batch);
			// String sql = "select * from lookup_result where batch_id='" + batch + "'";
			try {
				list = new LookupServiceInvoker().getLookupReport(params);
				if (list.isEmpty()) {
					System.out.println("<- Record not Found -> " + batch);
					msg = IConstants.ERROR_HTTP25;
				} else {
					LookupReport lookup = null;
					Map<String, String> map = null;
					while (!list.isEmpty()) {
						lookup = list.remove(0);
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
					// msg = "Result Count: " + list.size();
				}
			} catch (Exception ex) {
				System.out.println("Query request failed : " + ex);
				msg = IConstants.ERROR_HTTP13;
			}
		} else {
			System.out.println("<- Invalid HTTP Lookup Result URL Format -> ");
			msg = IConstants.ERROR_HTTP01;
		}
		out.print(msg);
	}
}
