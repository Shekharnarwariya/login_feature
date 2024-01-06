package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.hti.smpp.common.util.GlobalVars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@SpringBootApplication
@EnableDiscoveryClient
/**
 * Entry point for the Addressbook.
 */
public class AddressbookApplication {

    private final static Logger logger = LoggerFactory.getLogger(AddressbookApplication.class);

    @PostConstruct
    public void postConstruct() {
        logger.info("Addressbook Application is about to start.");
    }

    @PreDestroy
    public void preDestroy() {
      
        GlobalVars.hazelInstance.shutdown();
        logger.info("Addressbook Application is about to stop.");
    }

    public static void main(String[] args) {
        SpringApplication.run(AddressbookApplication.class, args);
        logger.info("Addressbook Application started successfully.");
    }
}
