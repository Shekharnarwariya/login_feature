package com.hti.user;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.HTIQueue;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.logica.smpp.pdu.DeliverSM;

public class WebDeliverWorker implements Runnable {
	private boolean stop;
	private Logger logger = LoggerFactory.getLogger("userLogger");
	private Logger dlrlogger = LoggerFactory.getLogger("userDlrLogger");
	private HTIQueue dlrQueue;
	private String system_id;
	private int threadNumber;
	private String webUrl;
	private CloseableHttpAsyncClient client;
	private Map<String, String> url_list = new HashMap<String, String>();
	private Map<String, DeliverSM> dlr_list = new HashMap<String, DeliverSM>();
	private boolean terminated;
	private boolean webDlrParam;

	public WebDeliverWorker(String system_id, int threadNumber, String webUrl, HTIQueue dlrQueue, boolean webDlrParam) {
		this.system_id = system_id;
		this.threadNumber = threadNumber;
		this.webUrl = webUrl;
		this.dlrQueue = dlrQueue;
		this.webDlrParam = webDlrParam;
		logger.info(system_id + "_WebDeliverWorker[" + threadNumber + "] Starting");
	}

	public void setWebUrl(String webUrl) {
		logger.info(system_id + "_WebDeliverWorker[" + threadNumber + "] Web Deliver Url: " + webUrl);
		this.webUrl = webUrl;
	}

	public boolean isTerminated() {
		return terminated;
	}

	@Override
	public void run() {
		while (!stop) {
			if (dlrQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} else {
				DeliverSM deliver_sm = null;
				String urlString = "", short_messege = "", messageId = "", status = "", sd = "", dd = "";
				int counter = 0;
				boolean isNNC = false;
				String mcc = "", mnc = "";
				while (!dlrQueue.isEmpty()) {
					urlString = "";
					deliver_sm = (DeliverSM) dlrQueue.dequeue();
					short_messege = deliver_sm.getShortMessage();
					messageId = short_messege
							.substring(short_messege.indexOf("id:") + 3, short_messege.indexOf(" sub:")).trim();
					status = short_messege.substring(short_messege.indexOf("stat:") + 5, short_messege.indexOf(" err:"))
							.trim();
					sd = short_messege
							.substring(short_messege.indexOf("submit date:") + 12, short_messege.indexOf(" done date:"))
							.trim();
					if (short_messege.contains("mcc")) {
						dd = short_messege
								.substring(short_messege.indexOf("done date:") + 10, short_messege.indexOf(" mcc:"))
								.trim();
						mcc = short_messege.substring(short_messege.indexOf("mcc:") + 4, short_messege.indexOf(" mnc:"))
								.trim();
						mnc = short_messege
								.substring(short_messege.indexOf("mnc:") + 4, short_messege.indexOf(" stat:")).trim();
						isNNC = true;
					} else {
						dd = short_messege
								.substring(short_messege.indexOf("done date:") + 10, short_messege.indexOf(" stat:"))
								.trim();
						isNNC = false;
					}
					// String respContent = "";
					try {
						urlString += "MessageId=" + messageId + "&Source="
								+ URLEncoder.encode(deliver_sm.getSourceAddr().getAddress(), "UTF-8") + "&Destination="
								+ URLEncoder.encode(deliver_sm.getDestAddr().getAddress(), "UTF-8") + "&Status="
								+ URLEncoder.encode(status, "UTF-8") + "&SubmitDate=" + sd + "&DoneDate=" + dd;
						if (isNNC) {
							urlString += "&mcc=" + mcc + "&mnc=" + mnc;
						}
						if (webDlrParam) {
							if (GlobalCache.HttpDlrParam.containsKey(messageId)) {
								urlString += "&" + GlobalCache.HttpDlrParam.remove(messageId);
							} else { // check in table http_dlr_param
								String web_dlr_param = com.hti.util.GlobalVars.userService.getWebDlrParam(messageId);
								if (web_dlr_param != null) {
									urlString += "&" + web_dlr_param;
								}
							}
						}
						url_list.put(messageId, urlString);
						dlr_list.put(messageId, deliver_sm);
					} catch (UnsupportedEncodingException e) {
						logger.error(messageId, e);
					}
					// ------------------------------------------------------
					if (++counter >= 100 || stop) {
						break;
					}
				}
				if (!stop) {
					if (!url_list.isEmpty()) {
						try {
							asyncDlr();
						} catch (Exception e) {
							logger.error("run(" + system_id + ")", e);
							for (DeliverSM deliverSM : dlr_list.values()) {
								GlobalQueue.DLRInsertQueue.add(deliverSM);
							}
						}
					}
				}
				url_list.clear();
				dlr_list.clear();
			}
		}
		if (!dlrQueue.isEmpty()) {
			logger.info(system_id + "_WebDeliverWorker[" + threadNumber + "] Pending(To Be Forward) WebDLR : "
					+ dlrQueue.size());
			while (!dlrQueue.isEmpty()) {
				GlobalQueue.DLRInsertQueue.add((DeliverSM) dlrQueue.dequeue());
			}
		}
		logger.info(system_id + "_WebDeliverWorker[" + threadNumber + "] Stopped");
		terminated = true;
	}

