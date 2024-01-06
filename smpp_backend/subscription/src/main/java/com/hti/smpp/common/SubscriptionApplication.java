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
public class SubscriptionApplication {

    private final static Logger logger = LoggerFactory.getLogger(SubscriptionApplication.class);

    @PostConstruct
    public void postConstruct() {
        logger.info("Subscription Application is about to start.");
    }

    @PreDestroy
    public void preDestroy() {
    	 GlobalVars.hazelInstance.shutdown();
        logger.info("Subscription Application is about to stop.");
    }

    public static void main(String[] args) {
        SpringApplication.run(SubscriptionApplication.class, args);
        logger.info("Subscription Application started successfully.");
    }
}
