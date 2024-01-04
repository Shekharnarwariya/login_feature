package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.hti.smpp.common.util.GlobalVars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Main class to run the Sales Application using Spring Boot.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SalesApplication {

    private final static Logger logger = LoggerFactory.getLogger(SalesApplication.class);

    @PostConstruct
    public void postConstruct() {
        logger.info("Sales Application is about to start.");
    }

    @PreDestroy
    public void preDestroy() {
    	 GlobalVars.hazelInstance.shutdown();
        logger.info("Sales Application is about to stop.");
    }

    public static void main(String[] args) {
        SpringApplication.run(SalesApplication.class, args);
        logger.info("Sales Application started successfully.");
    }
}
