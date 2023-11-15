package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class SmscApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmscApplication.class, args);
	}

}
