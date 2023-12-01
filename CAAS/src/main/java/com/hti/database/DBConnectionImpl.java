package com.hti.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.GlobalVar;

public class DBConnectionImpl implements DBConnection {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");
	private static List<Connection> ConnectionPool = new ArrayList<Connection>();
	// private static int MAX_CONN = GlobalVar.MAX_CONNECTION;
	static {
		logger.info("Initializing Connection Pool");
		logger.info(GlobalVar.DATABASE_URL + "[" + GlobalVar.DATABASE_DRIVER + "] User: " + GlobalVar.DATABASE_USER
				+ " Pass: " + GlobalVar.DATABASE_PASSWORD);
		try {
			Class.forName(GlobalVar.DATABASE_DRIVER);
			for (int i = 0; i < GlobalVar.MAX_CONNECTION; i++) {
				Connection con = DriverManager.getConnection(GlobalVar.DATABASE_URL, GlobalVar.DATABASE_USER,
						GlobalVar.DATABASE_PASSWORD);
				ConnectionPool.add(con);
			}
		} catch (ClassNotFoundException | SQLException ex) {
			logger.error("Connection Pool Initialize Error: ", ex.fillInStackTrace());
		}
		logger.info("Total Connection Stored: " + ConnectionPool.size());
	}

	public synchronized Connection getConnection() {
		//logger.debug("getConnection(): " + ConnectionPool.size());
		boolean isCreate = false;
		Connection connection = null;
		if (!ConnectionPool.isEmpty()) {
			connection = (Connection) ConnectionPool.remove(0);
			if (connection != null) {
				try {
					if (!connection.isValid(0)) {
						logger.warn("<--- Invalid Database Connection Found  --->");
						isCreate = true;
					}
				} catch (SQLException ex) {
					isCreate = true;
					logger.warn("<- SQLException While Validating Connection -> ");
				}
			}
			if (isCreate) {
				logger.warn("<--- Creating Additional Connection. Pooled --> " + ConnectionPool.size());
				connection = createConnection();
			}
		} else {
			logger.warn("<--- No Pool Connection Found --->");
			while (true) {
				try {
					logger.info("<-- Waiting For Database Connection --> ");
					wait();
					logger.info("<-- Exit Waiting For Database Connection --> ");
				} catch (InterruptedException ex) {
					logger.debug("<- InterruptedException While Waiting For Connection -> ");
				}
				try {
					if (!ConnectionPool.isEmpty()) {
						connection = (Connection) ConnectionPool.remove(0);
						break;
					}
				} catch (Exception ex) {
					logger.warn("", ex);
				}
			}
		}
		return connection;
	}

	private Connection createConnection() {
		logger.debug("createConnection()");
		Connection connection = null;
		try {
			Class.forName(GlobalVar.DATABASE_DRIVER);
			connection = DriverManager.getConnection(GlobalVar.DATABASE_URL, GlobalVar.DATABASE_USER,
					GlobalVar.DATABASE_PASSWORD);
		} catch (ClassNotFoundException | SQLException ex) {
			logger.error("createConnection(): " + ex);
		}
		return connection;
	}

	/**
	 *
	 * @param connection
	 */
	public synchronized void putConnection(Connection connection) {
		try {
			if (connection != null) {
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					logger.info(e.getMessage());
				}
				ConnectionPool.add(connection);
				notifyAll();
			}
		} catch (Exception ex) {
			logger.error("putConnection(): " + ex);
		}
	}
	
	/**
	 * closing the connections from the pool
	 */
	public synchronized void removeAllConnections() {
		if (ConnectionPool != null) {
			while (!ConnectionPool.isEmpty()) {
				Connection c = (Connection) ConnectionPool.remove(0);
				try {
					c.close();
				} catch (SQLException ex) {
					logger.debug("Error In Close Connection: " + ex);
				}
			} // end of for loop
			ConnectionPool = null;
		}
	}
}
