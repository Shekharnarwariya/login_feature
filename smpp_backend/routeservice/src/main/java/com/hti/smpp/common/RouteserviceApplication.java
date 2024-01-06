package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.hti.smpp.common.util.GlobalVars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@EnableDiscoveryClient
@SpringBootApplication
/**
 * The main class for the Route Service application.
 */
public class RouteserviceApplication {

    private final static Logger logger = LoggerFactory.getLogger(RouteserviceApplication.class);

    @PostConstruct
    public void postConstruct() {
        logger.info("Route Service Application is about to start.");
    }

    @PreDestroy
    public void preDestroy() {
    	 GlobalVars.hazelInstance.shutdown();
        logger.info("Route Service Application is about to stop.");
    }

    public static void main(String[] args) {
        SpringApplication.run(RouteserviceApplication.class, args);
        logger.info("Route Service Application started successfully.");
    }
}
