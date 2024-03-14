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

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.util.WriteLogThread;

@Service
public class JsonRouteResponse extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(JsonRouteResponse.class);
	@Autowired
	private IDatabaseService dbService;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
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
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
					br = null;
				}
			}
		}
		String decodeUrl = URLDecoder.decode(sbf.toString(), "UTF-8");
		WriteLogThread.logQueue.enqueue(request.getRemoteAddr() + " " + decodeUrl);
		JSONObject parse = new JSONObject(decodeUrl);
		System.out.println(parse.toString());
		out.print("ok");
		try {
			if (parse.has("message_uuid") && parse.has("status")) {
				String responseId = parse.getString("message_uuid");
				String status = parse.getString("status");
				if (status != null && responseId != null) {
					if (status.equalsIgnoreCase("submitted")) {
						System.out.println(responseId + " No need to update vng status: submitted");
					} else {
						if (status.equalsIgnoreCase("delivered")) {
							status = "DELIVRD";
						} else if (status.equalsIgnoreCase("rejected")) {
							status = "REJECTD";
						} else if (status.equalsIgnoreCase("undeliverable")) {
							status = "UNDELIV";
						} else {
							status = "UNKNOWN";
						}
						String statusCode = null, remarks = null;
						if (parse.has("error")) {
							JSONObject error = (JSONObject) parse.get("error");
							if (error.has("title")) {
								statusCode = error.getString("title");
							}
							if (error.has("detail")) {
								remarks = error.getString("detail");
							}
						}
						dbService.updateVngStatus(responseId, status, statusCode, remarks);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("", ex);
		}
	}
}
