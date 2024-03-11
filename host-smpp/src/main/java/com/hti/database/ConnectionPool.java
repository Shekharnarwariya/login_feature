package com.hti.database;
/*
 * ConnectionPool.java
 * Created on April 18, 2006, 2:30 PM by Ashish Jain
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.exception.DBException;
import com.hti.util.Constants;

/**
 *
 * @author Administrator
 */
public class ConnectionPool {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Vector<Connection> connections = null;
	private int pool_number;

	public ConnectionPool(int pool_number) {
		this.pool_number = pool_number;
		logger.info("Initializing Connection Pool[" + pool_number + "]");
	}

	/**
	 * closing the connections from the pool
	 */
	public synchronized void removeAllConnections() {
		if (connections != null) {
			while (!connections.isEmpty()) {
				Connection c = (Connection) connections.remove(0);
				try {
					c.close();
				} catch (SQLException ex) {
					logger.debug("Error In Close Connection[" + pool_number + "]: " + ex);
				}
			} // end of for loop
			connections = null;
		}
	}// end of the removeAllConnections()

	/**
	 * Creates a new maximum instance of Connection
	 */
	private Connection createConnection() {
		Connection conn = null;
		try {
			Class.forName(Constants.JDBC_DRIVER);
			conn = DriverManager.getConnection(Constants.DB_URL, Constants.DB_USER, Constants.DB_PASSWORD);
		} catch (Exception e) {
			logger.error("createConnection()", e);
		}
		return conn;
	}

	public synchronized void initialize() {
		if (connections == null) {
			connections = new Vector<Connection>();
			try {
				Class.forName(Constants.JDBC_DRIVER);
				int count = 0;
				while (count < Constants.MAX_CONNECTIONS) {
					Connection c = DriverManager.getConnection(Constants.DB_URL, Constants.DB_USER,
							Constants.DB_PASSWORD);
					connections.addElement(c);
					count++;
				}
				logger.info("Total Database Connections Created[" + pool_number + "] : " + count);
			} catch (Exception e) {
				logger.error("initialize()", e);
			}
		}
	}

	/**
	 * Checks out a connection from the pool. If no free connection is available, a new connection is created unless the max number of connections has been reached. If a free connection has been
	 * closed by the database, it's removed from the pool and this method is called again recursively.
	 */
	public synchronized Connection getConnection() throws DBException, SQLException {
		Connection con = null;
		boolean create = false;
		if (connections != null) {
			if (!connections.isEmpty()) {
				con = (Connection) connections.remove(0);
				if (con != null) {
					try {
						if (!con.isValid(0)) {
							logger.warn("<--- Invalid Database Connection Found[" + pool_number + "] ---> ");
							create = true;
						}
					} catch (SQLException ex) {
						create = true;
						logger.warn("<- SQLException While Validating Connection[" + pool_number + "] -> ");
					}
				}
				if (create) {
					logger.warn("<--- Creating Additional Connection. Pooled[" + pool_number + "]:-> "
							+ connections.size());
					con = createConnection();
				}
			} else {
				logger.warn("<--- No Pool Connection Found[" + pool_number + "] --->");
				while (true) {
					try {
						logger.info("<-- Waiting For Database Connection[" + pool_number + "] --> ");
						wait();
						logger.info("<-- Exit Waiting For Database Connection[" + pool_number + "] --> ");
					} catch (InterruptedException ex) {
						logger.debug("<- InterruptedException While Waiting For Connection[" + pool_number + "] -> ");
					}
					try {
						if (!connections.isEmpty()) {
							con = (Connection) connections.remove(0);
							break;
						}
					} catch (Exception ex) {
						logger.warn(ex.getMessage());
					}
				}
			}
			return con;
		} else {
			throw new SQLException("Connection Unavailable[" + pool_number + "]");
		}
	}// end of getConnection()

	/**
	 * put the instance of connection into the pool
	 */
	public synchronized void putConnection(Connection c) {
		if (c != null) {
			try {
				c.setAutoCommit(true);
			} catch (SQLException e) {
				logger.info(e.getMessage());
			}
			connections.addElement(c);
			notifyAll();
		}
	}

	public synchronized int size() {
		return connections.size();
	}
}
