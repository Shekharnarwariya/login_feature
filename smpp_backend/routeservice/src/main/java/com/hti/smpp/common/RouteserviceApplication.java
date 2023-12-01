package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class RouteserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RouteserviceApplication.class, args);
	}

}