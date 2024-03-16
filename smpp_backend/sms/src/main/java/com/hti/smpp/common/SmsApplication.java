package com.hti.smpp.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.hti.smpp.common.httpclient.ApiRequestProcessor;
import com.hti.smpp.common.util.GlobalVars;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
@ServletComponentScan
public class SmsApplication  {

	private final static Logger logger = LoggerFactory.getLogger(SmsApplication.class);

	@PostConstruct
	public void postConstruct() {
		logger.info("SMS Application is about to start.");
	}

	@PreDestroy
	public void preDestroy() {
		GlobalVars.hazelInstance.shutdown();
		logger.info("SMS Application is about to stop.");
	}

	public static void main(String[] args) {
		SpringApplication.run(SmsApplication.class, args);
		logger.info("SMS Application started successfully.");
		new ApiRequestProcessor();

	}
}
