package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
/**
 * Entry point for the Spring Boot application.
 */
public class BsfmApplication {

	public static void main(String[] args) {
		SpringApplication.run(BsfmApplication.class, args);
	}

}
