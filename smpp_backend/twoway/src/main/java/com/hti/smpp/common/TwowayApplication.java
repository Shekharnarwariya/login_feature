package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * The `TwowayApplication` class serves as the entry point for the Two-Way
 * Application.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class TwowayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TwowayApplication.class, args);
	}

}
