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
 * This class is the main entry point for the Spring Boot application.
 */ 

@SpringBootApplication
@EnableDiscoveryClient
public class LoginServiceApplication {

    private final static Logger logger = LoggerFactory.getLogger(LoginServiceApplication.class);

    @PostConstruct
    public void postConstruct() {
        logger.info("Login Service Application is about to start.");
    }

    @PreDestroy
    public void preDestroy() {
    	 GlobalVars.hazelInstance.shutdown();
        logger.info("Login Service Application is about to stop.");
    }

    public static void main(String[] args) {
        SpringApplication.run(LoginServiceApplication.class, args);
        logger.info("Login Service Application started successfully.");
    }
}