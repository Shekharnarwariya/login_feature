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
 * The `TwowayApplication` class serves as the entry point for the Two-Way
 * Application.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class TwowayApplication {

    private final static Logger logger = LoggerFactory.getLogger(TwowayApplication.class);

    @PostConstruct
    public void postConstruct() {
        logger.info("Two-Way Application is about to start.");
    }

    @PreDestroy
    public void preDestroy() {
    	 GlobalVars.hazelInstance.shutdown();
        logger.info("Two-Way Application is about to stop.");
    }

    public static void main(String[] args) {
        SpringApplication.run(TwowayApplication.class, args);
        logger.info("Two-Way Application started successfully.");
    }
}
