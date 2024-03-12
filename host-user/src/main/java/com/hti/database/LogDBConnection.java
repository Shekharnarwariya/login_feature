/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.GlobalVars;

/**
 *
 * @author Administrator
 */
public class LogDBConnection {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private final List<Connection> connectionlist = new ArrayList<Connection>();
	private int MAX_CONNECTION = 10;

	public LogDBConnection() {
	}

	public synchronized Connection openConnection() {
		boolean isCreate = false;
		Connection connection = null;
		if (!connectionlist.isEmpty()) {
			connection = (Connection) connectionlist.remove(0);
			if (connection != null) {
				try {
					if (!connection.isValid(0)) {
						logger.warn("<--- Invalid Log Database Connection Found  --->");
						isCreate = true;
					}
				} catch (SQLException ex) {
					isCreate = true;
					logger.warn("<- SQLException While Validating Log Connection -> ");
				}
			}
			if (isCreate) {
				logger.warn("<--- Creating Additional Log Connection.Pooled:-> " + connectionlist.size());
				connection = createConnection();
			}
		} else {
			logger.warn("<--- No Pool Log Connection Found --->");
			while (true) {
				try {
					logger.info("<-- Waiting For Log Database Connection --> ");
					wait();
					logger.info("<-- Exit Waiting For Log Database Connection --> ");
				} catch (InterruptedException ex) {
					logger.debug("<- InterruptedException While Waiting For Log Connection -> ");
				}
				try {
					if (!connectionlist.isEmpty()) {
						connection = (Connection) connectionlist.remove(0);
						break;
					}
				} catch (Exception ex) {
					logger.warn("", ex);
				}
			}
		}
		return connection;
	}

	public synchronized void releaseConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				logger.info(e.getMessage());
			}
			connectionlist.add(connection);
		}
	}

	public void closeConnections() {
		while (!connectionlist.isEmpty()) {
			Connection connection = (Connection) connectionlist.remove(0);
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
					logger.debug("Log Database Connection Close Error :" + ex);
				}
			}
		}
	}

	public void initialize() {
		Connection connection = null;
		for (int i = 0; i < MAX_CONNECTION; i++) {
			connection = createConnection();
			connectionlist.add(connection);
		}
		logger.info("Log Database Connection Count: " + connectionlist.size());
	}

	private Connection createConnection() {
		Connection connection = null;
		try {
			Class.forName(GlobalVars.JDBC_DRIVER);
			connection = DriverManager.getConnection(GlobalVars.LOG_DB_URL, GlobalVars.DB_USER, GlobalVars.DB_PASSWORD);
		} catch (Exception ex) {
			logger.error("Error In Creating Log Database Connection", ex);
		}
		return connection;
	}
}
