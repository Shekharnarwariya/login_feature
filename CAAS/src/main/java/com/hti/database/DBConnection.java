package com.hti.database;

import java.sql.Connection;

public interface DBConnection {
	public Connection getConnection();

	public void putConnection(Connection connection);
	
	public void removeAllConnections();
}
