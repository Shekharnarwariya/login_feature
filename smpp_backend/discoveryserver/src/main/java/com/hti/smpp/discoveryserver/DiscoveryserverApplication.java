package com.hti.smpp.discoveryserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@EnableEurekaServer
@SpringBootApplication
public class DiscoveryserverApplication {

	private final static Logger logger = LoggerFactory.getLogger(DiscoveryserverApplication.class);

	@PostConstruct
	public void postConstruct() {
		logger.info(" Discoveryserver Application is about to start.");
	}

	@PreDestroy
	public void preDestroy() {

		logger.info(" Discoveryserver Application is about to stop.");
	}

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryserverApplication.class, args);
		logger.info("Discoveryserver Application started successfully.");
	}

}