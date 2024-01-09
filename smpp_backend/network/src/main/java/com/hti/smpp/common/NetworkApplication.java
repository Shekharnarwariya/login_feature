package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.hti.smpp.common.util.GlobalVars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class NetworkApplication {

    private final static Logger logger = LoggerFactory.getLogger(NetworkApplication.class);

    @PostConstruct
    public void postConstruct() {
        logger.info("Network Application is about to start.");
    }

    @PreDestroy
    public void preDestroy() {
    	 GlobalVars.hazelInstance.shutdown();
        logger.info("Network Application is about to stop.");
    }

    public static void main(String[] args) {
        SpringApplication.run(NetworkApplication.class, args);
        logger.info("Network Application started successfully.");
    }
}