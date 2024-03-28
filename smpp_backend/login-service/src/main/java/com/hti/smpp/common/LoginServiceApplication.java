package com.hti.smpp.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.SessionLogInsert;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Main entry point for the Login Service Spring Boot application. It includes
 * lifecycle methods for initialization and destruction, and enables service
 * discovery for microservices architecture.
 */
@SpringBootApplication // Marks this class as a Spring Boot application
@EnableDiscoveryClient // Enables service discovery for this application
public class LoginServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(LoginServiceApplication.class); // Logger instance

	/**
	 * Initializes resources or services needed by the application. This method is
	 * called after the bean is created and dependency injection is complete.
	 */
	@PostConstruct
	public void postConstruct() {
		// Logging initialization message
		logger.info("Login Service Application is about to start.");
	}

	/**
	 * Cleans up resources before the application is shut down. This method is
	 * called when the bean is about to be destroyed.
	 */
	@PreDestroy
	public void preDestroy() {
		// Shutting down Hazelcast instance
		GlobalVars.hazelInstance.shutdown();
		// Logging shutdown message
		logger.info("Login Service Application is about to stop.");
	}

	/**
	 * Main method serving as the entry point of the application.
	 * 
	 * @param args Command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		// Running the Spring Boot application
		SpringApplication.run(LoginServiceApplication.class, args);
		// Logging the successful start of the application
		new SessionLogInsert();
		logger.info("Login Service Application started successfully.");
	}
}
