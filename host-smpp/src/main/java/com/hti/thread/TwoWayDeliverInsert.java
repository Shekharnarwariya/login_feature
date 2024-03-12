package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.logica.smpp.pdu.DeliverSM;

public class TwoWayDeliverInsert implements Runnable {
	private boolean stop;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = "insert into 2way_deliver_sm(source,destination,message,smsc) values(?,?,?,?)";

	public TwoWayDeliverInsert() {
		logger.info("2WayDeliverInsert Thread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (GlobalQueue.TwoWayDeliverInsertQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} else {
				DeliverSM deliver;
				int counter = 0;
				try {
					connection = GlobalCache.connnection_pool_1.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!GlobalQueue.TwoWayDeliverInsertQueue.isEmpty()) {
						deliver = (DeliverSM) GlobalQueue.TwoWayDeliverInsertQueue.dequeue();
						String short_msg = deliver.getShortMessage();
						if (short_msg == null) {
							if (deliver.getExtraOptional((short) 0x0424) != null) {
								try {
									short_msg = new String(
											deliver.getExtraOptional((short) 0x0424).getData().getBuffer())
													.substring(4);
								} catch (Exception ex) {
									logger.info(deliver.debugString(), ex);
									continue;
								}
							} else if (deliver.getOptional((short) 0x0424) != null) {
								try {
									short_msg = new String(deliver.getOptional((short) 0x0424).getData().getBuffer())
											.substring(4);
								} catch (Exception ex) {
									logger.info(deliver.debugString(), ex);
									continue;
								}
							} else {
								logger.info("Invalid 2way deliver_sm: " + deliver.debugString());
								continue;
							}
						}
						statement.setString(1, deliver.getSourceAddr().getAddress());
						statement.setString(2, deliver.getDestAddr().getAddress());
						statement.setString(3, short_msg);
						statement.setString(4, deliver.getSmsc());
						statement.addBatch();
						if (++counter > 1000) {
							break;
						}
					}
					if (counter > 0) {
						statement.executeBatch();
						connection.commit();
					}
				} catch (Exception ex) {
					logger.error("run()", ex);
				} finally {
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException e) {
							statement = null;
						}
					}
					GlobalCache.connnection_pool_1.putConnection(connection);
				}
			}
		}
		logger.info("2WayDeliverInsert Thread Stopped. Queue: " + GlobalQueue.TwoWayDeliverInsertQueue.size());
	}

	public void stop() {
		logger.info("2WayDeliverInsert Thread Stopping");
		stop = true;
	}
}
