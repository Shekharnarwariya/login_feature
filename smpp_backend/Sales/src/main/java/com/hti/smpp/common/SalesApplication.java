package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main class to run the Sales Application using Spring Boot.
 */

@SpringBootApplication
@EnableDiscoveryClient
public class SalesApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesApplication.class, args);
	}

}