	private void asyncDlr() {
		// int submit_counter = 0;
		client = HttpAsyncClients.createDefault();
		client.start();
		// logger.info(system_id + "[" + threadNumber + "]" + "<--- Submitting -->");
		Map<String, String> web_id_result = getWebId(url_list.keySet());
		Map<String, Future<HttpResponse>> responses = new HashMap<String, Future<HttpResponse>>();
		HttpGet get = null;
		for (Map.Entry<String, String> entry : url_list.entrySet()) {
			String web_dlr_url = entry.getValue();
			if (web_id_result.containsKey(entry.getKey())) {
				web_dlr_url += "&BatchId=" + web_id_result.get(entry.getKey());
			}
			get = new HttpGet(webUrl + "?" + web_dlr_url);
			get.addHeader("Cache-Control", "max-age=86400");
			get.addHeader("Content-type", "utf-8");
			dlrlogger.info(system_id + ": " + webUrl + "?" + web_dlr_url);
			responses.put(entry.getKey(), client.execute(get, null));
			// submit_counter++;
		}
		// logger.info(system_id + "[" + threadNumber + "]" + " Submitted:-> " + submit_counter);
		for (Map.Entry<String, Future<HttpResponse>> entry : responses.entrySet()) {
			String respContent = "";
			String messageId = entry.getKey();
			try {
				HttpResponse resp = entry.getValue().get(); // wait for the response
				HttpEntity entity = resp.getEntity();
				String response = EntityUtils.toString(entity);
				if (response != null && response.toLowerCase().contains("ok")) {
					respContent = "<" + system_id + ":--> " + messageId + " < Web Deliver Response > <" + response
							+ "> ";
					GlobalQueue.backupRespLogQueue.add((DeliverSM) dlr_list.remove(messageId));
				} else {
					respContent = "<" + system_id + ":--> " + messageId + " < Unexpected Web Deliver Response > <"
							+ response + "> ";
					GlobalQueue.DLRInsertQueue.add((DeliverSM) dlr_list.remove(messageId));
				}
			} catch (Exception e) {
				respContent = system_id + ":--> " + messageId + " < Web Deliver Error > < " + e.getMessage() + " >";
				GlobalQueue.DLRInsertQueue.add((DeliverSM) dlr_list.remove(messageId));
			}
			System.out.println(respContent);
			dlrlogger.info(respContent);
			if (stop) {
				break;
			}
		}
		try {
			client.close();
		} catch (IOException e) {
			logger.error(system_id + "[" + threadNumber + "]" + e);
		}
	}

	private Map<String, String> getWebId(Set<String> msg_id_set) {
		Map<String, String> result = new HashMap<String, String>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String sql = "select msg_id,web_id from api_status where msg_id in(" + String.join(",", msg_id_set) + ")";
		try {
			connection = GlobalCache.connection_pool_user.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			while (rs.next()) {
				if (rs.getLong("web_id") > 0) {
					result.put(rs.getString("msg_id"), rs.getString("web_id"));
				}
			}
		} catch (Exception q) {
			logger.error("getWebId(" + system_id + ")", q.fillInStackTrace());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException sqle) {
			}
			GlobalCache.connection_pool_user.putConnection(connection);
		}
		return result;
	}

	public void stop() {
		logger.info(system_id + "_WebDeliverWorker[" + threadNumber + "] Stopping");
		stop = true;
	}
}
