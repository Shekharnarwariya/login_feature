package com.hti.smpp.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.hti.smpp.common.util.GlobalVars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class UserApplication {

    private final static Logger logger = LoggerFactory.getLogger(UserApplication.class);

    @PostConstruct
    public void postConstruct() {
        logger.info("User Application is about to start.");
    }

    @PreDestroy
    public void preDestroy() {
    	 GlobalVars.hazelInstance.shutdown();
        logger.info("User Application is about to stop.");
    }

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        logger.info("User Application started successfully.");
    }
}
