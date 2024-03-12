package com;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.hti.database.ConnectionPool;
import com.hti.database.LogDBConnection;
import com.hti.thread.StartUserServer;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

@SpringBootApplication
public class HostUserApplication {

public static void main(String[] args) {
		SpringApplication.run(HostUserApplication.class, args);
		System.out.println("<-- Staring User Server ---> ");
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.reset();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(loggerContext);
		InputStream configStream = null;
		try {
			configStream = FileUtils.openInputStream(new File("config//userLogConfig.xml"));
			configurator.doConfigure(configStream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				configStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		StartUserServer smpp = new StartUserServer();
		if (smpp.loadCofigration()) {
			try {
				GlobalCache.connection_pool_user = new ConnectionPool(1);
				GlobalCache.connection_pool_user.initialize();
				GlobalCache.connection_pool_proc = new ConnectionPool(2);
				GlobalCache.connection_pool_proc.initialize();
			} catch (Exception e) {
				e.printStackTrace();
			}
			GlobalCache.logConnectionPool = new LogDBConnection();
			GlobalCache.logConnectionPool.initialize();
			smpp.initGlobalVars();
			smpp.ApplicationStart();
		} else {
			System.err.println("<-- User Configuration File Error --> ");
			GlobalVars.APPLICATION_STATUS = false;
		}
	}
	
}
