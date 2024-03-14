package com.hti.smpp.common.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.WriteXmlLogThread;

@Service
public class XmlResultAction extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(XmlResultAction.class);
	@Autowired
	private IDatabaseService dbService;

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
		StringBuilder sbf = new StringBuilder();
		InputStream is = null;
		BufferedReader br = null;
		String batch_id = "", username = null;
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
		String decodeUrl = URLDecoder.decode(sbf.toString(), "UTF-8");
		WriteXmlLogThread.logQueue.enqueue(request.getRemoteAddr() + " " + decodeUrl);
		List<BaseApiDTO> list = null;
		try {
			list = parseRequest(decodeUrl);
		} catch (IOException e) {
			logger.error("", e.fillInStackTrace());
			status = ResponseCode.IOSTREAM_ERROR;
		} catch (ParserConfigurationException e) {
			logger.error("", e.fillInStackTrace());
			status = ResponseCode.SYSTEM_ERROR;
		} catch (SAXException e) {
			status = ResponseCode.INVALID_REQUEST;
			logger.error("", e.fillInStackTrace());
		}
		List<ApiResultDTO> result = null;
		if (list != null && !list.isEmpty()) {
			BaseApiDTO xmlDTO = list.remove(0);
			logger.info(xmlDTO.getUsername() + ": " + xmlDTO.getWebid() + " " + xmlDTO.toString());
			batch_id = xmlDTO.getWebid();
			status = checkValidations(xmlDTO);
			username = xmlDTO.getUsername();
			if (status.equalsIgnoreCase(ResponseCode.NO_ERROR)) {
				result = dbService.getApiStatus(batch_id, username);
				logger.info(batch_id + " Total API Records : " + result.size());
				if (result.isEmpty()) {
					status = ResponseCode.NO_RECORDS;
				} else {
					List<String> toEnquireList = new ArrayList<String>();
					Map<String, String> destinations = new HashMap<String, String>();
					Iterator<ApiResultDTO> itr = result.iterator();
					while (itr.hasNext()) {
						ApiResultDTO apiResult = itr.next();
						if (apiResult.getMessageid() != null && apiResult.getMessageid().trim().length() > 1) {
							toEnquireList.add(apiResult.getMessageid());
							destinations.put(apiResult.getMessageid(), apiResult.getMsisdn());
							itr.remove();
						}
					}
					logger.info(batch_id + " mis enquire Records : " + toEnquireList);
					if (!toEnquireList.isEmpty()) {
						Map<String, ApiResultDTO> mis_result = null;
						try {
							mis_result = dbService.getDeliveryStatus(username, toEnquireList);
							logger.info("Total Mis Records Found: " + mis_result);
							for (String key : toEnquireList) {
								String destination = destinations.get(key);
								if (mis_result.containsKey(key)) {
									result.add(mis_result.get(key));
								} else {
									result.add(new ApiResultDTO(key, ResponseCode.NO_ERROR, "QUEUED", destination));
								}
							}
						} catch (Exception ex) {
							logger.error(username + ": " + ex);
							for (String key : toEnquireList) {
								result.add(new ApiResultDTO(key, ResponseCode.SYSTEM_ERROR, "", ""));
							}
						}
					}
				}
			}
		}
		StringBuffer xml_response = new StringBuffer();
		xml_response.append("<?xml version='1.0' encoding='UTF-8'?>");
		xml_response.append("<result>");
		xml_response.append("<batch_id>" + batch_id + "</batch_id>");
		if (result != null && !result.isEmpty()) {
			for (ApiResultDTO final_result : result) {
				xml_response.append("<report>");
				xml_response.append("<messageid>" + final_result.getMessageid() + "</messageid>");
				xml_response.append("<msisdn>" + final_result.getMsisdn() + "</msisdn>");
				xml_response.append("<dlr_status>" + final_result.getDlrStatus() + "</dlr_status>");
				xml_response.append("<request_status>" + final_result.getRequestStatus() + "</request_status>");
				if (final_result.getSubmitOn() != null && !final_result.getSubmitOn().isEmpty()) {
					xml_response.append("<submit_date>" + final_result.getSubmitOn() + "</submit_date>");
				} else {
					xml_response.append("<submit_date></submit_date>");
				}
				if (final_result.getDeliverOn() != null && !final_result.getDeliverOn().isEmpty()) {
					xml_response.append("<done_date>" + final_result.getDeliverOn() + "</done_date>");
				} else {
					xml_response.append("<done_date></done_date>");
				}
				xml_response.append("</report>");
			}
		} else {
			xml_response.append("<report>");
			xml_response.append("<messageid></messageid>");
			xml_response.append("<msisdn></msisdn>");
			xml_response.append("<dlr_status></dlr_status>");
			xml_response.append("<request_status>" + status + "</request_status>");
			xml_response.append("<submit_date></submit_date>");
			xml_response.append("<done_date></done_date>");
			xml_response.append("</report>");
		}
		xml_response.append("</result>");
		out.print(xml_response.toString());
	}

	private String checkValidations(BaseApiDTO xmlDTO) {
		String status = ResponseCode.NO_ERROR;
		if (xmlDTO.getAccessKey() != null) {
			com.hazelcast.query.Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
					.get("provCode").equal(xmlDTO.getAccessKey());
			for (WebMasterEntry webEntryItr : GlobalVars.WebmasterEntries.values(p)) {
				UserEntry userEntry = GlobalVars.UserEntries.get(webEntryItr.getUserId());
				xmlDTO.setUsername(userEntry.getSystemId());
				xmlDTO.setPassword(userEntry.getPassword());
				break;
			}
		}
		String username = xmlDTO.getUsername();
		String password = xmlDTO.getPassword();
		String batch_id = xmlDTO.getWebid();
		if (username == null || password == null || batch_id == null) {
			status = ResponseCode.INVALID_REQUEST;
			logger.error(batch_id + " <- Invalid Request Format -> ");
		} else {
			if (username.length() == 0 || username.length() > 16) {
				status = ResponseCode.INVALID_LOGIN;
				logger.error(batch_id + " Invalid Username: " + username);
			} else {
				if (password.length() == 0 || password.length() > 9) {
					status = ResponseCode.INVALID_LOGIN;
					logger.error(batch_id + " Invalid Password: " + password);
				} else if (batch_id.length() < 15) {
					status = ResponseCode.INVALID_BATCHID;
					logger.error(batch_id + " Invalid batch_id: " + batch_id);
				}
			}
		}
		return status;
	}

	private List<BaseApiDTO> parseRequest(String uri) throws ParserConfigurationException, SAXException, IOException {
		logger.info("parsing Request: " + uri);
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();
		XMLResultHandler handler = new XMLResultHandler();
		saxParser.parse(new InputSource(new StringReader(uri)), handler);
		return handler.getBulkList();
	}
}
