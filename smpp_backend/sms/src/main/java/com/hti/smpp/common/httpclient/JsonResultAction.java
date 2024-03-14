package com.hti.smpp.common.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.WriteLogThread;

@Service
public class JsonResultAction extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(JsonResultAction.class);
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
		WriteLogThread.logQueue.enqueue(decodeUrl);
		BaseApiDTO jsonDTO = null;
		try {
			jsonDTO = parseRequest(decodeUrl);
			logger.debug("<--- Request Format is Correct -->");
		} catch (JSONException e) {
			status = ResponseCode.INVALID_REQUEST;
			logger.error(batch_id, e.fillInStackTrace());
		} catch (InvalidFormatException e) {
			status = e.getMessage();
			logger.error(batch_id, e.getMessage());
		} catch (Exception e) {
			logger.error(batch_id, e.fillInStackTrace());
			status = ResponseCode.SYSTEM_ERROR;
		}
		List<ApiResultDTO> result = null;
		if (jsonDTO != null) {
			logger.info(jsonDTO.getUsername() + ": " + jsonDTO.getWebid() + " " + jsonDTO.toString());
			batch_id = jsonDTO.getWebid();
			username = jsonDTO.getUsername();
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
							if (mis_result.containsKey(key)) {
								result.add(mis_result.get(key));
							} else {
								result.add(
										new ApiResultDTO(key, ResponseCode.NO_ERROR, "QUEUED", destinations.get(key)));
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
		JSONObject resp_object = new JSONObject();
		JSONObject status_object = new JSONObject();
		status_object.put("batch_id", batch_id);
		JSONArray reports = new JSONArray();
		JSONObject report = null;
		if (result != null && !result.isEmpty()) {
			for (ApiResultDTO final_result : result) {
				report = new JSONObject();
				report.put("messageid", final_result.getMessageid());
				report.put("msisdn", final_result.getMsisdn());
				report.put("dlr_status", final_result.getDlrStatus());
				report.put("request_status", final_result.getRequestStatus());
				if (final_result.getSubmitOn() != null && !final_result.getSubmitOn().isEmpty()) {
					report.put("submit_date", final_result.getSubmitOn());
				} else {
					report.put("submit_date", "");
				}
				if (final_result.getDeliverOn() != null && !final_result.getDeliverOn().isEmpty()) {
					report.put("done_date", final_result.getDeliverOn());
				} else {
					report.put("done_date", "");
				}
				reports.put(report);
			}
		} else {
			report = new JSONObject();
			report.put("messageid", " ");
			report.put("msisdn", " ");
			// check if its schedule
			BaseApiDTO scheduleEntry = dbService.getApiSchedule(username, batch_id);
			if (scheduleEntry != null) {
				report.put("dlr_status", "SCHEDLD");
				report.put("request_status", ResponseCode.NO_ERROR);
				report.put("submit_date", scheduleEntry.getScheduleTime());
			} else {
				report.put("dlr_status", " ");
				report.put("request_status", status);
				report.put("submit_date", "");
			}
			report.put("done_date", "");
			reports.put(report);
		}
		status_object.put("report", reports);
		resp_object.put("result", status_object);
		out.print(resp_object.toString());
	}

	private BaseApiDTO parseRequest(String uri) throws JSONException, InvalidFormatException {
		logger.info("parsing Request: " + uri);
		BaseApiDTO json = new BaseApiDTO();
		JSONObject parse = new JSONObject(uri);
		JSONObject enquiry = parse.getJSONObject("enquiry");
		if (((enquiry.has("username") && enquiry.has("password")) || enquiry.has("accesskey"))
				&& enquiry.has("batch_id")) {
			String batch_id = enquiry.getString("batch_id");
			if (enquiry.has("username") && enquiry.has("password")) {
				String username = enquiry.getString("username");
				String password = enquiry.getString("password");
				if (username.length() == 0 || username.length() > 16) {
					logger.error(batch_id + " Invalid Username: " + username);
					throw new InvalidFormatException(ResponseCode.INVALID_LOGIN);
				} else if (password.length() == 0 || password.length() > 9) {
					logger.error(batch_id + " Invalid Password: " + password);
					throw new InvalidFormatException(ResponseCode.INVALID_LOGIN);
				}
				json.setUsername(username);
				json.setPassword(password);
			} else {
				String accessKey = enquiry.getString("accesskey");
				if (accessKey.length() != 15) {
					logger.error(batch_id + " Invalid AccessKey: " + accessKey);
					throw new InvalidFormatException(ResponseCode.INVALID_LOGIN);
				} else {
					boolean found = false;
					com.hazelcast.query.Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl()
							.getEntryObject().get("provCode").equal(accessKey);
					for (WebMasterEntry webEntryItr : GlobalVars.WebmasterEntries.values(p)) {
						UserEntry userEntry = GlobalVars.UserEntries.get(webEntryItr.getUserId());
						json.setUsername(userEntry.getSystemId());
						json.setPassword(userEntry.getPassword());
						found = true;
						break;
					}
					if (!found) {
						logger.info(batch_id + " Invalid AccessKey: " + accessKey);
						throw new InvalidFormatException(ResponseCode.INVALID_LOGIN);
					}
				}
			}
			if (batch_id.length() < 15) {
				logger.error(batch_id + " Invalid batch_id: " + batch_id);
				throw new InvalidFormatException(ResponseCode.INVALID_BATCHID);
			}
			json.setWebid(batch_id);
		} else {
			throw new InvalidFormatException(ResponseCode.INVALID_REQUEST);
		}
		return json;
	}
}
