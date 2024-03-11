package com;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.hti.database.ConnectionPool;
import com.hti.thread.StartSmppServer;
import com.hti.util.Constants;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

@SpringBootApplication
public class HostSmppApplication {

	public static void main(String[] args) {
		SpringApplication.run(HostSmppApplication.class, args);
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.reset();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(loggerContext);
		InputStream configStream = null;
		try {
			configStream = FileUtils.openInputStream(new File("config//smppLogConfig.xml"));
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
		StartSmppServer smpp = new StartSmppServer();
		if (smpp.loadCofigration()) {
			try {
				GlobalCache.connnection_pool_1 = new ConnectionPool(1);
				GlobalCache.connnection_pool_1.initialize();
				GlobalCache.connnection_pool_2 = new ConnectionPool(2);
				GlobalCache.connnection_pool_2.initialize();
			} catch (Exception e) {
				//logger.error("main()", e.fillInStackTrace());
				e.fillInStackTrace();
			}
			smpp.initGlobalVars();
			if (GlobalVars.MASTER_CLIENT) {
				smpp.initializeDistVar();
			}
			smpp.ApplicationStart();
		} else {
			System.err.println("Please Check Your Configuration File ");
			Constants.APPLICATION_STATUS = false;
		}
	}

}
