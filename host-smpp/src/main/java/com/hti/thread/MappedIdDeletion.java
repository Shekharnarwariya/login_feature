package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.GlobalCache;

public class MappedIdDeletion implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	public static boolean DELETE_OLD_RECORDS;
	private boolean stop;
	List<String> temp_list = new ArrayList<String>();
	private Connection connection = null;
	private PreparedStatement statement = null;
	// private String sql = null;
	private int count = 0;

	public MappedIdDeletion() {
		logger.info("MappedId deletion Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
			}
			if (DELETE_OLD_RECORDS) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, -3);
				String max_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime());
				max_msg_id = max_msg_id + "0000000000000";
				String deletesql = "delete from mapped_id where msg_id < " + max_msg_id + " limit 25000";
				logger.info(deletesql);
				try {
					connection = GlobalCache.connnection_pool_2.getConnection();
					statement = connection.prepareStatement(deletesql);
					int deletecount = statement.executeUpdate();
					logger.info("mapped_id Deletion Count : " + deletecount);
					if (deletecount < 25000) {
						DELETE_OLD_RECORDS = false;
						logger.info("<-- No Records to delete [mapped_id] -->");
					}
				} catch (Exception ex) {
					logger.error("process(1)", ex.fillInStackTrace());
				} finally {
					GlobalCache.connnection_pool_2.putConnection(connection);
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException ex) {
							statement = null;
						}
					}
				}
			}
			if (!GlobalCache.recievedDlrResponseId.isEmpty()) {
				count = 0;
				while (!GlobalCache.recievedDlrResponseId.isEmpty()) {
					temp_list.add((String) GlobalCache.recievedDlrResponseId.remove(0));
					if (++count >= 1000) {
						break;
					}
				}
				if (!temp_list.isEmpty()) {
					try {
						connection = GlobalCache.connnection_pool_2.getConnection();
						statement = connection.prepareStatement(
								"delete from mapped_id where response_id in('" + String.join("','", temp_list) + "')");
						int delete_count = statement.executeUpdate();
						logger.debug("mapped_id Records removed : " + delete_count + " Requested: " + temp_list.size());
					} catch (Exception ex) {
						logger.error("process(2)", ex);
					} finally {
						GlobalCache.connnection_pool_2.putConnection(connection);
						if (statement != null) {
							try {
								statement.close();
							} catch (SQLException ex) {
								statement = null;
							}
						}
						connection = null;
					}
					temp_list.clear();
				}
			}
		}
	}

	public void stop() {
		logger.info("MappedId deletion Stopping.Queue: " + GlobalCache.recievedDlrResponseId.size());
		stop = true;
	}
}
