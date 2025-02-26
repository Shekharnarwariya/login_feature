package com.hti.smpp.common.httpclient;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConnectionService {

	private static final Logger log = LoggerFactory.getLogger(ConnectionService.class);

//	@Value("${spring.datasource.url}")
	private String databaseUrl = "jdbc:mysql://localhost:3306/host_brd?allowPublicKeyRetrieval=true&useSSL=false";

//	@Value("${spring.datasource.username}")
	private String username = "root";

	// @Value("${spring.datasource.password}")
	private String password = "root";

	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			log.error("MySQL Driver not found!", e);
		}
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(databaseUrl, username, password);
	}
}