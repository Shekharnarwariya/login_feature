/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.HTIQueue;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.DeliverSM;
import com.logica.smpp.pdu.WrongLengthOfStringException;
import com.logica.smpp.pdu.tlv.WrongLengthException;

/**
 *
 * @author Administrator
 */
public class WebDeliverProcess implements Runnable {
	private Logger logger = LoggerFactory.getLogger("userLogger");
	private boolean stop;
	private String systemid;
	private String webUrl;
	private HTIQueue dlrQueue;
	private boolean reload = true;
	private List<WebDeliverWorker> workerList;
	private int numberOfQueue = 3;
	private int QueueNumber = 0;
	private HTIQueue[] pduQueue = new HTIQueue[numberOfQueue];
	private boolean terminated;
	private List<DeliverSM> process_list = new ArrayList<DeliverSM>();
	private List<String> temp_list = new ArrayList<String>();
	private boolean webDlrParam;

	public WebDeliverProcess(String systemid, String webUrl, HTIQueue dlrQueue, boolean reload,boolean webDlrParam) {
		logger.info(systemid + " WebDeliverForward Thread Starting");
		this.systemid = systemid;
		this.webUrl = webUrl;
		this.dlrQueue = dlrQueue;
		this.reload = reload;
		this.webDlrParam = webDlrParam;
	}

	public void setReload(boolean reload) {
		logger.info(systemid + " Web Deliver Reload Command ");
		this.reload = reload;
	}

	public void setWebUrl(String webUrl) {
		logger.info(systemid + " Web Deliver Url: " + webUrl);
		this.webUrl = webUrl;
		refreshWorkers();
	}

	public boolean isTerminated() {
		return terminated;
	}

	private void startWorkers() {
		workerList = new ArrayList<WebDeliverWorker>();
		for (int i = 0; i < numberOfQueue; i++) {
			pduQueue[i] = new HTIQueue();
			WebDeliverWorker worker = new WebDeliverWorker(systemid, i + 1, webUrl, pduQueue[i],webDlrParam);
			new Thread(worker, systemid + "_WebDeliverWorker[" + (i + 1) + "]").start();
			workerList.add(worker);
		}
	}

	public int getWorkerQueueSize() {
		int toReturn = 0;
		for (int i = 0; i < numberOfQueue; i++) {
			if (!pduQueue[i].isEmpty()) {
				toReturn += pduQueue[i].size();
			}
		}
		return toReturn;
	}

	private void stopWorkers() {
		for (WebDeliverWorker worker : workerList) {
			worker.stop();
			while (!worker.isTerminated()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void refreshWorkers() {
		for (WebDeliverWorker worker : workerList) {
			worker.setWebUrl(webUrl);
		}
	}

	@Override
	public void run() {
		startWorkers();
		while (!stop) {
			if (dlrQueue.isEmpty()) {
				if (reload) {
					logger.debug(systemid + " Getting DLRs From Database");
					int isMore = putProcessQueue(1000);
					if (isMore < 1000) {
						reload = false;
					}
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
					}
				}
			} else {
				int counter = 0;
				while (!dlrQueue.isEmpty()) {
					pduQueue[QueueNumber].enqueue((DeliverSM) dlrQueue.dequeue());
					if (++QueueNumber >= numberOfQueue) {
						QueueNumber = 0;
					}
					if (++counter > 1000) {
						break;
					}
				}
			}
		}
		stopWorkers();
		if (!dlrQueue.isEmpty()) {
			logger.info(systemid + " Pending(To Be Forward) WebDLR : " + dlrQueue.size());
			while (!dlrQueue.isEmpty()) {
				GlobalQueue.DLRInsertQueue.add((DeliverSM) dlrQueue.dequeue());
			}
		}
		logger.info(systemid + " WebDeliverForward Thread Stopped.");
		terminated = true;
	}

	private int putProcessQueue(int limit) {
		logger.debug(systemid + " putProcessQueue()");
		int count = 0;
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		DeliverSM deliverSM;
		// String msg_id = null;
		String sql = "select * from backup_response where server_id=" + GlobalVars.SERVER_ID
				+ " and client_name=? limit " + limit;
		try {
			connection = GlobalCache.connection_pool_user.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, systemid);
			rs = statement.executeQuery();
			// String client_name = null;
			while (rs.next()) {
				deliverSM = new DeliverSM();
				deliverSM.setEsmClass((byte) Data.SM_SMSC_DLV_RCPT_TYPE);
				// deliverSM.setDataCoding((byte) 0x03); // ISO-Latin-1
				deliverSM.setServerId(rs.getInt("server_id"));
				deliverSM.setClientName(rs.getString("client_name"));
				deliverSM.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, rs.getString("destination"));
				deliverSM.setSourceAddr((byte) 5, (byte) 0, rs.getString("source"));
				try {
					deliverSM.setReceiptedMessageId(rs.getString("msg_id"));
				} catch (WrongLengthException wle) {
					logger.error(rs.getString("client_name") + ":-> WrongLengthException For Deliver_SM: "
							+ rs.getString("msg_id"));
				}
				try {
					deliverSM.setShortMessage(rs.getString("pdu"));
				} catch (WrongLengthOfStringException wlse) {
					logger.error(rs.getString("client_name") + ":-> WrongLengthOfStringException For Deliver_SM: "
							+ rs.getString("msg_id"));
				}
				process_list.add(deliverSM);
				temp_list.add(rs.getString("msg_id"));
				count++;
			}
			logger.debug(systemid + " web deliver process list: " + process_list.size());
			if (!temp_list.isEmpty()) {
				sql = "delete from backup_response where msg_id in(" + String.join(",", temp_list) + ")";
				statement = connection.prepareStatement(sql);
				int delete_count = statement.executeUpdate();
				logger.info(systemid + " web backup_response removed : " + delete_count + " Requested: "
						+ temp_list.size());
				temp_list.clear();
			}
		} catch (Exception q) {
			logger.error("putProcessQueue(" + systemid + ")", q.fillInStackTrace());
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
		while (!process_list.isEmpty()) {
			dlrQueue.enqueue((DeliverSM) process_list.remove(0));
		}
		logger.debug(systemid + " Web DLR Enqueued Count: " + dlrQueue.size());
		return count;
	}

	public void stop() {
		logger.info(systemid + " WebDeliverForward Thread Stopping <" + dlrQueue.size() + ">");
		stop = true;
	}
}
