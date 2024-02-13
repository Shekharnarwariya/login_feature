package com.hti.smpp.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.hti.smpp.common.util.GlobalVars;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@SpringBootApplication
@EnableDiscoveryClient
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
